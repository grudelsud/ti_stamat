package stamat.controller.visual;



import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.ImageSearchHits;
import net.semanticmetadata.lire.ImageSearcher;
import net.semanticmetadata.lire.ImageSearcherFactory;
import net.semanticmetadata.lire.impl.VisualWordsImageSearcher;

import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import stamat.main.Analyser.constants;
import stamat.util.StamatException;

public class Searcher {

	private static Logger logger = Logger.getLogger(Searcher.class.getName());

	private String indexPath;
	private int numberOfResults;

	/**
	 * @param indexPath
	 * @param numberOfResults
	 */
	public Searcher(String indexPath, int numberOfResults) {
		this.indexPath = indexPath;
		this.numberOfResults = numberOfResults;
	}

	public List<SearchResult> searchFromIndex(String fileIdentifier, String fieldName, int numberOfResults) throws StamatException
	{
		if(!constants.checkAllowedIndexFeature(fieldName)) {
			throw new StamatException("feature must be one of: " + Arrays.asList(stamat.main.Analyser.constants.featureArray).toString());
		}
		logger.info("searching index " + fieldName + " under " + indexPath);
		IndexReader ir;
		try {
			ir = IndexReader.open(FSDirectory.open(new File(indexPath, fieldName)));
		} catch (IOException e) {
			throw new StamatException("search from index ioe while opening index: " + e.getMessage());
		}
		
		// first search doc named after fileIdentifier
		IndexSearcher luceneSearcher = new IndexSearcher(ir);
		Query query = new TermQuery(new Term(DocumentBuilder.FIELD_NAME_IDENTIFIER, fileIdentifier));
		TopDocs rs;
		Document doc;
		try {
			rs = luceneSearcher.search(query, 1);
			doc = luceneSearcher.doc(rs.scoreDocs[0].doc);
		} catch (IOException e) {
			throw new StamatException("search from index ioe while finding doc: " + e.getMessage() + ". weird...");
		} finally {
			try {
				luceneSearcher.close();
			} catch (IOException e) {
				throw new StamatException("search from index ioe while closing lucene searcher");
			}
		}
		// then find all similar documents according to fieldName descriptor
		ImageSearcher lireSearcher = constants.getSearcherFromFieldName(fieldName, numberOfResults);
		ImageSearchHits hits;
		try {
			hits = lireSearcher.search(doc, ir);
		} catch (IOException e) {
			throw new StamatException("search from index ioe while executing lire");
		}
		return getSearchResultListFromHits(hits);
	}

	/**
	 * @see stamat.controller.visual.Indexer.updateIndex for complete list of documentbuilders
	 * 
	 * @param is
	 * @param fieldName
	 * @return
	 * @throws StamatException 
	 */
	public List<SearchResult> search(InputStream is, String fieldName, int numberOfResults) throws IOException, StamatException
	{
		BufferedImage img = ImageIO.read(is);
		IndexReader ir = IndexReader.open(FSDirectory.open(new File(indexPath, fieldName)));
	
		ImageSearcher searcher = constants.getSearcherFromFieldName(fieldName, numberOfResults);
		ImageSearchHits hits = searcher.search(img, ir);
		return getSearchResultListFromHits(hits);
	}

	/**
	 * @param URL
	 * @param fieldName
	 * @param numberOfResults
	 * @return
	 * @throws StamatException
	 * @throws IOException
	 */
	public List<SearchResult> searchFromUrl(String URL, String fieldName, int numberOfResults) throws StamatException, IOException
	{
		logger.info("searching " + fieldName + " under " + indexPath + " from url " + URL);
		InputStream is = (new java.net.URL(URL)).openStream();
		return search(is, fieldName, numberOfResults);		
	}

	/**
	 * @param imagePath
	 * @param fieldName
	 * @param numberOfResults
	 * @return
	 * @throws StamatException
	 * @throws IOException
	 */
	public List<SearchResult> searchFromPath(String imagePath, String fieldName, int numberOfResults) throws StamatException, IOException
	{
		logger.info("searching " + fieldName + " under " + indexPath + " from path " + imagePath);
		FileInputStream fis = new FileInputStream(imagePath);
		return search(fis, fieldName, numberOfResults);
	}

	/**
	 * @param hits
	 * @return
	 */
	private List<SearchResult> getSearchResultListFromHits(ImageSearchHits hits) {
		List<SearchResult> results = new ArrayList<SearchResult>();

		int limit = numberOfResults < hits.length() ? numberOfResults : hits.length();
		for (int i = 0; i < limit; i++) {
			Document doc = hits.doc(i);
			String fileName = doc.getFieldable(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue();
			String url = doc.getFieldable(constants.INDEX_URL).stringValue();
			results.add(new SearchResult(fileName,url, i,hits.score(i)));
		}
		return results;
	}
	/**
	 * @param isImageQuery
	 * @return
	 * @throws CorruptIndexException
	 * @throws IOException
	 */
	@Deprecated
	public List<SearchResult> searcherCEDD(InputStream isImageQuery) throws IOException {
		BufferedImage img = ImageIO.read(isImageQuery);
		IndexReader ir = IndexReader.open(FSDirectory.open(new File(indexPath)));
		ImageSearcher searcher = ImageSearcherFactory.createCEDDImageSearcher(10);
	
		ImageSearchHits hits = searcher.search(img, ir);
		List<SearchResult> results = new ArrayList<SearchResult>();
	
		for (int i = 0; i < numberOfResults; i++) {
			Document doc = hits.doc(i);
			String fileName = doc.getFieldable(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue();
			String url = doc.getFieldable(constants.INDEX_URL).stringValue();
			results.add(new SearchResult(fileName,url, i,hits.score(i)));
		}
		return results;
	}

	/**
	 * @param URL
	 * @return
	 * @throws CorruptIndexException
	 * @throws IOException
	 */
	@Deprecated
	public List<SearchResult> searcherCEDDfromUrl(String URL) throws IOException {
		InputStream is = (new java.net.URL(URL)).openStream();
		return searcherCEDD(is);
	}

	/**
	 * @param imagePath
	 * @return
	 * @throws CorruptIndexException
	 * @throws IOException
	 */
	@Deprecated
	public List<SearchResult> searcherCEDDfromPath(String imagePath) throws IOException {
		FileInputStream fis = new FileInputStream(imagePath);
		return searcherCEDD(fis);
	}

	/**
	 * @param queryFile
	 * @return
	 * @throws CorruptIndexException
	 * @throws IOException
	 */
	@Deprecated
	public List<SearchResult> searcherSift(File queryFile) throws CorruptIndexException, IOException{

		List<SearchResult> results = new ArrayList<SearchResult>();

		IndexReader reader = IndexReader.open(FSDirectory.open(new File(indexPath)));
		String queryFileName = queryFile.getAbsolutePath();
		Query qFile;
		try {
			qFile = new QueryParser(Version.LUCENE_34, "descriptorImageIdentifier", new KeywordAnalyzer()).parse(queryFileName);
			IndexSearcher searcherIndexSift = new IndexSearcher(reader);
			int hitsPerPage = 1;
			TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
			searcherIndexSift.search(qFile, collector);
			ScoreDoc[] hitsIndex = collector.topDocs().scoreDocs;
			int docId = hitsIndex[0].doc;
			Document query = searcherIndexSift.doc(docId);
			System.out.println((1) + ". " + query.get("descriptorImageIdentifier"));

			VisualWordsImageSearcher searcher = new VisualWordsImageSearcher(numberOfResults, 
					DocumentBuilder.FIELD_NAME_SIFT_LOCAL_FEATURE_HISTOGRAM_VISUAL_WORDS);
			ImageSearchHits hits = searcher.search(query, reader);
			// show or analyze your results ....

			for (int i = 0; i < numberOfResults; i++) {
				Document doc = hits.doc(i);
				String fileName = doc.getFieldable(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue();
				String url = doc.getFieldable(constants.INDEX_URL).stringValue();
				results.add(new SearchResult(fileName,url, i,hits.score(i)));
			}

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return results;

	}
}
