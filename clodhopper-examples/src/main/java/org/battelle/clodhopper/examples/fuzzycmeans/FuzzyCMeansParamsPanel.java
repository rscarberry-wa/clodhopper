package org.battelle.clodhopper.examples.fuzzycmeans;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import org.battelle.clodhopper.Clusterer;
import org.battelle.clodhopper.distance.*;
import org.battelle.clodhopper.examples.ui.NumberDocument;
import org.battelle.clodhopper.examples.ui.ParamsPanel;
import org.battelle.clodhopper.examples.ui.ParamsPanelException;
import org.battelle.clodhopper.examples.ui.UIUtils;
import org.battelle.clodhopper.fuzzycmeans.*;
import org.battelle.clodhopper.seeding.ClusterSeeder;
import org.battelle.clodhopper.seeding.KMeansPlusPlusSeeder;
import org.battelle.clodhopper.seeding.RandomSeeder;
import org.battelle.clodhopper.tuple.TupleList;

public class FuzzyCMeansParamsPanel extends JPanel 
	implements ParamsPanel {

    /**
	 * 
	 */
	private static final long serialVersionUID = 6323315870765877452L;
	
    public static final String KMEANS_PLUS_PLUS = "KMeans++";
    public static final String RANDOM = "Random";
    
    private JTextField clusterCountTF = new JTextField();
    private JTextField maxIterationsTF = new JTextField();
    private JTextField fuzzinessTF = new JTextField();
    private JTextField epsilonTF = new JTextField();
    private JTextField threadCountTF = new JTextField();
    private JComboBox distanceMetricDD = new JComboBox();
    private JComboBox seedingDD = new JComboBox();
    private JTextField randomSeedTF = new JTextField();
        
    public FuzzyCMeansParamsPanel(FuzzyCMeansParams params) {
        
        super(new GridBagLayout());
        
        boolean setRandomSeed = true;
        
        if (params == null) {
        	params = new FuzzyCMeansParams();
        	params.setMaxIterations(1000);
        	// Leave the text field blank, then use the system time if nothing is entered.
        	setRandomSeed = false;
        }
        
        clusterCountTF.setColumns(8);
        clusterCountTF.setDocument(new NumberDocument(false, false));
        clusterCountTF.setText(String.valueOf(params.getClusterCount()));
    
        maxIterationsTF.setColumns(8);
        maxIterationsTF.setDocument(new NumberDocument(false, false));
        maxIterationsTF.setText(String.valueOf(params.getMaxIterations()));
        
        JLabel fuzzinessLbl = new JLabel("Fuzziness:");
        fuzzinessLbl.setToolTipText("The fuzziness level (> 1.0) for clustering.");
        fuzzinessTF.setToolTipText(fuzzinessLbl.getToolTipText());
        fuzzinessTF.setColumns(8);
        fuzzinessTF.setDocument(new NumberDocument(false, true));
        fuzzinessTF.setText(String.valueOf(params.getFuzziness()));
        
        JLabel epsilonLbl = new JLabel("Epsilon:");
        epsilonLbl.setToolTipText("The epsilon (nonnegative) for clustering.");
        epsilonTF.setToolTipText(epsilonLbl.getToolTipText());
        epsilonTF.setColumns(8);
        epsilonTF.setDocument(new NumberDocument(false, true));
        epsilonTF.setText(String.valueOf(params.getEpsilon()));        
        
        threadCountTF.setColumns(8);
        threadCountTF.setDocument(new NumberDocument(false, false));
        threadCountTF.setText(String.valueOf(params.getWorkerThreadCount()));
        
        randomSeedTF.setColumns(8);
        randomSeedTF.setDocument(new NumberDocument(false, false));
        
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
        
        seedingDD.addItem(KMEANS_PLUS_PLUS);
        seedingDD.addItem(RANDOM);
        
        ClusterSeeder seeder = params.getClusterSeeder();
        if (seeder instanceof KMeansPlusPlusSeeder) {
            seedingDD.setSelectedItem(KMEANS_PLUS_PLUS);
            if (setRandomSeed) {
            	randomSeedTF.setText(String.valueOf(((KMeansPlusPlusSeeder) seeder).getRandomGeneratorSeed()));
            }
        } else {
            seedingDD.setSelectedItem(RANDOM);
            if (setRandomSeed && seeder instanceof RandomSeeder) {
                randomSeedTF.setText(String.valueOf(((RandomSeeder) seeder).getRandomGeneratorSeed()));
            }
        }
        
        JLabel clusterCountLbl = new JLabel("Cluster Count (K):");
        clusterCountLbl.setToolTipText("The desired number of clusters.");
        clusterCountTF.setToolTipText(clusterCountLbl.getToolTipText());
        
        JLabel maxIterationsLbl = new JLabel("Iteration Limit:");
        maxIterationsLbl.setToolTipText("The maximum number of clustering iterations.");
        maxIterationsTF.setToolTipText(maxIterationsLbl.getToolTipText());
        
        JLabel threadCountLbl = new JLabel("Thread Count:");
        threadCountLbl.setToolTipText("Number of threads to use for parallel computations.");
        threadCountTF.setToolTipText(threadCountLbl.getToolTipText());
        
        JLabel randomSeedLbl = new JLabel("Random Seed:");
        randomSeedLbl.setToolTipText("Seed for random number generation.");
        randomSeedTF.setToolTipText(randomSeedLbl.getToolTipText());
        
        JLabel distanceMetricLbl = new JLabel("Distance Metric:");
        distanceMetricLbl.setToolTipText("The distance metric to use.");
        distanceMetricDD.setToolTipText(distanceMetricLbl.getToolTipText());
        
        JLabel seedingMethodLbl = new JLabel("Seeding Method:");
        seedingMethodLbl.setToolTipText("Method to use for selecting the initial cluster seeds.");
        seedingDD.setToolTipText(seedingMethodLbl.getToolTipText());
        
        this.add(clusterCountLbl, 
                new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, 
                        GridBagConstraints.EAST, GridBagConstraints.NONE, 
                        new Insets(10, 10, 5, 5), 0, 0));
        this.add(clusterCountTF,
                new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, 
                        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 
                        new Insets(10, 0, 5, 10), 0, 0));
        
        this.add(maxIterationsLbl, 
                new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, 
                        GridBagConstraints.EAST, GridBagConstraints.NONE, 
                        new Insets(5, 10, 5, 5), 0, 0));
        this.add(maxIterationsTF,
                new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, 
                        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 
                        new Insets(5, 0, 5, 10), 0, 0));
        
        this.add(fuzzinessLbl, 
                new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, 
                        GridBagConstraints.EAST, GridBagConstraints.NONE, 
                        new Insets(5, 10, 5, 5), 0, 0));
        this.add(fuzzinessTF,
                new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0, 
                        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 
                        new Insets(5, 0, 5, 10), 0, 0));
        
        this.add(epsilonLbl, 
                new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, 
                        GridBagConstraints.EAST, GridBagConstraints.NONE, 
                        new Insets(5, 10, 5, 5), 0, 0));
        this.add(epsilonTF,
                new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0, 
                        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 
                        new Insets(5, 0, 5, 10), 0, 0));

        this.add(randomSeedLbl, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, 
                        GridBagConstraints.EAST, GridBagConstraints.NONE, 
                        new Insets(5, 10, 5, 5), 0, 0));
        this.add(randomSeedTF, new GridBagConstraints(1, 4, 1, 1, 1.0, 0.0, 
                        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 
                        new Insets(5, 0, 5, 10), 0, 0));
        
        this.add(threadCountLbl, 
                new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, 
                        GridBagConstraints.EAST, GridBagConstraints.NONE, 
                        new Insets(5, 10, 5, 5), 0, 0));
        this.add(threadCountTF,
                new GridBagConstraints(1, 5, 1, 1, 1.0, 0.0, 
                        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 
                        new Insets(5, 0, 5, 10), 0, 0));
                
        this.add(distanceMetricLbl, 
                new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0, 
                        GridBagConstraints.EAST, GridBagConstraints.NONE, 
                        new Insets(5, 10, 5, 5), 0, 0));
        this.add(distanceMetricDD, new GridBagConstraints(1, 6, 1, 1, 1.0, 0.0, 
                        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 
                        new Insets(5, 0, 5, 10), 0, 0));
        
        this.add(seedingMethodLbl, 
                new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0, 
                        GridBagConstraints.EAST, GridBagConstraints.NONE, 
                        new Insets(5, 10, 5, 5), 0, 0));
        this.add(seedingDD, 
                new GridBagConstraints(1, 7, 1, 1, 1.0, 0.0, 
                        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 
                        new Insets(5, 0, 5, 10), 0, 0));
                      
        this.add(new JPanel(), new GridBagConstraints(0, 8, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER,
        		GridBagConstraints.BOTH, new Insets(0,0,0,0), 0, 0));
    }
    
    public FuzzyCMeansParamsPanel() {
        this(null);
    }
    
    public void setClusterCount(int clusterCount) {
    	clusterCountTF.setText(String.valueOf(clusterCount));
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

    public FuzzyCMeansParams getValidatedParams(int tupleCount) 
    	throws ParamsPanelException {
    	
    	java.util.List<String> errorMessages = new ArrayList<String> ();
    	
    	int clusterCount = UIUtils.extractInt(clusterCountTF, "cluster count", 1, tupleCount, errorMessages);
    	int maxIterations = UIUtils.extractInt(maxIterationsTF, "maximum iterations", 1, Integer.MAX_VALUE, errorMessages);
    	double fuzziness = UIUtils.extractDouble(fuzzinessTF, "fuzziness", 1.0, Double.MAX_VALUE, true, true, errorMessages);
    	double epsilon = UIUtils.extractDouble(epsilonTF, "epsilon", 0.0, Double.MAX_VALUE, true, true, errorMessages);
    	int threadCount = UIUtils.extractInt(threadCountTF, "thread count", 1, Integer.MAX_VALUE, errorMessages);
    	long randomSeed = UIUtils.extractLong(randomSeedTF, "random seed", Long.MIN_VALUE, Long.MAX_VALUE, 
    			System.currentTimeMillis(), errorMessages);
    	
    	String distanceMetricName = distanceMetricDD.getSelectedItem().toString();
    	DistanceMetric distanceMetric = UIUtils.distanceMetric(distanceMetricName);
    	
    	ClusterSeeder seeder = null;
    	String seederName = seedingDD.getSelectedItem().toString();
    	if (seederName.equals(RANDOM)) {
    		seeder = new RandomSeeder(randomSeed, new Random());
    	} else {
    		seeder = new KMeansPlusPlusSeeder(randomSeed, new Random(), distanceMetric.clone());
    	}
    	
    	FuzzyCMeansParams params = null;
    	
    	if (errorMessages.size() > 0) {
    		
    		throw new ParamsPanelException("Fuzzy CMeans Parameters Error", errorMessages);
    		
    	} else {
    		
    		FuzzyCMeansParams.Builder builder = new FuzzyCMeansParams.Builder()
    			.clusterCount(clusterCount)
    			.maxIterations(maxIterations)
    			.fuzziness(fuzziness)
    			.epsilon(epsilon)
    			.workerThreadCount(threadCount)
    			.clusterSeeder(seeder)
    			.distanceMetric(distanceMetric);
    		
    		params = builder.build();
    	}
    	
    	return params;
    }

	@Override
	public Clusterer getNewClusterer(TupleList tuples)
			throws ParamsPanelException {
		FuzzyCMeansParams params = getValidatedParams(tuples.getTupleCount());
		return new FuzzyCMeansClusterer(tuples, params);
	}

	public static void main(String[] args) {
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
                
                final FuzzyCMeansParamsPanel paramsPanel = new FuzzyCMeansParamsPanel();
                final int tupleCount = 10000;
                paramsPanel.setClusterCount((int) Math.sqrt(tupleCount));
                
                frame.getContentPane().add(paramsPanel, BorderLayout.CENTER);
                
                JButton button = new JButton("Get Parameters");
                button.addActionListener(new ActionListener() {
					
                	@Override
					public void actionPerformed(ActionEvent e) {
					
                		FuzzyCMeansParams params;
						try {
							params = paramsPanel.getValidatedParams(10000);
							if (params != null) {
								System.out.printf("clusterCount = %d\n", params.getClusterCount());
								System.out.printf("maxIterations = %d\n", params.getMaxIterations());
								System.out.printf("fuzziness = %f\n", params.getFuzziness());
								System.out.printf("epsilon = %f\n", params.getEpsilon());
								System.out.printf("seeder type = %s\n", params.getClusterSeeder().getClass().getSimpleName());
								System.out.printf("thread count = %d\n", params.getWorkerThreadCount());
								System.out.printf("distance metric = %s\n", params.getDistanceMetric().getClass().getSimpleName());
							}
						} catch (ParamsPanelException e1) {
							UIUtils.displayErrorDialog(paramsPanel, e1.getMessage(), e1.getErrorList());
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
