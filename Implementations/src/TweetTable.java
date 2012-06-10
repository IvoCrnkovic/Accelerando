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
	private static final long serialVersionUID = 5446506112220258045L;
	
	private TST<RBBST<Date, SuperTweet>> tweetTable;
	private Queue<SuperTweet> toBeUpdated;
	private int size;
	public TweetTable()
	{
		tweetTable = new TST<RBBST<Date, SuperTweet>>();
		toBeUpdated = new Queue<SuperTweet>();
		size = 0;
	}
	
	public int getSize()
	{
		return size;
	}
	public Queue<SuperTweet> getToBeUpdated()
	{
		return toBeUpdated;
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
	public void add(SuperTweet t, String subject)
	{
		if (subject == null | subject.length() == 0)
		{
			return;
		}
		if (tweetTable.contains(subject))
			tweetTable.get(subject).put(t.getTweet().getCreatedAt(), t);
		else
		{
			tweetTable.put(subject, new RBBST<Date, SuperTweet>());
			tweetTable.get(subject).put(t.getTweet().getCreatedAt(), t);
		}
		size++;
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
}
