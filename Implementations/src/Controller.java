import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;

import twitter4j.FilterQuery;
import twitter4j.StatusStream;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.User;

//TODO
//postgres
//MongoDb
//Hadoop

public class Controller
{
	final static String statusTableFile	= "StatusTable.stb",
					 statusTableBackup	= "StatusTableBackup.stb",
					 wordPolaritiesFile = "words.tst";
	final static Queue<Status> toBeAdded = new Queue<Status>();
	final static int MAX_TAGS = 20;
	static TweetEvaluator tweetEvaluator;
	static ArrayList<String> track = new ArrayList<String>();
	static ArrayList<Thread> streamThreads = new ArrayList<Thread>();
	static TwitterStream twitterStream;
	static Connection con;
	static long tweetNum = 1;
	
	static StatusListener listener = new StatusListener(){
	    public void onStatus(Status status)
	    {
	    	//TODO CHANGE THIS
	    	//TODO Implement Language Detection
	    	try {
				Detector langDetector = DetectorFactory.create();
				langDetector.append(status.getText());
				if (!langDetector.detect().equals("en"))
				{
					return;
				}
			} catch (LangDetectException e) {
				return;
			}
	    	if (toBeAdded.size() < 10000)
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
		PreparedStatement setFilesLogFalse = null, setFilesLogTrue = null, checkpoint = null,
							checkpointDefrag = null;
		System.out.print("Loading... ");
		try {
			con = getConnection("/Users/Antonio/My Documents/Startup/AccelerandoHSQLDB/");
			
			/*createTweetTable();
			//createSubjectTable();
			createUserTable();
			Statement s = con.createStatement();
			//s.execute("SET DEFAULT COLLATION TWEET_TABLE NO PAD");
			for (int i = 1; i <= MAX_TAGS; i++)
				s.execute("CREATE INDEX tag" + i + "_index ON tweet_table (tag" + i + ")");
			s.execute("CREATE INDEX date_index ON tweet_table (date)");
			s.close();*/
			setFilesLogFalse = con.prepareStatement("SET FILES LOG FALSE");
			setFilesLogTrue = con.prepareStatement("SET FILES LOG TRUE");
			checkpoint = con.prepareStatement("CHECKPOINT");
			checkpointDefrag = con.prepareStatement("CHECKPOINT DEFRAG");
			setFilesLogFalse.execute();
			checkpoint.execute();
		} 
		catch (SQLException e1) {
			e1.printStackTrace();
			System.out.println(e1);
			System.exit(0);
		}
		wordPolarities = CollectionMethods.<TST<PolarityValue>>load(wordPolaritiesFile);
		tweetEvaluator = new TweetEvaluator(wordPolarities);
		System.out.println("Done");
		
		// Authenticate
		System.out.print("Authenticating... ");
		twitterStream = CollectionMethods.authenticateStream();
		System.out.println("Done");
		
		//Initialize Language Detection
		try {
			DetectorFactory.loadProfile("lib/profiles");
		} catch (LangDetectException e2) {
			System.err.println("Failed to load language profile");
			System.exit(0);
		}
		
		
	    twitterStream.addListener(listener);
	    twitterStream.sample();
		
		
		Thread Client = new Thread(new Client());
		Thread tweetTableAdder = new Thread(new Adder());
		
		Client.start();
		tweetTableAdder.setPriority(Thread.MAX_PRIORITY);
		tweetTableAdder.start();
		//new Thread(new Adder()).start();
		//new Thread(new Adder()).start();
		
		
		long nextSave = System.currentTimeMillis() + 100000L;
		long nextDefrag = System.currentTimeMillis() + 900000L;
		
		for(;;)
		{
			if (System.currentTimeMillis() > nextSave)
			{System.out.print("Autosaving... ");
				try {
					if (System.currentTimeMillis() > nextDefrag)
					{System.out.print("Defragging... ");
						setFilesLogTrue.execute();
						checkpointDefrag.execute();
						setFilesLogFalse.execute();
						nextDefrag = System.currentTimeMillis() + 900000L;
					}
					checkpoint.execute();
					System.out.println("Done.");
				} catch (SQLException e) {
					System.err.println("SQLException: Automatic Commit Failed");
				}
				nextSave = System.currentTimeMillis() + 100000L;
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
	        "TAG1 varchar(140), " +
	        "TAG2 varchar(140), " +
	        "TAG3 varchar(140), " +
	        "TAG4 varchar(140), " +
	        "TAG5 varchar(140), " +
	        "TAG6 varchar(140), " +
	        "TAG7 varchar(140), " +
	        "TAG8 varchar(140), " +
	        "TAG9 varchar(140), " +
	        "TAG10 varchar(140), " +
	        "TAG11 varchar(140), " +
	        "TAG12 varchar(140), " +
	        "TAG13 varchar(140), " +
	        "TAG14 varchar(140), " +
	        "TAG15 varchar(140), " +
	        "TAG16 varchar(140), " +
	        "TAG17 varchar(140), " +
	        "TAG18 varchar(140), " +
	        "TAG19 varchar(140), " +
	        "TAG20 varchar(140), " +
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
	
	private static class Adder implements Runnable
	{
		static PreparedStatement userDBInsert = null, statusDBInsert = null,
								userDBQuery = null, userDBUpdate = null;
		static int threadsActive;
		static int batchSize = 0;
		public void run() 
		{
			try {
				statusDBInsert = con.prepareStatement("insert into TWEET_TABLE " +
				        "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "+
						"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
				userDBInsert = con.prepareStatement("insert into USERS " + 
				        "values(?, ?, ?, ?, ?, ?, ?, ?)");
				
				
				userDBQuery = con.prepareStatement("select * from USERS where ID = ?");
				userDBUpdate = con.prepareStatement("update USERS set FOLLOWERS = ?, FRIENDS = ?, " +
						"LISTED_COUNT = ?, NUM_TWEETS = ?, AVERAGE_POLARIZATION = ? WHERE ID = ?");
				
			} catch (SQLException e1) {
				System.err.println(e1);
				System.err.println("SQLException: Failed to Create Prepared Statement");
			}
			addStatus();
		}
		
		//TODO Multithread Adder
			static void addStatus()
			{
				Status current;
				User currentUser;
				double polarization;
/*
				if (batchSize > 25000)
				{
					System.out.print("Batch Updating... ");
					try {
						statusDBInsert.executeBatch();
					} catch (Exception e) {
						System.err.println("Batch Update Failed");
						e.printStackTrace();
					}
					batchSize = 0;
					System.out.println("Done");
				}*/
				
				if (!toBeAdded.isEmpty())
				{
					try
					{
						current = toBeAdded.dequeue();
						currentUser = current.getUser();
						polarization = tweetEvaluator.calculatePolarization(current);
						//TODO Remove usernames after @
						//TODO Create Tag Ignore List
						threadsActive = 2;
						new Thread(new AddToTweetTable(current, polarization)).start();
						new Thread(new AddToUserTable(currentUser, polarization)).start();
						//System.out.println("TWEET NO " + tweetNum++);
					}
					catch (Exception e)
					{
						System.err.println("Failed to add Status to Database");
						e.printStackTrace();
						addStatus();
						return;
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
					addStatus();
				}
			}
			static class AddToTweetTable implements Runnable
			{
				Status current;
				double polarization;
				public AddToTweetTable(Status c, double p)
				{
					current = c;
					polarization = p;
				}
				public void run()
				{
					// Add To Tweet Database
					try
					{
						statusDBInsert.setLong(1, current.getId());
						statusDBInsert.setString(2, current.getText());
						statusDBInsert.setLong(3, current.getCreatedAt().getTime());
						statusDBInsert.setInt(4, (int) current.getRetweetCount());
						statusDBInsert.setBoolean(5, current.isRetweet());
						statusDBInsert.setBoolean(6, current.isFavorited());
						statusDBInsert.setLong(7, current.getUser().getId());
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
						
						//Add Tags
						String text;
						text = current.getText();
						text = text.replaceAll("http:\\/\\/t.co\\/........", " ");
						text = text.toLowerCase();
						String[] tags = text.split("(\\W)+");
						
						int k = 0;
						outerloop: for (int i = 0; i < tags.length; i++)
						{
							for (int j = 0; j < i; j++)
								if (tags[j].equals(tags[i]))
									continue outerloop;
							if (tags[i] == null || tags[i].length() == 0)
							{
								continue;
							}
							if (k >= MAX_TAGS)
								break;
							
							statusDBInsert.setString(13 + k, tags[k]);
							k++;
						}
						for (; k < MAX_TAGS; k++)
						{
							statusDBInsert.setString(13 + k, null);
						}/*
						synchronized (Adder.class)
						{
							batchSize++;
						}*/
						//statusDBInsert.addBatch();
						statusDBInsert.executeUpdate();
					}
					catch (Exception e1)
					{
						System.err.println("Exception: Failed to add to Tweet Table");
						System.err.println(current.getId());
						e1.printStackTrace();
						synchronized (Adder.class)
						{
							threadsActive--;
							if (threadsActive == 0)
								addStatus();
						}
						return;
					}
					synchronized (Adder.class)
					{
						threadsActive--;
						if (threadsActive == 0)
							addStatus();
					}
				}
			}
			static class AddToUserTable implements Runnable
			{
				double polarization;
				User currentUser;
				ResultSet userResults;
				int userTweetCount;
				public AddToUserTable(User u, double d)
				{
					polarization = d;
					currentUser = u;
				}
				public void run()
				{
					try
					{
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
					}
					catch (Exception e1)
					{
						System.err.println("Exception: Failed to add to User Table");
						synchronized (Adder.class)
						{
							threadsActive--;
							if (threadsActive == 0)
								addStatus();
						}
						return;
					}
					
					synchronized (Adder.class)
					{
						threadsActive--;
						if (threadsActive == 0)
							addStatus();
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
			PreparedStatement querySubjects = null; 
			PreparedStatement[]  queryTweetPolarization = new PreparedStatement[MAX_TAGS];
			for (int i = 1; i <= MAX_TAGS; i++)
				try {
					queryTweetPolarization[i - 1] = con.prepareStatement("select polarization from tweet_table where tag" + i + " = ?");
				} catch (SQLException e) {
					e.printStackTrace();
				}
			Scanner in = new Scanner(System.in);
			do
			{
				System.out.print("Input: ");
				input = in.nextLine();
				System.out.println();
				if (input.equals("save"))
				{
					System.out.println("Saving...");
					Statement s = null;
					try {
						s = con.createStatement();
						s.execute("SET FILES LOG TRUE");
						s.execute("CHECKPOINT DEFRAG");
						s.execute("SET FILES LOG FALSE");
						s.execute("CHECKPOINT");
					} catch (SQLException e) {
						System.err.println("SQLException: Save Unsuccessful");
					}
					finally
					{
						if (s != null)
							try {
								s.close();
							} catch (SQLException e) {
								e.printStackTrace();
							}
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
					try
					{
						Statement s = con.createStatement();
						ResultSet r = s.executeQuery("select count(id) from tweet_table");
						r.next();
						System.out.println("Status Table Size = " + r.getInt(1));
						r = s.executeQuery("select count(id) from users");
						r.next();
						System.out.println("User Table Size = " + r.getInt(1));
						s.close();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
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
					synchronized (Controller.class)
					{/*
						System.out.print("Batch Updating... ");
						try {
							statusDBInsert.executeBatch();
						} catch (Exception e) {
							System.err.println("Batch Update Failed");
							e.printStackTrace();
						}
						System.out.println("Done.");*/
						System.out.print("Shutting Down...");
						try {
								
							Statement s = con.createStatement();
							s.execute("SHUTDOWN");
							//s = userConnection.createStatement();
							//s.execute("SHUTDOWN");
							//s = statusConnection.createStatement();
							//s.execute("SHUTDOWN");
						} catch (SQLException e) {
							System.err.println("SQLException: Shutdown Failed");
						}
						System.out.println("Done.");
						System.exit(0);
					}
				}
				
				if (input.equals("p"))
				{//TODO Implement multiple tag search
					String subject;
					double polarization = 0;
					System.out.print("Subject: ");
					subject = in.nextLine().toLowerCase();
					System.out.println();
					if (subject == null || subject.equals(""))
					{
						System.err.println("Illegal Key");
						continue;
					}
					ResultSet results;
					long size = 0;
					
					for (int i = 0; i < MAX_TAGS; i++)
					{
						try
						{
							queryTweetPolarization[i].setString(1, subject);
							results = queryTweetPolarization[i].executeQuery();
							while (results.next())
							{
								size++;
								polarization += results.getDouble(1);
							}
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
					if (size != 0L)
					polarization /= size;
					System.out.println("Subject Size = " + size);
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
