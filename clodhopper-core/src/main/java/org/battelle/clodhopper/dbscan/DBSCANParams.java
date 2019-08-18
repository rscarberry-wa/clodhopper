package org.battelle.clodhopper.dbscan;

import org.battelle.clodhopper.distance.DistanceMetric;
import org.battelle.clodhopper.distance.EuclideanDistanceMetric;

import java.util.Objects;

public class DBSCANParams {

    private final double epsilon;
    private final int minSamples;
    // The distance metric to use for finding nearest neighbors.
    private final DistanceMetric distanceMetric;

    public DBSCANParams() {
        this(0.5, 5, new EuclideanDistanceMetric());
    }

    public DBSCANParams(double epsilon, int minSamples, DistanceMetric distanceMetric) {
        if (Double.isNaN(epsilon) || epsilon <= 0.0) {
            throw new IllegalArgumentException("epsilon must be > 0.0: " + epsilon);
        }
        if (minSamples <= 0) {
            throw new IllegalArgumentException("minSamples must be > 0: " + minSamples);
        }
        Objects.requireNonNull(distanceMetric);
        this.epsilon = epsilon;
        this.minSamples = minSamples;
        this.distanceMetric = distanceMetric;
    }

    public double getEpsilon() {
        return epsilon;
    }

    public int getMinSamples() {
        return minSamples;
    }

    public DistanceMetric getDistanceMetric() {
        return distanceMetric;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DBSCANParams that = (DBSCANParams) o;
        return Double.compare(that.epsilon, epsilon) == 0 &&
                minSamples == that.minSamples &&
                Objects.equals(distanceMetric, that.distanceMetric);
    }

    @Override
    public int hashCode() {
        return Objects.hash(epsilon, minSamples, distanceMetric);
    }
}