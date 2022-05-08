package org.battelle.clodhopper.examples.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.battelle.clodhopper.Cluster;
import org.battelle.clodhopper.Clusterer;
import org.battelle.clodhopper.examples.data.ExampleData;
import org.battelle.clodhopper.examples.dbscan.DBSCANParamsPanel;
import org.battelle.clodhopper.examples.fuzzycmeans.FuzzyCMeansParamsPanel;
import org.battelle.clodhopper.examples.gmeans.GMeansParamsPanel;
import org.battelle.clodhopper.examples.hierarchical.ReverseNNHierarchicalParamsPanel;
import org.battelle.clodhopper.examples.hierarchical.StandardHierarchicalParamsPanel;
import org.battelle.clodhopper.examples.jarvispatrick.JarvisPatrickParamsPanel;
import org.battelle.clodhopper.examples.kmeans.KMeansParamsPanel;
import org.battelle.clodhopper.examples.project.Projection;
import org.battelle.clodhopper.examples.project.ProjectionParams;
import org.battelle.clodhopper.examples.project.Projector;
import org.battelle.clodhopper.examples.selection.SelectionEvent;
import org.battelle.clodhopper.examples.selection.SelectionListener;
import org.battelle.clodhopper.examples.selection.SelectionModel;
import org.battelle.clodhopper.examples.viz.ScatterPlot2D;
import org.battelle.clodhopper.examples.xmeans.XMeansParamsPanel;
import org.battelle.clodhopper.task.AbstractTask;
import org.battelle.clodhopper.task.ProgressHandler;
import org.battelle.clodhopper.task.Task;
import org.battelle.clodhopper.task.TaskAdapter;
import org.battelle.clodhopper.task.TaskEvent;
import org.battelle.clodhopper.task.TaskListener;
import org.battelle.clodhopper.task.TaskOutcome;
import org.battelle.clodhopper.tuple.FSTupleListFactory;
import org.battelle.clodhopper.tuple.TupleIO;
import org.battelle.clodhopper.tuple.TupleList;
import org.battelle.clodhopper.tuple.TupleListFactory;
import org.battelle.clodhopper.tuple.TupleListFactoryException;

public class ClodHopperUI extends JFrame implements SelectionListener {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    // Holds a MemoryMXBean for getting the current memory settings.
    // Use the initialization-on-demand holder idiom. instance isn't set until
    // the holder is accessed.
    private static class MXBeanHolder {

        private static final MemoryMXBean instance = ManagementFactory
                .getMemoryMXBean();
    }

    private static final String RUN_CLUSTERING = "Run Clustering";
    private static final String CANCEL_CLUSTERING = "Cancel Clustering";
    private static final String SAVE_RESULT = "Save Clustering Results";

    private static final String KMEANS = "KMeans";
    private static final String HIERARCHICAL = "Hierarchical";
    private static final String JARVISPATRICK = "Jarvis-Patrick";
    private static final String REVERSE_NN = "Reverse Nearest Neighbor";
    private static final String GMEANS = "GMeans";
    private static final String XMEANS = "XMeans";
    private static final String FUZZY_CMEANS = "Fuzzy CMeans";
    private static final String DBSCAN = "DBSCAN";

    private static final String[] CLUSTERING_METHODS = {KMEANS, HIERARCHICAL,
        JARVISPATRICK, REVERSE_NN, GMEANS, XMEANS, FUZZY_CMEANS, DBSCAN};

    private final JTabbedPane scatterPlotPane = new JTabbedPane();

    private final JTextField fileNameTF = new JTextField();
    private final JButton fileNameBrowseButton = new JButton("Browse...");

    private final JComboBox clusterTypeCB = new JComboBox();
    private final JPanel paramsPanel = new JPanel(new CardLayout());

    private final KMeansParamsPanel kmeansParamsPanel = new KMeansParamsPanel();
    private final StandardHierarchicalParamsPanel hierarchicalParamsPanel = new StandardHierarchicalParamsPanel();
    private final JarvisPatrickParamsPanel jarvisPatrickParamsPanel = new JarvisPatrickParamsPanel();
    private final ReverseNNHierarchicalParamsPanel reverseNNParamsPanel = new ReverseNNHierarchicalParamsPanel();
    private final XMeansParamsPanel xmeansParamsPanel = new XMeansParamsPanel();
    private final GMeansParamsPanel gmeansParamsPanel = new GMeansParamsPanel();
    private final FuzzyCMeansParamsPanel fuzzyCMeansParamsPanel = new FuzzyCMeansParamsPanel();
    private final DBSCANParamsPanel dbscanParamsPanel = new DBSCANParamsPanel();
    
    private final JTextArea statusTA = new JTextArea();
    private final JScrollPane statusSP = new JScrollPane(statusTA);
    private final JProgressBar progressBar = new JProgressBar();

    private final JButton runCancelButton = new JButton(RUN_CLUSTERING);
    private final JButton saveResultsButton = new JButton(SAVE_RESULT);

    private final JSplitPane mainSplitPane = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT);

    private JFileChooser fileChooser;
    private Map<String, ParamsPanel> paramsPanelMap = new HashMap<String, ParamsPanel>();

    private boolean loadingTuples;
    private String tuplesPath;
    private TupleList tuples;
    private TupleListFactory tupleListFactory;

    private java.util.List<Cluster> clusters;
    private Projection tupleProjection;
    private Projection clusterProjection;

    private ProcessingManager pm = new ProcessingManager();
    private Task<?> activeTask;

    public ClodHopperUI() {

        super("ClodHopper");

        runCancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runOrCancel();
            }
        });

        saveResultsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveClusterList();
            }
        });

        Container contentPane = this.getContentPane();

        JPanel leftPanel = new JPanel(new GridBagLayout());

        JLabel fileNameLbl = new JLabel("Input:");
        fileNameTF.setColumns(30);
        fileNameBrowseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectDataFile();
            }
        });
        fileNameTF.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (fileNameTF.getText().trim().length() > 0) {
                    conditionallyLoadTuples();
                }
            }
        });

        JLabel clusterTypeLbl = new JLabel("Method:");
        for (int i = 0; i < CLUSTERING_METHODS.length; i++) {
            clusterTypeCB.addItem(CLUSTERING_METHODS[i]);
        }
        clusterTypeCB.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                showProperParamsPanel();
            }
        });

        statusTA.setEditable(false);

        // These are probably the defaults.
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);

        scatterPlotPane.addContainerListener(new ContainerListener() {
            @Override
            public void componentAdded(ContainerEvent e) {
            }

            @Override
            public void componentRemoved(ContainerEvent e) {
                if (e.getChild() instanceof ScatterPlot2D) {
                    ((ScatterPlot2D) e.getChild()).getDataset()
                            .getTupleSelectionModel()
                            .removeSelectionListener(ClodHopperUI.this);
                }
            }
        });

        JPanel leftTopPanel = new JPanel(new GridBagLayout());
        leftTopPanel.add(fileNameLbl, new GridBagConstraints(0, 0, 1, 1, 0.0,
                0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                new Insets(0, 0, 5, 5), 0, 0));
        leftTopPanel.add(fileNameTF, new GridBagConstraints(1, 0, 1, 1, 1.0,
                0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 5, 5), 0, 0));
        leftTopPanel.add(fileNameBrowseButton, new GridBagConstraints(2, 0, 1,
                1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 0, 5, 0), 0, 0));
        leftTopPanel.add(clusterTypeLbl, new GridBagConstraints(0, 1, 1, 1,
                0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 5), 0, 0));
        leftTopPanel.add(clusterTypeCB, new GridBagConstraints(1, 1, 2, 1, 1.0,
                0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 0), 0, 0));

        // paramsPanel and paramsPanelMap must contain the same pairings.
        paramsPanel.add(kmeansParamsPanel, KMEANS);
        paramsPanelMap.put(KMEANS, kmeansParamsPanel);

        paramsPanel.add(hierarchicalParamsPanel, HIERARCHICAL);
        paramsPanelMap.put(HIERARCHICAL, hierarchicalParamsPanel);

        paramsPanel.add(jarvisPatrickParamsPanel, JARVISPATRICK);
        paramsPanelMap.put(JARVISPATRICK, jarvisPatrickParamsPanel);

        paramsPanel.add(reverseNNParamsPanel, REVERSE_NN);
        paramsPanelMap.put(REVERSE_NN, reverseNNParamsPanel);

        paramsPanel.add(xmeansParamsPanel, XMEANS);
        paramsPanelMap.put(XMEANS, xmeansParamsPanel);

        paramsPanel.add(gmeansParamsPanel, GMEANS);
        paramsPanelMap.put(GMEANS, gmeansParamsPanel);

        paramsPanel.add(fuzzyCMeansParamsPanel, FUZZY_CMEANS);
        paramsPanelMap.put(FUZZY_CMEANS, fuzzyCMeansParamsPanel);
        
        paramsPanel.add(dbscanParamsPanel, DBSCAN);
        paramsPanelMap.put(DBSCAN, dbscanParamsPanel);

        paramsPanel.setBorder(BorderFactory.createTitledBorder("Parameters"));

        leftPanel.add(leftTopPanel, new GridBagConstraints(0, 0, 1, 1, 1.0,
                0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(10, 10, 5, 10), 0, 0));

        leftPanel.add(paramsPanel, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(5, 10, 5, 10), 0, 0));
        leftPanel.add(statusSP, new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
                        5, 10, 2, 10), 0, 0));
        leftPanel.add(progressBar, new GridBagConstraints(0, 3, 1, 1, 1.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(0, 10, 5, 10), 0, 0));
        leftPanel.add(runCancelButton, new GridBagConstraints(0, 4, 1, 1, 1.0,
                0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(5, 10, 5, 10), 0, 0));
        leftPanel
                .add(saveResultsButton, new GridBagConstraints(0, 5, 1, 1, 1.0,
                        0.0, GridBagConstraints.CENTER,
                        GridBagConstraints.HORIZONTAL,
                        new Insets(0, 10, 10, 10), 0, 0));

        mainSplitPane.setLeftComponent(leftPanel);
        mainSplitPane.setRightComponent(scatterPlotPane);

        contentPane.add(mainSplitPane, BorderLayout.CENTER);

        showProperParamsPanel();
    }

    /**
     * Propagates selections between scatter plot panels.
     */
    public void selectionChanged(SelectionEvent e) {
        if (e.getRequester() != this && e.getSource() instanceof ExampleData) {
            ExampleData src = (ExampleData) e.getSource();
            SelectionModel srcModel = src.getTupleSelectionModel();
            final int tabCount = scatterPlotPane.getTabCount();
            for (int i = 0; i < tabCount; i++) {
                Component comp = scatterPlotPane.getComponentAt(i);
                if (comp instanceof ScatterPlot2D) {
                    ExampleData data = ((ScatterPlot2D) comp).getDataset();
                    if (data != src) {
                        data.getTupleSelectionModel().setSelected(this,
                                srcModel.getSelected());
                    }
                }
            }
        }
    }

    private synchronized void loadTuples(final String dataPath,
            final boolean continueWithClustering) {

        if (loadingTuples) {
            return;
        }

        tuples = null;
        tuplesPath = null;

        // Null these out in case they hold old references.
        if (tupleListFactory != null) {
            try {
                tupleListFactory.closeAll();
            } catch (TupleListFactoryException e) {
                // Unlikely, but should be able to ignore.
            } finally {
                tupleListFactory = null;
            }
        }

        ProgressMonitor pm = new ProgressMonitor(this, "Loading Data", "Loading data...", 0, 100);
        pm.setMillisToPopup(100);
        pm.setMillisToDecideToPopup(100);

        final TupleReader tupleReader = new TupleReader(dataPath, pm);

        tupleReader.addTaskListener(new TaskAdapter() {

            @Override
            public void taskEnded(TaskEvent e) {

                TaskOutcome outcome = tupleReader.getTaskOutcome();

                String popupTitle = null;
                String popupMessage = null;
                int popupType = JOptionPane.ERROR_MESSAGE;

                if (outcome == TaskOutcome.SUCCESS) {
                    try {
                        // These are stored in object fields, since they will be
                        // needed later.
                        tupleListFactory = tupleReader.getTupleListFactory();
                        tuples = tupleListFactory.openExistingTupleList(TupleReader.TUPLE_LIST_NAME);
                        tuplesPath = tupleReader.getTuplePath();
                        if (tuples.getTupleCount() > 0
                                && tuples.getTupleLength() > 0) {
                            if (continueWithClustering) {
                                try {
                                    Clusterer clusterer = setupClusterer(tuples);
                                    clusterer.setProgressEndpoints(0.0, 95.0);
                                    launchTask(clusterer);
                                } catch (ParamsPanelException ppe) {
                                    popupTitle = ppe.getMessage();
                                    popupMessage = UIUtils
                                            .constructMultilineHTMLMessage(ppe
                                                    .getErrorList());
                                }
                            } else {
                                activeTask = null;
                                getSelectedParamsPanel().setTupleCount(tuples.getTupleCount());
                                updateEnabling();
                            }
                        } else { // tuples has 0 data elements
                            popupTitle = "Error Opening Tuples";
                            popupMessage = "The tuple data file contains no elements.";
                        }
                    } catch (TupleListFactoryException e1) {
                        // This is very unlikely. If TupleReader finished
                        // successfully, the tuples
                        // should already be open.
                        popupTitle = "Error Opening Tuples";
                        popupMessage = (e1.getMessage() != null ? e1
                                .getMessage() : e1.toString());
                    }
                } else if (outcome == TaskOutcome.ERROR) {
                    popupTitle = "Error Opening Tuples";
                    popupMessage = tupleReader.getErrorMessage().orElse("reason unknown");
                } else if (outcome == TaskOutcome.CANCELLED) {
                    popupTitle = "Canceled";
                    popupMessage = "Loading of tuples was canceled.";
                    popupType = JOptionPane.INFORMATION_MESSAGE;
                }

                if (popupTitle != null) {

                    final Component comp = ClodHopperUI.this;
                    final String title = popupTitle;
                    final String message = popupMessage;
                    final int pType = popupType;

                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            // So after an error, it doesn't try loading the
                            // same bad file again.
                            if (tuples != null) {
                                fileNameTF.setText(tuplesPath);
                            } else {
                                fileNameTF.setText("");
                            }
                            JOptionPane.showMessageDialog(comp, message, title,
                                    pType);
                            loadingTuples = false;
                        }
                    });

                } else {
                    loadingTuples = false;
                }
            }
        });

        loadingTuples = true;
        launchFirstTask(tupleReader);
    }

    private void updateEnabling() {
        boolean isProcessing = activeTask != null && !(activeTask instanceof TupleReader);
        saveResultsButton.setEnabled(!isProcessing && (clusters != null));
        fileNameTF.setEditable(!isProcessing);
        fileNameBrowseButton.setEnabled(!isProcessing);
        clusterTypeCB.setEnabled(!isProcessing);
        runCancelButton.setEnabled(!(activeTask instanceof TupleReader));
        ParamsPanel paramsPanel = getSelectedParamsPanel();
        paramsPanel.setEnabled(!isProcessing);
    }

    private ParamsPanel getSelectedParamsPanel() {
        return paramsPanelMap.get(clusterTypeCB.getSelectedItem().toString());
    }

    private void selectDataFile() {
        if (fileChooser == null) {
            fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File("."));
        }
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fileChooser.getSelectedFile();
            if (f != null) {
                fileNameTF.setText(f.getAbsolutePath());
                conditionallyLoadTuples();
            }
        }
    }

    private void showProperParamsPanel() {
        CardLayout cl = (CardLayout) paramsPanel.getLayout();
        cl.show(paramsPanel, (String) clusterTypeCB.getSelectedItem());
        if (tuples != null) {
            getSelectedParamsPanel().setTupleCount(tuples.getTupleCount());
        }
    }

    private void conditionallyLoadTuples() {
        String dataPath = fileNameTF.getText().trim();
        if (dataPath.length() > 0) {
            if (tuples == null || !dataPath.equals(tuplesPath)) {
                loadTuples(dataPath, false);
            }
        }
    }

    private void runOrCancel() {
        if (activeTask != null) {
            if (!activeTask.isDone()) {
                activeTask.cancel(true);
            }
        } else {

            String dataPath = fileNameTF.getText().trim();
            if (dataPath.length() == 0) {
                JOptionPane.showMessageDialog(this,
                        "You must specify the file containing your data.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (tuples != null && dataPath.equals(tuplesPath)) {
                // Don't need to load the data again, since the path hasn't been
                // changed.
                try {
                    Clusterer clusterer = setupClusterer(tuples);
                    // Leave 5% for projection.
                    clusterer.setProgressEndpoints(0.0, 95.0);
                    launchFirstTask(clusterer);
                } catch (ParamsPanelException e) {
                    UIUtils.displayErrorDialog(this, e.getMessage(),
                            e.getErrorList());
                }
            } else { // Have to load the data.

                loadTuples(dataPath, true);

            }
        }
    }

    /**
     * Saves the clusters to a csv file, one cluster per line, with just the
     * indexes of the tuples assigned to the cluster. The center of the cluster
     * is not saved to the file, only the membership.
     */
    private void saveClusterList() {

        java.util.List<Cluster> clustersToSave = null;

        int selectedTab = scatterPlotPane.getSelectedIndex();
        if (selectedTab >= 0) {
            Component c = scatterPlotPane.getComponentAt(selectedTab);
            if (c instanceof ScatterPlot2D) {
                System.out.printf("Saving clusters for %s\n",
                        scatterPlotPane.getTitleAt(selectedTab));
                clustersToSave = ((ScatterPlot2D) c).getDataset().getClusters();
            }
        } else {
            clustersToSave = clusters;
        }

        if (clustersToSave != null) {

            if (fileChooser == null) {
                fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory(new File("."));
            }

            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setSelectedFile(null);

            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File f = fileChooser.getSelectedFile();
                if (f != null) {
                    if (f.exists()) {
                        int opt = JOptionPane.showConfirmDialog(this, "File "
                                + f.getName()
                                + " exists.  Overwrite this file?");
                        if (opt != JOptionPane.YES_OPTION
                                && opt != JOptionPane.OK_OPTION) {
                            return;
                        }
                    }
                    // Just embed the output code here for now. Probably will
                    // move this to a utility class for cluster i/o later after
                    // getting feedback on the format, etc.
                    PrintWriter pw = null;
                    try {
                        pw = new PrintWriter(new BufferedWriter(new FileWriter(
                                f)));
                        for (Cluster c : clustersToSave) {
                            pw.println(csvStringForCluster(c));
                        }
                    } catch (IOException ioe) {
                        JOptionPane.showMessageDialog(this,
                                "Error saving clusters: " + ioe.getMessage(),
                                "Error Saving Clusters",
                                JOptionPane.ERROR_MESSAGE);
                    } finally {
                        if (pw != null) {
                            pw.close();
                        }
                    }
                }
            }
        }
    }

    private static String csvStringForCluster(Cluster c) {
        StringBuilder sb = new StringBuilder();
        final int memberCount = c.getMemberCount();
        for (int i = 0; i < memberCount; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(c.getMember(i));
        }
        return sb.toString();
    }

    private boolean alreadyHasTitle(String title) {
        final int tabs = scatterPlotPane.getTabCount();
        for (int i = 0; i < tabs; i++) {
            if (title.equals(scatterPlotPane.getTitleAt(i))) {
                return true;
            }
        }
        return false;
    }

    private void launchFirstTask(Task<?> task) {
        // Null these, since they will be regenerated.
        clusters = null;
        tupleProjection = null;
        clusterProjection = null;
        if (!(task instanceof TupleReader)) {
            runCancelButton.setText(CANCEL_CLUSTERING);
        }
        statusTA.setText("");
        progressBar.setValue(progressBar.getMinimum());
        launchTask(task);
        updateEnabling();
    }

    private Clusterer setupClusterer(TupleList tuples)
            throws ParamsPanelException {
        String clusterType = clusterTypeCB.getSelectedItem().toString();
        ParamsPanel paramsPanel = paramsPanelMap.get(clusterType);
        return paramsPanel.getNewClusterer(tuples);
    }

    private Projector setupProjector(TupleList scratchTuples,
            java.util.List<Cluster> clusters) {
        // Defaults to 2 dimensions with a gravity of 0.2.
        ProjectionParams params = new ProjectionParams();
        return new Projector(scratchTuples, clusters, params);
    }

    private void appendToStatus(final String message) {
        if (message == null || message.length() == 0) {
            return;
        }
        if (SwingUtilities.isEventDispatchThread()) {
            statusTA.append(message);
            statusTA.append("\n");
            // Scroll it to the end, so the latest message is
            // visible.
            JScrollBar scrollBar = statusSP.getVerticalScrollBar();
            if (scrollBar != null) {
                scrollBar.setValue(scrollBar.getMaximum());
            }
        } else {
            // Interaction with swing components should be on
            // the EDT.
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    appendToStatus(message);
                }
            });
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

    private void launchTask(Task<?> task) {
        task.addTaskListener(pm);
        Thread t = new Thread(task);
        t.setDaemon(true);
        activeTask = task;
        t.start();
    }

    private class ProcessingManager implements TaskListener {

        @Override
        public void taskBegun(TaskEvent e) {
            appendToStatus(e.getMessage());
        }

        @Override
        public void taskMessage(TaskEvent e) {
            appendToStatus("  ... " + e.getMessage());
        }

        @Override
        public void taskProgress(TaskEvent e) {
            setProgress(e.getProgress());
        }

        @Override
        public void taskPaused(TaskEvent e) {
        }

        @Override
        public void taskResumed(TaskEvent e) {
        }

        @Override
        public void taskEnded(TaskEvent e) {

            appendToStatus(e.getMessage() + "\n");

            Task<?> nextTask = null;
            Task<?> task = e.getTask();

            task.removeTaskListener(this);

            TaskOutcome outcome = task.getTaskOutcome();

            String popupTitle = null;
            String popupMessage = null;
            int popupMessageType = JOptionPane.ERROR_MESSAGE;

            if (outcome == TaskOutcome.SUCCESS) {
                if (task instanceof Clusterer) {
                    Clusterer clusterer = (Clusterer) task;
                    clusters = clusterer.getClusters().get();
                    // Copy the tuples, since projection may modify them. Keep
                    // the originals
                    // unmodified in case we recluster.
                    TupleList scratchTuples;
                    try {
                        if (tupleListFactory.hasTuplesFor("scratch")) {
                            tupleListFactory.deleteTupleList(tupleListFactory
                                    .openExistingTupleList("scratch"));
                        }
                        scratchTuples = tupleListFactory.copyTupleList(
                                "scratch", tuples);
                        nextTask = setupProjector(scratchTuples, clusters);
                    } catch (TupleListFactoryException e1) {
                        popupTitle = "Projection Error";
                        popupMessage = "Error copying tuples: "
                                + e1.getMessage();
                    }
                } else if (task instanceof Projector) {

                    Projector projector = (Projector) task;
                    tupleProjection = projector.getPointProjection();
                    clusterProjection = projector.getClusterProjection();

                    final ExampleData dataset = new ExampleData(tuples,
                            clusters, tupleProjection, clusterProjection);

                    // Do the rest on the EDT, since it modifies the UI.
                    //
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {

                            dataset.getTupleSelectionModel()
                                    .addSelectionListener(ClodHopperUI.this);

                            String tabTitle = clusterTypeCB.getSelectedItem()
                                    .toString();
                            if (alreadyHasTitle(tabTitle)) {
                                String baseTitle = tabTitle;
                                int n = 2;
                                do {
                                    tabTitle = String.format("%s (%d)",
                                            baseTitle, n++);
                                } while (alreadyHasTitle(tabTitle));
                            }

                            ScatterPlot2D scatterPlot = new ScatterPlot2D();
                            scatterPlot.setDataset(dataset);
                            scatterPlot.setClustersVisible(true);
                            scatterPlot.setSelectActiveToolOnMousePress(true);

                            int tabNumber = scatterPlotPane.getTabCount();

                            scatterPlotPane.add(tabTitle, scatterPlot);
                            scatterPlotPane.setTabComponentAt(tabNumber,
                                    new CloseableTab(scatterPlotPane));
                            scatterPlotPane.setSelectedIndex(tabNumber);
                        }

                    });
                }

            } else if (outcome == TaskOutcome.ERROR) {

                if (task instanceof Clusterer) {
                    popupMessage = task.getErrorMessage().orElse("reason unknown");
                    popupTitle = "Error Clustering";
                } else if (task instanceof Projector) {
                    popupMessage = task.getErrorMessage().orElse("reason unknown");
                    popupTitle = "Error Projecting";
                }

            } else if (outcome == TaskOutcome.CANCELLED) {

                if (task instanceof Clusterer) {
                    popupTitle = "Clustering Canceled";
                    popupMessage = "Clustering has been canceled.";
                } else if (task instanceof Projector) {
                    popupTitle = "Projection Canceled";
                    popupMessage = "Projection has been canceled";
                }

                popupMessageType = JOptionPane.INFORMATION_MESSAGE;

            }

            if (popupMessage != null) {

                final String title = popupTitle;
                final String message = popupMessage;
                final int msgType = popupMessageType;

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        JOptionPane.showMessageDialog(ClodHopperUI.this,
                                message, title, msgType);
                    }
                });

            }

            if (nextTask != null) {
                launchTask(nextTask);
            } else {
                // All done!
                runCancelButton.setText(RUN_CLUSTERING);
                activeTask = null;
                updateEnabling();
            }

        }

    }

    private static class TupleReader extends AbstractTask<TupleList> {

        public static final String TUPLE_LIST_NAME = "tuples";
        private String path;
        private TupleListFactory factory;
        private ProgressMonitor progMonitor;

        TupleReader(String path, ProgressMonitor pm) {
            if (path == null) {
                throw new NullPointerException();
            }
            this.path = path;
            this.progMonitor = pm;
            if (pm != null) {
                this.addTaskListener(new TaskAdapter() {
                    public void taskProgress(TaskEvent e) {
                        double p = e.getProgress();
                        if (p >= getEndProgress()) {
                            progMonitor.setProgress(progMonitor.getMaximum());
                        } else {
                            int min = progMonitor.getMinimum();
                            int max = progMonitor.getMaximum();
                            int diff = max - min;
                            double begin = getBeginProgress();
                            double end = getEndProgress();
                            double spread = end - begin;
                            if (spread > 0.0) {
                                int iprogress = (int) (min + (p - begin)
                                        * (diff) / spread);
                                progMonitor.setProgress(iprogress);
                            }
                        }
                    }
                });
            }
        }

        @Override
        public String taskName() {
            return "loading of input data";
        }

        @Override
        protected void checkForCancel() {
            if (progMonitor != null && progMonitor.isCanceled()) {
                if (!this.isCancelled()) {
                    this.cancel(true);
                }
            }
            super.checkForCancel();
        }

        @Override
        protected TupleList doTask() throws Exception {

            ProgressHandler ph = new ProgressHandler(this);

            ph.postBegin();

            // Give creation of the factory 10%
            ph.subsection(0.1);
            ph.postBegin();
            this.factory = createTupleListFactory();
            ph.postEnd();

            // Give reading of the tuples 90%
            ph.subsection(0.9);
            ph.postBegin();

            File f = new File(path);

            TupleList tuples = TupleIO.loadCSV(f, "tuples", factory, this, ph);
            // Ends the 0.9 subsection
            ph.postEnd();

            ph.postMessage(String.format("%d tuples of length %d loaded from %s",
                    tuples.getTupleCount(), tuples.getTupleLength(), f.getName()));
            ph.postMessage(String.format("tuple list class = %s", tuples.getClass().getSimpleName()));

            // Ends the top-level
            ph.postEnd();

            return tuples;
        }

        public String getTuplePath() {
            return path;
        }

        public TupleListFactory getTupleListFactory() {
            return factory;
        }

        private TupleListFactory createTupleListFactory() throws IOException {

            MemoryUsage memUsage = MXBeanHolder.instance.getHeapMemoryUsage();
            long freeHeap = memUsage.getMax() - memUsage.getUsed();

            long ramThreshold = Math.max(0L, freeHeap / 4L);
            long fileThreshold = 1024L * 1024L * 1024L;

            String tmpDirPath = System.getProperty("java.io.tmpdir");
            File tmpdir = new File(tmpDirPath);
            File coordsTempDir = new File(tmpdir, "tuples_"
                    + String.valueOf(System.currentTimeMillis() % 100000));
            if (!coordsTempDir.exists()) {
                if (!coordsTempDir.mkdirs()) {
                    throw new IOException(
                            "could not make temporary directory: "
                            + coordsTempDir.getAbsolutePath());
                }
            }

            return new FSTupleListFactory(coordsTempDir, ramThreshold,
                    fileThreshold, fileThreshold);
        }

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager
                            .getSystemLookAndFeelClassName());
                } catch (Exception exception) {
                    exception.printStackTrace();
                }

                ClodHopperUI frame = new ClodHopperUI();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                Dimension screenSize = Toolkit.getDefaultToolkit()
                        .getScreenSize();
                int fwidth = Math.min(1024, screenSize.width);
                int fheight = Math.min(650 * fwidth / 1024, screenSize.height);
                frame.setSize(new Dimension(fwidth, fheight));
                frame.validate();

                // Center the window
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
