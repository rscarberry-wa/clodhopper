package org.battelle.clodhopper.dbscan;

import gnu.trove.list.TIntList;
import org.battelle.clodhopper.AbstractClusterer;
import org.battelle.clodhopper.Cluster;
import org.battelle.clodhopper.tuple.TupleKDTree;
import org.battelle.clodhopper.tuple.TupleList;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class DBSCANClusterer extends AbstractClusterer {

    private static final int NOISE = -2;
    private static final int UNASSIGNED = -1;

    private TupleList tuples;
    private DBSCANParams params;
    
    private TIntSet noisePoints;

    public DBSCANClusterer(final TupleList tuples, final DBSCANParams params) {
        Objects.requireNonNull(tuples);
        Objects.requireNonNull(params);
        this.tuples = tuples;
        this.params = params;
    }

    public boolean isNoise(int tupleIndex) {
        return noisePoints != null && noisePoints.contains(tupleIndex);
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

        for (int i=0; i<tupleCount; i++) {
            if (clusterAssignments[i] == UNASSIGNED) {
                int[] neighbors = kdTree.closeTo(i, epsilon);
                if (neighbors.length < minPoints) {
                    clusterAssignments[i] = NOISE;
                } else {
                    // For keeping duplicates out of the list
                    TIntSet neighborSet = new TIntHashSet(neighbors);
                    TIntList neighborList = new TIntArrayList(neighbors);
                    // Had sufficient points to make a new cluster.
                    clusterAssignments[i] = clusterNum;
                    // The size of the list may grow during execution of the loop.
                    for (int j=0; j<neighborList.size(); j++) {
                        int nbr = neighborList.get(j);
                        if (clusterAssignments[nbr] == NOISE) {
                            // It's an edge point on the cluster.
                            clusterAssignments[nbr] = clusterNum;
                        } else if (clusterAssignments[nbr] == UNASSIGNED) {
                            clusterAssignments[nbr] = clusterNum;
                            int[] moreNeighbors = kdTree.closeTo(nbr, epsilon);
                            if (moreNeighbors.length >= minPoints) {
                                for (int k=0; k<moreNeighbors.length; k++) {
                                    int nbr2 = moreNeighbors[k];
                                    if (!neighborSet.contains(nbr2)) {
                                        neighborSet.add(nbr2);
                                        neighborList.add(nbr2);
                                    }
                                }
                            }
                        }
                    }
                    clusterNum++;
                }
            }
        }

        return null;
    }

    @Override
    public String taskName() {
        return "DBSCAN clustering";
    }
}
