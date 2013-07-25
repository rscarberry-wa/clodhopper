package org.battelle.clodhopper.examples.tuple;

import org.battelle.clodhopper.tuple.*;

/**
 * This class demonstrates how to package a 2-dimensional array of data as
 * a TupleList, so that ClodHopper's clustering algorithm can process it.
 * 
 * @author R. Scarberry
 * @since 1.0.1
 *
 */
public class Array2DTupleList extends AbstractTupleList {

  private double[][] data;
  
  /**
   * Constructor
   * 
   * @param data a 2-D array, where data.length is the number of tuples, and the
   *   length of any of the elements data[i] is the tuple length.  This array must be
   *   non-null, of length greater than 0, and each element data[i] must be the same length.
   */
  public Array2DTupleList(double[][] data) {
      // Call parent constructor with the tupleCount and the tupleLength.
      // This code assume data is non-null and data.length > 0.  It also assumes
      // all data[i].length == data[0].length for all i != 0.
      super(data.length, data[0].length);
      this.data = data;
  }

  /**
   * Set the values for tuple number n.  The values are copied in from the supplied
   * buffer.
   */
  @Override
  public void setTuple(int n, double[] values) {
    System.arraycopy(values, 0, data[n], 0, tupleLength);
  }

  /**
   * Fetch values into a buffer.  If you pass in a null buffer, or a buffer of unsufficient length,
   * a new buffer is instantiated and returned with the values.
   */
  @Override
  public double[] getTuple(int n, double[] reuseBuffer) {
    double[] buffer = (reuseBuffer != null && reuseBuffer.length >= tupleLength) ? reuseBuffer : new double[tupleLength];
    System.arraycopy(data[n], 0, buffer, 0, tupleLength);
    return buffer;
  }

  /**
   * Get an individual tuple element.
   */
  @Override
  public double getTupleValue(int n, int col) {
    return data[n][col];
  }
  
}
