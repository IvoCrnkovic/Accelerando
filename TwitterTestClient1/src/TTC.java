import java.util.List;
import java.util.Scanner;
import java.io.*;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Tweet;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

public class TTC {
	
	public static void main(String[] args)
	{
		writeHotTopics();
	}
	
	private static void writeCommonWords() throws FileNotFoundException
	{
		File f = new File("wordlist.txt");
		Scanner in = new Scanner(f);
		TwitterFactory twitterFactory = new TwitterFactory();
        Twitter twitter = twitterFactory.getInstance();
        Query query;
        QueryResult result;
        List<Tweet> tweets;
        System.out.println("start");
        try {
        	PrintWriter out = null;
        	
			try {
				out = new PrintWriter(new FileWriter("CommonWords.txt"));
			} catch (IOException e) {
				e.printStackTrace();
			}
        	
        	String seed;
        	Tweet tweet;
        	String post;
        	
        	for(int j = 0; j < 1000; j++){
        		seed = in.next();
        		query = new Query(seed);
        		query.setRpp(2);
        		query.setLang("en");
				result = twitter.search(query);
				tweets = result.getTweets();
				if(tweets.isEmpty()){
					System.out.println(j + " (0)");
					continue;
				}
				tweet = tweets.get(0);
				post = tweet.getText();
				out.println(post);
				if(tweets.size() == 1){
					System.out.println(j + " (1)");
					continue;
				}
				tweet = tweets.get(1);
				post = tweet.getText();
				out.println(post);
				System.out.println(j + " (2)");
        	}
        	
        	out.close();
        	
		} catch (TwitterException e) {
			System.out.println("WARNING: EXCEPTION");
			e.printStackTrace();
		}
		
        System.out.println("\n--DONE--");
	}
	
	private static void writeHotTopics()
	{
		//number of tweets per topic
		int numTweets = 20;
		String[] topic = {"Justin Bieber",
				"Kim Kardashian",
				"Jennifer Aniston",
				"Lindsay Lohan",
				"Jennifer Lopez",
				"Britney Spears",
				"Katy Perry",
				"Megan Fox",
				"Lady Gaga",
				"Miley Cyrus",
				"Sandra Bullock",
				"Tiger Woods",
				"Barack Obama",
				"Kate Gosselin",
				"Jesse James",
				"Michael Jackson",
				"Casey Anthony",
				"Osama Bin Laden",
				"Hurricane Irene",
				"Japan Earthquake Tsunami",
				"Amy Winehouse",
				"Joplin Tornado",
				"Conrad Murray",
				"Rick Santorum",
				"Maria Sharapova",
				"Serena Williams",
				"Brett Favre",
				"Caroline Wozniacki",
				"Kobe Bryant",
				"Lebron James",
				"Lamar Odom",
				"Hope Solo",
				"Carmelo Anthony",
				"Selena Gomez",
				"Nicki Minaj",
				"Rihanna",
				"Beyonce",
				"Taylor Swift",
				"Xbox",
				"Kindle",
				"Playstation",
				"iPhone",
				"iPad",
				"Wii",
				"Nook",
				"Windows Phone",
				"Macbook",
				"American Idol",
				"Dancing with the Stars",
				"Glee",
				"Jersey Shore",
				"Family Guy",
				"Chelsea Handler",
				"The Young and the Restless",
				"True Blood",
				"Audrina",
				"Big Brother",
				"The Bachelor",
				"The Bachelorette",
				"The Biggest Loser",
				"Survivor",
				"Deadliest Catch",
				"Khloe Kardashian",
				"Heidi Montag",
				"Kourtney Kardashian",
				"Kendra Wilkinson",
				"Snooki",
				"Kristin Cavallari",
				"Audrina Patridge",
				"Tori Spelling",
				"David Letterman",
				"Conan O’Brien",
				"Jon Stewart",
				"Jay Leno",
				"Jimmy Fallon",
				"Jimmy Kimmel",
				"Stephen Colbert",
				"Craig Ferguson",
				"Wendy Williams",
				"Ellen Degeneres",
				"Anderson Cooper",
				"Rachael Ray",
				"Rosie O’Donnell",
				"Regis and Kelly",
				"The View",
				"Nate Berkus",
				"The Talk",
				"Gayle King",
				"Today Show",
				"Good Morning America",
				"The Early Show",
				"Fox & Friends",
				"Morning Joe",
				"CNN American Morning",
				"Fox News",
				"E3",
				"Kurt Busch",
				"MLB Draft",
				"Scott Walker",
				"Richard Dawson",
				"Miss USA",
				"Justin Blackmon",
				"Summer Jam",
				"Seattle",
				"John Edwards",
				"Snow White and the Huntsman",
				"Men in Black 3",
				"The Avengers",
				"Battleship",
				"The Dictator",
				"The Hunger Games",
				"Diablo 3",};
		
		//initialization
		TwitterFactory twitterFactory = new TwitterFactory();
        Twitter twitter = twitterFactory.getInstance();
        Query query;
        QueryResult result;
        List<Tweet> tweets;
        String tweetText;
    	int numResults, tweetsPrinted, numTopics = topic.length;
        System.out.println("start");
        try {
        	PrintWriter out = null;
			try {
				out = new PrintWriter(new FileWriter("HotTopics.txt"));
			} catch (IOException e) {
				e.printStackTrace();
			}
        	
        	//write tweets
        	for(int j = 0; j < numTopics; j++){
        		
        		//get tweets
        		query = new Query(topic[j]);
        		query.setRpp(numTweets);
        		query.setLang("en");
				result = twitter.search(query);
				tweets = result.getTweets();
				numResults = tweets.size();
				
				//print out name
				System.out.println(topic[j]);
				tweetsPrinted = 0;
				
				//print out tweets
				for(int i = 0; i < numResults && tweetsPrinted < 10; i++)
				{
					boolean putIt = true;
					tweetText = tweets.get(i).getText();
					if(tweetText.indexOf("RT") > -1)
						for(int k = 0; k < i; k++)
							if(tweetText.equals(tweets.get(k).getText()))
							{
								putIt = false;
								break;
							}
					if(tweetText.indexOf('\n') > -1)
						putIt = false;
					if(putIt)
					{
						out.println(topic[j]);
						out.println(tweetText);
						tweetsPrinted++;
					}
				}
        	}
        	
        	out.close();
        	
		} catch (TwitterException e) {
			System.out.println("WARNING: EXCEPTION");
			e.printStackTrace();
		}
		
        System.out.println("\n--DONE--");

	}
	
}
