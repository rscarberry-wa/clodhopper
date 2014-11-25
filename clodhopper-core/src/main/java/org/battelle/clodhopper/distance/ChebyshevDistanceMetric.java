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
 * 
 * ChebyshevDistanceMetric.java
 *
 *===================================================================*/
/**
 * The Chebyshev distance between two tuples is the maximum difference along any
 * dimension. The Chebyshev distance is also called the Maximum Metric, or the
 * L-infinity metric.
 *
 * @author R. Scarberry
 * @since 1.0.1
 *
 */
public class ChebyshevDistanceMetric implements DistanceMetric {

    /**
     * {@inheritDoc}
     */
    @Override
    public double distance(final double[] tuple1, final double[] tuple2) {
        double dist = 0.0;
        final int len = tuple1.length;
        for (int i = 0; i < len; i++) {
            double c1 = tuple1[i];
            double c2 = tuple2[i];
            double diff = Math.abs(c1 - c2);
            if (diff > dist) {
                dist = diff;
            }
        }
        return dist;
    }

    /**
     * {@inheritDoc}
     */
    public DistanceMetric clone() {
        try {
            return (DistanceMetric) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

}
