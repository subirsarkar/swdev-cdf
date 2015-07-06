package config.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.BorderFactory;

public class StatusBar extends JPanel {
  private JLabel label;
  public StatusBar() {
    this("Ready ...");
  }
  public StatusBar(final String text) {
    setLayout(new BorderLayout());

    label = new JLabel(text, SwingConstants.LEFT);
    label.setForeground(Color.black);
    label.setFont(new Font("SanSerif", Font.PLAIN, 11));
    label.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 4));
    add(label, BorderLayout.CENTER);
    add(new JSeparator(SwingConstants.VERTICAL), BorderLayout.EAST);
  }
  protected JLabel getLabel() {
    return label;
  }
  public String getText() {
    return label.getText();
  }
  public void setText(final String status) {
    label.setText(status);
  }
}
