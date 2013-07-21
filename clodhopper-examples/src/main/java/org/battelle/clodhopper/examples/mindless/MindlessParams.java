package org.battelle.clodhopper.examples.mindless;

public class MindlessParams {

  private int clusterCount;
  private long randomSeed;
  
  public MindlessParams(int clusterCount, long randomSeed) {
    this.clusterCount = clusterCount;
    this.randomSeed = randomSeed;
  }
  
  public int getClusterCount() {
    return clusterCount;
  }
  
  public void setClusterCount(int clusterCount) {
    this.clusterCount = clusterCount;
  }
  
  public long getRandomSeed() {
    return randomSeed;
  }
  
  public void setRandomSeed(long randomSeed) {
    this.randomSeed = randomSeed;
  }
}
