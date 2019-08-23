package org.battelle.clodhopper.task;

import java.util.Optional;
import java.util.concurrent.RunnableFuture;

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
 * Task.java
 *
 *===================================================================*/

/**
 * <p>The {@code Task } class is an interface defining objects 
 * that perform time-consuming chores while notifying listeners of their progress.  
 * Since it implements {@code java.lang.Runnable}, implementations are typically 
 * meant to be executed on dedicated threads, although they do not have to be.
 * However, {@code Task} provides status reporting
 * (messages and progress) and, since it extends {@code Future}, 
 * the ability to cancel before it is finished.</p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: Battelle Memorial Institute</p>
 *
 * @author R. Scarberry
 * @param <V> the result type returned by the <tt>get()</tt> method.
 * @since 1.0
 */
public interface Task<V> extends RunnableFuture<V> {

    /**
     * Add a listener to the receiver's list of {@code TaskListener}s.  
     * The listener is normally added before the thread executing the task is started.  As
     * the task executes, registered listeners receive event notifications
     * when the task starts, when it ends, and receive messages and progress 
     * in between.
     * @param listener - an object which implements {@code TaskListener}.
     */
    void addTaskListener(TaskListener listener);

    /**
     * Remove a registered listener.  Normally called after the task is
     * finished.
     * @param listener - a {@code TaskListener} previously added via
     * {@code addTaskListener(l)}.
     */
    void removeTaskListener(TaskListener listener);

    /**
     * Called after the task completes to get the outcome of the task.  Possible
     * return values are:
     * <ul>
     * <li>TaskOutcome.NOT_FINISHED - if the task has not been started or 
     *   has been started, but has not finished.
     * <li>TaskOutcome.CANCELLED - if the task was cancelled before it finished.
     * <li>TaskOutcome.ERROR - if the task was terminated by an error.
     * <li>TaskOutcome.SUCCESS - if the task finished successfully.
     * </ul>
     * @return TaskOutcome
     */
    TaskOutcome getTaskOutcome();
    
    /**
     * Returns true if the task has begun, false otherwise.
     * @return boolean
     */
    boolean isBegun();

    /**
     * Returns true if the task has finished, false otherwise.
     * @return boolean
     */
    boolean isEnded();

    /**
     * Returns the error message associated with an outcome of TaskOutcome.ERROR.
     * 
     * @return an {@code Optional} wrapping the error message if the task
     *   ended with an error. This will be empty if the task ended without an
     *   error or the task has not finished.
     */
    Optional<String> getErrorMessage();
    
    /**
     * Returns the {@code Throwable} associated with an outcome of 
     * TaskOutcome.ERROR. 
     * 
     * @return an instance of a {@code Throwable} wrapped by an
     *   {@code Optional}. The optional will be empty if the task has not
     *   finished or no error occurred.
     */
    Optional<Throwable> getError();

    /**
     * Get the current progress.
     * @return double
     */
    double getProgress();

    /**
     * Set the beginning and ending progress endpoints.  This method should be
     * called before starting the task.  If not called, the endpoints default
     * to 0.0 and 1.0.
     * @param begin - the beginning progress.
     * @param end - the ending progress.
     */
    void setProgressEndpoints(double begin, double end);        

    /**
     * Resets the task, so it can be repeated.  
     * @exception IllegalStateException if the task is currently running. 
     *   (isBegun() returns true and isEnded() returns false.)
     */
    void reset();

    /**
     * Implementations define this method, so the beginning and ending events
     * will carry a descriptive label.
     * @return String
     */
    String taskName();
    
    /**
     * Call to pause a running task.  Does nothing if the task
     * is not running or is already paused.
     */
    void pause();
    
    /**
     * Returns true if the task has been paused, false otherwise.
     * @return true if paused, false otherwise.
     */
    boolean isPaused();
    
    /**
     * Resume a task that has been paused.
     */
    void play();

}
