import java.util.*;
import twitter4j.*;

/**
 * SuperTweet extension of Tweet class.  Added polarization and weight fields and extended all existing fields and methods.
 */
public class SuperTweet implements Tweet, java.io.Serializable{
	
	/**
	 *Serial ID number that eclipse told us to use for serialization.  Don't really know what it does.
	 *@see Java.io.Serializable
	 */
	private static final long serialVersionUID = 1L;
	
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
	 * Direct extension of tweet.getHashtagEntities() method.  Not fully developed.
	 * <p>
	 *
	 * @return HashtagEntity[]
	 * @see    Tweet.getHashtagEntities()
	 */
	
	public HashtagEntity[] getHashtagEntities() {
		return tweet.getHashtagEntities();
	}

	/**
	 * Direct extension of tweet.getMediaEntities() method.  Not fully developed.
	 * <p>
	 *
	 * @return MediaEntity[]
	 * @see    Tweet.getMediaEntities()
	 */
	public MediaEntity[] getMediaEntities() {
		return tweet.getMediaEntities();
	}

	/**
	 * Direct extension of tweet.getURLEntities() method.  Not fully developed.
	 * <p>
	 *
	 * @return URLEntity[]
	 * @see    Tweet.getURLEntities()
	 */
	public URLEntity[] getURLEntities() {
		return tweet.getURLEntities();
	}

	/**
	 * Direct extension of tweet.getUserMentionEntities() method.  Not fully developed.
	 * <p>
	 *
	 * @return UserMentionEntity[]
	 * @see    Tweet.getUserMentionEntities()
	 */
	public UserMentionEntity[] getUserMentionEntities() {
		return tweet.getUserMentionEntities();
	}

	/**
	 * Direct extension of tweet.getAnnotations() method.  Not fully developed.
	 * <p>
	 *
	 * @return Annotations
	 * @see    Tweet.getAnnotations()
	 */
	public Annotations getAnnotations() {
		return tweet.getAnnotations();
	}

	/**
	 * Direct extension of tweet.getCreatedAt() method.  Not fully developed.
	 * <p>
	 *
	 * @return Date
	 * @see    Tweet.getCreatedAt()
	 */
	public Date getCreatedAt() {
		return tweet.getCreatedAt();
	}

	/**
	 * Direct extension of tweet.getFromUser() method.  Not fully developed.
	 * <p>
	 *
	 * @return String
	 * @see    Tweet.getFromUser()
	 */
	
	public String getFromUser() {
		return tweet.getFromUser();
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

	/**
	 * Direct extension of tweet.getFromUserId() method.  Not fully developed.
	 * <p>
	 *
	 * @return long
	 * @see    Tweet.getFromUserId()
	 */
	
	public long getFromUserId() {
		return tweet.getFromUserId();
	}

	/**
	 * Direct extension of tweet.getGeoLocation() method.  Not fully developed.
	 * <p>
	 *
	 * @return GeoLocation
	 * @see    Tweet.getGeoLocation()
	 */
	
	public GeoLocation getGeoLocation() {
		return tweet.getGeoLocation();
	}

	/**
	 * Direct extension of tweet.getId() method.  Not fully developed.
	 * <p>
	 *
	 * @return long
	 * @see    Tweet.getId()
	 */
	
	public long getId() {
		return tweet.getId();
	}

	/**
	 * Direct extension of tweet.getIsoLanguageCode() method.  Not fully developed.
	 * <p>
	 *
	 * @return String
	 * @see    Tweet.getIsoLanguageCode()
	 */
	public String getIsoLanguageCode() {
		return tweet.getIsoLanguageCode();
	}

	/**
	 * Direct extension of tweet.getLocation() method.  Not fully developed.
	 * <p>
	 *
	 * @return String
	 * @see    Tweet.getLocation()
	 */
	public String getLocation() {
		return tweet.getLocation();
	}

	/**
	 * Direct extension of tweet.getPlace() method.  Not fully developed.
	 * <p>
	 *
	 * @return Place
	 * @see    Tweet.getPlace()
	 */
	public Place getPlace() {
		return tweet.getPlace();
	}
	
	/**
	 * Method to get the +/- polarization of the superTweet.
	 * <p>
	 *
	 * @return double polarization between -5 and 5
	 */
	public double getPolarization() {
		return polarization;
	}

	/**
	 * Direct extension of tweet.getProfileImageUrl() method.  Not fully developed.
	 * <p>
	 *
	 * @return String
	 * @see    Tweet.getProfileImageUrl()
	 */
	public String getProfileImageUrl() {
		return tweet.getProfileImageUrl();
	}

	/**
	 * Direct extension of tweet.getSource() method.  Not fully developed.
	 * <p>
	 *
	 * @return String
	 * @see    Tweet.getSource()
	 */
	public String getSource() {
		return tweet.getSource();
	}
	
	
	/**
	 * Method to get the subject of the superTweet.
	 * <p>
	 *
	 * @return the subject of the superTweet.
	 */
	public String getSubject()
	{
		return subject;
	}

	/**
	 * Method to get the unmodified text of the tweet that this superTweet was extended form.
	 * <p>
	 *
	 * @return the text of the superTweet.
	 */
	public String getText() {
		return tweet.getText();
	}

	/**
	 * Direct extension of tweet.getToUser() method.  Not fully developed.
	 * <p>
	 *
	 * @return String
	 * @see    Tweet.getToUser()
	 */
	
	public String getToUser() {
		return tweet.getToUser();
	}

	/**
	 * Direct extension of tweet.getToUserId() method.  Not fully developed.
	 * <p>
	 *
	 * @return long
	 * @see    Tweet.getToUserId()
	 */
	public long getToUserId() {
		return tweet.getToUserId();
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

}
