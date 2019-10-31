/*
 * Copyright 2017 rande.
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
package org.battelle.clodhopper.distance;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Randall Scarberry
 */
public class DistanceComputationTest {

    public static final double EPSILON = 1.0e-12;

    // A distance metric must satisfy 4 criteria:
    //
    // 1) For any 2 tuples (X, Y), D(X, Y) >= 0.0
    // 2) D(X, Y) == 0 iff X and Y are equal.
    // 3) D(X, Y) == D(Y, X)
    // 4) D(X, Z) <= D(X,Y) + D(Y,Z)
    private static List<DistanceMetric> metrics;
    private static double[][] tuples;
    private static long seed;

    @BeforeAll
    public static void beforeClass() {
        metrics = Arrays.asList(
                new CanberraDistanceMetric(),
                new ChebyshevDistanceMetric(),
                new CosineDistanceMetric(),
                new EuclideanDistanceMetric(),
                new ManhattanDistanceMetric(),
                new TanimotoDistanceMetric(),
                new HammingDistanceMetric(),
                new BrayCurtisDistanceMetric()
        );
        int numTuples = 100;
        int tupleLen = 10;

        tuples = new double[numTuples][tupleLen];
        // The first one is a zero vector.
        Arrays.fill(tuples[0], 0.0);

        // Stash the seed, so if anything fails, we can reuse the seed
        // during debugging to replicate the problem.
        seed = 741509683586205L;//System.nanoTime();

        System.out.printf("DistanceComputationTest: random seed = " + seed);

        Random random = new Random(seed);

        for (int i = 1; i < numTuples; i++) {
            double[] tuple = tuples[i];
            for (int j = 0; j < tupleLen; j++) {
                tuple[j] = randomDouble(random);
            }
        }
    }

    // returns a random double in the range [-1000.0 - +1000.0]
    private static double randomDouble(Random random) {
        return (random.nextBoolean() ? +1.0 : -1.0) * 1000.0 * random.nextDouble();
    }

    @AfterAll
    public static void afterClass() {
        metrics = null;
    }

    @Test
    public void testDistancesNeverNegative() {
        for (int i = 0; i < tuples.length; i++) {
            for (int j = i; j < tuples.length; j++) {
                for (DistanceMetric dm : metrics) {
                    if (dm.getClass() == CosineDistanceMetric.class &&
                            (isZeroTuple(tuples[i]) || isZeroTuple(tuples[j]))) {
                        continue;
                    }
                    double dist = dm.distance(tuples[i], tuples[j]);
                    assertTrue(dist >= -EPSILON,
                            String.format("failed for metric %s on tuple (%d, %d)",
                                    dm.getClass().getSimpleName(), i, j));
                }
            }
        }
    }

    @Test
    public void testDistancesZeroIffTuplesAreEqual() {
        for (int i = 0; i < tuples.length; i++) {
            for (DistanceMetric dm : metrics) {
                double dist = dm.distance(tuples[i], tuples[i]);
                assertTrue(Math.abs(dist) <= EPSILON, "failed for metric: " + dm.getClass().getSimpleName());
            }
            for (int j = i + 1; j < tuples.length; j++) {
                if (!Arrays.equals(tuples[i], tuples[j])) {
                    for (DistanceMetric dm : metrics) {
                        // The cosine distance metric doesn't work if one tuple is all zeros and the other isn't.
                        if (dm.getClass() == CosineDistanceMetric.class && (isZeroTuple(tuples[i]) || isZeroTuple(tuples[j]))) {
                            continue;
                        }
                        double dist = dm.distance(tuples[i], tuples[j]);
                        assertTrue(dist > -EPSILON, "failed for metric: " + dm.getClass().getSimpleName());
                    }
                }
            }
        }
    }

    @Test
    public void testDistancesCommutative() {
        for (int i = 0; i < tuples.length; i++) {
            for (int j = i + 1; j < tuples.length; j++) {
                for (DistanceMetric dm : metrics) {
                    // The cosine distance metric doesn't work if one tuple is all zeros and the other isn't.
                    if (dm.getClass() == CosineDistanceMetric.class && (isZeroTuple(tuples[i]) || isZeroTuple(tuples[j]))) {
                        continue;
                    }
                    double dist1 = dm.distance(tuples[i], tuples[j]);
                    double dist2 = dm.distance(tuples[j], tuples[i]);
                    assertTrue(dist1 == dist2, "failed for metric: " + dm.getClass().getSimpleName());
                }
            }
        }
    }
    
    public static boolean isZeroTuple(double[] tuple) {
        for (int i = 0; i < tuple.length; i++) {
            if (Math.abs(tuple[i]) >= EPSILON) {
                return false;
            }
        }
        return true;
    }
}
