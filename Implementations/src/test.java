import java.util.*;
import java.io.*;
public class test { 
	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException
	{
		TweetHashTable.save("superTweets.data", new TweetHashTable());
	}
}
