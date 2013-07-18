package org.battelle.clodhopper.util;

import java.util.NoSuchElementException;

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

public class ArrayIntIterator implements IntIterator {

	private int[] values;
	private int cursor;
	
	public ArrayIntIterator(int[] values) {
		if (values == null) {
			throw new NullPointerException();
		}
		this.values = values;
	}
	
	
	public void gotoFirst() {
		cursor = 0;
	}
	
	public void gotoLast() {
		cursor = values.length;
	}
	
	public int getFirst() {
		gotoFirst();
		return getNext();
	}
	
	public int getLast() {
		gotoLast();
		return getPrev();
	}
	
	public boolean hasNext() {
		return cursor < values.length;
	}
	
	public boolean hasPrev() {
		return cursor > 0;
	}

	public int getNext() {
		if (hasNext()) {
			return values[cursor++];
		}
		throw new NoSuchElementException();
	}
	
	public int getPrev() {
		if (hasPrev()) {
			return values[--cursor];
		}
		throw new NoSuchElementException();
	}

	public int getSize() {
		return values.length;
	}
	
	public int[] toArray() {
		return (int[]) values.clone();
	}
	
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
