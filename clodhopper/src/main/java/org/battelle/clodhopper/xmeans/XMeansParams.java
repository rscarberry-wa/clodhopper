package org.battelle.clodhopper.xmeans;

import org.battelle.clodhopper.distance.DistanceMetric;
import org.battelle.clodhopper.kmeans.KMeansSplittingParams;
import org.battelle.clodhopper.seeding.ClusterSeeder;

public class XMeansParams extends KMeansSplittingParams {

	private boolean useOverallBIC = true;
	
	public XMeansParams() {}
	
	public boolean getUseOverallBIC() {
		return useOverallBIC;
	}
	
	public void setUseOverallBIC(boolean b) {
		useOverallBIC = b;
	}
	
	public static class Builder {
		
		private XMeansParams params = new XMeansParams();
		
		public Builder() {}
		
		public Builder minClusters(int minClusters) {
			params.setMinClusters(minClusters);
			return this;
		}
		
		public Builder maxClusters(int maxClusters) {
			params.setMaxClusters(maxClusters);
			return this;
		}
		
		public Builder minClusterToMeanThreshold(double minClusterToMeanThreshold) {
			params.setMinClusterToMeanThreshold(minClusterToMeanThreshold);
			return this;
		}
		
		public Builder distanceMetric(DistanceMetric distanceMetric) {
			params.setDistanceMetric(distanceMetric);
			return this;
		}
		
		public Builder clusterSeeder(ClusterSeeder seeder) {
			params.setClusterSeeder(seeder);
			return this;
		}

		public Builder workerThreadCount(int workerThreadCount) {
			params.setWorkerThreadCount(workerThreadCount);
			return this;
		}
		
		public Builder userOverallBIC(boolean b) {
			params.setUseOverallBIC(b);
			return this;
		}
		
		public XMeansParams build() {
			return params;
		}
	}
}
