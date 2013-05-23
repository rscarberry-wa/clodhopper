package org.battelle.clodhopper.task;

/**
 * <p>Interface defining objects which listen for <code>TaskEvents</code>s
 * from running <code>Task</code> implementations</p>
 *
 * @author R. Scarberry
 * @since 1.0
 */
public interface TaskListener {
	
    /**
     * Sent to listeners when a <code>Task</code> has begun execution.
     * @param e
     */
    public void taskBegun(TaskEvent e);

    /**
     * Message events sent to listeners as a <code>Task</code> executes.
     * @param e
     */
    public void taskMessage(TaskEvent e);

    /**
     * Events sent to listeners by executing <code>Task</code>s to 
     * indicate progress.
     * @param e
     */
    public void taskProgress(TaskEvent e);
    
    /**
     * Events sent to listeners by executing <code>Task</code>s to 
     * indicate that it has been paused.
     * @param e
     */
    public void taskPaused(TaskEvent e);
    
    /**
     * Events sent to listeners by executing <code>Task</code>s to 
     * indicate that a paused task has resumed.
     * @param e
     */
    public void taskResumed(TaskEvent e);

    /**
     * Final event sent to listeners when a <code>Task</code> finishes execution.
     * @param e
     */
    public void taskEnded(TaskEvent e);

}
