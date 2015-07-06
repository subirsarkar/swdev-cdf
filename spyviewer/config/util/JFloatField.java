package config.util;

import java.awt.event.KeyEvent;

/**
  * Subclass of JNumberField which ensures that only numbers are entered in the
  * field
  */
public class JFloatField extends JNumberField {

  //Basic constructors

  /**
   * Constructor
   */
  public JFloatField() {
    super();
  }

  /**
   * Constructor
   * @param string  Initial value as string
   */
  public JFloatField(String string) {
    super(string);
  }

  /**
   * Constructor
   * @param columns Number of characters space to allocate
   */
  public JFloatField(int columns) {
    super(columns);
  }

  /**
   * Constructor
   * @param string  Initial value as string
   * @param columns  Number of characters
   */
  public JFloatField(String string, int columns) {
    super(string, columns);
  }


  /** Defines allowed keyStrokes
   * @param event KeyEvent to judge
   * @return == true -> Character is allowed 
   */
  public boolean allowKey(KeyEvent event) {
    int keyCode = event.getKeyCode();
    char keyChar = event.getKeyChar();
    return ( (keyCode == KeyEvent.VK_PERIOD) || (keyChar == '.') || super.allowKey(event)  );
  }

  /**
   * Return the number as a float
   */
  public float getFloat() {
    return new Float(getText()).floatValue();
  }
  /**
   * Return the number as an double
   */
  public double getDouble() {
    return new Double(getText()).doubleValue();
  }

} // End class declaration
