/**
 * 
 */
package stamat.main.application;

import it.unifi.micc.homer.util.HomerException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.semanticmetadata.lire.DocumentBuilder;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.json.JSONObject;

import stamat.controller.visual.SearchResult;
import stamat.main.Analyser;
import stamat.util.StamatException;


/**
 * @author alisi
 *
 */
public class Main {

	@SuppressWarnings("static-access")
	private static Options buildOptions() {
		Options options = new Options();
		options.addOption("h", "help", false, "print this help and exit");
		
		/******************************************************************
		 * main switch, mutual exclusion
		 */ 
		OptionGroup ogMain = new OptionGroup();

		// language detect
		ogMain.addOption( OptionBuilder
				.hasArg(false)
				.withDescription("detect language of text input, requires options (-t | -tp) -lm -ls")
				.withLongOpt("language-detect")
				.create("L"));
		// entity extract
		ogMain.addOption( OptionBuilder
				.hasArg(false)
				.withDescription("entity extraction, requires options (-t | -tp) (-ecs -ep | -ecg). When using -ecg, Gate requires to be installed and home specified with parameter -Dgate.plugins.home=GATE_HOME")
				.withLongOpt("entity-extract")
				.create("E"));
		// topic model train
		ogMain.addOption( OptionBuilder
				.hasArg(false)
				.withDescription("train model, requires options (-t | -tp) -n -nk -lm -ls -m")
				.withLongOpt("topic-train")
				.create("Tt"));
		// topic infer
		ogMain.addOption( OptionBuilder
				.hasArg(false)
				.withDescription("infer topics from text using a reference model, requires options (-t | -tp) -n -lm -ls -m")
				.withLongOpt("topic-infer")
				.create("Ti"));
		// topic extract
		ogMain.addOption( OptionBuilder
				.hasArg(false)
				.withDescription("extract topics from text, requires options (-t | -tp) -n -nk -lm -ls")
				.withLongOpt("topic-extract")
				.create("Tx"));
		// visual index create
		ogMain.addOption( OptionBuilder
				.hasArg(false)
				.withDescription("create visual index, requires options -iI (-iP | -iF | -iU) -iId. Use -iI alone to create an empty index")
				.withLongOpt("visual-create-index")
				.create("Vc"));
		// visual index split
		ogMain.addOption( OptionBuilder
				.hasArg(false)
				.withDescription("split visual index, requires options -iI")
				.withLongOpt("visual-split-index")
				.create("Vs"));
		// visual similarity query
		ogMain.addOption( OptionBuilder
				.hasArg(false)
				.withDescription("query for visual similarity, requires options -iI -iP and -n")
				.withLongOpt("visual-query")
				.create("Vq"));
		
		options.addOptionGroup(ogMain);

		/******************************************************************
		 * other options, see requires above
		 */

		// image index path
		options.addOption( OptionBuilder
				.hasArg()
				.withArgName("imageIndexPath")
				.withDescription("full path of folder containing image index")
				.withLongOpt("image-index-path")
				.create("iI"));

		// image identifier
		options.addOption( OptionBuilder
				.hasArg()
				.withArgName("imageIdentifier")
				.withDescription("image identifier, used for indexing purposes")
				.withLongOpt("image-identifier")
				.create("iId"));

		// image URL
		options.addOption( OptionBuilder
				.hasArg()
				.withArgName("imageURL")
				.withDescription("URL of image")
				.withLongOpt("image-url")
				.create("iU"));

		// image folder path
		options.addOption( OptionBuilder
				.hasArg()
				.withArgName("imageFolderPath")
				.withDescription("full path of folder containing images")
				.withLongOpt("image-folder-path")
				.create("iF"));

		// image path
		options.addOption( OptionBuilder
				.hasArg()
				.withArgName("imagePath")
				.withDescription("full image path")
				.withLongOpt("image-path")
				.create("iP"));

		// model path
		options.addOption( OptionBuilder
				.hasArg()
				.withArgName("modelPath")
				.withDescription("lda model path")
				.withLongOpt("model-path")
				.create("m"));

		// stanford entity classifier
		options.addOption( OptionBuilder
				.hasArg(false)
				.withArgName("entityClassifierStanford")
				.withDescription("use Stanford NER")
				.withLongOpt("entity-class-stanford")
				.create("ecs"));

		// stanford entity classifier
		options.addOption( OptionBuilder
				.hasArg(false)
				.withArgName("entityClassifierGate")
				.withDescription("use Stanford GATE Annie")
				.withLongOpt("entity-class-gate")
				.create("ecg"));

		// entity classifier path
		options.addOption( OptionBuilder
				.hasArg()
				.withArgName("entityClassPath")
				.withDescription("entity classifier path (either stanford classifier or gate home would use the same parameter)")
				.withLongOpt("entity-class-path")
				.create("ep"));

		// number of outputs
		options.addOption( OptionBuilder
				.hasArg()
				.withArgName("num")
				.withDescription("number of outputs (topics / results)")
				.withLongOpt("num-outputs")
				.withType(Number.class)
				.create("n"));

		// number of keywords per topic
		options.addOption( OptionBuilder
				.hasArg()
				.withArgName("num")
				.withDescription("number of keywords per topic")
				.withLongOpt("num-keywords")
				.withType(Number.class)
				.create("nk"));

		// text input
		options.addOption( OptionBuilder
				.hasArg()
				.withArgName("textInput")
				.withDescription("text input from command line, can be used combined with -tp")
				.withLongOpt("text")
				.create("t"));

		// text input
		options.addOption( OptionBuilder
				.hasOptionalArgs()
				.withArgName("f1 ... fn d1 ... dn")
				.withDescription("space separated sequence of files and/or directories containing text input, can be used combined with -t")
				.withLongOpt("text-path")
				.create("tp"));

		// language profiles folder
		options.addOption( OptionBuilder
				.hasArg()
				.withArgName("langModelsPath")
				.withDescription("full path of folder containing language models")
				.withLongOpt("lang-models")
				.create("lm"));

		// stopwords folder
		options.addOption( OptionBuilder
				.hasArg()
				.withArgName("langStopwordPath")
				.withDescription("full path of folder containing stopwords")
				.withLongOpt("lang-stopwords")
				.create("ls"));
		return options;
	}

	private static void cmdRunner(CommandLine line, Options options) {
		if( line.hasOption("h") ) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(new PrintWriter(System.out, true), 120, "java -Djava.util.logging.config.file=stamat-logging.properties -jar stamat.main-cmdline.jar", "options:", options, 2, 4, "", true);
			return;
		}

		// grab all the options available on the command line
		String text = line.getOptionValue("t");
		String[] textPaths = line.getOptionValues("tp");
		String modelPath = line.getOptionValue("m");
		String langModels = line.getOptionValue("lm");
		String langStopwords = line.getOptionValue("ls");
		String entityClassifierPath = line.getOptionValue("ep");

		String indexPath = line.getOptionValue("iI");

		String imagePath = line.getOptionValue("iP");
		String imageFolderPath = line.getOptionValue("iF");
		String imageURL = line.getOptionValue("iU");
		String imageIdentifier = line.getOptionValue("iId");

		int numOutputs = 0;
		int numKeywords = 0;

		List<String> texts = new ArrayList<String>();

		if( text != null ) {
			texts.add(text);			
		}
		if( textPaths != null ) {
			try {
				for(String textPath : textPaths) {
					File file = new File(textPath);
					if( file.isDirectory() ) {
						File[] files = file.listFiles();
						for(File input : files) {
							texts.add(Main.fileReader(input));
						}
					} else if( file.isFile() ) {
						texts.add(Main.fileReader(file));
					}
				}
			} catch(Exception e) {
				System.out.println("Check text input!");
				return;
			}			
		}

		try {
			if( line.getOptionValue("n") != null ) {
				numOutputs = ((Number)line.getParsedOptionValue("n")).intValue();
			}
			if( line.getOptionValue("nk") != null ) {
				numKeywords = ((Number)line.getParsedOptionValue("nk")).intValue();
			}
		} catch (ParseException e) {
			// do nothing since values are initialized with 0 and result will make no sense
			System.out.println("Wrong input format! " + e.getMessage());
			return;
		}

		// cool, now run the main "switch" for command line options
		// image query
		if( line.hasOption("Vq")) {
			if(imagePath == null | indexPath == null | numOutputs == 0) {
				System.out.println("With -Vq use options: -iI -iP -n");
			} else {
				List<SearchResult> results = Analyser.visual.queryFromPath(imagePath, indexPath, numOutputs);
				for( SearchResult res : results ) {
					System.out.println(res.getPosition() + " - " +  res.getResult() + " [" + res.getSimilarity() + "]");
				}				
			}
			return;

		// split index
		} else	if( line.hasOption("Vs")) {
				if( indexPath == null ) {
					System.out.println("With -Vs use options: -iI");
				} else {
					try {
						Analyser.visual.splitIndex(indexPath);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				return;

		// visual index create
		} else if( line.hasOption("Vc")) {
			if( indexPath == null) {
				System.out.println("create visual index, requires options -iI (-iP | -iF | -iU) -iId. Use -iI alone to create an empty index.");
			} else {
				if( imageURL != null ) {
					HashMap<String, String> fields = new HashMap<String, String>();
					fields.put(DocumentBuilder.FIELD_NAME_IDENTIFIER, imageIdentifier == null ? imageURL : imageIdentifier);
					try {
						Analyser.visual.updateIndexfromURL(indexPath, imageURL, fields);
					} catch (StamatException e) {
						e.printStackTrace();
					}
//					Analyser.visual.updateIndexCEDDfromURL(indexPath, imageURL, imageURL);
				} else if( imagePath != null ) {
					Analyser.visual.updateIndexCEDDfromPath(indexPath, imagePath, imagePath);
				} else if( imageFolderPath != null ) {
					Analyser.visual.updateIndexCEDDfromFolder(indexPath, imageFolderPath);
				} else {
					Analyser.visual.createEmptyIndex(indexPath, null);
				}
			}
			return;

		// topic extract
		} else if( line.hasOption("Tx")) {
			if( texts.size() < 1 | numOutputs == 0 | numKeywords == 0 | langModels == null | langStopwords == null ) {
				System.out.println("With -Tx use options: (-t | -tp) -n -nk -lm -ls");
			} else {
				JSONObject result = Analyser.topic.extract2JSON(texts, numOutputs, numKeywords, langModels, langStopwords);
				System.out.println(result.toString());
			}
			return;

		// topic infer
		} else if( line.hasOption("Ti")) {
			if( texts.size() < 1 | numOutputs == 0 | modelPath == null | langModels == null | langStopwords == null ) {
				System.out.println("With -Ti use options: (-t | -tp) -n -lm -ls -m");
			} else {
				// TODO: check topic infer function, not sure it's working properly
				JSONObject result = Analyser.topic.infer2JSON(texts, langModels, langStopwords, numOutputs, modelPath);
				System.out.println(result.toString());
			}
			return;

		// topic model train
		} else if( line.hasOption("Tt")) {
			if( texts.size() < 1 | numOutputs == 0 | langModels == null | langStopwords == null | modelPath == null ) {
				System.out.println("With -Tt use options: (-t | -tp) -n -lm -ls -m");
			} else {
				JSONObject result = Analyser.topic.trainModel2JSON(texts, numOutputs, langModels, langStopwords, modelPath);
				System.out.println(result.toString());
			}
			return;

		// entity extract
		} else if( line.hasOption("E")) {
			boolean stanfordParCheck = line.hasOption("ecs") ? line.hasOption("ecs") && (entityClassifierPath != null) : true;
			if( texts.size() < 1 || !stanfordParCheck ) {
				System.out.println("With -E use options: (-t | -tp) (-ecs -ep | -ecg) -ep. When using -ecg, Gate requires to be installed and home specified with parameter -Dgate.plugins.home=GATE_HOME");
			} else {
				JSONObject result = new JSONObject();
				if(line.hasOption("ecs")) {
					result = Analyser.ned.extractStanford2JSON(texts.toString(), entityClassifierPath);					
				} else if(line.hasOption("ecg")) {
					result = Analyser.ned.exractAnnie2JSON(texts.toString());
				}
				System.out.println(result.toString());
			}
			return;

		// language detect
		} else if( line.hasOption("L")) {
			if( texts.size() < 1 | langModels == null | langStopwords == null ) {
				System.out.println("With -L use options: (-t | -tp) -lm -ls");
			} else {			
				JSONObject result = Analyser.language.detection2JSON(text, langModels, langStopwords);
				System.out.println(result.toString());
			}
			return;
			
		} else {
			System.out.println("Type 'java -jar stamat.main-cmdline.jar -h' for help");			
		}
	}

	private static String fileReader(File file) throws HomerException
	{
		StringBuffer sb = new StringBuffer();
		try {
			FileInputStream fis = new FileInputStream(file);
			int ch;
			while((ch = fis.read()) != -1) {
				sb.append((char)ch);
			}
			fis.close();
		} catch(Exception e) {
			throw new HomerException(e);
		}
		return sb.toString();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Options options = Main.buildOptions();

		try {
			CommandLineParser parser = new PosixParser();
			CommandLine line = parser.parse(options, args);
			Main.cmdRunner(line, options);
		} catch( Exception e ) {
			e.printStackTrace();
		}
	}

}
