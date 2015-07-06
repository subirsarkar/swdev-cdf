package config.util;

import java.awt.event.KeyEvent;

/**
  * Subclass of JNumberField which ensures that only hex numbers are entered in
  * the field
  */
public class JHexField extends JNumberField {

  // Basic constructors
 /**
   * Default Constructor
   */
  public JHexField() {
    super();
  }

  /** Constructor with initial value.
   * @param string  Initial value as string
   */
  public JHexField(String string) {
    super(string);
  }

  /**
   * Constructor with width parameter
   * @param columns  Number of characters space to allocate
   */
  public JHexField(int columns) {
    super(columns);
  }

  /**
   * Constructor with init value and width
   * @param string  Initial value as string
   * @param columns  Number of characters
   */
  public JHexField(String string, int columns) {
    super(string, columns);
  }


  /** Defines allowed keyStrokes
   * @param event KeyEvent to judge
   * @return == true -> Character is allowed 
   */
  public boolean allowKey(KeyEvent event) {
    int keyCode = event.getKeyCode();
    char keyChar = event.getKeyChar();
    if (super.allowKey(event)               ||
	//Letters
	keyChar == KeyEvent.VK_A            ||
	keyChar == KeyEvent.VK_B            ||
	keyChar == KeyEvent.VK_C            ||
	keyChar == KeyEvent.VK_D            ||
	keyChar == KeyEvent.VK_E            ||
	keyChar == KeyEvent.VK_F            ||

	// Lowercase here
	keyChar == KeyEvent.VK_A + 0x20     ||
	keyChar == KeyEvent.VK_B + 0x20     ||
	keyChar == KeyEvent.VK_C + 0x20     ||
	keyChar == KeyEvent.VK_D + 0x20     ||
	keyChar == KeyEvent.VK_E + 0x20     ||
	keyChar == KeyEvent.VK_F + 0x20) {
      return true;
    } else  {
      return false;
    }
  }

  /**
   * Return the number as an int
   */
  public int getInt() {
    return Tools.hex2Int(getText());
  }

  /**
   *  Return the number as a long
   */
  public long getLong() {
    return Tools.hex2Long(getText());
  }

} // End class declaration
