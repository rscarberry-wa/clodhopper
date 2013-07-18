package org.battelle.clodhopper.task;

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
 * TaskAdapter.java
 *
 *===================================================================*/

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
