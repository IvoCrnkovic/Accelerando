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
	public static void backupFile(File superTweetsFile, File superTweetsBackup) throws IOException, FileNotFoundException, NullPointerException 
	{
		FileInputStream tweetIn = null;
		FileOutputStream backupOut = null;
		byte[] buf = new byte[1024];
		superTweetsBackup.delete();
		superTweetsBackup.createNewFile();
		backupOut = new FileOutputStream(superTweetsBackup);
		tweetIn = new FileInputStream(superTweetsFile);
		int len;
		while ((len = tweetIn.read(buf)) > 0){
			backupOut.write(buf, 0, len);
		}
		backupOut.flush();
		backupOut.close();
	}
}
