package org.battelle.clodhopper.tuple;

import java.io.*;
import java.util.Arrays;
import java.util.Comparator;

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
 * MultiFileMappedTupleList.java
 *
 *===================================================================*/

public class MultiFileMappedTupleList extends AbstractTupleList {

    public static final String FILE_EXTENSION = ".tps";
    public static final String FILE_PREFIX = "_part_";
    
    private File directory;
    private FileMappedTupleList[] tupleLists;
    private int tuplesPerDivision;
    
    private MultiFileMappedTupleList(File directory, int tupleLength, int tupleCount, int divisions)
        throws IOException {
        super(tupleLength, tupleCount);
        if (directory == null) {
            throw new NullPointerException();
        }
        if (divisions <= 0) {
            throw new IllegalArgumentException("divisions must be > 0: " + divisions);
        }
        this.directory = directory;
        initEmptyDirectory(divisions);
        open(false);
    }
    
    private MultiFileMappedTupleList(File directory) throws IOException {
        super(0, 0);
        open(true);
    }
    
    public static MultiFileMappedTupleList createNew(File directory, int tupleLength, int tupleCount, int divisions) throws IOException {
        return new MultiFileMappedTupleList(directory, tupleLength, tupleCount, divisions);
    }
    
    public static MultiFileMappedTupleList openExisting(File directory) throws IOException {
        return new MultiFileMappedTupleList(directory);
    }
    
    public File getDirectory() {
        return directory;
    }
    
    @Override
    public void setTuple(int n, double[] values) {
        super.checkTupleIndex(n);
        tupleLists[n/tuplesPerDivision].setTuple(n%tuplesPerDivision, values);
    }

    @Override
    public double[] getTuple(int n, double[] reuseBuffer) {
        super.checkTupleIndex(n);
        return tupleLists[n/tuplesPerDivision].getTuple(n%tuplesPerDivision, reuseBuffer);
    }

    @Override
    public double getTupleValue(int n, int col) {
        super.checkTupleIndex(n);
        super.checkColumnIndex(col);
        return tupleLists[n/tuplesPerDivision].getTupleValue(n%tuplesPerDivision, col);
    }

    private void initEmptyDirectory(int divisions) throws IOException {
        
        if (!this.directory.exists()) {
            if (!this.directory.mkdir()) {
                throw new IOException("could not create directory");
            }
        } else if (!this.directory.isDirectory()) {
            throw new IOException("not a directory: " + directory.getAbsolutePath());
        }
        File[] existing = existingTupleFiles(this.directory);
        for (int i=0; i<existing.length; i++) {
            if (!existing[i].delete()) {
                throw new IOException("failure deleting existing file: " + existing[i].getAbsolutePath());
            }
        }
        
        if (divisions > this.tupleCount) {
            divisions = this.tupleCount;
        }
        
        int tuplesPerFile = tupleCount/divisions;
        int tuplesSoFar = 0;
        
        for (int i=0; i<divisions; i++) {
            int tuplesThisDivision = Math.min(tuplesPerFile, this.tupleCount - tuplesSoFar);
            String filename = FILE_PREFIX + i + FILE_EXTENSION;
            FileMappedTupleList.createNew(new File(this.directory, filename), this.tupleLength, tuplesThisDivision);
            tuplesSoFar += tuplesThisDivision;
        }
        
        assert tuplesSoFar == this.tupleCount;
    }
    
    public void open() throws IOException {
        open(false);
    }
    
    private void open(boolean setDimensions) throws IOException {
        File[] existing = existingTupleFiles(this.directory);
        this.tupleLists = new FileMappedTupleList[existing.length];
        int tupleLength = 0;
        int tupleCount = 0;
        for (int i=0; i<existing.length; i++) {
            this.tupleLists[i] = FileMappedTupleList.openExisting(existing[i]);
            if (i == 0) {
                tupleLength = this.tupleLists[i].getTupleLength();
            } else {
                if (this.tupleLists[i].getTupleLength() != tupleLength) {
                    throw new IOException(String.format("mismatched tuple lengths in data files: %d != %d", 
                            this.tupleLists[i].getTupleLength(), tupleLength));
                }
            }
            tupleCount += this.tupleLists[i].getTupleCount();
        }
        if (setDimensions) {
            this.tupleLength = tupleLength;
            this.tupleCount = tupleCount;
        } else { // If not setting the dimensions, they must match what they already are.
            if (this.tupleLength != tupleLength || this.tupleCount != tupleCount) {
                throw new IOException(String.format("unexpected tuple length or tuple count: %d != %d and/or %d != %d",
                        tupleLength, this.tupleLength, tupleCount, this.tupleCount));
            }
        }
        // This is always set, though.
        this.tuplesPerDivision = this.tupleCount/this.tupleLists.length;
    }
    
    public void close() throws IOException {
        for (int i=0; i<this.tupleLists.length; i++) {
            this.tupleLists[i].close();
        }
    }
    
    private static File[] existingTupleFiles(File directory) {
        // Delete existing tuple files, if any.
        File[] existing = directory.listFiles(new FileFilter() {
           @Override
           public boolean accept(File f) {
               String name = f.getName();
               return name.startsWith(FILE_PREFIX) && name.endsWith(FILE_EXTENSION);
           }
        });
        if (existing.length > 0) {
            Comparator<File> fileComp = new Comparator<File> () {
                @Override
                public int compare(File o1, File o2) {
                    int prefLen = FILE_PREFIX.length();
                    int extLen = FILE_EXTENSION.length();
                    String name1 = o1.getName();
                    String name2 = o2.getName();
                    int n1 = Integer.parseInt(name1.substring(prefLen, name1.length() - extLen));
                    int n2 = Integer.parseInt(name2.substring(prefLen, name2.length() - extLen));
                    return n1 < n2 ? -1 : n1 > n2 ? +1 : 0;
                }
            };
            Arrays.sort(existing, fileComp);
        }
        return existing;
    }
    
    public static boolean validateDirectory(File directory) throws IOException {
        File[] files = existingTupleFiles(directory);
        final int n = files.length;
        if (n > 0) {
            int[] dimensions = tupleDimensions(files[0]);
            if (dimensions != null) {
                for (int i=1; i<n; i++) {
                    int[] dims = tupleDimensions(files[i]);
                    if (dims == null) return false;
                    if (dims[0] != dimensions[0]) return false;
                    if (dims[1] != dimensions[1] && i < n-1) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
    
    public static int[] tupleDimensions(File f) throws IOException {
        if (f.isFile()) {
            long flen = f.length();
            DataInputStream in = null;
            try {
                in = new DataInputStream(new FileInputStream(f));
                int tupleLength = in.readInt();
                int tupleCount = in.readInt();
                
                long expectedFlen = 8L * ((long) tupleLength) * tupleCount;
                if (flen == expectedFlen) {
                    return new int[] { tupleLength, tupleCount };
                }
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException ioe) {
                    }
                }
            }
        }
        return null;
    }
}
