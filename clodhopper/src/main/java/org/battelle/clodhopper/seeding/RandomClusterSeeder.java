package org.battelle.clodhopper.seeding;

public interface RandomClusterSeeder extends ClusterSeeder {

	long getRandomGeneratorSeed();
	
	void setRandomGeneratorSeed(long seed);
	
}
