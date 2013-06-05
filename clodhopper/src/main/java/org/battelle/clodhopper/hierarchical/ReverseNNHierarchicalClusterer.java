/*
 * ReverseNNHierarchicalClusterTask.java
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

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.battelle.clodhopper.distance.CosineDistanceMetric;
import org.battelle.clodhopper.distance.DistanceMetric;
import org.battelle.clodhopper.distance.EuclideanDistanceMetric;
import org.battelle.clodhopper.task.ProgressHandler;
import org.battelle.clodhopper.tuple.TupleList;

/**
 * <p>
 * An implementation of hierarchical clustering which uses the
 * Reverse-nearest-neighbor approach. In each merge iteration, rather than
 * exhaustively searching for the two nearest nodes and merging them, RNN merges
 * the first pair of nodes found which are nearest neighbors. The RNN approach
 * can handle a vastly larger amount of records than standard hierarchical, but
 * does not give identical results.
 * </p>
 * 
 * <p>
 * Any <tt>distanceFunc</tt> implementation can be used with this class. By
 * definition, RNN hierarchical clustering with Euclidean distances is called
 * Ward's clustering. With cosine distances, the algorithm is called Group
 * Average clustering.
 * </p>
 * 
 * @author R.Scarberry
 * @since 1.0
 * 
 */
public class ReverseNNHierarchicalClusterer extends
		AbstractHierarchicalClusterer {

	public ReverseNNHierarchicalClusterer(TupleList tuples,
			HierarchicalParams params, Dendrogram dendrogram) {
		super(tuples, params, dendrogram);
	}

	public ReverseNNHierarchicalClusterer(TupleList tuples,
			HierarchicalParams params) {
		this(tuples, params, null);
	}

	@Override
	public String taskName() {
		DistanceMetric distanceMetric = params.getDistanceMetric();
		if (distanceMetric instanceof EuclideanDistanceMetric) {
			return "Ward's clustering";
		} else if (distanceMetric instanceof CosineDistanceMetric) {
			return "group average clustering";
		}
		return "reverse nearest neighbor hierarchical clustering using distance metric "
				+ distanceMetric.getClass().getSimpleName();
	}

	// Each element n holds the index of the nearest
	// neighbor to coordinate n, or -1 if not set.
	private int[] nearestNeighbors;

	// If nearestNeighbors[n] >= 0, nearestNeighborDistances[n]
	// holds the distance to n's nearest neighbor.
	private double[] nearestNeighborDistances;

	// Distance function used.
	private DistanceMetric distMetric;

	// For clusters of size > 1, holds the cluster centroids.
	private TIntObjectMap<double[]> centerMap;

	private BitSet unavailabilityBits;
	
	private ExecutorService threadPool;
	private List<DistanceCalculator> calculators;

	// Used by the DistanceCalculators -- these must be set by
	// nearestNeighbor() each time before invoking the calculators.
	private int currentDistIndex;
	private int currentDistSize;
	private double[] currentDistTupleValues;
	private double[] currentDistances;

	// Find nearest neighbor of coordinate with specified index.
	//
	private int nearestNeighbor(int index) throws Exception {

		int nn = this.nearestNeighbors[index];

		if (nn == -1) {

			final int tupleCount = this.tuples.getTupleCount();

			final int sz = this.dendrogram.nodeSize(index);

			if (sz > 1) {
				// Use the center from the center map. One HAS to be
				// in the map, otherwise there's a programming error.
				double[] center = this.centerMap.get(index);
				System.arraycopy(center, 0, this.currentDistTupleValues, 0, center.length);
			} else {
				// It's still a leaf node. Have to get the tuple.
				tuples.getTuple(index, this.currentDistTupleValues);
			}

			this.currentDistIndex = index;
			this.currentDistSize = sz;

			if (this.threadPool != null) {
				this.threadPool.invokeAll(this.calculators);
			} else {
				this.calculators.get(0).call();
			}

			double minDist = Double.MAX_VALUE;
			
			for (int ni = 0; ni < tupleCount; ni++) {
				if (ni != index && !this.unavailabilityBits.get(ni)) {
					double d = this.currentDistances[ni];
					if (d < minDist) {
						minDist = d;
						nn = ni;
					}
				}
			}

			this.nearestNeighbors[index] = nn;
			this.nearestNeighborDistances[index] = minDist;
		}

		return nn;
	}

	@Override
	protected void buildDendrogram() throws Exception {

		try {

			final int tupleCount = this.tuples.getTupleCount();
			final int tupleLength = this.tuples.getTupleLength();

			double beginP = getBeginProgress();
			double endP = getEndProgress();

			// To build a complete dendrogram, there are (tupleCount - 1)
			// merges.
			ProgressHandler ph = new ProgressHandler(this, beginP, endP, tupleCount - 1);
			if (endP > beginP) {
				ph.setMinProgressIncrement((endP - beginP) / 100);
			}
			
			ph.setMinTimeIncrement(500L);

			ph.postBegin();

			this.currentDistTupleValues = new double[tupleLength];
			this.currentDistances = new double[tupleCount];

			this.unavailabilityBits = new BitSet(tupleCount);

			// Put the tuple indexes into an array and shuffle them.
			int[] shuffledCoordIndices = new int[tupleCount];
			for (int i = 0; i < tupleCount; i++) {
				shuffledCoordIndices[i] = i;
			}

			Random r = new Random(this.params.getRandomSeed());

			for (int i = tupleCount - 1; i > 0; i--) {
				int j = r.nextInt(i + 1);
				if (i != j) {
					shuffledCoordIndices[i] ^= shuffledCoordIndices[j];
					shuffledCoordIndices[j] ^= shuffledCoordIndices[i];
					shuffledCoordIndices[i] ^= shuffledCoordIndices[j];
				}
			}
			
			this.centerMap = new TIntObjectHashMap<double[]>();

			int threadCount = params.getWorkerThreadCount();
			if (threadCount <= 0) {
				threadCount = Runtime.getRuntime().availableProcessors();
			}

			int tuplesSoFar = 0;

			// Element at n holds the index m of the nearest
			// neighbor of coordinate n. -1 indicates not
			// set.
			this.nearestNeighbors = new int[tupleCount];
			Arrays.fill(this.nearestNeighbors, -1);
			this.nearestNeighborDistances = new double[tupleCount];

			this.distMetric = this.params.getDistanceMetric();
			this.dendrogram = new Dendrogram(tupleCount);

			// Create the DistanceCalculators.
			this.calculators = new ArrayList<DistanceCalculator>(threadCount);

			// Need to apportion the work among the workers.
			for (int i = 0; i < threadCount; i++) {
				int tuplesForThisWorker = (int) Math
						.round(((double) tupleCount) * (i + 1) / threadCount)
						- tuplesSoFar;
				this.calculators.add(new DistanceCalculator(tuplesSoFar, tuplesSoFar
						+ tuplesForThisWorker));
				tuplesSoFar += tuplesForThisWorker;
			}

			assert tuplesSoFar == tupleCount;

			if (threadCount > 1) {
				this.threadPool = Executors.newFixedThreadPool(threadCount);
			}

			final double[] tupleBuf1 = new double[tupleLength];
			final double[] tupleBuf2 = new double[tupleLength];

			int currentIndexPos = 0;
			// Arbitrarily pick the starting point for the first search.
			int currentIndex = shuffledCoordIndices[currentIndexPos];
			
			while (!this.dendrogram.isFinished()) {

				checkForCancel();

				int nn = nearestNeighbor(currentIndex);

				if (nearestNeighbor(nn) == currentIndex) {
				
					int sz1 = this.dendrogram.nodeSize(currentIndex);
					int sz2 = this.dendrogram.nodeSize(nn);

					double[] buf1 = null, buf2 = null;
					if (sz1 > 1) {
						buf1 = this.centerMap.get(currentIndex);
					} else {
						buf1 = tupleBuf1;
						tuples.getTuple(currentIndex, buf1);
					}
					if (sz2 > 1) {
						buf2 = this.centerMap.get(nn);
					} else {
						buf2 = tupleBuf2;
						this.tuples.getTuple(nn, buf2);
					}

					int mergeIndex = this.dendrogram.mergeNodes(currentIndex, nn,
							this.nearestNeighborDistances[currentIndex]);

					int invalidatedIndex = mergeIndex == currentIndex ? nn : currentIndex;
					
					// Drop this id out of contention.
					this.unavailabilityBits.set(invalidatedIndex);

					if (this.centerMap.containsKey(invalidatedIndex)) {
						// Save some memory by not storing centers we no
						// longer need.
						this.centerMap.remove(invalidatedIndex);
					}

					double totalSz = sz1 + sz2;

					double[] center = null;
					if (sz1 > 1) {
						// Reuse the array.
						center = buf1;
					} else if (sz2 > 1) {
						// Reuse the array.
						center = buf2;
					} else {
						// Have to allocate a new centroid.
						center = new double[tupleLength];
					}

					for (int i = 0; i < tupleLength; i++) {
						center[i] = (sz1 * buf1[i] + sz2 * buf2[i]) / totalSz;
					}

					this.centerMap.put(mergeIndex, center);

					this.nearestNeighbors[mergeIndex] = -1;
					this.nearestNeighbors[invalidatedIndex] = -1;

					for (int i = 0; i < tupleCount; i++) {

						final int nni = this.nearestNeighbors[i];

						if (nni >= 0) {

							final int nsz = this.dendrogram.nodeSize(i);

							double[] buf = null;
							if (nsz > 1) {
								buf = this.centerMap.get(i);
								assert buf != null;
							} else {
								this.tuples.getTuple(i, tupleBuf1);
								buf = tupleBuf1;
							}

							double m = ((double) nsz * totalSz)
									/ (nsz + totalSz);
							double d = m * this.distMetric.distance(center, buf);

							// The old nearest neighbor was one of the nodes
							// that
							// were just merged. If it's moved closer, the
							// merged
							// node is still the nearest neighbor.
							if (nni == mergeIndex || nni == invalidatedIndex) {
								if (d <= this.nearestNeighborDistances[i]) {
									this.nearestNeighbors[i] = mergeIndex;
									this.nearestNeighborDistances[i] = d;
								} else {
									// It moved away, so will have to compute
									// all the distances to determine who the
									// new nearest neighbor is.
									this.nearestNeighbors[i] = -1;
								}
							} else {
								// The old nearest neighbor was neither of the
								// merged
								// nodes. If the merged node is now nearer than
								// the
								// old nearest neighbor, make it the new nearest
								// neighbor.
								if (d < this.nearestNeighborDistances[i]) {
									this.nearestNeighbors[i] = mergeIndex;
									this.nearestNeighborDistances[i] = d;
								}
							}

						}
					}

					if (!this.dendrogram.isFinished()) {
						
						// Set the beginning of the next NN search chain to be
						// the first index < nn.
						currentIndex = nn - 1;
						if (currentIndex < 0) {
							currentIndex = tupleCount - 1;
						}
						while (this.unavailabilityBits.get(currentIndex)) {
							currentIndexPos++;
							if (currentIndexPos >= tupleCount) {
								currentIndexPos = 0;
							}
							currentIndex = shuffledCoordIndices[currentIndexPos];
						}
						
					}

					ph.postStep();

				} else {

					currentIndex = nn;

				}
				
			}

			ph.postEnd();

		} finally {

			if (threadPool != null) {
				threadPool.shutdownNow();
				threadPool = null;
			}

			calculators.clear();
			calculators = null;

		}
	}

	// Used for parallel calculation of distances.
	//
	class DistanceCalculator implements Callable<Void> {

		private int startIndex, endIndex;
		private TupleList theTuples;
		private double[] buf;
		private DistanceMetric dm;

		DistanceCalculator(int startIndex, int endIndex) {
			this.startIndex = startIndex;
			this.endIndex = endIndex;
			this.theTuples = tuples;
			int dim = tuples.getTupleLength();
			buf = new double[dim];
			dm = params.getDistanceMetric().clone();
		}

		@Override
		public Void call() throws Exception {

			for (int i = startIndex; i < endIndex; i++) {
				
				if (i != currentDistIndex && !unavailabilityBits.get(i)) {
				
					double[] buffer = null;
					int sz = dendrogram.nodeSize(i);
					
					if (sz > 1) {
						buffer = centerMap.get(i);
						assert buffer != null;
					} else {
						buffer = this.buf;
						theTuples.getTuple(i, this.buf);
					}
					
					double d1 = dm.distance(currentDistTupleValues, buffer);					
					double m = ((double) currentDistSize * sz) / (currentDistSize + sz);
					currentDistances[i] = m * d1;
				}
			}

			return null;
		}

	}
}
