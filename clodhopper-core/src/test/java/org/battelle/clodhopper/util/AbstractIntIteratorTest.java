/*
 * Copyright 2019 randy.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.battelle.clodhopper.util;

import java.util.OptionalInt;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author randy
 */
public abstract class AbstractIntIteratorTest {
    
    public abstract IntIterator createIteratorFrom(int[] values);
    
    /**
     * Test of gotoFirst method, of class ArrayIntIterator.
     */
    @Test
    public void testContiguousRangeOfValues() {
        
        final int first = 11;
        final int last = 23;
        final int sz = last - first + 1;
        final int[] values = new int[sz];
        for (int i=0; i<sz; i++) {
            values[i] = first + i;
        }
        
        IntIterator it = createIteratorFrom(values);
        assertTrue(it.hasNext());
        assertFalse(it.hasPrev());
        
        assertEquals(sz, it.getSize());
        
        int n = 0;
        while(it.hasNext()) {
            assertEquals(values[n++], it.getNext());
        }
        
        it.gotoFirst();
        assertFalse(it.hasPrev());
        assertEquals(first, it.getNext());
        
        it.gotoLast();
        assertFalse(it.hasNext());
        assertEquals(last, it.getPrev());
        
        it.gotoFirst();
        while(it.hasNext()) {
            int next = it.getNext();
            int prev = it.getPrev();
            assertEquals(prev, next);
            assertEquals(next, it.getNext());
        }
        
        it.gotoLast();
        while(it.hasPrev()) {
            int prev = it.getPrev();
            int next = it.getNext();
            assertEquals(prev, next);
            assertEquals(prev, it.getPrev());
        }
        
        assertArrayEquals(values, it.toArray());
        
        it.gotoFirst();
        for (int i=0; i<values.length; i++) {
            assertTrue(it.hasNext());
            assertEquals(values[i], it.getNext());
        }
        for (int i=values.length - 1; i>=0; i--) {
            assertTrue(it.hasPrev());
            assertEquals(values[i], it.getPrev());
        }
    }
    
    @Test
    public void testIteratorWithNoValues() {
        IntIterator it = createIteratorFrom(new int[0]);
        assertEquals(0, it.getSize());
        assertFalse(it.hasNext());
        assertFalse(it.hasPrev());
        assertFalse(it.getFirst().isPresent());
        assertFalse(it.getLast().isPresent());
        it.gotoFirst();
        assertFalse(it.hasNext());
        assertFalse(it.hasPrev());
        it.gotoLast();
        assertFalse(it.hasPrev());
        assertFalse(it.hasNext());
        assertTrue(it.toArray().length == 0);
    }
    
    @Test
    public void testIteratorWithOneElement() {
        final int value = 33;
        IntIterator it = createIteratorFrom(new int[] { value });
        assertEquals(1, it.getSize());
        assertTrue(it.hasNext());
        assertFalse(it.hasPrev());
        OptionalInt opt = it.getFirst();
        assertTrue(opt.isPresent());
        assertEquals(value, opt.getAsInt());
        opt = it.getLast();
        assertTrue(opt.isPresent());
        assertEquals(value, opt.getAsInt());
        
        it.gotoFirst();
        assertTrue(it.hasNext());
        assertEquals(value, it.getNext());
        assertFalse(it.hasNext());
        assertTrue(it.hasPrev());
        assertEquals(value, it.getPrev());
    }
}
