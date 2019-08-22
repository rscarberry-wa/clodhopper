package org.battelle.clodhopper.dbscan;

import gnu.trove.list.TIntList;
import org.battelle.clodhopper.AbstractClusterer;
import org.battelle.clodhopper.Cluster;
import org.battelle.clodhopper.tuple.TupleKDTree;
import org.battelle.clodhopper.tuple.TupleList;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import java.io.File;
import java.util.ArrayList;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.battelle.clodhopper.ClusterStats;
import org.battelle.clodhopper.distance.DistanceCacheFactory;
import org.battelle.clodhopper.tuple.TupleMath;
import org.battelle.clodhopper.util.ArrayIntIterator;

public class DBSCANClusterer extends AbstractClusterer {

    private static final int NOISE = -2;
    private static final int UNASSIGNED = -1;

    private TupleList tuples;
    private DBSCANParams params;
    
    private DBSCANClassification tupleClassification;

    public DBSCANClusterer(final TupleList tuples, final DBSCANParams params) {
        Objects.requireNonNull(tuples);
        Objects.requireNonNull(params);
        this.tuples = tuples;
        this.params = params;
    }

    @Override
    protected List<Cluster> doTask() throws Exception {
        final int tupleCount = tuples.getTupleCount();

        if (tupleCount == 0) {
            finishWithError("zero tuples");
        }

        final TupleKDTree kdTree = TupleKDTree.forTupleListBalanced(
                tuples, params.getDistanceMetric());

        final int[] clusterAssignments = new int[tupleCount];
        Arrays.fill(clusterAssignments, UNASSIGNED);

        final double epsilon = params.getEpsilon();
        // kdTree.closeTo() doesn't return the point itself, it just returns
        // indices to other points in the neighborhood.
        final int minPoints = params.getMinSamples() - 1;

        int clusterNum = 0;
        
        TIntSet noiseIds = new TIntHashSet();
        TIntSet edgeIds = new TIntHashSet();
        TIntSet coreIds = new TIntHashSet();

        for (int i=0; i<tupleCount; i++) {
            if (clusterAssignments[i] == UNASSIGNED) {
                int[] neighbors = kdTree.closeTo(i, epsilon);
                if (neighbors.length < minPoints) {
                    // Call it noise for now, but it may turn out to be
                    // an edge point.
                    clusterAssignments[i] = NOISE;
                    noiseIds.add(i);
                } else {
                    coreIds.add(i);
                    // For keeping duplicates out of the list
                    TIntSet neighborSet = new TIntHashSet(neighbors);
                    TIntList neighborList = new TIntArrayList(neighbors);
                    // Had sufficient points to make a new cluster.
                    clusterAssignments[i] = clusterNum;
                    // The size of the list may grow during execution of the loop.
                    for (int j=0; j<neighborList.size(); j++) {
                        int nbr = neighborList.get(j);
                        if (clusterAssignments[nbr] == NOISE) {
                            // It's not noise after all, but an edge point on the cluster.
                            clusterAssignments[nbr] = clusterNum;
                            // Remember to remove it from the noiseIds
                            noiseIds.remove(nbr);
                            edgeIds.add(nbr);
                        } else if (clusterAssignments[nbr] == UNASSIGNED) {
                            clusterAssignments[nbr] = clusterNum;
                            int[] moreNeighbors = kdTree.closeTo(nbr, epsilon);
                            if (moreNeighbors.length >= minPoints) {
                                // It has sufficient number of neighbors to be core.
                                coreIds.add(nbr);
                                for (int k=0; k<moreNeighbors.length; k++) {
                                    int nbr2 = moreNeighbors[k];
                                    if (!neighborSet.contains(nbr2)) {
                                        neighborSet.add(nbr2);
                                        neighborList.add(nbr2);
                                    }
                                }
                            } else { // Doesn't have sufficient neighbors to be core.
                                edgeIds.add(nbr);
                            }
                        }
                    }
                    clusterNum++;
                }
            }
        }
        
        List<TIntArrayList> clusterMemberships = new ArrayList<>(clusterNum);
        for (int i=0; i<clusterNum; i++) {
            clusterMemberships.add(new TIntArrayList());
        }
        
        // Single tuple clusters for the noise points.
        List<Cluster> noiseClusters = new ArrayList<>();
        
        final TIntSet noisePoints = new TIntHashSet();
        
        for (int i=0; i<tupleCount; i++) {
            int assignment = clusterAssignments[i];
            if (assignment >= 0) {
                clusterMemberships.get(assignment).add(i);
            } else {
                assert assignment == NOISE;
                noiseClusters.add(
                    new Cluster(new int[] { i }, tuples.getTuple(i, null))
                );
                noisePoints.add(i); 
            }
        }
        
        List<Cluster> clusters = new ArrayList<>(
                clusterNum + noiseClusters.size());
        
        for (TIntArrayList memberList: clusterMemberships) {
            memberList.trimToSize();
            int[] members = memberList.toArray();
            Arrays.sort(members);
            double[] center = TupleMath.average(tuples, new ArrayIntIterator(members));
            clusters.add(new Cluster(members, center));
        }
        
        clusters.addAll(noiseClusters);
        
        this.tupleClassification = new DBSCANClassification(
                coreIds, edgeIds, noiseIds);
        
        List<Double> scores = 
                ClusterStats.computeSilhouetteCoefficients(
                        tuples, clusters, params.getDistanceMetric());
        
            postMessage("Clusters received the following silhouette scores:");
            int memberCount = 0;
            double weightedSum = 0;
            for (int i=0; i<clusters.size(); i++) {
                Cluster c = clusters.get(i);
                if (c.getMemberCount() == 1) {
                    break;
                }
                postMessage(String.format("\t%d: %f", i+1, scores.get(i).doubleValue()));
                weightedSum += scores.get(i).doubleValue() * c.getMemberCount();
                memberCount += c.getMemberCount();
            }
            if (memberCount > 0) {
                postMessage("  Overall score: " + weightedSum/memberCount);
            }
        
        return clusters;
    }

    @Override
    public String taskName() {
        return "DBSCAN clustering";
    }
    
    public Optional<DBSCANClassification> getTupleClassification() {
        return Optional.ofNullable(this.tupleClassification);
    }
}
