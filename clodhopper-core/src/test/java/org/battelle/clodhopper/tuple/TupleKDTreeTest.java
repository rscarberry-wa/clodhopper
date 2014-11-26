package org.battelle.clodhopper.tuple;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.battelle.clodhopper.distance.DistanceMetric;
import org.battelle.clodhopper.distance.EuclideanDistanceMetric;
import org.junit.Test;

public class TupleKDTreeTest {

  /* Timing the building of a kd-tree by iteratively doubling the tuple count.
   * With a doubling of N, the time taken typically goes up 2.4X.
   */
  /*
  @Test
  public void testBuildKDTreeTiming() {
    
    int tupleCount = 1000;
    int tupleLength = 10;
    
    DistanceMetric distMetric = new EuclideanDistanceMetric();
    
    long lastMsec = 0L;
    
    while(tupleCount < 1000000) {
      
      int numClusters = (int) Math.sqrt(tupleCount);
      
      TupleList tuples = generateTestTuples(tupleCount, tupleLength, numClusters);
      
      long tm1 = System.currentTimeMillis();
      
      TupleKDTree kdTree = TupleKDTree.forTupleList(tuples, distMetric);
      
      long msec = System.currentTimeMillis() - tm1;
      
      System.out.printf("%d tuples: time to generate KD-Tree = %d msec\n", tupleCount, msec);
      
      if (lastMsec > 0L) {
        System.out.printf("\tratio to last loop = %5.2f\n", ((double) msec)/lastMsec);
      }
      
      tupleCount *= 2;
      lastMsec = msec;
      
      System.gc();
    }
  }
  */
  
  @Test
  public void testForTupleList() {
    int tupleCount = 100;
    int numClusters = 10;
    int tupleLength = 10;
    int nnCount = 5;
    
    TupleList tuples = generateTestTuples(tupleCount, tupleLength, numClusters, 123L);
    DistanceMetric distMetric = new EuclideanDistanceMetric();

    TupleKDTree kdTree = TupleKDTree.forTupleList(tuples, distMetric);

    Random random = new Random(123L);
    int ndx = random.nextInt(tupleCount);
    
    List<TupleKDTree.DistanceEntry> distEntries = sortedDistanceEntries(ndx, tuples, distMetric);
    System.out.printf("Using dumb searching, the %d nearest neighbors are:\n", nnCount);
    final int lim = Math.min(nnCount, distEntries.size());

    int[] dumbNns = new int[lim];
    for (int i=0; i<lim; i++) {
      TupleKDTree.DistanceEntry entry = distEntries.get(i);
      dumbNns[i] = entry.getIndex();
    }
    
    int[] nns = kdTree.nearest(ndx, nnCount);

    for (int i=0; i<lim; i++) {
        assertTrue(dumbNns[i] == nns[i]);
    }
    
    double[] buf1 = new double[tupleLength];
    double[] buf2 = new double[tupleLength];
    
    tuples.getTuple(ndx, buf1);
    tuples.getTuple(nns[nns.length - 1], buf2);
        
    double md = distMetric.distance(buf1, buf2);
    int[] closeto = kdTree.closeTo(ndx, md);
    
    System.out.println("    md  = " + md);
    System.out.println("    ndx = " + ndx);
    System.out.println("    nns = " + toStr(nns));
    System.out.println("closeTo = " + toStr(closeto));
  
    TupleKDTree.KDNode node = kdTree.getRoot();
    System.out.println("root balance factor = " + node.balanceFactor());
  }
  
  @Test
  public void testForTupleListBalanced() {
      
    int tupleCount = 100;
    int numClusters = 10;
    int tupleLength = 10;
    int nnCount = 5;
    
    TupleList tuples = generateTestTuples(tupleCount, tupleLength, numClusters, 123L);
    DistanceMetric distMetric = new EuclideanDistanceMetric();

    TupleKDTree kdTree = TupleKDTree.forTupleListBalanced(tuples, distMetric);

    Random random = new Random(123L);
    int ndx = random.nextInt(tupleCount);
    
    List<TupleKDTree.DistanceEntry> distEntries = sortedDistanceEntries(ndx, tuples, distMetric);
    System.out.printf("Using dumb searching, the %d nearest neighbors are:\n", nnCount);
    final int lim = Math.min(nnCount, distEntries.size());

    int[] dumbNns = new int[lim];
    for (int i=0; i<lim; i++) {
      TupleKDTree.DistanceEntry entry = distEntries.get(i);
      dumbNns[i] = entry.getIndex();
    }
    
    int[] nns = kdTree.nearest(ndx, nnCount);

    for (int i=0; i<lim; i++) {
        assertTrue(dumbNns[i] == nns[i]);
    }
    
    double[] buf1 = new double[tupleLength];
    double[] buf2 = new double[tupleLength];
    
    tuples.getTuple(ndx, buf1);
    tuples.getTuple(nns[nns.length - 1], buf2);
     
    double md = distMetric.distance(buf1, buf2);
    int[] closeto = kdTree.closeTo(ndx, md);
    
    System.out.println("    md  = " + md);
    System.out.println("    ndx = " + ndx);
    System.out.println("    nns = " + toStr(nns));
    System.out.println("closeTo = " + toStr(closeto));
 
    TupleKDTree.KDNode node = kdTree.getRoot();
    System.out.println("root balance factor = " + node.balanceFactor());
  }

  @Test
  public void testNearestNdxNum() {
      
    int tupleCount = 5;
    int numClusters = 1;
    int tupleLength = 10;
    int nnCount = tupleCount - 1;
    
    TupleList tuples = generateTestTuples(tupleCount, tupleLength, numClusters, 123L);
    DistanceMetric distMetric = new EuclideanDistanceMetric();

    TupleKDTree kdTree = TupleKDTree.forTupleList(tuples, distMetric);
    
    for (int i=0; i<tupleCount; i++) {
        int[] nn = kdTree.nearest(i, nnCount);
        BitSet bits = new BitSet(tupleCount);
        for (int j=0; j<nn.length; j++) {
            int bit = nn[j];
            // A nn id should only be included once in the results.
            assertFalse(bits.get(bit));
            bits.set(bit);
        }
        // The search id shouldn't be among the nearest neighbors if asking for  
        // fewer nearest neighbors than in the tree.
        assertFalse(bits.get(i));
        
        // There should be this many nearest neighbors.
        assertTrue(bits.cardinality() == nnCount);
    }
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

  private static TupleList generateTestTuples(int tupleCount, int tupleLength, int numClusters, long seed) {
    
    // The exemplars are the random points to use as the cluster centers.
    double[][] exemplars = new double[numClusters][tupleLength];
    
    Random random = new Random(seed);
    
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
