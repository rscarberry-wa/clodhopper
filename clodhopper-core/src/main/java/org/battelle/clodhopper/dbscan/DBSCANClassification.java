/*
 * Copyright 2019 randy.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.battelle.clodhopper.dbscan;

import gnu.trove.set.TIntSet;
import java.io.Serializable;
import java.util.Objects;
import org.battelle.clodhopper.util.UnmodifiableTIntSet;

/**
 * {@code DBSCANClassification} is produced by the {@code DBSCANClusterer} to
 * indicate which tuples were classified as core, which were classified as
 * edge, and which were classified as noise.
 * 
 * @author R.Scarberry
 */
public class DBSCANClassification implements Serializable {
    
    private static final long serialVersionUID = -8544451925531566742L;
    
    private final TIntSet coreTupleIds;
    private final TIntSet edgeTupleIds;
    private final TIntSet noiseTupleIds;
    
    /**
     * Constructor.
     * 
     * @param coreTupleIds
     * @param edgeTupleIds
     * @param noiseTupleIds 
     */
    public DBSCANClassification(
            TIntSet coreTupleIds, TIntSet edgeTupleIds, TIntSet noiseTupleIds) {
        Objects.requireNonNull(coreTupleIds);
        Objects.requireNonNull(edgeTupleIds);
        Objects.requireNonNull(noiseTupleIds);
        
        this.coreTupleIds = coreTupleIds;
        this.edgeTupleIds = edgeTupleIds;
        this.noiseTupleIds = noiseTupleIds;
    }
    
    public boolean isCore(int tupleId) {
        return coreTupleIds.contains(tupleId);
    }
    
    public boolean isEdge(int tupleId) {
        return edgeTupleIds.contains(tupleId);
    }
    
    public boolean isNoise(int tupleId) {
        return noiseTupleIds.contains(tupleId);
    }
    
    public TIntSet getCoreTupleIds() {
        return new UnmodifiableTIntSet(this.coreTupleIds);
    }
    
    public TIntSet getEdgeTupleIds() {
        return new UnmodifiableTIntSet(this.edgeTupleIds);
    }
    
    public TIntSet getNoiseTupleIds() {
        return new UnmodifiableTIntSet(this.noiseTupleIds);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(
                this.coreTupleIds, this.edgeTupleIds, this.noiseTupleIds);
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o != null && o.getClass() == this.getClass()) {
            DBSCANClassification other = (DBSCANClassification) o;
            return Objects.equals(this.coreTupleIds, other.coreTupleIds) &&
                    Objects.equals(this.edgeTupleIds, other.edgeTupleIds) &&
                    Objects.equals(this.noiseTupleIds, other.noiseTupleIds);
        }
        return false;
    }
}
