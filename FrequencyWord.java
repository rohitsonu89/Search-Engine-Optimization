import java.util.HashMap;
import java.util.Iterator;

public class FrequencyWord extends Word
{
	// num times words is emphasized (bold, in header tags)
	public int timesEmphasized = 0;
	public int timesTitle = 0;
	
	FrequencyWord(String word)
	{
		super(word);
	}
	
	public float score()
	{
		return (float) frequency;
	}
	
	public static void getVectorDifference(HashMap<String, FrequencyWord> wordVector1, HashMap<String, FrequencyWord> wordVector2)
	{
		Iterator<String> iter = wordVector1.keySet().iterator();
		String wordStr;
		FrequencyWord wordObj1, wordObj2;
		while (iter.hasNext())
		{
			wordStr = (String) iter.next();
			wordObj1 = wordVector1.get(wordStr);
			wordObj2 = wordVector2.get(wordStr);
			
			if (wordObj2 != null)
				wordObj1.frequency -= wordObj2.frequency;
		}
	}
	
	public int compareTo(Word other)
	{
		return this.frequency - other.frequency;
	}
	
	public String toString()
	{
		return word + " " + frequency + " " + score();
	}
}
