package org.battelle.clodhopper.examples.viz;

import gnu.trove.list.array.TIntArrayList;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.TimerTask;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.battelle.clodhopper.Cluster;
import org.battelle.clodhopper.examples.data.ExampleData;
import org.battelle.clodhopper.examples.project.Projection;
import org.battelle.clodhopper.examples.selection.SelectionEvent;
import org.battelle.clodhopper.examples.selection.SelectionListener;
import org.battelle.clodhopper.examples.selection.SelectionModel;
import org.battelle.clodhopper.util.ArrayIntIterator;
import org.battelle.clodhopper.util.IntIterator;

public class ScatterPlot2D extends JComponent 
	implements SelectionListener {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// Bit flags to record state of keys when the mouse 
    // is pressed in mKeyBits.
    private static final int CTRL_DOWN_BIT = 0x01;
    private static final int SHIFT_DOWN_BIT = 0x02;
    private static final int ALT_DOWN_BIT = 0x04;
    private static final int META_DOWN_BIT = 0x08;
    
    // Mouse button identifiers
    private static final int LEFT_MOUSE_BUTTON = InputEvent.BUTTON1_MASK;
    private static final int MIDDLE_MOUSE_BUTTON = InputEvent.BUTTON2_MASK;
    private static final int RIGHT_MOUSE_BUTTON = InputEvent.BUTTON3_MASK;
    
    // Action constants
    private static final int INACTIVE = 0;
    // ZOOM
    private static final int ZOOM_IN = 1;
    private static final int ZOOM_OUT = 2;
    private static final int ZOOM_REGION = 3;
    private static final int ZOOM_RESET = 4;
    // a drag in ZOOM mode is only legal if no keys are down
    // otherwise we cannot detect when the user intended to
    // click but actually dragged (which will override the
    // click/mouse down response and result is unexpected
    // actions)
    private static final int ZOOM_ILLEGAL = 5;
    // PAN
    private static final int PANNING = 6;
    private static final int PAN_RESET = 7;
    // SELECT
    private static final int DOC_SELECT = 8;

    private static final int DOC_ADD = 9;
    private static final int DOC_REMOVE = 10;
    private static final int REGION_SELECT = 11;
    private static final int REGION_ADD = 12;
    private static final int REGION_REMOVE = 13;
    private static final int CLEAR_SELECTION = 14;
    private static final int CLEAR_PROBE = 15;

    public static enum Tool {
        SELECT, PAN, ZOOM, NONE
    };
    
    private static final int OFF_THE_SCREEN = -999;
    private static final int SCREEN_PIXEL_PAN_LIMIT = 5;
    private static final double SCREEN_PERCENT_PAN_LIMIT = 0.05;
    
    private static final long PAN_TIMER_DELAY = 50L;
    private static final long PAN_TIMER_REPEAT = 50L;

    private ExampleData mDataset;
    
    // Contain the x and y screen coordinates, or OFF_THE_SCREEN
    // if the coordinates are not visible.
    private IntegerPointList mCoordPointList;
    private IntegerPointList mClusterPointList;
    private PointSelector mPointSelector;
    
    // Indicates whether each coord is visible and colored.
    private BitSet mVisibleFlags;
    private BitSet mColoredFlags;
    
    private Dimension mCanvasDimension;
    private Point2D mLowerLeftDataViewport = new Point2D.Double();
    private Point2D mUpperRightDataViewport = new Point2D.Double();
    private java.awt.Point mMaxLowerLeftDrawingPoint;
    private java.awt.Point mMaxUpperRightDrawingPoint;

    private Tool mActiveTool = Tool.SELECT;
    
    // Selection Stuff
    private java.awt.Point mAnchorPoint = new java.awt.Point(0, 0);
    private boolean mAnchorSet;
    // The top of the selection, the bottom, and the current drag point.
    private java.awt.Point mSelectBoxTopPoint = new java.awt.Point( -1, -1);
    private java.awt.Point mSelectBoxBottomPoint = new java.awt.Point( -1, -1);
    private java.awt.Point mCurrentDragPoint = new java.awt.Point( -1, -1);

    // Key modifier flags
    private int mKeyBits;
    private int mMouseButton = LEFT_MOUSE_BUTTON;
    
    // The current action
    private int mCurrentAction = INACTIVE;
    
    // are there any points that lie outside the galaxy boundary?
    // using the SCREEN_PIXEL_PAN_LIMIT
    private boolean mAnyOutsidePoints;
    private boolean mShowRegionLine;
    
    // True, when in select mode and dragging off the edge
    // when the image is zoomed out.
    private boolean mAutoPanSelect;
    // to support autoPanning
    private java.util.Timer mPanSelectTimer = null;
    private int mPanSelectMarkX = 0;
    private int mPanSelectMarkY = 0;
    private int mPanSelectOutX = 0;
    private int mPanSelectOutY = 0;
    private int mPanSelectLastDragX = 0;
    private int mPanSelectLastDragY = 0;
    private int mPanSelectDragX = 0;
    private int mPanSelectDragY = 0;
    
    // Index of the currently-selected point (-1 if none).
    private int mSelectedPoint = -1;
    private int mHighlightedCluster = -1;
    
    // True if the clusters are visible.
    private boolean mClustersVisible;
    
    private boolean[] mClusterViewed;
    private boolean[] mClusterOpened;
    
    private boolean mSelectActiveToolOnMousePress;
    private boolean mContrastMode;
    
    private Color mContrastForeground;
    private Color mContrastBackground;
    private Color mSelectionColor;
    private Color mPendingSelectionColor;
    private Color mHighlightedClusterColor;
    private Color mSelectionBoxColor;
    private Color mClusterColor;
    
    private int mPointWidth = 3;
    private int mHalfPointWidth = mPointWidth/2;
    
    // Zoom orienting points
    private int mZoomX;
    private int mZoomY;
    // Increment factor per zoom operation
    private double mZoomFactor = 1.2;
    
    private Cursor mSelectCursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
    private Cursor mZoomCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
    private Cursor mPanCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    
    public ScatterPlot2D(ExampleData dataset) {
        
        setBackground(Color.lightGray);
        setContrastBackground(Color.lightGray);
        setForeground(Color.blue.darker());
        setContrastForeground(Color.darkGray);
        setSelectionColor(Color.green);
        setPendingSelectionColor(Color.magenta);
        setHighlightedClusterColor(Color.cyan);
        setSelectionBoxColor(Color.red);
        setClusterColor(Color.red.darker());
        
        setActiveTool(Tool.SELECT);
        
        setDataset(dataset);
        
        if (dataset == null) {
            initializeDataset();
        }
        
        super.setDoubleBuffered(true);
        
        this.enableEvents(AWTEvent.MOUSE_EVENT_MASK);
        this.enableEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK);
        this.enableEvents(AWTEvent.MOUSE_WHEEL_EVENT_MASK);
        
    }
    
    public ScatterPlot2D() {
        this(null);
    }
    
    public void setDataset(ExampleData dataset) {
        if ((dataset != null && !dataset.equals(mDataset)) ||
                (dataset == null && mDataset != null)) {
            if (mDataset != null) {
                mDataset.getTupleSelectionModel().removeSelectionListener(this);
            }
            mDataset = dataset;
            if (mDataset != null) {
                mDataset.getTupleSelectionModel().addSelectionListener(this);
            }
            initializeDataset();
            repaint();
        }
    }
    
    public ExampleData getDataset() {
        return mDataset;
    }
    
    public void setActiveTool(Tool tool) {
    	if (tool == null) throw new NullPointerException();
    	if (mActiveTool != tool) {
    		Tool old = mActiveTool;
    		mActiveTool = tool;
    		super.firePropertyChange("activeTool", old, tool);
    	}
    	Cursor cursor = Cursor.getDefaultCursor();
    	if (tool == Tool.SELECT) {
    		cursor = mSelectCursor;
    	} else if (tool == Tool.ZOOM) {
    		cursor = mZoomCursor;
    	} else if (tool == Tool.PAN) {
    		cursor = mPanCursor;
    	}
    	setCursor(cursor);
    }
    
    public Tool getActiveTool() {
    	return mActiveTool;
    }
    
    public void setClustersVisible(boolean b) {
        if (b != mClustersVisible) {
            mClustersVisible = b;
            super.firePropertyChange("clustersVisible", !b, b);
            repaint();
        }
    }
    
    public boolean getClustersVisible() {
        return mClustersVisible;
    }
    
    public void setSelectActiveToolOnMousePress(boolean b) {
    	if (mSelectActiveToolOnMousePress != b) {
    		mSelectActiveToolOnMousePress = b;
    		super.firePropertyChange("selectActiveToolOnMousePress", !b, b);
    	}
    }
    
    public boolean getSelectActiveToolOnMousePress() {
    	return mSelectActiveToolOnMousePress;
    }
    
    public int getPointWidth() {
        return mPointWidth;
    }
    
    public void setPointWidth(int pw) {
        if (pw <= 0) {
            throw new IllegalArgumentException("pointWidth <= 0: " + pw);
        }
        if (mPointWidth != pw) {
            int oldPW = mPointWidth;
            mPointWidth = pw;
            mHalfPointWidth = pw/2;
            super.firePropertyChange("pointWidth", oldPW, pw);
            repaint();
        }
    }
    
    public void setContrastBackground(Color bg) {
        if (bg == null) throw new NullPointerException();
        Color oldBG = mContrastBackground;
        if (oldBG == null || !bg.equals(oldBG)) {
            mContrastBackground = bg;
            // For the first call from the constructor, oldBG will be null.
            // No need to fire an event or repaint.
            if (oldBG != null) {
                super.firePropertyChange("contrastBackground", oldBG, bg);
                repaint();
            }
        }
    }
    
    public Color getContrastBackground() {
        return mContrastBackground;
    }
    
    public void setContrastForeground(Color fg) {
        if (fg == null) throw new NullPointerException();
        Color oldFG = mContrastForeground;
        if (oldFG == null || !fg.equals(oldFG)) {
            mContrastForeground = fg;
            // For the first call from the constructor, oldFG will be null.
            // No need to fire an event or repaint.
            if (oldFG != null) {
                super.firePropertyChange("contrastForeground", oldFG, fg);
                repaint();
            }
        }
    }
    
    public Color getContrastForeground() {
        return mContrastForeground;
    }

    public void setSelectionColor(Color c) {
        if (c == null) throw new NullPointerException();
        Color oldC = mSelectionColor;
        if (oldC == null || !c.equals(oldC)) {
            mSelectionColor = c;
            // For the first call from the constructor, oldC will be null.
            // No need to fire an event or repaint.
            if (oldC != null) {
                super.firePropertyChange("selectionColor", oldC, c);
                repaint();
            }
        }
    }
    
    public Color getSelectionColor() {
        return mSelectionColor;
    }

    public void setPendingSelectionColor(Color c) {
        if (c == null) throw new NullPointerException();
        Color oldC = mPendingSelectionColor;
        if (oldC == null || !c.equals(oldC)) {
            mPendingSelectionColor = c;
            // For the first call from the constructor, oldC will be null.
            // No need to fire an event or repaint.
            if (oldC != null) {
                super.firePropertyChange("pendingSelectionColor", oldC, c);
                repaint();
            }
        }
    }
    
    public Color getPendingSelectionColor() {
        return mPendingSelectionColor;
    }

    public void setClusterColor(Color c) {
        if (c == null) throw new NullPointerException();
        Color oldC = mClusterColor;
        if (oldC == null || !c.equals(oldC)) {
            mClusterColor = c;
            // For the first call from the constructor, oldC will be null.
            // No need to fire an event or repaint.
            if (oldC != null) {
                super.firePropertyChange("clusterColor", oldC, c);
                repaint();
            }
        }
    }
    
    public Color getClusterColor() {
        return mClusterColor;
    }

    public void setHighlightedClusterColor(Color c) {
        if (c == null) throw new NullPointerException();
        Color oldC = mHighlightedClusterColor;
        if (oldC == null || !c.equals(oldC)) {
            mHighlightedClusterColor = c;
            // For the first call from the constructor, oldC will be null.
            // No need to fire an event or repaint.
            if (oldC != null) {
                super.firePropertyChange("highlightedClusterColor", oldC, c);
                repaint();
            }
        }
    }
    
    public Color getHighlightedClusterColor() {
        return mHighlightedClusterColor;
    }

    public void setSelectionBoxColor(Color c) {
        if (c == null) throw new NullPointerException();
        Color oldC = mSelectionBoxColor;
        if (oldC == null || !c.equals(oldC)) {
            mSelectionBoxColor = c;
            // For the first call from the constructor, oldC will be null.
            // No need to fire an event or repaint.
            if (oldC != null) {
                super.firePropertyChange("selectionBoxColor", oldC, c);
                repaint();
            }
        }
    }
    
    public Color getSelectionBoxColor() {
        return mSelectionBoxColor;
    }

    public void setContrastMode(boolean b) {
        if (mContrastMode != b) {
            mContrastMode = b;
            repaint();
            super.firePropertyChange("contrastMode", !b, b);
        }
    }
    
    public boolean isContrastMode() {
        return mContrastMode;
    }
    
    private void initializeDataset() {
        
        mCanvasDimension = getSize();
        int numPoints = 0, numClusters = 0;
        
        if (mDataset != null) {
            numPoints = mDataset.getTuples().getTupleCount();
            numClusters = mDataset.getClusters().size();
            mCoordPointList = new IntegerPointList(numPoints, 2, OFF_THE_SCREEN);
            mClusterPointList = new IntegerPointList(numClusters, 2, OFF_THE_SCREEN);
            mVisibleFlags = new BitSet(numPoints);
            mColoredFlags = new BitSet(numPoints);
            mPointSelector = null;
        } else {
            mCoordPointList = mClusterPointList = null;
            mVisibleFlags = mColoredFlags = null;
            mPointSelector = null;
        }

        mClusterViewed = new boolean[numClusters];
        mClusterOpened = new boolean[numClusters];
        Arrays.fill(mClusterViewed, true);
        Arrays.fill(mClusterOpened, true);
        
        resetDataViewport();
    }
    
    private double mLeftMarginFraction = 0.05;
    private double mRightMarginFraction = 0.05;
    private double mTopMarginFraction = 0.05;
    private double mBottomMarginFraction = 0.05;
    
    public void setLeftMarginFraction(double f) {
        checkFraction(f);
        if (mLeftMarginFraction != f) {
            double old = mLeftMarginFraction;
            mLeftMarginFraction = f;
            super.firePropertyChange("leftMarginFraction", old, f);
            resetDataViewport();
        }
    }
    
    public double getLeftMarginFraction() {
        return mLeftMarginFraction;
    }
    
    public void setRightMarginFraction(double f) {
        checkFraction(f);
        if (mRightMarginFraction != f) {
            double old = mRightMarginFraction;
            mRightMarginFraction = f;
            super.firePropertyChange("rightMarginFraction", old, f);
            resetDataViewport();
        }
    }
    
    public double getRightMarginFraction() {
        return mRightMarginFraction;
    }
    
    public void setTopMarginFraction(double f) {
        checkFraction(f);
        if (mTopMarginFraction != f) {
            double old = mTopMarginFraction;
            mTopMarginFraction = f;
            super.firePropertyChange("topMarginFraction", old, f);
            resetDataViewport();
        }
    }
    
    public double getTopMarginFraction() {
        return mTopMarginFraction;
    }
    
    public void setBottomMarginFraction(double f) {
        checkFraction(f);
        if (mBottomMarginFraction != f) {
            double old = mBottomMarginFraction;
            mBottomMarginFraction = f;
            super.firePropertyChange("bottomMarginFraction", old, f);
            resetDataViewport();
        }
    }
    
    public double getBottomMarginFraction() {
        return mBottomMarginFraction;
    }

    private void checkFraction(double f) {
        if (Double.isNaN(f) || f < 0.0 || f > 1.0) {
            throw new IllegalArgumentException("fraction not in [0 - 1]: " + f);
        }
    }
    
    public void resetDataViewport() {
        mLowerLeftDataViewport.setLocation(-mLeftMarginFraction, 1.0 + mBottomMarginFraction);
        mUpperRightDataViewport.setLocation(1.0 + mRightMarginFraction, -mTopMarginFraction);
        transformAllDataToWindowCoordinates();
        repaint();
    }
    
    private void transformAllDataToWindowCoordinates() {
        
        if (mDataset != null) {
        
            final double xll = mLowerLeftDataViewport.getX();
            final double yll = mLowerLeftDataViewport.getY();
            final double xur = mUpperRightDataViewport.getX();
            final double yur = mUpperRightDataViewport.getY();
            
            final double xScale = (this.getWidth() - 1)/(xur - xll);
            final double yScale = (this.getHeight() - 1)/(yll - yur);
        
            // This assumes that initialDataset() has been called.
            //
            int numPoints = mCoordPointList.getPointCount();
            Projection tupleProjection = mDataset.getTupleProjection();
            
            mAnyOutsidePoints = false;
            
            int maxllx = Integer.MAX_VALUE;
            int maxlly = Integer.MIN_VALUE;
            int maxurx = Integer.MIN_VALUE;
            int maxury = Integer.MAX_VALUE;
            
            final int rightPanLimit  = getWidth() - (SCREEN_PIXEL_PAN_LIMIT + 1);
            final int bottomPanLimit = getHeight() - (SCREEN_PIXEL_PAN_LIMIT + 1);
            
            for (int i=0; i<numPoints; i++) {
            
                double px = tupleProjection.getProjection(i, 0);
                double py = tupleProjection.getProjection(i, 1);
                
                final int x = (int) (0.5 + (px - xll) * xScale);
                final int y = (int) (0.5 + (py - yur) * yScale);

                if (x < SCREEN_PIXEL_PAN_LIMIT || x > rightPanLimit) {
                    mAnyOutsidePoints = true;
                }
                
                mCoordPointList.setPointValue(i, 0, x);

                if (x < maxllx) {
                    maxllx = x;
                }
                if (x > maxurx) {
                    maxurx = x;
                }
                
                if (y < SCREEN_PIXEL_PAN_LIMIT || y > bottomPanLimit) {
                    mAnyOutsidePoints = true;
                }
                
                mCoordPointList.setPointValue(i, 1, y);

                if (y > maxlly) {
                    maxlly = y;
                }
                if (y < maxury) {
                    maxury = y;
                }
            }
            
            mMaxLowerLeftDrawingPoint = new java.awt.Point(maxllx, maxlly);
            mMaxUpperRightDrawingPoint = new java.awt.Point(maxurx, maxury);
            
            int numClusters = mClusterPointList.getPointCount();
            Projection clusterProjection = mDataset.getClusterProjection();
            for (int i=0; i<numClusters; i++) {
                double px = clusterProjection.getProjection(i, 0);
                double py = clusterProjection.getProjection(i, 1);
                int cx = (int) (0.5 + (px - xll) * xScale);
                int cy = (int) (0.5 + (py - yur) * yScale);
                if (cx < SCREEN_PIXEL_PAN_LIMIT || cx > rightPanLimit) {
                    mClusterPointList.setPointValue(i, 0, OFF_THE_SCREEN);
                } else {
                    mClusterPointList.setPointValue(i, 0, cx);
                }
                if (cy < SCREEN_PIXEL_PAN_LIMIT || cy > bottomPanLimit) {
                    mClusterPointList.setPointValue(i, 1, OFF_THE_SCREEN);
                } else {
                    mClusterPointList.setPointValue(i, 1, cy);
                }
            }
            
            mPointSelector = new PointSelector(mCoordPointList, mClusterPointList, 
                    mDataset.getTupleSelectionModel());
        }
    }
    
    private void doOrdinaryPointDraw(Graphics2D g2) {
        Color oldColor = g2.getColor();
        try {
            int numCoords = mCoordPointList != null ? mCoordPointList.getPointCount() : 0;
            SelectionModel sm = mDataset != null ? mDataset.getTupleSelectionModel() : null;
            Color nc = mContrastMode ? mContrastForeground : super.getForeground();
            for (int i=0; i<numCoords; i++) {
                int x = mCoordPointList.getPointValue(i, 0);
                int y = mCoordPointList.getPointValue(i, 1);
                if (x != OFF_THE_SCREEN && y != OFF_THE_SCREEN) {
                    Color c = sm.isSelected(i) ? mSelectionColor : nc;
                    g2.setColor(c);
                    drawPoint(g2, x, y);
                }
            }
        } finally {
            g2.setColor(oldColor);
        }
    }
    
    private void doFastPointDraw(Graphics2D g2) {
        
        if (mDataset == null) return;
        
        Color oldColor = g2.getColor();
        
        try {
            
            Color normal = mContrastMode ? getContrastForeground() : getForeground();
            Color pending = getPendingSelectionColor();
            Color selected = getSelectionColor();
            
            int numCoords = mCoordPointList.getPointCount();
            SelectionModel sm = mDataset.getTupleSelectionModel();
            SelectionModel pendingSM = mDataset.getPendingSelectionModel();
            
            for (int i=0; i<numCoords; i++) {
                int x = mCoordPointList.getPointValue(i, 0);
                int y = mCoordPointList.getPointValue(i, 1);
                if (x != OFF_THE_SCREEN && y != OFF_THE_SCREEN) {
                    Color c = sm.isSelected(i) ? selected : (pendingSM.isSelected(i) ? pending : normal);
                    g2.setColor(c);
                    g2.fillRect(x-1, y-1, 2, 2);
                }
            }
            
        } finally {
            g2.setColor(oldColor);
        }
    }
    
    private void drawPoint(Graphics2D g2, int x, int y) {
        g2.fillRect(x - mHalfPointWidth, y - mHalfPointWidth, 
                mPointWidth, mPointWidth);
    }
    
    /**
     * Supposedly needs to be overridden for double-buffering to work.
     */
    public void update(Graphics g) {
        paintComponent(g);
    }
    
    public void paintComponent(Graphics g) {
        
        Color bg = mContrastMode ? mContrastBackground : super.getBackground();
        
        if (mDataset == null) {
            // clear the background
            g.setColor(bg);
            g.fillRect(0, 0, getWidth(), getHeight());
            return;
        }
        
        int numCoords = mCoordPointList.getPointCount();
        for (int i=0; i<numCoords; i++) {
            int c = mDataset.getClusterForTuple(i);
            if (mClusterOpened[c]) {
                mVisibleFlags.set(i);
                mColoredFlags.clear(i);
            } else {
                mVisibleFlags.clear(i);
            }
        }
        
        Dimension size = this.getSize();
        
        if (!size.equals(mCanvasDimension)) {
              transformAllDataToWindowCoordinates();
              mCanvasDimension = size;
        }

        final int height = mCanvasDimension.height;
        final int width = mCanvasDimension.width;
        
        BufferedImage offScreenImage = new BufferedImage(width, height,
                                             BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = (Graphics2D) offScreenImage.getGraphics();
        // clear the background
        g2.setColor(bg);
        g2.fillRect(0, 0, width, height);
        
        if (mShowRegionLine && !altDown(mKeyBits)) {
            doFastPointDraw(g2);
        } else {
            doOrdinaryPointDraw(g2);
        }
        
        if (mHighlightedCluster >= 0) {
            Cluster c = mDataset.getClusters().get(mHighlightedCluster);
            IntIterator members = c.getMembers();
            if (members.hasNext()) {
                g2.setColor(mHighlightedClusterColor);
                while(members.hasNext()) {
                    int member = members.getNext();
                    if (mVisibleFlags.get(member)) {
                        int x = mCoordPointList.getPointValue(member, 0);
                        int y = mCoordPointList.getPointValue(member, 1);
                        if (x != OFF_THE_SCREEN && y != OFF_THE_SCREEN && mVisibleFlags.get(member)) {
                            drawPoint(g2, x, y);
                            mColoredFlags.set(member);
                        }
                    }
                }
            }
        }
        
        if (mSelectBoxTopPoint.x != -1 && mSelectBoxBottomPoint.x != -1 && mShowRegionLine) {
            if (mActiveTool == Tool.SELECT || mActiveTool == Tool.ZOOM) {
                g2.setColor(mSelectionBoxColor);
                g2.drawRect(mSelectBoxTopPoint.x, mSelectBoxTopPoint.y,
                        mSelectBoxBottomPoint.x -
                        mSelectBoxTopPoint.x,
                        mSelectBoxBottomPoint.y -
                        mSelectBoxTopPoint.y);
            }
        }
        
        if (mClustersVisible) {
            int numClusters = mClusterPointList.getPointCount();
            for (int i=0; i<numClusters; i++) {
                Color c = (i == mHighlightedCluster ? mHighlightedClusterColor : mClusterColor);
                g2.setColor(c);
                int x = mClusterPointList.getPointValue(i, 0);
                int y = mClusterPointList.getPointValue(i, 1);
                g2.drawOval(x - 5, y - 5, 10, 10);
                g2.drawOval(x - 4, y - 4, 8, 8);
            }
        }
        
        g.drawImage(offScreenImage, 0, 0, this);
    }
    
    private void selectClusterPoints(int clusterIndex, boolean select) {
        if (mDataset != null) {
            Cluster cluster = mDataset.getClusters().get(clusterIndex);
            IntIterator it = cluster.getMembers();
            if (select) {
                mDataset.getTupleSelectionModel().select(this, it);
                mDataset.getClusterSelectionModel().select(this, clusterIndex);
            } else {
                mDataset.getTupleSelectionModel().unselect(this, it);
                mDataset.getClusterSelectionModel().unselect(this, clusterIndex);
            }
        }
    }
    
    private boolean isColored(int pointIndex) {
        return mColoredFlags != null && mColoredFlags.get(pointIndex);
    }
    
    private void selectThePoint(int currentAction, int keyBits) {   
        if (mSelectedPoint >= 0) {
            if (altDown(keyBits) && !isColored(mSelectedPoint)) {
                return;
            }
            if (currentAction == DOC_SELECT) {
                mDataset.getTupleSelectionModel().setSelected(this,
                        new ArrayIntIterator(new int[] { mSelectedPoint }));
            } else if (currentAction == DOC_ADD) {
                mDataset.getTupleSelectionModel().select(this, mSelectedPoint);
            } else if (currentAction == DOC_REMOVE) {
                mDataset.getTupleSelectionModel().unselect(this, mSelectedPoint);
            }
        }
    }
    
    private void selectPendingSelections(int currentAction, boolean altDown) {
        if (mDataset != null) {
            TIntArrayList removalList = new TIntArrayList();
            SelectionModel model = mDataset.getPendingSelectionModel();
            int numCoords = model.getIndexCount();
            for (int i=0; i<numCoords; i++) {
                if (!(mVisibleFlags.get(i) && (!altDown || mColoredFlags.get(i)))) {
                    removalList.add(i);
                }
            }
            if (removalList.size() > 0) {
                model.unselect(this, new ArrayIntIterator(removalList.toArray()));
            }
            if (currentAction == REGION_SELECT) {
                mDataset.setSelectionsToPending(this);
            } else if (currentAction == REGION_ADD) {
                mDataset.addPendingSelections(this);
            } else if (currentAction == REGION_REMOVE) {
                mDataset.removePendingSelections(this);
            }  
            model.clearSelected(this);
        }
    }
    
    private void zoomIn() {
        Point2D fixedPoint = transformWindowToDataCoordinates(mZoomX, mZoomY);
        zoomDataViewport(fixedPoint, 1.0/mZoomFactor);
        transformAllDataToWindowCoordinates();
        repaint();
    }
    
    private void zoomOut() {
        Point2D fixedPoint = transformWindowToDataCoordinates(mZoomX, mZoomY);
        zoomDataViewport(fixedPoint, mZoomFactor);
        transformAllDataToWindowCoordinates();
        repaint();
    }
    
    private Point2D transformWindowToDataCoordinates(int x, int y) {
        double xFraction = ((double) x) / (getWidth() - 1);
        double yFraction = 1.0 - ((double) y) / (getHeight() - 1);
        double xData = mLowerLeftDataViewport.getX() +
                       xFraction *
                       (mUpperRightDataViewport.getX() -
                        mLowerLeftDataViewport.getX());
        double yData = mLowerLeftDataViewport.getY() +
                       yFraction *
                       (mUpperRightDataViewport.getY() -
                        mLowerLeftDataViewport.getY());
        return new Point2D.Double(xData, yData);
    }
    
    private void zoomDataViewport(Point2D fixedPoint, double zoomRatio) {
        double oldLeftPart = fixedPoint.getX() - mLowerLeftDataViewport.getX();
        double oldRightPart = mUpperRightDataViewport.getX() - fixedPoint.getX();
        double oldTopPart = mUpperRightDataViewport.getY() - fixedPoint.getY();
        double oldBottomPart = fixedPoint.getY() - mLowerLeftDataViewport.getY();
        double newLeftPart = zoomRatio * oldLeftPart;
        double newRightPart = zoomRatio * oldRightPart;
        double newTopPart = zoomRatio * oldTopPart;
        double newBottomPart = zoomRatio * oldBottomPart;
        double newLeft = fixedPoint.getX() - newLeftPart;
        double newBottom = fixedPoint.getY() - newBottomPart;
        double newRight = fixedPoint.getX() + newRightPart;
        double newTop = fixedPoint.getY() + newTopPart;
        setViewportIfLegal(newLeft, newRight, newTop, newBottom);
      }

    private void setViewportIfLegal(double newLeft, double newRight,
            double newTop, double newBottom) {
        // set the viewport if not too small and if at least one point
        // is sufficiently within it.
        if ((newLeft < (newRight - 0.00001)) &&
                (newTop < (newBottom - 0.00001))) {
            
            boolean allowed = false;
            double leftLim = newLeft + 0.05 * (newRight - newLeft);
            double rightLim = newLeft + 0.95 * (newRight - newLeft);
            double topLim = newTop + 0.05 * (newBottom - newTop);
            double bottomLim = newTop + 0.95 * (newBottom - newTop);
            
            Projection projection = mDataset.getTupleProjection();
            int numPoints = projection.getProjectionCount();
            
            for (int i=0; i<numPoints; i++) {
                double x = projection.getProjection(i, 0);
                double y = projection.getProjection(i, 1);
                if (mVisibleFlags.get(i) &&
                        leftLim < x && x < rightLim &&
                        topLim < y && y < bottomLim) {
                    allowed = true;
                    break;
                }
            }
            
            if (allowed) {
                mLowerLeftDataViewport.setLocation(newLeft, newBottom);
                mUpperRightDataViewport.setLocation(newRight, newTop);
            } else {
                Toolkit.getDefaultToolkit().beep();
            }
        }
    }
    
    private void zoomToRegion() {
        int llx = mSelectBoxTopPoint.x;
        int lly = mSelectBoxBottomPoint.y;
        int urx = mSelectBoxBottomPoint.x;
        int ury = mSelectBoxTopPoint.y;
        Point2D fixedPointLL = transformWindowToDataCoordinates(llx, lly);
        Point2D fixedPointUR = transformWindowToDataCoordinates(urx, ury);
        double newLeft = fixedPointLL.getX();
        double newBottom = fixedPointLL.getY();
        double newRight = fixedPointUR.getX();
        double newTop = fixedPointUR.getY();
        setViewportIfLegal(newLeft, newRight, newTop, newBottom);
        transformAllDataToWindowCoordinates();
        repaint();
      }
    
    private void startAutoPanSelect() {
        if (mPanSelectTimer == null) {
            mAutoPanSelect = true;
            mPanSelectTimer = new java.util.Timer(true);
            mPanSelectTimer.schedule(new TimerTask() {
                public void run() {
                    panTimerUpdate();
                }
            } , PAN_TIMER_DELAY, PAN_TIMER_REPEAT);
        }
    }
    
    private void stopAutoPanSelect() {
        if (mPanSelectTimer != null) {
            mPanSelectTimer.cancel();
            mPanSelectTimer = null;
        }
        mAutoPanSelect = false;
        mPanSelectMarkX = 0;
        mPanSelectMarkY = 0;
        mPanSelectOutX = 0;
        mPanSelectOutY = 0;
        mPanSelectLastDragX = 0;
        mPanSelectLastDragY = 0;
        mPanSelectDragX = 0;
        mPanSelectDragY = 0;
    }
    
    private void panTimerUpdate() {
        SwingUtilities.invokeLater(new Runnable() {
           public void run() {
               if (!moveAutoPanSelect()) {
                   stopAutoPanSelect();
               }
           }
        });
    }
    
    private boolean moveAutoPanSelect() {
        
        if (!mAnchorSet) return false;
        
        int xDist = (int) (0.5 + Math.sqrt(Math.abs(mPanSelectOutX)));
        if (xDist < 1) xDist = 1;
        
        int yDist = (int) (0.5 + Math.sqrt(Math.abs(mPanSelectOutY)));
        if (yDist < 1) yDist = 1;
        
        int xOut = mPanSelectOutX < 0 ? -xDist : xDist;
        int yOut = mPanSelectOutY < 0 ? -yDist : yDist;
        
        if (xOut == 0 && yOut == 0) return false;
        
        int markX = mPanSelectMarkX;
        int markY = mPanSelectMarkY;
        
        if (mPanSelectOutX == 0 && mPanSelectDragX != 0) {
            markX = mPanSelectDragX;
        }
        if (mPanSelectOutY == 0 && mPanSelectDragY != 0) {
            markY = mPanSelectDragY;
        }
        
        java.awt.Point lowerLeftLimit = panLowerLeftLimit();
        
        if (xOut < 0 && (markX + xOut < lowerLeftLimit.x)) {
            xOut += Math.min(-xOut, lowerLeftLimit.x - (markX + xOut));
        }
        
        java.awt.Point upperRightLimit = panUpperRightLimit();
        
        if (yOut < 0 && (markY + yOut < upperRightLimit.y)) {
            yOut += Math.min(-yOut, upperRightLimit.y - (markY + yOut));
        }

        if (xOut == 0 && yOut == 0) return false;
        
        int wlimit = getWidth() - 1;
        int hlimit = getHeight() - 1;
        
        double xFraction = ((double) xOut)/wlimit;
        double yFraction = ((double) yOut)/hlimit;
        
        moveDataViewport(xFraction, yFraction);
        transformAllDataToWindowCoordinates();
        
        mAnchorPoint.x -= xOut;
        mAnchorPoint.y -= yOut;
        
        mPanSelectMarkX = markX;
        mPanSelectMarkY = markY;
        
        markX -= xOut;
        markY -= yOut;
        
        if (markX < 0) {
            markX = 0;
        } else if (markX > wlimit) {
            markX = wlimit;
        }
        
        if (markY < 0) {
            markY = 0;
        } else if (markY > hlimit) {
            markY = hlimit;
        }
        
        markRegion(markX, markY);
        updatePendingSelections();
        return true;
    }
    
    private java.awt.Point panLowerLeftLimit() {
        int percentWidth = Math.max((int) (0.5 + SCREEN_PERCENT_PAN_LIMIT*getWidth()),
                SCREEN_PIXEL_PAN_LIMIT);
        int percentHeight = Math.max((int) (0.5 + SCREEN_PERCENT_PAN_LIMIT*getHeight()),
                SCREEN_PIXEL_PAN_LIMIT);
        return new Point(mMaxLowerLeftDrawingPoint.x - percentWidth,
                mMaxLowerLeftDrawingPoint.y + percentHeight);
    }
    
    private java.awt.Point panUpperRightLimit() {
        int percentWidth = Math.max((int) (0.5 + SCREEN_PERCENT_PAN_LIMIT*getWidth()),
                SCREEN_PIXEL_PAN_LIMIT);
        int percentHeight = Math.max((int) (0.5 + SCREEN_PERCENT_PAN_LIMIT*getHeight()),
                SCREEN_PIXEL_PAN_LIMIT);
        return new Point(mMaxUpperRightDrawingPoint.x + percentWidth,
                mMaxUpperRightDrawingPoint.y - percentHeight);
    }
    
    private void moveDataViewport(double xFraction, double yFraction) {
        double dataDeltaX = xFraction * (mUpperRightDataViewport.getX() - mLowerLeftDataViewport.getX());
        double dataDeltaY = yFraction * (mLowerLeftDataViewport.getY() - mUpperRightDataViewport.getY());
        mUpperRightDataViewport.setLocation(mUpperRightDataViewport.getX() + dataDeltaX,
                mUpperRightDataViewport.getY() + dataDeltaY);
        mLowerLeftDataViewport.setLocation(mLowerLeftDataViewport.getX() + dataDeltaX,
                mLowerLeftDataViewport.getY() + dataDeltaY);
    }

    private void markRegion(int x, int y) {
        mSelectBoxBottomPoint.x = x;
        mSelectBoxBottomPoint.y = y;
        
        if (mAnchorSet) {
            mSelectBoxTopPoint.x = mAnchorPoint.x;
            mSelectBoxTopPoint.y = mAnchorPoint.y;
        } else {
            mSelectBoxTopPoint.x = x;
            mSelectBoxTopPoint.y = y;
        }
        
        if (mSelectBoxTopPoint.x > mSelectBoxBottomPoint.x) {
            mSelectBoxTopPoint.x ^= mSelectBoxBottomPoint.x;
            mSelectBoxBottomPoint.x ^= mSelectBoxTopPoint.x;
            mSelectBoxTopPoint.x ^= mSelectBoxBottomPoint.x;            
        }
        if (mSelectBoxTopPoint.y > mSelectBoxBottomPoint.y) {
            mSelectBoxTopPoint.y ^= mSelectBoxBottomPoint.y;
            mSelectBoxBottomPoint.y ^= mSelectBoxTopPoint.y;
            mSelectBoxTopPoint.y ^= mSelectBoxBottomPoint.y;
        }
        
        repaint();
    }
    
    private void updatePendingSelections() {
        handlePendingSelections();
    }
    
    private void handlePendingSelections() {
        
        if (mDataset != null) {
            
        	// mPointSelector.findCoordinatesInside(IntegerHyperRect) finds points
        	// faster to small selection rectangles, but not for large ones. So stick
        	// with the current method.
            Rectangle selRect = new Rectangle(
            		mSelectBoxTopPoint.x,
                    mSelectBoxTopPoint.y,
                    mSelectBoxBottomPoint.x - mSelectBoxTopPoint.x,
                    mSelectBoxBottomPoint.y - mSelectBoxTopPoint.y);

            int numCoords = mCoordPointList.getPointCount();
            
            TIntArrayList addList = new TIntArrayList();

            for (int i=0; i<numCoords; i++) {
                int x = mCoordPointList.getPointValue(i, 0);
                int y = mCoordPointList.getPointValue(i, 1);
                if (mActiveTool == Tool.SELECT && selRect.contains(x, y)) {
                    if (altDown(mKeyBits)) {
                        if (mColoredFlags.get(i)) {
                            addList.add(i);
                        }
                    } else {
                        addList.add(i);
                    }
                }
            }
                        
            if (mClustersVisible) {
            	
                List<Cluster> clusters = mDataset.getClusters();
                int numClusters = mClusterPointList.getPointCount();
            
                for (int i=0; i<numClusters; i++) {
                    Cluster c = clusters.get(i);
                    int x = mClusterPointList.getPointValue(i, 0);
                    int y = mClusterPointList.getPointValue(i, 1);
                    if (mActiveTool == Tool.SELECT && selRect.contains(x, y)) {
                        int sz = c.getMemberCount();
                        for (int j=0; j<sz; j++) {
                            int coord = c.getMember(j);
                            if (altDown(mKeyBits)) {
                                if (mColoredFlags.get(coord)) {
                                    addList.add(coord);
                                }
                            } else {
                                addList.add(coord);
                            }
                        }
                    }
                }
            }

            SelectionModel model = mDataset.getPendingSelectionModel();
            if (addList.size() > 0) {
                model.setSelected(this, new ArrayIntIterator(addList.toArray()));
            }
        }
    }
        
    private void executePan(int x, int y) {
        if (mAnchorSet) {
            double xFraction = ((double) (mAnchorPoint.x - x)) / getWidth();
            double yFraction = ((double) (mAnchorPoint.y - y)) / getHeight();
            moveDataViewport(xFraction, yFraction);
            transformAllDataToWindowCoordinates();
        }
        mAnchorPoint.x = x;
        mAnchorPoint.y = y;
        repaint();
        mCurrentAction = PANNING;
    }
    
    private static boolean altDown(int bits) {
    	return (bits & ALT_DOWN_BIT) > 0;
    }
    
    private static boolean ctrlDown(int bits) {
    	return (bits & CTRL_DOWN_BIT) > 0;
    }
    
    private static boolean shiftDown(int bits) {
    	return (bits & SHIFT_DOWN_BIT) > 0;
    }
    
    private static boolean metaDown(int bits) {
    	return (bits & META_DOWN_BIT) > 0;
    }
    
    private static int getKeyBits(MouseEvent e) {
    	int bits = 0;
    	if (e.isControlDown()) bits ^= CTRL_DOWN_BIT;
    	if (e.isShiftDown()) bits ^= SHIFT_DOWN_BIT;
    	if (e.isAltDown()) bits ^= ALT_DOWN_BIT;
    	if (e.isMetaDown()) bits ^= META_DOWN_BIT;
    	return bits;
    }

    public void selectionChanged(SelectionEvent evt) {
        repaint();
    }
    
    private int getMouseButton(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
        	return LEFT_MOUSE_BUTTON;
        } else if (SwingUtilities.isRightMouseButton(e)) {
        	return RIGHT_MOUSE_BUTTON;
        } else if (SwingUtilities.isMiddleMouseButton(e)) {
        	return MIDDLE_MOUSE_BUTTON;
        } else {
        	// Shouldn't happen.
        	return LEFT_MOUSE_BUTTON;
        }
    }
    
    public void processMouseEvent(MouseEvent e) {
        
        super.processMouseEvent(e);
        
        int id = e.getID();
        mKeyBits = getKeyBits(e);

        if (id == MouseEvent.MOUSE_PRESSED && mSelectActiveToolOnMousePress) {
            mMouseButton = getMouseButton(e);            
        	if (mMouseButton == RIGHT_MOUSE_BUTTON) {
        		setActiveTool(Tool.PAN);
        	} else {
        		setActiveTool(Tool.SELECT);
        	}
        }
        
        if (id == MouseEvent.MOUSE_CLICKED) {
        	
        	mouseClicked(e);
        	
        } else {
        
        	if (mSelectActiveToolOnMousePress) {
        		if (SwingUtilities.isLeftMouseButton(e) || 
        			(SwingUtilities.isRightMouseButton(e) && mActiveTool == Tool.PAN)) {
        			if (id == MouseEvent.MOUSE_PRESSED) {
        				mousePressed(e);
        			} else if (id == MouseEvent.MOUSE_RELEASED) {
        				mouseReleased(e);
        			}
        		}
        	} else {
        		if (SwingUtilities.isLeftMouseButton(e)) {
        			if (id == MouseEvent.MOUSE_PRESSED) {
        				mousePressed(e);
        			} else if (id == MouseEvent.MOUSE_RELEASED) {
        				mouseReleased(e);
        			}
        		}
        	}
        }
    }
    
    public void mousePressed(MouseEvent e) {
        mAnchorPoint.x = mSelectBoxTopPoint.x = mSelectBoxBottomPoint.x = e.getX();
        mAnchorPoint.y = mSelectBoxTopPoint.y = mSelectBoxBottomPoint.y = e.getY();
        mAnchorSet = true;
        if (mActiveTool == Tool.SELECT) {
            mousePressedForSelect(e);
        } else if (mActiveTool == Tool.ZOOM) {
            mousePressedForZoom(e);
        }
    }
    
    public void mouseReleased(MouseEvent e) {
        
        int currentAction = mCurrentAction;
        mCurrentAction = INACTIVE;

        int keyBits = mKeyBits;

        // Reset key-down states.
        mKeyBits = 0;
        
        if (mActiveTool == Tool.SELECT) {
            
            mShowRegionLine = false;
            
            if (currentAction == DOC_SELECT ||
                    currentAction == DOC_ADD || 
                    currentAction == DOC_REMOVE) {
                
                selectThePoint(currentAction, keyBits);

                boolean ctrlDown = ctrlDown(keyBits);
                boolean altDown = altDown(keyBits);
                boolean shiftDown = shiftDown(keyBits);
                
                if (mHighlightedCluster >= 0) {
                    if (altDown || (ctrlDown && !shiftDown)) {
                        selectClusterPoints(mHighlightedCluster, true);
                    } else if (ctrlDown && shiftDown) {
                        selectClusterPoints(mHighlightedCluster, false);
                    }
                    mHighlightedCluster = -1;
                }

            } else if (currentAction == REGION_SELECT ||
                    currentAction == REGION_ADD ||
                    currentAction == REGION_REMOVE) {
            
                stopAutoPanSelect();
                
                selectPendingSelections(currentAction, e.isAltDown());
                
                mSelectBoxTopPoint.x = mSelectBoxTopPoint.y = -1;
                mSelectBoxBottomPoint.x = mSelectBoxBottomPoint.y = -1;
                mCurrentDragPoint.x = mCurrentDragPoint.y = -1;
                
            } else if (currentAction == CLEAR_SELECTION) {
                
                if (mDataset != null) {
                    mDataset.getTupleSelectionModel().clearSelected(this);
                }
            }
            
            mSelectedPoint = -1;
            
        } else if (mActiveTool == Tool.ZOOM) {
            
            mouseReleasedForZoom(currentAction);
            
        } else if (mActiveTool == Tool.PAN) {
            
            if (currentAction == PAN_RESET) {
                resetDataViewport();
            }
        }
        
        mAnchorPoint.x = mAnchorPoint.y = 0;
        mAnchorSet = false;
        
        if (mSelectActiveToolOnMousePress) {
        	setActiveTool(Tool.SELECT);
        }
        
    }    
    
    private void mouseClicked(MouseEvent e) {
    	
    	if (getMouseButton(e) == LEFT_MOUSE_BUTTON && mActiveTool == Tool.SELECT) {

    		if (!altDown(mKeyBits)) {
    			
                if (mPointSelector != null) {
                	
                    int clickedCluster = mPointSelector.findCluster(new int[] { e.getX(), e.getY() }, 5);

                    if (clickedCluster >= 0) {
                    	boolean add = ctrlDown(mKeyBits);
                    	Cluster cluster = mDataset.getClusters().get(clickedCluster);
                    	IntIterator toSelect = cluster.getMembers();
                    	if (add) {
                    		mDataset.getTupleSelectionModel().select(this, toSelect);
                    		mDataset.getClusterSelectionModel().select(this, clickedCluster);
                    	} else {
                    		mDataset.getTupleSelectionModel().setSelected(this, toSelect);
                    		mDataset.getClusterSelectionModel().setSelected(this, new ArrayIntIterator(new int[] { clickedCluster }));
                    	}
                    }
                    
    			}
    		}
    	}
    }
    
    private void mouseReleasedForZoom(int currentAction) {
        if (currentAction == ZOOM_IN) {
            zoomIn();
        } else if (currentAction == ZOOM_OUT) {
            zoomOut();
        } else if (currentAction == ZOOM_REGION) {
            mShowRegionLine = false;
            if (mSelectBoxBottomPoint.x != mSelectBoxTopPoint.x &&
                    mSelectBoxBottomPoint.y != mSelectBoxTopPoint.y) {
                zoomToRegion();
            } else {
                Toolkit.getDefaultToolkit().beep();
                repaint();
            }
            mSelectBoxTopPoint.x = mSelectBoxTopPoint.y = -1;
            mSelectBoxBottomPoint.x = mSelectBoxBottomPoint.y = -1;
        } else if (currentAction == ZOOM_ILLEGAL) {
            mShowRegionLine = false;
            Toolkit.getDefaultToolkit().beep();
            repaint();
            mSelectBoxTopPoint.x = mSelectBoxTopPoint.y = -1;
            mSelectBoxBottomPoint.x = mSelectBoxBottomPoint.y = -1;
        } else if (currentAction == ZOOM_RESET) {
            resetDataViewport();
        }
        mZoomX = mZoomY = 0;
    }
    
    private void mousePressedForSelect(MouseEvent e) {
    	
        if (mPointSelector != null) {
            
            int[] coord = new int[] { e.getX(), e.getY() };
            
            int currentPoint = mPointSelector.findCoordinate(coord, 5);
            
            boolean shiftDown = shiftDown(mKeyBits);
            boolean ctrlDown = ctrlDown(mKeyBits);
            boolean altDown = altDown(mKeyBits);
            
            if (shiftDown && !(ctrlDown || altDown)) {
                mCurrentAction = CLEAR_SELECTION;
            } else {
                mSelectedPoint = currentPoint;                
                if (ctrlDown && shiftDown) {
                    mCurrentAction = DOC_REMOVE;
                } else if (ctrlDown && !altDown) {
                	mCurrentAction = DOC_ADD;
                } else {
                	mCurrentAction = DOC_SELECT;
                }
            }
        }
    }
    
    private void mousePressedForZoom(MouseEvent e) {

    	mZoomX = e.getX();
        mZoomY = e.getY();
        mSelectBoxTopPoint.y = mAnchorPoint.y;
        
        boolean shiftDown = shiftDown(mKeyBits);
        boolean ctrlDown = ctrlDown(mKeyBits);

        if (ctrlDown) {
        	mCurrentAction = shiftDown ? ZOOM_RESET : ZOOM_OUT;
        } else {
        	mCurrentAction = ZOOM_IN;
        }
    }
    
    public void processMouseMotionEvent(MouseEvent e) {
        
        mHighlightedCluster = -1;
        
        if (mActiveTool == Tool.SELECT && mClustersVisible) {
            // MLH 8/22/08 Keep this from highlighting when we're doing ALT action
            int dragAltMask = InputEvent.ALT_DOWN_MASK | InputEvent.BUTTON1_DOWN_MASK;
            if (e.getModifiersEx() != dragAltMask) {
                if (mPointSelector != null) {
                    mHighlightedCluster = mPointSelector.findCluster(new int[] { e.getX(), e.getY() }, 5);
                    repaint();
                }
            }
        }
        
        if (e.getID() == MouseEvent.MOUSE_DRAGGED) {
            // support for drag-save
            // if shift & btn1 are both down while a drag is taking place send event
            // to super class for DragNDrop processing, and forego Galaxy mouse stuff (SJZ 10/5/06)
            int dragSaveMask = InputEvent.SHIFT_DOWN_MASK | InputEvent.BUTTON1_DOWN_MASK;
            if (e.getModifiersEx() == dragSaveMask) {
              super.processMouseMotionEvent(e);
            }
            mouseDragged(e);
          } else {
            super.processMouseMotionEvent(e);
        }
    }
    
    private void mouseDragged(MouseEvent e) {
    	
    	int mouseButton = getMouseButton(e);
        
    	if (mActiveTool == Tool.NONE || (mouseButton != mMouseButton) ||
                mCurrentAction == CLEAR_SELECTION ||
                mCurrentAction == CLEAR_PROBE) {
            return;
        }
        
    	final int x = e.getX();
    	final int y = e.getY();

        if (mActiveTool == Tool.SELECT) {
            
        	if (mAnchorSet) {

                final int wlimit = getWidth() - 1;
                final int hlimit = getHeight() - 1;
                
                if (mAutoPanSelect) {
                    int xOut = 0;
                    int yOut = 0;
                    if (x < 0) {
                        xOut = x;
                    } else if (x > wlimit) {
                        xOut = x - wlimit;
                    }
                    if (y < 0) {
                        yOut = y;
                    } else if (y > hlimit) {
                        yOut = y - hlimit;
                    }
                    if (xOut == 0 && yOut == 0) {
                        stopAutoPanSelect();
                        markRegion(x, y);
                        updatePendingSelections();
                    } else {
                        if (xOut != 0 && yOut == 0) {
                            if (y != mPanSelectLastDragY) {
                                mPanSelectDragY = y;
                            }
                        } else if (yOut != 0 && xOut == 0) {
                            if (x != mPanSelectLastDragX) {
                                mPanSelectDragX = x;
                            }
                        }
                        mPanSelectOutX = xOut;
                        mPanSelectOutY = yOut;
                    }
                } else { // mAutoPanSelect == false
                    
                   int markX = x;
                   int markY = y;
                   boolean startPan = false;

                   if (mAnyOutsidePoints) {
                       int xOut = 0;
                       int yOut = 0;
                       if (x < 0) {
                           xOut = x;
                       } else if (x > wlimit) {
                           xOut = x - wlimit;
                       }
                       if (y < 0) {
                           yOut = y;
                       } else if (y > hlimit) {
                           yOut = y - hlimit;
                       }
                       
                       if (xOut != 0 || yOut != 0) {
                           java.awt.Point lowerLeftLimit = panLowerLeftLimit();
                           if (xOut < 0 && (markX + xOut < lowerLeftLimit.x)) {
                               xOut += Math.min(-xOut, lowerLeftLimit.x - (markX + xOut));
                           }
                           if (yOut > 0 && (markY + yOut > lowerLeftLimit.y)) {
                               yOut -= Math.min(yOut, (markY + yOut) - lowerLeftLimit.y);
                           }
                           
                           java.awt.Point upperRightLimit = panUpperRightLimit();
                           if (xOut > 0 && (markX + xOut > upperRightLimit.x)) {
                               xOut -= Math.min(xOut, (markX + xOut) - upperRightLimit.x);
                           }
                           if (yOut < 0 && (markY + yOut < upperRightLimit.y)) {
                               yOut += Math.min(-yOut, upperRightLimit.y - (markY + yOut));
                           }
                       }
                       
                       if (xOut != 0 || yOut != 0) {
                           startPan = true;
                           mPanSelectOutX = xOut;
                           mPanSelectOutY = yOut;
                           
                           double xFraction = ((double) xOut)/wlimit;
                           double yFraction = ((double) yOut)/hlimit;
                           moveDataViewport(xFraction, yFraction);
                           transformAllDataToWindowCoordinates();
                           mAnchorPoint.x -= xOut;
                           mAnchorPoint.y -= yOut;
                           
                           mPanSelectMarkX = markX;
                           mPanSelectMarkY = markY;
                           
                           markX -= xOut;
                           markY -= yOut;
                       }
                   }
                   
                   if (markX < 0) {
                       markX = 0;      
                   } else if (markX > wlimit) {
                       markX = wlimit;
                   }
                   
                   if (markY < 0) {
                       markY = 0;       
                   } else if(markY > hlimit) {
                       markY = hlimit;
                   }
                   
                   mShowRegionLine = true;
                   mCurrentDragPoint = e.getPoint();
                   markRegion(markX, markY);
                   
                   updatePendingSelections();
                   
                   boolean shiftDown = shiftDown(mKeyBits);
                   boolean ctrlDown = ctrlDown(mKeyBits);
                   boolean altDown = altDown(mKeyBits);

                   if (ctrlDown) {
                	   mCurrentAction = shiftDown ? REGION_REMOVE : REGION_ADD;
                   } else {
                	   mCurrentAction = REGION_SELECT;
                   }
                   
                   if (startPan) {
                       startAutoPanSelect();
                   }
                }
                
                mPanSelectLastDragX = x;
                mPanSelectLastDragY = y;
            }
        } else if (mActiveTool == Tool.ZOOM) {
            mouseDraggedForZoom(e);
        } else if (mActiveTool == Tool.PAN) {
            executePan(x, y);
        }
    }
    
    private void mouseDraggedForZoom(MouseEvent e) {
        mShowRegionLine = true;
        markRegion(e.getX(), e.getY());
        boolean shiftDown = shiftDown(mKeyBits);
        boolean ctrlDown = ctrlDown(mKeyBits);
        boolean altDown = altDown(mKeyBits);
        if (!(shiftDown || ctrlDown || altDown)) {
            mCurrentAction = ZOOM_REGION;
        } else {
            mCurrentAction = ZOOM_ILLEGAL;
        }
    }
    
    public void processMouseWheelEvent(MouseWheelEvent e) {
        super.processMouseWheelEvent(e);
        mZoomX = e.getX();
        mZoomY = e.getY();
        if (e.getWheelRotation() < 0) {
            zoomIn();
        } else {
            zoomOut();
        }
    }
    
}
