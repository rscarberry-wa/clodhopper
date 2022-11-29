/*
 * Copyright 2019 rescarb.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.battelle.clodhopper.examples;

import org.battelle.clodhopper.examples.generation.NormalTupleGenerator;

import java.util.Random;

/**
 *
 * @author rescarb
 */
public class SilhouetteDemo {
    
    public static void main(String[] args) {
        
        final int numTuples = 20000;
        final int tupleLen = 10;
        final int numClusters = 25;
        final double clusterMult = 4.0;
        final double standardDev = 1.0;
        final double standardDevStandardDev = 1.0;
        
        NormalTupleGenerator tupleGenerator = new NormalTupleGenerator(
                tupleLen,
                numTuples,
                numClusters,
                clusterMult,
                standardDev,
                standardDevStandardDev,
                new Random(23432L));
                
        
    }
    
}
