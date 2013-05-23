package org.battelle.clodhopper.util;

public final class Sorting {

	private Sorting() {}
	
	public static void quickSort(IndexedSortable sortable) {
		quickSort(sortable, 0, sortable.getLength());
	}
	
	public static void quickSort(IndexedSortable sortable, int offset, int len) {
		
		final int limit = offset + len;
		
    	// Use an insertion sort for really small arrays.
    	if (len < 7) {
    		for (int i=offset + 1; i<limit; i++) {
    			sortable.markValue(i);
    			int j=i;
    			while(j > 0 && sortable.compareToMarkedValue(j-1) > 0) {	
    				sortable.transferValue(j-1, j);    				
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
    		int eighthLen = len/8;
    		// Index of median value of the 3 on the left.
    		left = medianOf3(sortable, left, left + eighthLen, left + 2*eighthLen);
    		// In the middle
    		m = medianOf3(sortable, m-eighthLen, m, m+eighthLen);
    		// On the right
    		right = medianOf3(sortable, right-2*eighthLen, right-eighthLen, right);
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
    		
    		while(c >= b && cmpValue >= 0) {
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
    	vecSwap(sortable, offset, b-1, s);
    	
    	s = Math.min(d-c, limit-d-1);
    	vecSwap(sortable, b, limit-s, s);
    	
    	s = b - a;
    	if (s > 1) {
    		quickSort(sortable, offset, s);
    	}
    	
    	s = d - c;
    	if (s > 1) {
    		quickSort(sortable, limit - s, s);
    	}
	}
	
	public static void quickSort(int[] arr, IntComparator comp) {
		quickSort(arr, 0, arr.length, comp);
	}
	
	public static void quickSort(int[] arr, int offset, int len, IntComparator comp) {
		final int limit = offset + len;
		
    	// Use an insertion sort for really small arrays.
    	if (len < 7) {
    		for (int i=offset + 1; i<limit; i++) {
    			int v = arr[i];
    			int j=i;
    			while(j > 0 && comp.compare(arr[j-1], v) > 0) {
    				arr[j] = arr[j-1];
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
    		int eighthLen = len/8;
    		// Index of median value of the 3 on the left.
    		left = medianOf3(arr, left, left + eighthLen, left + 2*eighthLen, comp);
    		// In the middle
    		m = medianOf3(arr, m-eighthLen, m, m+eighthLen, comp);
    		// On the right
    		right = medianOf3(arr, right-2*eighthLen, right-eighthLen, right, comp);
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
    		
    		while(c >= b && cmpValue >= 0) {
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
    	vecSwap(arr, offset, b-s, s);
    	
    	s = Math.min(d-c, limit-d-1);
    	vecSwap(arr, b, limit-s, s);
    	
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
		for (int i=0; i<len; i++, offset1++, offset2++) {
			swap(arr, offset1, offset2);
		}
	}
	
	private static void vecSwap(IndexedSortable sortable, int offset1, int offset2, int len) {
		for (int i=0; i<len; i++, offset1++, offset2++) {
			sortable.swap(offset1, offset2);
		}
	}
	
    /**
     * Given three indexes into the specified array, this method returns the index of the
     * middle element as determined by the supplied comparator.  If the comparator is null,
     * the middle index is determined by the <tt>compareTo</tt> methods of the objects, assuming
     * they implement comparable.
     * 
     * @param a
     * @param x
     * @param y
     * @param z
     * @param c
     * @return
     */
    public static int medianOf3(int[] a, int x, int y, int z, IntComparator c) {
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
    	if (c.compare(a[y], a[z]) > 1) {
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
    
    public static void parallelSort(double[] dValues, int[] iValues) {
    	quickSort(new DoubleIntIndexedSortable(dValues, iValues));
    }
    
    public static class DoubleIntIndexedSortable implements IndexedSortable {
    	
    	private double[] dValues;
    	private int[] iValues;
    	
    	private double markedDValue;
    	private int markedIValue;
    	private int markedIndex = -1;
    	
    	public DoubleIntIndexedSortable(double[] dValues, int[] iValues) {
    		if (dValues.length != iValues.length) {
    			throw new IllegalArgumentException("length mismatch");
    		}
    		this.dValues = dValues;
    		this.iValues = iValues;
    	}
    	
    	@Override
    	public int getLength() {
    		return dValues.length;
    	}
    	
    	@Override
    	public int compare(int n1, int n2) {
    		double v1 = dValues[n1];
    		double v2 = dValues[n2];
    		return v1 < v2 ? -1 : v1 > v2 ? 1 : 0;
    	}
    	
    	@Override
    	public int compareToMarkedValue(int n) {
    		if (markedIndex == -1) {
    			throw new IllegalStateException("no value has been marked");
    		}
    		double v = dValues[n];
    		return v < markedDValue ? -1 : v > markedDValue ? 1 : 0;
    	}
    		
    	@Override
    	public void swap(int n1, int n2) {
    		if (n1 != n2) {
    			double dTmp = dValues[n1];
    			int iTmp = iValues[n1];
    			dValues[n1] = dValues[n2];
    			iValues[n1] = iValues[n2];
    			dValues[n2] = dTmp;
    			iValues[n2] = iTmp;
    		}
    	}
    	
    	@Override
    	public void transferValue(int nSrc, int nDst) {
    		dValues[nDst] = dValues[nSrc];
    		iValues[nDst] = iValues[nSrc];
    	}
    	
    	@Override
    	public void markValue(int n) {
    		markedDValue = dValues[n];
    		markedIValue = iValues[n];
    		markedIndex = n;
    	}
    	
    	@Override
    	public void setToMarkedValue(int n) {
    		if (markedIndex == -1) {
    			throw new IllegalStateException("no value has been marked");
    		}
    		dValues[n] = markedDValue;
    		iValues[n] = markedIValue;
    	}
    }
}
