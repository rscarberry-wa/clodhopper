package org.battelle.clodhopper.task;

import java.util.ArrayList;
import java.util.List;

/**
 * <code>TaskEventSupport</code> is a utility class for managing <code>TaskListeners</code>
 * and for posting task life cycle events.
 * 
 * @author R. Scarberry
 * @since 1.0
 *
 */
public class TaskEventSupport {
	
	private List<TaskListener> listeners = new ArrayList<TaskListener> ();
	private TaskListener[] listenerArray;
	private Task<?> task;
	
	public TaskEventSupport(Task<?> task) {
		if (task == null) throw new NullPointerException();
		this.task = task;
	}

	public void addTaskListener (TaskListener l) {
		if (!listeners.contains(l)) {
			listeners.add(l);
			listenerArray = null;
		}
	}
	
	public void removeTaskListener (TaskListener l) {
		if (listeners.remove(l)) {
			listenerArray = null;
		}
	}
	
	public void fireTaskBegun() {
		TaskListener[] listeners = listeners();
		final int n = listeners.length;
		if (n > 0) {
			TaskEvent e = new TaskEvent(task, task.taskName() + " started");
			for (int i=0; i<n; i++) {
				listeners[i].taskBegun(e);
			}
		}
	}
	
	public void fireTaskMessage(String message) {
		TaskListener[] listeners = listeners();
		final int n = listeners.length;
		if (n > 0) {
			TaskEvent e = new TaskEvent(task, message);
			for (int i=0; i<n; i++) {
				listeners[i].taskMessage(e);
			}
		}
	}

	public void fireTaskProgress() {
		TaskListener[] listeners = listeners();
		final int n = listeners.length;
		if (n > 0) {
			TaskEvent e = new TaskEvent(task, "");
			for (int i=0; i<n; i++) {
				listeners[i].taskProgress(e);
			}
		}
	}

	public void fireTaskPaused() {
		TaskListener[] listeners = listeners();
		final int n = listeners.length;
		if (n > 0) {
			TaskEvent e = new TaskEvent(task, task.taskName() + " paused");
			for (int i=0; i<n; i++) {
				listeners[i].taskPaused(e);
			}
		}
	}

	public void fireTaskResumed() {
		TaskListener[] listeners = listeners();
		final int n = listeners.length;
		if (n > 0) {
			TaskEvent e = new TaskEvent(task, task.taskName() + " resumed");
			for (int i=0; i<n; i++) {
				listeners[i].taskResumed(e);
			}
		}
	}

	public void fireTaskEnded() {
		
		TaskListener[] listeners = listeners();
		
		final int n = listeners.length;
		
		if (n > 0) {

			TaskOutcome outcome = task.getTaskOutcome();
			
        	String msg = null;
        	if (outcome == TaskOutcome.SUCCESS) {
        		msg = task.taskName() + " has ended successfully";
        	} else if (outcome == TaskOutcome.CANCELLED) {
        		msg = task.taskName() + " has been canceled";
        	} else if (outcome == TaskOutcome.ERROR) {
        		msg = task.taskName() + " has ended with an error";
        	}

			TaskEvent e = new TaskEvent(task, msg);
			
			for (int i=0; i<n; i++) {
				listeners[i].taskEnded(e);
			}
		}
	}

	private TaskListener[] listeners() {
		if (listenerArray == null) {
			listenerArray = listeners.toArray(new TaskListener[listeners.size()]);
		}
		return listenerArray;
	}

}
