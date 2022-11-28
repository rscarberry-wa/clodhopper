package org.battelle.clodhopper.examples.dbscan;

import org.battelle.clodhopper.Cluster;
import org.battelle.clodhopper.dbscan.DBSCANParams;
import org.battelle.clodhopper.examples.data.ExampleData;
import org.battelle.clodhopper.examples.selection.SelectionEvent;
import org.battelle.clodhopper.examples.selection.SelectionListener;
import org.battelle.clodhopper.examples.ui.NumberDocument;
import org.battelle.clodhopper.examples.viz.ScatterPlot2D;
import org.battelle.clodhopper.task.Task;
import org.battelle.clodhopper.task.TaskEvent;
import org.battelle.clodhopper.task.TaskListener;
import org.battelle.clodhopper.tuple.TupleList;
import org.battelle.clodhopper.util.IntIterator;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class DBSCANDemo extends JPanel implements TaskListener, SelectionListener {

    private static final String RUN_TEXT = "Run DBSCAN Clustering";
    private static final String CANCEL_TEXT = "Cancel Clustering";

    // The center panel, which will display the scatter plot of the clustered dataset.
    private JPanel centerPanel = new JPanel();

    // Panel on the left which will hold the other components.
    private JPanel leftPanel = new JPanel();

    // Labels and corresponding text fields to go at the top of leftPanel.
    private JLabel epsilonLabel = new JLabel();
    private JLabel numPointsLabel = new JLabel();
    private JLabel numClustersLabel = new JLabel();
    private JLabel randomSeedLabel = new JLabel();
    private JLabel standardDevLabel = new JLabel();

    private JTextField epsilonTextField = new JTextField();
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
        numPointsLabel.setText("Number of Points");
        numClustersLabel.setText("Number of Clusters");
        randomSeedLabel.setText("Random Seed");
        standardDevLabel.setText("Standard Deviation");

        epsilonTextField.setDocument(new NumberDocument(false, true));
        numPointsTextField.setDocument(new NumberDocument(false, false));
        numClustersTextField.setDocument(new NumberDocument(false, false));
        randomSeedTextField.setDocument(new NumberDocument(false, false));
        standardDevTextField.setDocument(new NumberDocument(false, true));

        epsilonTextField.setText(String.valueOf(epsilon));
        numPointsTextField.setText(String.valueOf(numPoints));
        numClustersTextField.setText(String.valueOf(numClusters));
        randomSeedTextField.setText(String.valueOf(randomSeed));
        standardDevTextField.setText(String.valueOf(standardDev));

        epsilonTextField.setColumns(10);
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
        leftPanel.add(numPointsLabel, new GridBagConstraints(0, 1, 1, 1,
                0.0, 0.0,
                GridBagConstraints.EAST, GridBagConstraints.NONE,
                new Insets(0, 10, 5, 5), 0, 0));
        leftPanel.add(numClustersLabel, new GridBagConstraints(0, 2, 1, 1,
                0.0, 0.0,
                GridBagConstraints.EAST, GridBagConstraints.NONE,
                new Insets(0, 10, 5, 5), 0, 0));
        leftPanel.add(randomSeedLabel, new GridBagConstraints(0, 3, 1, 1,
                0.0, 0.0,
                GridBagConstraints.EAST, GridBagConstraints.NONE,
                new Insets(0, 10, 5, 5), 0, 0));
        leftPanel.add(standardDevLabel, new GridBagConstraints(0, 4, 1, 1,
                0.0, 0.0,
                GridBagConstraints.EAST, GridBagConstraints.NONE,
                new Insets(0, 10, 10, 5), 0, 0));

        leftPanel.add(epsilonTextField, new GridBagConstraints(1, 0, 1, 1,
                0.25, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                new Insets(10, 0, 5, 10), 100, 0));
        leftPanel.add(numPointsTextField, new GridBagConstraints(1, 1, 1, 1,
                0.25, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 5, 10), 0, 0));
        leftPanel.add(numClustersTextField, new GridBagConstraints(1, 2, 1, 1,
                0.25, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 5, 10), 0, 0));
        leftPanel.add(randomSeedTextField, new GridBagConstraints(1, 3, 1, 1,
                0.25, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 5, 10), 0, 0));
        leftPanel.add(standardDevTextField, new GridBagConstraints(1, 4, 1, 1,
                0.25, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 10, 10), 0, 0));

        leftPanel.add(runButton, new GridBagConstraints(0, 5, 2, 1,
                1.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(0, 10, 10, 10), 0, 0));
        leftPanel.add(progressBar, new GridBagConstraints(0, 6, 2, 1,
                1.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(0, 10, 10, 10), 0, 0));
        leftPanel.add(generatedDataPlot, new GridBagConstraints(0, 7, 2, 1,
                0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 10, 10, 10), 0, 0));
        leftPanel.add(resultsScrollPane, new GridBagConstraints(0, 8, 2, 1,
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

    private void runClustering() {
        if (!running) {

        } else {
            if (currentTask != null) currentTask.cancel(true);
        }
    }

    @Override
    public void taskBegun(TaskEvent e) {

    }

    @Override
    public void taskMessage(TaskEvent e) {

    }

    @Override
    public void taskProgress(TaskEvent e) {

    }

    @Override
    public void taskPaused(TaskEvent e) {

    }

    @Override
    public void taskResumed(TaskEvent e) {

    }

    @Override
    public void taskEnded(TaskEvent e) {

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
