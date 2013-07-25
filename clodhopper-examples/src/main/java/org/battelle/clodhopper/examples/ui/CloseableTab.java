package org.battelle.clodhopper.examples.ui;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;

import java.awt.*;
import java.awt.event.*;

public class CloseableTab extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JTabbedPane tabbedPane;
	
	public CloseableTab(final JTabbedPane tabbedPane) {
		super(new FlowLayout(FlowLayout.LEFT, 5, 0));
		if (tabbedPane == null) throw new NullPointerException();
		this.tabbedPane = tabbedPane;
		setOpaque(false);
		
		JLabel titleLabel = new JLabel() {
			@Override
			public String getText() {
				int i = tabbedPane.indexOfTabComponent(CloseableTab.this);
				if (i >= 0) {
					return tabbedPane.getTitleAt(i);
				}
				return "";
			}
		};
		
		add(titleLabel);
		
		CloseButton closeButton = new CloseButton();
		closeButton.addActionListener(this);
		
		add(closeButton);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		int i = tabbedPane.indexOfTabComponent(this);
		if (i >= 0) {
			tabbedPane.remove(i);
		}
	}
	
	private class CloseButton extends JButton {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public CloseButton() {
			final int width = 16;
			setPreferredSize(new Dimension(width, width));
			setToolTipText("Close tab");
			setUI(new BasicButtonUI());
			setContentAreaFilled(false);
			setFocusable(false);
			setRolloverEnabled(true);
			setBorderPainted(false);
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent e) {
					Component c = e.getComponent();
					if (c instanceof CloseButton) {
						((CloseButton) c).setBorderPainted(true);
					}
				}
				@Override
				public void mouseExited(MouseEvent e) {
					Component c = e.getComponent();
					if (c instanceof CloseButton) {
						((CloseButton) c).setBorderPainted(false);
					}
				}
			});
		}
		
		public void updateUI() {}
		
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g.create();
			ButtonModel model = getModel();
			if (model.isPressed()) {
				g2.translate(1, 1);
			}
			g2.setStroke(new BasicStroke(2));
			g2.setColor(model.isRollover() ? Color.red : Color.black);
			int shift = 4;
			int width = getWidth();
			int height = getHeight();
			g2.drawLine(shift, shift, width - shift, height - shift);
			g2.drawLine(width - shift, shift, shift, height - shift);
			g2.dispose();
		}
	}
}
