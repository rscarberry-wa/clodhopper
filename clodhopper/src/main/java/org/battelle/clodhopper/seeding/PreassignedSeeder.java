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
 * PreassignedSeeder.java
 *
 *===================================================================*/

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
