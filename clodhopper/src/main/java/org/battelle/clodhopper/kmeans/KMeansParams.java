package org.battelle.clodhopper.kmeans;

import org.battelle.clodhopper.distance.DistanceMetric;
import org.battelle.clodhopper.distance.EuclideanDistanceMetric;
import org.battelle.clodhopper.seeding.ClusterSeeder;
import org.battelle.clodhopper.seeding.KMeansPlusPlusSeeder;

public class KMeansParams {

	private int clusterCount;
	private int maxIterations = Integer.MAX_VALUE;
	private boolean replaceEmptyClusters = true;
	private int movesGoal;
	private int workerThreadCount;
	private DistanceMetric distanceMetric;
	private ClusterSeeder seeder;
	
	public KMeansParams() {
		workerThreadCount = Runtime.getRuntime().availableProcessors();
		distanceMetric = new EuclideanDistanceMetric();
		seeder = new KMeansPlusPlusSeeder(distanceMetric);
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
	
	public int getMovesGoal() {
		return movesGoal;
	}
	
	public void setMovesGoal(int n) {
		if (n < 0) throw new IllegalArgumentException("moves goal cannot be negative");
		this.movesGoal = n;
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
	
	public boolean getReplaceEmptyClusters() {
		return replaceEmptyClusters;
	}
	
	public void setReplaceEmptyClusters(boolean b) {
		replaceEmptyClusters = b;
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
		return seeder;
	}
	
	public void setClusterSeeder(ClusterSeeder seeder) {
		if (seeder == null) {
			throw new NullPointerException();
		}
		this.seeder = seeder;
	}
	
	public static class Builder {
		
		private KMeansParams params;
		
		public Builder() {
			params = new KMeansParams();
		}
		
		public Builder clusterCount(int n) {
			params.setClusterCount(n);
			return this;
		}
		
		public Builder maxIterations(int n) {
			params.setMaxIterations(n);
			return this;
		}
		
		public Builder movesGoal(int n) {
			params.setMovesGoal(n);
			return this;
		}
		
		public Builder workerThreadCount(int n) {
			params.setWorkerThreadCount(n);
			return this;
		}
		
		public Builder replaceEmptyClusters(boolean b) {
			params.setReplaceEmptyClusters(b);
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
		
		public KMeansParams build() {
			return params;
		}
	}
	
}
