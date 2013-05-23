package org.battelle.clodhopper.seeding;

import org.battelle.clodhopper.tuple.TupleList;

public class PreassignedSeeder implements ClusterSeeder {

	private TupleList seeds;
	
	public PreassignedSeeder(TupleList seeds) {
		if (seeds == null) {
			throw new NullPointerException();
		}
		this.seeds = seeds;
	}
	
	@Override
	public TupleList generateSeeds(TupleList tuples, int seedCount) {
		return seeds;
	}

}
