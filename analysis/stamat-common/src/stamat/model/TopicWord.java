/**
 * 
 */
package stamat.model;

/**
 * @author bertini
 *
 */
public class TopicWord extends SemanticKeyword {
	private String word;
	private double weight;
	private int count;
	/**
	 * @param word
	 * @param weight
	 */
	public TopicWord(String word, double weight) {
		super();
		this.word = word;
		this.weight = weight;
		this.count = 1;
	}
	/**
	 * @return the word
	 */
	public String getWord() {
		return word;
	}
	/**
	 * @param word the word to set
	 */
	public void setWord(String word) {
		this.word = word;
	}
	/**
	 * @return the weight
	 */
	public double getWeight() {
		return weight;
	}
	/**
	 * @param weight the weight to set
	 */
	public void setWeight(double weight) {
		this.weight = weight;
	}
	/**
	 * @return the count
	 */
	public int getCount() {
		return count;
	}
	/**
	 * @param count the count to set
	 */
	public void setCount(int count) {
		this.count = count;
	}


}
