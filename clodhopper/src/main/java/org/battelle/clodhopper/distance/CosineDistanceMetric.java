package org.battelle.clodhopper.distance;

import org.battelle.clodhopper.tuple.TupleMath;

/**
 * Computes cosine distance between pairs of tuples not containing NaNs.  The cosine
 * distance is derived from the Euclidean dot product formula.  For two tuples A and B, 
 * the cosine distance is the product of the lengths of the tuples and the cosine of the
 * angle between them.
 * 
 * @author R. Scarberry
 * @since 1.0
 */
public class CosineDistanceMetric implements DistanceMetric {

	/**
	 * {@inheritDoc}
	 */
	public double distance(double[] tuple1, double[] tuple2) {
		
        // The maximum of the absolute values of all the tuple values.
        double maxA = Math.max(
                TupleMath.absMaximum(tuple1),
                TupleMath.absMaximum(tuple2)); 
        
        final int len = tuple1.length;

        double cosine = 1;
        double sx = 0, sy = 0, sxy = 0;

        if (maxA > 0.0) {
            for (int i=0; i<len; i++) {
                double dx = tuple1[i]/maxA;
                double dy = tuple2[i]/maxA;
                sx += dx*dx;
                sy += dy*dy;
                sxy += dx*dy;
            }
            if (sxy != 0.0) {
                cosine = sxy/Math.sqrt(sx*sy);
            }
        }

        return 1.0 - Math.abs(cosine);
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
