package org.battelle.clodhopper.examples.xmeans;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import org.battelle.clodhopper.Cluster;
import org.battelle.clodhopper.Clusterer;
import org.battelle.clodhopper.distance.DistanceMetric;
import org.battelle.clodhopper.examples.TupleGenerator;
import org.battelle.clodhopper.seeding.ClusterSeeder;
import org.battelle.clodhopper.task.*;
import org.battelle.clodhopper.tuple.ArrayTupleList;
import org.battelle.clodhopper.tuple.TupleList;
import org.battelle.clodhopper.xmeans.XMeansClusterer;
import org.battelle.clodhopper.xmeans.XMeansParams;

/**
 * This is a simple example of how to cluster data using an XMeansClusterer.  
 * 
 * @author R. Scarberry
 *
 */
public class SimpleXMeansDemo {

  /**
   * A simple method for computing clusters for an array of data using the default
   * xmeans parameters.
   * 
   * @param data the data to be clustered.
   * @param tupleLength the length of the tuples.
   * @param tupleCount the number of tuples.
   * @return
   */
  public static List<Cluster> simpleClusterWithXMeans(double[] data, int tupleLength, int tupleCount) {
    
    // Wrap the data in an ArrayTupleList.
    TupleList tupleData = new ArrayTupleList(tupleLength, tupleCount, data);
    
    // Construct the parameters.
    XMeansParams params = new XMeansParams();
   
    // Display the default xmeans parameters.
    //
    ClusterSeeder seeder = params.getClusterSeeder();
    DistanceMetric distMetric = params.getDistanceMetric();
    int maxClusters = params.getMaxClusters();
    int minClusters = params.getMinClusters();
    double minClusterToMeanThreshold = params.getMinClusterToMeanThreshold();
    boolean useOverallBIC = params.getUseOverallBIC();
    int workerThreadCount = params.getWorkerThreadCount();
    
    System.out.println("XMeans Default Parameters:\n");
    System.out.printf("\tCluster seeding type = %s\n", seeder.getClass().getSimpleName());
    System.out.printf("\tDistance metric = %s\n", distMetric.getClass().getSimpleName());
    System.out.printf("\tmin clusters = %d, max clusters = %d\n", minClusters, maxClusters);
    System.out.printf("\tmin cluster to mean threshold = %f\n", minClusterToMeanThreshold);
    System.out.printf("\tUsing overall BIC = %s\n", String.valueOf(useOverallBIC));
    System.out.printf("\tWorker thread count = %d\n\n", workerThreadCount);
    
    // Construct the XMeansClusterer
    Clusterer xmeans = new XMeansClusterer(tupleData, params);
    
    // Add a listener for task life cycle events.
    // 
    xmeans.addTaskListener(new TaskListener() {

      @Override
      public void taskBegun(TaskEvent e) {
        System.out.printf("%s\n\n", e.getMessage());
      }

      @Override
      public void taskMessage(TaskEvent e) {
        System.out.println("  ... " + e.getMessage());
      }

      @Override
      public void taskProgress(TaskEvent e) {
        // Reports the progress.  Ignore for this example.
      }

      @Override
      public void taskPaused(TaskEvent e) {
        // Reports that the task has been paused. Won't happen in this example,
        // so ignore.
      }

      @Override
      public void taskResumed(TaskEvent e) {
        // Reports when a paused task has been resumed.
        // Ignore for this example.
      }

      @Override
      public void taskEnded(TaskEvent e) {
        // Reports when a task has finished regardless of whether the task succeeded or 
        // failed. Check the task outcome to know if it succeeded.
        System.out.printf("\n%s\n\n", e.getMessage());
      }
      
    });
    
    // Launch xmeans on another thread.
    Thread t = new Thread(xmeans);
    t.start();
    
    List<Cluster> clusters = null;
    
    try {
    
      // Since a Clusterer implements Future<List<Cluster>>, it has a blocking get() method.
      // 
      clusters = xmeans.get();
    
      // Don't have to worry about these, because the task outcome will tell you what happened.
    } catch (InterruptedException e1) {
    } catch (ExecutionException e1) {
    }

    
    if (xmeans.getTaskOutcome() == TaskOutcome.SUCCESS) {
      System.out.println("XMeans was successful!");
    } else if (xmeans.getTaskOutcome() == TaskOutcome.ERROR) {
      System.out.println("XMeans failed with the error: " + xmeans.getErrorMessage());
    } else if (xmeans.getTaskOutcome() == TaskOutcome.CANCELLED) {
      System.out.println("XMeans was somehow canceled, even though this method doesn't provide a path to cancellation.");
    } else {
      System.out.println("XMeans finished with unexpected outcome " + xmeans.getTaskOutcome() + ": please submit a bug report!");
    }
    
    return clusters;
  }
  
  public static void main(String[] args) {
    
    // Generate some test data using a TupleGenerator. This is randomly-generated data having the
    // characteristics we request.
    //
    int tupleCount = 4000;
    int tupleLength = 100;
    int clusterCount = 10;
    
    // The ratio between the largest sized cluster and the smallest. For all clusters to be the
    // same size, use 1.0.
    double clusterMultiplier = 4.0;
    
    // The mean standard deviation used for the clusters, 
    // and also the standard deviation of those standard deviations.
    double standardDev = 0.1;
    
    // This is a task mean to be executed on a background thread, but we'll be sleazy and call its 
    // run method directly for this example.
    TupleGenerator tupleGenerator = new TupleGenerator(tupleLength, tupleCount, clusterCount, 
        clusterMultiplier, standardDev, standardDev, new Random());
    
    // Not normally done for a long running task.  It'll block until finished.
    //
    tupleGenerator.run();
    
    // Before getting the data, check the outcome.
    //
    if (tupleGenerator.getTaskOutcome() == TaskOutcome.SUCCESS) {
      
      TupleList tuples = tupleGenerator.getTuples();
      
      // Place the data into a 1-D array in order to call the demo method.
      //
      double[] data = new double[tupleLength * tupleCount];
      
      // For scooping the data from the tupleList.
      double[] buffer = new double[tupleLength];
      
      int offset = 0;
      
      for (int i=0; i<tupleCount; i++) {
        tuples.getTuple(i, buffer);
        System.arraycopy(buffer, 0, data, offset, tupleLength);
        offset += tupleLength;
      }
      
      // No longer needed.
      tuples = null;
      
      // Test the method.
      //
      List<Cluster> clusters = simpleClusterWithXMeans(data, tupleLength, tupleCount);
      
      if (clusters != null) {
        
        // May not be the same as clusterCount, since XMeans attempted to statistically discern 
        // the distribution.
        //
        final int xmeansClusterCount = clusters.size();
        
        System.out.printf("\nXMeans Generated %d Clusters\n", xmeansClusterCount);
        
        for (int i=0; i<xmeansClusterCount; i++) {
          
          StringBuilder sb = new StringBuilder("[");
          
          Cluster c = clusters.get(i);
          
          int clusterSize = c.getMemberCount();
          double[] center = c.getCenter();
          
          for (int j=0; j<tupleLength; j++) {
            if (j > 0) {
              sb.append(", ");
            }
            sb.append(String.format("%5.2f", center[j]));
          }
          sb.append("]");
          
          System.out.printf("Cluster %d: size = %d, center = %s\n", (i+1), clusterSize, sb.toString());
        }
        
      }
      
    } else {
      
      System.out.printf("The TupleGenerator finished with outcome %s\n", tupleGenerator.getTaskOutcome());
      
    }
  }
}
