package org.battelle.clodhopper.gmeans;

import org.battelle.clodhopper.distance.DistanceMetric;
import org.battelle.clodhopper.kmeans.KMeansSplittingParams;
import org.battelle.clodhopper.seeding.ClusterSeeder;

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
 * GMeansParams.java
 *
 *===================================================================*/

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
