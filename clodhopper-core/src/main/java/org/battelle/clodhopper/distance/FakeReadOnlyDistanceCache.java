/*
 * Copyright 2019 randy.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.battelle.clodhopper.distance;

import java.io.IOException;
import java.util.Objects;
import org.battelle.clodhopper.tuple.TupleList;

/**
 * 
 * @author R.Scarberry
 */
public class FakeDistanceCache implements ReadOnlyDistanceCache {
    
    private final TupleList tuples;
    private final DistanceMetric distanceMetric;
    
    public FakeDistanceCache(TupleList tuples, DistanceMetric distanceMetric) {
        Objects.requireNonNull(tuples, "tuples is required");
        Objects.requireNonNull(distanceMetric, "distanceMetric is required");
        this.tuples = tuples;
        this.distanceMetric = distanceMetric;
    }

    @Override
    public int getNumIndices() {
        return tuples.getTupleCount();
    }

    @Override
    public double getDistance(int index1, int index2) {
        return distanceMetric.distance(tuples.getTuple(index1, null), 
                tuples.getTuple(index2, null));
    }

    @Override
    public double[] getDistances(int[] indices1, int[] indices2, double[] distances) throws IOException {
        if (indices1.length != indices2.length) {
            throw new IllegalArgumentException(
                    String.format("indices1.length must equal indices2.length: %d != %d",
                            indices1.length, indices2.length));
        }
        double[] result = distances;
        if (result == null || result.length < indices1.length) {
            result = new double[indices1.length];
        }
        for (int i=0; i<indices1.length; i++) {
            result[i] = getDistance(indices1[i], indices2[i]);
        }
        return result;
    }

    @Override
    public long getNumDistances() {
        long n = tuples.getTupleCount();
        return n*(n-1L)/2L;
    }

    @Override
    public double getDistance(long n) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long distancePos(int index1, int index2) {
        throw new UnsupportedOperationException();
    }
    
}
