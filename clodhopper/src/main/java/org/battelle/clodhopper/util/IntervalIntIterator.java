package org.battelle.clodhopper.util;

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
 * IntervalIntIterator.java
 *
 *===================================================================*/

/**
 * <p>Implementation of <tt>IntIterator</tt> for iterating over
 * a continuous range of integer primitives.</p>
 * 
 * @author R. Scarberry
 *
 */
public class IntervalIntIterator implements IntIterator {

    private int lower, upper, cursor;
    
    /**
     * Constructor which takes the upper and lower bounds,
     * inclusive of the lower bound, exclusive of the upper.
     * 
     * @param lower - the first element of the iteration.
     * @param upper - the last element of the iteration plus 1.
     */
    public IntervalIntIterator (int lower, int upper) {
        this.lower = Math.min(lower, upper);
        this.upper = Math.max(lower, upper) + 1;
        this.cursor = this.lower;
    }

    public int getFirst() {
        gotoFirst();
        return getNext();
    }

    public int getLast() {
        gotoLast();
        return getPrev();
    }

    public int getNext() {
        return this.cursor++;
    }

    public int getPrev() {
        return --this.cursor;
    }

    public void gotoFirst() {
        this.cursor = this.lower;
    }

    public void gotoLast() {
        this.cursor = this.upper;
    }

    public boolean hasNext() {
        return this.cursor >= this.lower && this.cursor < this.upper;
    }

    public boolean hasPrev() {
        return this.cursor > this.lower && this.cursor <= this.upper;
    }

    public int getSize() {
        return this.upper - this.lower;
    }

    public int[] toArray() {
        int sz = getSize();
        int[] rtn = new int[sz];
        for (int i=0; i<sz; i++) {
            rtn[i] = this.lower + i;
        }
        return rtn;
    }

    public IntIterator clone() {
        IntIterator clone = null;
        try {
            clone = (IntIterator) super.clone();
        } catch (CloneNotSupportedException cnse) {
        	throw new InternalError();
        }
        return clone;
    }
    
}
