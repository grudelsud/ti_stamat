package stamat.controller.language;

import it.unifi.micc.homer.model.TextDocument;
import it.unifi.micc.homer.util.HomerException;

public interface LanguageIdentifier {
	public enum Language {
		afrikaans,
		albanian,
		alemannich,
		arabic,
		armenian,
		basque,
		belarus,
		belarusian,
		bosnian,
		breton,
		bulgarian,
		catalan,
		chinese,
		croatian,
		czech,
		danish,
		dutch,
		english,
		estonian,
		finnish,
		french,
		georgian,
		german,
		greek,
		hawaian,
		hebrew,
		hindi,
		hungarian,
		icelandic,
		indonesian,
		irish,
		italian,
		japanese,
		korean,
		lituanian,
		lithuanian,
		luxembourgish,
		macedonian,
		norwegian_bokmal,
		norwegian_nynorsk,
		norwegian,
		occitan,
		persian,
		polish,
		portuguese,
		romanian,
		russian,
		serbian,
		serbo,
		slovak,
		slovakian,
		slovenian,
		spanish,
		swedish,
		turkish,
		ukrainian,
		vietnamese,
		welsh,
		unknown;
	}
	
	/**
	 * @param text
	 * @return most probable language of the text analyzed.
	 * 
	 * The method may use some heuristic in case of uncertainty in language detection: the number of stopwords of each possible language
	 * is computed: the higher the number of stopwords found the higher the probability that the text is written in that language
	 */
	public String identifyLanguageOf(String text) throws HomerException;
	public TextDocument cleanTextDocumentStopwords(TextDocument text);
}
