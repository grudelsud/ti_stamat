package models;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import models.dbo.Feeditemmedia;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import play.libs.Json;
import stamat.controller.visual.SearchResult;
import stamat.model.SemanticKeyword;

public class Utils {

	public static JsonNode feeditemmediaList2JSON(List<Feeditemmedia> feeditemmediaList)
	{
		ObjectNode dummyObject = Json.newObject();
		ArrayNode result = dummyObject.putArray("dummyKey");
		try {
			for(Feeditemmedia item : feeditemmediaList) {
				ObjectNode searchResultNode = result.addObject();
				
				searchResultNode.put("id", item.id);
				searchResultNode.put("url", item.abs_path + item.hash);
				searchResultNode.put("width", item.width);
				searchResultNode.put("height", item.height);
				searchResultNode.put("url_src", item.url);
				searchResultNode.put("flags", item.flags);
			}			
		} catch (NullPointerException e) {
		}
		return result;
	}

	public static JsonNode searchResultList2JSON(List<SearchResult> searchResultList)
	{
		ObjectNode dummyObject = Json.newObject();
		ArrayNode result = dummyObject.putArray("dummyKey");
		try {
			for(SearchResult searchResult : searchResultList) {
				ObjectNode searchResultNode = result.addObject();
				
				searchResultNode.put("result", searchResult.getResult());
				searchResultNode.put("url", searchResult.getURL());
				searchResultNode.put("position", searchResult.getPosition());
				searchResultNode.put("similarity", searchResult.getSimilarity());
			}			
		} catch (NullPointerException e) {
		}
		return result;
	}

	public static JsonNode semanticKeywordList2JSON(List<? extends SemanticKeyword> semanticKeywordList)
	{
		ObjectNode dummyObject = Json.newObject();
		ArrayNode result = dummyObject.putArray("dummyKey");
		for(SemanticKeyword semanticKeyword : semanticKeywordList) {
			ObjectNode semKeywordNode = result.addObject();

			semKeywordNode.put("keyword", semanticKeyword.getKeyword());
			semKeywordNode.put("type", semanticKeyword.getType().toString());
			semKeywordNode.put("confidence", semanticKeyword.getConfidence());
			semKeywordNode.put("num_occurences", semanticKeyword.getNumOccurrences());
			semKeywordNode.put("tf", semanticKeyword.getTf());
		}
		return result;
	}

	public static JsonNode map2JSON(Map<? extends Object, ? extends Object> map)
	{
		ObjectNode dummyObject = Json.newObject();
		ArrayNode result = dummyObject.putArray("dummyKey");
		for(Entry<?, ?> entry : map.entrySet()) {
			ObjectNode mapElementNode = result.addObject();
			if(entry.getKey().getClass() == String.class) {
				String key = (String)entry.getKey();
				if(entry.getValue().getClass() == Float.class) {
					Float val = (Float)entry.getValue();
					mapElementNode.put(key, val);
				} else if(entry.getValue().getClass() == String.class) {
					String val = (String)entry.getValue();
					mapElementNode.put(key, val);
				}
			}
		}
		return result;
	}

	@Deprecated
	public static JsonNode mapSF2JSON(Map<String, Float> map)
	{
		ObjectNode dummyObject = Json.newObject();
		ArrayNode result = dummyObject.putArray("dummyKey");
		for(String key : map.keySet()) {
			ObjectNode mapElementNode = result.addObject();
			mapElementNode.put(key, map.get(key));
		}
		return result;
	}

	public static ObjectNode returnSuccess(String message)
	{
		ObjectNode result = Json.newObject();
		result.put("success", message);
		return result;
	}

	public static ObjectNode returnSuccess(JsonNode data)
	{
		ObjectNode result = Json.newObject();
		result.put("success", data);
		return result;
	}

	public static ObjectNode returnError(String message)
	{
		ObjectNode result = Json.newObject();
		result.put("error", message);
		return result;
	}
}
