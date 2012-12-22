/**
 * 
 */
package it.unifi.micc.homer;

import it.unifi.micc.homer.model.AsciiTextDocument;
import it.unifi.micc.homer.model.TagCloud;
import it.unifi.micc.homer.util.HomerConstants;
import it.unifi.micc.homer.util.WordCounter;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.Vector;

import stamat.model.SemanticKeyword;
import stamat.util.StamatException;

/**
 * @author bertini
 * 
 */
public class TagCloudAnalyser {

	public static Vector<SemanticKeyword> process(String text, int numTopWords, String langStopwordPath, String langModelsPath) throws StamatException {

		AsciiTextDocument textDocument = new AsciiTextDocument(text);
		
		if (numTopWords == HomerConstants.AUTODETECT_NUMKEYWORDS)
			numTopWords = estimateNumTopWords(textDocument);

		TagCloud tc = new TagCloud(langStopwordPath, langModelsPath);
		TreeMap<Integer, List<SemanticKeyword>> tags = tc.computeTagCloud(textDocument);
		Vector<SemanticKeyword> v = new Vector<SemanticKeyword>();
		int wordCounter = 0;
		Iterator<Entry<Integer, List<SemanticKeyword>>> it = tags.entrySet().iterator();
	    while (it.hasNext() && (wordCounter < numTopWords) ) {
	    	Entry<Integer, List<SemanticKeyword>> pairs = it.next();
	    	for( SemanticKeyword sk : pairs.getValue() ) {
	    		v.add(sk);
				wordCounter++;
	    	}
		}
		return v;
	}

	private static int estimateNumTopWords(AsciiTextDocument textDocument) {
		int numWords = WordCounter.countWords(textDocument.getContent());
		if( (numWords / 15)<4 )
			numWords = (numWords % 15)*2;
		else if( (numWords % 15)<30)
			numWords = (numWords % 15)*3;
		else if( (numWords % 15)<60)
			numWords = (numWords % 15);
		else if( (numWords % 15)<200)
			numWords = (numWords % 15)/3;
		else if( (numWords % 15)<2000)
			numWords = (numWords % 15)/4;
		else if( (numWords % 15)<4000)
			numWords = (numWords % 15)/6;
		else
			numWords = (numWords % 15)/10;
		return numWords;
	}
}
