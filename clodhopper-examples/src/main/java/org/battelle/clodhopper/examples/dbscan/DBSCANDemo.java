package org.battelle.clodhopper.examples.dbscan;

import org.battelle.clodhopper.Cluster;
import org.battelle.clodhopper.Clusterer;
import org.battelle.clodhopper.dbscan.DBSCANClusterer;
import org.battelle.clodhopper.dbscan.DBSCANParams;
import org.battelle.clodhopper.distance.EuclideanDistanceMetric;
import org.battelle.clodhopper.examples.data.ExampleData;
import org.battelle.clodhopper.examples.generation.CircularArcTupleGenerator;
import org.battelle.clodhopper.examples.generation.ClusteredTuples;
import org.battelle.clodhopper.examples.generation.NormalTupleGenerator;
import org.battelle.clodhopper.examples.project.Projection;
import org.battelle.clodhopper.examples.project.ProjectionParams;
import org.battelle.clodhopper.examples.project.Projector;
import org.battelle.clodhopper.examples.selection.SelectionEvent;
import org.battelle.clodhopper.examples.selection.SelectionListener;
import org.battelle.clodhopper.examples.ui.NumberDocument;
import static org.battelle.clodhopper.examples.ui.UIUtils.*;
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

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class DBSCANDemo extends JPanel implements TaskListener, SelectionListener {

    private static final double GENERATION_PROGRESS_INC = 5.0;
    private static final double CLUSTERING_PROGRESS_INC = 90.0;
    private static final double PROJECTION_PROGRESS_INC = 5.0/2;

    private static final String RUN_TEXT = "Run DBSCAN Clustering";
    private static final String CANCEL_TEXT = "Cancel Clustering";

    // The center panel, which will display the scatter plot of the clustered dataset.
    private JPanel centerPanel = new JPanel();

    // Panel on the left which will hold the other components.
    private JPanel leftPanel = new JPanel();

    // Labels and corresponding text fields to go at the top of leftPanel.
    private JLabel epsilonLabel = new JLabel();
    private JLabel minSamplesLabel = new JLabel();
    private JLabel numPointsLabel = new JLabel();
    private JLabel numClustersLabel = new JLabel();
    private JLabel randomSeedLabel = new JLabel();
    private JLabel standardDevLabel = new JLabel();

    private JTextField epsilonTextField = new JTextField();
    private JTextField minSamplesTextField = new JTextField();
    private JTextField numPointsTextField = new JTextField();
    private JTextField numClustersTextField = new JTextField();
    private JTextField randomSeedTextField = new JTextField();
    private JTextField standardDevTextField = new JTextField();

    private JButton runButton = new JButton();
    private JProgressBar progressBar = new JProgressBar();

    private JTextArea resultsTextArea = new JTextArea();
    private JScrollPane resultsScrollPane = new JScrollPane();

    private JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

    private ScatterPlot2D generatedDataPlot, dscanDataPlot;
    private List<Cluster> generatedClusters;

    private boolean running;

    private Task<?> currentTask;

    private TupleList tupleList;

    public DBSCANDemo() {
        this(DBSCANParams.DEFAULT_EPSILON, DBSCANParams.DEFAULT_MIN_SAMPLES,
                1000, 4, 1234L, 0.1);
    }
    public DBSCANDemo(
            double epsilon,
            int minSamples,
            int numPoints,
            int numClusters,
            long randomSeed,
            double standardDev) {

        leftPanel.setLayout(new GridBagLayout());

        epsilonLabel.setText("Epsilon");
        minSamplesLabel.setText("Minimum Samples");
        numPointsLabel.setText("Number of Points");
        numClustersLabel.setText("Number of Clusters");
        randomSeedLabel.setText("Random Seed");
        standardDevLabel.setText("Standard Deviation");

        epsilonTextField.setDocument(new NumberDocument(false, true));
        minSamplesTextField.setDocument(new NumberDocument(false, false));
        numPointsTextField.setDocument(new NumberDocument(false, false));
        numClustersTextField.setDocument(new NumberDocument(false, false));
        randomSeedTextField.setDocument(new NumberDocument(false, false));
        standardDevTextField.setDocument(new NumberDocument(false, true));

        epsilonTextField.setText(String.valueOf(epsilon));
        minSamplesTextField.setText(String.valueOf(minSamples));
        numPointsTextField.setText(String.valueOf(numPoints));
        numClustersTextField.setText(String.valueOf(numClusters));
        randomSeedTextField.setText(String.valueOf(randomSeed));
        standardDevTextField.setText(String.valueOf(standardDev));

        epsilonTextField.setColumns(10);
        minSamplesTextField.setColumns(10);
        numPointsTextField.setColumns(10);
        numClustersTextField.setColumns(10);
        randomSeedTextField.setColumns(10);
        standardDevTextField.setColumns(10);

        runButton.setText(RUN_TEXT);
        runButton.addActionListener(e -> runClustering());

        resultsTextArea.setText("");
        resultsTextArea.setEditable(false);
        // To prevent the SP growing vertically when the text area holds a lot of text and
        // the frame is resized.
        resultsScrollPane.setPreferredSize(new Dimension(1, 1));
        resultsScrollPane.setBorder(BorderFactory.createTitledBorder("Clustering Status"));
        resultsScrollPane.getViewport().add(resultsTextArea, null);

        centerPanel.setBorder(BorderFactory.createEtchedBorder());

        generatedDataPlot = new ScatterPlot2D();
        generatedDataPlot.setClustersVisible(true);
        generatedDataPlot.setSelectActiveToolOnMousePress(true);
        Dimension dim = new Dimension(240, 240);
        generatedDataPlot.setMinimumSize(dim);
        generatedDataPlot.setPreferredSize(dim);

        // Sets up dscanDataPlot in center panel.
        initDBSCANPlot();

        // Arrange all the components in the layout
        leftPanel.add(epsilonLabel, new GridBagConstraints(0, 0, 1, 1,
                0.0, 0.0,
                GridBagConstraints.EAST, GridBagConstraints.NONE,
                new Insets(10, 10, 5, 5), 0, 0));
        leftPanel.add(minSamplesLabel, new GridBagConstraints(0, 1, 1, 1,
                0.0, 0.0,
                GridBagConstraints.EAST, GridBagConstraints.NONE,
                new Insets(0, 10, 5, 5), 0, 0));
        leftPanel.add(numPointsLabel, new GridBagConstraints(0, 2, 1, 1,
                0.0, 0.0,
                GridBagConstraints.EAST, GridBagConstraints.NONE,
                new Insets(0, 10, 5, 5), 0, 0));
        leftPanel.add(numClustersLabel, new GridBagConstraints(0, 3, 1, 1,
                0.0, 0.0,
                GridBagConstraints.EAST, GridBagConstraints.NONE,
                new Insets(0, 10, 5, 5), 0, 0));
        leftPanel.add(randomSeedLabel, new GridBagConstraints(0, 4, 1, 1,
                0.0, 0.0,
                GridBagConstraints.EAST, GridBagConstraints.NONE,
                new Insets(0, 10, 5, 5), 0, 0));
        leftPanel.add(standardDevLabel, new GridBagConstraints(0, 5, 1, 1,
                0.0, 0.0,
                GridBagConstraints.EAST, GridBagConstraints.NONE,
                new Insets(0, 10, 10, 5), 0, 0));

        leftPanel.add(epsilonTextField, new GridBagConstraints(1, 0, 1, 1,
                0.25, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                new Insets(10, 0, 5, 10), 100, 0));
        leftPanel.add(minSamplesTextField, new GridBagConstraints(1, 1, 1, 1,
                0.25, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 5, 10), 0, 0));
        leftPanel.add(numPointsTextField, new GridBagConstraints(1, 2, 1, 1,
                0.25, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 5, 10), 0, 0));
        leftPanel.add(numClustersTextField, new GridBagConstraints(1, 3, 1, 1,
                0.25, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 5, 10), 0, 0));
        leftPanel.add(randomSeedTextField, new GridBagConstraints(1, 4, 1, 1,
                0.25, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 5, 10), 0, 0));
        leftPanel.add(standardDevTextField, new GridBagConstraints(1, 5, 1, 1,
                0.25, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 10, 10), 0, 0));

        leftPanel.add(runButton, new GridBagConstraints(0, 6, 2, 1,
                1.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(0, 10, 10, 10), 0, 0));
        leftPanel.add(progressBar, new GridBagConstraints(0, 7, 2, 1,
                1.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(0, 10, 10, 10), 0, 0));
        leftPanel.add(generatedDataPlot, new GridBagConstraints(0, 8, 2, 1,
                0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 10, 10, 10), 0, 0));
        leftPanel.add(resultsScrollPane, new GridBagConstraints(0, 9, 2, 1,
                0.0, 1.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 10, 10, 10), 0, 0));

        mainSplitPane.setLeftComponent(leftPanel);
        mainSplitPane.setRightComponent(centerPanel);

        this.setLayout(new BorderLayout());
        this.add(mainSplitPane, BorderLayout.CENTER);
    }

    private void initDBSCANPlot() {
        dscanDataPlot = new ScatterPlot2D();
        dscanDataPlot.setClustersVisible(true);
        dscanDataPlot.setSelectActiveToolOnMousePress(true);
        centerPanel.setLayout(new BorderLayout());
        centerPanel.add(dscanDataPlot, BorderLayout.CENTER);
    }

    private double epsilon;
    private int minSamples;
    private int tupleCount;
    private int clusterCount;
    private long randomSeed;
    private double standardDev;

    private ClusteredTuples generatedClusteredTuples;
    private ScatterPlot2D currentDataPlot;

    private double beginProgress;
    private long startTime;

    private void runClustering() {

        if (!running) {

            try {

                epsilon = getEnteredValue(epsilonTextField, 0.0, 1.0);
                minSamples = getEnteredValue(minSamplesTextField, 1, 1000);
                tupleCount = getEnteredValue(numPointsTextField, 1, 10000000);
                clusterCount = getEnteredValue(numClustersTextField, 2, tupleCount - 1);
                randomSeed = getEnteredValue(randomSeedTextField, Long.MIN_VALUE, Long.MAX_VALUE);
                standardDev = getEnteredValue(standardDevTextField, 0.0, 1.0);

                generatedClusteredTuples = null;

                // Will clear the plots
                generatedDataPlot.setDataset(null);
                dscanDataPlot.setDataset(null);

                beginProgress = 0.0;
                currentDataPlot = generatedDataPlot;
                resultsTextArea.setText("");

                final CircularArcTupleGenerator genTask = new CircularArcTupleGenerator(
                        tupleCount, clusterCount, standardDev, new Random(randomSeed)
                );

                genTask.addTaskListener(this);

                Thread t = new Thread(genTask);
                t.setDaemon(true);
                t.start();

                running = true;
                runButton.setText(CANCEL_TEXT);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "There was a problem with one of the values you entered:\n\n"
                                + e.getMessage() + "\n\n"
                                + "Please correct the problem and try again.",
                        "Error", JOptionPane.ERROR_MESSAGE);

                running = false;
            }

        } else {
            if (currentTask != null) currentTask.cancel(true);
        }
    }

    private Projector setupNextProjectionTask(java.util.List<Cluster> clusters) {
        return new Projector(copyTuples(generatedClusteredTuples.getTuples()), clusters, new ProjectionParams());
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

    private Clusterer setupNextClusterTask() {
        Clusterer clusterer = null;
        if (currentDataPlot == generatedDataPlot) {
            DBSCANParams params = new DBSCANParams(epsilon, minSamples, new EuclideanDistanceMetric());
            clusterer = new DBSCANClusterer(generatedClusteredTuples.getTuples(), params);
            currentDataPlot = dscanDataPlot;
        }
        return clusterer;
    }

    @Override
    public void taskBegun(TaskEvent e) {
        startTime = System.currentTimeMillis();
        displayText("\n" + e.getMessage() + "\n");
    }

    @Override
    public void taskMessage(TaskEvent e) {
        displayText(e.getMessage());
    }

    @Override
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

    @Override
    public void taskEnded(TaskEvent e) {
        long totalMS = System.currentTimeMillis() - startTime;
        Task<?> nextTask = null;
        displayText(e.getMessage() + " (" + totalMS + " ms)\n");
        Task<?> task = e.getTask();
        org.battelle.clodhopper.task.TaskOutcome outcome = task.getTaskOutcome();

        if (task instanceof CircularArcTupleGenerator) {

            if (outcome == TaskOutcome.SUCCESS) {

                CircularArcTupleGenerator genTask = (CircularArcTupleGenerator) task;

                try {
                    generatedClusteredTuples = genTask.get();
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (ExecutionException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

                beginProgress += GENERATION_PROGRESS_INC;

                Projector projectionTask = setupNextProjectionTask(generatedClusteredTuples.getClusters());

                projectionTask.setProgressEndpoints(beginProgress,
                        beginProgress + PROJECTION_PROGRESS_INC);

                nextTask = projectionTask;
            }

        } else if (task instanceof Projector) {

            if (outcome == TaskOutcome.SUCCESS) {

                Projector projector = (Projector) task;
                java.util.List<Cluster> clusters = projector.getClusters();

                Projection pdata = projector.getPointProjection();

                if (currentDataPlot != null) {

                    ExampleData dataset = new ExampleData(generatedClusteredTuples.getTuples(),
                            clusters, pdata, projector.getClusterProjection());
                    dataset.getTupleSelectionModel().addSelectionListener(this);

                    ExampleData oldDataset = currentDataPlot.getDataset();
                    if (oldDataset != null) {
                        oldDataset.getTupleSelectionModel().removeSelectionListener(this);
                    }

                    currentDataPlot.setDataset(dataset);
                }

            }

            beginProgress += PROJECTION_PROGRESS_INC;

            nextTask = setupNextClusterTask();
            if (nextTask != null) {
                nextTask.setProgressEndpoints(beginProgress, beginProgress + CLUSTERING_PROGRESS_INC);
            }

        } else if (task instanceof Clusterer) {

            if (outcome == TaskOutcome.SUCCESS) {

                beginProgress += CLUSTERING_PROGRESS_INC;
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
                    nextTask.setProgressEndpoints(beginProgress,
                            beginProgress + PROJECTION_PROGRESS_INC);
                }

            } else {

                beginProgress += CLUSTERING_PROGRESS_INC;
                nextTask = setupNextClusterTask();
                if (nextTask != null) {
                    nextTask.setProgressEndpoints(beginProgress,
                            beginProgress + CLUSTERING_PROGRESS_INC);
                }
            }
        }

        if (nextTask != null) {
            nextTask.addTaskListener(this);
            Thread t = new Thread(nextTask);
            t.setDaemon(true);
            currentTask = nextTask;
            t.start();
        } else {
            running = false;
            currentTask = null;
            runButton.setText(RUN_TEXT);
        }

        task.removeTaskListener(this);
    }

    @Override
    public void selectionChanged(SelectionEvent e) {
        if (e.getRequester() != this) {
            Object src = e.getSource();
            ExampleData other = null;
            if (src == generatedDataPlot.getDataset()) {
                other = dscanDataPlot.getDataset();
            } else if (src == dscanDataPlot.getDataset()) {
                other = generatedDataPlot.getDataset();
            }
            if (other != null) {
                IntIterator toSelect = ((ExampleData) src).getTupleSelectionModel().getSelected();
                other.getTupleSelectionModel().setSelected(this, toSelect);
            }
        }
    }

    private void setProgress(final double progress) {
        if (SwingUtilities.isEventDispatchThread()) {
            if (progress < 0) {
                progressBar.setIndeterminate(true);
            } else {
                progressBar.setIndeterminate(false);
                progressBar.setValue((int) Math.round(progress));
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
            resultsTextArea.append(text);
            if (!text.endsWith("\n")) {
                resultsTextArea.append("\n");
            }
            JScrollBar scrollBar = resultsScrollPane.getVerticalScrollBar();
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
                frame.setTitle("DBSCAN Clustering Demo");

                contentPane.add(new DBSCANDemo(), BorderLayout.CENTER);
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
