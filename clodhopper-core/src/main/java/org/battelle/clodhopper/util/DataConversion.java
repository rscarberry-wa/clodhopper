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
 * 
 * DataConversion.java
 *
 *===================================================================*/
/**
 * Contains static utility methods for converting between various primitive data
 * types.
 *
 * @author R. Scarberry
 *
 */
public final class DataConversion {

    private DataConversion() {
    }

    /**
     * For the specified integer, returns a 4-element byte array containing its
     * two's-complement binary representation in big-endian order.
     *
     * @param n the integer value.
     * @return array of 4 bytes.
     */
    public static byte[] intToBytes(final int n) {
        return new byte[]{
            (byte) (n >> 24 & 0xff),
            (byte) (n >> 16 & 0xff),
            (byte) (n >> 8 & 0xff),
            (byte) (n & 0xff)
        };
    }

    /**
     * For the specified char, returns a 2-element byte array containing its
     * two's-complement binary representation in big-endian order.
     *
     * @param c the character value.
     * @return an array of 2 bytes.
     */
    public static byte[] charToBytes(final char c) {
        return new byte[]{
            (byte) (c >> 8 & 0xff),
            (byte) (c & 0xff)
        };
    }

    /**
     * Translates two bytes from the provided array into a char.
     *
     * @param bytes an array of at least 2 bytes.
     * @return a <code>char</code> constructed from the first 2 bytes of the
     * array.
     */
    public static char charFromBytes(final byte[] bytes) {
        return charFromBytes(bytes, 0);
    }

    /**
     * Translates the 2 bytes beginning at the specified index into a char.
     *
     * @param bytes an array which must be of sufficient length to have 2 bytes
     * beginning at the specified index.
     * @param index the index into the array of bytes.
     *
     * @return a <code>char</code> constructed from 2 bytes in the array.
     */
    public static char charFromBytes(final byte[] bytes, final int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("negative index: " + index);
        }
        if ((bytes.length - index) < 2) {
            throw new IllegalArgumentException("insufficient number of bytes: " + (bytes.length - index));
        }
        int b0 = bytes[index] & 0xff;
        int b1 = bytes[index + 1] & 0xff;
        return (char) (b0 << 8 | b1);
    }

    /**
     * Translates 4 bytes from the supplied array into a float.
     *
     * @param bytes an array containing at least 4 bytes.
     * @return a <code>float</code> constructed from the first 4 bytes of the
     * array.
     */
    public static float floatFromBytes(final byte[] bytes) {
        return floatFromBytes(bytes, 0);
    }

    /**
     * Translates 4 bytes from the supplied array into a float.
     *
     * @param bytes an array containing at least 4 bytes starting from the
     * specified offset.
     * @param offset the index into the array where the 4 bytes begin.
     *
     * @return a <code>float</code> constructed from 4 bytes of the array.
     */
    public static float floatFromBytes(final byte[] bytes, final int offset) {
        return Float.intBitsToFloat(intFromBytes(bytes, offset));
    }

    /**
     * Translates a <code>float</code> value to 4 bytes.
     *
     * @param f the value to translate.
     * @return an array of 4 bytes.
     */
    public static byte[] floatToBytes(final float f) {
        return intToBytes(Float.floatToIntBits(f));
    }

    /**
     * Translates an array of 8 bytes into a <code>double</code> value.
     *
     * @param bytes the array of at least 8 bytes.
     * @return the double value.
     */
    public static double doubleFromBytes(final byte[] bytes) {
        return doubleFromBytes(bytes, 0);
    }

    /**
     * Translates 8 bytes from an array into a <code>double</code> value.
     *
     * @param bytes the array of at least 8 bytes beginning at the specified
     * offset.
     * @param offset an offset into the array of bytes.
     * @return the double value.
     */
    public static double doubleFromBytes(final byte[] bytes, final int offset) {
        return Double.longBitsToDouble(longFromBytes(bytes, offset));
    }

    /**
     * Translates a double value into an array of 8 bytes.
     *
     * @param d the value to translate.
     * @return an array of 8 bytes.
     */
    public static byte[] doubleToBytes(final double d) {
        return longToBytes(Double.doubleToLongBits(d));
    }

    /**
     * Returns the integer representation of a two's-complement array of four
     * bytes. Does the reverse of <code>toBytes(int n)</code>.
     *
     * @param bytes an array of at least 4 bytes.
     * @return an <code>int</code> translated from the 4 bytes.
     */
    public static int intFromBytes(final byte[] bytes) {
        return intFromBytes(bytes, 0);
    }

    /**
     * Returns the integer representation of a two's-complement blocks of 4
     * bytes in the specified array.
     *
     * @param bytes an array containing at least 4 bytes beginning at the
     * specified index.
     * @param index an index into the array of bytes.
     * @return an <code>int</code> translated from the 4 bytes.
     */
    public static int intFromBytes(final byte[] bytes, final int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("negative index: " + index);
        }
        if ((bytes.length - index) < 4) {
            throw new IllegalArgumentException("insufficient number of bytes: " + (bytes.length - index));
        }
        int b0 = bytes[index] & 0xff;
        int b1 = bytes[index + 1] & 0xff;
        int b2 = bytes[index + 2] & 0xff;
        int b3 = bytes[index + 3] & 0xff;
        return b0 << 24 | b1 << 16 | b2 << 8 | b3;
    }

    /**
     * Translates an array of bytes into an array of integer values.
     *
     * @param bytes an array of bytes to translate into integers.
     * @return an array of <code>int</code> values of length equal to
     * <code>bytes.length/4</code>, since there are 4 bytes to an int.
     */
    public static int[] intsFromBytes(final byte[] bytes) {
        if (bytes.length % 4 != 0) {
            throw new IllegalArgumentException("number of bytes not a multiple of 4: " + bytes.length);
        }
        int intCount = bytes.length / 4;
        int[] ints = new int[intCount];
        for (int i = 0; i < intCount; i++) {
            ints[i] = intFromBytes(bytes, i * 4);
        }
        return ints;
    }

    /**
     * Translates an array of at least 2 bytes into a short.
     *
     * @param bytes the array of bytes.
     * @return a <code>short</code> value.
     */
    public static short shortFromBytes(final byte[] bytes) {
        return shortFromBytes(bytes, 0);
    }

    /**
     * Translates an array of at least 2 bytes beginning at the specified index
     * into a short.
     *
     * @param bytes the array of bytes.
     * @param index an index into the array of bytes.
     * @return a <code>short</code> value.
     */
    public static short shortFromBytes(final byte[] bytes, final int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("negative index: " + index);
        }
        if ((bytes.length - index) < 2) {
            throw new IllegalArgumentException("insufficient number of bytes: " + (bytes.length - index));
        }

        int b0 = bytes[index] & 0xff;
        int b1 = bytes[index + 1] & 0xff;

        return (short) (b0 << 8 | b1);
    }

    /**
     * Translates a <code>short</code> into 2 bytes.
     *
     * @param n the value to translate.
     * @return an array of 2 bytes.
     */
    public static byte[] shortToBytes(final short n) {
        return new byte[]{(byte) (n >> 8 & 0xff), (byte) (n & 0xff)};
    }

    /**
     * For the specified long, returns a byte array containing its
     * two's-complement binary representation.
     *
     * @param n the value to translate.
     * @return an array of 8 bytes.
     */
    public static byte[] longToBytes(final long n) {
        return new byte[]{
            (byte) (n >> 56 & 0xffL),
            (byte) (n >> 48 & 0xffL),
            (byte) (n >> 40 & 0xffL),
            (byte) (n >> 32 & 0xffL),
            (byte) (n >> 24 & 0xffL),
            (byte) (n >> 16 & 0xffL),
            (byte) (n >> 8 & 0xffL),
            (byte) (n & 0xffL)
        };

    }

    /**
     * Constructs an <code>int</code> value from two <code>short</code>s.
     *
     * @param s1 the first value.
     * @param s2 the second value.
     * @return an <code>int</code> value.
     */
    public static int intFromShorts(final short s1, final short s2) {
        int imask = 0xff;

        int b0 = (int) (s1 >> 8 & imask);
        int b1 = (int) (s1 & imask);
        int b2 = (int) (s2 >> 8 & imask);
        int b3 = (int) (s2 & imask);

        return b0 << 24 | b1 << 16 | b2 << 8 | b3;
    }

    /**
     * Translates an <code>int</code> value into two <code>short</code>s.
     *
     * @param n the value to translate.
     * @return an array of two <code>short</code>s.
     */
    public static short[] shortsFromInt(final int n) {
        int mask = 0xFF;

        short b0 = (short) (n >> 24 & mask);
        short b1 = (short) (n >> 16 & mask);
        short b2 = (short) (n >> 8 & mask);
        short b3 = (short) (n & mask);

        short s1 = (short) (b0 << 8 | b1);
        short s2 = (short) (b2 << 8 | b3);

        return new short[]{s1, s2};
    }

    /**
     * Translates an array of at least eight bytes into a <code>long</code>
     *
     * @param bytes the array of bytes.
     * @return a <code>long</code>.
     */
    public static long longFromBytes(final byte[] bytes) {
        return longFromBytes(bytes, 0);
    }

    /**
     * Returns the long integer representation of a two's-complement array of
     * eight bytes beginning at the specified index.
     *
     * @param bytes the array of bytes.
     * @param offset an offset into the array.
     *
     * @return a <code>long</code> value.
     */
    public static long longFromBytes(final byte[] bytes, final int offset) {
        if (bytes.length - offset < 8) {
            throw new IllegalArgumentException("insufficient number of bytes: " + bytes.length);
        }
        long b0 = bytes[offset + 0] & 0xffL;
        long b1 = bytes[offset + 1] & 0xffL;
        long b2 = bytes[offset + 2] & 0xffL;
        long b3 = bytes[offset + 3] & 0xffL;
        long b4 = bytes[offset + 4] & 0xffL;
        long b5 = bytes[offset + 5] & 0xffL;
        long b6 = bytes[offset + 6] & 0xffL;
        long b7 = bytes[offset + 7] & 0xffL;
        return b0 << 56 | b1 << 48 | b2 << 40 | b3 << 32 | b4 << 24 | b5 << 16 | b6 << 8 | b7;
    }

    /**
     * Constructs a <code>long</code> value from two <code>int</code>s.
     *
     * @param n1 the first value.
     * @param n2 the second value.
     * @return a <code>long</code> value.
     */
    public static long longFromInts(final int n1, final int n2) {
        int imask = 0xff;

        long b0 = (long) (n1 >> 24 & imask);
        long b1 = (long) (n1 >> 16 & imask);
        long b2 = (long) (n1 >> 8 & imask);
        long b3 = (long) (n1 & imask);
        long b4 = (long) (n2 >> 24 & imask);
        long b5 = (long) (n2 >> 16 & imask);
        long b6 = (long) (n2 >> 8 & imask);
        long b7 = (long) (n2 & imask);

        return b0 << 56 | b1 << 48 | b2 << 40 | b3 << 32 | b4 << 24 | b5 << 16 | b6 << 8 | b7;
    }

    /**
     * Splits a <code>long</code> value into two <code>int</code> values.
     *
     * @param n the value to translate.
     * @return an array of two <code>int</code>s.
     */
    public static int[] intsFromLong(final long n) {
        long lmask = 0xFFL;

        int b0 = (int) (n >> 56 & lmask);
        int b1 = (int) (n >> 48 & lmask);
        int b2 = (int) (n >> 40 & lmask);
        int b3 = (int) (n >> 32 & lmask);

        int b4 = (int) (n >> 24 & lmask);
        int b5 = (int) (n >> 16 & lmask);
        int b6 = (int) (n >> 8 & lmask);
        int b7 = (int) (n & lmask);

        int n1 = b0 << 24 | b1 << 16 | b2 << 8 | b3;
        int n2 = b4 << 24 | b5 << 16 | b6 << 8 | b7;

        return new int[]{n1, n2};
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
    public static void fromBytes(final byte[] sourceBytes,
            int sourceIndex,
            final double[] dest,
            int destIndex,
            final int byteCount) {

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

    /**
     * Sets the values in an array of doubles from values in an array of bytes.
     * This is convenient, say, if double data has been read from a stream as a
     * series of bytes and needs to be converted in bulk.
     *
     * @param sourceBytes array containing the bytes to be converted.
     * @param dest destination array for the double values.
     *
     * @throws IndexOutOfBoundsException if the length of the destination array
     * is insufficient.
     */
    public static void fromBytes(final byte[] sourceBytes, final double[] dest) {
        fromBytes(sourceBytes, 0, dest, 0, sourceBytes.length);
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
    public static void toBytes(final double[] source,
            int sourceIndex,
            final byte[] destBytes,
            int destIndex,
            final int doubleCount) {

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

    /**
     * Converts a source array of doubles values to an array of bytes. This is
     * convenient, say, if double data needs to be written to a stream in bulk
     * as a series of bytes.
     *
     * @param source array containing the doubles to be converted.
     * @param destBytes destination array of bytes.
     *
     * @throws IndexOutOfBoundsException if the destination array length is
     * insufficient.
     */
    public static void toBytes(final double[] source, final byte[] destBytes) {
        toBytes(source, 0, destBytes, 0, source.length);
    }

    /**
     * Converts a section of a source array of characters into bytes.
     *
     * @param source the array of characters to translate.
     * @param sourceIndex an offset into the source array of where to start.
     * @param destBytes an array of bytes where the translated bytes are to be
     * copied.
     * @param destIndex an offset into the array of bytes to begin the copy.
     * @param charCount the number of characters to translate into bytes.
     */
    public static void toBytes(final char[] source,
            int sourceIndex,
            final byte[] destBytes,
            int destIndex,
            final int charCount) {

        int byteCount = charCount * 2;
        if (destBytes.length < destIndex + byteCount) {
            throw new IllegalArgumentException("dest.length too small: "
                    + destBytes.length + " < " + (destIndex + byteCount));
        }
        for (int i = 0; i < charCount; i++) {
            char c = source[0];
            destBytes[destIndex++] = (byte) (c >> 8 & 0xff);
            destBytes[destIndex++] = (byte) (c & 0xff);
        }
    }

    /**
     * Translates an array of characters into bytes.
     *
     * @param source the array of characters to translate.
     * @param destBytes an array of bytes where the translated bytes are to be
     * copied. This array must be at least twice as long as the source array.
     */
    public static void toBytes(final char[] source, final byte[] destBytes) {
        toBytes(source, 0, destBytes, 0, source.length);
    }

}
