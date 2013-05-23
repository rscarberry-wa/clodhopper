package org.battelle.clodhopper.task;

/**
 * Utility class which you can extend if you are
 * interested in only doing something in response
 * to a subset of task event types.  Usage scenarios
 * are similar to those of <code>MouseAdapter</code>, et cetera.
 * 
 * @author R. Scarberry
 * @since 1.0
 */
public class TaskAdapter implements TaskListener {

	@Override
	public void taskBegun(TaskEvent e) {
	}

	@Override
	public void taskMessage(TaskEvent e) {
	}

	@Override
	public void taskProgress(TaskEvent e) {
	}

	@Override
	public void taskPaused(TaskEvent e) {
	}
	
	@Override
	public void taskResumed(TaskEvent e) {
	}

	@Override
	public void taskEnded(TaskEvent e) {
	}

}
