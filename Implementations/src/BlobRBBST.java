import java.sql.Blob;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * Serializable implementation of a left-leaning Red Black search tree
 * 
 */
public class BlobRBBST{
	private Blob blob;
	/**
	 * Boolean RED, True.
	 */
	private static final boolean RED   = true;
	
	/**
	 * Boolean Black, False.
	 */
    private static final boolean BLACK = false;

    /**
     * Node, storing the root of the search tree.
     */
    private Node root;     // root of the BST
    private final int NODE_SIZE = 47;
    private final int ID_OFFSET = 1;
    private final int KEY_OFFSET = 10;
    private final int VALUE_OFFSET = 18;
    private final int LEFT_OFFSET = 26;
    private final int RIGHT_OFFSET = 34;
    private final int COLOR_OFFSET = 42;
    private final int N_OFFSET = 43;
    
    public BlobRBBST(Blob blob)
    {
    	this.blob = blob;
    }
    
    private Node getNode(long nodeID) throws Exception
    {
    	if (nodeID == Long.MIN_VALUE)
    		return null;
    	byte[] bytes = blob.getBytes(1 + nodeID * ((long) NODE_SIZE), NODE_SIZE);
    	if (bytes[0] != Byte.MAX_VALUE || getLong(Arrays.copyOfRange(bytes, 1, 9)) != nodeID || bytes[9] != Byte.MAX_VALUE)
    	{
    		byte[] expected = new byte[10];
    		expected[0] = Byte.MAX_VALUE;
    		expected[9] = Byte.MAX_VALUE;
    		for (int i = 0; i < 8; i++)
    			expected[i + 1] = putLong(nodeID)[i];
    		throw new Exception("Blob Data Corrupted. Expected: " + Arrays.toString(expected) + 
    				"\nFound: " + Arrays.toString(Arrays.copyOfRange(bytes, 0, 10)));
    	}
    	
    	return new Node(getLong(bytes, KEY_OFFSET), getLong(bytes, VALUE_OFFSET), (bytes[COLOR_OFFSET] != 0), 
    			getInt(bytes, N_OFFSET), getLong(bytes, LEFT_OFFSET), getLong(bytes, RIGHT_OFFSET), nodeID);
    }
    private void putInt(int value, byte[] array, int offset) {
        array[offset]   = (byte)(0xff & (value >> 24));
        array[offset+1] = (byte)(0xff & (value >> 16));
        array[offset+2] = (byte)(0xff & (value >> 8));
        array[offset+3] = (byte)(0xff & value);
    }

    private  int getInt(byte[] array, int offset) {
      return
        ((array[offset]   & 0xff) << 24) |
        ((array[offset+1] & 0xff) << 16) |
        ((array[offset+2] & 0xff) << 8) |
         (array[offset+3] & 0xff);
    }
    private byte[] putLong(long value) {
    	byte[] array = new byte[8];
        array[0]   = (byte)(0xff & (value >> 56));
        array[1] = (byte)(0xff & (value >> 48));
        array[2] = (byte)(0xff & (value >> 40));
        array[3] = (byte)(0xff & (value >> 32));
        array[4] = (byte)(0xff & (value >> 24));
        array[5] = (byte)(0xff & (value >> 16));
        array[6] = (byte)(0xff & (value >> 8));
        array[7] = (byte)(0xff & value);
        return array;
      }
    public static void putLong(long value, byte[] array, int offset) {
        array[offset]   = (byte)(0xff & (value >> 56));
        array[offset+1] = (byte)(0xff & (value >> 48));
        array[offset+2] = (byte)(0xff & (value >> 40));
        array[offset+3] = (byte)(0xff & (value >> 32));
        array[offset+4] = (byte)(0xff & (value >> 24));
        array[offset+5] = (byte)(0xff & (value >> 16));
        array[offset+6] = (byte)(0xff & (value >> 8));
        array[offset+7] = (byte)(0xff & value);
      }
    private long getLong(byte[] array) {
        return
          ((long)(array[0]   & 0xff) << 56) |
          ((long)(array[1] & 0xff) << 48) |
          ((long)(array[2] & 0xff) << 40) |
          ((long)(array[3] & 0xff) << 32) |
          ((long)(array[4] & 0xff) << 24) |
          ((long)(array[5] & 0xff) << 16) |
          ((long)(array[6] & 0xff) << 8) |
          ((long)(array[7] & 0xff));
      }
    private long getLong(byte[] array, int offset) {
        return
          ((long)(array[offset]   & 0xff) << 56) |
          ((long)(array[offset + 1] & 0xff) << 48) |
          ((long)(array[offset + 2] & 0xff) << 40) |
          ((long)(array[offset + 3] & 0xff) << 32) |
          ((long)(array[offset + 4] & 0xff) << 24) |
          ((long)(array[offset + 5] & 0xff) << 16) |
          ((long)(array[offset + 6] & 0xff) << 8) |
          ((long)(array[offset + 7] & 0xff));
      }
    /**
     * BST helper node data type.
     * 
     *
     */
    private class Node{
    	
    	/**
    	 * Key type
    	 */
    	private Long key;
    	
    	/**
    	 * The data associated with a given key.
    	 */
        private Long val;
        private long leftID, rightID, ID;
        /**
         * Pointers to the left and right subtrees.
         */
        private Node leftNode, rightNode;
        private boolean foundLeft, foundRight;
        /**
         * Color of parent link.
         */
        private boolean color; 
 
        /**
         * Number of subtrees.
         */
        private int N;

        
        /**
         * Constructor method.
         * 
         * 
         * @param key The value for the key of the node for the Node to be created.
         * @param val The value for the associated data of the Node to be created.
         * @param color The True/False value of the parent node of the one to be created.
         * @param N The number of subtrees from the node to be created.
         */
        public Node(long key, long val, boolean color, int N, long leftID, long rightID, long ID)
        {
        	this.key = key;
        	this.val = val;
        	this.color = color;
        	this.N = N;
        	this.leftID = leftID;
        	this.rightID = rightID;
        	foundLeft = false;
            foundRight = false;
            this.ID = ID;
        }
        public Node(long key, long val, boolean color, int N) throws SQLException {
        	long blobLength = blob.length();
        	this.ID = blobLength / NODE_SIZE;
            this.key = key;
            this.val = val;
            this.color = color;
            this.N = N;
            foundLeft = false;
            foundRight = false;
            
            byte[] bytes = new byte[NODE_SIZE];
            bytes[0] = Byte.MAX_VALUE;
            putLong(ID, bytes, ID_OFFSET);
            bytes[9] = Byte.MAX_VALUE;
            putLong(key, bytes, KEY_OFFSET);
            putLong(val, bytes, VALUE_OFFSET);
            putLong(Long.MIN_VALUE, bytes, LEFT_OFFSET);
            putLong(Long.MIN_VALUE, bytes, RIGHT_OFFSET);
            if (color)
            	bytes[COLOR_OFFSET] = 1;
            else
            	bytes[COLOR_OFFSET] = 0;
            putInt(N, bytes, N_OFFSET);
            
            blob.setBytes(blobLength + 1, bytes);
        }
        
        
        public Node getRight() throws Exception
        {
        	if (foundRight)
        		return rightNode;
        	Node n = getNode(rightID);
        	foundRight = true;
        	return n;
        	
        }
        public Node getLeft() throws Exception
        {
        	if (foundLeft)
        		return leftNode;
        	Node n = getNode(leftID);
        	foundLeft = true;
        	return n;
        }
        public void setLeft(Node newNode) throws SQLException
        {
        	leftID = newNode.ID;
        	blob.setBytes(1 + ID * NODE_SIZE + LEFT_OFFSET, putLong(leftID));
        }
        public void setRight(Node newNode) throws SQLException
        {
        	rightID = newNode.ID;
        	blob.setBytes(1 + ID * NODE_SIZE + RIGHT_OFFSET, putLong(rightID));
        }
        public void setColor(boolean b) throws SQLException
        {
        	color = b;
        	byte[] bool = new byte[1];
        	if (b)
        		bool[0] = 1;
        	else
        		bool[0] = 0;
        	blob.setBytes(1 + ID * NODE_SIZE + COLOR_OFFSET, bool);
        }
        public void setN(int newN) throws SQLException
        {
        	N = newN;
        	byte[] Nbytes = new byte[4];
        	putInt(newN, Nbytes, 0);
        	blob.setBytes(1 + ID * NODE_SIZE + N_OFFSET, Nbytes);
        }
    }

   /*************************************************************************
    *  Node helper methods
    *************************************************************************/

    private boolean isRed(Node x) {
        if (x == null) return false;
        return (x.color == RED);
    }

    // number of node in subtree rooted at x; 0 if x is null
    private int size(Node x) {
        if (x == null) return 0;
        return x.N;
    }


   /*************************************************************************
    *  Size methods
    *************************************************************************/

    /**
     * Method get the size of the symbol table.
     * 
     * @return number of key-value pairs in this symbol table
     */
    public int size() { return size(root); }

    /**
     * Method to check if the table is empty.
     * 
     * @return True if and only if the root is null.
     */
    public boolean isEmpty() {
        return root == null;
    }

   /*************************************************************************
    *  Standard BST search
    *************************************************************************/

    /**
     * Standard method for getting the value associated with a given key.
     * 
     * <p>
     * Implements a standard BST search procedure, left leaning.
     * 
     * 
     * @param key The key to the data to be searched for.
     * @return The value associated with a given key, null if no such key.
     * @throws Exception 
     */
    public Long get(Long key) throws Exception { return get(root, key); }
    
    private Long get(Node x, Long key) throws Exception {
        while (x != null) {
            int cmp = key.compareTo(x.key);
            if      (cmp < 0) x = x.getLeft();
            else if (cmp > 0) x = x.getRight();
            else              return x.val;
        }
        return null;
    }

    /**
     * Method to determine if there is data associated with a given key.
     * 
     * @param key The key to look for data attached to.
     * @return True if there is data.
     * @throws Exception 
     */
    public boolean contains(Long key) throws Exception {
        return (get(key) != null);
    }

    // is there a key-value pair with the given key in the subtree rooted at x?
    private boolean contains(Node x, Long key) throws Exception {
        return (get(x, key) != null);
    }

   /*************************************************************************
    *  Red-black insertion
    *************************************************************************/
	/**
	 * Method to implement Red-Black insertion.
	 * 
	 * <p>
	 * inserts the key-value pair; overwrite the old value with the new value if the key is already present
     * 
	 * @param key The key to file the data under.
	 * @param val The data to insert.
	 * @throws Exception 
	 */
    public void put(Long key, Long val) throws Exception {
        root = put(root, key, val);
        root.color = BLACK;
    }

    // insert the key-value pair in the subtree rooted at h
    private Node put(Node h, Long key, Long val) throws Exception { 
        if (h == null) return new Node(key, val, RED, 1);

        
        if      (key < h.key) h.setLeft(put(h.getLeft(),  key, val)); 
        else if (key > h.key) h.setRight(put(h.getRight(), key, val)); 
        else              h.val   = val;

        // fix-up any right-leaning links
        if (isRed(h.getRight()) && !isRed(h.getLeft()))      h = rotateLeft(h);
        if (isRed(h.getLeft())  &&  isRed(h.getLeft().getLeft())) h = rotateRight(h);
        if (isRed(h.getLeft())  &&  isRed(h.getRight()))     flipColors(h);
        h.N = size(h.getLeft()) + size(h.getRight()) + 1;

        return h;
    }

   /*************************************************************************
    *  Red-black deletion
    *************************************************************************/

    /**
     * Method to implement Red-Black deletion of the minimum key.
     * <p>
     * Deletes the key-value pair with the minimum valued key
     *//*
    // delete the key-value pair with the minimum key
    public void deleteMin() {
        if (isEmpty()) throw new RuntimeException("BST underflow");

        // if both children of root are black, set root to red
        if (!isRed(root.left) && !isRed(root.right))
            root.color = RED;

        root = deleteMin(root);
        if (!isEmpty()) root.color = BLACK;
    }

    // delete the key-value pair with the minimum key rooted at h
    private Node deleteMin(Node h) { 
        if (h.left == null)
            return null;

        if (!isRed(h.left) && !isRed(h.left.left))
            h = moveRedLeft(h);

        h.left = deleteMin(h.left);
        return balance(h);
    }

    */
    /**
     * Method to implement Red-Black deletion of the maximum key.
     * <p>
     * Deletes the key-value pair with the maximum valued key
     *//*
    public void deleteMax() {
        if (isEmpty()) throw new RuntimeException("BST underflow");

        // if both children of root are black, set root to red
        if (!isRed(root.left) && !isRed(root.right))
            root.color = RED;

        root = deleteMax(root);
        if (!isEmpty()) root.color = BLACK;
    }

    // delete the key-value pair with the maximum key rooted at h
    private Node deleteMax(Node h) { 
        if (isRed(h.left))
            h = rotateRight(h);

        if (h.right == null)
            return null;

        if (!isRed(h.right) && !isRed(h.right.left))
            h = moveRedRight(h);

        h.right = deleteMax(h.right);

        return balance(h);
    }
*/
    /**
     * Method to implement Red-Black deletion of a given key.
     * <p>
     * Deletes the key-value pair with a given key
     *//*
    public void delete(Long key) { 
        if (!contains(key)) {
            System.err.println("symbol table does not contain " + key);
            return;
        }

        // if both children of root are black, set root to red
        if (!isRed(root.left) && !isRed(root.right))
            root.color = RED;

        root = delete(root, key);
        if (!isEmpty()) root.color = BLACK;
    }

    // delete the key-value pair with the given key rooted at h
    private Node delete(Node h, Long key) { 
        assert contains(h, key);

        if (key.compareTo(h.key) < 0)  {
            if (!isRed(h.left) && !isRed(h.left.left))
                h = moveRedLeft(h);
            h.left = delete(h.left, key);
        }
        else {
            if (isRed(h.left))
                h = rotateRight(h);
            if (key.compareTo(h.key) == 0 && (h.right == null))
                return null;
            if (!isRed(h.right) && !isRed(h.right.left))
                h = moveRedRight(h);
            if (key.compareTo(h.key) == 0) {
                h.val = get(h.right, min(h.right).key);
                h.key = min(h.right).key;
                h.right = deleteMin(h.right);
            }
            else h.right = delete(h.right, key);
        }
        return balance(h);
    }
*/
   /*************************************************************************
    *  red-black tree helper functions
 * @throws Exception 
 * @throws SQLException 
    *************************************************************************/

    // make a left-leaning link lean to the right
    private Node rotateRight(Node h) throws SQLException, Exception {
        assert (h != null) && isRed(h.getLeft());
        Node x = h.getLeft();
        h.setLeft(x.getRight());
        x.setRight(h);
        x.setColor(x.getRight().color);
        x.getRight().setColor(RED);
        x.setN(h.N);
        h.setN(size(h.getLeft()) + size(h.getRight()) + 1);
        return x;
    }

    // make a right-leaning link lean to the left
    private Node rotateLeft(Node h) throws SQLException, Exception {
        assert (h != null) && isRed(h.getRight());
        Node x = h.getRight();
        h.setRight(x.getLeft());
        x.setLeft(h);
        x.setColor(x.getLeft().color);
        x.getLeft().setColor(RED);
        x.setN(h.N);
        h.setN(size(h.getLeft()) + size(h.getRight()) + 1);
        return x;
    }

    // flip the colors of a node and its two children
    private void flipColors(Node h) throws SQLException, Exception {
        // h must have opposite color of its two children
        assert (h != null) && (h.getLeft() != null) && (h.getRight() != null);
        assert (!isRed(h) &&  isRed(h.getLeft()) &&  isRed(h.getRight()))
            || (isRed(h)  && !isRed(h.getLeft()) && !isRed(h.getRight()));
        h.setColor(!h.color);
        h.getLeft().setColor(!h.getLeft().color);
        h.getRight().setColor(!h.getRight().color);
    }

    /*
    // Assuming that h is red and both h.left and h.left.left
    // are black, make h.left or one of its children red.
    private Node moveRedLeft(Node h) throws SQLException, Exception {
        assert (h != null);
        assert isRed(h) && !isRed(h.getLeft()) && !isRed(h.getLeft().getLeft());

        flipColors(h);
        if (isRed(h.getRight().getLeft())) { 
            h.setRight(rotateRight(h.getRight()));
            h = rotateLeft(h);
            // flipColors(h);
        }
        return h;
    }
	*/
    /*
    // Assuming that h is red and both h.right and h.right.left
    // are black, make h.right or one of its children red.
    private Node moveRedRight(Node h) throws SQLException, Exception {
        assert (h != null);
        assert isRed(h) && !isRed(h.getRight()) && !isRed(h.getRight().getLeft());
        flipColors(h);
        if (isRed(h.getLeft().getLeft())) { 
            h = rotateRight(h);
            // flipColors(h);
        }
        return h;
    }
	*/
    
    /*
    // restore red-black tree invariant
    private Node balance(Node h) throws SQLException, Exception {
        assert (h != null);

        if (isRed(h.getRight()))                     			h = rotateLeft(h);
        if (isRed(h.getLeft()) && isRed(h.getLeft().getLeft())) h = rotateRight(h);
        if (isRed(h.getLeft()) && isRed(h.getRight()))     		flipColors(h);

        h.setN(size(h.getLeft()) + size(h.getRight()) + 1);
        return h;
    }
	*/

   /*************************************************************************
    *  Utility functions
    *************************************************************************/

    /**
     * Method to get the height of the tree.
     * @return An int storing the height of the tree.
     * @throws Exception 
     */
    public int height() throws Exception { return height(root); }
    private int height(Node x) throws Exception {
        if (x == null) return 0;
        return 1 + Math.max(height(x.getLeft()), height(x.getRight()));
    }

   /*************************************************************************
    *  Ordered symbol table methods.
    *************************************************************************/

    /**
     * Method to find the smallest key in the table.
     * 
     * @return Key with the smallest value in the table.  Null if empty tree.
     * @throws Exception 
     */
    public Long min() throws Exception {
        if (isEmpty()) return null;
        return min(root).key;
    } 

    // the smallest key in subtree rooted at x; null if no such key
    private Node min(Node x) throws Exception { 
        assert x != null;
        if (x.getLeft() == null) return x; 
        else                return min(x.getLeft()); 
    } 

    /**
     * Method to find the largest key in the table.
     * 
     * @return Key with the largest value in the table.  Null if empty tree.
     * @throws Exception 
     */
    public Long max() throws Exception {
        if (isEmpty()) return null;
        return max(root).key;
    } 

    // the largest key in the subtree rooted at x; null if no such key
    private Node max(Node x) throws Exception { 
        assert x != null;
        if (x.getRight() == null) return x; 
        else                 return max(x.getRight()); 
    } 

    /**
     * Method to find the largest key in the table less than or equal to the given key..
     * 
     * @return Key right below the desired key.  Null if empty tree.
     * @throws Exception 
     */
    public Long floor(Long key) throws Exception {
        Node x = floor(root, key);
        if (x == null) return null;
        else           return x.key;
    }    

    // the largest key in the subtree rooted at x less than or equal to the given key
    private Node floor(Node x, Long key) throws Exception {
        if (x == null) return null;
        int cmp = key.compareTo(x.key);
        if (cmp == 0) return x;
        if (cmp < 0)  return floor(x.getLeft(), key);
        Node t = floor(x.getRight(), key);
        if (t != null) return t; 
        else           return x;
    }

    /**
     * Method to find the smallest key in the table less than or equal to the given key..
     * 
     * @return Key right below the desired key.  Null if empty tree.
     * @throws Exception 
     */
    public Long ceiling(Long key) throws Exception {  
        Node x = ceiling(root, key);
        if (x == null) return null;
        else           return x.key;  
    }

    // the smallest key in the subtree rooted at x greater than or equal to the given key
    private Node ceiling(Node x, Long key) throws Exception {  
        if (x == null) return null;
        int cmp = key.compareTo(x.key);
        if (cmp == 0) return x;
        if (cmp > 0)  return ceiling(x.getRight(), key);
        Node t = ceiling(x.getLeft(), key);
        if (t != null) return t; 
        else           return x;
    }


    // the key of rank k
    public Long select(int k) throws Exception {
        if (k < 0 || k >= size())  return null;
        Node x = select(root, k);
        return x.key;
    }

    // the key of rank k in the subtree rooted at x
    private Node select(Node x, int k) throws Exception {
        assert x != null;
        assert k >= 0 && k < size(x);
        int t = size(x.getLeft()); 
        if      (t > k) return select(x.getLeft(),  k); 
        else if (t < k) return select(x.getRight(), k-t-1); 
        else            return x; 
    } 

    // number of keys less than key
    public int rank(Long key) throws Exception {
        return rank(key, root);
    } 

    // number of keys less than key in the subtree rooted at x
    private int rank(Long key, Node x) throws Exception {
        if (x == null) return 0; 
        int cmp = key.compareTo(x.key); 
        if      (cmp < 0) return rank(key, x.getLeft()); 
        else if (cmp > 0) return 1 + size(x.getLeft()) + rank(key, x.getRight()); 
        else              return size(x.getLeft()); 
    } 

   /***********************************************************************
    *  Range count and range search.
 * @throws Exception 
    ***********************************************************************/

    // all of the keys, as an Iterable
    public Iterable<Long> keys() throws Exception {
        return keys(min(), max());
    }

    // the keys between lo and hi, as an Iterable
    public Iterable<Long> keys(Long lo, Long hi) throws Exception {
        Queue<Long> queue = new Queue<Long>();
        // if (isEmpty() || lo.compareTo(hi) > 0) return queue;
        keys(root, queue, lo, hi);
        return queue;
    } 

    // add the keys between lo and hi in the subtree rooted at x
    // to the queue
    private void keys(Node x, Queue<Long> queue, Long lo, Long hi) throws Exception { 
        if (x == null) return; 
        int cmplo = lo.compareTo(x.key); 
        int cmphi = hi.compareTo(x.key); 
        if (cmplo < 0) keys(x.getLeft(), queue, lo, hi); 
        if (cmplo <= 0 && cmphi >= 0) queue.enqueue(x.key); 
        if (cmphi > 0) keys(x.getRight(), queue, lo, hi); 
    } 

    // number keys between lo and hi
    public int size(Long lo, Long hi) throws Exception {
        if (lo.compareTo(hi) > 0) return 0;
        if (contains(hi)) return rank(hi) - rank(lo) + 1;
        else              return rank(hi) - rank(lo);
    }
}