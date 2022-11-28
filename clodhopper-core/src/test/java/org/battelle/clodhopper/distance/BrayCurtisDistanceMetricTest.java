package org.battelle.clodhopper.distance;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class BrayCurtisDistanceMetricTest {

    @Test
    public void testDistance() {

        BrayCurtisDistanceMetric bc = new BrayCurtisDistanceMetric();

        Random random = new Random(90903L);
        final int loops = 100;
        for (int loop=0; loop<loops; loop++) {

            // Generate two tuples with all positive values.
            double[] tuple1 = generateTuple(10, 1.0, 100.0, random);
            double[] tuple2 = generateTuple(10, 1.0, 100.0, random);

            // The distance of a tuple to itself should be 0.
            assertEquals(0.0, bc.distance(tuple1, tuple1));
            assertEquals(0.0, bc.distance(tuple2, tuple2));

            double d = bc.distance(tuple1, tuple2);

            // BC has the property, that if all values are positive, the distance falls within [0 - 1]
            assertTrue(d >= 0.0);
            assertTrue(d <= 1.0);

            // With mirrored tuples, the denominator will be 0, thus the distance is +INFINITY
            for (int i=0; i<tuple1.length; i++) {
                tuple2[i] = -tuple1[i];
            }

            assertEquals(Double.POSITIVE_INFINITY, bc.distance(tuple1, tuple2));
        }
    }

    private static double[] generateTuple(final int len, final double min, final double max, final Random random) {
        double[] result = new double[len];
        Arrays.fill(result, min);
        if (max != min) {
            double spread = max - min;
            for (int i=0; i<len; i++) {
                result[i] += random.nextDouble() * spread;
            }
        }
        return result;
    }

}