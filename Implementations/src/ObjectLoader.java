/**
 * Helper Class. Writes and reads objects for String filenames
 */

import java.io.*;
public class ObjectLoader {
	public static Object load(String filename) throws IOException, ClassNotFoundException, FileNotFoundException
	{
		FileInputStream f_in = null;
		f_in = new FileInputStream (filename);
		ObjectInputStream words_in = new ObjectInputStream(f_in);
		Object obj = words_in.readObject();
		return obj;
	}
	// Save the TST
	public static void save(Object obj, String filename) throws IOException, FileNotFoundException
	{
		FileOutputStream f_out = new FileOutputStream (filename);
		ObjectOutputStream obj_out = new ObjectOutputStream (f_out);
		obj_out.writeObject(obj);
		obj_out.flush();
		obj_out.close();
	}
	
	
	// Backup the TweetHashTable data structure from superTweetsFile to superTweetsBackup
	public static void backupTweets(File superTweetsFile, File superTweetsBackup)
	{
		FileInputStream tweetIn = null;
		FileOutputStream backupOut = null;
		byte[] buf = new byte[1024];
		try
		{
			superTweetsBackup.delete();
			try {
				superTweetsBackup.createNewFile();
			} catch (IOException e1) {
				System.err.println("Unable to Create Backup");
			}
			try {
				backupOut = new FileOutputStream(superTweetsBackup);
			} catch (FileNotFoundException e2) {
				System.err.println("Unable to Initialize Backup Output Stream");
			}
			try {
				tweetIn = new FileInputStream(superTweetsFile);
			} catch (FileNotFoundException e1) {
				System.err.println("Unable to Initialize Backup Input Stream from " + superTweetsFile);
			}
			int len;
			while ((len = tweetIn.read(buf)) > 0){
				backupOut.write(buf, 0, len);
			}
			backupOut.flush();
			backupOut.close();
		}
		catch (NullPointerException e1)
		{
			System.err.println("Backup Failed");
		} catch (IOException e) {
			System.err.println("Backup Failed");
		}
	}
}
