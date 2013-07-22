package org.battelle.clodhopper.tuple;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.battelle.clodhopper.distance.DistanceMetric;
import org.battelle.clodhopper.distance.EuclideanDistanceMetric;
import org.junit.Test;

public class TupleKDTreeTest {

  @Test
  public void testForTupleList() {
    int tupleCount = 100;
    int numClusters = 10;
    int tupleLength = 10;
    int nnCount = 5;
    
    TupleList tuples = generateTestTuples(tupleCount, tupleLength, numClusters);
    DistanceMetric distMetric = new EuclideanDistanceMetric();

    TupleKDTree kdTree = TupleKDTree.forTupleList(tuples, distMetric);

    Random random = new Random(123L);
    int ndx = random.nextInt(tupleCount);
    
    List<TupleKDTree.DistanceEntry> distEntries = sortedDistanceEntries(ndx, tuples, distMetric);
    System.out.printf("Using dumb searching, the %d nearest neighbors are:\n", nnCount);
    final int lim = Math.min(nnCount, distEntries.size());
    for (int i=0; i<lim; i++) {
      TupleKDTree.DistanceEntry entry = distEntries.get(i);
      System.out.printf("\tIndex = %d, distance = %5.2f\n", entry.getIndex(), entry.getDistance());
    }
    
    int[] nns = kdTree.nearest(ndx, nnCount);
    
    double[] buf1 = new double[tupleLength];
    double[] buf2 = new double[tupleLength];
    
    tuples.getTuple(ndx, buf1);
    tuples.getTuple(nns[nns.length - 1], buf2);
    
    
    double md = distMetric.distance(buf1, buf2);
    int[] closeto = kdTree.closeTo(ndx, md);
    
    System.out.println("    ndx = " + ndx);
    System.out.println("    nns = " + toStr(nns));
    System.out.println("closeTo = " + toStr(closeto));
  
  }
  
  private static List<TupleKDTree.DistanceEntry> sortedDistanceEntries(int ndx, TupleList tuples, DistanceMetric distanceMetric) {
    
    final int tupleCount = tuples.getTupleCount();
    List<TupleKDTree.DistanceEntry> distEntries = new ArrayList<TupleKDTree.DistanceEntry>(tupleCount - 1);
    
    double[] buf1 = new double[tuples.getTupleLength()];
    double[] buf2 = new double[tuples.getTupleLength()];
    
    tuples.getTuple(ndx, buf1);
    
    for (int i=0; i<tupleCount; i++) {
      if (i != ndx) {
        tuples.getTuple(i, buf2);
        double dist = distanceMetric.distance(buf1, buf2);
        distEntries.add(new TupleKDTree.DistanceEntry(i, dist));
      }
    }
    
    Collections.sort(distEntries);
    
    return distEntries;
  }

  public static String toStr(int[] n) {
    StringBuilder sb = new StringBuilder();
    for (int i=0; i<n.length; i++) {
      if (i > 0) sb.append(", ");
      sb.append(n[i]);
    }
    return sb.toString();
  }

  private static TupleList generateTestTuples(int tupleCount, int tupleLength, int numClusters) {
    
    // The exemplars are the random points to use as the cluster centers.
    double[][] exemplars = new double[numClusters][tupleLength];
    
    Random random = new Random();
    
    for (int i=0; i<numClusters; i++) {
      double[] exemplar = exemplars[i];
      for (int j=0; j<tupleLength; j++) {
        exemplar[j] = random.nextDouble();
      }
    }
    
    TupleList tuples = new ArrayTupleList(tupleLength, tupleCount);
    double[] buffer = new double[tupleLength];
    
    for (int i=0; i<tupleCount; i++) {
      double[] exemplar = exemplars[random.nextInt(numClusters)];
      for (int j=0; j<tupleLength; j++) {
        // Add noise to each element.
        buffer[j] = exemplar[j] + random.nextDouble()*0.4 - 0.2;
      }
      tuples.setTuple(i, buffer);
    }
    
    return tuples;
  }
}
