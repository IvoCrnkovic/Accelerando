import java.util.List;
import java.util.Scanner;
import java.io.*;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Tweet;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

public class SentDet {

	public static void main(String[] args)
	{
		// TODO Auto-generated method stub

	}
	
	private static double determineSentiment(String tweet)
	{
		//remove fluff (@ and # and http)
		int io = tweet.indexOf("http://t.co/");
		while(io > -1)
		{
			if(io < )
			tweet.replaceAll(tweet.substring(io, io + 20), "")
			tweet.substr
			io = tweet.indexOf("http://t.co/");
		}
		tweet.replaceAll("RT", "");
		tweet.replaceAll("@name", "");
		
		//move each word into an array
		//sentiment on each word (accounting for not and but)
		//account for smileys
		//account for punctuation
		
		return 0;
	}

}
