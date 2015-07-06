package config.util;

import javax.swing.JTextField;
import java.awt.event.KeyEvent;

/**
  * Subclass of JTextField which ensures that only numbers are entered in the
  * field
  */
public class JNumberField extends JTextField {

  //Basic constructors

 /**
   * Default Constructor
   */
   public JNumberField() {
    super();
  }

  /** Constructor with initial value.
   * @param string  Initial value as string
   */
  public JNumberField(String string) {
    super(string);
  }

  /**
   * Constructor with width parameter
   * @param columns  Number of characters space to allocate
   */
  public JNumberField(int columns) {
    super(columns);
  }

  /**
   * Constructor with init value and width
   * @param string  Initial value as string
   * @param columns  Number of characters
   */
  public JNumberField(String string, int columns) {
    super(string, columns);
  }

  /**  Process keypress events and  eat disallowed ones */
  protected void processComponentKeyEvent(KeyEvent e) {
    int id = e.getID();
    switch(id)
      {
      case KeyEvent.KEY_TYPED:
      case KeyEvent.KEY_PRESSED:
      case KeyEvent.KEY_RELEASED:
	if (allowKey(e)) {
	  super.processComponentKeyEvent(e);
	} else {
	  e.consume();
	}
	break;

      }
  }

  /** Defines allowed keyStrokes
   * @param event KeyEvent to judge
   * @return == true -> Character is allowed 
   */
  public boolean allowKey(KeyEvent event) {
    char keyChar = event.getKeyChar();
    if (keyChar == KeyEvent.VK_0            ||
	keyChar == KeyEvent.VK_1            ||
	keyChar == KeyEvent.VK_2            ||
	keyChar == KeyEvent.VK_3            ||
	keyChar == KeyEvent.VK_4            ||
	keyChar == KeyEvent.VK_5            ||
	keyChar == KeyEvent.VK_6            ||
	keyChar == KeyEvent.VK_7            ||
	keyChar == KeyEvent.VK_8            ||
	keyChar == KeyEvent.VK_9            ||
	isControlKey(event)) {
      return true;
    } else  {
      return false;
    }
  }
  public boolean isControlKey( KeyEvent event) {
    int keyCode = event.getKeyCode();
    if (keyCode == KeyEvent.VK_BACK_SPACE   ||
	keyCode == KeyEvent.VK_TAB          ||
	keyCode == KeyEvent.VK_RIGHT        ||
	keyCode == KeyEvent.VK_LEFT         ||
	keyCode == KeyEvent.VK_UP           ||
	keyCode == KeyEvent.VK_DOWN         ||
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
   */
  public int getInt() {
    return Integer.parseInt(getText());
  }

  /**
   * Return the number as a long
   */
  public long getLong() {
    return Long.parseLong(getText());
  }

} // End class declaration
