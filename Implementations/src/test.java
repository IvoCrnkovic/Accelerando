/**
 * Test Client: For Testing Purposes Only
 */

import java.util.*;
import java.io.*;
import twitter4j.*;
public class test { 
	public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException
	{/*Get Subject Polarities
		TweetTable t = (TweetTable)ObjectLoader.load("superTweets.data");
		double polarization = 0;
		String input;
		Scanner in = new Scanner(System.in);
		GregorianCalendar c = new GregorianCalendar();
		c.set(2012, 6, 7);
		Date d = c.getTime();
		int num = 0;
		for (;;)
		{
			try
			{
			polarization = 0;
			num = 0;
			System.out.println("Input: ");
			input = in.next();
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
			catch (NullPointerException e)
			{
				System.out.println("Not Found");
			}
		}
		*/
		
		
		/* Save new TweetTable
		ObjectLoader.save(new TweetTable(), "superTweets.data");
		*/
	}
}
