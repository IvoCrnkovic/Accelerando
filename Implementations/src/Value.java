/**
 * The value to be associated with the words TST
 * Stores the score (polarity) of a word and its number of occurrences
 * @author Antonio
 */
public  class Value implements java.io.Serializable
{
	private int occurrences;
	private double score;
	public Value(double d, int i) {
		occurrences = i;
		score = d;
	}
	public String toString()
	{
		return "" + score + "\n" + occurrences; 
	}
	
	public double getScore()
	{
		return this.score;
	}
	public int getOccurrences()
	{
		return occurrences;
	}
	public void setScore(double score)
	{
		this.score = score;
	}
	public void incrementOccurrences()
	{
		occurrences++;
	}
}