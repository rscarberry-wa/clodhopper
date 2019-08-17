package org.battelle.clodhopper.util;

import java.util.NoSuchElementException;
import java.util.Objects;
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
 * ArrayIntIterator.java
 *
 *===================================================================*/

/**
 * An implementation of {@code IntIterator} backed by an array of int values.
 * @author R.Scarberry
 */
public class ArrayIntIterator implements IntIterator {

	private int[] values;
	private int cursor;
	
        /**
         * Constructor
         * @param values 
         */
	public ArrayIntIterator(int[] values) {
            Objects.requireNonNull(values, "values cannot be null");
            this.values = values;
	}
	
	@Override
	public void gotoFirst() {
            cursor = 0;
	}
	
        @Override
	public void gotoLast() {
            cursor = values.length;
	}
	
        @Override
	public OptionalInt getFirst() {
            return values.length > 0 ? 
                OptionalInt.of(values[0]) : OptionalInt.empty();
	}
	
        @Override
	public OptionalInt getLast() {
            return values.length > 0 ? 
                OptionalInt.of(values[values.length - 1]) : 
                OptionalInt.empty();
	}
	
        @Override
	public boolean hasNext() {
            return cursor < values.length;
	}
	
        @Override
	public boolean hasPrev() {
            return cursor > 0;
	}

        @Override
	public int getNext() {
            if (hasNext()) {
		return values[cursor++];
            }
            throw new NoSuchElementException();
	}
	
        @Override
	public int getPrev() {
            if (hasPrev()) {
		return values[--cursor];
            }
            throw new NoSuchElementException();
	}

        @Override
	public int getSize() {
            return values.length;
	}
	
        @Override
	public int[] toArray() {
            return (int[]) values.clone();
	}
	
        @Override
	public IntIterator clone() {
            try {
		ArrayIntIterator clone = (ArrayIntIterator) super.clone();
		clone.values = this.toArray();
		return clone;
            } catch (CloneNotSupportedException e) {
		throw new InternalError();
            }
	}
}
