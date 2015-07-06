package config;

import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import com.smartsockets.*;
import config.util.*;
import config.hist.*;

/**
 * <P>
 * A little utility to test SmartSockets message passing. 
 * Only three datatypes (Str, Int4 and Real8) are supported
 * to keep the utility simple. The message grammar and contents
 * are split across # symbol.</P>  
 * 
 * <P> New functionalities are added to it in order to send messages
 * contained in files either as just plain string or as pre-formatted
 * variables. The three categories of messages are
 * <UL>
 *   <LI>SVT Error messages</LI>
 *   <LI>Spy Buffer data messages </LI>
 *   <LI>Histogram messages</LI>
 * </UL> </P>
 *
 * <P>The whole idea behind this utility is to debug the smartsockets parts
 *  independently and in a useful way when either the crates are not available
 *  or the message publishing facilities are not fully implemented there yet.</P>
 * 
 * @version 0.1, July 2000
 * @author  Subir Sarkar
 */
public class MessageComposer extends DataFrame {
    /** Maximum amount of data that might be published */
  private static final int MAX_ARRAY_LEN = 1000;
    /** Button which executes packMessage() */
  private JButton packButton;
    /** Browse file button */
  private JButton browseButton;

    /** Send message as a file Checkbox */
  private JCheckBox fileChBox;
    /** <B>Send message as a file</B> Radiobutton options */
  private JRadioButton [] msgFromFileRB;
  
    /** <I>Message Destination</I> Field Combo Box*/
  private JComboBox destCB;
    /** <I>Message Grammar</I> Text Field */
  private JTextField grammarField;
    /** <I>Message </I> Text Field */
  private JTextField messField;
    /** <I>File </I> Combo Box */
  private JComboBox fileCB;
    /** <I>Partition</I> Text Field */
  private JTextField partitionField;
    /** <I>Subject</I> Text Field */
  private JTextField subjectField;
    /** <I>Sender</I> Text Field */
  private JTextField senderField;
    /** <I>State</I> Text Field */
  private JTextField stateField;
  private JSplitPane splitPane;

    /** Becomes true when the SS message get packed */
  private boolean isPacked;
    /** Define preferred sizes for my entry fields */
    /** Short field (80, 24) */
  private static Dimension shortSize = new Dimension(80, 24);
    /** Medium sized field (180, 24) */
  private static Dimension medSize = new Dimension(180, 24);
    /** Long field (240, 24) */
  private static Dimension longSize = new Dimension(240, 24);
    /** Huge area (240, 200) */
  private static Dimension hugeSize = new Dimension(240, 200);
    /** Spacing between the label and the field */
  private static Border border 
       = BorderFactory.createEmptyBorder(0, 0, 0, 10);
    /** SS message object */
  private TipcMsg msg = null;
    /** Reference to the RT Server */
  private TipcSrv srv = null; 
    /** vector which contains intermediate data */
  private int [] data = new int[MAX_ARRAY_LEN];
  private int currentIndex = 0;
  private boolean firstTime = true;
  private int nWords = 0;

    /** Message type options as menu item, SVT Error option menu */
  private JRadioButtonMenuItem errorRB;
    /** Spy Buffer data message option menu */
  private JRadioButtonMenuItem bufferRB;
    /** Histogram message option menu */
  private JRadioButtonMenuItem histRB;
  private JCheckBoxMenuItem sendContinuouslyCB;
  private JFrame parent;
  /** 
   * Initialise the class, no-arg constructor 
   */
  public MessageComposer() {
    this(null, Tools.getServer(), true);
  }
  /** 
   * Initialise the class 
   * @param parent  Reference to the caller class (SpyMessenger here)
   */
  public MessageComposer(JFrame parent) {
    this(parent, Tools.getServer(), true);
  }
  /** 
   * Initialise the class 
   * @param parent  Reference to the caller class (SpyMessenger here)
   * @param srv     Reference to the RT Server
   */
  public MessageComposer(JFrame parent, TipcSrv srv, boolean sa) {
    super(sa, "Simple SmartSockets Message Composer", false, true, -1);
    this.parent = parent;
    this.srv  = srv;
    try {
      msg = createMessage();
    } 
    catch (TipcException te) {
      Tut.warning(te);
    }
    buildGUI();
    String filename = Tools.getEnv("SVTMON_DIR")+"/help/a_MessageComposer.html";
    setHelpFile(filename, "About SmartSockets Message Publisher", 
                           new Dimension(400, 200));
  }
  /** Create the user interface */
  public void buildGUI() {  
    // Add all the main panels to the frame
    updateOptionMenu(getJMenuBar());
    addHelpMenu();
    addHelpInToolBar();

    JPanel px = new JPanel(new BorderLayout());

    addToolBar();
    getContentPane().add(px, BorderLayout.CENTER);
    addStatusBar();
     
    // Creates the helper class panel to hold all my components
    GriddedPanel panel = new GriddedPanel();
    panel.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));

    // Description label and field
    getTextPanel().setBorder(Tools.etchedTitledBorder(" Message Logger "));
    getTextPanel().setPreferredSize(hugeSize);
    splitPane = Tools.createSplitPane(JSplitPane.VERTICAL_SPLIT, panel, getTextPanel());

    px.add(splitPane, BorderLayout.CENTER);

    // Now build the application level panel
    // Subject Destination
    JLabel label = createLabel("Message Destination", Color.black, border);
    panel.addComponent(label, 1, 1);

    destCB = new JComboBox();
    String [] destOptions = getSpyMessenger().getSVTDest();
    for (int i = 0; i < destOptions.length; i++) {
      destCB.addItem(destOptions[i]);   
    }
    destCB.setSelectedIndex(4);
    destCB.setMaximumRowCount(4);
    destCB.setEditable(true);
    destCB.setPreferredSize(longSize);
    panel.addFilledComponent(destCB, 1, 2, 3, 1, GridBagConstraints.HORIZONTAL);

    // Pack button
    packButton = new JButton("Pack");
    packButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) { 
        try {
          packMessage();
        } catch (TipcException te) {
          Tut.warning(te);
        }
      }
    });
    panel.addFilledComponent(packButton, 1, 5);

    // Description label and field
    label = createLabel("Message Grammar", Color.black, border);
    panel.addComponent(label, 2, 1);

    grammarField = new JTextField();
    grammarField.setPreferredSize(longSize);
    panel.addFilledComponent(grammarField, 2, 2, 3, 1, GridBagConstraints.HORIZONTAL);

    // Send button
    JButton button = new JButton("Send");
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) { 
        File file = null;
        try {
          if (sendContinuouslyCB.isSelected()) 
            sendMessageContinuously();
          else if (fileChBox.isSelected()) {
            file = new File( ((String) fileCB.getSelectedItem()).trim() );
            sendFile(file, msgFromFileRB[0].isSelected());
          }
          else
            sendMessage();
        } 
        catch (TipcException te) {
	  Tut.warning(te);
        }
        catch (FileNotFoundException e) {
          getTextPanel().warn("The file " + file.getName() + " wasn't found!", Color.red);
        }
        catch (IOException e) {
          getTextPanel().warn("Error reading from " + file.getName(), Color.red);
        }  
      }
    });
    panel.addFilledComponent(button, 2, 5);

    // Description label and field
    label = createLabel("Message", Color.black, border);
    panel.addComponent(label, 3, 1);

    messField = new JTextField();
    messField.setPreferredSize(longSize);
    panel.addFilledComponent(messField, 3, 2, 3, 1, GridBagConstraints.HORIZONTAL);

    // Clear button
    button = new JButton("Clear");
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) { 
        clear();
      }
    });
    panel.addFilledComponent(button, 3, 5);

    // Filename label and field
    label = createLabel("Filename", Color.black, border);
    panel.addComponent(label, 4, 1);

    fileCB = new JComboBox();
    fileCB.setMaximumRowCount(4);
    fileCB.setEditable(true);
    fileCB.setPreferredSize(longSize);
    fileCB.setEnabled(false);
    fileCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) { 
        File file = null;
        try {
          file = new File( ((String) fileCB.getSelectedItem()).trim() );
          sendFile(file, msgFromFileRB[0].isSelected());
        }
        catch (TipcException te) {
	  Tut.warning(te);
        }
        catch (FileNotFoundException e) {
          getTextPanel().warn("The file " + file.getName() + " wasn't found!", Color.red);
        }
        catch (IOException e) {
          getTextPanel().warn("Error reading from " + file.getName(), Color.red);
        }  
      }
    });
    panel.addFilledComponent(fileCB, 4, 2, 3, 1, GridBagConstraints.HORIZONTAL);

    // Browse button
    browseButton = new JButton("Browse");
    browseButton.setEnabled(false);
    browseButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) { 
        openFile();
      }
    });
    panel.addFilledComponent(browseButton, 4, 5);

    // Partition
    label = createLabel("Partition", Color.black, border);
    panel.addComponent(label, 5, 1);

    partitionField = 
      new JTextField((parent != null) 
        ? Integer.toString(getSpyMessenger().getPartition()) : "0");
    partitionField.setEditable(false);
    partitionField.setBackground(Color.white);
    partitionField.setPreferredSize(shortSize);
    panel.addFilledComponent(partitionField, 5, 2, 1, 1, GridBagConstraints.NONE);

    // Subject
    label = createLabel("Subject", Color.black, border);
    panel.addComponent(label, 6, 1);

    subjectField = new JTextField((parent != null) 
         ? getSpyMessenger().clientName : " ");
    subjectField.setEditable(false);
    subjectField.setBackground(Color.white);
    subjectField.setPreferredSize(medSize);
    panel.addFilledComponent(subjectField, 6, 2, 1, 1, GridBagConstraints.HORIZONTAL);

    // Sender
    label = createLabel("Sender", Color.black, border); 
    panel.addComponent(label, 7, 1);

    senderField = new JTextField((parent != null) 
      ? getSpyMessenger().sender : " ");
    senderField.setEditable(false);
    senderField.setBackground(Color.white);
    senderField.setPreferredSize(medSize);
    panel.addFilledComponent(senderField, 7, 2, 1, 1, GridBagConstraints.HORIZONTAL);

    // Use File CheckBox
    fileChBox = new JCheckBox("Use File", false);
    fileChBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) { 
        if (fileChBox.isSelected()) {
          fileCB.setEnabled(true);
          browseButton.setEnabled(true);
          grammarField.setEnabled(false);
          messField.setEnabled(false);
          for (int i = 0; i < msgFromFileRB.length; i++)
            msgFromFileRB[i].setEnabled(true);
        }
        else {
          fileCB.setEnabled(false);
          browseButton.setEnabled(false);
          grammarField.setEnabled(true);
          messField.setEnabled(true);
          for (int i = 0; i < msgFromFileRB.length; i++)
            msgFromFileRB[i].setEnabled(false);
        }
      }
    });
    panel.addComponent(fileChBox, 7, 5, 1, 1, 
          GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL);

    // State
    label = createLabel("State", Color.black, border);
    panel.addComponent(label, 8, 1);

    stateField = new JTextField((parent != null)
      ? Integer.toString(getSpyMessenger().getActiveState()): "0");
    stateField.setEditable(false);
    stateField.setBackground(Color.white);
    stateField.setPreferredSize(shortSize);
    panel.addComponent(stateField, 8, 2);

    // Two radiobuttons to select type of smartsockets message
    String [] msgOptions = {
      "As Strings", "As Variables"
    };
    JPanel radioP = new JPanel(new GridLayout(msgOptions.length,1));
    radioP.setBorder(BorderFactory.createEtchedBorder());
    msgFromFileRB = new JRadioButton[msgOptions.length];
    ButtonGroup bGroup = new ButtonGroup();     
    for (int i = 0; i < msgOptions.length; i++) {
      msgFromFileRB[i] = new JRadioButton(msgOptions[i]); 
      msgFromFileRB[i].setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 0)); 
      msgFromFileRB[i].setFont(new Font("SansSerif", Font.BOLD, 10));
      msgFromFileRB[i].setEnabled(false);
      bGroup.add(msgFromFileRB[i]);
      radioP.add(msgFromFileRB[i]);
    }
    msgFromFileRB[1].setSelected(true);
    panel.addFilledComponent(radioP, 8, 5, 1, 2, GridBagConstraints.VERTICAL);
  }
  /** Add another menu to the menubar */
  public void updateOptionMenu(JMenuBar menuBar) {
    JMenu menu = menuBar.getMenu(1);

    ButtonGroup group = new ButtonGroup();

    errorRB = Tools.radioButtonItem("Send SVT Error Message", group, 
                         true, Color.black, KeyEvent.VK_E, 0);
    errorRB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
        int index = -1;
        String [] destOptions = getSpyMessenger().getSVTDest();
        for (int i = 0; i < destOptions.length; i++) {
          if (destOptions[i].equals(AppConstants.STATUS_SUBJECT)) index = i;
        }
        if (index > -1) destCB.setSelectedIndex(index);
      }
    });
    menu.add(errorRB);

    bufferRB = Tools.radioButtonItem("Send Spy Buffer Message", group, 
                         false, Color.black, KeyEvent.VK_B, 0);
    bufferRB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
        int index = -1;
        String [] destOptions = getSpyMessenger().getSVTDest();
        for (int i = 0; i < destOptions.length; i++) {
          if (destOptions[i].equals(AppConstants.BUFFER_SUBJECT)) index = i;
        }
        if (index > -1) destCB.setSelectedIndex(index);
      }
    });
    menu.add(bufferRB);

    histRB = Tools.radioButtonItem("Send Histogram Message", group, 
                         false, Color.black, KeyEvent.VK_H, 0);
    histRB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
        int index = -1;
        String [] destOptions = getSpyMessenger().getSVTDest();
        for (int i = 0; i < destOptions.length; i++) {
          if (destOptions[i].equals(AppConstants.HISTO_SUBJECT)) index = i;
        }
        if (index > -1) destCB.setSelectedIndex(index);
      }
    });
    menu.add(histRB);

    menu.addSeparator();

    sendContinuouslyCB = Tools.checkBoxItem("Send Message Continuously", " ", 
                         false, KeyEvent.VK_C);
    menu.add(sendContinuouslyCB);
  }
    /** 
     * Open a file to be sent as an SS message 
     */
  protected void openFile() {
    MessageComposer.this.repaint();
    if (getFileChooser().showOpenDialog(MessageComposer.this) != 
           JFileChooser.APPROVE_OPTION) return;
    Thread runner = new Thread() {
      public void run() {
        File fChoosen = getFileChooser().getSelectedFile();
        fileCB.addItem(fChoosen.getAbsolutePath());
      }
    };
    runner.start();
  }
    /**
     * Process individual lines from the file and convert into SS message
     * @param line       Line of text
     * @param asString   Decision whether to convert text as String only message or not 
     */
  public void createHistoMessage(BufferedReader input)  
        throws TipcException, IOException, NullPointerException
  {
    String line = null;
    String [] fields = new String[0];
    int i, j, nbin, nhist = 0;
    float [] bins   = new float[0];
    float [] errors = new float[0];

    if (msg != null) msg = null;
    TipcMt mt = TipcSvc.lookupMt(SVTHistoMessage.SVTHistoContatiner_MessageType);
    msg = TipcSvc.createMsg(mt);

    msg.setNumFields(0);
    msg.appendInt4(128048);
    msg.appendInt4(1002663710);

    String dest = null;
    while (true) {
      line  = Tools.getNextLine(input);
      if (line == null) break;
      nhist = Integer.parseInt(line);     // Required to specify # of histograms
                                          // to be added in the message

      TipcMsg [] submsg = new TipcMsg[nhist];
      for (i = 0; i < nhist; i++) {
        mt = TipcSvc.lookupMt(SVTHistoMessage.SVTHisto1D_MessageType);
        submsg[i] = TipcSvc.createMsg(mt);
        if      (i%2 == 0) dest = "/monitor/b0svt03";
        else if (i%3 == 0) dest = "/monitor/b0svt04";
        else               dest = "/monitor/b0svt05";
        submsg[i].setDest(dest);

        line = Tools.getNextLine(input);  // Histogram ID etc.
        fields = Tools.split(line);

        line = Tools.getNextLine(input);  // Read histogram title before adding other
	                                  // fields to message 
        submsg[i].setNumFields(0);

        submsg[i].appendInt4(Integer.parseInt(fields[0]));  // Histogram ID
        submsg[i].appendStr(line);                          // Title

        nbin = Integer.parseInt(fields[1]);                 // # of bins
        submsg[i].appendInt4(nbin); 

        submsg[i].appendReal4((new Float(fields[2])).floatValue()); // x min    
        submsg[i].appendReal4((new Float(fields[3])).floatValue()); // x max    

        submsg[i].appendInt4(Integer.parseInt(fields[4]));          // # of entries
        submsg[i].appendInt4(Integer.parseInt(fields[5]));          // Underflow
        submsg[i].appendInt4(Integer.parseInt(fields[6]));          // Overflow

        bins   = new float[nbin];
        errors = new float[nbin];
        for (j = 0; j < nbin; j++) {
          line      = Tools.getNextLine(input);    // Bin content, error
          fields    = Tools.split(line, " ");
          bins[j]   = (new Float(fields[0])).floatValue();  
          errors[j] = (new Float(fields[1])).floatValue();  
        } 
        submsg[i].appendReal4Array(bins);
        submsg[i].appendReal4Array(errors);
      }
      msg.appendMsgArray(submsg);
    }
  }
    /**
     * Process individual lines from the file and convert into SS message
     * @param line       Line of text
     * @param asString   Decision whether to convert text as String only message or not 
     */
  public void createErrorMessage(BufferedReader input)  
       throws TipcException, IOException {
  }
    /**
     * Process individual lines from the file and convert into SS message
     * @param line       Line of text
     * @param asString   Decision whether to convert text as String only message or not 
     */
  public void createBufferMessage(BufferedReader input) 
       throws TipcException, IOException {
    String line = null;
    String [] fields;
    int [] exactData;
    TipcMsg submsg = null;

    while (true) {
      line = Tools.getNextLine(input);
      fields = Tools.split(line);
      if (line.startsWith("NS")) {
        msg.appendInt4(Integer.parseInt(fields[1]));    // nCycles
        msg.appendStr(fields[2]);                       // Formatted Date  yy/mm/dd
        msg.appendStr(fields[3]);                       // Formatted time  hh:mm:ss
        msg.appendInt4(Integer.parseInt(fields[4]));    // L1 Counter
        msg.appendInt4(Integer.parseInt(fields[5]));    // Freeze bit
        msg.appendInt4(Integer.parseInt(fields[6].substring(1,fields[6].length()-2)));    // HF Counter
      }
      else if (line.startsWith("RC")) {
        msg.appendInt4(Integer.parseInt(fields[1]));    // partition number
        msg.appendStr(fields[2]);                       // RC state
        msg.appendInt4(Integer.parseInt(fields[3]));    // Event number
        // msg.appendReal4(Float.parseFloat(fields[4]));   // Current Rate
        msg.appendReal4((new Float(fields[4])).floatValue());   // Current Rate
        msg.appendInt4(Integer.parseInt(fields[5]));    // run number
      }
      else if (line.startsWith("SB")) {
        if (firstTime) {
          submsg = createMessage();
          firstTime = false;
        } 
        else {
          exactData = new int[nWords];
          System.arraycopy(data, 0, exactData, 0, nWords);
          submsg.appendInt4Array(exactData);
          msg.appendMsg(submsg);
          currentIndex = 0;
        }
        submsg.setNumFields(0);
        submsg.appendStr(fields[1]);                      // Crate name
        submsg.appendInt4(Integer.parseInt(fields[2]));   // Slot number
        submsg.appendStr(fields[3]);                      // Board name
        submsg.appendStr(fields[4]);                      // Spy name
        nWords = Integer.parseInt(fields[5]);
        submsg.appendInt4(nWords);                        // Number of Words
        if (fields[6].endsWith("+")) {
          submsg.appendInt4(
             Integer.parseInt(fields[6].substring(0,fields[6].length()-1))
          );                                              // Pointer
          submsg.appendInt4(1);                           // Wrap
        }
        else {
          submsg.appendInt4(Integer.parseInt(fields[6])); // Pointer
          submsg.appendInt4(0);                           // Wrap
        }
      }
      else if (line.startsWith("DA")) {
        for (int i = 1; i < fields.length; i++) {
          if (currentIndex == MAX_ARRAY_LEN) break;
          data[currentIndex++] = Integer.parseInt(fields[i], 16);
        }
      }
    }
  }
    /** Create Smartsockets message */
  public TipcMsg createMessage() throws TipcException {
    TipcMsg msg = TipcSvc.createMsg(TipcMt.NUMERIC_DATA);
    msg.setNumFields(0);
    return msg;
  }
    /**
     * Send histogram message by passing a filename and a destination subject path
     * @param filename  File which contains the text based histogram message
     * @param dest      Destination subject path
     */
  public void sendHistogramFile(final String filename, final String dest) 
     throws TipcException,  FileNotFoundException, IOException
  {
    if (!histRB.isSelected()) histRB.setSelected(true);
    if (!fileChBox.isSelected()) fileChBox.setSelected(true);
    destCB.addItem(dest);
    sendFile(new File(filename), false);
  }
    /**
     * Send histogram message by passing a filename
     * @param filename  File which contains the text based histogram message
     */
  public void sendHistogramFile(final String filename) 
     throws TipcException, FileNotFoundException, IOException
  {
    sendHistogramFile(filename, AppConstants.HISTO_SUBJECT);
  }
    /** 
     * Send a file as a SmartSockets message 
     * @param file  Reference to the File handle
     * @param asString boolean decision whether the SmartSocket message
     *                 is string only or not
     */
  public void sendFile(File file, boolean asString) 
     throws TipcException,  FileNotFoundException, IOException
  {
    BufferedReader input = null;
    firstTime = true;
    String line = null;

    msg.setNumFields(0);
  
    input = new BufferedReader(
       new InputStreamReader(new FileInputStream(file)));
    if (input != null) {
      if (asString) {
        while (true) {
          line = Tools.getNextLine(input);
          if (line == null) break;
          msg.appendStr(line);
        }
        input.close();
      }
      else {
        if (errorRB.isSelected()) 
          createErrorMessage(input);
        else if (bufferRB.isSelected()) 
          createBufferMessage(input);
        else if (histRB.isSelected()) 
          createHistoMessage(input);
      }
    }

    sendMessage();
  }
    /** 
     * Create a Label with specified property 
     * @param name   Name of the label
     * @param color  Text color
     * @param border Label border  
     * @return Reference to the label
     */
  public JLabel createLabel (String name, Color color, Border border) {
    JLabel label = new JLabel(name);
    label.setForeground(color);
    label.setBorder(border); // add some space on the right
    return label;
  }
    /** Clear the input fields */
  protected void clear() {
    destCB.setSelectedIndex(0); 
    grammarField.setText(""); 
    messField.setText("");
  }
    /** 
     * Read the input text fields, interpret them in a simple way splitting 
     * across # symbols and pack them into an Smart Sockets message
     */
  protected int packMessage() throws TipcException {
    String [] messType = new String[0]; 
    String [] messVal  = new String[0];
    msg.setNumFields(0);

    if (!((String)destCB.getSelectedItem()).trim().equals("")) {
      if (!grammarField.getText().trim().equals("")) {
        messType = grammarField.getText().trim().split("#");
      }
      if (!messField.getText().trim().equals("")) {
        messVal = messField.getText().trim().split("#");
      }
      if (messType.length == 0 || messVal.length == 0) return -1;
      if (messType.length != messVal.length) {
       getTextPanel().warn(" # of fields in message type and message field different....."); 
      	return -1;
      }
    }
    for (int i = 0; i < messType.length; i++) {
      getTextPanel().warn(messType[i] + "  " + messVal[i]);
      if (messType[i].equals("Str")) {
        msg.appendStr(messVal[i]);
      }
      else if (messType[i].equals("Int4")) {
        msg.appendInt4((new Integer(messVal[i])).intValue());
      }
      else if (messType[i].equals("Real8")) {
        msg.appendReal8((new Double(messVal[i])).doubleValue());
      }
      else {
        getTextPanel().warn(messType[i] + " not supported yet!");
      }
    }      
    if (isDebugOn()) msg.print();
    isPacked = true;
    return 0;    
  }
    /** 
     * Send the packed message, if not packed packs first before sending 
     */
  public void stopProducer()  {
    if (srv == null) return;
    try {
      TipcMsg stopMsg = createMessage();
      stopMsg.appendStr("Producer");
      stopMsg.appendInt4(1);
      stopMsg.setDest(AppConstants.ACK_SUBJECT);
      srv.send(stopMsg);
      srv.flush();
      stopMsg.destroy();
    }
    catch (TipcException ex) {
      ex.printStackTrace();
    }
  }
    /** 
     * Send the packed message, if not packed packs first before sending 
     */
  protected void sendMessage() throws TipcException {
    if (srv == null) return;
    if (!isPacked && !fileChBox.isSelected())  
      packMessage();

    if (isDebugOn()) msg.print();
    msg.setDest(((String)destCB.getSelectedItem()).trim());
    srv.send(msg);
    srv.flush();
    msg.destroy();
  }
    /** 
     * Send the packed message, if not packed packs first before sending 
     */
  protected void sendMessageContinuously () throws TipcException {
    if (errorRB.isSelected()) {
      Thread runner = new Thread() {
        public void run() {
          do {
            try {
              sendErrorMessage();
              Thread.sleep((long)(Math.random() * 100000));
            } 
            catch (TipcException e) {
              Tut.warning(e);
            }
            catch (InterruptedException e) {
              System.out.println("Sleep interrupted! ...\n");
            }
          } while (sendContinuouslyCB.isSelected());
        }
      };
      runner.start();
    }
    else {
      JOptionPane.showMessageDialog(MessageComposer.this,
         "Only Error message is supported!", 
         "Incorrect Selection", JOptionPane.INFORMATION_MESSAGE);
    }
  }
    /** Do not use Random.nextInt(n) as JDK 1.1.8 does not support it */
  protected void sendErrorMessage() throws TipcException {
    int index, nbuf, crateId;
    int [] counters = new int[8];
    String crateName;
    final int [] nBoards   = {14, 4, 15};
    final int AMS = 32, HB = 33, MRG = 34, HF = 35, TF = 36,  XTFA = 37;
    final String [][] names = {
      {"HF", "HF", "HF", "MRG", "AMS", "HB", "TF", "HF", "HF", "HF", "MRG", "AMS", "HB", "TF"}, 
      {"MRG", "MRG", "MRG", "MRG"},
      {"HF", "HF", "HF", "XTFA", "XTFA", "XTFA","HF", "HF", "HF", "XTFA", "XTFA", "XTFA",
                         "XTFA", "XTFA", "XTFA"} 
    };
    final int [][] slot = {
	{4, 5, 6, 7, 8, 11, 12, 13, 14, 15, 16, 17, 20, 21},
        {8, 16, 18, 20},
        {4, 5, 6, 8, 9, 10, 13, 14, 15, 16, 17, 18, 19, 20, 21}
    };
    final int [][] type = {
       {HF, HF, HF, MRG, AMS, HB, TF, HF, HF, HF, MRG, AMS, HB, TF}, 
       {MRG, MRG, MRG, MRG},
       {HF, HF, HF, XTFA, XTFA, XTFA, HF, HF, HF, XTFA, XTFA, XTFA, XTFA, XTFA, XTFA}
    };
    final int [] nBuffers = {2, 3, 5, 11, 2, 3}; 
    final HashMap<String, String[]> spyName = new HashMap<String, String[]>();
    spyName.put("AMS", new String[]{"AMS_HIT_SPY", "AMS_OUT_SPY"});
    spyName.put("HB", new String[]{
      	"HB_HIT_SPY",
      	"HB_ROAD_SPY",
      	"HB_OUTT_SPY"
      });
    spyName.put("MRG", new String[]{
      	"MRG_A_SPY",
      	"MRG_B_SPY",
      	"MRG_C_SPY",
      	"MRG_D_SPY",
      	"MRG_OUT_SPY"
      });
    spyName.put("HF", new String[]{
      	"HF_ISPY_0", 
      	"HF_ISPY_1", 
      	"HF_ISPY_2", 
      	"HF_ISPY_3", 
      	"HF_ISPY_4", 
      	"HF_ISPY_5", 
      	"HF_ISPY_6", 
      	"HF_ISPY_7", 
      	"HF_ISPY_8", 
      	"HF_ISPY_9", 
      	"HF_OUT_SPY" 
      });
    spyName.put("TF", new String[]{
      	"TF_ISPY",
      	"TF_OSPY"
      });
    spyName.put("XTFA", new String[]{
      	"XTFA_TRK_SPY",
      	"XTFA_1_SPY",
      	"XTFA_OUT_SPY"
      });
    
    Random rand = new Random();
    crateId     = (int) (AppConstants.nCrates*Math.random());
    crateName   = AppConstants.SVT_CRATE_PREFIX + crateId;
    if (isDebugOn()) System.out.println("Sending data for " + crateName);

    index = getIndex(crateId); // All the 6 tracker crates are the same

    msg.setNumFields(0);

    /* Append crate data to <CODE>msg</CODE>*/
    msg.appendStr("config.MessageComopser");       // Class Name
    msg.appendInt4((int)(new Date()).getTime());  // Time Stamp
    msg.appendStr(crateName);                     // Crate Name
    msg.appendInt4((int) (1000*Math.random()));   // Spymon iteration number
    msg.appendInt4((int) (300000*Math.random())); // Run number
    msg.appendInt4((int)(1000*Math.random()));    // Level 1 accept rate
    msg.appendInt4((int) (10*Math.random()));     // Partition Number
    msg.appendStr("Active");                      // RC State

    // Now the Board array
    TipcMsg [] boardMsg = new TipcMsg[nBoards[index]];
    for (int i = 0; i < nBoards[index]; i++) {
      boardMsg[i] = createMessage(); 
      boardMsg[i].setNumFields(0);
      boardMsg[i].appendStr("config.MessageComposer");       // Class Name
      boardMsg[i].appendInt4((int)(new Date()).getTime());        // Time Stamp
      boardMsg[i].appendStr(names[index][i]);           // Board Name/type
      boardMsg[i].appendInt4(slot[index][i]);           // Slot number
      boardMsg[i].appendInt4((int) (16*Math.random())); // Status
      boardMsg[i].appendInt4((int)(256*Math.random())); // Error register
      for (int l = 0; l < counters.length; l++) {
	counters[l] = getRandom();       
      }
      boardMsg[i].appendInt4Array(counters);            // Individual error register array

      // Now the buffer array
      nbuf = nBuffers[getIndex(names[index][i])];
      TipcMsg [] bufferMsg = new TipcMsg[nbuf];
      String [] sNames = spyName.get(names[index][i]);
      for (int j = 0; j < nbuf; j++) {
        bufferMsg[j] = createMessage(); 
        bufferMsg[j].setNumFields(0);
        bufferMsg[j].appendStr("config.MessageComposer");       // Class Name
        bufferMsg[j].appendInt4((int)(new Date()).getTime());   // Time Stamp
        bufferMsg[j].appendStr(sNames[j]);                      // Buffer Name/type
        bufferMsg[j].appendInt4((int) (3000*Math.random()));    // # of events
        bufferMsg[j].appendInt4((int) (300000*Math.random()));  // Total # of events
        for (int l = 0; l < counters.length; l++) {
	  counters[l] = (int) (100*Math.random());       
        }
        bufferMsg[j].appendInt4Array(counters);        // Individual error counter values
        for (int l = 0; l < counters.length; l++) {
	  counters[l] = (int) (300000*Math.random());
        }
        bufferMsg[j].appendInt4Array(counters);        // Total error counter values

        bufferMsg[j].appendInt4((int)(300000*Math.random())); // Pointer
        bufferMsg[j].appendInt4((int)(256*Math.random()));    // status
        //int [] data = new int[0];
        //bufferMsg[j].appendInt4Array(data);                   // Spy buffer
      } 
      boardMsg[i].appendMsgArray(bufferMsg); 
    }
    msg.appendMsgArray(boardMsg); 

    if (srv == null) return;
    if (isDebugOn()) msg.print();
    msg.setDest(((String)destCB.getSelectedItem()).trim());
    srv.send(msg);
    srv.flush();
    //msg.destroy();
  }
  public int getIndex(int crateId) {
    int index = 0;
    if (crateId == 6) 
      index = 1;
    else if (crateId == 7) 
      index = 2; 
    else 
      index = 0;
    return index; 
  }
  public int getIndex(final String name) {
    if      (name.equals("AMS"))  return 0;
    else if (name.equals("HB"))   return 1;
    else if (name.equals("MRG"))  return 2;
    else if (name.equals("HF"))   return 3;
    else if (name.equals("TF"))   return 4;
    else if (name.equals("XTFA")) return 5;
    else return -1;
  }
    /**
     * Get reference to the Spy Messenger
     * @return Reference to the Spy Messenger
     */
  public SpyMessenger getSpyMessenger() {
    return (SpyMessenger) parent;
  }
    /**
     * Get a random number either 0 or 1
     * @return Random number either 0 or 1
     */
  public static int getRandom() {
    if (Math.random() > 0.5) 
      return 1;
    else 
      return 0;
  }
  public static void main(String [] args)   {
    JFrame f = new MessageComposer(null, Tools.getServer(), true);
    f.setSize(600, 500);
    f.setVisible(true);
  }
}
