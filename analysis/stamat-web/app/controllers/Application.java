package controllers;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.PersistenceException;

import models.Constants;
import models.Utils;
import models.dbo.Feeditemmedia;
import models.dbo.Users;
import models.requests.EntitiesExtract;
import net.semanticmetadata.lire.DocumentBuilder;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;

import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.Akka;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import stamat.controller.visual.SearchResult;
import stamat.main.Analyser;
import stamat.model.NamedEntity;
import stamat.util.StamatException;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;
import views.html.index;

public class Application extends Controller {

	public static Result index()
	{
		return ok(index.render("STAMAT - API Backend"));
	}

	public static Result rankNews()
	{
		JsonNode json = request().body().asJson();
		if (json == null) 
		{
			return badRequest(Utils.returnError("expecting JSON request. please check that content-type is set to \"application/json\" and request body is properly encoded (e.g. JSON.stringify(data))"));
		} 
		else 
		{
			String user_name = "";
			String user_auth = "";
			Map<String, String> news = new HashMap<String, String>();
			Users user_auth_data = null;
			try 
			{
				user_name = json.get(Constants.json_fields.TWITTER_USER_NAME).getTextValue();
				user_auth = json.get(Constants.json_fields.TWITTER_AUTH_NAME).getTextValue();
				Iterator<JsonNode> newsIterator = json.get(Constants.json_fields.RANKNEWS_NEWS).getElements();
				while (newsIterator.hasNext()) 
				{
					JsonNode imageJson = newsIterator.next();
					// field id might be an integer, using "asText" to stay on a safe side
					String id = imageJson.get(Constants.json_fields.RANKNEWS_NEWS_ID).asText();
					String text = imageJson.get(Constants.json_fields.RANKNEWS_NEWS_TEXT).getTextValue();
					news.put(id, text);
				}
				user_auth_data = Users.find.where().eq("screen_name", user_auth).findUnique();
			} 
			catch (NullPointerException e) 
			{
				return badRequest(Utils.returnError("wrong json structure"));
			} 
			catch (PersistenceException pe) 
			{
				return badRequest(Utils.returnError("field "+Constants.json_fields.TWITTER_AUTH_NAME+" didn't return exactly 1 result"));
			}
			if (user_auth_data != null && !user_auth_data.oauth_token.isEmpty() ) 
			{
				ConfigurationBuilder cb = new ConfigurationBuilder();
				cb.setOAuthConsumerKey("XW7zty39b9veVxAbN444g");
				cb.setOAuthConsumerSecret("iw8tJREZoAsoBFMignPwDyCmgKvdFbr255WNcP9a7c");
				TwitterFactory tf = new TwitterFactory(cb.build());
				AccessToken at = new AccessToken(user_auth_data.oauth_token, user_auth_data.oauth_token_secret);
				Twitter twitter = tf.getInstance(at);

				try 
				{
					List<twitter4j.Status> result = null;
					boolean homeTimeline = false;
					if (user_name.length() > 0) 
					{
						// user_name specified, we're pulling content from the timeline specified
						result = twitter.getUserTimeline(user_name);
					}
					else
					{
						// user_name is empty, we're pulling tweets from users followed by authenticating user
						homeTimeline = true;
						result = twitter.getHomeTimeline();
					}
					Map<String, String> tweets = new HashMap<String, String>();
					Map<String, Float> tweetBoosts = new HashMap<String, Float>();
					for (twitter4j.Status status : result) 
					{
						String statusId = Long.toString(status.getId());
						float boost = (float)(1 + Math.log(1.0d + (double)status.getRetweetCount()));
						if (homeTimeline) 
						{
							twitter4j.User user = status.getUser();
							int followers = user.getFollowersCount();
							int friends = user.getFriendsCount();
							boost += (float)(1 + Math.log(1.0d + (double)followers / (double)friends));
						}
						tweets.put(statusId, status.getText());
						tweetBoosts.put(statusId, boost);
					}
					Map<String, Float> ranking = Analyser.ranking.news(news, tweets, tweetBoosts);
					ObjectNode container = Json.newObject();
					container.put("ranking", Utils.map2JSON(ranking));
					container.put("tweets", Utils.map2JSON(tweets));
					container.put("tweetboosts", Utils.map2JSON(tweetBoosts));
					return ok(Utils.returnSuccess(container));
				} 
				catch (TwitterException e) 
				{
					return ok(Utils.returnError("not really your lucky day, got a twitter exception " + e.getMessage()));
				}
			} 
			else 
			{
				return badRequest(Utils.returnError("User [" + user_auth + "] doesn't have rights to access the Twitter API"));
			}
			
//			JsonNode imageListJson = json.findValue(Constants.json_fields.INDEX_FIELD_IMAGES);
//			if( imageListJson != null ) {
//				Iterator<JsonNode> imageListJsonIterator = imageListJson.getElements();
//				String message = "indices added to " + indexPath + ": ";
//				while(imageListJsonIterator.hasNext()) {
//					
//				}
//			}
		}
	}

	public static Result entitiesExtractGATE()
	{
		Form<EntitiesExtract> form = form(EntitiesExtract.class).bindFromRequest();
		if( form.hasErrors() ) {
			return badRequest(Utils.returnError("invalid request"));
		}
		EntitiesExtract entityExtractRequest = form.get();

		final String text = entityExtractRequest.text;
		final String gateHomePath = play.Play.application().configuration().getString("stamat.gatehome");

		// right the slow code asynchronously
		Promise<ArrayList<NamedEntity>> promiseOfNamedentitylist = Akka.future(
			new Callable<ArrayList<NamedEntity>>() {
				public ArrayList<NamedEntity> call() {
					ArrayList<NamedEntity> list = Analyser.ned.exractAnnie(text, gateHomePath);

					// tracing memory right before returning the result
					String runtime = Application.memInfo() + "ned.GATE " + list.size();
					Logger.info(runtime);
					return list;
				}
			}
		);

		return async(
			promiseOfNamedentitylist.map(
				new Function<ArrayList<NamedEntity>, Result>() {
					public Result apply(ArrayList<NamedEntity> result) {
						JsonNode resultJson = Utils.semanticKeywordList2JSON(result);
						return ok(Utils.returnSuccess(resultJson));
					}
				}
			)
		);		
	}

	public static Result entitiesExtractSNER()
	{
		Form<EntitiesExtract> form = form(EntitiesExtract.class).bindFromRequest();
		
		if( form.hasErrors() ) {
			return badRequest(Utils.returnError("invalid request"));
		}
		EntitiesExtract entityExtractRequest = form.get();

		String classifierPath = Constants.FOLDER_3CLASS;
		ArrayList<NamedEntity> namedEntityList = Analyser.ned.extractStanford(entityExtractRequest.text, classifierPath);
		JsonNode result = Utils.semanticKeywordList2JSON(namedEntityList);
		return ok(Utils.returnSuccess(result));
	}
	
	public static Result visualIndex()
	{
		return TODO;
	}		

	@Deprecated
	public static Result visualNewIndex()
	{
		DynamicForm form = form().bindFromRequest();
		String name = form.get("name");
		Pattern p = Pattern.compile("^\\w{1,50}$");
		Matcher m = p.matcher(name);
		if( !m.matches() ) {
			return badRequest(Utils.returnError("invalid request, name must be between 1 and 50 alphanumerical characters"));				
		}
		StringBuilder returnMsg = new StringBuilder();
		String indexPath = Constants.FOLDER_INDICES + "/" + name;
		int returnCode = Analyser.visual.createEmptyIndex(indexPath, returnMsg);
		if( returnCode == Analyser.constants.SUCCESS ) {
			return ok(Utils.returnSuccess(returnMsg.toString()));			
		} else {
			return badRequest(Utils.returnError(returnMsg.toString()));
		}
	}		

	public static Result visualIndexImagesFromDB()
	{
		// read json post params
		JsonNode json = request().body().asJson();
		final String indexPath;
		final int num;
		if(json == null) {
			return badRequest(Utils.returnError("expecting JSON request. please check that content-type is set to \"application/json\" and request body is properly encoded (e.g. JSON.stringify(data))"));
		} else {
			try {
				num = json.get(Constants.json_fields.QUERY_FIELD_NUMOFRESULT).getIntValue();
				indexPath = Constants.FOLDER_INDICES + "/" + json.get(Constants.json_fields.INDEX_FIELD_INDEX).getTextValue();
			} catch(NullPointerException e) {
				return badRequest(Utils.returnError("Missing field '"+Constants.json_fields.QUERY_FIELD_NUMOFRESULT+"' or '"+Constants.json_fields.INDEX_FIELD_INDEX+"'"));						
			}			
		}

		// create the promise
		Promise<List<Feeditemmedia>> promiseOfFeeditemmediaList = Akka.future(
			new Callable<List<Feeditemmedia>>() 
			{
				public List<Feeditemmedia> call() 
				{
					// fetch 10 downloaded items (not already queued for indexing)
					List<Feeditemmedia> list = Feeditemmedia.find
							.where().eq("flags", Constants.db_fields.MEDIA_DOWNLOADED)
							.order().desc("id")
							.setMaxRows(num)
							.findList();

					// mark them as queued
					for(Feeditemmedia item : list) {
						item.flags = Constants.db_fields.MEDIA_DOWNLOADED | Constants.db_fields.MEDIA_QUEUEDFORINDEXING;
						item.update();
					}

					Logger.info("indexing " + num + " images");
					// start the indexing process
					for(Feeditemmedia item : list) {
						String id = Long.toString(item.id);
						String url = item.abs_path + item.hash;

						HashMap<String, String> indexedFields = new HashMap<String, String>();
						indexedFields.put(Analyser.constants.SEARCH_URL, url);
						indexedFields.put(DocumentBuilder.FIELD_NAME_IDENTIFIER, id);

						try {
							// if everything all right, mark them as indexed
							Analyser.visual.updateIndexfromURL(indexPath, url, indexedFields);
							item.flags = item.flags | Constants.db_fields.MEDIA_INDEXED;

							// tracing memory right before the next cycle
							String runtime = Application.memInfo() + "visual.indexDB "+id+" -> " + indexPath;
							Logger.info(runtime);
						} catch (StamatException e) {
							// if exception caught, mark them as unresolved
							item.flags = item.flags | Constants.db_fields.MEDIA_INDEXINGEXCEPTION;
							Logger.error("visualIndexImagesFromDB - error while indexing {id="+id+", url="+url+"} message: " + e.getMessage());
						}
						// save db item
						item.update();
					}
					return list;
				}
			}
		);
		
		// asynchronously return results
		return async(
			promiseOfFeeditemmediaList.map(
				new Function<List<Feeditemmedia>, Result>() 
				{
					public Result apply(List<Feeditemmedia> list) 
					{
						return ok(Utils.returnSuccess(Utils.feeditemmediaList2JSON(list)));
					}
				}
			)
		);
	}

	public static Result visualIndexImagesFromJSON()
	{
		JsonNode json = request().body().asJson();
		if(json == null) {
			return badRequest(Utils.returnError("expecting JSON request. please check that content-type is set to \"application/json\" and request body is properly encoded (e.g. JSON.stringify(data))"));
		} else {
			String indexPath = "";
			try {
				indexPath = Constants.FOLDER_INDICES + "/" + json.get(Constants.json_fields.INDEX_FIELD_INDEX).getTextValue();				
			} catch(NullPointerException e) {
				return badRequest(Utils.returnError("Missing field '"+Constants.json_fields.INDEX_FIELD_INDEX+"'"));
			}
			JsonNode imageListJson = json.findValue(Constants.json_fields.INDEX_FIELD_IMAGES);
			if( imageListJson != null ) {
				Iterator<JsonNode> imageListJsonIterator = imageListJson.getElements();
				String message = "indices added to " + indexPath + ": ";
				while(imageListJsonIterator.hasNext()) {
					JsonNode imageJson = imageListJsonIterator.next();

					String id = "", url = "";
					try {
						// field id might be an integer, using "asText" to stay on a safe side
						id = imageJson.get(Constants.json_fields.INDEX_FIELD_IMAGE_ID).asText();
						url = imageJson.get(Constants.json_fields.INDEX_FIELD_IMAGE_URL).getTextValue();
					} catch(NullPointerException e) {
						return badRequest(Utils.returnError("Missing field '"+Constants.json_fields.INDEX_FIELD_IMAGE_ID+"' or '"+Constants.json_fields.INDEX_FIELD_IMAGE_URL+"'"));						
					}
					HashMap<String, String> indexedFields = new HashMap<String, String>();
					indexedFields.put(Analyser.constants.SEARCH_URL, url);
					indexedFields.put(DocumentBuilder.FIELD_NAME_IDENTIFIER, id);

					message += indexedFields.toString() + " ";
					Logger.info("indexing " + indexedFields.toString());
					try {
						Analyser.visual.updateIndexfromURL(indexPath, url, indexedFields);
					} catch (StamatException e) {
						Logger.error("error while indexing {id="+id+", url="+url+"} message: " + e.getMessage());
					}
				}
				return ok(Utils.returnSuccess(message));				
			} else {
				return badRequest(Utils.returnError("expecting json format: {index: bla, images: [{}, {}, ...]}"));
			}
		}
	}

	public static Result visualSimilarity()
	{
		JsonNode json = request().body().asJson();
		if(json == null) {
			return badRequest(Utils.returnError("expecting JSON request. please check that content-type is set to 'application/json' and request body is properly encoded (e.g. JSON.stringify(data))"));
		}

		// request sanity checks first
		final String index, source, fileIdentifier, feature;
		final int numberOfResults;
		try {
			index = Constants.FOLDER_INDICES + "/" + json.get(Constants.json_fields.INDEX_FIELD_INDEX).getTextValue();
			source = json.get(Constants.json_fields.QUERY_FIELD_SOURCE).getTextValue();
			fileIdentifier = json.get(Constants.json_fields.QUERY_FIELD_FILEID).getTextValue();
			feature = json.get(Constants.json_fields.QUERY_FIELD_FEATURE).getTextValue();
			numberOfResults = json.get(Constants.json_fields.QUERY_FIELD_NUMOFRESULT).getIntValue();
		} catch( NullPointerException e) {
			String message = "Request, missing one of the fields: " +
				Constants.json_fields.INDEX_FIELD_INDEX + " " +
				Constants.json_fields.QUERY_FIELD_SOURCE + " " +
				Constants.json_fields.QUERY_FIELD_FILEID + " " +
				Constants.json_fields.QUERY_FIELD_FEATURE + " " +
				Constants.json_fields.QUERY_FIELD_NUMOFRESULT	+ " ";
			return badRequest(Utils.returnError(message));
		}

		if( !(source.equals(Analyser.constants.SEARCH_URL) || source.equals(Analyser.constants.SEARCH_INDEX)) ) {
			return badRequest("source must be either '"+Analyser.constants.SEARCH_URL+"' or '"+Analyser.constants.SEARCH_INDEX+"'");
		}

		if( !Analyser.constants.checkAllowedIndexFeature(feature) ) {
			Logger.info("visualSimilarity - "+ feature +" wrong feature descriptor");
			return badRequest(Utils.returnError("feature must be one of: " + Arrays.asList(Analyser.constants.featureArray).toString()));
		}

		// now run the search algorithm asynchronously
		Promise<List<SearchResult>> promiseOfSearchresultlist = Akka.future(
			new Callable<List<SearchResult>>() {
				public List<SearchResult> call() {
					List<SearchResult> result = null;
					if( source.equals(Analyser.constants.SEARCH_URL) ) {
						result = Analyser.visual.searchFromUrl(index, fileIdentifier, feature, numberOfResults);
					} else if( source.equals(Analyser.constants.SEARCH_INDEX) ) {
						result = Analyser.visual.searchFromIndex(index, fileIdentifier, feature, numberOfResults);
					}
					// tracing memory right before returning the result
					String runtime = Application.memInfo() + "visual.Sim " + fileIdentifier + " " + feature;
					Logger.info(runtime);
					return result;
				}
			}
		);
		return async(
			promiseOfSearchresultlist.map(
				new Function<List<SearchResult>, Result>() {
					public Result apply(List<SearchResult> result) {
						return ok(Utils.returnSuccess(Utils.searchResultList2JSON(result)));
					}
				}
			)
		);
	}

	/**
	 * useless function, used to copy and paste a template of async processing.
	 * presently used for entitiesExtractGate, visualIndexImagesFromDB, visualSimilary
	 * all other functions in this class are run synchronously
	 * 
	 * @return
	 */
	public static Result asyncCanvas()
	{
		Promise<String> promiseOfString = Akka.future(
			new Callable<String>() {
				public String call() {
					return "cheshire cat hides under deep folds of cumbersome code";
				}
			}
		);
		return async(
			promiseOfString.map(
				new Function<String, Result>() {
					public Result apply(String s) {
						return ok("all good, received: " + s);
					}
				}
			)
		);
	}
	
	private static String memInfo() {
		long tot = Runtime.getRuntime().totalMemory() / 1000000;
		long free = Runtime.getRuntime().freeMemory() / 1000000;
		long used = tot - free;

		long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
		int upSeconds = (int) (uptime / 1000) % 60 ;
		int upMinutes = (int) ((uptime / (1000*60)) % 60);
		int upHours   = (int) ((uptime / (1000*60*60)) % 24);
		
		return "[up "+upHours+"h:"+upMinutes+"m:"+upSeconds+"s - "+Runtime.getRuntime().availableProcessors() + "CPU - MEM Tot:"+tot+"M Used:"+used+"M Free:"+free+"M]\t";
	}
}