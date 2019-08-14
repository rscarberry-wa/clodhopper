package org.battelle.clodhopper;

import java.util.Arrays;
import java.util.List;

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
     * @param tuples the <code>TupleList</code> from which the cluster was generated.
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

}
