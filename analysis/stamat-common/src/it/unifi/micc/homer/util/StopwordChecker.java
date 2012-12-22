package it.unifi.micc.homer.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class StopwordChecker {
	private Set<String> stopwords;
	private static String langStopwordPath;

	private static Map<String, StopwordChecker> instances = new HashMap<String, StopwordChecker>();

	/**
	 * @param lang	Stopword language
	 * @param langStopPath	path to stopwordlist file
	 * 
	 * All stopwords are normalized to eliminate diacritic marks (accents)
	 */
	private StopwordChecker(String lang, String langStopPath) {
		BufferedReader bufRead;
		try {
			stopwords = new HashSet<String>();
			langStopwordPath = langStopPath;
			File stopwordFile = new File(langStopwordPath + lang.toString() + ".txt");
			bufRead = new BufferedReader(new InputStreamReader(new FileInputStream(stopwordFile),"UTF8"));
			String stopword = null;
			if (bufRead.ready()) {
				while ((stopword = bufRead.readLine()) != null) {
					if(!stopword.startsWith("#")) // check if it's a comment (i.e. starts with #)
						stopwords.add( StringOperations.removeDiacritics(stopword.trim()).toLowerCase().trim());
				}
				bufRead.close();
			}
		} catch (FileNotFoundException e) {
			System.err.println("Stopword list not available for language: " + lang.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static StopwordChecker getCheckerForLanguage(String lang, String langStopwordPath) {
		StopwordChecker checker = instances.get(lang);
		if (checker == null) {
			checker = new StopwordChecker(lang, langStopwordPath);
			instances.put(lang, checker);
		}
		return checker;
	}

	public boolean isStopword(String word) {
		boolean result = stopwords.contains(word.toLowerCase().trim());
		return result;
	}

	/**
	 * @return the langModelsPath
	 */
	public static String getLangModelsPath() {
		return langStopwordPath;
	}

	/**
	 * @param langModelsPath
	 *            the langModelsPath to set
	 */
	public static void setLangModelsPath(String langModelsPath) {
		StopwordChecker.langStopwordPath = langModelsPath;
	}
}
