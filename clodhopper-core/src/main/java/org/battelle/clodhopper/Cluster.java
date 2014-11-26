package org.battelle.clodhopper;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.battelle.clodhopper.util.ArrayIntIterator;
import org.battelle.clodhopper.util.IntIterator;

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
 * Cluster.java
 *
 *===================================================================*/
/**
 * This class implements a cluster entity.
 *
 * <p>
 * A Cluster is defined by this class as a set of members indexes paired with a
 * geometrical center. The indexes reference members of the
 * <code>TupleList</code> which was clustered. Instances of this class are
 * immutable.</p>
 *
 * @author R. Scarberry
 * @see org.battelle.clodhopper.tuple.TupleList
 * @since 1.0
 *
 */
public class Cluster {

    // Ids (usually 0-based indexes) of the member tuples of the cluster.
    private final int[] ids;
    // The geometric center of the cluster.
    private final double[] center;

    /**
     * Constructor
     *
     * @param ids an array containing the 0-indexed member identifiers, which
     * cannot be null.
     * @param center the geometric center of the cluster, which also cannot be
     * null.
     */
    public Cluster(final int[] ids, final double[] center) {
        Objects.requireNonNull(ids);
        Objects.requireNonNull(center);
        this.ids = Arrays.copyOf(ids, ids.length);
        this.center = Arrays.copyOf(center, center.length);
        Arrays.sort(this.ids);
    }

    /**
     * Get the length of the cluster center.
     *
     * @return the integer length.
     */
    public int getCenterLength() {
        return center.length;
    }

    /**
     * Get an element of the cluster center.
     *
     * @param n 0-based index of the center element.
     * 
     * @return value of the center element.
     */
    public double getCenterElement(int n) {
        return center[n];
    }

    /**
     * Get the cluster center.
     *
     * @return the cluster center. Since this class is immutable, this method
     * returns a protected copy.
     */
    public double[] getCenter() {
        return (double[]) center.clone();
    }

    /**
     * Get the number of cluster members.
     *
     * @return the number of members in the cluster.
     */
    public int getMemberCount() {
        return ids.length;
    }

    /**
     * Get the index of the nth member.
     *
     * @param n which member to retrieve (0-indexed)
     *
     * @return the member, as a 0-indexed id.
     */
    public int getMember(int n) {
        return ids[n];
    }

    /**
     * Get an iterator over all the members.
     *
     * @return an iterator over the cluster members.
     */
    public IntIterator getMembers() {
        return new ArrayIntIterator(ids);
    }

    /**
     * Get a hash code for the instance.
     *
     * @return the hash code for the cluster instance.
     */
    public int hashCode() {
        return 31 * Arrays.hashCode(ids) + Arrays.hashCode(center);
    }

    /**
     * Test for equality with another object. This method returns true if the
     * other object is also a cluster with the same members and center.
     *
     * @param o reference to an object to which this cluster instance is to be
     * compared for equality.
     *
     * @return true if equal, false otherwise.
     */
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof Cluster) {
            Cluster that = (Cluster) o;
            final int memCount = this.getMemberCount();
            final int centerLen = this.getCenterLength();
            if (memCount != that.getMemberCount() || centerLen != that.getCenterLength()) {
                return false;
            }
	    // Because of the sort of the ids in the constructor, this comparison does not
            // have to worry about the same ids being in a different order.
            return Arrays.equals(this.ids, that.ids) && Arrays.equals(this.center, that.center);
        }
        return false;
    }
}
