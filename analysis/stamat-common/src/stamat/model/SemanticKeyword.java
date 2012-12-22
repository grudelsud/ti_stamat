/**
 * 
 */
package stamat.model;

import it.unifi.micc.homer.util.HomerException;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONObject;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author bertini
 *
 */
public class SemanticKeyword {
	protected String keyword = "";
	protected KeywordType type = KeywordType.UNDEF;

	private float confidence = 0;
	private double probability = 0;
	private float tf = 0;
	private int numOccurrences = 0;

	public SemanticKeyword()
	{
	}

	/**
	 * @param keyword
	 * @param confidence
	 * @param type
	 */
	public SemanticKeyword(String keyword, float confidence, KeywordType type, double probability)
	{
		this.keyword = keyword;
		this.confidence = confidence;
		this.type = type;
		numOccurrences = 1;
		tf = (float)1.0;
		this.probability=probability;
	}

	public static Vector<SemanticKeyword> convertTopicToSemanticKeyword(Topic topic)
	{
		Vector<SemanticKeyword> results = new Vector<SemanticKeyword>();
		for( TopicWord word : topic.getWords() ) {
			SemanticKeyword result = new SemanticKeyword(word.getWord(), (float) word.getWeight(), KeywordType.TOPIC, topic.getAlpha() );
			results.add(result);
		}
		return results;
	}

	public static Vector<SemanticKeyword> selectTopSemanticKeywords(Vector<SemanticKeyword> detectedSW, int numTopWords)
	{
		Vector<SemanticKeyword> result = new Vector<SemanticKeyword>();
		if( detectedSW.size()<=numTopWords )
			result.addAll(detectedSW);
		else { // select only numTopWords
			// get unique SemanticKeyword list
			Map<String, SemanticKeyword> selectedSW = new HashMap<String, SemanticKeyword>();
			for( SemanticKeyword sw : detectedSW ) {
				SemanticKeyword test = selectedSW.get(sw.getKeyword());
				if( test == null )
					selectedSW.put(sw.getKeyword(), sw);
				else {
					test.setNumOccurrences(test.getNumOccurrences()+1);
					test.setConfidence((test.getConfidence()+sw.getConfidence())/2);
					selectedSW.put(test.getKeyword(), test);
				}
	
			}
			// convert Map into List
			Vector<SemanticKeyword> tempResult = new Vector<SemanticKeyword>();
			Iterator<Entry<String, SemanticKeyword>> it = selectedSW.entrySet().iterator();
			while (it.hasNext()) 
				tempResult.add(it.next().getValue());
			// reverse sort SemanticKeywords according to their frequency (from max to min)
			Collections.sort(tempResult, new Comparator<SemanticKeyword>() {
				public int compare(SemanticKeyword one, SemanticKeyword other) {
					if (one.getNumOccurrences() < other.getNumOccurrences())
						return 1;
					if (one.getNumOccurrences() == other.getNumOccurrences())
						return 0;
					return -1;
				}
			});
			int numWords = 0;
			// select most frequent SemanticKeyword
			for( SemanticKeyword sw : tempResult ) {
				if( (sw.getNumOccurrences() > 1) && (numWords<numTopWords) ) {
					result.add(sw);
					numWords++;
				}
			}
			// if there's need to select some more keyword
			if( (numTopWords-numWords)>0 ) {
				// reverse sort them according to confidence (from max to min)
				Collections.sort(tempResult, new Comparator<SemanticKeyword>() {
					public int compare(SemanticKeyword one, SemanticKeyword other) {
						if (one.getConfidence() < other.getConfidence())
							return 1;
						if (one.getConfidence() == other.getConfidence())
							return 0;
						return -1;
					}
				});
				// select most confident
				for( SemanticKeyword sw : tempResult ) {
					if( !result.contains(sw) && (numWords<numTopWords) ) {
						result.add(sw);
						numWords++;
					}
				}
			}
		}
		// reset number of occurrences (will be computed later)
		for( SemanticKeyword sw : result )
			sw.setNumOccurrences(0);
		return result;
	}

	public JSONObject toJSONItem() throws HomerException
	{
		try {
			JSONObject oJson = new JSONObject();
			String sanitizedKeyword = StringEscapeUtils.escapeHtml4(keyword);
			oJson.put("keyword", sanitizedKeyword);
			oJson.put("tf", tf);
			oJson.put("occurrences", numOccurrences);
			oJson.put("confidence", confidence);
			oJson.put("probability", probability);
			return oJson;


		} catch (Exception e) {
			throw new HomerException(e);
		}
	}

	public Document toRSSItem() throws HomerException
	{
		try {

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder        builder = factory.newDocumentBuilder();
			DOMImplementation      impl    = builder.getDOMImplementation();
			Document               xmldoc  = impl.createDocument(null, "item", null);

			// Root element.
			Element root = xmldoc.getDocumentElement();

			Element keywordTypeElement = xmldoc.createElementNS(null, type.toString().toLowerCase());
			root.appendChild(keywordTypeElement);

			Element e = null;
			Node    n = null;

			e = xmldoc.createElementNS(null, "keyword");
			String sanitizedKeyword = StringEscapeUtils.escapeHtml4(keyword); 
			n = xmldoc.createTextNode( sanitizedKeyword );
			e.appendChild(n);
			keywordTypeElement.appendChild(e);

			e = xmldoc.createElementNS(null, "tf");
			n = xmldoc.createTextNode( String.valueOf( tf ) );
			e.appendChild(n);
			keywordTypeElement.appendChild(e);

			e = xmldoc.createElementNS(null, "occurrences");
			n = xmldoc.createTextNode( String.valueOf( numOccurrences ) );
			e.appendChild(n);
			keywordTypeElement.appendChild(e);

			e = xmldoc.createElementNS(null, "confidence");
			n = xmldoc.createTextNode( String.valueOf( confidence ) );
			e.appendChild(n);
			keywordTypeElement.appendChild(e);

			e = xmldoc.createElementNS(null, "probability");
			n = xmldoc.createTextNode( String.valueOf( probability ) );
			e.appendChild(n);
			keywordTypeElement.appendChild(e);

			return xmldoc;

		} catch( Exception e ) {
			throw new HomerException( e );
		}
	}

	/**
	 * @return the keyword
	 */
	public String getKeyword() {
		return keyword;
	}

	/**
	 * @param keyword the keyword to set
	 */
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	/**
	 * @return the confidence
	 */
	public float getConfidence() {
		return confidence;
	}

	/**
	 * @param confidence the confidence to set
	 */
	public void setConfidence(float confidence) {
		this.confidence = confidence;
	}

	/**
	 * @return the type
	 */
	public KeywordType getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(KeywordType type) {
		this.type = type;
	}

	/**
	 * @return the numOccurrences
	 */
	public int getNumOccurrences() {
		return numOccurrences;
	}

	/**
	 * @param numOccurrences the numOccurrences to set
	 */
	public void setNumOccurrences(int numOccurrences) {
		this.numOccurrences = numOccurrences;
	}

	/**
	 * @return the tf
	 */
	public float getTf() {
		return tf;
	}

	/**
	 * @param tf the tf to set
	 */
	public void setTf(float tf) {
		this.tf = tf;
	}
}
