package org.battelle.clodhopper;

import java.util.*;
import java.util.List;

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
 * AbstractClusterSplitter.java
 *
 *===================================================================*/
public abstract class AbstractClusterSplitter implements ClusterSplitter {

    /**
     * Returns true if the number of members in the cluster is greater than 1.
     */
    @Override
    public boolean canSplit(final Cluster cluster) {
        return cluster.getMemberCount() > 1;
    }

    /**
     * Splits a cluster, but only if <code>canSpit(cluster)</code> returns true. After performing
     * the split the results are only returned if the <code>prefersSplit</code> method returns
     * also returns true. Otherwise, a singleton list containing the original cluster is returned.
     * 
     * @param cluster the cluster to be split, if a split is actually performed.
     * 
     * @return a list either containing the clusters resulting from the split or a singleton
     *   list containing the starting cluster.
     */
    @Override
    public final List<Cluster> split(final Cluster cluster) {
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
     * Subclasses must implement this method to perform the actual splitting of
     * a cluster.
     * 
     * @param cluster the <code>Cluster</code> to split.
     * 
     * @return a list of clusters derived by splitting the source cluster. 
     */
    protected abstract List<Cluster> performSplit(Cluster cluster);

}
