package org.battelle.clodhopper.tuple;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * FileMappedTupleList.java
 *
 *===================================================================*/
/**
 * Implements a <code>TupleList</code> backed by a single binary data file.
 * Reads and writes of the data are accomplished via random access of the file.
 *
 * @author R. Scarberry
 * @since 1.0
 *
 */
public class FileMappedTupleList extends AbstractTupleList {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileMappedTupleList.class);

    private final File file;
    private RandomAccessFile randomAccessFile;
    private ByteBuffer ioBuffer;

    /**
     * Constructor.
     *
     * @param file the file in which to store distances.
     * @param tupleLength the tuple length.
     * @param tupleCount the tuple count.
     *
     * @throws IOException if an IO error occurs.
     */
    protected FileMappedTupleList(final File file, final int tupleLength, final int tupleCount) throws IOException {
        super(tupleLength, tupleCount);
        if (file == null) {
            throw new NullPointerException();
        }
        this.file = file;
        initEmptyFile();
        open();
    }

    /**
     * Constructor.
     *
     * @param file the file to use to store distances.
     * @throws IOException if an IO error occurs.
     */
    protected FileMappedTupleList(final File file) throws IOException {
        super(0, 0);
        if (file == null) {
            throw new NullPointerException();
        }
        this.file = file;
        open();
    }

    /**
     * Factory method that creates a new <code>TupleList</code> backed by the
     * specified file.
     *
     * @param file the file for storing the data.
     * @param tupleLength the tuple length.
     * @param tupleCount the tuple count.
     * @return an instance of <code>FileMappedTupleList</code>.
     * @throws IOException if an IO problem occurs.
     */
    public static FileMappedTupleList createNew(final File file, final int tupleLength, final int tupleCount)
            throws IOException {
        return new FileMappedTupleList(file, tupleLength, tupleCount);
    }

    /**
     * Factory method that opens a <code>TupleList</code> backed by an existing
     * data file.
     *
     * @param file the file containing the tuples.
     * @return an instance of <code>FileMappedTupleList</code>.
     * @throws IOException if an IO error occurs.
     */
    public static FileMappedTupleList openExisting(final File file) throws IOException {
        return new FileMappedTupleList(file);
    }

    /**
     * Get the file backing this instance.
     *
     * @return a file instance.
     */
    public File getFile() {
        return file;
    }

    /**
     * Checks a file to see whether it contains valid tuple data.
     *
     * @param f the file to check.
     * @return true if the file validates, false otherwise.
     * @throws IOException if an IO error occurs.
     */
    public static boolean validateFile(final File f) throws IOException {
        if (f.exists() && f.isFile()) {
            DataInputStream in = null;
            try {
                in = new DataInputStream(new FileInputStream(f));
                int tupleLen = in.readInt();
                int tupleCount = in.readInt();
                long expectedFileLen = 8L * ((long) tupleLen) * tupleCount;
                return f.length() == expectedFileLen;
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
        return false;
    }

    private void initEmptyFile() throws IOException {
        DataOutputStream out = null;
        try {
            out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(this.file)));
            out.writeInt(this.tupleLength);
            out.writeInt(this.tupleCount);
            for (int i = 0; i < this.tupleCount; i++) {
                for (int j = 0; j < this.tupleLength; j++) {
                    out.writeDouble(0.0);
                }
            }
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    LOGGER.error("error closing output stream", e);
                }
            }
        }
    }

    /**
     * Get whether or not the backing file is open.
     *
     * @return true if the file is open.
     */
    public synchronized boolean isOpen() {
        return randomAccessFile != null;
    }

    /**
     * Opens the backing file. If the file is already open, this method does
     * nothing.
     *
     * @throws IOException if an IO error occurs.
     */
    public synchronized void open() throws IOException {
        if (!isOpen()) {
            boolean ok = false;
            try {
                randomAccessFile = new RandomAccessFile(this.file, "rw");
                int tlen = randomAccessFile.readInt();
                int tcount = randomAccessFile.readInt();
                ioBuffer = ByteBuffer.allocate(8 * tlen);
                this.tupleLength = tlen;
                this.tupleCount = tcount;
                ok = true;
            } finally {
                if (!ok) {
                    try {
                        close();
                    } catch (IOException e) {
                        LOGGER.error("error closing output stream", e);
                    }
                }
            }
        }
    }

    /**
     * Close the backing file if it is open.
     *
     * @throws IOException if an IO error occurs.
     */
    public synchronized void close() throws IOException {
        if (isOpen()) {
            randomAccessFile.close();
            randomAccessFile = null;
            ioBuffer = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTuple(final int n, final double[] values) {
        checkTupleIndex(n);
        checkValuesLength(values);
        checkOpen();
        try {
            randomAccessFile.seek(filePos(n));
            ioBuffer.clear();
            for (int i = 0; i < this.tupleLength; i++) {
                ioBuffer.putDouble(values[i]);
            }
            ioBuffer.flip();
            randomAccessFile.getChannel().write(ioBuffer);
        } catch (IOException e) {
            LOGGER.error("error setting tuple values", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double[] getTuple(final int n, final double[] reuseBuffer) {
        checkTupleIndex(n);
        checkOpen();
        double[] result = reuseBuffer != null && reuseBuffer.length >= tupleLength ? reuseBuffer
                : new double[tupleLength];
        try {
            randomAccessFile.seek(filePos(n));
            ioBuffer.clear();
            randomAccessFile.getChannel().read(ioBuffer);
            ioBuffer.flip();
            for (int i = 0; i < this.tupleLength; i++) {
                result[i] = ioBuffer.getDouble();
            }
        } catch (IOException e) {
            LOGGER.error("error reading tuple values", e);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getTupleValue(final int n, final int col) {
        checkTupleIndex(n);
        checkColumnIndex(col);
        checkOpen();
        try {
            randomAccessFile.seek(filePos(n) + 8L * col);
            return randomAccessFile.readDouble();
        } catch (IOException e) {
            LOGGER.error("error reading tuple value", e);
        }
        return 0.0;
    }

    // Ensures file is open.
    private void checkOpen() {
        if (!isOpen()) {
            throw new IllegalStateException("not open");
        }
    }

    private long filePos(final int n) {
        return 8L + n * ioBuffer.capacity();
    }

    protected void finalize() {
        try {
            close();
        } catch (IOException e) {
            LOGGER.error("error closing file", e);
        }
    }
}
