package org.battelle.clodhopper.kmeans;

import static org.junit.Assert.*;

import org.battelle.clodhopper.Cluster;
import org.battelle.clodhopper.Clusterer;
import org.battelle.clodhopper.kmeans.KMeansClusterer;
import org.battelle.clodhopper.kmeans.KMeansParams;
import org.battelle.clodhopper.task.*;
import org.battelle.clodhopper.tuple.TupleList;
import org.battelle.clodhopper.tuple.TupleMath;
import org.junit.Test;
import java.util.*;
import java.util.concurrent.ExecutionException;

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
 * KMeansTest.java
 *
 *===================================================================*/

public class KMeansTest {

	@Test
	public void testKMeans1() {
		
		int tupleLength = 100;
		int tupleCount = 100;
		int clusterCount = 10;
		
		TupleList tuples = TupleMath.generateRandomGaussianTuples(tupleLength, tupleCount, clusterCount, 
				new Random(), 0.15, 0.15);
		
		KMeansParams params = new KMeansParams.Builder().clusterCount(clusterCount).build();
		
		KMeansClusterer kmeans = new KMeansClusterer(tuples, params);
		
		kmeans.addTaskListener(new TaskAdapter() {
			@Override
			public void taskBegun(TaskEvent e) {
				String msg = e.getMessage();
				if (msg == null) { msg = "<null msg>"; }
				System.out.println(msg);
			}

			@Override
			public void taskMessage(TaskEvent e) {
				System.out.println(e.getMessage());
			}

			@Override
			public void taskEnded(TaskEvent e) {
				String msg = e.getMessage();
				if (msg == null) { msg = "<null msg>"; }
				System.out.println(msg);
			}
		});
		
		new Thread(kmeans).start();
		
		List<Cluster> clusters = null;
		
		try {
			
			clusters = kmeans.get();
		
		} catch (Exception e1) {
			
			e1.printStackTrace();
			
		}
		
		if (kmeans.getTaskOutcome() == TaskOutcome.ERROR) {
			Throwable t = kmeans.getError().orElse(null);
			if (t != null) {
				System.out.println(t.toString());
			}
		}
		
		assertTrue(kmeans.getTaskOutcome() == TaskOutcome.SUCCESS);
		
		for (Cluster c : clusters) {
			System.out.printf("Cluster size = %d\n", c.getMemberCount());
		}
	}

}
