package org.battelle.clodhopper.tuple;

/**
 * An abstract base class that can be used for implementations of <code>TupleList</code>
 * 
 * @author R. Scarberry
 * @since 1.0
 *
 */
public abstract class AbstractTupleList implements TupleList {

	protected int tupleLength;
	protected int tupleCount;
	
	/**
	 * Constructor.  Subclasses may call this constructor with 0 for both
	 * arguments, but they should later set tupleLength and tupleCount to the
	 * proper values.
	 * 
	 * @param tupleLength
	 * @param tupleCount
	 */
	protected AbstractTupleList(int tupleLength, int tupleCount) {
	    if (tupleLength < 0) {
	        throw new IllegalArgumentException("tupleLength < 0: " + tupleLength);
	    }
	    if (tupleCount < 0) {
	        throw new IllegalArgumentException("tupleCount < 0: "
	                + tupleCount);
	    }
	    this.tupleLength = tupleLength;
	    this.tupleCount = tupleCount;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getTupleCount() {
		return tupleCount;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getTupleLength() {
		return tupleLength;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public double[] getColumn(int col, double[] columnBuffer) {
		checkColumnIndex(col);
		int len = columnBuffer != null ? columnBuffer.length : 0;
		double[] result = len >= tupleCount ? columnBuffer : new double[tupleCount];
		for (int i=0; i<tupleCount; i++) {
			result[i] = getTupleValue(i, col);
		}
		return result;
	}

	/**
	 * Checks the tuple index, throwing an IndexOutOfBoundsException if it is
	 * out of range.
	 * 
	 * @param n
	 */
	protected void checkTupleIndex(int n) {
		if (n < 0 || n >= tupleCount) {
			throw new IndexOutOfBoundsException(String.format("tuple index not in [%d - %d]: %d", 0, tupleCount-1, n));
		}
	}
	
	/**
	 * Checks the column index, throwing an IndexOutOfBoundsException if it is invalid.
	 * @param col
	 */
	protected void checkColumnIndex(int col) {
		if (col < 0 || col >= tupleLength) {
			throw new IndexOutOfBoundsException(String.format("tuple column index not in [%d - %d]: %d", 0, tupleLength-1, col));
		}
	}

	/**
	 * Checks to ensure the specified values array is long enough to hold tuples from this object.
	 * 
	 * @param values
	 */
	protected void checkValuesLength(double[] values) {
		if (values.length < tupleLength) {
			throw new IllegalArgumentException(
					String.format("values array of insufficient length: %d < %d", values.length, tupleLength));
		}
	}
}
