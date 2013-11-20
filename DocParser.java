import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

public class DocParser
{
	public static HashMap<String, String> stopwords = fileToStringArray("src/stopword_list.txt");
	
	public static ParsedDoc getParsedDoc(String relevantText, String irrelevantText, Query query)
	{
		StringTokenizer st = new StringTokenizer(relevantText);
		
		// get relevant word vector and relevant word list
		HashMap<String, FrequencyWord> relevantWordVector = new HashMap<String, FrequencyWord>();
		ArrayList<String> relevantWordList = new ArrayList<String>();
		while (st.hasMoreTokens())
		{
			String wordStr = st.nextToken();
			// add word to wordList
			relevantWordList.add(wordStr);
			
			// skip stop words
			if (isStopWord(wordStr))
				continue;
			
			// add word to word vector
			FrequencyWord wordObj = relevantWordVector.get(wordStr);
			if (wordObj == null)
			{
				wordObj = new FrequencyWord(wordStr);
				relevantWordVector.put(wordStr, wordObj);
			}
			wordObj.frequency++;
		}
		
		// get irrelevant word vector
		st = new StringTokenizer(irrelevantText);
		HashMap<String, FrequencyWord> irrelevantWordVector = new HashMap<String, FrequencyWord>();
		while (st.hasMoreTokens())
		{
			String wordStr = st.nextToken();
			
			// skip stop words
			if (isStopWord(wordStr))
				continue;
			
			// add word to word vector
			FrequencyWord wordObj = irrelevantWordVector.get(wordStr);
			if (wordObj == null)
			{
				wordObj = new FrequencyWord(wordStr);
				irrelevantWordVector.put(wordStr, wordObj);
			}
			wordObj.frequency++;
		}
		
		// subtract irrelevant words from relevant
		FrequencyWord.getVectorDifference(relevantWordVector, irrelevantWordVector);
		
		// get relevant word array
		String[] relevantWordArray = new String[relevantWordList.size()];
		relevantWordList.toArray(relevantWordArray);
		
		// compute distance vectors
		ArrayList<DistanceVector> distanceVectors = new ArrayList<DistanceVector>();
		Iterator<String> iter = query.queryWords.iterator();
		while (iter.hasNext())
		{
			String queryWord = iter.next();
			if (!isStopWord(queryWord))
				distanceVectors.add(getDistanceVector(relevantWordArray, queryWord));
		}
		
		ParsedDoc parsedDoc = new ParsedDoc(relevantWordVector, relevantWordArray, distanceVectors);
		return parsedDoc;
	}
	
	public static HashMap<String, ScoreWord> wordScores(ParsedDoc parsedDoc)
	{
		final float DISTANCE_WORD_SIGNIFICANCE = 1;
		
		HashMap<String, ScoreWord> wordScores = new HashMap<String, ScoreWord>();
		
		
		// add frequency words to list of score words
		Iterator<FrequencyWord> iter = parsedDoc.wordVector.values().iterator();
		while (iter.hasNext())
		{
			FrequencyWord frequencyWord = iter.next();
			ScoreWord scoreWord = new ScoreWord(frequencyWord.word);
			scoreWord.score = frequencyWord.score();
			wordScores.put(frequencyWord.word, scoreWord);
		}
		
		// add distance words to list of score words
		for (DistanceVector distanceVector : parsedDoc.distanceVectors)
		{
			Iterator<DistanceWord> iter2 = distanceVector.distanceVector.values().iterator();
			while (iter2.hasNext())
			{
				DistanceWord distanceWord = iter2.next();
				ScoreWord scoreWord = wordScores.get(distanceWord.word);
				if (scoreWord == null)
				{
					scoreWord = new ScoreWord(distanceWord.word);
					wordScores.put(distanceWord.word, scoreWord);
				}
				scoreWord.score += DISTANCE_WORD_SIGNIFICANCE * distanceWord.score();
			}
		}
		
		return wordScores;
	}
	
	public static DistanceVector getDistanceVector(String[] wordArray, String queryWord)
	{
		DistanceVector distanceVector = new DistanceVector(queryWord, new HashMap<String, DistanceWord>());
		int numQueryOccurance = 0;
		// find query word and add to distance vector
		for (int queryIndex = 0; queryIndex < wordArray.length; queryIndex++)
		{
			if (wordArray[queryIndex].equals(queryWord))
			{
				HashMap<String, String> wordsAppearing = new HashMap<String, String>();
				numQueryOccurance++;
				for (int i = 1; i <= DistanceWord.DISTANCE_RANGE; i++)
				{
					int[] directions = {-1, 1};
					for (int direction : directions)
					{
						int wordIndex = queryIndex + direction * i;
						if (wordIndex < 0 || wordIndex >= wordArray.length)
							continue;
						// if word already appeared
						if (wordsAppearing.get(wordArray[wordIndex]) != null)
							continue;
						else
							wordsAppearing.put(wordArray[wordIndex], wordArray[wordIndex]);
						int signedDist = wordIndex - queryIndex;
						int absDist = Math.abs(wordIndex - queryIndex);
						DistanceWord wordObj = distanceVector.distanceVector.get(wordArray[wordIndex]);
						if (wordObj == null)
						{
							wordObj = new DistanceWord(wordArray[wordIndex]);
							distanceVector.distanceVector.put(wordArray[wordIndex], wordObj);
						}
						wordObj.frequency++;
						wordObj.signedDist += signedDist;
						wordObj.absDist += absDist;  
					}
				}
			}
		}
		
		// compute weight normalize 
		if (numQueryOccurance > 0)
		{
			distanceVector.numQueryOccurance = numQueryOccurance;
			Iterator<DistanceWord> iter = distanceVector.distanceVector.values().iterator();
			while (iter.hasNext())
			{
				DistanceWord distanceWord = iter.next();
				distanceWord.weight = (float) distanceWord.frequency / numQueryOccurance;
				// remove word if stop word and low weight
				if (isStopWord(distanceWord.word) && distanceWord.weight < 0.5)
					distanceWord.weight = 0;
				
				//distanceWord.signedDist = distanceWord.weight * distanceWord.signedDist / numQueryOccurance;
				//distanceWord.absDist = distanceWord.weight * distanceWord.absDist / numQueryOccurance;
				distanceWord.numQueryOccurance = numQueryOccurance; 
			}	
		}
		
		return distanceVector;
	}

	public static String getDocString(URLConnection connection)
	{
		// set timeout
		connection.setConnectTimeout(10000);
		connection.setReadTimeout(10000);
		String string = "";
		String line;
		try
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			while ((line = reader.readLine()) != null)
				string += line + "\n";
		} catch (Exception e)
		{
			return "";
		}
		return string;
	}
	
	public static void removeTags(String text)
	{
		text.replaceAll("<.*>", "");
	}
	
	
	public static class DistanceVector
	{
		HashMap<String, DistanceWord> distanceVector;
		String queryWord;
		int numQueryOccurance = 0;
		
		DistanceVector(String queryWord, HashMap<String, DistanceWord> distanceVector)
		{
			this.distanceVector = distanceVector;
			this.queryWord = queryWord;
		}
	}
	
	public static class ParsedDoc
	{
		public HashMap<String, FrequencyWord> wordVector;
		public ArrayList<DistanceVector> distanceVectors;
		public String[] wordList;
		
		ParsedDoc(HashMap<String, FrequencyWord> wordVector, String[] wordList,
				  ArrayList<DistanceVector> distanceVectors)
		{
			this.wordVector = wordVector;
			this.wordList = wordList;
			this.distanceVectors = distanceVectors;
		}
		
		public String toString()
		{
			String str = "Word Vector:\n";

		    ArrayList<FrequencyWord> words = new ArrayList<FrequencyWord>(wordVector.values());
		    Collections.sort(words, Collections.reverseOrder());
		    Iterator<FrequencyWord> it = words.iterator();
		    
		    int i  = 0;
		    while (it.hasNext() && i < 15)
		    {
		    	FrequencyWord word = it.next();
	    		str += word + "\n";
	    		i++;
		    }

		    str += "\nDistance Vectors:\n";
			
			for (DistanceVector distanceVector : distanceVectors)
			{
				str += distanceVector.queryWord + " " + distanceVector.numQueryOccurance + "\n";
				
				ArrayList<DistanceWord> distanceWords = new ArrayList<DistanceWord>(distanceVector.distanceVector.values());
			    Collections.sort(distanceWords, Collections.reverseOrder());
			    Iterator<DistanceWord> it2 = distanceWords.iterator();
		
			    i = 0;
			    while (it2.hasNext() && i < 15)
			    {
			    	DistanceWord distanceWord = it2.next();
			    	str += distanceWord + "\n";
			    	i++;
			    }
			    str += "\n";
			}
			return str;
		}
	}
	
	public static boolean isStopWord(String word)
	{
		return (stopwords.get(word) != null);
	}
	
	public static HashMap<String, String> fileToStringArray(String path)
	{
		HashMap<String, String> words = new HashMap<String, String>();
		try
		{
			BufferedReader input =  new BufferedReader(new FileReader(new File(path)));
		  
			String line;
			while ((line = input.readLine()) != null)
				words.put(line, line);
		} catch (Exception e)
		{
			return words;
		}
 
		return words;
	}
}
