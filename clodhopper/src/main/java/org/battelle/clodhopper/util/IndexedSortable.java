package org.battelle.clodhopper.util;

public interface IndexedSortable {

	int getLength();
	
	int compare(int n1, int n2);
	
	int compareToMarkedValue(int n);
	
	void swap(int n1, int n2);
	
	void transferValue(int nSrc, int nDst);
	
	void markValue(int n);
	
	void setToMarkedValue(int n);
	
}
