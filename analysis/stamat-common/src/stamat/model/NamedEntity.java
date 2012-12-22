/**
 * 
 */
package stamat.model;


/**
 * @author bertini
 * 
 */
public class NamedEntity extends SemanticKeyword implements Comparable<Object> {

	/** Contains the start position of entity's value */
	private int start = 0;
	/** Contains the start position of entity's value */
	private int end = 0;

	public NamedEntity() {	
	}

	public NamedEntity(KeywordType type, String keyword)
	{
		super();
		this.type = type;
		this.keyword = keyword;
	}

	public int compareTo(Object o) 
	{ 
		// TODO check: is this really needed ?
		NamedEntity aux = (NamedEntity) o;
		if (this.getStart() < aux.getStart())
			return -1;
		else if (this.getStart() == aux.getStart())
			return 0;
		else
			return 1;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}
}
