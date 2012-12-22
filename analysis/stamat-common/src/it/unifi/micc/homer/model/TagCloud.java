/**
 * 
 */
package it.unifi.micc.homer.model;

import it.unifi.micc.homer.util.WordCounter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import stamat.controller.language.LanguageDetector;
import stamat.model.KeywordType;
import stamat.model.SemanticKeyword;
import stamat.util.StamatException;

/**
 * @author bertini
 * 
 */
public class TagCloud {

	private String langStopwordPath;
	private String langModelsPath;

	/**
	 * @param langStopwordPath
	 * @param langModelsPath
	 */
	public TagCloud(String langStopwordPath, String langModelsPath) {
		this.langStopwordPath = langStopwordPath;
		this.langModelsPath = langModelsPath;
	}

	public TreeMap<Integer, List<SemanticKeyword>> computeTagCloud(TextDocument text) throws StamatException {
		LanguageDetector lId = LanguageDetector.getInstance(langModelsPath, langStopwordPath);
		String sanitizedText = lId.cleanTextDocumentStopwords(text).getContent();
		Map<String, Integer> wc = WordCounter.doWordCount(sanitizedText);
		int docSize = WordCounter.countWords(sanitizedText);
		// no MultiMaps in Java... simulate using List in the value
		TreeMap<Integer, List<SemanticKeyword>> result = new TreeMap<Integer, List<SemanticKeyword>>(Collections.reverseOrder());
		Iterator<Entry<String, Integer>> it = wc.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Integer> pairs = it.next();
			SemanticKeyword kw = new SemanticKeyword(pairs.getKey().toString(), (float) 1.0, KeywordType.TAG, 0.0);
			kw.setNumOccurrences(((Integer) pairs.getValue()).intValue());
			kw.setTf((float) ((float) kw.getNumOccurrences() / (float) docSize));

			List<SemanticKeyword> l = result.get(kw.getNumOccurrences());
			if (l == null)
				result.put(kw.getNumOccurrences(), l = new ArrayList<SemanticKeyword>());
			if (kw.getKeyword().length() > 1)
				l.add(kw);
		}

		return result;
	}

	/**
	 * @return the langStopwordPath
	 */
	public String getLangStopwordPath() {
		return langStopwordPath;
	}

	/**
	 * @param langStopwordPath
	 *            the langStopwordPath to set
	 */
	public void setLangStopwordPath(String langStopwordPath) {
		this.langStopwordPath = langStopwordPath;
	}

	/**
	 * @return the langModelsPath
	 */
	public String getLangModelsPath() {
		return langModelsPath;
	}

	/**
	 * @param langModelsPath
	 *            the langModelsPath to set
	 */
	public void setLangModelsPath(String langModelsPath) {
		this.langModelsPath = langModelsPath;
	}

}
