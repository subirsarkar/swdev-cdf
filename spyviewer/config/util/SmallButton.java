package config.util;

import java.awt.Insets;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.BorderFactory;

import javax.swing.border.Border;
import javax.swing.border.BevelBorder;
/** 
 * Extends JButton to create custom small buttons suitable in ToolBars
 */
public class SmallButton extends JButton implements MouseListener {
  protected Border raisedBorder;
  protected Border loweredBorder;
  protected Border etchedBorder;
  protected Border inactiveBorder;
  protected Border defaultBorder;

  protected StatusBar statusBar;
  private int borderType;

  public SmallButton(Action action, String tip, int borderType) {
    this(action, null, tip, borderType);
  }
  public SmallButton(Action action, String tip) {
    this(action, null, tip);
  }
  public SmallButton(Action action, StatusBar statusBar, String tip) {
    this(action, statusBar, tip, -1);
  }
  public SmallButton(Action action, StatusBar statusBar, String tip, int borderType) {
    super();
    if (action != null) 
      setIcon((Icon)action.getValue(Action.SMALL_ICON));
    this.statusBar = statusBar;
    this.borderType = borderType;
    raisedBorder   = BorderFactory.createRaisedBevelBorder();
    loweredBorder  = BorderFactory.createLoweredBevelBorder();
    inactiveBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);
    etchedBorder   = BorderFactory.createEtchedBorder();

    switch (borderType) {
    case BevelBorder.RAISED:
      defaultBorder = raisedBorder;
      break;
    case BevelBorder.LOWERED:
      defaultBorder = loweredBorder;
      break;
    default:
      defaultBorder = inactiveBorder;
      break;
    }
    
    setBorder(defaultBorder);
    setMargin(new Insets(1,1,1,1));
    setToolTipText(tip);

    addActionListener(action);
    addMouseListener(this);
    setRequestFocusEnabled(false);
  }

  public float getAlignmentY() { return 0.5f; }

  public void mousePressed(MouseEvent e) { 
    setBorder(loweredBorder);
  }
  public void mouseReleased(MouseEvent e) {
    setBorder(defaultBorder);
  }
  public void mouseClicked(MouseEvent e) {}
  public void mouseEntered(MouseEvent e) {

    switch (borderType) {
    case BevelBorder.LOWERED:
      setBorder(raisedBorder);
      break;
    case -1:
      setBorder(etchedBorder);
      break;
    default:
      break;
    }
    if (statusBar != null) {
      statusBar.setText(getToolTipText());
    }
  }
  public void mouseExited(MouseEvent e) {
    setBorder(defaultBorder);
    if (statusBar != null) {
      statusBar.setText(" ");
    }
  }
}
