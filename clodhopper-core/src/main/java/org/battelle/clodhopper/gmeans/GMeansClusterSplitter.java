package org.battelle.clodhopper.gmeans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.battelle.clodhopper.AbstractClusterSplitter;
import org.battelle.clodhopper.Cluster;
import org.battelle.clodhopper.ClusterStats;
import org.battelle.clodhopper.kmeans.KMeansClusterer;
import org.battelle.clodhopper.kmeans.KMeansParams;
import org.battelle.clodhopper.seeding.PreassignedSeeder;
import org.battelle.clodhopper.tuple.ArrayTupleList;
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
 * GMeansClusterSplitter.java
 *
 *===================================================================*/
/**
 * The cluster splitter variant used by g-means clustering.
 *
 * @author R. Scarberry
 * @since 1.0
 *
 */
public class GMeansClusterSplitter extends AbstractClusterSplitter {

    private static final Logger logger = Logger.getLogger(GMeansClusterSplitter.class);

    private TupleList tuples;
    private GMeansParams params;

    /**
     * Constructor
     *
     * @param tuples container for the data being clustered.
     * @param params the g-means clustering parameters.
     *
     * @throws NullPointerException if either of the parameters is null.
     */
    public GMeansClusterSplitter(TupleList tuples, GMeansParams params) {
        if (tuples == null || params == null) {
            throw new NullPointerException();
        }
        this.tuples = tuples;
        this.params = params;
    }

    @Override
    /**
     * Uses and Anderson-Darling gaussian test to determine whether or not to
     * prefer the split over the original cluster.
     *
     * @param origCluster the cluster that was split.
     * @param splitClusters the clusters resulting from the split.
     */
    public boolean prefersSplit(Cluster origCluster, List<Cluster> splitClusters) {
        return !TupleMath.andersonDarlingGaussianTest(projectToLineBetweenChildren(
                origCluster, splitClusters));
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public List<Cluster> performSplit(Cluster cluster) {
        TupleList seeds = createTwoSeeds(cluster);
        return runLocalKMeans(cluster, seeds);
    }

    /**
     * Projects the data in a cluster to the line connecting its two children's
     * centers.
     * 
     * @param cluster the parent cluster.
     * @param children the children of the cluster
     * 
     * @return an array containing 2 points defining a line.
     */
    private double[] projectToLineBetweenChildren(final Cluster cluster,
        final Collection<Cluster> children) {
        
        double[] projectedData = null;
        if (children.size() == 2) {
            Iterator<Cluster> it = children.iterator();
            double[] center1 = it.next().getCenter();
            double[] center2 = it.next().getCenter();
            int dim = center1.length;
            double[] projection = new double[dim];
            for (int i = 0; i < dim; i++) {
                projection[i] = center1[i] - center2[i];
            }
            projectedData = projectToVector(cluster, projection);
        }
        return projectedData;
    }

    /**
     * Projects all data in a cluster to one dimension, via the dot product with
     * a projection vector.
     * 
     * @param cluster the cluster of concern.
     * @param projection the projection vector.
     * 
     * @return the one-dimensional projection.
     */
    private double[] projectToVector(final Cluster cluster, final double[] projection) {
        int n = cluster.getMemberCount();
        int dim = tuples.getTupleLength();
        double[] projectedData = new double[n];
        double[] coords = new double[dim];
        for (int i = 0; i < n; i++) {
            tuples.getTuple(cluster.getMember(i), coords);
            projectedData[i] = TupleMath.dotProduct(coords, projection);
        }
        return projectedData;
    }

    /**
     * Create two cluster seeds by going +/- one standard deviation from the
     * cluster's center.
     * 
     * @param cluster the cluster of concern.
     *
     * @return TupleList containing two seeds
     */
    private TupleList createTwoSeeds(final Cluster cluster) {

        int dim = tuples.getTupleLength();

        double[][] stats = ClusterStats.computeMeanAndVariance(tuples, cluster);

        TupleList seeds = new ArrayTupleList(dim, 2);

        double[] seed1 = new double[dim];
        double[] seed2 = new double[dim];

        for (int i = 0; i < dim; i++) {
            double center = stats[i][0];
            double sdev = Math.sqrt(stats[i][1]);
            seed1[i] = center - sdev;
            seed2[i] = center + sdev;
        }

        seeds.setTuple(0, seed1);
        seeds.setTuple(1, seed2);

        return seeds;
    }

    /**
     * Runs k-means to split a cluster.
     *
     * @param cluster the cluster to split.
     * @param seeds the initial seeds for performing k-means to split the cluster.
     * 
     * @return a list of the resulting clusters.
     */
    protected List<Cluster> runLocalKMeans(final Cluster cluster, final TupleList seeds) {

        FilteredTupleList fcs = new FilteredTupleList(cluster.getMembers().toArray(), tuples);

        KMeansParams kparams = new KMeansParams.Builder()
                .clusterCount(seeds.getTupleCount())
                .maxIterations(Integer.MAX_VALUE)
                .movesGoal(0)
                .workerThreadCount(1)
                .replaceEmptyClusters(false)
                .distanceMetric(params.getDistanceMetric())
                .clusterSeeder(new PreassignedSeeder(seeds)).
                build();

        KMeansClusterer kmeans = new KMeansClusterer(fcs, kparams);
        kmeans.run();

        List<Cluster> clusters;
        try {
            clusters = kmeans.get();
        } catch (Exception e) {
            logger.error("error splitting cluster", e);
            return null;
        }

        int n = clusters.size();
        List<Cluster> clusterList = new ArrayList<Cluster>(n);
        for (int i = 0; i < n; i++) {
            Cluster c = clusters.get(i);
            int memCount = c.getMemberCount();
            int[] indexes = new int[memCount];
            for (int j = 0; j < memCount; j++) {
                indexes[j] = fcs.getFilteredIndex(c.getMember(j));
            }
            clusterList.add(new Cluster(indexes, c.getCenter()));
        }

        return clusterList;
    }

}
