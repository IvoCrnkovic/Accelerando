/**
 * Serializable implementation of a left-leaning Red Black search tree
 * 
 */
public class RBBST <Key extends Comparable<Key>, Value> implements java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5093189298076977124L;

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

    /**
     * BST helper node data type.
     * 
     *
     */
    private class Node implements java.io.Serializable{
    	/**
    	 *Serial ID number that eclipse told us to use for serialization.  Don't really know what it does.
    	 *@see Java.io.Serializable
    	 */
		
    	/**
    	 * Key type
    	 */
    	private Key key;
    	
    	/**
    	 * The data associated with a given key.
    	 */
        private Value val;
        
        /**
         * Pointers to the left and right subtrees.
         */
        private Node left, right;
        
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
        public Node(Key key, Value val, boolean color, int N) {
            this.key = key;
            this.val = val;
            this.color = color;
            this.N = N;
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
     */
    public Value get(Key key) { return get(root, key); }
    
    private Value get(Node x, Key key) {
        while (x != null) {
            int cmp = key.compareTo(x.key);
            if      (cmp < 0) x = x.left;
            else if (cmp > 0) x = x.right;
            else              return x.val;
        }
        return null;
    }

    /**
     * Method to determine if there is data associated with a given key.
     * 
     * @param key The key to look for data attached to.
     * @return True if there is data.
     */
    public boolean contains(Key key) {
        return (get(key) != null);
    }

    // is there a key-value pair with the given key in the subtree rooted at x?
    private boolean contains(Node x, Key key) {
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
	 */
    public void put(Key key, Value val) {
        root = put(root, key, val);
        root.color = BLACK;
    }

    // insert the key-value pair in the subtree rooted at h
    private Node put(Node h, Key key, Value val) { 
        if (h == null) return new Node(key, val, RED, 1);

        int cmp = key.compareTo(h.key);
        if      (cmp < 0) h.left  = put(h.left,  key, val); 
        else if (cmp > 0) h.right = put(h.right, key, val); 
        else              h.val   = val;

        // fix-up any right-leaning links
        if (isRed(h.right) && !isRed(h.left))      h = rotateLeft(h);
        if (isRed(h.left)  &&  isRed(h.left.left)) h = rotateRight(h);
        if (isRed(h.left)  &&  isRed(h.right))     flipColors(h);
        h.N = size(h.left) + size(h.right) + 1;

        return h;
    }

   /*************************************************************************
    *  Red-black deletion
    *************************************************************************/

    /**
     * Method to implement Red-Black deletion of the minimum key.
     * <p>
     * Deletes the key-value pair with the minimum valued key
     */
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


    /**
     * Method to implement Red-Black deletion of the maximum key.
     * <p>
     * Deletes the key-value pair with the maximum valued key
     */
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

    /**
     * Method to implement Red-Black deletion of a given key.
     * <p>
     * Deletes the key-value pair with a given key
     */
    public void delete(Key key) { 
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
    private Node delete(Node h, Key key) { 
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

   /*************************************************************************
    *  red-black tree helper functions
    *************************************************************************/

    // make a left-leaning link lean to the right
    private Node rotateRight(Node h) {
        assert (h != null) && isRed(h.left);
        Node x = h.left;
        h.left = x.right;
        x.right = h;
        x.color = x.right.color;
        x.right.color = RED;
        x.N = h.N;
        h.N = size(h.left) + size(h.right) + 1;
        return x;
    }

    // make a right-leaning link lean to the left
    private Node rotateLeft(Node h) {
        assert (h != null) && isRed(h.right);
        Node x = h.right;
        h.right = x.left;
        x.left = h;
        x.color = x.left.color;
        x.left.color = RED;
        x.N = h.N;
        h.N = size(h.left) + size(h.right) + 1;
        return x;
    }

    // flip the colors of a node and its two children
    private void flipColors(Node h) {
        // h must have opposite color of its two children
        assert (h != null) && (h.left != null) && (h.right != null);
        assert (!isRed(h) &&  isRed(h.left) &&  isRed(h.right))
            || (isRed(h)  && !isRed(h.left) && !isRed(h.right));
        h.color = !h.color;
        h.left.color = !h.left.color;
        h.right.color = !h.right.color;
    }

    // Assuming that h is red and both h.left and h.left.left
    // are black, make h.left or one of its children red.
    private Node moveRedLeft(Node h) {
        assert (h != null);
        assert isRed(h) && !isRed(h.left) && !isRed(h.left.left);

        flipColors(h);
        if (isRed(h.right.left)) { 
            h.right = rotateRight(h.right);
            h = rotateLeft(h);
            // flipColors(h);
        }
        return h;
    }

    // Assuming that h is red and both h.right and h.right.left
    // are black, make h.right or one of its children red.
    private Node moveRedRight(Node h) {
        assert (h != null);
        assert isRed(h) && !isRed(h.right) && !isRed(h.right.left);
        flipColors(h);
        if (isRed(h.left.left)) { 
            h = rotateRight(h);
            // flipColors(h);
        }
        return h;
    }

    // restore red-black tree invariant
    private Node balance(Node h) {
        assert (h != null);

        if (isRed(h.right))                      h = rotateLeft(h);
        if (isRed(h.left) && isRed(h.left.left)) h = rotateRight(h);
        if (isRed(h.left) && isRed(h.right))     flipColors(h);

        h.N = size(h.left) + size(h.right) + 1;
        return h;
    }


   /*************************************************************************
    *  Utility functions
    *************************************************************************/

    /**
     * Method to get the height of the tree.
     * @return An int storing the height of the tree.
     */
    public int height() { return height(root); }
    private int height(Node x) {
        if (x == null) return 0;
        return 1 + Math.max(height(x.left), height(x.right));
    }

   /*************************************************************************
    *  Ordered symbol table methods.
    *************************************************************************/

    /**
     * Method to find the smallest key in the table.
     * 
     * @return Key with the smallest value in the table.  Null if empty tree.
     */
    public Key min() {
        if (isEmpty()) return null;
        return min(root).key;
    } 

    // the smallest key in subtree rooted at x; null if no such key
    private Node min(Node x) { 
        assert x != null;
        if (x.left == null) return x; 
        else                return min(x.left); 
    } 

    /**
     * Method to find the largest key in the table.
     * 
     * @return Key with the largest value in the table.  Null if empty tree.
     */
    public Key max() {
        if (isEmpty()) return null;
        return max(root).key;
    } 

    // the largest key in the subtree rooted at x; null if no such key
    private Node max(Node x) { 
        assert x != null;
        if (x.right == null) return x; 
        else                 return max(x.right); 
    } 

    /**
     * Method to find the largest key in the table less than or equal to the given key..
     * 
     * @return Key right below the desired key.  Null if empty tree.
     */
    public Key floor(Key key) {
        Node x = floor(root, key);
        if (x == null) return null;
        else           return x.key;
    }    

    // the largest key in the subtree rooted at x less than or equal to the given key
    private Node floor(Node x, Key key) {
        if (x == null) return null;
        int cmp = key.compareTo(x.key);
        if (cmp == 0) return x;
        if (cmp < 0)  return floor(x.left, key);
        Node t = floor(x.right, key);
        if (t != null) return t; 
        else           return x;
    }

    /**
     * Method to find the smallest key in the table less than or equal to the given key..
     * 
     * @return Key right below the desired key.  Null if empty tree.
     */
    public Key ceiling(Key key) {  
        Node x = ceiling(root, key);
        if (x == null) return null;
        else           return x.key;  
    }

    // the smallest key in the subtree rooted at x greater than or equal to the given key
    private Node ceiling(Node x, Key key) {  
        if (x == null) return null;
        int cmp = key.compareTo(x.key);
        if (cmp == 0) return x;
        if (cmp > 0)  return ceiling(x.right, key);
        Node t = ceiling(x.left, key);
        if (t != null) return t; 
        else           return x;
    }


    // the key of rank k
    public Key select(int k) {
        if (k < 0 || k >= size())  return null;
        Node x = select(root, k);
        return x.key;
    }

    // the key of rank k in the subtree rooted at x
    private Node select(Node x, int k) {
        assert x != null;
        assert k >= 0 && k < size(x);
        int t = size(x.left); 
        if      (t > k) return select(x.left,  k); 
        else if (t < k) return select(x.right, k-t-1); 
        else            return x; 
    } 

    // number of keys less than key
    public int rank(Key key) {
        return rank(key, root);
    } 

    // number of keys less than key in the subtree rooted at x
    private int rank(Key key, Node x) {
        if (x == null) return 0; 
        int cmp = key.compareTo(x.key); 
        if      (cmp < 0) return rank(key, x.left); 
        else if (cmp > 0) return 1 + size(x.left) + rank(key, x.right); 
        else              return size(x.left); 
    } 

   /***********************************************************************
    *  Range count and range search.
    ***********************************************************************/

    // all of the keys, as an Iterable
    public Iterable<Key> keys() {
        return keys(min(), max());
    }

    // the keys between lo and hi, as an Iterable
    public Iterable<Key> keys(Key lo, Key hi) {
        Queue<Key> queue = new Queue<Key>();
        // if (isEmpty() || lo.compareTo(hi) > 0) return queue;
        keys(root, queue, lo, hi);
        return queue;
    } 

    // add the keys between lo and hi in the subtree rooted at x
    // to the queue
    private void keys(Node x, Queue<Key> queue, Key lo, Key hi) { 
        if (x == null) return; 
        int cmplo = lo.compareTo(x.key); 
        int cmphi = hi.compareTo(x.key); 
        if (cmplo < 0) keys(x.left, queue, lo, hi); 
        if (cmplo <= 0 && cmphi >= 0) queue.enqueue(x.key); 
        if (cmphi > 0) keys(x.right, queue, lo, hi); 
    } 

    // number keys between lo and hi
    public int size(Key lo, Key hi) {
        if (lo.compareTo(hi) > 0) return 0;
        if (contains(hi)) return rank(hi) - rank(lo) + 1;
        else              return rank(hi) - rank(lo);
    }
}