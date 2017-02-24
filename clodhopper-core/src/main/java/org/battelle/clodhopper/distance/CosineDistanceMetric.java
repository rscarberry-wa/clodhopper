package org.battelle.clodhopper.distance;

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
 * CosineDistanceMetric.java
 *
 *===================================================================*/
/**
 * Computes cosine distance between pairs of tuples not containing NaNs. The
 * cosine distance is derived from the Euclidean dot product formula. For two
 * tuples A and B, the cosine distance is the product of the lengths of the
 * tuples and the cosine of the angle between them.
 *
 * @author R. Scarberry
 * @since 1.0
 */
public class CosineDistanceMetric implements DistanceMetric {

    /**
     * {@inheritDoc}
     */
    @Override
    public double distance(final double[] tuple1, final double[] tuple2) {
        
        final int len = tuple1.length;
        
        double sumAB = 0;
        double sumA2 = 0, sumB2 = 0;
        
        for (int i = 0; i < len; i++) {
            sumAB += tuple1[i] * tuple2[i];
            sumA2 += tuple1[i]*tuple1[i];
            sumB2 += tuple2[i]*tuple2[i];
        } 
        
        sumA2 = Math.sqrt(sumA2);
        sumB2 = Math.sqrt(sumB2);
        
        double denom = sumA2 * sumB2;
        
        // If the denominator is zero, one or both has to be a zero tuple.
        if (denom == 0.0) {
            // If both are zero tuples, return 0.0.
            if (sumA2 == 0.0 && sumB2 == 0.0) {
                return 0.0;
            } else { // Otherwise throw an exception.
                throw new IllegalArgumentException(
                        "cosine distance cannot be computed between a zero tuple and a nonzero tuple");
            }
        }
        
        return 1.0 - (sumAB/(sumA2*sumB2));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public DistanceMetric clone() {
        try {
            return (DistanceMetric) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

}
