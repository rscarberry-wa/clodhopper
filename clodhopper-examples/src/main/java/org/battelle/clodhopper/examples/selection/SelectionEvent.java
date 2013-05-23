package org.battelle.clodhopper.examples.selection;

import org.battelle.clodhopper.util.IntIterator;

public class SelectionEvent extends java.util.EventObject {

	private static final long serialVersionUID = 1L;

	private Object requester;
	private IntIterator it;
	private SelectionType selectionType;
	private boolean adjusting;
	
	/**
     * Constructor
     * 
     * @param source the source object that owns the entities whose selection state has
     *   changed.
     * @param requester whatever triggered the selection.  This object can be null.  Often, the object
     *   that triggered the selection is also registered as a <code>SelectionListener</code> on the 
     *   source object. It needs to know when it is receiving an event for its own selection request.
     * @param it an iterator containing the indexes of the entities whose selection state
     *   has changed.
     * @param selectionType the type of selection change, SELECT, UNSELECT, or BOTH.
     * @param adjusting whether the selections are still being changed.  Listeners may prefer
     *   to ignore <code>SelectionEvent</code>s until receiving one with adjusting equal to false.
     */
 	public SelectionEvent(Object source, Object requester, IntIterator it, SelectionType selectionType, boolean adjusting) {
		super(source);
		if (it == null || selectionType == null) {
			throw new NullPointerException();
		}
		this.requester = requester;
		this.it = it;
		this.selectionType = selectionType;
		this.adjusting = adjusting;
	}
	
    /**
     * Returns the requester, which is the entity which made the call that changed the
     * selection and triggered the event.  This object can be null.
     * 
     * @return
     */
    public Object getRequester() {
        return requester;
    }
    
    /**
     * Returns the iterator containing the indexes of the entities whose selection
     * state has changed.
     * 
     * @return
     */
    public IntIterator getIntIterator() { return it; }
    
    /**
     * Returns the category of selection change.  If the returned category is <code>BOTH</code>,
     * the iterator returned by <code>getIntIterator()</code> contains the indexes of those 
     * entities that have been selected along with those that have been deselected.
     * 
     * @return
     */
    public SelectionType getSelectType() { 
    	return selectionType; 
    }
    
    /**
     * Returns whether or not the selections are still in the process of being changed.
     * 
     * @return
     */
    public boolean isAdjusting() { 
    	return adjusting; 
    }
}
