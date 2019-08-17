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

import java.util.Arrays;
import java.util.BitSet;

/**
 *
 * @author randy
 */
public class BitSetIntIteratorTest extends AbstractIntIteratorTest {

    @Override
    public IntIterator createIteratorFrom(int[] values) {
        Arrays.sort(values);
        int max = values.length > 0 ? values[values.length - 1] : 1;
        BitSet bitSet = new BitSet(max);
        for (int i=0; i<values.length; i++) {
            bitSet.set(values[i]);
        }
        return new BitSetIntIterator(bitSet, true);
    }
    
}
