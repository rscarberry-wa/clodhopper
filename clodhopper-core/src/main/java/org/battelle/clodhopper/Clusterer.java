package org.battelle.clodhopper;

import java.util.List;

import org.battelle.clodhopper.task.Task;

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
 * Clusterer.java
 *
 *===================================================================*/

/**
 * Interface defining tasks that perform clustering operations.
 * 
 * @author R. Scarberry
 * @since 1.0
 *
 */
public interface Clusterer extends Task<List<Cluster>> {
	
	/**
	 * Convenience method for using in place of get().  This is just like get(),
	 * but does not throw exceptions.  If an error occurs, this method simply returns null.
	 * You should only call this method after the clusterer has finished with a successful outcome.
	 * 
	 * @return
	 */
	List<Cluster> getClusters();
	
}
