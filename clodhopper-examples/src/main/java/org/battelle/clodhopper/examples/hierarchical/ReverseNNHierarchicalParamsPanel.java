package org.battelle.clodhopper.examples.hierarchical;

import org.battelle.clodhopper.Clusterer;
import org.battelle.clodhopper.examples.ui.ParamsPanelException;
import org.battelle.clodhopper.hierarchical.HierarchicalParams;
import org.battelle.clodhopper.hierarchical.ReverseNNHierarchicalClusterer;
import org.battelle.clodhopper.tuple.TupleList;

public class ReverseNNHierarchicalParamsPanel extends HierarchicalParamsPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ReverseNNHierarchicalParamsPanel(HierarchicalParams params) {
		super(params);
	}
	
	public ReverseNNHierarchicalParamsPanel() {
		this(null);
	}
	
	@Override
	public Clusterer getNewClusterer(TupleList tuples)
			throws ParamsPanelException {
		HierarchicalParams params = getValidatedParams(tuples.getTupleCount());
		return new ReverseNNHierarchicalClusterer(tuples, params);
	}

}
