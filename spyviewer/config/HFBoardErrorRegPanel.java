package config;

import java.util.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import config.svt.*;
import config.util.*;

/** This panel holds a summary information of the occurrance of 
 *  HF Board Error Registers.
 */
public class HFBoardErrorRegPanel extends JPanel {
  private static final boolean DEBUG = false;
  private static final Integer typeTest = new Integer(1);

    /** User defined data model to be used by the table */
  private MyTableModel dataModel;
    /** The JTable UI component */
  private JTable table;

  // Constructor
  public HFBoardErrorRegPanel(final String crate, final String board, int slot) {
    setLayout(new BorderLayout());
    setBorder(BorderFactory.createEtchedBorder());
    setBorder(Tools.etchedTitledBorder(" Error Regs "));
    
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
    int [] errReg = new int[0];
    int [] errTot = new int[0];

    SvtCrateMap map        = SvtCrateMap.getInstance();
    SvtCrateData crateData = map.getCrateData(crate);
    if (crateData != null) {
      errReg = crateData.getBoardData(board, slot).getErrorRegisters();
      errTot = crateData.getBoardData(board, slot).getErrorCounters();

      dataModel = new MyTableModel(errReg, errTot);
      table     = new JTable(dataModel);
      table.setPreferredScrollableViewportSize(new Dimension(500, 165));
      JScrollPane scrollPane = new JScrollPane(table);
      panel.add(scrollPane);

      if (DEBUG) {
        table.addMouseListener(new MouseAdapter() {
          public void mouseClicked(MouseEvent e) {
            dataModel.printDebugData();
          }
        });
      }
      add(panel, BorderLayout.CENTER);
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
    protected final String [] columnNames = SvtCratesFrame.berrMap.get("HF");
      /** Application specific data matrix */
//    protected Object [][] data = new Object[10][5];
    protected Object [][] data = new String[10][5];

    public MyTableModel(int [] errReg, int [] errTot) {
       int i, j;

       int [] errBit = new int[errTot.length];
       for (i = 0; i < 10; i++) 
         if ( (errReg[0] >> i & 0x1) > 0 ) errBit[i] = 1;

       for (i = 0; i < 6; i++) 
         if ( (errReg[0] >> (i+10) & 0x1) > 0 ) errBit[10+i] = 1;

       for (i = 6; i < 10; i++) 
         if ( (errReg[1] >> (i-6) & 0x1) > 0 ) errBit[i+16] = 1;

       for (i = 0; i < 10; i++) 
         if ( (errReg[1] >> (i+4) & 0x1) > 0 ) errBit[i+20] = 1;

       for (i = 0; i < 2; i++) 
         if ( (errReg[1] >> (i+14) & 0x1) > 0 ) errBit[i+30] = 1;

       for (i = 2; i < 10; i++) 
         if ( (errReg[2] >> (i-2) & 0x1) > 0 ) errBit[i+32] = 1;

       for (i = 0; i < 4; i++) 
         if ( (errReg[2] >> (i+8) & 0x1) > 0) errBit[i+40] = 1;

       for (i = 0; i < data.length; i++) 
         data[i][0] = errBit[i] + "/" + errTot[i];

       for (i = 0; i < data.length; i++) 
         data[i][1] = errBit[i+10] + "/" + errTot[i+10];

       for (i = 0; i < data.length; i++) 
         data[i][2] = errBit[i+20] + "/" + errTot[i+20];
        
       for (i = 0; i < data.length; i++) 
         data[i][3] = errBit[i+30] + "/" + errTot[i+30];

       for (i = 0; i < data.length; i++) 
         data[i][4] = (i < 4) ? errBit[i+40] + "/" + errTot[i+40] : " ";
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
       * editor for each cell depending on the data type.  
       * If we didn't implement this method,
       * then the last column would contain text ("true"/"false"),
       * rather than a check box for a boolean, for example.
       * 
       * @param col    Column number
       */
     public Class getColumnClass(int col) {
       //return getValueAt(0, col).getClass();
       // Forced to return an integer renderer even if the data type is String
       return typeTest.getClass();
     }
     /**
      * Need to implement this method if your table's data can change.
      * @param value   New value at cell (row,col) 
      * @param row     Row    Number
      * @param col     Column number
      */
     public void setValueAt(Object value, int row, int col) {
       // No more true form v1.3, but kept for backward compatibility
       // especially, because I use v1.1.8-ibm remotely
       if (DEBUG) {
          System.out.println("Setting value at " + row + "," + col
                         + " to " + value
                         + " (an instance of " 
                         + value.getClass() + ")");
       }
       if (data[0][col] instanceof Integer && !(value instanceof Integer)) {           
         try {
           data[row][col] = new Integer(value.toString());
           fireTableCellUpdated(row, col);
         } catch (NumberFormatException e) {
           JOptionPane.showMessageDialog(null,
             "The \"" + getColumnName(col)
             + "\" column accepts only integer values.");
         }
       } else {
         data[row][col] = value;
         fireTableCellUpdated(row, col);
       }

       if (DEBUG) {
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
