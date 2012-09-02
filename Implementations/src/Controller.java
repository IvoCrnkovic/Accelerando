import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

//TODO Look into gin fuzzy upper search limit
public class Controller
{
	final static String statusTableFile	= "StatusTable.stb",
					 statusTableBackup	= "StatusTableBackup.stb",
					 wordPolaritiesFile = "words.tst";
	final static Queue<Status> toBeAdded = new Queue<Status>();
	final static int MAX_TAGS = 20;
	static TweetEvaluator tweetEvaluator;
	static ArrayList<Thread> streamThreads = new ArrayList<Thread>();
	static TwitterStream twitterStream, queryStream;
	static Connection con;
	static PreparedStatement checkpoint;
	static long tweetNum = 1;
	static TST<Boolean> ignoredWords;
	
	static StatusListener listener = new StatusListener(){
	    public void onStatus(Status status)
	    {
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
		System.out.print("Loading... ");
		try {
			con = getConnection();
			/*
			createTweetTable();
			createUserTable();
			createFunctionTable();
			Statement s = con.createStatement();
			
			createTweetTablePartition(new Date(System.currentTimeMillis()));
			s.execute("CREATE TRIGGER insert_tweet_trigger BEFORE INSERT ON tweet_table FOR EACH ROW EXECUTE PROCEDURE tweet_table_insert_trigger()");
			//TODO Change to start with days
			s.execute("SET constraint_exclusion = partition");
			s.close();*/
			//setFilesLogFalse = con.prepareStatement("SET FILES LOG FALSE");
			//setFilesLogTrue = con.prepareStatement("SET FILES LOG TRUE");
			checkpoint = con.prepareStatement("CHECKPOINT");
			//checkpointDefrag = con.prepareStatement("CHECKPOINT DEFRAG");
			//setFilesLogFalse.execute();
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
		queryStream = CollectionMethods.authenticateSecondTwitterStream();
		twitterStream = CollectionMethods.authenticateStream();
		System.out.println("Done");
		
		//Initialize Language Detection
		try {
			DetectorFactory.loadProfile("lib/profiles");
		} catch (LangDetectException e2) {
			System.err.println("Failed to load language profile");
			System.exit(0);
		}
		
		
		queryStream.addListener(listener);
	    twitterStream.addListener(listener);
	    //twitterStream.sample();
		
	   
		
		Thread Client = new Thread(new Client());
		Thread tweetTableAdder = new Thread(new Adder());
		
		Client.start();
		tweetTableAdder.setPriority(Thread.MAX_PRIORITY);
		tweetTableAdder.start();
		//new Thread(new Adder()).start();
		//new Thread(new Adder()).start();
		
		
		long nextSave = System.currentTimeMillis() + 1000000L;
		
		for(;;)
		{
			if (System.currentTimeMillis() > nextSave)
			{
				try
				{System.out.print("Autosaving... ");
					checkpoint.execute();
					System.out.println("Done.");
				} catch (SQLException e) {
					System.err.println("SQLException: Automatic Commit Failed");
				}
				nextSave = System.currentTimeMillis() + 1000000L;
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
	
	private static void createTweetTable() throws SQLException {
	    String createString =
	        "CREATE TABLE TWEET_TABLE " +
	        "(ID bigint NOT NULL, " +
	        "TEXT varchar(400) NOT NULL, " +
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
	        "TAGS varchar[], " +
	        "USER_TAGS varchar[], " +
	        "LINKS varchar[], " +
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
	
	private static void createUserTable() throws SQLException
	{
		//TODO Put Influence in
		 String createString =
			        "CREATE TABLE USERS " +
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
	private static void createFunctionTable() throws SQLException
	{
		 String createString =
			        "CREATE TABLE FUNCTIONS " +
			        "(name varchar(200) NOT NULL, " +
			        "text varchar NOT NULL, " +
			        "PRIMARY KEY (name))";

		    Statement stmt = null;
		    try {
		        stmt = con.createStatement();
		        stmt.executeUpdate(createString);
		    }
		    finally {
		        if (stmt != null) { stmt.close(); }
		    }
	}
	
	private static void createTweetTablePartition(java.util.Date date) throws SQLException
	{
		Statement createPartition = con.createStatement();
		String suffix = dateToPartitionSuffix(date);
		createPartition.execute("CREATE TABLE tweet_table_" + suffix +
				" (CHECK (date >= " + date.getTime() + " AND date < " + (date.getTime() + 86400000L) +
				" )) INHERITS (tweet_table)");
		createPartition.execute("CREATE INDEX date_index_" + suffix + " ON tweet_table_" + suffix + " (date)");
		createPartition.execute("CREATE INDEX user_index_" + suffix + " ON tweet_table_" + suffix + " (user_id)");
		createPartition.execute("CREATE INDEX tag_index_" + suffix + " ON tweet_table_" + suffix + " USING GIN (tags)");
		createPartition.execute("CREATE INDEX user_tag_index_" + suffix + " ON tweet_table_" + suffix + " USING GIN (user_tags)");
		createPartition.close();
		updateTweetTableInsertScript(date);
	}
	
	private static String dateToPartitionSuffix(java.util.Date date)
	{
		Calendar cal = Calendar.getInstance();
	    cal.setTime(date);
	    int year = cal.get(Calendar.YEAR);
	    int month = cal.get(Calendar.MONTH);
	    int day = cal.get(Calendar.DAY_OF_MONTH);
		return year + "_" + month + "_" + day;
	}
	
	private static void updateTweetTableInsertScript(java.util.Date date) throws SQLException
	{
		Statement getFunction = con.createStatement();
		String text;
		ResultSet r = getFunction.executeQuery("SELECT text from functions where name = 'tweetTableInsert'");
		if (r.next())
		{
			text = r.getString(1);
			String[] parts = text.split("IF", 2);
			text = parts[0] + "IF (NEW.date >= " + date.getTime() + " AND NEW.date " +
					"< " + (date.getTime() + 86400000L) + " ) THEN INSERT INTO tweet_table_" + dateToPartitionSuffix(date) +
					" VALUES (NEW.*); ELSEIF" + parts[1];
			getFunction.executeUpdate("UPDATE functions SET text = '" + text.replaceAll("'", "''") + "' WHERE name = 'tweetTableInsert'");
		}
		else
		{
			text = "CREATE OR REPLACE FUNCTION tweet_table_insert_trigger() " +
					"RETURNS TRIGGER AS $$ BEGIN IF (NEW.date >= " + date.getTime() + " AND NEW.date " +
					"< " + (date.getTime() + 86400000L) + " ) THEN INSERT INTO tweet_table_" + dateToPartitionSuffix(date) +
					" VALUES (NEW.*); ELSE RAISE EXCEPTION ''Date Out Of Range''; END IF; RETURN NULL; END; $$ LANGUAGE plpgsql;";
			getFunction.executeUpdate("INSERT INTO functions values('tweetTableInsert', '" + text + "')");
		}
		getFunction.execute(text.replaceAll("''", "'"));
		getFunction.close();
	}
	
	private static Connection getConnection() throws SQLException {

		Connection conn = null;
	    Properties connectionProps = new Properties();
	    connectionProps.put("user", "postgres");
	    connectionProps.put("password", "princeton2015");
	    conn = DriverManager.getConnection("jdbc:postgresql://localhost/TwitterDB", connectionProps);
	    System.out.println("Connected to database");
	    return conn;
	}
	
	private static class Adder implements Runnable
	{
		static PreparedStatement userDBInsert = null, statusDBInsert = null,
								userDBQuery = null, userDBUpdate = null;
		static int threadsActive;
		//static int batchSize = 0;
		public void run() 
		{
			try {
				statusDBInsert = con.prepareStatement("insert into TWEET_TABLE " +
				        "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
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
			static void addStatus()
			{
				Status current;
				User currentUser;
				double polarization;
				/*if (batchSize > 25000)
				{
					System.out.print("Batch Updating... ");
					try {
						statusDBInsert.executeBatch();
					} catch (Exception e) {
						System.err.println("Batch Update Failed");
						e.printStackTrace();
					}
					batchSize = 0;
					try {
						checkpoint.execute();
					} catch (SQLException e) {
						System.err.println("Checkpoint Failed");
						e.printStackTrace();
					}
					System.out.println("Done");
				}*/
				
				if (!toBeAdded.isEmpty())
				{
					try
					{
						current = toBeAdded.dequeue();
						currentUser = current.getUser();
						polarization = tweetEvaluator.calculatePolarization(current);
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
				ArrayList<String> userTags = new ArrayList<String>();
				ArrayList<String> finalTags = new ArrayList<String>();
				ArrayList<String> links = new ArrayList<String>();
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
						Pattern linkPattern = Pattern.compile("http:\\/\\/t.co\\/(\\w)+");
						Matcher linkMatcher = linkPattern.matcher(text);
						while (linkMatcher.find())
							links.add(linkMatcher.group(1));
						
						text = text.replaceAll("http:\\/\\/t.co\\/(\\w)+", "");
						
						Pattern userTagPattern = Pattern.compile("@(\\w)+");
						Matcher userTagMatcher = userTagPattern.matcher(text);
						while (userTagMatcher.find())
						{
							userTags.add(userTagMatcher.group(1).substring(1));
							
						}
						
						text = text.replaceAll("@(\\w)+", "");
						
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
							finalTags.add(tags[i]);
						}
						statusDBInsert.setArray(13, con.createArrayOf("varchar", finalTags.toArray(new String[finalTags.size()])));
						statusDBInsert.setArray(14, con.createArrayOf("varchar", userTags.toArray(new String[userTags.size()])));
						statusDBInsert.setArray(15, con.createArrayOf("varchar", links.toArray(new String[links.size()])));
						
						/*synchronized (Adder.class)
						{
							batchSize++;
						}*/
						//statusDBInsert.addBatch();
						statusDBInsert.executeUpdate();
					}
					catch (Exception e1)
					{
						if (e1.getMessage().equals("ERROR: Date Out Of Range"))
						{
							System.out.println("Date Out of range. Creating new partition");
							try {
								createTweetTablePartition(current.getCreatedAt());
								run();
								return;
							} catch (SQLException e) {
								System.err.println("Failed To Create Partition");
								e.printStackTrace();
							}
						}
						else
						{
							System.err.println("Exception: Failed to add to Tweet Table");
							e1.printStackTrace();
						}
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
	//TODO this
	/*
		static class UserTagged implements Runnable
		{
			public UserTagged (String )
			public void run() {
				
				
			}
			
		}*/
	private static class Client implements Runnable
	{
		public void run() {
			String input = "";
			Thread thread;
			ArrayList<String> track = new ArrayList<String>();
			ArrayList<Double> locations = new ArrayList<Double>();
			PreparedStatement querySubjects = null; 
			PreparedStatement queryTweetPolarization = null;
			for (int i = 1; i <= MAX_TAGS; i++)
				try {
					queryTweetPolarization = con.prepareStatement("select polarization from tweet_table where tags @> ?");
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
				if (input.equals("open subject stream"))
				{
					System.out.print("Subject: ");
					input = in.nextLine().toLowerCase();
					System.out.println();
					String[] tags = input.split(" ");
					for (int i = 0; i < tags.length; i++)
					{
						if (track.contains(tags[i]) || tags[i] == null || tags[i].equals(""))
							continue;
						track.add(tags[i]);
					}
					FilterQuery query = new FilterQuery();
					tags = new String[track.size()];
					int i = 0;
					for (String s : track)
					{
						tags[i++] = s;
					}
					query.track(tags);
					queryStream.filter(query);
					continue;
				}
				if (input.equals("open location stream"))
				{
					System.out.print("Location: ");
					System.out.println();
					input = in.nextLine();
					String[] points = input.split(" ");
					double[] doublePoints = new double[4];
					try
					{
						for (int i = 0; i < 4; i++)
							doublePoints[i] = Double.parseDouble(points[i]);
					}
					catch (Exception e)
					{
						System.err.println("Invalid Input");
						continue;
					}
					for (int i = 0; i < 4; i++)
						locations.add(doublePoints[i]);
						
					FilterQuery query = new FilterQuery();
					String[] tags = new String[track.size()];
					double[][] places = new double[locations.size()][2];
					int i = 0;
					for (String s : track)
					{
						tags[i++] = s;
					}
					for (int j = 0; j < locations.size(); j += 4)
					{
						places[j / 2][0] = locations.get(j);
						places[j / 2][1] = locations.get(j + 1);
						places[(j / 2) + 1][0] = locations.get(j + 2);
						places[(j / 2) + 1][1] = locations.get(j + 3);
					}
					System.out.println(places[0][0]);
					System.out.println(places[0][1]);
					System.out.println(places[1][0]);
					System.out.println(places[1][1]);
					query.track(tags).locations(places);
					queryStream.filter(query);
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
					//TODO write
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
							s.execute("checkpoint");
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
				
				
				//N lg N performance
				if (input.equals("p"))
				{
					String subject;
					ArrayList<String> tags = new ArrayList<String>();
					double polarization = 0;
					System.out.print("Subject: ");
					subject = in.nextLine().toLowerCase();
					System.out.println();
					if (subject == null || subject.equals(""))
					{
						System.err.println("Illegal Key");
						continue;
					}
					String[] originalTags = subject.split("\\W");
					
					for (int i = 0; i < originalTags.length; i++)
						if (originalTags[i] != null && !originalTags[i].equals(""))
							tags.add(originalTags[i]);
					int size = 0;
					try 
					{
						queryTweetPolarization.setArray(1, con.createArrayOf("varchar", tags.toArray(new String[tags.size()])));
						ResultSet results = queryTweetPolarization.executeQuery();
						
						while (results.next())
						{
							polarization += results.getDouble(1);
							size++;
						}
					}
					catch (SQLException e)
					{
						e.printStackTrace();
					}
					
					if (size != 0)
						polarization /= size;
					System.out.println("Subject Size = " + size);
					System.out.println("Polarization = " + polarization);
					continue;
				}
			}
			while (true);
			
		}
		private class intString
		{
			String s;
			int d;
			public intString(String s, int d)
			{
				this.s = s;
				this.d = d;
			}
		}
	}
}
