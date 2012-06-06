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
		String tweet = "@BrookeHyland1 Ur the best dancer! Plz follow me?? I love u! I wish u were my sis and had ur dancing abilities! I hope u get a solo tonight :D";
		System.out.println(smileySentiment(tweet));
		System.out.println(punctuationSentiment(tweet));
		System.out.println(wordSentiment(tweetToArray(tweet)));
		System.out.println(determineSentiment(tweet));
	}
	
	private static String removeFluff(String tweet)
	{
		//remove fluff (@ and # and http)
		int io = tweet.indexOf("http://t.co/");
		while(io > -1)
		{
			if(io + 20 < tweet.length())
				tweet = tweet.replaceAll(tweet.substring(io, io + 20), "");
			else
				tweet = tweet.replaceAll(tweet.substring(io, tweet.length()), "");
			
			io = tweet.indexOf("http://t.co/");
		}
		/*tweet.replaceAll("RT", "");
		tweet.replaceAll("@name", "");*/
		
		return tweet;
	}
	
	private static String[] tweetToArray(String tweet)
	{
		tweet = tweet.toLowerCase();
		tweet = tweet.replaceAll("[^abcdefghijklmnopqrstuvwxyz0123456789]", " ");
		String[] tokens = tweet.split("\\s+");
		return tokens;
	}
	
	private static boolean contains(char[] array, char c)
	{
		int length = array.length;
		for(int i = 0; i < length; i++)
		{
			if(array[i] == c)
				return true;
		}
		return false;
	}
	
	private static double smileySentiment(String tweet)
	{
		int a=0,b=0,c=0,d=0,e=0,len=tweet.length();
		char[] eye  = {':',';','8','=','x','X'};
		char[] nose = {'-','^','\''};
		char[] good = {')',']','}','3','>','D','p','*'};
		char[] bad  = {'(','[','{','<'};
		char[] okay = {'|'};
		char[] skep = {'\\','/'};
		char[] surp = {'o','O','0','@'};
		
		for(int i = 0; i < len - 1; i++)
		{
			if(contains(eye, tweet.charAt(i)))
			{
				if(contains(nose, tweet.charAt(i+1)) && i < len - 2)
				{
					if(contains(good, tweet.charAt(i+2)))
						a++;
					else if(contains(bad, tweet.charAt(i+2)))
						b++;
					else if(contains(okay, tweet.charAt(i+2)))
						c++;
					else if(contains(skep, tweet.charAt(i+2)))
						d++;
					else if(contains(surp, tweet.charAt(i+2)))
						e++;
				}
				else
				{
					if(contains(good, tweet.charAt(i+1)))
						a++;
					else if(contains(bad, tweet.charAt(i+1)))
						b++;
					else if(contains(okay, tweet.charAt(i+1)))
						c++;
					else if(contains(skep, tweet.charAt(i+1)))
						d++;
					else if(contains(surp, tweet.charAt(i+1)))
						e++;
				}
			}
		}
		
		int[] toRet = {a,b,c,d,e};
		
		double total = a+b+c+d+e+.5;
		double sentiment = ((5*a) - (5*b) - (1*c) - (3*d)) / total;
		
		return sentiment;
	}
	
	private static double wordSentiment(String[] word)
	{
		return 5;
	}
	
	private static double punctuationSentiment(String tweet)
	{
		int[] punctuation = new int[2];
		int length = tweet.length();
		for(int i = 0; i < length; i++)
		{
			if(tweet.charAt(i) == '!')
				punctuation[0]++;
			else if (tweet.charAt(i) == '?')
				punctuation[1]++;
		}
		double total = punctuation[0] + punctuation[1] + 1;
		return (punctuation[0]+.5)/total;
	}
	
	private static double determineSentiment(String tweet)
	{
		tweet = removeFluff(tweet);
		double sSent = smileySentiment(tweet);
		double pSent = punctuationSentiment(tweet);
		double wSent = wordSentiment(tweetToArray(tweet));
		
		return pSent * (sSent + wSent) / 2;
	}
	

}
