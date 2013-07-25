package org.battelle.clodhopper.jarvispatrick;

import org.battelle.clodhopper.distance.DistanceMetric;
import org.battelle.clodhopper.distance.EuclideanDistanceMetric;

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
 * 
 * JarvisPatrickParams.java
 *
 *===================================================================*/

/**
 * Parameters object for the Jarvis-Patrick clustering algorithm.
 * 
 * @author R. Scarberry
 * @since 1.0.1
 *
 */
public class JarvisPatrickParams {
  
  public static final int NEAREST_NEIGHBORS_TO_EXAMINE_DEFAULT = 20;
  public static final int NEAREST_NEIGHBOR_OVERLAP_DEFAULT = 2;
  
  // The number of nearest neighbors to examine for every tuple.
  private int nearestNeighborsToExamine;
  // The minimum required nn overlap between tuples in order
  // for them to be assigned to the same cluster.
  private int nearestNeighborOverlap;
  // Whether or not tuples must be in each other's nn list
  // in order to be assigned to the same cluster.
  private boolean mutualNearestNeighbors;
  // The distance metric to use for finding nearest neighbors.
  private DistanceMetric distanceMetric;
  // The number of threads to use for the concurrent parts.
  private int workerThreadCount;
  
  /**
   * Constructor
   */
  public JarvisPatrickParams() {
    nearestNeighborsToExamine = NEAREST_NEIGHBORS_TO_EXAMINE_DEFAULT;
    nearestNeighborOverlap = NEAREST_NEIGHBOR_OVERLAP_DEFAULT;
    mutualNearestNeighbors = true;
    distanceMetric = new EuclideanDistanceMetric();
    workerThreadCount = Runtime.getRuntime().availableProcessors();
  }
  
  /**
   * Returns the number of nearest neighbors to examine for each tuple.
   * 
   * @return
   */
  public int getNearestNeighborsToExamine() {
    return nearestNeighborsToExamine;
  }
  
  /**
   * Set the number of nearest neighbors to examine for each tuple.
   * 
   * @param nearestNeighborsToExamine
   */
  public void setNearestNeighborsToExamine(int nearestNeighborsToExamine) {
    if (nearestNeighborsToExamine < 2) {
      throw new IllegalArgumentException("must be >= 2: " + nearestNeighborsToExamine);
    }
    this.nearestNeighborsToExamine = nearestNeighborsToExamine;
  }
  
  /**
   * Get the nearest neighbor overlap that must exist between two tuples in 
   * order for them to be assigned to the same cluster.
   * 
   * @return
   */
  public int getNearestNeighborOverlap() {
    return nearestNeighborOverlap;
  }
  
  /**
   * Set the nearest neighbor overlap required for two tuples to 
   * be assigned to the same cluster.
   * 
   * @param nearestNeighborOverlap
   */
  public void setNearestNeighborOverlap(int nearestNeighborOverlap) {
    if (nearestNeighborOverlap < 1) {
      throw new IllegalArgumentException("must be >= 1: " + nearestNeighborOverlap);
    }
    this.nearestNeighborOverlap = nearestNeighborOverlap;
  }
  
  /**
   * Return whether or not two tuples must be in each other's nearest neighbor
   * lists in order for them to be assigned to the same cluster.
   * 
   * @return
   */
  public boolean getMutualNearestNeighbors() {
    return mutualNearestNeighbors;
  }
  
  /**
   * Set whether or not two tuples must be in each other's nearest neighbor lists
   * in order for them to be in the same cluster.
   * 
   * @param b
   */
  public void setMutualNearestNeighbors(boolean b) {
    mutualNearestNeighbors = b;
  }
  
  /**
   * Get the distance metric.
   * 
   * @return
   */
  public DistanceMetric getDistanceMetric() {
    return distanceMetric;
  }
  
  /**
   * Set the distance metric.
   * 
   * @param distanceMetric
   */
  public void setDistanceMetric(DistanceMetric distanceMetric) {
    if (distanceMetric == null) {
      throw new NullPointerException();
    }
    this.distanceMetric = distanceMetric;
  }

  /**
   * Get the number of worker threads to use for concurrent parts of
   * the algorithm.
   * 
   * @return
   */
  public int getWorkerThreadCount() {
    return workerThreadCount;
  }
  
  /**
   * Set the number of threads to be used for concurrent parts of the
   * algorithm.
   * 
   * @param n
   */
  public void setWorkerThreadCount(int n) {
    if (n <= 0) {
      throw new IllegalArgumentException("worker thread count must be greater than 0");
    }
    this.workerThreadCount = n;
  }

  /**
   * Builder class for JarvisPatrickParams.
   * 
   * @author R. Scarberry
   *
   */
  public static class Builder {
    
    private JarvisPatrickParams params;
  
    public Builder() {
      params = new JarvisPatrickParams();
    }
    
    public Builder nearestNeighborsToExamine(int nearestNeighborsToExamine) {
      params.setNearestNeighborsToExamine(nearestNeighborsToExamine);
      return this;
    }
    
    public Builder nearestNeighborOverlap(int nearestNeighborOverlap) {
      params.setNearestNeighborOverlap(nearestNeighborOverlap);
      return this;
    }
    
    public Builder mutualNearestNeighbors(boolean b) {
      params.setMutualNearestNeighbors(b);
      return this;
    }
    
    public Builder workerThreadCount(int n) {
      params.setWorkerThreadCount(n);
      return this;
    }

    public Builder distanceMetric(DistanceMetric distanceMetric) {
      params.setDistanceMetric(distanceMetric);
      return this;
    }

    public JarvisPatrickParams build() {
      return params;
    }
  }
 
}
