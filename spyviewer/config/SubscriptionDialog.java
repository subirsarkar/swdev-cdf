package config;

import java.io.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import config.util.*;

/**
 * <P>
 * A little dialog window which displays the Smartsockets communication
 * related message subject areas. One can edit the subject areas as well
 * and validate the new configuration from this widget.</P>
 *
 * @author   S. Sarkar
 * @version  0.1, November 2000
 */

public class SubscriptionDialog extends JPanel {
  private SpyMessenger parent;
  private final Dimension shSize   = new Dimension(160, 20);
  private final Dimension medSize  = new Dimension(200, 20);
  private final Dimension longSize = new Dimension(320, 20);
  boolean [] atStart = {false, true, true, false, true, true};
  private SvtMessagePanel svtP;

  SubscriptionDialog(SpyMessenger parent) {
    this.parent = parent;
    setLayout(new BorderLayout());

    JPanel panel = new JPanel(new BorderLayout());
    add(panel, BorderLayout.NORTH);
      	
    svtP = new SvtMessagePanel();
    panel.add(svtP, BorderLayout.NORTH);
  }
  public boolean isSubjectSubscribed(int index) {
    return svtP.linePanel[index].isSelected();
  }
  public String getSubjectSubscribed(int index) {
    return svtP.linePanel[index].destTF.getText();
  }
  protected void resetSubscription() {
    for (int i = 0; i < svtP.linePanel.length; i++) {
      svtP.linePanel[i].destTF.setText(AppConstants.defSubs[i]);
    }
  }
  class SvtMessagePanel extends JPanel  {
    protected SingleLinePanel [] linePanel; 
    SvtMessagePanel() {
      buildGUI();
    }
    protected void buildGUI() {
      setBorder(Tools.etchedTitledBorder(" Subscription To SVT Messages "));
      int len = AppConstants.defSubs.length;
      setLayout(new GridLayout(len, 1));
    
      linePanel = new SingleLinePanel[len];
      for (int i = 0; i < len; i++) {
  	linePanel[i] = new SingleLinePanel(AppConstants.svtSubLabels[i], 
                                           AppConstants.defSubs[i], atStart[i], shSize, medSize); 
  	add(linePanel[i]);
      }
    }
  }
  class SingleLinePanel extends JPanel {
    public JCheckBox optionCB;
    public JTextField destTF;
    SingleLinePanel(String label, String dest, boolean selected, 
         Dimension labelSize, Dimension fieldSize) {
      setLayout(new BorderLayout());

      JPanel panel = new JPanel();
      add(panel, BorderLayout.WEST);

      panel.add(optionCB = Tools.createCBox(label, selected));
      optionCB.setPreferredSize(labelSize);
      optionCB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          changeState();
        }
      });
      panel.add(destTF = new JTextField(dest));
      destTF.setPreferredSize(fieldSize);
      destTF.setEnabled((selected) ? true : false);
      destTF.setBorder(BorderFactory.createLoweredBevelBorder());
    }
    public void changeState() {
      optionCB.setForeground(optionCB.isSelected() ? Color.black : Color.gray); 
      destTF.setEnabled(optionCB.isSelected());
    }
    public JCheckBox getOptionButton() {
      return optionCB;
    }
    public JTextField getDestField() {
      return destTF;
    }
    public boolean isSelected() {
      return optionCB.isSelected();
    }
  }
  public static void main(String [] argv) {
    JPanel panel = new SubscriptionDialog(new SpyMessenger("Spy Analysis"));
    Icon icon = new ImageIcon(AppConstants.iconDir + "edit_about.png");
    String [] options = {"Ok"};
    int opt = JOptionPane.showOptionDialog(new JFrame(""), 
         panel, "Subscription Dialog", 
         JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, 
	 icon, options, options[0]);
  }
}