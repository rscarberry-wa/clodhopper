package org.battelle.clodhopper;

import java.util.Arrays;

import org.battelle.clodhopper.util.ArrayIntIterator;
import org.battelle.clodhopper.util.IntIterator;

/**
 * This class implements a cluster entity.
 * 
 * <p>A Cluster is defined by this class as a set of members indexes paired with a 
 * geometrical center.  The indexes reference members of the <code>TupleList</code>
 * which was clustered.  Instances of this class are immutable.</p>
 * 
 * @author R. Scarberry
 * @see org.battelle.clodhopper.tuples.TupleList
 * @since 1.0
 *
 */
public class Cluster {

	private int[] ids;
	private double[] center;
	
	/**
	 * Constructor
	 * 
	 * @param ids an array containing the 0-indexed member identifiers.
	 * @param center the geometric center of the cluster.
	 */
	public Cluster(int[] ids, double[] center) {
		if (ids == null || center == null) throw new NullPointerException();
		Arrays.sort(ids);
		this.ids = ids;
		this.center = center;
	}
	
	/**
	 * Get the length of the cluster center.
	
	 * @return the integer length.
	 */
	public int getCenterLength() {
		return center.length;
	}
	
	/**
	 * Get an element of the cluster center.
	 * 
	 * @param n
	 * @return
	 */
	public double getCenterElement(int n) {
		return center[n];
	}
	
	/**
	 * Get the cluster center.
	 * 
	 * @return the cluster center.  Since this class is immutable, this method
	 *   returns a protected copy.
	 */
	public double[] getCenter() {
		return (double[]) center.clone();
	}
	
	/**
	 * Get the number of cluster members.
	 * 
	 * @return
	 */
	public int getMemberCount() {
		return ids.length;
	}
	
	/**
	 * Get the index of the nth member.
	 * 
	 * @param n which member to retrieve (0-indexed)
	 * 
	 * @return the member, as a 0-indexed id.
	 */
	public int getMember(int n) {
		return ids[n];
	}
	
	/**
	 * Get an iterator over all the members.
	 * 
	 * @return
	 */
	public IntIterator getMembers() {
		return new ArrayIntIterator(ids);
	}
	
	/**
	 * Get a hash code for the instance.
	 */
	public int hashCode() {
		int hc = 17;
		for (int i=0; i<ids.length; i++) {
			hc = 37*hc + ids[i];
		}
		for (int i=0; i<center.length; i++) {
			long bits = Double.doubleToLongBits(center[i]);
			hc = 37*hc + (int) (bits ^ (bits >>> 32));
		}
		return hc;
	}
	
	/**
	 * Test for equality with another object.  This method returns true if the
	 * other object is also a cluster with the same members and center.
	 */
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o instanceof Cluster) {
			Cluster that = (Cluster) o;
			final int memCount = this.getMemberCount();
			final int centerLen = this.getCenterLength();
			if (memCount != that.getMemberCount() || centerLen != that.getCenterLength()) {
				return false;
			}
			// Because of the sort of the ids in the constructor, this comparison does not
			// have to worry about the same ids being in a different order.
			for (int i=0; i<memCount; i++) {
				if (this.getMember(i) != that.getMember(i)) {
					return false;
				}
			}
			for (int i=0; i<centerLen; i++) {
				if (this.getCenterElement(i) != that.getCenterElement(i)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
}
