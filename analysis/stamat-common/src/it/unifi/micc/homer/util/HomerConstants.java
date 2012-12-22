package it.unifi.micc.homer.util;

import stamat.model.KeywordType;

/**
 * Used to store all the Homer constants and parameters, which are roughly categorized in:
 * - orione.properties: only data related to database access
 * - properties stored in the options table of the database (name starting with PROP_)
 * - post parameters sent by the search interfaces (name starting with POSTPAR_)
 * - all other constants (names mainly starting with PARAM_)
 *
 */
public abstract class HomerConstants {
	/**
	 * Property file located in the webapp absolute path
	 * 
	 * @see it.unifi.micc.Searcher.controller.Search
	 * @see it.unifi.micc.orione.util.OrioneProperties
	 */
	public static final String PROPFILE_NAME    = "homer.properties";

	/**
	 * Stored as a default comment when the property file is not present
	 */
	public static final String PROPFILE_COMMENT = "homer java webapp properties";

	/**
	 * Name of the properties stored as connection data in orione.properties. 
	 * All other options are retrieved from the options table in the orione database.
	 */
	public static final String PROP_DBHOST = "db_host";
	public static final String PROP_DBNAME = "db_name";
	public static final String PROP_DBUSER = "db_user";
	public static final String PROP_DBPASS = "db_pass";

	/**
	 * List of properties stored in the options table of the orione database
	 * see OrioneProperties.getDefaultOptions() for default values
	 */
	public static final String PROP_ANALYSIS  = "homer: default analyser";
	public static final String PROP_ENTITIES_REL_PATH  = "homer: NED (dictionary files) realtive path";
	public static final String PROP_VERBOSE_LOG = "homer: application log verbosity";
	public static final String PROP_HOSTNAME = "homer: servlet address (used for sirio search interface)";

	/**
	 * A few tons of parameters and post parameters used by the various interfaces
	 */
	public static final String PROP_ABSPATH			= "webapp absolute path"; // used almost everywhere to retrieve paths, set during homerproperties.getinstance()
	public static final String ANALYSER_TOPIC		= "topic";	// used to instantiate the appropriate analyser in analysefactory
	public static final String ANALYSER_NED			= "ned";	// used to instantiate the appropriate analyser in analysefactory
	public static final String ANALYSER_LANGUAGE	= "language";	// used to instantiate the appropriate analyser in analysefactory
	public static final String ANALYSER_TAGCLOUD	= "tagcloud";	// used to instantiate the appropriate analyser in analysefactory
	public static final String ANALYSER_TRAINLDAMODEL	= "traintopic";	// used to instantiate the appropriate analyser in analysefactory
	public static final String TEXTTYPE_ASCII		= "ASCII"; // input format
	public static final String MODELLDA				= "modelLDA"; // for using a trained model 
	public static final String TEXTTYPE_HTML		= "HTML"; // input format
	public static final String HTMLEXTRACT_ARTICLE	= "ARTICLE"; // input format
	public static final String JSON_OUTPUTFORMAT	= "JSON"; // output format
	public static final String RSS_OUTPUTFORMAT		= "RSS"; // output format
	
	public static final String POSTPAR_ANALYSIS     = "analysis"; // analysis type
	public static final String POSTPAR_PAGE         = "page"; // results paging
	public static final String POSTPAR_RESULTSPERPAGE	= "resultsPerPage"; // results per page
	public static final String POSTPAR_SEPARATOR	= ",";
	public static final String POSTPAR_TEXT			= "text"; // text to be analysed
	public static final String POSTPAR_TEXTLANG		= "textlang"; // language of text to be analysed
	public static final String POSTPAR_MODELLDA		= "modelLDA"; // for using a trained model
	public static final String POSTPAR_TEXTTYPE		= "texttype"; // text format: ASCII or HTML
	public static final String POSTPAR_TEXTEXTRACT	= "textextraction"; // text extraction: ARTICLE,...
	public static final String POSTPAR_OUTPUTFORMAT	= "outputformat"; // output format (JSON or RSS/XML)
	public static final String POSTPAR_INFILENAME	= "infilename"; // input text file name
	public static final String POSTPAR_INFILEPATH	= "infilepath"; // input file path
	public static final String POSTPAR_DOCURL		= "docurl"; // input URL
	public static final String POSTPAR_NUMKEYWORDS	= "numkeywords"; // max num. of extracted keywords
	public static final String POSTPAR_NUMTOPICS	= "numtopics"; // max num. of topics of document to be analyzed
	public static final String POSTPAR_ENTITYTYPES	= "entitytypes"; // type of named entities to be extracted
	public static final String POSTPAR_MODELNAME	= "modelname"; // input model name
	
	public static final String ANALYSIS_RESULTS		= "results";
	
	public static final int DEFAULT_NUMKEYWORDS		= 5;
	public static final int DEFAULT_NUMTOPICS		= 10;
	public static final String DEFAULT_ANALYSIS		= ANALYSER_TOPIC;
	public static final KeywordType DEFAULT_ENTITYTYPES = KeywordType.ALLENTS;
	public static final String DEFAULT_DBURL		= "localhost:8889/";
	public static final String DEFAULT_DBNAME		= "_stamat";
	public static final String DEFAULT_DBUSER		= "root";
	public static final String DEFAULT_DBPWD		= "root";
	
	public static final String DEFAULT_MODELLDA		= "";
	public static final String DEFAULT_TEXTTYPE		= TEXTTYPE_ASCII;
	public static final int AUTODETECT_NUMKEYWORDS	= -1;
	public static final int AUTODETECT_NUMTOPICS	= -1;

	/**
	 * @author thomas
	 * getting rid of old stuff
	 * 
	 * public static final String DEFAULT_REL_LANG_MODELS_PATH = "data/langmodels/";
	 * public static final String DEFAULT_REL_LANG_STOPWORD_PATH = "data/stopwords/";
	 */
}
