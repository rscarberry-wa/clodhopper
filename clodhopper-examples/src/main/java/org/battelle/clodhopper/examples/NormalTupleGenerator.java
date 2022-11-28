package org.battelle.clodhopper.examples;

import gnu.trove.list.array.TIntArrayList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.battelle.clodhopper.Cluster;
import org.battelle.clodhopper.task.AbstractTask;
import org.battelle.clodhopper.task.ProgressHandler;
import org.battelle.clodhopper.tuple.ArrayTupleList;
import org.battelle.clodhopper.tuple.TupleList;
import org.battelle.clodhopper.tuple.TupleMath;
import org.battelle.clodhopper.util.ArrayIntIterator;

public class NormalTupleGenerator extends AbstractTask<ClusteredTuples> {

	private int tupleLength;
	private int tupleCount;
	private int clusterCount;
	private Random random;
	private double clusterMultiplier;
	private double standardDev;
	private double standardDevStandardDev;
	
	public NormalTupleGenerator(
			int tupleLength, 
			int tupleCount, 
			int clusterCount,
			double clusterMultiplier,
			double standardDev,
			double standardDevStandardDev,
			Random random) {
		this.tupleLength = tupleLength;
		this.tupleCount = tupleCount;
		this.clusterCount = clusterCount;
		this.clusterMultiplier = clusterMultiplier;
		this.standardDev = standardDev;
		this.standardDevStandardDev = standardDevStandardDev;
		this.random = random;
	}
	
	@Override
	protected ClusteredTuples doTask() throws Exception {
        
        ProgressHandler ph = new ProgressHandler(this, tupleCount);
        ph.postBegin();
        
        TupleList data = new ArrayTupleList(tupleLength, tupleCount);
        checkForCancel();
        
        int cc = Math.max(1, Math.min(clusterCount, tupleCount));
                
        checkForCancel();
        
        double mult = Math.max(1.0, clusterMultiplier);
        
        double meanClusterProb = 1.0/cc;
        
        double[] pdfs = new double[cc];
        
        if (mult == 1.0) {
        	Arrays.fill(pdfs, meanClusterProb);
        } else {
        	double minProb = 1.0/((mult - 1.0) * cc);
        	double maxProb = mult * minProb;
        	double d = maxProb - minProb;
        	double cdf = 0.0;
        	for (int i=0; i<cc; i++) {
        		pdfs[i] = minProb + random.nextDouble()*d;
        		cdf += pdfs[i];
        	}
        	d = 1.0/cdf;
        	for (int i=0; i<cc; i++) {
        		pdfs[i] *= d;
        	}
        }
        
        checkForCancel();
        
        double[] cdfs = new double[cc];

        for (int i=1; i<cc; i++) {
        	cdfs[i] = cdfs[i-1] + pdfs[i-1];
        }
        
        checkForCancel();
        
        NormalDist[] clusterNormals = new NormalDist[cc];
        if (standardDevStandardDev > 0.0) {
        	NormalDist normal = new NormalDist(random, standardDev, standardDevStandardDev);
        	for (int i=0; i<cc; i++) {
        		clusterNormals[i] = new NormalDist(random, 0.0, normal.nextDouble());
        	}
        }
        
        double[][] exemplars = new double[cc][tupleLength];
        TIntArrayList[] clusterIDLists = new TIntArrayList[cc];

        for (int i = 0; i < cc; i++) {
                for (int j = 0; j < tupleLength; j++) {
                        exemplars[i][j] = random.nextDouble();
                }
                clusterIDLists[i] = new TIntArrayList();
        }

        double[] buf = new double[tupleLength];

        for (int i = 0; i < tupleCount; i++) {
            
        	double d = random.nextDouble();
            int cluster = 0;
            
            for (int j=cc-1; j>=0; j--) {
                if (d >= cdfs[j]) {
                    cluster = j;
                    break;
                }
            }
            
            double[] exemplar = exemplars[cluster];
            
            NormalDist normal = clusterNormals[cluster];
            
            for (int j = 0; j < tupleLength; j++) {
            	buf[j] = exemplar[j] + normal.nextDouble();
            }
            
            data.setTuple(i, buf);
            clusterIDLists[cluster].add(i);
            
            ph.postStep();
        }

        List<Cluster> clusts = new ArrayList<Cluster>(cc);
        for (int i=0; i<cc; i++) {
        	TIntArrayList memList = clusterIDLists[i];
        	memList.trimToSize();
        	int[] members = memList.toArray();
        	clusts.add(new Cluster(members, TupleMath.average(data, new ArrayIntIterator(members))));
        }
        
        ph.postEnd();
        
        return new ClusteredTuples(data, clusts);
	}

	@Override
	public String taskName() {
		return "random gaussian tuple and cluster generation";
	}
}
