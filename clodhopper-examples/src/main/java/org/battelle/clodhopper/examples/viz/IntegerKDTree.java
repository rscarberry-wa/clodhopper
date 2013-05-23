package org.battelle.clodhopper.examples.viz;

import java.util.*;
import gnu.trove.list.array.*;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class IntegerKDTree {

    private IntegerPointList mPointList;
    private int mMaxNdx;

    private int[] mNodes;
    private int[] mLefts;
    private int[] mRights;
    private boolean[] mDeleted;

    private int mCount;

    public IntegerKDTree(IntegerPointList pointList) {
        mPointList = pointList;
        mMaxNdx = mPointList.getPointCount() - 1;
        ensureCapacity(100);
    }

    public IntegerPointList getPointList() {
        return mPointList;
    }

    public static IntegerKDTree forPointList(IntegerPointList pointList) {
        IntegerKDTree kd = new IntegerKDTree(pointList);
        int numCoords = pointList.getPointCount();
        for (int i=0; i<numCoords; i++) {
            kd.insert(i);
        }
        return kd;
    }

    private void ensureCapacity(int minCap) {
        int curCap = currentCapacity();
        if (curCap < minCap) {
            int newCap = Math.max(curCap*2, minCap);
            int[] newNodes = new int[newCap];
            int[] newLefts = new int[newCap];
            int[] newRights = new int[newCap];
            boolean[] newDeleted = new boolean[newCap];
            if (curCap > 0) {
                System.arraycopy(mNodes, 0, newNodes, 0, curCap);
                System.arraycopy(mLefts, 0, newLefts, 0, curCap);
                System.arraycopy(mRights, 0, newRights, 0, curCap);
                System.arraycopy(mDeleted, 0, newDeleted, 0, curCap);
            }
            Arrays.fill(newNodes, curCap, newCap, -1);
            Arrays.fill(newLefts, curCap, newCap, -1);
            Arrays.fill(newRights, curCap, newCap, -1);
            Arrays.fill(newDeleted, curCap, newCap, false);
            mNodes = newNodes;
            mLefts = newLefts;
            mRights = newRights;
            mDeleted = newDeleted;
        }
    }

    private int currentCapacity() {
        return mNodes != null ? mNodes.length : 0;
    }

    private void newNodeOnLeft(int parentIndex, int ndx) {
        int m = mCount;
        mCount++;
        ensureCapacity(mCount);
        mNodes[m] = ndx;
        mLefts[parentIndex] = m;
    }

    private void newNodeOnRight(int parentIndex, int ndx) {
        int m = mCount;
        mCount++;
        ensureCapacity(mCount);
        mNodes[m] = ndx;
        mRights[parentIndex] = m;
    }

    private void checkNdx(int ndx) {
        if (ndx < 0 || ndx > mMaxNdx) {
            throw new IndexOutOfBoundsException("out of bounds: " + ndx);
        }
    }

    public void insert(int ndx) {

        checkNdx(ndx);

        // First thing to be inserted -- it becomes the root.
        if (mCount == 0) {
                ensureCapacity(1);
                mNodes[0] = ndx;
                mCount++;
            return;
        }

        int n = 0;
        int level = 0;
        int dim = mPointList.getDimensionCount();

        while(true) {
          int curNode = mNodes[n];
          if (curNode == ndx) {
              if (mDeleted[n]) {
                  mDeleted[n] = false;
                  return;
              }
              throw new IllegalArgumentException("duplicate insertion: " + ndx);
          } else {
              int d = level%dim;
              int coord = mPointList.getPointValue(ndx, d);
              int nodeCoord = mPointList.getPointValue(curNode, d);
              if (coord > nodeCoord) {
                if (mRights[n] < 0) {
                    newNodeOnRight(n, ndx);
                    return;
                } else {
                    n = mRights[n];
                }
              } else {
                if (mLefts[n] < 0) {
                    newNodeOnLeft(n, ndx);
                    return;
                } else {
                    n = mLefts[n];
                }
              }
          }
          level++;
        } // while

    }

    public void delete(int ndx) {

        checkNdx(ndx);
        
        if (mCount == 0) {
            return;
        }

        int n = 0;
        int level = 0;
        int dim = mPointList.getDimensionCount();

        while(true) {
          int curNode = mNodes[n];
          if (curNode == ndx) {
              mDeleted[n] = true;
              return;
          } else {
              int d = level%dim;
              int coord = mPointList.getPointValue(ndx, d);
              int nodeCoord = mPointList.getPointValue(curNode, d);
              if (coord > nodeCoord) {
                if (mRights[n] < 0) {
                    return;
                } else {
                    n = mRights[n];
                }
              } else {
                if (mLefts[n] < 0) {
                    return;
                } else {
                    n = mLefts[n];
                }
              }
          }
          level++;
        } // while
    }

    public int search(int[] coords) {

        int n = 0;
        int level = 0;
        int dim = mPointList.getDimensionCount();
        int[] nodeCoords = new int[mPointList.getDimensionCount()];

        while(true) {
          int curNode = (n >= 0 && n < mCount) ? mNodes[n] : -1;
          if (curNode < 0) return -1; // Not found.
          getPointValues(curNode, nodeCoords, mPointList);
          if (!mDeleted[n] && coordsEqual(coords, nodeCoords)) {
              return curNode;
          }
          int d = level%dim;
          if (coords[d] > nodeCoords[d]) {
              n = mRights[n];
          } else {
              n = mLefts[n];
          }
          level++;
        } // while

    }
    
    private static void getPointValues (int ndx, int[] values, IntegerPointList pointList) {
        int len = values.length;
        for (int i=0; i<len; i++) {
            values[i] = pointList.getPointValue(ndx, i);
        }
    }

    public int nearestNeighbor(int ndx) {
        
        checkNdx(ndx);

        int[] coords = new int[mPointList.getDimensionCount()];
        getPointValues(ndx, coords, mPointList);
        
        int[] nn = nearest(coords, 2);
        
        return nn[0] == ndx ? nn[1] : nn[0];
    }
    
    public int nearest(int[] coords) {
    	int[] nn = nearest(coords, 1);
    	return nn[0];
    }

    public int[] nearest(int ndx, int num) {

        if (num < 0 || num > mCount) {
            throw new IllegalArgumentException(
              "number of neighbors negative or greater than number of nodes: "
              + num);
        }


        int dim = mPointList.getDimensionCount();
        int[] coords = new int[dim];
        
        getPointValues(ndx, coords, mPointList);

        DistanceQueue dq = new DistanceQueue(num);

        rnearest(0, coords, num,
                 IntegerHyperRect.infiniteHyperRect(dim),
                 Integer.MAX_VALUE,
                 0, dim, dq, ndx);

        int[] ids = new int[num];
        for (int i=num-1; i>=0; i--) {
            ids[i] = dq.remove();
        }

        return ids;
    }
    
    public int[] nearest(int[] coords, int num) {

        if (num < 0 || num > mCount) {
            throw new IllegalArgumentException(
              "number of neighbors negative or greater than number of nodes: "
              + num);
        }

        int dim = coords.length;

        DistanceQueue dq = new DistanceQueue(num);

        rnearest(0, coords, num,
                 IntegerHyperRect.infiniteHyperRect(dim),
                 Integer.MAX_VALUE,
                 0, dim, dq, -1);

        int[] ids = new int[num];
        for (int i=num-1; i>=0; i--) {
            ids[i] = dq.remove();
        }

        return ids;
    }

    public int[] closeTo(int ndx, int maxDistSquared) {
        
        int dim = mPointList.getDimensionCount();
        int[] coords = new int[dim];
        getPointValues(ndx, coords, mPointList);

        DistanceQueue dq = new DistanceQueue();

        rcloseTo(0, coords,
                 IntegerHyperRect.infiniteHyperRect(dim),
                 maxDistSquared,
                 0, dim, dq, ndx);

        int sz = dq.size();
        int[] ids = new int[sz];
        for (int i=sz-1; i>=0; i--) {
            ids[i] = dq.remove();
        }

        return ids;
    }
    
    public int[] closeTo(int[] coords, int maxDistSquared) {

        int dim = coords.length;

        DistanceQueue dq = new DistanceQueue();

        rcloseTo(0, coords,
                 IntegerHyperRect.infiniteHyperRect(dim),
                 maxDistSquared,
                 0, dim, dq, -1);

        int sz = dq.size();
        int[] ids = new int[sz];
        for (int i=sz-1; i>=0; i--) {
            ids[i] = dq.remove();
        }

        return ids;
    }

    public int[] inside(IntegerHyperRect rect) {
    	final int dim = mPointList.getDimensionCount();
    	if (rect.getDimension() != dim) {
    		throw new IllegalArgumentException("dimension mismatch: " + rect.getDimension() + " != " + dim);
    	}
    	int[] midPoint = new int[dim];
    	int[] maxDiffs = new int[dim];
    	for (int i=0; i<dim; i++) {
    		int min = rect.getMinCornerCoord(i);
    		int max = rect.getMaxCornerCoord(i);
    		int maxDiff = (max - min)/2;
    		midPoint[i] = min + maxDiff;
    		maxDiffs[i] = maxDiff;
    	}
    	TIntArrayList intList = new TIntArrayList();
    	rcloseTo(0, midPoint, IntegerHyperRect.infiniteHyperRect(dim), maxDiffs, 0, dim, intList, -1);
    	return intList.toArray();
    }
    
    private void rnearest(int curNodeNdx, int[] targetCoords, int num,
                          IntegerHyperRect hr,
                          int maxDistSquared, int level, int dim,
                          DistanceQueue dq,
                          int ndxToExclude) {

        int curNode = mNodes[curNodeNdx];
        if (curNode < 0) {
            return;
        }

        // Component of coords to use for splitting.
        int s = level%dim;

        int[]curCoords = new int[mPointList.getDimensionCount()];
        getPointValues(curNode, curCoords, mPointList);

        int targetCoord = targetCoords[s];
        int curCoord = curCoords[s];

        boolean targetInLeft = targetCoord < curCoord;

        int nearerNodeNdx = -1, furtherNodeNdx = -1;
        if (targetInLeft) {
            nearerNodeNdx = mLefts[curNodeNdx];
            furtherNodeNdx = mRights[curNodeNdx];
        } else {
            nearerNodeNdx = mRights[curNodeNdx];
            furtherNodeNdx = mLefts[curNodeNdx];
        }

        if (nearerNodeNdx >= 0) {
            int oldCoord = 0;
            if (targetInLeft) {
                oldCoord = hr.getMaxCornerCoord(s);
                hr.setMaxCornerCoord(s, curCoords[s]);
            } else {
                oldCoord = hr.getMinCornerCoord(s);
                hr.setMinCornerCoord(s, curCoords[s]);
            }
            rnearest(nearerNodeNdx, targetCoords, num, hr, maxDistSquared, level+1, dim, dq, ndxToExclude);
            if (targetInLeft) {
                hr.setMaxCornerCoord(s, oldCoord);
            } else {
                hr.setMinCornerCoord(s, oldCoord);
            }
            if (dq.size() == num) {
                maxDistSquared = (int) dq.getMaxDistance();
            }
        }

        if (furtherNodeNdx >= 0) {
            int oldCoord = 0;
            if (targetInLeft) {
                oldCoord = hr.getMinCornerCoord(s);
                hr.setMinCornerCoord(s, curCoords[s]);
            } else {
                oldCoord = hr.getMaxCornerCoord(s);
                hr.setMaxCornerCoord(s, curCoords[s]);
            }
            if (euclideanDistSquared(hr.closestPoint(targetCoords), targetCoords) < maxDistSquared) {
                rnearest(furtherNodeNdx, targetCoords, num, hr, maxDistSquared, level+1, dim, dq, ndxToExclude);
            }
            if (targetInLeft) {
                hr.setMinCornerCoord(s, oldCoord);
            } else {
                hr.setMaxCornerCoord(s, oldCoord);
            }
            if (dq.size() == num) {
                maxDistSquared = (int) dq.getMaxDistance();
            }
        }

        if (!mDeleted[curNodeNdx] && curNodeNdx != ndxToExclude) {
            int curToTarget = euclideanDistSquared(curCoords, targetCoords);
            if (curToTarget < maxDistSquared) {
                if (dq.size() == num) {
                    dq.remove();
                }
                dq.add(curNode, curToTarget);
            }
        }
    }

    private void rcloseTo(int curNodeNdx, int[] targetCoords,
            IntegerHyperRect hr,
            int maxDistSquared, int level, int dim,
            DistanceQueue dq, int ndxToExclude) {

        int curNode = mNodes[curNodeNdx];
        if (curNode < 0) {
            return;
        }

        // Component of coords to use for splitting.
        int s = level%dim;

        int[] curCoords = new int[mPointList.getDimensionCount()];
        getPointValues(curNode, curCoords, mPointList);
        
        int targetCoord = targetCoords[s];
        int curCoord = curCoords[s];

        boolean targetInLeft = targetCoord <= curCoord;

        int nearerNodeNdx = -1, furtherNodeNdx = -1;
        if (targetInLeft) {
                nearerNodeNdx = mLefts[curNodeNdx];
                furtherNodeNdx = mRights[curNodeNdx];
        } else {
                nearerNodeNdx = mRights[curNodeNdx];
                furtherNodeNdx = mLefts[curNodeNdx];
        }

        if (nearerNodeNdx >= 0) {
                int oldCoord = 0;
                if (targetInLeft) {
                        oldCoord = hr.getMaxCornerCoord(s);
                        hr.setMaxCornerCoord(s, curCoords[s]);
                } else {
                        oldCoord = hr.getMinCornerCoord(s);
                        hr.setMinCornerCoord(s, curCoords[s]);
                }
                rcloseTo(nearerNodeNdx, targetCoords, hr, maxDistSquared, level+1, dim, dq, ndxToExclude);
                if (targetInLeft) {
                        hr.setMaxCornerCoord(s, oldCoord);
                } else {
                        hr.setMinCornerCoord(s, oldCoord);
                }
        }

        if (furtherNodeNdx >= 0) {
                int oldCoord = 0;
                if (targetInLeft) {
                        oldCoord = hr.getMinCornerCoord(s);
                        hr.setMinCornerCoord(s, curCoords[s]);
                } else {
                        oldCoord = hr.getMaxCornerCoord(s);
                        hr.setMaxCornerCoord(s, curCoords[s]);
                }
                if (euclideanDistSquared(hr.closestPoint(targetCoords), targetCoords) <= maxDistSquared) {
                        rcloseTo(furtherNodeNdx, targetCoords, hr, maxDistSquared, level+1, dim, dq, ndxToExclude);
                }
                if (targetInLeft) {
                        hr.setMinCornerCoord(s, oldCoord);
                } else {
                        hr.setMaxCornerCoord(s, oldCoord);
                }
        }

        if (!mDeleted[curNodeNdx] && curNodeNdx != ndxToExclude) {
                int curToTarget = euclideanDistSquared(curCoords, targetCoords);
                if (curToTarget <= maxDistSquared) {
                        dq.add(curNode, curToTarget);
                }
        }
    }

    private void rcloseTo(int curNodeNdx, int[] targetCoords,
            IntegerHyperRect hr,
            int[] maxDiffs, int level, int dim,
            TIntArrayList intList, int ndxToExclude) {

    	int curNode = mNodes[curNodeNdx];
    	if (curNode < 0) {
    		return;
    	}

    	// Component of coords to use for splitting.
    	int s = level%dim;

    	int[] curCoords = new int[dim];
    	for (int i=0; i<dim; i++) {
    		curCoords[i] = mPointList.getPointValue(curNode, i);
    	}

    	int targetCoord = targetCoords[s];
    	int curCoord = curCoords[s];

    	boolean targetInLeft = targetCoord <= curCoord;

    	int nearerNodeNdx = -1, furtherNodeNdx = -1;
    	if (targetInLeft) {
    		nearerNodeNdx = mLefts[curNodeNdx];
    		furtherNodeNdx = mRights[curNodeNdx];
    	} else {
    		nearerNodeNdx = mRights[curNodeNdx];
    		furtherNodeNdx = mLefts[curNodeNdx];
    	}

    	if (nearerNodeNdx >= 0) {
    		int oldCoord = 0;
    		if (targetInLeft) {
    			oldCoord = hr.getMaxCornerCoord(s);
    			hr.setMaxCornerCoord(s, curCoords[s]);
    		} else {
    			oldCoord = hr.getMinCornerCoord(s);
    			hr.setMinCornerCoord(s, curCoords[s]);
    		}
    		rcloseTo(nearerNodeNdx, targetCoords, hr, maxDiffs, level+1, dim, intList, ndxToExclude);
    		if (targetInLeft) {
    			hr.setMaxCornerCoord(s, oldCoord);
    		} else {
    			hr.setMinCornerCoord(s, oldCoord);
    		}
    	}

    	if (furtherNodeNdx >= 0) {
    		int oldCoord = 0;
    		if (targetInLeft) {
    			oldCoord = hr.getMinCornerCoord(s);
    			hr.setMinCornerCoord(s, curCoords[s]);
    		} else {
    			oldCoord = hr.getMaxCornerCoord(s);
    			hr.setMaxCornerCoord(s, curCoords[s]);
    		}
    		
    		if (diffsWithinBoundaries(hr.closestPoint(targetCoords), targetCoords, maxDiffs)) {
    			rcloseTo(furtherNodeNdx, targetCoords, hr, maxDiffs, level+1, dim, intList, ndxToExclude);
    		}
    		
    		if (targetInLeft) {
    			hr.setMinCornerCoord(s, oldCoord);
    		} else {
    			hr.setMaxCornerCoord(s, oldCoord);
    		}
    	}

    	if (!mDeleted[curNodeNdx] && curNodeNdx != ndxToExclude) {
    		if (diffsWithinBoundaries(curCoords, targetCoords, maxDiffs)) {
    			intList.add(curNode);
    		}
    	}
    }

    public static boolean coordsEqual(int[] coords1, int[] coords2) {
        int n = coords1.length;
        if (coords2.length != n) {
            throw new IllegalArgumentException("dimensions not equal: " + n + " != " + coords2.length);
        }
        for (int i=0; i<n; i++) {
            if (coords1[i] != coords2[i]) {
                return false;
            }
        }
        return true;
    }

    public static int euclideanDist(int[] coords1, int[] coords2) {
        return (int) (0.5 + Math.sqrt((double) euclideanDistSquared(coords1, coords2)));
    }

    public static int euclideanDistSquared(int[] coords1, int[] coords2) {
        int d = 0;
        int len = coords1.length;
        for (int i=0; i<len; i++) {
            int c = coords1[i] - coords2[i];
            d += c*c;
        }
        return d;
    }

    private static boolean diffsWithinBoundaries(int[] coords1, int[] coords2, int[] maxDiffs) {
    	final int dim = coords1.length;
    	for (int i=0; i<dim; i++) {
    		if (Math.abs(coords1[i] - coords2[i]) > maxDiffs[i]) return false;
    	}
    	return true;
    }
}
