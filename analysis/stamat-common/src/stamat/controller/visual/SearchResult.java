package stamat.controller.visual;


import org.json.JSONException;
import org.json.JSONObject;

public class SearchResult implements Comparable<SearchResult>{
	private String result = "";
	private String URL = "";
	private int position = 0;
	private double similarity = 0;

	/**
	 * Added new constructor with additional parameter URL
	 * 
	 * @param result
	 * @param position
	 * @param similarity
	 */
	@Deprecated
	public SearchResult(String result, int position, double similarity) {
		super();
		this.result = result;
		this.position = position;
		this.similarity = similarity;
	}

	/**
	 * @param result
	 * @param URL
	 * @param position
	 * @param similarity
	 */
	public SearchResult(String result, String URL, int position, double similarity) {
		super();
		this.result = result;
		this.URL = URL;
		this.position = position;
		this.similarity = similarity;
	}
	
	/**
	 * @return
	 */
	public JSONObject toJSONItem(){
			
		JSONObject oJson = new JSONObject();
		try {
			oJson.put("imageResult", this.result);
			oJson.put("imageURL", this.URL);
			oJson.put("position", this.position);
			oJson.put("similarity", this.similarity);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return oJson;
	}

	/**
	 * @return
	 */
	public String getResult() {
		return this.result;
	}

	/**
	 * @return
	 */
	public String getURL() {
		return this.URL;
	}
	/**
	 * @return
	 */
	public int getPosition() {
		return this.position;
	}

	/**
	 * @return
	 */
	public double getSimilarity() {
		return this.similarity;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SearchResult sr) {
		if(this.similarity < sr.getSimilarity()){
			return 1;
		} else if(this.similarity > sr.getSimilarity()){
			return -1;
		} else {
			return 0;
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString(){
		return this.result + " " + this.URL + " " + this.similarity;
	}
}
