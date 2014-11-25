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
 * ManhattanDistanceMetric.java
 *
 *===================================================================*/
/**
 * Instances of this class compute Manhattan distances from data not containing
 * NaNs. This type of distance metric is also called Taxicab or L1 distances.
 *
 * @author R. Scarberry
 * @since 1.0
 *
 */
public class ManhattanDistanceMetric implements DistanceMetric {

    @Override
    /**
     * {@inheritDoc}
     */
    public double distance(final double[] tuple1, final double[] tuple2) {
        double d = 0;
        final int len = tuple1.length;
        for (int i = 0; i < len; i++) {
            d += Math.abs(tuple1[i] - tuple2[i]);
        }
        return d;
    }

    @Override
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
