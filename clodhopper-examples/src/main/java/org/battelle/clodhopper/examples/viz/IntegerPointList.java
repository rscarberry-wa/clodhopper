package org.battelle.clodhopper.examples.viz;

public class IntegerPointList {

	private int[][] mPointValues;
    private int mNumPoints, mNumDimensions;
    
    public IntegerPointList(
            int numPoints, 
            int numDimensions,
            int initialValue) {
        mPointValues = new int[numPoints][numDimensions];
        mNumPoints = numPoints;
        mNumDimensions = numDimensions;
        if (initialValue != 0) {
            for (int i=0; i<numPoints; i++) {
                for (int j=0; j<numDimensions; j++) {
                    mPointValues[i][j] = initialValue;
                }
            }
        }
    }

    public int getDimensionCount() {
        return mNumDimensions;
    }

    public int getPointCount() {
        return mNumPoints;
    }

    public int getPointValue(int ndx, int dim) {
        return mPointValues[ndx][dim];
    }

    public void setPointValue(int ndx, int dim, int value) {
        mPointValues[ndx][dim] = value;
    }

}
