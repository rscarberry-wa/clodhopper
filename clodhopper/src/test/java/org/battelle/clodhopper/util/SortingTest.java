package org.battelle.clodhopper.util;

import static org.junit.Assert.*;
import java.util.*;

import org.junit.Test;

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
 * SortingTest.java
 *
 *===================================================================*/

public class SortingTest {

	@Test
	public void testParallelSort() {

		final int numValues = 1000;
		
		double[] dValues = new double[numValues];
		int[] iValues = new int[numValues];
		
		Map<Double, Integer> checkMap = new HashMap<Double, Integer> ();
		
		Random random = new Random();
		for (int i=0; i<numValues; i++) {
			double d = random.nextDouble();
			dValues[i] = d;
			iValues[i] = i;	
			checkMap.put(d, i);
		}
		
		double[] dValuesCopy = (double[]) dValues.clone();
		
		Sorting.parallelSort(dValues, iValues);
		
		Arrays.sort(dValuesCopy);
		
		for (int i=0; i<numValues; i++) {
			assertTrue(dValuesCopy[i] == dValues[i]);
			assertTrue(checkMap.get(dValues[i]).intValue() == iValues[i]);
		}
	}

//	@Test 
//	public void testQuickSort1() {
//		
//		Random random = new Random();
//		int numValues = 100;
//		
//		final int[] values1 = new int[numValues];
//		int[] values2 = new int[numValues];
//		
//		for (int i=0; i<numValues; i++) {
//			int n = random.nextInt(1000);
//			values1[i] = n;
//			values2[i] = n;
//		}
//		
//		Sorting.quickSort(values1, new IntComparator() {
//			@Override
//			public int compare(int n1, int n2) {
//				return n1 < n2 ? -1 : n1 > n2 ? 1 : 0;
//			}			
//		});
//		
//		Arrays.sort(values2);
//		
//		for (int i=0; i<numValues; i++) {
//			assertTrue(values1[i] == values2[i]);
//		}
//	}
}
