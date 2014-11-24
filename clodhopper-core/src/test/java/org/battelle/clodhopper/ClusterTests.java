package org.battelle.clodhopper;

import java.util.Arrays;
import java.util.Random;
import org.battelle.clodhopper.tuple.*;
import org.battelle.clodhopper.util.ArrayIntIterator;
import org.battelle.clodhopper.util.IntIterator;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

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
 * 
 * ClusterTests.java
 *
 *===================================================================*/
public class ClusterTests {
    
    private static Random random;
    private static TupleList tuples;
    
    public ClusterTests() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        random = new Random();
        final int numTuples = 200;
        final int tupleLen = 10;
        double[] values = new double[tupleLen * numTuples];
        for (int i=0; i<values.length; i++) {
            values[i] = random.nextDouble();
        }
        tuples = new ArrayTupleList(tupleLen, numTuples, values);
    }
    
    @AfterClass
    public static void tearDownClass() {
        random = null;
        tuples = null;
    }
    
    /**
     * Test the center-related methods of Cluster.
     */
    @Test
    public void testGetCenter() {
        final Cluster cluster = generateRandomCluster();
        final double[] center = cluster.getCenter();
        final int centerLen = cluster.getCenterLength();
        // Length of the cluster center should be the same length as the tuples.
        assertTrue(centerLen == tuples.getTupleLength());
        // The copy of the center returned from getCenter() should be the length
        // returned by getCenterLength().
        assertTrue(center.length == centerLen);
        for (int i=0; i<centerLen; i++) {
            final double d1 = center[i];
            final double d2 = cluster.getCenterElement(i);
            // None of the elements should be NaN
            assertTrue(!Double.isNaN(d1) && !Double.isNaN(d2));
            // And they should be equal.
            assertTrue(d1 == d2);
        }
        final int memberCount = cluster.getMemberCount();
        final int[] memberIds = new int[memberCount];
        for (int i=0; i<memberCount; i++) {
            memberIds[i] = cluster.getMember(i);
        }
        // Recompute the center from the ids.
        final double[] center2 = TupleMath.average(tuples, new ArrayIntIterator(memberIds));
        // Create a new cluster from the same data.
        final Cluster cluster2 = new Cluster(memberIds, center2);
        // Ascertain that the clusters are equal.
        assertEquals(cluster, cluster2);
    }
    
    /**
     * Tests getMemberCount(), getMember(int n), and getMembers().
     */
    @Test
    public void testGetMember() {
        final Cluster cluster = generateRandomCluster();
        final int memberCount = cluster.getMemberCount();
        int n = 0;
        final IntIterator memberIt = cluster.getMembers();
        while(memberIt.hasNext()) {
            assertTrue(memberIt.getNext() == cluster.getMember(n++));
        }
        assertTrue(n == memberCount);
    }
    
    private Cluster generateRandomCluster() {
        final int elementCount = 10 + random.nextInt(20);
        final int tupleCount = tuples.getTupleCount();
        final int[] indexes = new int[tupleCount];
        for (int i=0; i<tupleCount; i++) {
            indexes[i] = i;
        }
        for (int j=tupleCount-1; j>0; j--) {
            int i = random.nextInt(j+1);
            if (i != j) {
                int tmp = indexes[i];
                indexes[i] = indexes[j];
                indexes[j] = tmp;
            }
        }
        final int memberCount = Math.min(tupleCount, 5 + random.nextInt(tupleCount/10));
        final int[] memberIds = new int[memberCount];
        
        System.arraycopy(indexes, 0, memberIds, 0, memberCount);     
        
        // Make a sorted copy for computation of the center. The order of iteration makes
        // a tiny difference in the calculated values because of round-off errors.
        final int[] memberIdsCopy = Arrays.copyOf(memberIds, memberCount);
        Arrays.sort(memberIdsCopy);
        
        final double[] center = TupleMath.average(tuples, new ArrayIntIterator(memberIdsCopy));

        return new Cluster(memberIds, center);
    }
}
