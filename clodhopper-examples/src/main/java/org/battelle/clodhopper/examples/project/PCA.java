package org.battelle.clodhopper.examples.project;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

public class PCA {

    public static final double DEFAULT_LEVEL = 0.9;
    
    public enum CovarianceType {
        COVARIANCE, CORRELATION
    }
    
    private CovarianceType mCovarianceType;
    
    private int mPCAIndices = -1;
    private double mLevel;
    private RealMatrix mData;
    
    private RealMatrix mCovariance;
    
    private RealMatrix mPrincipalComponents;
    private RealVector mVariance;
    
    /**
     * Performs principal component analysis on the data in the specified matrix.  Each 
     * row of the matrix is considered to be a complete data sample or record.  The number of columns
     * (row length) of the data is the number of measurements in each sample.
     * 
     * @param data - a matrix containing the data.
     * @param covType - determines whether the eigenvalue decomposition is performed on the
     *   covariance matrix or the correlation matrix.  Allowed values are <tt>CovarianceType.COVARIANCE</tt>
     *   or <tt>CovarianceType.CORRELATION</tt>.
     * @param level - a threshold in the interval (0 - 1] used to determine how much to reduce the
     *   dimensionality of the data matrix.  The higher the threshold, the more the dimensionality is
     *   reduced.  
     */
    public PCA(RealMatrix data, CovarianceType covType, double level) {
        if(data == null || covType == null) {
            throw new NullPointerException();
        }
        if (data.getRowDimension() == 0 || data.getColumnDimension() == 0) {
            throw new IllegalArgumentException("data matrix must have at least 1 row and 1 column");
        }
        if (level <= 0.0 || level > 1.0) {
            throw new IllegalArgumentException("level not in (0.0 - 1.0]: " + level);
        }
        mData = data;
        mCovarianceType = covType;
        mLevel = level;
        computePrincipalComponents();
    }
    
    /**
     * Performs principal component analysis on the data in the specified matrix.  Each 
     * row of the matrix is considered to be a complete data sample or record.  The number of columns
     * (row length) of the data is the number of measurements in each sample.
     * 
     * @param data - a matrix containing the data.
     * @param covType - determines whether the eigenvalue decomposition is performed on the
     *   covariance matrix or the correlation matrix.  Allowed values are <tt>CovarianceType.COVARIANCE</tt>
     *   or <tt>CovarianceType.CORRELATION</tt>.
     * @param indices - the number of columns in the computed principal components array.  Passing a
     *   value less than the number of columns in data can be used to reduce the dimensionality of the
     *   data.
     */
    public PCA(RealMatrix data, CovarianceType covType, int indices) {
        if(data == null || covType == null) {
            throw new NullPointerException();
        }
        if (indices <= 0 || indices > data.getColumnDimension()) {
            throw new IllegalArgumentException("indices not in (0 - " + data.getColumnDimension() + "]: " + indices);
        }
        mData = data;
        mCovarianceType = covType;
        mPCAIndices = indices;
        computePrincipalComponents();
    }

    public PCA(RealMatrix data) {
        this(data, CovarianceType.COVARIANCE, data.getColumnDimension());
    }
    
    public RealMatrix getPrincipalComponents() {
        return mPrincipalComponents;
    }
    
    public RealVector getVariance() {
        return mVariance;
    }
    
    public RealMatrix getCovariance() {
        return mCovariance;
    }
    
    public RealMatrix getTransformedData(boolean center) {
        RealMatrix data = mData;
        if (center) {
            data = mData.copy();
            int rows = data.getRowDimension();
            int cols = data.getColumnDimension();
            double[] colmeans = new double[cols];
            for (int i=0; i<cols; i++) {
                colmeans[i] = mean(data.getColumnVector(i));
            }
            for (int j=0; j<cols; j++) {
                double cmean = colmeans[j];
                for (int i=0; i<rows; i++) {
                    data.setEntry(i, j, data.getEntry(i, j) - cmean);
                }
            }
        }
        return data.multiply(getPrincipalComponents());
    }
    
    public static double mean(RealVector vector) {
        double mean = Double.NaN;
        int len = vector.getDimension();
        if (len > 0) {
                double sum = 0.0;
                int n = 0;
                for (int i=0; i<len; i++) {
                        double v = vector.getEntry(i);
                        if (!Double.isNaN(v)) {
                                sum += v;
                                n++;
                        }
                }
                if (n > 0) {
                        mean = sum/n;
                }
        }
        return mean;
    }

    public RealMatrix getTransformedData() {
        return getTransformedData(true);
    }
    
    private void computePrincipalComponents() {

        // Compute the covariance matrix where each row of mData is
        // regarded as a separate observation or record.
        final RealMatrix transform = this.mData.transpose();
        
        this.mCovariance = transform.multiply(this.mData);        
        
        if (this.mCovarianceType == CovarianceType.CORRELATION) {
          // This transforms mCovariance to a correlation matrix in place.
          this.mCovariance = correlation(this.mCovariance);
        }

        // Do the eigenvalue decomposition of the covariance (or correlation) matrix.
        final EigenDecomposition ed = new EigenDecomposition(this.mCovariance, 0.0);
        
        final double[] realEigenvalues = ed.getRealEigenvalues();
        
        // If an explicit number of indices was not asked for (i.e., mPCAIndices == -1), then
        // compute mPCAIndices from mLevel, which is in (0 - 1].
        if (this.mPCAIndices < 0) {
          this.mPCAIndices = numPCAIndices(realEigenvalues, this.mLevel);
        }

        // The number of eigenvalues
        final int eigenCount = realEigenvalues.length;
        // The number of columns in the array of principal components.
        final int pcaCols = this.mPCAIndices;

        this.mPrincipalComponents = new Array2DRowRealMatrix(eigenCount, pcaCols);
        this.mVariance = new ArrayRealVector(pcaCols);
        
        // Copy the eigenvectors to mPrincipalComponents.
        for (int i = 0; i < pcaCols; i++) {
          
          RealVector eigenVec = ed.getEigenvector(i);

          for (int j=0; j<eigenCount; j++) {
        	this.mPrincipalComponents.setEntry(j, i, eigenVec.getEntry(j));  
          }
          
          // Copy the real part of the eigenvalue to mVariance.
          this.mVariance.setEntry(i, realEigenvalues[i]);
        }
    }
    
    private static int numPCAIndices(double[] sortedEigenvalues, double level) {
        int index = Math.max(0, sortedEigenvalues.length - 1);
        if (index > 0) {
            double sum = 0.0;
            for (int i=0; i<sortedEigenvalues.length; i++) {
            	sum += sortedEigenvalues[i];
            }
            double testValue = sortedEigenvalues[sortedEigenvalues.length - 1 - index]/sum;
            double threshold = -Math.pow(10, -6.0);
            while((testValue - level) < threshold && index > 0) {
                index--;
                testValue += sortedEigenvalues[sortedEigenvalues.length - 1 - index]/sum;
            }
        }
        return Math.max(index, 1);
    }
    
    public static RealMatrix correlation(RealMatrix covariance) {
    	final int cols = covariance.getColumnDimension();
    	for (int i=cols; --i >= 0; ) {
    		for (int j=i; --j >= 0; ) {
    			double stdDev1 = Math.sqrt(covariance.getEntry(i,i));
    			double stdDev2 = Math.sqrt(covariance.getEntry(j,j));
    			double cov = covariance.getEntry(i,j);
    			double corr = cov / (stdDev1*stdDev2);
    			
    			covariance.setEntry(i,j,corr);
    			covariance.setEntry(j,i,corr); // symmetric
    		}
    	}
    	for (int i=cols; --i >= 0; ) covariance.setEntry(i,i,1);
    	return covariance;	
    }
}
