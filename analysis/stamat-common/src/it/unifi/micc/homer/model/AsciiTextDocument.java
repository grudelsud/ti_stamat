/**
 * 
 */
package it.unifi.micc.homer.model;

import java.io.Serializable;

/**
 * @author bertini
 * 
 */
public class AsciiTextDocument extends TextDocument implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1979493571853783734L;

	public AsciiTextDocument(String content, String language) {
		super(content, language);
	}
	
	public AsciiTextDocument(String content) {
		super(content, null);
	}
}
