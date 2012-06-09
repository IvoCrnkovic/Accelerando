import java.util.*;
import java.io.*;
public class TXTtoTWTconverter {
	public static void main(String[] args)
	{
		TST<PolarityValue> tst = new TST<PolarityValue>();
		String txtFilename = "words.txt";
		String tstFilename = "words.tst";
		Scanner in = null;
		try {
			in = new Scanner(new File(txtFilename));
		} catch (FileNotFoundException e1) {
			System.err.println("Failed to initialize input stream");
		}
		double polarity;
		int occurrences;
		String subject;
		int numWords = in.nextInt();
		in.nextLine();
		for (int i = 0; i < numWords; i++)
		{
			System.out.println("1");
			subject = in.nextLine();
			polarity = in.nextDouble();
			in.nextLine();
			occurrences = in.nextInt();
			in.nextLine();
			tst.put(subject, new PolarityValue(polarity, occurrences));
		}
		try {
			ObjectLoader.save(tst, tstFilename);
		} catch (IOException e) {
			System.err.println("Save Unsuccessful");
		}
	}
}
