import twitter4j.Status;
import twitter4j.TwitterException;

/**
 * SuperTweet extension of Tweet class.  Added polarization and weight fields and extended all existing fields and methods.
 */
public class SuperStatus implements java.io.Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1476580968646201952L;

	/**
	 *The Tweet object that was extended to create the superTweet.
	 *
	 *@see Tweet
	 *@see superTweet.getTweet()
	 */
	private final Status status;
	
	/**
	 *The calculated polarization of the superTweet.  Runs from -5 to 5, the larger number representing a more positive outlook on the subject.
	 *
	 *@see TweetEvaluator.calculatePolarization(Tweet tweet)
	 *@see SuperTweet.getPolarization()
	 *@see SuperTweet.subject
	 */
	private double polarization;
	
	/**
	 *The calculated weighting of the superTweet.  Runs from 0 to infinity on a logarithmic scale, with larger numbers representing a more important tweet.
	 *
	 *@see TweetEvaluator.calculateWeight(Tweet tweet)
	 *@see SuperTweet.getPolarization()
	 *@see SuperTweet.subject
	 */
	private double weight = 0;
	
	private String[] tags;
	
	/**
	 * Constructor method.
	 * <p>
	 *
	 * @param  newTweet A Tweet object pulled from the twitter4j methods.
	 * @param  newSubject Whatever Brendan had to search to get this particular tweet.
	 * @param  eval An instance of the TweetEvaluator object, to determine sentiment.
	 * @see    Tweet
	 */
	
	public SuperStatus(Status status, String[] tags, TweetEvaluator eval) throws TwitterException
	{
		this.status = status;
        this.tags = tags;
		polarization = eval.calculatePolarization(this);
		weight = eval.calculateWeight(this);
	}

	/**
	 * Direct extension of tweet.compareTo() method.  Not fully developed.
	 * <p>
	 *
	 * @param  Tweet A Tweet object to compare to.
	 * @return int
	 * @see    Tweet.compareTo()
	 */
	
	public int compareTo(Status o) {
		return status.compareTo(o);
	}
	
	/**
	 * Method to get the Tweet that was extended to from the superTweet.
	 * <p>
	 *
	 * @return the Tweet object that was extended.
	 * @see    Tweet
	 */
	public Status getStatus()
	{
		return status;
	}
	
	/**
	 * Method to retrieve the weight of this superTweet.
	 * <p>
	 *
	 * @return double representing the weight from 0 to infinity.
	 */
	public double getWeight() {
		return weight;
	}
	
	public double getPolarization()
	{
		return polarization;
	}
	
	public void setPolariztion(double d)
	{
		polarization = d;
	}
	public String[] getTags()
	{
		return tags;
	}
	public void setTags(String[] tags)
	{
		this.tags = tags;
	}
	public boolean equals(Object y)
	{
		if (y == this) return true;
	    if (y == null) return false;
	    if (y.getClass() != this.getClass())
	       return false;
	    SuperStatus that = (SuperStatus) y;
	    if (this.polarization != that.polarization) return false;
	    if (this.weight != that.weight) return false;
	    if (!this.status.equals(that.status)) return false;
	    if (!this.tags.equals(that.tags)) return false;
	    return true;
	}
	public int hashCode()
	{
		int hash = 13;
		hash = 31 * hash + status.hashCode();
	    return hash;
	}
}
