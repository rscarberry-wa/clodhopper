package org.battelle.clodhopper.examples.data;

import org.battelle.clodhopper.*;
import org.battelle.clodhopper.examples.project.*;
import org.battelle.clodhopper.examples.selection.*;
import org.battelle.clodhopper.tuple.TupleList;
import org.battelle.clodhopper.util.IntIterator;

import java.util.*;

public class ExampleData {

	private TupleList tuples;
	private List<Cluster> clusters;
	private Projection tupleProjection;
	private Projection clusterProjection;
	
	private int[] clusterLookupTable;
	
	private SelectionModel tupleSelections;
	private SelectionModel pendingTupleSelections;
	private SelectionModel clusterSelections;
	
	public ExampleData(
			TupleList tuples, 
			List<Cluster> clusters, 
			Projection tupleProjection, 
			Projection clusterProjection) {
		
		if (tuples == null || clusters == null || tupleProjection == null || clusterProjection == null) {
			throw new NullPointerException();
		}
		if (tuples.getTupleCount() != tupleProjection.getProjectionCount()) {
			throw new IllegalArgumentException(String.format("tuple count != projection count: %d != %d",
					tuples.getTupleCount(), tupleProjection.getProjectionCount()));
		}
		if (clusters.size() != clusterProjection.getProjectionCount()) {
			throw new IllegalArgumentException(String.format("cluster count != cluster projection count: %d != %d",
					clusters.size(), clusterProjection.getProjectionCount()));
		}
		
		final int tupleCount = tuples.getTupleCount();
		final int clusterCount = clusters.size();
		
		clusterLookupTable = new int[tupleCount];
		
		Arrays.fill(clusterLookupTable, -1);
		
		for (int c=0; c<clusterCount; c++) {
			Cluster cluster = clusters.get(c);
			IntIterator it = cluster.getMembers();
			while(it.hasNext()) {
				clusterLookupTable[it.getNext()] = c;
			}
		}
		
		tupleSelections = new BitSetSelectionModel(this, tupleCount);
		pendingTupleSelections = new BitSetSelectionModel(this, tupleCount);
		clusterSelections = new BitSetSelectionModel(this, clusterCount);
	
		this.tuples = tuples;
		this.clusters = clusters;
		this.tupleProjection = tupleProjection;
		this.clusterProjection = clusterProjection;
	}
	
	public TupleList getTuples() {
		return tuples;
	}
	
	public List<Cluster> getClusters() {
		return clusters;
	}
	
	public int getClusterForTuple(int n) {
		return clusterLookupTable[n];
	}
	
	public void setSelectionsToPending(Object requester) {
		tupleSelections.setSelected(requester, pendingTupleSelections.getSelected());
		pendingTupleSelections.clearSelected(requester);
	}
	
    public void addPendingSelections(Object requester) {
        tupleSelections.select(requester, pendingTupleSelections.getSelected());
		pendingTupleSelections.clearSelected(requester);
    }
    
    public void removePendingSelections(Object requester) {
        tupleSelections.unselect(requester, pendingTupleSelections.getSelected());
		pendingTupleSelections.clearSelected(requester);
    }
    
    public Projection getTupleProjection() {
        return tupleProjection;
    }
    
    public Projection getClusterProjection() {
        return clusterProjection;
    }
    
    public SelectionModel getTupleSelectionModel() {
        return tupleSelections;
    }

    public SelectionModel getPendingSelectionModel() {
        return pendingTupleSelections;
    }
    
    public SelectionModel getClusterSelectionModel() {
        return clusterSelections;
    }

}
