import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import java.util.regex.Pattern;


public class TwitterCrawler {
	public static void main(String[] args) throws SQLException, MalformedURLException
	{
		String charsetName = "ISO-8859-1";
		Locale usLocale = new Locale("en", "US");
		System.setProperty("sun.net.client.defaultConnectTimeout", "1500");
        System.setProperty("sun.net.client.defaultReadTimeout",    "4000");
		Connection con = getConnection();
		Statement s = con.createStatement();
		ResultSet r = s.executeQuery("SELECT screen_name from users where num_tweets > 1");
		Pattern p = Pattern.compile("(\\w)+@(\\w)+\\.com");
		URL url;
		Scanner scanner = null;
		int total = 0, numFound = 0;
		while (r.next())
		{
			url = new URL("https://twitter.com/" + r.getString(1));
			 try {System.out.println("HERE");
		            URLConnection site = url.openConnection();
		            InputStream is     = site.getInputStream();
		            scanner            = new Scanner(new BufferedInputStream(is), charsetName);
		            scanner.useLocale(usLocale);
		            String found = scanner.findWithinHorizon(p, 0);
		            total++;
		            if (found != null)
		            {
		            	System.out.println(found);
		            	numFound++;
		            	System.out.println(numFound + " / " + total);
		            }
		        }
		        catch (IOException ioe) {
		            System.err.println("Could not open " + url);
		            ioe.printStackTrace();
		        }
		}
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
}
