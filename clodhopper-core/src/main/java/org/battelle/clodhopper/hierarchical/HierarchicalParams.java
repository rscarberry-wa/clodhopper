package org.battelle.clodhopper.hierarchical;

import org.battelle.clodhopper.distance.DistanceMetric;
import org.battelle.clodhopper.distance.EuclideanDistanceMetric;

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
 * HierarchicalParams.java
 *
 *===================================================================*/
public class HierarchicalParams {

    /**
     * Kinds of linkage types, which determine how distances from one
     * hierarchical node to another are calculated.
     *
     * COMPLETE -- also known as max-pairwise. Computed as the max distance
     * between a coordinate in one node and a coordinate in the other node.
     * SINGLE -- also known as min-pairwise. Computed as the min distance
     * between a coordinate in one node and a coordinate in the other node. MEAN
     * -- the distance between the node centers.
     *
     */
    public enum Linkage {

        /**
         * When using complete linkage, the distance between nodes of the
         * dendrogram is the maximum pairwise distance of any two pairs of
         * tuples i and j, where i is a member of one node and j is the member
         * of another.
         */
        COMPLETE,
        /**
         * When using single linkage, the distance between nodes of the
         * dendrogram is the maximum pairwise distance of any two pairs of
         * tuples i and j, where i is a member of one node and j is the member
         * of another.
         */
        SINGLE,
        /**
         * When using mean distance, the distance between node of the dendrogram
         * is found by averaging the tuples for ids in each node and then
         * computing the distance between them.
         */
        MEAN
    };

    /**
     * Defines whether generating clusters based on the number desired, or the
     * min coherence desired.
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

    /**
     * Constructor
     *
     * @param clusterCount desired number of clusters
     * @param linkage the linkage to be used; COMPLETE, SINGLE, or MEAN
     * @param criterion the criterion to be used; Criterion.CLUSTERS or
     * Criterion.COHERANCE
     * @param distanceMetric the metric to be used for computing distances
     * @param workerThreadCount number of threads to be used for concurrent
     * parts of the algorithm
     */
    public HierarchicalParams(int clusterCount, Linkage linkage, Criterion criterion,
            DistanceMetric distanceMetric, int workerThreadCount) {
        setClusterCount(clusterCount);
        setLinkage(linkage);
        setCriterion(criterion);
        setDistanceMetric(distanceMetric);
        setWorkerThreadCount(workerThreadCount);
    }

    /**
     * Constructor
     */
    public HierarchicalParams() {
    }

    /**
     * Translates strings such as &quot;COMPLETE&quot;, &quot;SINGLE&quot; and
     * &quot;MEAN&quot; into the appropriate linkage type (case-insensitive).
     *
     * @param linkageName name of the desired linkage.
     *
     * @return a Linkage enum
     *
     * @throws IllegalArgumentException if the string cannot be translated.
     */
    public static Linkage linkageFor(final String linkageName) {
        return Linkage.valueOf(linkageName.toUpperCase());
    }

    /**
     * Translates a string into the appropriate Criterion enum.
     *
     * @param criterionName name of the criterion.
     *
     * @return a <code>Criterion</code> enum.
     */
    public static Criterion criterionFor(final String criterionName) {
        return Criterion.valueOf(criterionName.toUpperCase());
    }

    /**
     * Get the requested cluster count.
     *
     * @return the cluster count.
     */
    public int getClusterCount() {
        return clusterCount;
    }

    /**
     * Set the requested cluster count.
     *
     * @param n the desired cluster count.
     *
     * @throws IllegalArgumentException if n is not a positive integer.
     */
    public void setClusterCount(final int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("cluster count must be greater than 0");
        }
        this.clusterCount = n;
    }

    /**
     * Get the desired cluster coherence. This parameter only matters when the
     * criterion is Criterion.COHERENCE.
     *
     * @return desired coherence.
     */
    public double getCoherenceDesired() {
        return coherenceDesired;
    }

    /**
     * Set the desired cluster coherence, which is only relevant if using
     * coherence as the clustering criterion.
     *
     * @param coherenceDesired the desired coherence.
     *
     * @throws IllegalArgumentException if the value is outside the range (0 -
     * 1].
     */
    public void setCoherenceDesired(final double coherenceDesired) {
        if (coherenceDesired <= 0.0 || coherenceDesired > 1.0) {
            throw new IllegalArgumentException("coherence not in (0 - 1]: " + coherenceDesired);
        }
        this.coherenceDesired = coherenceDesired;
    }

    /**
     * Return the minimum distance threshold used in computing coherences. If
     * not set, this defaults to 0.
     *
     * @return the minimum coherence threshold.
     */
    public double getMinCoherenceThreshold() {
        return minCoherenceThreshold;
    }

    /**
     * Set the minimum distance threshold used in computing coherances.
     *
     * @param minCoherenceThreshold the minimum threshold.
     */
    public void setMinCoherenceThreshold(final double minCoherenceThreshold) {
        this.minCoherenceThreshold = minCoherenceThreshold;
    }

    /**
     * Get the maximum distance threshold used for computing coherences.
     *
     * @return the max coherence threshold.
     */
    public double getMaxCoherenceThreshold() {
        return maxCoherenceThreshold;
    }

    /**
     * Set the maximum distance threshold used in computing coherences.
     *
     * @param maxCoherenceThreshold the max coherence threshold.
     */
    public void setMaxCoherenceThreshold(final double maxCoherenceThreshold) {
        this.maxCoherenceThreshold = maxCoherenceThreshold;
    }

    /**
     * Get the criterion used for selecting the clusters from a completed
     * dendrogram.
     *
     * @return the criterion used.
     */
    public final Criterion getCriterion() {
        return criterion;
    }

    /**
     * Set the criterion used for selecting the clusters from a completed
     * dendrogram.
     *
     * @param criterion the criterion to be used.
     */
    public void setCriterion(final Criterion criterion) {
        if (criterion == null) {
            throw new NullPointerException();
        }
        this.criterion = criterion;
    }

    /**
     * Get the distance metric used for computing distances during clustering.
     *
     * @return the <code>DistanceMetric</code>.
     */
    public DistanceMetric getDistanceMetric() {
        return distanceMetric;
    }

    /**
     * Set the distance metric used for computing distances during clustering.
     *
     * @param distanceMetric the distance metric to use.
     *
     * @throws NullPointerException if the object is null.
     *
     */
    public void setDistanceMetric(final DistanceMetric distanceMetric) {
        if (distanceMetric == null) {
            throw new NullPointerException();
        }
        this.distanceMetric = distanceMetric;
    }

    /**
     * Get the kind of linkage used for selecting which pairwise distance of use
     * in determining the distance between dendrogram nodes.
     *
     * @return the linkage to use.
     */
    public final Linkage getLinkage() {
        return linkage;
    }

    /**
     * Set the kind of linkage used.
     *
     * @param linkage the linkage to use.
     */
    public void setLinkage(final Linkage linkage) {
        if (linkage == null) {
            throw new NullPointerException();
        }
        this.linkage = linkage;
    }

    /**
     * Get the number of threads to be used for concurrent computing tasks.
     *
     * @return the worker thread count.
     */
    public int getWorkerThreadCount() {
        return workerThreadCount;
    }

    /**
     * Set the number of threads to be used for concurrent computing tasks.
     *
     * @param n the worker thread count.
     */
    public void setWorkerThreadCount(final int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("worker thread count must be greater than 0");
        }
        this.workerThreadCount = n;
    }

    /**
     * Get the seed for random number generation.
     *
     * @return seed used by the random number generator.
     */
    public long getRandomSeed() {
        return randomSeed;
    }

    /**
     * Set the seed for random number generation.
     *
     * @param randomSeed the seed to be used for random number generation.
     */
    public void setRandomSeed(final long randomSeed) {
        this.randomSeed = randomSeed;
    }

    @Override
    /**
     * {@inheritDoc}
     */
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

    @Override
    /**
     * {@inheritDoc}
     */
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
     * Builder class for convenience, so you don't have to remember the numerous
     * constructor parameters.
     *
     * @author R.Scarberry
     * @since 1.0
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
