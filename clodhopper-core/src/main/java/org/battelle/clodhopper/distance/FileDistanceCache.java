package org.battelle.clodhopper.distance;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;


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
 * FileDistanceCache.java
 *
 *===================================================================*/
/**
 * An implementation of <code>DistanceCache</code> that maintains the distances
 * in a file on disk.
 *
 * @author R. Scarberry
 * @since 1.0
 *
 */
public class FileDistanceCache implements DistanceCache {

    private final File file;
    private final int indexCount;
    private final long distanceCount;

    // For randomly accessing the file of distances.
    private RandomAccessFile raFile;

    /**
     * Constructor for creating an entirely new file-based distance cache.
     *
     * @param indexCount the number of entities for which to maintain distances.
     * The total number of distances is
     * <code>indexCount*(indexCount-1)/2</code>, since Dij == Dji and Dii = 0,
     * for i, j from 0 to (indexCount - 1).
     * @param f the file in which to store the distances.
     *
     * @throws IOException if an I/O error occurs.
     * @throws IllegalArgumentException if indexCount is negative.
     * @throws NullPointerException if f is null.
     */
    public FileDistanceCache(final int indexCount, final File f) throws IOException {

        if (indexCount < 0) {
            throw new IllegalArgumentException("number of indices < 0: " + indexCount);
        }

        if (f == null) {
            throw new NullPointerException();
        }

        this.indexCount = indexCount;
        this.file = f;

        this.distanceCount = ((long) indexCount * ((long) indexCount - 1L)) / 2L;

        openFile();

        // In order to restore from a file, need to write the index count.
        raFile.writeInt(indexCount);

	// Write 0.0 as the last distance, to expand the file to its complete size.
        // O/W, if not all distances are set before the object is done with, the
        // file will not be large enough to be used by DistanceCacheFactory.read()
        // to restore a DistanceCache object.
        long offset = 8L * (getNumDistances() - 1) + 4L;
        raFile.seek(offset);
        raFile.writeDouble(0.0);
    }

    /**
     * Constructor for opening an existing file-based distance cache. The
     * distances are assumed to already be present in the file.
     *
     * @param f a file containing the distances.
     *
     * @throws IOException
     */
    public FileDistanceCache(final File f) throws IOException {

        this.file = f;

        long actualLength = f.length();

        openFile();

        try {

            this.indexCount = raFile.readInt();
            if (this.indexCount < 0) {
                throw new IOException("invalid distance cache file: indexCount = " + indexCount);
            }

            this.distanceCount = ((long) indexCount * ((long) indexCount - 1L)) / 2L;

            long expectedLength = 8L * this.distanceCount + 4L;
            if (expectedLength != actualLength) {
                throw new IOException(String.format("invalid distance cache file: expected file length == %d, actual length == %d",
                        expectedLength, actualLength));
            }

        } catch (IOException e) {
            // Close the random access file, then rethrow.
            try {
                closeFile();
            } catch (IOException e2) {
                // Suppress
            }
            throw e;
        }
    }

    /**
     * Is the backing file open?
     *
     * @return
     */
    public synchronized boolean isOpen() {
        return raFile != null;
    }

    private synchronized void openFile() throws IOException {
        if (raFile == null) {
            raFile = new RandomAccessFile(file, "rw");
        }
    }

    /**
     * Closes the file backing the cache.
     *
     * @throws IOException
     */
    public synchronized void closeFile() throws IOException {
        if (raFile != null) {
            raFile.close();
            raFile = null;
        }
    }

    @Override
    protected void finalize() {
        if (isOpen()) {
            try {
                closeFile();
            } catch (IOException ioe) {
            }
        }
    }

    /**
     * Get the backing file.
     *
     * @return the file
     */
    public File getFile() {
        return file;
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= indexCount) {
            throw new IllegalArgumentException("index not in [0 - (" + indexCount + " - 1)]: " + index);
        }
    }

    /**
     * {@inheritDoc}
     */
    public long distancePos(int index1, int index2) {
        if (index1 == index2) {
            throw new IllegalArgumentException("indices are equal: " + index1);
        }
        if (index1 > index2) { // Swap them
            index1 ^= index2;
            index2 ^= index1;
            index1 ^= index2;
        }
        long n = indexCount - index1;
        return distanceCount - n * (n - 1) / 2 + index2 - index1 - 1;
    }

    private long fileOffset(int index1, int index2) {
		// 8L is the sizeof a double, 4L accounts for the indexCount written to 
        // the start of the file.
        return 8L * distancePos(index1, index2) + 4L;
    }

    private boolean contiguousIndices(int[] indices1, int[] indices2) {
        int n = indices1.length;
        if (n > 0) {
            long lastPos = distancePos(indices1[0], indices2[0]);
            for (int i = 1; i < n; i++) {
                long curPos = distancePos(indices1[i], indices2[i]);
                if (curPos != lastPos + 1L) {
                    return false;
                }
                lastPos = curPos;
            }
        }
        return true;
    }

    /**
     * Get the number of indices, N. Valid indices for the other methods are
     * then [0 - (N-1)].
     *
     * @return - the number of indices.
     */
    public int getNumIndices() {
        return indexCount;
    }

    public long getNumDistances() {
        return distanceCount;
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public synchronized double getDistance(long n) throws IOException {
        if (!isOpen()) {
            openFile();
        }
        long offset = 8L * n + 4L;
        raFile.seek(offset);
        return raFile.readDouble();
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public synchronized double getDistance(int index1, int index2) throws IOException {
        checkIndex(index1);
        checkIndex(index2);
        if (!isOpen()) {
            openFile();
        }
        raFile.seek(fileOffset(index1, index2));
        return raFile.readDouble();
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public synchronized double[] getDistances(int[] indices1, int[] indices2, double[] distances) throws IOException {
        int n = indices1.length;
        if (n != indices2.length) {
            throw new IllegalArgumentException(String.valueOf(n) + " != " + indices2.length);
        }
        double[] d = distances;
        if (distances != null) {
            if (distances.length != n) {
                throw new IllegalArgumentException("distance buffer length not equal to number of indices");
            }
        } else {
            d = new double[n];
        }
        if (n > 1 && contiguousIndices(indices1, indices2)) {
            // If they're continuous, only have to check the beginning and ending indices.
            checkIndex(indices1[0]);
            checkIndex(indices2[0]);
            checkIndex(indices1[n - 1]);
            checkIndex(indices2[n - 1]);
			// Everything can be read in one gulp, which is much faster than reading one
            // at a time.
            byte[] readBuffer = new byte[n * 8]; // 8 bytes per distance.
            if (!isOpen()) {
                openFile();
            }
            raFile.seek(fileOffset(indices1[0], indices2[0]));
            raFile.readFully(readBuffer);
            fromBytes(readBuffer, 0, d, 0, readBuffer.length);
        } else { // Indices aren't continuous, or have less than 2 to read.
                 // Get 'em one at a time.
            for (int i = 0; i < n; i++) {
                d[i] = getDistance(indices1[i], indices2[i]);
            }
        }
        return d;

    }

    /**
     * Sets the values in an array of doubles from values in an array of bytes.
     * This is convenient, say, if double data has been read from a stream as a
     * series of bytes and needs to be converted in bulk.
     *
     * @param sourceBytes array containing the bytes to be converted.
     * @param sourceIndex starting index into the array of source bytes.
     * @param dest destination array for the double values.
     * @param destIndex starting into into the destination array.
     * @param byteCount the number of bytes to be converted.
     *
     * @throws IndexOutOfBoundsException if the array dimensions do not match
     * the source index, destination index, or byte count.
     */
    public static void fromBytes(byte[] sourceBytes, int sourceIndex,
            double[] dest, int destIndex, int byteCount) {
        int doubleCount = byteCount / 8;
        if (dest.length < destIndex + doubleCount) {
            throw new IllegalArgumentException("dest.length too small: "
                    + dest.length + " < " + (destIndex + doubleCount));
        }
        for (int i = 0; i < doubleCount; i++) {
            int b0 = sourceBytes[sourceIndex++] & 0xFF;
            int b1 = sourceBytes[sourceIndex++] & 0xFF;
            int b2 = sourceBytes[sourceIndex++] & 0xFF;
            int b3 = sourceBytes[sourceIndex++] & 0xFF;
            int b4 = sourceBytes[sourceIndex++] & 0xFF;
            int b5 = sourceBytes[sourceIndex++] & 0xFF;
            int b6 = sourceBytes[sourceIndex++] & 0xFF;
            int b7 = sourceBytes[sourceIndex++] & 0xFF;
            dest[destIndex++] = Double.longBitsToDouble(
                    ((long) ((b0 << 24) + (b1 << 16) + (b2 << 8) + (b3 << 0)) << 32)
                    + (((b4 << 24) + (b5 << 16) + (b6 << 8) + (b7 << 0)) & 0xFFFFFFFFL)
            );
        }
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public synchronized void setDistance(int index1, int index2, double distance) throws IOException {
        checkIndex(index1);
        checkIndex(index2);
        if (!isOpen()) {
            openFile();
        }
        raFile.seek(fileOffset(index1, index2));
        raFile.writeDouble(distance);
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public synchronized void setDistances(int[] indices1, int[] indices2, double[] distances)
            throws IOException {
        int n = indices1.length;
        if (n != indices2.length) {
            throw new IllegalArgumentException(String.valueOf(n) + " != " + indices2.length);
        }
        if (n != distances.length) {
            throw new IllegalArgumentException("distance buffer length not equal to number of indices");
        }
        if (n > 1 && contiguousIndices(indices1, indices2)) {
            // If they're continuous, only have to check the beginning and ending indices.
            checkIndex(indices1[0]);
            checkIndex(indices2[0]);
            checkIndex(indices1[n - 1]);
            checkIndex(indices2[n - 1]);
			// Everything can be written in one schmeer, which is much faster than writing one
            // at a time.
            byte[] writeBuffer = new byte[n * 8]; // 8 bytes per distance.
            toBytes(distances, 0, writeBuffer, 0, n);
            if (!isOpen()) {
                openFile();
            }
            raFile.seek(fileOffset(indices1[0], indices2[0]));
            raFile.write(writeBuffer);
        } else {
            for (int i = 0; i < n; i++) {
                setDistance(indices1[i], indices2[i], distances[i]);
            }
        }
    }

    /**
     * Converts a source array of doubles values to an array of bytes. This is
     * convenient, say, if double data needs to be written to a stream in bulk
     * as a series of bytes.
     *
     * @param source array containing the doubles to be converted.
     * @param sourceIndex starting index into the source array.
     * @param destBytes destination array of bytes.
     * @param destIndex starting into into the destination array.
     * @param doubleCount the number of doubles to be converted.
     *
     * @throws IndexOutOfBoundsException if the array dimensions do not match
     * the source index, destination index, or double count.
     */
    public static void toBytes(double[] source, int sourceIndex,
            byte[] destBytes, int destIndex, int doubleCount) {
        int byteCount = doubleCount * 8;
        if (destBytes.length < destIndex + byteCount) {
            throw new IllegalArgumentException("dest.length too small: "
                    + destBytes.length + " < " + (destIndex + byteCount));
        }
        for (int i = 0; i < doubleCount; i++) {
            long bits = Double.doubleToLongBits(source[sourceIndex++]);
            destBytes[destIndex++] = (byte) ((int) (bits >>> 56) & 0xFF);
            destBytes[destIndex++] = (byte) ((int) (bits >>> 48) & 0xFF);
            destBytes[destIndex++] = (byte) ((int) (bits >>> 40) & 0xFF);
            destBytes[destIndex++] = (byte) ((int) (bits >>> 32) & 0xFF);
            destBytes[destIndex++] = (byte) ((int) (bits >>> 24) & 0xFF);
            destBytes[destIndex++] = (byte) ((int) (bits >>> 16) & 0xFF);
            destBytes[destIndex++] = (byte) ((int) (bits >>> 8) & 0xFF);
            destBytes[destIndex++] = (byte) ((int) (bits >>> 0) & 0xFF);
        }
    }
}
