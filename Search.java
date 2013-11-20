import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;

public class Search extends Thread
{
	private static final String BASE_URL = "http://boss.yahooapis.com/ysearch/web/v1/";
	public Query query;
	private SearchResult[] searchResults;
	private float targetPrecision;
	private String appID;
	private float currentPrecision = 0;
	
	Search(Query startQuery, float targetPrecision, String appID)
	{
		this.query = startQuery;
		this.targetPrecision = targetPrecision;
		this.appID = appID;
	}
	
	public float initialSearch()
	{
		System.out.println("Parameters:");
		System.out.println("Client key  = " + appID);
		System.out.println("Query       = " + query);
		System.out.println("Precision   = " + targetPrecision);
		return search();
	}
	
	public float feedbackSearch()
	{
		System.out.println("Feed Back search");
		//have to accept the arguments of modified query.
		System.out.println("Parameters:");
		System.out.println("Client key: = " + appID);
		System.out.println("Query       = " + query);
		System.out.println("Precision   = " + currentPrecision);
		
		
	    
		return search();
	}
	
	private void addQueryWord(String newQueryWord, DocParser.ParsedDoc parsedDoc)
	{
		if (newQueryWord == null)
			return;
		DistanceWord minDistQueryWord = null;
		String oldQueryWord = null;

		int side = 2;
		
	    for (DocParser.DistanceVector distanceVector : parsedDoc.distanceVectors)
	    {
	    	DistanceWord distanceWord = distanceVector.distanceVector.get(newQueryWord);
	    	if (distanceWord == null)
	    		continue;
	
	    	side = (distanceWord.signedDist < 0 && (side == -1 || side == 2)) ? -1 :
	    		   (distanceWord.signedDist > 0 && (side == 1 || side == 2)) ? 1 : 0;
	    	
	    	if (minDistQueryWord == null || (minDistQueryWord != null && distanceWord.score() > minDistQueryWord.score()))
	    	{
	    		minDistQueryWord = distanceWord;
	    		oldQueryWord = distanceVector.queryWord;
	    	}
	    }
	    
	    if (!DocParser.isStopWord(newQueryWord) && query.queryWords.size() > 2)
		    if (side == -1)
		    	oldQueryWord = query.queryWords.get(0);
		    else if (side == 1)
		    	oldQueryWord = query.queryWords.get(query.queryWords.size()-1);
		    	

	    query.addQueryWord(newQueryWord, oldQueryWord, minDistQueryWord);
	}
	// search iteration
	public float search()
	{
		URLConnection connection;
		String urlStr;
		try
		{
			URL url;
			String encodedQuery = URLEncoder.encode(query.toString(), "UTF-8");
			
			urlStr = BASE_URL + encodedQuery + "?appid=" + appID + "&count=10&format=xml";
			System.out.println(urlStr);
			url = new URL(urlStr);
			connection = url.openConnection();
		} catch (Exception e)
		{
			return 1;
		}
		
		searchResults = XMLParser.getResults(DocParser.getDocString(connection));
		
		System.out.println("URL: " + urlStr);
		System.out.println("Total no of results : " + searchResults.length);
		
		if (searchResults.length == 0)
			System.exit(0);
		
		System.out.println("Yahoo! Search Results:");
		System.out.println("======================");
		
		int numRelevant = 0;
		
		for (int i = 0 ; i < searchResults.length; i++)
		{
			System.out.println("\nResult " +  (i + 1) + "\n[\n" + searchResults[i] + "]\n");
			Scanner in = new Scanner(System.in);
			String isRelevant;
			do{
				System.out.print("Relevant (Y/N)? ");
				isRelevant = in.next();
			} while(!(isRelevant.equalsIgnoreCase("Y")|| isRelevant.equalsIgnoreCase("N" )));
			if (isRelevant.equalsIgnoreCase("Y"))
			{
				searchResults[i].isRelevant = true;
				numRelevant++;
			}
		}
		
		currentPrecision = (float) numRelevant/10;

		if (currentPrecision < targetPrecision)
		{
			System.out.print("\nLoading pages");
			
			// get all relevant/irrelevant doc text
			String relevantText = "";
			String irrelevantText = "";
			for (SearchResult result : searchResults)
			{
				while (result.page == null)
					yield();
				System.out.print(".");
				if (result.isRelevant)
					relevantText += " " + result.page;
				else
					irrelevantText += " " + result.page;
			}
			System.out.println("\n");
		 
			DocParser.ParsedDoc parsedDoc = DocParser.getParsedDoc(relevantText, irrelevantText, query);
			
			HashMap<String, ScoreWord> wordScores = DocParser.wordScores(parsedDoc);
	
			ArrayList<ScoreWord> scoreWords = new ArrayList<ScoreWord>(wordScores.values());
		    Collections.sort(scoreWords, Collections.reverseOrder());
		    Iterator<ScoreWord> it2 = scoreWords.iterator();
	
		    String newWord1 = "";
		    String newWord2 = "";
		    int curNewWord = 0;
		    while (it2.hasNext() && curNewWord < 2)
		    {
		    	ScoreWord scoreWord = it2.next();
		    	if (curNewWord == 0 && !query.isInQuery(scoreWord.word))
		    	{
		    		curNewWord++;
		    		newWord1 = scoreWord.word;
		    	}
		    	else if (curNewWord == 1 && !query.isInQuery(scoreWord.word))
		    	{
		    		newWord2 = scoreWord.word;
		    		curNewWord++;
		    	}
		    }
			
		    // add new words
		    addQueryWord(newWord2, parsedDoc);
		    // add new distance vector for new word
		    parsedDoc.distanceVectors.add(DocParser.getDistanceVector(parsedDoc.wordList, newWord2));
		    addQueryWord(newWord1, parsedDoc);
		}
		
		return currentPrecision;
	}
}
