import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
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

	private final TST<RBBST<Date, Long>> tweetTable;
	private final RBBST<Long, SuperTweet> tweetHolder;
	private int size;
	public TweetTable()
	{
		tweetTable = new TST<RBBST<Date, Long>>();
		tweetHolder = new RBBST<Long, SuperTweet>();
		size = 0;
	}
	public int getSize()
	{
		return size;
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
		long id = t.getTweet().getId();
		if (subject == null | subject.length() == 0)
		{
			return;
		}
		if (tweetTable.contains(subject))
			tweetTable.get(subject).put(t.getTweet().getCreatedAt(), id);
		else
		{
			tweetTable.put(subject, new RBBST<Date, Long>());
			tweetTable.get(subject).put(t.getTweet().getCreatedAt(), id);
		}
		if (!tweetHolder.contains(id))
		{
			tweetHolder.put(id, t);
			size++;
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
	public Queue<SuperTweet> getTweets(String subject, Date startDate, Date endDate)
	{
		Queue<SuperTweet> q = new Queue<SuperTweet>();
		RBBST<Date, Long> tree = tweetTable.get(subject);
		try
		{
			for (Date d : tree.keys(startDate, endDate))
			{
				q.enqueue(tweetHolder.get(tree.get(d)));
			}
			return q;
		}
		catch(NullPointerException e)
		{
			return q;
		}
	}
	public Queue<SuperTweet> getTweets(String[] subjects, Date startDate, Date endDate)
	{
		SuperTweet[] targetTweets;
		Queue<SuperTweet> currentTweets;
		SuperTweet[] currentArray;
		int j = 0;
		if (subjects.length == 0)
			return new Queue<SuperTweet>();
		Arrays.sort(subjects);
		currentTweets = getTweets(subjects[0], startDate, endDate);
		targetTweets = new SuperTweet[currentTweets.size()];
		for (SuperTweet s : currentTweets)
		{
			targetTweets[j] = s;
			j++;
		}
		//TODO finish
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
	
	public TST<RBBST<Date, SuperTweet>> getTST()
	{
		return tweetTable;
	}
}
