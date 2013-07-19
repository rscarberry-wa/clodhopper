package org.battelle.clodhopper.seeding;

import java.util.Arrays;
import java.util.Random;

import org.battelle.clodhopper.distance.DistanceMetric;
import org.battelle.clodhopper.tuple.ArrayTupleList;
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
 * KMeansPlusPlusSeeder.java
 *
 *===================================================================*/

public class KMeansPlusPlusSeeder extends RandomSeeder {
	
	private DistanceMetric distMetric;

	public KMeansPlusPlusSeeder(long seed, Random random, DistanceMetric distMetric) {
		super(seed, random);
		if (distMetric == null) {
			throw new NullPointerException();
		}
		this.distMetric = distMetric;
	}
	
	public KMeansPlusPlusSeeder(DistanceMetric distMetric) {
		this(System.nanoTime(), new Random(), distMetric);
	}
	
	@Override
	public TupleList generateSeeds(TupleList tuples, int seedCount) {

		if (seedCount <= 0) {
			throw new IllegalArgumentException();
		}

		final int tupleCount = tuples.getTupleCount();
		
		if (tupleCount == 0 && seedCount > 0) {
			throw new IllegalArgumentException("cannot generate seeds from an empty TupleList");
		}
		
		if (seedCount > tupleCount) {
			// Can't have more seeds that choices.
			seedCount = tupleCount;
		}
		
		final int tupleLength = tuples.getTupleLength();
		
		// Set the seed before doing anything using random number generation.
		random.setSeed(seed);
		
		// Generate the potential seeds.  This is just the tuple indexes shuffled.
		int[] potentialSeeds = getShuffledTupleIndexes(tupleCount, random);
		
		double[] minSqDists = new double[tupleCount];
		// Set to true to indicate when a potential seed is no longer available.
		boolean[] unavailable = new boolean[tupleCount];
		
		int[] seedList = new int[seedCount];
		int seedsFound = 0;
		
		int firstSeed = random.nextInt(tupleCount);

		// For accumulating the indexes of the tuples used as seeds.
		seedList[seedsFound++] = potentialSeeds[firstSeed];
		unavailable[firstSeed] = true;
		
		// Working buffers
		double[] buffer1 = new double[tupleLength];
		double[] buffer2 = new double[tupleLength];
		
		// Note that firstSeed is an index into potentialSeeds.  It's not
		// a tuple index itself.
		tuples.getTuple(potentialSeeds[firstSeed], buffer1);
		
		// Initialize minSqDists
		for (int i=0; i<tupleCount; i++) {
			if (i != firstSeed) {
				tuples.getTuple(potentialSeeds[i], buffer2);
				double dist = distMetric.distance(buffer1, buffer2);
				minSqDists[i] = dist*dist;
			}
		}
		
		while(seedsFound < seedCount) {
	
			// Sum the elements of minSqDists for those potential seeds that remain available.
			double sqDistSum = 0;
			for (int i=0; i<tupleCount; i++) {
				if (!unavailable[i]) {
					sqDistSum += minSqDists[i];
				}
			}
			
			// Compute a threshold value.
			double threshold = random.nextDouble() * sqDistSum;
			
			double probSum = 0;
			int newSeedIndex = -1;
			int lastAvailable = -1;
			
			for (int i=0; i<tupleCount; i++) {
				if (!unavailable[i]) {
					lastAvailable = i;
					probSum += minSqDists[i];
					if (probSum >= threshold) {
						newSeedIndex = i;
						break;
					}
				}
			}
			
			if (newSeedIndex == -1) {
				newSeedIndex = lastAvailable;
			}
			
			if (newSeedIndex >= 0) {
				
				seedList[seedsFound++] = newSeedIndex;
				unavailable[newSeedIndex] = true;
				
				if (seedsFound < seedCount) {
					
					// Update minSqDists using distance between the new seed index and the available seeds.
					//
					tuples.getTuple(potentialSeeds[newSeedIndex], buffer1);
					
					for (int i=0; i<tupleCount; i++) {
						if (!unavailable[i]) {
							tuples.getTuple(potentialSeeds[i], buffer2);
							double dist = distMetric.distance(buffer1, buffer2);
							double distSq = dist*dist;
							// Only update if the distance is smaller.
							if (distSq < minSqDists[i]) {
								minSqDists[i] = distSq;
							}
						}
					}
				}
				
			} else { // newSeedIndex == -1
				
				// Must break from while loop, since no other seeds are available whether
				// or not seedsFound == seedCount.
				break;
			}
		}
		
		TupleList seeds = new ArrayTupleList(tupleLength, seedsFound);
		
		Arrays.sort(seedList, 0, seedsFound);
		
		for (int i=0; i<seedsFound; i++) {
			tuples.getTuple(seedList[i], buffer1);
			seeds.setTuple(i, buffer1);
		}
		
		return seeds;
		
	}


}
