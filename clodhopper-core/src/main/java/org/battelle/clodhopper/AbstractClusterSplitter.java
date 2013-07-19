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
	public boolean canSplit(Cluster cluster) {
		return cluster.getMemberCount() > 1;
	}

	@Override
	public abstract boolean prefersSplit(Cluster origCluster, List<Cluster> splitClusters);

	@Override
	public final List<Cluster> split(Cluster cluster) {
	    if (canSplit(cluster)) {
	        List<Cluster> children = performSplit(cluster);
	        int childCount = children != null ? children.size() : 0;
	        if (childCount >= 2 && prefersSplit(cluster, children)) {
	            return children;
	        }
	    }
	    return Arrays.asList(new Cluster[] { cluster });
	}
	
	protected abstract List<Cluster> performSplit(Cluster cluster);

}
