package config.hist;

import java.util.Vector;
import java.util.Enumeration;
import java.awt.Toolkit;
import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import config.util.MyTreeModelListener;
import config.util.AppConstants;
import config.util.Tools;

/**
 * List the histogram in a tree like fasion, using different folders.
 * The structure should exactly follow the one found in the consumer.
 *
 * @author S. Sarkar
 * @version 0.1, March 2001
 */
public class HistogramListPanel extends JPanel implements HistogramUpdateable {
  public static final int INIT_SIZE = 1000;
  public static final boolean DEBUG = false;
  private Vector<Histogram> histoList = new Vector<Histogram>(INIT_SIZE);
  private JTree tree;
  private JScrollPane treeView;
  private DefaultMutableTreeNode rootNode;
  private DefaultTreeModel treeModel; 
  private MyTreeModelListener mtmListener; 
  private HistogramDrawingManager agent;
  private Toolkit toolkit = Toolkit.getDefaultToolkit();

  /** Construct the List Panel 
   *  @param agent The object which manages calls to drawHist, Redirects to proper places
   */
  public HistogramListPanel(HistogramDrawingManager agent) {
    this.agent = agent;
    setLayout(new BorderLayout());
    buildGUI();
  }
  /** Build the user interface */
  private void buildGUI() {
    // Create the nodes 
    rootNode    = new DefaultMutableTreeNode("SVTSPYMON");
    treeModel   = new DefaultTreeModel(rootNode); 
    mtmListener = new MyTreeModelListener(treeModel); 
    treeModel.addTreeModelListener(mtmListener); 

    // Create a tree that allows one selection at a time 
    tree = new JTree(treeModel);
    tree.setEditable(true);
    tree.setFont(AppConstants.gFont);
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

    tree.putClientProperty("JTree.lineStyle", "Angled");
    tree.setShowsRootHandles(true);

    DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
    renderer.setOpenIcon(new ImageIcon(AppConstants.iconDir+"opened.gif"));
    renderer.setClosedIcon(new ImageIcon(AppConstants.iconDir+"closed.gif"));
    renderer.setLeafIcon(new ImageIcon(AppConstants.iconDir+"histogram.gif"));
    tree.setCellRenderer(renderer);

    // Listen to the selection changes
    tree.addTreeSelectionListener(new TreeSelectionListener() {
      public void valueChanged(TreeSelectionEvent e) {
        drawSelectedHistogram();
      }
    });
    // Create a scroll pane and add the tree to it 
    treeView = new JScrollPane(tree);
    add(treeView, BorderLayout.CENTER);
  }
  /** Remove all nodes except the root node. */
  public void clear() {
    rootNode.removeAllChildren();
    treeModel.reload();
  }
  /** Remove the currently selected node. */
  public void removeCurrentNode() {
    TreePath currentSelection = tree.getSelectionPath();
    if (currentSelection != null) {
      DefaultMutableTreeNode currentNode = 
        (DefaultMutableTreeNode)currentSelection.getLastPathComponent();
      MutableTreeNode parent = (MutableTreeNode) currentNode.getParent();
      if (parent != null) {
        treeModel.removeNodeFromParent(currentNode);
        return;
      }
    } 

    // Either there was no selection, or the root was selected.
    toolkit.beep();
  }
  /** Draw the selected histogram on the plot panel. Delegates the responsibility 
   *  to the drawing agent
   */
  public void drawSelectedHistogram() {
    if (getHistogramInfo() == null) return;
    try {
      agent.drawHist(getSelectedHistogram());          
    }
    catch (NullPointerException ex) {
      ex.printStackTrace();
    }
  }
  /** On receipt of new histograms, update the list
   *  @param histData  Collection of histogram data
   */
  public void updateList(final HistogramColl histData) {
    int nhist = histData.getNHist();
    if (nhist != histoList.size()) {
      if (DEBUG) System.out.println("Updating histogram List");
      agent.setLabel("Updating list ...");
      for (int i = 0; i < histData.getNHist(); i++) {
        Histogram hist = histData.getHistogram(i);
        if (hist == null) continue;

        if (!histoList.contains(hist)) {
          if (DEBUG) System.out.println("Adding: " + hist.getTitle());
          addObject(hist, rootNode);
          histoList.addElement(hist);
        }
      }
      Runnable setUpdate = new Runnable() {
        public void run() {
          // Make sure that the tree does not collapse, but after the content is updated
          // still displays the previously selected path
          TreePath savedPath = tree.getSelectionPath();

          treeModel.reload();

          tree.setSelectionRow(tree.getRowForPath(savedPath));
          tree.makeVisible(savedPath);
        }
      };
      SwingUtilities.invokeLater(setUpdate);
      if (DEBUG) System.out.println("List size = " + histoList.size());
      agent.setLabel(histoList.size() + " objects ...");
    }
    Runnable setUpdate = new Runnable() {
      public void run() {
        agent.redrawHistograms();
      }
    };
    SwingUtilities.invokeLater(setUpdate);
  }
  /** Add a new new histogram as a child node 
   *  @param hist   histogram object
   *  @param rNode  Parent node
   */
  public void addObject(Histogram hist, DefaultMutableTreeNode rNode) {
    DefaultMutableTreeNode child = null;

    String folder = hist.getFolder().trim();
    if (DEBUG) System.out.println("Folder: " + folder);
    if (folder.equals(AppConstants.DefaultFolder)) {
      System.out.println("title: " + hist.getTitle());
    }
    String [] nodes = folder.split("/");

    DefaultMutableTreeNode pNode = rNode;
    for (int j = 0; j < nodes.length; j++) {
      boolean isAdded = false;
      if (DEBUG) System.out.println("Node Name: " + nodes[j]);
      for (Enumeration e = pNode.children(); e.hasMoreElements();) {
        DefaultMutableTreeNode obj = (DefaultMutableTreeNode) e.nextElement();
        if (DEBUG) System.out.println("Object = " + obj);
        if (obj != null && obj.toString().equals(nodes[j])) {
          if (DEBUG) System.out.println("Node " + obj + " already added");
          pNode   = obj;
          isAdded = true;
          break;  
        }
      }
      if (isAdded) continue;
      //child = addObject(pNode, child, true);
      pNode.add(child = new DefaultMutableTreeNode(nodes[j]));
      if (DEBUG) System.out.println("Parent Node = " + pNode + 
                     " Child Node: " + child);
      pNode = child;
    }

    // and lastly, add the histogram
    //addObject(pNode, new HistogramInfo(hist), true);
    pNode.add(new DefaultMutableTreeNode(new HistogramInfo(hist)));
  }
  /** Add child to the currently selected node. */
  public DefaultMutableTreeNode addObject(Object child) {
    DefaultMutableTreeNode parentNode = null;
    TreePath parentPath = tree.getSelectionPath();

    if (parentPath == null) {
       parentNode = rootNode;
    } else {
       parentNode = (DefaultMutableTreeNode) (parentPath.getLastPathComponent());
    }

    return addObject(parentNode, child, true);
  }
  public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent,
                                          Object child) {
    return addObject(parent, child, false);
  }
  /** add a new child node 
   *  @param parent          Parent node
   *  @param child           Child object
   *  @param shouldBeVisible option whether the newly added child node should be visible
   *  @return reference to the new child node
   */
  public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent,
                                          Object child, 
                                          boolean shouldBeVisible) 
  {
    DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child);
    if (parent == null) {
      parent = rootNode;
    }
    treeModel.insertNodeInto(childNode, parent, parent.getChildCount());

    // Make sure the user can see the lovely new node.
    if (shouldBeVisible) {
      tree.scrollPathToVisible(new TreePath(childNode.getPath()));
    }
    return childNode;
  }
  /** Get reference to the selected histogram object 
   *  @return reference to the selected histogram object 
   */
  public Histogram getSelectedHistogram() { 
    HistogramInfo histInfo = (HistogramInfo) getHistogramInfo();
    if (histInfo == null) throw new NullPointerException();
    return histInfo.getHistogram();          
  }
  /** Select the previous histogram and return a reference to it
   *  @return reference to the next histogram object 
   */
  public Histogram getPreviousHistogram() { 
    int row = tree.getMaxSelectionRow();
    if (row == 0) row = 1;
    tree.setSelectionRow(row-1);
    return getSelectedHistogram();
  } 
  /** Select the next histogram and return a reference to it
   *  @return reference to the next histogram object 
   */
  public Histogram getNextHistogram() { 
    int row = tree.getMaxSelectionRow();
    if (row == histoList.size()) row = histoList.size() - 1;
    tree.setSelectionRow(row+1);
    return getSelectedHistogram();
  } 
  /** Get information about the currently selected histogram 
   *  return information about the histogram object
   */
  public Object getHistogramInfo() {
    DefaultMutableTreeNode node = 
      (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
    if (node == null) return null;
    if (!node.isLeaf() || node.isRoot()) return null;

    return node.getUserObject();
  }
  /** Inner class <CODE>HistogramInfo</CODE> which the tree uses to
   *  display information about the histogram
   */
  private class HistogramInfo {
    /** Reference to a histogram object */
    private Histogram hist;
    /** @param hist An histogram object */
    HistogramInfo(Histogram hist) {
      this.hist = hist;
    }
    /** Override <CODE>toString()</CODE> to return the histogram title 
     *  @return Histogram title
     */
    public String toString() {
      return hist.getTitle();
    }
    /** Get a reference to a histogram
     *  @return Reference to a histogram
     */
    public Histogram getHistogram() {
      return hist;
    }
  }
}
