package config.util;

import java.awt.Component;
import java.awt.Dimension;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.BorderFactory;

/**
 * <P>A utility class to augment displaying short help text in a window.
 * Help text can be supplied as an html file or as html text embedded  
 * in code. Naturally the first option gives more flexibility although
 * a little inefficient.
 *
 * <P> The help window is a JOptionPane with an embedded panel and
 * a custom icon.
 * 
 * @author Subir Sarkar
 * @version 1.0  06/11/2001
 */
public class AboutDialog  {
    /** Parent object, preferably a Component */
  private Component parent;
    /** Custom icons */
  private Icon icon;
    /** Help text */
  private String message;
    /** If help is in a file, the File object */
  private File file;
    /** The app the help is all about */
  private String aboutWhat;
    /** Whether help text is in a file */
  private boolean isFile;
    /** Text area which displays the help text ultimately */
  private TextPanel textPanel;

    /** Debug flag */
  public static final boolean DEBUG = false;
    /** Default icon */
  private static final Icon helpIcon 
    = new ImageIcon(AppConstants.iconDir+"edit_about.gif");

  /** Construct the dialog window with the help message 
   *  @param parent  The parent window
   *  @param message The help text in html format
   */
  public AboutDialog (Component parent, String message) {
    this(parent, message, "About the Application ...");
  }
  /** Construct the dialog window with the help message 
   *  @param parent  The parent window
   *  @param message The help text in html format
   *  @param aboutWhat  The name of the application the help is about
   */
  public AboutDialog (Component parent, String message, String aboutWhat) {
    this(parent, message, aboutWhat, helpIcon);
  }
  /** Construct the dialog window with the help message 
   *  @param parent  The parent window
   *  @param message The help text in html format
   *  @param aboutWhat  The name of the application the help is about
   *  @param icon  The custom icon
   */
  public AboutDialog (Component parent, String message, String aboutWhat, Icon icon) {
    this.message   = message;
    this.aboutWhat = aboutWhat;
    this.icon      = icon;
    isFile         = false;
    initDisplay();
  }
  /** Construct the dialog window with the help text taken from a file
   *  @param parent  The parent window
   *  @param file    The help file in html format
   */
  public AboutDialog (Component parent, File file) {
    this(parent, file, "About the Application ...");
  }
  /** Construct the dialog window with the help text taken from a file
   *  @param parent  The parent window
   *  @param aboutWhat  The name of the application the help is about
   *  @param file    The help file in html format
   */
  public AboutDialog (Component parent, File file, String aboutWhat) {
    this(parent, file, aboutWhat, helpIcon);
  }
  /** Construct the dialog window with the help text taken from a file
   *  @param parent  The parent window
   *  @param aboutWhat  The name of the application the help is about
   *  @param file    The help file in html format
   *  @param icon  The custom icon
   */
  public AboutDialog (Component parent, File file, String aboutWhat, Icon icon) {
    this.file      = file;
    this.aboutWhat = aboutWhat;
    this.icon      = icon;
    isFile         = true;
    initDisplay();
  }
  /** Setup help text, place in the text area */
  protected void initDisplay() {
    textPanel = new HtmlPanel();
    textPanel.setBorder(BorderFactory.createLoweredBevelBorder());
    // Font size is set in the help file for html
    if (isFile) {
      readFile();
      textPanel.setWindowSize(new Dimension(500, 500));
    }
    else {
      setText(); 
      textPanel.setWindowSize(new Dimension(500, 230));
    }
  }
  /** Now make visible within a JOptionPane */
  public void show() {      
    JOptionPane.showMessageDialog(parent, 
         textPanel, aboutWhat, JOptionPane.INFORMATION_MESSAGE, icon);
  }
  /** For help embedded in code, create a temporary html file, with header and footer */
  public String getHeader() {
    return "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2 Transitional//EN\">\n"+
           "<HTML>\n<HEAD><TITLE> "+ aboutWhat + " </TITLE></HEAD>\n<BODY>\n\n"+
           "<H4>"+aboutWhat+"</H4>\n";
  }
  /** For help embedded in code, create a temporary html file, with header and footer */
  public String getFooter() {
    return "\n\n</BODY>\n</HTML>";
  }
  /** Read the content of the file and place them inside the text area */
  public void readFile() {
     FileReader reader = null;
     try  {
       reader = new FileReader(file);
       textPanel.read(reader);
       reader.close();
     }   
     catch (IOException ex)  {
       JOptionPane.showMessageDialog(null,
        "File Not Found", "ERROR", JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
     }
     finally {
       if (reader != null) {
         try {
           reader.close();
         } 
         catch (IOException x) {}
       }
     } 
  }
  /** Create an temporary html component from help messages */
  public void setText() {
    StringBuilder buf = new StringBuilder(AppConstants.SMALL_BUFFER_SIZE);
    buf.append(getHeader());
    
    buf.append(message);
    buf.append(getFooter());
    if (DEBUG) System.out.println(buf.toString());
    textPanel.setText(buf.toString());
  }
  public void setSize(final Dimension size) {
    textPanel.setWindowSize(size);
  }
  /** Test the class in a stanalone manner*/
  public static void main(String [] argv) {
    String dataPath =  Tools.getEnv("SVTMON_DIR")+"/help/";
    AboutDialog dialog = 
      new AboutDialog(null, new File(dataPath+"Welcome.html"),
             "About Spy Monitoring Program");
    dialog.show();
  }
}
