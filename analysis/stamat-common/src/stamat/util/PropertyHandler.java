package stamat.util;

import java.io.FileInputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PropertyHandler {
	private static Logger logger = Logger.getLogger(PropertyHandler.class.getName());

	private static PropertyHandler instance = null;

	private FileInputStream fis = null;
	private Properties properties = null;
	
	private PropertyHandler() {
	}

	private void loadProperties(String fileName)
	{
		try {
			this.fis = new FileInputStream(fileName);
			this.properties = new Properties();
			this.properties.load(fis);
		} catch(Exception e) {
			logger.log(Level.SEVERE, "whoops! exception caught while reading property file. message: " + e.getMessage());
		} finally {
			try {
				this.fis.close();
			} catch(Exception t) {
				logger.log(Level.SEVERE, "whoops! exception caught while closing property file. message: " + t.getMessage());
			}
		}		
	}
	
	public static PropertyHandler getInstance(String fileName) {
		if( PropertyHandler.instance == null ) {
			PropertyHandler.instance = new PropertyHandler();
			PropertyHandler.instance.loadProperties(fileName);
		}
		return PropertyHandler.instance;
	}

	/**
	 * @return the properties
	 */
	public Properties getProperties() {
		return properties;
	}
}
