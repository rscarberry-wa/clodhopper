package org.battelle.clodhopper.distance;

/**
 * Implementation of the Tanimoto distance metric.
 * 
 * @author R. Scarberry
 * @since 1.0
 *
 */
public class TanimotoDistanceMetric implements DistanceMetric {

	@Override
	/**
	 * {@inheritDoc}
	 */
	public double distance(double[] tuple1, double[] tuple2) {
		final int len = tuple1.length;
		double snum = 0.0;
		double sdenom = 0.0;
		for (int i=0; i<len; i++) {
			double x = tuple1[i];
			double y = tuple2[i];
			double xy = x*y;
			snum += xy;
			sdenom += (x*x + y*y - xy);
		}
		return sdenom != 0.0 ? 1.0 - snum/sdenom : 0.0;
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
