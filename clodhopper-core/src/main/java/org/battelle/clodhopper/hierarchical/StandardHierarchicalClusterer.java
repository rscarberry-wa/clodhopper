/*
 * StandardHierarchicalClusterTask.java
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.battelle.clodhopper.distance.DistanceCache;
import org.battelle.clodhopper.distance.DistanceCacheFactory;
import org.battelle.clodhopper.distance.DistanceMetric;
import org.battelle.clodhopper.task.ProgressHandler;
import org.battelle.clodhopper.tuple.TupleList;

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
 * StandardHierarchicalClusterer.java
 *
 *===================================================================*/
/**
 * <p>
 * Implementation of standard hierarchical clustering which takes advantage of
 * the presence of multiple processors.</p>
 */
public class StandardHierarchicalClusterer extends AbstractHierarchicalClusterer {

    public static final long DEFAULT_MEM_THRESHOLD = 128L * 1024L * 1024L;
    public static final long DEFAULT_FILE_THRESHOLD = 2L * 1024L * 1024L * 1024L;

	// Threshold that determines the number of coordinates whose
    // pairwise distances can be cached in RAM.  Defaulting to 128MB,
    // this equates to 5793 coordinates.  If there are more coordinates
    // than this, a file-based cache will have to be used.
    private long distanceCacheMemThreshold = DEFAULT_MEM_THRESHOLD;

	// The file threshold limits the number of coordinates that can be
    // hierarchically clustered.  By default, this threshold is 2GB, limiting
    // the number of coordinates to 23,170.
    private long distanceCacheFileThreshold = DEFAULT_FILE_THRESHOLD;

	// The directory in which to store cache files temporarily during the
    // construction of a new dendrogram.
    private File cacheFileLocation;

    public StandardHierarchicalClusterer(TupleList tuples,
            HierarchicalParams params,
            Dendrogram dendrogram) {
        super(tuples, params, dendrogram);
    }

    public StandardHierarchicalClusterer(TupleList tuples,
            HierarchicalParams params) {
        this(tuples, params, null);
    }

    /**
     * Returns the memory threshold for storing pairwise distances between
     * coordinates in RAM. If the memory required is greater than this
     * threshold, but less than the file cache threshold, the distances will be
     * cached in a file. If the memory required is even greater than the
     * distance cache file threshold, hierarchical clustering will fail.
     *
     * @return - the threshold as a number of bytes.
     */
    public long getDistanceCacheMemoryThreshold() {
        return distanceCacheMemThreshold;
    }

    /**
     * Sets the memory threshold for storing pairwise distances between
     * coordinates in RAM. If the memory required is greater than this
     * threshold, the distances must be cached in a file, as long as the memory
     * required is less than the file threshold. If the memory required is even
     * greater than the distance cache file threshold, hierarchical clustering
     * will fail.
     * 
     * @param threshold the maximum byte threshold for storing distances in memory.
     */
    public void setDistanceCacheMemoryThreshold(final long threshold) {
        distanceCacheMemThreshold = threshold;
    }

    /**
     * Returns the file threshold for storing pairwise distances between
     * coordinates. If the memory required is greater than this threshold,
     * hierarchical clustering will fail.
     *
     * @return - the threshold as a number of bytes.
     */
    public long getDistanceCacheFileThreshold() {
        return distanceCacheFileThreshold;
    }

    /**
     * Sets the memory threshold for storing distances from coordinates to
     * cluster centers in RAM. If the memory required is greater than this
     * threshold, the distances are not stored, but are computed as needed. On
     * platforms with large amounts of RAM, setting this value high may result
     * in better clustering speed on large coordinate sets. If not set, the
     * threshold defaults to 128MB.
     * 
     * @param threshold the maximum threshold in bytes for storing distances in a file.
     */
    public void setDistanceCacheFileThreshold(final long threshold) {
        distanceCacheFileThreshold = threshold;
    }

    /**
     * Gets the directory in which temporary distance cache files are to be
     * placed during construction of a new dendrogram.
     *
     * @return - the directory or null if not set.
     */
    public File getCacheFileLocation() {
        return cacheFileLocation;
    }

    /**
     * Set the directory in which temporary distance cache files are to be
     * placed during the construction of a new dendrogram. If the parameter is
     * null, the default temporary directory will be used. If location does not
     * exist, it will be created when necessary. Temporary files should not be
     * left in this directory, since they are deleted when no longer needed.
     *
     * @param location directory in which temporary distance caches are to be stored.
     * 
     * @throws IllegalArgumentException - if the location exists but is not a
     * directory.
     */
    public void setCacheFileLocation(final File location) {
        if (location != null && location.exists() && !location.isDirectory()) {
            throw new IllegalArgumentException("not a directory: " + location);
        }
        cacheFileLocation = location;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String taskName() {
        return "hierarchical clustering";
    }

    protected void buildDendrogram() throws Exception {

        ProgressHandler ph = new ProgressHandler(this);

        double beginP = this.getBeginProgress();
        double endP = this.getEndProgress();

        if (endP > beginP) {
            ph.setMinProgressIncrement((endP - beginP) / 100.0);
        }
        ph.setMinTimeIncrement(500L);

        ph.postBegin();

        double fracForCacheCreation = 0.05;
        double fracForInitDistances = 0.10;
        double fracForMerging = 0.85;

        int tupleCount = tuples.getTupleCount();

	// File used for cache and copy of the cache.  cacheFile is only used if
        // the pairwise distances are stored in a FileDistanceCache.  cacheFile2
        // is only used if optimizing the dendrogram.
        File cacheFile = null, cacheFile2 = null;

        Optional<DistanceCache> cache = Optional.empty();
        SubtaskManager mgr = null;

        try {

            ph.subsection(fracForCacheCreation);

            // Create a temp file for the cache, even though it might not be used.
            cacheFile = File.createTempFile("dcache", null, cacheFileLocation);
            cacheFile.deleteOnExit();

            dendrogram = new Dendrogram(tupleCount);

            ph.postMessage("creating new distance cache");

            if (tupleCount > 1) {
                cache = DistanceCacheFactory.newDistanceCache(tupleCount,
                        distanceCacheMemThreshold, distanceCacheFileThreshold, cacheFile);
            } 

            ph.postEnd();

            if (tupleCount > 1) {
                int numProcessors = params.getWorkerThreadCount();
                mgr = new SubtaskManager(numProcessors,
                        params, tuples, cache);
            }

            ph.subsection(fracForInitDistances);

            ph.postMessage("initializing distances in the cache");

            if (mgr != null) {
                mgr.initializeDistances();
            }

            ph.postEnd();

            boolean done = dendrogram.isFinished();
            // To hold the indices of the nearest neighbors to be merged in each iteration.
            int[] nnPair = new int[2];
            double[] nnDistance = new double[1];

            ph.subsection(fracForMerging, tupleCount - 1);

            ph.postMessage("merging nodes");

            while (!done) {

                if (!mgr.lookupNearestNeighbors(nnPair, nnDistance)) {
                    finishWithError("problem finding nearest neighbors");
                }

                int mergeID = dendrogram.mergeNodes(nnPair[0], nnPair[1], nnDistance[0]);

                done = dendrogram.isFinished();

                if (!done) {
                    mgr.updateDistances(mergeID);
                    mgr.updateNearestNeighbors();
                }

                ph.postStep();

            } // while

            ph.postEnd();

        } finally {

            cache = null;
            if (mgr != null) {
                mgr.shutdown();
            }

            // Clean up temporary files.
            if (cacheFile != null && cacheFile.exists()) {
                cacheFile.delete();
            }
            if (cacheFile2 != null && cacheFile2.exists()) {
                cacheFile2.delete();
            }
        }

    }

    private class SubtaskManager {

	// Codes for what the workers are currently doing.
        //
        // Nothing currently.
        static final int DOING_NOTHING = 0;
        // Initial pairwise distance computation -- done once.
        static final int INITIALIZING_DISTANCES = 1;
        // Recomputation of distances affected by the last merge.
        static final int UPDATING_DISTANCES = 2;
        // Updating of the dendrogram nodes.
        static final int UPDATING_NEAREST_NEIGHBORS = 3;

        // What the object is currently doing.
        private int doing = DOING_NOTHING;

	// The ThreadPool that runs the Workers when in multi-processor mode.
        // O/W, it is null.
        private ExecutorService threadPool;

        // The worker objects which implement Runnable.
        private List<Worker> workers;

	// Indices of nearest neighbors. The index of the nearest neighbor of
        // node n is
        // found at mNNIndices[n]
        private int[] nnIndices;
        // Nearest neighbor distances corresponding 1:1 with mNNIndices.
        private double[] nnDistances;

        private int mergeIndex, leftIndex, rightIndex;
        private int leftCount, rightCount;

        private int coordCount;
        private DistanceCache cache;
        private HierarchicalParams.Linkage linkage;

        // Constructor.
        SubtaskManager(int numWorkers,
            final HierarchicalParams params,
            final TupleList tuples,
            final Optional<DistanceCache> cache) {

            if (numWorkers <= 0) {
                throw new IllegalArgumentException("number of workers <= 0: "
                        + numWorkers);
            }

            this.cache = cache.orElse(null);
            this.coordCount = tuples.getTupleCount();

            nnIndices = new int[coordCount];
            Arrays.fill(nnIndices, -1); // -1 indicates "not assigned"
            nnDistances = new double[coordCount];
            Arrays.fill(nnDistances, Double.MAX_VALUE);

            this.linkage = params.getLinkage();

            long distanceCount = ((long) coordCount) * ((long) coordCount - 1L) / 2L;
            if (numWorkers > coordCount) {
                postMessage("reducing number of worker threads to the number of coordinates");
                numWorkers = coordCount;
            } else if (numWorkers > distanceCount) {
                postMessage("reducing number of worker threads to the number of distances");
                numWorkers = (int) distanceCount;
            }

            long distancesSoFar = 0L;
            int coordsSoFar = 0;

            // Create the Updaters.
            this.workers = new ArrayList<Worker>(numWorkers);

            // Need to apportion the work among the workers.
            for (int i = 0; i < numWorkers; i++) {

                long distancesForThisWorker = Math.round(((double) (distanceCount * (i + 1))) / numWorkers)
                        - distancesSoFar;

                int coordsForThisWorker = (int) Math.round(((double) coordCount) * (i + 1) / numWorkers) - coordsSoFar;

                this.workers.add(new Worker(distancesSoFar, distancesForThisWorker,
                        coordsSoFar, coordsForThisWorker));

                distancesSoFar += distancesForThisWorker;
                coordsSoFar += coordsForThisWorker;
            }

            if (numWorkers > 1) {
                this.threadPool = Executors.newFixedThreadPool(numWorkers);
            }
        }

		// Null the items that could be consuming large amounts of
        // memory.
        protected void finalize() {
            this.cache = null;
        }

		// Called to stop the threads of the thread pool, which would otherwise
        // keep waiting for another request to do something.
        void shutdown() {
            if (threadPool != null) {
                threadPool.shutdownNow();
            }
        }

        /**
         * Find the nearest neighbor pair, placing the indices into the provided
         * array of length 2.
         *
         * @param indices array into which to place the nearest neighbor indexes.
         * @param distance 1 element array to hold the nearest neighbor distance.
         * 
         * @return - true if a pair is found.
         */
        boolean lookupNearestNeighbors(int[] indices, double[] distance) {
            boolean found = false;
            double dmin = Double.MAX_VALUE;
            int index1 = -1, index2 = -1;
            int len = nnIndices.length;
            for (int i = 0; i < len; i++) {
                int nindex = nnIndices[i];
                if (nindex >= 0) {
                    double d = nnDistances[i];
                    if (d < dmin) {
                        index1 = i;
                        index2 = nindex;
                        dmin = d;
                        found = true;
                    }
                }
            }
            if (found) {
                indices[0] = Math.min(index1, index2);
                indices[1] = Math.max(index1, index2);
                distance[0] = dmin;
            }
            return found;
        }

        boolean initializeDistances() throws Exception {
            doing = INITIALIZING_DISTANCES;
            return work();
        }

        boolean updateDistances(int mergeID) throws Exception {

            mergeIndex = mergeID;

            // One of these, usually the left, is the same as mMergeIndex.
            leftIndex = dendrogram.leftChildID(mergeIndex);
            rightIndex = dendrogram.rightChildID(mergeIndex);

            leftCount = dendrogram.nodeSize(leftIndex);
            rightCount = dendrogram.nodeSize(rightIndex);

			// The usual convention is for the merge index to be the lesser of
            // the left child index and the right child index.  Since the merge index
            // is definitely one of these, the count for the child that merge index
            // is equal to is too large.  It's actually the count of the merged
            // node and needs to be adjusted downward.
            //
            if (leftIndex == mergeIndex) {
                leftCount -= rightCount;
                // The other index is no longer in contention.
                nnIndices[rightIndex] = -1;
            } else {
                rightCount -= leftCount;
                // The other index is no longer in contention.
                nnIndices[leftIndex] = -1;
            }

            doing = UPDATING_DISTANCES;
            return work();
        }

        boolean updateNearestNeighbors() throws Exception {
            doing = UPDATING_NEAREST_NEIGHBORS;
            return work();
        }

        // Perform the current task -- mDoing should be set to the proper value.
        private boolean work() throws Exception {
            boolean ok = false;
            if (threadPool != null) {
                threadPool.invokeAll(workers);
                ok = true;
            } else {
                // Just call the single worker directly
                workers.get(0).call();
                ok = true;
            }
            return ok;
        }

		// Class that does the deeds.
        //
        private class Worker implements Callable<Void> {

            private int index1Min, index1Max;
            private int index2Min, index2Max;

            private int startTuple;
            private int tupleCount;

            // Working buffers
            private double[] buf1, buf2;

			// The coordinate set -- ref. to same object used by everything
            // else.
            // Set to prevent having to call getCoordinateSet() repeatedly.
            private TupleList theTuples;

			// Personal clone of the DistanceFunc, to avoid synchronization
            // problems.
            // All workers sharing the same object would probably work, but use
            // a clone
            // to be safe.
            private DistanceMetric distMetric;

            // Constructor
            Worker(long startDistance, long distanceCount,
                    int startTuple, int tupleCount) {

				// Set the endpoints for the indices. These are the indices
                // into the distance cache.
                int[] indices = DistanceCacheFactory.getIndicesForDistance(startDistance, cache);
                index1Min = indices[0];
                index2Min = indices[1];

                indices = DistanceCacheFactory.getIndicesForDistance(startDistance
                        + distanceCount - 1, cache);

                index1Max = indices[0];
                index2Max = indices[1];

                this.startTuple = startTuple;
                this.tupleCount = tupleCount;

                this.theTuples = tuples;
                buf1 = new double[theTuples.getTupleCount()];
                buf2 = new double[buf1.length];

                this.distMetric = params.getDistanceMetric().clone();
            }

            public Void call() throws Exception {
                switch (doing) {
                    case INITIALIZING_DISTANCES:
                        workerInitializeDistances();
                        break;
                    case UPDATING_DISTANCES:
                        workerUpdateDistances();
                        break;
                    case UPDATING_NEAREST_NEIGHBORS:
                        workerUpdateNearestNeighbors();
                        break;
                }

                return null;
            }

		    // Compute the distances.
            //
            private void workerInitializeDistances() {

                if (cache != null) {

                    final int setAtATime = 1024;
                    int[] indices1 = new int[setAtATime];
                    int[] indices2 = new int[setAtATime];
                    double[] distances = new double[setAtATime];
                    int count = 0;

                    int numIndices = cache.getNumIndices();

                    try {

                        for (int i = index1Min; i <= index1Max; i++) {

                            int jmin = i == index1Min ? index2Min : i + 1;
                            int jmax = i == index1Max ? index2Max : numIndices - 1;

                            for (int j = jmin; j <= jmax; j++) {

                                indices1[count] = i;
                                indices2[count] = j;

                                theTuples.getTuple(i, buf1);
                                theTuples.getTuple(j, buf2);

                                double distance = distMetric.distance(buf1, buf2);

		            			// These 2 if-blocks initialize the mNNDistances and
                                // mNNIndices.
                                if (distance < nnDistances[i]) {
                                    nnDistances[i] = distance;
                                    nnIndices[i] = j;
                                }
                                if (distance < nnDistances[j]) {
                                    nnDistances[j] = distance;
                                    nnIndices[j] = i;
                                }

                                distances[count++] = distance;

                                if (count == setAtATime) {
                                    cache.setDistances(indices1, indices2, distances);
                                    count = 0;
                                }

                                checkForCancel();

                            } // for (int j...

                        } // for (int i...

                        if (count > 0) {

                            if (count < setAtATime) {
                                indices1 = Arrays.copyOfRange(indices1, 0, count);
                                indices2 = Arrays.copyOfRange(indices2, 0, count);
                                distances = Arrays.copyOfRange(distances, 0, count);
                            }

                            cache.setDistances(indices1, indices2, distances);
                        }

                    } catch (IOException ioe) {

                        String errMsg = ioe.getMessage();
                        if (errMsg == null) {
                            errMsg = ioe.toString();
                        }
                        finishWithError("error initializing pairwise distances: " + errMsg);
                        return;

                    } catch (CancellationException ce) {
						// Ignore, since the thread running the cluster task
                        // will
                        // report the cancel.
                    }
                }
            }

			// Update nearest neighbors.
            //
            private void workerUpdateNearestNeighbors() {
                try {

                    int lim = startTuple + tupleCount;

                    for (int i = startTuple; i < lim; i++) {

                        int nnIndex = nnIndices[i];

                        if (nnIndex >= 0) {

                            if (i == mergeIndex || nnIndex == leftIndex || nnIndex == rightIndex) {

                                int newNNIndex = i;
                                double newNNDistance = Double.MAX_VALUE;

                                int n = nnIndices.length;
                                for (int j = i + 1; j < n; j++) {
                                    if (nnIndices[j] >= 0) {
                                        double d = cache.getDistance(i, j);
                                        if (d < newNNDistance) {
                                            newNNIndex = j;
                                            newNNDistance = d;
                                        }
                                    }
                                }

                                checkForCancel();

			                  // The "bug" discussed above will sometimes set a node's
                                // nearest neighbor id to itself with a nn distance of
                                // Double.MAX_VALUE.  But it won't cause any harm.
                                nnIndices[i] = newNNIndex;
                                nnDistances[i] = newNNDistance;

                            } // if (i == mMergeNodeID ...

                        } // if (mNodes[i] != null

                    } // for (int i=mStartNode...

                } catch (IOException ioe) {

                    String errMsg = ioe.getMessage();
                    if (errMsg == null) {
                        errMsg = ioe.toString();
                    }
                    finishWithError("error updating nearest neighbors: " + errMsg);

                } catch (CancellationException ce) {
			    	 // Ignore, since the thread running the cluster task
                    // will
                    // report the cancel.
                }
            }

            private void workerUpdateDistances() {

                try {

                    int lim = startTuple + tupleCount;

                    TIntArrayList getList1 = new TIntArrayList();
                    TIntArrayList getList2 = new TIntArrayList();

                    for (int i = startTuple; i < lim; i++) {
                        if (nnIndices[i] >= 0 && i != mergeIndex) {
                            getList1.add(i);
                            getList1.add(i);
                            getList2.add(leftIndex);
                            getList2.add(rightIndex);
                        }
                        checkForCancel();
                    }

                    getList1.trimToSize();
                    getList2.trimToSize();

                    int sz = getList1.size();

                    if (sz > 0) { // getList1 and getList2 are the same length

                        int[] indices1 = getList1.toArray();
                        int[] indices2 = getList2.toArray();

                        getList1.clear();
                        getList2.clear();

                        double[] distances = new double[sz];
                        cache.getDistances(indices1, indices2, distances);

                        // No longer need.
                        indices2 = null;

                        // sz is always even
                        double[] distancesToSet = new double[sz / 2];
                        int[] setIndices1 = new int[sz / 2];
                        Arrays.fill(setIndices1, mergeIndex);
                        int[] setIndices2 = new int[sz / 2];

                        int count = 0;
                        for (int i = 0; i < sz; i += 2) {
                            switch (linkage) {
                                case COMPLETE:
                                    distancesToSet[count] = Math.max(distances[i], distances[i + 1]);
                                    break;
                                case SINGLE:
                                    distancesToSet[count] = Math.min(distances[i], distances[i + 1]);
                                    break;
                                case MEAN:
                                    distancesToSet[count]
                                            = (leftCount * distances[i] + rightCount * distances[i + 1])
                                            / (leftCount + rightCount);
                                    break;
                                default:
                                    finishWithError("unsupported linkage type: " + linkage);
                            }

                            setIndices2[count] = indices1[i];
                            count++;
                            checkForCancel();
                        }

                        // No longer need.
                        indices1 = null;

                        cache.setDistances(setIndices1, setIndices2, distancesToSet);
                    }
                } catch (IOException ioe) {
                    String errMsg = ioe.getMessage();
                    if (errMsg == null) {
                        errMsg = ioe.toString();
                    }
                    finishWithError("error updating pairwise distances: " + errMsg);
                } catch (CancellationException ce) {
			    	 // Ignore, since the thread running the cluster task
                    // will
                    // report the cancel.
                }
            }
        }
    }
}
