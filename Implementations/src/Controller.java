import java.util.GregorianCalendar;
import java.util.Scanner;
import twitter4j.Twitter;


public class Controller
{
	public static void main(String[] args) 
	{
		final String tweetTableFile		= "superTweets.data",
					 tweetTableBackup	= "superTweetsBackup.data",
					 wordPolaritiesFile = "words.tst";
		TweetTable tweetTable;
		TST<PolarityValue> wordPolarities;
		
		// Load
		System.out.print("Loading... ");
		tweetTable = CollectionMethods.<TweetTable>load(tweetTableFile);
		wordPolarities = CollectionMethods.<TST<PolarityValue>>load(wordPolaritiesFile);
		System.out.println("Done");
		
		// Authenticate
		System.out.print("Authenticating... ");
		Twitter twitter = CollectionMethods.authenticate();
		System.out.println("Done");
		
		Thread trendingTweetPuller = new Thread(new TrendingTweetPull(tweetTable, wordPolarities, 
												tweetTableFile, tweetTableBackup, twitter));
		Thread Client = new Thread(new Client(tweetTable, wordPolarities, tweetTableFile, 
											  tweetTableBackup, twitter));
		Client.start();
		trendingTweetPuller.start();
	}
	private static class TrendingTweetPull implements Runnable
	{
		TweetTable tweetTable;
		TST<PolarityValue> wordPolarities;
		String tweetTableFile, tweetTableBackup;
		Twitter twitter;
		public TrendingTweetPull(TweetTable tweetTable, TST<PolarityValue> wordPolarities, 
								 String tweetTableFile, String tweetTableBackup, Twitter twitter)
		{
			this.tweetTable = tweetTable;
			this.wordPolarities = wordPolarities;
			this.tweetTableFile = tweetTableFile;
			this.tweetTableBackup = tweetTableBackup;
			this.twitter = twitter;
		}
		public void run() {
			TrendingTweetPuller.trendingTweetPull(tweetTable, wordPolarities, tweetTableFile, 
												  tweetTableBackup, twitter);
		}
	}
	private static class Client implements Runnable
	{
		TweetTable tweetTable;
		TST<PolarityValue> wordPolarities;
		String tweetTableFile, tweetTableBackup;
		Twitter twitter;
		public Client(TweetTable tweetTable, TST<PolarityValue> wordPolarities, String tweetTableFile, 
					  String tweetTableBackup, Twitter twitter)
		{
			this.tweetTable = tweetTable;
			this.wordPolarities = wordPolarities;
			this.tweetTableFile = tweetTableFile;
			this.tweetTableBackup = tweetTableBackup;
			this.twitter = twitter;
		}
		public void run() {
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
					System.out.print("Input: ");
					input = in.next();
					if (input.equals("save"))
					{
						System.out.println("Saving...");
						CollectionMethods.save(tweetTable, tweetTableFile);
						System.out.println("Done");
						continue;
					}
					if (input.equals("show"))
					{
						for (String s : tweetTable.getSubjects())
							System.out.println(s + ": " + tweetTable.subjectSize(s));
						continue;
					}
					try {
						SingleSubjectPuller.singleSubjectPull(input, tweetTable, wordPolarities, twitter);
					} catch (UnsuccessfulOperationException e) {
						System.err.println("Single Subject Pull Unsuccessful");
					}
					System.out.println("\nSize = " + tweetTable.subjectSize(input));
					for (SuperTweet tweet : tweetTable.getTweets(input))
					{
						polarization += tweet.getPolarization();
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
			while (true);
			
		}
		
	}
}
