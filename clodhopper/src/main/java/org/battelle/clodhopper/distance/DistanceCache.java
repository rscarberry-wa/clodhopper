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
 * A cache for storing measurements relating pairs of entities denoted by integer
 * indices.  Normally used for storing similarity measures such as distances
 * or correlations, but the measures could mean anything as long as they obey
 * these properties:</p>
 * assumptions:
 * <ul>
 * <li>Indices are numbered from 0 to <code>(getNumIndices() - 1)</code>.
 * <li>For indices i and j, Dij = Dji, where Dij is the distance between 
 *     indices i and j.
 * <li>Dii = 0.0 for all i in <code>[0 - (getNumIndices() - 1)]</code>.
 * </ul>
 * @author R. Scarberry
 * @since 1.0
 */
public interface DistanceCache extends ReadOnlyDistanceCache {

	
	/**
	 * Get distances in bulk.  Element i of the returned array will contain the
	 * distance between indices1[i] and indices2[i].  Therefore, indices1 and indices2
	 * must be the same length.  If distances is non-null, it must be the same length
	 * as indices1 and indices2.  If it's null, a new distances array is allocated and
	 * returned.
	 * @param indices1
	 * @param indices2
	 * @param distances
	 * @return
	 */
	public double[] getDistances(int[] indices1, int[] indices2, double[] distances) throws IOException;
	
	/**
	 * Set the distance between the identities identified by index1 and index2.
	 * @param index1
	 * @param index2
	 * @param distance
	 */
	public void setDistance(int index1, int index2, double distance) throws IOException;
	
	/**
	 * Set distances in bulk.  All three arrays must be the same length.
	 * @param indices1
	 * @param indices2
	 * @param distances
	 */
	public void setDistances(int[] indices1, int[] indices2, double[] distances) throws IOException;

}
