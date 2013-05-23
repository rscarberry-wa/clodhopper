package org.battelle.clodhopper.examples.hierarchical;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import org.battelle.clodhopper.Clusterer;
import org.battelle.clodhopper.examples.ui.ParamsPanelException;
import org.battelle.clodhopper.examples.ui.UIUtils;
import org.battelle.clodhopper.hierarchical.HierarchicalParams;
import org.battelle.clodhopper.hierarchical.StandardHierarchicalClusterer;
import org.battelle.clodhopper.tuple.TupleList;

public class StandardHierarchicalParamsPanel extends HierarchicalParamsPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public StandardHierarchicalParamsPanel(HierarchicalParams params) {
		super(params);
	}
	
	public StandardHierarchicalParamsPanel() {
		this(new HierarchicalParams());
	}
	
	
	@Override
	public Clusterer getNewClusterer(TupleList tuples)
			throws ParamsPanelException {
		HierarchicalParams params = getValidatedParams(tuples.getTupleCount());
		return new StandardHierarchicalClusterer(tuples, params);
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
                
                final HierarchicalParamsPanel hierarchicalPanel = new StandardHierarchicalParamsPanel();
                final int tupleCount = 10000;
                hierarchicalPanel.setClusterCount((int) Math.sqrt(tupleCount));
                
                frame.getContentPane().add(hierarchicalPanel, BorderLayout.CENTER);
                
                JButton button = new JButton("Get Parameters");
                button.addActionListener(new ActionListener() {
					
                	@Override
					public void actionPerformed(ActionEvent e) {
					
                		HierarchicalParams params;
						try {
							params = hierarchicalPanel.getValidatedParams(10000);
							if (params != null) {
								System.out.printf("clusterCount = %d\n", params.getClusterCount());
								System.out.printf("thread count = %d\n", params.getWorkerThreadCount());
								System.out.printf("distance metric = %s\n", params.getDistanceMetric().getClass().getSimpleName());
							}
						} catch (ParamsPanelException e1) {
							UIUtils.displayErrorDialog(hierarchicalPanel, e1.getMessage(), e1.getErrorList());
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
