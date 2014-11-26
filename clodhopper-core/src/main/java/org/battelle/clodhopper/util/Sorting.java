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
 * Sorting.java
 *
 *===================================================================*/
public final class Sorting {

    private Sorting() {
    }

    /**
     * Selects a pivot index with the index range [left, right] that contains
     * a likely median value using the supplied comparator for evaluation.
     * 
     * @param indices the array of integer values.
     * @param left the left boundary of the range, inclusive.
     * @param right the right boundary of the range, inclusive.
     * @param comp the comparator to use.
     * 
     * @return the pivot index.
     */
    public static int selectPivotIndex(final int[] indices, 
        int left, 
        int right, 
        final IntComparator comp) {

        final int len = right - left + 1;

        // Select a partition element index, m
        int midIndex = left + (len >> 1);

        // For arrays > 40, use the pseudomedian of 9
        //
        if (len > 40) {
            int eighthLen = len / 8;
            // Index of median value of the 3 on the left.
            left = medianOf3(indices, left, left + eighthLen, left + 2 * eighthLen, comp);
            // In the middle
            midIndex = medianOf3(indices, midIndex - eighthLen, midIndex, midIndex + eighthLen, comp);
            // On the right
            right = medianOf3(indices, right - 2 * eighthLen, right - eighthLen, right, comp);
        }

        // Convert to index of the median of the 3.
        return medianOf3(indices, left, midIndex, right, comp);
    }

    /**
     * Rearranges the contents of the specified array of indices so that values on the 
     * left of a pivot index are less than or equal to values on the right of a pivot index.
     * @param indices contains the values to rearrange.
     * @param k0 a value somewhere in the range [left0, right0], usually the midpoint.
     * @param left0 left boundary of the index range.
     * @param right0 right boundary of the index range.
     * @param comp comparator to use.
     * @return the pivot index of the array.
     */
    public static int partitionIndices(
            final int[] indices,
            final int k0,
            final int left0,
            final int right0,
            final IntComparator comp) {

        int k = k0;
        int left = left0;
        int right = right0;

        for (;;) {

            int idx = selectPivotIndex(indices, left, right, comp);
            int pivotIndex = partition(indices, left, right, idx, comp);

            if (left + k - 1 == pivotIndex) {
                int i = right0;
                while (i > pivotIndex) {
                    if (comp.compare(indices[i], indices[pivotIndex]) == 0) {
                        pivotIndex++;
                        if (i > pivotIndex) {
                            swap(indices, i, pivotIndex);
                        }
                    } else {
                        i--;
                    }
                }
                return pivotIndex;
            }

            if (left + k - 1 < pivotIndex) {
                right = pivotIndex - 1;
            } else {
                k -= (pivotIndex - left + 1);
                left = pivotIndex + 1;
            }
        }
    }

    private static int partition(final int[] indices, 
        final int left, 
        final int right, 
        final int pivotIndex, 
        final IntComparator comp) {
        
        int pivot = indices[pivotIndex];
        swap(indices, right, pivotIndex);

        int store = left;
        for (int i = left; i < right; i++) {
            if (comp.compare(indices[i], pivot) <= 0) {
                swap(indices, i, store);
                store++;
            }
        }

        swap(indices, right, store);
        return store;
    }

    /**
     * Performs a quick sort on the specified object.
     * @param sortable an instance of <code>IndexedSortable</code>.
     */
    public static void quickSort(final IndexedSortable sortable) {
        quickSort(sortable, 0, sortable.getLength());
    }

    /**
     * Performs a quick sort of the specified object at the range defined by the
     * two arguments.
     * @param sortable an instance of <code>IndexedSortable</code>.
     * @param offset an offset into the sortable object.
     * @param len the number of items to sort.
     */
    public static void quickSort(final IndexedSortable sortable, final int offset, final int len) {

        final int limit = offset + len;

        // Use an insertion sort for really small arrays.
        if (len < 7) {
            for (int i = offset + 1; i < limit; i++) {
                sortable.markValue(i);
                int j = i;
                while (j > 0 && sortable.compareToMarkedValue(j - 1) > 0) {
                    sortable.transferValue(j - 1, j);
                    j--;
                }
                sortable.setToMarkedValue(j);
            }
            // Done!
            return;
        }

        // Select a partition element index, m
        int m = offset + (len >> 1);

        int left = offset;
        int right = limit - 1;

        // For arrays > 40, use the pseudomedian of 9
        //
        if (len > 40) {
            int eighthLen = len / 8;
            // Index of median value of the 3 on the left.
            left = medianOf3(sortable, left, left + eighthLen, left + 2 * eighthLen);
            // In the middle
            m = medianOf3(sortable, m - eighthLen, m, m + eighthLen);
            // On the right
            right = medianOf3(sortable, right - 2 * eighthLen, right - eighthLen, right);
        }

        // Convert to index of the median of the 3.
        m = medianOf3(sortable, left, m, right);

        sortable.markValue(m);

        int a = offset;
        int b = offset;
        int c = limit - 1;
        int d = c;

        while (true) {

            int cmpValue = b <= c ? sortable.compareToMarkedValue(b) : 0;

            while (b <= c && cmpValue <= 0) {
                if (cmpValue == 0) {
                    sortable.swap(a++, b);
                }
                b++;
                if (b <= c) {
                    cmpValue = sortable.compareToMarkedValue(b);
                }
            }

            cmpValue = c >= b ? sortable.compareToMarkedValue(c) : 0;

            while (c >= b && cmpValue >= 0) {
                if (cmpValue == 0) {
                    sortable.swap(c, d--);
                }
                c--;
                if (c >= b) {
                    cmpValue = sortable.compareToMarkedValue(c);
                }
            }

            if (b > c) {
                break;
            }

            sortable.swap(b++, c--);
        }

        int s = Math.min(a - offset, b - a);
        vecSwap(sortable, offset, b - 1, s);

        s = Math.min(d - c, limit - d - 1);
        vecSwap(sortable, b, limit - s, s);

        s = b - a;
        if (s > 1) {
            quickSort(sortable, offset, s);
        }

        s = d - c;
        if (s > 1) {
            quickSort(sortable, limit - s, s);
        }
    }

    /**
     * Performs a quick sort of the values of an array using the supplied comparator.
     * @param arr the array of values to sort.
     * @param comp the comparator.
     */
    public static void quickSort(final int[] arr, final IntComparator comp) {
        quickSort(arr, 0, arr.length, comp);
    }

    /**
     * Performs a quick sort of a section of an array of values using the supplied comparator.
     * @param arr the array of values to sort.
     * @param offset the offset into the array defining the start of the section.
     * @param len the number of items in the section.
     * @param comp the comparator.
     */
    public static void quickSort(final int[] arr, final int offset, final int len, final IntComparator comp) {
        final int limit = offset + len;

        // Use an insertion sort for really small arrays.
        if (len < 7) {
            for (int i = offset + 1; i < limit; i++) {
                int v = arr[i];
                int j = i;
                while (j > 0 && comp.compare(arr[j - 1], v) > 0) {
                    arr[j] = arr[j - 1];
                    j--;
                }
                arr[j] = v;
            }
            // Done!
            return;
        }

        // Select a partition element index, m
        int m = offset + (len >> 1);

        int left = offset;
        int right = limit - 1;

        // For arrays > 40, use the pseudomedian of 9
        //
        if (len > 40) {
            int eighthLen = len / 8;
            // Index of median value of the 3 on the left.
            left = medianOf3(arr, left, left + eighthLen, left + 2 * eighthLen, comp);
            // In the middle
            m = medianOf3(arr, m - eighthLen, m, m + eighthLen, comp);
            // On the right
            right = medianOf3(arr, right - 2 * eighthLen, right - eighthLen, right, comp);
        }

        // Convert to index of the median of the 3.
        m = medianOf3(arr, left, m, right, comp);

        int partitionValue = arr[m];

        int a = offset;
        int b = offset;
        int c = limit - 1;
        int d = c;

        while (true) {

            int cmpValue = b <= c ? comp.compare(arr[b], partitionValue) : 0;

            while (b <= c && cmpValue <= 0) {
                if (cmpValue == 0) {
                    swap(arr, a++, b);
                }
                b++;
                if (b <= c) {
                    cmpValue = comp.compare(arr[b], partitionValue);
                }
            }

            cmpValue = c >= b ? comp.compare(arr[c], partitionValue) : 0;

            while (c >= b && cmpValue >= 0) {
                if (cmpValue == 0) {
                    swap(arr, c, d--);
                }
                c--;
                if (c >= b) {
                    cmpValue = comp.compare(arr[c], partitionValue);
                }
            }

            if (b > c) {
                break;
            }

            swap(arr, b++, c--);
        }

        int s = Math.min(a - offset, b - a);
        vecSwap(arr, offset, b - s, s);

        s = Math.min(d - c, limit - d - 1);
        vecSwap(arr, b, limit - s, s);

        s = b - a;
        if (s > 1) {
            quickSort(arr, offset, s, comp);
        }

        s = d - c;
        if (s > 1) {
            quickSort(arr, limit - s, s, comp);
        }
    }

    private static void swap(int[] arr, int i, int j) {
        if (i != j) {
            arr[i] ^= arr[j];
            arr[j] ^= arr[i];
            arr[i] ^= arr[j];
        }
    }

    private static void vecSwap(int[] arr, int offset1, int offset2, int len) {
        for (int i = 0; i < len; i++, offset1++, offset2++) {
            swap(arr, offset1, offset2);
        }
    }

    private static void vecSwap(IndexedSortable sortable, int offset1, int offset2, int len) {
        for (int i = 0; i < len; i++, offset1++, offset2++) {
            sortable.swap(offset1, offset2);
        }
    }

    /**
     * Given three indexes into the specified array, this method returns the
     * index of the middle element as determined by the supplied comparator. If
     * the comparator is null, the middle index is determined by the
     * <tt>compareTo</tt> methods of the objects, assuming they implement
     * comparable.
     *
     * @param a the array of values.
     * @param x first median candidate.
     * @param y second median candidate.
     * @param z third median candidate.
     * @param c the comparator to use.
     * @return one of the three median candidates.
     */
    public static int medianOf3(final int[] a, final int x, final int y, final int z, final IntComparator c) {
        if (c.compare(a[x], a[y]) < 0) {
            // x < y
            if (c.compare(a[y], a[z]) < 0) {
                // y < z, so x < y < z
                // y is the middle
                return y;
            }
            // x < y, but y >= z
            if (c.compare(a[x], a[z]) < 0) {
                // x < z
                // x < z <= y
                return z;
            }
            // x >= z
            // Has to be x.
            return x;
        }
        // x >= y
        if (c.compare(a[y], a[z]) > 0) {
            // y > z
            return y;
        }
        // y <= z
        if (c.compare(a[x], a[z]) > 0) {
            // x > z
            return z;
        }
        return x;
    }

    /**
     * Given three indexes into the specified object, this method returns the
     * index of the middle element. 
     *
     * @param sortable an <code>IndexedSortable</code>.
     * @param x first median candidate.
     * @param y second median candidate.
     * @param z third median candidate.
     * @return one of the three median candidates.
     */
    public static int medianOf3(IndexedSortable sortable, int x, int y, int z) {
        if (sortable.compare(x, y) < 0) {
            // x < y
            if (sortable.compare(y, z) < 0) {
                // y < z, so x < y < z
                // y is the middle
                return y;
            }
            // x < y, but y >= z
            if (sortable.compare(x, z) < 0) {
                // x < z
                // x < z <= y
                return z;
            }
            // x >= z
            // Has to be x.
            return x;
        }
        // x >= y
        if (sortable.compare(y, z) > 1) {
            // y > z
            return y;
        }
        // y <= z
        if (sortable.compare(x, z) > 0) {
            // x > z
            return z;
        }
        return x;
    }

    /**
     * Sorts the first array of values while similarly rearranging the second array of values.
     * 
     * @param dValues the values to sort.
     * @param iValues a parallel array that is rearranged with the first array to maintain 
     *     one-to-one correspondence.
     */
    public static void parallelSort(double[] dValues, int[] iValues) {
        quickSort(new DoubleIntIndexedSortable(dValues, iValues));
    }

    /**
     * Implementation of <code>IndexedSortable</code> that wraps an array of 
     * doubles to be sorted and another array of integers to be kept in one-to-one
     * correspondence with the doubles.
     */
    public static class DoubleIntIndexedSortable implements IndexedSortable {

        private final double[] dValues;
        private final int[] iValues;

        private double markedDValue;
        private int markedIValue;
        private int markedIndex = -1;

        /**
         * Constructor
         * @param dValues an array of double values.
         * @param iValues an array of int values.
         * 
         * @throws IllegalArgumentException if the arrays do not have the same length.
         */
        public DoubleIntIndexedSortable(final double[] dValues, final int[] iValues) {
            if (dValues.length != iValues.length) {
                throw new IllegalArgumentException("length mismatch");
            }
            this.dValues = dValues;
            this.iValues = iValues;
        }

        /**
         * {@inheritDoc }
         */
        @Override
        public int getLength() {
            return dValues.length;
        }

        /**
         * {@inheritDoc }
         */
        @Override
        public int compare(final int n1, final int n2) {
            double v1 = dValues[n1];
            double v2 = dValues[n2];
            return v1 < v2 ? -1 : v1 > v2 ? 1 : 0;
        }

        /**
         * {@inheritDoc }
         */
        @Override
        public int compareToMarkedValue(final int n) {
            if (markedIndex == -1) {
                throw new IllegalStateException("no value has been marked");
            }
            double v = dValues[n];
            return v < markedDValue ? -1 : v > markedDValue ? 1 : 0;
        }

        /**
         * {@inheritDoc }
         */
        @Override
        public void swap(final int n1, final int n2) {
            if (n1 != n2) {
                double dTmp = dValues[n1];
                int iTmp = iValues[n1];
                dValues[n1] = dValues[n2];
                iValues[n1] = iValues[n2];
                dValues[n2] = dTmp;
                iValues[n2] = iTmp;
            }
        }

        /**
         * {@inheritDoc }
         */
        @Override
        public void transferValue(final int nSrc, final int nDst) {
            dValues[nDst] = dValues[nSrc];
            iValues[nDst] = iValues[nSrc];
        }

        /**
         * {@inheritDoc }
         */
        @Override
        public void markValue(final int n) {
            markedDValue = dValues[n];
            markedIValue = iValues[n];
            markedIndex = n;
        }

        /**
         * {@inheritDoc }
         */
        @Override
        public void setToMarkedValue(final int n) {
            if (markedIndex == -1) {
                throw new IllegalStateException("no value has been marked");
            }
            dValues[n] = markedDValue;
            iValues[n] = markedIValue;
        }
    }
}
