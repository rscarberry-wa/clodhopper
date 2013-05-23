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
			Throwable t = kmeans.getError();
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
