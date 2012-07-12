/**
 * Test Client: For Testing Purposes Only
 */

import java.sql.*;
import java.util.*;
import java.io.*;

import twitter4j.*;
public class Test {
	static Connection con;
	public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException, SQLException
	{
		//CollectionMethods.save(new StatusTable(), "statusTable.stb");
		con = getConnection("/Users/Antonio/My Documents/Startup/AccelerandoDB/");
		Statement s = con.createStatement();
		PreparedStatement p = con.prepareStatement("COMMIT");
		//s.execute("SHUTDOWN SCRIPT");
		//createTweetTable();
		//createSubjectTable();
		//createUserTable();
		p.execute();
	}
	private static void addIgnoredWords() throws FileNotFoundException
	{
		TST<PolarityValue> newTST = CollectionMethods.<TST<PolarityValue>>load("polarities.tst");
		Scanner in = new Scanner(new File("ignore.txt"));
		String s;
		while (in.hasNext())
		{
			s = in.next();
			in.nextLine();
			if (newTST.contains(s))
			{
				System.out.println("Deleting: " + s);
				newTST.put(s, null);
				if (!newTST.contains(s))
					System.out.println("SUCCESSFUL");
			}
		}
		CollectionMethods.save(newTST, "polarities.tst");
	}
	private static void addExistingToDictionary()
	{
		TST<PolarityValue> origin = CollectionMethods.<TST<PolarityValue>>load("words.tst");
		TST<PolarityValue> newTST = CollectionMethods.<TST<PolarityValue>>load("polarities.tst");
		for (String s : origin.keys())
			if (newTST.contains(s))
				newTST.put(s, origin.get(s));
		CollectionMethods.save(newTST, "polarities.tst");
	}
	private static void createDictionary() throws FileNotFoundException
	{
		Scanner slang = new Scanner(new File("SlangDict.txt"));
		Scanner dict = new Scanner(new File("Dictionary.txt"));
		ArrayList<String> words = new ArrayList<String>();
		while (slang.hasNext())
		{
			words.add(slang.next());
			slang.nextLine();
		}
		while (dict.hasNext())
		{
			words.add(dict.next("\\w+"));
		}
		Collections.shuffle(words);
		TST<PolarityValue> newTST = new TST<PolarityValue>();
		for (String s : words)
			newTST.put(s, new PolarityValue(0., 0));
		CollectionMethods.save(newTST, "polarities.tst");
	}
	private static void showScores() throws FileNotFoundException
	{
		TST<PolarityValue> words = CollectionMethods.<TST<PolarityValue>>load("polarities.tst");
		ArrayList<Holder> polarities = new ArrayList<Holder>();
		PrintStream stream = new PrintStream("results.txt");
		for (String s : words.keys())
		{
			polarities.add(new Holder(words.get(s), s));
		}
		Collections.sort(polarities);
		for (Holder h : polarities)
		{
			if (h.v.getOccurrences() == 0)
				stream.println(h.s + "\t\t\tSCORE: " + h.v.getScore() + "\t\tOCCURRENCES: " + h.v.getOccurrences());
			else
				stream.println(h.s + "\t\t\tSCORE: " + h.v.getScore() + "\t\tOCCURRENCES: " + (h.v.getOccurrences() - 5));
		}
	}
	private static class Holder implements Comparable
	{
		PolarityValue v;
		String s;
		public Holder(PolarityValue val, String st)
		{
			v = val;
			s = st;
		}
		@Override
		public int compareTo(Object arg0) {
			if (this.v.getScore() > ((Holder) arg0).v.getScore())
				return 1;
			if (this.v.getScore() < ((Holder) arg0).v.getScore())
				return -1;
			return 0;
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
	
}