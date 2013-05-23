package org.battelle.clodhopper.seeding;

import org.battelle.clodhopper.tuple.TupleList;

/**
 * A <code>ClusterSeeder</code> generates the initial cluster seeds for algorithms that
 * need them, such as k-means.
 * 
 * @author R. Scarberry
 * @since 1.0
 *
 */
public interface ClusterSeeder {

	/**
	 * Given a collection of tuple data, generate seeds. The seeds generated are not
	 * required to belong to the passed in list of tuples.  Also, the actual number of seeds
	 * generated may not always equal the number requested.
	 * 
	 * @param tuples contains the data to generate seeds for.
	 * @param seedCount the requested number of seeds.
	 * 
	 * @return the seeds, packaged as a <code>TupleList</code>
	 */
	TupleList generateSeeds(TupleList tuples, int seedCount);

}
