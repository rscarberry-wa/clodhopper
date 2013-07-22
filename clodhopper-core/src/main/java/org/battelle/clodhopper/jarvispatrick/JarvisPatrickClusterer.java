package org.battelle.clodhopper.jarvispatrick;

import gnu.trove.list.array.TIntArrayList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.battelle.clodhopper.AbstractClusterer;
import org.battelle.clodhopper.Cluster;
import org.battelle.clodhopper.task.ProgressHandler;
import org.battelle.clodhopper.tuple.TupleKDTree;
import org.battelle.clodhopper.tuple.TupleList;
import org.battelle.clodhopper.tuple.TupleMath;
import org.battelle.clodhopper.util.ArrayIntIterator;

public class JarvisPatrickClusterer extends AbstractClusterer {

  private TupleList tuples;
  private JarvisPatrickParams params;

  public JarvisPatrickClusterer(TupleList tuples, JarvisPatrickParams params) {
    if (tuples == null || params == null) {
      throw new NullPointerException();
    }
    this.tuples = tuples;
    this.params = params;
  }

  @Override
  public String taskName() {
    return "Jarvis-Patrick clustering";
  }

  @Override
  protected List<Cluster> doTask() throws Exception {

    final int tupleCount = tuples.getTupleCount();

    // tupleCount steps for computing nearest neighbors.
    // tupleCount*(tupleCount - 1)/2 for the nested i-j for-loops for
    // assignment.
    // 1 step for assigning the remaining unassigned tuples
    // 1 step for constructing the final clusters.
    //
    int totalSteps = tupleCount + tupleCount * (tupleCount - 1) / 2 + 2;

    if (tupleCount == 0) {
      finishWithError("zero tuples");
    }

    ProgressHandler ph = new ProgressHandler(this, totalSteps);
    ph.postBegin();

    int nearestNeighborsToExamine = params.getNearestNeighborsToExamine();
    if (nearestNeighborsToExamine > tupleCount - 1) {
      // Can't exceed the number of neighbors that exist.
      ph.postMessage(String
          .format(
              "reducing nearest neighbors to examine to %d, the number of possible neighbors",
              tupleCount - 1));
      nearestNeighborsToExamine = tupleCount - 1;
    }

    int nearestNeighborOverlap = params.getNearestNeighborOverlap();
    if (nearestNeighborOverlap > nearestNeighborsToExamine) {
      ph.postMessage(String
          .format(
              "reducing nearest neighbor overlap to %d, the number of nearest neighbors to examine",
              nearestNeighborsToExamine));
      nearestNeighborOverlap = nearestNeighborsToExamine;
    }

    boolean mutualNN = params.getMutualNearestNeighbors();

    // Use this array to keep track of the cluster assignments. -1 means
    // not assigned yet.
    int[] clusterAssignments = new int[tupleCount];
    Arrays.fill(clusterAssignments, -1);

    // For now, assume we can compute the nearest neighbors of every
    // tuple once up front. (Might have to revisit this issue for large
    // numbers of tuples and large numbers for nearestNeighborsToExamine)
    int[][] nearestNeighbors = computeNearestNeighbors(ph,
        nearestNeighborsToExamine);

    List<TIntArrayList> clusterMemberships = new ArrayList<TIntArrayList>();

    for (int i = 0; i < tupleCount; i++) {

      int icluster = clusterAssignments[i];

      int[] inearestNeighbors = nearestNeighbors[i];

      for (int j = i + 1; j < tupleCount; j++) {

        int jcluster = clusterAssignments[j];

        // If they're already both in the same cluster, there's no
        // reason to test further.
        //
        if (icluster < 0 || jcluster < 0 || icluster != jcluster) {

          // j must be one of i's nearest neighbors
          if (!mutualNN || Arrays.binarySearch(inearestNeighbors, j) >= 0) {

            int[] jnearestNeighbors = nearestNeighbors[j];

            // And i must be one of j's nearest neighbors.
            if (!mutualNN || Arrays.binarySearch(jnearestNeighbors, i) >= 0) {
              // If the overlap is large enough, place them in the same cluster.
              if (overlapAtLeast(inearestNeighbors, jnearestNeighbors,
                  nearestNeighborOverlap)) {
                // If they're already assigned to different clusters, merge
                // them.
                if (icluster >= 0 && jcluster >= 0) {
                  icluster = mergeClusters(icluster, jcluster,
                      clusterMemberships, clusterAssignments);
                  // Assign to i's cluster
                } else if (icluster >= 0) {
                  assignToCluster(j, icluster, clusterMemberships);
                  clusterAssignments[j] = icluster;
                  // Assign to j's cluster
                } else if (jcluster >= 0) {
                  assignToCluster(i, jcluster, clusterMemberships);
                  clusterAssignments[i] = jcluster;
                  icluster = jcluster;
                  // Assign them both to a new cluster, since neither are in a
                  // cluster already.
                } else {
                  icluster = assignToNextCluster(i, clusterMemberships);
                  assignToCluster(j, icluster, clusterMemberships);
                  clusterAssignments[i] = clusterAssignments[j] = icluster;
                }
              }
            }
          }
        }

        ph.postStep();
      }

    }

    // There still may be unassigned tuples, because they didn't have a large
    // enough nn overlap
    // with any of the other tuples. Assign them to their own clusters.
    for (int i = 0; i < tupleCount; i++) {
      if (clusterAssignments[i] < 0) {
        clusterAssignments[i] = assignToNextCluster(i, clusterMemberships);
      }
    }

    ph.postStep();

    List<Cluster> clusters = new ArrayList<Cluster>();

    for (TIntArrayList memberList : clusterMemberships) {
      memberList.trimToSize();
      if (memberList.size() > 0) {
        int[] members = memberList.toArray();
        Arrays.sort(members);
        double[] center = TupleMath.average(tuples, new ArrayIntIterator(
            members));
        clusters.add(new Cluster(members, center));
      }
    }

    ph.postStep();

    ph.postEnd();

    return clusters;
  }

  private int assignToNextCluster(int index,
      List<TIntArrayList> clusterMemberships) {
    int nextCluster = clusterMemberships.size();
    TIntArrayList memberList = new TIntArrayList();
    memberList.add(index);
    clusterMemberships.add(memberList);
    return nextCluster;
  }

  private void assignToCluster(int index, int cluster,
      List<TIntArrayList> clusterMemberships) {
    clusterMemberships.get(cluster).add(index);
  }

  private int mergeClusters(int index1, int index2,
      List<TIntArrayList> clusterMemberships, int[] clusterAssignments) {
    int newIndex = Math.min(index1, index2);
    int oldIndex = Math.max(index1, index2);
    if (newIndex != oldIndex) {
      TIntArrayList list1 = clusterMemberships.get(newIndex);
      TIntArrayList list2 = clusterMemberships.get(oldIndex);
      list1.addAll(list2);
      int sz = list2.size();
      for (int i = 0; i < sz; i++) {
        clusterAssignments[list2.get(i)] = newIndex;
      }
      list2.clear();
    }
    return newIndex;
  }

  private int[][] computeNearestNeighbors(ProgressHandler ph,
      int nearestNeighborsToExamine) {

    final int tupleCount = tuples.getTupleCount();
    int[][] nearestNeighbors = new int[tupleCount][];

    // A KD-Tree provides an efficient way of quickly looking up nearest
    // neighbors.
    TupleKDTree kdTree = TupleKDTree.forTupleList(tuples,
        params.getDistanceMetric());

    for (int i = 0; i < tupleCount; i++) {
      // The nn indexes come back sorted by distance, not by index.
      int[] nn = kdTree.nearest(i, nearestNeighborsToExamine);
      // These must be sorted by index for overlapAtLeast() to work properly.
      Arrays.sort(nn);
      nearestNeighbors[i] = nn;
      ph.postStep();
    }

    return nearestNeighbors;
  }

  private boolean overlapAtLeast(int[] arr1, int[] arr2, int minOverlap) {

    final int len1 = arr1.length;
    final int len2 = arr2.length;

    int i = 0;
    int j = 0;
    int overlap = 0;
    while (i < len1 && j < len2) {
      if (arr1[i] < arr2[j]) {
        i++;
      } else if (arr1[i] > arr2[j]) {
        j++;
      } else {
        overlap++;
        if (overlap >= minOverlap) {
          return true;
        }
        i++;
        j++;
      }
    }

    return false;
  }
}
