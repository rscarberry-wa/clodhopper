package org.battelle.clodhopper.tuple;

import java.io.IOException;

/**
 * An exception class thrown when something goes wrong with a <code>TupleListFactory</code>
 * 
 * @author R. Scarberry
 * @since 1.0
 *
 */
public class TupleListFactoryException extends IOException {

    private static final long serialVersionUID = -8769847441627268156L;

    /**
     * Constructor
     */
    public TupleListFactoryException() {}
    
    /**
     * Constructor
     * @param message
     */
    public TupleListFactoryException(String message) {
        super(message);
    }
    
    /**
     * Constructor
     * @param cause
     */
    public TupleListFactoryException(Throwable cause) {
        super(cause);
    }
    
    /**
     * Constructor
     * 
     * @param message
     * @param cause
     */
    public TupleListFactoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
