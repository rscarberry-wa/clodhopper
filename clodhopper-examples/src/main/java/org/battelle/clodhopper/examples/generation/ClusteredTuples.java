package org.battelle.clodhopper.examples.generation;

import org.battelle.clodhopper.Cluster;
import org.battelle.clodhopper.tuple.TupleList;

import java.util.List;

/**
 * Contains tuples and the clusters to which they have been assigned.
 */
public class ClusteredTuples {

    private TupleList tuples;
    private List<Cluster> clusters;

    public ClusteredTuples(TupleList tuples, List<Cluster> clusters) {
        this.tuples = tuples;
        this.clusters = clusters;
    }

    public TupleList getTuples() {
        return tuples;
    }

    public List<Cluster> getClusters() {
        return clusters;
    }

}
