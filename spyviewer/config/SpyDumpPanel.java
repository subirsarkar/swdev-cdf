package config;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import config.util.*;
/*
 * @version  0.1, July 2000
 * @author   S. Sarkar
 */

public class SpyDumpPanel extends JPanel {
  private static boolean DEBUG = true;
  private String data   = new String("Not valid");
  private String sender = new String("Unknown");
  private JFrame parent;
  private JLabel statusLabel  = new JLabel("No data yet ...");
  private TextPanel textPanel = new TextPanel();
  
  public SpyDumpPanel(JFrame parent) {
    this.parent = parent;
    setLayout(new BorderLayout());

    JPanel panel = new JPanel(new BorderLayout());
    add(panel, BorderLayout.NORTH);

    statusLabel.setBorder(BorderFactory.createEmptyBorder(5,2,2,1));
    statusLabel.setForeground(Color.black);
    panel.add(statusLabel, BorderLayout.WEST);

    textPanel.setTextFont(new Font("SanSerif", Font.PLAIN, 10));
    add(textPanel, BorderLayout.CENTER);
  }
  public void setData(String data) {
    this.data = data;
  }
  public String getData() {
    return data;
  }
  public void setSender(String sender) {
    this.sender = sender;
  }
  public String getSender() {
    return sender;
  }
  public void update() {
    statusLabel.setText("Data from " + sender);
    textPanel.setText(data);
  }
  protected String getText() {
    return textPanel.getText();
  }
  protected JLabel getLabel() {
    return statusLabel;
  }
}
