package org.battelle.clodhopper.distance;

/**
 * Instances of this class compute Manhattan distances from data not containing NaNs.  
 * This type of distance metric is also called Taxicab or L1 distances.
 * 
 * @author R. Scarberry
 * @since 1.0
 *
 */
public class ManhattanDistanceMetric implements DistanceMetric {

	@Override
	/**
	 * {@inheritDoc}
	 */
	public double distance(double[] tuple1, double[] tuple2) {
		double d = 0;
		final int len = tuple1.length;
		for (int i=0; i<len; i++) {
			d += Math.abs(tuple1[i] - tuple2[i]);
		}
		return d;
	}

	@Override
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
