package org.battelle.clodhopper.distance;

/*=====================================================================
 * 
 *                       CLODHOPPER CLUSTERING API
 * 
 * -------------------------------------------------------------------- 
 * 
 * Copyright (C) 2013 Battelle Memorial Institute 
 * http://www.battelle.org
 * 
 * -------------------------------------------------------------------- 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * -------------------------------------------------------------------- 
 * *
 * RAMDistanceCache.java
 *
 *===================================================================*/
/**
 * An implementation of <code>DistanceCache</code> that maintains all distances
 * in memory.
 *
 * @author R. Scarberry
 * @since 1.0
 *
 */
public class RAMDistanceCache implements DistanceCache {

    /**
     * The maximum number of indices for a RAMDistanceCache. Any higher and
     * mDistances would require a greater length than an int can accommodate.
     */
    public static final int MAX_INDEX_COUNT = 0x10000;

    private final int indexCount;
    private final double[] distances;

    /**
     * Constructor
     *
     * @param indexCount the number of entities for which to maintain distances.
     * The total number of distances is
     * <code>indexCount*(indexCount-1)/2</code>, since Dij == Dji and Dii = 0,
     * for i, j from 0 to (indexCount - 1).
     *
     * @throws IllegalArgumentException if indexCount is negative or greater
     * than MAX_INDEX_COUNT (0x10000). Since the distances are stored in an
     * array, having indexCount above the limit would result in too many
     * distances to hold in an array.
     */
    public RAMDistanceCache(final int indexCount) {
        if (indexCount < 0) {
            throw new IllegalArgumentException("number of indices < 0: " + indexCount);
        }
        if (indexCount > MAX_INDEX_COUNT) {
            throw new IllegalArgumentException("number of indices greater than " + MAX_INDEX_COUNT + ": " + indexCount);
        }
        this.indexCount = indexCount;
        int numDistances = indexCount * (indexCount - 1) / 2;
        this.distances = new double[numDistances];
    }

    /**
     * Constructor
     *
     * @param indexCount the number of entities for which to maintain distances.
     * The total number of distances is
     * <code>indexCount*(indexCount-1)/2</code>, since Dij == Dji and Dii = 0,
     * for i, j from 0 to (indexCount - 1).
     * @param distances an array in which to store the distances. This array
     * must be of length <code>indexCount*(indexCount-1)</code>
     *
     * @throws IllegalArgumentException if indexCount is negative or greater
     * than MAX_INDEX_COUNT (0x10000). Since the distances are stored in an
     * array, having indexCount above the limit would result in too many
     * distances to hold in an array. Also throws this exception if distances is
     * not the proper length.
     */
    RAMDistanceCache(final int indexCount, final double[] distances) {
        if (indexCount < 0) {
            throw new IllegalArgumentException("number of indices < 0: " + indexCount);
        }
        if (indexCount > MAX_INDEX_COUNT) {
            throw new IllegalArgumentException("number of indices greater than " + MAX_INDEX_COUNT + ": " + indexCount);
        }
        this.indexCount = indexCount;
        int numDistances = indexCount * (indexCount - 1) / 2;
        if (distances.length != numDistances) {
            throw new IllegalArgumentException("invalid number of distances: " + distances.length + " != " + numDistances);
        }
        this.distances = distances;
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= indexCount) {
            throw new IllegalArgumentException("index not in [0 - (" + indexCount + " - 1)]: " + index);
        }
    }

    /**
     * Get the number of indices, N. Valid indices for the other methods are
     * then [0 - (N-1)].
     *
     * @return - the number of indices.
     */
    @Override
    public int getNumIndices() {
        return indexCount;
    }

    /**
     * Get the number of distances maintained by this cache.
     */
    @Override
    public long getNumDistances() {
        return (long) distances.length;
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public double getDistance(final long n) {
        return distances[(int) n];
    }

	// Returns the index into mDistances of the distance measure for 
    // index1 and index2.
    private int distanceIndex(int index1, int index2) {
        if (index1 == index2) {
            throw new IllegalArgumentException("indices are equal: " + index1);
        }
        if (index1 > index2) { // Swap them
            index1 ^= index2;
            index2 ^= index1;
            index1 ^= index2;
        }
        int n = indexCount - index1;
        return distances.length - n * (n - 1) / 2 + index2 - index1 - 1;
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public long distancePos(final int index1, final int index2) {
        return (long) distanceIndex(index1, index2);
    }

    @Override
    /**
     * Get the distance between the entities represented by index1 and index2.
     *
     * @param index1
     * @param index2
     * @return
     */
    public double getDistance(final int index1, final int index2) {
        checkIndex(index1);
        checkIndex(index2);
        double d = 0.0;
        if (index1 != index2) {
            d = distances[distanceIndex(index1, index2)];
        }
        return d;
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public double[] getDistances(final int[] indices1, final int[] indices2, final double[] distances) {
        int n = indices1.length;
        if (n != indices2.length) {
            throw new IllegalArgumentException(String.valueOf(n) + " != " + indices2.length);
        }
        double[] d = distances;
        if (distances != null) {
            if (distances.length != n) {
                throw new IllegalArgumentException("distance buffer length not equal to number of indices");
            }
        } else {
            d = new double[n];
        }
        for (int i = 0; i < n; i++) {
            d[i] = getDistance(indices1[i], indices2[i]);
        }
        return d;
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public void setDistance(final int index1, final int index2, final double distance) {
        checkIndex(index1);
        checkIndex(index2);
        if (index1 != index2) {
            distances[distanceIndex(index1, index2)] = distance;
        }
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public void setDistances(final int[] indices1, final int[] indices2, final double[] distances) {
        final int n = indices1.length;
        if (n != indices2.length) {
            throw new IllegalArgumentException(String.valueOf(n) + " != " + indices2.length);
        }
        if (n != distances.length) {
            throw new IllegalArgumentException("distance buffer length not equal to number of indices");
        }
        for (int i = 0; i < n; i++) {
            this.distances[distanceIndex(indices1[i], indices2[i])] = distances[i];
        }
    }

}
