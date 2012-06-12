/**
 * Program to pull tweets matching a single set of subjects from twitter.
 * Written by Ivo Crnkovic-Rubsamen with code blocks from Antonio Juliano and Brendan Chou
 */

import java.util.*;
import twitter4j.*;

public class SingleSubjectPuller {
	public static void singleSubjectPull(String subject, TweetTable tweetTable, TST<PolarityValue> wordPolarities, Twitter twitter) throws UnsuccessfulOperationException
	{System.out.println("2");
		// Number of tweets to pull
		final int tweetsToPull = 100;

		

		// Instance Variables
		Query query;
		List<Tweet> tweets = null;
		int resultsSize, numTweets, totalNumTweets;
		TweetEvaluator tweetEvaluator;
		Queue<CollectionMethods.TweetHolder> toBeAdded = new Queue<CollectionMethods.TweetHolder>();
        tweetEvaluator = new TweetEvaluator(wordPolarities);



		numTweets = 0;
		System.out.println(tweetTable.size() + " SuperTweets currently in TweetTable.");


		// Gather Tweets
		System.out.print("Gathering Tweets About " + subject + "... ");
		query = new Query(subject);
		query.setRpp(tweetsToPull);
		query.setLang("en");
		try
		{
			tweets = twitter.search(query).getTweets();
		}
		catch (TwitterException e)
		{
    		if (e.isErrorMessageAvailable())
    			System.err.println(e.getErrorMessage());
    		else
    			System.err.println("TwitterException: Unsuccessful");
			throw new UnsuccessfulOperationException();
		}
		resultsSize = tweets.size();
		for(int k = 0; k < resultsSize; k++)
		{
			numTweets++;
			toBeAdded.enqueue(new CollectionMethods.TweetHolder(tweets.get(k), subject));
			//tweetTable.add(new SuperTweet(tweets.get(k), trendName, tweetEvaluator, twitter));
		}
		System.out.println("Done.\n" + numTweets + " Tweets Collected.\nAdding Tweets to TweetTable... ");


		//Add Tweets to TweetTable Based Around Rate Limit
        totalNumTweets = CollectionMethods.addToTweetTable(toBeAdded, tweetTable, twitter,
        												   tweetEvaluator);
        if (totalNumTweets == 0)
        	throw new UnsuccessfulOperationException();
        
        System.out.print("Finished Adding Tweets to TweetTable.\n" + totalNumTweets + " Tweets Added.");
	}
}
