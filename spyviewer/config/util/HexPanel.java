package config.util;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

/**
 * Component subclassing JPanel, containing a JLabel ("0x") and a JHaxField.
 * @Author Th.Speer
 * @Version 0.1
 */

public class HexPanel extends JPanel {


  private JHexField hexField;
  private JLabel    hexLabel;
  

  /**
   * Constructs a new TextField. 
   * The Hex field is empty
   */
  
  public HexPanel() {
    super();
    hexField = new JHexField();
    finishPanel();
  }


  /**
   * Constructs a new HexPanel. 
   * The Hex field is initialized with the specified text. 
   */

  public HexPanel(String string) {
    super();
    hexField = new JHexField(string);
    finishPanel();
  }


  /**
   * Constructs a new empty HexPanel. 
   * The Hex field is constructed with the specified number of columns. 
   */

  public HexPanel(int columns) {
    super();
    hexField = new JHexField(columns);
    finishPanel();
  }


  /**
   * Constructs a new HexPanel. 
   * The Hex field is initialized with the specified text and columns. 
   */

  public HexPanel(String string, int columns) {
    super();
    hexField = new JHexField(string, columns);
    finishPanel();
  }

  /**
   * Constructs a new HexPanel. 
   * The Hex field is initialized with the specified number and columns. 
   */

  public HexPanel(int content, int columns) {
    super();
    hexField = new JHexField(Integer.toHexString(content), columns);
    finishPanel();
  }



  private void finishPanel() {
    hexLabel = new JLabel("0x", SwingConstants.LEFT);
    hexLabel.setForeground(Color.black);
    hexField.setHorizontalAlignment(JTextField.RIGHT);   
//     setLayout(new FlowLayout());
//     add("West", hexLabel);
//     add("East", hexField);
    GridBagLayout gridbag2 = new GridBagLayout();
    setLayout(gridbag2);
    GridBagConstraints GBC = new GridBagConstraints();
    Tools.buildConstraints(GBC,0,0,1,1,0.,0.,GBC.EAST,GBC.NONE);
    gridbag2.setConstraints(hexLabel,GBC);
    Tools.buildConstraints(GBC,1,0,1,1,0.,0.,GBC.WEST,GBC.NONE);
    gridbag2.setConstraints(hexField,GBC);
     add(hexLabel);
     add(hexField);
  }
  

  /**
   * This method returns the JHexField, which can then be used with all 
   * the JHexField methods
   *  @returns The JHexField contained in the Panel.
   */

  public JHexField getHexField() {
    return hexField;
  }

  public int getInt() {
    return hexField.getInt();
  }

  public void setInt(int number) {
    hexField.setText(Integer.toHexString(number));
  }

   public static void main(String args[]) {
    JFrame f = new JFrame("Hex Panel");
    HexPanel essaih = new HexPanel("fa123", 7);
    f.getContentPane().add(essaih);
    f.setSize(100, 100);
    f.setVisible(true);
  }

}
