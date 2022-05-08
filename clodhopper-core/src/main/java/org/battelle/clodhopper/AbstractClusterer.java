package org.battelle.clodhopper;

import java.lang.annotation.Inherited;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.battelle.clodhopper.task.AbstractTask;
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
 * AbstractClusterer.java
 *
 *===================================================================*/
public abstract class AbstractClusterer extends AbstractTask<List<Cluster>>
    implements Clusterer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractClusterer.class);
    
    protected AbstractClusterer() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<List<Cluster>> getClusters() {
        List<Cluster> clusters = null;
        try {
            clusters = get();
        } catch (InterruptedException | ExecutionException e) {
            // Log the error
            LOGGER.error("error obtaining clusters", e);
        }
        return Optional.ofNullable(clusters);
    }
}
