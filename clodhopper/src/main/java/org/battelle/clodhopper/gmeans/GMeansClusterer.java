package org.battelle.clodhopper.gmeans;

import java.util.List;

import org.battelle.clodhopper.Cluster;
import org.battelle.clodhopper.ClusterSplitter;
import org.battelle.clodhopper.kmeans.KMeansSplittingClusterer;
import org.battelle.clodhopper.tuple.TupleList;

public class GMeansClusterer extends KMeansSplittingClusterer {

	public GMeansClusterer(TupleList tuples, GMeansParams params) {
		super(tuples, params);
	}
	
	@Override
	public String taskName() {
		return "g-means clustering";
	}

	@Override
	protected void initializeIteration(List<Cluster> clusters) {
		// No op for g-means
	}

	@Override
	protected ClusterSplitter createSplitter(List<Cluster> clusters,
			Cluster cluster) {
		return new GMeansClusterSplitter(tuples, (GMeansParams) params);
	}

}
