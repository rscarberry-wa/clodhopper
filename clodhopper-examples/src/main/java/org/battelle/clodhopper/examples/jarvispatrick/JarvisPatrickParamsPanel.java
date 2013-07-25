package org.battelle.clodhopper.examples.jarvispatrick;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.battelle.clodhopper.Clusterer;
import org.battelle.clodhopper.distance.DistanceMetric;
import org.battelle.clodhopper.examples.ui.NumberDocument;
import org.battelle.clodhopper.examples.ui.ParamsPanel;
import org.battelle.clodhopper.examples.ui.ParamsPanelException;
import org.battelle.clodhopper.examples.ui.UIUtils;
import org.battelle.clodhopper.jarvispatrick.JarvisPatrickClusterer;
import org.battelle.clodhopper.jarvispatrick.JarvisPatrickParams;
import org.battelle.clodhopper.tuple.TupleList;

public class JarvisPatrickParamsPanel extends JPanel
  implements ParamsPanel {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private JTextField nearestNeighborsToExamineTF = new JTextField();
  private JTextField nearestNeighborOverlapTF = new JTextField();
  private JCheckBox mutualNearestNeighborsCB = new JCheckBox("Mutual Nearest Neighbors");
  private JTextField threadCountTF = new JTextField();
  private JComboBox distanceMetricDD = new JComboBox();

  public JarvisPatrickParamsPanel(JarvisPatrickParams params) {
    
    super(new GridBagLayout());
    
    if (params == null) {
      params = new JarvisPatrickParams();
    }
    
    nearestNeighborsToExamineTF.setColumns(8);
    nearestNeighborsToExamineTF.setDocument(new NumberDocument(false, false));
    nearestNeighborsToExamineTF.setText(String.valueOf(params.getNearestNeighborsToExamine()));
    
    nearestNeighborOverlapTF.setColumns(8);
    nearestNeighborOverlapTF.setDocument(new NumberDocument(false, false));
    nearestNeighborOverlapTF.setText(String.valueOf(params.getNearestNeighborOverlap()));
    
    mutualNearestNeighborsCB.setSelected(params.getMutualNearestNeighbors());
    mutualNearestNeighborsCB.setToolTipText("Require that tuples be mutual nearest neighbors in order to be clustered together.");

    threadCountTF.setColumns(8);
    threadCountTF.setDocument(new NumberDocument(false, false));
    threadCountTF.setText(String.valueOf(params.getWorkerThreadCount()));
    
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
    
    JLabel nearestNeighborsToExamineLabel = new JLabel("Nearest Neighbors to Examine:");
    nearestNeighborsToExamineLabel.setToolTipText("The number of nearest neighbors to examine for every tuple");
    nearestNeighborsToExamineTF.setToolTipText(nearestNeighborsToExamineLabel.getToolTipText());
    
    JLabel nearestNeighborOverlapLabel = new JLabel("Nearest Neighbor Overlap:");
    nearestNeighborOverlapLabel.setToolTipText("The nearest neighbor overlap that causes tuples to be placed in the same cluster");
    nearestNeighborOverlapTF.setToolTipText(nearestNeighborOverlapLabel.getToolTipText());

    JLabel threadCountLbl = new JLabel("Thread Count:");
    threadCountLbl.setToolTipText("Number of threads to use for parallel computations.");
    threadCountTF.setToolTipText(threadCountLbl.getToolTipText());

    JLabel distanceMetricLbl = new JLabel("Distance Metric:");
    distanceMetricLbl.setToolTipText("The distance metric to use.");
    distanceMetricDD.setToolTipText(distanceMetricLbl.getToolTipText());
    
    this.add(nearestNeighborsToExamineLabel, 
        new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, 
                GridBagConstraints.EAST, GridBagConstraints.NONE, 
                new Insets(10, 10, 5, 5), 0, 0));
    this.add(nearestNeighborsToExamineTF,
        new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, 
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 
                new Insets(10, 0, 5, 10), 0, 0));
    this.add(nearestNeighborOverlapLabel, 
        new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, 
                GridBagConstraints.EAST, GridBagConstraints.NONE, 
                new Insets(5, 10, 5, 5), 0, 0));
    this.add(nearestNeighborOverlapTF,
        new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, 
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 
                new Insets(5, 0, 5, 10), 0, 0));
    
    JPanel mutualNNPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    mutualNNPanel.add(mutualNearestNeighborsCB);
    
    this.add(mutualNNPanel, new GridBagConstraints(0, 2, 2, 1, 1.0, 0.0, 
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 
                new Insets(5, 0, 5, 10), 0, 0));
    this.add(threadCountLbl, 
        new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, 
                GridBagConstraints.EAST, GridBagConstraints.NONE, 
                new Insets(5, 10, 5, 5), 0, 0));
    this.add(threadCountTF,
        new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0, 
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 
                new Insets(5, 0, 5, 10), 0, 0));
    this.add(distanceMetricLbl, 
        new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, 
                GridBagConstraints.EAST, GridBagConstraints.NONE, 
                new Insets(5, 10, 5, 5), 0, 0));
    this.add(distanceMetricDD, new GridBagConstraints(1, 4, 1, 1, 1.0, 0.0, 
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 
                new Insets(5, 0, 5, 10), 0, 0));
    
    this.add(new JPanel(), new GridBagConstraints(0, 5, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER,
        GridBagConstraints.BOTH, new Insets(0,0,0,0), 0, 0));

  }
  
  public JarvisPatrickParamsPanel() {
    this(null);
  }
  
  @Override
  public Clusterer getNewClusterer(TupleList tuples)
      throws ParamsPanelException {
    return new JarvisPatrickClusterer(tuples, getValidatedParams(tuples.getTupleCount()));
  }

  @Override
  public void setTupleCount(int tupleCount) {
    // noop
  }
  
  public JarvisPatrickParams getValidatedParams(int tupleCount) throws ParamsPanelException {
    
    java.util.List<String> errorMessages = new ArrayList<String> ();
    
    int nearestNeighborsToExamine = UIUtils.extractInt(nearestNeighborsToExamineTF, "nearest neighbors to examine", 2, tupleCount - 1, errorMessages);
    
    int nextLimit = errorMessages.size() == 0 ? nearestNeighborsToExamine : tupleCount - 1;
    
    int nearestNeighborOverlap = UIUtils.extractInt(nearestNeighborOverlapTF, "nearest neighbor overlap", 1, nextLimit, errorMessages);
   
    int threadCount = UIUtils.extractInt(threadCountTF, "thread count", 1, Integer.MAX_VALUE, errorMessages);

    String distanceMetricName = distanceMetricDD.getSelectedItem().toString();
    
    DistanceMetric distanceMetric = UIUtils.distanceMetric(distanceMetricName);
    
    JarvisPatrickParams params = null;
    
    if (errorMessages.size() > 0) {
      
      throw new ParamsPanelException("Jarvis-Patrick Parameters Error", errorMessages);
      
    } else {
      
      JarvisPatrickParams.Builder builder = new JarvisPatrickParams.Builder()
        .nearestNeighborsToExamine(nearestNeighborsToExamine)
        .nearestNeighborOverlap(nearestNeighborOverlap)
        .mutualNearestNeighbors(mutualNearestNeighborsCB.isSelected())
        .workerThreadCount(threadCount)
        .distanceMetric(distanceMetric);
      
      params = builder.build();
    }
    
    return params;
  }


}
