package org.battelle.clodhopper.examples.project;

import java.util.Arrays;

import org.battelle.clodhopper.tuple.ArrayTupleList;
import org.battelle.clodhopper.tuple.TupleList;

public class Projection {

	private double[] minBounds;
	private double[] maxBounds;
	
	private boolean minMaxSet;
	private double[] minima;
	private double[] maxima;
	
	private TupleList data;

	public Projection(int projectionLength, int projectionCount, double minBound, double maxBound) {
		data = new ArrayTupleList(projectionLength, projectionCount);
		this.minBounds = new double[projectionLength];
		Arrays.fill(this.minBounds, minBound);
		this.maxBounds = new double[projectionLength];
		Arrays.fill(this.maxBounds, maxBound);
		this.minima = new double[projectionLength];
		Arrays.fill(this.minima, Double.MAX_VALUE);
		this.maxima = new double[projectionLength];
		Arrays.fill(this.maxima, -Double.MAX_EXPONENT);
	}
	
	public Projection(int projectionLength, int projectionCount) {
		this(projectionLength, projectionCount, -Double.MAX_VALUE, Double.MAX_VALUE);
	}
	
	public int getProjectionLength() {
		return data.getTupleLength();
	}
	
	public int getProjectionCount() {
		return data.getTupleCount();
	}
	
	public double getMinimum(int dim) {
		if (!minMaxSet) {
			computeMinMax();
		}
		return minima[dim];
	}
	
	public double getMaximum(int dim) {
		if (!minMaxSet) {
			computeMinMax();
		}
		return maxima[dim];
	}
	
	public void computeMinMax() {
		if (data != null) {
			final int n = data.getTupleCount();
			final int len = data.getTupleLength();
			final double[] buffer = new double[len];
			Arrays.fill(minima, Double.MAX_VALUE);
			Arrays.fill(maxima, -Double.MAX_VALUE);
			for (int i=0; i<n; i++) {
				data.getTuple(i, buffer);
				for (int j=0; j<len; j++) {
					double v = buffer[j];
					if (v < minima[j]) {
						minima[j] = v;
					}
					if (v > maxima[j]) {
						maxima[j] = v;
					}
				}
			}
			minMaxSet = true;
		}
	}
	
	public double getMinBound(int dim) {
		return minBounds[dim];
	}
	
	public void setMinBound(int dim, double value) {
		minBounds[dim] = value;
	}
	
	public double getMaxBound(int dim) {
		return maxBounds[dim];
	}
	
	public void setMaxBound(int dim, double value) {
		maxBounds[dim] = value;
	}
	
	public void setProjection(int n, double[] projection) {
		data.setTuple(n, projection);
		minMaxSet = false;
	}
	
	public double[] getProjection(int n, double[] reuseBuffer) {
		return data.getTuple(n, reuseBuffer);
	}
	
	public double getProjection(int n, int dim) {
		return data.getTupleValue(n, dim);
	}
}
