package org.battelle.clodhopper.xmeans;

import java.util.List;

import org.battelle.clodhopper.Cluster;
import org.battelle.clodhopper.ClusterSplitter;
import org.battelle.clodhopper.ClusterStats;
import org.battelle.clodhopper.Clusterer;
import org.battelle.clodhopper.kmeans.KMeansSplittingClusterer;
import org.battelle.clodhopper.tuple.TupleList;

public class XMeansClusterer extends KMeansSplittingClusterer 
	implements Clusterer {

	private double overallBIC;
	
	public XMeansClusterer(TupleList tuples, XMeansParams params) {
		super(tuples, params);
	}
	
	@Override
	public String taskName() {
		return "x-means clustering";
	}

	protected void initializeIteration(List<Cluster> clusters) {
		overallBIC = ClusterStats.computeBIC(tuples, clusters);
	}
	
	protected ClusterSplitter createSplitter(List<Cluster> clusters, Cluster cluster) {
		return new XMeansClusterSplitter(tuples, clusters, overallBIC, (XMeansParams) params);
	}

}
