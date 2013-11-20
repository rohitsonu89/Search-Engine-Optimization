public class DistanceWord extends Word
{
	public float absDist = 0;
	public float signedDist = 0;
	public int numQueryOccurance = 0;
	
	public static final int DISTANCE_RANGE = 5;
	private final float STOP_WORD_SIGNIFICANCE = 1f;
	
	DistanceWord(String word)
	{
		super(word);
	}
	
	public float score()
	{
		float distanceScore = (DISTANCE_RANGE - absDist / frequency) / (DISTANCE_RANGE - 1);
		float score = (weight * distanceScore + weight) * numQueryOccurance / 2f;
		//float score = ((1 - Math.abs(absDist - 1)) + (1 - Math.abs(weight - 1))) * numQueryOccurance / 2f;
		if (DocParser.isStopWord(word))
			score *= STOP_WORD_SIGNIFICANCE;
		return score;
	}
	
	public String toString()
	{
		return word + " " + frequency + " " + numQueryOccurance + " " + absDist + " " + signedDist + " " + weight;
	}
	
	public int compareTo(Word other)
	{
		return (this.score() - other.score() == 0) ? 0 :
			(this.score() - other.score() > 0) ? 1 : -1;
	}
}
