import java.util.ArrayList;
import java.util.Iterator;

 
public class Query
{	
	public ArrayList<String> queryWords;
	
	Query(String query)
	{
		this.queryWords = new ArrayList<String>();
		for (String queryWord : query.split(" "))
			this.queryWords.add(queryWord.toLowerCase());
	}
	
	
	public void addQueryWord(String newQueryWord, String oldQueryWord, DistanceWord distWord)
	{
		if (distWord == null)
		{
			queryWords.add(newQueryWord);
			return;
		}
		int index = queryWords.indexOf(oldQueryWord);
		if (distWord.signedDist > 0)
			index++;
		queryWords.add(index, newQueryWord);
	}
	
	public String toString()
	{
		String query = "";
		Iterator<String> iter = queryWords.iterator();
		while (iter.hasNext())
			query += iter.next() + " ";
		return query;
	}
	
	public boolean isInQuery(String word)
	{
		return (queryWords.indexOf(word) != -1);
	}

}
