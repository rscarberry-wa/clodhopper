package org.battelle.clodhopper.examples.project;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealMatrixChangingVisitor;
import org.apache.commons.math3.linear.RealVector;
import org.battelle.clodhopper.Cluster;
import org.battelle.clodhopper.task.AbstractTask;
import org.battelle.clodhopper.task.ProgressHandler;
import org.battelle.clodhopper.tuple.TupleList;
import org.battelle.clodhopper.util.IntIterator;

public class Projector extends AbstractTask<Projection> {

	private TupleList pointTuples;
	private List<Cluster> clusters;
	
	private ProjectionParams params;
	
	private Projection pointProjection;
	private Projection pcaProjection;
		
	public Projector(TupleList pointTuples, List<Cluster> clusters, ProjectionParams params) {
		if (pointTuples == null || clusters == null || params == null) {
			throw new NullPointerException();
		}
		this.pointTuples = pointTuples;
		this.clusters = clusters;
		this.params = params;
	}
	
	public List<Cluster> getClusters() {
		return clusters;
	}
	
	public TupleList getTuples() {
		return pointTuples;
	}
	
	public Projection getPointProjection() {
		return pointProjection;
	}
	
	public Projection getClusterProjection() {
		return pcaProjection;
	}
	
	@Override
	public Projection doTask() throws Exception {
				
		Projection pointProj = null;
		Projection clusterProj = null;
		
		int steps = 4;
		if (params.getNormalizeDimensions()) steps++;
		if (params.getNormalizeCoordinates()) steps++;
		if (params.getGravity() < 1.0) steps++;
		
		ProgressHandler ph = new ProgressHandler(this, steps);
		ph.postBegin();
		
		try {
			
	        int clusterCount = clusters.size();
	        int numCoords = pointTuples.getTupleCount();
	        int numDim = pointTuples.getTupleLength();
	        int projectionDim = params.getNumDimensions();

	        // Removes NaNs from the input data replacing them with 0s.
	        //
	        RealMatrixChangingVisitor nanRemover = new RealMatrixChangingVisitor() {

				@Override
				public void start(int rows, int columns, int startRow, int endRow,
						int startColumn, int endColumn) {
				}

				@Override
				public double visit(int row, int column, double value) {
					if (Double.isNaN(value)) return 0.0;
					return value;
				}

				@Override
				public double end() {
					return 0;
				}
	        	
	        };
	        
	        // Make a matrix from the cluster centers. Will be pcaTupleCount X numDim
	        RealMatrix pcaMatrix = toRealMatrix(clusters);
	        // Eliminate NaNs, if any.
	        pcaMatrix.walkInColumnOrder(nanRemover);
	        
	        checkForCancel();
	        
	        if (params.getNormalizeDimensions()) {
	            
	        	// Compute the mins and maxes.  This was originally done from the coordinates,
	        	// not the cluster centers, but Dave Gillen changed it to this to get
	        	// the result to more closely match IN-SPIRE's.
	            RealMatrix dimMinMax = computeMinMax(pcaMatrix);
	            // Normalize the cluster centers.
	            normalizeDimensions(new RealMatrixTupleList(pcaMatrix), dimMinMax);
	        
	            checkForCancel();
	            
	            // Normalize the coordinates.  Since this changes the coordinates,
	            // projection should be done on a COPY of the coordinates if the original coordinates
	            // need to be preserved.
	            normalizeDimensions(pointTuples, dimMinMax);
	            
	            ph.postStep();
	        }

	        // Compute the projection matrix.
	        PCA pca = new PCA(pcaMatrix, PCA.CovarianceType.COVARIANCE, projectionDim);
	        RealMatrix projMatrix = pca.getPrincipalComponents();
	        
	        checkForCancel();
	        
	        standardizeOrientation(projMatrix);
	        ph.postStep();

	        // Generate the projection data for the clusters.
	        // Have to pass in minAllowed and maxAllowed with extreme values to constructor of
	        // the project data objects, since they default to restricting values to [0 - 1].
	        double[] minAllowed = new double[projectionDim];
	        double[] maxAllowed = new double[projectionDim];
	        Arrays.fill(minAllowed, -Double.MAX_VALUE);
	        Arrays.fill(maxAllowed, Double.MAX_VALUE);

	        // Compute the cluster projection.
	        clusterProj = new Projection(projectionDim, clusterCount, -Double.MAX_VALUE, Double.MAX_VALUE);

	        checkForCancel();
	        
	        double[] projectionBuf = new double[projectionDim];

	        for (int i = 0; i < clusterCount; i++) {
	            RealVector centroid = pcaMatrix.getRowVector(i);
	            for (int j = 0; j < projectionDim; j++) {
	                RealVector column = projMatrix.getColumnVector(j);
	                projectionBuf[j] = centroid.dotProduct(column);
	            }
	            clusterProj.setProjection(i, projectionBuf);
	        }
	        
	        ph.postStep();

	        // Compute the point projection.
	        pointProj = new Projection(projectionDim, numCoords, -Double.MAX_VALUE, Double.MAX_VALUE);

	        RealVector coordinate = new ArrayRealVector(numDim);
	        double[] coordBuf = new double[numDim];

	        for (int i = 0; i < numCoords; i++) {
	            pointTuples.getTuple(i, coordBuf);
	            for (int j=0; j<numDim; j++) {
	            	double d = coordBuf[j];
	            	coordinate.setEntry(j, Double.isNaN(d) ? 0.0 : d);
	            }
	            for (int j = 0; j < projectionDim; j++) {
	                RealVector column = projMatrix.getColumnVector(j);
	                projectionBuf[j] = coordinate.dotProduct(column);
	            }
	            pointProj.setProjection(i, projectionBuf);
	        }

	        ph.postStep();

	        // Normalize the coordinates, if asked to do so.
	        if (params.getNormalizeCoordinates()) {
	            RealMatrix coordMinMax = computeMinMax(clusterProj);
	            normalizeCoordinates(clusterProj, coordMinMax);
	            normalizeCoordinates(pointProj, coordMinMax);
	            ph.postStep();
	        }
	        
	        // Apply the gravity transform.
	        if (params.getGravity() < 1.0) {
	            applyGravityTransform(clusterProj, pointProj, params.getGravity());
	            ph.postStep();
	        }

	        // Do the final normalization.
	        normalizeGlobally(clusterProj, pointProj);
	        ph.postStep();
	        
		} finally {
			
			pointProjection = pointProj;
			pcaProjection = clusterProj;
			
		}
		
		return pointProjection;		
	}
	
	public static RealMatrix toRealMatrix(List<Cluster> clusters) {
		
		int rows = clusters.size();
		
		double [][] data = new double[rows][];
		for (int i=0; i<rows; i++) {
			// getCenter() returns a copy of the center, so it can be
			// assigned directly.
			data[i] = clusters.get(i).getCenter();
		}
		
		return new Array2DRowRealMatrix(data);
	}
	
    protected static RealMatrix computeMinMax(TupleList coordList) {

        int rows = coordList.getTupleCount();
        int cols = coordList.getTupleLength();

        double[] dmin = new double[cols];
        double[] dmax = new double[cols];
        Arrays.fill(dmin, Double.MAX_VALUE);
        Arrays.fill(dmax, -Double.MAX_VALUE);

        double[] coords = new double[cols];
        for (int i = 0; i < rows; i++) {
            coordList.getTuple(i, coords);
            for (int j = 0; j < cols; j++) {
                double d = coords[j];
                if (Double.isNaN(d))
                    d = 0.0;
                if (d < dmin[j])
                    dmin[j] = d;
                if (d > dmax[j])
                    dmax[j] = d;
            }
        }

        return new Array2DRowRealMatrix(new double[][] { dmin, dmax});
    }

    protected static RealMatrix computeMinMax(final RealMatrix coordList) {

          final int rows = coordList.getRowDimension();
          final int cols = coordList.getColumnDimension();

          final double[] dmin = new double[cols];
          final double[] dmax = new double[cols];
          Arrays.fill(dmin, Double.MAX_VALUE);
          Arrays.fill(dmax, -Double.MAX_VALUE);

          for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
              double d = coordList.getEntry(i, j);
              if (Double.isNaN(d)) {
                d = 0.0;
              }
              if (d < dmin[j]) {
                dmin[j] = d;
              }
              if (d > dmax[j]) {
                dmax[j] = d;
              }
            }
          }

          return new Array2DRowRealMatrix(new double[][] { dmin, dmax});
        }

      protected static RealMatrix computeMinMax(Projection projData) {

          int rows = projData.getProjectionCount();
          int cols = projData.getProjectionLength();

          double[] dmin = new double[cols];
          double[] dmax = new double[cols];
          Arrays.fill(dmin, Double.MAX_VALUE);
          Arrays.fill(dmax, -Double.MAX_VALUE);

          double[] proj = new double[cols];
          for (int i = 0; i < rows; i++) {
              projData.getProjection(i, proj);
              for (int j = 0; j < cols; j++) {
                  double d = proj[j];
                  if (d < dmin[j])
                      dmin[j] = d;
                  if (d > dmax[j])
                      dmax[j] = d;
              }
          }

          return new Array2DRowRealMatrix(new double[][] { dmin, dmax} );
      }

      protected static void normalizeDimensions(
              TupleList nspace,
              RealMatrix minmax) {

          int rows = nspace.getTupleCount();
          int cols = nspace.getTupleLength();

          double[] dmin = new double[cols];
          double[] drange = new double[cols];
          for (int i = 0; i < cols; i++) {
              dmin[i] = minmax.getEntry(0, i);
              drange[i] = minmax.getEntry(1, i) - dmin[i];
          }

          double[] coords = new double[cols];
          for (int i = 0; i < rows; i++) {
              nspace.getTuple(i, coords);
              for (int j = 0; j < cols; j++) {
                  double min = dmin[j];
                  double range = drange[j];
                  if (range == 0.0) {
                      coords[j] = 0.0;
                  } else {
                      coords[j] = (coords[j] - min) / range;
                  }
              }
              nspace.setTuple(i, coords);
          }

      }

      protected static void normalizeCoordinates(
              Projection pts,
              RealMatrix minmax) {

          int numCoords = pts.getProjectionCount();
          int dimensions = pts.getProjectionLength();

          double[] dmin = new double[dimensions];
          double[] drange = new double[dimensions];
          for (int i = 0; i < dimensions; i++) {
              dmin[i] = minmax.getEntry(0, i);
              drange[i] = minmax.getEntry(1, i) - dmin[i];
              pts.setMinBound(i, -Double.MAX_VALUE);
              pts.setMaxBound(i, Double.MAX_VALUE);
          }

          double[] proj = new double[dimensions];
          for (int i = 0; i < numCoords; i++) {
              pts.getProjection(i, proj);
              for (int j = 0; j < dimensions; j++) {
                  double r = drange[j];
                  if (r == 0.0) {
                      proj[j] = 0.5;
                  } else {
                      proj[j] = (proj[j] - dmin[j]) / r;
                  }
              }
              pts.setProjection(i, proj);
          }

          for (int i=0; i<dimensions; i++) {
              pts.setMinBound(i, 0.0);
              pts.setMaxBound(i, 1.0);
          }
      }

      protected static void normalizeGlobally(
              Projection clusterProjection,
              Projection pointProjection) {

          double min = Double.MAX_VALUE;
          double max = -Double.MAX_VALUE;
          
          int projectionDim = clusterProjection.getProjectionLength();

          for (int d = 0; d < projectionDim; d++) {
              double m = Math.min(clusterProjection.getMinimum(d), pointProjection.getMinimum(d));
              if (m < min) {
                  min = m;
              }
              m = Math.max(clusterProjection.getMaximum(d), pointProjection.getMaximum(d));
              if (m > max) {
                  max = m;
              }
          }

          // Since we're changing projection values, set min/max allowed to values
          // that cannot cause problems.
          for (int d = 0; d < projectionDim; d++) {
              clusterProjection.setMinBound(d, -Double.MAX_VALUE);
              pointProjection.setMinBound(d, -Double.MAX_VALUE);
              clusterProjection.setMaxBound(d, Double.MAX_VALUE);
              pointProjection.setMaxBound(d, Double.MAX_VALUE);
          }

          double range = max - min;
          double[] buffer = new double[projectionDim];

          // Trivial case -- all projections are [0.0, 0.0], so shift 'em to [0.5, 0.5]
          if (range <= 0.0) {

              Arrays.fill(buffer, 0.5);
              int numPoints = clusterProjection.getProjectionCount();
              for (int i = 0; i < numPoints; i++) {
                  clusterProjection.setProjection(i, buffer);
              }

              numPoints = pointProjection.getProjectionCount();
              for (int i = 0; i < numPoints; i++) {
                  pointProjection.setProjection(i, buffer);
              }

          } else { // Usual case

              int numPoints = clusterProjection.getProjectionCount();
              for (int i = 0; i < numPoints; i++) {
                  clusterProjection.getProjection(i, buffer);
                  for (int d = 0; d < projectionDim; d++) {
                      buffer[d] = (buffer[d] - min) / range;
                  }
                  clusterProjection.setProjection(i, buffer);
              }

              numPoints = pointProjection.getProjectionCount();
              for (int i = 0; i < numPoints; i++) {
                  pointProjection.getProjection(i, buffer);
                  for (int d = 0; d < projectionDim; d++) {
                      buffer[d] = (buffer[d] - min) / range;
                  }
                  pointProjection.setProjection(i, buffer);
              }
          }

          for (int d = 0; d < projectionDim; d++) {
              clusterProjection.setMinBound(d, 0.0);
              pointProjection.setMinBound(d, 0.0);
              clusterProjection.setMaxBound(d, 1.0);
              pointProjection.setMaxBound(d, 1.0);
          }
          
          pointProjection.computeMinMax();
          clusterProjection.computeMinMax();
          
      }

      /**
       * Standardize the projection's orientation to eliminate reflections and
       * rotations caused by minor perturbation of the data. This is done by
       * making the eigenvectors (matrix columns) face in the same general
       * direction as an arbitrary reference vector.
       * 
       * @param projection_matrix
       *            DoubleMatrix2D
       */
      protected static void standardizeOrientation(
                      RealMatrix projection_matrix) {
              int dimensions = projection_matrix.getColumnDimension();

              for (int d = 0; d < dimensions; d++) {
                      // Compute dot product with the reference vector (1, 1, ..., 1),
                      // which was chosen specifically to simplify this step
                      RealVector column = projection_matrix.getColumnVector(d);
                      double dot_product = 0.0;
                      for (int i=0; i<column.getDimension(); i++) {
                          dot_product += column.getEntry(i);
                      }

                      if (dot_product < 0) {
                              // Eigenvector points the wrong way; reverse it
                              int n = column.getDimension();
                              for (int i = 0; i < n; i++) {
                                      projection_matrix.setEntry(i, d, (-1.0 * column.getEntry(i)));
                              }
                      }
              }
      }
      
      private void applyGravityTransform(Projection clusterProjection,
              Projection pointProjection, double factor) {

          int clusterCount = clusters.size();
          int projectionDim = clusterProjection.getProjectionLength();

          double factorComp = 1.0 - factor;
          double[] clustFactors = new double[projectionDim];

          double[] clusterPt = new double[projectionDim];
          double[] coordPt = new double[projectionDim];

          // For each cluster
          for (int c = 0; c < clusterCount; c++) {
        	  
              clusterProjection.getProjection(c, clusterPt);
              for (int d = 0; d < projectionDim; d++) {
                  clustFactors[d] = factorComp * clusterPt[d];
              }
              
              Cluster cluster = clusters.get(c);
              IntIterator it = cluster.getMembers();
              
              while(it.hasNext()) {
            	  
            	  int id = it.getNext();
            	  
            	  pointProjection.getProjection(id, coordPt);
                  for (int d = 0; d < projectionDim; d++) {
                      coordPt[d] = factor * coordPt[d] + clustFactors[d];
                  }
                  
                  pointProjection.setProjection(id, coordPt);
              }
              
          }
      }

	@Override
	public String taskName() {
		return "projection";
	}
}
