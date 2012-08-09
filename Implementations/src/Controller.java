import java.io.IOException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;
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
	
	static long tweetNum = 1;
	
	static StatusListener listener = new StatusListener(){
	    public void onStatus(Status status)
	    {
	    	//TODO CHANGE THIS
	    	//TODO Implement Language Detection
	    	if (toBeAdded.size() < 1000)
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
			con = getConnection("/Users/Antonio/My Documents/Startup/AccelerandoDB/");
			Statement s = con.createStatement();
			s.execute("SET FILES LOB SCALE 1");
			s.close();
			createTweetTable();
			createSubjectTable();
			createUserTable();
			setFilesLogFalse = con.prepareStatement("SET FILES LOG FALSE");
			setFilesLogTrue = con.prepareStatement("SET FILES LOG TRUE");
			checkpoint = con.prepareStatement("CHECKPOINT");
			checkpointDefrag = con.prepareStatement("CHECKPOINT DEFRAG");
			setFilesLogFalse.execute();
			checkpoint.execute();
		} 
		catch (SQLException e1) {
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
		
		
		
	    twitterStream.addListener(listener);
	    twitterStream.sample();
		
		
		Thread Client = new Thread(new Client());
		Thread tweetTableAdder = new Thread(new Adder());
		
		Client.start();
		tweetTableAdder.setPriority(Thread.MAX_PRIORITY);
		tweetTableAdder.start();
		
		
		long nextSave = System.currentTimeMillis() + 600000L;
		
		for(;;)
		{
			if (System.currentTimeMillis() > nextSave)
			{
				nextSave = System.currentTimeMillis() + 600000L;
				try {
					setFilesLogTrue.execute();
					checkpointDefrag.execute();
					setFilesLogFalse.execute();
					checkpoint.execute();
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
		//TODO Increase blob size limit
	    String createString =
		        "CREATE CACHED TABLE SUBJECTS" +
		        "(SUBJECT varchar(20) NOT NULL, " +
		        "RBBST BLOB(4G), " +
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
	
	private static class Adder implements Runnable
	{
		static PreparedStatement statusDBInsert = null, userDBInsert = null, subjectDBInsert = null,
								subjectBDQuery = null, userDBQuery = null, userDBUpdate = null,
								subjectDBUpdate = null;
		static int threadsActive;
		public void run() 
		{
			try {
				statusDBInsert = con.prepareStatement("insert into TWEET_TABLE " +
				        "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
				userDBInsert = con.prepareStatement("insert into USERS " + 
				        "values(?, ?, ?, ?, ?, ?, ?, ?)");
				subjectDBInsert = con.prepareStatement("insert into SUBJECTS " + 
				        "values(?, ?)");
				subjectBDQuery = con.prepareStatement("select * from SUBJECTS where SUBJECT = ? for update");
				subjectDBUpdate = con.prepareStatement("update SUBJECTS set RBBST = ? WHERE SUBJECT = ?");
				userDBQuery = con.prepareStatement("select * from USERS where ID = ?");
				userDBUpdate = con.prepareStatement("update USERS set FOLLOWERS = ?, FRIENDS = ?, " +
						"LISTED_COUNT = ?, NUM_TWEETS = ?, AVERAGE_POLARIZATION = ? WHERE ID = ?");
			} catch (SQLException e1) {
				System.err.println(e1);
				System.err.println("SQLException: Failed to Create Prepared Statement");
			}
			addStatus();
		}
			static void addStatus()
			{
				Status current;
				User currentUser;
				double polarization;
				String text;
				if (!toBeAdded.isEmpty())
				{
					//TODO implement thread number
					current = toBeAdded.dequeue();
					currentUser = current.getUser();
					polarization = tweetEvaluator.calculatePolarization(current);
					//TODO Remove usernames after @
					//TODO Create Tag Ignore List
					// Add To Subject Database
					text = current.getText();
					text = text.replaceAll("http:\\/\\/t.co\\/........", " ");
					text = text.toLowerCase();
					String[] tags = text.split("(\\W)+");
						
					            
					outerloop: for (int i = 0; i < tags.length; i++)
					{
						for (int j = 0; j < i; j++)
							if (tags[j].equals(tags[i]))
								continue outerloop;
						if (tags[i] == null || tags[i].length() == 0)
						{
							continue;
						}
						new Thread(new AddToSubjectTable(tags[i], current)).start();
					}
					new Thread(new AddToTweetTable(current, polarization)).start();
					new Thread(new AddToUserTable(currentUser, polarization)).start();
					System.out.println("TWEET NO " + (tweetNum++));
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
					
						statusDBInsert.executeUpdate();
					}
					catch (SQLException e1)
					{
						return;
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
					catch (SQLException e1)
					{
						return;
					}
				}
			}	
				
			static class AddToSubjectTable implements Runnable
			{
				String tag;
				ResultSet subjectResults;
				SubjectBlobArray arr;
				Blob blob;
				Status current;
				public AddToSubjectTable(String t, Status s)
				{
					current = s;
					tag = t;
				}
				//TODO synchronization
				public void run()
				{
					try
					{
					subjectBDQuery.setString(1, tag);
					
					subjectResults = subjectBDQuery.executeQuery();
				
				//System.out.println(tags[i]);	
				//System.out.flush();
				if(subjectResults.next())
			    {//System.out.println("EXISTS");	
			    	blob = subjectResults.getBlob(2);
			    	arr = new SubjectBlobArray(blob);
			    	arr.add(current.getCreatedAt().getTime(), current.getId());
			    	subjectDBUpdate.setBlob(1, blob);
			    	subjectDBUpdate.setString(2, tag);
			    	
			    		subjectDBUpdate.executeUpdate();
			    }
			    else
			    {
			    	subjectDBInsert.setString(1, tag);
			    	subjectDBInsert.setBlob(2, SubjectBlobArray.createSubjectBlob());
			    	subjectDBInsert.executeUpdate();
			    	subjectResults = subjectBDQuery.executeQuery();
			    	subjectResults.next();
			    	blob = subjectResults.getBlob(2);
			    	arr = new SubjectBlobArray(blob);
			    	arr.add(current.getCreatedAt().getTime(), current.getId());
			    	subjectDBUpdate.setBlob(1, blob);
			    	subjectDBUpdate.setString(2, tag);
			    	
			    		subjectDBUpdate.executeUpdate();
			    }
					}
					catch (SQLException e1)
					{
						return;
					}
					catch (IOException e2)
					{
						return;
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
			PreparedStatement querySubjects = null, queryTweetPolarization = null;
			try
			{
				querySubjects = con.prepareStatement("select * from SUBJECTS where SUBJECT = ?");
				queryTweetPolarization = con.prepareStatement("select polarization from tweet_table where id = ?");
			}
			catch (SQLException e1)
			{
				e1.printStackTrace();
				System.exit(1);
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
							Statement s = con.createStatement();
							s.execute("SET FILES LOG TRUE");
							s.execute("CHECKPOINT DEFRAG");
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
