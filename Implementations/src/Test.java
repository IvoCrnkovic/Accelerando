/**
 * Test Client: For Testing Purposes Only
 */

import java.util.*;
import java.io.*;
import twitter4j.*;
public class Test { 
	public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException
	{
		ObjectLoader.save(new TweetTable(), "superTweets.data");
	}
}