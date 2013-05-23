package org.battelle.clodhopper.examples.selection;

import org.battelle.clodhopper.util.IntIterator;

/**
 * Interface to represent and control the selection state of a collection of 
 * N entities identified by indexes [0 - (N-1)].
 * 
 * @author R. Scarberry
 *
 */
public interface SelectionModel {
   
    /**
     * Adds a listener to the collection that is notified each time
     * a change to the selection occurs.
     * 
     * @param listener
     */
    public void addSelectionListener (SelectionListener listener);
    
    /**
     * Removes a listener from the collection that is notified each time
     * a change to the selection occurs.
     * 
     * @param listener
     */
    public void removeSelectionListener (SelectionListener listener);
    
    /**
     * Clears the entire selection.
     */
    public void clearSelected(Object requester);
    
    /**
     * Returns true if the specified index is in the selected set.
     * @param index
     * @return
     */
    public boolean isSelected(int index);
    
    /**
     * Returns an iterator containing all selected indexes.
     * 
     * @return
     */
    public IntIterator getSelected();
    
    /**
     * Returns an iterator containing all unselected indexes.
     * 
     * @return
     */
    public IntIterator getUnselected();
    
    /**
     * Sets the selection to the indexes contained in the iterator.  
     * Selected indexes not in the iterator are deselected.
     * 
     * @param it iterator containing the indexes to be selected.
     */
    public void setSelected(Object requester, IntIterator it);
    
    /**
     * Select the specified index.  The selection state of other indexes is
     * not affected.
     * 
     * @param index
     */
    public void select(Object requester, int index);
    
    /**
     * Select the indexes contained in the specified iterator.  The selection state
     * of other indexes is not affected.
     * 
     * @param it
     */
    public void select(Object requester, IntIterator it);
    
    /**
     * Unselect the specified index.  The selection state of other indexes is
     * not affected.
     * 
     * @param index
     */
    public void unselect(Object requester, int index);
    
    /**
     * Unselect the indexes contained in the specified iterator.  The selection state
     * of other indexes is not affected.
     * 
     * @param it
     */
    public void unselect(Object requester, IntIterator it);
    
    /**
     * Select all indexes.
     */
    public void selectAll(Object requester);
    
    /**
     * Get the number of selected indexes.
     * 
     * @return
     */
    public int getSelectedCount();
    
    /**
     * Get the number of indexes for which selection states are being maintained.
     * 
     * @return
     */
    public int getIndexCount();
    
    /**
     * Returns true if the selections are undergoing a series of changes.
     * 
     * @return
     */
    public boolean getValueIsAdjusting();
    
    /**
     * Sets a flag to indicate whether or not the selections should be regarded as undergoing a series
     * of changes.  This is so listeners may choose to ignore processing in response to
     * select events when the flag is true.  The final event
     * in the series of changes should be propagated after the flag is set back to false, so 
     * listeners can do a full update. 
     * 
     * @param adjusting
     */
    public void setValueIsAdjusting(boolean adjusting);

}
