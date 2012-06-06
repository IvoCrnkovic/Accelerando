import java.io.File;
import java.util.*;

import twitter4j.*;

public class TweetEvaluator {
	
	
	public static void main (String [] args)//testing purposes only
	{
		String words [] = {"the", "donkey", "eats", "poo"};
		wordSentiment(words);
	}
	
	
	public static double calculatePolarization(Tweet tweet) {
		return determineSentiment(tweet.getText());
	}
	
	public static double calculateWeight(Tweet tweet) {
		return 1;
	}
	
	private static String removeFluff(String text)
	{
		int io = text.indexOf("http://t.co/");
		while(io > -1)
		{
			if(io + 20 < text.length())
				text = text.replaceAll(text.substring(io, io + 20), "");
			else
				text = text.replaceAll(text.substring(io, text.length()), "");
			
			io = text.indexOf("http://t.co/");
		}		
		return text;
	}
	
	private static String[] tweetToArray(String text)
	{
		text = text.toLowerCase();
		text = text.replaceAll("[^abcdefghijklmnopqrstuvwxyz0123456789]", " ");
		String[] tokens = text.split("\\s+");
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
	
	private static double smileySentiment(String text)
	{
		int a=0,b=0,c=0,d=0,e=0,len=text.length();
		char[] eye  = {':',';','8','=','x','X'};
		char[] nose = {'-','^','\''};
		char[] good = {')',']','}','3','>','D','p','*'};
		char[] bad  = {'(','[','{','<'};
		char[] okay = {'|'};
		char[] skep = {'\\','/'};
		char[] surp = {'o','O','0','@'};
		
		for(int i = 0; i < len - 1; i++)
		{
			if(contains(eye, text.charAt(i)))
			{
				if(contains(nose, text.charAt(i+1)) && i < len - 2)
				{
					if(contains(good, text.charAt(i+2)))
						a++;
					else if(contains(bad, text.charAt(i+2)))
						b++;
					else if(contains(okay, text.charAt(i+2)))
						c++;
					else if(contains(skep, text.charAt(i+2)))
						d++;
					else if(contains(surp, text.charAt(i+2)))
						e++;
				}
				else
				{
					if(contains(good, text.charAt(i+1)))
						a++;
					else if(contains(bad, text.charAt(i+1)))
						b++;
					else if(contains(okay, text.charAt(i+1)))
						c++;
					else if(contains(skep, text.charAt(i+1)))
						d++;
					else if(contains(surp, text.charAt(i+1)))
						e++;
				}
			}
		}
		
		double total = a+b+c+d+e+.5;
		double sentiment = ((5*a) - (5*b) - (1*c) - (3*d)) / total;
		
		return sentiment;
	}
	
	private static double wordSentiment(String[] words)
	{
		double totalWordSentiment = 0;
		File wordsFile = new File("../Implementations/words.txt");
		
		TST<PolarityGenerator.Value> wordsTST = PolarityGenerator.load(wordsFile);
		
		for (int i = 0; i < words.length;i++)
		{
			System.out.println(wordsTST.get(words [i]));
		}
		
		
		return totalWordSentiment;
	}
	
	private static double getWordPolarity(String word)
	{
		double polarity = 0;
		return polarity;
	}
	
	private static double punctuationSentiment(String text)
	{
		int[] punctuation = new int[2];
		int length = text.length();
		for(int i = 0; i < length; i++)
		{
			if(text.charAt(i) == '!')
				punctuation[0]++;
			else if (text.charAt(i) == '?')
				punctuation[1]++;
		}
		double total = punctuation[0] + punctuation[1] + 1;
		return (punctuation[0]+.5)/total;
	}
	
	private static double determineSentiment(String text)
	{
		text = removeFluff(text);
		double sSent = smileySentiment(text);
		double pSent = punctuationSentiment(text);
		double wSent = wordSentiment(tweetToArray(text));
		
		return pSent * (sSent + wSent) / 2;
	}
}
