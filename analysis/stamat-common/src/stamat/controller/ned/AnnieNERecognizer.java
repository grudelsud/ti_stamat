package stamat.controller.ned;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.Document;
import gate.DocumentContent;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.ProcessingResource;
import gate.corpora.DocumentContentImpl;
import gate.creole.ANNIEConstants;
import gate.creole.ResourceInstantiationException;
import gate.creole.SerialAnalyserController;
import gate.util.GateException;
import it.unifi.micc.homer.util.StringOperations;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import stamat.model.KeywordType;
import stamat.model.NamedEntity;

public class AnnieNERecognizer {

	Logger logger = Logger.getLogger(AnnieNERecognizer.class.getName());

	/** Contains Annie for NED */
	private static AnnieNERecognizer instance = null;

	/** The Corpus Pipeline application to contain ANNIE */
	private static SerialAnalyserController annieController = null;

	//	private static AnnieNEDAnalyser annie = null;

	int minNEDLength = 2; // entities must be > than minNEDLength

	private AnnieNERecognizer() {
		init();
	}

	private AnnieNERecognizer(String gateHomePath) {
		init(gateHomePath);
	}

	public static AnnieNERecognizer getInstance() {
		if (instance == null) {
			instance = new AnnieNERecognizer();
		}
		return instance;
	}

	public static AnnieNERecognizer getInstance(String gateHomePath) {
		if (instance == null) {
			instance = new AnnieNERecognizer(gateHomePath);
		}
		return instance;
	}

	private void init(String gateHomePath)
	{
		System.setProperty("gate.plugins.home", gateHomePath);
		this.init();
	}

	/** Init Gate e call the method for to initialize Annie */
	private void init()
	{
		String gateHomePath = System.getProperty("gate.plugins.home");
		try {
			// Initialize the GATE library
			Gate.init();

			// Load ANNIE plugin
			File gateHome = new File(gateHomePath);
			File pluginsHome = new File(gateHome, "plugins");
			Gate.getCreoleRegister().registerDirectories(new File(pluginsHome, "ANNIE").toURI().toURL());

			// Initialize ANNIE (this may take several hundreds of years, but it's loaded only once...)
			if (annieController == null) {
				// create a serial analyser controller to run ANNIE with
				annieController = (SerialAnalyserController) Factory.createResource("gate.creole.SerialAnalyserController", Factory.newFeatureMap(), Factory.newFeatureMap(),"ANNIE_" + Gate.genSym());
				// load each PR as defined in ANNIEConstants
				for (int i = 0; i < ANNIEConstants.PR_NAMES.length; i++) {
					FeatureMap params = Factory.newFeatureMap();
					// use default parameters
					ProcessingResource pr = (ProcessingResource) Factory.createResource(ANNIEConstants.PR_NAMES[i], params);
					// add the PR to the pipeline controller
					annieController.add(pr);
				}
				Corpus corpus = (Corpus) Factory.createResource("gate.corpora.CorpusImpl");
				// add a Corpus to Annie (we will use only single documents for NER, sill annie wants a non-null corpus
				annieController.setCorpus(corpus);

			}
		} catch (GateException ge) {
			logger.log(Level.WARNING, "exception caught: " + ge.getMessage());
		} catch (IOException io) {
			logger.log(Level.WARNING, "exception caught: " + io.getMessage());
		}
	}

	public ArrayList<NamedEntity> extractEntity(String text, ArrayList<KeywordType> type) {

		ArrayList<NamedEntity> entities = new ArrayList<NamedEntity>();

		try {
			/**
			 * From Gate Docs - NOTE: if at the time when execute() is invoked, the document is not null, 
			 * it is assumed that this controller is invoked from another controller and only this document 
			 * is processed while the corpus (which must still be non-null) is ignored. If the document is 
			 * null, all documents in the corpus are processed in sequence.
			 */
			Document doc = (Document) Factory.createResource("gate.corpora.DocumentImpl");
			DocumentContent content = new DocumentContentImpl(text);
			doc.setContent(content);

			// fill Annie with contect and run the entity recognizer
			annieController.setDocument(doc);
			annieController.execute();

			/**
			 *  TODO: this is perfect marco bertini style, immensely cumbersome. why on earth 
			 *  we should take an arraylist, pour it into an hashset, then convert it to another set
			 *  of gate annotations?
			 */
			
			// get all annotations of Document
			AnnotationSet defaultAnnotation = doc.getAnnotations();

			// set a list of annotations' names that you want
			Set<String> annotTypesRequired = new HashSet<String>();
			if (type.contains(KeywordType.ALLENTS)) {
				for (int i = 0; i < KeywordType.values().length; i++) {
					if (KeywordType.values()[i] != KeywordType.ALLENTS)
						annotTypesRequired.add(KeywordType.values()[i].toString());
				}
			} else {
				for (int i = 0; i < type.size(); i++) {
					annotTypesRequired.add(type.get(i).toString());
				}
			}

			// select from list of all annotations only those that you have selected
			Set<Annotation> annotRequired = new HashSet<Annotation>(defaultAnnotation.get(annotTypesRequired));

			Iterator<Annotation> it = annotRequired.iterator();
			while (it.hasNext()) {
				Annotation auxAnnotation = it.next();
				Iterator<NamedEntity> iter;
				int index = -1;
				String entity = "";
				// entities must have more than minNEDLength characters
				if (auxAnnotation.getEndNode().getOffset().intValue() - auxAnnotation.getStartNode().getOffset().intValue() > minNEDLength) {
					boolean occurrence = false;
					boolean substitute = false;
					entity = StringOperations.tokenizeAndCorrect(doc.getContent().getContent(new Long(auxAnnotation.getStartNode().getOffset().intValue()), new Long(auxAnnotation.getEndNode().getOffset().intValue())).toString());
					iter = entities.iterator();
					
					// 1st check whether the entity already exist
					while (iter.hasNext() && occurrence == false && substitute == false) {
						NamedEntity ed = iter.next();
						if (ed.getKeyword().equals(entity) == true || ed.getKeyword().length() > entity.length() && ed.getKeyword().indexOf(entity) >= 0) {
							occurrence = true;
						} else if (ed.getKeyword().length() < entity.length() && entity.indexOf(ed.getKeyword()) >= 0) {
							substitute = true;
							index = entities.indexOf(ed);
						}
					}
					
					// then create the entity if doesn't exist or update with new offset values
					if (occurrence == false && substitute == false) {
						NamedEntity currEntity = new NamedEntity();
						currEntity.setStart((auxAnnotation.getStartNode().getOffset().intValue()));
						currEntity.setEnd((auxAnnotation.getEndNode().getOffset().intValue()));
						currEntity.setKeyword(entity.trim());
						currEntity.setType(KeywordType.fromString(auxAnnotation.getType()));
						entities.add(currEntity);
					} else if (substitute == true) {
						NamedEntity currEntity = new NamedEntity();
						currEntity.setStart((auxAnnotation.getStartNode().getOffset().intValue()));
						currEntity.setEnd((auxAnnotation.getEndNode().getOffset().intValue()));
						currEntity.setKeyword(entity.trim());
						currEntity.setType(KeywordType.fromString(auxAnnotation.getType()));
						entities.set(index, currEntity);
					}
				}
			}
			// clean up, hope to solve memory leaks...
			Factory.deleteResource(doc);
			Collections.sort(entities);
		} catch (ResourceInstantiationException e) {
			logger.log(Level.WARNING, "gate threw a ResourceInstantiationException: " + e.getMessage());
		} catch (GateException e) {
			logger.log(Level.WARNING, "gate threw a GateException: " + e.getMessage());
		}
		return entities;
	}
}