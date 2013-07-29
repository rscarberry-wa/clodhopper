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
 * Contains static utility methods for converting between various primitive data types.
 * 
 * @author R. Scarberry
 *
 */
public final class DataConversion {

	private DataConversion() {}
	
	/**
	 * For the specified integer, returns a 4-element byte array containing its two's-complement
	 * binary representation in big-endian order.
	 * 
	 * @param n
	 * @return
	 */
	public static byte[] intToBytes(int n) {
		return new byte[] {
			(byte) (n >> 24 & 0xff),
			(byte) (n >> 16 & 0xff),
			(byte) (n >> 8 & 0xff),
			(byte) (n & 0xff)
		};
	}
	
	/**
	 * For the specified char, returns a 2-element byte array containing its two's-complement
	 * binary representation in big-endian order.
	 * 
	 * @param n
	 * @return
	 */
	public static byte[] charToBytes(char c) {
		return new byte[] {
				(byte) (c >> 8 & 0xff),
				(byte) (c & 0xff)
		};
	}

	/**
	 * Translates two bytes from the provided array into a char.
	 * 
	 * @param bytes
	 * @return
	 */
	public static char charFromBytes(byte[] bytes) {
		return charFromBytes(bytes, 0);
	}
	
	/**
	 * Translates the 2 bytes beginning at the specified index into a char.
	 * 
	 * @param bytes
	 * @param index
	 * 
	 * @return
	 */
	public static char charFromBytes(byte[] bytes, int index) {
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
	 * @param bytes
	 * @return
	 */
	public static float floatFromBytes(byte[] bytes) {
		return floatFromBytes(bytes, 0);
	}
	
	public static float floatFromBytes(byte[] bytes, int offset) {
		return Float.intBitsToFloat(intFromBytes(bytes, offset));
	}
	
	public static byte[] floatToBytes(float f) {
		return intToBytes(Float.floatToIntBits(f));
	}
	
	public static double doubleFromBytes(byte[] bytes) {
		return doubleFromBytes(bytes, 0);
	}
	
	public static double doubleFromBytes(byte[] bytes, int offset) {
		return Double.longBitsToDouble(longFromBytes(bytes, offset));
	}
	
	public static byte[] doubleToBytes(double d) {
		return longToBytes(Double.doubleToLongBits(d));
	}
	
	/**
	 * Returns the integer representation of a two's-complement array of four bytes.  
	 * Does the reverse of <code>toBytes(int n)</code>.
	 * 
	 * @param n
	 * @return
	 */
	public static int intFromBytes(byte[] bytes) {
		return intFromBytes(bytes, 0);
	}
	
	public static int intFromBytes(byte[] bytes, int index) {
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

	public static int[] intsFromBytes(byte[] bytes) {
		if (bytes.length % 4 != 0) {
			throw new IllegalArgumentException("number of bytes not a multiple of 4: " + bytes.length);
		}
		int intCount = bytes.length/4;
		int[] ints = new int[intCount];
		for (int i=0; i<intCount; i++) {
			ints[i] = intFromBytes(bytes, i*4);
		}
		return ints;
	}
	
	public static short shortFromBytes(byte[] bytes) {
		return shortFromBytes(bytes, 0);
	}
	
	public static short shortFromBytes(byte[] bytes, int index) {
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
	
	public static byte[] shortToBytes(short n) {
		return new byte[] { (byte) (n >> 8 & 0xff), (byte) (n & 0xff) };
	}
	
	/**
	 * For the specified long, returns a byte array containing its two's-complement
	 * binary representation.
	 * 
	 * @param n
	 * @return
	 */
	public static byte[] longToBytes(long n) {
		return new byte[] {
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
	
	public static int intFromShorts(short s1, short s2) {
		int imask = 0xff;
		
		int b0 = (int) (s1 >> 8 & imask);
		int b1 = (int) (s1 & imask);
		int b2 = (int) (s2 >> 8 & imask);
		int b3 = (int) (s2 & imask);

		return b0 << 24 | b1 << 16 | b2 << 8 | b3;
	}
	
	public static short[] shortsFromInt(int n) {
		int mask = 0xFF;
		
		short b0 = (short) (n >> 24 & mask);
		short b1 = (short) (n >> 16 & mask);
		short b2 = (short) (n >> 8 & mask);
		short b3 = (short) (n & mask);
		
		short s1 = (short) (b0 << 8 | b1);
		short s2 = (short) (b2 << 8 | b3);
		
		return new short[] { s1, s2 };
	}

	public static long longFromBytes(byte[] bytes) {
		return longFromBytes(bytes, 0);
	}
	
	/**
	 * Returns the integer representation of a two's-complement array of four bytes.  
	 * Does the reverse of <code>toBytes(int n)</code>.
	 * 
	 * @param n
	 * @return
	 */
	public static long longFromBytes(byte[] bytes, int offset) {
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
	
	public static long longFromInts(int n1, int n2) {
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
	
	public static int[] intsFromLong(long n) {
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
		
		return new int[] { n1, n2 };
	}

	/**
	 * Sets the values in an array of doubles from values in an array of bytes.  This is 
	 * convenient, say, if double data has been read from a stream as a series of bytes and
	 * needs to be converted in bulk.
	 * 
	 * @param sourceBytes array containing the bytes to be converted.
	 * @param sourceIndex starting index into the array of source bytes.
	 * @param dest destination array for the double values.
	 * @param destIndex starting into into the destination array.
	 * @param byteCount the number of bytes to be converted.
	 * 
	 * @throws IndexOutOfBoundsException if the array dimensions do not match
	 *   the source index, destination index, or byte count.
	 */
	public static void fromBytes(byte[] sourceBytes, int sourceIndex, 
			double[] dest, int destIndex, int byteCount) {
		int doubleCount = byteCount/8;
		if (dest.length < destIndex + doubleCount) {
			throw new IllegalArgumentException("dest.length too small: " +
					dest.length + " < " + (destIndex + doubleCount));
		}
		for (int i=0; i<doubleCount; i++) {
			int b0 = sourceBytes[sourceIndex++] & 0xFF;
			int b1 = sourceBytes[sourceIndex++] & 0xFF;
			int b2 = sourceBytes[sourceIndex++] & 0xFF;
			int b3 = sourceBytes[sourceIndex++] & 0xFF;
			int b4 = sourceBytes[sourceIndex++] & 0xFF;
			int b5 = sourceBytes[sourceIndex++] & 0xFF;
			int b6 = sourceBytes[sourceIndex++] & 0xFF;
			int b7 = sourceBytes[sourceIndex++] & 0xFF;
			dest[destIndex++] = Double.longBitsToDouble(
				((long)((b0 << 24) + (b1 << 16) + (b2 << 8) + (b3 << 0)) << 32)
				+
				(((b4 << 24) + (b5 << 16) + (b6 << 8) + (b7 << 0)) & 0xFFFFFFFFL)
			);
		}
	}

	/**
	 * Sets the values in an array of doubles from values in an array of bytes.  This is 
	 * convenient, say, if double data has been read from a stream as a series of bytes and
	 * needs to be converted in bulk.
	 * 
	 * @param sourceBytes array containing the bytes to be converted.
	 * @param dest destination array for the double values.
	 * 
	 * @throws IndexOutOfBoundsException if the length of the destination array is
	 *   insufficient.
	 */
	public static void fromBytes(byte[] sourceBytes, double[] dest) {
		fromBytes(sourceBytes, 0, dest, 0, sourceBytes.length);
	}

	/**
	 * Converts a source array of doubles values to an array of bytes.  This is 
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
	 *   the source index, destination index, or double count.
	 */
	public static void toBytes(double[] source, int sourceIndex, 
			byte[] destBytes, int destIndex, int doubleCount) {
		int byteCount = doubleCount*8;
		if (destBytes.length < destIndex + byteCount) {
			throw new IllegalArgumentException("dest.length too small: " +
					destBytes.length + " < " + (destIndex + byteCount));
		}
		for (int i=0; i<doubleCount; i++) {
			long bits = Double.doubleToLongBits(source[sourceIndex++]);
			destBytes[destIndex++] = (byte) ((int)(bits >>> 56) & 0xFF);
			destBytes[destIndex++] = (byte) ((int)(bits >>> 48) & 0xFF);
			destBytes[destIndex++] = (byte) ((int)(bits >>> 40) & 0xFF);
			destBytes[destIndex++] = (byte) ((int)(bits >>> 32) & 0xFF);
			destBytes[destIndex++] = (byte) ((int)(bits >>> 24) & 0xFF);
			destBytes[destIndex++] = (byte) ((int)(bits >>> 16) & 0xFF);
			destBytes[destIndex++] = (byte) ((int)(bits >>>  8) & 0xFF);
			destBytes[destIndex++] = (byte) ((int)(bits >>>  0) & 0xFF);
		}
	}

	/**
	 * Converts a source array of doubles values to an array of bytes.  This is 
	 * convenient, say, if double data needs to be written to a stream in bulk 
	 * as a series of bytes.
	 * 
	 * @param source array containing the doubles to be converted.
	 * @param destBytes destination array of bytes.
	 * 
	 * @throws IndexOutOfBoundsException if the destination array length is
	 *   insufficient.
	 */
	public static void toBytes(double[] source, byte[] destBytes) {
		toBytes(source, 0, destBytes, 0, source.length);
	}
	
	public static void toBytes(char[] source, int sourceIndex, 
			byte[] destBytes, int destIndex, int charCount) {
		int byteCount = charCount * 2;
		if (destBytes.length < destIndex + byteCount) {
			throw new IllegalArgumentException("dest.length too small: " +
					destBytes.length + " < " + (destIndex + byteCount));
		}
		for (int i=0; i<charCount; i++) {
			char c = source[0];
			destBytes[destIndex++] = (byte) (c >> 8 & 0xff);
			destBytes[destIndex++] = (byte) (c & 0xff);
		}
	}	

	public static void toBytes(char[] source, byte[] destBytes) {
		toBytes(source, 0, destBytes, 0, source.length);
	}

}
