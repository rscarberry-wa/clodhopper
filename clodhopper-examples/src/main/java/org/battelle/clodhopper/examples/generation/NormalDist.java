package org.battelle.clodhopper.examples.generation;

import java.util.Random;

public class NormalDist {

	private Random random;
	private double mean;
	private double standardDev;
	
	private double cachedValue;
	private boolean hasCachedValue;
	
	public NormalDist(Random random, double mean, double standardDev) {
		if (random == null) throw new NullPointerException();
		if (standardDev < 0.0) {
			throw new IllegalArgumentException("standardDev < 0: " + standardDev);
		}
		this.random = random;
	}
	
	public double nextValue() {
		
		if (hasCachedValue) {
			hasCachedValue = false;
			return cachedValue;
		}
		
		double x,y,r,z;
		do {
			x = 2.0*nextDouble() - 1.0; 
			y = 2.0*nextDouble() - 1.0;		 
			r = x*x+y*y;
		} while (r >= 1.0);

		z = Math.sqrt(-2.0*Math.log(r)/r);
		cachedValue = mean + standardDev*x*z;
		hasCachedValue = true;
		
		return mean + standardDev*y*z;				
	}
	
	public double nextDouble() {
		while(true) {
			int nextInt = random.nextInt();
			if (nextInt != 0) {
				return (double) (nextInt & 0xFFFFFFFFL) * 2.3283064365386963E-10;
			}
		}
	}
}
