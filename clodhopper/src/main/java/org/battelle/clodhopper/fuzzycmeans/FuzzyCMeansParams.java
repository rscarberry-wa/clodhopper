package org.battelle.clodhopper.fuzzycmeans;

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
 * FuzzyCMeansParams.java
 *
 *===================================================================*/

/**
 * The parameter class for fuzzy c-means clustering.
 * 
 * @author R. Scarberry
 * @since 1.0
 *
 */
public class FuzzyCMeansParams {

	public static final double DEFAULT_FUZZINESS = 2.0;
	public static final double DEFAULT_EPSILON = 1.0;

	private int clusterCount; 
	private int maxIterations = Integer.MAX_VALUE;
	
	private double fuzziness = DEFAULT_FUZZINESS; 
	private double epsilon = DEFAULT_EPSILON; 
	
	private DistanceMetric distanceMetric;
	private ClusterSeeder clusterSeeder;
	private int workerThreadCount = Runtime.getRuntime().availableProcessors();
	
	public FuzzyCMeansParams() {
		distanceMetric = new EuclideanDistanceMetric();
		clusterSeeder = new KMeansPlusPlusSeeder(distanceMetric);
	}
	
	/**
	 * Get the number of requested clusters.
	 * 
	 * @return
	 */
	public int getClusterCount() {
		return clusterCount;
	}
	
	/**
	 * Set the number of requested clusters.
	 * 
	 * @param n the number desired
	 * 
	 * @throws IllegalArgumentException if n is not a positive integer.
	 */
	public void setClusterCount(int n) {
		if (n <= 0) {
			throw new IllegalArgumentException("cluster count must be greater than 0");
		}
		this.clusterCount = n;
	}
	
	/**
	 * Get the maximum number of iterations before clustering will terminate
	 * regardless of whether or not the error level is less than the epsilon value.
	 * 
	 * @return
	 */
	public int getMaxIterations() {
		return maxIterations;
	}
	
	/**
	 * Set the maximum number of iterations.
	 * 
	 * @param n
	 */
	public void setMaxIterations(int n) {
		if (n <= 0) {
			throw new IllegalArgumentException("max iterations must be greater than 0");
		}
		this.maxIterations = n;
	}
	
	/**
	 * Get the fuzziness level, a value of at least 1, which is used in computing
	 * interim cluster centers and the error level.
	 * 
	 * @return
	 */
	public double getFuzziness() {
		return fuzziness;
	}
	
	/**
	 * Set the fuzziness level.
	 * 
	 * @param fuzziness
	 * 
	 * @throws IllegalArgumentException if less than 1.0
	 */
	public void setFuzziness(double fuzziness) {
		if (fuzziness < 1.0) {
			throw new IllegalArgumentException("fuzziness < 1.0: " + fuzziness);
		}
		this.fuzziness = fuzziness;
	}
	
	/**
	 * Get the epsilon value.
	 * When the computed error level is less than this value, clustering terminates.
	 * 
	 * @return
	 */
    public final double getEpsilon() {
    	return epsilon;
    }
    
    /**
     * Set the epsilon value.
     *
     * @param epsilon
     * 
     * @throws IllegalArgumentException if less than 0
     */
    public final void setEpsilon(double epsilon) {
    	if (epsilon < 0.0) {
    		throw new IllegalArgumentException("epsilon < 0.0: " + epsilon);
    	}
    	this.epsilon = epsilon;
    }
		
    /**
     * Get the number of worker threads to be used for concurrent subtasks.
     * 
     * @return
     */
	public int getWorkerThreadCount() {
		return workerThreadCount;
	}
	
	/**
	 * Set the number of worker threads to be used for concurrent subtasks.
	 * 
	 * @param n
	 * 
	 * @throws IllegalArgumentException if less than 1
	 */
	public void setWorkerThreadCount(int n) {
		if (n <= 0) {
			throw new IllegalArgumentException("worker thread count must be greater than 0");
		}
		this.workerThreadCount = n;
	}

	/**
	 * Get the distance metric to be used during clustering.
	 * 
	 * @return
	 */
	public DistanceMetric getDistanceMetric() {
		return distanceMetric;
	}
	
	/**
	 * Set the distance metric to be used during clustering.
	 * 
	 * @param distanceMetric
	 * 
	 * @throws NullPointerException if null
	 */
	public void setDistanceMetric(DistanceMetric distanceMetric) {
		if (distanceMetric == null) {
			throw new NullPointerException();
		}
		this.distanceMetric = distanceMetric;
	}
	
	/**
	 * Get the object used to pick the initial cluster seeds.
	 * 
	 * @return
	 */
	public ClusterSeeder getClusterSeeder() {
		return clusterSeeder;
	}
	
	/**
	 * Set the object used to pick the initial cluster seeds.
	 * 
	 * @param seeder
	 * 
	 * @throws NullPointerException if null
	 */
	public void setClusterSeeder(ClusterSeeder seeder) {
		if (seeder == null) {
			throw new NullPointerException();
		}
		this.clusterSeeder = seeder;
	}
	
	/**
	 * A builder class used for convenience in creating a FuzzyCMeansParams object.
	 * Each method returns a reference to the builder, so calls can be chained.
	 *  
	 * @author R. Scarberry
	 * @since 1.0
	 *
	 */
    public static class Builder {
    	
    	private FuzzyCMeansParams params;
    	
    	public Builder() {
    		params = new FuzzyCMeansParams();
    	}
    	
    	public Builder clusterCount(int clusterCount) {
    		params.setClusterCount(clusterCount);
    		return this;
    	}
    	
    	public Builder fuzziness(double fuzziness) {
    		params.setFuzziness(fuzziness);
    		return this;
    	}
    	
    	public Builder maxIterations(int maxIterations) {
    		params.setMaxIterations(maxIterations);
    		return this;
    	}
    	
    	public Builder epsilon(double epsilon) {
    		params.setEpsilon(epsilon);
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

    	public FuzzyCMeansParams build() {
    		return params;
    	}
    }

}
