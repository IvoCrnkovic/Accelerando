import java.util.*;
import java.io.*;
import twitter4j.*;

public class TrendingTweetPuller {
	
	public static void main(String[] args) throws TwitterException {
		TweetEvaluator.calculateWeight(null);
		/*System.out.println("-BEGIN-\n");
		
		TwitterFactory twitterFactory = new TwitterFactory();
        Twitter twitter = twitterFactory.getInstance();
        ResponseList<Trends> trendsList = twitter.getDailyTrends();
        Query query;
        List<Tweet> tweets;
    	int trendsListSize, hourlyTrendArraySize, resultsSize;
    	
        trendsListSize = trendsList.size();
        for(int i = 0; i < trendsListSize; i++)
        {
        	System.out.print("\nHour " + i + ": ");
        	Trends hourlyTrends = trendsList.get(i);
			Trend[] hourlyTrendArray = hourlyTrends.getTrends();
			hourlyTrendArraySize = hourlyTrendArray.length;
			for(int j = 0; j < hourlyTrendArraySize; j++)
			{
				String trendName = hourlyTrendArray[j].getName();
				query = new Query(trendName);
		        query.setRpp(2);
		        query.setLang("en");
				tweets = twitter.search(query).getTweets();
				resultsSize = tweets.size();
				for(int k = 0; k < resultsSize; k++)
					addTweetToTree(new SuperTweet(tweets.get(k), trendName));
				System.out.print(trendName + ", ");
			}
        }
        
        System.out.println("\n\n\n-COMPLETE-");/**/
        
	}
	
	private static void addTweetToTree(SuperTweet superTweet)
	{
		
	}

}
