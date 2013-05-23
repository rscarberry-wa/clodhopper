package org.battelle.clodhopper.tuple;

/**
 * A <code>TupleList</code> is a container for numeric data comprised of a number
 * of fixed length tuples (or vectors).
 * 
 * @author R. Scarberry
 * @since 1.0
 *
 */
public interface TupleList {

	/**
	 * Get the number of tuples.
	 * 
	 * @return
	 */
	int getTupleCount();
	
	/**
	 * Get the length of the tuples.
	 * 
	 * @return
	 */
	int getTupleLength();
	
	/**
	 * Set the values of a tuple.
	 * 
	 * @param n the 0-indexed identifier of the tuple.
	 * @param values an array containing the values.
	 * 
	 * @throws IndexOutOfBoundsException if n is outside the range [0 - (getTupleCount() - 1)]
	 * @throws IllegalArgumentException if values.length is less than getTupleLength().
	 */
	void setTuple(int n, double[] values);
	
	/**
	 * Get the values for a tuple.
	 * 
	 * @param n the 0-indexed identifier of the tuple.
	 * @param reuseBuffer an array into which to copy the values. If null, this method 
	 *   allocates and returns a new array containing the values.
	 *   
	 * @return the array containing the values.
	 */
	double[] getTuple(int n, double[] reuseBuffer);
	
	/**
	 * Get the values for the tuple column.
	 * 
	 * @param col 0-indexed column number.
	 * @param columnBuffer an array in which to copy the values.  If null, a new array
	 *   is allocated and returned.
	 *   
	 * @return the array containing the column values.  This array will be of length getTupleCount().
	 * 
	 * @throws IndexOutOfBoundsException if col is outside the range [0 - (getTupleLength() - 1)]
	 */
	double[] getColumn(int col, double[] columnBuffer);
	
	/**
	 * Get the value for a single tuple cell.
	 * 
	 * @param n the 0-indexed identifier of the tuple.
	 * @param col 0-indexed column number.
	 * 
	 * @return the double value
	 * 
	 * @throws IndexOutOfBoundsException if either n or col are out of range.
	 */
	double getTupleValue(int n, int col);
	
}
