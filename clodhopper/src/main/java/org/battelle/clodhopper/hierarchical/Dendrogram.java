/*
 * Dendrogram.java
 * 
 * JAC: Java Analytic Components
 * 
 * For information contact Randall Scarberry, randall.scarberry@pnl.gov
 * 
 * Notice: This computer software was prepared by Battelle Memorial Institute, 
 * hereinafter the Contractor, under Contract No. DE-AC05-76RL0 1830 with the 
 * Department of Energy (DOE).  All rights in the computer software are 
 * reserved by DOE on behalf of the United States Government and the Contractor
 * as provided in the Contract.  You are authorized to use this computer 
 * software for Governmental purposes but it is not to be released or 
 * distributed to the public.  NEITHER THE GOVERNMENT NOR THE CONTRACTOR MAKES 
 * ANY WARRANTY, EXPRESS OR IMPLIED, OR ASSUMES ANY LIABILITY FOR THE USE OF 
 * THIS SOFTWARE.  This notice including this sentence must appear on any 
 * copies of this computer software.
 */
package org.battelle.clodhopper.hierarchical;

import gnu.trove.list.array.TIntArrayList;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import org.battelle.clodhopper.Cluster;
import org.battelle.clodhopper.ClusterStats;
import org.battelle.clodhopper.tuple.TupleList;
import org.battelle.clodhopper.tuple.TupleMath;
import org.battelle.clodhopper.util.ArrayIntIterator;

/**
 * <p>The <tt>Dendrogram</tt> class represents dendrograms produced
 * by hierarchical clustering algorithms.  Even though methods make the 
 * dendrogram appear to be a tree structure, internally all nodes are represented
 * by primitives.  For this reason, this class is highly scalable and uses 
 * little memory as compared to more object-oriented implementations.  Also, 
 * traversal methods are not recursive, so there is no danger of stack overflow
 * exceptions from trying to traverse large and highly imbalanced dendrograms.
 * </p>
 * 
 * @author R.Scarberry
 * @since 1.0
 *
 */
public class Dendrogram implements Externalizable {

    private static final int EXTERNALIZABLE_VERSION = 1;
    
    // Contains IDs of the nodes, both the non-leaf and leaf.
	// Length is equal to 2*mLeafCount - 1.  The IDs are generally
    // the indexes of the items being clustered.
	private int[] nodeIDs;
	
	// Indices of the parent nodes - same length as mNodeIDs. Initialized to -1
	// which means "parent not set".  When the dendrogram is finished all elements
	// except 0 should have values >= 0.  Element 0 will remain -1 since the
	// root node does not have a parent.
	private int[] parentIndices;
	// Stores the indices into mNodeIDs of the left and right
	// children nodes of the non-leaf node at each level
	// 0 - (getLeafCount() - 2).
	private int[] leftIndices, rightIndices;

	// Contains the size of the node at levels [0 - (getLeafCount() - 2)].
	private int[] sizes;

	// Given an ID, what index into the above arrays will give
	// the pertinent info for that ID?
	private int[] indicesForIDs;

	private double[] distances, coherences;
	
	private boolean coherencesComputed;
	
	// Used in computation of coherences.  If not explicitly set,
	// the max threshold becomes the max value in mDistances.
	private double minCoherenceThreshold = 0.0;
	private double maxCoherenceThreshold = Double.NaN;

	// Number of leaf nodes -- same as the number of coordinates or vectors
	// that are being clustered.
	private int leafCount;

	// The current level -- counts down from getLeafCount()-1 to 0 as nodes are
	// merged.  The leaf level is getLeafCount() - 1.  The non-leaf levels
	// are [0 - (getLeafCount() - 2)].
	private int currentLevel;
	
	/**
	 * Constructor.  Initially forms a dendrogram with only leaf nodes
	 * with IDs <code>[0 - (leafCount - 1)]</code>.  Completion of the dendrogram requires
	 * <code>(leafCount - 1)</code> calls to <code>merge()</code>.
	 *
	 * @param leafCount
	 */
	public Dendrogram(int leafCount) {

		if (leafCount == 0) {
			throw new IllegalArgumentException(
			        "number of leaves must be > 0");
		}

		this.leafCount = leafCount;

		// Total number of nodes
		int nodeCount = 2 * leafCount - 1;
		// Number of non-leaf nodes
		int nonLeafCount = leafCount - 1;

		// The array containing the node IDs, both leaf and non-leaf.
		nodeIDs = new int[nodeCount];
		// Initialize the leaf node IDs, which are at the bottom of the array
		// and are numbered sequentially
		int id = 0;
		for (int i = nonLeafCount; i < nodeCount; i++) {
			nodeIDs[i] = id++;
		}

		// Initialize the parent indices.
		parentIndices = new int[nodeCount];
		Arrays.fill(parentIndices, -1);

		int index = nonLeafCount;
		indicesForIDs = new int[leafCount];
		for (int i = 0; i < leafCount; i++) {
			indicesForIDs[i] = index++;
		}

		leftIndices = new int[nonLeafCount];
		rightIndices = new int[nonLeafCount];
		sizes = new int[nonLeafCount];

		distances = new double[nonLeafCount];
		coherences = new double[nonLeafCount];

		currentLevel = nonLeafCount; // == (ids.numIDs() - 1)

	}

	public double getMinCoherenceThreshold() {
		return minCoherenceThreshold;
	}
	
	public void setMinCoherenceThreshold(double d) {
		minCoherenceThreshold = d;
		coherencesComputed = false;
	}
	
	public double getMaxCoherenceThreshold() {
		return maxCoherenceThreshold;
	}
	
	public void setMaxCoherenceThreshold(double d) {
		maxCoherenceThreshold = d;
		coherencesComputed = false;
	}
	
	/**
	 * Returns an array containing the mappings for the leaf ids.  The mapping
	 * for the leaf with index n will be found in the nth element of the array.
	 * These mappings can be regarded as the left-to-right positions of the nodes
	 * of level greater than or equal to the specified level containing each leaf
	 * id.  The dendrogram must be finished to call this method.  The specified level
	 * must be in the range <code>[0 - getLeafLevel()]</code>.  If level is equal to
	 * the leaf level, all mappings will be unique.  If level is less than the leaf level,
	 * for each non-leaf node at levels greater than level, all child leaf IDs will be
	 * collapsed to the smallest mapping.
	 * @param level
	 * @return - an array of length <code>getLeafCount()</code> containing the mappings
	 *   for each index n in the nth position.
	 * @exception - IllegalStateException, if the dendrogram is not finished.
	 * @exception - IndexOutOfBoundsException, if the level is out of range.
	 */
	public int[] getLeafIDMapping(final int level) {

		checkFinished();
		int leafLevel = getLeafLevel();
		if (level < 0 || level > leafLevel) {
			throw new IndexOutOfBoundsException("level not in [0 - " + leafLevel + "]");
		}

		int[] orderedIDs = getOrderedLeafIDs();
		int numIDs = orderedIDs.length;
		int[] mapping = new int[numIDs];
		// Set values in mapping to ID positions in orderedIDs
		for (int i=0; i<numIDs; i++) {
			mapping[orderedIDs[i]] = i;
		}

		int levelsToHandle = leafLevel - level;
		if (levelsToHandle > 0) {
			boolean[] handled = new boolean[levelsToHandle];
			for (int lvl=level; lvl<leafLevel; lvl++) {
				if (!handled[lvl-level]) {
					int[] oids = getOrderedLeafIDs(lvl);
					// Find the smallest leaf id.
					int sid = Integer.MAX_VALUE;
					for (int i=0; i<oids.length; i++) {
						if (oids[i] < sid) {
							sid = oids[i];
						}
					}
					if (sid == -1) {
						oids = getOrderedLeafIDs(lvl);
					}
					int smapping = mapping[sid];
					for (int i=0; i<oids.length; i++) {
						mapping[oids[i]] = smapping;
					}
					// Now have to mark child non-leaf levels as having
					// been handled.
					int[] clvls = getChildNonLeafLevels(lvl);
					for (int i=0; i<clvls.length; i++) {
						handled[clvls[i]-level] = true;
					}
				}
			}
		}

		return mapping;
	}

	public int[] getOrderedLeafIDs() {
		checkFinished();
		return getOrderedLeafIDs(0);
	}

	public int[] getOrderedLeafIDs(final int level) {

		if (level < currentLevel || level >= getLeafLevel()) {
		    // Handle the trivial case of a dendrogram for a single point.
		    if ((level == 0) && (level == currentLevel) && (level == getLeafLevel())) {
		        return new int[0];
		    }
			throw new IndexOutOfBoundsException("level not in [" +
					currentLevel + " - (" +
					getLeafLevel() + " - 1)] : " + level);
		}

		TIntArrayList rtnList = new TIntArrayList();

		// To use as a stack for the next level to traverse.
		TIntArrayList intList = new TIntArrayList();

		int currentLevel = level;
		int leafLevel = getLeafLevel();

		if (currentLevel < leafLevel) {

			OUTER:
			while (true) {
				int leftLevel = leftIndices[currentLevel];
				int rightLevel = rightIndices[currentLevel];
				if (leftLevel >= leafLevel) { // Encountered a leaf on the left
					rtnList.add(nodeIDs[leftLevel]);
					if (rightLevel >= leafLevel) { // Encountered a leaf on the right.
						rtnList.add(nodeIDs[rightLevel]);
						int sz = intList.size();
						INNER:
						while(true) {
							if (sz == 0) break OUTER;
							currentLevel = intList.get(sz - 1);
							intList.removeAt(sz - 1);
							sz--;
							if (currentLevel >= leafLevel) {
								rtnList.add(nodeIDs[currentLevel]);
							} else {
								break INNER;
							}
						}
					} else { // Right is not a leaf
						currentLevel = rightLevel;
					}
				} else { // Left node is not a leaf
					// Store the right, so we can get back to it later.
					intList.add(rightLevel);
					// Make the left the current level, so it'll be
					// traversed down next.
					currentLevel = leftLevel;
				}
			}

		} else if (leafCount == 1) {

			// Must be only one leaf, so the dendrogram starts out
			// finished with one leaf node and no others.
			rtnList.add(nodeIDs[0]);
		}

		return rtnList.toArray();
	}

	public int[] getChildNonLeafLevels(final int level) {

		if (level < currentLevel || level >= getLeafLevel()) {
			throw new IndexOutOfBoundsException("level not in [" +
					currentLevel + " - (" +
					getLeafLevel() + " - 1)]");
		}

		TIntArrayList rtnList = new TIntArrayList();

		// To use as a stack for the next level to traverse.
		TIntArrayList intList = new TIntArrayList();

		int currentLevel = level;
		int leafLevel = getLeafLevel();

		while (true) {

			if (currentLevel > level) {
				rtnList.add(currentLevel);
			}

			int leftLevel = leftIndices[currentLevel];
			int rightLevel = rightIndices[currentLevel];

			if (leftLevel < leafLevel) {
				currentLevel = leftLevel;
				if (rightLevel < leafLevel) {
					intList.add(rightLevel);
				}
			} else if (rightLevel < leafLevel) {
				currentLevel = rightLevel;
			} else {
				int sz = intList.size();
				if (sz > 0) {
					currentLevel = intList.get(sz - 1);
					intList.removeAt(sz - 1);
				} else {
					break;
				}
			}
		}

		return rtnList.toArray();
	}

	/**
	 * Get the ID of the root dendrogram node, which is normally 0.
	 * @return
	 */
	public int getRootID() {
		checkFinished();
		// mCurrentLevel == 0
		return nodeIDs[currentLevel];
	}

	/**
	 * Get the ID for the non-leaf node at the specified level.
	 * The level must be greater than or equal to the current level and
	 * less than the leaf node level, otherwise -1 is returned.
	 * @param level
	 * @return - the ID for the specified level.
	 */
	public int getLevelID(int level) {
		if (level >= currentLevel && level < leftIndices.length) {
			return nodeIDs[level];
		}
		return -1;
	}

	/**
	 * Get the ID of the left child of the node at the specified
	 * level.
	 *
	 * @param parentLevel - a non-leaf node level greater than or equal to
	 *   the current level.
	 * @return - the left child ID or -1 if parentLevel is not a valid
	 *   non-leaf level.
	 */
	public int getLeftChildID(int parentLevel) {
		if (parentLevel >= currentLevel) {
			return getChildID(parentLevel, leftIndices);
		}
		return -1;
	}

	/**
	 * Get the ID of the right child of the node at the specified
	 * level.
	 *
	 * @param parentLevel - a non-leaf node level greater than or equal to
	 *   the current level.
	 * @return - the right child ID or -1 if parentLevel is not a valid
	 *   non-leaf level.
	 */
	public int getRightChildID(int parentLevel) {
		return getChildID(parentLevel, rightIndices);
	}

	/**
	 * Get the level (not the ID) of the left child of the node at the specified
	 * level.
	 *
	 * @param parentLevel - a non-leaf node level greater than or equal to
	 *   the current level.
	 * @return - the left child level or -1 if parentLevel is not a valid
	 *   non-leaf level.
	 */
	public int getLeftChildLevel(int parentLevel) {
		return getChildLevel(parentLevel, leftIndices);
	}

	/**
	 * Get the level (not the ID) of the right child of the node at the specified
	 * level.
	 *
	 * @param parentLevel - a non-leaf node level greater than or equal to
	 *   the current level.
	 * @return - the right child level or -1 if parentLevel is not a valid
	 *   non-leaf level.
	 */
	public int getRightChildLevel(int parentLevel) {
		return getChildLevel(parentLevel, rightIndices);
	}

	// Gets the left or right child id of the non-leaf node at the specified
	// level.
	private int getChildID(int parentLevel, int[] childIndices) {
		if (parentLevel >= currentLevel && parentLevel < childIndices.length) {
			return nodeIDs[childIndices[parentLevel]];
		}
		return -1;
	}

	// Gets the level of the left or right child of the non-leaf node at the
	// specified level.
	private int getChildLevel(int parentLevel, int[] childIndices) {
		if (parentLevel >= currentLevel && parentLevel < childIndices.length) {
			int childLevel = childIndices[parentLevel];
			if (childLevel > childIndices.length) {
				childLevel = childIndices.length;
			}
			return childLevel;
		}
		return -1;
	}

	/**
	 * Get the dendrogram's root node.  This may only be called when
	 * <code>isFinished()</code> returns true.
	 * @return - the root node.
	 * @exception - IllegalStateException if the dendrogram is not finished.
	 */
	public Node getRoot() {
		checkFinished();
		return new Node(0, nodeIDs[0]);
	}

	/**
	 * Get the non-leaf node for the specified level.  The specified
	 * level must be in the range <code>[0 - (getLeafLevel() - 1)]</code>.
	 *
	 * @param level
	 * @return - the non-leaf node for the specified level.
	 */
	public Node getNode(int level) {
		checkFinished();
		if (level >= currentLevel && level < getLeafLevel()) {
			return new Node(level, nodeIDs[level]);
		}
		throw new IndexOutOfBoundsException("level not in [" +
				currentLevel + " - (" + getLeafLevel() + " - 1)]: " +
				level);
	}

	/**
	 * Get the leaf node level, which is equal to the number of
	 * leaves minus one.
	 * @return - the leaf node level.
	 */
	public int getLeafLevel() {
		return leafCount - 1;
	}

	public int getRightMostLeafID(int parentLevel) {
		while(parentLevel < rightIndices.length) {
			parentLevel = rightIndices[parentLevel];
		}
		return nodeIDs[parentLevel];
	}

	public int getLeftMostLeafID(int parentLevel) {
		while(parentLevel < leftIndices.length) {
			parentLevel = leftIndices[parentLevel];
		}
		return nodeIDs[parentLevel];
	}


	/**
	 * Merge the nodes identified by id1 and id2 using the specified distance as
	 * the decision distance.
	 *
	 * @param id1
	 * @param id2
	 * @param distance
	 * @return - the ID of the new merged node, which is always the minimum of
	 *   id1 and id2.
	 */
	public int mergeNodes(int id1, int id2, double distance) {

		if (currentLevel == 0) {
			throw new IllegalStateException("dendrogram is already finished");
		}

		int mergeID = Math.min(id1, id2);

		currentLevel--;
		nodeIDs[currentLevel] = mergeID;
		int leftIndex = indicesForIDs[id1];
		int rightIndex = indicesForIDs[id2];
		leftIndices[currentLevel] = leftIndex;
		rightIndices[currentLevel] = rightIndex;
		parentIndices[leftIndex] = currentLevel;
		parentIndices[rightIndex] = currentLevel;
		distances[currentLevel] = distance;
		sizes[currentLevel] = nodeSize(id1) + nodeSize(id2);

		indicesForIDs[mergeID] = currentLevel;

		return mergeID;
	}

	public int leftChildID(int parentID) {
		int parentIndex = indicesForIDs[parentID];
		return nodeIDs[leftIndices[parentIndex]];
	}

	public int rightChildID(int parentID) {
		int parentIndex = indicesForIDs[parentID];
		return nodeIDs[rightIndices[parentIndex]];
	}

	/**
	 * Find the id of the neighbor leaf immediately to the right
	 * of the leaf with the specified id.
	 * @param id
	 * @return - the id of the neighbor leaf on the right or -1 if
	 *   there is no neighbor on the right.
	 */
	public int rightNeighborLeafID(int id) {
		return neighborID(id, rightIndices, leftIndices);
	}

	/**
	 * Find the id of the neighbor leaf immediately to the left
	 * of the leaf with the specified id.
	 * @param id
	 * @return - the id of the neighbor leaf on the left or -1 if
	 *   there is no neighbor on the left.
	 */
	public int leftNeighborLeafID(int id) {
		return neighborID(id, leftIndices, rightIndices);
	}

	private int neighborID(int id, int[] indices1, int[] indices2) {
		if (id >= 0 && id < leafCount) {
			// Start off with lastIndex being the leaf index for id, and
			// index being the parent level of the leaf with id.
			int lastIndex = leafCount - 1 + id;
			int index = parentIndices[lastIndex];
			// If looking for the left neighbor id, we tranverse up the
			// parent hierarchy until we've gone one level to the left.
			// Then we go down one level to the left, then down to the right
			// until a leaf is encountered. (Draw a diagram of a dendrogram
			// and trace the path with a pen -- you'll understand.)
			while (index >= 0 && indices1[index] == lastIndex) {
				lastIndex = index;
				index = parentIndices[index];
			}
			// If there is no neighbor index will be -1 here.
			if (index >= currentLevel) {
				// Go down one level to the left if looking for a left neighbor.
				index = indices1[index];
				// Until a leaf is encounted, go down to the right.
				while (index < indices2.length) {
					index = indices2[index];
				}
				// Return the leaf id.
				return nodeIDs[index];
			}
		}
		// Either the id was out of range or it had no neighbor on the
		// side requested.
		return -1;
	}

	public int nodeSize(int id) {
		int index = indicesForIDs[id];
		return index < sizes.length ? sizes[index] : 1;
	}

	public void computeCoherences() {
		
		checkFinished();
		
		double maxd = 0.0;
		
		if (!Double.isNaN(maxCoherenceThreshold)) {
			maxd = maxCoherenceThreshold;
		} else {
			for (int i = 0; i < distances.length; i++) {
				if (maxd < distances[i]) {
					maxd = distances[i];
				}
			}
		}
		
		double mind = 0.0;
		
		if (!Double.isNaN(minCoherenceThreshold)) {
			mind = minCoherenceThreshold;
		}
		
		if (maxd > 0.0) {
			
			double denom = maxd - mind;
			
			for (int i = 0; i < distances.length; i++) {
				// Coherences will range from 0.0 to 1.0. 1.0 means
				// the decision distance was 0.0, such as when merging
				// 2 identical coordinates. The coherence is 0.0 for
				// the node with the maximum decision distance.
				// The usual case is for the root node to have a
				// coherence of 0.0.
				coherences[i] = 1.0 - (distances[i] - mind)/denom;
			}
			
		} else {
			// All decision distances are 0.0, meaning all coordinates
			// are the same. Just set all the coherences to their max
			// value 1.0
			Arrays.fill(coherences, 1.0);
		}
		
		coherencesComputed = true;
	}

	/**
	 * Get the number of IDs used to create this dendrogram. This is also the
	 * number of levels in a finished dendrogram.
	 *
	 * @return
	 */
	public int getLeafCount() {
		return leafCount;
	}

	/**
	 * Has the dendrogram been completely formed? That is, have the nodes been
	 * merged until the current level is 0?
	 *
	 * @return
	 */
	public boolean isFinished() {
		return currentLevel == 0;
	}

	/**
	 * Get the current level of the dendrogram, which ranges from
	 * <code>getLeafCount() - 1</code> to 0.
	 *
	 * @return
	 */
	public int getCurrentLevel() {
		return currentLevel;
	}

	public int[] getNodeIDs(int index) {

		int nonLeafCount = leafCount - 1;

		int n = index < nonLeafCount ? sizes[index] : 1;

		int[] rtn = new int[n];
		TIntArrayList intList = null;
		if (n > 1) {
			intList = new TIntArrayList();
		}

		int count = 0;
		int currentIndex = index;
		while (count < n) {
			if (currentIndex < nonLeafCount) {
				intList.add(rightIndices[currentIndex]);
				currentIndex = leftIndices[currentIndex];
			} else {
				rtn[count++] = nodeIDs[currentIndex];
				int lastIndex = intList != null ? intList.size() - 1 : -1;
				if (lastIndex >= 0) {
					currentIndex = intList.get(lastIndex);
					intList.removeAt(lastIndex);
				}
			}
		}

		return rtn;
	}

	public synchronized List<int[]> generateClusterGroupings(int clustersDesired) {
	    
            // Ensures current level == 0.
            checkFinished();

            if (clustersDesired <= 0 || clustersDesired > leafCount) {
                    throw new IllegalArgumentException("clusters desired not in [0 - ("
                                    + leafCount + " - 1)]: " + clustersDesired);
            }

            int nodeCount = nodeIDs.length;
            BitSet bits = new BitSet(nodeCount);

            List<int[]> clusters = new ArrayList<int[]>(clustersDesired);
            
            int currentIndex = clustersDesired - 1;

            TIntArrayList intList = new TIntArrayList();

            for (int i = currentIndex; i < nodeCount; i++) {
                    if (!bits.get(i)) { // If an ancestor has not been turned into a
                            // cluster...
                            clusters.add(getNodeIDs(i));
                            // While loop is to keep descendents of node just turned into a
                            // cluster
                            // from also being turned into clusters.
                            int ci = i;
                            while (true) {
                                    if (ci < leftIndices.length) {
                                            bits.set(leftIndices[ci]);
                                            bits.set(rightIndices[ci]);
                                            intList.add(rightIndices[ci]);
                                            ci = leftIndices[ci];
                                    } else {
                                            int lastIndex = intList.size() - 1;
                                            if (lastIndex >= 0) {
                                                    ci = intList.get(lastIndex);
                                                    intList.removeAt(lastIndex);
                                            } else {
                                                    break;
                                            }
                                    }
                            }
                    }
            }

            return clusters;
	}
	
	public synchronized List<Cluster> generateClusters(int clustersDesired, TupleList tuples) {

		// Ensures current level == 0.
		checkFinished();

		if (leafCount != tuples.getTupleCount()) {
			throw new IllegalArgumentException(
					"dendrogram does not match tuples: leaf node count = "
							+ leafCount + ", tuple count = "
							+ tuples.getTupleCount());
		}

		List<int[]> clusterGroups = generateClusterGroupings(clustersDesired);
		int numClusters = clusterGroups.size();
		
		List<Cluster> clusters = new ArrayList<Cluster> (numClusters);
		for (int i=0; i<numClusters; i++) {
			int[] members = clusterGroups.get(i);
			double[] center = TupleMath.average(tuples, new ArrayIntIterator(members));
		    clusters.add(new Cluster(members, center));
		}

		return clusters;
	}
	
	public synchronized List<Cluster> generateOptimalClusters(TupleList tuples) {

	    // Ensures current level == 0.
        checkFinished();

		if (leafCount != tuples.getTupleCount()) {
			throw new IllegalArgumentException(
					"dendrogram does not match tuples: leaf node count = "
							+ leafCount + ", tuple count = "
							+ tuples.getTupleCount());
		}
        
        final int tupleCount = tuples.getTupleCount();
        
        double maxBIC = -Double.MAX_VALUE;
        List<Cluster> bestClusters = null;
        
        for (int numClusters = 1; numClusters <= tupleCount; numClusters++) {
            List<Cluster> clusters = generateClusters(numClusters, tuples);
            double bic = ClusterStats.computeBIC(tuples, clusters);
            if (bic > maxBIC) {
                maxBIC = bic;
                bestClusters = clusters;
            } else if (bic < 0.0 || maxBIC/bic >= 2.0) {
                break;
            }
        }
	    
        return bestClusters;
	}
	
	public int clustersWithCoherenceExceeding(double coherence) {

		checkFinished();

		// Ensure coherence is within valid range.
		if (coherence < 0.0 || coherence > 1.0) {
			throw new IllegalArgumentException("coherence not in [0.0 - 1.0]: "
					+ coherence);
		}
		
		if (!coherencesComputed) {
			computeCoherences();
		}

		int nonLeafCount = leafCount - 1;

		int clusters = leafCount;
		for (int i = 0; i < nonLeafCount; i++) {
			if (coherences[i] >= coherence) {
				clusters = i + 1;
				break;
			}
		}

		return clusters;
	}

	private void checkFinished() {
		if (!isFinished()) {
			throw new IllegalStateException("dendrogram is not finished");
		}
	}

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(EXTERNALIZABLE_VERSION);
        writeIntArray(out, this.nodeIDs);
        writeIntArray(out, this.parentIndices);
        writeIntArray(out, this.leftIndices);
        writeIntArray(out, this.rightIndices);
        writeIntArray(out, this.sizes);
        writeIntArray(out, this.indicesForIDs);
        writeDoubleArray(out, this.distances);
        writeDoubleArray(out, this.coherences);
        out.writeInt(leafCount);
        out.writeInt(currentLevel);
    }
    
    @Override
    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        int version = in.readInt();
        if (version != EXTERNALIZABLE_VERSION) {
            throw new IOException("invalid version: " + version);
        }
        this.nodeIDs = readIntArray(in);
        this.parentIndices = readIntArray(in);
        this.leftIndices = readIntArray(in);
        this.rightIndices = readIntArray(in);
        this.sizes = readIntArray(in);
        this.indicesForIDs = readIntArray(in);
        this.distances = readDoubleArray(in);
        this.coherences = readDoubleArray(in);
        this.leafCount = in.readInt();
        this.currentLevel = in.readInt();
    }
    
    private static void writeIntArray(ObjectOutput out, int[] arr) throws IOException {
    	final int n = arr != null ? arr.length : -1;
    	out.writeInt(n);
    	for (int i=0; i<n; i++) {
    		out.writeInt(arr[i]);
    	}
    }
    
    private static int[] readIntArray(ObjectInput in) throws IOException {
    	final int n = in.readInt();
    	final int[] result = n >= 0 ? new int[n] : null;
    	for (int i=0; i<n; i++) {
    		result[i] = in.readInt();
    	}
    	return result;
    }

    private static void writeDoubleArray(ObjectOutput out, double[] arr) throws IOException {
    	final int n = arr != null ? arr.length : -1;
    	out.writeInt(n);
    	for (int i=0; i<n; i++) {
    		out.writeDouble(arr[i]);
    	}
    }
    
    private static double[] readDoubleArray(ObjectInput in) throws IOException {
    	final int n = in.readInt();
    	final double[] result = n >= 0 ? new double[n] : null;
    	for (int i=0; i<n; i++) {
    		result[i] = in.readDouble();
    	}
    	return result;
    }

    public class Node {

		private int mLevel;
		private int mID;

		private Node(int level, int id) {
			mLevel = level;
			mID = id;
		}

		public boolean isRoot() {
			return mLevel == 0;
		}

		public boolean isLeaf() {
			return mLevel == leafCount - 1;
		}

		public int getLevel() {
			return mLevel;
		}

		public int getID() {
			return mID;
		}

		public Node leftChild() {
			if (!isLeaf()) {
				return new Node(getLeftChildLevel(mLevel), getLeftChildID(mLevel));
			}
			return null;
		}

		public Node rightChild() {
			if (!isLeaf()) {
				return new Node(getRightChildLevel(mLevel), getRightChildID(mLevel));
			}
			return null;
		}

		public double distance() {
			return isLeaf() ? Double.NaN : distances[mLevel];
		}

		public double coherence() {
			return isLeaf() ? Double.NaN : coherences[mLevel];
		}

	}

}
