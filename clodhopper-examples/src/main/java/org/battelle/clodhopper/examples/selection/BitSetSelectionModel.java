package org.battelle.clodhopper.examples.selection;

import java.util.*;

import org.battelle.clodhopper.util.ArrayIntIterator;
import org.battelle.clodhopper.util.BitSetIntIterator;
import org.battelle.clodhopper.util.IntIterator;
import org.battelle.clodhopper.util.IntervalIntIterator;

/**
 * <p>Thread-safe implementation of the interface <code>SelectionModel</code>
 * which represents and controls the selection state of a collection of 
 * N entities identified by indexes [0 - (N-1)].</p>
 * 
 * <p>All methods that alter the selection state use some degree of
 * synchronization.  When selecting records, it is wise to do so using
 * the least number of calls possible.</p>
 * 
 * <p>When the selection state changes, one <code>SelectionEvent</code> is
 * posted to all registered listeners.  Listeners that respond to the event
 * asynchronously should use a clone of the <code>IntIterator</code> passed
 * in the event.  The <code>IntIterator</code> implementations are
 * generally not thread-safe.</p>
 * 
 * @author R.Scarberry
 *
 */
public class BitSetSelectionModel implements SelectionModel {

    // List of listeners
    private List<SelectionListener> listeners = new ArrayList<SelectionListener>();
    // Redundant array of the listeners that's lazily instantiated for performance.
    private SelectionListener[] listenerArray;
    
    // Source object that "owns" the entities.
    private Object source;
    // A bitvector containing a number of bits equal to the number of entities.
    private BitSet selectionBits;
    // Whether or not the selection state is undergoing adjustment by a series of calls
    // to the methods of this object.
    private boolean adjusting;

    /**
     * Constructor
     * 
     * @param source object that owns the entities (records).
     * @param recordCount the number of entities (records).
     * 
     * @throws NullPointerException if source == null.
     * @throws IllegalArgumentException if recordCount is negative.
     */
    public BitSetSelectionModel(Object source, int recordCount) {
        if (source == null)
            throw new NullPointerException();
        this.source = source;
        this.selectionBits = new BitSet(recordCount);
    }

    /**
     * Adds a listener to be notified each time
     * a change to the selection occurs.
     * 
     * @param listener a listener for selections
     */
    public void addSelectionListener(final SelectionListener listener) {
        synchronized (listeners) {
            if (!listeners.contains(listener)) {
                listeners.add(listener);
                listenerArray = null;
            }
        }
    }

    /**
     * Clears the entire selection.  Posts a single event to all registered listeners.
     * @param requester entity requesting the operation.
     */
    public void clearSelected(final Object requester) {
        IntIterator it = null;
        synchronized (selectionBits) {
            // Clone the bit vector before clearing the bits, so the clone can be used in the iterator.
            // The iterator will contain just those indexes that were cleared.
            it = new BitSetIntIterator((BitSet) selectionBits.clone());
            selectionBits.clear();
        }
        fireSelectionChanged(requester, it, SelectionType.UNSELECT, getValueIsAdjusting());
    }

    /**
     * Get the number of indexes for which selection states are being maintained.
     * 
     * @return the number of indexes.
     */
    public int getIndexCount() {
        return selectionBits.size();
    }

    /**
     * Returns an iterator containing all selected indexes.
     * 
     * @return an iterator over the indexes of the selected items.
     */
    public IntIterator getSelected() {
        synchronized (selectionBits) {
            // Return a clone in the iterator for thread safety.
            return new BitSetIntIterator((BitSet) selectionBits.clone());
        }
    }

    /**
     * Get the number of selected indexes.
     * 
     * @return the number of items selected.
     */
    public int getSelectedCount() {
        synchronized (selectionBits) {
            return selectionBits.cardinality();
        }
    }

    /**
     * Returns an iterator containing all unselected indexes.
     * 
     * @return an iterator over the indexes of the unselected items.
     */
    public IntIterator getUnselected() {
        synchronized (selectionBits) {
            // Return a clone in the iterator for thread safety.
            return new BitSetIntIterator((BitSet) selectionBits.clone(), false);
        }
    }

    /**
     * Returns true if the selections are undergoing a series of changes.
     * 
     * @return true if the selection is undergoing change, false otherwise.
     */
    public boolean getValueIsAdjusting() {
        return adjusting;
    }

    /**
     * Returns true if the specified index is in the selected set.
     * @param index the item to check.
     * @return true if the item is selected, false otherwise.
     */
    public boolean isSelected(final int index) {
        synchronized (selectionBits) {
            return selectionBits.get(index);
        }
    }

    /**
     * Removes a listener from the collection that is notified each time
     * a change to the selection occurs.
     * 
     * @param listener the listener to remove.
     */
    public void removeSelectionListener(final SelectionListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
            listenerArray = null;
        }
    }
    
    private SelectionListener[] getListeners() {
    	if (listenerArray == null) {
    		synchronized (listeners) {
    			listenerArray = listeners.toArray(new SelectionListener[listeners.size()]);
    		}
    	}
    	return listenerArray;
    }

    /**
     * <p>Select the specified index.  The selection state of other indexes is
     * not affected.</p>
     * <p>NOTE: It is quite unwise to select many indexes through a series
     * of calls to this method.  It is much more efficent to call
     * <code>select(IntIterator it)</code></p>
     * 
     * @param requester entity requesting the selection.
     * @param index identifies the item to select.
     */
    public void select(final Object requester, final int index) {
        IntIterator it = null;
        synchronized (selectionBits) {
            selectionBits.set(index);
            it = new ArrayIntIterator(new int[] { index });
        }
        fireSelectionChanged(requester, it, SelectionType.SELECT,
                getValueIsAdjusting());
    }

    /**
     * Select the indexes contained in the specified iterator.  The selection state
     * of other indexes is not affected.
     * 
     * @param requester entity requesting the selection.
     * @param it identifies the times to select.
     */
    public void select(final Object requester, final IntIterator it) {
        synchronized (selectionBits) {
            it.gotoFirst();
            while (it.hasNext()) {
                selectionBits.set(it.getNext());
            }
        }
        fireSelectionChanged(requester, it, SelectionType.SELECT,
                getValueIsAdjusting());
    }

    /**
     * Select all indexes.
     * 
     * @param requester entity requesting the selection.
     */
    public void selectAll(final Object requester) {
        synchronized (selectionBits) {
            int sz = selectionBits.size();
            for (int i = 0; i < sz; i++) {
                selectionBits.set(i);
            }
        }
        fireSelectionChanged(requester, new IntervalIntIterator(0,
                selectionBits.size() - 1), SelectionType.SELECT,
                getValueIsAdjusting());
    }

    /**
     * <p>Sets the selection to the indexes contained in the iterator.  
     * Selected indexes not in the iterator are deselected.</p>
     * 
     * <p>This method propagates one <code>SelectionEvent</code> to registered 
     * listeners.</p>
     * 
     * @param requester entity requesting the selection.
     * @param it identifies the times to select.
     */
    public void setSelected(final Object requester, final IntIterator it) {
        if (it.getSize() == 0) {
            clearSelected(requester);
        } else {
            SelectionType selectType = SelectionType.BOTH;
            IntIterator eventIt = null;
            synchronized (selectionBits) {
                // At the end of the while-loop, this will contain bits set for
                // both the cleared records and the selected records.
                BitSet bv1 = (BitSet) selectionBits.clone();
                // Clear the currently-set bits.
                selectionBits.clear();
                // Now, set the bits in the iterator.
                it.gotoFirst();
                while (it.hasNext()) {
                    int index = it.getNext();
                    selectionBits.set(index);
                    bv1.set(index);
                }
                // If bv1 equals mSelectionBits, no selections were cleared, so the
                // select type is just SELECT.
                if (bv1.equals(selectionBits)) {
                    selectType = SelectionType.SELECT;
                }
                eventIt = new BitSetIntIterator(bv1);
            }
            fireSelectionChanged(requester, eventIt, selectType, getValueIsAdjusting());
        }
    }

    /**
     * Sets a flag to indicate whether or not the selections should be regarded as undergoing a series
     * of changes.  This is so listeners may choose to ignore processing in response to
     * select events when the flag is true.  The final event
     * in the series of changes should be propagated after the flag is set back to false, so 
     * listeners can do a full update. 
     * 
     * @param adjusting boolean value to set.
     */
    public void setValueIsAdjusting(final boolean adjusting) {
        this.adjusting = adjusting;
    }

    /**
     * <p>Unselect the specified index.  The selection state of other indexes is
     * not affected.</p>
     * <p>NOTE: It is quite unwise to unselect many indexes through a series
     * of calls to this method.  It is much more efficient to call
     * <code>unselect(IntIterator it)</code></p>
     * 
     * @param requester entity requesting the selection change.
     * @param index identifies the item to unselect.
     */
    public void unselect(final Object requester, final int index) {
        IntIterator it = null;
        synchronized (selectionBits) {
            selectionBits.clear(index);
            it = new ArrayIntIterator(new int[] { index });
        }
        fireSelectionChanged(requester, it, SelectionType.UNSELECT,
                getValueIsAdjusting());
    }

    /**
     * Unselect the indexes contained in the specified iterator.  The selection state
     * of other indexes is not affected.
     * 
     * @param requester entity requesting the selection change.
     * @param it identifies the item to unselect.
     */
    public void unselect(final Object requester, final IntIterator it) {
        synchronized (selectionBits) {
            it.gotoFirst();
            while (it.hasNext()) {
                selectionBits.clear(it.getNext());
            }
        }
        fireSelectionChanged(requester, it, SelectionType.UNSELECT,
                getValueIsAdjusting());
    }

    // Fires a selection event to registered listeners.
    private void fireSelectionChanged(Object requester, IntIterator it,
            SelectionType selectType, boolean adjusting) {
    	SelectionListener[] lstnrs = getListeners();
    	final int sz = lstnrs.length;
        if (sz > 0) {
            // Propagate the event after releasing the lock on mListeners.
            SelectionEvent event = new SelectionEvent(source, requester, it, selectType,
                    adjusting);
            for (int i = 0; i < sz; i++) {
                it.gotoFirst();
                lstnrs[i].selectionChanged(event);
            }
        }
    }
}
