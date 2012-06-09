/**
 * Test Client: For Testing Purposes Only
 */

import java.util.*;
import java.io.*;
public class test { 
	public static void main(String[] args) throws FileNotFoundException, IOException
	{
		ObjectLoader.save(new TweetTable(), "superTweets.data");
	}
}
