/**
 * 
 */
package it.unifi.micc.homer.util;

/**
 * @author bertini
 *
 */
public class HomerException extends Exception {
	static final long serialVersionUID = 1L;

	public HomerException() {
	}

	public HomerException(String arg0) {
		super(arg0);
	}

	public HomerException(Throwable arg0) {
		super(arg0);
	}

	public HomerException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}
}
