package org.battelle.clodhopper.tuple;

/**
 * Defines entities that provide a fixed-length tuple signature.  Such a signature
 * is simply a representation as an array of doubles.
 * 
 * @author R.Scarberry
 * @since 1.0
 *
 */
public interface Tupleable {

	/**
	 * Get the tuple signature for the object.
	 * 
	 * @return
	 */
	double[] getTupleSignature();
	
}
