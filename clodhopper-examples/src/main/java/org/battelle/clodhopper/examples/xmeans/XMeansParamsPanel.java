package org.battelle.clodhopper.examples.xmeans;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import org.battelle.clodhopper.Clusterer;
import org.battelle.clodhopper.distance.*;
import org.battelle.clodhopper.examples.kmeans.KMeansParamsPanel;
import org.battelle.clodhopper.examples.ui.NumberDocument;
import org.battelle.clodhopper.examples.ui.ParamsPanel;
import org.battelle.clodhopper.examples.ui.ParamsPanelException;
import org.battelle.clodhopper.examples.ui.UIUtils;
import org.battelle.clodhopper.gmeans.*;
import org.battelle.clodhopper.kmeans.*;
import org.battelle.clodhopper.seeding.ClusterSeeder;
import org.battelle.clodhopper.seeding.KDTreeSeeder;
import org.battelle.clodhopper.seeding.KMeansPlusPlusSeeder;
import org.battelle.clodhopper.seeding.RandomSeeder;
import org.battelle.clodhopper.tuple.TupleList;
import org.battelle.clodhopper.xmeans.XMeansClusterer;
import org.battelle.clodhopper.xmeans.XMeansParams;

public class XMeansParamsPanel extends JPanel 
	implements ParamsPanel {

	private JTextField minClustersTF = new JTextField();
	private JTextField maxClustersTF = new JTextField();
	private JTextField minClusterToMeanTF = new JTextField();
    private JTextField threadCountTF = new JTextField();
    private JComboBox distanceMetricDD = new JComboBox();
    private JComboBox seedingDD = new JComboBox();
    private JTextField randomSeedTF = new JTextField();
    private JCheckBox useOverallBICCB = new JCheckBox();
    
    public XMeansParamsPanel(XMeansParams params) {
    	
    	super(new GridBagLayout());
    	
    	boolean setRandomSeed = true;
    	if (params == null) {
    		params = new XMeansParams();
    		setRandomSeed = false;
    	}
    	
    	JLabel minMaxClustersLbl = new JLabel("Min/Max Number of Clusters:");
        minClustersTF.setColumns(4);
        minClustersTF.setDocument(new NumberDocument(false, false));
        minClustersTF.setText(String.valueOf(params.getMinClusters()));
    	minClustersTF.setToolTipText("The minimum number of clusters to be generated.");
    	
        maxClustersTF.setColumns(4);
        maxClustersTF.setDocument(new NumberDocument(false, false));
        if (params.getMaxClusters() != Integer.MAX_VALUE) {
        	maxClustersTF.setText(String.valueOf(params.getMaxClusters()));
        }
        maxClustersTF.setToolTipText("The maximum number of clusters to be generated.");
        
        JPanel minMaxPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        minMaxPanel.add(minClustersTF);
        minMaxPanel.add(maxClustersTF);
        
        JLabel minClusterToMeanLbl = new JLabel("Min Cluster Size Ratio:");
        minClusterToMeanLbl.setToolTipText("The minimum cluster size to mean size threshold.");
        minClusterToMeanTF.setColumns(8);
        minClusterToMeanTF.setDocument(new NumberDocument(false, true));
        minClusterToMeanTF.setText(String.valueOf(params.getMinClusterToMeanThreshold()));
        minClusterToMeanTF.setToolTipText(minClusterToMeanLbl.getToolTipText());
        
        JLabel threadCountLbl = new JLabel("Thread Count:");
        threadCountLbl.setToolTipText("Number of threads to use for parallel computations.");
        threadCountTF.setToolTipText(threadCountLbl.getToolTipText());
        threadCountTF.setColumns(8);
        threadCountTF.setDocument(new NumberDocument(false, false));
        threadCountTF.setText(String.valueOf(params.getWorkerThreadCount()));
        
        JLabel randomSeedLbl = new JLabel("Random Seed:");
        randomSeedLbl.setToolTipText("Seed for random number generation.");
        randomSeedTF.setToolTipText(randomSeedLbl.getToolTipText());
        randomSeedTF.setColumns(8);
        randomSeedTF.setDocument(new NumberDocument(false, false));

        JLabel seedingMethodLbl = new JLabel("Seeding Method:");
        seedingMethodLbl.setToolTipText("Method to use for selecting the initial cluster seeds.");
        seedingDD.setToolTipText(seedingMethodLbl.getToolTipText());
        seedingDD.addItem(KMeansParamsPanel.KMEANS_PLUS_PLUS);
        seedingDD.addItem(KMeansParamsPanel.RANDOM);
        seedingDD.addItem(KMeansParamsPanel.KDTREE);
        
        ClusterSeeder seeder = params.getClusterSeeder();
        if (seeder instanceof KMeansPlusPlusSeeder) {
            seedingDD.setSelectedItem(KMeansParamsPanel.KMEANS_PLUS_PLUS);
            if (setRandomSeed) {
            	randomSeedTF.setText(String.valueOf(((KMeansPlusPlusSeeder) seeder).getRandomGeneratorSeed()));
            }
        } else if (seeder instanceof KDTreeSeeder) {
        	seedingDD.setSelectedItem(KMeansParamsPanel.KDTREE);
        	if (setRandomSeed) {
            	randomSeedTF.setText(String.valueOf(((KDTreeSeeder) seeder).getRandomGeneratorSeed()));
        	}
        } else {
            seedingDD.setSelectedItem(KMeansParamsPanel.RANDOM);
            if (setRandomSeed && seeder instanceof RandomSeeder) {
                randomSeedTF.setText(String.valueOf(((RandomSeeder) seeder).getRandomGeneratorSeed()));
            }
        }

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
        
        JLabel useOverallBICLbl = new JLabel("Use Overall BIC");
        useOverallBICLbl.setToolTipText("When evaluating splits, use the Bayes Information Criterion for the overall cluster distribution.");
        useOverallBICCB.setToolTipText(useOverallBICLbl.getToolTipText());
        useOverallBICCB.setSelected(params.getUseOverallBIC());
        
        this.add(minMaxClustersLbl, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
        		GridBagConstraints.EAST, GridBagConstraints.NONE,
        		new Insets(10, 10, 5, 5), 0, 0));
        this.add(minMaxPanel, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
        		GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        		new Insets(10, 0, 5, 10), 0, 0));
        
        this.add(minClusterToMeanLbl, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
        		GridBagConstraints.EAST, GridBagConstraints.NONE,
        		new Insets(5, 10, 5, 5), 0, 0));
        this.add(minClusterToMeanTF, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0,
        		GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        		new Insets(5, 0, 5, 10), 0, 0));
        
        this.add(randomSeedLbl, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
        		GridBagConstraints.EAST, GridBagConstraints.NONE,
        		new Insets(5, 10, 5, 5), 0, 0));
        this.add(randomSeedTF, new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0,
        		GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        		new Insets(5, 0, 5, 10), 0, 0));

        this.add(threadCountLbl, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
        		GridBagConstraints.EAST, GridBagConstraints.NONE,
        		new Insets(5, 10, 5, 5), 0, 0));
        this.add(threadCountTF, new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0,
        		GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        		new Insets(5, 0, 5, 10), 0, 0));
       
        this.add(distanceMetricLbl, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, 
        		GridBagConstraints.EAST, GridBagConstraints.NONE, 
        		new Insets(5, 10, 5, 5), 0, 0));
        this.add(distanceMetricDD, new GridBagConstraints(1, 4, 1, 1, 1.0, 0.0, 
        		GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 
        		new Insets(5, 0, 5, 10), 0, 0));
        
        this.add(seedingMethodLbl, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, 
        		GridBagConstraints.EAST, GridBagConstraints.NONE, 
        		new Insets(5, 10, 5, 5), 0, 0));
        this.add(seedingDD, new GridBagConstraints(1, 5, 1, 1, 1.0, 0.0, 
        		GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 
        		new Insets(5, 0, 5, 10), 0, 0));

        this.add(useOverallBICLbl, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0, 
        		GridBagConstraints.EAST, GridBagConstraints.NONE, 
        		new Insets(5, 10, 10, 5), 0, 0));
        this.add(useOverallBICCB, new GridBagConstraints(1, 6, 1, 1, 1.0, 0.0, 
        		GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 
        		new Insets(5, 0, 10, 10), 0, 0));

        this.add(new JPanel(), new GridBagConstraints(0, 7, 2, 1, 1.0, 1.0, 
        		GridBagConstraints.CENTER, GridBagConstraints.BOTH, 
        		new Insets(0, 0, 0, 0), 0, 0));
    }
    
    public XMeansParamsPanel() {
    	this(null);
    }

    @Override
    public void setTupleCount(int tupleCount) {
    	int maxClusterCount = 0;
    	try {
    		maxClusterCount = Integer.parseInt(maxClustersTF.getText().trim());
    	} catch (NumberFormatException nfe) {
    	}
    	if (maxClusterCount == 0 || maxClusterCount > tupleCount) {
    		maxClusterCount = tupleCount;
    		maxClustersTF.setText(String.valueOf(maxClusterCount));
    	}
    	int minClusterCount = 0;
    	try {
    		minClusterCount = Integer.parseInt(minClustersTF.getText().trim());
    	} catch (NumberFormatException nfe) {
    	}
    	if (minClusterCount == 0 || minClusterCount > maxClusterCount) {
    		minClusterCount = maxClusterCount;
    		minClustersTF.setText(String.valueOf(minClusterCount));
    	}
    }

    public XMeansParams getValidatedParams(int tupleCount) throws ParamsPanelException {
    	
    	java.util.List<String> errorMessages = new ArrayList<String> ();
    	
    	XMeansParams.Builder builder = new XMeansParams.Builder();
    	
    	int minClusters = UIUtils.extractInt(minClustersTF, "minimum cluster count", 1, tupleCount, errorMessages);
    	
    	if (errorMessages.size() == 0) {
    		builder.minClusters(minClusters);
    	}
    	
    	if (maxClustersTF.getText().trim().length() > 0) {
    		int maxClusters = UIUtils.extractInt(maxClustersTF, "maximum cluster count", 1, tupleCount, errorMessages);
    		if (errorMessages.size() == 0) {
    			if (maxClusters < minClusters) {
    				errorMessages.add("The maximum cluster count cannot be less than the minimum cluster count.");
    			} else {
    				builder.maxClusters(maxClusters);
    			}
    		}
    	}
    	
    	double minClusterToMean = UIUtils.extractDouble(minClusterToMeanTF, "cluster size threashold", 0.0, 1.0, true, true, errorMessages);
    	int threadCount = UIUtils.extractInt(threadCountTF, "thread count", 1, Integer.MAX_VALUE, errorMessages);
    	long randomSeed = UIUtils.extractLong(randomSeedTF, "random seed", Long.MIN_VALUE, Long.MAX_VALUE, 
    			System.currentTimeMillis(), errorMessages);
    	
    	String distanceMetricName = distanceMetricDD.getSelectedItem().toString();
    	DistanceMetric distanceMetric = UIUtils.distanceMetric(distanceMetricName);
    	
    	ClusterSeeder seeder = null;
    	String seederName = seedingDD.getSelectedItem().toString();
    	if (seederName.equals(KMeansParamsPanel.RANDOM)) {
    		seeder = new RandomSeeder(randomSeed, new Random());
    	} else if (seederName.equals(KMeansParamsPanel.KDTREE)) {
    		seeder = new KDTreeSeeder(randomSeed, new Random());
    	} else {
    		seeder = new KMeansPlusPlusSeeder(randomSeed, new Random(), distanceMetric.clone());
    	}
  	
    	XMeansParams params = null;
    	
    	if (errorMessages.size() > 0) {
    		
    		throw new ParamsPanelException("GMeans Parameters Error", errorMessages);
    		
    	} else {
    		
    		builder.minClusterToMeanThreshold(minClusterToMean)
    				.workerThreadCount(threadCount)
    				.clusterSeeder(seeder)
    				.distanceMetric(distanceMetric)
    				.userOverallBIC(useOverallBICCB.isSelected());
    		
    		params = builder.build();
    	}
    	
    	return params;
   }
    
   @Override
   public Clusterer getNewClusterer(TupleList tuples) throws ParamsPanelException {
	   XMeansParams params = getValidatedParams(tuples.getTupleCount());
	   return new XMeansClusterer(tuples, params);
   }
    
   public static void main(String[] args) {
    
	/**
	 * @param args
	 */
    SwingUtilities.invokeLater(new Runnable() {
        public void run() {
            try {
                UIManager.setLookAndFeel(UIManager.
                                         getSystemLookAndFeelClassName());
            } catch (Exception exception) {
                exception.printStackTrace();
            }

            JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            
            final XMeansParamsPanel xmeansPanel = new XMeansParamsPanel();
            final int tupleCount = 10000;
            
            frame.getContentPane().add(xmeansPanel, BorderLayout.CENTER);
            
            JButton button = new JButton("Get Parameters");
            button.addActionListener(new ActionListener() {
				
            	@Override
				public void actionPerformed(ActionEvent e) {
				
            		XMeansParams params;
					try {
						params = xmeansPanel.getValidatedParams(tupleCount);
						if (params != null) {
							System.out.printf("seeder type = %s\n", params.getClusterSeeder().getClass().getSimpleName());
							System.out.printf("thread count = %d\n", params.getWorkerThreadCount());
							System.out.printf("distance metric = %s\n", params.getDistanceMetric().getClass().getSimpleName());
						}
					} catch (ParamsPanelException e1) {
						UIUtils.displayErrorDialog(xmeansPanel, e1.getMessage(), e1.getErrorList());
					}
					
				}
            	
            });
            
            frame.getContentPane().add(button, BorderLayout.SOUTH);
            
            frame.setSize(new Dimension(400, 400));
            
            frame.validate();

            // Center the window
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension frameSize = frame.getSize();
            if (frameSize.height > screenSize.height) {
                frameSize.height = screenSize.height;
            }
            if (frameSize.width > screenSize.width) {
                frameSize.width = screenSize.width;
            }
            frame.setLocation((screenSize.width - frameSize.width) / 2,
                              (screenSize.height - frameSize.height) / 2);
            frame.setVisible(true);
        }
    });
}

}
