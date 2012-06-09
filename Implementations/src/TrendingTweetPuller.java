/************************************************************************
 * Program to add Trending Tweets to a SuperTweet Symbol Table every Hour
 * Written by Antonio Juliano and Brendan Chou
 ***********************************************************************
 */

import java.util.*;
import java.io.*;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

public class TrendingTweetPuller {
	public static void main(String[] args) throws TwitterException {
		// Files to Load From
		String tweetFile = "superTweets.data";
		String wordsFile = "words.tst";
		
		// Number of tweets from each subject to pull
		final int tweetsToPull = 10;
		
		
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
        Twitter twitter = tf.getInstance();
		
		
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
    	
    	
    	// Load From File
        try {
			tweetTable = (TweetTable)ObjectLoader.load(tweetFile);
		} catch (FileNotFoundException e3) {
			System.err.println("FileNotFoundException: Failed to load TweetTable from " + tweetFile);
		} catch (IOException e3) {
			System.err.println("IOException: Failed to load TweetTable from " + tweetFile);
		} catch (ClassNotFoundException e3) {
			System.err.println("ClassNotFoundException: Failed to load TweetTable from " + tweetFile);
		}
        wordPolarities = ()
    	
    	tweetEvaluator = new TweetEvaluator(wordPolarities, users);
    	
    	
    	
    	// Process Loop
    	for(;;)
    	{
    		// Create Backup
    		System.out.print("Backing Up... ");
    		backupTweets(superTweetsFile, superTweetsBackup);
    		System.out.println("Done.");
    		
    		
    		// Calculate Next Update Time
    		origin.add(Calendar.HOUR_OF_DAY, 1);
    		nextUpdate = origin.getTime();
    		
    		
    		// Gather Tweets
    		trendsList = twitter.getDailyTrends();
	        trendsListSize = trendsList.size();
	        System.out.print("Assimilating Tweets... ");
	        for(int i = 0; i < trendsListSize; i++)
	        {
	        	Trends hourlyTrends = trendsList.get(i);
				Trend[] hourlyTrendArray = hourlyTrends.getTrends();
				hourlyTrendArraySize = hourlyTrendArray.length;
				for(int j = 0; j < hourlyTrendArraySize; j++)
				{
					String trendName = hourlyTrendArray[j].getName();
					query = new Query(trendName);
			        query.setRpp(tweetsToPull);
			        query.setLang("en");
			        try
			        {
			        	tweets = twitter.search(query).getTweets();
			        }
			        catch (TwitterException e)
			        {
			        	continue;
			        }
					resultsSize = tweets.size();
					for(int k = 0; k < resultsSize; k++)
					{
						numTweets++;
						tweetTable.add(new SuperTweet(tweets.get(k), trendName, tweetEvaluator));
					}
				}
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
	        
	        
	        // Wait Until Next Update Time
	        while(new Date().before(nextUpdate))
	        {
	        	try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					System.err.println("Interrupted");
				}
	        }
    	}
	}
	
	
	// Backup the TweetHashTable data structure from superTweetsFile to superTweetsBackup
	private static void backupTweets(File superTweetsFile, File superTweetsBackup)
	{
        FileInputStream tweetIn = null;
        FileOutputStream backupOut = null;
        byte[] buf = new byte[1024];
		try
		{
    		superTweetsBackup.delete();
    		try {
				superTweetsBackup.createNewFile();
			} catch (IOException e1) {
				System.err.println("Unable to Create Backup");
			}
    		try {
				backupOut = new FileOutputStream(superTweetsBackup);
			} catch (FileNotFoundException e2) {
				System.err.println("Unable to Initialize Backup Output Stream");
			}
    		try {
				tweetIn = new FileInputStream(superTweetsFile);
			} catch (FileNotFoundException e1) {
				System.err.println("Unable to Initialize Backup Input Stream from " + superTweetsFile);
			}
    		int len;
    		while ((len = tweetIn.read(buf)) > 0){
    			backupOut.write(buf, 0, len);
    		}
			backupOut.flush();
			backupOut.close();
		}
		catch (NullPointerException e1)
		{
			System.err.println("Backup Failed");
		} catch (IOException e) {
			System.err.println("Backup Failed");
		}
	}
}
