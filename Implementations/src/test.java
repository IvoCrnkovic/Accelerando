/**
 * Test Client: For Testing Purposes Only
 */

import java.util.*;
import java.io.*;
import twitter4j.*;
public class test { 
	public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException
	{
		TweetTable t = (TweetTable)ObjectLoader.load("superTweets.data");
		double polarization = 0;
		String input;
		Scanner in = new Scanner(System.in);
		GregorianCalendar c = new GregorianCalendar();
		c.set(2012, 6, 7);
		Date d = c.getTime();
		int num = 0;
		polarization = 0;
		num = 0;
		input = "it";
		RBBST<Date, SuperTweet> rbbst = t.getRBBST(input);
		System.out.println("Size = " + rbbst.size());
		for (Date date : rbbst.keys())
		{
			polarization += rbbst.get(date).getPolarization();
			num++;
		}
		polarization /= num;
		System.out.println("Polarization: " + polarization);
	}
}
