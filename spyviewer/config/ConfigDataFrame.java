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

public class ConfigDataFrame extends DataFrame {
  private JCheckBoxMenuItem debugOnlyCB;
  protected Properties props     = new Properties();
  protected Properties transProp = new Properties();
  public ConfigDataFrame(boolean standalone, final String title) {
    super(standalone, title, false, true, -1);
    updateMenu();
  }
    /** Prepare UI */
  protected void updateMenu() {
    updateFileMenu(getJMenuBar());
    updateOptionMenu(getJMenuBar());
    addHelpMenu();
    addHelpInToolBar();
  }
  private void updateFileMenu(JMenuBar menuBar) {
    JMenuItem item;
    JMenu menu = menuBar.getMenu(0);
  
    menu.insertSeparator(2);
  
    Icon icon = AppConstants.openIcon;
    Action action = new AbstractAction("Load Configuration", icon) { 
      public void actionPerformed(ActionEvent e) {
        ConfigDataFrame.this.repaint();
        JFileChooser fileChooser = getFileChooser();
        if (fileChooser.showOpenDialog(ConfigDataFrame.this) != 
               JFileChooser.APPROVE_OPTION) return;
        final File fName = fileChooser.getSelectedFile();
        if (fName == null) return;
        loadProperties(fName.getName());
      }
    };
    item =  menu.insert(action, 3); 
    menu.insert(item, 3);
  
    icon = new ImageIcon(AppConstants.iconDir+"mini-read.png");
    action = new AbstractAction("Show Configuration", icon) { 
      public void actionPerformed(ActionEvent e) {
        props.list(System.out);        
      }
    };
    item =  menu.insert(action, 4); 
    menu.insert(item, 4);
  
    icon = new ImageIcon(AppConstants.iconDir+"file_save.png");
    action = new AbstractAction("Save Configuration", icon) { 
      public void actionPerformed(ActionEvent e) {
        String filename = Tools.getEnv("SVTMON_DIR")+"/crate_config_new.prop";
        saveProperties(filename);
      }
    };
    item =  menu.insert(action, 5); 
    menu.insert(item, 5);

    icon = new ImageIcon(AppConstants.iconDir+"file_save.png");
    action = new AbstractAction("Save Configuration As ...", icon) { 
      public void actionPerformed(ActionEvent e) {
        ConfigDataFrame.this.repaint();
        JFileChooser fileChooser = getFileChooser();
        if (fileChooser.showSaveDialog(ConfigDataFrame.this) != 
           JFileChooser.APPROVE_OPTION)  return;
        final File fName = fileChooser.getSelectedFile();
        if (fName == null) return;
        saveProperties(fName.getName());
      }
    };
    item =  menu.insert(action, 6); 
    menu.insert(item, 6);

    menu.insertSeparator(7);
  }
  /** 
   * Update option 
   * @param menuBar   The Menubar object reference
   */
  private void updateOptionMenu(JMenuBar menuBar) {
    JMenu menu = menuBar.getMenu(1);

    debugOnlyCB = new JCheckBoxMenuItem("Debug only", false);
    menu.add(debugOnlyCB);
  }
  public boolean isDebugOnly() {
    return debugOnlyCB.isSelected();
  }
  public void loadProperties(final String filename) {
    Tools.loadProperties(props, filename);
  }
  public void showProperties() {
    props.list(System.out);
  }
  public void saveProperties(final String filename) {
    Tools.saveProperties(props, filename);
  }
  public void updateProperties() {
    for (Enumeration e = transProp.propertyNames() ; e.hasMoreElements() ;) {
      String key = (String)e.nextElement();
      props.put(key, transProp.getProperty(key));
    }
  }
  public void resetProperties() {
    // Start afresh the next time around
    transProp.clear();
  }
  public static String [] getDestinations(final String type) {
    String [] dests = new String[AppConstants.nCrates+1]; 
    String prefix   = "/spymon/"+type+"/";
    for (int i = 0; i < dests.length-1; i++) {
      dests[i] = new String(prefix+AppConstants.SVT_CRATE_PREFIX+i);
    }
    dests[dests.length-1] = new String(prefix+"...");
    return dests;
  }

  /** Test the class standalone */
  public static void main(String [] argv) {
    ConfigDataFrame f = new ConfigDataFrame(true, "XXX");
    f.setSize(630, 570);
    f.setVisible(true);
  }
}
