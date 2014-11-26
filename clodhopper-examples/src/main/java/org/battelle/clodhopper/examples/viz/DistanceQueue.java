package org.battelle.clodhopper.examples.viz;

import java.util.*;
import gnu.trove.set.hash.*;
import java.io.IOException;

/**
 * <p>Title: DistanceQueue</p>
 *
 * <p>Description: Queue for containing integer ids associated with distances
 *   The method <tt>remove()</tt> returns the highest distance
 *   id in the queue.  If multiple ids tie for highest distance, the
 *   one that has been in the queue the longest is returned.
 *   That is, removal is FIFO for objects with the same distance.
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: Battelle</p>
 *
 * @author R. Scarberry
 * @version 1.0
 */
public final class DistanceQueue implements java.io.Serializable {

    private static final long serialVersionUID = 2173417056069014466L;
    
    // Transient, since these are handled manually in readObject/writeObject.
    private transient LinkedList<Entry> mEntries;
    private transient TIntHashSet mIDSet;
    
    private double mMaxDistance;
    private long mSNCounter;

    /**
     * Constructs a new queue with the default initial capacity of 20 and no
     * limit on the maximum allowed distance.
     */
    public DistanceQueue() {
        this(20, Double.MAX_VALUE);
    }

    /**
     * Constructs a new queue with the given initial capacity and no
     * limit on the maximum allowed distance.
     * @param capacity the initial capacity.
     * @throws IllegalArgumentException if capacity is not positive.
     */
    public DistanceQueue(int capacity) {
        this(capacity, Double.MAX_VALUE);
    }

    /**
     * Constructs a new queue with the given initial capacity and
     * maximum allowed distance.
     * @param capacity the initial capacity.
     * @param maxDistance the upper limit for distance of objects added to the
     *   queue.
     * @throws IllegalArgumentException if capacity is not positive.
     */
    public DistanceQueue(int capacity, double maxDistance) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("invalid capacity: " + capacity);
        }
        mEntries = new LinkedList<Entry>();
        mMaxDistance = maxDistance;
        mIDSet = new TIntHashSet(capacity);
    }

    /**
     * Add an id associating with it the given distance.
     * @param id the id to be added.
     * @param distance the distance to associate with the id.
     * @throws IllegalArgumentException if distance &gt; max distance allowed.
     */
    public void add(int id, double distance) {
        
        if (distance > mMaxDistance) {
            throw new IllegalArgumentException("illegal distance: " + distance
                    + " > " + mMaxDistance);
        }
        
        if (contains(id)) {
            remove(id);
        }
        
        if (mSNCounter == Long.MAX_VALUE) {
            // Extremely unlikely, but here just to be sure.
            resetSerialNumbers();
        }
        
        int n = binarySearch(distance);
        if (n < 0) {
            n = -n - 1;
        }
        
        mEntries.add(n, new Entry(id, distance, mSNCounter++));
        mIDSet.add(id);
    }

    private int binarySearch(double distance) {
        
        int low = 0;
        int high = mEntries.size() - 1;

        while (low <= high) {
            
            int mid = (low + high) >>> 1;
            
            double midVal = mEntries.get(mid).getDistance();

            if (midVal > distance)
                low = mid + 1;
            else if (midVal < distance)
                high = mid - 1;
            else
                return mid; // key found
        }
        
        return -(low + 1);  // key not found.
    }

    /**
     * Resets the serial numbers of the entries to prevent overflow of the
     * serial number counter.  Not likely to be called, but here just in case.
     */
    private void resetSerialNumbers() {
        final int sz = mEntries.size();
        if (sz > 0) {
            // At first I thought about figuring out the min sn, then subtracting
            // it from all sns, but that would not be foolproof.  It's possible,
            // for example, that entry 0 has priority 10 and sn == Long.MAX_VALUE,
            // while entry 1 has priority 9 and sn == 0.  Just sort the current
            // entries, then number the sns consecutively.
            Collections.sort(mEntries);
            mSNCounter = 0L;
            for (int i = 0; i < sz; i++) {
                mEntries.get(i).setSerialNum(mSNCounter++);
            }
        }
    }

    /**
     * Removes the id with the highest distance.
     *
     * @return the id with the highest distance or -1, if the
     *   queue is empty
     */
    public int remove() {
        int rtn = -1;
        if (mEntries.size() > 0) {
            Entry e = mEntries.removeFirst();
            rtn = e.getValue();
            mIDSet.remove(rtn);
        }
        return rtn;
    }

    /**
     * Does the queue contain the given id?  Use this call sparingly, as
     * it does a linear search through the queue's entries.
     * @param id the id.
     * @return boolean true if the queue contains the id, false otherwise.
     */
    public boolean contains(int id) {
        return mIDSet.contains(id);
    }

    /**
     * Removes all occurrences of the given id in the queue.
     * @param id the id.
     * @return boolean true if at least one occurrence of the id was
     *   contained in the queue.  false, otherwise.
     */
    public boolean remove(int id) {
        Iterator<Entry> it = mEntries.iterator();
        while(it.hasNext()) {
            if (it.next().getValue() == id) {
                it.remove();
                mIDSet.remove(id);
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the id with the highest priority, but leaves it in the queue.
     * @return the id, or -1, if the queue is empty.
     */
    public int front() {
        int rtn = -1;
        if (mEntries.size() > 0) {
            rtn = mEntries.get(0).getValue();
        }
        return rtn;
    }

    /**
     * Gets the highest distance, but leaves the id it's associated with in the queue.
     * @return the highest distance, or 0.0 if the queue is empty.
     */
    public double frontDistance() {
        double rtn = 0.0;
        if (mEntries.size() > 0) {
            rtn = mEntries.get(0).getDistance();
        }
        return rtn;
    }

    /**
     * Get the max distance for ids allowed in this queue.
     * @return double
     */
    public double getMaxAllowedDistance() {
        return mMaxDistance;
    }

    /**
     * Get the max distance of all the ids currently in the queue.
     * @return double
     */
    public double getMaxDistance() {
        return frontDistance();
    }

    /**
     * Get the number of ids in the queue.
     * @return int
     */
    public int size() {
        return mEntries.size();
    }

    /**
     * Is the queue empty?
     * @return boolean
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Empties the queue.
     */
    public void clear() {
        mEntries.clear();
        mIDSet.clear();
        mSNCounter = 0L;
    }

    /**
     * Save the state of the <tt>DistanceQueue</tt> instance to a stream (that
     * is, serialize it).
     * 
     * @param s the stream to which to write the data.
     * @throws IOException if an IO error occurs.
     */
    private void writeObject(final java.io.ObjectOutputStream s)
            throws IOException {
        
        // Read in mCount, mSNCounter, etc.
        s.defaultWriteObject();
        
        final int sz = mEntries.size();
        // Write out array length
        s.writeInt(sz);
        // Write out all elements in the proper order.
        for (int i = 0; i < sz; i++) {
            Entry entry = mEntries.get(i);
            s.writeInt(entry.getValue());
            s.writeDouble(entry.getDistance());
            s.writeLong(entry.getSerialNum());
        }
    }

    /**
     * Reconstitute the <tt>DistanceQueue</tt> instance from a stream (that is,
     * deserialize it).
     * 
     * @param s the stream from which to read the data.
     * 
     * @throws IOException if an IO error occurs.
     * @throws ClassNotFoundException if the class of the object cannot be loaded.
     */
    private void readObject(final java.io.ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        // Read in mCount, mSNCounter, etc.
        s.defaultReadObject();
        // Read in array length and allocate array
        final int sz = s.readInt();
        mEntries = new LinkedList<Entry>();
        mIDSet = new TIntHashSet();
        // Read in all elements in the proper order.
        for (int i = 0; i < sz; i++) {
            mEntries.add(new Entry(s.readInt(), s.readDouble(), s.readLong()));
        }
    }

    // Class to hold the data and their priorities.
    //
    static final class Entry implements Comparable<Entry> {

        private int mValue;
        private double mDistance;
        // Consecutive serial numbers are used to preserve FIFO behavior for
        // objects added to the queue with the same priority.
        private long mSN;

        Entry(int value, double distance, long sn) {
            mValue = value;
            mDistance = distance;
            mSN = sn;
        }

        int getValue() {
            return mValue;
        }

        double getDistance() {
            return mDistance;
        }

        long getSerialNum() {
            return mSN;
        }

        void setSerialNum(long sn) {
            mSN = sn;
        }

        @Override
        public int compareTo(Entry o) {
            if (this.mDistance < o.mDistance) {
                return 1;
            } else if (this.mDistance > o.mDistance) {
                return -1;
            }
            // priorities equal -- compare serial numbers.  The first entry added
            // should have the lowest serial number.
            return this.mSN > o.mSN ? 1 : -1;
        }
    }
}
