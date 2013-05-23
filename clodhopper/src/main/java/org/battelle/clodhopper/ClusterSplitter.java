package org.battelle.clodhopper;

import java.util.List;

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
