package org.battelle.clodhopper.hierarchical;

import org.battelle.clodhopper.distance.DistanceMetric;
import org.battelle.clodhopper.distance.EuclideanDistanceMetric;

public class HierarchicalParams {
    /**
     * Kinds of linkage types, which determine how distances from one
     * hierarchical node to another are calculated.
     *
     * COMPLETE -- also known as max-pairwise.  Computed as the max
     *             distance between a coordinate in one node and a coordinate
     *             in the other node.
     * SINGLE   -- also known as min-pairwise. Computed as the min
     *             distance between a coordinate in one node and a coordinate
     *             in the other node.
     * MEAN     -- the distance between the node centers.
     *
     */
    public enum Linkage {
        COMPLETE, SINGLE, MEAN
    };

    /**
     * Defines whether generating clusters based on the number desired, or the min coherence desired.
     */
    public enum Criterion {
        CLUSTERS, COHERENCE
    };

    private DistanceMetric distanceMetric = new EuclideanDistanceMetric();

    private Linkage linkage = Linkage.COMPLETE;

    private Criterion criterion = Criterion.CLUSTERS;

    // Only one of these is relevant, depending on the value of mCriterion. 
    private int clusterCount = 1;

    private double coherenceDesired = 0.8;
    
    // These are used in computing the coherences if selecting clusters based on 
    // mCoherenceDesired.
    private double minCoherenceThreshold = 0.0;
    // Being NaN means that the max decision distance (min similarity) is used to compute the coherence.
    private double maxCoherenceThreshold = Double.NaN;

    // The number of worker threads to use for performing time-consuming concurrent tasks.
    // If -1, then select based on the number of processors.
    private int workerThreadCount = Runtime.getRuntime().availableProcessors();
    
    // Random generator seed for variants of hierarchical that use it.
    private long randomSeed = System.currentTimeMillis();

    public HierarchicalParams(int clusterCount, Linkage linkage, Criterion criterion,
    		DistanceMetric distanceMetric, int workerThreadCount) {
    	setClusterCount(clusterCount);
    	setLinkage(linkage);
    	setCriterion(criterion);
    	setDistanceMetric(distanceMetric);
    	setWorkerThreadCount(workerThreadCount);
    }

    public HierarchicalParams() {}

    public static Linkage linkageFor(String linkageName) {
    	return Linkage.valueOf(linkageName.toUpperCase());
    }
    
    public static Criterion criterionFor(String criterionName) {
    	return Criterion.valueOf(criterionName.toUpperCase());
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
	
	public double getCoherenceDesired() {
		return coherenceDesired;
	}
	
	public void setCoherenceDesired(double coherenceDesired) {
		if (coherenceDesired <= 0.0 || coherenceDesired > 1.0) {
			throw new IllegalArgumentException("coherence not in (0 - 1]: " + coherenceDesired);
		}
		this.coherenceDesired = coherenceDesired;
	}
    
    public double getMinCoherenceThreshold() {
    	return minCoherenceThreshold;
    }

    public void setMinCoherenceThreshold(double minCoherenceThreshold) {
    	this.minCoherenceThreshold = minCoherenceThreshold;
    }

    public double getMaxCoherenceThreshold() {
    	return maxCoherenceThreshold;
    }

    public void setMaxCoherenceThreshold(double maxCoherenceThreshold) {
    	this.maxCoherenceThreshold = maxCoherenceThreshold;
    }

    public final Criterion getCriterion() {
        return criterion;
    }
    
    public void setCriterion(Criterion criterion) {
    	if (criterion == null) {
    		throw new NullPointerException();
    	}
    	this.criterion = criterion;
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

    public final Linkage getLinkage() {
        return linkage;
    }
    
    public void setLinkage(Linkage linkage) {
    	if (linkage == null) {
    		throw new NullPointerException();
    	}
    	this.linkage = linkage;
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
	
	public long getRandomSeed() {
		return randomSeed;
	}
	
	public void setRandomSeed(long randomSeed) {
		this.randomSeed = randomSeed;
	}

	public int hashCode() {
        int hc = distanceMetric.hashCode();
        hc = 37 * hc + linkage.hashCode();
        hc = 37 * hc + criterion.hashCode();
        long bits = Double.doubleToLongBits(coherenceDesired);
        hc = 37 * hc + (int) (bits ^ (bits >>> 32));
        bits = Double.doubleToLongBits(minCoherenceThreshold);
        hc = 37 * hc + (int) (bits ^ (bits >>> 32));
        bits = Double.doubleToLongBits(maxCoherenceThreshold);
        hc = 37 * hc + (int) (bits ^ (bits >>> 32));
        hc = 37 * hc + workerThreadCount;
        hc = 37 * hc + (int) (randomSeed ^ (randomSeed >>> 32));
        return hc;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof HierarchicalParams) {
            HierarchicalParams other = (HierarchicalParams) o;
            return this.distanceMetric.equals(other.distanceMetric)
                    && this.linkage == other.linkage
                    && this.criterion == other.criterion
                    && this.clusterCount == other.clusterCount
                    && Double.doubleToLongBits(this.coherenceDesired) == Double
                            .doubleToLongBits(other.coherenceDesired)
                    && Double.doubleToLongBits(this.minCoherenceThreshold) == Double
                            .doubleToLongBits(other.minCoherenceThreshold)
                    && Double.doubleToLongBits(this.maxCoherenceThreshold) == Double
                            .doubleToLongBits(other.maxCoherenceThreshold)
                    && this.workerThreadCount == other.workerThreadCount
                    && this.randomSeed == other.randomSeed;
        }
        return false;
    }

    
    /**
     * Builder class for convenience, so you don't have to remember the numerous constructor
     * parameters.
     * 
     * @author d3j923
     *
     */
    public static class Builder {
    	
    	private HierarchicalParams params;
    	
    	public Builder() {
    		params = new HierarchicalParams();
    	}

    	public Builder criterion(Criterion criterion) {
    		params.setCriterion(criterion);
    		return this;
    	}
    	
    	public Builder linkage(Linkage linkage) {
    		params.setLinkage(linkage);
    		return this;
    	}
    	
    	public Builder distanceMetric(DistanceMetric distanceMetric) {
    		params.setDistanceMetric(distanceMetric);
    		return this;
    	}
    	
    	public Builder clusterCount(int clusterCount) {
    		params.setClusterCount(clusterCount);
    		return this;
    	}
    	
    	public Builder coherenceDesired(double coherence) {
    		params.setCoherenceDesired(coherence);
    		return this;
    	}
    	
    	public Builder minCoherenceThreshold(double minCoherenceThreshold) {
    		params.setMinCoherenceThreshold(minCoherenceThreshold);
    		return this;
    	}
    	
    	public Builder maxCoherenceThreshold(double maxCoherenceThreshold) {
    		params.setMaxCoherenceThreshold(maxCoherenceThreshold);
    		return this;
    	}

    	public Builder randomSeed(long randomSeed) {
    		params.setRandomSeed(randomSeed);
    		return this;
    	}
    	
    	public Builder workerThreadCount(int workerThreadCount) {
    		params.setWorkerThreadCount(workerThreadCount);
    		return this;
    	}
    	
    	public HierarchicalParams build() {
    		return params;
    	}
    }

}
