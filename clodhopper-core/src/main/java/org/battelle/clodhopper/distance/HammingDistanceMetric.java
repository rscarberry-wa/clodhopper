package org.battelle.clodhopper.distance;

/**
 * Computes a normalized Hamming distance between two tuple instances.
 * This is the number of element-by-element comparisons that are not approximately
 * equal divided by the number of elements. Therefore, values returned by this
 * distance metric always fall within [0 - 1].
 * <p>
 *     Testing for equality of elements does not use exact comparisons
 *     unless the epsilon used is 0.0.
 * </p>
 * <p>
 *     Note: Hamming distances that are not normalized to [0-1] are popularly used
 *     to compare strings. See: https://en.wikipedia.org/wiki/Hamming_distance
 * </p>
 */
public class HammingDistanceMetric implements DistanceMetric {

    public static final double DEFAULT_EPSILON = Math.ulp(1.0);

    private final double epsilon;

    /**
     * Constructor
     * @param epsilon used for element by element equality tests. Typically this
     *                should be a small positive value.
     */
    public HammingDistanceMetric(double epsilon) {
        this.epsilon = Math.abs(epsilon);
    }

    /**
     * Default constructor which uses an epsilon equal to ULP(1.0)
     */
    public HammingDistanceMetric() {
        this(DEFAULT_EPSILON);
    }

    @Override
    public double distance(double[] tuple1, double[] tuple2) {
        checkSameLength(tuple1, tuple2);
        final int sz = tuple1.length;
        int diffCount = 0;
        for (int i=0; i<sz; i++) {
            if (different(tuple1[i], tuple2[i])) {
                diffCount++;
            }
        }
        return ((double) diffCount)/sz;
    }

    private boolean different(double v1, double v2) {
        if (Double.compare(v1, v2) == 0) {
            return false;
        }
        return Math.abs(v1 - v2) > epsilon;
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
