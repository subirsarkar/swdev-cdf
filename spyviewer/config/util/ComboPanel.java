package config.util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Color;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.BorderFactory;

public class ComboPanel extends JPanel {
  private JLabel label;
  private JComboBox comboBox;
  private static final Dimension longSize = new Dimension(270, 25);
  public ComboPanel(String [] destStr, String name, int hgap) {
    setLayout(new BorderLayout());
    label = new JLabel(name);
    label.setForeground(Color.black);
    label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, hgap));
    add(label, BorderLayout.WEST);

    comboBox = new JComboBox();
    if (destStr != null) {
      for (int i = 0; i < destStr.length; i++)
        comboBox.addItem(destStr[i]);   
      comboBox.setSelectedIndex(0);
    }
    comboBox.setEditable(true);
    comboBox.setPreferredSize(longSize);
    add(comboBox, BorderLayout.CENTER);
  }
  protected JLabel getLabel() {
    return label;
  }
  protected JComboBox getComboBox() {
    return comboBox;
  }
  public String getSelectedItem() {
    return (String)comboBox.getSelectedItem();
  }
  public void addItem(final String item) {
    comboBox.addItem(item);
  }
}
