package org.battelle.clodhopper;

import java.util.List;

import org.battelle.clodhopper.task.Task;

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
