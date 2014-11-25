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
 * EuclideanDistanceMetric.java
 *
 *===================================================================*/
/**
 * A <code>DistanceMetric</code> implementation for computing euclidean. The
 * data from which distances are computed by this class may not contain NaNs.
 *
 * @author R. Scarberry
 * @since 1.0
 *
 */
public class EuclideanDistanceMetric implements DistanceMetric {

    /**
     * {@inheritDoc}
     */
    @Override
    public double distance(final double[] tuple1, final double[] tuple2) {
        // Holds the squared distance.
        double d2 = 0;
        // tuple1.length should be the same as tuple2.length.
        final int len = tuple1.length;
        for (int i = 0; i < len; i++) {
            double d = tuple1[i] - tuple2[i];
            d2 += d * d;
        }
        return Math.sqrt(d2);
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
