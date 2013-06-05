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
 * TanimotoDistanceMetric.java
 *
 *===================================================================*/

/**
 * Implementation of the Tanimoto distance metric.
 * 
 * @author R. Scarberry
 * @since 1.0
 *
 */
public class TanimotoDistanceMetric implements DistanceMetric {

	@Override
	/**
	 * {@inheritDoc}
	 */
	public double distance(double[] tuple1, double[] tuple2) {
		final int len = tuple1.length;
		double snum = 0.0;
		double sdenom = 0.0;
		for (int i=0; i<len; i++) {
			double x = tuple1[i];
			double y = tuple2[i];
			double xy = x*y;
			snum += xy;
			sdenom += (x*x + y*y - xy);
		}
		return sdenom != 0.0 ? 1.0 - snum/sdenom : 0.0;
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
