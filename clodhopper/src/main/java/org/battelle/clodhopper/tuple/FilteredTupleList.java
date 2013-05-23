package org.battelle.clodhopper.tuple;

public class FilteredTupleList extends AbstractTupleList {

	private int[] indexes;
	private TupleList filteredTuples;
	
	public FilteredTupleList(int[] indexes, TupleList tuples) {
		super(tuples.getTupleLength(), indexes.length);
		final int wrappedTupleCount = tuples.getTupleCount();
		for (int i=0; i<tupleCount; i++) {
			int n = indexes[i];
			if (n < 0 || n >= wrappedTupleCount) {
				throw new IndexOutOfBoundsException(
						String.format("filtered tuple index not in [0 - %d]: %d", wrappedTupleCount, n));
			}
		}
		this.indexes = indexes;
		this.filteredTuples = tuples;
	}
	
	@Override
	public void setTuple(int n, double[] values) {
		filteredTuples.setTuple(indexes[n], values);
	}

	@Override
	public double[] getTuple(int n, double[] reuseBuffer) {
		return filteredTuples.getTuple(indexes[n], reuseBuffer);
	}

	@Override
	public double getTupleValue(int n, int col) {
		return filteredTuples.getTupleValue(indexes[n], col);
	}

	public int getFilteredIndex(int n) {
		return indexes[n];
	}
}
