package org.battelle.clodhopper.gmeans;

import org.battelle.clodhopper.distance.DistanceMetric;
import org.battelle.clodhopper.kmeans.KMeansSplittingParams;
import org.battelle.clodhopper.seeding.ClusterSeeder;

/**
 * Parameter class for G-Means clustering.
 * 
 * @author R. Scarberry
 *
 */
public class GMeansParams extends KMeansSplittingParams {

	public static class Builder {
		
		private GMeansParams params = new GMeansParams();
		
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
		
		public GMeansParams build() {
			return params;
		}
	}	
}
