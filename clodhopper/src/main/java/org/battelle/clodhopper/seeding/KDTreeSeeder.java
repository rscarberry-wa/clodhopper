package org.battelle.clodhopper.seeding;

import org.battelle.clodhopper.tuple.ArrayTupleList;
import org.battelle.clodhopper.tuple.TupleList;
import org.battelle.clodhopper.tuple.TupleMath;
import org.battelle.clodhopper.util.ArrayIntIterator;
import org.battelle.clodhopper.util.Sorting;

import gnu.trove.list.array.TIntArrayList;

import java.util.*;

public class KDTreeSeeder extends RandomSeeder {

	private static final int SAMPLING_LIMIT = 10000;
	
	public KDTreeSeeder (long seed, Random random) {
		super(seed, random);
	}
	
	public KDTreeSeeder() {
		this(System.nanoTime(), new Random());
	}
	
	@Override
	public TupleList generateSeeds(TupleList tuples, int seedCount) {
        
		if (seedCount <= 0) {
            throw new IllegalArgumentException();
        }

		random.setSeed(seed);
		
        int coordCount = tuples.getTupleCount();
        int coordLen = tuples.getTupleLength();

        int samplingLimit = Math.min(coordCount, SAMPLING_LIMIT);

        int[] indices = new int[coordCount];
        for (int i = 0; i < coordCount; i++) {
            indices[i] = i;
        }

        if (samplingLimit < coordCount) {
            for (int i = 0, m = coordCount; m > 0; i++, m--) {
                int j = i + random.nextInt(m);
                if (i != j) {
                    indices[i] ^= indices[j];
                    indices[j] ^= indices[i];
                    indices[i] ^= indices[j];
                }
            }
            int[] tmpIndices = new int[samplingLimit];
            System.arraycopy(indices, 0, tmpIndices, 0, samplingLimit);
            indices = tmpIndices;
        }

        // The number of splits necessary to generate a sufficient number of
        // bottom nodes for seeding the protoclusters. The formala assumes
        // the tree doesn't run out of unique coordinates before splitting
        // down
        // to the level chosen. That is, it assumes no leaf nodes are
        // generated.
        // Leaf nodes cannot be split.
        int splits = (int) Math.ceil(Math.log((double) seedCount)
                / Math.log(2.0));

        // Create a kd-tree split down to the necessary level.
        KDTreeNode root = KDTreeNode.createKDTree(tuples, indices, splits);

        // Get the nodes at the bottom level.
        KDTreeNode[] nodes = root.getNodesAtLevel(splits);
        int numNodes = nodes.length;

        // If the number of nodes is insufficient, it's because duplicate
        // coordinates caused leaf nodes to be formed before the split level
        // was reached, so the number of nodes at the split level was less
        // than expected.
        if (numNodes < seedCount) {

            List<KDTreeNode> nodeList = new ArrayList<KDTreeNode>(seedCount);
            for (int i = 0; i < numNodes; i++) {
                nodeList.add(nodes[i]);
            }

            // Get the leaf nodes above the split level.
            KDTreeNode[] leafNodes = root.getLeafNodes(splits - 1);
            int numLeafNodes = leafNodes.length;
            for (int i = 0; i < numLeafNodes; i++) {
                nodeList.add(leafNodes[i]);
            }

            leafNodes = null; // Done with it.

            // Keep splitting another level until we have enough nodes or
            // there are no more nodes to split.
            while (nodeList.size() < seedCount) {
                // Split another level. This will recurse down to the
                // unsplit
                // non-leaf nodes and split them.
                root.split(++splits);
                KDTreeNode[] tmp = root.getNodesAtLevel(splits);
                if (tmp.length == 0) {
                    // Nothing could be split because all nodes at the
                    // bottom
                    // were leaves.
                    // Break out of the while or will be stuck in an
                    // infinite loop.
                    break;
                }
                for (int i = 0; i < tmp.length; i++) {
                    nodeList.add(tmp[i]);
                }
            }

            numNodes = nodeList.size();
            nodes = new KDTreeNode[numNodes];
            nodeList.toArray(nodes);
        }

        if (seedCount < numNodes) {
            // Not all nodes can be used to seed the clusters. Need to
            // shuffle so the ones used will be random.
            for (int i = numNodes - 1; i > 0; i--) {
                int j = random.nextInt(i + 1);
                if (i != j) {
                    KDTreeNode tmp = nodes[i];
                    nodes[i] = nodes[j];
                    nodes[j] = tmp;
                }
            }
        }
        
        int seedsFound = Math.min(seedCount, numNodes);

        TupleList seeds = new ArrayTupleList(coordLen, seedsFound);
        double[] buffer = new double[coordLen];
        
        for (int i = 0; i < seedsFound; i++) {
            seeds.setTuple(i, nodes[i].getCenter(buffer));
        }

        return seeds;
	}

	static class KDTreeNode {
	
		private TupleList tuples;
		private int level;
		private HyperRect rect;
		private int[] indexes;
		private double[] center;
		private int splitDim;
		private double splitValue;
		
		private KDTreeNode left, right;
		
		private KDTreeNode(TupleList tuples, int[] indexes, int level) {
			
			if (tuples == null) {
				throw new NullPointerException();
			}
			if (level < 0) {
				throw new IllegalArgumentException("level < 0: " + level);
			}
			
			final int tupleLen = tuples.getTupleLength();
			int numTuples = 0;
			
			if (indexes != null) {
				numTuples = indexes.length;
				this.indexes = indexes;
			} else {
				if (level != 0) {
					throw new IllegalArgumentException("indexes must be supplied for nodes other than the root");
				}
				numTuples = tuples.getTupleCount();
				this.indexes = new int[numTuples];
				for (int i=0; i<numTuples; i++) {
					this.indexes[i] = i;
				}
			}
			
			if (numTuples == 0) {
				throw new IllegalArgumentException("0 tuples");
			}
			
			this.tuples = tuples;
			this.level = level;
			
			double[] maxCorner = TupleMath.maxCorner(tuples, new ArrayIntIterator(this.indexes));
			double[] minCorner = TupleMath.minCorner(tuples, new ArrayIntIterator(this.indexes));
			
			rect = new HyperRect(tupleLen);
			
			double[] spreads = new double[tupleLen];
			int[] dimensions = new int[tupleLen];

			for (int i=0; i<tupleLen; i++) {
				double max = maxCorner[i];
				double min = minCorner[i];
				if (Double.isNaN(max)) {
					throw new IllegalArgumentException("all values NaN for dimension " + i);
				}
				rect.setMaxCornerElement(i, max);
				rect.setMinCornerElement(i, min);
				
				spreads[i] = max - min;
				dimensions[i] = i;
			}
			
			this.splitDim = -1;
			
			if (!rect.isPoint()) {

				// Sort by the spreads, but rearrange the dimension indexes
				// in tandem.
				Sorting.parallelSort(spreads, dimensions);
				
				int sd = dimensions[tupleLen - 1];
				
				double median = TupleMath.median(tuples, sd, new ArrayIntIterator(this.indexes)); 
			
				this.splitDim = sd;
				this.splitValue = median;
			}
			
			this.center = TupleMath.average(tuples, new ArrayIntIterator(this.indexes));
		}
		
		public static KDTreeNode createKDTree(TupleList tuples, int[] indexes, int splits) {
			KDTreeNode root = new KDTreeNode(tuples, indexes, 0);
			root.split(splits);
			return root;
		}
		
		public KDTreeNode[] getNodesAtLevel(int level) {
			List<KDTreeNode> nodeList = new ArrayList<KDTreeNode> ();
			_getNodesAtLevel(nodeList, level);
			return nodeList.toArray(new KDTreeNode[nodeList.size()]);
		}
		
		private void _getNodesAtLevel(List<KDTreeNode> nodeList, int level) {
			if (level == this.level) {
				nodeList.add(this);
			} else if (level > this.level) {
				if (this.left != null) {
					this.left._getNodesAtLevel(nodeList, level);
					this.right._getNodesAtLevel(nodeList, level);
				}
			}
		}
		
		public KDTreeNode[] getLeafNodes(int maxLevel) {
			List<KDTreeNode> nodeList = new ArrayList<KDTreeNode> ();
			_getLeafNodes(nodeList, maxLevel);
			return nodeList.toArray(new KDTreeNode[nodeList.size()]);
		}
		
		private void _getLeafNodes(List<KDTreeNode> nodeList, int maxLevel) {
			if (this.level <= maxLevel) {
				if (isLeaf()) {
					nodeList.add(this);
				} else if (this.level < maxLevel) {
					if (this.left != null) {
						this.left._getLeafNodes(nodeList, maxLevel);
						this.right._getLeafNodes(nodeList, maxLevel);
					}
				}
			}
		}
		
		public double[] getCenter(double[] buffer) {
			double[] result = buffer != null && buffer.length == center.length ? buffer : new double[center.length];
			System.arraycopy(this.center, 0, result, 0, this.center.length);
			return result;
		}

		public void split(int splits) {
			
			if (splits <= 0 || isLeaf()) {
				return;
			}
			
			if (!isSplit()) {
				
				TIntArrayList leftList = new TIntArrayList();
				TIntArrayList rightList = new TIntArrayList();
				
				int n = this.indexes.length;
				
				for (int i=0; i<n; i++) {
					int ndx = this.indexes[i];
					if (this.tuples.getTupleValue(ndx, this.splitDim) <= this.splitValue) {
						leftList.add(ndx);
					} else {
						rightList.add(ndx);
					}
				}
				
				if (leftList.size() == 0 || rightList.size() == 0) {
					System.err.printf("got a problem....\n");
				}
				
				this.left = new KDTreeNode(tuples, leftList.toArray(), this.level + 1);
				this.right = new KDTreeNode(tuples, rightList.toArray(), this.level + 1);
			}
			
			splits--;
			
			if (splits > 0) {
				if (!this.left.isLeaf()) {
					this.left.split(splits);
				}
				if (!this.right.isLeaf()) {
					this.right.split(splits);
				}
			}
		}
		
		public boolean isLeaf() {
			return this.splitDim == -1;
		}
		
		public boolean isSplit() {
			return this.left != null;
		}
	}
	
	static class HyperRect {
		
		private double[] minCorner;
		private double[] maxCorner;
		
		public HyperRect(int dimensions) {
			minCorner = new double[dimensions];
			maxCorner = new double[dimensions];
		}
		
		public int getDimensions() {
			return minCorner.length;
		}
		
		public double getMinCornerElement(int n) {
			return minCorner[n];
		}
		
		public void setMinCornerElement(int n, double d) {
			minCorner[n] = d;
		}
		
		public double getMaxCornerElement(int n) {
			return maxCorner[n];
		}
		
		public void setMaxCornerElement(int n , double d) {
			maxCorner[n] = d;
		}
		
		public boolean isPoint() {
			final int dim = getDimensions();
			for (int i=0; i<dim; i++) {
				if (minCorner[i] != maxCorner[i]) return false;
			}
			return true;
		}
	}
}
