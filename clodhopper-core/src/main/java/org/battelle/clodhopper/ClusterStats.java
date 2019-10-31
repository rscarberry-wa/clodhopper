package org.battelle.clodhopper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import org.battelle.clodhopper.distance.DistanceMetric;
import org.battelle.clodhopper.distance.ReadOnlyDistanceCache;
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
 * Contains statistical calculation methods relating to clustering. Since all methods are static, this class is
 * uninstantiable.
 *
 * @author R. Scarberry
 * @since 1.0
 *
 */
public final class ClusterStats {

    /**
     * The natural logarithm of 2*Math.PI.
     */
    private static final double LOG2PI = Math.log(2 * Math.PI);

    // Constructor private to prevent instantiation.
    private ClusterStats() {
    }

    /**
     * Computes the mean and variance of the data from a subset of tuples represented by a cluster.
     *
     * @param tuples a <code>TupleList</code> containing the data represented by the cluster.
     * @param cluster the <code>Cluster</code>, which contains the included tuple ids.
     *
     * @return a 2D array of dimensions [tupleLen][2], where tupleLen is the length of the tuples. For a given index n
     * in [0 - (tupleLen-1)], element 0 is the mean, element 1 is the variance.
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
     * Computes the Bayes Information Criterion (BIC) for a list of clusters. object.
     *
     * @param tuples the <code>TupleList</code> used to generate the clusters.
     * @param clusters a list containing the <code>Cluster</code>s.
     *
     * @return the Bayes Information Criterion.
     */
    public static double computeBIC(final TupleList tuples, final List<Cluster> clusters) {

        Objects.requireNonNull(tuples);
        Objects.requireNonNull(clusters);
        
        double bic = 0.0;

        // Get the number of clusters and the dimensionality.
        final int clusterCount = clusters.size();
        // Get the dimensionality
        final int tupleLength = tuples.getTupleLength();

        if (clusterCount > 0) {

            // Sum of the member counts. This is usually equal to the number of tuples, since
            // all are typically clustered.
            final int clusterMemberSum = clusters.stream()
                    .mapToInt(Cluster::getMemberCount)
                    .sum();

            final double logClusterMemberSum = Math.log(clusterMemberSum);

            final double logLikelihoodSum = clusters.stream().mapToDouble(c ->
                    computeLogLikelihood(tuples, clusterCount, c, logClusterMemberSum)
            ).sum();

            // Count the parameters in the model
            double p = clusterCount * (tupleLength + 1);
            // Compute the criterion
            bic = logLikelihoodSum - p / 2 * logClusterMemberSum;

            // Added this on 3/13/2006 to normalize on cluster size. I don't
            // think the paper we got the bic formula from does this. -- R.Scarberry
            if (clusterMemberSum > 0) {
                bic /= clusterMemberSum;
            }
        }

        return bic;
    }

    /**
     * Computes the log likelihood score for a given cluster.
     * @param tuples contains the tuple data. Must not be null.
     * @param clusterCount the number of clusters in the cluster distribution.
     * @param cluster the cluster for which to compute the log likelihood. Must not be null.
     * @param logClusterMemberSum the log of the sum of all cluster members.
     * @return the log likelihood.
     */
    private static double computeLogLikelihood(
            final TupleList tuples,
            final int clusterCount,
            final Cluster cluster,
            final double logClusterMemberSum) {

        // Declare as doubles since they are used in floating point calculations.
        final double tupleLength = tuples.getTupleLength();
        final double memberCount = cluster.getMemberCount();

        double logLikelihood = 0.0;

        // If R_n < K, sigma2 will be < 0, which will make L NaN, because of
        // Math.log(sigma2).
        //
        if (memberCount > clusterCount) {

            // Estimate variance
            double sigma2 = computeDistortion(tuples, cluster);
            if (sigma2 > 0) {
                sigma2 /= (memberCount - clusterCount);
            }

            // Estimate log-likelihood
            logLikelihood = -memberCount / 2 * LOG2PI - (memberCount * tupleLength / 2) * Math.log(sigma2)
                - (memberCount - clusterCount) / 2 + memberCount * Math.log(memberCount) - memberCount
                * logClusterMemberSum;
        }

        return logLikelihood;
    }

    /**
     * Computes the Bayes Information Criterion (BIC) for a single {@code Cluster} object.
     *
     * @param tuples the <code>TupleList</code> from which the cluster was generated.
     * @param cluster the <code>Cluster</code> being evaluated.
     *
     * @return the Bayes Information Criterion.
     */
    public static double computeBIC(final TupleList tuples, final Cluster cluster) {
        return computeBIC(tuples, new Cluster[]{cluster});
    }

    /**
     * Computes the Bayes Information Criterion for an array of {@code Cluster} instances.
     *
     * @param tuples contains the tuple data for the clusters.
     * @param clusters an array of {@code Cluster} instances
     *
     * @return the Bayes Information Criterion.
     */
    public static double computeBIC(final TupleList tuples, final Cluster[] clusters) {
        return computeBIC(tuples, Arrays.asList(clusters));
    }

    /**
     * Computes the distortion measure for a cluster
     * @param tuples contains the tuple data.
     * @param cluster the cluster whose distortion is to be computed.
     * @return the distortion.
     */
    private static double computeDistortion(final TupleList tuples, final Cluster cluster) {
        return cluster.getMemberCount() * TupleMath.norm1(computeVariance(tuples, cluster));
    }

    /**
     * Computes the member variance of a cluster.
     * @param tuples contains the tuple data.
     * @param cluster the cluster whose variance is to be computed.
     * @return the variance.
     */
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
     * Find the nearest cluster in the list of clusters to the specified cluster.
     *
     * @param clusters a list of clusters which cannot be null or of length less than 2
     * @param cluster a member of the list of clusters
     * @param distanceMetric the distance metric to use, which must also not be null
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

        for (Cluster c : clusters) {
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

    // The executor to use in asynchronous computations when no executor is
    // provided as an argument.
    private static final Executor DEFAULT_EXECUTOR
            = (ForkJoinPool.getCommonPoolParallelism() > 1) ? ForkJoinPool.commonPool()
            // Single thread per runnable.
            : runnable -> new Thread(runnable).start();

    private static CompletableFuture<Double> computeSilhouetteCoefficientAsync(
            final double[] clusterMemberAs,
            final double[] clusterMemberBs,
            final Executor executor
    ) {
        return CompletableFuture.supplyAsync(() -> computeSilhouetteCoefficient(clusterMemberAs, clusterMemberBs),
            executor);
    }

    private static double computeSilhouetteCoefficient(
            final double[] clusterMemberAs,
            final double[] clusterMemberBs) {
        final int numMembers = clusterMemberAs.length;
        double sum = 0.0;
        for (int i = 0; i < numMembers; i++) {
            double memberA = clusterMemberAs[i];
            double memberB = clusterMemberBs[i];
            double max = Math.max(memberA, memberB);
            double memberS = max != 0.0 ? (memberB - memberA) / max : 0.0;
            sum += memberS;
        }
        return sum / numMembers;
    }

    public static List<Double> computeSilhouetteCoefficients(
            TupleList tuples,
            List<Cluster> clusters,
            DistanceMetric distanceMetric,
            Executor executor) throws ClusteringException {

        Objects.requireNonNull(tuples);
        Objects.requireNonNull(clusters);
        Objects.requireNonNull(distanceMetric);

        final int numClusters = clusters.size();

        try {

            // Will contain ones for computes both As and Bs for the
            // members of each cluster. They'll be in the order: a, b, a, b, ...
            List<CompletableFuture<double[]>> abFutures = new ArrayList<>(2 * numClusters);

            for (int i = 0; i < numClusters; i++) {
                // Use clones of the distance metrics, since they're not
                // threadsafe.
                abFutures.add(computeAsAsync(tuples, clusters.get(i),
                        distanceMetric.clone(), executor)
                );
                abFutures.add(computeBsAsync(tuples, clusters, i,
                        distanceMetric.clone(), executor));
            }

            CompletableFuture<Void> abFuturesAll = CompletableFuture.allOf(
                    abFutures.toArray(new CompletableFuture[abFutures.size()]));

            CompletableFuture<List<double[]>> abFuturesResult = abFuturesAll.thenApply(
                    v -> abFutures.stream()
                                .map(CompletableFuture::join)
                                .collect(Collectors.toList())
            );

            final List<double[]> clusterABs = abFuturesResult.get();

            List<CompletableFuture<Double>> sFutures = new ArrayList<>(numClusters);
            for (int i = 0; i < clusterABs.size(); i += 2) {
                sFutures.add(computeSilhouetteCoefficientAsync(
                        clusterABs.get(i), clusterABs.get(i + 1), executor));
            }

            CompletableFuture<Void> sFuturesAll = CompletableFuture.allOf(
                    sFutures.toArray(new CompletableFuture[sFutures.size()]));

            CompletableFuture<List<Double>> sFuturesResult = sFuturesAll.thenApply(
                    v -> sFutures.stream().map(CompletableFuture::join).collect(Collectors.toList()));

            return sFuturesResult.get();

        } catch (ExecutionException | InterruptedException e) {
            throw new ClusteringException("error computing silhouette coefficients", e);
        }
    }

    public static List<Double> computeSilhouetteCoefficients(
            TupleList tuples,
            List<Cluster> clusters,
            DistanceMetric distanceMetric) throws ClusteringException {
        return computeSilhouetteCoefficients(tuples, clusters, distanceMetric,
                DEFAULT_EXECUTOR);
    }
    
    public static List<Double> computeSilhouetteCoefficientsSequentially(
            TupleList tuples,
            List<Cluster> clusters,
            DistanceMetric distanceMetric) {

        Objects.requireNonNull(tuples);
        Objects.requireNonNull(clusters);
        Objects.requireNonNull(distanceMetric);

        final int numClusters = clusters.size();

        final List<Double> silhouetteCoefficients = new ArrayList<>(numClusters);

        int cId = 0;
        for (Cluster c : clusters) {
            final double[] As = computeAs(tuples, c, distanceMetric);
            final double[] Bs = computeBs(tuples, clusters, cId++, distanceMetric);
            final int numMembers = c.getMemberCount();
            double sSum = 0.0;
            for (int i = 0; i < numMembers; i++) {
                double denom = Math.max(As[i], Bs[i]);
                if (denom != 0.0) {
                    sSum += (Bs[i] - As[i]) / denom;
                }
            }
            silhouetteCoefficients.add(sSum / numMembers);
        }

        return silhouetteCoefficients;
    }

    /**
     * Returns a completable future for asynchronous computation of the As for a cluster. Used
     * in the computation of the silhouette coefficient.
     *
     * @param tuples contains the tuple data.
     * @param cluster the cluster whose member As are to be computed.
     * @param distanceMetric the distance metric.
     * @param executor the executor.
     * @return a completable future.
     */
    private static CompletableFuture<double[]> computeAsAsync(
            final TupleList tuples,
            final Cluster cluster,
            final DistanceMetric distanceMetric,
            final Executor executor) {
        return CompletableFuture.supplyAsync(() -> computeAs(tuples, cluster, distanceMetric), executor);
    }

    /**
     * Returns a completable future for asynchronous computation of the Bs for a cluster. Used
     * in the computation of the silhouette coefficient.
     *
     * @param tuples contains the tuple data.
     * @param clusters the list of clusters.
     * @param clusterId the index of the cluster of interest.
     * @param distanceMetric the distance metric to use.
     * @param executor the executor.
     * @return a completable future.
     */
    private static CompletableFuture<double[]> computeBsAsync(
            final TupleList tuples,
            final List<Cluster> clusters,
            final int clusterId,
            final DistanceMetric distanceMetric,
            final Executor executor
    ) {
        return CompletableFuture.supplyAsync(() -> computeBs(tuples, clusters, clusterId, distanceMetric), executor);
    }

    /**
     * Computes the As for a cluster.
     *
     * @param tuples contains the tuple data.
     * @param cluster the cluster of interest.
     * @param distanceMetric the distance metric.
     * @return an array of the As for the members of the cluster.
     */
    private static double[] computeAs(
            TupleList tuples,
            Cluster cluster,
            DistanceMetric distanceMetric) {

        final int numMembers = cluster.getMemberCount();
        final double[] result = new double[numMembers];

        if (numMembers > 1) {
            final double[] tupleBuffer1 = new double[tuples.getTupleLength()];
            final double[] tupleBuffer2 = new double[tuples.getTupleLength()];
            for (int i = 0; i < numMembers - 1; i++) {
                tuples.getTuple(cluster.getMember(i), tupleBuffer1);
                for (int j = i + 1; j < numMembers; j++) {
                    tuples.getTuple(cluster.getMember(j), tupleBuffer2);
                    double d = distanceMetric.distance(tupleBuffer1, tupleBuffer2);
                    result[i] += d;
                    result[j] += d;
                }
            }
            for (int i = 0; i < numMembers; i++) {
                result[i] /= (numMembers - 1);
            }
        }

        return result;
    }

    private static double[] computeAs(
            Cluster cluster,
            ReadOnlyDistanceCache distanceCache) throws IOException {

        final int numMembers = cluster.getMemberCount();
        final double[] result = new double[numMembers];

        if (numMembers > 1) {
            for (int i = 0; i < numMembers - 1; i++) {
                for (int j = i + 1; j < numMembers; j++) {
                    // Use the indices for the cluster, not the indices into
                    // tuples.
                    double d = distanceCache.getDistance(i, j);
                    result[i] += d;
                    result[j] += d;
                }
            }
            for (int i = 0; i < numMembers; i++) {
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

        for (int i = 0; i < members.length; i++) {
            int tupleId = members[i];
            int nearestClusterId = findSecondNearestCluster(
                    tuples, clusters, tupleId, clusterId, distanceMetric
            );
            final Cluster nearestCluster = clusters.get(nearestClusterId);
            final int nearestClusterMemberCount = nearestCluster.getMemberCount();
            tuples.getTuple(tupleId, tupleBuffer1);
            for (int j = 0; j < nearestClusterMemberCount; j++) {
                tuples.getTuple(nearestCluster.getMember(j), tupleBuffer2);
                Bs[i] += distanceMetric.distance(tupleBuffer1, tupleBuffer2);
            }
            Bs[i] /= nearestClusterMemberCount;
        }

        return Bs;
    }

    private static int findSecondNearestCluster(
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

        for (int i = 0; i < numClusters; i++) {
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
