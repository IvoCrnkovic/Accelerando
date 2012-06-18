import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.Configuration;


public class StreamPuller 
{
	public static void streamPull(TweetTable tweetTable, TST<PolarityValue> wordPolarities, String tweetTableFile, String tweetTableBackup, Configuration config)
	{
		Queue<Status> toBeAdded = new Queue<Status>();
		StatusListener listener = new StatusListener(){
	        public void onStatus(Status status) {
	            toBeAdded.enqueue(status);
	        }
	        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}
	        public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}
	        public void onException(Exception ex) {
	            ex.printStackTrace();
	        }
			public void onScrubGeo(long arg0, long arg1) {
				// TODO Auto-generated method stub
				
			}
	    };
	    TwitterStream twitterStream = new TwitterStreamFactory(config).getInstance();
	    twitterStream.addListener(listener);
	    twitterStream.sample();
	}
}
