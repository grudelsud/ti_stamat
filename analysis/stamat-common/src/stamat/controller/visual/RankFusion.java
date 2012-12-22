package stamat.controller.visual;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class RankFusion {
	private List<SearchResult> resultsSCD;
	private List<SearchResult> resultsCLD;
	private List<SearchResult> resultsEHD;
	
	public RankFusion(List<SearchResult> resultsSCD,
			List<SearchResult> resultsCLD, List<SearchResult> resultsEHD) {
		super();
		this.resultsSCD = resultsSCD;
		this.resultsCLD = resultsCLD;
		this.resultsEHD = resultsEHD;
	}
	
	public List<SearchResult> mergeWithBORDACount(){
		Map<String, Integer> rankFusionMap = new HashMap<String, Integer>();
		for(SearchResult searchResultSCD : resultsSCD){
			rankFusionMap.put(searchResultSCD.getResult(), Integer.valueOf(resultsSCD.size() - searchResultSCD.getPosition()));
		}
		for(SearchResult searchResultCLD : resultsCLD){
			Integer oldEntry = rankFusionMap.get(searchResultCLD.getResult());
			if(oldEntry != null){
				rankFusionMap.put(searchResultCLD.getResult(), oldEntry + Integer.valueOf(resultsCLD.size() - searchResultCLD.getPosition()));
			}else{
				rankFusionMap.put(searchResultCLD.getResult(), Integer.valueOf(resultsCLD.size() - searchResultCLD.getPosition()));
			}
			
		}
		for(SearchResult searchResultEHD : resultsEHD){
			Integer oldEntry = rankFusionMap.get(searchResultEHD.getResult());
			if(oldEntry != null){
				rankFusionMap.put(searchResultEHD.getResult(), oldEntry + Integer.valueOf(resultsEHD.size() - searchResultEHD.getPosition()));
			}else{
				rankFusionMap.put(searchResultEHD.getResult(), Integer.valueOf(resultsEHD.size() - searchResultEHD.getPosition()));
			}
		}
		List<SearchResult> rankedResults = new ArrayList<SearchResult>();
		int i=0;
		for(Entry<String, Integer> entry : rankFusionMap.entrySet()){
			rankedResults.add(new SearchResult(entry.getKey(), i++, entry.getValue()));
		}
		Collections.sort(rankedResults);
		return rankedResults.subList(0, resultsSCD.size());
	}
	
	public List<SearchResult> mergeWithRankProduct(){
		Map<String, Integer> rankFusionMap = new HashMap<String, Integer>();
		Map<String, Integer> supportMap = new HashMap<String, Integer>();
		for(SearchResult searchResultSCD : resultsSCD){
			rankFusionMap.put(searchResultSCD.getResult(), Integer.valueOf(searchResultSCD.getPosition()+1));
			supportMap.put(searchResultSCD.getResult(), 1);
		}
		for(SearchResult searchResultCLD : resultsCLD){
			Integer oldEntry = rankFusionMap.get(searchResultCLD.getResult());
			if(oldEntry != null){
				rankFusionMap.put(searchResultCLD.getResult(), oldEntry * Integer.valueOf(searchResultCLD.getPosition()+1));
				supportMap.put(searchResultCLD.getResult(), supportMap.get(searchResultCLD.getResult())+1);
			}else{
				rankFusionMap.put(searchResultCLD.getResult(), Integer.valueOf(searchResultCLD.getPosition()+1));
				supportMap.put(searchResultCLD.getResult(), 1);
			}
			
		}
		for(SearchResult searchResultEHD : resultsEHD){
			Integer oldEntry = rankFusionMap.get(searchResultEHD.getResult());
			if(oldEntry != null){
				rankFusionMap.put(searchResultEHD.getResult(), oldEntry * Integer.valueOf(searchResultEHD.getPosition()+1));
				supportMap.put(searchResultEHD.getResult(), supportMap.get(searchResultEHD.getResult())+1);
			}else{
				rankFusionMap.put(searchResultEHD.getResult(), Integer.valueOf(searchResultEHD.getPosition()+1));
				supportMap.put(searchResultEHD.getResult(), 1);
			}
		}
		List<SearchResult> rankedResults = new ArrayList<SearchResult>();
		int i=0;
		for(Entry<String, Integer> entry : rankFusionMap.entrySet()){
			rankedResults.add(new SearchResult(entry.getKey(), i++, Math.pow(entry.getValue(), Math.pow(supportMap.get(entry.getKey()),-1))));
		}
		Collections.sort(rankedResults, new Comparator<SearchResult>() {
			@Override
			public int compare(SearchResult sr1, SearchResult sr2) {
				if(sr1.getSimilarity() < sr2.getSimilarity()){
					return -1;
				}else if(sr1.getSimilarity() > sr2.getSimilarity()){
					return +1;
				}else{
					return 0;
				}
			}
		});
		return rankedResults.subList(0, resultsSCD.size());
	}
	
	public List<SearchResult> mergeWithInvertedRankPosition(){
		Map<String, Double> rankFusionMap = new HashMap<String, Double>();
		for(SearchResult searchResultSCD : resultsSCD){
			rankFusionMap.put(searchResultSCD.getResult(), Double.valueOf(Math.pow(searchResultSCD.getPosition()+1,-1)));
		}
		for(SearchResult searchResultCLD : resultsCLD){
			Double oldEntry = rankFusionMap.get(searchResultCLD.getResult());
			if(oldEntry != null){
				rankFusionMap.put(searchResultCLD.getResult(), oldEntry + Double.valueOf(Math.pow(searchResultCLD.getPosition()+1,-1)));
			}else{
				rankFusionMap.put(searchResultCLD.getResult(), Double.valueOf(Math.pow(searchResultCLD.getPosition()+1,-1)));
			}
			
		}
		for(SearchResult searchResultEHD : resultsEHD){
			Double oldEntry = rankFusionMap.get(searchResultEHD.getResult());
			if(oldEntry != null){
				rankFusionMap.put(searchResultEHD.getResult(), oldEntry + Double.valueOf(Math.pow(searchResultEHD.getPosition()+1,-1)));
			}else{
				rankFusionMap.put(searchResultEHD.getResult(), Double.valueOf(Math.pow(searchResultEHD.getPosition()+1,-1)));
			}
		}
		List<SearchResult> rankedResults = new ArrayList<SearchResult>();
		int i=0;
		for(Entry<String, Double> entry : rankFusionMap.entrySet()){
			rankedResults.add(new SearchResult(entry.getKey(), i++, Math.pow(entry.getValue(),-1)));
		}
		Collections.sort(rankedResults, new Comparator<SearchResult>() {
			@Override
			public int compare(SearchResult sr1, SearchResult sr2) {
				if(sr1.getSimilarity() < sr2.getSimilarity()){
					return -1;
				}else if(sr1.getSimilarity() > sr2.getSimilarity()){
					return +1;
				}else{
					return 0;
				}
			}
		});
		return rankedResults.subList(0, resultsSCD.size());
	}
	
}