import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Arrays;

import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialException;


public class SubjectBlobArray {
	private final int NODE_SIZE = 16;
    private final int ID_OFFSET = 8;
    private final int ARRAY_OFFSET = 9;
    
    private int subjectsInQueue;
    private long size;
    
    private Blob blob;
    
    public SubjectBlobArray(Blob blob) throws SQLException
    {
    	this.blob = blob;
    	size = getLong(blob.getBytes(1, 8), 0);
    }
    
    public static Blob createSubjectBlob() throws SerialException, SQLException
    {
    	byte[] b = putLong(0L);
    	return (Blob) new SerialBlob(b);
    }
    public long[] getIDs() throws SQLException, IOException
    {
    	long returnSize = size, returnOffset = 0;
    	if (returnSize > (long) Integer.MAX_VALUE)
    	{
    		returnOffset = returnSize - (long) Integer.MAX_VALUE;
    		returnSize = (long) Integer.MAX_VALUE;
    	}
    	long[] IDs = new long[(int) returnSize];
    	InputStream bytes = blob.getBinaryStream((long) ARRAY_OFFSET + returnOffset,
    			returnSize * (long) NODE_SIZE);
		byte[] buffer = new byte[NODE_SIZE * 1000];
		int bufferSize;
		int position = 0;
		do
		{
			bufferSize = bytes.read(buffer);
			for (int i = 0; i < bufferSize; i += NODE_SIZE)
			{
				IDs[position] = getLong(buffer, i + ID_OFFSET);
				position++;
			}
		}
		while(bufferSize != 0);
		
		return IDs;
    }
    
	public long[] getIDs(long startDate, long endDate) throws SQLException, IOException
	{
		if (size == 0)
			return new long[0];
		
		long start, end;
		long[] IDs;
		start = binarySearch(startDate);
		end = binarySearch(endDate);
		if (end != size - 1)
			end++;
		long IDsize = end - start + 1;
		if (IDsize > (long) Integer.MAX_VALUE)
			IDsize = (long) Integer.MAX_VALUE;
		IDs = new long[(int) (IDsize)];
		
		InputStream bytes = blob.getBinaryStream((long) ARRAY_OFFSET + 
				(start * (long) NODE_SIZE), (IDsize) * (long) NODE_SIZE);
		byte[] buffer = new byte[NODE_SIZE * 1000];
		int bufferSize;
		int position = 0;
		do
		{
			bufferSize = bytes.read(buffer);
			for (int i = 0; i < bufferSize; i += NODE_SIZE)
			{
				IDs[position] = getLong(buffer, i + ID_OFFSET);
				position++;
			}
		}
		while(bufferSize != 0);
		
		return IDs;
	}
	
    public void add(long date, long ID) throws SQLException, IOException
    {
    	
    	long insertionPoint;
    	/*System.out.println("size = " + size);
    	if (size != 0)
    	{
    		System.out.println("date = " + date);
    		System.out.println("num = " + getLong(blob.getBytes(ARRAY_OFFSET + (size * (long) NODE_SIZE), 8)));
        	
    	}*/
    	if (size == 0)
    		insertionPoint = 1;
    	
    	else if (date >= getLong(blob.getBytes(ARRAY_OFFSET + (size * (long) NODE_SIZE), 8)))
    		insertionPoint = size + 1;
    		
    	
    	else
    	{
    		insertionPoint = binarySearch(date);
    		InputStream remainingBytes = blob.getBinaryStream((long) ARRAY_OFFSET + 
    				(insertionPoint * (long) NODE_SIZE), (size - insertionPoint) * (long) NODE_SIZE);
    		byte[] buffer = new byte[10000];
    		int bufferSize;
    		for (int counter = 0;;counter++)
    		{
    			bufferSize = remainingBytes.read(buffer);
    			//TODO Check illegal argument
    			if (bufferSize <= 0)
    				break;
    			/*
    			System.out.println("Shifting Remaining Bytes\nStart Byte: " + ((insertionPoint + 1L) * (long) NODE_SIZE + 10000 * counter) +
    					"\nBuffer Size: " + bufferSize);
    			*/
    			blob.setBytes((insertionPoint + 1L) * (long) NODE_SIZE + 10000 * counter, buffer, 0, bufferSize);
    		}
    	}
    	//insert
    	byte[] toBeInserted = new byte[16];
    	putLong(date, toBeInserted, 0);
    	putLong(ID, toBeInserted, 8);
    	if (blob.length() > 1000)
    	{
    		System.out.println("Blob size = " + blob.length());
    		System.out.println(Arrays.toString(blob.getBytes(1, (int)blob.length())));
    	}
    	blob.setBytes(1, putLong(++size));
    	blob.setBytes((long) ARRAY_OFFSET + (insertionPoint * (long) NODE_SIZE), toBeInserted);
    }
    
	private long binarySearch(long date) throws SQLException
	{
		if (size == 0)
			return -1;
		
		long min, max, mid;
		min = 0;
		max = (size - 1) / (long) NODE_SIZE;
		long currentDate;
		while (max >= min)
		{
			mid = (min + max) / 2;
			currentDate = getLong(blob.getBytes((long) ARRAY_OFFSET + (mid * (long) NODE_SIZE), 8));
			if (currentDate < date)
				min = mid + 1;
			else if (currentDate > date)
				max = mid - 1;
			else if (currentDate == date)
				return mid;
		}
		while (max < size && getLong(blob.getBytes((long) ARRAY_OFFSET + (max * (long) NODE_SIZE), 8)) < date)
			max++;
		return max - 1;
	}
	
	private static void putInt(int value, byte[] array, int offset) {
        array[offset]   = (byte)(0xff & (value >> 24));
        array[offset+1] = (byte)(0xff & (value >> 16));
        array[offset+2] = (byte)(0xff & (value >> 8));
        array[offset+3] = (byte)(0xff & value);
    }

    private static int getInt(byte[] array, int offset) {
      return
        ((array[offset]   & 0xff) << 24) |
        ((array[offset+1] & 0xff) << 16) |
        ((array[offset+2] & 0xff) << 8) |
         (array[offset+3] & 0xff);
    }
    private static byte[] putLong(long value) {
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
    private static long getLong(byte[] array) {
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
    private static long getLong(byte[] array, int offset) {
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
}
