import java.io.IOException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;

import org.hsqldb.jdbc.JDBCConnection;
import org.hsqldb.jdbc.JDBCPreparedStatement;

import twitter4j.FilterQuery;
import twitter4j.StatusStream;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.User;

public class Controller
{
	final static String statusTableFile	= "StatusTable.stb",
					 statusTableBackup	= "StatusTableBackup.stb",
					 wordPolaritiesFile = "words.tst";
	final static Queue<Status> toBeAdded = new Queue<Status>();
	static TweetEvaluator tweetEvaluator;
	static ArrayList<String> track = new ArrayList<String>();
	static ArrayList<Thread> streamThreads = new ArrayList<Thread>();
	static TwitterStream twitterStream;
	static Connection con;
	
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
	
	public static void main(String[] args) throws SQLException
	{
		PreparedStatement commitStatement = null;
		TST<PolarityValue> wordPolarities;
		
		// Load
		System.out.print("Loading... ");
		try {
			con = getConnection("/Users/Antonio/My Documents/Startup/AccelerandoDB/");System.out.println("1");
			//con.prepareStatement("DROP TABLE TWEETS").execute();
			//createTweetTable();
			//createSubjectTable();
			//createUserTable();System.out.println("4");
			//subjectConnection = getConnection("/Users/Antonio/My Documents/Startup/SubjectsDatabase/");
			//userConnection = getConnection("/Users/Antonio/My Documents/Startup/SubjectsDatabase/");
			//createTweetTable();
			
		} catch (SQLException e1) {
			System.out.println(e1);
			System.exit(0);
		}
		commitStatement = con.prepareStatement("COMMIT");
		//userCommitStatement = userConnection.prepareStatement("COMMIT");
		//subjectCommitStatement = subjectConnection.prepareStatement("COMMIT");
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
				try {
					commitStatement.executeUpdate();
				} catch (SQLException e) {
					System.err.println("SQLException: Automatic Commit Failed");
				}
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
	
	public static void createTweetTable() throws SQLException {
	    String createString =
	        "CREATE CACHED TABLE TWEET_TABLE " +
	        "(ID bigint NOT NULL, " +
	        "TEXT varchar(200) NOT NULL, " +
	        "DATE bigint, " +
	        "RETWEET_COUNT int NOT NULL, " +
	        "IS_RETWEET boolean NOT NULL, " +
	        "IS_FAVORITED boolean NOT NULL, " +
	        "USER_ID bigint NOT NULL, " +
	        "COUNTRY_CODE varchar(10), " +
	        "LATTITUDE float, " +
	        "LONGITUDE float, " +
	        "POLARIZATION float NOT NULL, " +
	        "WEIGHT float NOT NULL, " +
	        "PRIMARY KEY (ID))";

	    Statement stmt = null;
	    try {
	        stmt = con.createStatement();
	        stmt.executeUpdate(createString);
	    } 
	    finally {
	        if (stmt != null) { stmt.close(); }
	    }
	}

	public static void createSubjectTable() throws SQLException
	{
	    String createString =
		        "CREATE CACHED TABLE SUBJECTS" +
		        "(SUBJECT varchar(20) NOT NULL, " +
		        "RBBST BLOB, " +
		        "PRIMARY KEY (SUBJECT))";

		    Statement stmt = null;
		    try {
		        stmt = con.createStatement();
		        stmt.executeUpdate(createString);
		    }
		    finally {
		        if (stmt != null) { stmt.close(); }
		    }
	}
	
	public static void createUserTable() throws SQLException
	{
		 String createString =
			        "CREATE CACHED TABLE USERS " +
			        "(ID bigint NOT NULL, " +
			        "FOLLOWERS integer NOT NULL, " +
			        "FRIENDS integer NOT NULL, " +
			        "LISTED_COUNT integer NOT NULL, " +
			        "NAME varchar(40), " +
			        "SCREEN_NAME varchar(30) NOT NULL, " +
			        "NUM_TWEETS integer NOT NULL, " + 
			        "AVERAGE_POLARIZATION float NOT NULL, " +
			        "PRIMARY KEY (ID))";

		    Statement stmt = null;
		    try {
		        stmt = con.createStatement();
		        stmt.executeUpdate(createString);
		    }
		    finally {
		        if (stmt != null) { stmt.close(); }
		    }
	}
	public static Connection getConnection(String location) throws SQLException {

		Connection conn = null;
	    Properties connectionProps = new Properties();
	    connectionProps.put("user", "ajuliano");
	    connectionProps.put("password", "accelerando");
	    conn = DriverManager.getConnection("jdbc:hsqldb:file:" + location + "accelerandoDB", connectionProps);
	    System.out.println("Connected to database");
	    return conn;
	}
	
	private static class TweetTableAdder implements Runnable
	{
		public void run() 
		{
			PreparedStatement statusDBInsert = null, userDBInsert = null, subjectDBInsert = null,
								subjectBDQuery = null, userDBQuery = null, userDBUpdate = null;
			ResultSet subjectResults, userResults;
			String text;
			Blob blob;
			BlobRBBST rbbst;
			try {
				statusDBInsert = con.prepareStatement("insert into TWEET_TABLE " +
				        "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
				userDBInsert = con.prepareStatement("insert into USERS " + 
				        "values(?, ?, ?, ?, ?, ?, ?, ?)");
				subjectDBInsert = con.prepareStatement("insert into SUBJECTS " + 
				        "values(?, EMPTY_BLOB())");
				subjectBDQuery = con.prepareStatement("select * from SUBJECTS where SUBJECT = '?'");
				userDBQuery = con.prepareStatement("select * from USERS where ID = ?");
				userDBUpdate = con.prepareStatement("update USERS set FOLLOWERS = ?, FRIENDS = ?, " +
						"LISTED_COUNT = ?, NUM_TWEETS = ?, AVERAGE_POLARIZATION = ? WHERE ID = ?");
			} catch (SQLException e1) {
				System.out.println(e1);
				System.err.println("SQLException: Failed to Create Prepared Statement");
			}
			Status current;
			User currentUser;
			double polarization;
			int userTweetCount;
			for(;;)
			{
				if (!toBeAdded.isEmpty())
				{
					current = toBeAdded.dequeue();
					currentUser = current.getUser();
					polarization = tweetEvaluator.calculatePolarization(current);
					//TODO add to Databases
					try
					{
						// Add To Tweet Database
						statusDBInsert.setLong(1, current.getId());
						statusDBInsert.setString(2, current.getText());
						statusDBInsert.setLong(3, current.getCreatedAt().getTime());
						statusDBInsert.setInt(4, (int) current.getRetweetCount());
						statusDBInsert.setBoolean(5, current.isRetweet());
						statusDBInsert.setBoolean(6, current.isFavorited());
						statusDBInsert.setLong(7, currentUser.getId());
						if (current.getPlace() != null)
							statusDBInsert.setString(8, current.getPlace().getCountryCode());
						else
							statusDBInsert.setString(8, null);
						if (current.getGeoLocation() != null)
						{
							statusDBInsert.setDouble(9, current.getGeoLocation().getLatitude());	
							statusDBInsert.setDouble(10, current.getGeoLocation().getLongitude());
						}
						else
						{
							statusDBInsert.setDouble(9, Double.NaN);
							statusDBInsert.setDouble(10, Double.NaN);
						}
						statusDBInsert.setDouble(11, polarization);
						statusDBInsert.setDouble(12, tweetEvaluator.calculateWeight(current));
						statusDBInsert.executeUpdate();
						
						
						
						// Add To User Database
						userDBQuery.setLong(1, currentUser.getId());
						userResults = userDBQuery.executeQuery();
						if (userResults.next())
						{
							userTweetCount = userResults.getInt(7);
							userDBUpdate.setInt(1, currentUser.getFollowersCount());
							userDBUpdate.setInt(2, currentUser.getFriendsCount());
							userDBUpdate.setInt(3, currentUser.getListedCount());
							userDBUpdate.setInt(4, userTweetCount + 1);
							userDBUpdate.setDouble(5, (userResults.getDouble(8) * userTweetCount + polarization) /
													(userTweetCount + 1));
							userDBUpdate.setLong(6, currentUser.getId());
							userDBUpdate.executeUpdate();
						}
						else
						{
							userDBInsert.setLong(1, currentUser.getId());
							userDBInsert.setInt(2, currentUser.getFollowersCount());
							userDBInsert.setInt(3, currentUser.getFriendsCount());
							userDBInsert.setInt(4, currentUser.getListedCount());
							userDBInsert.setString(5, currentUser.getName());
							userDBInsert.setString(6, currentUser.getScreenName());
							userDBInsert.setInt(7, 1);
							userDBInsert.setDouble(8, polarization);
							userDBInsert.executeUpdate();
						}
						
						
						
						
						// Add To Subject Database
						text = current.getText();
						text = text.replaceAll("http:\\/\\/t.co\\/........", " ");
						text = text.toLowerCase();
						String[] tags = text.split("(\\W)+");
						for (int i = 0; i < tags.length; i++)
						{
							if (tags[i] == null || tags[i].length() == 0)
							{
								continue;
							}
							subjectBDQuery.setString(1, tags[i]);
							subjectResults = subjectBDQuery.executeQuery();
								
							if(subjectResults.next())
						    {
						    	blob = subjectResults.getBlob(2);
						    	rbbst = new BlobRBBST(blob);
						    	rbbst.put(current.getCreatedAt().getTime(), current.getId());
						    }
						    else
						    {
						    	subjectDBInsert.setString(1, tags[i]);
						    	subjectDBInsert.executeUpdate();
						    	subjectResults = subjectBDQuery.executeQuery();
						    	subjectResults.next();
						    	blob = subjectResults.getBlob(2);
						    	rbbst = new BlobRBBST(blob);
						    	rbbst.put(current.getCreatedAt().getTime(), current.getId());
						    }
						}
					}
					catch (Exception e)
					{
						System.err.println("Exception: Failed to add status to Database");
						System.err.println(e);
					}
				} 
				
				else
				{
					try 
					{
						Thread.sleep(100);
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
			JDBCPreparedStatement commitStatement = null;
			try {
				commitStatement = (JDBCPreparedStatement) con.prepareStatement("COMMIT");
			} catch (SQLException e1) {
				System.err.println(e1);
			}
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
					try {
						commitStatement.executeUpdate();
					} catch (SQLException e) {
						System.err.println("SQLException: Save Unsuccessful");
					}
					System.out.println("Done");
					continue;
				}
				/*
				if (input.equals("backup"))
				{
					System.out.println("Backing Up...");
					CollectionMethods.backup(statusTableFile, statusTableBackup);
					System.out.println("Done");
					continue;
				}
				*/
				if (input.equals("show"))
				{
					//TODO Write
					continue;
				}
				if (input.equals("size"))
				{
					//TODO Write
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
				if (input.equals("exit"))
				{
					synchronized (this)
					{
						try {
							commitStatement.executeUpdate();
							Statement s = con.createStatement();
							s.execute("SHUTDOWN");
							//s = userConnection.createStatement();
							//s.execute("SHUTDOWN");
							//s = statusConnection.createStatement();
							//s.execute("SHUTDOWN");
						} catch (SQLException e) {
							System.err.println("SQLException: Shutdown Failed");
						}
						System.exit(0);
					}
				}
					/*
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
				}*/
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
