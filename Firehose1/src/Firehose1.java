import twitter4j.*;
import twitter4j.auth.*;
import twitter4j.conf.*;
import java.lang.*;

public class Firehose1 {

	public static void main(String[] args) {
		final String user = "turtleman755";
		final String password = "accelerando";
		final String consumerKey = "bceUVDFbpUh2pcu6gvpp9w";
		final String consumerSecret = "pvei5cAMJgy5qXQPjuyyki508ZxHPM6ypRJt94OW9sY";
		final String token = "17186983-6cc4E3GPsn1aFNrMsr5qJKpm8a0mxFl8ozsmUP43t";
		final String tokenSecret = "p4Sc5WrSdSL2R4cUxqal86QRosbW5txbFQYF5ItOow";
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true);
		cb.setUser(user);
		cb.setPassword(password);
		cb.setOAuthConsumerKey(consumerKey);
		cb.setOAuthConsumerSecret(consumerSecret);
		cb.setOAuthAccessToken(token);
		cb.setOAuthAccessTokenSecret(tokenSecret);

		
		StatusListener SL = new StatusListener()
		{
	        public void onStatus(Status status)
	        {
	        	System.out.println("got status");
	        }
	        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice)
	        {
	        	System.out.println("got deletionnotice");
	        }
	        public void onTrackLimitationNotice(int numberOfLimitedStatuses)
	        {
	        	System.out.println("got tracklimitationnotice");
	        }
	        public void onException(Exception ex) {
	            //ex.printStackTrace();
	        }
			public void onScrubGeo(long arg0, long arg1)
			{
				System.out.println("got scrubgeo");
			}
	    };
		AccessToken AT = new AccessToken(token, tokenSecret);
		
		TwitterStreamFactory TSF = new TwitterStreamFactory(cb.build());
		TwitterStream TS = TSF.getInstance(AT);
		TS.addListener(SL);
		//StatusStream ss = TS.getFilterStream();
		//TS.sample();

	}

}
