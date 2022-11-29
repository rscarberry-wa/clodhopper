package org.battelle.clodhopper.examples.multiple;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;

import org.battelle.clodhopper.Cluster;
import org.battelle.clodhopper.Clusterer;
import org.battelle.clodhopper.distance.EuclideanDistanceMetric;
import org.battelle.clodhopper.examples.generation.ClusteredTuples;
import org.battelle.clodhopper.examples.generation.NormalTupleGenerator;
import org.battelle.clodhopper.examples.data.ExampleData;
import org.battelle.clodhopper.examples.project.Projection;
import org.battelle.clodhopper.examples.project.ProjectionParams;
import org.battelle.clodhopper.examples.project.Projector;
import org.battelle.clodhopper.examples.selection.SelectionEvent;
import org.battelle.clodhopper.examples.selection.SelectionListener;
import org.battelle.clodhopper.examples.selection.SelectionModel;
import org.battelle.clodhopper.examples.ui.NumberDocument;
import org.battelle.clodhopper.examples.viz.ScatterPlot2D;
import org.battelle.clodhopper.fuzzycmeans.FuzzyCMeansClusterer;
import org.battelle.clodhopper.fuzzycmeans.FuzzyCMeansParams;
import org.battelle.clodhopper.gmeans.GMeansClusterer;
import org.battelle.clodhopper.gmeans.GMeansParams;
import org.battelle.clodhopper.hierarchical.HierarchicalParams;
import org.battelle.clodhopper.hierarchical.ReverseNNHierarchicalClusterer;
import org.battelle.clodhopper.hierarchical.StandardHierarchicalClusterer;
import org.battelle.clodhopper.kmeans.KMeansClusterer;
import org.battelle.clodhopper.kmeans.KMeansParams;
import org.battelle.clodhopper.seeding.KMeansPlusPlusSeeder;
import org.battelle.clodhopper.task.Task;
import org.battelle.clodhopper.task.TaskEvent;
import org.battelle.clodhopper.task.TaskListener;
import org.battelle.clodhopper.task.TaskOutcome;
import org.battelle.clodhopper.tuple.ArrayTupleList;
import org.battelle.clodhopper.tuple.TupleList;
import org.battelle.clodhopper.xmeans.XMeansClusterer;
import org.battelle.clodhopper.xmeans.XMeansParams;

public class GeneratedDataPanel extends JPanel implements TaskListener,
		SelectionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final double GENERATION_PROGRESS_INC = 2.0;
	private static final double CLUSTERING_PROGRESS_INC = 93.0 / 6.0;
	private static final double PROJECTION_PROGRESS_INC = 5.0 / 7;

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

	private JTextField mCoordTF = new JTextField();

	private JTextField mDimensionsTF = new JTextField();

	private JTextField mClustersTF = new JTextField();

	private JTextField mRandomTF = new JTextField();

	private JTextField mStandardDevTF = new JTextField();

	private JTextField mNumProcessorsTF = new JTextField();

	private JButton mRunButton = new JButton();

	private JProgressBar mProgressBar = new JProgressBar();

	private JScrollPane mResultsSP = new JScrollPane();

	private JTextArea mResultsTA = new JTextArea();

	private JSplitPane mMainSplitPane = new JSplitPane(
			JSplitPane.HORIZONTAL_SPLIT);

	// The datasets plotted in the JFreeChart ChartPanels
	private ScatterPlot2D mGeneratedClusterData, mKMeansClusterData,
			mXMeansClusterData, mGMeansClusterData, mHierarchicalClusterData,
			mWardsClusterData, mFuzzyCMeansClusterData;
	// Contains the above Galaxies to make it easier to propagate selections.
	private java.util.List<ScatterPlot2D> mGalaxies = new ArrayList<ScatterPlot2D>();

	public void selectionChanged(SelectionEvent e) {
		if (e.getRequester() != this && e.getSource() instanceof ExampleData) {
			ExampleData src = (ExampleData) e.getSource();
			SelectionModel srcModel = src.getTupleSelectionModel();
			Iterator<ScatterPlot2D> it = mGalaxies.iterator();
			while (it.hasNext()) {
				ScatterPlot2D g = it.next();
				ExampleData dataset = g.getDataset();
				if (dataset != null && dataset != src) {
					dataset.getTupleSelectionModel().setSelected(this,
							srcModel.getSelected());
				}
			}
		}
	}

	// Set to true when kmeans is running
	private boolean mRunning;

	private Task<?> mCurrentTask;

	private ClusteredTuples mGeneratedClusteredTuples;

	public GeneratedDataPanel() {
		this(DEFAULT_COORDS, DEFAULT_DIMENSIONS, DEFAULT_CLUSTERS, 1234L,
				DEFAULT_STANDARD_DEV);
	}

	public GeneratedDataPanel(int numCoords, int numDimensions,
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

		mCoordTF.setDocument(new NumberDocument(false, false));
		mCoordTF.setText(String.valueOf(numCoords));
		mCoordTF.setColumns(10);

		mDimensionsTF.setDocument(new NumberDocument(false, false));
		mDimensionsTF.setText(String.valueOf(numDimensions));
		mDimensionsTF.setColumns(10);

		mClustersTF.setDocument(new NumberDocument(false, false));
		mClustersTF.setText(String.valueOf(numClusters));
		mClustersTF.setColumns(10);

		mRandomTF.setDocument(new NumberDocument(false, false));
		mRandomTF.setText(String.valueOf(randomSeed));
		mRandomTF.setColumns(10);

		mStandardDevTF.setDocument(new NumberDocument(false, true));
		mStandardDevTF.setText(String.valueOf(standardDev));
		mStandardDevTF.setColumns(10);

		mNumProcessorsTF.setDocument(new NumberDocument(false, false));
		mNumProcessorsTF.setText(String.valueOf(maxProcessors));
		mNumProcessorsTF.setColumns(10);
		mNumProcessorsTF.setEnabled(maxProcessors > 1);

		mRunButton.setText(RUN_TEXT);
		mRunButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				runClustering();
			}
		});

		mResultsTA.setText("");
		mResultsTA.setEditable(false);
		// So the scrollpane doesn't grow vertically when the text area holds a
		// lot of text
		// and the applet is resized.
		mResultsSP.setPreferredSize(new Dimension(1, 1));

		mResultsSP.setBorder(BorderFactory
				.createTitledBorder("Clustering Messages"));
		mResultsSP.getViewport().add(mResultsTA, null);

		mCenterPanel.setBorder(BorderFactory.createEtchedBorder());

		// Set up the dataset and panel for the generated test data.
		mGeneratedClusterData = new ScatterPlot2D();
		mGeneratedClusterData.setClustersVisible(true);
		mGeneratedClusterData.setSelectActiveToolOnMousePress(true);

		Dimension dim = new Dimension(240, 240);
		mGeneratedClusterData.setMinimumSize(dim);
		mGeneratedClusterData.setPreferredSize(dim);
		JPanel generatedPanel = initChartPanel(mGeneratedClusterData,
				"Generated Clusters");

		// This places the ChartPanels for the clustering algorithms in
		// mCenterPanel.
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
		// The size padding is set to 200 on this field to make the split pane
		// divider
		// move to the right.
		mLeftPanel.add(mCoordTF, new GridBagConstraints(1, 0, 1, 1, 0.25, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				new Insets(10, 0, 5, 10), 100, 0));
		mLeftPanel.add(mDimensionsTF, new GridBagConstraints(1, 1, 1, 1, 0.25,
				0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				new Insets(0, 0, 5, 10), 0, 0));
		mLeftPanel.add(mClustersTF, new GridBagConstraints(1, 2, 1, 1, 0.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				new Insets(0, 0, 5, 10), 0, 0));
		mLeftPanel.add(mRandomTF, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				new Insets(0, 0, 5, 10), 0, 0));
		mLeftPanel.add(mStandardDevTF, new GridBagConstraints(1, 4, 1, 1, 0.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				new Insets(0, 0, 5, 10), 0, 0));
		mLeftPanel.add(mNumProcessorsTF, new GridBagConstraints(1, 5, 1, 1,
				0.25, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(0, 0, 10, 10), 0, 0));

		mLeftPanel.add(mRunButton, new GridBagConstraints(0, 6, 2, 1, 1.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(0, 10, 10, 10), 0, 0));
		mLeftPanel.add(mProgressBar, new GridBagConstraints(0, 7, 2, 1, 1.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(0, 10, 10, 10), 0, 0));
		mLeftPanel.add(generatedPanel, new GridBagConstraints(0, 8, 2, 1, 0.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 10, 10, 10), 0, 0));
		mLeftPanel.add(mResultsSP, new GridBagConstraints(0, 9, 2, 1, 0.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 10, 10, 10), 0, 0));

		mMainSplitPane.setLeftComponent(mLeftPanel);
		mMainSplitPane.setRightComponent(mCenterPanel);

		this.setLayout(new BorderLayout());
		this.add(mMainSplitPane, BorderLayout.CENTER);
	}

	private void initPlots() {
		mKMeansClusterData = new ScatterPlot2D();
		mXMeansClusterData = new ScatterPlot2D();
		mGMeansClusterData = new ScatterPlot2D();
		mHierarchicalClusterData = new ScatterPlot2D();
		mWardsClusterData = new ScatterPlot2D();
		mFuzzyCMeansClusterData = new ScatterPlot2D();
		mCenterPanel.setLayout(new GridLayout(3, 2));
		mCenterPanel
				.add(initChartPanel(mKMeansClusterData, "K-Means Clusters"));
		mCenterPanel
				.add(initChartPanel(mXMeansClusterData, "X-Means Clusters"));
		mCenterPanel
				.add(initChartPanel(mGMeansClusterData, "G-Means Clusters"));
		mCenterPanel.add(initChartPanel(mHierarchicalClusterData,
				"Hierarchical Clusters"));
		mCenterPanel.add(initChartPanel(mWardsClusterData, "Ward's Clusters"));
		mCenterPanel.add(initChartPanel(mFuzzyCMeansClusterData,
				"Fuzzy C-Means Clusters"));
	}

	private JPanel initChartPanel(ScatterPlot2D galaxy, String title) {
		galaxy.setClustersVisible(true);
		galaxy.setSelectActiveToolOnMousePress(true);
		mGalaxies.add(galaxy);
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		JLabel label = new JLabel(title);
		label.setFont(new Font(label.getFont().getName(), Font.BOLD, 18));
		label.setBackground(galaxy.getBackground());
		label.setOpaque(true);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		panel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		panel.add(label, BorderLayout.NORTH);
		panel.add(galaxy, BorderLayout.CENTER);
		return panel;
	}

	int mNumCoords;
	int mNumDimensions;
	int mNumClusters;
	long mSeed;
	int mNumProcessors;
	double mStandardDev;

	private double mBeginProgress;

	long mStartTime;

	private ScatterPlot2D mCurrentGalaxy;

	private void runClustering() {

		if (!mRunning) {

			try {

				mNumCoords = getEnteredValue(mCoordTF, 1, 10000000);
				mNumDimensions = getEnteredValue(mDimensionsTF, 2, 200);
				mNumClusters = getEnteredValue(mClustersTF, 1, (mNumCoords - 1));
				mSeed = getEnteredValue(mRandomTF, Long.MIN_VALUE,
						Long.MAX_VALUE);
				mNumProcessors = getEnteredValue(mNumProcessorsTF, 1, Runtime
						.getRuntime().availableProcessors());
				mStandardDev = getEnteredValue(mStandardDevTF, 0.0, 1.0);

				// Clear the plots of data.
				Iterator<ScatterPlot2D> it = mGalaxies.iterator();
				while (it.hasNext()) {
					it.next().setDataset(null);
				}

				mBeginProgress = 0.0;

				mCurrentGalaxy = mGeneratedClusterData;

				mResultsTA.setText("");

				NormalTupleGenerator genTask = new NormalTupleGenerator(mNumDimensions,
						mNumCoords, mNumClusters, 4.0, mStandardDev,
						mStandardDev, new Random(mSeed));

				genTask.setProgressEndpoints(0.0, GENERATION_PROGRESS_INC);
				genTask.addTaskListener(this);

				mCurrentTask = genTask;
				mRunning = true;
				mRunButton.setText(CANCEL_TEXT);

				Thread t = new Thread(genTask);
				t.setDaemon(true);
				t.start();

			} catch (Exception e) {

				JOptionPane.showMessageDialog(this,
						"There was a problem with one of the values you entered:\n\n"
								+ e.getMessage() + "\n\n"
								+ "Please correct the problem and try again.",
						"Error", JOptionPane.ERROR_MESSAGE);

				mRunning = false;
			}

		} else {

			System.err.println("Canceling current task....");

			if (mCurrentTask != null) {
				mCurrentTask.cancel(true);
			}
		}
	}

	/**
	 * Displays an error dialog stating that insufficient memory is available.
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
         * @param tf the textfield to validate.
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
         * @param tf the textfield to validate.
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
         * @param tf the textfield to validate.
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

	private Clusterer setupNextClusterTask() {

		Clusterer clusterTask = null;

		if (mCurrentGalaxy == mGeneratedClusterData) {

			KMeansParams params = new KMeansParams.Builder()
					.clusterCount(mNumClusters)
					.maxIterations(Integer.MAX_VALUE)
					.movesGoal(0)
					.workerThreadCount(mNumProcessors)
					.distanceMetric(new EuclideanDistanceMetric())
					.clusterSeeder(
							new KMeansPlusPlusSeeder(mSeed, new Random(),
									new EuclideanDistanceMetric())).build();

			clusterTask = new KMeansClusterer(mGeneratedClusteredTuples.getTuples(), params);

			mCurrentGalaxy = mKMeansClusterData;

		} else if (mCurrentGalaxy == mKMeansClusterData) {

			XMeansParams.Builder builder = new XMeansParams.Builder();
			
			XMeansParams params = builder.workerThreadCount(mNumProcessors)
					.distanceMetric(new EuclideanDistanceMetric())
					.clusterSeeder(
							new KMeansPlusPlusSeeder(mSeed, new Random(),
									new EuclideanDistanceMetric())).build();

			clusterTask = new XMeansClusterer(mGeneratedClusteredTuples.getTuples(), params);

			mCurrentGalaxy = mXMeansClusterData;

		} else if (mCurrentGalaxy == mXMeansClusterData) {

			GMeansParams params = (GMeansParams) new GMeansParams.Builder()
					.workerThreadCount(mNumProcessors)
					.distanceMetric(new EuclideanDistanceMetric())
					.clusterSeeder(
							new KMeansPlusPlusSeeder(mSeed, new Random(),
									new EuclideanDistanceMetric())).build();

			clusterTask = new GMeansClusterer(mGeneratedClusteredTuples.getTuples(), params);

			mCurrentGalaxy = mGMeansClusterData;

		} else if (mCurrentGalaxy == mGMeansClusterData) {

			HierarchicalParams params = new HierarchicalParams.Builder()
					.clusterCount(mNumClusters)
					.workerThreadCount(mNumProcessors)
					.distanceMetric(new EuclideanDistanceMetric())
					.linkage(HierarchicalParams.Linkage.COMPLETE).build();

			clusterTask = new StandardHierarchicalClusterer(mGeneratedClusteredTuples.getTuples(),
					params);

			mCurrentGalaxy = mHierarchicalClusterData;

		} else if (mCurrentGalaxy == mHierarchicalClusterData) {

			HierarchicalParams params = new HierarchicalParams.Builder()
					.clusterCount(mNumClusters)
					.workerThreadCount(mNumProcessors)
					.distanceMetric(new EuclideanDistanceMetric())
					.linkage(HierarchicalParams.Linkage.COMPLETE).build();

			clusterTask = new ReverseNNHierarchicalClusterer(mGeneratedClusteredTuples.getTuples(),
					params);

			mCurrentGalaxy = mWardsClusterData;

		} else if (mCurrentGalaxy == mWardsClusterData) {

			FuzzyCMeansParams params = new FuzzyCMeansParams.Builder()
					.clusterCount(mNumClusters)
					.workerThreadCount(mNumProcessors)
					.distanceMetric(new EuclideanDistanceMetric())
					.clusterSeeder(
							new KMeansPlusPlusSeeder(mSeed, new Random(),
									new EuclideanDistanceMetric())).build();

			clusterTask = new FuzzyCMeansClusterer(mGeneratedClusteredTuples.getTuples(), params);

			mCurrentGalaxy = mFuzzyCMeansClusterData;
		}

		return clusterTask;
	}

	private Projector setupNextProjectionTask(java.util.List<Cluster> clusters) {
		return new Projector(copyTuples(mGeneratedClusteredTuples.getTuples()), clusters,
				new ProjectionParams());
	}

	private static TupleList copyTuples(TupleList tuples) {
		final int tupleLength = tuples.getTupleLength();
		final int tupleCount = tuples.getTupleCount();
		TupleList result = new ArrayTupleList(tupleLength, tupleCount);
		double[] buffer = new double[tupleLength];
		for (int i = 0; i < tupleCount; i++) {
			tuples.getTuple(i, buffer);
			result.setTuple(i, buffer);
		}
		return result;
	}

        /**
         * {@inheritDoc }
         */
        @Override
	public void taskBegun(TaskEvent e) {
		mStartTime = System.currentTimeMillis();
		displayText("\n" + e.getMessage() + "\n");
	}

        /**
         * {@inheritDoc }
         */
        @Override
	public void taskEnded(TaskEvent e) {

		long totalMS = System.currentTimeMillis() - mStartTime;
		Task<?> nextTask = null;
		displayText(e.getMessage() + " (" + totalMS + " ms)\n");
		Task<?> task = e.getTask();
		TaskOutcome outcome = task.getTaskOutcome();

		if (task instanceof NormalTupleGenerator) {

			if (outcome == TaskOutcome.SUCCESS) {

				NormalTupleGenerator genTask = (NormalTupleGenerator) task;

				try {
					mGeneratedClusteredTuples = genTask.get();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (ExecutionException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				mBeginProgress += GENERATION_PROGRESS_INC;

				Projector projectionTask = setupNextProjectionTask(mGeneratedClusteredTuples.getClusters());
				projectionTask.setProgressEndpoints(mBeginProgress,
						mBeginProgress + PROJECTION_PROGRESS_INC);

				nextTask = projectionTask;
			}

		} else if (task instanceof Projector) {

			if (outcome == TaskOutcome.SUCCESS) {

				Projector projTask = (Projector) task;
				java.util.List<Cluster> clusters = projTask.getClusters();
				Projection pdata = projTask.getPointProjection();

				if (mCurrentGalaxy != null) {
					ExampleData dataset = new ExampleData(mGeneratedClusteredTuples.getTuples(),
							clusters, pdata, projTask.getClusterProjection());
					dataset.getTupleSelectionModel().addSelectionListener(this);
					if (mCurrentGalaxy.getDataset() != null) {
						mCurrentGalaxy.getDataset().getTupleSelectionModel()
								.removeSelectionListener(this);
					}
					mCurrentGalaxy.setDataset(dataset);
				}
			}

			mBeginProgress += PROJECTION_PROGRESS_INC;

			nextTask = setupNextClusterTask();
			if (nextTask != null) {
				nextTask.setProgressEndpoints(mBeginProgress, mBeginProgress
						+ CLUSTERING_PROGRESS_INC);
			}

		} else if (task instanceof Clusterer) {

			if (outcome == TaskOutcome.SUCCESS) {

				mBeginProgress += CLUSTERING_PROGRESS_INC;
				Clusterer clusterTask = (Clusterer) task;
				try {
					nextTask = setupNextProjectionTask(clusterTask.get());
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
			mCurrentTask = nextTask;

			Thread t = new Thread(nextTask);
			t.setDaemon(true);
			t.start();

		} else {
			mRunning = false;
			mCurrentTask = null;
			mRunButton.setText(RUN_TEXT);
		}

		task.removeTaskListener(this);
	}

        /**
         * {@inheritDoc }
         */
        @Override
	public void taskMessage(TaskEvent e) {
		displayText(e.getMessage());
	}

        /**
         * {@inheritDoc }
         */
        @Override
	public void taskProgress(TaskEvent e) {
		setProgress(e.getProgress());
	}

        /**
         * {@inheritDoc }
         */
        @Override
	public void taskPaused(TaskEvent e) {
		displayText(e.getMessage());
	}

        /**
         * {@inheritDoc }
         */
        @Override
	public void taskResumed(TaskEvent e) {
		displayText(e.getMessage());
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

	public static void main(String[] args) {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {

				@Override
				public void run() {
					try {
						UIManager.setLookAndFeel(UIManager
								.getSystemLookAndFeelClassName());
					} catch (Exception exception) {
						exception.printStackTrace();
					}

					// Center the window
					Dimension screenSize = Toolkit.getDefaultToolkit()
							.getScreenSize();

					JFrame frame = new JFrame("Clodhopper Clustering Demonstration");
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

					frame.getContentPane().add(new GeneratedDataPanel());
					frame.setSize(new Dimension(1024, 900));
					frame.validate();

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
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

}
