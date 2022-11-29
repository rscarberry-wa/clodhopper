package org.battelle.clodhopper.examples.generation;

import gnu.trove.list.array.TIntArrayList;
import org.battelle.clodhopper.Cluster;
import org.battelle.clodhopper.task.AbstractTask;
import org.battelle.clodhopper.task.ProgressHandler;
import org.battelle.clodhopper.tuple.ArrayTupleList;
import org.battelle.clodhopper.tuple.TupleList;
import org.battelle.clodhopper.tuple.TupleMath;
import org.battelle.clodhopper.util.ArrayIntIterator;

import java.util.*;

public class CircularArcTupleGenerator extends AbstractTask<ClusteredTuples> {

    private static final double TWO_PI = 2.0 * Math.PI;
    private static final double MAX_RADIUS = 0.4;

    // Represents a section of a circle in 2D space. arcEnd and arcBegin are assumed to be
    // in radians.
    private static class Arc {
        private final double radius;
        private final double arcBegin;
        private final double arcEnd;

        Arc(double radius, double arcBegin, double arcEnd) {
            this.radius = radius;
            this.arcBegin = arcBegin;
            this.arcEnd = arcEnd;
        }

        public double arcLength() {
            return Math.abs(arcEnd - arcBegin) * radius;
        }

        public double getRadius() {
            return radius;
        }

        public double getArcBegin() {
            return arcBegin;
        }

        public double getArcEnd() {
            return arcEnd;
        }
    }

    private final int tupleCount;
    private final int clusterCount;
    private final double standardDev;
    private final Random random;

    public CircularArcTupleGenerator(int tupleCount, int clusterCount, double standardDev, Random random) {
        this.tupleCount = tupleCount;
        this.clusterCount = clusterCount;
        this.standardDev = standardDev;
        this.random = random;
    }

    @Override
    protected ClusteredTuples doTask() throws Exception {

        ProgressHandler ph = new ProgressHandler(this, tupleCount);
        ph.postBegin();

        checkParameters();

        // Generate the arcs on which points will be placed with normally-distributed noise.
        Arc[] arcs = new Arc[clusterCount];
        for (int i=0; i<clusterCount; i++) arcs[i] = randomArc(random);

        checkForCancel();

        // We want the density of the points on the arcs to be equal. So the probability of
        // a point being placed on an arc should be proportional to the arc length.
        final double totalArcLength = Arrays.asList(arcs).stream().mapToDouble(Arc::arcLength).sum();
        double[] probabilities = Arrays.asList(arcs).stream().mapToDouble(arc -> arc.arcLength()/totalArcLength).toArray();

        NormalDist normalDist = new NormalDist(random, standardDev, standardDev);

        TIntArrayList[] clusterIDLists = new TIntArrayList[clusterCount];
        for (int i=0; i<clusterCount; i++) {
            clusterIDLists[i] = new TIntArrayList();
        }

        checkForCancel();

        TupleList tupleList = new ArrayTupleList(2, tupleCount);

        for (int i=0; i<tupleCount; i++) {
            int arc = pickArc(probabilities, random);
            double[] tuple = addNoise(randomPointOnArc(arcs[arc], random), normalDist);
            tupleList.setTuple(i, tuple);
            clusterIDLists[arc].add(i);
        }

        checkForCancel();

        List<Cluster> clusters = new ArrayList<>(clusterCount);
        for (int i=0; i<clusterCount; i++) {
            TIntArrayList memberList = clusterIDLists[i];
            memberList.trimToSize();
            int[] members = memberList.toArray();
            clusters.add(new Cluster(members, TupleMath.average(tupleList, new ArrayIntIterator(members))));
        }

        ph.postEnd();

        return new ClusteredTuples(tupleList, clusters);
    }

    @Override
    public String taskName() {
        return "random generation of clusters along circular arcs";
    }

    // Run at the beginning of doTask to ensure the parameters are valid.
    private void checkParameters() {
        if (tupleCount <= 0) throw new IllegalArgumentException("tupleCount must be > 0: " + tupleCount);
        if (clusterCount <= 0 || clusterCount > tupleCount) throw new IllegalArgumentException(
                String.format("clusterCount not in range [1 - %d]: %d", tupleCount, clusterCount)
        );
        if (standardDev < 0) throw new IllegalArgumentException("standardDev must be >= 0: " + standardDev);
        Objects.requireNonNull(random);
    }

    private static Arc randomArc(Random random) {
        double arc1 = TWO_PI * random.nextDouble();
        double arc2 = TWO_PI * random.nextDouble();
        return new Arc(MAX_RADIUS * random.nextDouble(), Math.min(arc1, arc2), Math.max(arc1, arc2));
    }

    private static double[] randomPointOnArc(Arc arc, Random random) {
        double alpha = arc.getArcBegin() + (arc.getArcEnd() - arc.getArcBegin()) * random.nextDouble();
        double x = 0.5 + arc.getRadius()*Math.cos(alpha);
        double y = 0.5 + arc.getRadius()*Math.sin(alpha);
        return new double[] { x, y };
    }

    private static double[] addNoise(double[] point, NormalDist normalDist) {
        final double[] result = new double[point.length];
        System.arraycopy(point, 0, result, 0, point.length);
        for (int i=0; i<result.length; i++) result[i] += normalDist.nextDouble();
        return result;
    }

    private static int pickArc(double[] arcProbabilities, Random random) {
        double value = random.nextDouble();
        double sum = 0.0;
        for (int i=0; i<arcProbabilities.length; i++) {
            if (value >= sum && value < sum + arcProbabilities[i]) return i;
            sum += arcProbabilities[i];
        }
        return arcProbabilities.length - 1;
    }
}
