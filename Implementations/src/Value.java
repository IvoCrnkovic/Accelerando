/**
 * Implements an object for storing the value to be associated with the words TST
 * 
 * <p>
 * Stores the score (polarity) of a word and its number of occurrences
 */
public  class Value implements java.io.Serializable
{
	/**
	 * Int storing the number of times the word has appeared in our voting system.
	 */
	private int occurrences;
	
	/**
	 * double storing the current polarization voting record of the word.
	 */
	private double score;
	
	/**
	 * Constructor method.
	 * 
	 * 
	 * @param newScore The score field of the new Value.
	 * @param newOccurences The occurences field of the new Value.
	 */
	public Value(double newScore, int newOccurences) {
		score = newScore;
		occurrences = newOccurences;	
	}
	
	/**
	 * Method for interpreting the Value as a string.
	 * 
	 * @return score + "\n" + occurrences
	 */
	public String toString()
	{
		return "" + score + "\n" + occurrences; 
	}
	
	/**
	 * Method for getting the score field of a given value.
	 * 
	 * @return The score field of the value the method is called on.
	 */
	public double getScore()
	{
		return this.score;
	}
	
	/**
	 * Method for getting the occurences field of a given value.
	 * 
	 * @return The occurences field of the value the method is called on.
	 */
	public int getOccurrences()
	{
		return occurrences;
	}
	
	/**
	 * Method for setting the score field of a given value.
	 * 
	 * @param score The value to set the score to.
	 */
	public void setScore(double score)
	{
		this.score = score;
	}
	
	/**
	 * Method for incrementings the occurences field of a given value.
	 */
	public void incrementOccurrences()
	{
		occurrences++;
	}
}