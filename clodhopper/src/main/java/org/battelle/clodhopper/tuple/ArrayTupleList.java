package org.battelle.clodhopper.tuple;

import java.io.*;
import java.io.IOException;

/**
 * <p>A simple implementation of <tt>TupleList</tt> which maintains
 * the coordinate data in a one-dimensional array.</p>
 * 
 * @author R. Scarberry
 * @since 1.0
 */
public class ArrayTupleList extends AbstractTupleList {

    private final double[] values;

    /**
     * Constructs a new <tt>ArrayTupleList</tt> with all values initialized to
     * zero.
     * @param tupleLength the length of each tuple
     * @param tupleCount the number of tuples
     */
    public ArrayTupleList(int tupleLength, int tupleCount) {
    	super(tupleLength, tupleCount);
    	this.values = new double[tupleLength * tupleCount];
    }

    /**
     * Constructs a new <tt>ArrayTupleList</tt> using the provided array of 
     * values.  This array is not copied, so any changes made directly to this array
     * will change the data in this tuple list.
     * 
     * @param tupleLength the length of each tuple
     * @param tupleCount the number of tuples
     * @param values an array containing the tuple values, which should have a length at least
     *   tupleLength * tupleCount.
     * 
     * @throws IllegalArgumentException if either tupleLength or tupleCount is negative or if values 
     *   has insufficient length.
     */
    public ArrayTupleList(int tupleLength, int tupleCount,
            double[] values) {
    	super(tupleLength, tupleCount);
    	if (values.length < tupleLength * tupleCount) {
        	throw new IllegalArgumentException(String.format("values.length < %d: %d", tupleLength*tupleCount, values.length));
        }
        this.values = values;
    }

    /**
     * {@inheritDoc}
     */
	@Override
	public void setTuple(int n, double[] values) {
		checkTupleIndex(n);
		checkValuesLength(values);
		System.arraycopy(values, 0, this.values, n*tupleLength, tupleLength);
	}

    /**
     * {@inheritDoc}
     */
	@Override
	public double[] getTuple(int n, double[] reuseBuffer) {
		checkTupleIndex(n);
		double[] result = reuseBuffer != null && reuseBuffer.length >= tupleLength ? reuseBuffer :
			new double[tupleLength];
		System.arraycopy(this.values, n*tupleLength, result, 0, tupleLength);
		return result;
	}
	
    /**
     * {@inheritDoc}
     */
	@Override
	public double getTupleValue(int n, int col) {
		checkTupleIndex(n);
		checkColumnIndex(col);
		return this.values[n*tupleLength + col];
	}

    /**
     * {@inheritDoc}
     */
	@Override
	public double[] getColumn(int col, double[] columnBuffer) {
		checkColumnIndex(col);
		int len = columnBuffer != null ? columnBuffer.length : 0;
		double[] result = len >= tupleCount ? columnBuffer : new double[tupleCount];
		for (int i=0, currentNdx=col; i<tupleCount; i++, currentNdx+=tupleLength) {
			result[i] = this.values[currentNdx];
		}
		return result;
	}

	/**
	 * Loads an instance of of <code>ArrayTupleList</code> from a file containing
	 * binary tuple data. The file format is very simple binary of two ints specifying the
	 * tuple length and tuple count, then the tuple data itself.
	 * 
	 * @param f the file containing the data.
	 * 
	 * @return an <code>ArrayTupleList</code> object
	 * 
	 * @throws IOException if an I/O error occurs.
	 */
	public static ArrayTupleList loadFromFile(File f) throws IOException {
	    ArrayTupleList tuples = null;
	    DataInputStream in = null;
	    try {
	        in = new DataInputStream(new BufferedInputStream(new FileInputStream(f)));
	        int tupleLength = in.readInt();
	        int tupleCount = in.readInt();
	        tuples = new ArrayTupleList(tupleLength, tupleCount);
	        double[] buffer = new double[tupleLength];
	        for (int i=0; i<tupleCount; i++) {
	            for (int j=0; j<tupleLength; j++) {
	                buffer[j] = in.readDouble();
	            }
	            tuples.setTuple(i, buffer);
	        }
	    } finally {
	        if (in != null) {
	            try {
	                in.close();
	            } catch (IOException ioe) {
	            }
	        }
	    }
	    return tuples;
	}
	
	/**
	 * Saves the tuple data to a file in the format that can be reloading using the
	 * <code>loadFromFile</code> method.
	 * 
	 * @param tuples
	 * 
	 * @param f
	 * 
	 * @throws IOException
	 */
	public static void saveToFile(TupleList tuples, File f) throws IOException {
	    final int tupleLength = tuples.getTupleLength();
	    final int tupleCount = tuples.getTupleCount();
	    DataOutputStream out = null;
	    try {
	        out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f)));
	        double[] buffer = new double[tupleLength];
	        out.writeInt(tupleLength);
	        out.writeInt(tupleCount);
	        for (int i=0; i<tupleCount; i++) {
	            tuples.getTuple(i, buffer);
	            for (int j=0; j<tupleLength; j++) {
	                out.writeDouble(buffer[j]);
	            }
	        }
	    } finally {
	        if (out != null) {
	            try {
	                out.close();
	            } catch (IOException ioe) {
	            }
	        }
	    }
	}
}
