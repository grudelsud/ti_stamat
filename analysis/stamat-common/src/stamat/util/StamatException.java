package stamat.util;

public class StamatException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2481849137614171684L;

	public StamatException(String s) {
		super(s);
	}

	public StamatException(Exception e) {
		super(e);
	}

}