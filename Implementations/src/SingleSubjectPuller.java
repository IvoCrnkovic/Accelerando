import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import twitter4j.Query;
import twitter4j.ResponseList;
import twitter4j.Trends;
import twitter4j.Tweet;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

/**
 * @author c10ic1
 *
 */
public class SingleSubjectPuller {

	/**
	 * @param args
	 */
	public static void main (String [] args) {
		String subject = "Lady Gaga";
		
		// Files to Load From
		String tweetFile = "superTweets.data";
		String wordsFile = "words.tst";
		String userFile = "users.data";

		// Number of tweets from each subject to pull
		final int tweetsToPull = 100;

		// Authenticate
		final String user = "turtleman755";
		final String password = "accelerando";
		final String consumerKey = "bceUVDFbpUh2pcu6gvpp9w";
		final String consumerSecret = "pvei5cAMJgy5qXQPjuyyki508ZxHPM6ypRJt94OW9sY";
		final String token = "17186983-6cc4E3GPsn1aFNrMsr5qJKpm8a0mxFl8ozsmUP43t";
		final String tokenSecret = "p4Sc5WrSdSL2R4cUxqal86QRosbW5txbFQYF5ItOow";
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true);
		cb.setUser(user);
		cb.setPassword(password);
		cb.setOAuthConsumerKey(consumerKey);
		cb.setOAuthConsumerSecret(consumerSecret);
		cb.setOAuthAccessToken(token);
		cb.setOAuthAccessTokenSecret(tokenSecret);
		TwitterFactory tf = new TwitterFactory(cb.build());
		AccessToken accessToken = new AccessToken(token, tokenSecret);
		Twitter twitter = tf.getInstance(accessToken);

		// Instance Variables
		TweetTable tweetTable = null;
		Date nextUpdate;
		ResponseList<Trends> trendsList;
		Query query;
		File superTweetsBackup = new File("superTweetsBackup.data");
		File superTweetsFile = new File(tweetFile);
		List<Tweet> tweets = null;
		int trendsListSize, hourlyTrendArraySize, resultsSize, numTweets = 0;
		GregorianCalendar origin = new GregorianCalendar();
		TST<SuperUser> users = null;
		TST<PolarityValue> wordPolarities = null;
		TweetEvaluator tweetEvaluator;
		Object obj = null;

		// Load From File
		try {
			obj = ObjectLoader.load(tweetFile);
		} catch (FileNotFoundException e3) {
			System.err.println("FileNotFoundException: Failed to load TweetTable from " + tweetFile);
			System.exit(0);
		} catch (IOException e3) {
			System.err.println("IOException: Failed to load TweetTable from " + tweetFile);
			System.exit(0);
		} catch (ClassNotFoundException e3) {
			System.err.println("ClassNotFoundException: Failed to load TweetTable from " + tweetFile);
			System.exit(0);
		} 
		if (obj instanceof TweetTable)
			tweetTable = (TweetTable) obj;
		else
		{
			System.err.println("Class Mismatch: Failed to load TweetTable from " + tweetFile);
			System.exit(0);
		}

		try {
			obj = ObjectLoader.load(wordsFile);
		} catch (FileNotFoundException e3) {
			System.err.println("FileNotFoundException: Failed to load Word Polarizations from " + wordsFile);
			System.exit(0);
		} catch (IOException e3) {
			System.err.println("IOException: Failed to load Word Polarizations from " + wordsFile);
			System.exit(0);
		} catch (ClassNotFoundException e3) {
			System.err.println("ClassNotFoundException: Failed to load Word Polarizations from " + wordsFile);
			System.exit(0);
		} 
		if (obj instanceof TST<?>)
			wordPolarities = (TST<PolarityValue>) obj;
		else
		{
			System.err.println("Class Mismatch: Failed to load Word Polarizations from " + wordsFile);
			System.exit(0);
		}

		try {
			obj = ObjectLoader.load(userFile);
		} catch (FileNotFoundException e3) {
			System.err.println("FileNotFoundException: Failed to load User TST from " + userFile);
			System.exit(0);
		} catch (IOException e3) {
			System.err.println("IOException: Failed to load User TST from " + userFile);
			System.exit(0);
		} catch (ClassNotFoundException e3) {
			System.err.println("ClassNotFoundException: Failed to load User TST from " + userFile);
			System.exit(0);
		} 
		if (obj instanceof TST<?>)
			users = (TST<SuperUser>) obj;
		else
		{
			System.err.println("Class Mismatch: Failed to load User TST from " + userFile);
			System.exit(0);
		}

		tweetEvaluator = new TweetEvaluator(wordPolarities, users);

		//backup 
		System.out.print("Backing Up... ");
		ObjectLoader.backupTweets(superTweetsFile, superTweetsBackup);
		System.out.println("Done.");
		
		query = new Query(subject);
        query.setRpp(tweetsToPull);
        query.setLang("en");
        try
        {
        	tweets = twitter.search(query).getTweets();
        }
        catch (TwitterException e)
        {
        	
        }
		resultsSize = tweets.size();
		for(int k = 0; k < resultsSize; k++)
		{
			numTweets++;
			tweetTable.add(new SuperTweet(tweets.get(k), subject, tweetEvaluator, twitter));
		}
		
		System.out.print("Done.\n" + numTweets + " Tweets Collected.\nSaving Tweets... ");
        try {
			ObjectLoader.save(tweetTable, tweetFile);
		} catch (FileNotFoundException e1) {
			System.err.println("FileNotFoundException: Could not save TweetTable to " + tweetFile);
		} catch (IOException e1) {
			System.err.println("IOException: Could not save TweetTable to " + tweetFile);
		}
        System.out.println("Done.\nNext Update at " + origin.getTime().toString());
	}
}
