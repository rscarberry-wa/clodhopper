package org.battelle.clodhopper.tuple;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

/**
 * Implements a <code>TupleList</code> backed by a single binary data file.
 * Reads and writes of the data are accomplished via random access of the file.
 * 
 * @author R. Scarberry
 * @since 1.0
 *
 */
public class FileMappedTupleList extends AbstractTupleList {

	private static final Logger logger = Logger.getLogger(FileMappedTupleList.class);
	
	private File file;
	private RandomAccessFile randomAccessFile;
	private ByteBuffer ioBuffer;
	
	/**
	 * Constructor.
	 * 
	 * @param file
	 * @param tupleLength
	 * @param tupleCount
	 * 
	 * @throws IOException
	 */
	protected FileMappedTupleList(File file, int tupleLength, int tupleCount) throws IOException {
		super(tupleLength, tupleCount);
		if (file == null) {
			throw new NullPointerException();
		}
		this.file = file;
		initEmptyFile();
		open();
	}
	
	/**
	 * Constructor.
	 * 
	 * @param file
	 * @throws IOException
	 */
	protected FileMappedTupleList(File file) throws IOException {
		super(0, 0);
		if (file == null) {
			throw new NullPointerException();
		}
		this.file = file;
		open();
	}
	
	/**
	 * Factory method that creates a new <code>TupleList</code> backed by the specified file.
	 * 
	 * @param file
	 * @param tupleLength
	 * @param tupleCount
	 * @return
	 * @throws IOException
	 */
	public static FileMappedTupleList createNew(File file, int tupleLength, int tupleCount)
		throws IOException {
		return new FileMappedTupleList(file, tupleLength, tupleCount);
	}
	
	/**
	 * Factory method that opens a <code>TupleList</code> backed by an existing data file.
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static FileMappedTupleList openExisting(File file) throws IOException {
		return new FileMappedTupleList(file);
	}
	
	public File getFile() {
	    return file;
	}
	
	public static boolean validateFile(File f) throws IOException {
	    if (f.exists() && f.isFile()) {
	        DataInputStream in = null;
	        try {
	            in = new DataInputStream(new FileInputStream(f));
	            int tupleLen = in.readInt();
	            int tupleCount = in.readInt();
	            long expectedFileLen = 8L*((long) tupleLen) * tupleCount;
	            return f.length() == expectedFileLen;
	        } finally {
	            if (in != null) {
	                try {
	                    in.close();
	                } catch (IOException e) {
	                }
	            }
	        }
	    }
	    return false;
	}
	
	private void initEmptyFile() throws IOException {
		DataOutputStream out = null;
		try {
			out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(this.file)));
			out.writeInt(this.tupleLength);
			out.writeInt(this.tupleCount);
			for (int i=0; i<this.tupleCount; i++) {
				for (int j=0; j<this.tupleLength; j++) {
					out.writeDouble(0.0);
				}
			}
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					logger.error("error closing output stream", e);
				}
			}
		}
	}
	
	public synchronized boolean isOpen() {
		return randomAccessFile != null;
	}
	
	public synchronized void open() throws IOException {
		if (!isOpen()) {
			boolean ok = false;
			try {
				randomAccessFile = new RandomAccessFile(this.file, "rw");
				int tlen = randomAccessFile.readInt();
				int tcount = randomAccessFile.readInt();
				ioBuffer = ByteBuffer.allocate(8*tlen);
				this.tupleLength = tlen;
				this.tupleCount = tcount;
				ok = true;
			} finally {
				if (!ok) {
					try {
					  close();
					} catch (IOException e) {
						logger.error("error closing output stream", e);
					}
				}
			}
		}
	}
	
	public synchronized void close() throws IOException {
		if (isOpen()) {
			randomAccessFile.close();
			randomAccessFile = null;
			ioBuffer = null;
		}
	}

	@Override
	public void setTuple(int n, double[] values) {
		checkTupleIndex(n);
		checkValuesLength(values);
		checkOpen();
		try {
			randomAccessFile.seek(filePos(n));
			ioBuffer.clear();
			for (int i=0; i<this.tupleLength; i++) {
				ioBuffer.putDouble(values[i]);
			}
			ioBuffer.flip();
			randomAccessFile.getChannel().write(ioBuffer);
		} catch (IOException e) {
			logger.error("error setting tuple values", e);			
		}
	}

	@Override
	public double[] getTuple(int n, double[] reuseBuffer) {
		checkTupleIndex(n);
		checkOpen();
		double[] result = reuseBuffer != null && reuseBuffer.length >= tupleLength ? reuseBuffer :
			new double[tupleLength];
		try {
			randomAccessFile.seek(filePos(n));
			ioBuffer.clear();
			randomAccessFile.getChannel().read(ioBuffer);
			ioBuffer.flip();
			for (int i=0; i<this.tupleLength; i++) {
				result[i] = ioBuffer.getDouble();
			}
		} catch (IOException e) {
			logger.error("error reading tuple values", e);			
		}
		return result;
	}

	@Override
	public double getTupleValue(int n, int col) {
		checkTupleIndex(n);
		checkColumnIndex(col);
		checkOpen();
		try {
			randomAccessFile.seek(filePos(n) + 8L*col);
			return randomAccessFile.readDouble();
		} catch (IOException e) {
			logger.error("error reading tuple value", e);			
		}
		return 0.0;
	}

    // Ensures file is open.
    private void checkOpen() {
    	if (!isOpen()) {
    		throw new IllegalStateException("not open");
    	}
    }
    
    private long filePos(int n) {
    	return 8L + n * ioBuffer.capacity();
    }
    
    protected void finalize() {
    	try {
    		close();
    	} catch (IOException e) {
    		logger.error("error closing file", e);
    	}
    }
}
