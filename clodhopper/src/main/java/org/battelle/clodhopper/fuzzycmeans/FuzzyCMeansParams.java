package org.battelle.clodhopper.fuzzycmeans;

import org.battelle.clodhopper.distance.DistanceMetric;
import org.battelle.clodhopper.distance.EuclideanDistanceMetric;
import org.battelle.clodhopper.seeding.ClusterSeeder;
import org.battelle.clodhopper.seeding.KMeansPlusPlusSeeder;

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
	private long randomSeed = System.currentTimeMillis();
	
	public FuzzyCMeansParams() {
		distanceMetric = new EuclideanDistanceMetric();
		clusterSeeder = new KMeansPlusPlusSeeder(distanceMetric);
	}
	
	public int getClusterCount() {
		return clusterCount;
	}
	
	public void setClusterCount(int n) {
		if (n <= 0) {
			throw new IllegalArgumentException("cluster count must be greater than 0");
		}
		this.clusterCount = n;
	}
	
	public int getMaxIterations() {
		return maxIterations;
	}
	
	public void setMaxIterations(int n) {
		if (n <= 0) {
			throw new IllegalArgumentException("max iterations must be greater than 0");
		}
		this.maxIterations = n;
	}
	
	public double getFuzziness() {
		return fuzziness;
	}
	
	public void setFuzziness(double fuzziness) {
		if (fuzziness < 1.0) {
			throw new IllegalArgumentException("fuzziness < 1.0: " + fuzziness);
		}
		this.fuzziness = fuzziness;
	}
	
    public final double getEpsilon() {
    	return epsilon;
    }
    
    public final void setEpsilon(double epsilon) {
    	if (epsilon < 0.0) {
    		throw new IllegalArgumentException("epsilon < 0.0: " + epsilon);
    	}
    	this.epsilon = epsilon;
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

	public DistanceMetric getDistanceMetric() {
		return distanceMetric;
	}
	
	public void setDistanceMetric(DistanceMetric distanceMetric) {
		if (distanceMetric == null) {
			throw new NullPointerException();
		}
		this.distanceMetric = distanceMetric;
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
	
	public long getRandomSeed() {
		return randomSeed;
	}
	
	public void setRandomSeed(long randomSeed) {
		this.randomSeed = randomSeed;
	}

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
    	
    	public Builder randomSeed(long seed) {
    		params.setRandomSeed(seed);
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
