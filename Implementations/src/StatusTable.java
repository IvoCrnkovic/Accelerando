import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import twitter4j.Status;
import twitter4j.User;
/**
 * Class for storing Statuses
 */
public class StatusTable implements java.io.Serializable
{
	private static final long serialVersionUID = -4330239673197534165L;
	
	private long size;
	
	public StatusTable()
	{
		size = 0L;
	}
	
	public synchronized long size()
	{
		return size;
	}
	
	public synchronized int subjectSize(String subject)
	{
		// TODO: Write
	}
	
	public synchronized int subjectSize(String subject, Date startDate, Date endDate)
	{
		// TODO: Write
	}
	
	public synchronized int subjectSize(String[] subject, Date startDate, Date endDate)
	{
		// TODO: Write
	}
	
	public synchronized int subjectSize(String[] subject)
	{
		// TODO: Write
	}
	
	public synchronized void add(Status t, TweetEvaluator eval, PreparedStatement s, PreparedStatement u, PreparedStatement sub)
	{
		if (t == null)
			return;
		User user = t.getUser();
		
		//Add to Database
		/*
		try
		{
			u.setLong(1, user.getId());
			u.setInt(2, user.getFollowersCount());
			u.setInt(3, user.getFriendsCount());
			u.setInt(4, user.getListedCount());
			u.setString(5, user.getName());
			u.setString(6, user.getScreenName());
			u.executeUpdate();
		}
		catch (SQLException e1)
		{
			System.err.println("SQLException: Unable to Add to User Database");
			return;
		}
		*/
		try
		{
			s.setLong(1, t.getId());
			s.setString(2, t.getText());
			s.setLong(3, t.getCreatedAt().getTime());
			s.setInt(4, (int) t.getRetweetCount());
			s.setBoolean(5, t.isRetweet());
			s.setBoolean(6, t.isFavorited());
			s.setLong(7, user.getId());
			if (t.getPlace() != null)
				s.setString(8, t.getPlace().getCountryCode());
			else
				s.setString(8, null);
			if (t.getGeoLocation() != null)
			{
				s.setDouble(9, t.getGeoLocation().getLatitude());	
				s.setDouble(10, t.getGeoLocation().getLongitude());
			}
			else
			{
				s.setDouble(9, Double.NaN);
				s.setDouble(10, Double.NaN);
			}
			s.setDouble(11, eval.calculatePolarization(t));
			s.setDouble(12, eval.calculateWeight(t));
			s.executeUpdate();
		}
		catch (SQLException e1)
		{
			System.err.println("SQLException: Unable to Add to Status Database");
			return;
		}
		
		//UPDATE
		String text;
		text = t.getText();
		text = text.replaceAll("http:\\/\\/t.co\\/........", " ");
		text = text.toLowerCase();
		String[] tags = text.split("(\\W)+");
		for (int i = 0; i < tags.length; i++)
		{
			if (tags[i] == null | tags[i].length() == 0)
			{
				return;
			}
			if (tweetTable.contains(tags[i]))
				tweetTable.get(tags[i]).put(t.getCreatedAt().getTime(), t.getId());
			else
			{
				tweetTable.put(tags[i], new RBBST<Long, Long>());
				tweetTable.get(tags[i]).put(t.getCreatedAt().getTime(), t.getId());
			}
		}
		
		size++;
	}
	
	public synchronized Queue<String> getSubjects()
	{
		return (Queue<String>) tweetTable.keys();
	}
	
	public synchronized Queue<Long> getTweets(String subject, Date startDate, Date endDate)
	{
		Queue<Long> q = new Queue<Long>();
		RBBST<Long, Long> tree = null;
		if (subject == null || subject.equals("") || startDate == null || endDate == null)
			return q;
		if (tweetTable.contains(subject))
			tree = tweetTable.get(subject);
		for (Long l : tree.keys(startDate.getTime(), endDate.getTime()))
		{
			q.enqueue(tree.get(l));
		}
		return q;
	}
	
	public synchronized Queue<Long> getTweets(String[] subjects, Date startDate, Date endDate)
	{
		Queue<Long> currentTweets, targetTweets = new Queue<Long>();
		ArrayList<Long> IDs1 = new ArrayList<Long>();
		ArrayList<Long> IDs2 = new ArrayList<Long>();
		boolean using1 = true;
		String[] editedSubjects;
		int index, num = 0;
		if (subjects.length == 0 || startDate == null || endDate == null)
			return targetTweets;
		
		for (int i = 0; i < subjects.length; i++)
			if (subjects[i] != null && !subjects[i].equals(""))
				num++;
		editedSubjects = new String[num];
		size = 0;
		for (int i = 0; i < subjects.length; i++)
		{
			if (subjects[i] != null && !subjects[i].equals(""))
			{
				editedSubjects[num] = subjects[i];
				num++;
			}
		}
		if (editedSubjects.length == 0)
			return targetTweets;
		
		currentTweets = getTweets(editedSubjects[0], startDate, endDate);
		for (Long l : currentTweets)
		{
			IDs1.add(l);
		}
		Collections.sort(IDs1);
		
		for (int i = 1; i < editedSubjects.length; i++)
		{
			currentTweets = getTweets(editedSubjects[i], startDate, endDate);
			for (Long l : currentTweets)
			{
				if (using1)
				{
					index = Collections.binarySearch(IDs1, l);
					if (index >= 0)
						IDs2.add(l);
				}
				else
				{
					index = Collections.binarySearch(IDs2, l);
					if (index >= 0)
						IDs1.add(l);
				}
			}
			if (using1)
			{
				using1 = false;
				IDs1 = new ArrayList<Long>();
			}
			else
			{
				using1 = true;
				IDs2 = new ArrayList<Long>();
			}
		}
		
		if (using1)
			for (Long l : IDs1)
				targetTweets.enqueue(l);
		else
			for (Long l : IDs2)
				targetTweets.enqueue(l);
		return targetTweets;
	}
	
	public synchronized Queue<Long> getTweets(String subject)
	{
		Queue<Long> q = new Queue<Long>();
		if (subject == null || subject.equals("") || !tweetTable.contains(subject))
			return q;
		RBBST<Long, Long> tree = tweetTable.get(subject);
		for (Long l : tree.keys())
		{
			q.enqueue(tree.get(l));
		}
		return q;
	}
	
	public synchronized Queue<Long> getTweets(String[] subjects)
	{
		return getTweets(subjects, new Date(0L), new Date());
	}
	
	public synchronized Queue<Long> getAllTweets()
	{
		Queue<Long> allTweets = new Queue<Long>();
		for (String s : tweetTable.keys())
		{
			for (Long l : getTweets(s))
			{
				allTweets.enqueue(l);
			}
		}
		return allTweets;
	}
	
	public synchronized Queue<Long> getAllTweets(Date startDate, Date endDate)
	{
		Queue<Long> allTweets = new Queue<Long>();
		for (String s : tweetTable.keys())
		{
			for (Long l : getTweets(s, startDate, endDate))
			{
				allTweets.enqueue(l);
			}
		}
		return allTweets;
	}
}
