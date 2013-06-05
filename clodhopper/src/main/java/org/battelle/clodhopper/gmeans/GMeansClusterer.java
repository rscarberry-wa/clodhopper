package org.battelle.clodhopper.gmeans;

import java.util.List;

import org.battelle.clodhopper.Cluster;
import org.battelle.clodhopper.ClusterSplitter;
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
 * GMeansClusterer.java
 *
 *===================================================================*/

/**
 * Implementation of the g-means clustering algorithm.
 * <br />
 * See the following reference: 
 * 
 * Greg Hamerly, Charles Elkan: "Learning the k in k-means",
 * Neural Information Processing Systems, 2003
 * 
 * at http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.9.3574<br />

 * @author R.Scarberry
 * @since 1.0
 *
 */
public class GMeansClusterer extends KMeansSplittingClusterer {

	/**
	 * Constructor
	 * 
	 * @param tuples
	 * @param params
	 */
	public GMeansClusterer(TupleList tuples, GMeansParams params) {
		super(tuples, params);
	}
	
	@Override
	/**
	 * {@inheritDoc}
	 */
	public String taskName() {
		return "g-means clustering";
	}

	@Override
	/**
	 * For g-means, this is a no-op.
	 */
	protected void initializeIteration(List<Cluster> clusters) {
		// No op for g-means
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	protected ClusterSplitter createSplitter(List<Cluster> clusters,
			Cluster cluster) {
		return new GMeansClusterSplitter(tuples, (GMeansParams) params);
	}

}
