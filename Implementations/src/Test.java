/**
 * Test Client: For Testing Purposes Only
 */

import java.util.*;
import java.io.*;
import twitter4j.*;
public class Test { 
	public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException
	{System.out.println("start");
		System.out.println("Loading");
		TST<PolarityValue> wordPolarities = CollectionMethods.<TST<PolarityValue>>load("words.tst");
		TweetTable t = (TweetTable)ObjectLoader.load("superTweets.data");
		
		double polarization = 0;
		String input = "";
		Scanner in = new Scanner(System.in);
		GregorianCalendar c = new GregorianCalendar();
		c.set(2012, 6, 7);
		int num = 0;
		do
		{
			try
			{
				polarization = 0;
				num = 0;
				System.out.println("Input: ");
				input = in.next();
				try {
					System.out.println("1");
					SingleSubjectPuller.singleSubjectPull(input, t, wordPolarities);
				} catch (UnsuccessfulOperationException e) {
					System.err.println("Unsuccessful");
				}
				RBBST<Date, SuperTweet> rbbst = t.getRBBST(input);
				System.out.println("\nSize = " + rbbst.size());
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
		while (!input.equals("exit"));
		System.out.println("saving...");
		CollectionMethods.save(t, "superTweets.data");
		System.out.println("done");
		/* Save new TweetTable
		ObjectLoader.save(new TweetTable(), "superTweets.data");
		*/
	}
}