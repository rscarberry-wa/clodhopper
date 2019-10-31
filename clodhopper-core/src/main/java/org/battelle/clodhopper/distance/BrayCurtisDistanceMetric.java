package org.battelle.clodhopper.distance;

public class BrayCurtisDistanceMetric implements DistanceMetric {

    @Override
    public double distance(double[] tuple1, double[] tuple2) {

        checkSameLength(tuple1, tuple2);
        final int sz = tuple1.length;

        double s1 = 0.0;
        double s2 = 0.0;

        for (int i=0; i<sz; i++) {
            double v1 = tuple1[i];
            double v2 = tuple2[i];
            s1 += Math.abs(v1 - v2);
            s2 += Math.abs(v1 + v2);
        }

        // Will happen if they're equal.
        if (0.0 == s1) {
            return 0.0;
        }

        return s2 != 0.0 ? s1/s2 : Double.POSITIVE_INFINITY;
    }

    @Override
    public DistanceMetric clone() {
        try {
            return (DistanceMetric) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }
}
