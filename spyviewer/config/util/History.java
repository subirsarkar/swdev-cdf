package config.util;

import java.awt.Font;
import java.awt.Color;
import java.awt.Container;
import java.awt.Toolkit;

import javax.swing.JTextPane;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.JScrollPane;

import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.BadLocationException;

import java.util.GregorianCalendar;

/**
 * <P>
 * Provides a way to print history/warning/error messages to any JTextPane 
 * which is passed to a static method. Although JTextPane is a little
 * heavier than JTextArea, it is much more flexible and one can use different
 * fonts/color for individual text strings. One can also add labels and icons,
 * or for that matter any JComponent inside JTextPane.
 *
 * <P>
 * A singleton instance is available to manage all requests. If default style
 * is preferred obtaining teh singleton instance is needed. Otherwise one
 * can simply use the static methods to print formatted and styled text.
 *
 * @version 0.2, August 2000 
 * @author S. Sarkar
 */

public class History {
    /** Gregorian calendar to get date, time etc. in a nice format */
  private static GregorianCalendar calendar;
    /** Default style for the JTextPane */
  protected static boolean defaultStyle = true; 
    /** Interval of time between two beeps */
  private static final int BEEP_INTERVAL  = 100; 
    /** Style attribute set */
  protected static SimpleAttributeSet fontColorAttribute 
      = new SimpleAttributeSet();
    /** Default Font family (Helvetica) */
  protected static String defaultFamily = "Helvetica";
    /** Default Font size (12) */
  protected static int defaultSize = 12;
    /** Default foreground (Black) */
  protected static Color defaultForeground = Color.black;
    /** Default background (White) */
  protected static Color defaultBackground = Color.white;
    /** Default Font type (PLAIN) */
  protected static String defaultType      = "Plain";

    /** Static reference to <CODE>this</CODE> */
  private static History _instance = null;

  /** 
   * Singleton constructor. If the class is already instantiated
   * returns the instance, else creates it. Only one object of History
   * class manages all the interactions using static methods.
   *
   * @return  An instance of History class
   */
  public static History Instance() {
    if (_instance == null) {
      _instance = new History();
    }
    if (!isDefaultStyle()) {
      useDefaultStyle(); 
    }
    return _instance;
  }
  /** 
   * Protected class which one can't create directly. Used to setup 
   * proper defaults.
   */
  protected History () {
    useDefaultStyle();
  }
  /** Check whether default style is set or not */
  public static boolean isDefaultStyle() {
    return defaultStyle;
  }
  /** 
   * Set or unset Default style 
   * @param decision  Decision to set Default style
   */
  protected static void setDefaultStyle(boolean decision) {
    defaultStyle = decision;
  }
  /**
   * Set default value for all the attributes 
   */ 
  protected static void useDefaultStyle() {
    defaultStyle = true;
    StyleConstants.setForeground(fontColorAttribute, defaultForeground);
    // StyleConstants.setBackground(fontColorAttribute, defaultBackground);
    StyleConstants.setFontFamily(fontColorAttribute, defaultFamily);
    StyleConstants.setFontSize(fontColorAttribute, defaultSize);
    StyleConstants.setBold(fontColorAttribute, false);
    StyleConstants.setItalic(fontColorAttribute, false);
    StyleConstants.setUnderline(fontColorAttribute, false);
  }
  /**
   * Change default attributes with user option
   * @param fontFamily    Text Font Family (Helvetica, Times etc.)
   * @param fontType      Text Font Type   (Plain, Bold, Italic etc.)
   * @param fontSize      Text Font Size   (8 ... 24)
   * @param fgColor       Text foreground color
   * @param bgColor       Text background color
   */
  protected static void changeStyle(String fontFamily, String fontType, 
                int fontSize, Color fgColor, Color bgColor) {
    defaultStyle = false;

    StyleConstants.setForeground(fontColorAttribute, fgColor);
    // StyleConstants.setBackground(fontColorAttribute, bgColor);
    StyleConstants.setFontFamily(fontColorAttribute, fontFamily);
    StyleConstants.setFontSize(fontColorAttribute, fontSize);
    if (fontType != null) {
      if (fontType.equals("Italic"))
        StyleConstants.setItalic(fontColorAttribute, true);
      else if (fontType.equals("Bold"))
        StyleConstants.setBold(fontColorAttribute, true);
      else if (fontType.equals("Underline"))
        StyleConstants.setUnderline(fontColorAttribute, true);
    }
  }
  /**
   * Add styled text in the JTextPane
   * @param textPane      The JTextPane under consideration
   * @param text          Text to be inserted
   */
  public static void addText(JTextPane textPane, String text) {
    if (!isDefaultStyle()) {
      setDefaultStyle(true);
      useDefaultStyle();
    }
    addText(textPane, text, 
            defaultFamily, defaultType, defaultSize, 
            defaultForeground, defaultBackground, 0, false);
  }
  /**
   * Add styled text in the JTextPane
   * @param textPane      The JTextPane under consideration
   * @param text          Text to be inserted
   * @param fgColor       Text foreground color
   */
  public static void addText(JTextPane textPane, String text, Color fgColor) {
     setDefaultStyle(false);
     addText(textPane, text, 
             defaultFamily, defaultType, defaultSize, 
             fgColor, defaultBackground, 0, false);
  } 
  /**
   * Add styled text in the JTextPane
   * @param textPane      The JTextPane under consideration
   * @param text          Text to be inserted
   * @param fontSize      Text Font Size   (8 ... 24)
   */
  public static void addText(JTextPane textPane, String text, int fontSize) {
     setDefaultStyle(false);
     addText(textPane, text, 
             defaultFamily, defaultType, fontSize, 
             defaultForeground, defaultBackground, 0, false);
  } 
  /**
   * Add styled text in the JTextPane
   * @param textPane      The JTextPane under consideration
   * @param text          Text to be inserted
   * @param fontSize      Text Font Size   (8 ... 24)
   * @param fgColor       Text foreground color
   */
  public static void addText(JTextPane textPane, String text, int fontSize, 
                             Color fgColor) {
     setDefaultStyle(false);
     addText(textPane, text, 
             defaultFamily, defaultType, fontSize, 
             fgColor, defaultBackground, 0, false);
  } 
  /**
   * Add styled text in the JTextPane
   * @param textPane      The JTextPane under consideration
   * @param text          Text to be inserted
   * @param addTime       If true, adds Current time after the text String 
   */
  public static void addText(JTextPane textPane, String text, boolean addTime) {
     setDefaultStyle(false);
     addText(textPane, text, 
             defaultFamily, defaultType, defaultSize, 
             defaultForeground, defaultBackground, 0, addTime);
  } 
  /**
   * Add styled text in the JTextPane
   * @param textPane      The JTextPane under consideration
   * @param text          Text to be inserted
   * @param fontFamily    Text Font Family (Helvetica, Times etc.)
   * @param fontType      Text Font Type   (Plain, Bold, Italic etc.)
   * @param fontSize      Text Font Size   (8 ... 24)
   * @param fgColor       Text foreground color
   */
  public static void addText(JTextPane textPane, String text, String fontFamily, 
                             String fontType, int fontSize, Color fgColor) {
     setDefaultStyle(false);
     addText(textPane, text, 
             fontFamily, fontType, fontSize, 
             fgColor, defaultBackground, 0, false);
  }
  /**
   * Add styled text in the JTextPane
   * @param textPane      The JTextPane under consideration
   * @param text          Text to be inserted
   * @param fontFamily    Text Font Family (Helvetica, Times etc.)
   * @param fontType      Text Font Type   (Plain, Bold, Italic etc.)
   * @param fontSize      Text Font Size   (8 ... 24)
   * @param fgColor       Text foreground color
   * @param bgColor       Text background color
   * @param nBells        # of times System bell to be rung
   * @param addTime       If true, adds Current time after the text String 
   */
  public static void addText(JTextPane textPane, String text, String fontFamily, 
                             String fontType, int fontSize, Color fgColor, 
                             Color bgColor, int nbells, boolean addTime) {
     if (!isDefaultStyle()) 
       changeStyle(fontFamily, fontType, fontSize, fgColor, bgColor);
     if (text != null) {
       if (addTime) {
         calendar = new GregorianCalendar();
         text = text + " (" + calendar.getTime() + ")";
       }
       insertText(textPane, text, fontColorAttribute);
     }
     if (nbells> 0) ringBell(nbells);
  }
  /**
   * Ring system bells 
   * @param nBells    Number of times System bell is rung
   */
  protected static void ringBell(int nBells) {   
    while (nBells-- > 0) {
      Toolkit.getDefaultToolkit().beep();
      try {
	Thread.sleep(BEEP_INTERVAL);
      } 
      catch (InterruptedException e) {};
    } 
  }
  /**
   * Insert (append) styled text in JTextPane
   * @param textPane  The JTextPane under consideration
   * @param text      The text to be appended
   * @param set       The Style attribute (font, size, color etc.) 
   */
  protected static void insertText(JTextPane textPane, String text, AttributeSet set) {
    if (textPane == null) return;
    textPane.setEditable(true);
    try {
      textPane.getDocument().insertString(textPane.getDocument().getLength(), 
            text + "\n", set); 
    } 
    catch (BadLocationException e) {
      e.printStackTrace();
    }
    textPane.setEditable(false);
  }
  /** 
   * Mark the selected text
   * @param textPane  The JTextPane under consideration
   */
  public static void setEndSelection(JTextPane textPane) {
    textPane.setSelectionStart(textPane.getDocument().getLength());
    textPane.setSelectionEnd(textPane.getDocument().getLength()); 
  }
  /** 
   * Add a JLabel component to the JTextPane 
   * @param textPane  The JTextPane under consideration
   * @param labeltext Label String
   * @param iconName  Fully Specified Icon name 
   */
  public static void addLabel(JTextPane textPane, String labelText, String iconName) { 
    if (labelText != null) { 
      textPane.setEditable(true);
      JLabel label = new JLabel(labelText); 
      if (iconName != null) label.setIcon(new ImageIcon(iconName));
      label.setOpaque(true);
      if (textPane != null)   {
        try {
           textPane.insertComponent(label);
        }
        catch (Exception e) {
          System.out.println(e);
        }
      }
      textPane.setEditable(false);
    } 
    else { 
      Toolkit.getDefaultToolkit().beep();
    } 
  } 
  /** 
   * Add an icon to the JTextPane 
   * @param textPane  The JTextPane under consideration
   * @param iconName  Fully Specified Icon name 
   */
  public static void addIcon(JTextPane textPane, String iconName) { 
    if (iconName != null) { 
      textPane.setEditable(true);
      Icon icon = new ImageIcon(iconName);
      if (textPane != null)   {
        try {
          textPane.insertIcon(icon);
        }
        catch (Exception e) {
          System.out.println(e);
        }
      }
      textPane.setEditable(false);
    } 
    else { 
      Toolkit.getDefaultToolkit().beep();
    } 
  } 
  public static void warn(JTextPane textPane, final String message) {
    warn(textPane, message, Color.black);
  }
  public static void warn(final JTextPane textPane, final String message, final Color color) {
    Runnable setTextRun = new Runnable() {
      public void run () {
        try {
          History.addText(textPane, message, color);
        }
        catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    };
    SwingUtilities.invokeLater(setTextRun);
  }
  /** Test the utility in a standalone mode */
  public static void main(String [] argv) {
    JTextPane textPane;
    JFrame f = new JFrame();
    Container content = f.getContentPane();
    content.add(new JScrollPane(textPane = new JTextPane()));

    History history = History.Instance();
    history.setEndSelection(textPane);
    history.addText(textPane, "Hello world");
    history.addText(textPane, "Hello world", true);
    history.addText(textPane, "Hello world\n", "Times", "Bold", 10, Color.red);
    history.addText(textPane, 
          "Hello world\n", "Times", "Italic", 10, Color.green, Color.yellow, 0, true);
    history.addText(textPane, "Hello world\n", "Times", "Underline", 10, Color.blue);
    history.setEndSelection(textPane);
    history.addLabel(textPane, "Hello world\n", AppConstants.iconDir+"/file_open.gif");
    try {
      history.addIcon(textPane, AppConstants.iconDir+"/file_open.gif");
    } 
    catch (Exception e) {
      System.out.println(e);
    }
    textPane.setEditable(false);
    f.setSize(600,300);
    f.setVisible(true);
  }
}
