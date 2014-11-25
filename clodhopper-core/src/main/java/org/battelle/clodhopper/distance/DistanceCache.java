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
 * DistanceCache.java
 *
 *===================================================================*/
/**
 * Extends <code>ReadOnlyDistanceCache</code> with methods for setting distances.
 * 
 * @author R. Scarberry
 * @since 1.0
 */
public interface DistanceCache extends ReadOnlyDistanceCache {

    /**
     * Set the distance between the identities identified by index1 and index2.
     *
     * @param index1
     * @param index2
     * @param distance
     */
    public void setDistance(int index1, int index2, double distance) throws IOException;

    /**
     * Set distances in bulk. All three arrays must be the same length.
     *
     * @param indices1
     * @param indices2
     * @param distances
     */
    public void setDistances(int[] indices1, int[] indices2, double[] distances) throws IOException;

}
