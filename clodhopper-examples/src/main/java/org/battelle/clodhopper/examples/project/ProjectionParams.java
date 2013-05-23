package org.battelle.clodhopper.examples.project;

public class ProjectionParams {
	
	private double gravity = 0.2;
	private boolean normalizeDimensions = true;
	private boolean normalizeCoordinates = true;
	private int numDimensions = 2;
	
	public ProjectionParams(
			int numDimensions, 
			double gravity,
			boolean normalizeDimensions,
			boolean normalizeCoordinates) {
		if (gravity < 0.0 || gravity > 1.0) {
			throw new IllegalArgumentException("invalid gravity: " + gravity);
		}
		if (numDimensions <= 0) {
			throw new IllegalArgumentException("number of dimensions <= 0: " + numDimensions);
		}
		this.gravity = gravity;
		this.normalizeDimensions = normalizeDimensions;
		this.normalizeCoordinates = normalizeCoordinates;
		this.numDimensions = numDimensions;
	}
	
	public ProjectionParams(int numDimensions) {
	    this(numDimensions, 0.2, true, true);
	}
	
	public ProjectionParams() {
		this(2, 0.2, true, true);
	}
	
	public double getGravity() {
		return gravity;
	}
	
	public boolean getNormalizeDimensions() {
		return normalizeDimensions;
	}
	
	public boolean getNormalizeCoordinates() {
		return normalizeCoordinates;
	}
	
	public int getNumDimensions() {
		return numDimensions;
	}
	
	public int hashCode() {
		long bits = Double.doubleToLongBits(gravity);
		int hc = (int) (bits ^ (bits >>> 32));
		hc = hc*37 + (normalizeDimensions ? 1 : 0);
		hc = hc*37 + (normalizeCoordinates ? 1 : 0);
		hc = hc*37 + numDimensions;
		return hc;
	}
	
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o instanceof ProjectionParams) {
			ProjectionParams other = (ProjectionParams) o;
			return other.getGravity() == this.getGravity() &&
			  other.getNormalizeCoordinates() == this.getNormalizeCoordinates() &&
			  other.getNormalizeDimensions() == this.getNormalizeDimensions() &&
			  other.getNumDimensions() == this.getNumDimensions();
		}
		return false;
	}

}
