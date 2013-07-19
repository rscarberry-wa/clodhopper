package org.battelle.clodhopper.seeding;

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
 * ClusterSeeder.java
 *
 *===================================================================*/

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
