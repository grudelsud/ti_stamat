package stamat.controller.Ranking;



import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import stamat.util.StamatException;


public class NewsTweets {

	private static String FIELD_KEY = "key";
	private static String FIELD_VAL = "value";

	private static Logger logger = Logger.getLogger(NewsTweets.class.getName());

	private NewsTweets() {
	}

	private static Directory createIndex(Map<String, String> content) throws IOException
	{
		//create tweets index
		StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
		Directory index = new RAMDirectory();
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36, analyzer);
		IndexWriter w = new IndexWriter(index, config);

		for (Entry<String, String> entry : content.entrySet()) 
		{
			String key = entry.getKey();
			String val = entry.getValue();
			Document doc = new Document();
			doc.add(new Field(FIELD_KEY, key, Field.Store.YES, Field.Index.NOT_ANALYZED));
			doc.add(new Field(FIELD_VAL, val, Field.Store.YES, Field.Index.ANALYZED));
			w.addDocument(doc);
		}
		w.close();
		return index;
	}

	private static Directory createIndex(Map<String, String> content, Map<String, Float> contentBoost) throws IOException
	{
		//create tweets index
		StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
		Directory index = new RAMDirectory();
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36, analyzer);
		IndexWriter w = new IndexWriter(index, config);

		for (Entry<String, String> entry : content.entrySet()) 
		{
			String key = entry.getKey();
			String val = entry.getValue();
			Document doc = new Document();
			doc.add(new Field(FIELD_KEY, key, Field.Store.YES, Field.Index.NOT_ANALYZED));
			doc.add(new Field(FIELD_VAL, val, Field.Store.YES, Field.Index.ANALYZED));

			Float boost = contentBoost.get(key);
			if (boost != null)
			{
				doc.setBoost(boost);
			}
			w.addDocument(doc);
		}
		w.close();
		return index;
	}

	public static Map<String, Float> rankNews(Map<String,String> news, Map<String,String> tweets) throws IOException, StamatException {
		return rankCorpus(news, tweets);
	}

	public static Map<String, Float> rankNews(Map<String,String> news, Map<String,String> tweets, Map<String,Float> tweetBoosts) throws IOException, StamatException 
	{
		return rankCorpus(news, tweets, tweetBoosts);
	}

	public static Map<String, Float> rankTweets(Map<String,String> tweets, Map<String,String> news) throws IOException, StamatException 
	{
		return rankCorpus(tweets, news);
	}

	public static Map<String, Float> rankCorpus(Map<String,String> corpus, Directory refereesIndex) throws IOException, StamatException
	{
		StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);

		HashMap<String,Float> mScore = new HashMap<String,Float>();
		ValueComparator bvc =  new ValueComparator(mScore);

		for (Entry<String, String> entry : corpus.entrySet()) 
		{
			String key = entry.getKey();
			String val = entry.getValue();
			try 
			{
				mScore.put(key, new Float(computeRelevance(val, refereesIndex, analyzer)));
			} 
			catch (ParseException e) 
			{
				throw new StamatException("Parse exception while computing news item " + key);
			}
		}

		TreeMap<String,Float> sortedScore = new TreeMap<String,Float>(bvc);
		sortedScore.putAll(mScore);
		return sortedScore;		
	}

	public static Map<String, Float> rankCorpus(Map<String,String> corpus, Map<String,String> referees) throws IOException, StamatException
	{
		Directory refereesIndex = createIndex(referees);
		return rankCorpus(corpus, refereesIndex);
	}

	public static Map<String, Float> rankCorpus(Map<String,String> corpus, Map<String,String> referees, Map<String,Float> refereeBoosts) throws IOException, StamatException
	{
		Directory refereesIndex = createIndex(referees, refereeBoosts);
		return rankCorpus(corpus, refereesIndex);		
	}

	private static class ValueComparator implements Comparator<String> 
	{
		private Map<String, Float> base;

		public ValueComparator(Map<String, Float> base) 
		{
			this.base = base;
		}

		// Note: this comparator imposes orderings that are inconsistent with equals.
		public int compare(String a, String b) 
		{
			if (base.get(a) >= base.get(b)) 
			{
				return -1;
			} 
			else 
			{
				return 1;
			}
		}
	}

	private static float computeRelevance(String source, Directory index, StandardAnalyzer analyzer) throws IOException, ParseException 
	{
		// the "title" arg specifies the default field to use when no field is explicitly specified in the query.
		Query q = new QueryParser(Version.LUCENE_36, FIELD_VAL, analyzer).parse(source);

		int hitsPerPage = 10;
		IndexReader reader = IndexReader.open(index);
		IndexSearcher searcher = new IndexSearcher(reader);
		TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
		searcher.search(q, collector);
		ScoreDoc[] hits = collector.topDocs().scoreDocs;

		logger.info("Found " + hits.length + " hits.");

		float totScore=0;
		for (int i = 0; i < hits.length; ++i) 
		{
			totScore += hits[i].score;
			// Document d = searcher.doc(hits[i].doc);
		}

		// searcher can only be closed when there is no need to access the documents any more. 
		searcher.close();
		return totScore;
	}

	public static Map<String,String> createTestTweets()
	{
		Map<String,String> mp=new HashMap<String, String>();

		// adding or set elements in Map by put method key and value pair
		mp.put("2", "Ordini un sushi online... ed è già qui. Come? Con la nuova rete veloce #4G di Vodafone #Vodafone4G http://youtu.be/bUgF2pSYI3c  http://4g.vodafone.it ");
		mp.put("1", "Temporeggiano le #borse europee in attesa dei dati #USA: sussidi di disoccupazione; ordini dei beni durevoli e vendite di immobili in corso.");
		mp.put("3", "Cresce l’#eCommerce B2C in Italia con un tasso del 19% http://www.netpropaganda.net/cresce-ecommerce-b2c-1025.html … #shopping #online #web");
		mp.put("4", "Speriamo che la vostra su http://www.adviseonly.com  non sia tra queste: Le #password più usate su #internet. http://huff.to/QHHG9Z  #internet #web");
		mp.put("5", "Da oggi LTE è una realtà! La rivoluzione che stavamo aspettando è arrivata con TIM #TimLte #Tim4G");
		return mp;
	}

	public static Map<String,String> createTestNews()
	{
		Map<String,String> mp=new HashMap<String, String>();

		// adding or set elements in Map by put method key and value pair
		mp.put("2", "Two cani");
		mp.put("1", "Come si mangia il sushi");
		mp.put("3", "Three topi cani Three");
		mp.put("4", "Four elefanti");
		mp.put("5", "Cinque canguri");
		return mp;
	}

}
