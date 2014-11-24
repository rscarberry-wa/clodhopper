package org.battelle.clodhopper.tuple;

import java.io.*;
import java.util.Arrays;
import java.util.Random;

import org.battelle.clodhopper.util.IntComparator;
import org.battelle.clodhopper.util.IntIterator;
import org.battelle.clodhopper.util.Sorting;

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
 * TupleMath.java
 *
 *===================================================================*/
public final class TupleMath {

    /**
     * Square root of (2 * pi).
     */
    static public final double SQRT2PI = Math.sqrt(2 * Math.PI);

    private TupleMath() {
    }

    public static double[] minCorner(TupleList tuples, IntIterator ids) {

        final int len = tuples.getTupleLength();

        double[] result = new double[len];
        Arrays.fill(result, Double.NaN);

        double[] buffer = new double[len];

        ids.gotoFirst();

        while (ids.hasNext()) {
            int id = ids.getNext();
            tuples.getTuple(id, buffer);
            for (int i = 0; i < len; i++) {
                double d = buffer[i];
                if (!Double.isNaN(d)) {
                    double v = result[i];
                    if (Double.isNaN(v) || (d < v)) {
                        result[i] = d;
                    }
                }
            }
        }

        return result;
    }

    public static double[] maxCorner(TupleList tuples, IntIterator ids) {

        final int len = tuples.getTupleLength();

        double[] result = new double[len];
        Arrays.fill(result, Double.NaN);

        double[] buffer = new double[len];

        while (ids.hasNext()) {
            int id = ids.getNext();
            tuples.getTuple(id, buffer);
            for (int i = 0; i < len; i++) {
                double d = buffer[i];
                if (!Double.isNaN(d)) {
                    double v = result[i];
                    if (Double.isNaN(v) || (d > v)) {
                        result[i] = d;
                    }
                }
            }
        }

        return result;
    }

    public static double[] average(TupleList tuples, IntIterator ids) {
        final int len = tuples.getTupleLength();
        double[] result = new double[len];
        double[] buffer = new double[len];
        int count = 0;
        ids.gotoFirst();
        while (ids.hasNext()) {
            tuples.getTuple(ids.getNext(), buffer);
            addTo(result, buffer);
            count++;
        }
        if (count > 0) {
            divideBy(result, count);
        }
        return result;
    }

    public static double median(TupleList tuples, int column, IntIterator ids) {
        int[] idsArray = ids.toArray();
        final int len = idsArray.length;
        double[] values = new double[len];
        for (int i = 0; i < len; i++) {
            values[i] = tuples.getTupleValue(idsArray[i], column);
        }
        return median(values);
    }

    /**
     * Computes the median from a number of values contained in an array. NaNs
     * are not included in the calculation.
     *
     * @param values - the values for which to compute the median.
     *
     * @return the median value.
     */
    public static double median(double[] values) {
        return median(values, false);
    }

    /**
     * Compute the median from a number of values contained in an array. If NaNs
     * are present in the array, they are not included in the calculation.
     *
     * @param values - an array containing the values.
     * @param canRearrangeValues - if true, the previous array is rearranged by
     * sorting. If false, the array of values is not altered, but the method is
     * slightly less efficient.
     * @return - the median value, or NaN if the array is null, of length 0, or
     * contains no non-NaN elements.
     */
    public static double median(double[] values, boolean canRearrangeValues) {
        double median = Double.NaN;
        if (values != null) {
            double[] copy = null;
            if (canRearrangeValues) {
                copy = values; // Don't really copy, since the values can be rearranged.
            } else {
                copy = new double[values.length];
                System.arraycopy(values, 0, copy, 0, values.length);
            }
			// Sort the values.  If there are any NaNs, they'll end up
            // at the end of the array.
            java.util.Arrays.sort(copy);
            int n = copy.length - 1;
            while (n >= 0 && Double.isNaN(copy[n])) {
                n--;
            }
			// Increment n once, and it becomes the number of
            // non-NaN values.
            n++;
            if (n > 0) {
                int mid = n / 2;
                median = (n % 2 == 0) ? (copy[mid - 1] + copy[mid]) / 2.0 : copy[mid];
            }
        }
        return median;
    }

    public static void addTo(double[] toWhat, double[] toAdd) {
        final int len = toAdd.length;
        for (int i = 0; i < len; i++) {
            toWhat[i] += toAdd[i];
        }
    }

    public static void divideBy(double[] values, int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("n <= 0: " + n);
        }
        final int len = values.length;
        for (int i = 0; i < len; i++) {
            values[i] /= n;
        }
    }

    public static double minimum(double[] tuple) {
        double min = Double.NaN;
        final int n = tuple.length;
        if (n > 0) {
            min = tuple[0];
            for (int i = 1; i < n; i++) {
                double v = tuple[i];
                if (v < min) {
                    min = v;
                }
            }
        }
        return min;
    }

    public static double maximum(double[] tuple) {
        double max = Double.NaN;
        final int n = tuple.length;
        if (n > 0) {
            max = tuple[0];
            for (int i = 1; i < n; i++) {
                double v = tuple[i];
                if (v > max) {
                    max = v;
                }
            }
        }
        return max;
    }

    public static double absMinimum(double[] tuple) {
        double min = Double.NaN;
        final int n = tuple.length;
        if (n > 0) {
            min = Math.abs(tuple[0]);
            for (int i = 1; i < n; i++) {
                double v = Math.abs(tuple[i]);
                if (v < min) {
                    min = v;
                }
            }
        }
        return min;
    }

    public static double absMaximum(double[] tuple) {
        double max = Double.NaN;
        final int n = tuple.length;
        if (n > 0) {
            max = Math.abs(tuple[0]);
            for (int i = 1; i < n; i++) {
                double v = Math.abs(tuple[i]);
                if (v > max) {
                    max = v;
                }
            }
        }
        return max;
    }

    /**
     * Computes the L1 Norm of an array of tuple values.
     *
     * @param tuple
     *
     * @return the norm.
     */
    public static double norm1(double[] tuple) {
        double sum = 0.0;
        int n = tuple.length;
        int nanCount = 0;
        for (int i = 0; i < n; i++) {
            double d = tuple[i];
            if (Double.isNaN(d)) {
                nanCount++;
            } else {
                sum += Math.abs(d);
            }
        }
        if (nanCount > 0 && nanCount < n) {
            sum *= n / (n - nanCount);
        }
        return sum;
    }

    public static TupleList generateRandomGaussianTuples(
            int tupleLength,
            int tupleCount,
            int clusterCount,
            Random random,
            double tupleStandardDev,
            double clusterSizeStandardDev) {

        if (clusterCount <= 0) {
            throw new IllegalArgumentException("clusterCount must be > 0");
        }

        TupleList tuples = new ArrayTupleList(tupleLength, tupleCount);

        double[][] exemplars = new double[clusterCount][tupleLength];
        for (int i = 0; i < clusterCount; i++) {
            for (int j = 0; j < tupleLength; j++) {
                exemplars[i][j] = random.nextDouble();
            }
        }

        double meanClusterSize = ((double) tupleCount) / clusterCount;
        double clusterSDev = clusterSizeStandardDev * meanClusterSize;

        int[] clusterSizes = new int[clusterCount];
        int total = 0;

        for (int i = 0; i < clusterCount; i++) {
            clusterSizes[i] = (int) (meanClusterSize + random.nextGaussian() * clusterSDev + 0.5);
            total += clusterSizes[i];
        }

        if (total < tupleCount) {
            int n = tupleCount - total;
            for (int i = 0; i < n; i++) {
                clusterSizes[random.nextInt(clusterCount)]++;
            }
        } else {
            int n = total - tupleCount;
            for (int i = 0; i < n; i++) {
                clusterSizes[random.nextInt(clusterCount)]--;
            }
        }

        int[] whichCluster = new int[tupleCount];
        int n = 0;

        for (int i = 0; i < clusterCount; i++) {
            int size = clusterSizes[i];
            for (int j = 0; j < size; j++) {
                whichCluster[n++] = i;
            }
        }

        // No longer needed.
        clusterSizes = null;

        // Shuffle the elements of whichCluster.
        for (int i = tupleCount - 1; i > 1; i--) {
            int j = random.nextInt(i + 1);
            if (i != j) {
                // Swaps the elements.
                whichCluster[i] ^= whichCluster[j];
                whichCluster[j] ^= whichCluster[i];
                whichCluster[i] ^= whichCluster[j];
            }
        }

        double[] buffer = new double[tupleLength];

        for (int i = 0; i < tupleCount; i++) {
            int cluster = whichCluster[i];
            double[] exemplar = exemplars[cluster];
            System.arraycopy(exemplar, 0, buffer, 0, tupleLength);
            for (int j = 0; j < tupleLength; j++) {
                buffer[j] = exemplar[j] + random.nextGaussian() * tupleStandardDev;
            }
            tuples.setTuple(i, buffer);
        }

        return tuples;
    }

    public static int uniqueTupleCount(TupleList tuples) {

        final int tupleCount = tuples.getTupleCount();

        int[] indexes = new int[tupleCount];
        for (int i = 0; i < tupleCount; i++) {
            indexes[i] = i;
        }

        // Now sort the indexes based upon the values.
        Sorting.quickSort(indexes, new TupleListValueComparator(tuples));

        int uniqueTuples = tupleCount > 0 ? 1 : 0;

        if (tupleCount > 0) {

            final int tupleLength = tuples.getTupleLength();

            double[] buffer1 = new double[tupleLength];
            double[] buffer2 = new double[tupleLength];

            tuples.getTuple(0, buffer1);

            double[] lastBuf = buffer1;
            double[] nextBuf = buffer2;

            for (int i = 1; i < tupleCount; i++) {
                tuples.getTuple(indexes[i], nextBuf);
                if (compareValues(lastBuf, nextBuf) != 0) {
                    uniqueTuples++;
                    double[] tmp = lastBuf;
                    lastBuf = nextBuf;
                    nextBuf = tmp;
                }
            }

        }

        return uniqueTuples;
    }

    public static int checkUniqueTupleCount(TupleList tuples, final int minRequired) {

        final int tupleCount = tuples.getTupleCount();

        int[] indexes = new int[tupleCount];
        for (int i = 0; i < tupleCount; i++) {
            indexes[i] = i;
        }

        // Now sort the indexes based upon the values.
        Sorting.quickSort(indexes, new TupleListValueComparator(tuples));

        int uniqueTuples = tupleCount > 0 ? 1 : 0;

        if (tupleCount > 0) {

            final int tupleLength = tuples.getTupleLength();

            double[] buffer1 = new double[tupleLength];
            double[] buffer2 = new double[tupleLength];

            tuples.getTuple(0, buffer1);

            double[] lastBuf = buffer1;
            double[] nextBuf = buffer2;

            for (int i = 1; i < tupleCount; i++) {
                tuples.getTuple(indexes[i], nextBuf);
                if (compareValues(lastBuf, nextBuf) != 0) {
                    uniqueTuples++;
                    if (uniqueTuples == minRequired) {
                        return minRequired;
                    }
                    double[] tmp = lastBuf;
                    lastBuf = nextBuf;
                    nextBuf = tmp;
                }
            }

        }

        return uniqueTuples;
    }

    public static int compareValues(double[] values1, double[] values2) {
        final int len = values1.length;
        for (int i = 0; i < len; i++) {
            double v1 = values1[i];
            double v2 = values2[i];
			// Don't have to worry about NaNs, since if v1 is NaN and v2 is NaN, v1 < v2 evaluates to false,
            // v1 > v2 evaluates to false.  v1 == v2 evaluates to false also, but not calling that.
            if (v1 < v2) {
                return -1;
            } else if (v1 > v2) {
                return 1;
            }
        }
        return 0;
    }

    public static class TupleListValueComparator implements IntComparator {

        private TupleList tuples;
        private double[] buffer1, buffer2;

        public TupleListValueComparator(TupleList tuples) {
            int tupleLength = tuples.getTupleLength();
            buffer1 = new double[tupleLength];
            buffer2 = new double[tupleLength];
            this.tuples = tuples;
        }

        @Override
        public int compare(int n1, int n2) {
            tuples.getTuple(n1, buffer1);
            tuples.getTuple(n2, buffer2);
            return compareValues(buffer1, buffer2);
        }

    }

    /**
     * Computes the mean and sample variance of a distribution of values.
     *
     * (Originally written by Grant Nakamura and adapted by R. Scarberry.)
     *
     * @param values the values for which to compute the statistics.
     *
     * @return double array of length 2 with mean the 0th element and variance
     * the 1st. (These will be NaN if the values are of length 0.)
     */
    public static double[] meanAndVariance(double[] values) {
        double mean = Double.NaN, variance = Double.NaN;
        int n = values.length;
        if (n > 0) {
            double sumX = 0;
            double sumX2 = 0;
            int nonNaN = 0;
            for (int i = 0; i < n; i++) {
                double v = values[i];
                if (!Double.isNaN(v)) {
                    sumX += v;
                    sumX2 += v * v;
                    nonNaN++;
                }
            }
            if (nonNaN > 0) {
                mean = sumX / nonNaN;
                variance = (sumX2 - mean * sumX) / nonNaN;
            }
        }
        return new double[]{mean, variance};
    }

    /**
     * Computes the probability density function for a standard (mean = 0,
     * variance = 1) normal distribution.
     *
     * (Grant Nakamura originally wrote this code for the DistributionUtils
     * class in the IN-SPIRE codebase. R. Scarberry adapted it for JAC.)
     */
    static public double normalPdf(double x) {
        double pdf = Math.exp(-x * x / 2) / SQRT2PI;
        return pdf;
    }

    /**
     * Computes the cumulative distribution function for a standard (mean = 0,
     * variance = 1) normal distribution. This uses a numerical approximation
     * with absolute error < 7.5e-8, (eq. 26.2.17) from the Handbook of
     * Mathematical Functions, Abramowitz & Stegun, 10th printing.
     *
     * (Grant Nakamura originally wrote this code for the DistributionUtils
     * class in the IN-SPIRE codebase. R. Scarberry adapted it for JAC.)
     */
    public static double normalCdf(double x) {
        final double p = 0.2316419;
        final double[] b = {0.319381530, -0.356563782, 1.781477937,
            -1.821255978, 1.330274429};

        double xAbs = Math.abs(x);
        double t = 1 / (1 + p * xAbs);

        double sum = 0;
        for (int i = b.length - 1; i >= 0; i--) {
            sum = (sum + b[i]) * t;
        }

        double pdf = normalPdf(xAbs);
        double cdf = pdf * sum;
        if (x > 0) {
            cdf = 1 - cdf;
        }

        return cdf;
    }

    /**
     * Tests a one-dimensional distribution to see if it may be Gaussian, based
     * on an Anderson-Darling test.
     *
     * (Grant Nakamura originally wrote this code for the DistributionUtils
     * class in the IN-SPIRE codebase. R. Scarberry adapted it for JAC.)
     */
    public static boolean andersonDarlingGaussianTest(double[] values) {

        boolean result = false;

        int n = values.length;

        if (n > 0) {

            // Estimate mean and variance of the distribution
            double[] distrib = meanAndVariance(values);
            double mean = distrib[0];
            double variance = distrib[1];

            variance *= n / (n - 1.0); // Bessel's correction

            double stdDev = Math.sqrt(variance);

            double[] z = new double[n];
            for (int i = 0; i < n; i++) {
                // Normalize to a standard normal (mean=0, variance=1)
                double xNorm = (values[i] - mean) / stdDev;

                // Find the cumulative distribution
                z[i] = normalCdf(xNorm);
            }

            Arrays.sort(z);
            double sum = 0;
            for (int i = 0; i < n; i++) {
                sum += (2 * i + 1)
                        * (Math.log(z[i]) + Math.log(1 - z[(n - 1) - i]));
            }
            double andersonDarling = sum / (-n) - n;
            andersonDarling *= (1 + 0.75 / n + 2.25 / (n * n));

			// Use critical value corresponding to significance level of 0.0001.
            // This will be conservative about rejecting the null hypothesis
            // that
            // the distribution is Gaussian.
            result = (andersonDarling <= 1.8692);
        }

        return result;
    }

    /**
     * Returns the dot product of the two arrays of double values.
     *
     * @param buf1
     * @param buf2
     *
     * @return the dot product.
     *
     * @throws IllegalArgumentException - if both arrays do not have the same
     * length.
     */
    public static double dotProduct(double[] buf1, double[] buf2) {
        if (buf1.length != buf2.length) {
            throw new IllegalArgumentException("buf1.length != buf2.length: "
                    + buf1.length + " != " + buf2.length);
        }
        int n = buf1.length;
        double sum = 0.0;
        int nanCount = 0;
        for (int i = 0; i < n; i++) {
            double d = buf1[i] * buf2[i];
            if (Double.isNaN(d)) {
                nanCount++;
            } else {
                sum += d;
            }
        }
        if (nanCount > 0 && nanCount < n) {
            sum *= n / (n - nanCount);
        }
        return sum;
    }

    public static void main(String[] args) {

        TupleList tuples = generateRandomGaussianTuples(10000, 100, 25, new Random(), 0.15, 10.0);

        PrintWriter pw = null;

        try {

            int tupleCount = tuples.getTupleCount();
            int tupleLength = tuples.getTupleLength();

            pw = new PrintWriter(new BufferedWriter(new FileWriter("tuples.vect.txt")));
            pw.printf("[FILETYPE] Vector List (text)\n[VERSION] 2.0\n%d %d\n", tupleCount, tupleLength);

            double[] buffer = new double[tupleLength];

            for (int i = 0; i < tupleCount; i++) {
                pw.print(i);
                tuples.getTuple(i, buffer);
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < tupleLength; j++) {
                    sb.append(' ');
                    sb.append(String.valueOf(buffer[j]));
                }
                pw.println(sb.toString());
            }

        } catch (IOException e) {

            e.printStackTrace();

        } finally {
            if (pw != null) {
                pw.close();
            }
        }
    }
}
