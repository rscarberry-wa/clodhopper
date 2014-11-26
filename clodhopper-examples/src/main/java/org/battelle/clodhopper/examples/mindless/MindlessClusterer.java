package org.battelle.clodhopper.examples.mindless;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import gnu.trove.list.array.TIntArrayList;

import org.battelle.clodhopper.AbstractClusterer;
import org.battelle.clodhopper.Cluster;
import org.battelle.clodhopper.task.ProgressHandler;
import org.battelle.clodhopper.tuple.TupleList;
import org.battelle.clodhopper.tuple.TupleMath;
import org.battelle.clodhopper.util.ArrayIntIterator;

/**
 * This incredibly useless form of clustering exists only to demonstrate
 * how to write a clusterer by extending AbstractClusterer.  I did not want the
 * complexity of the algorithm to obscure the demonstration of the benefits of extending
 * AbstractClusterer and of using utility classes such as ProgressHandler.
 * 
 * @author R. Scarberry
 *
 */
public class MindlessClusterer extends AbstractClusterer {

  private final TupleList tuples;
  private final MindlessParams params;
  
  /**
   * Constructor which expects a TupleList and a parameters object the
   * same as the rest of the clusterers.
   * 
   * @param tuples the tuple to cluster.
   * @param params contains the clustering parameters.
   */
  public MindlessClusterer(final TupleList tuples, final MindlessParams params) {
    if (tuples == null || params == null) {
      throw new NullPointerException();
    }
    this.tuples = tuples;
    this.params = params;
  }
  
  /**
   * All tasks must provide this method.
   * @return a string describing the method of clustering.
   */
  @Override
  public String taskName() {
    return "mindless clustering";
  }

  /**
   * All extensions of AbstractTask must provide this method
   * which is called from AbstractTask's final run method.
   * @return a list of clusters.
   */
  @Override
  protected List<Cluster> doTask() throws Exception {

    List<Cluster> clusters = null;
    
    final int tupleCount = tuples.getTupleCount();
    
    // Normally do some error checking on the inputs.  If something
    // doesn't make sense, abort by calling finishWithError.  Calling this
    // method generates an exception which pops control back into the run
    // method.  So there is no need to return after the method.
    if (tupleCount == 0) {
      finishWithError("zero tuples");
    }
    
    // Also check the cluster count.
    int clusterCount = params.getClusterCount();
    if (clusterCount <= 0) {
      finishWithError("clusterCount <= 0");
    }
    
    // Instantiate a ProgressHandler, which handles progress, status messages,
    // and it checks for cancellation.  If the clusterer has been canceled
    // control also pops back into the run method.
    ProgressHandler ph = new ProgressHandler(this, tupleCount);
    
    // Send the first progress event.
    ph.postBegin();

    // If requesting more clusters than tuples, reduce the cluster count and
    // post a status message.
    if (clusterCount > tupleCount) {
      ph.postMessage(String.format("reducing cluster count from %d to %d, the number of tuples", 
          clusterCount, tupleCount));
      clusterCount = tupleCount;
    }
    
    // This is where the mindlessness begins. Just randomly assign tuples to one of the
    // clusters. Collect the tuple indexes in TIntArrayLists
    //
    TIntArrayList[] memberLists = new TIntArrayList[clusterCount];
    for (int i=0; i<clusterCount; i++) {
      memberLists[i] = new TIntArrayList();
    }
    
    // Instantiate a random number generator using the seed in params.
    Random random = new Random(params.getRandomSeed());
    
    // Make the mindless assignments.
    //
    for (int i=0; i<tupleCount; i++) {
      
      int assignment = random.nextInt(clusterCount);
      
      memberLists[assignment].add(i);
      
      // Post a progress step.  This will also check for cancelation in case the
      // user came to his/her senses and decided mindless clustering might not be
      // the way to go.
      ph.postStep();
      
    }
    
    // Instantiate the list.
    clusters = new ArrayList<>();
    
    // Transform the TIntArrayLists to actual clusters, but only if they 
    // are not empty. (If clusterCount is near TupleCount, it is quite possible for
    // some clusters to be empty.)
    //
    for (int i=0; i<clusterCount; i++) {
      
      TIntArrayList memberList = memberLists[i];
      memberList.trimToSize();
      
      int[] members = memberList.toArray();
      
      if (members.length > 0) {
        // Compute the center using the utility class, TupleMath.
        double[] center = TupleMath.average(tuples, new ArrayIntIterator(members));
        clusters.add(new Cluster(members, center));
      }
      
    }
    
    // Post the final progress event.
    ph.postEnd();
    
    // Return the list of our mindlessly-generated clusters.
    return clusters;
  }

}
