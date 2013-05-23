package org.battelle.clodhopper.task;

/**
 * Interface which objects implement to indicate that their processes can be canceled.
 * 
 * @author R. Scarberry
 * @since 1.0
 *
 */
public interface Cancelable {

	boolean cancel(boolean force);
	
	boolean isCanceled();
	
}
