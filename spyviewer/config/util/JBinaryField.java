package config.util;

import java.awt.event.KeyEvent;
import java.awt.event.TextListener;

/**
  * Subclass of JNumberField which ensures that only 1s and 0s  are 
  * entered in the field
  */
public class JBinaryField extends JNumberField {

  TextListener textListener = null;

  //Basic constructors

  /**
   * Default Constructor
   */
  public JBinaryField() {
    super();
  }

  /** Constructor with initial value.
   * @param string  Initial value as string
   */
  public JBinaryField(String string) {
    super(string);
  }

  /**
   * Constructor with width parameter
   * @param columns  Number of characters space to allocate
   */
  public JBinaryField(int columns) {
    super(columns);
  }

  /**
   * Constructor with init value and width
   * @param string  Initial value as string
   * @param columns  Number of characters
   */
  public JBinaryField(String string, int columns) {
    super(string, columns);
  }

  /** Defines allowed keyStrokes
   * @param event KeyEvent to judge
   * @return == true -> Character is allowed 
   */
  public  boolean allowKey(KeyEvent event) {
    int keyCode = event.getKeyCode();
    char keyChar = event.getKeyChar();
    if (keyChar == KeyEvent.VK_0            ||
	keyChar == KeyEvent.VK_1            ||
	keyCode == KeyEvent.VK_BACK_SPACE   ||
	keyCode == KeyEvent.VK_TAB          ||
	keyCode == KeyEvent.VK_RIGHT        ||
	keyCode == KeyEvent.VK_LEFT         ||
	keyCode == KeyEvent.VK_DELETE       ||
	keyCode == KeyEvent.VK_HOME         ||
	keyCode == KeyEvent.VK_END          ||
	keyCode == KeyEvent.VK_ENTER        ||
	keyCode == KeyEvent.VK_ESCAPE       ||
	keyCode == KeyEvent.VK_SHIFT) {
      return true;
    } else  {
      return false;
    }
  }

  /**
   * Return the number as an int
   * @return value of text
   */
  public int getInt() {
    return Integer.parseInt(getText(), 2);
  }

  /**
   * Return the number as a long
   * @return value of text
   */
  public long getLong() {
    return Long.parseLong(getText(), 2);
  }

} // End class declaration
