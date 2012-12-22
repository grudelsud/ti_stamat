/**
 * 
 */
package it.unifi.micc.homer.model;

import java.util.List;

import stamat.controller.language.LanguageDetector;
import stamat.model.SemanticKeyword;
import stamat.util.StamatException;

/**
 * @author bertini
 *
 */
public class TextDocument {
	protected List<SemanticKeyword> keywords;
	protected String content;
	protected String language;
	/**
	 * 
	 */
	public TextDocument() {
		super();
	}
	/**
	 * @param content
	 * @param language
	 */
	public TextDocument(String content, String language) {
		super();
		this.content = content;
		this.language = language;
	}
	/**
	 * @param content
	 */
	public TextDocument(String content) {
		super();
		this.content = content;
		this.language = null;
	}
	
	public boolean autoSetLanguage(String langModelsPath, String langStopwordPath) throws StamatException {
		boolean result = false;
		LanguageDetector langIdent = LanguageDetector.getInstance(langModelsPath, langStopwordPath);
		this.language = langIdent.identifyLanguageOf(content);
		if( this.language != "unknown" )
			result = true;
		return result;
	}
	
	/**
	 * @return the content
	 */
	public String getContent() {
		return content;
	}
	/**
	 * @param content the content to set
	 */
	public void setContent(String content) {
		this.content = content;
	}
	/**
	 * @return the language
	 */
	public String getLanguage() {
		return language;
	}
	/**
	 * @param language the language to set
	 */
	public void setLanguage(String language) {
		this.language = language;
	}
	/**
	 * @return the keywords
	 */
	public List<SemanticKeyword> getKeywords() {
		return keywords;
	}

	/**
	 * @param keywords the keywords to set
	 */
	public void setKeywords(List<SemanticKeyword> keywords) {
		this.keywords = keywords;
	}
}
