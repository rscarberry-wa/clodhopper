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
 * TaskOutcome.java
 *
 *===================================================================*/

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
