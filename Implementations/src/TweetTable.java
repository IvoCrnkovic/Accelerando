import java.util.Arrays;
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

	private final TST<RBBST<Date, Long>> tweetTable;
	private final RBBST<Long, SuperTweet> tweetHolder;
	private int size;
	public TweetTable()
	{
		tweetTable = new TST<RBBST<Date, Long>>();
		tweetHolder = new RBBST<Long, SuperTweet>();
		size = 0;
	}
	public int size()
	{
		return size;
	}
	
	public int subjectSize(String subject)
	{
		if (tweetTable.contains(subject))
			return tweetTable.get(subject).size();
		return 0;
	}
	public int subjectSize(String subject, Date startDate, Date endDate)
	{
		if (tweetTable.contains(subject))
			return tweetTable.get(subject).size(startDate, endDate);
		return 0;
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
	public Queue<String> getSubjects()
	{
		return (Queue<String>) tweetTable.keys();
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
		Queue<SuperTweet> currentTweets, targetTweets = new Queue<SuperTweet>();
		String[] currentTags;
		SuperTweet currentTweet;
		int foundIndex, size;
		boolean found;
		if (subjects.length == 0)
			return targetTweets;
		Arrays.sort(subjects);
		currentTweets = getTweets(subjects[0], startDate, endDate);
		size = currentTweets.size();
		for (int i = 0; i < size; i++)
		{
			foundIndex = 0;
			found = true;
			currentTweet = currentTweets.dequeue();
			currentTags = currentTweet.getTags();
			Arrays.sort(currentTags);
			for (int k = 1; k < subjects.length; k++)
			{
				foundIndex = Arrays.binarySearch(currentTags, foundIndex, subjects.length - 1, subjects[k]);
				if (foundIndex < 0)
				{
					found = false;
					break;
				}
			}
			if (found)
				targetTweets.enqueue(currentTweet);
		}
		return targetTweets;
	}
	/**
	 * Returns All Tweets in TweetTable. Potentially very long (N lg N) runtime.
	 * @return All Tweets
	 */
	public Queue<SuperTweet> getAllTweets()
	{
		Queue<SuperTweet> allTweets = new Queue<SuperTweet>();
		for (long t : tweetHolder.keys())
		{
			allTweets.enqueue(tweetHolder.get(t));
		}
		return allTweets;
	}
	/**
	 * Returns All Tweets in TweetTable. Potentially very long (S * lg N * lg M) runtime.
	 * @return All Tweets
	 */
	public Queue<SuperTweet> getAllTweets(Date startDate, Date endDate)
	{
		Queue<SuperTweet> allTweets = new Queue<SuperTweet>();
		RBBST<Date, Long> current;
		for (String s : tweetTable.keys())
		{
			current = tweetTable.get(s);
			for (Date d : current.keys(startDate, endDate))
			{
				allTweets.enqueue(tweetHolder.get(current.get(d)));
			}
		}
		return allTweets;
	}
}
