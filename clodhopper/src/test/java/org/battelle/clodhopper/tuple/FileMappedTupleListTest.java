package org.battelle.clodhopper.tuple;

import static org.junit.Assert.*;

import java.io.*;
import java.util.*;

import org.battelle.clodhopper.tuple.ArrayTupleList;
import org.battelle.clodhopper.tuple.FileMappedTupleList;
import org.battelle.clodhopper.tuple.TupleList;
import org.junit.*;

public class FileMappedTupleListTest {

	private File tempFile;
	
	@Before
	public void setup() throws Exception {
		tempFile = File.createTempFile("fileMappedTuples", "tmp");
		tempFile.deleteOnExit();
	}
	
	@Test
	public void testOne() throws Exception {
		
		Random random = new Random();
		int tlen = 5 + random.nextInt(20);
		int tcount = 20 + random.nextInt(100);
		
		TupleList arrayTuples = new ArrayTupleList(tlen, tcount);
		FileMappedTupleList fmTuples = FileMappedTupleList.createNew(tempFile, tlen, tcount);
	
		double[] buffer = new double[tlen];

		for (int i=0; i<tcount; i++) {
			for (int j=0; j<tlen; j++) {
				buffer[j] = random.nextDouble();
			}
			arrayTuples.setTuple(i, buffer);
			fmTuples.setTuple(i, buffer);
		}
		
		fmTuples.close();
		
		fmTuples = FileMappedTupleList.openExisting(tempFile);
		assertTrue(fmTuples.getTupleCount() == tcount);
		assertTrue(fmTuples.getTupleLength() == tlen);
		
		double[] buffer2 = new double[tlen];
		for (int i=0; i<tcount; i++) {
			arrayTuples.getTuple(i, buffer);
			fmTuples.getTuple(i, buffer2);
			for (int j=0; j<tlen; j++) {
				assertTrue(buffer[j] == buffer2[j]);
			}
		}
		
		fmTuples.close();
	}

}
