/**
 * Test Client: For Testing Purposes Only
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.io.*;
import twitter4j.*;
public class Test {
	static Connection con;
	public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException, SQLException
	{
		CollectionMethods.save(new StatusTable(), "statusTable.stb");
	}
	public static void createTweetTable() throws SQLException {
	    String createString =
	        "CREATE CACHED TABLE " + "accelerandoDB" + ".TWEETS " +
	        "(ID bigint NOT NULL, " +
	        "TEXT varchar(200) NOT NULL, " +
	        "RETWEET_COUNT smallint NOT NULL, " +
	        "IS_RETWEET boolean NOT NULL, " +
	        "IS_FAVORITED boolean NOT NULL, " +
	        "USER_ID bigint NOT NULL, " +
	        "COUNTRY_CODE varchar(10), " +
	        "LATTITUDE float, " +
	        "LONGITUDE float, " +
	        "PRIMARY KEY (ID), " + 
	        "FOREIGN KEY (USER_ID) REFERENCES accelerandoDatabase.USERS (ID))";

	    Statement stmt = null;
	    try {
	        stmt = con.createStatement();
	        stmt.executeUpdate(createString);
	        stmt.executeUpdate("SHUTDOWN");
	    } catch (SQLException e) {
	        System.err.println(e);
	    } finally {
	        if (stmt != null) { stmt.close(); }
	    }
	}
	public static void createUserTable() throws SQLException
	{
	    String createString =
		        "CREATE CACHED TABLE " + "accelerandoDB" + ".USERS " +
		        "(ID bigint NOT NULL, " +
		        "FOLLOWERS integer NOT NULL, " +
		        "FRIENDS integer NOT NULL, " +
		        "LISTED_COUNT integer NOT NULL, " +
		        "NAME varchar(40), " +
		        "SCREEN_NAME varchar(30) NOT NULL, " +
		        "PRIMARY KEY (ID))";

		    Statement stmt = null;
		    try {
		        stmt = con.createStatement();
		        stmt.executeUpdate(createString);
		        stmt.executeUpdate("SHUTDOWN");
		    } catch (SQLException e) {
		        System.err.println(e);
		    } finally {
		        if (stmt != null) { stmt.close(); }
		    }
	}
	public static Connection getConnection(String location) throws SQLException {

	    Connection conn = null;
	    Properties connectionProps = new Properties();
	    connectionProps.put("user", "ajuliano");
	    connectionProps.put("password", "accelerando");
	    conn = DriverManager.getConnection("jdbc:hsqldb:file:" + location + "accelerandoDB;shutdown=true", connectionProps);
	    System.out.println("Connected to database");
	    return conn;
	}
}