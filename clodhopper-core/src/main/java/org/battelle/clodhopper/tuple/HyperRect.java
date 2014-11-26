package org.battelle.clodhopper.tuple;

/*=====================================================================
 * 
 *                       CLODHOPPER CLUSTERING API
 * 
 * -------------------------------------------------------------------- 
 * 
 * Copyright (C) 2013 Battelle Memorial Institute 
 * http://www.battelle.org
 * 
 * -------------------------------------------------------------------- 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * -------------------------------------------------------------------- 
 * *
 * HyperRect.java
 *
 *===================================================================*/
/**
 * Represents a rectangle in arbitrary N-dimensional space, where N must be 
 * at least 1.
 * 
 * @author R. Scarberry
 *
 */
public class HyperRect {

  private final double[] minCorner;
  private final double[] maxCorner;

  /**
   * Constructs a hyper-rectangle with zero volume in
   * hyper-space. Both corners are initialized to (0,0,...,0)
   * 
   * @param dim - the dimensionality of the hyper-rectangle.
   */
  public HyperRect(final int dim) {
      if (dim <= 0) {
        throw new IllegalArgumentException("dimension must be > 0");
      }
      minCorner = new double[dim];
      maxCorner = new double[dim];
  }

  /**
   * <p>Constructs a hyper-rectangle with the specified minimum and maximum 
   * corner points.  For example, if the dimensionality is 2, minCorner is the
   * lower-left corner and maxCorner is the upper-right.</p>
   * <p>The constructor rearranges the values in minCorner and maxCorner to
   * ensure that each element of minCorner is less than or equal to the
   * corresponding element of maxCorner.</p> 
   * 
   * @param cornerMin contains the minimum values for all dimensions.
   * @param cornerMax contains the maximum values for all dimensions.
   * 
   * @throws IllegalArgumentException - if minCorner and maxCorner do not
   *   have the same length.
   */
  public HyperRect(final double[] cornerMin, final double[] cornerMax) {
      int dim = cornerMin.length;
      if (dim != cornerMax.length) {
          throw new IllegalArgumentException("inconsistent dimensions: "
                                             + dim
                                             + " != " + cornerMax.length);
      }
      minCorner = new double[dim];
      maxCorner = new double[dim];
      // Ensures corners really are the min/max corners.
      for (int i=0; i<dim; i++) {
          double d1 = cornerMin[i];
          double d2 = cornerMax[i];
          if (d1 < d2) {
              minCorner[i] = d1;
              maxCorner[i] = d2;
          } else {
              minCorner[i] = d2;
              maxCorner[i] = d1;
          }
      }
  }

  /**
   * Is this hyper-rectangle actually a point?  That is, are the min and max
   * vertices the same?
   * @return boolean
   */
  public boolean isPoint() {
      int dim = minCorner.length;
      for (int i=0; i<dim; i++) {
          if (minCorner[i] != maxCorner[i]) {
              return false;
          }
      }
      return true;
  }

  /**
   * Returns the index of the dimension having the smallest difference
   * between the minimum vertex and maximum vertex point.
   * 
   * @return the dimension with the minimum difference between in maximum and minimum values.
   */
  public int dimensionOfMinWidth() {
      int dim = minCorner.length;
      int minDim = 0;
      if (dim > 0) {
          double minWidth = maxCorner[0] - minCorner[0];
          for (int d=1; d<dim; d++) {
              double width = maxCorner[d] - minCorner[d];
              if (width < minWidth) {
                  minWidth = width;
                  minDim = d;
              }
          }
      }
      return minDim;
  }

  /**
   * Returns the index of the dimension having the largest difference
   * between the minimum vertex and maximum vertex point.
   * 
   * @return the dimension with the maximum difference between in maximum and minimum values.
   */
  public int dimensionOfMaxWidth() {
      int dim = minCorner.length;
      int maxDim = 0;
      if (dim > 0) {
          double maxWidth = maxCorner[0] - minCorner[0];
          for (int d=1; d<dim; d++) {
              double width = maxCorner[d] - minCorner[d];
              if (width > maxWidth) {
                  maxWidth = width;
                  maxDim = d;
              }
          }
      }
      return maxDim;
  }

  /**
   * Sets one of the minimum vertex values.
   * 
   * @param n - the dimension index for the value to be changed.
   * @param value - the new value.
   * 
   * @throws IllegalArgumentException - if the value is greater than
   *   the corresponding maximum vertex element.
   */
  public void setMinCornerCoord(final int n, final double value) {
      if (value <= maxCorner[n]) {
          minCorner[n] = value;
      } else {
          throw new IllegalArgumentException("exceeds max corner coordinate: "
                                             + value + " > " + maxCorner[n]);
      }
  }

  /**
   * Returns the element of the minimum vertex point for the specified dimension.
   * @param n - the index of the dimension.
   * @return the value of the minimum corner element.
   */
  public double getMinCornerCoord(final int n) {
      return minCorner[n];
  }

  /**
   * Sets one of the maximum vertex values.
   * 
   * @param n - the dimension index for the value to be changed.
   * @param value - the new value.
   * 
   * @throws IllegalArgumentException - if the value is less than
   *   the corresponding maximum vertex element.
   */
  public void setMaxCornerCoord(final int n, final double value) {
      if (value >= minCorner[n]) {
          maxCorner[n] = value;
      } else {
          throw new IllegalArgumentException("less that min corner coordinate: "
                                             + value + " < " + minCorner[n]);
      }
  }

  /**
   * Returns the element of the maximum vertex point for the specified dimension.
   * @param n - the index of the dimension.
   * @return the value of the maximum corner element.
   */
  public double getMaxCornerCoord(final int n) {
      return maxCorner[n];
  }

  public Object clone() {
      return new HyperRect(minCorner, maxCorner);
  }

  /**
   * Returns the closest point on the surface or within
   * the hyper-rectangle to the specified point. If the point is
   * within the rectangle, a clone of the point itself is returned.
   * 
   * @param point a point value.
   * 
   * @return an array the same length as the dimension of this hyperrect containing its
   *   closest point to the specified point.
   */
  public double[] closestPoint(final double[] point) {
      int dim = point.length;
      checkDimension(dim);
      double[] closest = new double[dim];
      for (int i=0; i<dim; i++) {
          double d = point[i];
          if (d < minCorner[i]) {
              closest[i] = minCorner[i];
          } else if (d > maxCorner[i]) {
              closest[i] = maxCorner[i];
          } else {
              closest[i] = d;
          }
      }
      return closest;
  }

  /**
   * Returns true if the specified point is contained within or
   * on the surface of the hyper-rectangle.
   * 
   * @param point a point value.
   * @return true if the point is contained in this hyperrect.
   */
  public boolean contains(final double[] point) {
      int dim = point.length;
      checkDimension(dim);
      for (int i=0; i<dim; i++) {
          double d = point[i];
          if (d < minCorner[i] || d > maxCorner[i]) {
              return false;
          }
      }
      return true;
  }

  /**
   * Returns the dimensionality of the hyper-rectangle.
   * @return the dimension of this hyperrect.
   */
  public int getDimension() {
      return minCorner.length;
  }

  /**
   * Utility method which returns a hyper-rectangle of infinite volume. 
   * The elements of the minimum vertex are all set to Double.NEGATIVE_INFINITY
   * and all element of the maximum vertex are set to Double.POSITIVE_INFINITY.
   * 
   * @param dim the dimension of the desired <code>HyperRect</code>.
   * @return a <code>HyperRect</code> instance with infinite width in each dimension.
   */
  public static HyperRect infiniteHyperRect(final int dim) {
      HyperRect hrect = new HyperRect(dim);
      java.util.Arrays.fill(hrect.minCorner, Double.NEGATIVE_INFINITY);
      java.util.Arrays.fill(hrect.maxCorner, Double.POSITIVE_INFINITY);
      return hrect;
  }

  /**
   * Returns the intersection of this hyper-rectangle with another,
   * if the two hyper-rectangles intersect.
   * @param other the other <code>HyperRect</code> instance.
   * @return - the intersection, or null if the hyper-rectangles do not intersect.
   */
  public HyperRect intersectionWith(final HyperRect other) {
      int dim = other.getDimension();
      checkDimension(dim);
      HyperRect intersection = new HyperRect(dim);
      double[] minCorner = intersection.minCorner;
      double[] maxCorner = intersection.maxCorner;
      for (int i=0; i<dim; i++) {
          minCorner[i] = Math.max(this.minCorner[i], other.minCorner[i]);
          maxCorner[i] = Math.min(this.maxCorner[i], other.maxCorner[i]);
          if (minCorner[i] >= maxCorner[i]) {
              return null;
          }
      }
      return intersection;
  }

  /**
   * Returns true if this hyper-rectangle intersects with the other.
   * @param other the other hyperrect.
   * @return true if the hyperrects intersect.
   */
  public boolean intersectsWith(final HyperRect other) {
      int dim = other.getDimension();
      checkDimension(dim);
      for (int i=0; i<dim; i++) {
          if(Math.max(this.minCorner[i], other.minCorner[i]) >=
             Math.min(this.maxCorner[i], other.maxCorner[i])) {
            return false;
          }
      }
      return true;
  }

  /**
   * Returns the volume of the hyper-rectangle.  The volume is the product
   * of all the dimension widths.
   * @return the volume.
   */
  public double volume() {
      double v = 0.0;
      int dim = minCorner.length;
      for (int i=0; i<dim; i++) {
          v *= maxCorner[i] - minCorner[i];
      }
      return v;
  }

  // Check that dim equals the dimension of this hyper-rectangle.
  private void checkDimension(final int dim) {
      if (dim != minCorner.length) {
          throw new IllegalArgumentException("wrong number of dimensions: " +
                                             dim + " != " + minCorner.length);
      }
  }

}
