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
 * DistanceMetric.java
 *
 *===================================================================*/
/**
 * A class implements <code>DistanceMetric</code> if it is meant to compute
 * distances between pairs of tuples. All implementations should provide
 * deep-copy clone methods.
 *
 * @author R. Scarberry
 *
 */
public interface DistanceMetric extends Cloneable {

    /**
     * Computes the distance between tuple data contained in two arrays of the
     * same length.
     *
     * @param tuple1
     * @param tuple2
     *
     * @return
     */
    double distance(double[] tuple1, double[] tuple2);

    /**
     * @return a deep copy of the instance.
     */
    DistanceMetric clone();

}
