import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import twitter4j.FilterQuery;
import twitter4j.StatusStream;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;

public class Controller
{
	final static String statusTableFile	= "StatusTable.stb",
					 statusTableBackup	= "StatusTableBackup.stb",
					 wordPolaritiesFile = "words.tst";
	final static Queue<Status> toBeAdded = new Queue<Status>();
	static StatusTable statusTable;
	static TweetEvaluator tweetEvaluator;
	static ArrayList<String> track = new ArrayList<String>();
	static ArrayList<Thread> streamThreads = new ArrayList<Thread>();
	static TwitterStream twitterStream;
	static StatusListener listener = new StatusListener(){
	    public void onStatus(Status status)
	    {
	            toBeAdded.enqueue(status);
	    }
	    public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) 
	    {
	    	//System.err.println("Deletion Notice");
	    }
	    public void onTrackLimitationNotice(int numberOfLimitedStatuses) 
	    {
	    	System.err.println("Limitation Notice: " + numberOfLimitedStatuses);
	    }
	    public void onException(Exception ex) {
	    	System.err.println("Exception");
	    }
	    public void onScrubGeo(long arg0, long arg1) {
			System.err.println("ScrubGeo");
	    }
	};
	public static void main(String[] args) 
	{
		TST<PolarityValue> wordPolarities;
		
		// Load
		System.out.print("Loading... ");
		statusTable = CollectionMethods.<StatusTable>load(statusTableFile);
		wordPolarities = CollectionMethods.<TST<PolarityValue>>load(wordPolaritiesFile);
		tweetEvaluator = new TweetEvaluator(wordPolarities);
		System.out.println("Done");
		
		// Authenticate
		System.out.print("Authenticating... ");
		twitterStream = CollectionMethods.authenticateStream();
		System.out.println("Done");
		
		
		
	    twitterStream.addListener(listener);
	    twitterStream.sample();
		
		
		Thread Client = new Thread(new Client());
		Thread tweetTableAdder = new Thread(new TweetTableAdder());
		
		Client.start();
		tweetTableAdder.setPriority(Thread.MAX_PRIORITY);
		tweetTableAdder.start();
		
		long nextSave = System.currentTimeMillis() + 600000;
		for(;;)
		{
			if (System.currentTimeMillis() > nextSave)
			{
				nextSave = System.currentTimeMillis() + 600000;
				CollectionMethods.backup(statusTableFile, statusTableBackup);
				CollectionMethods.save(statusTable, statusTableFile);
			}
			else
			{
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					System.err.println("Interrupted");
					return;
				}
			}
		}
		
	}
	private static class TweetTableAdder implements Runnable
	{
		public void run() 
		{
			Status current;
			String text;
			String[] words;
			for(;;)
			{
				if (!toBeAdded.isEmpty())
				{
					current = toBeAdded.dequeue();
					text = current.getText();
		    		text = text.replaceAll("http:\\/\\/t.co\\/........", " ");
		    		text = text.toLowerCase();
		    		words = text.split("(\\W)+");
					
					//TODO Add subject to tags list
					try {
						statusTable.add(new SuperStatus(current, words, tweetEvaluator));
					} catch (TwitterException e) {
					}
				} 
				
				else
				{
					try 
					{
						Thread.sleep(1000);
					} 
					catch (InterruptedException e) 
					{
						System.err.println("Adder Interrupted");
					}
				}
			}
		}
	}
	private static class Client implements Runnable
	{
		public void run() {
			String input = "";
			FilterQuery query;
			Thread thread;
			Scanner in = new Scanner(System.in);
			do
			{
					System.out.print("Input: ");
					input = in.nextLine();
					System.out.println();
					if (input.equals("save"))
					{
						System.out.println("Saving...");
						CollectionMethods.save(statusTable, statusTableFile);
						System.out.println("Done");
						continue;
					}
					if (input.equals("backup"))
					{
						System.out.println("Backing Up...");
						CollectionMethods.backup(statusTableFile, statusTableBackup);
						System.out.println("Done");
						continue;
					}
					if (input.equals("show"))
					{
						for (String s : statusTable.getSubjects())
							System.out.println(s + ": " + statusTable.subjectSize(s));
						continue;
					}
					if (input.equals("size"))
					{
						System.out.println("Size: " + statusTable.size());
						continue;
					}
					if (input.equals("memory"))
					{
						System.out.println("Total Memory: " + Runtime.getRuntime().totalMemory() / 1000000 + " MB");
						System.out.println("Free Memory: " + Runtime.getRuntime().freeMemory() / 1000000 + " MB");
						continue;
					}
					if (input.equals("open stream"))
					{
						System.out.print("Subject: ");
						input = in.nextLine().toLowerCase();
						System.out.println();
						if (track.contains(input))
						{
							System.out.println("Stream Already Exists");
							continue;
						}
						track.add(input);
						query = new FilterQuery();
						query.track(input.split(" "));
						try {
							thread = new Thread(new SubjectStream(twitterStream.getFilterStream(query), track.indexOf(input)));
						} catch (TwitterException e) {
							System.err.println("Twitter Exception: Unable to open stream");
							continue;
						}
						streamThreads.add(thread);
						thread.setPriority(Thread.MIN_PRIORITY);
						thread.start();
						continue;
					}
					if (input.equals("show streams"))
					{
						for (String s : track)
							System.out.println(s);
						continue;
					}
					if (input.equals("close stream"))
					{
						System.out.print("Subject: ");
						input = in.nextLine();
						System.out.println();
						int index = track.indexOf(input);
						if (index == -1)
						{
							System.out.println("Not Found");
							continue;
						}
						track.remove(index);
						streamThreads.get(index).interrupt();
						continue;
					}
					if (input.equals("queue size"))
					{
						System.out.println("Queue Size: " + toBeAdded.size());
						continue;
					}
					if (input.equals("p"))
					{
						String subject;
						int num = 0;
						double polarization = 0;
						System.out.print("Subject: ");
						subject = in.nextLine().toLowerCase();
						System.out.println();
						if (subject == null || subject.equals(""))
						{
							System.err.println("Illegal Key");
							continue;
						}
						System.out.println("Subject Size = " + statusTable.subjectSize(subject.split(" ")));
						for (SuperStatus s : statusTable.getTweets(subject.split(" ")))
						{
							polarization += s.getPolarization() * s.getWeight();
							num++;
						}
						if (num != 0)
							polarization /= (double) num;
						System.out.println("Polarization = " + polarization);
						continue;
					}
			}
			while (true);
		}
	}
	private static class SubjectStream implements Runnable
	{
		StatusStream stream;
		int index;
		public SubjectStream(StatusStream s, int index)
		{
			stream = s;
			this.index = index;
		}
		public void run() {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			for(;;)
			{
				if(Thread.interrupted())
				{
					try {
						stream.close();
					} catch (IOException e) {
						System.err.println("Failed to close StatusStream");
					}
					return;
				}
				try {
					stream.next(listener);
				} catch (TwitterException e) {/*
					track.remove(index);
					try {
						stream.close();
						streamThreads.remove(index);
					} catch (IOException e1) {
						System.err.println("Failed to close StatusStream");
					}*/
					System.err.println("Twitter Exception");
				}
			}
		}
	}
}
