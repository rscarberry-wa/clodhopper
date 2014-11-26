package org.battelle.clodhopper.tuple;

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
 * Tupleable.java
 *
 *===================================================================*/
/**
 * Defines entities that provide a fixed-length tuple signature. Such a
 * signature is simply a representation as an array of doubles.
 *
 * @author R.Scarberry
 * @since 1.0
 *
 */
public interface Tupleable {

    /**
     * Get the tuple signature for the object.
     *
     * @return the tuple signature for the receiver.
     */
    double[] getTupleSignature();

}
