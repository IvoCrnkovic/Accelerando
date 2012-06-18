import java.util.Arrays;
import java.util.Date;
/**
 * Class for storing SuperTweets
 */
//TODO Optimize memory
public class StatusTable implements java.io.Serializable
{
	private static final long serialVersionUID = -4330239673197534165L;
	
	private final TST<RBBST<Date, SuperStatus>> tweetTable;
	private int size;
	public StatusTable()
	{
		tweetTable = new TST<RBBST<Date, SuperStatus>>();
		size = 0;
	}
	public synchronized int size()
	{
		return size;
	}
	
	public synchronized int subjectSize(String subject)
	{
		if (tweetTable.contains(subject))
			return tweetTable.get(subject).size();
		return 0;
	}
	public synchronized int subjectSize(String subject, Date startDate, Date endDate)
	{
		if (tweetTable.contains(subject))
			return tweetTable.get(subject).size(startDate, endDate);
		return 0;
	}
	public synchronized int subjectSize(String[] subject, Date startDate, Date endDate)
	{
		return getTweets(subject, startDate, endDate).size();
	}
	public synchronized int subjectSize(String[] subject)
	{
		return getTweets(subject).size();
	}
	public synchronized void add(SuperStatus t)
	{
		String[] tags = t.getTags();
		if (tags == null || tags.length == 0)
			return;
		for (int i = 0; i < tags.length; i++)
		{
			if (tags[i] == null | tags[i].length() == 0)
			{
				return;
			}
			if (tweetTable.contains(tags[i]))
				tweetTable.get(tags[i]).put(t.getStatus().getCreatedAt(), t);
			else
			{
				tweetTable.put(tags[i], new RBBST<Date, SuperStatus>());
				tweetTable.get(tags[i]).put(t.getStatus().getCreatedAt(), t);
			}
		}
		size++;
	}
	public synchronized Queue<String> getSubjects()
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
	public synchronized Queue<SuperStatus> getTweets(String subject, Date startDate, Date endDate)
	{
		Queue<SuperStatus> q = new Queue<SuperStatus>();
		if (subject == null || subject.equals("") || startDate == null || endDate == null)
			return q;
		RBBST<Date, SuperStatus> tree = tweetTable.get(subject);
		try
		{
			for (Date d : tree.keys(startDate, endDate))
			{
				q.enqueue(tree.get(d));
			}
			return q;
		}
		catch(NullPointerException e)
		{
			return q;
		}
	}
	public synchronized Queue<SuperStatus> getTweets(String[] subjects, Date startDate, Date endDate)
	{
		Queue<SuperStatus> currentTweets, targetTweets = new Queue<SuperStatus>();
		String[] currentTags, editedSubjects;
		SuperStatus currentTweet;
		int foundIndex, size = 0;
		boolean found;
		if (subjects.length == 0 || startDate == null || endDate == null)
			return targetTweets;
		
		for (int i = 0; i < subjects.length; i++)
			if (subjects[i] != null && !subjects[i].equals(""))
				size++;
		editedSubjects = new String[size];
		size = 0;
		for (int i = 0; i < subjects.length; i++)
		{
			if (subjects[i] != null && !subjects[i].equals(""))
			{
				editedSubjects[size] = subjects[i];
				size++;
			}
		}
		if (editedSubjects.length == 0)
			return targetTweets;
		
		Arrays.sort(editedSubjects);
		currentTweets = getTweets(editedSubjects[0], startDate, endDate);
		size = currentTweets.size();
		for (int i = 0; i < size; i++)
		{
			foundIndex = 0;
			found = true;
			currentTweet = currentTweets.dequeue();
			currentTags = currentTweet.getTags();
			Arrays.sort(currentTags);
			for (int k = 1; k < editedSubjects.length; k++)
			{
				foundIndex = Arrays.binarySearch(currentTags, foundIndex, editedSubjects.length - 1, editedSubjects[k]);
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
	public synchronized Queue<SuperStatus> getTweets(String subject)
	{
		Queue<SuperStatus> q = new Queue<SuperStatus>();
		if (subject == null || subject.equals(""))
			return q;
		RBBST<Date, SuperStatus> tree = tweetTable.get(subject);
		try
		{
			for (Date d : tree.keys())
			{
				q.enqueue(tree.get(d));
			}
			return q;
		}
		catch(NullPointerException e)
		{
			return q;
		}
	}
	public synchronized Queue<SuperStatus> getTweets(String[] subjects)
	{
		Queue<SuperStatus> currentTweets, targetTweets = new Queue<SuperStatus>();
		String[] currentTags, editedSubjects;
		SuperStatus currentTweet;
		int foundIndex, size = 0;
		boolean found;
		if (subjects.length == 0)
			return targetTweets;
		for (int i = 0; i < subjects.length; i++)
			if (subjects[i] != null && !subjects[i].equals(""))
				size++;
		editedSubjects = new String[size];
		size = 0;
		for (int i = 0; i < subjects.length; i++)
		{
			if (subjects[i] != null && !subjects[i].equals(""))
			{
				editedSubjects[size] = subjects[i];
				size++;
			}
		}
		if (editedSubjects.length == 0)
			return targetTweets;
		Arrays.sort(editedSubjects);
		currentTweets = getTweets(editedSubjects[0]);
		size = currentTweets.size();
		for (int i = 0; i < size; i++)
		{
			foundIndex = 0;
			found = true;
			currentTweet = currentTweets.dequeue();
			currentTags = currentTweet.getTags();
			Arrays.sort(currentTags);
			for (int k = 1; k < editedSubjects.length; k++)
			{
				foundIndex = Arrays.binarySearch(currentTags, foundIndex, editedSubjects.length - 1, editedSubjects[k]);
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
	 * Returns All Tweets in TweetTable. Potentially very long (N lg N) runtime, and large memory usage.
	 * @return All Tweets
	 */
	public synchronized Queue<SuperStatus> getAllTweets()
	{
		Queue<SuperStatus> allTweets = new Queue<SuperStatus>();
		for (String s : tweetTable.keys())
		{
			for (SuperStatus t : getTweets(s))
			{
				allTweets.enqueue(t);
			}
		}
		return allTweets;
	}
	/**
	 * Returns All Tweets in TweetTable. Potentially very long (S * lg N * lg M) runtime.
	 * @return All Tweets
	 */
	public synchronized Queue<SuperStatus> getAllTweets(Date startDate, Date endDate)
	{
		Queue<SuperStatus> allTweets = new Queue<SuperStatus>();
		for (String s : tweetTable.keys())
		{
			for (SuperStatus t : getTweets(s, startDate, endDate))
			{
				allTweets.enqueue(t);
			}
		}
		return allTweets;
	}
}
