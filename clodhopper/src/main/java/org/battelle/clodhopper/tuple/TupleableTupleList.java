package org.battelle.clodhopper.tuple;

import java.util.List;

/**
 * A wrapper to make a <tt>java.util.List&lt;Tupleable&gt;</tt> conform to the
 * TupleList interface.  Instances of this class are read-only.  The tuple signature of the
 * contained object cannot be modified.
 * 
 * @author R.Scarberry
 * @since 1.0
 *
 */
public class TupleableTupleList extends AbstractTupleList {
	
	private List<Tupleable> tupleables;
	
	/**
	 * Constructor
	 * 
	 * @param tupleables a list containing Tupleable objects.
	 * 
	 * @throws NullPointerException if the list is null.
	 */
	public TupleableTupleList(List<Tupleable> tupleables) {
		super(0, 0);
		this.tupleCount = tupleables.size();
		if (this.tupleCount > 0) {
			this.tupleLength = tupleables.get(0).getTupleSignature().length;
		}
		this.tupleables = tupleables;
	}

	@Override
	/**
	 * This method is unsupported for this class.
	 * 
	 * @throws UnsupportedOperationException
	 */
	public void setTuple(int n, double[] values) {
		throw new UnsupportedOperationException();
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public double[] getTuple(int n, double[] reuseBuffer) {
		checkTupleIndex(n);
		double[] result = reuseBuffer != null && reuseBuffer.length >= tupleLength ? reuseBuffer :
			new double[tupleLength];
		double[] signature = this.tupleables.get(n).getTupleSignature();
		System.arraycopy(signature, 0, result, 0, tupleLength);
		return result;
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public double getTupleValue(int n, int col) {
		checkTupleIndex(n);
		checkColumnIndex(col);
		return this.tupleables.get(n).getTupleSignature()[col];
	}

}
