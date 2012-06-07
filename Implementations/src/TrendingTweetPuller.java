import java.util.*;
import java.io.*;

import twitter4j.*;
public class TrendingTweetPuller {
	public static void main(String[] args) throws TwitterException {
		String tweetFile = "superTweets.data";
		TweetHashTable tweetTable = null;
		TwitterFactory twitterFactory = new TwitterFactory();
        Twitter twitter = twitterFactory.getInstance();
        ResponseList<Trends> trendsList;
        Query query;
        File superTweetsBackup = new File("superTweetsBackup.data");
        List<Tweet> tweets = null;
    	int trendsListSize, hourlyTrendArraySize, resultsSize;
    	GregorianCalendar origin = new GregorianCalendar();
    	System.out.println("Origin: " + origin.getTime());
        tweetTable = TweetHashTable.load(tweetFile);
    	Date nextUpdate;
    	for(;;)
    	{
    		System.out.println("Origin First: " + origin.getTime());
    		origin.roll(Calendar.HOUR, 1);
    		System.out.println("Origin Second: " + origin.getTime());
    		nextUpdate = origin.getTime();
    		trendsList = twitter.getDailyTrends();
	        trendsListSize = trendsList.size();
	        System.out.println("Assimilating Tweets... ");
	        for(int i = 0; i < trendsListSize; i++)
	        {
	        	System.out.println("" + (int)((double)i / (double)trendsListSize * 100) + "%");
	        	Trends hourlyTrends = trendsList.get(i);
				Trend[] hourlyTrendArray = hourlyTrends.getTrends();
				hourlyTrendArraySize = hourlyTrendArray.length;
				for(int j = 0; j < hourlyTrendArraySize; j++)
				{
					String trendName = hourlyTrendArray[j].getName();
					query = new Query(trendName);
			        query.setRpp(2);
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
						tweetTable.add(new SuperTweet(tweets.get(k), trendName));
				}
	        }
	        TweetHashTable.save(tweetFile, tweetTable);
	        System.out.println("Done.");
	        System.out.println("Now: " + new Date() + "\nThen: " + nextUpdate);
	        while(new Date().before(nextUpdate))
	        {
	        	System.out.println("Waiting");
	        	try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					System.err.println("Interrupted");
				}
	        }
    	}
	}
}
