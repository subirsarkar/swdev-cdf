package config;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import javax.swing.border.*;

import com.smartsockets.*;
import daqmsg.*;
import rc.*;

import config.svt.*;
import config.util.*;

/**
 * <P>
 * Displays all the board related errors in a single window.
 * One can navigate board errors for the whole system from
 * this window. On arrival of new messages the window should
 * be automatically updated.
 * @author   S. Sarkar
 * @version  0.1, Oct 30, 2001
 */

public class SvtBoardErrorPanel extends JPanel {
  private static boolean DEBUG = true;
  private static final int TOTALWIDTH = 750;
  private static final int XWIDTH     = 160;
  private static final int XHEIGHT    = 450;

  protected JSplitPane splitPane;
  protected ListPanel listPanel;
  protected ErrorPanel errorPanel;
  protected DataFrame parent;
    /** Constructor 
     *  @param parent  Parent frame, SpyMessenger in this case
     */
  public SvtBoardErrorPanel(DataFrame parent) {
    this.parent = parent;
    buildGUI();
  }
    /** Prepare UI */
  protected void buildGUI() {
    setLayout(new BorderLayout());

    /* create the Crate List Panel */
    listPanel = new ListPanel();
    listPanel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

    /* create the Crate List Panel */
    JPanel aPanel = new JPanel();
    aPanel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

    /* Add the scroll panes to a split pane */
    splitPane = Tools.createSplitPane(JSplitPane.HORIZONTAL_SPLIT, listPanel, aPanel);
    Dimension minSize = listPanel.getSize();
    aPanel.setMinimumSize(minSize);
    listPanel.setMinimumSize(minSize);

    splitPane.setDividerLocation(XWIDTH);
    splitPane.setPreferredSize(new Dimension(TOTALWIDTH, XHEIGHT));

    add(splitPane, BorderLayout.CENTER);
  }
  class ErrorPanel extends JPanel {
    String crate;
    String board;
    int slot;
    JPanel eePanel, simPanel, cdfPanel, modePanel, regPanel;
    ErrorPanel(final String crate, final String board, int slot) {
      this.crate = crate;
      this.board = board;
      this.slot  = slot;
      buildGUI();
    }
    protected void buildGUI() {
      setLayout(new BorderLayout());
      setBorder(BorderFactory.createEtchedBorder());

      JPanel panel  = new JPanel(new BorderLayout());
      add(panel, BorderLayout.CENTER);

      JPanel tPanel = new JPanel(new GridLayout(1, 4));
      tPanel.add(eePanel   = new GlobalErrorSummaryPanel(crate, board, slot));
      tPanel.add(simPanel  = new GlobalErrorSummaryPanel(crate, board, slot, "Simulation"));
      tPanel.add(cdfPanel  = new CDFErrorPanel(crate, board, slot));
      tPanel.add(modePanel = new TModePanel(crate, board, slot));
      panel.add(tPanel, BorderLayout.NORTH);

      JPanel bPanel = new JPanel(new BorderLayout());
      if (board.equals("HF"))
        regPanel = new HFBoardErrorRegPanel(crate, board, slot);
      else
        regPanel = new BoardErrorRegPanel(crate, board, slot);
      bPanel.add(regPanel, BorderLayout.CENTER);

      panel.add(bPanel, BorderLayout.CENTER);
    }
  }
  class ListPanel extends JPanel {
    private JTree tree;
    private JScrollPane treeView;
    private DefaultMutableTreeNode rootNode;
    ListPanel() {
        // Create the nodes
      rootNode = new DefaultMutableTreeNode("SVT");
      createNodes();
      
        // Create a tree that allows one selection at a time 
      tree = new JTree(rootNode);
      tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
      tree.putClientProperty("JTree.lineStyle", "Angled");
      tree.setShowsRootHandles(true);
      
      DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
      renderer.setLeafIcon(new ImageIcon(AppConstants.iconDir+"mini-ball.png"));
      tree.setCellRenderer(renderer);

        // Listen to the selection changes 
      tree.addTreeSelectionListener(new TreeSelectionListener() {
        public void valueChanged(TreeSelectionEvent e) {
          BoardInfo info = (BoardInfo)getBoardInfo();
          if (info == null) return;
          try {
            SvtBoardData boardData = info.getBoardData();
            JPanel panel = new ErrorPanel(info.getCrateName(), boardData.getType(), boardData.getSlot());
            splitPane.setRightComponent(panel);
            if (parent.isDebugOn()) 
               parent.getTextPanel().displayText(boardData.toString());
          }
          catch (NullPointerException ex) {
            ex.printStackTrace();
          }
        }
      });
        // Create a scroll pane and add the tree to it 
      treeView = new JScrollPane(tree);
      treeView.setPreferredSize(new Dimension(XWIDTH, XHEIGHT-10));
      add(treeView, BorderLayout.CENTER);
    }
    public Dimension getSize() {
      return treeView.getPreferredSize();
    }
    protected void createNodes() {
      SvtCrateMap map = SvtCrateMap.getInstance();

      for (int i = 0; i < AppConstants.nCrates; i++) {
        String key = new String(SVT_CRATE_PREFIX+i);
        if (!map.isCrateReady(i)) continue;
        SvtCrateData crateData = map.getCrateData(key);
        if (crateData == null) return;

        String crateName = crateData.getName();
    	DefaultMutableTreeNode crate = new DefaultMutableTreeNode(new CrateInfo(crateData));
    	rootNode.add(crate);

        SvtBoardData [] boardData = crateData.getBoardData();
        for (int j = 0; j < boardData.length; j++) { 
          if (boardData[j].getNBuffers() == 0) continue;
      	  DefaultMutableTreeNode board = new DefaultMutableTreeNode(new BoardInfo(crateData.getName(), boardData[j]));
  	  crate.add(board);
        }
      }
    }
    protected DefaultMutableTreeNode getRootNode() {
      return rootNode;
    }
    /** Inner class <CODE>CrateInfo</CODE> which the tree uses to
     *  display information about the Crate
     */
    class CrateInfo {
      /** Reference to a crate object */
      private SvtCrateData crateData;
      /** @param crateData A CrateData object */
      CrateInfo(SvtCrateData crateData) {
    	this.crateData = crateData;
      }
      /** Override <CODE>toString()</CODE> to return the Crate Name 
       *  @return Crate name
       */
      public String toString() {
    	return crateData.getName();
      }
      /** Get a reference to a histogram
       *  @return Reference to a histogram
       */
      SvtCrateData getCrateData() {
    	return crateData;
      }
    }
    /** Inner class <CODE>BoardInfo</CODE> which the tree uses to
     *  display information about the Board
     */
    class BoardInfo {
      /** Reference to a board object */
      private SvtBoardData boardData;
      private String crateName;
      /** @param boardData A BoardData object */
      BoardInfo(final String crateName, final SvtBoardData boardData) {
        this.crateName = crateName;
    	this.boardData = boardData;
      }
      /** Override <CODE>toString()</CODE> to return the Board Name 
       *  @return Board name
       */
      public String toString() {
        return boardData.getType()+"-"+boardData.getSlot();
      }
      /** @return Reference to a histogram  */
      SvtBoardData getBoardData() {
    	return boardData;
      }
      /**  @return Reference to a histogram */
      String getCrateName() {
    	return crateName;
      }
    }
    public SvtBoardData getSelectedBoard() { 
      Object boardInfo = getBoardInfo();
      if (boardInfo == null) throw new NullPointerException();
      return ((BoardInfo)boardInfo).getBoardData();          
    } 
    public Object getBoardInfo() {
      DefaultMutableTreeNode node = 
        (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
      if (node == null) return null;
      if (!node.isLeaf() || node.isRoot()) return null;

      Object nodeInfo = node.getUserObject();
      if (nodeInfo == null) return null;

      return nodeInfo;
    }
  }
}
