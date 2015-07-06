package config.util;

import java.awt.*;
import javax.swing.JMenuItem;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

public class MenuHelpTextAdapter implements ChangeListener {
  private JMenuItem menuItem;
  private String helpText;
  private StatusBar statusBar;

  public MenuHelpTextAdapter(JMenuItem menuItem, String helpText,
                             StatusBar statusBar)  {
    this.menuItem  = menuItem;
    this.helpText  = helpText;
    this.statusBar = statusBar;
    menuItem.addChangeListener(this);
  }

  public void stateChanged(ChangeEvent evt) {
    if (menuItem.isArmed())
      statusBar.setText(helpText);
    else 
      statusBar.setText(" ");
  }
}
