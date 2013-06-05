package org.battelle.clodhopper.kmeans;

import org.battelle.clodhopper.distance.DistanceMetric;
import org.battelle.clodhopper.distance.EuclideanDistanceMetric;
import org.battelle.clodhopper.seeding.ClusterSeeder;
import org.battelle.clodhopper.seeding.KMeansPlusPlusSeeder;

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
 * KMeansSplittingParams.java
 *
 *===================================================================*/

public class KMeansSplittingParams {

	private int minClusters = 1;
	private int maxClusters = Integer.MAX_VALUE;
	private double minClusterToMeanThreshold = 0.05;
	private DistanceMetric distanceMetric = new EuclideanDistanceMetric();
	private ClusterSeeder clusterSeeder = new KMeansPlusPlusSeeder(new EuclideanDistanceMetric());
	private int workerThreadCount = Runtime.getRuntime().availableProcessors();
	
	public KMeansSplittingParams() {}
	
	public int getMinClusters() {
		return minClusters;
	}
	
	public void setMinClusters(int minClusters) {
		if (minClusters < 1) {
			throw new IllegalArgumentException("minClusters < 1: " + minClusters);
		}
		this.minClusters = minClusters;
	}
	
	public int getMaxClusters() {
		return maxClusters;
	}
	
	public void setMaxClusters(int maxClusters) {
		if (maxClusters < 1) {
			throw new IllegalArgumentException("maxClusters < 1: " + maxClusters);
		}
		this.maxClusters = maxClusters;
	}
	
    /**
     * Returns a threshold for determining if small clusters are to be
     * thrown out at the end of clustering.  By default this threshold is
     * 0.05.  If set to zero, no clusters will be thrown out because of size.
     * If greater than zero, clusters having sizes less than this value
     * times the mean cluster size are thrown out.
     * @return
     */
    public double getMinClusterToMeanThreshold() {
        return minClusterToMeanThreshold;
    }

    public void setMinClusterToMeanThreshold(double t) {
    	if (t < 0.0) {
    		throw new IllegalArgumentException("minClusterToMeanThreshold < 0: " + t);
    	}
        minClusterToMeanThreshold = t;
    }

	public DistanceMetric getDistanceMetric() {
		return distanceMetric;
	}
	
	public void setDistanceMetric(DistanceMetric distanceMetric) {
		if (distanceMetric == null) {
			throw new NullPointerException();
		}
		this.distanceMetric = distanceMetric;
	}
	
	public int getWorkerThreadCount() {
		return workerThreadCount;
	}
	
	public void setWorkerThreadCount(int n) {
		if (n <= 0) {
			throw new IllegalArgumentException("worker thread count must be greater than 0");
		}
		this.workerThreadCount = n;
	}

	public ClusterSeeder getClusterSeeder() {
		return clusterSeeder;
	}
	
	public void setClusterSeeder(ClusterSeeder seeder) {
		if (seeder == null) {
			throw new NullPointerException();
		}
		this.clusterSeeder = seeder;
	}

}
