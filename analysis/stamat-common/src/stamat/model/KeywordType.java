/**
 * 
 */
package stamat.model;

/**
 * @author bertini
 *
 */
public enum KeywordType {
	ALLENTS, PERSON, LOCATION, DATE, TIME, MONEY, TOPIC, PERCENT, ORGANIZATION, FROMTAG, LANGUAGE, TAG, UNDEF;
	
	public String toString() {
		switch (this) {
			case ALLENTS:	return "Allents";
			case PERSON:		return "Person";
			case ORGANIZATION:	return "Organization";
			case LOCATION:		return "Location";
			case DATE:		return "Date";
			case TIME:		return "Time";
			case MONEY:	return "Money";
			case TOPIC:		return "Topic";
			case PERCENT:	return "Percent";
			case FROMTAG:	return "Fromtag";
			case LANGUAGE:	return "Language";
			case TAG:		return "Tag";
		}
		return "undef";
	}
	
	public static KeywordType fromString(String op) {
		if(op.equals("Allents"))	return ALLENTS;
		if(op.equals("Person"))		return PERSON;
		if(op.equals("Organization"))	return ORGANIZATION;
		if(op.equals("Location"))		return LOCATION;
		if(op.equals("Date"))		return DATE;
		if(op.equals("Time"))		return TIME;
		if(op.equals("Money"))		return MONEY;
		if(op.equals("Topic"))		return TOPIC;
		if(op.equals("Percent"))	return PERCENT;
		if(op.equals("Fromtag"))	return FROMTAG;
		if(op.equals("Language"))	return LANGUAGE;
		if(op.equals("Tag"))		return TAG;
		return UNDEF;
	}
}
