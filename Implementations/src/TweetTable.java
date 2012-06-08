import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.File;
import java.util.Date;
public class TweetTable implements java.io.Serializable
{
	private static final long serialVersionUID = 1L;
	private int size;
	private TST<RBBST<Date, SuperTweet>> tweetTable;
	public TweetTable()
	{
		tweetTable = new TST<RBBST<Date, SuperTweet>>();
	}
	
	// Add a SuperTweet to the table
	public void add(SuperTweet t)
	{
		String subject = t.getSubject();
		if (subject == null | subject.length() == 0)
		{
			System.err.println("Subjectless Tweet: Was not Added to TweetTable");
			return;
		}
		if (tweetTable.contains(subject))
			tweetTable.get(subject).put(t.getCreatedAt(), t);
		else
		{
			tweetTable.put(t.getSubject(), new RBBST<Date, SuperTweet>());
			tweetTable.get(subject).put(t.getCreatedAt(), t);
		}
	}
	
	// Return all SuperTweets with a given subject between a start and end Date as an Iterable
	public Iterable<SuperTweet> getTweets(String subject, Date startDate, Date endDate)
	{
		Queue<SuperTweet> q = new Queue<SuperTweet>();
		RBBST<Date, SuperTweet> tree = tweetTable.get(subject);
		for (Date d : tree.keys(startDate, endDate))
		{
			q.enqueue(tree.get(d));
		}
		return q;
	}
	
	// Load a TweetHashTable from a given filename
	public static TweetTable load(String filename)
	{
		TweetTable tweetTable = null;
		FileInputStream superTweetInputStream = null;
        ObjectInputStream superTweetReader = null;
        try {
        	superTweetInputStream = new FileInputStream(filename);
		} catch (FileNotFoundException e1) {
			System.err.println(filename + " was not found.\n Initializing to new TweetTable");
			return new TweetTable();
		}
        try {
			superTweetReader = new ObjectInputStream(superTweetInputStream);
		} catch (IOException e2) {
			System.err.println("Unable to Initialize tweet reader");
			return null;
		}
        try {
			tweetTable = (TweetTable)superTweetReader.readObject();
		} catch (IOException e2) {
			System.err.println("Unable to Read " + filename);
			return null;
		} catch (ClassNotFoundException e2) {
			System.err.println("Unable to Read " + filename);
			return null;
		}
        return tweetTable;
	}
	
	// Save this TweetHashTable to a given filename
	public void save(String filename)
	{
		FileOutputStream superTweetStream = null;
        ObjectOutputStream superTweetWriter = null;
        File superTweetFile = new File(filename);
        superTweetFile.delete();
        try {
			superTweetFile.createNewFile();
		} catch (IOException e1) {
			System.err.println("Unable to Create superTweets.twt");
			return;
		}
        try {
        	superTweetStream = new FileOutputStream(superTweetFile);
		} catch (FileNotFoundException e1) {
			System.err.println("File not found");
			return;
		}
        try {
        	superTweetWriter = new ObjectOutputStream(superTweetStream);
		} catch (IOException e1) {
			System.err.println("Unable to Open Object Output Stream");
			return;
		}
        try {
			superTweetWriter.writeObject(this);
		} catch (IOException e1) {
			System.err.println("Unable to Write to Table to " + filename);
		}
        try {
			superTweetWriter.flush();
		} catch (IOException e) {
			System.err.println("Unable to Flush Output to " + filename);
		}
        try {
			superTweetWriter.close();
		} catch (IOException e) {
			System.err.println("Unable to Close Object Output Stream");
		}
	}
}
