package config.util;

import java.awt.Component;
import java.awt.Color;
import javax.swing.JList;
import javax.swing.Icon;
import javax.swing.DefaultListCellRenderer;

// This is the only method defined by ListCellRenderer.
// We just reconfigure the JLabel each time we're called.
public class MyCellRenderer extends DefaultListCellRenderer {
  Icon selIcon;
  Icon uselIcon;
  final Color evenCellColor = Color.white; // new Color(225,225,255);      
  final Color oddCellColor  = new Color(240,240,255);      

  public MyCellRenderer(final Icon icon) {
    this(icon, icon);
  }
  public MyCellRenderer(final Icon selIcon, final Icon uselIcon) {
    this.selIcon  = selIcon;    
    this.uselIcon = uselIcon;
  }
  public Component getListCellRendererComponent(
    JList list,
    Object value,            // value to display
    int index,               // cell index
    boolean isSelected,      // is the cell selected
    boolean cellHasFocus)    // the list and the cell have the focus
  {
      /* The DefaultListCellRenderer class will take care of
       * the JLabels text property, it's foreground and background
       * colors, and so on.
       */
     super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

     String s = value.toString();
     setText(s);
     if (isSelected) {
       setIcon(selIcon);
       setBackground(list.getSelectionBackground());
     }
     else {
       setIcon(uselIcon);
       if (index%2 == 0) 
         setBackground(evenCellColor);
       else 
         setBackground(oddCellColor);
     }
     return this;
  }
}
