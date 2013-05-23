package org.battelle.clodhopper.util;

public interface IntIterator extends Cloneable {

	void gotoFirst();
	
	void gotoLast();
	
	int getFirst();
	
	int getLast();
	
	boolean hasNext();
	
	boolean hasPrev();
	
	int getNext();
	
	int getPrev();
	
	int getSize();
	
	int[] toArray();
	
	IntIterator clone();
	
}
