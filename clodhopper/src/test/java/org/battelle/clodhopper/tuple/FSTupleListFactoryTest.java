package org.battelle.clodhopper.tuple;

import static org.junit.Assert.*;

import org.junit.*;
import java.io.*;
import java.util.Random;

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
 * FSTupleListFactoryTest.java
 *
 *===================================================================*/

public class FSTupleListFactoryTest {

    private File dir;
    
    @Before
    public void createTempDir() {
        String dirName = "tempDir_";
        int n = 0;
        File d = null;
        boolean ok = false;
        while(true) {
            d = new File(dirName + n);
            if (!d.exists() && d.mkdir()) {
                ok = true;
                break;
            } else if (n >= 50) {
                break;
            }
            n++;
        }
        if (ok) {
            dir = d;
        }
    }
    
    @After
    public void deleteTempDir() {
        if (dir != null && dir.isDirectory()) {
            try {
                delete(dir);
            } catch (IOException ioe) {
                System.err.println(ioe.getMessage());
            }
        }
    }
    
    private static void delete(File f) throws IOException {
        if (f.isDirectory()) {
            File[] files = f.listFiles();
            for (int i=0; i<files.length; i++) {
                delete(files[i]);
            }   
        } 
        if (f.exists()) {
            if (!f.delete()) {
                System.err.println("could not delete: " + f.getAbsolutePath());
            }
        }
    }
    
    @Test
    public void testOne() throws Exception {
    
        FSTupleListFactory factory = new FSTupleListFactory(dir, 24L*1024L, 48L*1024L, 24L*1024L);
        
        TupleList tupleList1 = factory.createNewTupleList("one", 10, 256);
        
        Random random = new Random();
        
        double[] buffer = new double[10];
        for (int i=0; i<tupleList1.getTupleCount(); i++) {
            for (int j=0; j<buffer.length; j++) {
                buffer[j] = random.nextDouble();
            }
            tupleList1.setTuple(i, buffer);
        }
        
        assertTrue(tupleList1 instanceof ArrayTupleList);
        
        factory.closeTupleList(tupleList1);
        
        TupleList tupleList1Copy = factory.openExistingTupleList("one");
        
        assertTrue(tupleList1 != tupleList1Copy);
        
        assertTrue(tupleListsEqual(tupleList1, tupleList1Copy));
        
        TupleList tupleList2 = factory.createNewTupleList("two", 10, 400);
        
        assertTrue(tupleList2 instanceof FileMappedTupleList);
        
        TupleList tupleList3 = factory.createNewTupleList("three", 10, 756);
        
        assertTrue(tupleList3 instanceof MultiFileMappedTupleList);
        
        factory.closeAll();
        
    }

    public static boolean tupleListsEqual(TupleList tuples1, TupleList tuples2) {
        final int tupleLength = tuples1.getTupleLength();
        final int tupleCount = tuples1.getTupleCount();
        if (tupleLength == tuples2.getTupleLength() && tupleCount == tuples2.getTupleCount()) {
            double[] buffer1 = new double[tupleLength];
            double[] buffer2 = new double[tupleLength];
            for (int i=0; i<tupleCount; i++) {
                tuples1.getTuple(i, buffer1);
                tuples2.getTuple(i, buffer2);
                for (int j=0; j<tupleLength; j++) {
                    if (Double.doubleToLongBits(buffer1[j]) != Double.doubleToLongBits(buffer2[j])) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }
}
