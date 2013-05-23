package org.battelle.clodhopper.examples.project;

import org.apache.commons.math3.linear.RealMatrix;
import org.battelle.clodhopper.tuple.AbstractTupleList;

public class RealMatrixTupleList extends AbstractTupleList {

	private RealMatrix realMatrix;
	
	public RealMatrixTupleList(RealMatrix realMatrix) {
		super(realMatrix.getColumnDimension(), realMatrix.getRowDimension());
		this.realMatrix = realMatrix;
	}
	
	@Override
	public void setTuple(int n, double[] values) {
		checkTupleIndex(n);
		if (values.length < this.tupleLength) {
			throw new IllegalArgumentException(String.format("too few values: %d < %d", values.length, tupleLength));
		}
		for (int i=0; i<tupleLength; i++) {
			realMatrix.setEntry(n, i, values[i]);
		}
	}

	@Override
	public double[] getTuple(int n, double[] reuseBuffer) {
		checkTupleIndex(n);
		double[] result = (reuseBuffer != null && reuseBuffer.length >= tupleLength ? reuseBuffer : new double[tupleLength]);
		for (int i=0; i<tupleLength; i++) {
			result[i] = realMatrix.getEntry(n, i);
		}
		return result;
	}

	@Override
	public double getTupleValue(int n, int col) {
		return realMatrix.getEntry(n, col);
	}

}
