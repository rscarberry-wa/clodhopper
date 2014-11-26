package org.battelle.clodhopper.tuple;

import gnu.trove.list.array.TIntArrayList;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

import org.battelle.clodhopper.distance.DistanceMetric;
import org.battelle.clodhopper.util.IntComparator;
import org.battelle.clodhopper.util.Sorting;

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
 * TupleKDTree.java
 *
 *===================================================================*/
/**
 * Represents a KD-Tree for points represented as tuples within a
 * <code>TupleList</code>.
 *
 * @author R. Scarberry
 * @since 1.0.1
 *
 */
public class TupleKDTree {

    // All points stored in this kd-tree must be from this tuple list.
    private final TupleList tuples;
    // The distance metric used for any distance computations, such as
    // those used for computing nearest neighbors.
    private final DistanceMetric distanceMetric;
    // The maximum index for tuples that can be added to this kd-tree.
    // This is equal to the tuple count minus 1.
    private int maxNdx;

    // Contains the indexes of tuples that have been added. -1 means empty.
    private int[] nodes;
    // lefts and rights correspond 1:1 with elements of nodes. If nodes[n] contains a tuple index,
    // lefts[n] contains an index into nodes of the left child tuple. rights[n] contains the index into
    // nodes of the right child tuple. -1 indicates no child set.
    private int[] lefts;
    private int[] rights;

    // Number of tuples that have been added.
    private int count;

    /**
     * Constructor
     * 
     * @param tuples the <code>TupleList</code> which contains the data source for the kd-tree.
     * @param distanceMetric the <code>DistanceMetric</code> to use.
     */
    public TupleKDTree(final TupleList tuples, final DistanceMetric distanceMetric) {
        if (tuples == null || distanceMetric == null) {
            throw new NullPointerException();
        }
        this.tuples = tuples;
        this.distanceMetric = distanceMetric;
        maxNdx = tuples.getTupleCount() - 1;
        ensureCapacity(100);
    }

    /**
     * Returns the root node for object-oriented traversal.
     * 
     * @return the root node. 
     */
    public KDNode getRoot() {
        if (count > 0) {
            return new KDNode(0, 0);
        }
        return null;
    }

    /**
     * Get the tuple list for the kd-tree.
     * 
     * @return an instance of <code>TupleList</code>. 
     */
    public TupleList getTupleList() {
        return tuples;
    }

    /**
     * Get the distance metric.
     * 
     * @return an instance of <code>DistanceMetric</code>. 
     */
    public DistanceMetric getDistanceMetric() {
        return distanceMetric;
    }

    /**
     * Get the number of dimensions.
     * @return the number of dimensions.
     */
    public int getDimensions() {
        return tuples.getTupleLength();
    }

    /**
     * Factory method that builds a kd-tree from a tuple list by adding every
     * tuple.
     *
     * @param tuples the <code>TupleList</code>.
     * @param distanceMetric the <code>DistanceMetric</code>.
     * @return a fully populated kd-tree.
     */
    public static TupleKDTree forTupleList(final TupleList tuples,
        final DistanceMetric distanceMetric) {
        
        TupleKDTree kd = new TupleKDTree(tuples, distanceMetric);
        int tupleCount = tuples.getTupleCount();
        for (int i = 0; i < tupleCount; i++) {
            kd.insert(i);
        }
        return kd;
    }

    /**
     * Factory method that builds a balanced kd-tree from a tuple list.
     * 
     * @param tuples the <code>TupleList</code>.
     * @param distanceMetric the <code>DistanceMetric</code>.
     * @return a fully populated kd-tree.
     */
    public static TupleKDTree forTupleListBalanced(final TupleList tuples,
        final DistanceMetric distanceMetric) {
        
        final TupleKDTree kd = new TupleKDTree(tuples, distanceMetric);
        final int tupleCount = tuples.getTupleCount();
        final int tupleLen = tuples.getTupleLength();
        final int[] tupleIndices = new int[tupleCount];
        for (int i = 0; i < tupleCount; i++) {
            tupleIndices[i] = i;
        }
        final IntComparator[] comparators = new IntComparator[tupleLen];
        for (int dim = 0; dim < tupleLen; dim++) {
            comparators[dim] = new TupleIndexComparator(tuples, dim);
        }
        generateBalanced(kd, tupleIndices, 0, tupleIndices.length - 1, 0, comparators);
        return kd;
    }

    private static void generateBalanced(
        final TupleKDTree kdtree,
        final int[] indices,
        final int left,
        final int right,
        final int dim,
        final IntComparator[] comparators) {

        if (left <= right) {

            if (left == right) {

                kdtree.insert(indices[left]);

            } else {

                int mid = 1 + (right - left) / 2;
                int partitionIndex = Sorting.partitionIndices(indices, mid, left, right, comparators[dim]);

                kdtree.insert(indices[partitionIndex]);

                int n = partitionIndex - 1;
                int nextDim = (dim + 1) % kdtree.getDimensions();
                if (n >= left) {
                    generateBalanced(kdtree, indices, left, n, nextDim, comparators);
                }

                n = partitionIndex + 1;
                if (n <= right) {
                    generateBalanced(kdtree, indices, n, right, nextDim, comparators);
                }
            }

        }
    }

    private void ensureCapacity(final int minCap) {
        int curCap = currentCapacity();
        if (curCap < minCap) {
            int newCap = Math.max(curCap * 2, minCap);
            int[] newNodes = new int[newCap];
            int[] newLefts = new int[newCap];
            int[] newRights = new int[newCap];
            boolean[] newDeleted = new boolean[newCap];
            if (curCap > 0) {
                System.arraycopy(nodes, 0, newNodes, 0, curCap);
                System.arraycopy(lefts, 0, newLefts, 0, curCap);
                System.arraycopy(rights, 0, newRights, 0, curCap);
            }
            Arrays.fill(newNodes, curCap, newCap, -1);
            Arrays.fill(newLefts, curCap, newCap, -1);
            Arrays.fill(newRights, curCap, newCap, -1);
            Arrays.fill(newDeleted, curCap, newCap, false);
            nodes = newNodes;
            lefts = newLefts;
            rights = newRights;
        }
    }

    private int currentCapacity() {
        return nodes != null ? nodes.length : 0;
    }

    private void newNodeOnLeft(final int parentIndex, final int ndx) {
        int m = count;
        count++;
        ensureCapacity(count);
        nodes[m] = ndx;
        // Set to the index into nodes, not the tuple index itself.
        lefts[parentIndex] = m;
    }

    private void newNodeOnRight(final int parentIndex, final int ndx) {
        int m = count;
        count++;
        ensureCapacity(count);
        nodes[m] = ndx;
        rights[parentIndex] = m;
    }

    private void checkNdx(final int ndx) {
        if (ndx < 0 || ndx > maxNdx) {
            throw new IndexOutOfBoundsException("out of bounds: " + ndx);
        }
    }

    /**
     * Insert the tuple with the specified index.
     * 
     * @param ndx index of the tuple to insert. 
     */
    public void insert(final int ndx) {

        checkNdx(ndx);

        // First thing to be inserted -- it becomes the root.
        if (count == 0) {
            ensureCapacity(1);
            nodes[0] = ndx;
            count++;
            return;
        }

        // Current node index
        int n = 0;
        // Search depth into the tree
        int depth = 0;
        // Total # of tuple dimensions
        final int dim = tuples.getTupleLength();

        while (true) {

            int curNode = nodes[n];

            if (curNode == ndx) {

                // Already present, return.
                return;
                
            } else {

                // Pick the dimension for comparison, which cycles from 0 to (dim-1), then
                // starts over.
                int d = depth % dim;

                double coord = tuples.getTupleValue(ndx, d);

                double nodeCoord = tuples.getTupleValue(curNode, d);

                if (coord > nodeCoord) {

                    // Make it the right child of the current node, if the right child has not been set.
                    if (rights[n] < 0) {
                        newNodeOnRight(n, ndx);
                        return;
                    } else {
                        n = rights[n];
                    }

                } else { // coord <= nodeCoord

                    // If left child has not been set, make it the left child.
                    if (lefts[n] < 0) {
                        newNodeOnLeft(n, ndx);
                        return;
                    } else {
                        n = lefts[n];
                    }

                }
            }

            depth++;

        } // while

    }

    /**
     * Checks whether or not the tuple with the specified index has been added to the 
     * kd-tree.
     * 
     * @param ndx the index of the tuple whose presence to check.
     * 
     * @return true if the tuple is in the kd-tree, false otherwise. 
     */
    public boolean contains(final int ndx) {
        checkNdx(ndx);

        // Can't find anything if empty.
        if (count == 0) {
            return false;
        }

        int n = 0;
        int depth = 0;
        final int dim = tuples.getTupleLength();

        while (true) {
            int curNode = nodes[n];
            if (curNode == ndx) {
                return true;
            } else {
                int d = depth % dim;
                double coord = tuples.getTupleValue(ndx, d);
                double nodeCoord = tuples.getTupleValue(curNode, d);
                if (coord > nodeCoord) {
                    if (rights[n] < 0) {
                        return false;
                    } else {
                        n = rights[n];
                    }
                } else {
                    if (lefts[n] < 0) {
                        return false;
                    } else {
                        n = lefts[n];
                    }
                }
            }
            depth++;
        } // while
    }
    
    /**
     * Searches for an added tuple having the specified values. This method only
     * returns a non-zero tuple index if an exact match is found. Use
     * <code>closeTo()</code> to find close matches.
     *
     * @param coords the coordinates for which to search.
     *
     * @return the tuple index if the values are matched exactly, -1 otherwise.
     */
    public int search(final double[] coords) {

        int n = 0;
        int depth = 0;
        final int dim = tuples.getTupleLength();

        // Buffer for scooping out data for comparison.
        final double[] nodeCoords = new double[dim];

        while (true) {

            int curNode = (n >= 0 && n < count) ? nodes[n] : -1;

            if (curNode < 0) {
                return -1; // Not found.
            }

            tuples.getTuple(curNode, nodeCoords);

            if (Arrays.equals(coords, nodeCoords)) {
                return curNode;
            }

            int d = depth % dim;

            if (coords[d] > nodeCoords[d]) {
                n = rights[n];
            } else {
                n = lefts[n];
            }

            depth++;

        } // while

    }

    /**
     * Finds the nearest neighbor of the tuple with the specified index. If the tuple
     * with the specified index is in the kd-tree, the nearest different index is returned.
     * 
     * @param ndx the index of the tuple.
     * 
     * @return the index of the nearest neighbor or -1 if none found.
     */
    public int nearestNeighbor(final int ndx) {

        checkNdx(ndx);

        double[] coords = new double[tuples.getTupleLength()];

        tuples.getTuple(ndx, coords);

        int[] nn = nearest(coords, 2);

        return nn[0] == ndx ? nn[1] : nn[0];
    }

    /**
     * Finds the tuple that is closest to the specified coordinate.
     * 
     * @param coords array containing the coordinate value. Its length should be equal
     *   to the dimensionality of the tuples added to the kd-tree.
     * 
     * @return the index of the nearest tuple or -1 if none found. 
     */
    public int nearest(final double[] coords) {
        int[] nn = nearest(coords, 1);
        return nn[0];
    }

    /**
     * Finds the nearest neighbors of the tuple with the specified index.
     * 
     * @param ndx the index of the tuple.
     * @param num the number of nearest neighbors to return.
     * 
     * @return array of length <code>num</code> containing the tuple indexes of the 
     *     nearest neighbors.
     * 
     * @throws IllegalArgumentException if <code>num</code> is greater than the number
     *     of tuples added to the kd-tree exclusive of the search tuple.
     */
    public int[] nearest(final int ndx, final int num) {

        final int maxNum = num == count ? (contains(ndx) ? count - 1 : count) : count;
        
        if (num < 0 || num > maxNum) {
            throw new IllegalArgumentException(
                    "number of neighbors negative or greater than number of nodes: "
                    + num);
        }

        int dim = tuples.getTupleLength();
        double[] coords = new double[dim];
        tuples.getTuple(ndx, coords);

        LinkedList<DistanceEntry> distanceList = new LinkedList<>();

        rnearest(0, coords, num, HyperRect.infiniteHyperRect(dim),
                Double.MAX_VALUE, 0, dim, distanceList, ndx);

        int[] ids = new int[num];
        for (int i = 0; i < num; i++) {
            DistanceEntry entry = distanceList.removeFirst();
            ids[i] = entry.getIndex();
        }

        return ids;
    }

    /**
     * Search for nearest neighbors of a specified coordinate.
     * @param coords the search coordinate, which should have the same number of dimensions
     *     as the tuples added to this kd-tree.
     * @param num the number of nearest neighbors desired.
     * @return an array of length <code>num</code> containing the nearest neighbor indexes.
     * 
     * @throws IllegalArgumentException if <code>num</code> is greater than the number
     *     of tuples added to the kd-tree.
     */
    public int[] nearest(final double[] coords, final int num) {

        if (num < 0 || num > count) {
            throw new IllegalArgumentException(
                    "number of neighbors negative or greater than number of nodes: "
                    + num);
        }

        int dim = coords.length;

        LinkedList<DistanceEntry> distanceList = new LinkedList<DistanceEntry>();

        rnearest(0, coords, num, HyperRect.infiniteHyperRect(dim),
                Double.MAX_VALUE, 0, dim, distanceList, -1);

        int[] ids = new int[num];
        for (int i = 0; i < num; i++) {
            DistanceEntry entry = distanceList.removeFirst();
            ids[i] = entry.getIndex();
        }

        return ids;
    }

    /**
     * Search for tuples close to another tuple.
     * 
     * @param ndx the index of the search tuple.
     * @param maxDistance the maximum distance threshold for the result.
     * @return an array, possibly of length 0, of the indexes of other tuples within the 
     *     specified distance of the search tuple.
     */
    public int[] closeTo(final int ndx, final double maxDistance) {

        int dim = tuples.getTupleLength();
        double[] coords = new double[dim];
        tuples.getTuple(ndx, coords);

        LinkedList<DistanceEntry> distanceList = new LinkedList<DistanceEntry>();

        rcloseTo(0, coords, HyperRect.infiniteHyperRect(dim), maxDistance, 0,
                dim, distanceList, ndx);

        int sz = distanceList.size();
        int[] ids = new int[sz];
        for (int i = 0; i < sz; i++) {
            DistanceEntry entry = distanceList.removeFirst();
            ids[i] = entry.getIndex();
        }

        return ids;
    }

    /**
     * Search for tuples close to a coordinate.
     * 
     * @param coords the search coordinate, which should have the same number of dimensions
     *     as the tuples added to this kd-tree.
     * @param maxDistance the maximum distance threshold for the result.
     * @return an array, possibly of length 0, of the indexes of other tuples within the 
     *     specified distance of the search tuple.
     */
    public int[] closeTo(final double[] coords, final double maxDistance) {

        int dim = coords.length;

        LinkedList<DistanceEntry> distanceList = new LinkedList<>();

        rcloseTo(0, coords, HyperRect.infiniteHyperRect(dim), maxDistance, 0,
                dim, distanceList, -1);

        int sz = distanceList.size();
        int[] ids = new int[sz];
        for (int i = 0; i < sz; i++) {
            DistanceEntry entry = distanceList.removeFirst();
            ids[i] = entry.getIndex();
        }

        return ids;
    }

    /**
     * Search for all tuples contained in the specified region of space.
     * @param rect a <code>HyperRect</code> defining the search region.
     * @return an array, possibly of length 0, containing the indexes of tuples 
     *     contained within the search space.
     */
    public int[] inside(final HyperRect rect) {
        final int dim = tuples.getTupleLength();
        if (rect.getDimension() != dim) {
            throw new IllegalArgumentException("dimension mismatch: "
                    + rect.getDimension() + " != " + dim);
        }
        double[] midPoint = new double[dim];
        double[] maxDiffs = new double[dim];
        for (int i = 0; i < dim; i++) {
            double min = rect.getMinCornerCoord(i);
            double max = rect.getMaxCornerCoord(i);
            double maxDiff = (max - min) / 2;
            midPoint[i] = min + maxDiff;
            maxDiffs[i] = maxDiff;
        }
        TIntArrayList intList = new TIntArrayList();
        rcloseTo(0, midPoint, HyperRect.infiniteHyperRect(dim), maxDiffs, 0, dim,
                intList, -1);

        intList.trimToSize();
        return intList.toArray();
    }

    private void rnearest(final int curNodeNdx, 
        final double[] targetCoords, 
        final int num,
        final HyperRect hr, 
        double maxDistance, 
        final int level, 
        final int dim,
        final LinkedList<DistanceEntry> distanceList, 
        final int ndxToExclude) {

        int curNode = nodes[curNodeNdx];
        if (curNode < 0) {
            return;
        }

        // Component of coords to use for splitting.
        int s = level % dim;

        double[] curCoords = tuples.getTuple(curNode, null);

        double targetCoord = targetCoords[s];
        double curCoord = curCoords[s];

        boolean targetInLeft = targetCoord < curCoord;

        int nearerNodeNdx = -1, furtherNodeNdx = -1;
        if (targetInLeft) {
            nearerNodeNdx = lefts[curNodeNdx];
            furtherNodeNdx = rights[curNodeNdx];
        } else {
            nearerNodeNdx = rights[curNodeNdx];
            furtherNodeNdx = lefts[curNodeNdx];
        }

        if (nearerNodeNdx >= 0) {
            double oldCoord = 0.0;
            if (targetInLeft) {
                oldCoord = hr.getMaxCornerCoord(s);
                hr.setMaxCornerCoord(s, curCoords[s]);
            } else {
                oldCoord = hr.getMinCornerCoord(s);
                hr.setMinCornerCoord(s, curCoords[s]);
            }
            rnearest(nearerNodeNdx, targetCoords, num, hr, maxDistance, level + 1,
                    dim, distanceList, ndxToExclude);
            if (targetInLeft) {
                hr.setMaxCornerCoord(s, oldCoord);
            } else {
                hr.setMinCornerCoord(s, oldCoord);
            }
            if (distanceList.size() == num) {
                maxDistance = distanceList.getLast().getDistance();
            }
        }

        if (furtherNodeNdx >= 0) {
            double oldCoord = 0.0;
            if (targetInLeft) {
                oldCoord = hr.getMinCornerCoord(s);
                hr.setMinCornerCoord(s, curCoords[s]);
            } else {
                oldCoord = hr.getMaxCornerCoord(s);
                hr.setMaxCornerCoord(s, curCoords[s]);
            }

            double distance = distanceMetric.distance(hr.closestPoint(targetCoords), targetCoords);

            if (distance < maxDistance) {
                rnearest(furtherNodeNdx, targetCoords, num, hr, maxDistance,
                        level + 1, dim, distanceList, ndxToExclude);
            }

            if (targetInLeft) {
                hr.setMinCornerCoord(s, oldCoord);
            } else {
                hr.setMaxCornerCoord(s, oldCoord);
            }
            if (distanceList.size() == num) {
                maxDistance = distanceList.getLast().getDistance();
            }
        }

        if (curNode != ndxToExclude) {
            double curToTarget = distanceMetric.distance(curCoords, targetCoords);
            if (curToTarget < maxDistance) {
                addToDistanceList(distanceList, new DistanceEntry(curNode, curToTarget), num);
            }
        }
    }

    private static void addToDistanceList(final LinkedList<DistanceEntry> distanceList, 
        final DistanceEntry entry, 
        final int maxSize) {
        
        int n = Collections.binarySearch(distanceList, entry);
        if (n < 0) {
            // The usual case, since this method is never called if an entry is already in the 
            // list.
            n = -n - 1;
            distanceList.add(n, entry);
            while (distanceList.size() > maxSize) {
                distanceList.removeLast();
            }
        }
    }

    private void rcloseTo(final int curNodeNdx, 
        final double[] targetCoords, 
        final HyperRect hr,
        final double maxDistance, 
        final int level, 
        final int dim, 
        final LinkedList<DistanceEntry> distanceList,
        final int ndxToExclude) {

        int curNode = nodes[curNodeNdx];
        if (curNode < 0) {
            return;
        }

        // Component of coords to use for splitting.
        int s = level % dim;

        double[] curCoords = tuples.getTuple(curNode, null);

        double targetCoord = targetCoords[s];
        double curCoord = curCoords[s];

        boolean targetInLeft = targetCoord <= curCoord;

        int nearerNodeNdx = -1, furtherNodeNdx = -1;
        if (targetInLeft) {
            nearerNodeNdx = lefts[curNodeNdx];
            furtherNodeNdx = rights[curNodeNdx];
        } else {
            nearerNodeNdx = rights[curNodeNdx];
            furtherNodeNdx = lefts[curNodeNdx];
        }

        if (nearerNodeNdx >= 0) {
            double oldCoord = 0.0;
            if (targetInLeft) {
                oldCoord = hr.getMaxCornerCoord(s);
                hr.setMaxCornerCoord(s, curCoords[s]);
            } else {
                oldCoord = hr.getMinCornerCoord(s);
                hr.setMinCornerCoord(s, curCoords[s]);
            }
            rcloseTo(nearerNodeNdx, targetCoords, hr, maxDistance, level + 1, dim,
                    distanceList, ndxToExclude);
            if (targetInLeft) {
                hr.setMaxCornerCoord(s, oldCoord);
            } else {
                hr.setMinCornerCoord(s, oldCoord);
            }
        }

        if (furtherNodeNdx >= 0) {
            double oldCoord = 0.0;
            if (targetInLeft) {
                oldCoord = hr.getMinCornerCoord(s);
                hr.setMinCornerCoord(s, curCoords[s]);
            } else {
                oldCoord = hr.getMaxCornerCoord(s);
                hr.setMaxCornerCoord(s, curCoords[s]);
            }
            double distance = distanceMetric.distance(hr.closestPoint(targetCoords), targetCoords);
            if (distance <= maxDistance) {
                rcloseTo(furtherNodeNdx, targetCoords, hr, maxDistance, level + 1,
                        dim, distanceList, ndxToExclude);
            }
            if (targetInLeft) {
                hr.setMinCornerCoord(s, oldCoord);
            } else {
                hr.setMaxCornerCoord(s, oldCoord);
            }
        }

        if (curNode != ndxToExclude) {
            double curToTarget = distanceMetric.distance(curCoords, targetCoords);
            if (curToTarget <= maxDistance) {
                addToDistanceList(distanceList, new DistanceEntry(curNode, curToTarget), Integer.MAX_VALUE);
            }
        }
    }

    private void rcloseTo(final int curNodeNdx, 
        final double[] targetCoords, 
        final HyperRect hr,
        final double[] maxDiffs, 
        final int level, 
        final int dim, 
        final TIntArrayList intList,
        final int ndxToExclude) {

        int curNode = nodes[curNodeNdx];
        if (curNode < 0) {
            return;
        }

        // Component of coords to use for splitting.
        int s = level % dim;

        double[] curCoords = tuples.getTuple(curNode, null);

        double targetCoord = targetCoords[s];
        double curCoord = curCoords[s];

        boolean targetInLeft = targetCoord <= curCoord;

        int nearerNodeNdx = -1, furtherNodeNdx = -1;
        if (targetInLeft) {
            nearerNodeNdx = lefts[curNodeNdx];
            furtherNodeNdx = rights[curNodeNdx];
        } else {
            nearerNodeNdx = rights[curNodeNdx];
            furtherNodeNdx = lefts[curNodeNdx];
        }

        if (nearerNodeNdx >= 0) {
            double oldCoord = 0.0;
            if (targetInLeft) {
                oldCoord = hr.getMaxCornerCoord(s);
                hr.setMaxCornerCoord(s, curCoords[s]);
            } else {
                oldCoord = hr.getMinCornerCoord(s);
                hr.setMinCornerCoord(s, curCoords[s]);
            }
            rcloseTo(nearerNodeNdx, targetCoords, hr, maxDiffs, level + 1, dim,
                    intList, ndxToExclude);
            if (targetInLeft) {
                hr.setMaxCornerCoord(s, oldCoord);
            } else {
                hr.setMinCornerCoord(s, oldCoord);
            }
        }

        if (furtherNodeNdx >= 0) {
            double oldCoord = 0.0;
            if (targetInLeft) {
                oldCoord = hr.getMinCornerCoord(s);
                hr.setMinCornerCoord(s, curCoords[s]);
            } else {
                oldCoord = hr.getMaxCornerCoord(s);
                hr.setMaxCornerCoord(s, curCoords[s]);
            }

            if (diffsWithinBoundaries(hr.closestPoint(targetCoords), targetCoords,
                    maxDiffs)) {
                rcloseTo(furtherNodeNdx, targetCoords, hr, maxDiffs, level + 1, dim,
                        intList, ndxToExclude);
            }

            if (targetInLeft) {
                hr.setMinCornerCoord(s, oldCoord);
            } else {
                hr.setMaxCornerCoord(s, oldCoord);
            }
        }

        if (curNodeNdx != ndxToExclude) {
            if (diffsWithinBoundaries(curCoords, targetCoords, maxDiffs)) {
                intList.add(curNode);
            }
        }
    }

    private static boolean diffsWithinBoundaries(double[] coords1,
            double[] coords2, double[] maxDiffs) {
        final int dim = coords1.length;
        for (int i = 0; i < dim; i++) {
            if (Math.abs(coords1[i] - coords2[i]) > maxDiffs[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Represents a node in the kd-tree for object-oriented traversal.
     */
    public class KDNode {

        private final int index;
        private final int dim;

        private KDNode(final int index, final int dim) {
            this.index = index;
            this.dim = dim;
        }

        public boolean isLeaf() {
            return lefts[index] < 0 && rights[index] < 0;
        }

        public int getTupleIndex() {
            return nodes[index];
        }

        public boolean hasLeft() {
            return lefts[index] >= 0;
        }

        public boolean hasRight() {
            return rights[index] >= 0;
        }

        public KDNode getLeft() {
            final int leftIndex = lefts[index];
            if (leftIndex >= 0) {
                return new KDNode(leftIndex, (dim + 1) % getDimensions());
            }
            return null;
        }

        public KDNode getRight() {
            final int rightIndex = rights[index];
            if (rightIndex >= 0) {
                return new KDNode(rightIndex, (dim + 1) % getDimensions());
            }
            return null;
        }

        public int descendantsOnLeft() {
            KDNode left = getLeft();
            if (left != null) {
                return 1 + left.descendantsOnLeft() + left.descendantsOnRight();
            }
            return 0;
        }

        public int descendantsOnRight() {
            KDNode right = getRight();
            if (right != null) {
                return 1 + right.descendantsOnLeft() + right.descendantsOnRight();
            }
            return 0;
        }

        /**
         * Returns the balance factor for the node. If a node is perfectly
         * balanced, that is, having the same number of descendants on the left
         * as on the right, the balance factor will be 1.0. Imbalanced nodes
         * always return a balance factor with an absolute values greater than
         * 1.0. However, if the number of descendants on the right is greater
         * than the number of descendants on the left, the balance factor is
         * negative. Leaf nodes have a balance factor of NaN.
         *
         * @return the balance factor.
         */
        public double balanceFactor() {

            final int onLeft = descendantsOnLeft();
            final int onRight = descendantsOnRight();

            int min = Math.min(onLeft, onRight);
            int max = Math.max(onLeft, onRight);

            // More descendants on the left than on the right.
            boolean positiveSlope = onLeft > onRight;

            if (max > 0) {
                double slope;
                if (min > 0) {
                    slope = ((double) max) / min;
                    if (!positiveSlope) {
                        slope = -slope;
                    }
                } else {
                    slope = positiveSlope ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
                }
                return slope;
            }

            // onLeft == onRight == 0.
            return Double.NaN;
        }

    }

    public static class DistanceEntry implements Comparable<DistanceEntry> {

        private int index;
        private double distance;

        public DistanceEntry(int index, double distance) {
            this.index = index;
            this.distance = distance;
        }

        public int getIndex() {
            return index;
        }

        public double getDistance() {
            return distance;
        }

        @Override
        public int compareTo(DistanceEntry o) {
            return this.distance < o.distance ? -1 : this.distance > o.distance ? +1
                    : this.index < o.index ? -1 : this.index > o.index ? +1 : 0;
        }

    }

    /**
     * Used for sorting tuple indices based on the values of those tuples for a
     * specified dimension.
     */
    private static class TupleIndexComparator implements IntComparator {

        private final TupleList tupleData;
        private final int dim;

        private TupleIndexComparator(TupleList tupleData, int dim) {
            this.tupleData = tupleData;
            this.dim = dim;
        }

        @Override
        public int compare(int n1, int n2) {
            double v1 = tupleData.getTupleValue(n1, dim);
            double v2 = tupleData.getTupleValue(n2, dim);
            return v1 < v2 ? -1 : v1 > v2 ? +1 : 0;
        }
    }

}
