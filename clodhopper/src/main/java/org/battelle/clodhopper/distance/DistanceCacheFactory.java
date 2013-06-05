package org.battelle.clodhopper.distance;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

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
 * DistanceCacheFactory.java
 *
 *===================================================================*/

/**
 * A class containing static factory methods for creating instances of <code>DistanceCache</code>.
 * 
 * @author R. Scarberry
 *
 */
public class DistanceCacheFactory {

	private DistanceCacheFactory() {}
	
	/**
	 * Creates a new distance cache for holding pairwise distances.  The number of distance
	 * for a given tupleCount is <code>tupleCount*(tupleCount - 1)/2</code>.  Since each distance
	 * is stored as an 8 byte double, the space required either in memory or on disk is <code>4*tupleCount(tupleCount - 1)</code>.
	 * 
	 * @param tupleCount the number of tuples for which distances must be maintained.
	 * @param memoryThreshold the memory threshold determining whether or not to create a 
	 *   cache storing all the distances in memory. 
	 * @param fileThreshold the threshold determining whether or not the distances can be stored in a disk file.
	 * @param cacheFile the file to use for the cache if a disk file is used for the cache.
	 * 
	 * @return an instance of <code>DistanceCache</code> or null if the amount of space required exceeds both
	 *   the memory and file thresholds.
	 *
	 * @throws IOException
	 */
	public static DistanceCache newDistanceCache(
		int tupleCount,
		long memoryThreshold, 
		long fileThreshold, 
		File cacheFile) throws IOException {
		
		long size = distanceCacheSize(tupleCount);
		if (size <= memoryThreshold) {
			return new RAMDistanceCache(tupleCount);
		} else if (size <= fileThreshold) {
			return new FileDistanceCache(tupleCount, cacheFile);
		}
		
		return null;
	}
	
	/**
	 * Wraps the provided distance cache to hide write operations.
	 * 
	 * results in <code>UnsupportedOperationException</code>s.
	 * 
	 * @param cache
	 * 
	 * @return
	 */
	public static ReadOnlyDistanceCache asReadOnly(DistanceCache cache) {
		if (cache instanceof ReadOnlyDistanceCache) {
			return (ReadOnlyDistanceCache) cache;
		} else {
			return new ReadOnlyWrapper(cache);
		}
	}
	
	/**
	 * Returns the amount of file space required to store all the distances for 
	 * the specified number of tuples. 
	 * 
	 * @param tupleCount
	 * @return
	 */
	public static long distanceCacheSize(int tupleCount) {
		return 4L + 4L*tupleCount*((long)tupleCount - 1);
	}
	
	/**
	 * Returns the maximum number of tuples whose pairwise distances can fit within the
	 * specified number of bytes.
	 * 
	 * @param byteThreshold
	 * @return
	 */
	public static int tupleLimit(long byteThreshold) {
		return (int) ((Math.sqrt(16.0 + 16.0 * (byteThreshold - 4L)) + 4.0)/8.0);
	}
	
	/**
	 * Returns a 2-element array containing the indexes of the tuples whose distance is
	 * stored at the specified position in the distance case.
	 * 
	 * @param pos
	 * @param cache
	 * @return
	 */
	public static int[] getIndicesForDistance(long pos, ReadOnlyDistanceCache cache) {

	    if (pos < 0 || pos >= cache.getNumDistances()) {
	        throw new IndexOutOfBoundsException("pos not in [0 - (" 
	                + cache.getNumDistances() + " - 1)]: " + pos);
	    }

	    int coordCount = cache.getNumIndices();       
	    double b = 2.0*coordCount - 1;

	    int i = (int)(-(Math.sqrt(b*b - 8.0*pos) - b)/2.0);
	    int j = i+1;

	    j += (int) (pos - cache.distancePos(i, j));

	    return new int[] { i, j };
	}
	
	/**
	 * Saves a <code>DistanceCache</code> to a disk file.
	 * 
	 * @param cache
	 * @param f
	 * @throws IOException
	 */
	public static void save(DistanceCache cache, File f) throws IOException {

		if (cache instanceof FileDistanceCache) {

			FileDistanceCache fileCache = (FileDistanceCache) cache;
			if (fileCache.isOpen()) {
				fileCache.closeFile();
			}
			
			File src = fileCache.getFile();
			FileInputStream fis = null;
			FileOutputStream fos = null;

			try {
				
				// The first version of this used the nio method of
				// getting the source FileChannel and the destination
				// FileChannel and using the FileChannel method 
				// transferTo.  But this failed on large files ( > 2GB), 
				// so I changed it to the more traditional copy method.
				
				long flen = src.length();
				fis = new FileInputStream(src);
				
				// Found this buffer size to give the speediest 
				// performance on my Windows XP laptop.  May want
				// to make the buffer size a static class member
				// initialized to an optimum value for the OS.
				byte[] ioBuffer = new byte[16*1024];
								
				fos = new FileOutputStream(f);				
				
				long transferred = 0L;
				while (transferred < flen) {					
					int bytesRead = fis.read(ioBuffer);
					if (bytesRead > 0) {
						fos.write(ioBuffer, 0, bytesRead);
					}
					transferred += bytesRead;
				}
				
			} finally {
				
				if (fos != null) {
					// Don't trap IOException, because if this file
					// doesn't close successfully, the cache probably didn't
					// save successfully.
					fos.close();
				}
				
				if (fis != null) {
					try {
						fis.close();
					} catch (IOException ioe) {
						// Ignore, since this probably didn't hose the save.
					}
				}
				
			}
			
		} else { // Some other kind, probably a RAMDistanceCache
			
			DataOutputStream dos = null;
			
			try {

				dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f)));
				dos.writeInt(cache.getNumIndices());
				long numDistances = cache.getNumDistances();
				
				for (long d=0L; d<numDistances; d++) {
					dos.writeDouble(cache.getDistance(d));
				}
				
			} finally {
				
				if (dos != null) {
					// Don't trap IOException, because if this file
					// doesn't close successfully, the cache probably didn't
					// save successfully.
					dos.close();
				}
			}
			
		}
	}
	
	/**
	 * Loads a distance caches from a disk file if the number of distances in the file
	 * fits within the specified thresholds.
	 * 
	 * @param f
	 * @param memoryThreshold
	 * @param fileThreshold
	 * @return
	 * @throws IOException
	 */
	public static DistanceCache read(File f, 
			long memoryThreshold, 
			long fileThreshold) throws IOException {

		DistanceCache cache = null;
		
		DataInputStream dis = null;
		
		try {
			
			long flen = f.length();

			dis = new DataInputStream(new BufferedInputStream(new FileInputStream(f)));
			
			int numIndices = dis.readInt();
			long expectedLen = distanceCacheSize(numIndices);
			
			if (numIndices < 0 || flen != expectedLen) {
				throw new IOException("invalid distance cache file");
			}
			
			if (numIndices <= RAMDistanceCache.MAX_INDEX_COUNT && flen <= memoryThreshold) {
				
				int numDistances = numIndices*(numIndices - 1)/2;
				
				double[] distances = new double[numDistances];
				for (int i=0; i<numDistances; i++) {
					distances[i] = dis.readDouble();
				}
				
				cache = new RAMDistanceCache(numIndices, distances);
				
			} else if (flen <= fileThreshold) {
				
				try {
					dis.close();
				} catch (IOException x) {	
				}
				
				cache = new FileDistanceCache(f);
				
			} else {
				
				throw new IOException("cache file is too large: " + flen + " > " + fileThreshold);
				
			}
			
			
		} finally {
		
			if (dis != null) {
				try {
					dis.close();
				} catch (IOException ioe) {
					
				}
			}
			
		}

		return cache;
	}
	
	// Wrapper that hides the methods for setting distances.
	//
	private static class ReadOnlyWrapper implements ReadOnlyDistanceCache {
		
		private DistanceCache mCache;
		
		ReadOnlyWrapper(DistanceCache cache) {
			mCache = cache;
		}
		
		public int getNumIndices() {
			return mCache.getNumIndices();
		}
		
		public double getDistance(int index1, int index2) throws IOException {
			return mCache.getDistance(index1, index2);
		}
		
		public double[] getDistances(int[] indices1, int[] indices2, double[] distances) 
		throws IOException {
			return mCache.getDistances(indices1, indices2, distances);
		}

		public long getNumDistances() {
			return mCache.getNumDistances();
		}
		
		public double getDistance(long n) throws IOException {
			return mCache.getDistance(n);
		}

		public long distancePos(int index1, int index2) {
			return mCache.distancePos(index1, index2);
		}
		
	}
	
}
