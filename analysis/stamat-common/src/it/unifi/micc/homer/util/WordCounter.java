/**
 * 
 */
package it.unifi.micc.homer.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author bertini
 * 
 */
public class WordCounter {

	private final static String wordDelimiters = " \t\n\r\f.?!,;:~@#$%^&*()+={}[]<>/\"|\\";

	public static int doLineCount(String msg) {
		String lineDelim = "\n\r";
		String buf = new String(msg);
		StringTokenizer st = new StringTokenizer(buf, lineDelim);
		int lineCount = st.countTokens();

		return lineCount;
	}

	// action method that does the analysis
	public static Map<String, Integer> doWordCount(String text) {
		Map<String, Integer> wc = new HashMap<String, Integer>();
		String buf = new String(text);
		StringTokenizer st = new StringTokenizer(buf, wordDelimiters);
		while (st.hasMoreTokens()) {
			// get the token & change to lowercase
			String token = st.nextToken().toLowerCase();
			if (wc.containsKey(token)) {
				int value = wc.get(token).intValue() + 1;
				wc.put(token, Integer.valueOf(value));
			} else
				wc.put(token, Integer.valueOf(1));
		}
		return wc;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	// Sort HashMap keeping duplicate values http://www.lampos.net/sort-hashmap
	public static LinkedHashMap sortHashMapByValuesD(HashMap passedMap) {
	    List mapKeys = new ArrayList(passedMap.keySet());
	    List mapValues = new ArrayList(passedMap.values());
	    Collections.sort(mapValues);
	    Collections.sort(mapKeys);
	        
	    LinkedHashMap sortedMap = 
	        new LinkedHashMap();
	    
	    Iterator valueIt = mapValues.iterator();
	    while (valueIt.hasNext()) {
	        Object val = valueIt.next();
	        Iterator keyIt = mapKeys.iterator();
	        
	        while (keyIt.hasNext()) {
	            Object key = keyIt.next();
	            String comp1 = passedMap.get(key).toString();
	            String comp2 = val.toString();
	            
	            if (comp1.equals(comp2)){
	                passedMap.remove(key);
	                mapKeys.remove(key);
	                sortedMap.put((String)key, (Double)val);
	                break;
	            }

	        }

	    }
	    return sortedMap;
	}
	
	public static int countWords(String text) {
		String buf = new String(text);
		StringTokenizer st = new StringTokenizer(buf, wordDelimiters);
		int numTokens = st.countTokens();
		return numTokens;
	}

	public static int countWordInstances(String text, String word) {
		// match full words case insensitive and with \Q+\E avoids to interpret the possible special chars in the string
		Pattern p = Pattern.compile("\\b\\Q"+word+"\\E\\b", Pattern.CASE_INSENSITIVE + Pattern.UNICODE_CASE);
		Matcher m = p.matcher(text); // get a matcher object
		int count = 0;
		while (m.find()) {
			count++;
		}
		return count;
	}

}
