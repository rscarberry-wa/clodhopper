package org.battelle.clodhopper.distance;

import org.battelle.clodhopper.tuple.TupleMath;

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
    public double distance(final double[] tuple1, final double[] tuple2) {

        // The maximum of the absolute values of all the tuple values.
        final double maxA = Math.max(
                TupleMath.absMaximum(tuple1),
                TupleMath.absMaximum(tuple2));

        final int len = tuple1.length;

        double cosine = 1;
        double sx = 0, sy = 0, sxy = 0;

        if (maxA > 0.0) {
            for (int i = 0; i < len; i++) {
                double dx = tuple1[i] / maxA;
                double dy = tuple2[i] / maxA;
                sx += dx * dx;
                sy += dy * dy;
                sxy += dx * dy;
            }
            if (sxy != 0.0) {
                cosine = sxy / Math.sqrt(sx * sy);
            }
        }

        return 1.0 - Math.abs(cosine);
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
