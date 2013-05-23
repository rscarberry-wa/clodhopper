package org.battelle.clodhopper.distance;

/**
 * A class implements <code>DistanceMetric</code> if it is meant to compute distances
 * between pairs of tuples.  All implementations should provide deep-copy clone methods.
 * 
 * @author R. Scarberry
 *
 */
public interface DistanceMetric extends Cloneable {

	/**
	 * Computes the distance between tuple data contained in two arrays of the same length.
	 * 
	 * @param tuple1
	 * @param tuple2
	 * 
	 * @return
	 */
	double distance(double[] tuple1, double[] tuple2);
	
	/**
	 * @return a deep copy of the instance.
	 */
	DistanceMetric clone();
	
}
