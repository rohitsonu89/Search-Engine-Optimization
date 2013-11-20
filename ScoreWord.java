public class ScoreWord extends Word
{
	public float score;
	
	ScoreWord(String word)
	{
		super(word);
	}
	
	public String toString()
	{
		return word + " " + score;
	}

	public int compareTo(Word other)
	{
		return (this.score() - other.score() == 0) ? 0 :
			(this.score() - other.score() > 0) ? 1 : -1;
	}

	public float score()
	{
		return score;
	}
}
