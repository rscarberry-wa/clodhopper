package org.battelle.clodhopper;

import java.util.*;
import java.util.List;

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
