package org.battelle.clodhopper.gmeans;

import org.battelle.clodhopper.distance.DistanceMetric;
import org.battelle.clodhopper.kmeans.KMeansSplittingParams;
import org.battelle.clodhopper.seeding.ClusterSeeder;

/**
 * Parameter class for G-Means clustering. This class does not add
 * any parameters to its parent class KMeansSplittingParams.  It exists
 * to provide its own nested builder class.
 * 
 * @author R. Scarberry
 * @since 1.0
 *
 */
public class GMeansParams extends KMeansSplittingParams {

	/**
	 * Builder class for GMeansParams.  Since all methods return
	 * a reference to the builder, calls can be chained for convenience.
	 * 
	 * @author R. Scarberry
	 * @since 1.0
	 *
	 */
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
