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
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author R.Scarberry
 */
public class IntervalIntIteratorTest {
    
    /**
     * Test of getFirst method, of class IntervalIntIterator.
     */
    @Test
    public void testSimpleRange() {
        final int lower = 12;
        final int upper = 17;
        IntIterator it = new IntervalIntIterator(lower, upper);
        assertEquals(upper - lower + 1, it.getSize());
        assertTrue(it.hasNext());
        assertFalse(it.hasPrev());
        OptionalInt opt = it.getFirst();
        assertTrue(opt.isPresent());
        assertEquals(lower, opt.getAsInt());
        opt = it.getLast();
        assertTrue(opt.isPresent());
        assertEquals(upper, opt.getAsInt());
        for (int i=lower; i<=upper; i++) {
            assertTrue(it.hasNext());
            assertEquals(i, it.getNext());
        }
        for (int i=upper; i>=lower; i--) {
            assertTrue(it.hasPrev());
            assertEquals(i, it.getPrev());
        }
    }
}
