package org.battelle.clodhopper.examples.mindless;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import org.battelle.clodhopper.Cluster;
import org.battelle.clodhopper.task.TaskEvent;
import org.battelle.clodhopper.task.TaskListener;
import org.battelle.clodhopper.task.TaskOutcome;
import org.battelle.clodhopper.tuple.ArrayTupleList;
import org.battelle.clodhopper.tuple.TupleList;

/**
 * Runs the MindlessClusterer to be sure it operates as expected.
 * 
 * @author R. Scarberry
 *
 */
public class MindlessDemo {

  public static void main(String[] args) {
    
    int tupleCount = 2000;
    int tupleLength = 10;
    int clusterCount = 25;
    
    // Generate some random data to test the MindlessClusterer.  No point in having
    // data that actually falls into clusters, because the MindlessClusterer wouldn't
    // find them anyway.
    
    long seed = System.currentTimeMillis();
    Random random = new Random(seed);
    double[] data = new double[tupleLength * tupleCount];
    for (int i=0; i<data.length; i++) {
      data[i] = random.nextDouble();
    }
    
    // Put the data in a TupleList
    TupleList tuples = new ArrayTupleList(tupleLength, tupleCount, data);
    
    // Construct the parameters.
    MindlessParams params = new MindlessParams(clusterCount, seed);
    
    // Construct the clusterer
    MindlessClusterer mindless = new MindlessClusterer(tuples, params);
    
    // Progress goes from 0 - 1 by default.
    mindless.setProgressEndpoints(0.0, 100.0);
    
    // Maintaining this in order to show the progress every 5% or so.
    final double[] lastProgressHolder = new double[1];
    lastProgressHolder[0] = -5.0;
    
    //System.out.printf("mindless progress endpoints %f - %f\n", mindless.)
    mindless.addTaskListener(new TaskListener() {

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
        if (e.getProgress() - lastProgressHolder[0] >= 5.0) {
          System.out.printf(" ... progress = %4.1f%%\n", e.getProgress());
          lastProgressHolder[0] = e.getProgress();
        }
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
    
    // Start it on a thread.
    new Thread(mindless).start();
    
    List<Cluster> clusters = null;
    try {
      clusters = mindless.get();
    } catch (InterruptedException e1) {
    } catch (ExecutionException e1) {
    }
    
    if (mindless.getTaskOutcome() == TaskOutcome.SUCCESS) {
      
      System.out.println("\nCLUSTERING RESULTS:\n");
      
      final int actualClusterCount = clusters.size();
      for (int i=0; i<actualClusterCount; i++) {
        Cluster c = clusters.get(i);
        System.out.printf("Cluster %d, member count = %d\n", (i+1), c.getMemberCount());
      }
      
    } else if (mindless.getTaskOutcome() == TaskOutcome.ERROR) {
      
      Throwable t = mindless.getError().orElse(null);
      String errMsg = mindless.getErrorMessage().orElse("unknown error");
      
      System.out.printf("Mindless clustering ended with error: %s", errMsg);
      
      if (t != null) {
        
        System.out.printf("\tand experienced the following:\n");
        t.printStackTrace(System.out);
        
      }
    }
    
  }
  
}
