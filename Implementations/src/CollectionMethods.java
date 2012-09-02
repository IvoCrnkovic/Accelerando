import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import twitter4j.*;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;
public class CollectionMethods {
	final static String username = "turtleman755";
	final static String password = "accelerando";
	final static String consumerKey = "bceUVDFbpUh2pcu6gvpp9w";
	final static String consumerSecret = "pvei5cAMJgy5qXQPjuyyki508ZxHPM6ypRJt94OW9sY";
	final static String token = "17186983-6cc4E3GPsn1aFNrMsr5qJKpm8a0mxFl8ozsmUP43t";
	final static String tokenSecret = "p4Sc5WrSdSL2R4cUxqal86QRosbW5txbFQYF5ItOow";
	final static String username2 = "accelerando2015";
	final static String password2 = "princeton2015";
	final static String consumerKey2 = "jXpbKl4g9FTMuTLKp57zQ";
	final static String consumerSecret2 = "ySX84mrnKO2dySpeeVDjMReCXmilE8JKXEXXl7xSPAc";
	final static String token2 = "774334322-bzRyWU9I7eAHfHuRBNlExGBuA2vVzXeceOtpVD71";
	final static String tokenSecret2 = "5xzXW0K4eJixCOMWwikqGSEJri0arAsYdUOQus0";
	public static Twitter authenticate()
	{
		ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true);
        cb.setUser(username);
        cb.setPassword(password);
        cb.setOAuthConsumerKey(consumerKey);
        cb.setOAuthConsumerSecret(consumerSecret);
        cb.setOAuthAccessToken(token);
        cb.setOAuthAccessTokenSecret(tokenSecret);
        TwitterFactory tf = new TwitterFactory(cb.build());
        AccessToken accessToken = new AccessToken(token, tokenSecret);
        Twitter twitter = tf.getInstance(accessToken);
        return twitter;
	}
	public static TwitterStream authenticateStream()
	{
		ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true);
        cb.setUser(username);
        cb.setPassword(password);
        cb.setOAuthConsumerKey(consumerKey);
        cb.setOAuthConsumerSecret(consumerSecret);
        cb.setOAuthAccessToken(token);
        cb.setOAuthAccessTokenSecret(tokenSecret);
        TwitterStreamFactory tf = new TwitterStreamFactory(cb.build());
        AccessToken accessToken = new AccessToken(token, tokenSecret);
        TwitterStream twitterStream = tf.getInstance(accessToken);
        return twitterStream;
	}
	public static TwitterStream authenticateSecondTwitterStream()
	{
		ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true);
        cb.setUser(username2);
        cb.setPassword(password2);
        cb.setOAuthConsumerKey(consumerKey2);
        cb.setOAuthConsumerSecret(consumerSecret2);
        cb.setOAuthAccessToken(token2);
        cb.setOAuthAccessTokenSecret(tokenSecret2);
        TwitterStreamFactory tf = new TwitterStreamFactory(cb.build());
        AccessToken accessToken = new AccessToken(token2, tokenSecret2);
        TwitterStream twitterStream = tf.getInstance(accessToken);
        return twitterStream;
	}
	public synchronized static <objectType> objectType load(String filename) 
	{
		Object obj = null;
		objectType object;
		try {
			obj = ObjectLoader.load(filename);
		} catch (FileNotFoundException e3) {
			System.err.println("FileNotFoundException: Failed to load TweetTable from " + filename);
			System.exit(0);
		} catch (IOException e3) {
			System.err.println("IOException: Failed to load TweetTable from " + filename);
			System.exit(0);
		} catch (ClassNotFoundException e3) {
			System.err.println("ClassNotFoundException: Failed to load TweetTable from " + filename);
			System.exit(0);
		}
		
		// TODO May throw error
        object = (objectType) obj;
        return object;
	}		
	public synchronized static void backup(String objectFile, String backupFile)
	{
		try {
				ObjectLoader.backupFile(objectFile, backupFile);
			} catch (FileNotFoundException e3) {
				System.err.println("FileNotFoundException: Unable to Create Backup of TweetTable");
			} catch (NullPointerException e3) {
				System.err.println("NullPointerException: Unable to Create Backup of TweetTable");
			} catch (IOException e3) {
				System.err.println("IOException: Unable to Create Backup of TweetTable");
			}
	}
	public static void handleRateLimitException(TwitterException e, Object toBeSaved, String objectFile, String backupFile)
	{
		Date nextUpdate;
		System.out.println("Hourly Limit of " + e.getRateLimitStatus().getHourlyLimit() + " requests has been Reached.\n" +
        								"Process will Resume: " + e.getRateLimitStatus().getResetTime());
        			nextUpdate = e.getRateLimitStatus().getResetTime();
        			
        			// Create Backups
        			System.out.print("Backing Up... ");
            		backup(objectFile, backupFile);
            		System.out.println("Done.");
            		
            		// Save
        			System.out.print("Saving... ");
        			save(toBeSaved, objectFile);
        			System.out.println("Done.");
        			// Wait Until Rate Limit Resets
        			while(new Date().before(nextUpdate))
        	        {
        	        	try {
        					Thread.sleep(10000);
        				} catch (InterruptedException e2) {
        					System.err.println("Interrupted");
        				}
        	        }
	}
	public synchronized static void save(Object toBeSaved, String filename)
	{
		try {
        	ObjectLoader.save(toBeSaved, filename);
        } catch (FileNotFoundException e2) {
        	System.err.println("FileNotFoundException: Could not save TweetTable to " + filename);
        } catch (IOException e2) {
        	System.err.println("IOException: Could not save TweetTable to " + filename);
        }
	}
	
	public static class TweetHolder
	{
		private String subject;
		private Tweet tweet;
		public TweetHolder(Tweet tweet, String subject)
		{
			this.subject = subject;
			this.tweet = tweet;
		}
	}
}
