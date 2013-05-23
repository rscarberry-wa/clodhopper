package org.battelle.clodhopper.examples.hierarchical;

import javax.swing.*;
import javax.swing.text.Document;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import org.battelle.clodhopper.distance.*;
import org.battelle.clodhopper.examples.kmeans.KMeansParamsPanel;
import org.battelle.clodhopper.examples.ui.NumberDocument;
import org.battelle.clodhopper.examples.ui.ParamsPanel;
import org.battelle.clodhopper.examples.ui.ParamsPanelException;
import org.battelle.clodhopper.examples.ui.UIUtils;
import org.battelle.clodhopper.hierarchical.*;
import org.battelle.clodhopper.kmeans.KMeansParams;
import org.battelle.clodhopper.seeding.ClusterSeeder;
import org.battelle.clodhopper.seeding.KMeansPlusPlusSeeder;
import org.battelle.clodhopper.seeding.RandomSeeder;
import org.battelle.clodhopper.tuple.TupleList;

public abstract class HierarchicalParamsPanel extends JPanel 
	implements ParamsPanel {

	private static final String CLUSTERS_LABEL = "Cluster Count:";
	private static final String COHERENCE_LABEL = "Cluster Coherence:";
	private static final String CLUSTERS_TOOLTIP = "The desired number of clusters.";
	private static final String COHERENCE_TOOLTIP = "The desired cluster coherence.";

	private JLabel clusterCountLbl = new JLabel();
    private JTextField clusterCountTF = new JTextField();
    private JTextField threadCountTF = new JTextField();
    
    private JLabel minMaxCoherenceLbl = new JLabel();
    private JTextField minCoherenceThresholdTF = new JTextField();
    private JTextField maxCoherenceThresholdTF = new JTextField();
    
    private JComboBox distanceMetricDD = new JComboBox();
    private JTextField randomSeedTF = new JTextField();
    private JComboBox linkageDD = new JComboBox();
    private JComboBox criterionDD = new JComboBox();
    
    private HierarchicalParams.Criterion lastCriterion;
    private String clusterCountText = "", coherenceText = "";
    
    public HierarchicalParamsPanel(HierarchicalParams params) {
    	
    	super(new GridBagLayout());
    	
    	boolean setRandomSeed = true;
    	if (params == null) {
    		params = new HierarchicalParams();
    		setRandomSeed = false;
    	}
    	
    	clusterCountText = String.valueOf(params.getClusterCount());
    	coherenceText = String.valueOf(params.getCoherenceDesired());
    	
        clusterCountTF.setColumns(8);
        clusterCountTF.setDocument(new NumberDocument(false, false));
        clusterCountTF.setText(String.valueOf(params.getClusterCount()));
        
        minMaxCoherenceLbl.setText("Min/Max Thresholds:");
        minCoherenceThresholdTF.setColumns(4);
        minCoherenceThresholdTF.setDocument(new NumberDocument(false, true));
        minCoherenceThresholdTF.setText(String.valueOf(params.getMinCoherenceThreshold()));
        
        maxCoherenceThresholdTF.setColumns(4);
        maxCoherenceThresholdTF.setDocument(new NumberDocument(false, true));
        double maxCoherenceThreshold = params.getMaxCoherenceThreshold();       
        maxCoherenceThresholdTF.setText(Double.isNaN(maxCoherenceThreshold) ? "" : String.valueOf(maxCoherenceThreshold));
    	
        threadCountTF.setColumns(8);
        threadCountTF.setDocument(new NumberDocument(false, false));
        threadCountTF.setText(String.valueOf(params.getWorkerThreadCount()));
        
        randomSeedTF.setColumns(8);
        randomSeedTF.setDocument(new NumberDocument(false, false));
        if (setRandomSeed) {
        	randomSeedTF.setText(String.valueOf(params.getRandomSeed()));
        }
        
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
        
        String selectedItem = null;
        for (HierarchicalParams.Criterion criterion : HierarchicalParams.Criterion.values()) {
        	String name = UIUtils.properEnumName(criterion.name());
        	if (criterion == params.getCriterion()) {
        		selectedItem = name;
        	}
        	criterionDD.addItem(name);
        }
        if (selectedItem != null) {
        	criterionDD.setSelectedItem(selectedItem);
        }
        
        criterionDD.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				updateEnabling();
			}
        });
        
        selectedItem = null;
        for (HierarchicalParams.Linkage linkage : HierarchicalParams.Linkage.values()) {
        	String name = UIUtils.properEnumName(linkage.name());
        	if (linkage == params.getLinkage()) {
        		selectedItem = name;
        	}
        	linkageDD.addItem(name);
        }
        
        if (selectedItem != null) {
        	linkageDD.setSelectedItem(selectedItem);
        }
        
        JLabel criterionLbl = new JLabel("Selection Criterion: ");
        criterionLbl.setToolTipText("The criterion used for selecting clusters.");
        criterionDD.setToolTipText(criterionLbl.getToolTipText());
        
        minCoherenceThresholdTF.setToolTipText("The minimum threshold for computing cluster coherences, if the criterion is Coherence.");
        maxCoherenceThresholdTF.setToolTipText("<html>The maximum threshold for computing cluster coherences, if the criterion is Coherence.<br />If left blank, the maximum decision distance is used.</html>");

        JLabel threadCountLbl = new JLabel("Thread Count:");
        threadCountLbl.setToolTipText("Number of threads to use for parallel computations.");
        threadCountTF.setToolTipText(threadCountLbl.getToolTipText());
        
        JLabel randomSeedLbl = new JLabel("Random Seed:");
        randomSeedLbl.setToolTipText("Seed for random number generation.");
        randomSeedTF.setToolTipText(randomSeedLbl.getToolTipText());
        
        JLabel distanceMetricLbl = new JLabel("Distance Metric:");
        distanceMetricLbl.setToolTipText("The distance metric to use.");
        distanceMetricDD.setToolTipText(distanceMetricLbl.getToolTipText());
        
        JLabel linkageLbl = new JLabel("Linkage Type:");
        linkageLbl.setToolTipText("The linkage type used for computing distances between intermediate clusters.");
        linkageDD.setToolTipText(linkageLbl.getToolTipText());
                
        JPanel minMaxPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        minMaxPanel.add(minCoherenceThresholdTF);
        minMaxPanel.add(maxCoherenceThresholdTF);
        
        this.add(criterionLbl, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, 
        		GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 10, 5, 5), 0, 0));
        this.add(criterionDD, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, 
        		GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 0, 5, 10), 0, 0));
        
        this.add(clusterCountLbl, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, 
        		GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 10, 5, 5), 0, 0));
        this.add(clusterCountTF, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, 
        		GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 10), 0, 0));
        
        this.add(minMaxCoherenceLbl, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, 
        		GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 10, 5, 5), 0, 0));
        this.add(minMaxPanel, new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0, 
        		GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 10), 0, 0));
        
        this.add(randomSeedLbl, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, 
                GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 10, 5, 5), 0, 0));
        this.add(randomSeedTF, new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0, 
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 
                new Insets(5, 0, 5, 10), 0, 0));

        this.add(threadCountLbl, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, 
                GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 10, 5, 5), 0, 0));
        this.add(threadCountTF, new GridBagConstraints(1, 4, 1, 1, 1.0, 0.0, 
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 
                new Insets(5, 0, 5, 10), 0, 0));
        
        this.add(distanceMetricLbl, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, 
        				GridBagConstraints.EAST, GridBagConstraints.NONE, 
        				new Insets(5, 10, 10, 5), 0, 0));
        this.add(distanceMetricDD, new GridBagConstraints(1, 5, 1, 1, 1.0, 0.0, 
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 
                new Insets(5, 0, 10, 10), 0, 0));
        
        this.add(linkageLbl, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0, 
				GridBagConstraints.EAST, GridBagConstraints.NONE, 
				new Insets(5, 10, 10, 5), 0, 0));
        this.add(linkageDD, new GridBagConstraints(1, 6, 1, 1, 1.0, 0.0, 
        		GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 
        		new Insets(5, 0, 10, 10), 0, 0));
        
        this.add(new JPanel(), new GridBagConstraints(0, 7, 2, 1, 1.0, 1.0, 
        		GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        
        updateEnabling();
    }
    
    public HierarchicalParamsPanel() {
    	this(null);
    }
    
    public void setClusterCount(int clusterCount) {
    	String s = String.valueOf(clusterCount);
    	if (selectedCriterion() == HierarchicalParams.Criterion.CLUSTERS) {
    		clusterCountTF.setText(s);
    	} else {
    		clusterCountText = s;
    	}
    }
    
    @Override
    public void setTupleCount(int tupleCount) {
    	int clusterCount = 0;
    	try {
    		clusterCount = Integer.parseInt(clusterCountTF.getText().trim());
    	} catch (NumberFormatException nfe) {
    	}
    	if (clusterCount == 0 || clusterCount > tupleCount) {
    		if (clusterCount == 0) {
    			clusterCount = (int) Math.sqrt(tupleCount);
    		} else {
    			clusterCount = tupleCount;
    		}
    		clusterCountTF.setText(String.valueOf(clusterCount));
    	}
    }

    public HierarchicalParams getValidatedParams(int tupleCount) throws ParamsPanelException {
    	
    	java.util.List<String> errorMessages = new ArrayList<String> ();
    	
    	HierarchicalParams.Criterion criterion = selectedCriterion();
    	HierarchicalParams.Linkage linkage = selectedLinkage();
    	
    	HierarchicalParams.Builder builder = new HierarchicalParams.Builder();
    	builder.criterion(criterion).linkage(linkage);
    	
    	if (criterion == HierarchicalParams.Criterion.CLUSTERS) {
        	int clusterCount = UIUtils.extractInt(clusterCountTF, "cluster count", 1, tupleCount, errorMessages);
        	if (errorMessages.size() == 0) {
        		builder.clusterCount(clusterCount);
        	}
    	} else if (criterion == HierarchicalParams.Criterion.COHERENCE) {
    		double coherence = UIUtils.extractDouble(clusterCountTF, "coherence", 0.0, 1.0, false, true, errorMessages);
    		double minCoherenceThreshold = UIUtils.extractDouble(minCoherenceThresholdTF, "min coherence threshold", 0.0, Double.NaN, true, true, errorMessages);
    		boolean setMaxCoherenceThreshold = maxCoherenceThresholdTF.getText().trim().length() > 0;
    		double maxCoherenceThreshold = 0.0;
    		if (setMaxCoherenceThreshold) {
    			maxCoherenceThreshold = UIUtils.extractDouble(maxCoherenceThresholdTF, "max coherence threshold", 0.0, Double.NaN, true, true, errorMessages);
    		}
    		if (errorMessages.size() == 0) {
    			builder.coherenceDesired(coherence).minCoherenceThreshold(minCoherenceThreshold);
    			if (setMaxCoherenceThreshold) {
    				builder.maxCoherenceThreshold(maxCoherenceThreshold);
    			}
    		}
    	}

    	int threadCount = UIUtils.extractInt(threadCountTF, "thread count", 1, Integer.MAX_VALUE, errorMessages);
    	long randomSeed = UIUtils.extractLong(randomSeedTF, "random seed", Long.MIN_VALUE, Long.MAX_VALUE, System.currentTimeMillis(), errorMessages);
    	
    	String distanceMetricName = distanceMetricDD.getSelectedItem().toString();
    	DistanceMetric distanceMetric = UIUtils.distanceMetric(distanceMetricName);
    	
    	HierarchicalParams params = null;
    	
    	if (errorMessages.size() > 0) {
    		
    		throw new ParamsPanelException("Hierarchical Parameters Error", errorMessages);
    		
    	} else {
    		
    		builder.workerThreadCount(threadCount).randomSeed(randomSeed).distanceMetric(distanceMetric);
    		
    		params = builder.build();
    	}
    	
    	return params;
    }
    
    private void updateEnabling() {
    	
    	HierarchicalParams.Criterion criterion = selectedCriterion();
    	
    	if (criterion != lastCriterion) {
    	
    		boolean countEnabled = criterion == HierarchicalParams.Criterion.CLUSTERS;
    		
    		String newText;
    		Document newDoc;
    		
    		if (criterion == HierarchicalParams.Criterion.CLUSTERS) {
    			newText = clusterCountText;
    			newDoc = new NumberDocument(false, false);
    		} else {
    			newText = coherenceText;
    			newDoc = new NumberDocument(false, true);
    		}
    		
    		if (lastCriterion == HierarchicalParams.Criterion.CLUSTERS) {
    			clusterCountText = clusterCountTF.getText();
    		} else if (lastCriterion == HierarchicalParams.Criterion.COHERENCE) {
    			coherenceText = clusterCountTF.getText();
    		}
    		
    		clusterCountLbl.setText(countEnabled ? CLUSTERS_LABEL : COHERENCE_LABEL);
    		clusterCountLbl.setToolTipText(countEnabled ? CLUSTERS_TOOLTIP : COHERENCE_TOOLTIP);
    		clusterCountTF.setToolTipText(clusterCountLbl.getToolTipText());
    		
    		clusterCountTF.setDocument(newDoc);
    		clusterCountTF.setText(newText);
    		
    		minMaxCoherenceLbl.setEnabled(!countEnabled);
    		minCoherenceThresholdTF.setEnabled(!countEnabled);
    		maxCoherenceThresholdTF.setEnabled(!countEnabled);
    		
    		lastCriterion = criterion;
    		
    		this.revalidate();
    	}
    }
    
    private HierarchicalParams.Criterion selectedCriterion() {
    	return HierarchicalParams.criterionFor(criterionDD.getSelectedItem().toString());
    }
    
    private HierarchicalParams.Linkage selectedLinkage() {
    	return HierarchicalParams.linkageFor(linkageDD.getSelectedItem().toString());
    }
    
}
