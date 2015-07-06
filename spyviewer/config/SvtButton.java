package config;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import config.util.StatusBar;

public class SvtButton extends JButton implements MouseListener {
  private StatusBar statusBar;
  public SvtButton(String name, Icon icon) {
    this(name, icon, null, "");
  }
  public SvtButton(Icon icon, StatusBar statusBar, String tip) {
    this("", icon, statusBar, tip);
  }
  public SvtButton(StatusBar statusBar, String tip) {
    this("", null, statusBar, tip);
  }
  public SvtButton(String name, Icon icon, StatusBar statusBar, String tip) {
    super(name, icon);
    this.statusBar = statusBar;
    setMargin(new Insets(0, 0, 0, 0));
    setToolTipText(tip);
    addMouseListener(this);
  }
  public void mousePressed(MouseEvent e) { }
  public void mouseReleased(MouseEvent e) {}
  public void mouseClicked(MouseEvent e) {}
  public void mouseEntered(MouseEvent e) {
    if (statusBar != null) {
      statusBar.setText(getToolTipText());
    }
  }
  public void mouseExited(MouseEvent e) {
    if (statusBar != null) {
      statusBar.setText(" ");
    }
  }
}
