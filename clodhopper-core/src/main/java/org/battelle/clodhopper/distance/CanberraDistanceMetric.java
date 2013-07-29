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
 * CanberraDistanceMetric.java
 *
 *===================================================================*/

/**
 * <p>The Canberra distance is the sum of of the fractional differences between 
 * each pair of coordinate elements. Each fractional difference has value between 0 and 1. 
 * If one of the coordinate elements is zero, the term is 1.0 regardless of the corresponding
 * element from the other coordinate. If both coordinate elements are 0, the
 * fractional difference is regarded as 0. The Canberra distance is very sensitive to small changes 
 * when both coordinates are near zero.</p> 
 * 
 * @author R.Scarberry
 * @since 1.0.1
 *
 */
public class CanberraDistanceMetric implements DistanceMetric {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double distance(double[] tuple1, double[] tuple2) {
        double dist = 0.0;
        final int len = tuple1.length;
        for (int i = 0; i < len; i++) {
            double c1 = tuple1[i];
            double c2 = tuple2[i];
            double denom = Math.abs(c1) + Math.abs(c2);
            if (denom != 0.0) {
                dist += Math.abs(c1 - c2) / denom;
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
