package org.battelle.clodhopper.tuple;

import java.util.Set;

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
 * TupleListFactory.java
 *
 *===================================================================*/
/**
 * A <code>TupleListFactory</code> is an entity that creates and manages
 * </code>TupleList</code>s.
 *
 * @author R. Scarberry
 * @since 1.0
 *
 */
public interface TupleListFactory {

    /**
     * Create a new <code>TupleList</code> and associate it with a name. The
     * name must be unique in this factory.
     *
     * @param name the name to be associated with the tuple list.
     * @param tupleLength the length of the tuples.
     * @param tupleCount the number of tuples.
     *
     * @return a <code>TupleList</code>
     *
     * @throws TupleListFactoryException if something goes wrong such as another
     * <code>TupleList</code> already being associated with the name, or if an
     * I/O error occurs.
     */
    TupleList createNewTupleList(String name, int tupleLength, int tupleCount) throws TupleListFactoryException;

    /**
     * Opens an existing tuple list associated with the specified name.
     *
     * @param name the name to be associated with the tuples.
     * @return an instance of <code>TupleList</code>.
     * @throws TupleListFactoryException if an error occurs, such as no such
     * tuple list existing.
     */
    TupleList openExistingTupleList(String name) throws TupleListFactoryException;

    /**
     * Create a copy of a tuple list and associate it with a different name.
     *
     * @param nameForCopy the name to associate with the copy.
     * @param original the original <code>TupleList</code>
     * @return a <code>TupleList</code>
     * @throws TupleListFactoryException if something goes wrong such as another
     * <code>TupleList</code> already being associated with the name, or if an
     * I/O error occurs.
     */
    TupleList copyTupleList(String nameForCopy, TupleList original) throws TupleListFactoryException;

    /**
     * Returns a set containing the names of all <code>TupleList</code>s managed
     * by this factory.
     *
     * @return a set of the tuple list names.
     */
    Set<String> tupleListNames();

    /**
     * For checking to see whether or not a tuple list is associated with a
     * name.
     *
     * @param name the name to check for.
     * 
     * @return true if tuples have been associated with the name.
     */
    boolean hasTuplesFor(String name);

    /**
     * Delete the specified tuple list from this factory.
     *
     * @param tuples the <code>TupleList</code> to delete from the factory.
     *
     * @throws TupleListFactoryException if an error occurs, such as the tuple
     * list not being managed by this factory.
     */
    void deleteTupleList(TupleList tuples) throws TupleListFactoryException;

    /**
     * Close the specified tuple list without deleting it. It can later be
     * reopened. If the tuple list is a type that is maintained in memory,
     * closing it with this method should persist its values.
     *
     * @param tuples the <code>TupleList</code> to close.
     *
     * @throws TupleListFactoryException if a problem occurs.
     */
    void closeTupleList(TupleList tuples) throws TupleListFactoryException;

    /**
     * Closes all the open tuple lists managed by this factory.
     *
     * @throws TupleListFactoryException if a problem occurs.
     */
    void closeAll() throws TupleListFactoryException;

}
