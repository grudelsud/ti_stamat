/**
 * 
 */
package stamat.controller.topic;

import it.unifi.micc.homer.model.TextDocument;
import it.unifi.micc.homer.util.HomerConstants;
import it.unifi.micc.homer.util.StringOperations;
import it.unifi.micc.homer.util.WordCounter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import stamat.controller.language.LanguageDetector;
import stamat.model.SemanticKeyword;
import stamat.model.Topic;
import stamat.model.TopicWord;
import stamat.util.StamatException;

import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.Input2CharSequence;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceLowercase;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.Alphabet;
import cc.mallet.types.IDSorter;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.util.MalletLogger;

/**
 * @author alisi
 * 
 */
public class TopicDetector {
	private static Logger logger = Logger.getLogger(TopicDetector.class.getName());
	private static boolean disableLangDetection = false;

	private static int numIterations = 2000;
	private static int numInterval = 50;
	private int numberOfTopics = HomerConstants.AUTODETECT_NUMTOPICS;
	private int numTopWords = HomerConstants.AUTODETECT_NUMKEYWORDS;

	public static Vector<SemanticKeyword> extract(List<String> texts, String langModelsPath, String langStopwordPath) throws StamatException
	{
		int numTopics = HomerConstants.AUTODETECT_NUMTOPICS;
		int numTopWords = HomerConstants.AUTODETECT_NUMKEYWORDS;
		return TopicDetector.extract(texts, langModelsPath, langStopwordPath, numTopics, numTopWords, null);
	}

	public static Vector<SemanticKeyword> extract(List<String> texts, String langModelsPath, String langStopwordPath, int numTopics, int numTopWords) throws StamatException
	{
		return TopicDetector.extract(texts, langModelsPath, langStopwordPath, numTopics, numTopWords, null);
	}

	public static Vector<SemanticKeyword> extract(List<String> texts, String langModelsPath, String langStopwordPath, int numTopics, int numTopWords, String ldaModelPath) throws StamatException
	{
		logger.log(Level.INFO, "extracting topics");
		ParallelTopicModel lda = null;
		if( ldaModelPath != null ) {
			TrainedModel trainedModel = TrainedModel.getInstance(ldaModelPath);
			lda = trainedModel.getModel();
		}

		TopicDetector topicd = new TopicDetector();

		StringBuffer allSanitizedText = new StringBuffer();
		StringBuffer language = new StringBuffer();
		InstanceList instances = TopicDetector.textDocumentList2instanceList(texts, langModelsPath, langStopwordPath, allSanitizedText, language);

		if (numTopics == HomerConstants.AUTODETECT_NUMTOPICS) {
			topicd.numberOfTopics = numTopics = estimateNumberOfTopics(allSanitizedText);
		} else {
			topicd.numberOfTopics = numTopics;
		}

		if (numTopWords == HomerConstants.AUTODETECT_NUMKEYWORDS) {
			topicd.numTopWords = numTopWords = estimateNumTopWords(allSanitizedText);
		} else {
			topicd.numTopWords = numTopWords;
		}

		String text = allSanitizedText.toString();
		int wordCount = WordCounter.countWords(text);
		int topicLimit = wordCount < topicd.numberOfTopics ? wordCount : topicd.numberOfTopics;
		List<Topic> detectedTopics = topicd.extract(instances, lda, topicLimit, language.toString());

		Vector<SemanticKeyword> detectedSW = new Vector<SemanticKeyword>();
		for( Topic topic : detectedTopics ) {
			detectedSW.addAll(SemanticKeyword.convertTopicToSemanticKeyword(topic));
		}
		Vector<SemanticKeyword> result = SemanticKeyword.selectTopSemanticKeywords(detectedSW, numTopWords*numTopics);
		int docSize = WordCounter.countWords(text);
		for( SemanticKeyword kw : result ) {
			kw.setNumOccurrences(WordCounter.countWordInstances(text, kw.getKeyword()));
			kw.setTf((float)((float)kw.getNumOccurrences()/(float)docSize));
		}
		return result;
	}

	public static Vector<Topic> infer(List<String> texts, String langModelsPath, String langStopwordPath, int numTopWords, String ldaModelPath) throws StamatException
	{
		StringBuffer allSanitizedText = new StringBuffer();
		StringBuffer language = new StringBuffer();
		InstanceList instances = TopicDetector.textDocumentList2instanceList(texts, langModelsPath, langStopwordPath, allSanitizedText, language);
	
		Instance inst = instances.get(0);
	
		Vector<Topic> topics = new Vector<Topic>();
		TrainedModel tm = TrainedModel.getInstance(ldaModelPath);
		ParallelTopicModel lda = tm.getModel();
		TopicInferencer inferencer = lda.getInferencer();
	
		double[] alphaEstimated = inferencer.getSampledDistribution(inst, 10, 1, 5);
	
		Object[][] topWords = TopicDetector.getTopWordsWithWeights(lda.getSortedWords(), lda.getNumTopics(), numTopWords, lda.getAlphabet());

		String text = allSanitizedText.toString();
		int wordCount = WordCounter.countWords(text);
		int topicLimit = wordCount < lda.getNumTopics() ? wordCount : lda.getNumTopics();
		for (int topicCount = 0; topicCount < topicLimit; topicCount++) {
			Topic topic = new Topic(alphaEstimated[topicCount], language.toString());
			for (Object word : topWords[topicCount]) {
				topic.addWord((TopicWord) word);
			}
			topics.add(topic);
		}
		return topics;
	}

	public static void train(List<String> trainingTexts, int numTopics, String langModelsPath, String langStopwordPath, String ldaModelPath) throws IOException
	{
		MalletLogger.getLogger(ParallelTopicModel.class.getName()).setLevel(Level.INFO);
		TrainedModel tm;
		try {
			tm = TrainedModel.getInstance(ldaModelPath);			
		} catch(Exception e) {
			tm = TrainedModel.createInstance(ldaModelPath, numTopics);
		}
		ParallelTopicModel lda = tm.getModel();
		StringBuffer allSanitizedText = new StringBuffer();
		StringBuffer language = new StringBuffer();
		InstanceList instances = TopicDetector.textDocumentList2instanceList(trainingTexts, langModelsPath, langStopwordPath, allSanitizedText, language);
		
		lda.setNumIterations(numIterations);
		lda.setOptimizeInterval(numInterval);
		lda.addInstances(instances);
		lda.estimate();
		tm.saveModelFile(lda);
		
		// used in debug mode to see the extracted topic
		//Object[][] topWords = MalletTopWordsExtractor.getTopWordsWithWeights(lda.getSortedWords(),numTopics, 3, lda.getAlphabet());	
	}

	/**
	 * Internal usage only, use the public extract methods for library purposes
	 * 
	 * @param instances
	 * @param numberOfTopics
	 * @param numTopWords
	 * @param lda can be null, in this case it would create an empty lda model and add stuff to it
	 * @return
	 */
	private List<Topic> extract(InstanceList instances, ParallelTopicModel lda, int topicLimit, String language)
	{
		MalletLogger.getLogger(ParallelTopicModel.class.getName()).setLevel(Level.INFO);
		List<Topic> topics = new ArrayList<Topic>();

		if( lda == null ) {
			lda = new ParallelTopicModel(this.numberOfTopics);
		}
		lda.setNumIterations(TopicDetector.numIterations);
		lda.setOptimizeInterval(TopicDetector.numInterval);
		lda.addInstances(instances);
		try {
			lda.estimate();
		} catch (Exception e) {
			// do nothing, it should be some silly & useless iteration exception
		}

		Object[][] topWords = TopicDetector.getTopWordsWithWeights(lda.getSortedWords(),this.numberOfTopics, this.numTopWords, lda.getAlphabet());
		for (int topicCount = 0; topicCount < topicLimit; topicCount++) {
			Topic topic = new Topic(lda.alpha[topicCount], language);
			for (Object word : topWords[topicCount]) {
				topic.addWord((TopicWord) word);
			}
			topics.add(topic);
		}
		return topics;
	}

	public static Object[][] getTopWordsWithWeights(ArrayList<TreeSet<IDSorter>> topicSortedWords, int numTopics, int numWords, Alphabet alphabet) {
	
		Object[][] result = new Object[numTopics][];
	
		for (int topic = 0; topic < numTopics; topic++) {
			Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();
	
			TreeSet<IDSorter> sortedWords = topicSortedWords.get(topic);
	
			// How many words should we report? Some topics may have fewer than
			// the default number of words with non-zero weight.
			int limit = numWords;
			if (sortedWords.size() < numWords) {
				limit = sortedWords.size();
			}
			result[topic] = new Object[limit];
	
			for (int i = 0; i < limit; i++) {
				IDSorter info = iterator.next();
				TopicWord tw = new TopicWord(alphabet.lookupObject(info.getID()).toString(), info.getWeight());
				result[topic][i] = tw;
			}
		}
	
		return result;
	}

	private static InstanceList textDocumentList2instanceList(List<String> texts, String langModelsPath, String langStopwordPath, StringBuffer allSanitizedText, StringBuffer language) {
		// Pipes: tokenize, lowercase, remove stopwords, map to features
		ArrayList<Pipe> pipeList = new ArrayList<Pipe>();
		Pattern tokenPattern = Pattern.compile("[\\p{L}\\p{N}_]+");

		pipeList.add(new Input2CharSequence("UTF-8"));
		pipeList.add(new CharSequence2TokenSequence(tokenPattern));
		pipeList.add(new TokenSequenceLowercase());
		// stopword removal is done at later stage: we should pass the stopword file name here...:
		// pipeList.add(new TokenSequenceRemoveStopwords(new File("stoplists/en.txt"), "UTF-8", false, false, false) );
		// pipeList.add(new TokenSequenceRemoveStopwords());
		pipeList.add(new TokenSequence2FeatureSequence());

		InstanceList instances = new InstanceList(new SerialPipes(pipeList));
		List<Instance> tmpInstanceList = new ArrayList<Instance>();
		Map<String, Integer> langFreq = new LinkedHashMap<String, Integer>();

		for (String text : texts) {
			String sanitizedText = text;
			sanitizedText = StringOperations.removeURLfromString(sanitizedText);
			sanitizedText = StringOperations.removeMentions(sanitizedText);
			sanitizedText = StringOperations.removeNonLettersFromString(sanitizedText);

			if (!TopicDetector.disableLangDetection) {
				LanguageDetector lId;
				String lang;
				try {
					lId = LanguageDetector.getInstance(langModelsPath, langStopwordPath);
					TextDocument cleaned = lId.cleanTextDocumentStopwords(text);
					sanitizedText = cleaned.getContent();
					lang = cleaned.getLanguage();
				} catch (StamatException e) {
					lang = "unknown";
				}
				Integer val = langFreq.get(language) == null ? new Integer(1) : (Integer)(langFreq.get(language) + 1);
				langFreq.put(lang, val);
			}

			if (!sanitizedText.trim().equalsIgnoreCase("")) {
				Instance inst = new Instance(sanitizedText, null, (text.length() > 9 ? text.substring(0, 9) : "cut"), text);
				tmpInstanceList.add(inst);
				allSanitizedText.append(sanitizedText);
			}
		}
		List<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(langFreq.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> m1, Map.Entry<String, Integer> m2) {
                return (m2.getValue()).compareTo(m1.getValue());
            }
        });

        language.append(list.get(0).getKey());
		instances.addThruPipe(tmpInstanceList.iterator());
		return instances;
	}

	/**
	 * used to autodetect number of topics depending on length of input
	 * 
	 * @param allSanitizedText
	 * @return
	 */
	public static int estimateNumTopWords(StringBuffer allSanitizedText) {
		int numTopics = estimateNumberOfTopics(allSanitizedText);
		int maxWords;
		if (numTopics < 2) {
			maxWords = 8;
		} else if (numTopics < 4) {
			maxWords = numTopics * 6;
		} else {
			maxWords = (numTopics / 10) > 10 ? (numTopics * 8) : (numTopics * 7);
		}
		if (maxWords > 100) {
			maxWords = 100;
		}
		return maxWords;
	}

	/**
	 * used to autodetect number of topics depending on length of input
	 * 
	 * @param allSanitizedText
	 * @return
	 */
	public static int estimateNumberOfTopics(StringBuffer allSanitizedText) {
		int numWords = WordCounter.countWords(allSanitizedText.toString());
		int numTopics;
		if( numWords < 200 ) {
			numTopics = 1;
		} else if( numWords < 500 ) {
			numTopics = 2;
		} else {
			numTopics = (numWords / 1500) > 7 ? (numWords / 1500) : 3;
		}
		if( numTopics > 15 ) {
			numTopics = 15;
		}
		return numTopics;
	}
}
