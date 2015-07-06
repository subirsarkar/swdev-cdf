package config;

import java.io.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import com.smartsockets.*;

import config.svt.*;
import config.util.*;

/**
 * <P>
 * This is a simple tool to send SmartSockets command mesasges from a GUI.
 * <UL>
 *   <LI>Start/Stop/Suspend/Resume/Continue message</LI>
 * </UL>
 *
 * @author   S. Sarkar
 * @version  0.9, April 24, 2001
 */

public class SendCommandFrame extends ConfigDataFrame {
  private static final Class intType = (new Integer(1)).getClass();
  private JSplitPane splitPane;
  private SendCommandPanel sendPanel;
  private static final Dimension longSize = new Dimension(250, 100);
  private static final Dimension medSize  = new Dimension(110, 25);
  private static final Dimension smaSize  = new Dimension(85, 25);
  protected TipcSrv srv;
    /** Constructor 
     *  @param srv     The RT Server
     */
  public SendCommandFrame(TipcSrv srv, boolean standalone) {
    super(standalone, "Command Message Sender");
    this.srv = srv;
    buildGUI();

    String filename = Tools.getEnv("SVTMON_DIR")+"/help/a_SendCommand.html";
    setHelpFile(filename, "About SVT Command Message Sender", new Dimension(500, 400));
  }
    /** Prepare UI */
  protected void buildGUI() {
    Container content = getContentPane();
    content.add(getToolBar(), BorderLayout.NORTH);

    JPanel panel = new JPanel(new BorderLayout());
    content.add(panel, BorderLayout.CENTER);

    // create the Comand Panel 
    sendPanel = new SendCommandPanel();

    TextPanel textPanel = getTextPanel();
    textPanel.setBorder(Tools.etchedTitledBorder(" Message Logger "));
    textPanel.setPreferredSize(longSize);

    splitPane = Tools.createSplitPane(JSplitPane.VERTICAL_SPLIT, sendPanel, textPanel);
    panel.add(splitPane, BorderLayout.CENTER);

    addStatusBar();
  }
  protected void loadDefaultProperties() {
    String filename = Tools.getEnv("SVTMON_DIR")+"/crate_config.prop";
    loadProperties(filename);    
  }
  class SendCommandPanel extends JPanel {
    private CommandPanel commPanel;
    private ConfigPanel confPanel;
    SendCommandPanel() {
      super(true);
      setLayout(new BorderLayout());
      setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 0));
      JPanel panel = new JPanel(new BorderLayout());
      add(panel, BorderLayout.CENTER);

      commPanel = new CommandPanel();      
      confPanel = new ConfigPanel();      
      panel.add(commPanel, BorderLayout.NORTH);
      panel.add(confPanel, BorderLayout.CENTER);
    }
  }
  class CommandPanel extends JPanel {
    private ComboPanel destPanel;
    CommandPanel() {
      setLayout(new BorderLayout());
      setBorder(BorderFactory.createEmptyBorder(2, 2, 1, 1));    	  

      destPanel = new ComboPanel(getDestinations("command"), "Dest", 10);
           // (top, left, bottom, right) 
      destPanel.setBorder(BorderFactory.createEmptyBorder(5, 1, 10, 0));

      JPanel p1 = new JPanel(new BorderLayout());
      p1.add(destPanel, BorderLayout.WEST);
      add(p1, BorderLayout.NORTH);

      JPanel aPanel = new JPanel(new GridLayout(2,5, 5, 5));
      aPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 10, 2));
      add(aPanel, BorderLayout.CENTER);

      final String[] names =  {
         "pause",
         "resume",
         "stop",
         "reset",
         "configure",
         "trigger",
         "sendStatus",
         "sendHist",
         "resetErrot",
         "resetHist"
      };

      final String [] tips = {
         "Pause Monitoring", 
         "Resume Monitoring Session",
         "Stop monitoring",
         "Reset",
         "Read configuration from file",
         "Trigger a monitoring cycle",
         "Send Status Messages",
         "Send Histogram messages",
         "Reset Board/Spy-buffer Errors",
         "Reset histogram contents"
      };

      JButton button;
      for (int i = 0; i < names.length; i++) {
         button = Tools.createButton(names[i]);
         final String command = names[i];
         button.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent ev) {
             String dest    = destPanel.getSelectedItem();
             warn("-> " + dest + " " + command + " ("+new Date()+")", Color.blue);
             if (!isDebugOnly()) Tools.sendCommand(srv, dest, command, false);
           }
         });
         button.setPreferredSize(medSize);
         button.setToolTipText(tips[i]);
         aPanel.add(button);
      }
    }
  }
  class ConfigPanel extends JPanel {
    private JPanel tablePanel;
    private JPanel buttonPanel;
    public ConfigPanel() {
      super(true);
      setLayout(new BorderLayout());
      setBorder(BorderFactory.createEmptyBorder(10, 2, 1, 1));    	  
      loadDefaultProperties();
      tablePanel  = new TablePanel();

      JPanel panel = new JPanel(new BorderLayout());
      buttonPanel = new ButtonPanel();
      panel.add(buttonPanel, BorderLayout.WEST);

      add(tablePanel, BorderLayout.CENTER);
      add(panel, BorderLayout.SOUTH);
    }
    class TablePanel extends JPanel {
      String [] pNames  = new String[props.size()];
      String [] pValues = new String[props.size()];
      final String [] columnNames = new String[] {"Property", "Value"};
        /** User defined data model to be used by the table */
      private MyTableModel dataModel;
        /** The JTable UI component */
      private JTable table;
      public TablePanel() {
        super(true);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(3, 1, 3, 1));
      	  
        JPanel panel = new JPanel(new BorderLayout());
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
      
        // The second column should be wider
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
      
        table.setPreferredScrollableViewportSize(new Dimension(400, 150));
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane);
        add(panel, BorderLayout.NORTH);
      }
      protected void fillData() {
        int i = 0;
        for (Enumeration e = props.propertyNames(); e.hasMoreElements();) {
           String key = (String)e.nextElement();
           String val = props.getProperty(key);
           pNames[i]  = key;
           pValues[i] = val;
           i++;
        }
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
    class ButtonPanel extends JPanel {
      ComboPanel destPanel;
      ButtonPanel() {
    	setLayout(new FlowLayout());
    	destPanel = new ComboPanel(SendCommandFrame.getDestinations("config"), "Dest", 10);
    	add(destPanel);
    
    	JButton button = Tools.createButton("Reset");
        button.setPreferredSize(smaSize);
    	add(button);
    	button.addActionListener( new ActionListener() {
    	  public void actionPerformed(ActionEvent e) {
    	    resetProperties();
    	  }         
    	});
    
    	button  = Tools.createButton("Update");
        button.setPreferredSize(smaSize);
    	add(button);
    	button.addActionListener( new ActionListener() {
    	public void actionPerformed(ActionEvent e) {
    	    updateProperties();
    	  }  
    	});
    
    	button  = Tools.createButton("Send");
        button.setPreferredSize(smaSize);
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
    	  submsg[index].appendStr(transProp.getProperty(key));
    	  index++;
    	}
    	msg.appendMsgArray(submsg);
    	if (isDebugOn() || isDebugOnly())  
           Tools.showMessage(SendCommandFrame.this, msg);
      
    	if (isDebugOnly()) return;
    
    	String dest = destPanel.getSelectedItem();
    	msg.setDest(dest);
    
    	srv.send(msg);
    	srv.flush();
    	msg.destroy();
    
    	// Now merge the two proerties, i.e update the original one
    	updateProperties();
    
    	// Start afresh the next time around with the temporary property
    	transProp.clear();
      }
    }
  }
  /** Test the class standalone */
  public static void main(String [] argv) {
    SendCommandFrame f = new SendCommandFrame(Tools.getServer(), true);
    f.setSize(630, 570);
    f.setVisible(true);
  }
}
