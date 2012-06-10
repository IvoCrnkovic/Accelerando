import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Scanner;


public class Controller {

	/**
	 * @param args
	 */
	public void main(String[] args) {
		Scanner in = null;
		Scanner stdIn = new Scanner(System.in);
		GregorianCalendar origin = new GregorianCalendar();
		
		//Get the subject
		System.out.println("Enter something to analyze!");
		String subject = in.nextLine();
		String [] subjects = subject.split(" ");
		
		
		//Determine if we need to get more tweets on the entered subject.
		Date endTime = origin.getTime();
		origin.add(Calendar.HOUR_OF_DAY, -1);
		Date startTime = origin.getTime();
		
		Iterable targetTweets = TweetTable.getTweets(subjects[0], startTime, endTime);
		
		for (int i = 0; i < subjects.length; i++)
		{
			SingleSubjectPuller.singleSubjectPull(subjects[i])
		}
		
	}

}
