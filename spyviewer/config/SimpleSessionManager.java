package config;

import java.io.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import config.util.*;

/**
 *  <P>
 *   
 *  @version 0.1, October 2001
 *  @author  Subir Sarkar
 */
public class SimpleSessionManager extends JPanel {
    /** Parent frame */
  private SpyMessenger parent;
    /** Panel which contains the SmartSockets Subscriptions */
  private SubscriptionDialog aPanel;
    /** Panel which contains the partition-to-watch box and the 
     *  Connect button
     */
  private ButtonPanel bPanel;

    /** Initialise the object */
  public SimpleSessionManager(SpyMessenger parent) {
    this.parent = parent;
    buildGUI();
  }
    /** Create the user interface */
  private void buildGUI() {
    setLayout(new BorderLayout());

    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
    add(panel, BorderLayout.NORTH);

    panel.add(aPanel = new SubscriptionDialog(parent), BorderLayout.NORTH);
    panel.add(bPanel = new ButtonPanel(), BorderLayout.CENTER);
  }
  public SubscriptionDialog getSubscriptionPanel() {
    return aPanel;
  }
  public ButtonPanel getButtonPanel() {
    return bPanel;
  }
    /** Define the button panel */
  public class ButtonPanel extends JPanel {
    private WordPanel wordPanel;

    public ButtonPanel () {
      setLayout(new BorderLayout());
      setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));

      JPanel panel1 = new JPanel(new FlowLayout());

      wordPanel = new WordPanel("Partition to Watch:", 
          String.valueOf(parent.partitionToWatch), 40, 5, JTextField.RIGHT);
      panel1.add(wordPanel);
      
      JPanel panel2 = new JPanel(new FlowLayout());
      JButton button = new JButton("Reset");
      panel2.add(button, BorderLayout.WEST);
      button.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          aPanel.resetSubscription();
        }
      });

      button = new JButton("Start");
      panel2.add(button, BorderLayout.EAST);
      button.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          SimpleSessionManager.this.setVisible(false);
          parent.insertScheme();
        }
      });

      add(panel1, BorderLayout.WEST);
      add(panel2, BorderLayout.EAST);
    }
  }
    /** 
     * Test the class 
     * @param argv Input argument list
     */
  public static void main (String [] argv) {
    JFrame f = new JFrame("Session Manager");
    Container content = f.getContentPane();
    content.add(new SimpleSessionManager(null));
    f.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });
    f.setSize(300,300);
    f.setVisible(true);
  }
}
