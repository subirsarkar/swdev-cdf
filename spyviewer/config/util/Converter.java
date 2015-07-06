package config.util;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JTextField;

import javax.swing.border.Border;

/**
 *  Utility to convert an integral number into different formats. Available
 *  formats are Decimal, Binary, Octal and Hexadecimal.
 * 
 *  @version 0.1   July, 2000
 *  @author  Adapted from CDFVME_COMMON
 */
public class Converter extends JFrame implements KeyListener {

  public JNumberField longF = new JNumberField("0", 16), 
                     octalF = new JNumberField("0", 16);
  public JHexField hexF     = new JHexField("0", 16);
  public JBinaryField binF  = new JBinaryField("0", 16);
  public long value;
  boolean standAlone;

  // Constructors 

  /** @param standAlone  Specifies whether the applicaiton is part of a bigger one
   *                     or standalone
   */ 
  public Converter(boolean standAlone) {
    this(0, standAlone);
  }

  /** 
   * @param initialValue Start the utility with some initial value to be shown
   * @param standAlone  Specifies whether the applicaiton is part of a bigger one
   *                     or standalone
   */ 
  public Converter(int initialValue, boolean standAlone) {
    super("Convert number in different formats");
    this.standAlone = standAlone;
    buildGUI(initialValue);    
  }
  /** Prepare user inteface */
  public void buildGUI(int initialValue) {
     addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent eW) {
          closeWindow();
        }
     });
     GriddedPanel panel = new GriddedPanel();
     panel.setBorder(BorderFactory.createEtchedBorder());
 
     // Spacing between the label and the field
     Border border  = BorderFactory.createEmptyBorder(0, 0, 0, 10);
     Border border1 = BorderFactory.createEmptyBorder(0, 20, 0, 10);
  
     JLabel label1 = new JLabel("Decimal");
     label1.setBorder(border);
     panel.addComponent(label1, 1, 1);
 
     longF.setHorizontalAlignment(JNumberField.RIGHT);
     panel.addFilledComponent(longF, 1, 2, 2, 1,GridBagConstraints.HORIZONTAL);

     JLabel label2 = new JLabel("Hex");
     label2.setBorder(border);
     panel.addComponent(label2, 2, 1);
 
     hexF.setHorizontalAlignment(JNumberField.RIGHT);
     panel.addFilledComponent(hexF, 2, 2, 2, 1,GridBagConstraints.HORIZONTAL);
     
     JLabel label3 = new JLabel("Octal");
     label3.setBorder(border);
     panel.addComponent(label3, 3, 1);
 
     octalF.setHorizontalAlignment(JNumberField.RIGHT);
     panel.addFilledComponent(octalF, 3, 2, 2, 1,GridBagConstraints.HORIZONTAL);

     JLabel label4 = new JLabel("Binary");
     label4.setBorder(border);
     panel.addComponent(label4, 4, 1);
 
     binF.setHorizontalAlignment(JNumberField.RIGHT);
     panel.addFilledComponent(binF, 4, 2, 3, 1, GridBagConstraints.HORIZONTAL);

     longF.addKeyListener(this);
     hexF.addKeyListener(this);
     octalF.addKeyListener(this);
     binF.addKeyListener(this);

     value = initialValue;

     updateFields();
     getContentPane().add(panel, BorderLayout.NORTH);

     JPanel panel2 = new JPanel(new FlowLayout());
     JButton button = new JButton("Clear");
     button.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent event) { 
         clearFields();
       }
     });
     panel2.add(button);

     button = new JButton("Close");
     button.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent event) { 
          closeWindow();
        }
     });
     panel2.add(button);
     getContentPane().add(panel2, BorderLayout.SOUTH);
  }
  /** Quit the application if standalone or simply close the window */ 
  protected void closeWindow() {
    if (standAlone) {
      System.exit(0);
    } else {
      setVisible(false);
      dispose();
    }
  }
  /** Clear all the text entry fields */
  protected void clearFields() {
     longF.setText("");
     hexF.setText("");
     octalF.setText("");
     binF.setText("");
  }
  // Implement KeyListener interface

  public void keyTyped(KeyEvent e) {};
  public void keyPressed(KeyEvent e) {};
  public void keyReleased(KeyEvent e) {
    
    //System.out.println(e.toString() + "\t" + e.getKeyText(e.getKeyCode()) + 
    //		       "\t" + e.getKeyCode() + "\t" + e.getKeyChar());
    try {
      long newvalue = ((JNumberField) e.getSource()).getLong();
      if (newvalue != value) {
        int caretPos = ((JNumberField) e.getSource()).getCaretPosition();
	value = newvalue;
	updateFields();
	((JNumberField) e.getSource()).setCaretPosition(caretPos);
      }
    } catch (NumberFormatException eN) {
      if (!((JTextField)e.getSource()).getText().equals("")) {
	System.out.println("Number Format Exception " + 
			   ((JTextField)e.getSource()).getText() + " " + 
			   e.getSource());
	eN.printStackTrace();
      }
    }
  }

  /** Update all the text entry fields as immediate response to key input */
  public void updateFields() {
    longF.setText(Long.toString(value));
    hexF.setText(Long.toHexString(value));
    binF.setText(Long.toBinaryString(value));
    octalF.setText(Long.toOctalString(value));
  }

  /** Test the widget */
  public static void main(String [] argv) {
    Converter c = new Converter(true);
    c.setSize(220, 160);
    c.setVisible(true);
  }
}
