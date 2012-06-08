import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.File;
import java.util.Date;
/**
 * Class for storing SuperTweets
 */
public class TweetTable implements java.io.Serializable

{
	/**
	 *Serial ID number that eclipse told us to use for serialization.  Don't really know what it does.
	 *@see Java.io.Serializable
	 */
	private static final long serialVersionUID = 1L;
	private int size;
	private TST<RBBST<Date, SuperTweet>> tweetTable;
	public TweetTable()
	{
		tweetTable = new TST<RBBST<Date, SuperTweet>>();
	}
	
	
	
	/**
	 * Method to add a supertweet to the table.
	 * 
	 * <p>
	 * Adds a supertweet to the table, indexing by the hash code of the subject and the time the tweet was created.
	 * 
	 * 
	 * @param t The tweet to be added.
	 */
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
	

	/**
	 * Method for creating iterators to go through the hash table.
	 * 
	 * <p>
	 * Creates an iterable that goes through all the tweets of a given subject from some start date to some end date.
	 * 
	 * @param subject The subject of the tweets desired.
	 * @param startDate The earliest time to be looked at.
	 * @param endDate The latest time to be looked at.
	 * @return The iterable.
	 */
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
	
	/**
	 * Returns the RBBST associated with a given subject
	 * @param subject The subject of the tweets
	 * @return the RBBST associated with subject
	 */
	public RBBST<Date, SuperTweet> getRBBST(String subject) 
	{
		return tweetTable.get(subject);
	}
	
	
	/**
	 * Method to load a TweetTable from a file.
	 * 
	 * <p>
	 * Uses object serialization to read TweetTables from files.
	 * 
	 * 
	 * @param filename The name of the file to read from.
	 * @return The hash table from the file.
	 */
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
			System.err.println("IOException: Unable to Read " + filename);
			return null;
		} catch (ClassNotFoundException e2) {
			System.err.println("ClassNotFoundException: Unable to Read " + filename);
			return null;
		}
        return tweetTable;
	}
	
	/**
	 * Method to save a TweetTable to disk in a file.
	 * 
	 * <p>
	 * Uses object serialization the write the TweetTable to a file.
	 * 
	 * 
	 * @param filename The name of the file to write to or create if it does not already exist.
	 */
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
