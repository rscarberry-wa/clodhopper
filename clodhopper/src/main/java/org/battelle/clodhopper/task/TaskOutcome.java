package org.battelle.clodhopper.task;

/**
 * Simple enumeration used to indicate the outcome of a
 * <code>Task</code>.  The values are interpreted as follows:
 * <p>
 * <ul>
 * <li>NOT_FINISHED - the <code>Task</code> has not begun or has not finished.
 * <li>CANCELLED    - the <code>Task</code> was cancelled before it finished.
 * <li>ERROR        - the <code>Task</code> encountered a fatal error.
 * <li>SUCCESS      - the <code>Task</code> finished successfully.
 * </ul>
 * 
 * @author R. Scarberry
 * @since 1.0
 *
 */
public enum TaskOutcome {
    NOT_FINISHED,
    CANCELLED,
    ERROR,
    SUCCESS
}
