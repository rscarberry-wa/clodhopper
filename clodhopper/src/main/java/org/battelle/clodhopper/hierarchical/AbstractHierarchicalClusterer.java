package org.battelle.clodhopper.hierarchical;

import java.util.List;

import org.battelle.clodhopper.AbstractClusterer;
import org.battelle.clodhopper.Cluster;
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
 * AbstractHierarchicalClusterer.java
 *
 *===================================================================*/

/**
 * <p>Abstract base class for implementations of hierarchical clustering.
 * Subclasses must implement the <tt>buildDendrogram</tt> method.  Subclasses cannot
 * implement the <tt>doTask()</tt> method since it is final.  The
 * difference between different hierarchical clustering algorithms is their methods
 * for generating the dendrogram.</p>
 * 
 * @author R. Scarberry
 * @since 1.0 
 *
 */
public abstract class AbstractHierarchicalClusterer extends AbstractClusterer {

	protected TupleList tuples;
	protected HierarchicalParams params;	
	
    // The dendrogram produced by the clustering implementation.
    protected Dendrogram dendrogram;
    protected List<Cluster> clusters;
    
    /**
     * Constructor.
     * 
     * @param cs - contains the coordinates to be clustered.
     * @param params - the hierarchical clustering parameters.
     * @param dendrogram - if non-null, a dendrogram to be reused.  
     *   This dendrogram should have been produced by an earlier run
     *   of the algorithm on the same coordinate list.  If null, it is 
     *   ignored.
     */
    public AbstractHierarchicalClusterer(TupleList cs,
            HierarchicalParams params, Dendrogram dendrogram) {
        if (cs == null || params == null) {
        	throw new NullPointerException();
        }
        this.tuples = cs;
    	this.params = params;
    	this.dendrogram = dendrogram;
    }

    /**
     * Constructor for completely reclustering a
     * list of coordinates.
     * 
     * @param cs - contains the coordinates to be clustered.
     * @param params - the hierarchical clustering parameters.
     */
    public AbstractHierarchicalClusterer(TupleList cs, HierarchicalParams params) {
        this(cs, params, null);
    }

    /**
     * Returns the dendrogram produced by the task, or the dendrogram reused by
     * the task if the dendrogram was provided by the constructor.
     * If creating a new dendrogram, this method should not be
     * called until the task is finished.
     * @return
     */
    public Dendrogram getDendrogram() {
        return dendrogram;
    }

    /**
     * Perform the work of this task.  Since this method is final,
     * subclasses must perform their work in <tt>buildDendrogram()</tt>.
     */
    protected final List<Cluster> doTask() throws Exception {

        int tupleCount = tuples.getTupleCount();

        // Have to have at least one coordinate.
        if (tupleCount == 0) {
            finishWithError("zero tuples to cluster");
        }

        // If reusing an existing dendrogram, it has to have the same number
        // of leaves as coordinates.
        if (dendrogram != null && dendrogram.getLeafCount() != tupleCount) {
            finishWithError("invalid dendrogram: leaf count = "
                    + dendrogram.getLeafCount() + ", tuple count = "
                    + tupleCount);
        }

        if (dendrogram == null) {
            // This should build a completely new dendrogram.
            buildDendrogram();
        } else {
            postMessage("reclustering from existing dendrogram");
        }

        int clusterCount = 0;

        HierarchicalParams.Criterion criterion = params.getCriterion();
        
        if (criterion == HierarchicalParams.Criterion.CLUSTERS) {
            
        	clusterCount = params.getClusterCount();
            if (clusterCount > tupleCount) {
                postMessage("reducing number of clusters to the number of tuples: "
                        + tupleCount);
                clusterCount = tupleCount;
            }
            
        } else if (criterion == HierarchicalParams.Criterion.COHERENCE) {
        	
        	dendrogram.setMinCoherenceThreshold(params.getMinCoherenceThreshold());
        	dendrogram.setMaxCoherenceThreshold(params.getMaxCoherenceThreshold());
            
        	clusterCount = dendrogram.clustersWithCoherenceExceeding(params.getCoherenceDesired());
        	
        } else {
            
        	finishWithError("unsupported criterion: " + criterion);
        	
        }

        clusters = dendrogram.generateClusters(clusterCount, tuples);

        return clusters;
    }

    /**
     * Build a new dendrogram.  Subclasses must implement this method.
     * @throws Exception if anything goes wrong.  The exception bubbles up
     *   to the run method of <tt>AbstractTask</tt> and sets the error message.
     */
    protected abstract void buildDendrogram() throws Exception;
}
