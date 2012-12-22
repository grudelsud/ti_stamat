package stamat.controller.topic;

import java.io.File;

import stamat.util.StamatException;
import cc.mallet.topics.ParallelTopicModel;

/**
 * @author alisi
 *
 */

public class TrainedModel {
	
	File modelFile = null;
	ParallelTopicModel model = null;

	private TrainedModel() {
	}

	/**
	 * This function throws an instance if the model doesn't exist in the specified path.
	 * You should use createInstance in this case, which saves a new empty model ready to be trained.
	 * 
	 * @param path
	 * @return
	 * @throws StamatException 
	 * @throws Exception
	 */
	public static TrainedModel getInstance(String path) throws StamatException {
		TrainedModel tm = new TrainedModel();
		tm.modelFile = new File(path);
		try {
			tm.model = ParallelTopicModel.read(tm.modelFile);
		} catch (Exception e) {
			throw new StamatException(e);
		}
		return tm;
	}

	/**
	 * Creates a new instance of a paralleltopimodel on the filesystem and returns an instance of trainedmodel.
	 * Use this function if getIntstance throws an exception.
	 * 
	 * @param path
	 * @param numberOfTopics
	 * @return
	 */
	public static TrainedModel createInstance(String path, int numberOfTopics) {
		TrainedModel tm = new TrainedModel();
		ParallelTopicModel model = new ParallelTopicModel(numberOfTopics);
		tm.modelFile = new File(path);
		tm.saveModelFile(model);
		return tm;
	}

	public ParallelTopicModel getModel(){
		return this.model;
	}
	
	public void saveModelFile(ParallelTopicModel ldaModel){
		this.model= ldaModel;
		this.model.write(this.modelFile);
	}
}