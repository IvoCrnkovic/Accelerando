import twitter4j.Tweet;
import twitter4j.TwitterException;

/**
 * SuperTweet extension of Tweet class.  Added polarization and weight fields and extended all existing fields and methods.
 */
public class SuperTweet implements java.io.Serializable{
	/**
	 *Serial ID number that eclipse told us to use for serialization.  Don't really know what it does.
	 *@see Java.io.Serializable
	 */
	private static final long serialVersionUID = -7918283392747206072L;
	
	/**
	 *The Tweet object that was extended to create the superTweet.
	 *
	 *@see Tweet
	 *@see superTweet.getTweet()
	 */
	private final Tweet tweet;
	
	/**
	 *The search term that was used to get this particular tweet.
	 *
	 *@see SuperTweet.getSubject()
	 */
	private final String subject;
	
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
	
	/**
	 *The polarization score given to the tweet in the initial voting process.
	 *
	 *@see PolarityGenerator
	 */
	private int vote;
	
	
	/**
	 * Constructor method.
	 * <p>
	 *
	 * @param  newTweet A Tweet object pulled from the twitter4j methods.
	 * @param  newSubject Whatever Brendan had to search to get this particular tweet.
	 * @param  eval An instance of the TweetEvaluator object, to determine sentiment.
	 * @see    Tweet
	 */
	
	public SuperTweet(Tweet newTweet, String newSubject, TweetEvaluator eval)
	{
		tweet = newTweet;
		subject = newSubject;
		polarization = eval.calculatePolarization(tweet);
		try {
			weight = eval.calculateWeight(tweet);
		} catch (TwitterException e) {
			
			System.err.println("TwitterException: Unable to Calculate Tweet Weight. Initializing to default value (0).");
			if(e.isErrorMessageAvailable())
				System.err.println(e.getErrorMessage());
			if (e.exceededRateLimitation())
			{
				System.err.println("Current Hourly Limit: " + e.getRateLimitStatus().getHourlyLimit());
				System.err.println("Reset Time: " + e.getRateLimitStatus().getResetTime());
			}
		}
		vote = 0;
	}

	/**
	 * Direct extension of tweet.compareTo() method.  Not fully developed.
	 * <p>
	 *
	 * @param  Tweet A Tweet object to compare to.
	 * @return int
	 * @see    Tweet.compareTo()
	 */
	
	public int compareTo(Tweet o) {
		return tweet.compareTo(o);
	}
	
	/**
	 * Method to get the Tweet that was extended to from the superTweet.
	 * <p>
	 *
	 * @return the Tweet object that was extended.
	 * @see    Tweet
	 */
	public Tweet getTweet()
	{
		return tweet;
	}
	
	public String getSubject()
	{
		return subject;
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
	
	public int getVote()
	{
		return vote;
	}
	
	public void setVote(int v)
	{
		vote = v;
	}
}
