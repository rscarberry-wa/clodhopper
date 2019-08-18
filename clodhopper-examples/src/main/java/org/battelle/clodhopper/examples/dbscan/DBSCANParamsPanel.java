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
package org.battelle.clodhopper.examples.dbscan;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.battelle.clodhopper.Clusterer;
import org.battelle.clodhopper.dbscan.DBSCANClusterer;
import org.battelle.clodhopper.dbscan.DBSCANParams;
import org.battelle.clodhopper.distance.DistanceMetric;
import org.battelle.clodhopper.examples.ui.NumberDocument;
import org.battelle.clodhopper.examples.ui.ParamsPanel;
import org.battelle.clodhopper.examples.ui.ParamsPanelException;
import org.battelle.clodhopper.examples.ui.UIUtils;
import org.battelle.clodhopper.tuple.TupleList;

/**
 *
 * @author randy
 */
public class DBSCANParamsPanel extends JPanel implements ParamsPanel {
    
    private final JTextField epsilonTF = new JTextField();
    private final JTextField minSamplesTF = new JTextField();
    private final JComboBox distanceMetricDD = new JComboBox();

    public DBSCANParamsPanel(DBSCANParams params) {
        super(new GridBagLayout());
        
        Objects.requireNonNull(params);
                
        JLabel epsilonLbl = new JLabel("Epsilon:");
        epsilonLbl.setToolTipText(
                "The greater than zero distance from each point to search for neighboring points"
        );
        epsilonTF.setToolTipText(epsilonLbl.getToolTipText());
        epsilonTF.setColumns(8);
        epsilonTF.setDocument(new NumberDocument(false, true));
        epsilonTF.setText(String.valueOf(params.getEpsilon()));
        
        JLabel minSamplesLbl = new JLabel("Min Samples:");
        minSamplesLbl.setToolTipText(
                "Minimum number of neighbors a point must have to be considered a core point"
        );
        minSamplesTF.setToolTipText(minSamplesLbl.getToolTipText());
        minSamplesTF.setColumns(8);
        minSamplesTF.setDocument(new NumberDocument(false, false));
        minSamplesTF.setText(String.valueOf(params.getMinSamples()));
        
        JLabel distanceMetricLbl = new JLabel("Distance Metric:");
        distanceMetricLbl.setToolTipText("The distance metric to use.");
        distanceMetricDD.setToolTipText(distanceMetricLbl.getToolTipText());

        String selectedMetric = null;
        for (String key : UIUtils.distanceMetricNames()) {
            distanceMetricDD.addItem(key);
            DistanceMetric distanceMetric = UIUtils.distanceMetric(key);
            if (distanceMetric.getClass() == params.getDistanceMetric().getClass()) {
                selectedMetric = key;
            }
        }
        if (selectedMetric != null) {
            distanceMetricDD.setSelectedItem(selectedMetric);
        }
        
        this.add(epsilonLbl, 
                new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, 
                        GridBagConstraints.EAST, GridBagConstraints.NONE, 
                        new Insets(10, 10, 5, 5), 0, 0));
        this.add(epsilonTF,
                new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, 
                        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 
                        new Insets(10, 0, 5, 10), 0, 0));
        
        this.add(minSamplesLbl, 
                new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, 
                        GridBagConstraints.EAST, GridBagConstraints.NONE, 
                        new Insets(5, 10, 5, 5), 0, 0));
        this.add(minSamplesTF,
                new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, 
                        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 
                        new Insets(5, 0, 5, 10), 0, 0));

        this.add(distanceMetricLbl, 
                new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, 
                        GridBagConstraints.EAST, GridBagConstraints.NONE, 
                        new Insets(5, 10, 5, 5), 0, 0));
        
        this.add(distanceMetricDD, new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0, 
                        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 
                        new Insets(5, 0, 5, 10), 0, 0));
    }
    
    public DBSCANParamsPanel() {
        this(new DBSCANParams());
    }
    
    @Override
    public Clusterer getNewClusterer(TupleList tuples) throws ParamsPanelException {
        return new DBSCANClusterer(tuples, getValidatedParams());
    }

    @Override
    public void setTupleCount(int tupleCount) {
        // Noop -- nothing to do
    }
    
    public DBSCANParams getValidatedParams() throws ParamsPanelException {
        List<String> errorMessages = new ArrayList<>();
        double epsilon = UIUtils.extractDouble(epsilonTF, "epsilon",
                0.0, Double.MAX_VALUE, false, true, errorMessages);
        int minSamples = UIUtils.extractInt(minSamplesTF, "min samples", 1, Integer.MAX_VALUE, errorMessages);
    	String distanceMetricName = distanceMetricDD.getSelectedItem().toString();
    	DistanceMetric distanceMetric = UIUtils.distanceMetric(distanceMetricName);
        
        if (errorMessages.size() > 0) {
            throw new ParamsPanelException("DBSCAN Parameters Error", errorMessages);
        }
        
        return new DBSCANParams(epsilon, minSamples, distanceMetric);
    }
}
