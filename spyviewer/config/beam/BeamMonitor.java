package config.beam;

import java.util.Observer;
import java.util.Observable;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.JSplitPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;

import com.smartsockets.TipcProcessCb;
import com.smartsockets.TipcMsg;
import com.smartsockets.TipcCb;
import com.smartsockets.Tut;
import com.smartsockets.TipcException;

import config.hist.XYPlotPanel;

import config.util.DataFrame;
import config.util.Tools;
import config.util.AppConstants;
import config.util.AbstractMessageThread;
import config.util.TextPanel;
import config.util.MyTreeModelListener;
import config.util.HtmlPanel;

public class BeamMonitor extends DataFrame {
  public static final int TREEWIDTH = 150;
  public static final int XHEIGHT   = 620;
  public static final int XWIDTH    = 800;
  private static final Dimension minSize = new Dimension(300, 50);
  private JSplitPane splitPane;
  private JSplitPane splitPane2;
  private JSplitPane splitPane3;
  private JPanel plotCanvas    = new JPanel(new GridLayout(1,2));
  private ListPanel listPanel  = new ListPanel();
  private HtmlPanel htmlPanel  = new HtmlPanel();
  private BeamPositionByBarrel bpBarrel = new BeamPositionByBarrel();
  private BeamPositionByWedge  bpWedge  = new BeamPositionByWedge();
  private XYPlotPanel [] plotPanels = new XYPlotPanel[2];

  private MessageThread mThread   = null;
  public BeamMonitor(boolean standalone, boolean connectOnStartup) {
    super(standalone, "Online Beam Position Monitor", false, true, -1);
    buildGUI();

    if (connectOnStartup) startMessageThread();

    // Set Help file
    String filename = Tools.getEnv("SVTMON_DIR")+"/help/a_BeamMonitor.html";
    setHelpFile(filename, "About Beam Position Monitor", new Dimension(600, 400));
  }
  protected void buildGUI() {
    addToolBar();

    addHelpMenu();
    addHelpInToolBar();

    // Create the main application area now
    JPanel panel = new JPanel(new BorderLayout());

    listPanel.setBorder(BorderFactory.createEmptyBorder(5,0,0,0));
    htmlPanel.setBorder(BorderFactory.createEmptyBorder(5,0,0,0));
    plotCanvas.setBackground(Color.white);
    for (int i = 0; i < plotPanels.length; i++) {
      plotPanels[i]  = new XYPlotPanel(new Dimension(350, 300), false);
      plotCanvas.add(plotPanels[i]);
    }

    listPanel.setMinimumSize(new Dimension(150, 300));
    htmlPanel.setMinimumSize(minSize);

    splitPane2 = Tools.createSplitPane(JSplitPane.VERTICAL_SPLIT, htmlPanel, plotCanvas);
    splitPane2.setDividerLocation(350);
    splitPane.setRightComponent(splitPane2);

    splitPane = Tools.createSplitPane(JSplitPane.HORIZONTAL_SPLIT, listPanel, splitPane2);
    splitPane.setDividerLocation(150);
    splitPane.setPreferredSize(new Dimension(XWIDTH, XHEIGHT));

    TextPanel textPanel = getTextPanel();
    textPanel.setPreferredSize(minSize);
    textPanel.setBorder(Tools.etchedTitledBorder(" Message Logger "));

    splitPane3 = Tools.createSplitPane(JSplitPane.VERTICAL_SPLIT, splitPane, textPanel);
    splitPane3.setDividerLocation(550);
    panel.add(splitPane3, BorderLayout.CENTER);

    // Add at the center
    getContentPane().add(panel, BorderLayout.CENTER);

    addStatusBar();
  }
  public void update() {
    listPanel.updatePageInfo();
  }
  protected void exitApp() {
    if (mThread != null && mThread.isConnected()) stopMessageThread();      
    dispose();          
    System.exit(0);
  }
  protected void startMessageThread() {
    super.startMessageThread();
    mThread = new MessageThread();
  }
  protected void stopMessageThread() {
    super.stopMessageThread();
    Tools.ensureEventThread();
    if (mThread == null) throw new NullPointerException("Message Thread not available!");
    mThread.halt();
  }
  public void drawPlots(IBeamPosition handler) {
    double [] xValueArr = handler.getXDataArray();
    double [] yValueArr = handler.getYDataArray();
    double [] xErrArr   = handler.getXErrorArray();
    double [] yErrArr   = handler.getYErrorArray();

    boolean isBarrel = (xValueArr.length == AppConstants.nBarrel) ? true : false;
    String xlabel = ((isBarrel) ? "Barrel" : "Wedge");

    double [] xpos = new double[xValueArr.length];
    for (int i = 0; i < xpos.length; i++) 
       xpos[i] = i + 0.5;
    
    for (int i = 0; i < plotPanels.length; i++) {
      double [] data;
      double [] error;
      String ylabel;
      
      if (i == 0) {
        data  = xValueArr;
        error = xErrArr;
        ylabel = "X-Pos";
      }
      else {
        data  = yValueArr;
        error = yErrArr;
        ylabel = "Y-Pos";
      }
      plotPanels[i].setData(xpos, data, error, "");
      plotPanels[i].setLabels(xlabel, ylabel);
      plotPanels[i].setRange(xpos[0]-1.0, xpos[xpos.length-1]+1.0, data[0]-100.,data[data.length-1]+100.);
      plotPanels[i].drawPlot();
    }
  }
  public void clearPanel() {
    plotCanvas.removeAll();
    plotCanvas.repaint();
  }
  class ListPanel extends JPanel {
    private JTree tree;
    private JScrollPane treeView;
    private DefaultMutableTreeNode rootNode;
    private DefaultTreeModel treeModel;
    private MyTreeModelListener mtmListener;
    ListPanel() {
      buildGUI();
    } 
    protected void buildGUI() {
      // Create the nodes 
      rootNode    = new DefaultMutableTreeNode("BeamPosition");
      treeModel   = new DefaultTreeModel(rootNode);
      mtmListener = new MyTreeModelListener(treeModel);
      treeModel.addTreeModelListener(mtmListener);
  
      createNodes();
    
      // Create a tree that allows one selection at a time 
      tree = new JTree(treeModel);
      tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    
      tree.putClientProperty("JTree.lineStyle", "Angled");
      tree.setShowsRootHandles(true);
    
      DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
      renderer.setOpenIcon(new ImageIcon(AppConstants.iconDir+"opened.gif"));
      renderer.setClosedIcon(new ImageIcon(AppConstants.iconDir+"closed.gif"));
      renderer.setLeafIcon(new ImageIcon(AppConstants.iconDir+"mini-ball.gif"));
      tree.setCellRenderer(renderer);
  
      // Listen to the selection changes
      tree.addTreeSelectionListener(new TreeSelectionListener() {
  	public void valueChanged(TreeSelectionEvent e) {
          updatePageInfo();
  	}
      });
      // Create a scroll pane and add the tree to it 
      treeView = new JScrollPane(tree);
      treeView.setPreferredSize(new Dimension(TREEWIDTH, XHEIGHT-10));
      add(treeView, BorderLayout.CENTER);
    }
    protected void createNodes() {
      DefaultMutableTreeNode node = new DefaultMutableTreeNode("All Barrels");
      rootNode.add(node);

      DefaultMutableTreeNode wedges = new DefaultMutableTreeNode("By Wedge");
      rootNode.add(wedges);

      for (int i = 0; i < AppConstants.nBarrel; i++) {
        node = new DefaultMutableTreeNode("Barrel " + i);
        wedges.add(node);
      }
    }
    /** Update Beam Position info for this page 
     *  either html data or the plots
     */
    public void updatePageInfo() {
      DefaultMutableTreeNode node = 
        (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
      if (node == null) return;
    
      Object nodeInfo = node.getUserObject();
      if (!node.isLeaf()) return;
  
      String leafName = nodeInfo.toString();
      if (leafName.equals("All Barrels")) {
        htmlPanel.displayText(bpBarrel.getData());
        BeamMonitor.this.drawPlots(bpBarrel);
      }
      else {
        String [] fields = Tools.split(leafName);
        bpWedge.setBarrel(Integer.parseInt(fields[1]));
        htmlPanel.displayText(bpWedge.getData());
        BeamMonitor.this.drawPlots(bpWedge);
      }
      repaint();
    }
    protected void reloadTree() {
      treeModel.reload();
    }
    protected JTree getTree() {
      return tree;
    }
    protected JScrollPane getScrollPane() {
      return treeView;
    }
    protected DefaultMutableTreeNode getRootNode() {
      return rootNode;
    }
  }
  class MessageThread extends AbstractMessageThread {
    private Observer beamObserver = new BeamObserver();
    private IBeamPosition bpData  = null;
    /** 
     * Initialise the Thread 
     */
    public MessageThread() {
      setBeamCallback(true);
      setBeamByWedgeCallback(true);
      
      startThread();
    }
    public void halt() {
       setBeamCallback(false);
       setBeamByWedgeCallback(false);

       mThread.closeSrv();
       setThreadToStop(true);
       setConnected(false);
    }
    public class BeamObserver implements Observer {
      public BeamObserver() {}
      public void update(Observable obj, Object arg) {
        if (!(arg instanceof IBeamPosition)) return;
        final IBeamPosition bpData = (IBeamPosition) arg;
  
        // Always update GUI via event handling thread
        // Here first we update the data 
        // For All Barrel 'BeamPositionByBarrel' should update
        // otherwise, 'BeamPositionByWedge'
        // Once the data is updated we update display with 
        // correct data
        Runnable setLabelRun = new Runnable() {
          public void run () {
            try {
              BeamMonitor.this.update();
            }
            catch (Exception ex) {
              ex.printStackTrace();
            }
          }
        };
        SwingUtilities.invokeLater(setLabelRun);
        if (isDebugOn()) warn("Beam Position dump:\n" + bpData);
      }
    }
    /** Inner class to handle Beam position related message */
    public class ProcessBeamMessage implements TipcProcessCb {
      /** 
       * Process Beam related messages when the callback is triggered
       * @param  msg   Reference to the SmartSockets message
       * @param  obj   User specified object that might be passed to the callback
       */
      public void process (TipcMsg msg, Object arg) {
  	if (isDebugOn()) warn("Beam messages published", Color.green);
  	try {
  	  msg.setCurrent(0);  // position the field ptr to the beginning of the message
          bpBarrel.update(msg);
  	} 
        catch (TipcException e) {
  	  Tut.warning(e);
  	}    
  
  	setChanged();
  	notifyObservers(bpBarrel);
      } 
    }
    /** Inner class to handle Beam position related message */
    public class ProcessBeamByWedgeMessage implements TipcProcessCb {
      /** 
       * Process Beam related messages when the callback is triggered
       * @param  msg   Reference to the SmartSockets message
       * @param  obj   User specified object that might be passed to the callback
       */
      public void process (TipcMsg msg, Object arg) {
  	if (isDebugOn()) warn("BeamByWedge messages published", Color.green);
  	try {
  	  msg.setCurrent(0);  // position the field ptr to the beginning of the message
          bpWedge.update(msg);
  	} 
        catch (TipcException e) {
  	  Tut.warning(e);
  	}    
  
  	setChanged();
  	notifyObservers(bpWedge);
      } 
    }
    /** Register Beam Position callback */
    protected void setBeamCallback (boolean setCallback) {
      TipcCb pBeam = null;
      String dest = AppConstants.BEAM_SUBJECT;
      try {
  	if (setCallback) {
          if (!srv.getSubjectSubscribe(dest)) {
  	    ProcessBeamMessage evRef = new ProcessBeamMessage();
  	    pBeam = srv.addProcessCb(evRef, dest, srv); 
  	    if (pBeam == null) {
  	      Tut.exitFailure("WARNING. Couldn't register beam subject callback!\n");
  	    }
  	    srv.setSubjectSubscribe(dest, true);
  	    warn("INFO. Subscribed to  " + dest, Color.green);
  
  	    addObserver(beamObserver);
  	  }
  	} 
        else {
  	  if (srv.getSubjectSubscribe(dest)) {
  	    srv.setSubjectSubscribe(dest, false);
  	    warn("INFO. Unsubscribed from  " +  dest, Color.red);
  
  	    deleteObserver(beamObserver);
  	  }
  	}
      } 
      catch (TipcException Tipe) {
  	Tut.warning(Tipe);
      } 
    }
    /** Register Beam Position callback */
    protected void setBeamByWedgeCallback (boolean setCallback) {
      TipcCb pBeam = null;
      String dest = AppConstants.BEAM_WEDGE_SUBJECT;
      try {
  	if (setCallback) {
          if (!srv.getSubjectSubscribe(dest)) {
  	    ProcessBeamByWedgeMessage evRef = new ProcessBeamByWedgeMessage();
  	    pBeam = srv.addProcessCb(evRef, dest, srv); 
  	    if (pBeam == null) {
  	      Tut.exitFailure("WARNING. Couldn't register beam subject callback!\n");
  	    }
  	    srv.setSubjectSubscribe(dest, true);
  	    warn("INFO. Subscribed to  " + dest, Color.green);
  
  	    addObserver(beamObserver);
  	  }
  	} 
        else {
  	  if (srv.getSubjectSubscribe(dest)) {
  	    srv.setSubjectSubscribe(dest, false);
  	    warn("INFO. Unsubscribed from  " +  dest, Color.red);
  
  	    deleteObserver(beamObserver);
  	  }
  	}
      } 
      catch (TipcException Tipe) {
  	Tut.warning(Tipe);
      } 
    }
  }
  public static void main(String [] argv) {
    boolean connOnStartup = true;
    if (argv.length > 0) connOnStartup = (Integer.parseInt(argv[0]) > 0) ? true : false;

    BeamMonitor gui = new BeamMonitor(true, connOnStartup);
    gui.setSize(new Dimension(900,720));
    gui.setVisible(true);
  }
}
