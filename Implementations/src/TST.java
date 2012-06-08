import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.File;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Collections;

/**
 *Implementation of a Ternary Search Trie
 */
public class TST<Value> implements java.io.Serializable
{
	/**
	 *Serial ID number that eclipse told us to use for serialization.  Don't really know what it does.
	 *@see Java.io.Serializable
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Int storing the number of key-value pairs in the TST
	 */
	private int N;       // size
    private Node root;   // root of TST

    private class Node implements java.io.Serializable {
        private char c;                 // character
        private Node left, mid, right;  // left, middle, and right subtries
        private Value val;              // value associated with string
    }

    // return number of key-value pairs
    public int size() {
        return N;
    }

    public boolean contains(String key) {
        return get(key) != null;
    }

    public Value get(String key) {
        if (key == null || key.length() == 0) throw new RuntimeException("illegal key");
        Node x = get(root, key, 0);
        if (x == null) return null;
        return (Value) x.val;
    }

    // return subtrie corresponding to given key
    private Node get(Node x, String key, int d) {
        if (key == null || key.length() == 0) throw new RuntimeException("illegal key");
        if (x == null) return null;
        char c = key.charAt(d);
        if      (c < x.c)              return get(x.left,  key, d);
        else if (c > x.c)              return get(x.right, key, d);
        else if (d < key.length() - 1) return get(x.mid,   key, d+1);
        else                           return x;
    }


    public void put(String s, Value val) {
        if (!contains(s)) N++;
        root = put(root, s, val, 0);
    }

    private Node put(Node x, String s, Value val, int d) {
        char c = s.charAt(d);
        if (x == null) {
            x = new Node();
            x.c = c;
        }
        if      (c < x.c)             x.left  = put(x.left,  s, val, d);
        else if (c > x.c)             x.right = put(x.right, s, val, d);
        else if (d < s.length() - 1)  x.mid   = put(x.mid,   s, val, d+1);
        else                          x.val   = val;
        return x;
    }
    
    public String longestPrefixOf(String s) {
        if (s == null || s.length() == 0) return null;
        int length = 0;
        Node x = root;
        int i = 0;
        while (x != null && i < s.length()) {
            char c = s.charAt(i);
            if      (c < x.c) x = x.left;
            else if (c > x.c) x = x.right;
            else {
                i++;
                if (x.val != null) length = i;
                x = x.mid;
            }
        }
        return s.substring(0, length);
    }

    // all keys in symbol table
    public Iterable<String> keys() {
        Queue<String> queue = new Queue<String>();
        collect(root, "", queue);
        return queue;
    }

    // all keys starting with given prefix
    public Iterable<String> prefixMatch(String prefix) {
        Queue<String> queue = new Queue<String>();
        Node x = get(root, prefix, 0);
        if (x == null) return queue;
        if (x.val != null) queue.enqueue(prefix);
        collect(x.mid, prefix, queue);
        return queue;
    }

    // all keys in subtrie rooted at x with given prefix
    private void collect(Node x, String prefix, Queue<String> queue) {
        if (x == null) return;
        collect(x.left,  prefix,       queue);
        if (x.val != null) queue.enqueue(prefix + x.c);
        collect(x.mid,   prefix + x.c, queue);
        collect(x.right, prefix,       queue);
    }


    // return all keys matching given wilcard pattern
    public Iterable<String> wildcardMatch(String pat) {
        Queue<String> queue = new Queue<String>();
        collect(root, "", 0, pat, queue);
        return queue;
    }
 
    public void collect(Node x, String prefix, int i, String pat, Queue<String> q) {
        if (x == null) return;
        char c = pat.charAt(i);
        if (c == '.' || c < x.c) collect(x.left, prefix, i, pat, q);
        if (c == '.' || c == x.c) {
            if (i == pat.length() - 1 && x.val != null) q.enqueue(prefix + x.c);
            if (i < pat.length() - 1) collect(x.mid, prefix + x.c, i+1, pat, q);
        }
        if (c == '.' || c > x.c) collect(x.right, prefix, i, pat, q);
    }
    
    // Save the TST
    public void save(File fileName) throws IOException
    {
    	// Use a FileOutputStream to send data to a file
    	// called myobject.data.
    	FileOutputStream f_out = new FileOutputStream (fileName);
    	// Use an ObjectOutputStream to send object data to the
    	// FileOutputStream for writing to disk.
    	ObjectOutputStream obj_out = new ObjectOutputStream (f_out);
    	// Pass our object to the ObjectOutputStream's
    	// writeObject() method to cause it to be written out
    	// to disk.
    	obj_out.writeObject (this);
    }
    
    // Load an Unparamartrized TST from a File
    public static TST load(File fileName) throws IOException, ClassNotFoundException
    {
        	FileInputStream f_in = null;
    		
    		try {
    			f_in = new FileInputStream (fileName);
    		} catch (FileNotFoundException e2) {
    			System.out.println("Could not find " + fileName);
    		}
    		// Read object using ObjectInputStream.
    		ObjectInputStream words_in = new ObjectInputStream(f_in);
    		// Read an object.
    		TST generatedTST = (TST) words_in.readObject();
    		return generatedTST;
    }
    
    
    // Test save/load
    public static void main(String[] args) throws IOException, ClassNotFoundException
    {
    	/*TST<PolarityGenerator.Value> t = new TST<PolarityGenerator.Value>();
    	t.put("a", new PolarityGenerator.Value(1,1));
    	t.put("ab", new PolarityGenerator.Value(2,2));
    	t.put("ac", null);
    	t.put("ad", null);
    	t.put("ae", null);
    	t.put("f", null);
    	for (String s : t.keys())
    	{
    		System.out.println(s);
    		System.out.println(t.get(s).toString());
    	}
    	
    	
    	
    	t.save(placeForTST);
    	
    	
    	File placeForTST = new File("test.tst");
    
    	
    	
    	TST<PolarityGenerator.Value> q = TST.load(placeForTST);
    	System.out.println("s");
    	for (String s : q.keys())
    	{
    		System.out.println(s);
    		System.out.println(q.get(s).toString());
    	}
    	
    	TST<PolarityGenerator.Value> t = load(new File("words.tst"));
    	for (String s : t.keys())
    	{
    		System.out.println(s);
    		System.out.println(t.get(s).toString());
    	}
    	*/
    }
}
