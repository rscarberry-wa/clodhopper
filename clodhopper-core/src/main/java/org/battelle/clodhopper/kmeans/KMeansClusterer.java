package org.battelle.clodhopper.kmeans;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import org.battelle.clodhopper.AbstractClusterer;
import org.battelle.clodhopper.Cluster;
import org.battelle.clodhopper.ClusterStats;
import org.battelle.clodhopper.distance.DistanceMetric;
import org.battelle.clodhopper.seeding.ClusterSeeder;
import org.battelle.clodhopper.task.ProgressHandler;
import org.battelle.clodhopper.task.TaskOutcome;
import org.battelle.clodhopper.tuple.FilteredTupleList;
import org.battelle.clodhopper.tuple.TupleList;
import org.battelle.clodhopper.tuple.TupleMath;
import org.battelle.clodhopper.util.ArrayIntIterator;
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
 * KMeansClusterer.java
 *
 *===================================================================*/
public class KMeansClusterer extends AbstractClusterer {

    private static final int MOVES_TRACKING_WINDOW_LEN = 6;

    private TupleList tuples;
    private KMeansParams params;

    // Temporary cluster objects
    private ProtoCluster[] protoClusters;
    // To keep track of existing cluster assignments
    private int[] clusterAssignments;
    // Manages the subtasks performed by workers running in parallel
    private SubtaskManager subtaskManager;
	// To keep track of past states of the protoClusters to prevent getting caught in
    // an infinite loop near the end of clustering when replacing clusters that become empty.
    private Set<ProtoClusterState> pastStates;

    // Set to true if clustering does not appear to be converging to detect the case of
    // clustering oscillating between states.
    private boolean oscillationDetectionOn;

    public KMeansClusterer(TupleList tuples, KMeansParams params) {
        if (tuples == null || params == null) {
            throw new NullPointerException();
        }
        this.tuples = tuples;
        this.params = params;
    }

    @Override
    public String taskName() {
        return "k-means";
    }

    @Override
    public List<Cluster> doTask() throws Exception {

        List<Cluster> clusters = null;

        try {

            final int tupleCount = tuples.getTupleCount();
            final int requestedClusterCount = params.getClusterCount();

            // Do some error checking up front. The finishWithError() method
            // generates a TaskErrorException which pops us out of this method.
            if (tupleCount == 0) {
                finishWithError("zero tuples");
            }
            if (requestedClusterCount <= 0) {
                finishWithError("requested cluster count must be greater than 0: " + requestedClusterCount);
            }

            final int maxIterations = params.getMaxIterations();

            final int progressSteps = 2 * maxIterations;

            final ProgressHandler ph = new ProgressHandler(this, progressSteps);

            ph.postBegin();

            // Pick some initial centers based upon the seeding method.
            initializeCenters(ph);

            // The actual cluster count may be less than the requested cluster count. For example,
            // if the number of unique tuples is less than the requested cluster count, it will
            // be reduced.
            final int actualClusterCount = protoClusters.length;

            ph.postMessage(String.format("%d initial cluster centers selected", actualClusterCount));

            // The trivial case.  No work to do, since everything is to be in 1 cluster.
            //
            if (actualClusterCount == 1) {

                ProtoCluster cluster = protoClusters[0];
              
                // Add them all to the protocluster.
                cluster.ensureCurrentCapacity(tupleCount);
                for (int i = 0; i < tupleCount; i++) {
                    cluster.add(i);
                }
                
                cluster.updateCenter(tuples);

            } else {

                // Determine the number of worker threads for concurrent subtasks.
                final int workerCount = params.getWorkerThreadCount() > 0 ? 
                    params.getWorkerThreadCount() : Runtime.getRuntime().availableProcessors();

                subtaskManager = new SubtaskManager(workerCount);

                // Keeps a running count of the cluster assignments.
                clusterAssignments = new int[tupleCount];
                
                // -1 is a flag indicator meaning unassigned.
                Arrays.fill(clusterAssignments, -1);

                makeAssignments();

                ph.postMessage("initial cluster assignments have been made");
                ph.postStep();

                int moves = 0;
                int iteration = 0;
                boolean emptyClustersReplaced = false;

                final int movesGoal = params.getMovesGoal();
                final int iterationLimit = params.getMaxIterations();

                TIntArrayList moveDiffList = new TIntArrayList();
                List<List<Move>> pastMoveLists = null;
                int moveDiffListIndex = 0;
                boolean oscillationDetected = false;

                do {

                    emptyClustersReplaced = false;
                    oscillationDetected = false;

                    computeCenters();

                    int movesLast = moves;

                    moves = makeAssignments();

                    ph.postStep();

                    iteration++;
                    ph.postMessage(String.format("iteration %d: %d moves", iteration, moves));

                    if (oscillationDetectionOn) {

                        // Don't let it grow larger than MOVES_TRACKING_WINDOW_LEN.
                        if (pastMoveLists.size() == MOVES_TRACKING_WINDOW_LEN) {
                            pastMoveLists.remove(0);
                        }
                        pastMoveLists.add(subtaskManager.getMovesList());

                        oscillationDetected = hasOscillation(pastMoveLists);

                        if (oscillationDetected) {
                            ph.postMessage("oscillation between clustering states detected");
                        }

                    } else if (iteration > 1) {

                        if (moveDiffList.size() < MOVES_TRACKING_WINDOW_LEN) {
                            moveDiffList.add(moves - movesLast);
                        } else {
                            moveDiffList.set(moveDiffListIndex++, moves - movesLast);
                            if (moveDiffListIndex == MOVES_TRACKING_WINDOW_LEN) {
                                moveDiffListIndex = 0;
                            }
                        }

                        if (moveDiffList.size() == MOVES_TRACKING_WINDOW_LEN) {
                            int avg = 0;
                            for (int i = 0; i < MOVES_TRACKING_WINDOW_LEN; i++) {
                                avg += moveDiffList.get(i);
                            }
                            avg /= MOVES_TRACKING_WINDOW_LEN;
                            if (avg <= 2) {
                                oscillationDetectionOn = true;
                                pastMoveLists = new ArrayList<>();
                            }
                        }

                    }

                    if (moves <= movesGoal || iteration >= iterationLimit || oscillationDetected) {
                        emptyClustersReplaced = params.getReplaceEmptyClusters() && replaceEmptyClusters(ph);
                        if (emptyClustersReplaced) {

                            oscillationDetectionOn = false;
                            moveDiffList.clear();
                            moveDiffListIndex = 0;
                            pastMoveLists = null;

                            computeCenters();
                            int additionalMoves = makeAssignments();
                            ph.postMessage(String.format("after replacement of empty clusters, %d additional moves", additionalMoves));
                        }
                    }

                } while ((moves > movesGoal && iteration < iterationLimit && !oscillationDetected) || emptyClustersReplaced);
            }

            int emptyClustersDeleted = 0;
            clusters = new ArrayList<Cluster>(actualClusterCount);

            for (int c = 0; c < actualClusterCount; c++) {
                ProtoCluster cluster = protoClusters[c];
                if (!cluster.isEmpty()) {
                    int[] members = new int[cluster.currentSize];
                    System.arraycopy(cluster.currentMembers, 0, members, 0, cluster.currentSize);
                    clusters.add(new Cluster(members, cluster.center));
                } else {
                    emptyClustersDeleted++;
                }
            }

            if (emptyClustersDeleted > 0) {
                ph.postMessage(String.format(
                        "number of clusters was reduced to %d, because of %d clusters which became empty",
                        clusters.size(), emptyClustersDeleted));
            }

            ph.postEnd();

        } finally {

            protoClusters = null;
            pastStates = null;
            clusterAssignments = null;

            if (subtaskManager != null) {
                subtaskManager.shutdown();
                subtaskManager = null;
            }

        }

        return clusters;
    }

    private void initializeCenters(ProgressHandler ph) {

        ClusterSeeder seeder = params.getClusterSeeder();

        int clusterCount = params.getClusterCount();

        int uniqueTupleCount = TupleMath.uniqueTupleCount(tuples);

        // There is no point in requesting more clusters than there are unique tuples.
        if (clusterCount > uniqueTupleCount) {
            ph.postMessage(String.format("reducing requested number of clusters from %d to %d, the number of unique tuples",
                clusterCount, uniqueTupleCount));
            clusterCount = uniqueTupleCount;
        }

        TupleList seeds = seeder.generateSeeds(tuples, clusterCount);

        final int tupleLength = seeds.getTupleLength();
        final int seedCount = seeds.getTupleCount();

        protoClusters = new ProtoCluster[seedCount];

        for (int i = 0; i < seedCount; i++) {
            double[] center = new double[tupleLength];
            seeds.getTuple(i, center);
            protoClusters[i] = new ProtoCluster(center);
        }

    }

    private int makeAssignments() {
        int clusterCount = protoClusters.length;
        for (int c = 0; c < clusterCount; c++) {
            protoClusters[c].checkPoint();
        }
        subtaskManager.makeAssignments();
        return subtaskManager.getMoves();
    }

    private void computeCenters() {
        int clusterCount = protoClusters.length;
        for (int c = 0; c < clusterCount; c++) {
            ProtoCluster cluster = protoClusters[c];
            if (!cluster.isEmpty()) {
                cluster.setUpdateFlag();
            }
            checkForCancel();
        }
        subtaskManager.computeCenters();
    }

    private boolean replaceEmptyClusters(ProgressHandler ph) {

        boolean emptyClustersReplaced = false;

        int clusterCount = protoClusters.length;
        int emptyClusterCount = 0;
        for (int c = 0; c < clusterCount; c++) {
            if (protoClusters[c].isEmpty()) {
                emptyClusterCount++;
            }
        }

        if (emptyClusterCount > 0) {

            ProtoClusterState currentState = new ProtoClusterState(protoClusters);

            if (pastStates != null && pastStates.contains(currentState)) {
                ph.postMessage(String.format("since the current cluster state has been encountered before, "
                        + "%s empty clusters will not be replaced", emptyClusterCount));
                return false;
            }

            ph.postMessage(String.format("attempting the replacement of %d empty clusters", emptyClusterCount));

            if (pastStates == null) {
                pastStates = new HashSet<ProtoClusterState>();
            }

            // Add the current state, so ending up in the same state again will be detected.
            pastStates.add(currentState);

            int nonEmptyClusterCount = clusterCount - emptyClusterCount;

            // Accumulate the Baye's Information Criterion and indexes of the clusters that are not empty.
            final double[] bics = new double[clusterCount];
            int[] indexes = new int[nonEmptyClusterCount];

            int count = 0;
            for (int i = 0; i < clusterCount; i++) {
                ProtoCluster cluster = protoClusters[i];
                if (!cluster.isEmpty()) {
                    bics[i] = computeProtoClusterBIC(cluster);
                    indexes[count++] = i;
                }
            }

			// Sort the indexes of the non-empty clusters by their BICs.  The ones at the
            // bottom will be the best candidates to be split.
            Sorting.quickSort(indexes, new IntComparator() {
                @Override
                public int compare(int n1, int n2) {
                    double bic1 = bics[n1];
                    double bic2 = bics[n2];
                    return bic1 < bic2 ? -1 : bic1 > bic2 ? 1 : 0;
                }
            });

            count = 0;
            for (int i = 0; i < clusterCount; i++) {
                ProtoCluster cluster = protoClusters[i];
                if (cluster.isEmpty()) {
                    boolean replaced = false;
                    if (count < indexes.length) {
                        int splitNdx = indexes[count];
                        ProtoCluster clusterToSplit = protoClusters[splitNdx];
                        if (clusterToSplit.currentSize > 1) {

                            checkForCancel();

                            ProtoCluster[] newClusters = split(clusterToSplit, ph);

                            if (newClusters != null && newClusters.length == 2) {
                                protoClusters[splitNdx] = newClusters[0];
                                protoClusters[i] = newClusters[1];
                                replaced = true;
                                emptyClustersReplaced = true;
                            }
                        }

                        count++;
                    }

                    if (!replaced) {
                        cluster.setAssignmentCandidate(false);
                    }
                }
            }

        }

        return emptyClustersReplaced;
    }

    private ProtoCluster[] split(ProtoCluster cluster, ProgressHandler ph) {

        int[] memberIndexes = new int[cluster.currentSize];
        System.arraycopy(cluster.currentMembers, 0, memberIndexes, 0, cluster.currentSize);

        FilteredTupleList filteredTuples = new FilteredTupleList(memberIndexes, tuples);

        ClusterSeeder seeder = params.getClusterSeeder();

        KMeansParams splitterParams = new KMeansParams.Builder().
                clusterCount(2).
                workerThreadCount(params.getWorkerThreadCount()).
                distanceMetric(params.getDistanceMetric()).
                clusterSeeder(seeder).
                replaceEmptyClusters(false).build();

        KMeansClusterer splitter = new KMeansClusterer(filteredTuples, splitterParams);
        splitter.run();

        ProtoCluster[] result = null;

        TaskOutcome splitterOutcome = splitter.getTaskOutcome();

        if (splitterOutcome == TaskOutcome.SUCCESS) {

            List<Cluster> clusters = null;
            try {
                clusters = splitter.get();
                // Since the result has been verified to be success, neither exception should happen.
            } catch (InterruptedException e) {
            } catch (ExecutionException e) {
            }
            int sz = clusters != null ? clusters.size() : 0;
            result = new ProtoCluster[sz];
            for (int i = 0; i < sz; i++) {
                Cluster c = clusters.get(i);
                int clusterSz = c.getMemberCount();
                int[] members = new int[clusterSz];
                for (int j = 0; j < clusterSz; j++) {
                    // The cluster members have to be mapped back to the indexes of tuples.
                    members[j] = filteredTuples.getFilteredIndex(c.getMember(j));
                }
                result[i] = new ProtoCluster(members, c.getCenter());
            }

        } else if (splitterOutcome == TaskOutcome.ERROR) {

            String errorMessage = null;

            Throwable t = splitter.getError();

            if (t != null) {
                errorMessage = t.getMessage();
                if (errorMessage == null || errorMessage.length() == 0) {
                    errorMessage = t.getClass().getSimpleName() + " was thrown";
                }
            } else {
                errorMessage = "unknown cause";
            }

            ph.postMessage("splitting of cluster failed: " + errorMessage);
        }

        return result;
    }

    private int nearestCluster(int tupleNdx, double[] buffer, DistanceMetric distMetric) {

        int nearest = -1;
        double min = Double.MAX_VALUE;
        int lastNearest = clusterAssignments[tupleNdx];
        boolean onlyConsiderChanged = false;

        tuples.getTuple(tupleNdx, buffer);

		// If the last cluster to which the tuple was assigned did not change in the previous
        // iteration, performance is enormously enhanced by only considering the distance to it and
        // to the clusters that changed in the previous iteration.  The clusters that did not change
        // lost out to lastCluster before and they haven't moved any closer.
        //
        if (lastNearest >= 0) {

            ProtoCluster lastCluster = protoClusters[lastNearest];

            if (lastCluster.isAssignmentCandidate() && !lastCluster.getUpdateFlag()) {
                onlyConsiderChanged = true;
                nearest = lastNearest;
                min = distMetric.distance(buffer, lastCluster.center);
            }

        }

        final int clusterCount = protoClusters.length;
        for (int c = 0; c < clusterCount; c++) {
            ProtoCluster cluster = protoClusters[c];
            if (cluster.isAssignmentCandidate()) {
                if (!onlyConsiderChanged || cluster.getUpdateFlag()) {
                    double d = distMetric.distance(buffer, cluster.center);
                    if (d < min) {
                        min = d;
                        nearest = c;
                    }
                }
            }
        }

        return nearest;
    }

    private boolean hasOscillation(List<List<Move>> moveLists) {

        final int numLists = moveLists.size();

        if (numLists > 1) {

            TIntObjectMap<int[]> stateMap = new TIntObjectHashMap<int[]>();
            List<Move> lastList = moveLists.get(numLists - 1);

            for (Move mv : lastList) {
                stateMap.put(mv.getTupleIndex(), new int[]{
                    mv.getFromCluster(), mv.getToCluster()
                });
            }

            for (int i = numLists - 2; i >= 0; i--) {

                List<Move> moveList = moveLists.get(i);

                boolean checkMap = true;

                for (Move mv : moveList) {

                    int coordIndex = mv.getTupleIndex();

                    if (stateMap.containsKey(coordIndex)) {
                        int[] fromTo = stateMap.get(coordIndex);
                        // Would not make sense otherwise.
                        assert mv.getToCluster() == fromTo[0];
                        fromTo[0] = mv.getFromCluster();
                    } else {
                        stateMap.put(coordIndex, new int[]{mv.getFromCluster(), mv.getToCluster()});
                        // from and to for this coordinate cannot be the same, so no reason to check.
                        checkMap = false;
                    }

                }

                if (checkMap) {

                    // Check all the values in the map to see if from and to are the same.  This would indicate an 
                    // oscillation back to a previous membership state.
                    int[] coordIndexes = stateMap.keys();
                    boolean fromTosTheSame = true;

                    for (int j = 0; j < coordIndexes.length; j++) {
                        int[] fromTo = stateMap.get(coordIndexes[j]);
                        if (fromTo[0] != fromTo[1]) {
                            fromTosTheSame = false;
                            break;
                        }
                    }

                    if (fromTosTheSame) {
                        return true;
                    }

                }
            }

        }

        return false;
    }

    private double computeProtoClusterBIC(ProtoCluster protoCluster) {
        int[] membershipCopy = new int[protoCluster.currentSize];
        System.arraycopy(protoCluster.currentMembers, 0, membershipCopy, 0, protoCluster.currentSize);
        return ClusterStats.computeBIC(tuples, new Cluster(membershipCopy, protoCluster.center));
    }

    private class SubtaskManager {

        private final List<CenterComputationWorker> centerCompWorkers;
        private final List<AssignmentWorker> assignmentWorkers;

        // Non-null only if the number of worker threads > 1
        private ExecutorService threadPool;

        private SubtaskManager(int workerCount) {

            final int tupleCount = tuples.getTupleCount();
            final int clusterCount = protoClusters.length;

            final int assignmentWorkerCount = Math.min(workerCount, tupleCount);
            int[] tuplesPerAssignmentWorker = new int[assignmentWorkerCount];

            Arrays.fill(tuplesPerAssignmentWorker, tupleCount/assignmentWorkerCount);

            int leftOver = tupleCount%assignmentWorkerCount;
            for (int i = 0; i < leftOver; i++) {
                tuplesPerAssignmentWorker[i]++;
            }

            assignmentWorkers = new ArrayList<>(assignmentWorkerCount);

            int startTuple = 0;
            for (int i = 0; i < assignmentWorkerCount; i++) {
                int endTuple = startTuple + tuplesPerAssignmentWorker[i];
                assignmentWorkers.add(new AssignmentWorker(startTuple, endTuple));
                startTuple = endTuple;
            }

            final int centerCompWorkerCount = Math.min(workerCount, clusterCount);
            int[] clustersPerCenterCompWorker = new int[centerCompWorkerCount];
            Arrays.fill(clustersPerCenterCompWorker, clusterCount/centerCompWorkerCount);

            leftOver = clusterCount%centerCompWorkerCount;
            for (int i = 0; i < leftOver; i++) {
                clustersPerCenterCompWorker[i]++;
            }

            centerCompWorkers = new ArrayList<>(centerCompWorkerCount);
            int startCluster = 0;
            for (int i = 0; i < centerCompWorkerCount; i++) {
                int endCluster = startCluster + clustersPerCenterCompWorker[i];
                centerCompWorkers.add(new CenterComputationWorker(startCluster, endCluster));
                startCluster = endCluster;
            }

            if (assignmentWorkerCount > 1 || centerCompWorkerCount > 1) {
                threadPool = Executors.newFixedThreadPool(Math.max(assignmentWorkerCount, centerCompWorkerCount));
            }
        }

        private void shutdown() {
            if (threadPool != null) {
                threadPool.shutdownNow();
            }
        }

        private boolean makeAssignments() {
            boolean ok = false;
            if (threadPool != null) {
                try {
                    threadPool.invokeAll(assignmentWorkers);
                    ok = true;
                } catch (InterruptedException e) {
                    // Normal, if canceled during cluster assignment.
                }
            } else {
                try {
                    // Single-threaded, so just call directly.
                    assignmentWorkers.get(0).call();
                    ok = true;
                } catch (Exception e) {
                }
            }
            return ok;
        }

        private boolean computeCenters() {
            boolean ok = false;
            if (threadPool != null) {
                try {
                    threadPool.invokeAll(centerCompWorkers);
                    ok = true;
                } catch (InterruptedException e) {
                    // Normal, if canceled during cluster assignment.
                }
            } else {
                try {
                    // Single-threaded, so just call directly.
                    centerCompWorkers.get(0).call();
                    ok = true;
                } catch (Exception e) {
                }
            }
            return ok;
        }

        private int getMoves() {
            // Return the sum of the moves from the individual assignment workers.
            return assignmentWorkers.stream().map(AssignmentWorker::getMoves).reduce(0, (a, b) -> a + b);
        }

        private List<Move> getMovesList() {
            List<Move> movesList = null;
            if (oscillationDetectionOn) {
                movesList = new ArrayList<>();
                for (AssignmentWorker aw : assignmentWorkers) {
                    movesList.addAll(aw.getMovesList());
                }
            }
            return movesList;
        }

        private class CenterComputationWorker implements Callable<Void> {

            private int startCluster, endCluster;

            private CenterComputationWorker(int startCluster, int endCluster) {
                this.startCluster = startCluster;
                this.endCluster = endCluster;
            }

            public Void call() throws Exception {
                try {
                    for (int c = startCluster; c < endCluster; c++) {
                        checkForCancel();
                        ProtoCluster cluster = protoClusters[c];
                        if (cluster.getUpdateFlag()) {
                            cluster.updateCenter(tuples);
                        }
                    }
                } catch (CancellationException e) {
                    // Will be detected by the main execution thread.
                }
                return null;
            }
        }

        private class AssignmentWorker implements Callable<Void> {

            private int startTuple, endTuple;
            private double[] buffer;
            private DistanceMetric distanceMetric;
            private int moves;
            // Only set when oscillationDetectionOn == true.
            private List<Move> movesList;

            private AssignmentWorker(int startTuple, int endTuple) {
                this.startTuple = startTuple;
                this.endTuple = endTuple;
                this.buffer = new double[tuples.getTupleLength()];
                this.distanceMetric = (DistanceMetric) params.getDistanceMetric().clone();
            }

            private int getMoves() {
                return moves;
            }

            private List<Move> getMovesList() {
                return movesList;
            }

            @Override
            public Void call() throws Exception {
                try {
                    moves = 0;
                    if (oscillationDetectionOn) {
                        movesList = new ArrayList<>();
                    }
                    for (int i = startTuple; i < endTuple; i++) {
                        int c = nearestCluster(i, buffer, distanceMetric);
                        if (c >= 0) {
                            protoClusters[c].add(i);
                            if (clusterAssignments[i] != c) {
                                if (oscillationDetectionOn) {
                                    movesList.add(new Move(i, clusterAssignments[i], c));
                                }
                                clusterAssignments[i] = c;
                                moves++;
                            }
                        }
                    }
                } catch (CancellationException e) {
                    // Will be detected in main execution thread.
                }
                return null;
            }
        }
    }

    private static class ProtoCluster {

        private int[] previousMembers;

        private int[] currentMembers;
        private int currentSize;

        private double[] center;
        private boolean updateFlag;

        private boolean assignmentCandidate = true;

        private ProtoCluster(double[] center) {
            this.center = (double[]) center.clone();
        }

        private ProtoCluster(int[] members, double[] center) {
            this.currentMembers = (int[]) members.clone();
            this.center = (double[]) center.clone();
        }

        private int size() {
            return currentSize;
        }

        private int[] getMembers() {
            int[] result = new int[currentSize];
            if (currentSize > 0) {
                System.arraycopy(currentMembers, 0, result, 0, currentSize);
            }
            return result;
        }

	// This method must be synchronized, since it will be called from multiple threads by
        // the AssignmentWorkers.
        private synchronized void add(int newMember) {
            ensureCurrentCapacity(currentSize + 1);
            currentMembers[currentSize++] = newMember;
        }

        private void updateCenter(TupleList tuples) {
            // Other code assumes currentMembers is sorted.  This is a good spot to place the sort.
            trimToSizeAndSort();
            this.center = TupleMath.average(tuples, new ArrayIntIterator(currentMembers));
        }

        private boolean isEmpty() {
            return currentSize == 0;
        }

        private void setUpdateFlag() {
            boolean sameAsPrevious = true;
            int previousSize = previousMembers != null ? previousMembers.length : 0;
            if (currentSize != previousSize) {
                sameAsPrevious = false;
            } else {
                for (int i = 0; i < currentSize; i++) {
                    if (previousMembers[i] != currentMembers[i]) {
                        sameAsPrevious = false;
                        break;
                    }
                }
            }
            updateFlag = !sameAsPrevious;
        }

        private boolean getUpdateFlag() {
            return updateFlag;
        }

        private void checkPoint() {
            previousMembers = new int[currentSize];
            if (currentSize > 0) {
                System.arraycopy(currentMembers, 0, previousMembers, 0, currentSize);
                currentMembers = null;
                currentSize = 0;
            }
        }

        private boolean isAssignmentCandidate() {
            return assignmentCandidate;
        }

        private void setAssignmentCandidate(boolean b) {
            assignmentCandidate = b;
        }

        private void trimToSizeAndSort() {
            if (currentMembers == null || currentMembers.length != currentSize) {
                int[] temp = new int[currentSize];
                if (currentMembers != null) {
                    System.arraycopy(currentMembers, 0, temp, 0, currentSize);
                }
                Arrays.sort(temp);
                currentMembers = temp;
            }
        }

        private void ensureCurrentCapacity(int capacity) {
            int currentCapacity = currentMembers != null ? currentMembers.length : 0;
            if (currentCapacity < capacity) {
                int newCapacity = Math.max(7, 2 * currentCapacity);
                if (newCapacity < capacity) {
                    newCapacity = capacity;
                }
                int[] temp = new int[newCapacity];
                if (currentCapacity > 0) {
                    System.arraycopy(currentMembers, 0, temp, 0, currentCapacity);
                }
                currentMembers = temp;
            }
        }

    }

    private static class ProtoClusterState {

        private int[] members;
        private int[] sizes;

        private ProtoClusterState(final ProtoCluster[] protoClusters) {

            final int sz = protoClusters.length;
            int totalMemberCount = 0;

            int[] clusterIndexes = new int[sz];
            for (int i = 0; i < sz; i++) {
                clusterIndexes[i] = i;
                totalMemberCount += protoClusters[i].currentSize;
            }

            // This assumes the membership of the protoclusters is sorted.
            //
            Sorting.quickSort(clusterIndexes, (n1, n2) -> {

                    ProtoCluster c1 = protoClusters[n1];
                    ProtoCluster c2 = protoClusters[n2];

                    int min1 = c1.isEmpty() ? Integer.MAX_VALUE : c1.currentMembers[0];
                    int min2 = c2.isEmpty() ? Integer.MAX_VALUE : c2.currentMembers[0];

                    return min1 < min2 ? -1 : min1 > min2 ? 1 : 0;
                }
            );

            this.members = new int[totalMemberCount];
            this.sizes = new int[sz];

            int offset = 0;
            for (int i = 0; i < sz; i++) {
                int ndx = clusterIndexes[i];
                ProtoCluster cluster = protoClusters[ndx];
                int clusterSz = cluster.currentSize;
                sizes[i] = clusterSz;
                if (clusterSz > 0) {
                    System.arraycopy(cluster.currentMembers, 0, members, offset, clusterSz);
                    offset += clusterSz;
                }
            }
        }

        @Override
        public int hashCode() {
            int hc = 17;
            for (int i = 0; i < sizes.length; i++) {
                hc = 31 * hc + sizes[i];
            }
            for (int i = 0; i < members.length; i++) {
                hc = 31 * hc + members[i];
            }
            return hc;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (o != null && o.getClass() == this.getClass()) {
                ProtoClusterState other = (ProtoClusterState) o;
                if (other.sizes.length == this.sizes.length && other.members.length == this.members.length) {
                    int n = 0;
                    for (int i = 0; i < this.sizes.length; i++) {
                        if (other.sizes[i] != this.sizes[i]) {
                            return false;
                        }
                        int lim = n + sizes[i];
                        for (int j = n; j < lim; j++) {
                            if (other.members[j] != this.members[j]) {
                                return false;
                            }
                        }
                        n += sizes[i];
                    }
                    return true;
                }
            }
            return false;
        }
    }

    // Class used in detecting oscillations near the end of clustering, say from a few
    // tuples hopping from one cluster to another then back again.  This is a very rare
    // occurence that I've only seen when using cosine distances.
    private static class Move {

        private final int tupleIndex;
        private final int fromCluster;
        private final int toCluster;

        Move(int tupleIndex, int fromCluster, int toCluster) {
            this.tupleIndex = tupleIndex;
            this.fromCluster = fromCluster;
            this.toCluster = toCluster;
        }

        int getTupleIndex() {
            return tupleIndex;
        }

        int getFromCluster() {
            return fromCluster;
        }

        int getToCluster() {
            return toCluster;
        }
    }
}
