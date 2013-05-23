package org.battelle.clodhopper.examples.ui;

import org.battelle.clodhopper.Clusterer;
import org.battelle.clodhopper.tuple.TupleList;

public interface ParamsPanel {

	Clusterer getNewClusterer(TupleList tuples) throws ParamsPanelException;
	
	boolean isEnabled();
	
	void setEnabled(boolean b);
	
	void setTupleCount(int tupleCount);
	
}
