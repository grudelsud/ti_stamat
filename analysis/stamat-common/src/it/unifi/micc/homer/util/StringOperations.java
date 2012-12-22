package it.unifi.micc.homer.util;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;


public class StringOperations {
	private static final String PLAIN_ASCII = "AaEeIiOoUu" // grave
			+ "AaEeIiOoUuYy" // acute
			+ "AaEeIiOoUuYy" // circumflex
			+ "AaOoNn" // tilde
			+ "AaEeIiOoUuYy" // umlaut
			+ "Aa" // ring
			+ "Cc" // cedilla
			+ "OoUu" // double acute
	;

	private static final String UNICODE = "\u00C0\u00E0\u00C8\u00E8\u00CC\u00EC\u00D2\u00F2\u00D9\u00F9"
			+ "\u00C1\u00E1\u00C9\u00E9\u00CD\u00ED\u00D3\u00F3\u00DA\u00FA\u00DD\u00FD"
			+ "\u00C2\u00E2\u00CA\u00EA\u00CE\u00EE\u00D4\u00F4\u00DB\u00FB\u0176\u0177" + "\u00C3\u00E3\u00D5\u00F5\u00D1\u00F1"
			+ "\u00C4\u00E4\u00CB\u00EB\u00CF\u00EF\u00D6\u00F6\u00DC\u00FC\u0178\u00FF" + "\u00C5\u00E5" + "\u00C7\u00E7"
			+ "\u0150\u0151\u0170\u0171";
	
	public static String hashtagify(String input) {
		String result = new String("#");
		String[] tokens = input.split("\\s");
		for (int i = 0; i < tokens.length; i++) {
			result = result.concat(tokens[i]);
		}
		return result;
	}

	public static List<String> extractHashtags(String tweetText) {
		List<String> hashtags = new ArrayList<String>();
		String regex = "#\\w+";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(tweetText);
		while (m.find()) {
			String htag = m.group();
			// System.out.println("Found hashtag: " + htag);
			hashtags.add(htag);
		}
		return hashtags;
	}

	public static String removeMentions(String input) {
		return input.replaceAll("[@]+([A-Za-z0-9-_]+)", "");
	}

	public static String removeStopwords(String input, String lang, String langStopwordPath) {
		if( lang == null )
			return input;
		StringBuffer results = new StringBuffer("");
		String[] tokens = input.split("[\\s\\p{Punct}]+");
		StopwordChecker stopCheck = StopwordChecker.getCheckerForLanguage(lang, langStopwordPath);
		for (int i = 0; i < tokens.length; i++) {
			if (!stopCheck.isStopword( removeDiacritics(tokens[i]) )) {
				results.append( sanitizeWord(tokens[i]) ); 
				results.append(" ");
			}
		}
		String result = results.toString();
		if (result.length() != 0) {
			result = result.substring(0, result.length() - 1);
		}
		return result;
	}

	/**
	 * @param input text to be analysed
	 * @param lang language of the text
	 * @param langStopwordPath path to the list of stopwords for the selected language
	 * @return number of eliminated stopwords, or -1 if language can not be identified
	 */
	public static int countStopwords(String input, String lang, String langStopwordPath) {
		int result = 0;
		if( lang == null )
			return -1;
		String[] tokens = input.split("[\\s\\p{Punct}]+");
		StopwordChecker stopCheck = StopwordChecker.getCheckerForLanguage(lang, langStopwordPath);
		for (int i = 0; i < tokens.length; i++) {
			if (stopCheck.isStopword(  StringOperations.removeDiacritics(tokens[i]) )) {
				result++;
			}
		}
		return result;
	}

	/**
	 * @param word
	 * @return word without punctuation: !"#$%&'()*+,-./:;<=>?@[\]^_`{|}~
	 * 
	 * use regex to remove the punctuation and spaces (http://recurial.com/programming/removing-punctuation-and-spaces-from-java-string/)
	 */
	protected static String sanitizeWord(String word) {
		String result = word.replaceAll("\\p{Punct}", " ");
		return result.trim();
	}
	
	// From: http://www.rgagnon.com/javadetails/java-0456.html
	public static String unAccent(String s) {
		if (s == null)
			return null;
		StringBuilder sb = new StringBuilder();
		int n = s.length();
		for (int i = 0; i < n; i++) {
			char c = s.charAt(i);
			int pos = UNICODE.indexOf(c);
			if (pos > -1) {
				sb.append(PLAIN_ASCII.charAt(pos));
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}
	
	// From: http://stackoverflow.com/questions/249087/how-do-i-remove-diacritics-accents-from-a-string-in-net
	public static String removeDiacritics(String input)
    {
        String nrml = Normalizer.normalize(input, Normalizer.Form.NFD);
        StringBuilder stripped = new StringBuilder();
        for (int i=0;i<nrml.length();++i)
        {
            if (Character.getType(nrml.charAt(i)) != Character.NON_SPACING_MARK)
            {
                stripped.append(nrml.charAt(i));
            }
        }
        return stripped.toString();
    }

	public static String removeURLfromString(String originalText) {
		String result = originalText;
		String regex = "\\(?\\b(http://|www[.])[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_()|]";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(originalText);
		while (m.find()) {
			String url = m.group();
			if (url.startsWith("(")) {
				url = url.substring(1, url.length());
			}
			if (url.endsWith(")")) {
				url = url.substring(0, url.length() - 1);
			}
			result = result.replace(url, "");
		}
		return result;
	}

	public static List<String> extractURLs(String originalText) {
		List<String> urls = new ArrayList<String>();
		String regex = "\\(?\\b(http://|www[.])[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_()|]";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(originalText);
		while (m.find()) {
			String url = m.group();
			if (url.startsWith("(")) {
				url = url.substring(1, url.length());
			}
			if (url.endsWith(")")) {
				url = url.substring(0, url.length() - 1);
			}
			urls.add(url);
		}
		return urls;
	}

	public static String makeSqlList(List<? extends Object> objects) {
		String result = "(";
		for (int i = 0; i < objects.size() - 1; i++) {
			result = result
					.concat("'" + objects.get(i).toString() + "'" + ", ");
		}
		if (objects.size() > 0) {
			result = result.concat("'"
					+ objects.get(objects.size() - 1).toString() + "'");
		}
		result = result.concat(")");
		return result;
	}

	public static String removeHash(String input) {
		if (input.startsWith("#")) {
			return input.substring(1);
		} else
			return input;
	}

	public static String concatStrings(List<String> strings) {
		StringBuffer result = new StringBuffer("");
		for (String token : strings) {
			result.append(token);
			result.append(" ");
		}
		return result.toString().trim();
	}

	public static String logicOrConcatStrings(List<String> strings) {
		String result = "";
		StringBuffer tempResult = new StringBuffer(result);
		for (String token : strings) {
			tempResult.append("\"");
			tempResult.append(token);
			tempResult.append("\"");
			tempResult.append(" OR ");
		}
		result = tempResult.toString()
;		if (result.length() > 0) {
			result = result.substring(0, result.length() - 4);
		}
		return result;
	}

	public static String removeNonLettersFromString(String string) {
		char[] res = new char[string.length()];
		char[] originalStr = string.toCharArray();
		int resSize = 0;
		for (int i = 0; i < originalStr.length; i++) {
			if (Character.isLetterOrDigit(originalStr[i])
					|| Character.isWhitespace(originalStr[i])) {
				res[resSize++] = originalStr[i];
			}
		}
		return new String(res);
	}

	public static String cleanHTML(String content) {
		return Jsoup.parse(content).text();	//extract text from HTML document		
	}

	public static String tokenizeAndCorrect(String data) {
		StringBuffer exactString = new StringBuffer("");
		StringTokenizer token = new StringTokenizer(data);
		while (token.hasMoreTokens()) {
			exactString.append(StringOperations.firstLetterCaps(token.nextToken()));
			exactString.append(" ");
		}
		return exactString.toString();
	}

	public static String firstLetterCaps(String data) {
		String firstLetter = data.substring(0, 1).toUpperCase();
		String restLetters = data.substring(1).toLowerCase();
		return firstLetter + restLetters;
	}
}
