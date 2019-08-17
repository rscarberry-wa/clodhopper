package org.battelle.clodhopper.util;

import java.util.OptionalInt;

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
 * IntIterator.java
 *
 *===================================================================*/

/**
 * An iterator over a collection of {@code int} primitives.*
 * @author  R. Scarberry
 */
public interface IntIterator extends Cloneable {

        /**
         * Position the cursor to the first element of the iteration.
         */
	void gotoFirst();
	
        /**
         * Position the cursor to the last element of the iteration.
         */
	void gotoLast();
	
        /**
         * Get the first value in the iteration, if present. A call to this
         * method does not effect the state of the iterator.
         * 
         * @return an optional wrapping the first value, empty if not present. 
         */
	OptionalInt getFirst();
	
        /**
         * Get the last value in the iteration, if present. A call to this
         * method does not effect the state of the iterator.
         * 
         * @return an optional wrapping the first value, empty if not present. 
         */
	OptionalInt getLast();

        /**
         * Returns true if another values remains in the iteration, false 
         * otherwise. Before calling {@code getNext()} ensure that this 
         * method returns true.
         * 
         * @return a boolean
         */
	boolean hasNext();
	
        /**
         * Returns true if it is safe to call {@code getPrev()}, false if not.
         * @return a boolean
         */
	boolean hasPrev();
	
        /**
         * Get the next value in the iteration if one is present.
         * @return the next value
         * @throws NoSuchElementException if another value is not present 
         *   in the forward iteration.
         */
	int getNext();
	
        /**
         * Get the previous value in the iteration if one is present.
         * @return the previous value
         * @throws NoSuchElementException if no previous value is present.
         */
	int getPrev();
	
        /**
         * Get the number of values in the iteration.
         * @return the number of values, always greater than or equal to zero.
         */
	int getSize();
	
        /**
         * Get the values in the iteration as an array.
         * @return the array of values, never null but possibly empty.
         */
	int[] toArray();
	
        /**
         * Get a clone of the iterator.
         * @return 
         */
	IntIterator clone();
	
}
