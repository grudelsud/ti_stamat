package stamat.controller.language;

import stamat.util.StamatException;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;

import it.unifi.micc.homer.model.TextDocument;
import it.unifi.micc.homer.util.StringOperations;

/**
 * 
 * @author alisi
 *
 */
public class LanguageDetector implements LanguageIdentifier {

	private static String langModelsPath;
	private static String langStopwordPath;
	private static LanguageDetector instance = null;

	private LanguageDetector(String modelsPath, String stopwordPath) throws LangDetectException
	{
		LanguageDetector.langModelsPath = modelsPath;
		LanguageDetector.langStopwordPath = stopwordPath;
		
		DetectorFactory.loadProfile(modelsPath);
		LanguageDetector.instance = this;
	}
	
	public static LanguageDetector getInstance(String modelsPath, String stopwordPath) throws StamatException
	{
		if(LanguageDetector.instance == null) {
			try {
				LanguageDetector.instance = new LanguageDetector(modelsPath, stopwordPath);
			} catch (LangDetectException e) {
				throw new StamatException(e);
			}
		}
		return LanguageDetector.instance;
	}

	public String identifyLanguageOf(String text)
	{
		try {
			Detector detector = DetectorFactory.create();
			detector.append(text);
			return detector.detect();
		} catch (LangDetectException e) {
			return "unknown";
		}
	}

	public TextDocument cleanTextDocumentStopwords(String text)
	{
		String lang = this.identifyLanguageOf(text);
		lang = lang.equals("unknown") ? "en" : lang;
		return new TextDocument(StringOperations.removeStopwords(text, lang, LanguageDetector.langStopwordPath), lang);
	}

	@Override
	public TextDocument cleanTextDocumentStopwords(TextDocument text)
	{
		String lang = text.getLanguage();
		String source = text.getContent();
		
		lang = lang == null ? this.identifyLanguageOf(source) : lang;
		lang = lang.equals("unknown") ? "en" : lang;

		return new TextDocument(StringOperations.removeStopwords(source, lang, LanguageDetector.langStopwordPath), lang);
	}
	
	/**
	 * @return the langModelsPath
	 */
	public static String getLangModelsPath()
	{
		return langModelsPath;
	}

	/**
	 * @param langModelsPath the langModelsPath to set
	 */
	public static void setLangModelsPath(String langModelsPath)
	{
		LanguageDetector.langModelsPath = langModelsPath;
	}


	/**
	 * @return the langStopwordPath
	 */
	public static String getLangStopwordPath()
	{
		return langStopwordPath;
	}

	/**
	 * @param langStopwordPath the langStopwordPath to set
	 */
	public static void setLangStopwordPath(String langStopwordPath)
	{
		LanguageDetector.langStopwordPath = langStopwordPath;
	}

}
