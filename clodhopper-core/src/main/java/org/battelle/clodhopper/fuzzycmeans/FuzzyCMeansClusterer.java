package org.battelle.clodhopper.fuzzycmeans;

import gnu.trove.list.array.TIntArrayList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.battelle.clodhopper.AbstractClusterer;
import org.battelle.clodhopper.Cluster;
import org.battelle.clodhopper.distance.DistanceMetric;
import org.battelle.clodhopper.seeding.ClusterSeeder;
import org.battelle.clodhopper.task.ProgressHandler;
import org.battelle.clodhopper.tuple.TupleList;
import org.battelle.clodhopper.tuple.TupleMath;
import org.battelle.clodhopper.util.ArrayIntIterator;

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
 * FuzzyCMeansClusterer.java
 *
 *===================================================================*/

/**
 * Implementation of the fuzzy c-means clustering algorithm, which allows a data
 * item to belong to multiple clusters simultaneously with varying degrees of
 * membership. The final list of clusters returned is formed by assigning each tuple to
 * the cluster for which its membership is largest.  However, the methods
 * <code>getDegreesOfMembership(int tupleIndex)</code> and 
 * <code>getClusterCenter(int clusterIndex)</code> are provided to obtain the fuzzy 
 * results.
 * 
 * <br />
 * See the following references: <br />
 * J. C. Dunn (1973): "A Fuzzy Relative of the ISODATA Process and Its Use in
 * Detecting Compact Well-Separated Clusters", Journal of Cybernetics 3: 32-57 <br />
 * J. C. Bezdek (1981):
 * "Pattern Recognition with Fuzzy Objective Function Algorithms", Plenum Press,
 * New York
 * 
 * @author R.Scarberry
 * @since 1.0
 * 
 */
public class FuzzyCMeansClusterer extends AbstractClusterer {

	private TupleList tuples;
	private FuzzyCMeansParams params;

	// Synchronization object for changing degreesOfMembership
	private Object mfLock = new Object();

	// The degrees of membership for every tuple to every cluster.  
	// Of dimensions [tupleCount][clusterCount]
	private double[][] degreesOfMembership;
	// The cluster centers.  Of dimensions [clusterCount][tupleLength]
	private double[][] clusterCenters;

	// The workers that update the degrees of membership
	private List<DegreesOfMembershipUpdater> domUpdaters;
	// The workers that update the cluster centers
	private List<ClusterCenterUpdater> centerUpdaters;
	// The workers that recompute the error after each iteration
	private List<ErrorCalculator> errorCalculators;

	// The thread pool.  Non-null only when using multiple threads
	private ExecutorService threadPool;

	// This may seem redundant, since the params has a clusterCount.  But
	// this is set to the actual clusterCount if params requests more 
	// than the number of unique seeds that can be generated.
	private int clusterCount;
	// Just duplicates an item in params.
	private double fuzziness;
	// Derived from fuzziness to reduce calculations.
	private double fuzzyPower;

	/**
	 * Constructor
	 * 
	 * @param tuples contains the data to be clustered.
	 * @param params contains the parameters.
	 */
	public FuzzyCMeansClusterer(TupleList tuples, FuzzyCMeansParams params) {
		if (tuples == null || params == null) {
			throw new NullPointerException();
		}
		this.tuples = tuples;
		this.params = params;
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public String taskName() {
		return "fuzzy c-means clustering";
	}
	
	/**
	 * For a given tuple, get the degrees of membership into the clusters.
	 * 
	 * @param tupleIndex
	 * 
	 * @return an array containing the degrees of membership.  The length is equal to the
	 *   number of clusters.  The values should sum to 1.0.
	 *  
	 */
	public double[] getDegreesOfMembership(int tupleIndex) {
		return (double[]) degreesOfMembership[tupleIndex].clone();
	}
	
	/**
	 * Returns the fuzzily-computed cluster center for a given index.  The centers returned
	 * by this method are generally different from the centers of the clusters returned by <code>getClusters()</code>, because
	 * the latter method assigned the tuples to one and only one cluster then computes the centers by averaging 
	 * the tuples.
	 * 
	 * @param clusterIndex
	 * 
	 * @return an array containing the cluster center.
	 */
	public double[] getClusterCenter(int clusterIndex) {
		return (double[]) clusterCenters[clusterIndex].clone();
	}

	@Override
	protected List<Cluster> doTask() throws Exception {

		List<Cluster> clusters = null;

		try {

			final int maxIterations = params.getMaxIterations();

			final int steps = 2 + maxIterations;

			ProgressHandler ph = new ProgressHandler(this, getBeginProgress(),
					getEndProgress(), steps);

			this.fuzziness = params.getFuzziness();
			this.fuzzyPower = 2.0 / (fuzziness - 1.0);

			final int tupleCount = tuples.getTupleCount();
			if (tupleCount == 0) {
				finishWithError("zero tuples");
			}

			int workerThreadCount = params.getWorkerThreadCount();

			if (workerThreadCount <= 0) {
				workerThreadCount = Runtime.getRuntime().availableProcessors();
			}

			domUpdaters = new ArrayList<DegreesOfMembershipUpdater>(
					workerThreadCount);
			errorCalculators = new ArrayList<ErrorCalculator>(workerThreadCount);

			int tuplesPerWorker = tupleCount / workerThreadCount;
			int startTuple = 0;
			for (int i = 0; i < workerThreadCount; i++) {
				int numTuples = i < (workerThreadCount - 1) ? tuplesPerWorker
						: tupleCount - startTuple;
				domUpdaters.add(new DegreesOfMembershipUpdater(startTuple,
						numTuples));
				errorCalculators
						.add(new ErrorCalculator(startTuple, numTuples));
				startTuple += numTuples;
			}

			// Initializes the centers and sets this.clusterCount, which may be
			// less than
			// than params.getClusterCount() because of too few unique tuples.
			//
			initializeCenters(ph);

			centerUpdaters = new ArrayList<ClusterCenterUpdater>(
					workerThreadCount);
			int clustersPerWorker = this.clusterCount / workerThreadCount;

			int startCluster = 0;
			for (int i = 0; i < workerThreadCount; i++) {
				int numClusters = i < (workerThreadCount - 1) ? clustersPerWorker
						: clusterCount - startCluster;
				centerUpdaters.add(new ClusterCenterUpdater(startCluster,
						numClusters));
				startCluster += numClusters;
			}

			if (workerThreadCount > 1) {
				threadPool = Executors.newFixedThreadPool(workerThreadCount);
			}

			this.degreesOfMembership = new double[tupleCount][this.clusterCount];

			updateDegreesOfMembership();

			ph.postStep();

			final double epsilon = params.getEpsilon();

			double prevError = calculateError();

			int iteration = 0;

			while (iteration < maxIterations) {

				this.checkForCancel();

				updateClusterCenters();

				this.checkForCancel();

				updateDegreesOfMembership();

				this.checkForCancel();

				double error = calculateError();

				ph.postStep();

				iteration++;

				double errorDelta = Math.abs(prevError - error);

				ph.postMessage(String.format(
						"iteration %d, error changed from %f to %f", iteration,
						prevError, error));

				prevError = error;

				if (errorDelta < epsilon) {
					break;
				}
			}

			ph.postMessage("generating final clusters");

			clusters = generateFinalClusters();

			ph.postMessage(clusters.size() + " clusters generated");

			ph.postEnd();

		} finally {

			if (this.threadPool != null) {
				this.threadPool.shutdownNow();
				this.threadPool = null;
			}

			this.domUpdaters = null;
			this.centerUpdaters = null;
			this.errorCalculators = null;

		}

		return clusters;
	}

	private void initializeCenters(ProgressHandler ph) {

		ph.postMessage("initializing cluster centers");

		ClusterSeeder seeder = params.getClusterSeeder();

		int clustCount = params.getClusterCount();

		// uniqueTupleCount <= clusterCount, since checkUniqueTupleCount() stops
		// counting
		// unique tuples when the count equals clusterCount.
		int uniqueTupleCount = TupleMath.checkUniqueTupleCount(tuples,
				clustCount);

		if (uniqueTupleCount < clustCount) {
			clustCount = uniqueTupleCount;
		}

		this.checkForCancel();

		TupleList seeds = seeder.generateSeeds(tuples, clustCount);

		this.checkForCancel();

		this.clusterCount = seeds.getTupleCount();
		this.clusterCenters = new double[this.clusterCount][tuples.getTupleLength()];
		
		for (int i = 0; i < this.clusterCount; i++) {
			seeds.getTuple(i, this.clusterCenters[i]);
		}

		if (this.clusterCount < clustCount) {
			ph.postMessage("number of clusters reduced to " + this.clusterCount
					+ ", the number of unique tuples");
		}
	}

	private void updateDegreesOfMembership() throws Exception {
		synchronized (mfLock) {
			if (this.threadPool != null) {
				this.threadPool.invokeAll(domUpdaters);
			} else {
				domUpdaters.get(0).call();
			}
		}
	}

	private double calculateError() throws Exception {

		if (this.threadPool != null) {
			this.threadPool.invokeAll(errorCalculators);
		} else {
			errorCalculators.get(0).call();
		}

		double error = 0.0;
		final int sz = errorCalculators.size();
		for (int i = 0; i < sz; i++) {
			error += errorCalculators.get(i).getError();
		}

		return error;
	}

	private void updateClusterCenters() throws Exception {
		if (this.threadPool != null) {
			this.threadPool.invokeAll(centerUpdaters);
		} else {
			this.centerUpdaters.get(0).call();
		}
	}

	private List<Cluster> generateFinalClusters() {

		TIntArrayList[] memberLists = new TIntArrayList[this.clusterCount];

		final int tupleCount = tuples.getTupleCount();

		for (int i = 0; i < tupleCount; i++) {
			double max = 0.0;
			int maxc = 0;
			for (int j = 0; j < this.clusterCount; j++) {
				double m = degreesOfMembership[i][j];
				if (j == 0 || m > max) {
					max = m;
					maxc = j;
				}
			}
			if (memberLists[maxc] == null) {
				memberLists[maxc] = new TIntArrayList();
			}
			memberLists[maxc].add(i);
		}

		List<Cluster> clist = new ArrayList<Cluster>(this.clusterCount);

		for (int i = 0; i < this.clusterCount; i++) {
			TIntArrayList memList = memberLists[i];
			if (memList != null) {
				memList.trimToSize();
				int[] members = memList.toArray();
				if (members.length > 0) {
					double[] center = TupleMath.average(tuples,
							new ArrayIntIterator(members));
					clist.add(new Cluster(members, center));
				}
			}
		}

		return clist;
	}

	class DegreesOfMembershipUpdater implements Callable<Void> {

		private int startTuple;
		private int numTuples;
		private DistanceMetric dm;

		DegreesOfMembershipUpdater(int startTuple, int numTuples) {
			this.startTuple = startTuple;
			this.numTuples = numTuples;
			this.dm = params.getDistanceMetric().clone();
		}

		@Override
		public Void call() throws Exception {
			final int lim = startTuple + numTuples;
			final int tupleLength = tuples.getTupleLength();

			final double[] buffer = new double[tupleLength];
			final double[] dists = new double[clusterCount];

			for (int i = startTuple; i < lim; i++) {

				tuples.getTuple(i, buffer);

				for (int j = 0; j < clusterCount; j++) {
					dists[j] = dm.distance(buffer, clusterCenters[j]);
				}

				for (int j = 0; j < clusterCount; j++) {

					double dist = dists[j];
					double denom = 0.0;

					for (int k = 0; k < clusterCount; k++) {
						double ratio = 1.0;
						if (k != j) {
							double dist2 = dists[k];
							ratio = dist / dist2;
						}
						denom += Math.pow(ratio, fuzzyPower);
					}

					degreesOfMembership[i][j] = 1.0 / denom;
				}
			}

			return null;
		}

	}

	class ClusterCenterUpdater implements Callable<Void> {

		private int startCluster;
		private int numClusters;

		ClusterCenterUpdater(int startCluster, int numClusters) {
			this.startCluster = startCluster;
			this.numClusters = numClusters;
		}

		@Override
		public Void call() throws Exception {

			final int lim = startCluster + numClusters;
			for (int i = startCluster; i < lim; i++) {
				Arrays.fill(clusterCenters[i], 0.0);
			}

			final int tupleCount = tuples.getTupleCount();
			final int tupleLength = tuples.getTupleLength();

			double[] buffer = new double[tupleLength];
			double[][] denoms = new double[numClusters][tupleLength];

			for (int i = 0; i < tupleCount; i++) {
				tuples.getTuple(i, buffer);

				for (int j = startCluster; j < lim; j++) {
					double m = degreesOfMembership[i][j];
					double f = Math.pow(m, fuzziness);
					double[] center = clusterCenters[j];
					double[] denom = denoms[j - startCluster];
					for (int k = 0; k < tupleLength; k++) {
						center[k] += f * buffer[k];
						denom[k] += f;
					}
				}
			}

			for (int i = startCluster; i < lim; i++) {
				double[] center = clusterCenters[i];
				double[] denom = denoms[i - startCluster];
				for (int j = 0; j < tupleLength; j++) {
					center[j] /= denom[j];
				}
			}

			return null;
		}
	}

	class ErrorCalculator implements Callable<Void> {

		private int startTuple;
		private int numTuples;
		private DistanceMetric dm;
		private double error;

		ErrorCalculator(int startTuple, int numTuples) {
			this.startTuple = startTuple;
			this.numTuples = numTuples;
			this.dm = params.getDistanceMetric().clone();
		}

		double getError() {
			return error;
		}

		@Override
		public Void call() throws Exception {

			double err = 0.0;

			final int tupleLength = tuples.getTupleLength();
			final double[] buffer = new double[tupleLength];
			final int lim = startTuple + numTuples;

			for (int i = startTuple; i < lim; i++) {
				tuples.getTuple(i, buffer);

				for (int j = 0; j < clusterCount; j++) {
					double dist = dm.distance(buffer, clusterCenters[j]);
					double m = degreesOfMembership[i][j];
					double mult = Math.pow(m, fuzziness);
					err += dist * mult;
				}
			}

			this.error = err;

			return null;
		}

	}
}
