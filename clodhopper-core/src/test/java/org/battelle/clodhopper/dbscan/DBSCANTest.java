/*
 * Copyright 2019 randy.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.battelle.clodhopper.dbscan;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import java.util.BitSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import org.battelle.clodhopper.Cluster;
import org.battelle.clodhopper.distance.EuclideanDistanceMetric;
import org.battelle.clodhopper.task.TaskAdapter;
import org.battelle.clodhopper.task.TaskEvent;
import org.battelle.clodhopper.task.TaskOutcome;
import org.battelle.clodhopper.tuple.TupleKDTree;
import org.battelle.clodhopper.tuple.TupleList;
import org.battelle.clodhopper.tuple.TupleMath;
import org.battelle.clodhopper.util.IntIterator;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 *
 * @author R.Scarberry
 */
public class DBSCANTest {

    @Test
    public void testDBCAN1() throws Exception {
        int tupleLength = 100;
        int tupleCount = 100;
        int clusterCount = 10;

        TupleList tuples = TupleMath.generateRandomGaussianTuples(tupleLength, tupleCount, clusterCount,
                new Random(), 0.15, 0.15);

        DBSCANParams params = new DBSCANParams(2.185, 5, new EuclideanDistanceMetric());
        DBSCANClusterer dbscan = new DBSCANClusterer(tuples, params);

        dbscan.addTaskListener(new TaskAdapter() {
            @Override
            public void taskBegun(TaskEvent e) {
                String msg = e.getMessage();
                if (msg == null) {
                    msg = "<null msg>";
                }
                System.out.println(msg);
            }

            @Override
            public void taskMessage(TaskEvent e) {
                System.out.println(e.getMessage());
            }

            @Override
            public void taskEnded(TaskEvent e) {
                String msg = e.getMessage();
                if (msg == null) {
                    msg = "<null msg>";
                }
                System.out.println(msg);
            }
        });

        new Thread(dbscan).start();
        List<Cluster> clusters = dbscan.get();

        if (dbscan.getTaskOutcome() != TaskOutcome.SUCCESS) {
            if (dbscan.getTaskOutcome() == TaskOutcome.ERROR) {
                if (dbscan.getError() != null) {
                    System.err.println(dbscan.getError());
                }
            }
            fail("dbscan did not succeed");
        }

        BitSet bitSet = new BitSet(tuples.getTupleCount());

        for (Cluster c : clusters) {
            IntIterator it = c.getMembers();
            while (it.hasNext()) {
                bitSet.set(it.getNext());
            }
            System.out.printf("Cluster size = %d\n", c.getMemberCount());
        }

        assertEquals(tuples.getTupleCount(), bitSet.cardinality());
        
        Optional<DBSCANClassification> classOpt = dbscan.getTupleClassification();
        
        assertTrue(classOpt.isPresent());
        
        DBSCANClassification classification = classOpt.get();
        
        assertEquals(0, intersection(classification.getCoreTupleIds(), 
                classification.getEdgeTupleIds()).size());
        assertEquals(0, intersection(classification.getCoreTupleIds(), 
                classification.getNoiseTupleIds()).size());
        assertEquals(0, intersection(classification.getEdgeTupleIds(), 
                classification.getNoiseTupleIds()).size());
        
        assertEquals(tuples.getTupleCount(), 
                classification.getCoreTupleIds().size() +
                        classification.getEdgeTupleIds().size() +
                        classification.getNoiseTupleIds().size());
        
        TupleKDTree kdtree = TupleKDTree.forTupleListBalanced(tuples, 
                params.getDistanceMetric());
        
        // Subtract one since the point itself won't be included in what's
        // returned from the kdtree.
        int minNeighbors = params.getMinSamples() - 1;
        
        TIntIterator coreIt = classification.getCoreTupleIds().iterator();
        while(coreIt.hasNext()) {
            int[] nbrs = kdtree.closeTo(coreIt.next(), params.getEpsilon());
            assertTrue(nbrs.length >= minNeighbors);
        }
        
        TIntIterator edgeIt = classification.getEdgeTupleIds().iterator();
        while(edgeIt.hasNext()) {
            int[] nbrs = kdtree.closeTo(edgeIt.next(), params.getEpsilon());
            assertFalse(nbrs.length >= minNeighbors);
        }

        TIntIterator noiseIt = classification.getNoiseTupleIds().iterator();
        while(noiseIt.hasNext()) {
            int[] nbrs = kdtree.closeTo(noiseIt.next(), params.getEpsilon());
            assertFalse(nbrs.length >= minNeighbors);
        }
    }
    
    private static TIntSet intersection(TIntSet set1, TIntSet set2) {
        TIntSet result = new TIntHashSet();
        TIntIterator it = set1.iterator();
        while(it.hasNext()) {
            int value = it.next();
            if (set2.contains(value)) {
                result.add(value);
            }
        }
        return result;
    }

}
