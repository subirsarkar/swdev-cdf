package config.db;

import java.util.*;
import java.io.*;
import java.util.zip.CRC32;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.table.*;

import java.sql.*;

import config.util.*;

public class SvtDbBrowser extends DataFrame {
  private static final int INIT_SIZE = 200;
  private static final ImageIcon fcIcon = new ImageIcon(AppConstants.iconDir+"closed.gif");
  private static final ImageIcon fsIcon = new ImageIcon(AppConstants.iconDir+"opened.gif");
  private static final Class intType = (new Integer(1)).getClass();
  private Vector<String> vec       = new Vector<String>(INIT_SIZE);
  private Vector mapsetVec = new Vector(INIT_SIZE);
  private Vector hwsetVec  = new Vector(INIT_SIZE);
  private Vector<String> activeVec = new Vector<String>(INIT_SIZE);
  private DbBrowserPanel brPanel;
  private JSplitPane splitPane; 
  private static final Dimension medSize  = new Dimension(300, 100);
  public final static int TOTALWIDTH = 600;
  public final static int TREEWIDTH  = 130;
  public final static int XHEIGHT    = 230;
  private static final Font cfont    = new Font("SansSerif", Font.PLAIN, 12);
  private static int prevRun = 0;
  public static final String [] nodeNames = 
    {
       "General", 
       "Pattern", 
       "SSMap", 
       "Fcon",
       "IFit"
    };
  public static final HashMap<String, String> xMap = new HashMap<String,String>();
  static {
    xMap.put(nodeNames[1], ".patt");
    xMap.put(nodeNames[2], ".ss");
    xMap.put(nodeNames[3], ".fcon");
    xMap.put(nodeNames[4], ".ifit");
  }

  String mapsetName = new String();
  String mapsetCrc  = new String();
  String hwsetName  = new String();
  String hwsetCrc   = new String();
  String hwTS       = new String();
  String mapTS      = new String();

  public SvtDbBrowser(boolean salone) {
    super(salone, "SVT Database Browser", true, true, -1);
    loadDB();

    buildGUI();

    // Set Help file
    String filename = Tools.getEnv("SVTMON_DIR")+"/help/a_SvtDbBrowser.html";
    setHelpFile(filename, "About SVT Database Browser", new Dimension(540, 500));
  }
  protected void exitApp() {
    SvtDb.getInstance().dropConnection();
    super.exitApp();
  }
  protected void closeFrame() {
    SvtDb.getInstance().dropConnection();
    super.closeFrame();
  }
  protected void loadDB() {
    try {
      readDB();
    }
    catch (SQLException sqle) {
      sqle.printStackTrace();
    }
    catch (IOException ex) {
      ex.printStackTrace();
    }
  }
  protected void buildGUI() {
    addToolBar();

    JPanel panel = new JPanel(new BorderLayout());

    brPanel = new DbBrowserPanel();

    TextPanel textPanel = getTextPanel();
    textPanel.setBorder(Tools.etchedTitledBorder(" Message Logger "));
    textPanel.setPreferredSize(medSize);

    splitPane = Tools.createSplitPane(JSplitPane.VERTICAL_SPLIT, brPanel, textPanel);
    panel.add(splitPane, BorderLayout.CENTER);

    getContentPane().add(panel, BorderLayout.CENTER);  // Error Panel + Message area
    addStatusBar();
  }
  public void updateGUI() {
    brPanel.listPanel.setTreeIndex(1);
    brPanel.splitPane.remove(brPanel.splitPane.getRightComponent());
    brPanel.configPanel.setData();
    brPanel.splitPane.setRightComponent(brPanel.configPanel);

    // Once again, very rough, but make a working version first
    brPanel.listViewPanel.setActiveData();
    brPanel.listViewPanel.setItems(activeVec);
  }
  class ListViewPanel extends JPanel {
    protected MutableList list;
    protected JPopupMenu popup;
    protected JCheckBoxMenuItem checkBox;
    ListViewPanel() {
      super(true);
      setLayout(new BorderLayout());
      setBorder(Tools.etchedTitledBorder(" Opreations "));
  
      setActiveData();  // Fill activeData properly
  
      // List View
      list = new MutableList();
      list.setFont(cfont);
      list.setCellRenderer(new MyCellRenderer(fsIcon, fcIcon));
      list.setVisibleRowCount(6);
      setItems(activeVec);

      JScrollPane scp = new JScrollPane(list);
  
      add(scp, BorderLayout.CENTER);
  
      popup = addPopup();

      // Add listener to components that can bring up popup menus.
      MouseListener popupListener = new PopupListener(popup);
      list.addMouseListener(popupListener);
      scp.addMouseListener(popupListener);

      // Buttons 
      JPanel panel = new JPanel(new BorderLayout());
      panel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
      add(panel, BorderLayout.EAST);
  
      JPanel p = new JPanel(new GridLayout(4, 1, 0, 3));
      panel.add(p, BorderLayout.NORTH);

      JButton button = Tools.createButton("Show File");
      button.setPreferredSize(new Dimension(120, 22));
      p.add(button);
      button.addActionListener( new ActionListener() {
        
        public void actionPerformed(ActionEvent e) {
          if (list.getSelectedIndex() == -1) {
            warn("WARNING: Select a file first!", Color.red);
            return;
          }
          if (list.getSelectionMode() == ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
               && list.getSelectedIndices().length > 1) {
            warn("INFO: For simplicity, only one file at a time can be displayed, skipping ...", 
                           Color.red);
            warn("\tSelect single_selection_mode from the PopUp menu ...", Color.red);
            return;
          }

          final String file = list.getSelectedValue().toString(); 
          if (file.indexOf(".patt") != -1) {
            warn("WARNING: Pattern files are too large to display, save in a file, skipping ...", Color.red);
            return;
          }
          Thread runner = new Thread() {
            public void run() {
              try {
                showFile(file); // Show file content
              }
              catch (SQLException sqle) {
                sqle.printStackTrace();
              }
              catch (IOException ex) {
                ex.printStackTrace();
              }
            }
          };
          runner.start();
        }  
      });
  
      button = Tools.createButton("Save File(s)");
      button.setPreferredSize(new Dimension(120, 22));
      p.add(button);
      button.addActionListener( new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          Thread runner = new Thread() {
            public void run() {
              Object [] files = list.getSelectedValues();
              int nfiles = files.length;
              for (int i = 0; i < nfiles; i++) {
                String file = files[i].toString();
                setStatusText("Saving " + file + " (" + (i+1) + "/" + nfiles + ")");
                try {
                  saveFile(file); // Save file content
                }
                catch (SQLException sqle) {
                  sqle.printStackTrace();
                }
                catch (IOException ex) {
                  ex.printStackTrace();
                }
              }
              setStatusText("Ready ...");
            }
          };
          runner.start();
        }  
      });

      button = Tools.createButton("Save All ...");
      button.setPreferredSize(new Dimension(120, 22));
      p.add(button);
      button.addActionListener( new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          Thread runner = new Thread() {
            public void run() {
              int nfiles = list.getModel().getSize();
              for (int i = 0; i < nfiles; i++) {
                String file = list.getModel().getElementAt(i).toString(); 
                setStatusText("Saving " + file + " (" + (i+1) + "/" + nfiles + ")");
                try {
                  saveFile(file); // Save file content
                }
                catch (SQLException sqle) {
                  sqle.printStackTrace();
                }
                catch (IOException ex) {
                  ex.printStackTrace();
                }
              }
              setStatusText("Ready ...");
            }
          };
          runner.start();
        }  
      });

      button = Tools.createButton("Validate");
      button.setPreferredSize(new Dimension(120, 22));
      p.add(button);
      button.addActionListener( new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          Thread runner = new Thread() {
            public void run() {
              Object [] files = list.getSelectedValues();
              int nfiles = files.length;
              for (int i = 0; i < nfiles; i++) {
                String file = files[i].toString();
                setStatusText("Validating " + file + " (" + (i+1) + "/" + nfiles + ")");
                try {
                  long crc = BillCRC.compute(file); // Calculate Checksum Bill's way
                  warn(file + ": CRC = " + crc, Color.blue);
                }
                catch (SQLException sqle) {
                  sqle.printStackTrace();
                  continue;
                }
                catch (IOException ex) {
                  ex.printStackTrace();
                }
              }
              setStatusText("Ready ...");
            }
          };
          runner.start();
        }  
      });
    }
    public void setActiveData() {
      activeVec.removeAllElements();
      activeVec.addElement(mapsetName);
      activeVec.addElement(hwsetName);
      for (Iterator<String> it = vec.iterator(); it.hasNext(); )
        activeVec.addElement(it.next());
    }
    public void setItems(final Vector<String> vec) {
      DefaultListModel model = list.getContents();
      model.clear();
      for (Iterator<String> it = vec.iterator(); it.hasNext(); )
        model.addElement(it.next());
    }
    protected JPopupMenu addPopup() {
      // Add popup menu
      JPopupMenu popup = new JPopupMenu();

      checkBox = new JCheckBoxMenuItem("Single Selection Mode", false);
      checkBox.addActionListener(new ActionListener() {
    	public void actionPerformed(ActionEvent e) {
    	   if (checkBox.isSelected()) 
                 list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    	   else  list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    	}
      });
      popup.add(checkBox);

      JMenuItem item = new JMenuItem("Select All");
      item.addActionListener(new ActionListener() {
    	public void actionPerformed(ActionEvent e) {
    	  list.setSelectionInterval(0, list.getModel().getSize()-1);
    	}
      });
      popup.add(item);

      item = new JMenuItem("Clear Selection");
      item.addActionListener(new ActionListener() {
    	public void actionPerformed(ActionEvent e) {
    	  list.clearSelection();
    	}
      });
      popup.add(item);

      popup.addSeparator();
    
      ButtonGroup group = new ButtonGroup();

      JRadioButtonMenuItem rbItem 
         = Tools.radioButtonItem("Show all Mapset files", group, 
                                false, Color.black, KeyEvent.VK_M, 0);
      rbItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          try {
            Vector<String> vec = SvtDb.getInstance().getConfigFiles("mapset");
            setItems(vec);
          }
          catch (SQLException ex) {
            ex.printStackTrace();
          }
          catch (IOException ex) {
            ex.printStackTrace();
          }
        }
      });
      popup.add(rbItem);

      rbItem = Tools.radioButtonItem("Show all Hwset files", group, 
                                false, Color.black, KeyEvent.VK_H, 0);
      rbItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          try {
            Vector<String> vec = SvtDb.getInstance().getConfigFiles("hwset");
            setItems(vec);
          }
          catch (SQLException ex) {
            ex.printStackTrace();
          }
          catch (IOException ex) {
            ex.printStackTrace();
          }
        }
      });
      popup.add(rbItem);

      rbItem = Tools.radioButtonItem("Show Active Configuration", group, 
                                true, Color.black, KeyEvent.VK_C, 0);
      rbItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          setItems(activeVec);
        }
      });
      popup.add(rbItem);

      return popup;
    }
  }
  class ButtonPanel extends JPanel {
    WordPanel runPanel;
    JButton reloadB;
    ButtonPanel() {
      super(true);
      setLayout(new FlowLayout());
  
      runPanel = new WordPanel("Run #", " ", 100, 12, JTextField.RIGHT);
      add(runPanel);

      JButton button = Tools.createButton("ReadDB");
      button.addActionListener( new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          String text = runPanel.getText().trim();
          if (text == null || text.equals("")) {
            warn("WARNING: Please specify a valid Run Number first!", Color.red);
            return;
          }
          
          int run = Integer.parseInt(text);
          if (run == prevRun) return;
          prevRun = run;
          try {
            readDB(run); // Read DB info for the run number specified
          }
          catch (SQLException ex) {
            ex.printStackTrace();
          }
          catch (IOException ex) {
            ex.printStackTrace();
          }
          reloadB.setEnabled(true);
        }         
      });
      add(button);

      reloadB = Tools.createButton("Reload Active");
      reloadB.addActionListener( new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          loadDB(); 
          updateGUI();
          reloadB.setEnabled(false);
          prevRun = 0;
        }         
      });
      reloadB.setEnabled(false);
      add(reloadB);
    }
  }  
  class DbBrowserPanel extends JPanel {
    private ButtonPanel buttonPanel;
    protected JSplitPane splitPane;
    protected ListPanel listPanel;
    protected TablePanel tablePanel;
    protected ConfigPanel configPanel;
    protected ListViewPanel listViewPanel;

    DbBrowserPanel() {
      super(true);
      buildGUI();
    } 
    protected void buildGUI() {
      setLayout(new BorderLayout());

      JPanel panel = new JPanel(new BorderLayout());
      panel.setBorder(Tools.etchedTitledBorder(" View "));
      add(panel, BorderLayout.NORTH);

      // create the Crate List Panel 
      listPanel = new ListPanel(this);
      listPanel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

      configPanel = new ConfigPanel();
      configPanel.setData();

      // Add the scroll panes to a split pane
      splitPane = Tools.createSplitPane(JSplitPane.HORIZONTAL_SPLIT, listPanel, configPanel);
      listPanel.setTreeIndex(1);

      Dimension minSize = listPanel.getScrollPane().getPreferredSize();
      configPanel.setMinimumSize(minSize);
      listPanel.setMinimumSize(minSize);

      splitPane.setDividerLocation(TREEWIDTH);
      splitPane.setPreferredSize(new Dimension(TOTALWIDTH, XHEIGHT));
      panel.add(splitPane, BorderLayout.CENTER);

      JPanel px = new JPanel(new BorderLayout());
      panel.add(px, BorderLayout.SOUTH);

      buttonPanel = new ButtonPanel();
      buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 5));
      px.add(buttonPanel, BorderLayout.EAST);

      listViewPanel = new ListViewPanel();
      add(listViewPanel, BorderLayout.CENTER);
    }
    protected JSplitPane getSplitPanel() {
      return splitPane;
    }
    class ConfigPanel extends JPanel {
      final String [] labels = {"Mapset", " Hwset"};
      final int [] gaps = {4, 8};
      private SetPanel [] setPanel = new SetPanel[labels.length];
      TextPanel tp = new TextPanel();
      ConfigPanel() {
    	super(true);
    	setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
    	setLayout(new BorderLayout());
    	JPanel panel = new JPanel(new GridLayout(2, 1, 0, 2));
    	add(panel, BorderLayout.NORTH);
    
    	for (int i = 0; i < labels.length; i++) {
    	  setPanel[i] = new SetPanel(labels[i], 290, 80, 
    	      gaps[i], ((i == 0) ? true : false)); // width of the two fields, gap
    	  panel.add(setPanel[i]);
    	}

        tp.setPreferredSize(new Dimension(TOTALWIDTH-TREEWIDTH, 200));
        tp.setBorder(BorderFactory.createEmptyBorder(20, 10, 0, 10));
        add(tp, BorderLayout.CENTER);
      }
      public void setData() {
    	setPanel[0].setFilename(mapsetName);
    	setPanel[0].setCRC(mapsetCrc);
    	setPanel[1].setFilename(hwsetName);
    	setPanel[1].setCRC(hwsetCrc);

        tp.clear();
        tp.warn("-> " + mapsetName + "\n\t added @ " + mapTS);
        tp.warn("-> " + hwsetName + " \n\t added @ " + hwTS);
      }
      class SetPanel extends JPanel {
    	private WordPanel wordPanel;
    	private JTextField crcTF;
    	SetPanel (String titl, int setWidth, int crcWidth, int gap, boolean isSel) {
    	  super(true);
    	  setLayout(new BorderLayout());
    	    
    	  JPanel panel = new JPanel();
    	  add(panel, BorderLayout.WEST);
    	    
    	  wordPanel = new WordPanel(titl, " ", setWidth, gap, JTextField.RIGHT);
    	  wordPanel.setEditable(false);
    	  panel.add(wordPanel);
    
    	  crcTF = new JTextField();
          crcTF.setBackground(Color.white);
          crcTF.setBorder(BorderFactory.createLoweredBevelBorder());
    	  crcTF.setEditable(false);
    	  crcTF.setPreferredSize(new Dimension(crcWidth, 25));
    	  crcTF.setHorizontalAlignment(JTextField.RIGHT);
    	  crcTF.setFont(cfont);
    	  panel.add(crcTF);
    	}
    	public String getFilename() {
          return wordPanel.getText();
    	}
    	public void setFilename(String name) {
    	  wordPanel.setText(name);
    	}
    	public String getCRC() {
    	  return crcTF.getText();
    	}
    	public void setCRC(String crc) {
    	  crcTF.setText(crc);
    	}
      }
    }
    class TablePanel extends JPanel {
      final String [] columnNames = new String[] {"Wedge", "Filename"};
        /** User defined data model to be used by the table */
      private MyTableModel dataModel;
        /** The JTable UI component */
      private JTable table = null;
      private String [] keys;
      private String [] values;

      TablePanel(String [] tags) {
        super(true);

        keys = new String[tags.length];
        for (int i = 0; i < keys.length; i++)
          keys[i] = String.valueOf(i);

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.white);
        add(panel, BorderLayout.CENTER);

        fillData(tags);

        // Decide what to do when the table content changes
        dataModel = new MyTableModel(columnNames, keys, values); 
        dataModel.addTableModelListener(new TableModelListener() {
          public void tableChanged(TableModelEvent ev) {
            int row    = ev.getFirstRow();
            int column = ev.getColumn();
            String columnName = dataModel.getColumnName(column);
          }
        });
        table = new JTable(dataModel); 
        table.setFont(cfont);
    
        // Different width of column
        TableColumn column = null;
        for (int i = 0; i < columnNames.length; i++) {
          column = table.getColumnModel().getColumn(i);
          if (i == 0) {
            column.setPreferredWidth(50);
          } else {
            column.setPreferredWidth(300);
          }
        }
        table.setPreferredScrollableViewportSize(new Dimension(TOTALWIDTH-TREEWIDTH, XHEIGHT)); 
        JScrollPane scrollPane = new JScrollPane(table); 
        panel.add(scrollPane, BorderLayout.CENTER); 
      }
      protected void updateContent(String [] tags) {
        fillData(tags);
        dataModel.setTableContent(keys, values);
        repaint();
      }
      protected void fillData(String [] tags) {
        values = new String[tags.length];
        System.arraycopy(tags, 0, values, 0, values.length);
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
          if (values.length > 0) {
            for (int i = 0; i < data.length; i++) {
              for (int j = 0; j < data[i].length; j++) {
                data[i][j] = (j == 0) ? keys[i] : values[i];
              }
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
          return false;
        }
         /**
          * JTable uses this method to determine the default renderer/
          * editor for each cell.  If we didn't implement this method,
          * then the last column would contain text ("true"/"false"),
          * rather than a check box.
          * @param col    Column number
          */
        public Class getColumnClass(int col) {
          //return getValueAt(0, col).getClass();
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
            for (int j = 0; j < numCols; j++) {
              System.out.print("  " + data[i][j]);
            }
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
      private MyTreeModelListener mtmListener;

      private DbBrowserPanel parent;
      ListPanel(DbBrowserPanel parent) {
        super(true);
        this.parent = parent;
    	buildGUI();
      } 
      protected void buildGUI() {
    	// Create the nodes 
    	rootNode    = new DefaultMutableTreeNode("SVTDB");
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
        renderer.setLeafIcon(new ImageIcon(AppConstants.iconDir+"mini-ball.gif"));
        tree.setCellRenderer(renderer);

    	// Listen to the selection changes
    	tree.addTreeSelectionListener(new TreeSelectionListener() {
    	  public void valueChanged(TreeSelectionEvent e) {
    	    DefaultMutableTreeNode node = 
    	      (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
    	    if (node == null) return;
      
    	    if (node.isLeaf()) {  
              parent.splitPane.remove(parent.splitPane.getRightComponent());
              if (node.toString().equals("General")) {
                parent.splitPane.setRightComponent(configPanel);
              }
              else {
                String [] tags  = getFilenames(vec, xMap.get(node.toString()));
                // Temporary solution, think better
                String [] ntags = new String[0];
                if (isDebugOn()) System.out.println("Len = " + tags.length);
                if (node.toString().equals("Fcon")) {
                   ntags = new String[tags.length/6];
                   int k = 0;
                   for (int j = 0; j < tags.length; j++) {
                     if (j%6 == 0) ntags[k++] = tags[j];
                   }
                   tags = ntags;
                }
                if (tablePanel == null) {
                  tablePanel = new TablePanel(tags);
                }
                else {
                  tablePanel.updateContent(tags);
                }
                parent.splitPane.setRightComponent(tablePanel);
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
      protected String [] getFilenames(Vector<String> vec, final String tag) {
        int nElem = 0;
        for (Iterator<String> it = vec.iterator(); it.hasNext(); ) {
          String name = it.next();
          if (isDebugOn()) System.out.println(name + "/" + tag);
          if (name.indexOf(tag) != -1) nElem++;
        }
	String [] elems = new String[nElem];
        int i = 0;
        for (Iterator<String> it = vec.iterator(); it.hasNext(); )
          elems[i++] = it.next();

        return elems;
      }
      protected void createNodes() {
        for (int i = 0; i < nodeNames.length; i++) {
      	  DefaultMutableTreeNode node = new DefaultMutableTreeNode(nodeNames[i]);
    	  rootNode.add(node);
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
      public void setTreeIndex(int index) {
        tree.setSelectionRow(index);
      }
    }
  }
  /**
   * Retrive hwset & mapset from hdwdb
   * first get name/crc 
   */
  public void readDB() throws SQLException, IOException {
    warn("Reading Active SVT configuration from Database ...", Color.green);

    SvtDb dbh = SvtDb.getInstance();

    String dbvalue = dbh.getConfig("mapset");
    mapTS = dbh.getTS(dbvalue);
    getMapName(dbvalue);
    
    dbvalue = dbh.getConfig("hwset");
    hwTS = dbh.getTS(dbvalue);
    getHwName(dbvalue);

    readContent();
  }
  /**
   * Retrive hwset & mapset from hdwdb
   * first get name/crc 
   */
  public void readDB(int run) throws SQLException, IOException {
    warn("Reading SVT configuration from Database for run = " + run, Color.green);

    SvtDb dbh = SvtDb.getInstance();
    String dbvalue = dbh.getConfigFor(run, "map@%");
    mapTS = dbh.getTS(dbvalue);
    getMapName(dbvalue);
    
    dbvalue = dbh.getConfigFor(run, "hw@%");
    hwTS = dbh.getTS(dbvalue);
    getHwName(dbvalue);

    readContent();

    updateGUI();
  }
  public void getMapName(final String line) {
    String [] fields = line.split("/");
    if (fields.length != 2)
      throw new RuntimeException("Does not find both the fields, len = " + fields.length);
   
    mapsetName = fields[0];
    mapsetCrc  = fields[1];
    if (isDebugOn()) System.out.println("SvtDbBrowser: mapsetName = " + mapsetName 
                                                          + " crc = " + mapsetCrc);
  } 
  public void getHwName(final String line) {
    String [] fields = line.split("/");
    if (fields.length != 2)
      throw new RuntimeException("Does not find both the fields, len = " + fields.length);

    hwsetName = fields[0];
    hwsetCrc  = fields[1];
    if (isDebugOn()) System.out.println("SvtDbBrowser: hwsetName = " + hwsetName 
                                                         + " crc = " + hwsetCrc);
  }
  public void readContent() throws SQLException, IOException  {
    // Now get the actual BLOBs
    String mapset = SvtDb.getInstance().getData(mapsetName);

    vec.removeAllElements();
    String [] lines = mapset.split("\\n");
    for (int i = 0; i < lines.length; i++) {
      if (lines[i].indexOf("pattFile") != -1 || 
          lines[i].indexOf("ssFile")   != -1 ||
          lines[i].indexOf("fconFile") != -1 ||
          lines[i].indexOf("ifitFile") != -1) {

        String [] fields = Tools.split(lines[i]);  // two fields separated by blank space(s)
        if (fields.length != 2)
           throw new RuntimeException("Does not find both the fields, len = " 
                 + fields.length + ": " + lines[i]);
        vec.addElement(fields[1]);
      }
    }
  }
  public void showFile(final String filename) throws SQLException, IOException {
    String data = SvtDb.getInstance().getData(filename);
    if (data == null) return;
        
    DataFrame f = new DataFrame(false, filename, true);
    f.displayText(data);
    f.setSize(600, 630);
    f.setVisible(true);
  }
  public static void saveFile(final String filename) throws SQLException, IOException {
    String data = SvtDb.getInstance().getData(filename);
    if (data == null) return;

    Tools.writeFile(filename, data);
  }
  public void calculateCRC(final String filename) throws SQLException, IOException {
    String data = SvtDb.getInstance().getData(filename);
    String [] lines = data.split("\\n");
    CRC32 checksum = new CRC32();
    for (int i = 0; i < lines.length; i++)
      checksum.update(lines[i].getBytes());
    warn(filename + ": CRC = " + Long.toString(checksum.getValue()), Color.blue);
  }
  public static void main(String [] argv) {
    JFrame f = new SvtDbBrowser(true);
    f.setSize(620, 650);
    f.setVisible(true);
  } 
}
