package org.battelle.clodhopper.distance;

/**
 * A <code>DistanceMetric</code> implementation for computing euclidean.  The
 * data from which distances are computed by this class may not contain NaNs.
 * 
 * @author R. Scarberry
 * @since 1.0
 *
 */
public class EuclideanDistanceMetric implements DistanceMetric {

	/**
	 * {@inheritDoc}
	 */
	public double distance(double[] tuple1, double[] tuple2) {
		// Holds the squared distance.
		double d2 = 0;
		// tuple1.length should be the same as tuple2.length.
		final int len = tuple1.length;
		for (int i=0; i<len; i++) {
			double d = tuple1[i] - tuple2[i];
			d2 += d*d;
		}
		return Math.sqrt(d2);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public DistanceMetric clone() {
		try {
			return (DistanceMetric) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError();
		}
	}

}
