package org.battelle.clodhopper.tuple;

import java.io.IOException;

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
 * TupleListFactoryException.java
 *
 *===================================================================*/

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
