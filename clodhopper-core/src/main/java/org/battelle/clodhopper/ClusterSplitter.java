package org.battelle.clodhopper;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
 * ClusterSplitter.java
 *
 *===================================================================*/
/**
 * Define entities used for splitting clusters into new clusters whose members
 * are subsets of the original cluster's members.
 *
 * @author R. Scarberry
 * @since 1.0
 *
 */
public interface ClusterSplitter {

    /**
     * Returns true if the specified cluster can be split using this splitter.
     *
     * @param cluster the cluster to be tested.
     * 
     * @return true if the cluster can be split.
     */
    default boolean canSplit(Cluster cluster) {
        Objects.requireNonNull(cluster);
        return cluster.getMemberCount() > 1;
    }

    /**
     * Returns true if this splitter prefers the clusters resulting from the
     * split to the original cluster.
     *
     * @param origCluster the cluster that was split.
     * @param splitClusters the clusters resulting from the split.
     * 
     * @return true if replacing the original cluster with the split clusters is preferred.
     */
    boolean prefersSplit(Cluster origCluster, List<Cluster> splitClusters);

    /**
     * Split the specified cluster, returning the split clusters in a list.
     *
     * @param cluster the cluster to be split, which should never be null.
     * 
     * @return a list of clusters resulting from the split, which should never be null.
     */
    default List<Cluster> split(Cluster cluster) {
        Objects.requireNonNull(cluster);
        if (canSplit(cluster)) {
            List<Cluster> children = performSplit(cluster);
            int childCount = children != null ? children.size() : 0;
            if (childCount >= 2 && prefersSplit(cluster, children)) {
                return children;
            }
        }
        return Collections.singletonList(cluster);
    }

    /**
     * Implementations should perform the actual splitting of
     * a cluster in this method.
     *
     * @param cluster the <code>Cluster</code> to split.
     *
     * @return a list of clusters derived by splitting the source cluster.
     */
    List<Cluster> performSplit(Cluster cluster);
}
