import java.util.Scanner;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import com.mashape.client.exceptions.MashapeClientException;

import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
public class Comparer {
	static TweetEvaluator tweetEvaluator;
	static Queue<Status> q;
	static StatusListener listener = new StatusListener(){
	    public void onStatus(Status status)
	    {
	    	try {
				langDetector = DetectorFactory.create();
			} catch (LangDetectException e) {
				e.printStackTrace();
				return;
			}
			langDetector.append(status.getText());
			try {
				if (!langDetector.detect().equals("en"))
				{
					return;
				}
			} catch (LangDetectException e1) {
				System.err.println(e1);
				return;
			}
	    	if (q.size() < 100)
	    		q.enqueue(status);
	    }
	    public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) 
	    {
	    	//System.err.println("Deletion Notice");
	    }
	    public void onTrackLimitationNotice(int numberOfLimitedStatuses) 
	    {
	    	System.err.println("Limitation Notice: " + numberOfLimitedStatuses);
	    }
	    public void onException(Exception ex) {
	    	System.err.println("Exception");
	    }
	    public void onScrubGeo(long arg0, long arg1) {
			System.err.println("ScrubGeo");
	    }
	};
	private static Detector langDetector;
	public static void main(String[] args) {
		q = new Queue<Status>();
		tweetEvaluator = new TweetEvaluator(CollectionMethods.<TST<PolarityValue>>load("words.tst"));
		TwitterStream twitterStream = CollectionMethods.authenticateStream();
		twitterStream.addListener(listener);
	    twitterStream.sample();
	    
	    
	  //Lang Detection
	  		try {
	  			DetectorFactory.loadProfile("lib/profiles");
	  		} catch (LangDetectException e2) {
	  			System.err.println("Failed to load language profile");
	  			System.exit(0);
	  		}
	  		try {
	  			langDetector = DetectorFactory.create();
	  		} catch (LangDetectException e2) {
	  			System.err.println("Failed to Create Language Detector");
	  			System.exit(0);
	  		}
	  		langDetector.setMaxTextLength(200);
	  		
	  		
	    Thread Client = new Thread(new Client());
	    Client.setPriority(Thread.MAX_PRIORITY);
	    Client.start();
	}
	
	
	
	private static class Client implements Runnable
	{
		public void run() {
			int calls = 0;
			Status s;
			System.out.println("Private Key: ");
			String privateKey = new Scanner(System.in).next();
			SentimentAnalysisFree client = new SentimentAnalysisFree("nhbhqz3xqonjwaw8yurrxngptmavny", privateKey);
			for (int i = 0; i < 10; i++)
			{
				if (!q.isEmpty())
				{
					s = q.dequeue();
					System.out.println("Status:\t\t\t\t\t" + s.getText());
					System.out.println("Our Polarization:\t\t" + (tweetEvaluator.calculatePolarization(s) / 5));
					JSONObject obj = null;
					try {
						calls++;
						if (calls > 10)
							System.exit(0);
						obj = client.classifytext("en", s.getText());
					} catch (MashapeClientException e) {
						e.printStackTrace();
						System.exit(0);
					}
					try {
						System.out.println("Reference Polarization:\t" + obj.getDouble("value"));
					} catch (JSONException e) {
						e.printStackTrace();
						System.exit(0);
					}
				}
			}
		}
	}
}
