package org.battelle.clodhopper.tuple;

import java.util.*;
import java.util.Map.Entry;

/**
 * A simple implementation of <code>TupleListFactory</code> that maintains all tuple lists
 * in memory. This implementation has no persistence mechanisms.
 * 
 * @author R. Scarberry
 * @since 1.0
 *
 */
public class ArrayTupleListFactory implements TupleListFactory {

	private Map<String, TupleList> tupleListMap = new HashMap<String, TupleList> ();
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized TupleList createNewTupleList(String name, int tupleLength,
			int tupleCount) throws TupleListFactoryException {

		if (name == null) {
            throw new NullPointerException();
        }
        
        if (tupleListMap.containsKey(name)) {
            throw new TupleListFactoryException("tuples already exist for name " + name);
        }
        
        TupleList tuples = new ArrayTupleList(tupleLength, tupleCount);
        tupleListMap.put(name, tuples);
        
        return tuples;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized TupleList openExistingTupleList(String name)
			throws TupleListFactoryException {
        if (!tupleListMap.containsKey(name)) {
            throw new TupleListFactoryException("tuples do not exist for name " + name);
        }
        return tupleListMap.get(name);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized TupleList copyTupleList(String nameForCopy, TupleList original)
			throws TupleListFactoryException {
		
		final int tupleLength = original.getTupleLength();
		final int tupleCount = original.getTupleCount();
		
		TupleList copy = createNewTupleList(nameForCopy, tupleLength, tupleCount);
	
		double[] buffer = new double[tupleLength];
		for (int i=0; i<tupleCount; i++) {
			original.getTuple(i, buffer);
			copy.setTuple(i, buffer);
		}
		
		return copy;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized Set<String> tupleListNames() {
		return new TreeSet<String> (tupleListMap.keySet());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized boolean hasTuplesFor(String name) {
		return tupleListMap.containsKey(name);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void deleteTupleList(TupleList tuples)
			throws TupleListFactoryException {
		String name = nameAssociatedWithTuples(tuples);
        if (name == null) {
            throw new TupleListFactoryException("tuples not associated with this factory");
        }
        tupleListMap.remove(name);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void closeTupleList(TupleList tuples)
			throws TupleListFactoryException {
		deleteTupleList(tuples);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void closeAll() throws TupleListFactoryException {
		tupleListMap.clear();
	}

    private String nameAssociatedWithTuples(TupleList tuples) {
        
        Iterator<Entry<String, TupleList>> it = tupleListMap.entrySet().iterator();
        
        while(it.hasNext()) {
            Entry<String, TupleList> entry = it.next();
            if (entry.getValue() == tuples) {
                return entry.getKey();
            }
        }

        return null;
    }
}
