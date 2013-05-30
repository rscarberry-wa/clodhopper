package org.battelle.clodhopper.gmeans;

import java.util.List;

import org.battelle.clodhopper.Cluster;
import org.battelle.clodhopper.ClusterSplitter;
import org.battelle.clodhopper.kmeans.KMeansSplittingClusterer;
import org.battelle.clodhopper.tuple.TupleList;

/**
 * Implementation of the g-means clustering algorithm.
 * <br />
 * See the following reference: 
 * 
 * Greg Hamerly, Charles Elkan: "Learning the k in k-means",
 * Neural Information Processing Systems, 2003
 * 
 * at http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.9.3574<br />

 * @author R.Scarberry
 * @since 1.0
 *
 */
public class GMeansClusterer extends KMeansSplittingClusterer {

	/**
	 * Constructor
	 * 
	 * @param tuples
	 * @param params
	 */
	public GMeansClusterer(TupleList tuples, GMeansParams params) {
		super(tuples, params);
	}
	
	@Override
	/**
	 * {@inheritDoc}
	 */
	public String taskName() {
		return "g-means clustering";
	}

	@Override
	/**
	 * For g-means, this is a no-op.
	 */
	protected void initializeIteration(List<Cluster> clusters) {
		// No op for g-means
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	protected ClusterSplitter createSplitter(List<Cluster> clusters,
			Cluster cluster) {
		return new GMeansClusterSplitter(tuples, (GMeansParams) params);
	}

}
