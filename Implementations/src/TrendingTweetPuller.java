/************************************************************************
 * Program to add Trending Tweets to a SuperTweet Symbol Table every Hour
 * Written by Antonio Juliano and Brendan Chou
 ***********************************************************************
 */

import java.util.*;
import twitter4j.*;

public class TrendingTweetPuller {
	public static TweetTable trendingTweetPull(TweetTable tweetTable, TST<PolarityValue> wordPolarities, String tweetTableFile, String tweetTableBackup) throws UnsuccessfulOperationException
	{
		// Number of tweets from each subject to pull
		final int tweetsToPull = 100;
		
		
		// Authenticate
		System.out.print("Authenticating... ");
		Twitter twitter = CollectionMethods.authenticate();
		System.out.println("Done");
        
		// Instance Variables
        ResponseList<Trends> trendsList;
        Query query;
        List<Tweet> tweets = null;
    	int trendsListSize, hourlyTrendArraySize, resultsSize, numTweets, totalNumTweets;
    	TweetEvaluator tweetEvaluator;
    	Queue<CollectionMethods.TweetHolder> toBeAdded = new Queue<CollectionMethods.TweetHolder>();
        tweetEvaluator = new TweetEvaluator(wordPolarities);
        
    	
    	
    	// Process Loop
    	for(;;)
    	{
    		numTweets = 0;
    		System.out.println(tweetTable.size() + " SuperTweets currently in TweetTable.");
    		
    		// Create Backup
    		System.out.print("Backing Up... ");
    		CollectionMethods.backup(tweetTableFile, tweetTableBackup);
    		System.out.println("Done.");
    		
    		
    		// Gather Tweets
    		try
    		{
	    		trendsList = twitter.getDailyTrends();
		        trendsListSize = trendsList.size();
		        System.out.print("Gathering Tweets... ");
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
							toBeAdded.enqueue(new CollectionMethods.TweetHolder(tweets.get(k), trendName));
						}
					}
		        }
    		}
    		// TODO check infinite errors
    		catch (TwitterException e)
    		{
    			if (e.exceededRateLimitation())
        		{
        			CollectionMethods.handleRateLimitException(e, tweetTable, tweetTableFile, tweetTableBackup);
        			numTweets = 0;
        			continue;
        		}
        		else
        		{
        			if (e.isErrorMessageAvailable())
        				System.err.println(e.getErrorMessage());
        			else
        				System.err.println("TwitterException: Continuing");
        			continue;
        		}
    		}
	        System.out.println("Done.\n" + numTweets + " Tweets Collected.\nAdding Tweets to TweetTable... ");
	        
	        
	        //Add Tweets to TweetTable Based Around Rate Limit
	        totalNumTweets = CollectionMethods.addToTweetTable(toBeAdded, tweetTable, twitter,
	        												   tweetEvaluator, tweetTableFile, tweetTableBackup);
	        
	        
	        System.out.print("Finished Adding Tweets to TweetTable.\n" + totalNumTweets + " Tweets Added.\nSaving... ");
	        CollectionMethods.save(tweetTable, tweetTableFile);
	        System.out.println("Done.");
    	}
	}
}
