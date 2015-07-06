package config;

import java.io.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import javax.swing.table.*;

import com.smartsockets.*;

import config.svt.*;
import config.util.*;

/**
 * <P>
 * This widget is used to configure the spymon options in the SVT crates.
 * A single message is created for each crate and published via SmartSockets.
 * The configuration options are described in detail below.</P>
 * <P>
 * <UL>
 *    <LI>Mode of monitoring waiting for R_C partition or do routine tasks 
 *        even when the partition is not there.</LI>
 *    <LI>Run Mode. Routine pole and reading of spy buffers even if there is 
 *        no freeze from outside. This option has to be elaborated in future
 *        to make it more flexible.</LI>
 *    <LI>Frequency of buffer reading and number of words to be read (assumed to be 
 *        the same for all the buffers for now).</LI>
 *    <LI>Which buffers are selected for readout</LI>
 *    <LI>Which histograms in a spy buffer should be filled (if booked).</LI>
 * </UL>
 * <P>The histogram option is non-trivial if we do configure histogram booking
 *    at startup. In that case it will be safe to first read the list of histograms
 *    from the crate and then select them for filling. We may single out standard
 *    histograms and always fill them. </P>
 *
 * @author   S. Sarkar
 * @version  0.1, April 24, 2001
 * @version  0.5, November 22, 2001
 */

public class SvtCrateConfig extends ConfigDataFrame  {
  private static final Class intType = (new Integer(1)).getClass();
  public final static int TOTALWIDTH  = 600;
  public final static int TREEWIDTH   = 200;
  public final static int XHEIGHT     = 250;
  public final static String [] tails = 
    {
       "_spy",
       "_spy_readout",
       "_spy_dump",
       "_spy_histo",
       "_spy_simulation",
       "_spy_readoutWords",
       "_spy_maxDumpWords",
       "_spy_dumpFrequency",
       "_spy_errorHisto",
       "_spy_stdHisto",
       "_spy_physHisto",
       "_spy_expertHisto"
    };
  private JSplitPane splitPane; 
  private SvtSpyConfigPanel scPanel;
  private ButtonPanel buttonPanel;
  private static final Dimension longSize  = new Dimension(300, 100);
  private static final Dimension shortSize = new Dimension(100, 20);

    /** Constructor 
     *  @param parent  Parent frame, SpyMessenger in this case
     */
  public SvtCrateConfig(boolean standalone) {
    super(standalone, "SVT Crate Configuration Message Builder");
    loadDefaultProperties();
    buildGUI();

    String filename = Tools.getEnv("SVTMON_DIR")+"/help/a_SvtCrateConfig.html";
    setHelpFile(filename, "About SVT Crate Config Message Builder", 
                              new Dimension(500, 400));
  }
  protected void loadDefaultProperties() {
    String filename = Tools.getEnv("SVTMON_DIR")+"/spy_config.prop";
    loadProperties(filename);    
  }
  public void loadProperties(final String filename) {
    Thread runner = new Thread() {
      public void run() {
        Tools.loadProperties(props, filename);
      }
    };
    runner.start();
  }
    /** Prepare UI */
  protected void buildGUI() {
    addToolBar();
    updateToolBar();
  
    JPanel panel = new JPanel(new BorderLayout());
    scPanel = new SvtSpyConfigPanel(this);
    panel.add(scPanel, BorderLayout.NORTH);

    TextPanel textPanel = getTextPanel();
    textPanel.setBorder(Tools.etchedTitledBorder(" Message Logger "));
    textPanel.setPreferredSize(longSize);

    splitPane = Tools.createSplitPane(JSplitPane.VERTICAL_SPLIT, scPanel, textPanel);
    panel.add(splitPane, BorderLayout.CENTER);
    
    JPanel px = new JPanel(new BorderLayout());
    panel.add(px, BorderLayout.SOUTH);

    buttonPanel = new ButtonPanel();
    buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 5));
    px.add(buttonPanel, BorderLayout.EAST);

    getContentPane().add(panel, BorderLayout.CENTER);
    addStatusBar();
  }
  // Add new buttons to the existing toolbar
  protected void updateToolBar() {
    Icon icon = new ImageIcon(AppConstants.iconDir+"Refresh.png");
    Action action = new AbstractAction("Refresh", icon) {
      public void actionPerformed(ActionEvent e) {
        scPanel.updateCrateList();
      }
    };
    addToolElement(action, "Update tree content ...", -1, 1);
  }
  public void updateGUI(SvtCrateData crateData) {
    scPanel.updateGUI(crateData);
  }
  public void resetProperties() {
    super.resetProperties();
    // Reset UI display from original property, actually from the one last updated
    scPanel.updateTableContent();
  }
  class ButtonPanel extends JPanel {
    ComboPanel destPanel;
    ButtonPanel() {
      setLayout(new FlowLayout());
  
      destPanel = new ComboPanel(SendCommandFrame.getDestinations("config"), "Destination", 12);
      add(destPanel);

      JButton button = Tools.createButton("Reset");
      add(button);
      button.addActionListener( new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          resetProperties();
        }         
      });
  
      button  = Tools.createButton("Update");
      add(button);
      button.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent e) {
          updateProperties();
  	}  
      });
  
      button  = Tools.createButton("Send");
      add(button);
      button.addActionListener( new ActionListener() {
  	public void actionPerformed(ActionEvent e) {
          try {
            sendMessage();
          }
          catch (TipcException ex) {
            Tut.warning(ex);
          }
          catch (IOException ex) {
            ex.printStackTrace();
          }
          catch (NullPointerException ex) {
            ex.printStackTrace();
          }
  	}  
      });
    }
    void sendMessage() throws TipcException, IOException, NullPointerException {
      TipcMsg msg = TipcSvc.createMsg(TipcMt.STRING_DATA);
      msg.setNumFields(0); 

      TipcMsg [] submsg = new TipcMsg[transProp.size()];
      int index = 0;
      for (Enumeration e = transProp.propertyNames() ; e.hasMoreElements() ;) {
        submsg[index] = TipcSvc.createMsg(TipcMt.STRING_DATA);
        submsg[index].setNumFields(0); 
        String key = (String)e.nextElement();
        submsg[index].appendStr(key);
        submsg[index].appendInt4(Integer.parseInt(transProp.getProperty(key)));
        index++;
      }
      msg.appendMsgArray(submsg);
      if (isDebugOn() || isDebugOnly()) Tools.showMessage(SvtCrateConfig.this, msg);
    
      if (isDebugOnly()) return;

      String dest = destPanel.getSelectedItem();
      msg.setDest(dest);

      TipcSrv srv = Tools.getServer();
      srv.send(msg);
      srv.flush();
      msg.destroy();

      // Now merge the two proerties, i.e update the original one
      updateProperties();

      // Start afresh the next time around with the temporary property
      transProp.clear();
    }
  }
  class SvtSpyConfigPanel extends JPanel {
    protected DataFrame parent;
    protected JSplitPane splitPane;
    protected ListPanel listPanel;
    protected BufferTablePanel tablePanel = null;

    SvtSpyConfigPanel(DataFrame parent) {
      this.parent = parent;
      buildGUI();
    } 
    protected void buildGUI() {
      setLayout(new BorderLayout());

      // create the Crate List Panel 
      listPanel = new ListPanel(this);
      listPanel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

      JPanel aPanel = new JPanel(new BorderLayout());
      aPanel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

      // Add the scroll panes to a split pane
      splitPane = Tools.createSplitPane(JSplitPane.HORIZONTAL_SPLIT, listPanel, aPanel);
      Dimension minSize = listPanel.getScrollPane().getPreferredSize();
      aPanel.setMinimumSize(minSize);
      listPanel.setMinimumSize(minSize);

      splitPane.setDividerLocation(200);
      splitPane.setPreferredSize(new Dimension(TOTALWIDTH, XHEIGHT));

      add(splitPane, BorderLayout.CENTER);
    }
    public void updateCrateList() {
      listPanel.updateNodes();
    }
    public void updateGUI(SvtCrateData crateData) {
      listPanel.addCrate(crateData);
      listPanel.reloadTree();
    }
    protected JSplitPane getSplitPanel() {
      return splitPane;
    }
    void updateTableContent() {
      tablePanel.setTableContent();
    }
    class BufferTablePanel extends JPanel {
      protected String crate;
      protected int slot;
      protected String board;
      protected String buffer;
      String [] pNames  = new String[tails.length];
      String [] pValues = new String[tails.length];
      final String [] columnNames = new String[] {"Property", "Value"};

        /** User defined data model to be used by the table */
      private MyTableModel dataModel;
        /** The JTable UI component */
      private JTable table = null;
      BufferTablePanel(final String crate, int slot, final String board, final String buffer) {
        setBuffer(crate, slot, board, buffer);
        buildGUI();
      } 
      protected void buildGUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(5,5,0,0));

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.white);
        add(panel, BorderLayout.CENTER);

        fillData();
        dataModel = new MyTableModel(columnNames, pNames, pValues); 
        dataModel.addTableModelListener(new TableModelListener() {
          public void tableChanged(TableModelEvent ev) {
            int row    = ev.getFirstRow();
            int column = ev.getColumn();
            String columnName = dataModel.getColumnName(column);
            transProp.put(dataModel.getValueAt(row, 0), dataModel.getValueAt(row, column));
            if (isDebugOn()) transProp.list(System.out);
          }
        });
        table = new JTable(dataModel); 
        TableColumn column = null;
        for (int i = 0; i < columnNames.length; i++) {
          column = table.getColumnModel().getColumn(i);
          if (i == 0) 
            column.setPreferredWidth(250);
          else 
            column.setPreferredWidth(50);
        }
        table.setPreferredScrollableViewportSize(new Dimension(TOTALWIDTH-TREEWIDTH, XHEIGHT)); 
        JScrollPane scrollPane = new JScrollPane(table); 
        panel.add(scrollPane, BorderLayout.CENTER); 
      }
      protected void setTableContent() {
        fillData();
        dataModel.setTableContent(pNames, pValues);
      }
      void updateTableContent() {
        setTableContent();
        repaint();
      }
      protected void fillData() {
        for (int i = 0; i < pNames.length; i++) {
          pNames[i]  = getKey(crate, slot, board, buffer, tails[i]);
            // Check whether this property has been modified, if not use 'default'
          pValues[i] = transProp.getProperty(pNames[i], props.getProperty(pNames[i]));
        }
      }
      protected void setBuffer(final String crate, 
                               int slot, 
                               final String board, 
                               final String buffer) 
      {
        this.crate  = crate;
        this.slot   = slot;
        this.board  = board;
        this.buffer = buffer;
      }
        /** Get the table data model
         *  @return The reference to the table data model
         */
      public AbstractTableModel getDataModel() {
        return dataModel;
      }
        /** Inner class TableModel specific to the application */
      class MyTableModel extends AbstractTableModel {
          /** Application specific column names */
        protected final String [] columnNames;
          /** Application specific data matrix */
        protected Object [][] data;

        MyTableModel(final String [] columnNames, final String [] keys, final String [] values) {
          this.columnNames = columnNames;
          data = new Object[keys.length][columnNames.length];
          setTableContent(keys, values);
        }
        void setTableContent(final String [] keys, final String [] values) {
          for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
              data[i][j] = (j == 0) ? keys[i] : values[i];
            }
          }
        }
          /** Get number of columns */
        public int getColumnCount() {
          return columnNames.length;
        }
         /** Get number of rows */
        public int getRowCount() {
          return data.length;
        }
         /** 
          * Get name of the ith column 
          * @param  col      column number 
          */
        public String getColumnName(int col) {
          return columnNames[col];
        }
         /** 
          * Get value at cell (row,col)
          * @param  row      row number 
          * @param  col      column number 
          */
        public Object getValueAt(int row, int col) {
          return data[row][col];
        }
         /** 
          * Check if the Table cell referred to by (row,col) is editable 
          * @param  row      row number 
          * @param  col      column number 
          */
        public boolean isCellEditable(int row, int col) {
          if (col == 0) return false;
          return true;
        }
         /**
          * JTable uses this method to determine the default renderer/
          * editor for each cell.  If we didn't implement this method,
          * then the last column would contain text ("true"/"false"),
          * rather than a check box.
          * @param col    Column number
          */
        public Class getColumnClass(int col) {
          if (col == 0)
            return getValueAt(0, col).getClass();
          else 
            return intType;
        }
         /**
          * Need to implement this method if your table's data can change.
          * @param value   New value at cell (row,col) 
          * @param row     Row    Number
          * @param col     Column number
          */
        public void setValueAt(Object value, int row, int col) {
          // No more true from v1.3, but kept for backward compatibility
          // especially, because I use v1.1.8-ibm remotely
          if (isDebugOn()) {
             System.out.println("Setting value at " + row + "," + col
                            + " to " + value
                            + " (an instance of " 
                           + value.getClass() + ")");
          }
          data[row][col] = value;
          fireTableCellUpdated(row, col);

          if (isDebugOn()) {
            System.out.println("New value of data:");
            printDebugData();
          }
        }
         /** Debug method which prints the contents of the table */
        private void printDebugData() {
          int numRows = getRowCount();
          int numCols = getColumnCount();

          for (int i = 0; i < numRows; i++) {
            System.out.print("    row " + i + ":");
            for (int j = 0; j < numCols; j++) 
              System.out.print("  " + data[i][j]);
            System.out.println();
          }
          System.out.println("--------------------------");
        }
      }
    }
    class ListPanel extends JPanel {
      private JTree tree;
      private JScrollPane treeView;
      private DefaultMutableTreeNode rootNode;
      private DefaultTreeModel treeModel;
      private SvtSpyConfigPanel parent;
      private MyTreeModelListener mtmListener;
      ListPanel(SvtSpyConfigPanel parent) {
        this.parent = parent;
    	buildGUI();
      } 
      protected void buildGUI() {
    	// Create the nodes 
    	rootNode = new DefaultMutableTreeNode("SVT");
        treeModel = new DefaultTreeModel(rootNode);
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
        renderer.setLeafIcon(new ImageIcon(AppConstants.iconDir+"Wizard.png"));
        tree.setCellRenderer(renderer);

    	// Listen to the selection changes
    	tree.addTreeSelectionListener(new TreeSelectionListener() {
    	  public void valueChanged(TreeSelectionEvent e) {
    	    DefaultMutableTreeNode node = 
    	      (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
    	    if (node == null) return;
      
    	    Object nodeInfo = node.getUserObject();
    	    String leafName = nodeInfo.toString();
    	    if (node.isLeaf()) {  
              DefaultMutableTreeNode board = (DefaultMutableTreeNode) node.getParent();
              DefaultMutableTreeNode crate = null;
              if (board != null) crate = (DefaultMutableTreeNode) board.getParent();

              if (crate != null) {
                String [] fields = board.toString().split("-");
                String boardName = fields[0];
                int    slot      = Integer.parseInt(fields[1]);
                if (tablePanel == null) {
                  tablePanel = new BufferTablePanel(crate.toString(), slot, boardName, leafName);
                  parent.splitPane.setRightComponent(tablePanel);
                }
                else {
                  if (isDebugOn()) 
                    System.out.println(crate.toString()+":"+slot+":"+boardName+":"+leafName);
                  tablePanel.setBuffer(crate.toString(), slot, boardName, leafName);
                  tablePanel.updateTableContent();
                }
              }
    	    }
            repaint();
    	  }
    	});
    	// Create a scroll pane and add the tree to it 
    	treeView = new JScrollPane(tree);
        treeView.setPreferredSize(new Dimension(TREEWIDTH, XHEIGHT-10));
    	add(treeView, BorderLayout.CENTER);
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
      protected void updateNodes() {
	createNodes();
        treeModel.reload();
      }
      protected void reloadTree() {
        treeModel.reload();
      }
      protected void addCrate(SvtCrateData crateData) {
        String crateName = crateData.getName();
    	DefaultMutableTreeNode crate = new DefaultMutableTreeNode(crateName);
    	rootNode.add(crate);
	
        SvtBoardData [] boardData = crateData.getBoardData();
        for (int j = 0; j < boardData.length; j++) { 
          String boardName = boardData[j].getType();
          int slot         = boardData[j].getSlot();
          if (boardData[j].getNBuffers() == 0) continue;
      	  DefaultMutableTreeNode board = new DefaultMutableTreeNode(boardName+"-"+slot);
    	  crate.add(board);
	
          SvtBufferData [] bufferData = boardData[j].getBufferData();
          for (int k = 0; k < bufferData.length; k++) {
            String bufferName = bufferData[k].getType();
            DefaultMutableTreeNode buffer = new DefaultMutableTreeNode(bufferName);
    	    board.add(buffer);
          }
        }
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
  }
  public String getKey(final String crate, 
                       int slot, 
                       final String board,
                       final String spybuf, 
                       final String tail) 
  {
     StringBuilder buffer = new StringBuilder(AppConstants.SMALL_BUFFER_SIZE);
     buffer.insert(0, board.toLowerCase());
     StringBuilder tbuf = new StringBuilder(AppConstants.SMALL_BUFFER_SIZE);
     tbuf.insert(0, "w");
     String wedge;
     int index = (slot < 13) ? 0 : 1;
     if (index == 0) {
       if      (crate.equals("b0svt00")) wedge = "00"; 
       else if (crate.equals("b0svt01")) wedge = "02";
       else if (crate.equals("b0svt02")) wedge = "04";
       else if (crate.equals("b0svt03")) wedge = "06";
       else if (crate.equals("b0svt04")) wedge = "08";
       else                              wedge = "10";
     }
     else {
       if      (crate.equals("b0svt00")) wedge = "01";
       else if (crate.equals("b0svt01")) wedge = "03";
       else if (crate.equals("b0svt02")) wedge = "05";
       else if (crate.equals("b0svt03")) wedge = "07";
       else if (crate.equals("b0svt04")) wedge = "09";
       else                              wedge = "11";
     }
     tbuf.append(wedge);
     if (board.equals("HF")) {
       if      (slot == 4 || slot == 13 ) tbuf.append("_b0b1");
       else if (slot == 5 || slot == 14 ) tbuf.append("_b2b3");
       else                               tbuf.append("_b4b5");
     }
     buffer.append("_");
     buffer.append(tbuf.toString());
     buffer.append("_");
     String buffId;
     String [] fields = spybuf.split("_");

     if (board.equals("HF") && spybuf.indexOf("OUT") == -1) 
       buffId = fields[fields.length-1];
     else if (board.equals("TF"))
       buffId = fields[1].substring(0,1);
     else 
       buffId = fields[1];

     buffer.append(buffId.toLowerCase());
     buffer.append(tail);
     return buffer.toString();
  }
  /** Test the class standalone */
  public static void main(String [] argv) {
    SvtCrateConfig frame = new SvtCrateConfig(true);
    System.out.println(frame.getKey("b0svt05", 11, "HB", "out", "_spy"));
    frame.setSize(800, 600);
    frame.setVisible(true);
  }
}
