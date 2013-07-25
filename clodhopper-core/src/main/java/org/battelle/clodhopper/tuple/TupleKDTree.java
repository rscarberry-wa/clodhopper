package org.battelle.clodhopper.tuple;

import gnu.trove.list.array.TIntArrayList;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

import org.battelle.clodhopper.distance.DistanceMetric;

public class TupleKDTree {

  private TupleList tuples;
  private DistanceMetric distanceMetric;
  private int maxNdx;

  private int[] nodes;
  private int[] lefts;
  private int[] rights;
  private boolean[] deleted;

  private int count;

  public TupleKDTree(TupleList tuples, DistanceMetric distanceMetric) {
    if (tuples == null || distanceMetric == null) {
      throw new NullPointerException();
    }
    this.tuples = tuples;
    this.distanceMetric = distanceMetric;
    maxNdx = tuples.getTupleCount() - 1;
    ensureCapacity(100);
  }

  public TupleList getTupleList() {
    return tuples;
  }

  public DistanceMetric getDistanceMetric() {
    return distanceMetric;
  }

  public static TupleKDTree forTupleList(TupleList tuples,
      DistanceMetric distanceMetric) {
    TupleKDTree kd = new TupleKDTree(tuples, distanceMetric);
    int tupleCount = tuples.getTupleCount();
    for (int i = 0; i < tupleCount; i++) {
      kd.insert(i);
    }
    return kd;
  }

  private void ensureCapacity(int minCap) {
    int curCap = currentCapacity();
    if (curCap < minCap) {
      int newCap = Math.max(curCap * 2, minCap);
      int[] newNodes = new int[newCap];
      int[] newLefts = new int[newCap];
      int[] newRights = new int[newCap];
      boolean[] newDeleted = new boolean[newCap];
      if (curCap > 0) {
        System.arraycopy(nodes, 0, newNodes, 0, curCap);
        System.arraycopy(lefts, 0, newLefts, 0, curCap);
        System.arraycopy(rights, 0, newRights, 0, curCap);
        System.arraycopy(deleted, 0, newDeleted, 0, curCap);
      }
      Arrays.fill(newNodes, curCap, newCap, -1);
      Arrays.fill(newLefts, curCap, newCap, -1);
      Arrays.fill(newRights, curCap, newCap, -1);
      Arrays.fill(newDeleted, curCap, newCap, false);
      nodes = newNodes;
      lefts = newLefts;
      rights = newRights;
      deleted = newDeleted;
    }
  }

  private int currentCapacity() {
    return nodes != null ? nodes.length : 0;
  }

  private void newNodeOnLeft(int parentIndex, int ndx) {
    int m = count;
    count++;
    ensureCapacity(count);
    nodes[m] = ndx;
    lefts[parentIndex] = m;
  }

  private void newNodeOnRight(int parentIndex, int ndx) {
    int m = count;
    count++;
    ensureCapacity(count);
    nodes[m] = ndx;
    rights[parentIndex] = m;
  }

  private void checkNdx(int ndx) {
    if (ndx < 0 || ndx > maxNdx) {
      throw new IndexOutOfBoundsException("out of bounds: " + ndx);
    }
  }

  public void insert(int ndx) {

    checkNdx(ndx);

    // First thing to be inserted -- it becomes the root.
    if (count == 0) {
      ensureCapacity(1);
      nodes[0] = ndx;
      count++;
      return;
    }

    int n = 0;
    int level = 0;
    int dim = tuples.getTupleLength();

    while (true) {
      int curNode = nodes[n];
      if (curNode == ndx) {
        if (deleted[n]) {
          deleted[n] = false;
          return;
        }
        throw new IllegalArgumentException("duplicate insertion: " + ndx);
      } else {

        int d = level % dim;

        double coord = tuples.getTupleValue(ndx, d);

        double nodeCoord = tuples.getTupleValue(curNode, d);
        if (coord > nodeCoord) {
          if (rights[n] < 0) {
            newNodeOnRight(n, ndx);
            return;
          } else {
            n = rights[n];
          }
        } else {
          if (lefts[n] < 0) {
            newNodeOnLeft(n, ndx);
            return;
          } else {
            n = lefts[n];
          }
        }
      }
      level++;
    } // while
  }

  public void delete(int ndx) {

    checkNdx(ndx);

    if (count == 0) {
      return;
    }

    int n = 0;
    int level = 0;
    int dim = tuples.getTupleLength();

    while (true) {
      int curNode = nodes[n];
      if (curNode == ndx) {
        deleted[n] = true;
        return;
      } else {
        int d = level % dim;
        double coord = tuples.getTupleValue(ndx, d);
        double nodeCoord = tuples.getTupleValue(curNode, d);
        if (coord > nodeCoord) {
          if (rights[n] < 0) {
            return;
          } else {
            n = rights[n];
          }
        } else {
          if (lefts[n] < 0) {
            return;
          } else {
            n = lefts[n];
          }
        }
      }
      level++;
    } // while
  }

  public int search(double[] coords) {

    int n = 0;
    int level = 0;
    final int dim = tuples.getTupleLength();
    final double[] nodeCoords = new double[dim];

    while (true) {
      int curNode = (n >= 0 && n < count) ? nodes[n] : -1;
      if (curNode < 0)
        return -1; // Not found.
      tuples.getTuple(curNode, nodeCoords);
      if (!deleted[n] && coordsEqual(coords, nodeCoords)) {
        return curNode;
      }
      int d = level % dim;
      if (coords[d] > nodeCoords[d]) {
        n = rights[n];
      } else {
        n = lefts[n];
      }
      level++;
    } // while

  }

  public int nearestNeighbor(int ndx) {

    checkNdx(ndx);

    double[] coords = new double[tuples.getTupleLength()];
    tuples.getTuple(ndx, coords);

    int[] nn = nearest(coords, 2);

    return nn[0] == ndx ? nn[1] : nn[0];
  }

  public int nearest(double[] coords) {
    int[] nn = nearest(coords, 1);
    return nn[0];
  }

  public int[] nearest(int ndx, int num) {

    if (num < 0 || num > count) {
      throw new IllegalArgumentException(
          "number of neighbors negative or greater than number of nodes: "
              + num);
    }

    int dim = tuples.getTupleLength();
    double[] coords = new double[dim];
    tuples.getTuple(ndx, coords);

    LinkedList<DistanceEntry> distanceList = new LinkedList<DistanceEntry>();

    rnearest(0, coords, num, HyperRect.infiniteHyperRect(dim),
        Double.MAX_VALUE, 0, dim, distanceList, ndx);

    int[] ids = new int[num];
    for (int i = 0; i < num; i++) {
      DistanceEntry entry = distanceList.removeFirst();
      ids[i] = entry.getIndex();
    }

    return ids;
  }

  public int[] nearest(double[] coords, int num) {

    if (num < 0 || num > count) {
      throw new IllegalArgumentException(
          "number of neighbors negative or greater than number of nodes: "
              + num);
    }

    int dim = coords.length;

    LinkedList<DistanceEntry> distanceList = new LinkedList<DistanceEntry>();

    rnearest(0, coords, num, HyperRect.infiniteHyperRect(dim),
        Double.MAX_VALUE, 0, dim, distanceList, -1);

    int[] ids = new int[num];
    for (int i = 0; i < num; i++) {
      DistanceEntry entry = distanceList.removeFirst();
      ids[i] = entry.getIndex();
    }

    return ids;
  }

  public int[] closeTo(int ndx, double maxDistance) {

    int dim = tuples.getTupleLength();
    double[] coords = new double[dim];
    tuples.getTuple(ndx, coords);

    LinkedList<DistanceEntry> distanceList = new LinkedList<DistanceEntry>();

    rcloseTo(0, coords, HyperRect.infiniteHyperRect(dim), maxDistance, 0,
        dim, distanceList, ndx);

    int sz = distanceList.size();
    int[] ids = new int[sz];
    for (int i = 0; i < sz; i++) {
      DistanceEntry entry = distanceList.removeFirst();
      ids[i] = entry.getIndex();
    }

    return ids;
  }

  public int[] closeTo(double[] coords, double maxDistance) {

    int dim = coords.length;

    LinkedList<DistanceEntry> distanceList = new LinkedList<DistanceEntry>();

    rcloseTo(0, coords, HyperRect.infiniteHyperRect(dim), maxDistance, 0,
        dim, distanceList, -1);

    int sz = distanceList.size();
    int[] ids = new int[sz];
    for (int i=0; i<sz; i++) {
      DistanceEntry entry = distanceList.removeFirst();
      ids[i] = entry.getIndex();
    }

    return ids;
  }

  public int[] inside(HyperRect rect) {
    final int dim = tuples.getTupleLength();
    if (rect.getDimension() != dim) {
      throw new IllegalArgumentException("dimension mismatch: "
          + rect.getDimension() + " != " + dim);
    }
    double[] midPoint = new double[dim];
    double[] maxDiffs = new double[dim];
    for (int i = 0; i < dim; i++) {
      double min = rect.getMinCornerCoord(i);
      double max = rect.getMaxCornerCoord(i);
      double maxDiff = (max - min) / 2;
      midPoint[i] = min + maxDiff;
      maxDiffs[i] = maxDiff;
    }
    TIntArrayList intList = new TIntArrayList();
    rcloseTo(0, midPoint, HyperRect.infiniteHyperRect(dim), maxDiffs, 0, dim,
        intList, -1);

    intList.trimToSize();
    return intList.toArray();
  }

  private void rnearest(int curNodeNdx, double[] targetCoords, int num,
      HyperRect hr, double maxDistance, int level, int dim,
      LinkedList<DistanceEntry> distanceList, int ndxToExclude) {

    int curNode = nodes[curNodeNdx];
    if (curNode < 0) {
      return;
    }

    // Component of coords to use for splitting.
    int s = level % dim;

    double[] curCoords = tuples.getTuple(curNode, null);

    double targetCoord = targetCoords[s];
    double curCoord = curCoords[s];

    boolean targetInLeft = targetCoord < curCoord;

    int nearerNodeNdx = -1, furtherNodeNdx = -1;
    if (targetInLeft) {
      nearerNodeNdx = lefts[curNodeNdx];
      furtherNodeNdx = rights[curNodeNdx];
    } else {
      nearerNodeNdx = rights[curNodeNdx];
      furtherNodeNdx = lefts[curNodeNdx];
    }

    if (nearerNodeNdx >= 0) {
      double oldCoord = 0.0;
      if (targetInLeft) {
        oldCoord = hr.getMaxCornerCoord(s);
        hr.setMaxCornerCoord(s, curCoords[s]);
      } else {
        oldCoord = hr.getMinCornerCoord(s);
        hr.setMinCornerCoord(s, curCoords[s]);
      }
      rnearest(nearerNodeNdx, targetCoords, num, hr, maxDistance, level + 1,
          dim, distanceList, ndxToExclude);
      if (targetInLeft) {
        hr.setMaxCornerCoord(s, oldCoord);
      } else {
        hr.setMinCornerCoord(s, oldCoord);
      }
      if (distanceList.size() == num) {
        maxDistance = distanceList.getLast().getDistance();
      }
    }

    if (furtherNodeNdx >= 0) {
      double oldCoord = 0.0;
      if (targetInLeft) {
        oldCoord = hr.getMinCornerCoord(s);
        hr.setMinCornerCoord(s, curCoords[s]);
      } else {
        oldCoord = hr.getMaxCornerCoord(s);
        hr.setMaxCornerCoord(s, curCoords[s]);
      }
      
      double distance = distanceMetric.distance(hr.closestPoint(targetCoords), targetCoords);
      
      if (distance < maxDistance) {
        rnearest(furtherNodeNdx, targetCoords, num, hr, maxDistance,
            level + 1, dim, distanceList, ndxToExclude);
      }
      
      if (targetInLeft) {
        hr.setMinCornerCoord(s, oldCoord);
      } else {
        hr.setMaxCornerCoord(s, oldCoord);
      }
      if (distanceList.size() == num) {
        maxDistance = distanceList.getLast().getDistance();
      }
    }

    if (!deleted[curNodeNdx] && curNodeNdx != ndxToExclude) {
      double curToTarget = distanceMetric.distance(curCoords, targetCoords);
      if (curToTarget < maxDistance) {
        addToDistanceList(distanceList, new DistanceEntry(curNode, curToTarget), num);
      }
    }
  }
  
  private static void addToDistanceList(LinkedList<DistanceEntry> distanceList, DistanceEntry entry, int maxSize) {
    int n = Collections.binarySearch(distanceList, entry);
    if (n < 0) {
      // The usual case, since this method is never called if an entry is already in the 
      // list.
      n = -n - 1;
      distanceList.add(n, entry);
      while(distanceList.size() > maxSize) {
        distanceList.removeLast();
      }
    }
  }

  private void rcloseTo(int curNodeNdx, double[] targetCoords, HyperRect hr,
      double maxDistance, int level, int dim, LinkedList<DistanceEntry> distanceList,
      int ndxToExclude) {

    int curNode = nodes[curNodeNdx];
    if (curNode < 0) {
      return;
    }

    // Component of coords to use for splitting.
    int s = level % dim;

    double[] curCoords = tuples.getTuple(curNode, null);

    double targetCoord = targetCoords[s];
    double curCoord = curCoords[s];

    boolean targetInLeft = targetCoord <= curCoord;

    int nearerNodeNdx = -1, furtherNodeNdx = -1;
    if (targetInLeft) {
      nearerNodeNdx = lefts[curNodeNdx];
      furtherNodeNdx = rights[curNodeNdx];
    } else {
      nearerNodeNdx = rights[curNodeNdx];
      furtherNodeNdx = lefts[curNodeNdx];
    }

    if (nearerNodeNdx >= 0) {
      double oldCoord = 0.0;
      if (targetInLeft) {
        oldCoord = hr.getMaxCornerCoord(s);
        hr.setMaxCornerCoord(s, curCoords[s]);
      } else {
        oldCoord = hr.getMinCornerCoord(s);
        hr.setMinCornerCoord(s, curCoords[s]);
      }
      rcloseTo(nearerNodeNdx, targetCoords, hr, maxDistance, level + 1, dim,
          distanceList, ndxToExclude);
      if (targetInLeft) {
        hr.setMaxCornerCoord(s, oldCoord);
      } else {
        hr.setMinCornerCoord(s, oldCoord);
      }
    }

    if (furtherNodeNdx >= 0) {
      double oldCoord = 0.0;
      if (targetInLeft) {
        oldCoord = hr.getMinCornerCoord(s);
        hr.setMinCornerCoord(s, curCoords[s]);
      } else {
        oldCoord = hr.getMaxCornerCoord(s);
        hr.setMaxCornerCoord(s, curCoords[s]);
      }
      double distance = distanceMetric.distance(hr.closestPoint(targetCoords), targetCoords);
      if (distance <= maxDistance) {
        rcloseTo(furtherNodeNdx, targetCoords, hr, maxDistance, level + 1,
            dim, distanceList, ndxToExclude);
      }
      if (targetInLeft) {
        hr.setMinCornerCoord(s, oldCoord);
      } else {
        hr.setMaxCornerCoord(s, oldCoord);
      }
    }

    if (!deleted[curNodeNdx] && curNodeNdx != ndxToExclude) {
      double curToTarget = distanceMetric.distance(curCoords, targetCoords);
      if (curToTarget <= maxDistance) {
        addToDistanceList(distanceList, new DistanceEntry(curNode, curToTarget), Integer.MAX_VALUE);
      }
    }
  }

  private void rcloseTo(int curNodeNdx, double[] targetCoords, HyperRect hr,
      double[] maxDiffs, int level, int dim, TIntArrayList intList,
      int ndxToExclude) {

    int curNode = nodes[curNodeNdx];
    if (curNode < 0) {
      return;
    }

    // Component of coords to use for splitting.
    int s = level % dim;

    double[] curCoords = tuples.getTuple(curNode, null);

    double targetCoord = targetCoords[s];
    double curCoord = curCoords[s];

    boolean targetInLeft = targetCoord <= curCoord;

    int nearerNodeNdx = -1, furtherNodeNdx = -1;
    if (targetInLeft) {
      nearerNodeNdx = lefts[curNodeNdx];
      furtherNodeNdx = rights[curNodeNdx];
    } else {
      nearerNodeNdx = rights[curNodeNdx];
      furtherNodeNdx = lefts[curNodeNdx];
    }

    if (nearerNodeNdx >= 0) {
      double oldCoord = 0.0;
      if (targetInLeft) {
        oldCoord = hr.getMaxCornerCoord(s);
        hr.setMaxCornerCoord(s, curCoords[s]);
      } else {
        oldCoord = hr.getMinCornerCoord(s);
        hr.setMinCornerCoord(s, curCoords[s]);
      }
      rcloseTo(nearerNodeNdx, targetCoords, hr, maxDiffs, level + 1, dim,
          intList, ndxToExclude);
      if (targetInLeft) {
        hr.setMaxCornerCoord(s, oldCoord);
      } else {
        hr.setMinCornerCoord(s, oldCoord);
      }
    }

    if (furtherNodeNdx >= 0) {
      double oldCoord = 0.0;
      if (targetInLeft) {
        oldCoord = hr.getMinCornerCoord(s);
        hr.setMinCornerCoord(s, curCoords[s]);
      } else {
        oldCoord = hr.getMaxCornerCoord(s);
        hr.setMaxCornerCoord(s, curCoords[s]);
      }

      if (diffsWithinBoundaries(hr.closestPoint(targetCoords), targetCoords,
          maxDiffs)) {
        rcloseTo(furtherNodeNdx, targetCoords, hr, maxDiffs, level + 1, dim,
            intList, ndxToExclude);
      }

      if (targetInLeft) {
        hr.setMinCornerCoord(s, oldCoord);
      } else {
        hr.setMaxCornerCoord(s, oldCoord);
      }
    }

    if (!deleted[curNodeNdx] && curNodeNdx != ndxToExclude) {
      if (diffsWithinBoundaries(curCoords, targetCoords, maxDiffs)) {
        intList.add(curNode);
      }
    }
  }

  public static boolean coordsEqual(double[] coords1, double[] coords2) {
    int n = coords1.length;
    if (coords2.length != n) {
      throw new IllegalArgumentException("dimensions not equal: " + n + " != "
          + coords2.length);
    }
    for (int i = 0; i < n; i++) {
      if (Double.doubleToLongBits(coords1[i]) != Double
          .doubleToLongBits(coords2[i])) {
        return false;
      }
    }
    return true;
  }

  private static boolean diffsWithinBoundaries(double[] coords1,
      double[] coords2, double[] maxDiffs) {
    final int dim = coords1.length;
    for (int i = 0; i < dim; i++) {
      if (Math.abs(coords1[i] - coords2[i]) > maxDiffs[i])
        return false;
    }
    return true;
  }

  public static class DistanceEntry implements Comparable<DistanceEntry> {

    private int index;
    private double distance;

    public DistanceEntry(int index, double distance) {
      this.index = index;
      this.distance = distance;
    }

    public int getIndex() {
      return index;
    }

    public double getDistance() {
      return distance;
    }

    @Override
    public int compareTo(DistanceEntry o) {
      return this.distance < o.distance ? -1 : this.distance > o.distance ? +1
          : this.index < o.index ? -1 : this.index > o.index ? +1 : 0;
    }

  }

}
