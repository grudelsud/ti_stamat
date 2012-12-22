package stamat.main;

import it.unifi.micc.homer.model.AsciiTextDocument;
import it.unifi.micc.homer.util.WordCounter;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.DocumentBuilderFactory;
import net.semanticmetadata.lire.ImageSearcher;
import net.semanticmetadata.lire.ImageSearcherFactory;
import net.semanticmetadata.lire.impl.SiftDocumentBuilder;
import net.semanticmetadata.lire.impl.SurfDocumentBuilder;
import net.semanticmetadata.lire.impl.VisualWordsImageSearcher;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.store.LockObtainFailedException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import stamat.controller.ned.AnnieNERecognizer;
import stamat.controller.ned.StanfordNERecognizer;
import stamat.controller.topic.TopicDetector;
import stamat.controller.visual.Indexer;
import stamat.controller.visual.SearchResult;
import stamat.controller.visual.Searcher;
import stamat.model.KeywordType;
import stamat.model.NamedEntity;
import stamat.model.SemanticKeyword;
import stamat.model.Topic;
import stamat.model.TopicWord;
import stamat.util.StamatException;
import stamat.controller.Ranking.NewsTweets;

/**
 * 
 * @author alisi
 *
 */
public class Analyser {
	private static Logger logger = Logger.getLogger(Analyser.class.getName());

	public static class constants {
		public static final int SUCCESS = 0;
		public static final int ERROR = 1;
		
		public static final String SEARCH_URL = constants.INDEX_URL;
		public static final String SEARCH_INDEX = "index";
		
		public static final String INDEX_URL = "url";

		public static final String[] featureArray = {
			DocumentBuilder.FIELD_NAME_AUTOCOLORCORRELOGRAM,
			DocumentBuilder.FIELD_NAME_SCALABLECOLOR,
			DocumentBuilder.FIELD_NAME_CEDD,
			DocumentBuilder.FIELD_NAME_COLORHISTOGRAM,
			DocumentBuilder.FIELD_NAME_COLORLAYOUT,
			DocumentBuilder.FIELD_NAME_TAMURA,
			DocumentBuilder.FIELD_NAME_EDGEHISTOGRAM,
			DocumentBuilder.FIELD_NAME_FCTH,
			DocumentBuilder.FIELD_NAME_GABOR,
			DocumentBuilder.FIELD_NAME_JCD,
			DocumentBuilder.FIELD_NAME_JPEGCOEFFS,
			DocumentBuilder.FIELD_NAME_SIFT,
			DocumentBuilder.FIELD_NAME_SURF
		};

		public static boolean checkAllowedIndexFeature(String feature)
		{
			if( Arrays.asList(featureArray).contains(feature) ) {
				return true;
			} else {
				return false;
			}
		}

		public static DocumentBuilder getDocBuilderFromFieldName(String fieldName) throws StamatException
		{
			DocumentBuilder docBuilder;
			if( fieldName.equals(DocumentBuilder.FIELD_NAME_AUTOCOLORCORRELOGRAM)) {
				docBuilder = DocumentBuilderFactory.getAutoColorCorrelogramDocumentBuilder();
			} else if( fieldName.equals(DocumentBuilder.FIELD_NAME_SCALABLECOLOR) ) {
				docBuilder = DocumentBuilderFactory.getScalableColorBuilder();
			} else if( fieldName.equals(DocumentBuilder.FIELD_NAME_CEDD) ) {
				docBuilder = DocumentBuilderFactory.getCEDDDocumentBuilder();
			} else if( fieldName.equals(DocumentBuilder.FIELD_NAME_COLORHISTOGRAM) ) {
				docBuilder = DocumentBuilderFactory.getColorHistogramDocumentBuilder();
			} else if( fieldName.equals(DocumentBuilder.FIELD_NAME_COLORLAYOUT) ) {
				docBuilder = DocumentBuilderFactory.getColorLayoutBuilder();
			} else if( fieldName.equals(DocumentBuilder.FIELD_NAME_TAMURA) ) {
				docBuilder = DocumentBuilderFactory.getTamuraDocumentBuilder();
			} else if( fieldName.equals(DocumentBuilder.FIELD_NAME_EDGEHISTOGRAM) ) {
				docBuilder = DocumentBuilderFactory.getEdgeHistogramBuilder();
			} else if( fieldName.equals(DocumentBuilder.FIELD_NAME_FCTH) ) {
				docBuilder = DocumentBuilderFactory.getFCTHDocumentBuilder();
			} else if( fieldName.equals(DocumentBuilder.FIELD_NAME_GABOR) ) {
				docBuilder = DocumentBuilderFactory.getGaborDocumentBuilder();
			} else if( fieldName.equals(DocumentBuilder.FIELD_NAME_JCD) ) {
				docBuilder = DocumentBuilderFactory.getJCDDocumentBuilder();
			} else if( fieldName.equals(DocumentBuilder.FIELD_NAME_JPEGCOEFFS) ) {
				docBuilder = DocumentBuilderFactory.getJpegCoefficientHistogramDocumentBuilder();
			} else if( fieldName.equals(DocumentBuilder.FIELD_NAME_SIFT) ) {
				docBuilder = new SiftDocumentBuilder();
			} else if( fieldName.equals(DocumentBuilder.FIELD_NAME_SURF) ) {
				docBuilder = new SurfDocumentBuilder();
			} else {
				throw new StamatException("wrong index field");
			}
			return docBuilder;
		}
		/**
		 * @param fieldName
		 * @param numberOfResults
		 * @return
		 * @throws StamatException
		 */
		public static ImageSearcher getSearcherFromFieldName(String fieldName, int numberOfResults) throws StamatException
		{
			ImageSearcher searcher;
			if( fieldName.equals(DocumentBuilder.FIELD_NAME_AUTOCOLORCORRELOGRAM)) {
				searcher = ImageSearcherFactory.createAutoColorCorrelogramImageSearcher(numberOfResults);
			} else if( fieldName.equals(DocumentBuilder.FIELD_NAME_SCALABLECOLOR) ) {
				searcher = ImageSearcherFactory.createScalableColorImageSearcher(numberOfResults);
			} else if( fieldName.equals(DocumentBuilder.FIELD_NAME_CEDD) ) {
				searcher = ImageSearcherFactory.createCEDDImageSearcher(numberOfResults);
			} else if( fieldName.equals(DocumentBuilder.FIELD_NAME_COLORHISTOGRAM) ) {
				searcher = ImageSearcherFactory.createColorHistogramImageSearcher(numberOfResults);
			} else if( fieldName.equals(DocumentBuilder.FIELD_NAME_COLORLAYOUT) ) {
				searcher = ImageSearcherFactory.createColorLayoutImageSearcher(numberOfResults);
			} else if( fieldName.equals(DocumentBuilder.FIELD_NAME_TAMURA) ) {
				searcher = ImageSearcherFactory.createTamuraImageSearcher(numberOfResults);
			} else if( fieldName.equals(DocumentBuilder.FIELD_NAME_EDGEHISTOGRAM) ) {
				searcher = ImageSearcherFactory.createEdgeHistogramImageSearcher(numberOfResults);
			} else if( fieldName.equals(DocumentBuilder.FIELD_NAME_FCTH) ) {
				searcher = ImageSearcherFactory.createFCTHImageSearcher(numberOfResults);
			} else if( fieldName.equals(DocumentBuilder.FIELD_NAME_GABOR) ) {
				searcher = ImageSearcherFactory.createGaborImageSearcher(numberOfResults);
			} else if( fieldName.equals(DocumentBuilder.FIELD_NAME_JCD) ) {
				searcher = ImageSearcherFactory.createJCDImageSearcher(numberOfResults);
			} else if( fieldName.equals(DocumentBuilder.FIELD_NAME_JPEGCOEFFS) ) {
				searcher = ImageSearcherFactory.createJpegCoefficientHistogramImageSearcher(numberOfResults);
			} else if( fieldName.equals(DocumentBuilder.FIELD_NAME_SIFT) ) {
				searcher = new VisualWordsImageSearcher(numberOfResults, DocumentBuilder.FIELD_NAME_SIFT_LOCAL_FEATURE_HISTOGRAM_VISUAL_WORDS);
			} else if( fieldName.equals(DocumentBuilder.FIELD_NAME_SURF) ) {
				searcher = new VisualWordsImageSearcher(numberOfResults, DocumentBuilder.FIELD_NAME_SURF_LOCAL_FEATURE_HISTOGRAM_VISUAL_WORDS);
			} else {
				throw new StamatException("wrong index field");
			}
			return searcher;
		}
	}

	public static class ned {
		
		/**
		 * @param text
		 * @param classifierPath
		 * @return
		 */
		public static ArrayList<NamedEntity> extractStanford(String text, String classifierPath)
		{
			StanfordNERecognizer ner = StanfordNERecognizer.getInstance(classifierPath);
			ArrayList<NamedEntity> entityList = ner.extractEntity(text, null);
			return evalTFs(text, entityList);
		}

		/**
		 * @param text
		 * @param classifierPath
		 * @return
		 */
		public static String extractStanford2XML(String text, String classifierPath)
		{
			StanfordNERecognizer ner = StanfordNERecognizer.getInstance(classifierPath);
			String result = ner.extractEntity2XML(text, null);
			return result;
		}

		/**
		 * @param text
		 * @param classifierPath
		 * @return
		 */
		public static JSONObject extractStanford2JSON(String text, String classifierPath)
		{
			JSONObject result = new JSONObject();
			try {
				JSONArray entities = Analyser.semanticKeywordList2JSON(Analyser.ned.extractStanford(text, classifierPath));
				result.put("success", entities);
			} catch (JSONException e) {
				logger.log(Level.WARNING, e.getMessage());
			}
			return result;
		}

		/**
		 * -Dgate.plugins.home=GATE_HOME must be defined on command line
		 * 
		 * @param text
		 * @param keywordTypes
		 * @param gateHome
		 * @return
		 */
		public static ArrayList<NamedEntity> exractAnnie(String text)
		{
			AnnieNERecognizer ned = AnnieNERecognizer.getInstance();
			ArrayList<KeywordType> keywordTypes = new ArrayList<KeywordType>();
			keywordTypes.add(KeywordType.LOCATION);
			keywordTypes.add(KeywordType.PERSON);
			keywordTypes.add(KeywordType.ORGANIZATION);
			ArrayList<NamedEntity> entityList = ned.extractEntity(text, keywordTypes);	//returns entities without repetitions		
			return evalTFs(text, entityList);
		}

		/**
		 * @param text
		 * @param gateHomePath
		 * @return
		 */
		public static ArrayList<NamedEntity> exractAnnie(String text, String gateHomePath)
		{
			AnnieNERecognizer ned = AnnieNERecognizer.getInstance(gateHomePath);
			ArrayList<KeywordType> keywordTypes = new ArrayList<KeywordType>();
			keywordTypes.add(KeywordType.LOCATION);
			keywordTypes.add(KeywordType.PERSON);
			keywordTypes.add(KeywordType.ORGANIZATION);
			ArrayList<NamedEntity> entityList = ned.extractEntity(text, keywordTypes);	//returns entities without repetitions		
			return evalTFs(text, entityList);
		}

		/**
		 * @param text
		 * @param keywordTypes
		 * @param gateHome
		 * @return
		 */
		public static JSONObject exractAnnie2JSON(String text)
		{
			JSONObject result = new JSONObject();
			try {
				JSONArray entities = Analyser.semanticKeywordList2JSON(Analyser.ned.exractAnnie(text));
				result.put("success", entities);
			} catch (JSONException e) {
				logger.log(Level.WARNING, e.getMessage());
			}
			return result;
		}
	}


	public static class topic {
		/**
		 * @param texts
		 * @param numTopics
		 * @param numTopWords
		 * @param langModelsPath
		 * @param langStopwordPath
		 * @return
		 * @throws Exception
		 */
		public static Vector<SemanticKeyword> extract(List<String> texts, int numTopics, int numTopWords, String langModelsPath, String langStopwordPath)
		{
			Vector<SemanticKeyword> semanticKeywordVector = null;
			try {
				semanticKeywordVector = TopicDetector.extract(texts, langModelsPath, langStopwordPath, numTopics, numTopWords);
			} catch (StamatException e) {
				logger.log(Level.SEVERE, e.getMessage());
			}
			return semanticKeywordVector;
		}

		/**
		 * @param texts
		 * @param numTopics
		 * @param numTopWords
		 * @param langModelsPath
		 * @param langStopwordPath
		 * @return
		 */
		public static JSONObject extract2JSON(List<String> texts, int numTopics, int numTopWords, String langModelsPath, String langStopwordPath)
		{
			JSONObject result = new JSONObject();
			try {
				JSONArray keywords = Analyser.semanticKeywordList2JSON(Analyser.topic.extract(texts, numTopics, numTopWords, langModelsPath, langStopwordPath));
				result.put("success", keywords);
			} catch (JSONException e) {
				logger.log(Level.WARNING, e.getMessage());
			}
			return result;
		}

		/**
		 * @param texts
		 * @param numTopics
		 * @param numTopWords
		 * @param langModelsPath
		 * @param langStopwordPath
		 * @param ldaModelPath
		 * @return
		 * @throws Exception
		 */
		public static Vector<SemanticKeyword> extract(List<String> texts, int numTopics, int numTopWords, String langModelsPath, String langStopwordPath, String ldaModelPath) 
		{
			Vector<SemanticKeyword> semanticKeywordVector = null;
			try {
				semanticKeywordVector = TopicDetector.extract(texts, langModelsPath, langStopwordPath, numTopics, numTopWords, ldaModelPath);
			} catch (StamatException e) {
				logger.log(Level.SEVERE, e.getMessage());
			}
			return semanticKeywordVector;
		}

		/**
		 * @param texts
		 * @param numTopics
		 * @param numTopWords
		 * @param langModelsPath
		 * @param langStopwordPath
		 * @param ldaModelPath
		 * @return
		 */
		public static JSONObject extract2JSON(List<String> texts, int numTopics, int numTopWords, String langModelsPath, String langStopwordPath, String ldaModelPath) 
		{
			JSONObject result = new JSONObject();
			try {
				JSONArray keywords = Analyser.semanticKeywordList2JSON(Analyser.topic.extract(texts, numTopics, numTopWords, langModelsPath, langStopwordPath, ldaModelPath));
				result.put("success", keywords);
			} catch (JSONException e) {
				logger.log(Level.WARNING, e.getMessage());
			}
			return result;
		}

		/**
		 * @param texts
		 * @param langModelsPath
		 * @param langStopwordPath
		 * @param numTopWords
		 * @param ldaModelPath
		 * @return
		 * @throws Exception
		 */
		public static List<Topic> infer(List<String> texts, String langModelsPath, String langStopwordPath, int numTopWords, String ldaModelPath)
		{
			List<Topic> topics = null;
			try {
				topics = TopicDetector.infer(texts, langModelsPath, langStopwordPath, numTopWords, ldaModelPath);
			} catch (StamatException e) {
				logger.log(Level.SEVERE, e.getMessage());
			}
			return topics;
		}

		/**
		 * @param texts
		 * @param langModelsPath
		 * @param langStopwordPath
		 * @param numTopWords
		 * @param ldaModelPath
		 * @return
		 */
		public static JSONObject infer2JSON(List<String> texts, String langModelsPath, String langStopwordPath, int numTopWords, String ldaModelPath)
		{
			JSONObject result = new JSONObject();
			try {
				JSONArray topicsJSON = Analyser.topicList2JSON(Analyser.topic.infer(texts, langModelsPath, langStopwordPath, numTopWords, ldaModelPath));
				result.put("success", topicsJSON);
			} catch(JSONException e) {
				logger.log(Level.WARNING, e.getMessage());
			}
			return result;
		}

		/**
		 * @param trainingTexts
		 * @param numTopics
		 * @param langModelsPath
		 * @param langStopwordPath
		 * @param ldaModelPath
		 * @return
		 */
		public static JSONObject trainModel2JSON(List<String> trainingTexts, int numTopics, String langModelsPath, String langStopwordPath, String ldaModelPath) 
		{
			JSONObject result = new JSONObject();
			try {
				TopicDetector.train(trainingTexts, numTopics, langModelsPath, langStopwordPath, ldaModelPath);
				result.put("success", "all good");
			} catch (JSONException e) {
				logger.log(Level.WARNING, e.getMessage());
			} catch (IOException e) {
				logger.log(Level.SEVERE, e.getMessage());
			}
			return result;
		}
	}


	public static class language {
		
		/**
		 * @param text
		 * @param langModelsPath
		 * @param langStopwordPath
		 * @return
		 * @throws Exception
		 */
		public static Vector<SemanticKeyword> detection(String text, String langModelsPath, String langStopwordPath)
		{
			AsciiTextDocument textDocument = new AsciiTextDocument(text);
			Vector<SemanticKeyword> semanticKeywordVector = new Vector<SemanticKeyword>();
			try {
				textDocument.autoSetLanguage(langModelsPath, langStopwordPath);
			} catch (StamatException e) {
				logger.log(Level.WARNING, e.getMessage());
			}
			SemanticKeyword sw = new SemanticKeyword(textDocument.getLanguage().toString(), (float)1.0, KeywordType.LANGUAGE, 1.0);
			semanticKeywordVector.add(sw);
			return semanticKeywordVector;
		}

		/**
		 * @param text
		 * @param langModelsPath
		 * @param langStopwordPath
		 * @return
		 */
		public static JSONObject detection2JSON(String text, String langModelsPath, String langStopwordPath) 
		{
			JSONObject result = new JSONObject();
			try {
				JSONArray keywords = Analyser.semanticKeywordList2JSON(Analyser.language.detection(text, langModelsPath, langStopwordPath));
				result.put("success", keywords);
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
			return result;
		}
	}
	
	public static class ranking 
	{
		public static Map<String, Float> news(Map<String, String> news, Map<String, String> tweets) 
		{
			Map<String, Float> result = null;
			try {
				result = NewsTweets.rankNews(news, tweets);
			} catch (IOException e) {
				logger.warning(e.getMessage());
			} catch (StamatException e) {
				logger.warning(e.getMessage());
			}
			return result;
		}

		public static Map<String, Float> news(Map<String, String> news, Map<String, String> tweets, Map<String, Float> tweetBoosts) 
		{
			Map<String, Float> result = null;
			try {
				result = NewsTweets.rankNews(news, tweets, tweetBoosts);
			} catch (IOException e) {
				logger.warning(e.getMessage());
			} catch (StamatException e) {
				logger.warning(e.getMessage());
			}
			return result;
			
		}

		public static Map<String, Float> tweets(Map<String, String> tweets, Map<String, String> news) 
		{
			Map<String, Float> result = null;
			try {
				result = NewsTweets.rankNews(tweets, news);
			} catch (IOException e) {
				logger.warning(e.getMessage());
			} catch (StamatException e) {
				logger.warning(e.getMessage());
			}
			return result;
		}
	}
	
	/**
	 * class visual
	 * performs analysis on images and retrieves a list of similar images
	 * 
	 * commento beppe: righe di codice da riutilizzare
	 * List<SearchResult> currentResultSCD = searchSCD.search(new File("ucid.v2-png/" + query));
	 * List<SearchResult> currentResultCLD = searchCLD.search(new File("ucid.v2-png/" + query));
	 * List<SearchResult> currentResultEHD = searchEHD.search(new File("ucid.v2-png/" + query));
	 * 
	 * SEARCHRESULT NOW HAS AN ADDITIONAL FIELD! PLEASE CHECK DEPRECATED CONSTRUCTOR!
	 * 
	 * RankFusion rankFusion = new RankFusion(currentResultSCD, currentResultCLD, currentResultEHD);
	 * List<SearchResult> mergedWithBorda = rankFusion.mergeWithBORDACount();
	 * List<SearchResult> mergedWithRankProduct = rankFusion.mergeWithRankProduct();
	 * List<SearchResult> mergedWithInvertedRankPosition = rankFusion.mergeWithInvertedRankPosition();
	 *
	 */
	public static class visual {

		/**
		 * utility method, created to split indices where all the lire descriptors are merged together. use it once, then forget it
		 * 
		 * @param indexPath
		 * @throws IOException
		 */
		public static void splitIndex(String indexPath) throws IOException
		{
			Indexer indexing = new Indexer(indexPath);
			indexing.splitFeatures();
		}

		/**
		 * really useless. split indices should be used now.
		 * 
		 * @param indexPath
		 */
		@Deprecated
		public static int createEmptyIndex(String indexPath, StringBuilder message)
		{
			message = message == null ? new StringBuilder() : message;
			Indexer indexing = new Indexer(indexPath);
			try {
				indexing.createEmptyIndex();
				message.append("index created: " + indexPath);
				logger.log(Level.INFO, "index created: " + indexPath);
				return constants.SUCCESS;
			} catch (CorruptIndexException e) {
				logger.log(Level.SEVERE, e.getMessage());
				message.append(e.getMessage());
			} catch (LockObtainFailedException e) {
				logger.log(Level.SEVERE, e.getMessage());
				message.append(e.getMessage());
			} catch (IOException e) {
				logger.log(Level.SEVERE, e.getMessage());
				message.append(e.getMessage());
			}
			return constants.ERROR;
		}

		/**
		 * @param indexPath
		 * @param URL
		 * @param feature one of DocumentBuilder.FIELD_NAME
		 * @param numberOfResults
		 * @return
		 */
		public static List<SearchResult> searchFromUrl(String indexPath, String URL, String feature, int numberOfResults)
		{
			Searcher searcher = new Searcher(indexPath, numberOfResults);
			try {
				return searcher.searchFromUrl(URL, feature, numberOfResults);
			} catch (StamatException e) {
				logger.log(Level.SEVERE, "searchFromIndex se: " + e.getMessage());
			} catch (IOException e) {
				logger.log(Level.SEVERE, "searchFromIndex ioe: " + e.getMessage());
			}
			return null;
		}

		/**
		 * @param indexPath
		 * @param fileIdentifier
		 * @param feature
		 * @param numberOfResults
		 * @return
		 */
		public static List<SearchResult> searchFromIndex(String indexPath, String fileIdentifier, String feature, int numberOfResults)
		{
			Searcher searcher = new Searcher(indexPath, numberOfResults);
			try {
				return searcher.searchFromIndex(fileIdentifier, feature, numberOfResults);
			} catch (StamatException e) {
				logger.log(Level.SEVERE, "searchFromIndex se: " + e.getMessage());
			}
			return null;
		}
		
		/**
		 * @param indexPath
		 * @param URL
		 * @param indexedFields
		 * @throws StamatException 
		 */
		public static void updateIndexfromURL(String indexPath, String URL, Map<String, String> indexedFields) throws StamatException
		{
			Indexer indexer = new Indexer(indexPath);
			HashMap<String, String> fields = new HashMap<String, String>();
			fields.putAll(indexedFields);
			if( !fields.containsKey(Analyser.constants.SEARCH_URL) ) {
				fields.put(Analyser.constants.SEARCH_URL, URL);
			}
			try {
				indexer.updateSplitIndexFromURL(URL, fields);
			} catch (IOException e) {
				logger.warning("ioe while updating index from " + URL + " - " + e.getMessage());
				throw new StamatException("url-related exception caught while updating index from url");
			}
		}

		/**
		 * @param indexPath
		 */
		public static void createSIFTHistograms(String indexPath)
		{
			Indexer indexer = new Indexer(indexPath);
			try {
				indexer.createSIFTHistogram();
			} catch (IOException e) {
				logger.log(Level.SEVERE, e.getMessage());
			}
		}

		/**
		 * @param indexPath
		 */
		public static void createSURFHistograms(String indexPath)
		{
			Indexer indexer = new Indexer(indexPath);
			try {
				indexer.createSURFHistogrm();
			} catch (IOException e) {
				logger.log(Level.SEVERE, e.getMessage());
			}
		}

		/**
		 * @param indexPath
		 * @param URL
		 */
		@Deprecated
		public static void updateIndexCEDDfromURL(String indexPath, String URL, String imageIdentifier)
		{
			Indexer indexing = new Indexer(indexPath);
			try {
				indexing.updateIndexCEDDfromUrl(URL, imageIdentifier);
			} catch (MalformedURLException e) {
				logger.log(Level.WARNING, e.getMessage());
			} catch (IOException e) {
				logger.log(Level.SEVERE, e.getMessage());
			}
		}

		/**
		 * @param indexPath
		 * @param imagePath
		 */
		@Deprecated
		public static void updateIndexCEDDfromPath(String indexPath, String imagePath, String imageIdentifier) 
		{
			Indexer indexing = new Indexer(indexPath);
			try {
				indexing.updateIndexCEDDfromPath(imagePath, imageIdentifier);
			} catch (IOException e) {
				logger.log(Level.SEVERE, e.getMessage());
			}
		}
	
		/**
		 * @param indexPath
		 * @param imageFolderPath
		 * @throws CorruptIndexException
		 * @throws LockObtainFailedException
		 * @throws IOException
		 */
		@Deprecated
		public static void updateIndexCEDDfromFolder(String indexPath, String imageFolderPath) 
		{
			Indexer indexing = new Indexer(indexPath);
			try {
				indexing.updateIndexCEDDfromFolder(imageFolderPath);
			} catch (IOException e) {
				logger.log(Level.SEVERE, e.getMessage());
			}
		}

		/**
		 * @param URL
		 * @param indexPath
		 * @param weightSCD
		 * @param weightCLD
		 * @param weightEHD
		 * @param numberOfResults
		 * @return
		 */
		@Deprecated
		public static List<SearchResult> queryFromUrl(String URL, String indexPath, float weightSCD, float weightCLD, float weightEHD, int numberOfResults) 
		{
			Searcher search = new Searcher(indexPath, numberOfResults+1);
			List<SearchResult> currentResult = null;
			try {
				currentResult = search.searcherCEDDfromUrl(URL);
			} catch (CorruptIndexException e) {
				logger.log(Level.SEVERE, e.getMessage());
			} catch (IOException e) {
				logger.log(Level.SEVERE, e.getMessage());
			}
			return currentResult;			
		}

		/**
		 * @param URL
		 * @param indexPath
		 * @param numberOfResults
		 * @return
		 */
		@Deprecated
		public static List<SearchResult> queryFromUrl(String URL, String indexPath, int numberOfResults) 
		{
			return Analyser.visual.queryFromUrl(URL, indexPath, 1f, 1f, 1f, numberOfResults);
		}

		/**
		 * @param queryImage
		 * @param numberOfResults
		 * @return
		 * @throws CorruptIndexException
		 * @throws IOException
		 */
		@Deprecated
		public static List<SearchResult> queryFromPath(String imagePath, String indexPath, float weightSCD, float weightCLD, float weightEHD, int numberOfResults) 
		{
			Searcher search = new Searcher(indexPath, numberOfResults+1);
			List<SearchResult> currentResult = null;
			try {
				currentResult = search.searcherCEDDfromPath(imagePath);
			} catch (CorruptIndexException e) {
				logger.log(Level.SEVERE, e.getMessage());
			} catch (IOException e) {
				logger.log(Level.SEVERE, e.getMessage());
			}
			return currentResult;
		}
	
		/**
		 * @param imagePath
		 * @param numberOfResults
		 * @return
		 * @throws CorruptIndexException
		 * @throws IOException
		 */
		@Deprecated
		public static List<SearchResult> queryFromPath(String imagePath, String indexPath, int numberOfResults) 
		{
			return Analyser.visual.queryFromPath(imagePath, indexPath, 1f, 1f, 1f, numberOfResults);
		}
	
		/**
		 * @param imagePath
		 * @param numberOfResults
		 * @return
		 * @throws CorruptIndexException
		 * @throws IOException
		 */
		@Deprecated
		public static JSONObject queryFromPath2JSON(String imagePath, String indexPath, int numberOfResults) 
		{
			try {
				return Analyser.searchResult2JSON(Analyser.visual.queryFromPath(imagePath, indexPath, numberOfResults));
			} catch (JSONException e) {
				logger.log(Level.SEVERE, e.getMessage());
			}
			return null;		
		}		
	}

	/**
	 * @param text
	 * @param result
	 * @return
	 */
	private static ArrayList<NamedEntity> evalTFs(String text, ArrayList<NamedEntity> result) 
	{
		int docSize = WordCounter.countWords(text);
		for( NamedEntity an : result ) {
			String keyword = an.getKeyword().trim();
			int numOccurrences = WordCounter.countWordInstances(text, keyword);
			an.setNumOccurrences(numOccurrences);
			an.setTf((float)numOccurrences / (float)docSize);
	
			// the regex of countWordInstances compute the presence of only separated words: if entity has some symbols next to it then it won't be counted	 
			if(an.getTf()==0) {
				an.setTf(1);
			}
		}
		return result;
	}

	/**
	 * @param topicList
	 * @return
	 */
	private static JSONArray topicList2JSON(List<Topic> topicList) 
	{
		JSONArray topicsJSON = new JSONArray();
		for(Topic topic : topicList) {
			JSONObject topicJSON = new JSONObject();
			JSONArray wordsJSON = new JSONArray();
			try {
				List<TopicWord> words = topic.getWords();
				for(TopicWord word : words) {
					JSONObject wordJSON = new JSONObject();
					wordJSON.put("word", word.getWord());
					wordJSON.put("count", word.getCount());
					wordJSON.put("weight", word.getWeight());
					wordsJSON.put(wordJSON);
				}
				topicJSON.put("words", wordsJSON);
				topicJSON.put("alpha", topic.getAlpha());
				topicJSON.put("language", topic.getLanguage());
				topicsJSON.put(topicJSON);
			} catch(Exception e) {
				System.err.println(e.getMessage());
			}
		}
		return topicsJSON;
	}

	/**
	 * @param semanticKeywordList
	 * @return
	 * @throws JSONException 
	 */
	private static JSONArray semanticKeywordList2JSON(List<? extends SemanticKeyword> semanticKeywordList) throws JSONException 
	{
		JSONArray keywordsJSON = new JSONArray();
		for(SemanticKeyword semanticKeyword : semanticKeywordList) {
			JSONObject semanticKeywordJSON = new JSONObject();

			semanticKeywordJSON.put("keyword", semanticKeyword.getKeyword());
			semanticKeywordJSON.put("type", semanticKeyword.getType());
			semanticKeywordJSON.put("confidence", semanticKeyword.getConfidence());
			semanticKeywordJSON.put("num_occurences", semanticKeyword.getNumOccurrences());
			semanticKeywordJSON.put("tf", semanticKeyword.getTf());
			keywordsJSON.put(semanticKeywordJSON);				
		}
		return keywordsJSON;
	}

	/**
	 * @param result
	 * @return
	 * @throws JSONException 
	 */
	private static JSONObject searchResult2JSON(List<SearchResult> result) throws JSONException {
		JSONObject jsonDoc = new JSONObject();
		int size = result != null ? result.size() : 0;
		JSONArray elementsArray = new JSONArray();
		if (size > 0) {
			for (SearchResult element : result){
				elementsArray.put(element.toJSONItem());
				jsonDoc.put("results", elementsArray);
			}

		} else {
			jsonDoc.put("result", elementsArray);
		}
		return jsonDoc;
	}
}
