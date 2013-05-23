package org.battelle.clodhopper.util;

import java.util.NoSuchElementException;

public class ArrayIntIterator implements IntIterator {

	private int[] values;
	private int cursor;
	
	public ArrayIntIterator(int[] values) {
		if (values == null) {
			throw new NullPointerException();
		}
		this.values = values;
	}
	
	
	public void gotoFirst() {
		cursor = 0;
	}
	
	public void gotoLast() {
		cursor = values.length;
	}
	
	public int getFirst() {
		gotoFirst();
		return getNext();
	}
	
	public int getLast() {
		gotoLast();
		return getPrev();
	}
	
	public boolean hasNext() {
		return cursor < values.length;
	}
	
	public boolean hasPrev() {
		return cursor > 0;
	}

	public int getNext() {
		if (hasNext()) {
			return values[cursor++];
		}
		throw new NoSuchElementException();
	}
	
	public int getPrev() {
		if (hasPrev()) {
			return values[--cursor];
		}
		throw new NoSuchElementException();
	}

	public int getSize() {
		return values.length;
	}
	
	public int[] toArray() {
		return (int[]) values.clone();
	}
	
	public IntIterator clone() {
		try {
			ArrayIntIterator clone = (ArrayIntIterator) super.clone();
			clone.values = this.toArray();
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new InternalError();
		}
	}
}
