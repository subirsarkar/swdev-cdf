package config;

import java.io.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.tree.*;

import config.util.*;
import config.svt.*;

public class SvtBoardErrorFrame extends DataFrame {
  private static final int TOTALWIDTH = 750;
  private static final int XWIDTH     = 160;
  private static final int XHEIGHT    = 450;
  private static Dimension medSize = new Dimension(180, 50);
  
  private SvtBoardErrorPanel bePanel;
  private JSplitPane splitPane;

  public SvtBoardErrorFrame(boolean standAlone) {
    super(standAlone, "SVT Board Error Details", true, true, -1);
    buildGUI();

    String filename = Tools.getEnv("SVTMON_DIR")+"/help/a_SvtBoardErrorFrame.html";
    setHelpFile(filename, "About Board Errors", new Dimension(500, 300));
  }
  protected void buildGUI() {
    addToolBar();

    JPanel panel = new JPanel(new BorderLayout());

    bePanel = new SvtBoardErrorPanel();
    TextPanel textPanel = getTextPanel();
    textPanel.setBorder(Tools.etchedTitledBorder(" Message Logger "));
    textPanel.setPreferredSize(medSize);

    splitPane = Tools.createSplitPane(JSplitPane.VERTICAL_SPLIT, bePanel, textPanel);
    panel.add(splitPane, BorderLayout.CENTER);

    getContentPane().add(panel, BorderLayout.CENTER);  // Error Panel + Message area
    addStatusBar();
  }
  protected SvtBoardErrorPanel getErrorPanel() {
    return bePanel;    
  }
  public void updateGUI(final SvtCrateData crateData) {
    bePanel.updateGUI(crateData);
  }
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
    protected JSplitPane splitPane;
    protected ListPanel listPanel;
    protected ErrorPanel errorPanel;
    private Vector<String> crateList = new Vector<String>(AppConstants.nCrates);
      /** Constructor 
       */
    public SvtBoardErrorPanel() {
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
    protected void updateGUI(SvtCrateData crateData) {
      if (!crateList.contains(crateData.getName())) {
        listPanel.addCrate(crateData);
      }
      else { // Updated data for the selected board
        BoardInfo info = (BoardInfo) listPanel.getBoardInfo();
        if (info == null) return;
        if (!info.getCrateName().equals(crateData.getName())) return;
        SvtBoardData boardData = 
          crateData.getBoardData(info.getBoardName(), info.getBoardSlot());
        info.setBoardData(boardData);
        listPanel.showBoardData(info);
      }
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
      private MyTreeModelListener mtmListener; 
      private DefaultTreeModel treeModel; 

      ListPanel() {
        super(true);
  	  // Create the nodes
  	rootNode    = new DefaultMutableTreeNode("SVT");
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
        renderer.setOpenIcon(new ImageIcon(AppConstants.iconDir+"opened.png"));
        renderer.setClosedIcon(new ImageIcon(AppConstants.iconDir+"closed.png"));
  	renderer.setLeafIcon(new ImageIcon(AppConstants.iconDir+"mini-ball.png"));
  	tree.setCellRenderer(renderer);
  
	// Listen to the selection changes 
  	tree.addTreeSelectionListener(new TreeSelectionListener() {
  	  public void valueChanged(TreeSelectionEvent e) {
      	    BoardInfo info = (BoardInfo) getBoardInfo();
  	    if (info == null) return;
            showBoardData(info);
  	  }
  	});
  	// Create a scroll pane and add the tree to it 
  	treeView = new JScrollPane(tree);
  	treeView.setPreferredSize(new Dimension(XWIDTH, XHEIGHT-10));
  	add(treeView, BorderLayout.CENTER);

      }
      public void showData(BoardInfo info) {
        boolean found = false;
        for (Enumeration e = rootNode.children() ; e.hasMoreElements() ;) {
          DefaultMutableTreeNode crate = (DefaultMutableTreeNode) e.nextElement();
          if (crate.toString().equals(info.getCrateName())) {
            TreePath cratePath = new TreePath(new Object[] {rootNode, crate}); 
            // Does not work with 1.1.8
            // tree.setExpandsSelectedPaths(true);
            tree.expandPath(cratePath); 
            for (Enumeration x = crate.children() ; x.hasMoreElements() ;) {
              DefaultMutableTreeNode board = (DefaultMutableTreeNode) x.nextElement();
              String name = info.getBoardData().getType()+"-"+info.getBoardData().getSlot();
              if (board.toString().equals(name)) {
                if (board.isRoot() || !board.isLeaf()) break;

                TreePath boardPath = new TreePath(new Object[] {rootNode, crate, board});
                if (isDebugOn()) System.out.println(boardPath);

                tree.setSelectionPath(boardPath);
                tree.setSelectionRow(tree.getRowForPath(boardPath));
                tree.makeVisible(boardPath);
                found = true;
                break;
              }
            }
            if (found) break;
          }
        }        
        repaint();
      }
      public void selectRow(int row) {
        tree.setSelectionRow(row);
      }
      protected void showBoardData(BoardInfo info) {
      	try {
      	  SvtBoardData boardData = info.getBoardData();
      	  JPanel panel = new ErrorPanel(info.getCrateName(),
      		                        boardData.getType(), boardData.getSlot());
      	  splitPane.remove(splitPane.getRightComponent());
      	  splitPane.setRightComponent(panel);
      	  if (isDebugOn()) displayText(boardData.toString());
      	}
      	catch (NullPointerException ex) {
      	  ex.printStackTrace();
      	}
      }
      public Dimension getSize() {
  	return treeView.getPreferredSize();
      }
      protected void createNodes() {
        SvtCrateMap map = SvtCrateMap.getInstance();

        for (Map.Entry<String, SvtCrateData> entry : map.entrySet()) {  
          String key = entry.getKey();
          SvtCrateData crateData = entry.getValue();
          if (crateData == null) continue;
          addCrate(crateData);
        }    	
      }
      public void addCrate(SvtCrateData crateData) {
  	String crateName = crateData.getName();
  	DefaultMutableTreeNode crate 
          = new DefaultMutableTreeNode(new CrateInfo(crateData));
  	rootNode.add(crate);
  	
  	SvtBoardData [] boardData = crateData.getBoardData();
  	for (int j = 0; j < boardData.length; j++) { 
  	  if (boardData[j].getNBuffers() == 0) continue;
  	  DefaultMutableTreeNode board 
            = new DefaultMutableTreeNode(new BoardInfo(crateData.getName(), boardData[j]));
  	  crate.add(board);
  	}

        crateList.addElement(crateData.getName());
        treeModel.reload();
      }
      protected DefaultMutableTreeNode getRootNode() {
  	return rootNode;
      }
      public SvtBoardData getSelectedBoard() { 
  	Object boardInfo = getBoardInfo();
  	if (boardInfo == null) throw new NullPointerException("Null BoardInfo reference");
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
  public void showData(BoardInfo info) {
    // Easier option which does not show the element in the list
    // bePanel.listPanel.showBoardData(info); 

    // 
    bePanel.listPanel.showData(info); 
  }
  public static void main(String [] argv) {
    JFrame f = new SvtBoardErrorFrame(true);
    f.setSize(f.getPreferredSize());
    f.setVisible(true);
  }
}
