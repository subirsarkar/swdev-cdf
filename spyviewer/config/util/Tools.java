package config.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.DataOutputStream;
import java.io.PrintWriter;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.Reader;
import java.io.FileReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

import java.util.Collection;
import java.util.Vector;
import java.util.Properties;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;

import java.awt.Event;
import java.awt.Color;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Component;

import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.JSplitPane;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JMenuItem;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingConstants;
import javax.swing.KeyStroke;
import javax.swing.JLabel;
import javax.swing.BorderFactory;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyleConstants;
import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;

import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.border.Border;

import java.security.MessageDigest;

import com.smartsockets.TipcSrv;
import com.smartsockets.TipcSvc;
import com.smartsockets.TipcException;
import com.smartsockets.TipcMt;
import com.smartsockets.TipcMsg;
import com.smartsockets.Tut;

import config.SvtCrateMap;

 /**
  *  Provides useful tools to accomplish common tasks
  * 
  *  @version 0.2   March 13, 2000
  *  @author  Subir Sarkar
  */
public class Tools  {
  public static final int MAXLEN  = AppConstants.LARGE_BUFFER_SIZE; 
  public static final String COMMENT_STRING = new String("//");
  public static final String lafNames [] = 
    {
      "Java/Metal", 
      "Cross-platform", 
      "System", 
      "GTK", 
      "Motif" 
    };

  public static final String [] lafClassNames = 
    {
      "javax.swing.plaf.metal.MetalLookAndFeel",
      UIManager.getCrossPlatformLookAndFeelClassName(),
      UIManager.getSystemLookAndFeelClassName(),
      "com.sun.java.swing.plaf.gtk.GTKLookAndFeel",
      "com.sun.java.swing.plaf.motif.MotifLookAndFeel"
    };

   /**  
    * Prepare   an integer mask
    * @param    nbit    Make all the nbit bits equal to 1
    * @return   The mask as integer
    */ 
  public static int getMask(int nbit) {
    int mask = 0;
    for (int i = 0; i < nbit; i++)
      mask = (mask << 1) | 1;
    return mask;
  }
   /** 
    * Get as many blanks as you need 
    * @param   n   # of spaces
    * @return  The blank String of desired length
    */
  public static String getSpaces(int n) {
     StringBuilder sb = new StringBuilder(n);
     for (int i = 0; i < n; i++) sb.append(' ');
     return sb.toString();
  }
   /** 
    * Create a JCheckbox and return the reference to it
    * 
    * @param   label       String label attached to the checkbox and action command
    * @param   decision    Selected at initialisation or not
    * @return  The reference to the checkbox
    */
  public static JCheckBox createCBox (String label, boolean decision) {
    return createCBox(label, label, decision);         
  }
   /** 
    * Create a JCheckbox and return the reference to it
    * 
    * @param   label       String label attached to the checkbox
    * @param   command     Command to be executed
    * @param   decision    Selected at initialisation or not
    * @return  The reference to the checkbox
    */
  public static JCheckBox createCBox (String label, String command, boolean decision) {
    JCheckBox checkB = new JCheckBox(label, decision);   
    checkB.setActionCommand(command);
    if (decision)
      checkB.setForeground(Color.black);
    else
      checkB.setForeground(Color.gray);
    return checkB;         
  }
   /** 
    * Create a JButton and return a reference to it
    * 
    * @param label  String label attached to the button and action command
    * @return the reference to the button
    */
  public static JButton createButton (String label) {
    return createButton (label, label);
  }
   /** 
    * Create a JButton and return a reference to it
    * 
    * @param label       String label attached to the button
    * @param command     Command to be executed
    * @return the reference to the button
    */
  public static JButton createButton (String label, String command) {
    JButton button = new JButton(label);
    button.setActionCommand(command);
    return button;
  }
   /** 
    * Create JRadioButton and return a reference to it
    * 
    * @param label       String label attached to the checkbox and action command
    * @param group       Buttongroup to hold the radiobutton
    * @param selected    Selected at initialisation or not
    */
  public static JRadioButton radioButton(String label, ButtonGroup group, 
                                   boolean selected) {
     return radioButton(label,label,group,selected);
  }
   /** 
    * Create JRadioButton and return a reference to it
    *  
    * @param label       String label attached to the checkbox
    * @param command     Command string associtated with the radiobutton
    * @param group       Buttongroup
    * @param selected    Selected at initialisation or not
    */
  public static JRadioButton radioButton(String label, String Command, 
                 ButtonGroup group, boolean selected) {
    JRadioButton radioButton  =  new JRadioButton(label);
    radioButton.setHorizontalTextPosition(SwingConstants.RIGHT);
    radioButton.setSelected(selected);
    radioButton.setActionCommand(label);
    group.add(radioButton);
    return radioButton;
  }
   /** 
    * Create JMenuItem and return a reference to it
    *
    * @param label       String label attached to the checkbox
    * @param iconFile    Name of the Icon file
    * @param keyCode     Key accelerator code
    */
  public static JMenuItem menuItem (String label, String iconFile, char mnemonic, int keyCode) {
    JMenuItem menu = new JMenuItem(label);
    Icon myIcon    = new ImageIcon(iconFile);
    menu.setIcon(myIcon);
    menu.setHorizontalTextPosition(SwingConstants.RIGHT);
    //menu.setMnemonic(mnemonic);   // Obsolete
    menu.setMnemonic(keyCode);
    menu.setAccelerator(KeyStroke.getKeyStroke(keyCode, KeyEvent.CTRL_MASK));
    menu.setActionCommand(label);
    return menu;
  }
   /** 
    * Create JCheckBoxMenuItem and return a reference to it
    * 
    * @param label       String label attached to the checkbox
    * @param iconFile    Name of the Icon file
    * @param isSel       Selected at initialisation or not
    * @param keyCode     Key accelerator code
    */
  public static  JCheckBoxMenuItem checkBoxItem (String label, String iconFile, 
                                          boolean isSel, int keyCode) {
    Icon myIcon;
    if (iconFile.equals("")) {
      myIcon = null;   
    } else { 
      myIcon = new ImageIcon(iconFile);
    }
    JCheckBoxMenuItem cbItem = new JCheckBoxMenuItem(label, myIcon, isSel);
    cbItem.setMnemonic(keyCode);
    cbItem.setAccelerator(KeyStroke.getKeyStroke(keyCode, KeyEvent.CTRL_MASK));
    cbItem.setHorizontalTextPosition(SwingConstants.RIGHT);
    cbItem.setActionCommand(label);
    return cbItem;
  }

   /** 
    * Create JRadioButtonMenuItem and return a reference to it
    *
    * @param label       String label attached to the checkbox
    * @param group       Buttongroup
    * @param selected    Selected at initialisation or not
    * @param color       Fore/Background color
    * @param keyCode     Key accelerator code
    * @param opt         Choose Fore/Background
    */
  public static JRadioButtonMenuItem radioButtonItem(String label, ButtonGroup group, 
                               boolean selected, Color color, int keyCode, int opt) {
    JRadioButtonMenuItem rbItem  =  new JRadioButtonMenuItem(label);
    if (opt == 0) {
      rbItem.setForeground(color);
    } 
    else {
      rbItem.setBackground(color);
    }
    rbItem.setMnemonic(keyCode);
    rbItem.setAccelerator(KeyStroke.getKeyStroke(keyCode, KeyEvent.CTRL_MASK));
    rbItem.setHorizontalTextPosition(SwingConstants.RIGHT);
    rbItem.setSelected(selected);
    rbItem.setActionCommand(label);
    group.add(rbItem);

    return rbItem;
  }
  public static JLabel createLabel() {
    return createLabel("");
  }
  public static JLabel createLabel(String text) {
    JLabel label = new JLabel(text);
    label.setForeground(Color.black);
    return label;
  }
  public static JLabel createLabel(String text, Icon icon, int adj, Color fg, Border border) {
    JLabel label = new JLabel(text, icon, adj);
    label.setForeground(fg);
    if (border != null) label.setBorder(border);
    return label;
  }
   /** 
    * Read data from a file and fill an integer array 
    * @param  filename     File to be read
    * @return an integer array holing the content of the file
    */
  public static int [] readFile(String filename) {
    BufferedReader input = openInputFile(filename);
    int [] data = new int[MAXLEN];
    if (input == null)   return data;
    String line = null;
    int    nline = 0;
    try {
      while (true) {
        line = getNextLine(input); 
        if (nline > MAXLEN-1) break;
        data[nline++] = hex2Int(line);
      }
    } 
    catch (IOException e) {
      try {   
        input.close();
      } 
      catch (IOException ex) {
        System.out.println("Error closing file " + ex);
      }
    }
    catch (NullPointerException e) {
      try {   
        input.close();
      } 
      catch (IOException ex) {
        System.out.println("Error closing file " + ex);
      }
    }
    finally {
      try {   
        input.close();
      } 
      catch (IOException ex) {
        System.out.println("Error closing file " + ex);
      }
    }
    return data;
  }
   /** 
    * Read data from a file and fill an long array 
    *
    * @param   filename    File to be read
    * @return an long array holing the content of the file
    */
  public static long [] readLongFromFile(String filename) {
    BufferedReader input = openInputFile(filename);
    long [] data = new long[MAXLEN];
    if (input == null)   return data;
    String line = null;
    int    nline = 0;
    try {
      while (true) {
        line = getNextLine(input); 
        if (nline > MAXLEN-1) break;
        data[nline++] = hex2Long(line);
      }
    } 
    catch (IOException e) {
      try {   
        input.close();
      } 
      catch (IOException ex) {
        System.out.println("Error closing file " + ex);
      }
    }
    catch (NullPointerException e) {
      try {   
        input.close();
      } 
      catch (IOException ex) {
        System.out.println("Error closing file " + ex);
      }
    }
    finally {
      try {   
        input.close();
      } 
      catch (IOException ex) {
        System.out.println("Error closing file " + ex);
      }
    }
    return data;
  }
   /** 
    * Write data from an array to a file 
    *
    * @param    filename  Output file
    * @param    data      Data array to be saved in the file 
    */
  public static void writeFile(String filename, int [] data) {
    DataOutputStream output = openOutputFile(filename);
    try {
      for (int i = 0; i < data.length; i++)
        output.writeBytes(Integer.toHexString(data[i]) + "\r\n");
    } 
    catch (IOException e) {
      try {   
        output.close();
      } 
      catch (IOException ex) {
        System.out.println("Error closing file " + ex);
      }
    }
    catch (NullPointerException e) {
      try {   
        output.close();
      } 
      catch (IOException ex) {
        System.out.println("Error closing file " + ex);
      }
    }
    finally {
      try {   
        output.flush();
        output.close();
      } 
      catch (IOException ex) {
        System.out.println("Error closing file " + ex);
      }
    }
  }
  public static void writeFile(String filename, String line) {
    DataOutputStream output = openOutputFile(filename);
    try {
      output.writeBytes(line);
    } 
    catch (IOException e) {
      try {   
        output.close();
      } 
      catch (IOException ex) {
        System.out.println("Error closing file " + ex);
      }
    }
    catch (NullPointerException e) {
      try {   
        output.close();
      } 
      catch (IOException ex) {
        System.out.println("Error closing file " + ex);
      }
    }
    finally {
      try {   
        output.flush();
        output.close();
      } 
      catch (IOException ex) {
        System.out.println("Error closing file " + ex);
      }
    }
  }
   /** 
    * Get a slice of a Vector 
    * 
    * @param    vec       Original Vector 
    * @param    start     First index
    * @param    end       last index
    * @return   a Vector
    */
  public static Vector<Object> getVectorSlice(Vector<Object> vec, int start, int end) {
    Vector<Object> newVec = new Vector<Object>();
    for (int i = start; i <= end; i++)
      newVec.addElement(vec.elementAt(i));

    return newVec;
  }

   /**
    * Split a string to an array of strings
    * 
    * @param string     The input string
    * @param token      The separating character
    * @return A String array with words
    */
  public static String[] split(String line) {
    return Tools.split(line, " ");
  }
  public static String[] split(String line, String token) {
    StringTokenizer t = new StringTokenizer(line, token);
    int ntoken = t.countTokens();
    String [] newString = new String[ntoken];
    int i = 0;
    while (t.hasMoreTokens()) {
      newString[i++] = t.nextToken();
    }
    return newString;
  }

  /**
   * Fills all the GridBagConstraints variables in a single call.
   */
  public static void buildConstraints(GridBagConstraints gbc, 
                                  int gx, int gy,
                                  int gw, int gh, 
                                  double wx, double wy, 
                                  int anchor, int fill) {
    gbc.gridx      = gx;
    gbc.gridy      = gy;
    gbc.gridwidth  = gw;
    gbc.gridheight = gh;
    gbc.weightx    = wx;
    gbc.weighty    = wy;
    gbc.anchor     = anchor;
    gbc.fill       = fill;
  }
  /**
   * Method to write a hex-array into a new file
   * 
   * @param file  The file to which to save
   * @param dataArray The array to save
   */
  public static void saveHexToFile(File file, int[] dataArray){
    saveHexToFile(file, dataArray, 0xffffffff);
  }
  /**
   * Method to write a hex-array into a new file
   * @param file      The file to which to save
   * @param dataArray The array to save
   * @param mask      A mask to apply on the data words
   */
  public static void saveHexToFile(File file, int [] dataArray, int mask){
    int len = dataArray.length;
    if (len <= 0) return;

    PrintWriter pout = null;
    try {
      pout = new PrintWriter(new BufferedOutputStream(new FileOutputStream(file)));
      for (int i = 0; i < len; i++) {
        pout.println(Integer.toHexString(dataArray[i] & mask));
      }
    } 
    catch (IOException ex) {
      System.err.println("Error writing the file: "+ex.getMessage());
    } 
    finally {
      pout.flush(); 
      pout.close();
    }
  }
  /**
   * Method to count the line of a file
   * @param file  The file to be counted
   * @return The number of lines
   */
  public static int countLine(File file) {
    int fileLength = 0;
    BufferedReader is = null;
    try {
      is = new BufferedReader(new FileReader(file));
      String line = is.readLine();
      while (line != null) {
        String stringData = line.trim();
        if (!stringData.startsWith(COMMENT_STRING)) {
          try {
            Integer.parseInt(stringData, 16);
            fileLength++;
          } 
          catch (NumberFormatException ex) {
            System.out.println(ex.getMessage());
          }
        }
        line = is.readLine();
      }
    } 
    catch (IOException e) {
      System.out.println("Trouble reading the file.");
      return 0;
    } 
    finally {
      try {
        is.close();
      } 
      catch (IOException e) {
      }
    }
    return fileLength;
  }

  /**
   * Method to load a hex file of unknown length. Lines starting with //
   * will be discarded.
   * @param file  The file to be loaded
   * @return returns the array 
   */
  public static int[] loadHexFile(final File file) {
    int fileLength = countLine(file);
    int [] fileData = new int[fileLength];
    BufferedReader is = null;

    try {
      int i = 0;
      is = new BufferedReader(new FileReader(file));
      String line = is.readLine();
        while (line != null) {
          String stringData = line.trim();
          if (!stringData.startsWith(COMMENT_STRING)) {
            try {
              fileData[i] = Integer.parseInt(stringData, 16);
              i++;
            } 
            catch (NumberFormatException err) {;}
          }
          line = is.readLine();
       }
    } 
    catch (IOException e) {
      System.err.println("Trouble reading the file.");
      return null;
    } 
    finally {
      try {
        is.close();
      } 
      catch (IOException e) {
      }
    }
    return fileData;
  }

  /**
   * Method to load a hex file of known length. It is expected that each line 
   * represents one hexadecimal integer. Lines starting with //
   * will NOT be discarded, but will give an error.
   * @param file        The file to be loaded.
   * @param fileLength  The number of lines/numbers.
   * @return            returns the array.
   */
  public static int [] loadHexFile(final File file, int fileLength) {
    int [] fileData = new int[fileLength];
    BufferedReader is = null;

    try {
      int i = 0;
      is = new BufferedReader(new FileReader(file));
      String line = is.readLine();
      while ((line != null)&(i<fileLength)) {
        fileData[i] = Integer.valueOf(line, 16).intValue();
        i++;
        line = is.readLine();
      }
    } 
    catch (IOException e) {
      System.err.println("Trouble reading the file.");
      return null;
    } 
    finally {
      try {
        is.close(); 
      } 
      catch (IOException e) {
      }
    }
    return fileData;
  }
   /**
    * Get environmental variable specified by String var
    * with system command, env
    * @param var Environment variable name
    */
  public static String getEnv(String var)  {
    Properties env = new Properties();
    String envCmd[] = {"/bin/sh", "-c", "/usr/bin/env"};
    try {
      Process envP = Runtime.getRuntime().exec(envCmd);
      try  {
        env.load(envP.getInputStream());
      }
      catch (IOException e)  {
        System.out.println("Error reading environmental variables");
        e.printStackTrace();
      }
      envP.destroy();
    }
    catch (IOException e)  {
      System.out.println("Error executing env. Exc: = " + e);
      e.printStackTrace();
    }
    return env.getProperty(var);
  }
   /**
    * Get current directory using system command, pwd
    */
  public static String getPWD()  {
    Properties prop = System.getProperties();
    return prop.getProperty("user.dir");
  }

   /** 
    * Execute system command must be given in absolute path, 
    * with optional output
    *
    * @param cmdStr String of unix command to be executed
    * @param withOutput Print to stdout for debug
    */
  public static void execSys(String cmdStr, boolean withOutput)  {
    String cmd[] = new String [3];
    // use bourne shell to interpret the command
    cmd[0] = "/bin/sh";
    cmd[1] = "-c";
    // actual command must be in absolute path
    cmd[2] = cmdStr;
    try  {
      Process cmdP = Runtime.getRuntime().exec(cmd);
      BufferedReader in = new BufferedReader
        (new InputStreamReader(cmdP.getInputStream()));

      String tmpS = new String();
      try  {
        while ((tmpS = in.readLine()) != null)
          if (withOutput) System.out.println(tmpS);
      }
      catch (IOException e)  {
        in.close();
      }
      cmdP.destroy();
    }
    catch (IOException e)  {
      e.printStackTrace();
      System.out.println("System command execution failed.");
    }
  }
   /**
    * Convert hex string to integer (32 bits)
    * @param hval  String of number in hex format
    */
  public static int hex2Int(String hval) {
    return (int) Long.parseLong(hval, 16);
  }

   /**
    * Convert hex string to Short (16 bits)
    * @param hval  String of number in hex format
    */
  public static short hex2Short(String hval) {
    return (short) Long.parseLong(hval, 16);
  }

   /**
    * Convert hex string to Byte (8 bits)
    * @param hval  String of number in hex format
    */
  public static byte hex2Byte(String hval) {
    return (byte) Long.parseLong(hval, 16);
  }

   /**
    * Convert hex string to long (64 bits)
    * @param hval  String of number in hex format
    */
  public static long hex2Long(String hval) {
    long data;
    if (hval.length() < 16) {
      data = Long.parseLong(hval, 16);
    }
    else {
      // Split the number into 2
      String upperS = new String(hval.substring(0, 8));    // takes chars 0 - 7
      String lowerS = new String(hval.substring(8,16));   // takes chars 8 - 15
      long upper = Long.parseLong(upperS, 16);
      long lower = Long.parseLong(lowerS, 16);
      data = (upper << 32) + lower;
    }
    return data;
  }
  
  /** This function left pads a String with blanks 
   *  @param String String to pad
   *  @param int desired width of string
   *  @return Padded String
   */
  public static String padString(String inString, int strWidth) {
    return padString(inString, strWidth, ' ');
  }
  /** This function left pads a String with the given character 
   *  @param String String to pad
   *  @param int desired width of string
   *  @param Character Padding Character
   *  @return Padded String
   */

  public static String padString(String inString, int strWidth, char padder) {
    String outString = inString.trim();
    for (int i=inString.trim().length(); i<strWidth; i++)
      outString = new String(padder + outString);
    return outString;
  }
  
   /** This function accepts a string and returns the boolean value
    *  corresponding to the first word of the string.
    *  @param line The string which is converted to a boolean
    */
  public static boolean getBoolean(final String line) {
    String state;
    StringTokenizer stringTokenizer = new StringTokenizer(line);

    // get the first token of the string (the value to convert to an int)
    state = stringTokenizer.nextToken();

    return (Boolean.valueOf(state).booleanValue());
  }
   /** This function accepts a string and returns the value of the 
    *  integer at the beginning of the string.  It's included to 
    *  make the calls that read the board's configuration file
    *  a little more readable
    *
    * @param line The string which is converted into an integer
    */
  public static int getInteger(final String line) {
    String number;
    StringTokenizer stringTokenizer = new StringTokenizer(line);

    // get the first token of the string (the value to convert to an int)
    number = stringTokenizer.nextToken();

    // convert the string to an int and return the value
    return Integer.valueOf(number).intValue();
  }
   /** This function accepts a string and returns the value of the 
    *  short at the beginning of the string.  It's included to 
    *  make the calls that read the board's configuration file
    *  a little more readable
    *
    *  @param String line
    */
  public static short getShort(final String line) {
    String number;
    StringTokenizer stringTokenizer = new StringTokenizer(line);

    // get the first token of the string (the value to convert to an int)
    number = stringTokenizer.nextToken();

    // convert the string to an int and return the value
    return Short.valueOf(number).shortValue();
  }
   /** This function accepts a string and returns the value of the 
    *  byte at the beginning of the string.  It's included to 
    *  make the calls that read the board's configuration file
    *  a little more readable
    *
    *  @param String line
    */
  public static byte getByte(final String line) {
    String number;
    StringTokenizer stringTokenizer = new StringTokenizer(line);

    // get the first token of the string (the value to convert to an int)
    number = stringTokenizer.nextToken();

    // convert the string to an byte and return the value
    return Byte.valueOf(number).byteValue();
  }
   /** This function accepts a String line and returns the value of the 
    *  String at the beginning of the line.  It's included to 
    *  make the calls that read the board's configuration file
    *  a little more readable
    *
    *  @param String line
    */
  public static String getString(final String line) {
    StringTokenizer stringTokenizer = new StringTokenizer(line);

    // get the first token of the string (the string we want)
    return stringTokenizer.nextToken();
  }

   /**
    * Open file for output. This is just a convenience method so
    * everyone doesnt' have to catch the same silly errors
    *
    * @param filename  The file name to open 
    */
  public static DataOutputStream openOutputFile(String filename) {
    DataOutputStream output = null;
    try {
        // Open the file
      File file = new File(filename);
        // create the stream
      output = new DataOutputStream(new BufferedOutputStream
                                   (new FileOutputStream(file)));
    }
    catch (FileNotFoundException e) {
      System.out.println("The file " + filename + " wasn't found!");
    }
    catch (IOException e) {
      System.out.println("Error reading from " + filename);
    }
    catch (NullPointerException e) {} // in case no file is passed

    // Return the output ready for writing
    return output;
  }
   /**
    * Open file for output with option to append.
    * everyone doesnt' have to catch the same silly errors
    *
    * @param filename  The file name to open 
    * @param append  True to append
    */
  public static DataOutputStream openOutputFile(String filename,
                                                boolean append) {
    DataOutputStream output = null;
    try {
      // create the stream for append
      output = new DataOutputStream(new BufferedOutputStream(new 
                             FileOutputStream(filename, append)));
    }
    catch (FileNotFoundException e) {
      System.out.println("The file " + filename + " wasn't found!");
    }
    catch (IOException e) {
      System.out.println("Error reading from " + filename);
    }
    catch (NullPointerException e) {} //in case no file is passed

    // Return the output ready for writing
    return output;
  }
   /**
    * Open file for input.  This is just a convenience method so
    * everyone doesnt' have to catch the same silly errors.
    * This opens a file to read characters
    *
    * @param filename  The file name to open 
    */
  public static BufferedReader openInputFile(final String filename) {
    return openInputFile(new File(filename));
  }
  public static BufferedReader openInputFile(final File file) {
    BufferedReader input = null;
    try {
      // Open the file
      input = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
    }
    catch (FileNotFoundException e) {
      System.out.println("The file " + file.getName() + " wasn't found!");
    }
    catch (IOException e) {
      System.out.println("Error reading from " + file.getName());
    }
    return input;
  }
   /**
    * Open file for input at the byte level.  This can be used for reading
    * binary or character data
    *
    * @param filename  The file name to open 
    */

  public static BufferedInputStream openRawInputFile(String filename) {
    BufferedInputStream input = null;
    try {
      // Open the file
      input = new BufferedInputStream(new FileInputStream(filename));
    }
    catch (FileNotFoundException e) {
      System.out.println("The file " + filename + " wasn't found!");
    }
    catch (IOException e) {
      System.out.println("Error reading from " + filename);
    }
    return input;
  }
  public static EtchedBorder etchedBorder() {
    return new EtchedBorder(EtchedBorder.LOWERED,
                                 Color.black, Color.lightGray.brighter());
  }

  public static String getNextLine(BufferedReader reader) 
    throws  IOException, NullPointerException 
  {
    String in = null;
    StringTokenizer line;
    int index, i, nwords;
    
    do {
      in = reader.readLine();
      if (in == null) break;
      index = in.indexOf(COMMENT_STRING);       // Strip comments
      if (index != -1)   
        in = in.substring(0, index);
      line = new StringTokenizer(in);
      nwords = line.countTokens();
    } 
    while (nwords == 0);       // Skip Blank lines

    return in;
  }
  public static String [] tokenizeNextLine(BufferedReader reader) 
       throws  IOException, NullPointerException {
    StringTokenizer line;
    int nwords;
    String words[];
 
    line = new StringTokenizer(getNextLine(reader));
    nwords = line.countTokens();
    words = new String[nwords];
    // Parse line into constituent words
    for (int i = 0; i < nwords; i++)
      words[i] = line.nextToken();

    return words;
  }
  /**
   * Code to append file to another file
   * @param fromName Source file name
   * @param toName Destination file name
   */
  public static void append(String fromName, String toName) {
    byte[] buff = new byte[4096];

    BufferedInputStream in = openRawInputFile(fromName);
    DataOutputStream out   = openOutputFile(toName, true);

    while (true) {
      try {
        int iRead = in.read(buff);
        if (iRead < 0)
          break;
        out.write(buff, 0, iRead);
      } 
      catch (IOException e) {
        break;
      }
    }
    try {
      out.flush();
    } 
    catch (IOException e) {}
  }
  /**
   * Code to append file to an already open stream
   * @param fromName Source file name
   * @param out Destination stream
   */
  public static void append(String fromName, DataOutputStream out) {
    byte[] buff = new byte[4096];
    BufferedInputStream in = openRawInputFile(fromName);
    while (true) {
      try {
        int iRead = in.read(buff);
        if (iRead < 0)
          break;
        out.write(buff, 0, iRead);
      } 
      catch (IOException e) {
        break;
      }
    }
    try {
      out.flush();
    } catch (IOException e) {}
  }

  public static TitledBorder etchedTitledBorder(String title, Color color) {
    return Tools.etchedTitledBorder(title, TitledBorder.LEFT, TitledBorder.TOP,
                "SansSerif", Font.BOLD, 12, color);
  }
  public static TitledBorder etchedTitledBorder(String title) {
    return Tools.etchedTitledBorder(title, TitledBorder.LEFT, 12);
  }
  public static TitledBorder etchedTitledBorder(String title, int size) {
    return Tools.etchedTitledBorder(title, TitledBorder.CENTER, size);
  }
  public static TitledBorder etchedTitledBorder(String title, int adj, int size) {
    return Tools.etchedTitledBorder(title, adj, TitledBorder.TOP,
                "SansSerif", Font.BOLD, size, Color.black);
  }
  public static TitledBorder etchedTitledBorder(String title, int adj, int pos, 
         String family, int type, int size, Color color) {
    return BorderFactory.createTitledBorder(
           BorderFactory.createEtchedBorder(), title, adj,
               pos, new Font(family, type, size), color);
  }
  public static Border linedTitledBorder(String title) {
    return BorderFactory.createTitledBorder(
           BorderFactory.createLineBorder(Color.black), title, TitledBorder.CENTER,
               TitledBorder.TOP, new Font("SansSerif", Font.PLAIN,12), Color.black);
  }
  public static Border emptyTitledBorder(String title) {
    return BorderFactory.createTitledBorder(
           BorderFactory.createEmptyBorder(), title, TitledBorder.CENTER,
               TitledBorder.TOP, new Font("SansSerif", Font.PLAIN,9), Color.black);
  }
  public static String getTimeString() {
    return (new GregorianCalendar()).getTime().toString();
  }
  public static void ensureEventThread() {
    // throws an exception if not invoked by the event thread
    if (SwingUtilities.isEventDispatchThread()) return;
    throw new RuntimeException("Only the event thread should invoke this method");
  }
    /** 
     * Create a connection to the RTServer and return a reference
     */
  public static TipcSrv getServer() {
    TipcSrv srv = null;
    try {
      Tut.setOption("ss.project", "CDF_DAQ");
      String ssinit = Tools.getEnv("SMARTSOCKETS_CONFIG_DIR");
      if (ssinit != null) 
        Tut.loadOptionsFile(ssinit+"/java.cm");
      else
        System.out.println("Cannot read config file....");
  
      /* connect to RTserver */
      srv = TipcSvc.getSrv();
      if (!srv.create())
        Tut.exitFailure("Couldn't connect to RTserver!\n");
      else
        System.out.println("Connected to RT Server...");
    } 
    catch (TipcException e) {
      Tut.fatal(e);
    } 
    return srv;
  }
  public static void sendCommand(final String dest, final String command) {
    Tools.sendCommand(dest, command, false);
  }
  public static void sendCommand(final String dest, final String command, boolean debug) {
    TipcSrv srv = Tools.getServer();
    Tools.sendCommand(srv, dest, command, debug);
  }
  public static void sendCommand(final TipcSrv srv, final String dest, 
                                 final String command, boolean debug) {
    try {
      TipcMsg msg = TipcSvc.createMsg(TipcMt.STRING_DATA);
      msg.setNumFields(0); 
      msg.appendStr(command);
    
      msg.setDest(dest);
      if (debug) msg.print();        
    
      srv.send(msg);
      srv.flush();
      msg.destroy();
    }
    catch (TipcException ex) {
      System.out.println("Tipc Exception occurred: " + ex.getMessage());
      Tut.warning(ex);
    }
  }
  public static void sendCommand(final String dest, final String command, int flag) {
    Tools.sendCommand(dest, command, flag, false);
  }
  public static void sendCommand(final String dest, final String command, 
                                 int flag, boolean debug) {
    TipcSrv srv = Tools.getServer();
    Tools.sendCommand(srv, dest, command, flag, debug);
  }
  public static void sendCommand(final TipcSrv srv, final String dest, 
           final String command, int flag, boolean debug) {
    try {
      TipcMsg msg = TipcSvc.createMsg(TipcMt.NUMERIC_DATA);
      msg.setNumFields(0); 
      msg.appendStr(command);
      msg.appendInt4(flag);
    
      msg.setDest(dest);
      if (debug) msg.print();        
    
      srv.send(msg);
      srv.flush();
      msg.destroy();
    }
    catch (TipcException ex) {
      System.out.println("Tipc Exception occurred: " + ex.getMessage());
      Tut.warning(ex);
    }
  }
  public static void sendFilename(final String filename, final String dest) {
    Tools.sendFilename(filename, dest, false);
  }
  public static void sendFilename(final String filename, final String dest, boolean debug) {
    TipcSrv srv = Tools.getServer();
    Tools.sendFilename(srv, filename, dest, debug);
  }
  public static void sendFilename(final TipcSrv srv, final String filename, 
                                  final String dest, boolean debug) 
  {
    try {
      TipcMsg msg = TipcSvc.createMsg(TipcMt.STRING_DATA);
      msg.setNumFields(0); 
      msg.appendStr("Read");
      if (filename == null) {
        JOptionPane.showMessageDialog(null, "Filename field may not be empty!",
             "Error Message", JOptionPane.ERROR_MESSAGE);
        return;
      }
      msg.appendStr(filename);
    
      msg.setDest(dest);
      if (debug) msg.print();        
    
      srv.send(msg);
      srv.flush();
      msg.destroy();
    }
    catch (TipcException ex) {
      System.out.println("Tipc Exception occurred: " + ex.getMessage());
      Tut.warning(ex);
    }
  }
  public static JSplitPane createSplitPane(int orientation, 
                                 final Component aComp, final Component bComp) 
  {
    JSplitPane splitPane = new JSplitPane(orientation); 
    splitPane.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
    // Should be able to use JSplitPane method getOrientation()
    if (orientation == JSplitPane.HORIZONTAL_SPLIT) {
      splitPane.setLeftComponent(aComp);
      splitPane.setRightComponent(bComp);
    }
    else {
      splitPane.setTopComponent(aComp);
      splitPane.setBottomComponent(bComp);
    }
    splitPane.setOneTouchExpandable(true); 
    splitPane.resetToPreferredSizes(); 
    return splitPane;
  }
  public static void displayText(final JTextComponent textComp, final String str) {
    Reader reader = null;
    try {
      reader = new StringReader(str);
      textComp.read(reader, str);
      reader.close();
    }
    catch (IOException ex) {
      System.out.println("Cannot open StringReader");
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
  public static void showHelp(Component component, final String helpFile, final String aboutApp) {
    final Dimension dialogSize = new Dimension(500, 400);
    AboutDialog dialog = new AboutDialog(component, new File(helpFile), aboutApp);
    dialog.setSize(dialogSize);
    dialog.show();
  }
  public static StringBuilder getFD(final int [] data) {
    StringBuilder sb = new StringBuilder(AppConstants.MEDIUM_BUFFER_SIZE);
    int ncol = 0;
    for (int i = 0; i < data.length; i++) {
      sb.append(AppConstants.h6Format.sprintf(data[i])).append(" ");
      if (ncol++ == 10) {
        sb.append("\n");
        ncol = 0;
      }
    }
    sb.append("\n");

    return sb;
  }
  public static void showMessage(DataFrame f, TipcMsg msg) {
    // Store the content of msg.print(PrintWriter) in a StringBuilder object
    StringWriter bufw = new StringWriter();
    PrintWriter out   = new PrintWriter(bufw);
    msg.print(out);
    f.displayText(bufw.toString());
  }
  public static void loadProperties(final Properties props, final String filename) {
    try  {
      BufferedInputStream in = new BufferedInputStream(new FileInputStream(filename));
      props.load(in);
      in.close();
    } 
    catch (IOException ex) {
      ex.printStackTrace();
    }
  }
  public static void saveProperties(final Properties props, final String filename) {
    Thread runner = new Thread() {
      public void run() {
        try  {
          BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(filename));
          // OLD Message: Deprecated, but nope, I cannot use 'store' as JDK1.1.8 does not support it
          // props.save(out, "Saved from app session");
          props.store(out, "Saved from app session");
          out.close();
        } 
        catch (IOException ex) {
          ex.printStackTrace();
        }
      }
    };
    runner.start();
  }
  public static String getHeader(final String title) {
    StringBuilder buf = new StringBuilder(AppConstants.SMALL_BUFFER_SIZE);

    buf.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">\n"); 
    buf.append("<HTML>\n");
    buf.append("<HEAD><TITLE>" + title + "</TITLE></HEAD>\n");
    buf.append("<BODY BGCOLOR=\"white\" LINK=\"blue\" VLINK=\"red\">\n");

    return buf.toString();
  }
  public static String getFooter() {
    StringBuilder buf = new StringBuilder(AppConstants.SMALL_BUFFER_SIZE);

    buf.append("</BODY>");
    buf.append("</HTML>");

    return buf.toString();
  }
  public static int getCrateIndex(final String crate) {
    return Integer.parseInt(crate.substring(crate.length()-1));
  }
  public static boolean isCrateNameValid(final String crateName) {
    int index = getCrateIndex(crateName);
    if (index < 0 || index > AppConstants.nCrates-1) return false;

    return true;
  }
  public static boolean isCrateReady(final String crateName) {
    if (!isCrateNameValid(crateName)) return false;

    SvtCrateMap map = SvtCrateMap.getInstance();
    if (map == null) return false;

    return map.isCrateReady(crateName);
  }
  /**
   * Utility method for setting the font and color of a JTextPane. The
   * result is roughly equivalent to calling setFont(...) and 
   * setForeground(...) on an AWT TextArea.
   */
  public static void setTextAttributes(JTextPane component, AttributeSet attrs) {
    // Retrieve the pane's document object
    StyledDocument doc = component.getStyledDocument();

    // Replace the style for the entire document. We exceed the length
    // of the document by 1 so that text entered at the end of the
    // document uses the attributes.
    doc.setCharacterAttributes(0, doc.getLength() + 1, attrs, false);
    component.repaint();
  }
  public static void setJTextPaneFont(JTextPane jtp, Font font, Color c) {
    // Start with the current input attributes for the JTextPane. This
    // should ensure that we do not wipe out any existing attributes
    // (such as alignment or other paragraph attributes) currently
    // set on the text area.
    MutableAttributeSet attrs = jtp.getInputAttributes();

    // Set the font family, size, and style, based on properties of
    // the Font object. Note that JTextPane supports a number of
    // character attributes beyond those supported by the Font class.
    // For example, underline, strike-through, super- and sub-script.
    StyleConstants.setFontFamily(attrs, font.getFamily());
    StyleConstants.setFontSize(attrs, font.getSize());
    StyleConstants.setItalic(attrs, (font.getStyle() & Font.ITALIC) != 0);
    StyleConstants.setBold(attrs, (font.getStyle() & Font.BOLD) != 0);

    // Set the font color
    StyleConstants.setForeground(attrs, c);

    // Retrieve the pane's document object
    StyledDocument doc = jtp.getStyledDocument();

    // Replace the style for the entire document. We exceed the length
    // of the document by 1 so that text entered at the end of the
    // document uses the attributes.
    doc.setCharacterAttributes(0, doc.getLength() + 1, attrs, false);
  }
  public static void setLookAndFeel(JFrame frame, final String lafName) throws java.lang.Exception {
    int index = -1;
    for (int i = 0; i < lafNames.length; i++)
      if (lafName.equals(lafNames[i])) index = i;
    UIManager.setLookAndFeel(lafClassNames[index]);
    SwingUtilities.updateComponentTreeUI(frame);
    frame.pack();
  }
  protected static final String copyInputStream(InputStream in, OutputStream out) throws IOException {
    byte[] buffer = new byte[1024];
    int len;

    while((len = in.read(buffer)) >= 0)
      out.write(buffer, 0, len);

    in.close();

    String b = out.toString();
    out.close();

    return b;
  }
  public static void printCollection(Collection<?> c) {
    for (Object e : c) {
      System.out.println(e);
    }
  }
  public static <T> void fromArrayToCollection(T [] a, Collection<T> c) {
    for (T o : a) {
       c.add(o);
    }
  }
  public static boolean isComment(final String line) {
    String l = line.trim();
    if (l.startsWith("#") || l.startsWith("//")) return true;

    return false;
  } 
}
