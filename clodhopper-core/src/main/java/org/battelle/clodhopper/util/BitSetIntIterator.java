package org.battelle.clodhopper.util;

import java.util.BitSet;
import java.util.NoSuchElementException;
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
 * BitSetIntIterator.java
 *
 *===================================================================*/

/**
 * <p>An implementation of {@code IntInterator} to iterate over the set or 
 * unset bits of a {@code java.util.BitSet}.  The {@code BitSet}
 * is in no way modified by the iterator.</p>
 * <p>Do not use an instance of {@code BitSetIntIterator} on a {@code BitSet}
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
        final int lim = bits.size();
        for (int i=0; i<lim; i++) {
            if (bits.get(i) == set) {
                first = i;
                break;
            }
        }
        last = first;
        for (int i=lim-1; i>first; i--) {
            if (bits.get(i) == set) {
                last = i;
                break;
            }
        }
        cursor = first;
    }
    
    /**
     * Constructor for iterating over the set bits.
     * 
     * @param bits - contains the bits to be iterated.
     */
    public BitSetIntIterator(BitSet bits) {
        this(bits, true);
    }

    @Override
    public OptionalInt getFirst() {
        return first >= 0 ? OptionalInt.of(first) : OptionalInt.empty();
    }

    @Override
    public OptionalInt getLast() {
        return last >= 0 ? OptionalInt.of(last) : OptionalInt.empty();
    }

    @Override
    public int getNext() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        int rtn = cursor;
        gotoNext();
        return rtn;
    }

    @Override
    public int getPrev() {
        if (!hasPrev()) {
            throw new NoSuchElementException();
        }
        gotoPrev();
        return cursor;
    }

    @Override
    public void gotoFirst() {
        cursor = first;
    }

    @Override
    public void gotoLast() {
        cursor = last >= 0 ? last + 1 : -1;
    }

    @Override
    public boolean hasNext() {
        return cursor >= 0 && cursor <= last;
    }

    @Override
    public boolean hasPrev() {
        return cursor >= 0 && cursor > first;
    }

    @Override
    public int getSize() {
        int sz = bits.cardinality();
        if (!set) {
            sz = bits.size() - sz;
        }
        return sz;
    }

    @Override
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

    @Override
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
            if (cursor == -1 && last >= 0) {
                cursor = last + 1;
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
        }
    }
}
