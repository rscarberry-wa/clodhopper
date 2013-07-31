package org.battelle.clodhopper.tuple;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

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
 * FSTupleListFactory.java
 *
 *===================================================================*/

/**
 * An implementation of <code>TupleListFactory</code> that stores <code>TupleList</code>s
 * on the file system.
 * 
 * @author R. Scarberry
 * @since 1.0
 *
 */
public class FSTupleListFactory implements TupleListFactory {
    
    private static final String SINGLE_FILE_PREFIX = "__tuples_s__";
    private static final String TUPLE_FILE_EXTENSION = ".tpl";
    private static final String MULTI_FILE_DIRECTORY = "multi";
    
    // Half a gig
    public static final long DEFAULT_RAM_THRESHOLD = 512L*1024L*1024L;
    // Four gig
    public static final long DEFAULT_SINGLE_FILE_THRESHOLD = 4L*1024L*1024L*1024L;
    // Half a gig
    public static final long DEFAULT_SINGLE_FILE_SIZE = 512L*1024L*1024L;
    
    private long ramThreshold;
    private long singleFileThreshold;
    private long singleFileSize;
    
    // Root directory of the factory.
    private File directory;
    
    private Map<String, Object> tupleListMap = new HashMap<String, Object> ();
    private Object singleFileSentinel = new Object();
    private Object multiFileSentinel = new Object();
    
    /**
     * Constructor.  The default RAM and file thresholds are used.
     * 
     * @param directory root directory for the factory.  All tuple data for this factory
     *   exists under this directory.
     * 
     * @throws TupleListFactoryException
     */
    public FSTupleListFactory(File directory) throws TupleListFactoryException {
        this(directory, DEFAULT_RAM_THRESHOLD, DEFAULT_SINGLE_FILE_THRESHOLD, DEFAULT_SINGLE_FILE_SIZE);
    }
    
    /**
     * Constructor
     * 
     * @param directory root directory for the factory.  All tuple data for this factory
     *   exists under this directory.
     * @param ramThreshold the threshold for storing tuple data in RAM. If the memory required by a 
     *   tuple list is less than this threshold, the factory returns a memory resident tuple list class.
     * @param singleFileThreshold the threshold for being able to store the data for a tuple list
     *   in a single file.  If the space required for a tuple list exceeds this threshold, its data
     *   is spread over multiple files.
     * @param singleFileSize the maximum file size for tuple lists that span multiple files.
     * 
     * @throws TupleListFactoryException
     */
    public FSTupleListFactory(File directory, long ramThreshold, 
            long singleFileThreshold, long singleFileSize) throws TupleListFactoryException {
        
        if (directory == null) {
            throw new NullPointerException();
        }
        if (directory.exists() && !directory.isDirectory()) {
            throw new TupleListFactoryException("not a directory: " + directory.getAbsolutePath());
        }
        
        this.directory = directory;
        this.ramThreshold = ramThreshold;
        this.singleFileThreshold = singleFileThreshold;
        this.singleFileSize = singleFileSize;
        
        if (!this.directory.exists()) {
            if (!this.directory.mkdir()) {
                throw new TupleListFactoryException("could not create directory: " + this.directory.getAbsolutePath());
            }
        }
        
        File multiDir = multiDirectory();
        
        if (!multiDir.exists()) {
            if (!multiDir.mkdir()) {
                throw new TupleListFactoryException("could not create directory for multiple file tuples: " + multiDir.getAbsolutePath());
            }
        } else if (!multiDir.isDirectory()) {
            throw new TupleListFactoryException("file for multiple file tuples exists but is not a directory: " + multiDir.getAbsolutePath());
        }
        
        loadExistingTupleNames();
    }
    
    /**
     * Returns the home directory of the factory.
     * 
     * @return a directory
     * 
     * @since 1.0.1
     */
    public File getDirectory() {
    	return directory;
    }
    
    /**
     * {@inheritDoc}
     */
    public synchronized Set<String> tupleListNames() {
        return new TreeSet<String> (tupleListMap.keySet());
    }
    
    /**
     * {@inheritDoc}
     */
    public synchronized boolean hasTuplesFor(String name) {
        return tupleListMap.containsKey(name);
    }

    // Used by the constructor to load up the names of all existing tuple lists.
    //
    private void loadExistingTupleNames() throws TupleListFactoryException {
        
        File[] singleFiles = this.directory.listFiles(new FileFilter() {
           @Override
           public boolean accept(File f) {
               if (f.isFile()) {
                   String name = f.getName();
                   return name.startsWith(SINGLE_FILE_PREFIX) && name.endsWith(TUPLE_FILE_EXTENSION);
               }
               return false;
           }
        });
        
        for (int i=0; i<singleFiles.length; i++) {
            File f = singleFiles[i];
            try {
                if (FileMappedTupleList.validateFile(f)) {
                    String fname = f.getName();
                    String tupleName = fname.substring(SINGLE_FILE_PREFIX.length(), 
                            fname.length() - TUPLE_FILE_EXTENSION.length());
                    tupleListMap.put(tupleName, singleFileSentinel);
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        
        File[] multiDirs = this.multiDirectory().listFiles();
        for (int i=0; i<multiDirs.length; i++) {
            File dir = multiDirs[i];
            try {
                if (MultiFileMappedTupleList.validateDirectory(dir)) {
                    tupleListMap.put(dir.getName(), multiFileSentinel);
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized TupleList createNewTupleList(String name, int tupleLength, int tupleCount) 
    		throws TupleListFactoryException {
        
        if (name == null) {
            throw new NullPointerException();
        }
        
        if (tupleListMap.containsKey(name)) {
            throw new TupleListFactoryException("tuples already exist for name " + name);
        }
        
        TupleList tuples = null;
        
        long dataLen = 8L*tupleLength*tupleCount;
        if (dataLen <= this.ramThreshold) {
            tuples = new ArrayTupleList(tupleLength, tupleCount);
        } else if (dataLen <= this.singleFileThreshold) {
            try {
                tuples = FileMappedTupleList.createNew(singleFileForTuples(name), tupleLength, tupleCount);
            } catch (IOException ioe) {
                throw new TupleListFactoryException(ioe);
            }
        } else {
           try {
               int divisions = (int) (((double) dataLen)/this.singleFileSize);
               tuples = MultiFileMappedTupleList.createNew(multiDirForTuples(name), tupleLength, tupleCount, divisions);
           } catch (IOException ioe) {
               throw new TupleListFactoryException(ioe);
           }
        }
        
        tupleListMap.put(name, tuples);
        
        return tuples;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized TupleList openExistingTupleList(String name) throws TupleListFactoryException {
        TupleList tuples = null;
        if (!tupleListMap.containsKey(name)) {
            throw new TupleListFactoryException("tuples do not exist for name " + name);
        }
        Object o = tupleListMap.get(name);
        try {
            if (o == this.singleFileSentinel) {
                File f = singleFileForTuples(name);
                if (f.length() <= this.ramThreshold) {
                    tuples = ArrayTupleList.loadFromFile(f);
                } else {
                    tuples = FileMappedTupleList.openExisting(f);
                }
                tupleListMap.put(name, tuples);
            } else if (o == this.multiFileSentinel) {
                tuples = MultiFileMappedTupleList.openExisting(multiDirForTuples(name));
                tupleListMap.put(name, tuples);
            } else if (o instanceof TupleList) {
                tuples = (TupleList) o;
                if (tuples instanceof FileMappedTupleList) {
                    ((FileMappedTupleList) tuples).open();
                } else if (tuples instanceof MultiFileMappedTupleList) {
                    ((MultiFileMappedTupleList) tuples).open();
                }
            }
        } catch (IOException e) {
            throw new TupleListFactoryException(e);
        }
        return tuples;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized TupleList copyTupleList(String nameForCopy, TupleList original) throws TupleListFactoryException {
        final int tupleLength = original.getTupleLength();
        final int tupleCount = original.getTupleCount();
        
        TupleList copy = createNewTupleList(nameForCopy, tupleLength, tupleCount);
        
        double[] buffer = new double[tupleLength];
        for (int i=0; i<tupleCount; i++) {
            copy.setTuple(i, original.getTuple(i, buffer));
        }
        
        return copy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void deleteTupleList(TupleList tuples) throws TupleListFactoryException {
        
        String name = nameAssociatedWithTuples(tuples);
        if (name == null) {
            throw new TupleListFactoryException("tuples not associated with this factory");
        }
        
        try {
            if (tuples instanceof FileMappedTupleList) {
                FileMappedTupleList fmTupleList = (FileMappedTupleList) tuples;
                File f = fmTupleList.getFile();
                fmTupleList.close();
                if (!f.delete()) {
                    throw new TupleListFactoryException("could not delete file for tuples associated with name " + name);
                }
            } else if (tuples instanceof MultiFileMappedTupleList) {
                MultiFileMappedTupleList mfmTupleList = (MultiFileMappedTupleList) tuples;
                File dir = mfmTupleList.getDirectory();
                mfmTupleList.close();
                if (!dir.delete()) {
                    throw new TupleListFactoryException("could not delete directory for tuples associated with name " + name);
                }
            }
            tupleListMap.remove(name);
        } catch (TupleListFactoryException tle) {
            throw tle;
        } catch (IOException e) {
            throw new TupleListFactoryException(e);
        }
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void closeTupleList(TupleList tuples) throws TupleListFactoryException {
        
        String name = nameAssociatedWithTuples(tuples);
        if (name == null) {
            throw new TupleListFactoryException("tuples not associated with this factory");
        }
        
        try {
            
            if (tuples instanceof FileMappedTupleList) {
                ((FileMappedTupleList) tuples).close();
                tupleListMap.put(name, singleFileSentinel);
            } else if (tuples instanceof MultiFileMappedTupleList) {
                ((MultiFileMappedTupleList) tuples).close();
                tupleListMap.put(name, multiFileSentinel);
            } else {
                File f = singleFileForTuples(name);
                ArrayTupleList.saveToFile(tuples, f);
                tupleListMap.put(name, singleFileSentinel);
            } 
            
        } catch (IOException ioe) {
            throw new TupleListFactoryException(ioe);
        }
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void closeAll() throws TupleListFactoryException {
        List<TupleList> openTupleLists = new ArrayList<TupleList> ();
        Iterator<Entry<String, Object>> it = tupleListMap.entrySet().iterator();
        while(it.hasNext()) {
            Entry<String, Object> entry = it.next();
            if (entry.getValue() instanceof TupleList) {
                openTupleLists.add((TupleList) entry.getValue());
            }
        }
        for (TupleList tl : openTupleLists) {
            closeTupleList(tl);
        }
    }

    // Finds the name associated with the specified tuple list.
    //
    private String nameAssociatedWithTuples(TupleList tuples) {
        
            Iterator<Entry<String, Object>> it = tupleListMap.entrySet().iterator();
            
            while(it.hasNext()) {
                Entry<String, Object> entry = it.next();
                if (entry.getValue() == tuples) {
                    return entry.getKey();
                }
            }

            return null;
    }
    
    // Returns a file object for a tuple list to be stored in a single file.
    //
    private File singleFileForTuples(String name) {
        return new File(directory, SINGLE_FILE_PREFIX + name + TUPLE_FILE_EXTENSION);
    }
    
    // Returns the directory file to be used for tuples spanning multiple files.
    //
    private File multiDirForTuples(String name) {
        return new File(multiDirectory(), name);
    }
    
    // Returns the parent directory for all tuple lists to be associated with multiple files.
    private File multiDirectory() {
        return new File(directory, MULTI_FILE_DIRECTORY);
    }
}
