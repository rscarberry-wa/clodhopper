package org.battelle.clodhopper.task;

import java.util.EventObject;

/**
 * <p>Class which defines event objects propagated to <code>TaskListener</code>s
 * by executing <code>Task</code>s</p>
 *
 * @author R. Scarberry
 * @since 1.0
 */
public class TaskEvent extends EventObject {

    private static final long serialVersionUID = 2358453825277138394L;

    private String msg;

    /**
     * Constructor.
     * @param src Task - the source of the event.
     * @param msg String - a message, which can be null.
     */
    public TaskEvent(Task<?> src, String msg) {
        super(src);
        this.msg = msg;
    }

    /**
     * Get the Task which is the source of this event.
     * @return Task
     */
    public Task<?> getTask() {
        return (Task<?>) getSource();
    }

    /**
     * Get the message associated with this event.  This method never returns
     * null.  If no message was provided in the constructor, the empty
     * string is returned.
     * @return String
     */
    public String getMessage() {
        return msg != null ? msg : "";
    }

    /**
     * Get the progress for the Task associated with the event.
     * @return double
     */
    public double getProgress() {
        Task<?> task = getTask();
        return task != null ? task.getProgress() : 0.0;
    }
}
