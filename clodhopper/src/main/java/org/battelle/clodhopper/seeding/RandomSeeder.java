package org.battelle.clodhopper.seeding;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import org.battelle.clodhopper.tuple.ArrayTupleList;
import org.battelle.clodhopper.tuple.TupleList;

public class RandomSeeder implements RandomClusterSeeder {

	protected long seed;
	protected Random random;
	
	public RandomSeeder(long seed, Random random) {
		this.seed = seed;
		if (random != null) {
			this.random = random;
		} else {
			this.random = new Random();
		}
	}
	
	public RandomSeeder() {
		this(System.nanoTime(), new Random());
	}
	
	public long getRandomGeneratorSeed() {
		return seed;
	}
	
	public void setRandomGeneratorSeed(long seed) {
		this.seed = seed;
	}
	
	@Override
	public TupleList generateSeeds(TupleList tuples, int seedCount) {

		if (seedCount <= 0) {
			throw new IllegalArgumentException();
		}

		int tupleCount = tuples.getTupleCount();
		
		if (tupleCount == 0 && seedCount > 0) {
			throw new IllegalArgumentException("cannot generate seeds from an empty TupleList");
		}
		
		int tupleLength = tuples.getTupleLength();
		
        random.setSeed(seed);

        int[] indices = getShuffledTupleIndexes(tupleCount, random);

        int centersFound = 0;
        Map<SeedCandidate, SeedCandidate> seedMap = new LinkedHashMap<SeedCandidate, SeedCandidate>(
                seedCount * 2);

        for (int i = 0; i < tupleCount && centersFound < seedCount; i++) {
            int ndx = indices[i];
            double[] center = new double[tupleLength];
            tuples.getTuple(ndx, center);
            SeedCandidate seed = new SeedCandidate(center);
            if (!seedMap.containsKey(seed)) {
            	seedMap.put(seed, seed);
                centersFound++;
            }
        }

        TupleList seeds = new ArrayTupleList(tupleLength, centersFound);
        int n = 0;
        for (SeedCandidate seed : seedMap.keySet()) {
        	seeds.setTuple(n++, seed.getCenter());
        }
        
        return seeds;
	}
	
	protected static int[] getShuffledTupleIndexes(final int tupleCount, final Random random) {
		int[] shuffledIndexes = new int[tupleCount];
		// Place the indexes in an array.
		for (int i=0; i<tupleCount; i++) {
			shuffledIndexes[i] = i;
 		}
		// Now shuffle them.
		for (int i=tupleCount-1; i>0; i--) {
			int j = random.nextInt(i);
			if (i != j) {
				int tmp = shuffledIndexes[i];
				shuffledIndexes[i] = shuffledIndexes[j];
				shuffledIndexes[j] = tmp;
			}
		}
		return shuffledIndexes;
	}

	static class SeedCandidate {
		
		private double[] center;
		
		SeedCandidate(double[] center) {
			this.center = center;
		}
		
		double[] getCenter() {
			return center;
		}
		
		public int hashCode() {
            int hc = 0;
            int len = center.length;
            if (len > 0) {
                long l = Double.doubleToLongBits(center[0]);
                hc = (int) (l ^ (l >>> 32));
                for (int i = 1; i < len; i++) {
                    l = Double.doubleToLongBits(center[i]);
                    hc = 37 * hc + (int) (l ^ (l >>> 32));
                }
            }
            return hc;
		}
		
        public boolean equals(Object o) {
            if (o == this)
                return true;
            if (o instanceof SeedCandidate) {
                double[] otherCenter = ((SeedCandidate) o).center;
                int n = this.center.length;
                if (n == otherCenter.length) {
                    for (int i = 0; i < n; i++) {
                        if (Double.doubleToLongBits(this.center[i]) != Double
                                .doubleToLongBits(otherCenter[i])) {
                            return false;
                        }
                    }
                    return true;
                }
            }
            return false;
        }
	}
}
