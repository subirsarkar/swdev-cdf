package config.util;

import java.awt.Insets;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

import javax.swing.JToggleButton;
import javax.swing.ImageIcon;
import javax.swing.BorderFactory;

import javax.swing.border.Border;

/** 
 * Extends JToggleButton to create custom small toggle buttons
 */
public class SmallToggleButton extends JToggleButton  
     implements ItemListener, MouseListener  {
  protected Border raisedBorder;
  protected Border loweredBorder;
  protected Border inactiveBorder;
  protected Border etchedBorder;

  public SmallToggleButton(boolean selected, 
     ImageIcon imgUnselected, ImageIcon imgSelected, String tip) {
    super(imgUnselected, selected);
    setHorizontalAlignment(CENTER);
    setBorderPainted(true);
    inactiveBorder = BorderFactory.createEmptyBorder(2,2,2,2);
    raisedBorder   = BorderFactory.createRaisedBevelBorder();
    loweredBorder  = BorderFactory.createLoweredBevelBorder();
    etchedBorder   = BorderFactory.createEtchedBorder();
    // setBorder(selected ? loweredBorder : raisedBorder);
    setBorder(selected ? loweredBorder : inactiveBorder);
    setMargin(new Insets(1,1,1,1));
    setToolTipText(tip);
    setRequestFocusEnabled(false);
    setSelectedIcon(imgSelected);
    addItemListener(this);
  }

  public float getAlignmentY() { return 0.5f; }

  public void itemStateChanged(ItemEvent e) {
    // setBorder(isSelected() ? loweredBorder : raisedBorder);
    setBorder(isSelected() ? loweredBorder : inactiveBorder);
  }
  public void mousePressed(MouseEvent e) { 
    setBorder(loweredBorder);
  }
  public void mouseReleased(MouseEvent e) {
    setBorder(inactiveBorder);
  }
  public void mouseClicked(MouseEvent e) {}
  public void mouseEntered(MouseEvent e) {
    setBorder(etchedBorder);
  }
  public void mouseExited(MouseEvent e) {
    setBorder(inactiveBorder);
  }
}
