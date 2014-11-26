package org.battelle.clodhopper.distance;

import java.io.IOException;

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
 * ReadOnlyDistanceCache.java
 *
 *===================================================================*/
/**
 * <p>A cache storing distances relating pairs of entities denoted by integer
 * indices. Normally used for storing similarity measures such as distances or
 * correlations, but the measures could mean anything as long as they obey these
 * properties:</p>
 * <ul>
 * <li>Indices are numbered from 0 to <code>(getNumIndices() - 1)</code>.
 * <li>For indices i and j, Dij = Dji, where Dij is the distance between indices
 * i and j.
 * <li>Dii = 0.0 for all i in <code>[0 - (getNumIndices() - 1)]</code>.
 * </ul>
 *
 * @author R.Scarberry
 * @since 1.0
 */
public interface ReadOnlyDistanceCache {

    /**
     * Get the number of indices, N. Valid indices for the other methods are
     * then [0 - (N-1)].
     *
     * @return - the number of indices.
     */
    public int getNumIndices();

    /**
     * Get the distance between the entities represented by index1 and index2.
     *
     * @param index1 the first index.
     * @param index2 the second index.
     * @return the distance between the entities identified by the indexes.
     * @throws IOException if an IO error occurs.
     */
    public double getDistance(int index1, int index2) throws IOException;

    /**
     * Get distances in bulk. Element i of the returned array will contain the
     * distance between indices1[i] and indices2[i]. Therefore, indices1 and
     * indices2 must be the same length. If distances is non-null, it must be
     * the same length as indices1 and indices2. If it's null, a new distances
     * array is allocated and returned.
     *
     * @param indices1 the first array of indexes.
     * @param indices2 the second array of indexes.
     * @param distances an array to which, if null and of sufficient length, the 
     *   distances are copied.
     * @return the array of distances which will be the same as the third parameter if
     *   it is non-null and of sufficient length. Otherwise, a new array is returned.
     * @throws IOException if an IO error occurs.
     */
    public double[] getDistances(int[] indices1, int[] indices2, double[] distances) throws IOException;

    /**
     * Get the number of distances which, if <code>N == getNumIndices()</code>,
     * is always equal to <code>N(N-1)/2</code>.
     *
     * @return - the number of pairwise distances.
     */
    public long getNumDistances();

    /**
     * Get distance n, where n is in <code>[0 - (getNumDistances() - 1)]</code>.
     *
     * @param n index of the desired distance.
     * @return the distance
     * @throws IOException if an IO errors occurs.
     */
    public double getDistance(long n) throws IOException;

    /**
     * Get the sequential distance number for the distance identified by index1
     * and index2. Both parameters should be in the range [0 - (getNumIndices()
     * - 1)] and not equal to each other.
     *
     * @param index1 the first index.
     * @param index2 the second index.
     * @return - a distance position in the range [0 - (getNumDistances() - 1)].
     */
    public long distancePos(int index1, int index2);

}
