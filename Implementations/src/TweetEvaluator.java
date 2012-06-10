import twitter4j.*;

/**
 * Class useful for determining polarization and weighting of superTweets.  Includes post-processing methods for sanitize and intepret tweet text.
 */
public class TweetEvaluator {
	
	TST<PolarityValue> wordPolarities = null;
	TST<SuperUser> users = null;
	/**
	 * Constructor Method.
	 * @param tstFilename The filename where the .tst file with serialized TST can be found to 
	 */
	public TweetEvaluator(TST<PolarityValue> wordPolarities, TST<SuperUser> users)
	{
		this.wordPolarities = wordPolarities;
		this.users = users;
	}
	
	/**
	 * Method for calculating polarization of a tweet.
	 * 
	 * <p>
	 * Algorithm is as follows:
	 * Total Sentiment = return pSent * (sSent + wSent) / 2, where pSent is the punctuation sentiment, sSent is the smiley sentiment and wSent is the word sentiment.
	 * pSent is calculated as a multiplier based off the number of exclamation points or question points in the text of the tweet.
	 * sSent is calculated by assigning positive or negative values to the different parts of smileys and then adding them together.
	 * wSent is calculated as an average of the word sentiment of each word in the tweet text.
	 * 
	 * @param tweet the tweet to analyze
	 * @return A double representing the total polarization of the tweet.
	 */
	public double calculatePolarization(SuperTweet tweet) {
		return determineSentiment(tweet.getTweet().getText());
	}
	
	
	/**
	 *Method for calculating the weight (importance) of any given tweet.
	 *
	 *<p>
	 *The weight is equal to the base 10 logarithm of the sum of the followers and listed count.
	 *
	 * @param tweet The tweet to analyze.
	 * @return A double representing the weight of the tweet.
	 * @throws TwitterException
	 */
	public double calculateWeight(SuperTweet tweet) throws TwitterException 
	{
        long numFollowers= tweet.getUser().getFollowersCount();
        long numListed   = tweet.getUser().getListedCount();
        double total = numFollowers + numListed;
        
        return Math.log10(total+2);
	}
	
	private String removeFluff(String text)
	{
		int io = text.indexOf("http://t.co/");
		while(io > -1)
		{
			if(io + 20 < text.length())
				text = text.replaceAll(text.substring(io, io + 20), "");
			else
				text = text.replaceAll(text.substring(io, text.length()), "");
			
			io = text.indexOf("http://t.co/");
		}		
		return text;
	}
	
	private String[] tweetToArray(String text)
	{
		text = text.toLowerCase();
		text = text.replaceAll("[^abcdefghijklmnopqrstuvwxyz0123456789]", " ");
		String[] tokens = text.split("\\s+");
		return tokens;
	}
	
	private boolean contains(char[] array, char c)
	{
		int length = array.length;
		for(int i = 0; i < length; i++)
		{
			if(array[i] == c)
				return true;
		}
		return false;
	}
	
	private double smileySentiment(String text)
	{
		int a=0,b=0,c=0,d=0,e=0,len=text.length();
		char[] eye  = {':',';','8','=','x','X'};
		char[] nose = {'-','^','\''};
		char[] good = {')',']','}','3','>','D','p','*'};
		char[] bad  = {'(','[','{','<'};
		char[] okay = {'|'};
		char[] skep = {'\\','/'};
		char[] surp = {'o','O','0','@'};
		
		for(int i = 0; i < len - 1; i++)
		{
			if(contains(eye, text.charAt(i)))
			{
				if(contains(nose, text.charAt(i+1)) && i < len - 2)
				{
					if(contains(good, text.charAt(i+2)))
						a++;
					else if(contains(bad, text.charAt(i+2)))
						b++;
					else if(contains(okay, text.charAt(i+2)))
						c++;
					else if(contains(skep, text.charAt(i+2)))
						d++;
					else if(contains(surp, text.charAt(i+2)))
						e++;
				}
				else
				{
					if(contains(good, text.charAt(i+1)))
						a++;
					else if(contains(bad, text.charAt(i+1)))
						b++;
					else if(contains(okay, text.charAt(i+1)))
						c++;
					else if(contains(skep, text.charAt(i+1)))
						d++;
					else if(contains(surp, text.charAt(i+1)))
						e++;
				}
			}
		}
		
		double total = a+b+c+d+e+.5;
		double sentiment = ((5*a) - (5*b) - (1*c) - (3*d)) / total;
		
		return sentiment;
	}
	
	private double wordSentiment(String[] words)
	{
		double totalWordSentiment = 0;
		String word;
		for (int i = 0; i < words.length; i++)
		{
			word = words[i];
			if (word != null && word.length() != 0 && wordPolarities.contains(word))
				totalWordSentiment += wordPolarities.get(word).getScore();
		}
		if (words.length != 0)
			totalWordSentiment /= words.length;
		
		return totalWordSentiment;
	}
	
	private double punctuationSentiment(String text)
	{
		int[] punctuation = new int[2];
		int length = text.length();
		for(int i = 0; i < length; i++)
		{
			if(text.charAt(i) == '!')
				punctuation[0]++;
			else if (text.charAt(i) == '?')
				punctuation[1]++;
		}
		double total = punctuation[0] + punctuation[1] + 1;
		return (punctuation[0] + .5) / total;
	}
	
	private double determineSentiment(String text)
	{
		text = removeFluff(text);
		double sSent = smileySentiment(text);
		double pSent = punctuationSentiment(text);
		double wSent = wordSentiment(tweetToArray(text));
		
		return (1 + pSent * .3) * (sSent * .35 + wSent * .65);
	}
}
