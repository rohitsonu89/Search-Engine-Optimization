abstract public class Word implements Comparable<Word>
{
	public String word;
	// num times word appears
	public int frequency = 0;
	// frequency / total # of words for frequency word
	// frequency / total # of time query word appears
	public float weight = 0;	
	
	Word(String word)
	{
		this.word = word;
	}
	
	abstract public float score();
	
	public String toString()
	{
		return word;
	}
}
