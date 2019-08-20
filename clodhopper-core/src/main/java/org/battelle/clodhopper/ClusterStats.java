package org.battelle.clodhopper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.battelle.clodhopper.distance.DistanceCache;
import org.battelle.clodhopper.distance.DistanceCacheFactory;
import org.battelle.clodhopper.distance.DistanceMetric;
import org.battelle.clodhopper.distance.FakeDistanceCache;
import org.battelle.clodhopper.distance.FileDistanceCache;
import org.battelle.clodhopper.distance.RAMDistanceCache;
import org.battelle.clodhopper.distance.ReadOnlyDistanceCache;
import org.battelle.clodhopper.tuple.FilteredTupleList;

import org.battelle.clodhopper.tuple.TupleList;
import org.battelle.clodhopper.tuple.TupleMath;

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
 * ClusterStats.java
 *
 *===================================================================*/
/**
 * Contains statistical calculation methods relating to clustering. Since all
 * methods are static, this class is uninstantiable.
 *
 * @author R. Scarberry
 * @since 1.0
 *
 */
public final class ClusterStats {

    /**
     * The natural logarithm of 2*Math.PI.
     */
    public static final double LOG2PI = Math.log(2 * Math.PI);

    // Constructor private to prevent instantiation.
    private ClusterStats() {
    }

    /**
     * Computes the mean and variance of the data from a subset of tuples
     * represented by a cluster.
     *
     * @param tuples a <code>TupleList</code> containing the data represented by
     * the cluster.
     * @param cluster the <code>Cluster</code>, which contains the included
     * tuple ids.
     *
     * @return a 2D array of dimensions [tupleLen][2], where tupleLen is the
     * length of the tuples. For a given index n in [0 - (tupleLen-1)], element
     * 0 is the mean, element 1 is the variance.
     */
    public static double[][] computeMeanAndVariance(final TupleList tuples, final Cluster cluster) {

        int tupleLen = tuples.getTupleLength();
        if (tupleLen != cluster.getCenterLength()) {
            throw new IllegalArgumentException(String.format(
                    "dimension mismatch: %d != %d", tupleLen, cluster.getCenterLength()));
        }
        double[][] result = new double[tupleLen][2];
        for (int i = 0; i < tupleLen; i++) {
            Arrays.fill(result[i], Double.NaN);
        }
        int sz = cluster.getMemberCount();
        if (sz > 0) {
            double[] buffer = new double[tupleLen];
            double[] sums = new double[tupleLen];
            double[] sumSqs = new double[tupleLen];
            for (int i = 0; i < sz; i++) {
                tuples.getTuple(cluster.getMember(i), buffer);
                for (int j = 0; j < tupleLen; j++) {
                    double v = buffer[j];
                    sums[j] += v;
                    sumSqs[j] += v * v;
                }
            }
            for (int j = 0; j < tupleLen; j++) {
                double mean = sums[j] / sz;
                result[j][0] = mean;
                result[j][1] = (sumSqs[j] - mean * sums[j]) / sz;
            }
        }
        return result;
    }

    /**
     * Computes the Bayes Information Criterion (BIC) for a list of clusters.
     * object.
     *
     * @param tuples the <code>TupleList</code> used to generate the clusters.
     * @param clusters a list containing the <code>Cluster</code>s.
     *
     * @return the Bayes Information Criterion.
     */
    public static double computeBIC(final TupleList tuples, final List<Cluster> clusters) {

        double bic = 0.0;

        // Get the number of clusters and the dimensionality.
        final int K = clusters.size();
        // Get the dimensionality
        final int M = tuples.getTupleLength();

        if (K > 0) {

            // Sum of the member counts.
            final int R = clusters.stream().mapToInt(c -> c.getMemberCount()).sum();

            final double logR = Math.log(R);

            final double LSum = clusters.stream().mapToDouble(c -> {

                int R_n = c.getMemberCount();

                double L = 0.0;

                // If R_n < K, sigma2 will be < 0, which will make L NaN, because of 
                // Math.log(sigma2).
                //
                if (R_n > K) {

                    // Estimate variance
                    double sigma2 = computeDistortion(tuples, c);
                    if (sigma2 > 0) {
                        sigma2 /= (R_n - K);
                    }

                    // Estimate log-likelihood
                    L = -R_n / 2 * LOG2PI - (R_n * M / 2) * Math.log(sigma2)
                            - (R_n - K) / 2 + R_n * Math.log(R_n) - R_n
                            * logR;
                }

                return L;

            }).sum();

            // Count the parameters in the model
            double p = K * (M + 1);
            // Compute the criterion
            bic = LSum - p / 2 * logR;

            // Added this on 3/13/2006 to normalize on cluster size. I don't
            // think the paper we got the bic formula from does this. -- R.Scarberry
            if (R > 0) {
                bic /= R;
            }
        }

        return bic;
    }

    /**
     * Computes the Bayes Information Criterion (BIC) for a single
     * {@code Cluster} object.
     *
     * @param tuples the <code>TupleList</code> from which the cluster was
     * generated.
     * @param cluster the <code>Cluster</code> being evaluated.
     *
     * @return the Bayes Information Criterion.
     */
    public static double computeBIC(final TupleList tuples, final Cluster cluster) {
        return computeBIC(tuples, new Cluster[]{cluster});
    }

    /**
     * Computes the Bayes Information Criterion for an array of {@code Cluster}
     * instances.
     *
     * @param tuples contains the tuple data for the clusters.
     * @param clusters an array of {@code Cluster} instances
     *
     * @return the Bayes Information Criterion.
     */
    public static double computeBIC(final TupleList tuples, final Cluster[] clusters) {
        return computeBIC(tuples, Arrays.asList(clusters));
    }

    private static double computeDistortion(final TupleList tuples, final Cluster cluster) {
        return cluster.getMemberCount() * TupleMath.norm1(computeVariance(tuples, cluster));
    }

    private static double[] computeVariance(final TupleList tuples, final Cluster cluster) {

        int n = cluster.getMemberCount();
        double[] center = cluster.getCenter();

        int dim = center.length;
        double[] variance = new double[dim];

        if (n > 0) {

            double[] sum = new double[dim];
            double[] buffer = new double[dim];
            int[] nonNaNCount = new int[dim];

            for (int i = 0; i < n; i++) {
                tuples.getTuple(cluster.getMember(i), buffer);
                for (int j = 0; j < dim; j++) {
                    double d = buffer[j];
                    if (!Double.isNaN(d)) {
                        sum[j] += d;
                        variance[j] += d * d;
                        nonNaNCount[j]++;
                    }
                }
            }

            for (int i = 0; i < dim; i++) {
                if (nonNaNCount[i] > 0) {
                    variance[i] = Math.max(0.0, (variance[i] - center[i] * sum[i]) / nonNaNCount[i]);
                }
            }
        }

        return variance;
    }

    /**
     * Find the nearest cluster in the list of clusters to the
     * specified cluster.
     *
     * @param clusters a list of clusters which cannot be null or of length less
     *   than 2
     * @param cluster a member of the list of clusters 
     * @param distanceMetric the distance metric to use, which must also not be
     *   null
     * @return nearest cluster int the cluster list
     */
    public static Cluster nearestCluster(
            List<Cluster> clusters,
            Cluster cluster,
            DistanceMetric distanceMetric) {

        Objects.requireNonNull(clusters);
        Objects.requireNonNull(cluster);
        Objects.requireNonNull(distanceMetric);

        final int sz = clusters.size();
        if (sz < 2) {
            throw new IllegalArgumentException("clusters must have at least 2 elements");
        }

        final double[] center = cluster.getCenter();
        
        Cluster nearestCluster = null;
        double minDistance = 0.0;
        
        for (Cluster c: clusters) {
            if (c != cluster) {
                double d = distanceMetric.distance(c.getCenter(), center);
                if (nearestCluster == null || d < minDistance) {
                    nearestCluster = c;
                    minDistance = d;
                }
            }
        }

        return nearestCluster;
    }

    public static Optional<List<Double>> computeSilhouetteCoefficients(
            TupleList tuples,
            List<Cluster> clusters,
            DistanceMetric distanceMetric,
            DistanceCacheFactory distanceCacheFactory) throws IOException {

        Objects.requireNonNull(tuples);
        Objects.requireNonNull(clusters);
        Objects.requireNonNull(distanceMetric);
        Objects.requireNonNull(distanceCacheFactory);
        
        final int numTuples = tuples.getTupleCount();
        final int numClusters = clusters.size();
        final double[] As = new double[numTuples];
        final double[] Bs = new double[numTuples];
        
        final int numThreads = Runtime.getRuntime().availableProcessors();
        
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        
        List<Double> result = null;
        
        try {
            
            List<Callable<double[]>> aCallables = new ArrayList<>(clusters.size());
            
            for (Cluster c: clusters) {
                aCallables.add(() -> {
                        ReadOnlyDistanceCache distanceCache = null;
                        try {
                        DistanceMetric dm = distanceMetric.clone();
                        Optional<ReadOnlyDistanceCache> opt = computePairwiseDistances(
                                tuples, c, dm, 
                                distanceCacheFactory);
                        distanceCache = opt.orElse(
                                new FakeDistanceCache(
                                        new FilteredTupleList(
                                                c.getMembers().toArray(), 
                                                tuples), 
                                        dm)
                        );
                        return computeAs(c, distanceCache);
                        } finally {
                            if (distanceCache instanceof FileDistanceCache) {
                                FileDistanceCache fdc = (FileDistanceCache) distanceCache;
                                fdc.closeFile();
                                fdc.getFile().delete();
                            }
                        }
                });
            }
            
            List<Future<double[]>> aFutures = executorService.invokeAll(aCallables);
            
            for (int i=0; i<numClusters; i++) {
                Cluster c = clusters.get(i);
                final double[] clusterAs = aFutures.get(i).get();
                final int numMembers = c.getMemberCount();
                for (int j=0; j<numMembers; j++) {
                    As[c.getMember(j)] = clusterAs[j];
                }
            }
            
            List<Callable<double[]>> bCallables = new ArrayList<>(numClusters);
            
            for (int i=0; i<numClusters; i++) {
                final int clusterId = i;
                bCallables.add(() -> {
                    return computeBs(tuples, clusters, clusterId, distanceMetric.clone());
                });
            }
            
            List<Future<double[]>> bFutures = executorService.invokeAll(bCallables);
            
            for (int i=0; i<numClusters; i++) {
                Cluster c = clusters.get(i);
                final double[] clusterBs = bFutures.get(i).get();
                final int numMembers = c.getMemberCount();
                for (int j=0; j<numMembers; j++) {
                    Bs[c.getMember(j)] = clusterBs[j];
                }
            }
            
        } catch (Exception e) {
            
        } finally {
            if (executorService != null) {
                executorService.shutdownNow();
            }
        }
        
        List<Double> clusterSilhouettes = new ArrayList<>(numClusters);
        for (Cluster c: clusters) {
            final int memberCount = c.getMemberCount();
            double sum = 0.0;
            for (int i=0; i<memberCount; i++) {
                final int tupleId = c.getMember(i);
                final double maxAB = Math.max(As[tupleId], Bs[tupleId]);
                double tupleSilhouette = Bs[tupleId] - As[tupleId];
                if (maxAB != 0.0) {
                    tupleSilhouette /= maxAB;
                }
                sum += tupleSilhouette;
            }
            clusterSilhouettes.add(sum/memberCount);
        }
        
        return Optional.ofNullable(clusterSilhouettes);
    }
    
    public static OptionalDouble computeSilhouetteCoefficient(
            TupleList tuples,
            List<Cluster> clusters,
            Cluster cluster,
            DistanceMetric distanceMetric,
            DistanceCacheFactory distanceCacheFactory) throws IOException {
        
        Objects.requireNonNull(tuples);
        Objects.requireNonNull(clusters);
        Objects.requireNonNull(cluster);
        Objects.requireNonNull(distanceMetric);
        Objects.requireNonNull(distanceCacheFactory);
        
        final int numMembers = cluster.getMemberCount();
        
        if (numMembers == 0) {
            throw new IllegalArgumentException("cluster is empty");
        }
        
        if (clusters.size() == 1 || numMembers == 1) {
            return OptionalDouble.of(0.0);
        }
        
        double[] a = new double[numMembers];
        double[] b = new double[numMembers];
        ReadOnlyDistanceCache intraDistCache = null;
        
        try {
            
            Optional<ReadOnlyDistanceCache> opt = computePairwiseDistances(
                    tuples, cluster, distanceMetric, distanceCacheFactory);
            if (!opt.isPresent()) {
                return OptionalDouble.empty();
            }
            
            intraDistCache = opt.get();
            
            for (int i=0; i<numMembers; i++) {
                double sum = 0.0;
                for (int j=0; j<numMembers; j++) {
                    if (i != j) {
                        sum += intraDistCache.getDistance(i, j);
                    }
                }
                a[i] = sum/(numMembers - 1);
            }
            
        } finally {
            if (intraDistCache instanceof FileDistanceCache) {
                FileDistanceCache fdc = (FileDistanceCache) intraDistCache;
                fdc.closeFile();
                fdc.getFile().delete();
            }
        }
        
        final Cluster nearestCluster = nearestCluster(clusters, cluster, distanceMetric);
        final int numMembers2 = nearestCluster.getMemberCount();
        final double[] tupleBuffer1 = new double[tuples.getTupleLength()];
        final double[] tupleBuffer2 = new double[tuples.getTupleLength()];
        
        for (int i=0; i<numMembers; i++) {
            double sum = 0.0;
            tuples.getTuple(cluster.getMember(i), tupleBuffer1);
            for (int j=0; j<numMembers2; j++) {
                tuples.getTuple(nearestCluster.getMember(j), tupleBuffer2);
                sum += distanceMetric.distance(tupleBuffer1, tupleBuffer2);
            }
            b[i] = sum/numMembers2;
        }
        
        double sum = 0.0;
        for (int i=0; i<numMembers; i++) {
            sum += (b[i] - a[i])/Math.max(a[i], b[i]);
        }
        
        return OptionalDouble.of(sum/numMembers);
    }
    
    private static double[] computeAs(
            Cluster cluster,
            ReadOnlyDistanceCache distanceCache) throws IOException {
        
        final int numMembers = cluster.getMemberCount();
        final double[] result = new double[numMembers];
        
        if (numMembers > 1) {
            for (int i=0; i<numMembers-1; i++) {
                for (int j=i+1; j<numMembers; j++) {
                    // Use the indices for the cluster, not the indices into
                    // tuples.
                    double d = distanceCache.getDistance(i, j);
                    result[i] += d;
                    result[j] += d;
                }
            }
            for (int i=0; i<numMembers; i++) {
                result[i] /= (numMembers - 1);
            }
        }
        
        return result;
    }
    
    private static double[] computeBs(
            TupleList tuples,
            List<Cluster> clusters,
            int clusterId,
            DistanceMetric distanceMetric) {
            
            final int[] members = clusters.get(clusterId).getMembers().toArray();
            final double[] Bs = new double[members.length];
            
            final double[] tupleBuffer1 = new double[tuples.getTupleLength()];
            final double[] tupleBuffer2 = new double[tuples.getTupleLength()];
            
            for (int i=0; i<members.length; i++) {
                int tupleId = members[i];
                int nearestClusterId = findSecondNearestCluster(
                        tuples, clusters, tupleId, clusterId, distanceMetric
                );
                final Cluster nearestCluster = clusters.get(nearestClusterId);
                final int nearestClusterMemberCount = nearestCluster.getMemberCount();
                tuples.getTuple(tupleId, tupleBuffer1);
                for (int j=0; j<nearestClusterMemberCount; j++) {
                    tuples.getTuple(nearestCluster.getMember(j), tupleBuffer2);
                    Bs[i] += distanceMetric.distance(tupleBuffer1, tupleBuffer2);
                }
                Bs[i] /= nearestClusterMemberCount;
            }
            
            return Bs;
    }
    
    private static double[] computeBs(
            TupleList tuples,
            List<Cluster> clusters,
            int[] tupleIds,
            int[] nearestClusterIds,
            DistanceMetric distanceMetric
    ) {
        final double[] Bs = new double[tupleIds.length];
        for (int i=0; i<Bs.length; i++) {
            Bs[i] = computeB(tuples, clusters, tupleIds[i], 
                    nearestClusterIds[i], distanceMetric);
        }
        return Bs;
    }
            
    private static double computeB(
            TupleList tuples, 
            List<Cluster> clusters, 
            int tupleId,
            int nearestClusterId,
            DistanceMetric distanceMetric) {
        
        final Cluster nearestCluster = clusters.get(nearestClusterId);
        
        final int numMembers = nearestCluster.getMemberCount();
        final double[] tuple = tuples.getTuple(tupleId, null);
        final double[] tupleBuffer = new double[tuple.length];
        
        double sum = 0.0;
        for (int i=0; i<numMembers; i++) {
            tuples.getTuple(nearestCluster.getMember(i), tupleBuffer);
            sum += distanceMetric.distance(tuple, tupleBuffer);
        }
        
        return sum/numMembers;
    }

    /**
     * Computes the distances between every 2 members in a cluster. If the
     * cluster has n members, the total number of pairwise distances is
     * n(n-1)/2, since the distance(i,j) == distance(j,i). The distance
     * from a tuple member with itself is always 0, so this is not computed.
     * 
     * @param tuples contains the tuple data referenced by the cluster membership
     * @param cluster the cluster, which contains the member indexes into
     *   tuples
     * @param distanceMetric the distance metric to use
     * @param distanceCacheFactory the factory for creating the distance cache
     * @return a {@code ReadOnlyDistanceCache} containing the pairwise distances
     *   wrapped in an optional.
     */
    public static Optional<ReadOnlyDistanceCache> computePairwiseDistances(
            TupleList tuples,
            Cluster cluster,
            DistanceMetric distanceMetric,
            DistanceCacheFactory distanceCacheFactory) throws IOException {
            
        Objects.requireNonNull(tuples);
        Objects.requireNonNull(cluster);
        Objects.requireNonNull(distanceMetric);
        Objects.requireNonNull(distanceCacheFactory);
        
        final int numMembers = cluster.getMemberCount();

        double[] tupleBuffer1 = new double[tuples.getTupleLength()];
        double[] tupleBuffer2 = new double[tuples.getTupleLength()];

        DistanceCache distanceCache = distanceCacheFactory.newDistanceCache(
                numMembers).orElse(null);

        if (distanceCache != null) {
            for (int i = 0; i < numMembers - 1; i++) {
                tuples.getTuple(cluster.getMember(i), tupleBuffer1);
                for (int j = i + 1; j < numMembers; j++) {
                    tuples.getTuple(cluster.getMember(j), tupleBuffer2);
                    distanceCache.setDistance(i, j,
                            distanceMetric.distance(tupleBuffer1, tupleBuffer2));
                }
            }
        }

        return Optional.ofNullable(distanceCache);
    }
    
    public static int[] findSecondNearestClusters(
        TupleList tuples,
        List<Cluster> clusters,
        int[] tupleIds,
        int[] memberShipClusterIds,
        DistanceMetric distanceMetric) {

        Objects.requireNonNull(tuples);
        Objects.requireNonNull(clusters);
        Objects.requireNonNull(distanceMetric);
        Objects.requireNonNull(tupleIds);
        Objects.requireNonNull(memberShipClusterIds);
        
        if (tupleIds.length != memberShipClusterIds.length) {
            throw new IllegalArgumentException(
                    String.format(
                            "tupleIds.length must equal memberShipClusterIds.length: %d != %d",
                            tupleIds.length, memberShipClusterIds.length));
        }
        
        final int[] nearestClusterIds = new int[tupleIds.length];
        
        for (int i=0; i<tupleIds.length; i++) {
            nearestClusterIds[i] = findSecondNearestCluster(
                tuples, clusters, tupleIds[i], memberShipClusterIds[i],
                    distanceMetric);
        }
        
        return nearestClusterIds;
    }
    
    public static int findSecondNearestCluster(
            TupleList tuples, 
            List<Cluster> clusters, 
            int tupleId,
            int memberShipClusterId,
            DistanceMetric distanceMetric) {
        
        Objects.requireNonNull(tuples);
        Objects.requireNonNull(clusters);
        Objects.requireNonNull(distanceMetric);
        
        final double[] tuple = tuples.getTuple(tupleId, null);
        final int numClusters = clusters.size();
        
        int nearestCluster = -1;
        double minDistance = 0.0;
        
        for (int i=0; i<numClusters; i++) {
            if (i != memberShipClusterId) {
                double d = distanceMetric.distance(tuple, clusters.get(i).getCenter());
                if (nearestCluster == -1 || d < minDistance) {
                    nearestCluster = i;
                    minDistance = d;
                }
            }
        }
        
        return nearestCluster;
    }
}
