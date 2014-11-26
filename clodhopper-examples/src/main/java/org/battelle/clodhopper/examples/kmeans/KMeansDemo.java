package org.battelle.clodhopper.examples.kmeans;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.concurrent.ExecutionException;

import javax.swing.*;

import org.battelle.clodhopper.Cluster;
import org.battelle.clodhopper.Clusterer;
import org.battelle.clodhopper.distance.EuclideanDistanceMetric;
import org.battelle.clodhopper.examples.TupleGenerator;
import org.battelle.clodhopper.examples.data.ExampleData;
import org.battelle.clodhopper.examples.project.Projection;
import org.battelle.clodhopper.examples.project.ProjectionParams;
import org.battelle.clodhopper.examples.project.Projector;
import org.battelle.clodhopper.examples.selection.SelectionEvent;
import org.battelle.clodhopper.examples.selection.SelectionListener;
import org.battelle.clodhopper.examples.ui.NumberDocument;
import org.battelle.clodhopper.examples.viz.ScatterPlot2D;
import org.battelle.clodhopper.kmeans.KMeansClusterer;
import org.battelle.clodhopper.kmeans.KMeansParams;
import org.battelle.clodhopper.seeding.KMeansPlusPlusSeeder;
import org.battelle.clodhopper.task.Task;
import org.battelle.clodhopper.task.TaskEvent;
import org.battelle.clodhopper.task.TaskListener;
import org.battelle.clodhopper.task.TaskOutcome;
import org.battelle.clodhopper.tuple.ArrayTupleList;
import org.battelle.clodhopper.tuple.TupleList;
import org.battelle.clodhopper.util.IntIterator;

public class KMeansDemo extends JPanel implements TaskListener, SelectionListener {

    private static final double GENERATION_PROGRESS_INC = 5.0;
    private static final double CLUSTERING_PROGRESS_INC = 90.0;
    private static final double PROJECTION_PROGRESS_INC = 5.0/2;

    private static final String RUN_TEXT = "Run Clustering Algorithms";

    private static final String CANCEL_TEXT = "Cancel Current Algorithm";

    public static final int DEFAULT_COORDS = 4000;

    public static final int DEFAULT_DIMENSIONS = 10;

    public static final int DEFAULT_CLUSTERS = 10;

    public static final long DEFAULT_SEED = 1234L;

    public static final double DEFAULT_STANDARD_DEV = 0.1;

    private JPanel mCenterPanel = new JPanel();

    private JPanel mLeftPanel = new JPanel();

    private JLabel mCoordLabel = new JLabel();

    private JLabel mDimensionsLabel = new JLabel();

    private JLabel mClusterLabel = new JLabel();

    private JLabel mRandomLabel = new JLabel();

    private JLabel mStandardDevLabel = new JLabel();

    private JLabel mNumProcessorsLabel = new JLabel();

    private JTextField mTupleCountTF = new JTextField();

    private JTextField mTupleLengthTF = new JTextField();

    private JTextField mClusterCountTF = new JTextField();

    private JTextField mRandomSeedTF = new JTextField();

    private JTextField mStandardDevTF = new JTextField();

    private JTextField mThreadCountTF = new JTextField();

    private JButton mRunButton = new JButton();

    private JProgressBar mProgressBar = new JProgressBar();

    private JScrollPane mResultsSP = new JScrollPane();

    private JTextArea mResultsTA = new JTextArea();

    private JSplitPane mMainSplitPane = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT);

    private ScatterPlot2D mGeneratedGalaxy, mXMeansGalaxy;

    private java.util.List<Cluster> mGeneratedClusters;
    
    // Set to true when kmeans is running
    private boolean mRunning;

    private Task<?> mCurrentTask;

    private TupleList tupleList;

    public KMeansDemo() {
        this(DEFAULT_COORDS, DEFAULT_DIMENSIONS, DEFAULT_CLUSTERS, 1234L,
                DEFAULT_STANDARD_DEV);
    }

    public KMeansDemo(int numCoords, int numDimensions,
            int numClusters, long randomSeed, double standardDev) {

        if (numCoords <= 0) {
            numCoords = DEFAULT_COORDS;
        }
        if (numDimensions <= 0) {
            numDimensions = DEFAULT_DIMENSIONS;
        }
        if (numClusters <= 0 || numClusters > numCoords) {
            numClusters = Math.min(DEFAULT_CLUSTERS, numCoords);
        }
        if (standardDev < 0.0) {
            standardDev = DEFAULT_STANDARD_DEV;
        }

        mLeftPanel.setLayout(new GridBagLayout());

        mCoordLabel.setText("Number of points:");
        mDimensionsLabel.setText("Number of dimensions:");
        mClusterLabel.setText("Number of clusters:");
        mRandomLabel.setText("Random seed:");
        mStandardDevLabel.setText("Standard deviation:");

        int maxProcessors = Runtime.getRuntime().availableProcessors();
        if (maxProcessors > 1) {
            mNumProcessorsLabel.setText("Number of processors [1-"
                    + maxProcessors + "]:");
        } else {
            mNumProcessorsLabel.setText("Number of processors:");
        }

        mTupleCountTF.setDocument(new NumberDocument(false, false));
        mTupleCountTF.setText(String.valueOf(numCoords));
        mTupleCountTF.setColumns(10);

        mTupleLengthTF.setDocument(new NumberDocument(false, false));
        mTupleLengthTF.setText(String.valueOf(numDimensions));
        mTupleLengthTF.setColumns(10);

        mClusterCountTF.setDocument(new NumberDocument(false, false));
        mClusterCountTF.setText(String.valueOf(numClusters));
        mClusterCountTF.setColumns(10);

        mRandomSeedTF.setDocument(new NumberDocument(false, false));
        mRandomSeedTF.setText(String.valueOf(randomSeed));
        mRandomSeedTF.setColumns(10);

        mStandardDevTF.setDocument(new NumberDocument(false, true));
        mStandardDevTF.setText(String.valueOf(standardDev));
        mStandardDevTF.setColumns(10);

        mThreadCountTF.setDocument(new NumberDocument(false, false));
        mThreadCountTF.setText(String.valueOf(maxProcessors));
        mThreadCountTF.setColumns(10);
        mThreadCountTF.setEnabled(maxProcessors > 1);

        mRunButton.setText(RUN_TEXT);
        mRunButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                runClustering();
            }
        });

        mResultsTA.setText("");
        mResultsTA.setEditable(false);
        // So the scrollpane doesn't grow vertically when the text area holds a lot of text
        // and the applet is resized.
        mResultsSP.setPreferredSize(new Dimension(1, 1));

        mResultsSP.setBorder(BorderFactory
                .createTitledBorder("Clustering Messages"));
        mResultsSP.getViewport().add(mResultsTA, null);

        mCenterPanel.setBorder(BorderFactory.createEtchedBorder());

        mGeneratedGalaxy = new ScatterPlot2D();
        mGeneratedGalaxy.setClustersVisible(true);
        mGeneratedGalaxy.setSelectActiveToolOnMousePress(true);

        Dimension dim = new Dimension(240, 240);
        mGeneratedGalaxy.setMinimumSize(dim);
        mGeneratedGalaxy.setPreferredSize(dim);
        
        // This places the ChartPanels for the clustering algorithms in mCenterPanel.
        initPlots();

        mLeftPanel.add(mCoordLabel, new GridBagConstraints(0, 0, 1, 1, 0.0,
                0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                new Insets(10, 10, 5, 5), 0, 0));
        mLeftPanel.add(mDimensionsLabel, new GridBagConstraints(0, 1, 1, 1,
                0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                new Insets(0, 10, 5, 5), 0, 0));
        mLeftPanel.add(mClusterLabel, new GridBagConstraints(0, 2, 1, 1, 0.0,
                0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                new Insets(0, 10, 5, 5), 0, 0));
        mLeftPanel.add(mRandomLabel, new GridBagConstraints(0, 3, 1, 1, 0.0,
                0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                new Insets(0, 10, 5, 5), 0, 0));
        mLeftPanel.add(mStandardDevLabel, new GridBagConstraints(0, 4, 1, 1,
                0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                new Insets(0, 10, 5, 5), 0, 0));
        mLeftPanel.add(mNumProcessorsLabel, new GridBagConstraints(0, 5, 1, 1,
                0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                new Insets(0, 10, 10, 5), 0, 0));
        // The size padding is set to 200 on this field to make the split pane divider
        // move to the right.
        mLeftPanel.add(mTupleCountTF, new GridBagConstraints(1, 0, 1, 1, 0.25, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                new Insets(10, 0, 5, 10), 100, 0));
        mLeftPanel.add(mTupleLengthTF, new GridBagConstraints(1, 1, 1, 1, 0.25,
                0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 5, 10), 0, 0));
        mLeftPanel.add(mClusterCountTF, new GridBagConstraints(1, 2, 1, 1, 0.0,
                0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 5, 10), 0, 0));
        mLeftPanel.add(mRandomSeedTF, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 5, 10), 0, 0));
        mLeftPanel.add(mStandardDevTF, new GridBagConstraints(1, 4, 1, 1, 0.0,
                0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 5, 10), 0, 0));
        mLeftPanel.add(mThreadCountTF, new GridBagConstraints(1, 5, 1, 1,
                0.25, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL, new Insets(0, 0, 10, 10), 0, 0));

        mLeftPanel.add(mRunButton, new GridBagConstraints(0, 6, 2, 1, 1.0,
                0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(0, 10, 10, 10), 0, 0));
        mLeftPanel.add(mProgressBar, new GridBagConstraints(0, 7, 2, 1, 1.0,
                0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(0, 10, 10, 10), 0, 0));
        mLeftPanel.add(mGeneratedGalaxy, new GridBagConstraints(0, 8, 2, 1, 0.0,
                0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 10, 10, 10), 0, 0));
        mLeftPanel.add(mResultsSP, new GridBagConstraints(0, 9, 2, 1, 0.0,
                1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 10, 10, 10), 0, 0));

        mMainSplitPane.setLeftComponent(mLeftPanel);
        mMainSplitPane.setRightComponent(mCenterPanel);

        this.setLayout(new BorderLayout());
        this.add(mMainSplitPane, BorderLayout.CENTER);
    }

    private void initPlots() {
        mXMeansGalaxy = new ScatterPlot2D();
        mXMeansGalaxy.setClustersVisible(true);
        mXMeansGalaxy.setSelectActiveToolOnMousePress(true);
        mCenterPanel.setLayout(new BorderLayout());
        mCenterPanel.add(mXMeansGalaxy,
                        BorderLayout.CENTER);
    }

    public void selectionChanged(SelectionEvent e) {
        if (e.getRequester() != this) {
            Object src = e.getSource();
            ExampleData other = null;
            if (src == mGeneratedGalaxy.getDataset()) {
                other = mXMeansGalaxy.getDataset();
            } else if (src == mXMeansGalaxy.getDataset()) {
                other = mGeneratedGalaxy.getDataset();
            }
            if (other != null) {
                IntIterator toSelect = ((ExampleData) src).getTupleSelectionModel().getSelected();
                other.getTupleSelectionModel().setSelected(this, toSelect);
            }
        }
    }

    private void runClustering() {

        if (!mRunning) {

            try {

                mNumCoords = getEnteredValue(mTupleCountTF, 1, 10000000);
                mNumDimensions = getEnteredValue(mTupleLengthTF, 2, 200);
                mNumClusters = getEnteredValue(mClusterCountTF, 1, (mNumCoords - 1));
                mSeed = getEnteredValue(mRandomSeedTF, Long.MIN_VALUE,
                        Long.MAX_VALUE);
                mNumProcessors = getEnteredValue(mThreadCountTF, 1, Runtime
                        .getRuntime().availableProcessors());
                mStandardDev = getEnteredValue(mStandardDevTF, 0.0, 1.0);

                mGeneratedClusters = null;

                // Clear the plots of data.
                mGeneratedGalaxy.setDataset(null);
                mXMeansGalaxy.setDataset(null);

                mBeginProgress = 0.0;

                mCurrentGalaxy = mGeneratedGalaxy;

                mResultsTA.setText("");

                final TupleGenerator genTask = new TupleGenerator(mNumDimensions, mNumCoords, 
                		mNumClusters, 4.0, mStandardDev, mStandardDev, new Random(mSeed));  
                
                genTask.addTaskListener(this);
                
                Thread t = new Thread(genTask);
                t.setDaemon(true);                
                t.start();
                
                mRunning = true;
                mRunButton.setText(CANCEL_TEXT);

            } catch (Exception e) {

                JOptionPane.showMessageDialog(this,
                        "There was a problem with one of the values you entered:\n\n"
                                + e.getMessage() + "\n\n"
                                + "Please correct the problem and try again.",
                        "Error", JOptionPane.ERROR_MESSAGE);

                mRunning = false;
            }

        } else {

            if (mCurrentTask != null) {
                mCurrentTask.cancel(true);
            }
        }

    }
    
    int mNumCoords;

    int mNumDimensions;

    int mNumClusters;

    long mSeed;

    int mNumProcessors;

    double mStandardDev;

    private double mBeginProgress, mEndProgress;

    long mStartTime;

    private ScatterPlot2D mCurrentGalaxy;

    /**
     * Displays an error dialog stating that insufficient memory is
     * available.
     * @param message the message to display.
     */
    private void displayInsufficientMemoryDialog(final String message) {

        String dlgMessage = null;
        if (message != null && message.length() > 0) {
            dlgMessage = "Insufficient memory is available ("
                    + message
                    + ").\n"
                    + "Try reducing the number of coordinates and/or the number of clusters.";
        } else {
            dlgMessage = "Insufficient memory is available.  Try reducing the \n"
                    + "number of coordinates and/or the number of clusters.";
        }

        JOptionPane.showMessageDialog(this, dlgMessage, "Insufficient Memory",
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Method for validating entries typed into text fields.
     * @param tf the text field to validate.
     * @param min the minimum value.
     * @param max the maximum value.
     * @return the entered value.
     */
    private static int getEnteredValue(final JTextField tf, final int min, final int max) {
        int value = 0;
        String s = tf.getText().trim();
        if (s.length() == 0) {
            throw new RuntimeException("blank entry");
        }
        try {
            value = Integer.parseInt(s);
            if (value < min || value > max) {
                throw new RuntimeException("outside range [" + min + " - "
                        + max + "]: " + value);
            }
        } catch (NumberFormatException nfe) {
            throw new RuntimeException("invalid number: " + s);
        }
        return value;
    }

    /**
     * Method for validating entries typed into text fields.
     * @param tf the text field to validate.
     * @param min the minimum value.
     * @param max the maximum value.
     * @return the entered value.
     */
    private static long getEnteredValue(final JTextField tf, final long min, final long max) {
        long value = 0L;
        String s = tf.getText().trim();
        if (s.length() == 0) {
            throw new RuntimeException("blank entry");
        }
        try {
            value = Long.parseLong(s);
            if (value < min || value > max) {
                throw new RuntimeException("outside range [" + min + " - "
                        + max + "]: " + value);
            }
        } catch (NumberFormatException nfe) {
            throw new RuntimeException("invalid number: " + s);
        }
        return value;
    }

    /**
     * Method for validating entries typed into text fields.
     * @param tf the text field to validate.
     * @param min the minimum value.
     * @param max the maximum value.
     * @return the entered value.
     */
    private static double getEnteredValue(final JTextField tf, final double min, final double max) {
        double value = 0;
        String s = tf.getText().trim();
        if (s.length() == 0) {
            throw new RuntimeException("blank entry");
        }
        try {
            value = Double.parseDouble(s);
            if (value < min || value > max) {
                throw new RuntimeException("outside range [" + min + " - "
                        + max + "]: " + value);
            }
        } catch (NumberFormatException nfe) {
            throw new RuntimeException("invalid number: " + s);
        }
        return value;
    }

    // Creates a new cluster list by sorting the clusters in the source 
    // using a CenterComparator.  This makes the plots have more 
    // consistent colors when two clustering methods give very similar results.
    private static java.util.List<Cluster> rearrangeClusters(java.util.List<Cluster> source) {
        Cluster[] clusters = source.toArray(new Cluster[source.size()]);
        Arrays.sort(clusters, new CenterComparator());
        java.util.List<Cluster> result = new ArrayList<Cluster>(clusters.length);
        for (int i=0; i<clusters.length; i++) {
        	result.add(clusters[i]);
        }
        return result;
    }

    // A simple comparator for sorting clusters by their centers.
    private static class CenterComparator implements Comparator<Cluster> {

        public int compare(Cluster c1, Cluster c2) {
            double[] center1 = c1.getCenter();
            double[] center2 = c2.getCenter();
            int n = center1.length;
            for (int i = 0; i < n; i++) {
                double d1 = center1[i];
                double d2 = center2[i];
                if (d1 < d2)
                    return -1;
                if (d1 > d2)
                    return +1;
            }
            return 0;
        }

    }

    private Clusterer setupNextClusterTask() {
        Clusterer clusterer = null;
        if (mCurrentGalaxy == mGeneratedGalaxy) {

        	KMeansPlusPlusSeeder seeder = new KMeansPlusPlusSeeder(mSeed, new Random(), new EuclideanDistanceMetric());
        	
        	KMeansParams params = new KMeansParams.Builder().clusterCount(mNumClusters).clusterSeeder(seeder).workerThreadCount(mNumProcessors).build();
        	
        	clusterer = new KMeansClusterer(tupleList, params);
        	
            mCurrentGalaxy = mXMeansGalaxy;
        }
        
        return clusterer;
    }

    private Projector setupNextProjectionTask(java.util.List<Cluster> clusters) {
        return new Projector(copyTuples(tupleList), clusters, new ProjectionParams());
    }
    
    private static TupleList copyTuples(TupleList tuples) {
    	final int tupleCount = tuples.getTupleCount();
    	final int tupleLength = tuples.getTupleLength();
    	TupleList result = new ArrayTupleList(tupleLength, tupleCount);
    	double[] buffer = new double[tupleLength];
    	for (int i=0; i<tupleCount; i++) {
    		result.setTuple(i, tuples.getTuple(i, buffer));
    	}
    	return result;
    }

    private void setProgress(final double progress) {
        if (SwingUtilities.isEventDispatchThread()) {
            if (progress < 0) {
                mProgressBar.setIndeterminate(true);
            } else {
                mProgressBar.setIndeterminate(false);
                mProgressBar.setValue((int) Math.round(progress));
            }
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    setProgress(progress);
                }
            });
        }
    }

    private void displayText(final String text) {
        if (SwingUtilities.isEventDispatchThread()) {
            mResultsTA.append(text);
            if (!text.endsWith("\n")) {
                mResultsTA.append("\n");
            }
            JScrollBar scrollBar = mResultsSP.getVerticalScrollBar();
            if (scrollBar != null)
                scrollBar.setValue(scrollBar.getMaximum());
        } else {
            SwingUtilities.invokeLater(new Runnable() {
               public void run() {
                   displayText(text);
               }
            });
        }
    }
    
    /**
     * Application entry point.
     *
     * @param args String[]
     */
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
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                JPanel contentPane = (JPanel) frame.getContentPane();
                contentPane.setLayout(new BorderLayout());
                frame.setSize(new Dimension(1200, 900));
                frame.setTitle("K-Means Demo");
                
                contentPane.add(new KMeansDemo(), BorderLayout.CENTER);
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

    public void taskBegun(TaskEvent e) {
        mStartTime = System.currentTimeMillis();
        displayText("\n" + e.getMessage() + "\n");
    }

    public void taskEnded(TaskEvent e) {
        
        long totalMS = System.currentTimeMillis() - mStartTime;
        Task<?> nextTask = null;
        displayText(e.getMessage() + " (" + totalMS + " ms)\n");
        Task<?> task = e.getTask();
        org.battelle.clodhopper.task.TaskOutcome outcome = task.getTaskOutcome();

        if (task instanceof TupleGenerator) {
        
            if (outcome == TaskOutcome.SUCCESS) {
                
                TupleGenerator genTask = (TupleGenerator) task;
                          
                try {
					tupleList = genTask.get();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (ExecutionException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
                
//                try {
//                	TupleIO.saveCSV(new File("tuples.csv"), tupleList);
//                } catch (IOException ioe) {
//                	ioe.printStackTrace();
//                }
                
                mBeginProgress += GENERATION_PROGRESS_INC;
                
                Projector projectionTask = setupNextProjectionTask(genTask.getClusters());
                
                projectionTask.setProgressEndpoints(mBeginProgress,
                    mBeginProgress + PROJECTION_PROGRESS_INC);
                
                nextTask = projectionTask;
            }
                
        } else if (task instanceof Projector) {
        
            if (outcome == TaskOutcome.SUCCESS) {
                
                Projector projector = (Projector) task;
                java.util.List<Cluster> clusters = projector.getClusters();
                
                Projection pdata = projector.getPointProjection();

                if (mCurrentGalaxy != null) {
                	
                	ExampleData dataset = new ExampleData(tupleList, clusters, pdata, projector.getClusterProjection());
                    dataset.getTupleSelectionModel().addSelectionListener(this);
                    
                    ExampleData oldDataset = mCurrentGalaxy.getDataset();
                    if (oldDataset != null) {
                        oldDataset.getTupleSelectionModel().removeSelectionListener(this);
                    }
                    
                    mCurrentGalaxy.setDataset(dataset);
                }
            
            }
            
            mBeginProgress += PROJECTION_PROGRESS_INC;
            
            nextTask = setupNextClusterTask();
            if (nextTask != null) {
                nextTask.setProgressEndpoints(mBeginProgress, mBeginProgress + CLUSTERING_PROGRESS_INC);
            }

        } else if (task instanceof Clusterer) {
            
            if (outcome == TaskOutcome.SUCCESS) {

                mBeginProgress += CLUSTERING_PROGRESS_INC;
                Clusterer clusterer = (Clusterer) task;
                
                try {
					nextTask = setupNextProjectionTask(clusterer.get());
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (ExecutionException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
                
                if (nextTask != null) {
                    nextTask.setProgressEndpoints(mBeginProgress,
                            mBeginProgress + PROJECTION_PROGRESS_INC);
                }
                
            } else {
            
                mBeginProgress += CLUSTERING_PROGRESS_INC;
                nextTask = setupNextClusterTask();
                if (nextTask != null) {
                    nextTask.setProgressEndpoints(mBeginProgress,
                        mBeginProgress + CLUSTERING_PROGRESS_INC);
                }
            }
        }
        
        if (nextTask != null) {
            nextTask.addTaskListener(this);
            Thread t = new Thread(nextTask);
            t.setDaemon(true);
            mCurrentTask = nextTask;
            t.start();
        } else {
            mRunning = false;
            mCurrentTask = null;
            mRunButton.setText(RUN_TEXT);
        }
        
        task.removeTaskListener(this);
    }

    public void taskMessage(TaskEvent e) {
        displayText(e.getMessage());
    }

    public void taskProgress(TaskEvent e) {
        setProgress(e.getProgress());
    }

	@Override
	public void taskPaused(TaskEvent e) {
        displayText(e.getMessage());
	}

	@Override
	public void taskResumed(TaskEvent e) {
        displayText(e.getMessage());
	}
}
