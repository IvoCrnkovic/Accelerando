import java.io.File;
import java.io.IOException;

/**Author: Ivo Crnkovic-Rubsamen
 * Date Created: June 7, 2012
 * A little file to change TSTs being stored as .txt files to .tst files.  Use method change.
 */


public class changeFromTXTToTST {
	
	private static void change (String txtFileName, String tstFileName) throws IOException
	{
		TST<PolarityGenerator.Value> TST = PolarityGenerator.loadTXT(new File(txtFileName));
		
		TST.save(new File(tstFileName));
	}
	
	public static void main(String[] args) throws IOException 
	{
		change("words.txt", "words.tst");
	}

}
