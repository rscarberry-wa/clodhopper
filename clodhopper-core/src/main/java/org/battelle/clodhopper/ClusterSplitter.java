package org.battelle.clodhopper;

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
 * ClusterSplitter.java
 *
 *===================================================================*/

/**
 * Define entities used for splitting clusters into new clusters whose members are subsets
 * of the original cluster's members.
 * 
 * @author R. Scarberry
 * @since 1.0
 *
 */
public interface ClusterSplitter {

	/**
	 * Returns true if the specified cluster can be split using
	 * this splitter.
	 * 
	 * @param cluster
	 * @return
	 */
	boolean canSplit(Cluster cluster);
	
	/**
	 * Returns true if this splitter prefers the clusters resulting from the split to
	 * the original cluster.
	 * 
	 * @param origCluster
	 * @param splitClusters
	 * @return
	 */
	boolean prefersSplit(Cluster origCluster, List<Cluster> splitClusters);
	
	/**
	 * Split the specified cluster, returning the split clusters in a list.
	 * 
	 * @param cluster
	 * @return
	 */
	List<Cluster> split(Cluster cluster);
	
}
