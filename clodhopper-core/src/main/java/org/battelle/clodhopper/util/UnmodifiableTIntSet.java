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

import gnu.trove.TIntCollection;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.set.TIntSet;
import java.util.Collection;
import java.util.Objects;

/**
 * {@code UnmodifiableTIntSet} is a wrapper class that renders the wrapped
 * {@code gnu.trove.TIntSet} unmodifiable. All methods that normally alter the
 * set throw {@code UnsupportedOperationException}.
 * 
 * @author R.Scarberry
 */
public class UnmodifiableTIntSet implements TIntSet {

    private final TIntSet innerSet;
    
    public UnmodifiableTIntSet(TIntSet innerSet) {
        Objects.requireNonNull(innerSet, "innerSet is required");
        this.innerSet = innerSet;
    }
    
    @Override
    public int getNoEntryValue() {
        return innerSet.getNoEntryValue();
    }

    @Override
    public int size() {
        return innerSet.size();
    }

    @Override
    public boolean isEmpty() {
        return innerSet.isEmpty();
    }

    @Override
    public boolean contains(int i) {
        return innerSet.contains(i);
    }

    @Override
    public TIntIterator iterator() {
        return new UnmodifiableTIntIterator(innerSet.iterator());
    }

    @Override
    public int[] toArray() {
        return innerSet.toArray();
    }

    @Override
    public int[] toArray(int[] ints) {
        return innerSet.toArray(ints);
    }

    @Override
    public boolean add(int i) {
        throw new UnsupportedOperationException("add not supported");
    }

    @Override
    public boolean remove(int i) {
        throw new UnsupportedOperationException("remove not supported");
    }

    @Override
    public boolean containsAll(Collection<?> clctn) {
        return innerSet.containsAll(clctn);
    }

    @Override
    public boolean containsAll(TIntCollection tic) {
        return innerSet.containsAll(tic);
    }

    @Override
    public boolean containsAll(int[] ints) {
        return innerSet.containsAll(ints);
    }

    @Override
    public boolean addAll(Collection<? extends Integer> clctn) {
        throw new UnsupportedOperationException("addAll not supported");
    }

    @Override
    public boolean addAll(TIntCollection tic) {
        throw new UnsupportedOperationException("addAll not supported");
    }

    @Override
    public boolean addAll(int[] ints) {
        throw new UnsupportedOperationException("addAll not supported");
    }

    @Override
    public boolean retainAll(Collection<?> clctn) {
        throw new UnsupportedOperationException("retainAll not supported");
    }

    @Override
    public boolean retainAll(TIntCollection tic) {
        throw new UnsupportedOperationException("retainAll not supported");
    }

    @Override
    public boolean retainAll(int[] ints) {
        throw new UnsupportedOperationException("retainAll not supported");
    }

    @Override
    public boolean removeAll(Collection<?> clctn) {
        throw new UnsupportedOperationException("removeAll not supported");
    }

    @Override
    public boolean removeAll(TIntCollection tic) {
        throw new UnsupportedOperationException("removeAll not supported");
    }

    @Override
    public boolean removeAll(int[] ints) {
        throw new UnsupportedOperationException("removeAll not supported");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("clear not supported");
    }

    @Override
    public boolean forEach(TIntProcedure tip) {
        return innerSet.forEach(tip);
    }
    
    private static class UnmodifiableTIntIterator implements TIntIterator {

        private final TIntIterator innerIterator;
        
        private UnmodifiableTIntIterator(TIntIterator innerIterator) {
            this.innerIterator = innerIterator;
        }
        
        @Override
        public int next() {
            return innerIterator.next();
        }

        @Override
        public boolean hasNext() {
            return innerIterator.hasNext();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove is not supported");
        }
        
    }
}
