package config;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import config.util.*;

public class ReceiverDisplayPanel extends JPanel {

  protected JPanel labelP;
  protected TextPanel textPanel;
  protected JLabel partitionLabel, subjectLabel, senderLabel, stateLabel;

  private JFrame parent;

  public ReceiverDisplayPanel(JFrame parent) {
    this.parent = parent;
    buildGUI();
  }
  protected void buildGUI() {
    setLayout(new BorderLayout());
    setBorder(BorderFactory.createLoweredBevelBorder());

    String label = " ";
    Box labelP = Box.createVerticalBox();
     
    partitionLabel = new JLabel(label, SwingConstants.LEFT);
    partitionLabel.setForeground(Color.black);

    subjectLabel = new JLabel(label, SwingConstants.LEFT);
    subjectLabel.setForeground(Color.black);

    senderLabel = new JLabel(label, SwingConstants.LEFT);
    senderLabel.setForeground(Color.black);

    stateLabel = new JLabel(label, SwingConstants.LEFT);
    stateLabel.setForeground(Color.black);

    updateLabels();

    labelP.add(partitionLabel);
    labelP.add(Box.createVerticalStrut(5));
    labelP.add(subjectLabel);
    labelP.add(Box.createVerticalStrut(5));
    labelP.add(senderLabel);
    labelP.add(Box.createVerticalStrut(5));
    labelP.add(stateLabel);

    add(labelP, BorderLayout.NORTH);

    textPanel = new TextPanel();
    add(textPanel, BorderLayout.CENTER);
  }
  protected void updateLabels() {
    String label;
    label = "Partition " + 
	 ((parent != null) ? Integer.toString(getSpyMessenger().getPartition()) : "0");
    partitionLabel.setText(label);

    label = "Subject " + 
	 ((parent != null) ? getSpyMessenger().getSubject() : " ");
    subjectLabel.setText(label);

    label = "Sender " + 
	 ((parent != null) ? getSpyMessenger().getSender() : " ");
    senderLabel.setText(label);

    label = "DAQ State " + 
	 ((parent != null) ? Integer.toString(getSpyMessenger().getActiveState()) : "0");
    stateLabel.setText(label);
  }
  public String getText() {
    return textPanel.getText();
  }
  public TextPanel getTextPanel() {
    return textPanel;
  }
  public SpyMessenger getSpyMessenger() {
    return (SpyMessenger) parent;
  }
}
