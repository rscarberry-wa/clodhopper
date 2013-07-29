package org.battelle.clodhopper.random;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

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
 * XORShiftRandom.java
 *
 *===================================================================*/

/**
 * <p>
 * A medium quality random number generator that uses the XOR shift
 * algorithm to quickly generate random numbers.
 * </p>
 * 
 * @author R. Scarberry
 * @since 1.0.1
 *
 */
public class XORShiftRandom extends Random {

	private static final long serialVersionUID = 1L;

	private final AtomicLong mSeed;
	
	public XORShiftRandom() {
		this(System.nanoTime());
	}
	
	public XORShiftRandom(long seed) {
		// super() is implicitly called here... Which
		// calls setSeed() before mSeed is initialized.
		mSeed = new AtomicLong(0L);
		setSeed(seed);
	}
	
	public synchronized void setSeed(long seed) {
		// Since mSeed is not initialized when this
		// is first called by the superclass' default constructor,
		// need this null check.
		if (this.mSeed != null) this.mSeed.set(seed);
		// Call the superclass' method, so the haveNextNextGaussian flag
		// is reset.
		super.setSeed(seed);
	}
	
    protected int next(int bits) {
        long oldseed, nextseed;
        AtomicLong seed = this.mSeed;
        do {
        	nextseed = oldseed = seed.get();
        	nextseed ^= (nextseed << 21);
        	nextseed ^= (nextseed >>> 35);
        	nextseed ^= (nextseed << 4);
        } while (!seed.compareAndSet(oldseed, nextseed));
        return (int)(nextseed & ((1L << bits) - 1));
	}
    
}
