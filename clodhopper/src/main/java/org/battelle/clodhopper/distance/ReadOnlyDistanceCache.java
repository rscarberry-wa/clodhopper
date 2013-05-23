package org.battelle.clodhopper.distance;

import java.io.IOException;

/**
 * A cache storing distances relating pairs of entities denoted by integer
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
 * @author R.Scarberry
 * @since 1.0
 */
public interface ReadOnlyDistanceCache {

	/**
	 * Get the number of indices, N.  Valid indices for the other methods are
	 * then [0 - (N-1)].
	 * @return - the number of indices.
	 */
	public int getNumIndices();
	
	/**
	 * Get the distance between the entities represented by index1 and index2.
	 * @param index1
	 * @param index2
	 * @return
	 */
	public double getDistance(int index1, int index2) throws IOException;
	
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
	 * Get the number of distances which, if <code>N == getNumIndices()</code>, is
	 * always equal to <code>N(N-1)/2</code>.  
	 * @return - the number of pairwise distances.
	 */
	public long getNumDistances();
	
	/**
	 * Get distance n, where n is in <code>[0 - (getNumDistances() - 1)]</code>.
	 * @param n
	 * @return
	 * @throws IOException
	 */
	public double getDistance(long n) throws IOException;

	/**
	 * Get the sequential distance number for the distance identified by index1 and index2.
	 * Both parameters should be in the range [0 - (getNumIndices() - 1)] and not equal to 
	 * each other.
	 * @param index1
	 * @param index2
	 * @return - a distance position in the range [0 - (getNumDistances() - 1)].
	 */
	public long distancePos(int index1, int index2);
	
}
