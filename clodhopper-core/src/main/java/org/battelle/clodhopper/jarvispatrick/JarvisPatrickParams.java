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
  
  public static final int NEAREST_NEIGHBORS_TO_EXAMINE_DEFAULT = 6;
  public static final int NEAREST_NEIGHBOR_OVERLAP_DEFAULT = 2;
  
  private int nearestNeighborsToExamine;
  private int nearestNeighborOverlap;
  private DistanceMetric distanceMetric;
  private int workerThreadCount;
  
  public JarvisPatrickParams() {
    nearestNeighborsToExamine = NEAREST_NEIGHBORS_TO_EXAMINE_DEFAULT;
    nearestNeighborOverlap = NEAREST_NEIGHBOR_OVERLAP_DEFAULT;
    distanceMetric = new EuclideanDistanceMetric();
    workerThreadCount = Runtime.getRuntime().availableProcessors();
  }
  
  public int getNearestNeighborsToExamine() {
    return nearestNeighborsToExamine;
  }
  
  public void setNearestNeighborsToExamine(int nearestNeighborsToExamine) {
    if (nearestNeighborsToExamine < 2) {
      throw new IllegalArgumentException("must be >= 2: " + nearestNeighborsToExamine);
    }
    this.nearestNeighborsToExamine = nearestNeighborsToExamine;
  }
  
  public int getNearestNeighborOverlap() {
    return nearestNeighborOverlap;
  }
  
  public void setNearestNeighborOverlap(int nearestNeighborOverlap) {
    if (nearestNeighborOverlap < 1) {
      throw new IllegalArgumentException("must be >= 1: " + nearestNeighborOverlap);
    }
    this.nearestNeighborOverlap = nearestNeighborOverlap;
  }
  
  public DistanceMetric getDistanceMetric() {
    return distanceMetric;
  }
  
  public void setDistanceMetric(DistanceMetric distanceMetric) {
    if (distanceMetric == null) {
      throw new NullPointerException();
    }
    this.distanceMetric = distanceMetric;
  }

  public int getWorkerThreadCount() {
    return workerThreadCount;
  }
  
  public void setWorkerThreadCount(int n) {
    if (n <= 0) {
      throw new IllegalArgumentException("worker thread count must be greater than 0");
    }
    this.workerThreadCount = n;
  }

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
