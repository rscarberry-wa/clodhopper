package org.battelle.clodhopper.random;

import java.util.Random;

import org.battelle.clodhopper.util.DataConversion;

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
 * MersenneTwisterRandom.java
 *
 *===================================================================*/

/**
 * Generates pseudo random numbers using the algorithm developed in 1997 by 
 * Makoto Matsumoto and Takuji Nishimura.  It provides very fast generation of
 * high-quality pseudorandom numbers.
 * 
 * @author R. Scarberry
 * @since 1.0.1
 *
 */
public class MersenneTwisterRandom extends Random {
	
	private static final long serialVersionUID = -1121202510384406275L;

	private static final int INDICES = 624;
	
	private final int[] mMT = new int[INDICES];
	private int mMTIndex;
	
	public MersenneTwisterRandom() {
		this(System.nanoTime());
	}
	
	public MersenneTwisterRandom(long seed) {
        super(seed);
	}
	
    @Override
	public synchronized void setSeed(long seed) {
		// Call the superclass' method, so the haveNextNextGaussian flag
		// is reset.
		super.setSeed(seed);
		if (mMT != null) {
			initMT(DataConversion.intsFromBytes(DataConversion.longToBytes(seed)));
		}
	}
	
	private void initMT(int[] seeds) {
		
		// First of all, seed with 19650218.
		mMT[0] = 19650218;
		for (int i=1; i<INDICES; i++) {
			mMT[i] = (1812433253 * (mMT[i-1] ^ (mMT[i-1] >> 30)) + i);
		}
		
		int i=1, j=0;
		for (int k=INDICES; k>0; k--) {
			mMT[i] = ((mMT[i] ^ (mMT[i-1] ^ mMT[i-1] >>> 30) * 1664525) + seeds[j] + j);
			i++; j++;
			if (i == INDICES) {
				mMT[0] = mMT[INDICES-1];
				i = 1;
			}
			if (j == seeds.length) {
				j = 0;
			}
		}
		
		for (int k=INDICES-1; k>0; k--) {
			mMT[i] = ((mMT[i] ^ (mMT[i-1] ^ mMT[i-1] >>> 30) * 1566083941) - i);
			i++;
			if (i == INDICES) {
				i = 1;
				mMT[0] = mMT[INDICES-1];
			}
		}
		
		mMT[0] = 0x80000000;
		mMTIndex = 0;
	}
	
    @Override
	protected int next(int bits) {
		
		int y;
		
		synchronized (this) {
			if (mMTIndex == 0) {
				generateNumbers();
			}
			y = mMT[mMTIndex++];
			if (mMTIndex == INDICES) {
				mMTIndex = 0;
			}
		}
		
		y ^= (y >>> 11);
		y ^= (y << 7 & 0x9d2c5680);
		y ^= (y << 15 & 0xefc60000);
		y ^= (y >>> 18);
				
		return (y >>> 32 - bits);
	}
	
	private void generateNumbers() {
		int y;
		int i;
		for (i=0; i<227; i++) {
			y = mMT[i] & 0x80000000 | mMT[i+1] & 0x7fffffff;
			mMT[i] = mMT[i+397] ^ y >>> 1;
			if ((y & 0x01) > 0) {
				mMT[i] ^= 0x9908b0df;
			}
		}
		int lim = INDICES - 1;
		for (; i<lim; i++) {
			y = mMT[i] & 0x80000000 | mMT[i+1] & 0x7fffffff;
			mMT[i] = mMT[i-227] ^ y >>> 1;
			if ((y & 0x01) > 0) {
				mMT[i] ^= 0x9908b0df;
			}
		}
		y = mMT[lim] & 0x80000000 | mMT[0] & 0x7fffffff;
		mMT[lim] = mMT[396] ^ y >>> 1;
		if ((y & 0x01) > 0) {
			mMT[lim] ^= 0x9908b0df;
		}
		mMTIndex = 0;
	}	
}
