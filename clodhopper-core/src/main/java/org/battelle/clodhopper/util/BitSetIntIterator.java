package org.battelle.clodhopper.util;

import java.util.*;

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
 * BitSetIntIterator.java
 *
 *===================================================================*/

/**
 * <p>An implementation of <code>IntInterator</code> to iterate over the set or 
 * unset bits of a <code>cern.colt.bitvector.BitVector</code>.  The <code>BitVector</code>
 * is in no way modified by the iterator.</p>
 * <p>Do not use an instance of <code>BitSetIntIterator</code> on a <code>BitVector</code>
 * that is being modified by other threads.  The iterator assumes the bits are static.</p>
 * 
 * @author R. Scarberry
 *
 */
public class BitSetIntIterator implements IntIterator {
   
    // The bit vector
    private BitSet bits;
    // Whether iterating over the set or the unset bits.
    private boolean set;
    // The position of the cursor, the first set bit, and the last set bit.
    private int cursor = -1, first = -1, last = -1;
    // Set to true by gotoLast().  
    private boolean lastFlag;
    
    /**
     * Constructor.
     * 
     * @param bits - contains the bits to be iterated.
     * @param set - iterates over the set bit if true, over the unset bits if false.
     */
    public BitSetIntIterator(BitSet bits, boolean set) {
        if (bits == null) throw new NullPointerException();
        this.bits = bits;
        this.set = set;
        gotoFirst();
    }
    
    /**
     * Constructor for iterating over the set bits.
     * 
     * @param bits - contains the bits to be iterated.
     */
    public BitSetIntIterator(BitSet bits) {
        this(bits, true);
    }

    /**
     * Returns the index of the first bit matching the constructor
     * argument <code>set</code>.  
     *
     * @return - a nonnegative index or -1 if no matching bits are present. 
     */
    public int getFirst() {
        gotoFirst();
        return getNext();
    }

    /**
     * Returns the index of the last bit matching the constructor
     * argument <code>set</code>.
     *
     * @return - a nonnegative index or -1 if no matching bits are present. 
     */
    public int getLast() {
        gotoLast();
        return getPrev();
    }

    /**
     * Returns the index of the next bit matching the constructor
     * argument <code>set</code>.  Call <code>hasNext()</code> prior to calling
     * this method to confirm that another matching bit is present.
     *
     * @return - a nonnegative index or -1 if no matching bits are present. 
     */
    public int getNext() {
        int rtn = cursor;
        gotoNext();
        if (!hasNext()) {
            gotoLast();
        }
        return rtn;
    }

    /**
     * Returns the index of the previous bit matching the constructor
     * argument <code>set</code>.  Call <code>hasPrev()</code> prior to calling
     * this method to confirm that another matching bit is present.
     *
     * @return - a nonnegative index or -1 if no matching bits are present. 
     */
    public int getPrev() {
        gotoPrev();
        lastFlag = false;
        return cursor;
    }

    /**
     * Position the cursor for iterating forward starting from the first bit
     * that matches the constructor argument <code>set</code>
     */
    public void gotoFirst() {
        if (first == -1) {
           int lim = bits.size();
           for (int i=0; i<lim; i++) {
               if (bits.get(i) == set) {
                   first = i;
                   break;
               }
           }
        }
        cursor = first;
    }

    /**
     * Position the cursor for iterating backwards starting from the last bit
     * that matches the constructor argument <code>set</code>
     */
    public void gotoLast() {
        cursor = -1;
        if (last == -1) {
            int lim = bits.size();
            for (int i=lim-1; i>=0; i--) {
                if (bits.get(i) == set) {
                    last = i;
                    break;
                }
            }
         }
         lastFlag = (last >= 0);
    }

    /**
     * Is there another bit matching the constructor argument <code>set</code>
     * in the forward direction?  That is, will an immediate call to 
     * <code>getNext()</code> return a nonnegative index?
     */
    public boolean hasNext() {
        return cursor >= 0;
    }

    /**
     * Is there another bit matching the constructor argument <code>set</code>
     * in the backward direction?  That is, will an immediate call to 
     * <code>getPrev()</code> return a nonnegative index?
     */
    public boolean hasPrev() {
        int csr = cursor;
        gotoPrev();
        boolean rtn = cursor >= 0;
        cursor = csr;
        return rtn;
    }

    /** 
     * Returns the number of bits matching the constructor argument <code>set</code>.
     */
    public int getSize() {
        int sz = bits.cardinality();
        if (!set) {
            sz = bits.size() - sz;
        }
        return sz;
    }

    
    /** 
     * Returns the indexes of the bits matching the constructor argument <code>set</code>.
     * This array will always be ascendingly sorted.
     */
    public int[] toArray() {
        int sz = getSize();
        int[] rtn = new int[sz];
        int count = 0;
        int lim = bits.size();
        for (int i=0; i<lim; i++) {
            if (bits.get(i) == set) {
                rtn[count++] = i;
            }
        }
        return rtn;
    }

    /**
     * Returns a deep clone of the receiving object.
     */
    public IntIterator clone() {
        BitSetIntIterator clone = null; 
        try {
            clone = (BitSetIntIterator) super.clone();
            clone.bits = (BitSet) this.bits.clone();
        } catch (CloneNotSupportedException cnse) {
            // Won't happen.
        }
        return clone;
    }

    // Moves the cursor forward.
    private void gotoNext() {
        if (cursor >= 0) {
            int start = cursor + 1;
            cursor = -1;
            int lim = bits.size();
            for (int i=start; i<lim; i++) {
                if (bits.get(i) == set) {
                    cursor = i;
                    break;
                }
            }
        }
    }
    
    // Moves the cursor backwards.
    private void gotoPrev() {
        if (cursor >= 0) {
            int start = cursor - 1;
            cursor = -1;
            for (int i=start; i>=0; i--) {
                if (bits.get(i) == set) {
                    cursor = i;
                    break;
                }
            }
        } else if (lastFlag) {
            cursor = last;
        }
    }

}
