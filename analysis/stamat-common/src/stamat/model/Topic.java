/**
 * 
 */
package stamat.model;

import java.util.ArrayList;
import java.util.List;


/**
 * @author bertini
 *
 */
public class Topic {
	private List<TopicWord> words;
	private double alpha;
	private String language;
	
	public Topic(double alpha, String language){
		this.words = new ArrayList<TopicWord>();
		this.alpha = alpha;
		this.language = language;
	}
	
	public double getAlpha(){
		return alpha;
	}
	
	public void addWord(TopicWord word){
		this.words.add(word);
	}
	
	public List<TopicWord> getWords(){
		return words;
	}
	
	public String getLanguage(){
		return language;
	}
}
