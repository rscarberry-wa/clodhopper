package org.battelle.clodhopper.examples.viz;

import org.battelle.clodhopper.examples.selection.SelectionModel;

public class PointSelector {

    public enum Flag {
        COORD,
        CLUSTER,
        SELECTED_COORD,
        COORDS_IN_RANGE
    };
    
    private IntegerPointList mCoordPoints;
    private IntegerPointList mClusterPoints;
    private IntegerKDTree mCoordTree;
    private IntegerKDTree mClusterTree;
    private SelectionModel mSelectionModel;
    
    public PointSelector(
            IntegerPointList coordPoints,
            IntegerPointList clusterPoints,
            SelectionModel selectionModel) {
        if (coordPoints == null || clusterPoints == null || selectionModel == null) {
            throw new NullPointerException();
        }
        mCoordPoints = coordPoints;
        mClusterPoints = clusterPoints;
        mSelectionModel = selectionModel;
        mCoordTree = IntegerKDTree.forPointList(mCoordPoints);
        mClusterTree = IntegerKDTree.forPointList(mClusterPoints);
    }
    
    public int[] findCoordinatesInside(IntegerHyperRect rect) {
    	return mCoordTree.inside(rect);
    }
    
    public int findCoordinate(int[] point, int range) {
        return _findCoordinate(point, range, mCoordTree);
    }
    
    public int[] findCoordinates(int[] point, int range) {
        return _find(point, range, mCoordTree);
    }
    
    public int findCluster(int[] point, int range) {
        return _findCoordinate(point, range, mClusterTree);
    }
    
    public int[] findClusters(int[] point, int range) {
        return _find(point, range, mClusterTree);
    }
    
    public int findSelectedCoordinate(int[] point, int range) {
        int rtn = -1;
        int[] coords = _find(point, range, mCoordTree);
        if (coords.length > 0) {
            for (int i=0; i<coords.length; i++) {
                int c = coords[i];
                if (mSelectionModel.isSelected(c)) {
                    rtn = c;
                    break;
                }
            }
        }
        return rtn;
    }    
    
    private static int _findCoordinate(int[] point, int range, IntegerKDTree tree) {
        int rtn = -1;
        int[] coords = _find(point, range, tree);
        if (coords.length > 0) {
            rtn = coords[0];
        }
        return rtn;
    }
    
    private static int[] _find(int[] point, int range, IntegerKDTree tree) {
        return tree.closeTo(point, range*range);
    }
    
}
