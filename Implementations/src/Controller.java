import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Scanner;

import twitter4j.TwitterException;


public class Controller {

	/**
	 * @param args
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws TwitterException 
	 */
	public static void main(String[] args) {
		Scanner in = null;
		Scanner stdIn = new Scanner(System.in);
		GregorianCalendar origin = new GregorianCalendar();
		TweetEvaluator tweetEvaluator;
		TST<PolarityValue> wordPolarities = null;
		final String wordsFile = "words.tst";
		Object obj = null;
		TST<SuperUser> users = null;
		double finalSentiment = 0;
		boolean enoughData = true;
		Iterable<SuperTweet> targetTweets = null;


		TweetTable tweets = null;
		try {
			tweets = (TweetTable) ObjectLoader.load("superTweets.data");
		} catch (FileNotFoundException e3) {
			System.err.println("FileNotFoundException: Failed to load TweetTable from " + "superTweets.data");
			enoughData = false;
		} catch (IOException e3) {
			System.err.println("IOException: Failed to load TweetTable from " + "superTweets.data");
			enoughData = false;
		} catch (ClassNotFoundException e3) {
			System.err.println("ClassNotFoundException: Failed to load TweetTable from " + "superTweets.data");
			enoughData = false;
		} 


		//Get the subject
		//System.out.println("Enter something to analyze!");
		String subject = "Lady Gaga";
		String [] subjects = subject.split(" ");


		//Determine if we need to get more tweets on the entered subject.
		Date endTime = origin.getTime();
		origin.add(Calendar.HOUR_OF_DAY, -1);
		Date startTime = origin.getTime();

		int numberOfTweets = 0;
		
		try
		{
			targetTweets = tweets.getTweets(subjects, startTime, endTime);

			for (SuperTweet tweet : targetTweets)
			{
				numberOfTweets++;
			}
		}
		catch (NullPointerException e)
		{
			enoughData = false;
		}
		//decide if we have enough data and if not run single subject puller
		if (numberOfTweets < 10)
		{
			enoughData = false;
		}

		//Now that we have enough data, its time to actually do some analysis.

		//If we added tweets, we have to again go to the database and grab all the 
		//tweets that meet each subject.
		if (!enoughData)
		{
			for (int i = 0; i < subjects.length; i++)
			{
				try {
					SingleSubjectPuller.singleSubjectPull(subjects[i]);
				} catch (TwitterException e) {
					System.err.println("Twitter Exception");
					e.printStackTrace();
				}
			}
			targetTweets = tweets.getTweets(subjects, startTime, endTime);
		}


		try {
			obj = ObjectLoader.load(wordsFile);
		} catch (FileNotFoundException e3) {
			System.err.println("FileNotFoundException: Failed to load Word Polarizations from " + wordsFile);
			System.exit(0);
		} catch (IOException e3) {
			System.err.println("IOException: Failed to load Word Polarizations from " + wordsFile);
			System.exit(0);
		} catch (ClassNotFoundException e3) {
			System.err.println("ClassNotFoundException: Failed to load Word Polarizations from " + wordsFile);
			System.exit(0);
		} 
		if (obj instanceof TST<?>)
			wordPolarities = (TST<PolarityValue>) obj;
		else
		{
			System.err.println("Class Mismatch: Failed to load Word Polarizations from " + wordsFile);
			System.exit(0);
		}
		if (obj instanceof TST<?>)
			users = (TST<SuperUser>) obj;

		tweetEvaluator = new TweetEvaluator(wordPolarities, users);

		double totalWeight = 0;
		double totalSentiment = 0;
		double sentiment = 0;
		double weight = 0;

		for (SuperTweet tweet : targetTweets)
		{
			try {
				sentiment = tweetEvaluator.calculatePolarization(tweet);
				weight = tweetEvaluator.calculateWeight(tweet);
			} catch (TwitterException e) {
				System.err.println("Twitter Exception");
				e.printStackTrace();
			}
			totalWeight += weight;
			totalSentiment += sentiment * weight;
		}
		finalSentiment = totalSentiment / totalWeight;

		System.out.println("finalSentiment is " + finalSentiment);

	}
}
