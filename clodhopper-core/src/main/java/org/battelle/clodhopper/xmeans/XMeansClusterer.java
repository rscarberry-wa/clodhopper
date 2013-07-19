package org.battelle.clodhopper.xmeans;

import java.util.List;

import org.battelle.clodhopper.Cluster;
import org.battelle.clodhopper.ClusterSplitter;
import org.battelle.clodhopper.ClusterStats;
import org.battelle.clodhopper.Clusterer;
import org.battelle.clodhopper.kmeans.KMeansSplittingClusterer;
import org.battelle.clodhopper.tuple.TupleList;

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
 * XMeansClusterer.java
 *
 *===================================================================*/

public class XMeansClusterer extends KMeansSplittingClusterer 
	implements Clusterer {

	private double overallBIC;
	
	public XMeansClusterer(TupleList tuples, XMeansParams params) {
		super(tuples, params);
	}
	
	@Override
	public String taskName() {
		return "x-means clustering";
	}

	protected void initializeIteration(List<Cluster> clusters) {
		overallBIC = ClusterStats.computeBIC(tuples, clusters);
	}
	
	protected ClusterSplitter createSplitter(List<Cluster> clusters, Cluster cluster) {
		return new XMeansClusterSplitter(tuples, clusters, overallBIC, (XMeansParams) params);
	}

}
