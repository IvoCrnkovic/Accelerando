/**
 * Program to pull tweets matching a single set of subjects from twitter.
 * Written by Ivo Crnkovic-Rubsamen with code blocks from Antonio Juliano and Brendan Chou
 */

import java.util.*;
import java.io.*;
import twitter4j.*;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

public class SingleSubjectPuller {
	@SuppressWarnings("unchecked")
	public static void singleSubjectPull(String subject) throws TwitterException {
		// Files to Load From
		final String tweetTableFile = "superTweets.data";
		final String wordsFile = "words.tst";
		final String tweetTableBackup = "superTweetsBackup.data";

		// Number of tweets to pull
		final int tweetsToPull = 100;

		// Authenticate
		final String username = "turtleman755";
		final String password = "accelerando";
		final String consumerKey = "bceUVDFbpUh2pcu6gvpp9w";
		final String consumerSecret = "pvei5cAMJgy5qXQPjuyyki508ZxHPM6ypRJt94OW9sY";
		final String token = "17186983-6cc4E3GPsn1aFNrMsr5qJKpm8a0mxFl8ozsmUP43t";
		final String tokenSecret = "p4Sc5WrSdSL2R4cUxqal86QRosbW5txbFQYF5ItOow";
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true);
		cb.setUser(username);
		cb.setPassword(password);
		cb.setOAuthConsumerKey(consumerKey);
		cb.setOAuthConsumerSecret(consumerSecret);
		cb.setOAuthAccessToken(token);
		cb.setOAuthAccessTokenSecret(tokenSecret);
		TwitterFactory tf = new TwitterFactory(cb.build());
		AccessToken accessToken = new AccessToken(token, tokenSecret);
		Twitter twitter = tf.getInstance(accessToken);

		// Instance Variables
		TweetTable tweetTable = null;
		Date nextUpdate;
		ResponseList<Trends> trendsList;
		Query query;
		List<Tweet> tweets = null;
		List<User> usersToBeAdded = null;
		int trendsListSize, hourlyTrendArraySize, resultsSize, numTweets, totalNumTweets;
		TST<SuperUser> users = null;
		TST<PolarityValue> wordPolarities = null;
		TweetEvaluator tweetEvaluator;
		Object obj = null;
		Queue<TweetHolder> toBeAdded = new Queue<TweetHolder>();
		TweetHolder nextToBeAdded;
		SuperTweet superTweetToBeAdded;
		String text;
		String[] words;
		final int lookupSize = 100;
		String[] usersToLookup = new String[lookupSize];
		TweetHolder[] tweetsToBeAdded = new TweetHolder[lookupSize];
		String[] foundUsers;
		List<User> finalUsers;
		List<Tweet> finalTweets;

		// Load From File
		try {
			obj = ObjectLoader.load(tweetTableFile);
		} catch (FileNotFoundException e3) {
			System.err.println("FileNotFoundException: Failed to load TweetTable from " + tweetTableFile);
			System.exit(0);
		} catch (IOException e3) {
			System.err.println("IOException: Failed to load TweetTable from " + tweetTableFile);
			System.exit(0);
		} catch (ClassNotFoundException e3) {
			System.err.println("ClassNotFoundException: Failed to load TweetTable from " + tweetTableFile);
			System.exit(0);
		} 
		if (obj instanceof TweetTable)
			tweetTable = (TweetTable) obj;
		else
		{
			System.err.println("Class Mismatch: Failed to load TweetTable from " + tweetTableFile);
			System.exit(0);
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



		numTweets = 0;
		System.out.println(tweetTable.getSize() + " SuperTweets currently in TweetTable.");

		// Create Backup
		System.out.print("Backing Up... ");
		try {
			ObjectLoader.backupFile(tweetTableFile, tweetTableBackup);
		} catch (FileNotFoundException e3) {
			System.err.println("FileNotFoundException: Unable to Create Backup of TweetTable");
		} catch (NullPointerException e3) {
			System.err.println("NullPointerException: Unable to Create Backup of TweetTable");
		} catch (IOException e3) {
			System.err.println("IOException: Unable to Create Backup of TweetTable");
		}
		System.out.println("Done.");


		// Gather Tweets
		System.out.print("Gathering Tweets... ");
		query = new Query(subject);
		query.setRpp(tweetsToPull);
		query.setLang("en");
		try
		{
			tweets = twitter.search(query).getTweets();
		}
		catch (TwitterException e)
		{

		}
		resultsSize = tweets.size();
		for(int k = 0; k < resultsSize; k++)
		{
			numTweets++;
			toBeAdded.enqueue(new TweetHolder(tweets.get(k), subject));
			//tweetTable.add(new SuperTweet(tweets.get(k), trendName, tweetEvaluator, twitter));
		}
		System.out.println("Done.\n" + numTweets + " Tweets Collected.\nAdding Tweets to TweetTable... ");


		//Add Tweets to TweetTable Based Around Rate Limit
		numTweets = 0;
		totalNumTweets = 0;
		while (!toBeAdded.isEmpty())
		{
			try
			{
				// Ignore last tweets in Queue, possibly change later
				if (toBeAdded.size() < lookupSize)
					continue;

				for (int i = 0; i < lookupSize; i++)
				{
					tweetsToBeAdded[i] = toBeAdded.dequeue();
					usersToLookup[i] = tweetsToBeAdded[i].tweet.getFromUser();
				}
				usersToBeAdded = twitter.lookupUsers(usersToLookup);
				// Match up users and tweets
				foundUsers = new String[usersToBeAdded.size()];
				int count = 0;
				for (User u : usersToBeAdded)
				{
					foundUsers[count] = u.getName();
					count++;
				}
				Arrays.sort(foundUsers);
				for (int i = 0; i < lookupSize; i++)
					Arrays.binarySearch(foundUsers, tweetsToBeAdded[i].tweet.getFromUser());


				for (int i = 0; i < lookupSize; i++)
				{
					text = tweetsToBeAdded[i].tweet.getText();
					text = text.replaceAll("http:\\/\\/t.co\\/........", " ");
					text = text.toLowerCase();
					words = text.split("(\\W)+");
					superTweetToBeAdded = new SuperTweet(tweetsToBeAdded[i].tweet, usersToBeAdded.get(i), words,
							tweetEvaluator, twitter);
					tweetTable.add(superTweetToBeAdded, tweetsToBeAdded[i].subject);

					for (int j = 0; j < words.length; j++)
					{
						if (words[j].length() == 0 || words[j] == null)
							continue;
						//TODO Check if this actually stores a reference
						tweetTable.add(superTweetToBeAdded, words[j]);
					}
					numTweets++;
					totalNumTweets++;
				}

			}
			catch (TwitterException e)
			{
				if (e.exceededRateLimitation())
				{
					System.out.println("Hourly Limit of " + e.getRateLimitStatus().getHourlyLimit() + " requests has been Reached.\n" +
							numTweets + " Tweets have been added to the TweetTable in the Past Hour.\nProcess will Resume" +
							": " + e.getRateLimitStatus().getResetTime());
					nextUpdate = e.getRateLimitStatus().getResetTime();

					// Create Backups
					System.out.print("Backing Up... ");
					try {
						ObjectLoader.backupFile(tweetTableFile, tweetTableBackup);
					} catch (FileNotFoundException e3) {
						System.err.println("FileNotFoundException: Unable to Create Backup of TweetTable");
					} catch (NullPointerException e3) {
						System.err.println("NullPointerException: Unable to Create Backup of TweetTable");
					} catch (IOException e3) {
						System.err.println("IOException: Unable to Create Backup of TweetTable");
					}
					System.out.println("Done.");

					// Save
					System.out.print("Saving... ");
					try {
						ObjectLoader.save(tweetTable, tweetTableFile);
					} catch (FileNotFoundException e1) {
						System.err.println("FileNotFoundException: Could not save TweetTable to " + tweetTableFile);
					} catch (IOException e1) {
						System.err.println("IOException: Could not save TweetTable to " + tweetTableFile);
					}
					System.out.println("Done.");

					numTweets = 0;

					// Wait Until Rate Limit Resets
					while(new Date().before(nextUpdate))
					{
						try {
							Thread.sleep(10000);
						} catch (InterruptedException e2) {
							System.err.println("Interrupted");
						}
					}
				}
				else
				{
					if (e.isErrorMessageAvailable())
						System.err.println(e.getErrorMessage());
				}
			}
		}


		System.out.print("Finished Adding Tweets to TweetTable.\n" + totalNumTweets + " Tweets Added.\nSaving... ");
		try {
			ObjectLoader.save(tweetTable, tweetTableFile);
		} catch (FileNotFoundException e1) {
			System.err.println("FileNotFoundException: Could not save TweetTable to " + tweetTableFile);
		} catch (IOException e1) {
			System.err.println("IOException: Could not save TweetTable to " + tweetTableFile);
		}
		System.out.println("Done.");
	}
	private static class TweetHolder
	{
		private String subject;
		private Tweet tweet;
		public TweetHolder(Tweet tweet, String subject)
		{
			this.subject = subject;
			this.tweet = tweet;
		}
	}
}
