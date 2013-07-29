package org.battelle.clodhopper.random;

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
 * 
 * HighQualityRandom.java
 *
 *===================================================================*/

/**
 * Uses algorithm from <tt>Numerical Recipes: The Art of Scientific Computing</tt> to
 * give relatively fast, high quality random number generation.  
 * 
 * @author R. Scarberry (Found on the site http://www.javamex.com/ and adapted.)
 *
 * @since 1.0.1
 */
public class HighQualityRandom extends Random {

	private static final long serialVersionUID = 1L;

	private long mU;
	private long mV = 4101842887655102017L;
	private long mW = 1;

	public HighQualityRandom() {
		this(System.nanoTime());
	}

	public HighQualityRandom(long seed) {
		setSeed(seed);
	}

	public synchronized void setSeed(long seed) {
		mU = seed ^ mV;
		nextLong();
		mV = mU;
		nextLong();
		mW = mV;
		nextLong();
	}

	public synchronized long nextLong() {
		// LGC, like java.util.Random
		mU = mU * 2862933555777941757L + 7046029254386353087L;
		// xor shift
		mV ^= mV >>> 17;
		mV ^= mV << 31;
		mV ^= mV >>> 8;
		// Multiply-with-carry
		mW = 4294957665L * (mW & 0xffffffff) + (mW >>> 32);
		// xor shift
		long x = mU ^ (mU << 21);
		x ^= x >>> 35;
		x ^= x << 4;
		long ret = (x + mV) ^ mW;
		return ret;
	}

	protected int next(int bits) {
		return (int) (nextLong() >>> (64-bits));
	}
}
