/**
 * 
 */
package stamat.controller.ned;

import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import stamat.model.KeywordType;
import stamat.model.NamedEntity;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;

/**
 * @author alisi
 *
 */
public class StanfordNERecognizer {

	private static String serializedClassifier;
	private static StanfordNERecognizer instance = null;
	
	private StanfordNERecognizer() {
	}

	public static StanfordNERecognizer getInstance(String serializedClassifier) 
	{
		if(instance == null) {
			instance = new StanfordNERecognizer();
			StanfordNERecognizer.serializedClassifier = serializedClassifier;
		}
		return instance;
	}

	@SuppressWarnings("unchecked")
	public String extractEntity2XML(String text, ArrayList<KeywordType> type)
	{
		AbstractSequenceClassifier<CoreLabel> classifier = CRFClassifier.getClassifierNoExceptions(StanfordNERecognizer.serializedClassifier);
		String out = "<xml>" + classifier.classifyWithInlineXML(text).replaceAll("(\\[|\\])", "") + "</xml>";
		return out;
	}
	
	public ArrayList<NamedEntity> extractEntity(String text, ArrayList<KeywordType> type) 
	{
		String out = this.extractEntity2XML(text, type);
		Document doc = Jsoup.parse(out, "", Parser.xmlParser());
		ArrayList<NamedEntity> entities = new ArrayList<NamedEntity>();

		Elements people = doc.getElementsByTag("person");
		for(Element element : people) {
			entities.add(new NamedEntity(KeywordType.PERSON, element.html()));
		}
		Elements organizations = doc.getElementsByTag("organization");
		for(Element element : organizations) {
			entities.add(new NamedEntity(KeywordType.ORGANIZATION, element.html()));
		}
		Elements locations = doc.getElementsByTag("location");
		for(Element element : locations) {
			entities.add(new NamedEntity(KeywordType.LOCATION, element.html()));
		}
		return entities;
	}
}
