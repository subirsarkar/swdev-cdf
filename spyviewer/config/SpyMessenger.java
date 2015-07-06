package config;

import java.io.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import com.smartsockets.*;
import daqmsg.*;
import rc.*;

import config.svt.*;
import config.hist.*;
import config.util.*;
import config.db.*;

/**
 * <P>The main java program which acts as a Spy Configurator
 * as well as a receiver of messages coming from the Crate CPUs
 * and the spy manager. The main thread works as the sender
 * of the message whereas many new threads may act as listeners.</P>
 * 
 * <P>
 * We have understood that always being a part of the main run 
 * control may not be the ideal situation. We may, want to be only 
 * a part of the CDF_DAQ project and listen to messages published 
 * by crate processes which at present is considered the default option.</P>
 *
 * <P>
 * If within the framework of main Run Control, 
 * as soon as paritioning is done the main thread no longer looks
 * for messages but starts the message receiver thread
 * which constantly looks for messages by registering both R_C 
 * command as well as Spy specific callbacks. The receiver thread, 
 * in this situation should trap all the R_C commands and take 
 * necessary actions on behalf of the whole application. For example, 
 * if R_C aborts or resets, the 'state' should be reset such that when 
 * partitioning is done again, the main thread is ready to listen to 
 * that. If this model is disadvantageous, which we must feel soon, 
 * we should look for a better solution.</P>
 *
 * <P>
 * The above description assumes that this application will
 * become a part of the main Run Control. If not, and still 
 * the Spy Messsenger likes to keep track of the R_C state, 
 * one of the crate processes should, after decoding messages
 * coming from R_C, broadcast for the Unix side processes.</P>
 *
 * @author   S. Sarkar
 * @version  0.1, July 2000
 * @version  0.5, June 2001
 */

public class SpyMessenger extends DataFrame 
                          implements Serializable
{
     /** SmartSockets Client sunject as a unix tree structure */
  protected static String clientName = "/frontEnd/SpyMessenger";
     /** Name of the sender, user defined */
  protected static String sender = "config.SpyMessenger";
  protected String [] rcSubDest  = new String[4];
  protected String [] svtSubDest = new String[6];
  boolean [] subscriptionAtStart = new boolean[6];
  protected int partitionToWatch = 0;

  protected JCheckBoxMenuItem [] subscriptionsCB;
    /** Status bar */
  protected JPanel statusBar;   
    /** Tool Tip shown in status Bar */
  protected JPanel tipPanel;   
    /** Status of the Application */
  protected JLabel statusLabel;
    /** Embed a digital clock at the right corner of the status bar */
  protected DigitalClock dClock = new DigitalClock();
    /**  Embed a timer to calculate time elapsed between start and stop of an event */
  protected TimeEvent timeEvent = new TimeEvent(-1);
     /** 
      * Reference to SmartSockets RT Server. When the client connects to
      * to the RT server, all message passing occurs thru' this variable
      */
  private static TipcSrv srv = null;
     /** Reference to SmartSockets message related to partitioning */
  private TipcMsg msgPartition = null;
   
     /** Partition number the SS client is in, initially it is set to -1 */
  protected int activePartition;
    /** 
     * User defined state depending on actions performed by R_C 
     * of the SS client. initially it is set to -1 
     */
  protected int activeState;
    /** 
     * True when the partition is done or redone and partition number has a +ve value 
     */
  protected boolean isPartitioned = false;
    /** reference to SmartSockets receiver thread */
  protected ReceiverThread rThread = null;
    /** When true, stops the SmartSockets message receiver thread */
  protected boolean stopThread;
  
  /** If checked be a part of CDF Run, by default unchecked */
  protected JCheckBoxMenuItem listenToRC;
  protected JCheckBoxMenuItem dumpSpyCB;
  
  private JSplitPane splitPane;
  
  private TabPanel tabPanel;
  private SimpleSessionManager seManager;
  private boolean connectByDefault = true;
  private boolean receiverAlive    = false;
  
  private SpySchemePanel spyScheme = null;

  private HistogramDisplayFrame histoDisplay = null;
  private SvtCrateConfig configFrame = null;
  private SvtCratesFrame crateStatusFrame = null;
  private WindowAdapter winListener = new MyWindowListener();

  // Number of acknpwledgements from crates, when this sums upto 
  // nCrates change the color of the connection and reset it.
  private static int nAck = 0;

  /* Constructor */
  public SpyMessenger(String title) {
    super(true, title, false, true, -1, false);
    activePartition = -1;
    activeState     = -1;
    buildGUI();

    String filename = Tools.getEnv("SVTMON_DIR")+"/help/a_SpyMessenger.html";
    setHelpFile(filename, "About SVT Spy Buffer Monitoring", new Dimension(400, 240));
  }
  /** Prepare UI */
  protected void buildGUI() {
    Container content = getContentPane();
  
    setFont(new Font("SansSerif", Font.PLAIN, 10));
  
    updateMenu();
    addHelpInToolBar();
    addToolBar();
  
    // Add session manager and the tabs to a split pane 
    // ---- seManager = new SessionManager(this); 
    seManager = new SimpleSessionManager(this);
  
    tabPanel = new TabPanel();
  
    // Place the splitpane in the main container    
    splitPane = Tools.createSplitPane(JSplitPane.VERTICAL_SPLIT, seManager, tabPanel);
    // ---- splitPane.setDividerLocation(250);
    content.add(splitPane, BorderLayout.CENTER);
  
    // Then the timer/Status bar/DigiClock
    JPanel p = new JPanel(new BorderLayout());
    content.add(p, BorderLayout.SOUTH);
  
    StatusBar statusBar = getStatusBar();
    statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
    p.add(statusBar, BorderLayout.CENTER);
  
    timeEvent.setBorder(BorderFactory.createLoweredBevelBorder());
    p.add(timeEvent, BorderLayout.WEST);
  
    dClock.setBorder(BorderFactory.createLoweredBevelBorder());
    p.add(dClock, BorderLayout.EAST);
  }
  private void updateMenu() {
    updateFileMenu(getJMenuBar());
    updateOptionMenu(getJMenuBar());
    addViewMenu(getJMenuBar());
    addSubscriptionMenu(getJMenuBar());
    addToolMenu(getJMenuBar());
    updateHelpMenu(getJMenuBar());
  }
  class TabPanel extends JPanel {
    private JTabbedPane tabs;
    private TextPanel beamPanel;
    private ReceiverDisplayPanel commandPanel;
    private SpyDumpPanel dumpPanel;
    public TabPanel() {
      setLayout(new BorderLayout());
      setBorder(BorderFactory.createLoweredBevelBorder()); 
  
      tabs = new JTabbedPane();
      tabs.setTabPlacement(SwingConstants.BOTTOM);
  
      tabs.addTab("Messages", null, getTextPanel());
  
      commandPanel = new ReceiverDisplayPanel(SpyMessenger.this);
      tabs.addTab("RC Command", null, commandPanel);
  
      dumpPanel = new SpyDumpPanel(SpyMessenger.this);
      tabs.addTab("Spy Dump", null, dumpPanel);

      beamPanel = new TextPanel();
      beamPanel.setTextFont(new Font("SanSerif", Font.PLAIN, 12));
      beamPanel.setContentType("text/html");
      tabs.addTab("Beam Position", null, beamPanel);
  
      tabs.setSelectedIndex(0);
  
      add(tabs, BorderLayout.CENTER);
    }
    protected TextPanel getHistoryPanel() {
      return getTextPanel();
    }
    protected ReceiverDisplayPanel getCommandPanel() {
      return commandPanel;
    }
    protected SpyDumpPanel getDumpPanel() {
      return dumpPanel;
    }
    protected TextPanel getBeamPanel() {
      return beamPanel;
    }
    protected JTabbedPane getTab() {
      return tabs;
    }
  }
  /** 
   * Connect to RT Server and wait for Partition message 
   */
  protected void initSrv() throws TipcException {
    if (srv == null) { 
      getHistoryLogger().setText("");
      activeState   = RCStateConstants.STATE_WAITING;
      isPartitioned = false;
      Tut.setOption("ss.project", "CDF_DAQ");
      String ssinit = Tools.getEnv("SMARTSOCKETS_CONFIG_DIR");
      if (ssinit != null) {
        Tut.loadOptionsFile(ssinit+"/java.cm");
      } else {
        Runnable setTextRun = new Runnable() {
          public void run() {
            if (isDebugOn()) 
              getHistoryLogger().addText("Cannot connect...", "Helvetica", "Bold", 10, Color.red);
          }
        };
        SwingUtilities.invokeLater(setTextRun);
      }
    
      /* connect to RTserver */
      srv = TipcSvc.getSrv();
      if (!srv.create()) {
        Tut.exitFailure("ERROR. Couldn't connect to RTserver!\n");
      } else {
        System.out.println("INFO. Connected to RT Server...");
        if (isDebugOn()) getHistoryLogger().warn("Connected to RT Server...");
      }
      if (srv != null && isPartitionNeeded()) waitForPartition();
    }
  }
  public TipcSrv getServer() {
    return srv;
  }
  /** 
   * When R_C aborts, undo partition and be ready for 
   * partitioning once again 
   */ 
  protected void redoPartition () throws TipcException {
    undoPartition();
    waitForPartition();
  }
  /** 
   * Undo partition i.e set <CODE>isPartition</CODE> to false and
   * <CODE>activePartition to -1 </CODE>
   */  
  protected void undoPartition() {
    isPartitioned   = false;
    activePartition = -1;
  }
  /** 
   * Set a partition callback and sit inside an infinite 
   * loop to listen to messages 
   */
  protected void waitForPartition()  {
    if (srv != null) {
      partitionCallback(true);
      Thread runner = new Thread() {
        public void run() {
          while (!isPartitioned) {
            try {
              msgPartition = srv.next(TipcDefs.TIMEOUT_FOREVER);
              if (msgPartition != null) {
                srv.process(msgPartition);
                msgPartition.destroy();
              } 
            } catch (TipcException e) {
              Tut.warning(e);
            }        
            partitionCallback(false);
            if (rThread != null)  rThread.registerCallback(true);
            if (isDebugOn()) showSubscriber("waitForPartition(): After partition");
          }
        }
      };
      runner.start();
    }
  }
  /** 
   * Setup Partition message callback 
   */
  protected void partitionCallback (boolean setCallback) {
    TipcCb pStat = null;
    String dest  = "/setPartition"+clientName; 
    try {
      if (setCallback) {
        if (!srv.getSubjectSubscribe(dest)) {
          ProcessPartition pRef = new ProcessPartition();
          pStat = srv.addProcessCb(pRef, dest, srv);
          if (pStat == null) {
            Tut.warning("WARNING. Couldn't register subject callback!\n");
          }
          srv.setSubjectSubscribe(dest, true);
          // subscribe to the appropriate subject 
          System.out.println("Subject = " + dest);
          if (isDebugOn()) getHistoryLogger().warn("Subject = " + dest);
        }
      }
      else  {
        if (srv.getSubjectSubscribe(dest)) {
          srv.setSubjectSubscribe(dest, false);
          // srv.removeProcessCb(pStat);          
        }
      }
    } catch (TipcException Tipe) {
      Tut.warning(Tipe);
    } 
  }
  /** 
   * Close connection to RT Server 
   */
  private void closeSrv() {
    if (srv != null) {
      try {
        System.out.println("INFO. Closing connection to TipcSrv ...");
        srv.destroy(); // disconnect from RTserver
        srv = null;       
      } catch (TipcException Tipe) {
        Tut.warning(Tipe);
      }
    }
  }
  /** 
   * Create menubar and add the menus and meun items within 
   */
  private void updateFileMenu(JMenuBar menuBar) {
    JMenuItem item;
    JMenu menu = menuBar.getMenu(0);
  
    menu.insertSeparator(2);
  
    Icon icon = new ImageIcon(AppConstants.iconDir+"file_save.png");
    Action action = new AbstractAction("Save Configuration", icon) { 
      public void actionPerformed(ActionEvent e) {
        saveState();
      }
    };
    item =  menu.insert(action, 3); 
    menu.insert(item, 3);
  
    icon = new ImageIcon(AppConstants.iconDir+"mini-read.png");
    action = new AbstractAction("Show Configuration", icon) { 
      public void actionPerformed(ActionEvent e) {
        readState();
      }
    };
    item =  menu.insert(action, 4); 
    menu.insert(item, 4);
  
    menu.insertSeparator(5);
  }
  /** 
   * Create menubar and add the menus and meun items within 
   */
  private void addViewMenu(JMenuBar menuBar) {
    JMenuItem item;
  
    /* View menu */
    JMenu menu = new JMenu("View");
    menu.setMnemonic('v');
    menuBar.add(menu);
  
    menu.addSeparator();
    JMenu newMenu = new JMenu("Clone Window");
    menu.add(newMenu);
  
    Icon icon = new ImageIcon(AppConstants.iconDir+"windows.png");
    Action action = new AbstractAction("Messages", icon) { 
      public void actionPerformed(ActionEvent e) {
         openViewer("View History Log", 
                     getHistoryLogger().getText(),
       	             "Current History Log ...");
      }
    };
    item =  newMenu.add(action);
    newMenu.add(item);
  
    action = new AbstractAction("RC Commands", icon) { 
      public void actionPerformed(ActionEvent e) {
         openViewer("View RC Commands", 
                     tabPanel.getCommandPanel().getText(),
       	             "RC commands sent so far ...");
      }
    };
    item =  newMenu.add(action);
    newMenu.add(item);
  
    action = new AbstractAction("Spy Dump", icon) { 
      public void actionPerformed(ActionEvent e) {
         openViewer("View Spy Dump", 
                     tabPanel.getDumpPanel().getText(),
       	             tabPanel.getDumpPanel().getLabel().getText());
      }
    };
    item =  newMenu.add(action);
    newMenu.add(item);
  }
  /** 
   * Add option menu to the menubar
   * @param menuBar   The Menubar object reference
   */
  private void updateOptionMenu(JMenuBar menuBar) {
    JMenuItem item;
  
    JMenu menu = menuBar.getMenu(1);

    listenToRC = new JCheckBoxMenuItem("Listen to Run_Control", false);
    listenToRC.setEnabled(false);
    menu.add(listenToRC);
  
    dumpSpyCB = new JCheckBoxMenuItem("Show Spy Dump", true);
    menu.add(dumpSpyCB);
  }
  /** 
   * Add SmartSockets message subscription menu to the menubar. The menu items
   * are check buttons and one can modify the list of subscribers at run time
   * @param menuBar   The Menubar object reference
   */
  private void addSubscriptionMenu(JMenuBar menuBar) {
    JMenuItem item;
  
    /* Option menu */
    JMenu menu = new JMenu("Subscription");
    menu.setMnemonic('c');
    menuBar.add(menu);
  }
  /** 
   * Add a number of useful tools relevant to the application, like a SmartSockets message 
   * composer, SVT word decoder, an analysis configuration tool etc.
   * @param menuBar   The Menubar object reference
   */
  private void addToolMenu(JMenuBar menuBar) {
    JMenuItem item;
    JMenu menu = new JMenu("Tools");
    menu.setMnemonic('t');
  
    Icon icon = new ImageIcon(AppConstants.iconDir+"Wizard.png");
    Action action = new AbstractAction("Send Command Message", icon) { 
      public void actionPerformed(ActionEvent e) {
        sendCommand();
      }
    };
    item = menu.add(action);
    menu.add(item);

    icon = new ImageIcon(AppConstants.iconDir+"Wizard.png");
    action = new AbstractAction("Configure Spy Monitoring", icon) { 
      public void actionPerformed(ActionEvent e) {
        configurator();
      }
    };
    item = menu.add(action);
    menu.add(item);
  
    action = new AbstractAction("Database Browser", null) { 
      public void actionPerformed(ActionEvent e) {
        JFrame f = new SvtDbBrowser(false);
        f.setSize(f.getPreferredSize());
        f.setVisible(true);
      }
    };
    item = menu.add(action);
    menu.add(item);

    action = new AbstractAction("Message Composer", null) { 
      public void actionPerformed(ActionEvent e) {
        composer();
      }
    };
    item = menu.add(action);
    menu.add(item);
  
    icon = new ImageIcon(AppConstants.iconDir+"mini-calc.png");
    action = new AbstractAction("Convert", icon) { 
      public void actionPerformed(ActionEvent e) {
        JFrame f = new Converter(false);
        f.setSize(220, 160);
        f.setVisible(true);
      }
    };
    item = menu.add(action);
    menu.add(item);
  
    icon = new ImageIcon(AppConstants.iconDir+"mini-application.png");
    action = new AbstractAction("Decode SVT Words", icon) {
      public void actionPerformed(ActionEvent e) {
        JFrame f = new DecodeWord("SVT Word Decoder", false);
        f.setSize(f.getPreferredSize());
        f.setVisible(true);
      }
    };
    item = menu.add(action);
    menu.add(item);
  
    action = new AbstractAction("Memory Test", null) { 
      public void actionPerformed(ActionEvent e) {
        JFrame f = new MemoryTest("Test JVM Memory", false);
        f.setSize(600, 400);
        f.setVisible(true);
      }
    };
    item = menu.add(action);
    menu.add(item);
  
    icon = new ImageIcon(AppConstants.iconDir+"mini-edit.png");
    action = new AbstractAction("Editor", icon) { 
      public void actionPerformed(ActionEvent e) {
        editFile();
      }
    };
    item =  menu.add(action); 
    menu.add(item);

    menuBar.add(menu);
  }
  private void updateHelpMenu(JMenuBar menuBar) {
    JMenuItem item;
    addHelpMenu();
    JMenu menu = menuBar.getMenu(menuBar.getMenuCount()-1);
  
    Action action = new AbstractAction("Description", null) { 
      public void actionPerformed(ActionEvent e) {
        Tools.showHelp(SpyMessenger.this, 
               Tools.getEnv("SVTMON_DIR")+"/help/Welcome.html",
               "Description of the application");
      }
    };
    item =  menu.add(action);
    menu.add(item);

    action = new AbstractAction("API Doc", null) { 
      public void actionPerformed(ActionEvent e) {
        JFrame f 
          = new Browser("file:"+Tools.getEnv("SVTMON_DIR")+"/doc/packages.html", false);
        f.setSize(f.getPreferredSize());
//        f.setSize(600, 650);
        f.setVisible(true);
      }
    };
    item =  menu.add(action);
    menu.add(item);
  }
  private void updateSubscriptionMenu(JMenuBar menuBar) {
    JMenuItem item;
    JMenu menu = menuBar.getMenu(3);
  
    subscriptionsCB = new JCheckBoxMenuItem[svtSubDest.length];
    for (int i = 0; i < subscriptionsCB.length; i++) {
      final int index = i;
      subscriptionsCB[index] = 
        new JCheckBoxMenuItem(AppConstants.svtSubLabels[index], subscriptionAtStart[index]);
      subscriptionsCB[index].addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          if (rThread != null) {
            boolean setCallback = subscriptionsCB[index].isSelected();
            switch (index) {
              case 0: 
                rThread.rcStateCallback(setCallback);
                break;
              case 1: 
                rThread.statusCallback(setCallback);
                break;
              case 2: 
                rThread.bufferCallback(setCallback);
                break;
              case 3: 
                rThread.histoCallback(setCallback);
                break;
              case 4: 
                rThread.beamCallback(setCallback);
                break;
              case 5: 
                rThread.ackCallback(setCallback);
                break;
              default:
                break;
            }
          }
        }
      });
      menu.add(subscriptionsCB[index]);
    }
  }
  /** Get the active state of the client */
  public int getPartition() {
    return activePartition;
  }
  /** 
   * Set active state of the client 
   * @param newState  Value of the new active state  
   */
  protected void setActiveState(int newState) {
    activeState = newState;
  }
  /** Get the active state of the client  */
  public int getActiveState() {
    return activeState;
  }
  /** Get the subject string of the client */
  public String getSubject() {
    return clientName;
  }
  /** Get the subject string of the client */
  public String getSender() {
    return sender;
  }
  /** 
   * Start a standalone SS message composer, opens a new window 
   */
  protected boolean composer() {
    if (!foundRTServer()) return false;

    JFrame f = new MessageComposer(this, srv, false);
//    f.setSize(f.getPreferredSize());
    f.setSize(550, 600);
    f.setVisible(true);

    return true;
  }
  /** 
   * Start the Svt analysis configurator. When we start the configurator
   * it only knows about those crates which have already communicated.
   * Although, in principle, new crates should be added to the list 
   * when new messages come, for simplicity it is no done. If you want
   * close the configurator window and open again for update or click
   * on the update button.
   */
  protected void configurator() {
    if (!foundRTServer()) return;

    if (configFrame == null) {
      configFrame = new SvtCrateConfig(false);
      configFrame.addWindowListener(winListener);
      //configFrame.setSize(configFrame.getPreferredSize());
      configFrame.setSize(700, 550);
    }
    configFrame.setVisible(true);
  }
  /** 
   * Start command sender
   */
  protected void sendCommand() {
    if (!foundRTServer()) return;

    JFrame frame = new SendCommandFrame(srv, false);
    frame.setSize(630, 570);
    frame.setVisible(true);
  }
  public boolean foundRTServer() {
    if (srv == null) {
      JOptionPane.showMessageDialog(SpyMessenger.this,
         "Open connection to RT Server first!", 
         "Imporper instantiation", JOptionPane.WARNING_MESSAGE);
      return false;
    }
    return true;
  }
  /** 
   * Start SS message receiver, opens a new message window and runs the 
   * receiver as a different thread 
   */
  protected boolean startReceiver() {
    if (!assertPartition()) return false;
    Tools.ensureEventThread();

    startReceiverThread();
    spyScheme.changeIcon(new ImageIcon(AppConstants.iconDir+"svt_scheme_2.png"));
    receiverAlive = true;
    return true;
  }
  /** Show message if partition is not correct and some application is attempted */ 
  protected void noPartition() {
    JOptionPane.showMessageDialog(this, "** Partition # cannot be "+activePartition,
       "Alert", JOptionPane.ERROR_MESSAGE); 
  }
  /** Check if we should be a part of R_C, i.e partitioning need be done */
  protected boolean assertPartition() {
    if (activePartition <= 0 && listenToRC.isSelected()) {
      noPartition();
      return false;
    }
    return true;
  }
  /** 
   * Start SS message receiver thread
   * @param  srv Reference to the RT Server
   */
  protected boolean startReceiverThread() { 
    stopThread = false;
    rThread = new ReceiverThread(srv, "Receiver");  
    return true;
  }
  /** 
   * Stop SS message receiver thread and close the display
   */
  protected void stopReceiver() {
    stopThread    = true;
    receiverAlive = false;
  }
  protected void exitApp() {
    confirmQuit();
  }
  /** Quit the application after confirmation */
  private void confirmQuit() {
    int confirm = JOptionPane.showOptionDialog(this,
                  "Really Exit?", "Exit Confirmation",
                  JOptionPane.YES_NO_OPTION, 
                  JOptionPane.QUESTION_MESSAGE,
                  null, null, null);
    if (confirm == 0) {
      if (rThread != null) rThread.registerCallback(false);      
      closeSrv();
      dispose();          
      System.exit(0);
    }
  }
  /** Save state of the object using serialization */
  protected void saveState() {
    String today = "Today";
    Date date    = new Date();
    System.out.println("saveState(): " + today + "  " + date);

    try {
      FileOutputStream out = new FileOutputStream("theTime.dat");
      ObjectOutputStream s = new ObjectOutputStream(out);
      s.writeObject(today);
      s.writeObject(date);
      s.flush();
      s.close();
    } catch(Exception e) {
      System.out.println("Error. " + e);
    }
  }
  /** Read state of the object from persistent storage */
  protected void readState() {
    try {
      FileInputStream in  = new FileInputStream("theTime.dat");
      ObjectInputStream s = new ObjectInputStream(in);
      String today = (String)s.readObject();
      Date date = (Date)s.readObject();
      s.close();
      System.out.println("readState(): " + today + "  " + date);
    } catch(Exception e) {
      System.out.println("Error. " + e);
    }
  }
  /** Open a simple text editor to edit short files */
  protected void editFile() {
    JFrame editor = new SimpleEditor(false);
//    editor.setSize(editor.getPreferredSize());
    editor.setSize(500, 550);
    editor.setVisible(true);     
  }
  /** Send an acknowledge in reply to some action taken by other clients (i.e R_C) 
   * @param dest Message destination  
   */
  protected void sendAck() {
    TipcMsg msgAck = null;
    String dest = rcSubDest[2];
    if (isDebugOn()) 
      getHistoryLogger().warn("Sending acknowledgement to " + dest);
    try {
      GenericAck ack = new GenericAck();
      ack.time       = (int) System.currentTimeMillis()/1000;
      ack.partition  = activePartition;
      ack.sender     = clientName;
      ack.state      = Integer.toString(activeState);
      ack.result     = "SUCCESS";

      msgAck = ack.pack();
      msgAck.setDest(dest);
      srv.send(msgAck);
      srv.flush();
      msgAck.destroy();
    } catch (TipcException e) {
      Tut.warning(e);
    }    
  }
  /** Check if partition is needed */
  public boolean isPartitionNeeded() {
    if (listenToRC.isSelected()) return true;
    else return false;
  }
  /** Process Partition message by setting a callback function */
  public class ProcessPartition implements TipcProcessCb {
    /** 
     * Process partition message when the callback is triggered
     * @param  msg   Reference to the SmartSockets message
     * @param  obj   User specified object that might be passed to the callback
     */
    public void process (TipcMsg msg, Object arg) {
      if (isDebugOn()) 
        getHistoryLogger().warn("process(): Setting partition ...");
      try {
        // position the field ptr to the beginning of the message
        msg.setCurrent(0);
        String cmd  = msg.nextStr();
        int  time   = msg.nextInt4();
        if (cmd.equals("rc.SetPartition")) {
          activeState      = RCStateConstants.STATE_WAITING;
          activePartition  = msg.nextInt4();
          if (isDebugOn()) getHistoryLogger().warn("setPartition "+activePartition);
          if ((activePartition >= RCStateConstants.MIN_PARTITION) && 
              (activePartition <= RCStateConstants.MAX_PARTITION)) {
            if (activeState == RCStateConstants.STATE_PARTITION) {
              getHistoryLogger().warn("process(): You must issue Reset first, activeState = " 
                               + activeState);
              return;
            }
            isPartitioned = true;
            activeState   = RCStateConstants.STATE_PARTITION;
            rcSubDest[1] = "/partition-"+activePartition+"/command"+clientName+"/...";
            rcSubDest[2] = "/partition-"+activePartition+"/ack";
            rcSubDest[3] = "/partition-"+activePartition+"/error";
            sendAck();
            if (isDebugOn()) showSubscriber("process(): At partition");
          }
        }
      } catch (TipcException e) {
        Tut.warning(e);
      }    
    } 
  }
  /** 
   * Convenience method to print subscribers 
   * @param caller  The method which calls this 
   */
  public void showSubscriber(String caller) {
    System.out.println(caller + " Subscribers: ");
    String [] subs = srv.getSubscribedList();
    for (int i = 0; i < subs.length; i++) {
       System.out.println(subs[i]);
    }
  }
  public void insertScheme() {
    getDefaultSubscription();
    if (spyScheme == null) spyScheme = new SpySchemePanel(this);
    splitPane.remove(seManager);
    splitPane.setTopComponent(spyScheme);
    splitPane.resetToPreferredSizes();

    if (connectByDefault) {
      try {
        initSrv();
        HistoMessageCreator.createMessageTypes();
        updateSubscriptionMenu(getJMenuBar());
        startReceiver();
      }
      catch (TipcException e) {
        Tut.warning(e);
      }
      catch (NullPointerException e) {
        e.printStackTrace();
      }
    }
  }
  private void getDefaultSubscription() {
    for (int i = 0; i < subscriptionAtStart.length; i++) {
      subscriptionAtStart[i] = seManager.getSubscriptionPanel().isSubjectSubscribed(i);
      svtSubDest[i]          = seManager.getSubscriptionPanel().getSubjectSubscribed(i);
    }
  }
  public void openViewer(String title, String data, String label) {
    DataFrame f = new DataFrame(false, title, true);
    f.setText(data);
    f.setStatusText(label);
    f.setSize(620, 630);
    f.setVisible(true);
  }
  public void showCrateStatus(final SvtCrateData crateData) {
    if (crateStatusFrame == null) {
      crateStatusFrame = new SvtCratesFrame(false);
      crateStatusFrame.addWindowListener(winListener);
      crateStatusFrame.pack();
      crateStatusFrame.setVisible(true); // as it is opened automatically
    }
    crateStatusFrame.updateGUI(crateData);      
  }
  public void createHistogramViewer() {
    histoDisplay = new HistogramDisplayFrame(false);
    histoDisplay.addWindowListener(winListener);
    histoDisplay.showCanvas();
    histoDisplay.setSize(histoDisplay.getPreferredSize());
  }
  public void showHistogramViewer() {
    if (histoDisplay == null) throw 
      new NullPointerException("Create HistogramDisplayFrame first!"); 
    histoDisplay.setVisible(true);
  }
  class MyWindowListener extends WindowAdapter implements WindowListener {
    public MyWindowListener() {}
    public void windowClosed(WindowEvent e) {
      if (isDebugOn()) System.out.println(e.paramString());
      Window obj = e.getWindow();
      if (isDebugOn()) System.out.println(((JFrame)obj).getTitle());
      if (obj instanceof HistogramDisplayFrame) {
        histoDisplay.removeWindowListener(winListener);
        histoDisplay = null;
      }
      else if (obj instanceof SvtCratesFrame) {
        crateStatusFrame.removeWindowListener(winListener);
        crateStatusFrame = null;
      }
      else if (obj instanceof SvtCrateConfig) {
        configFrame.removeWindowListener(winListener);
        configFrame = null;
      }
    }
  }
  public TabPanel getTabPanel() {
    return tabPanel;
  }
  public SpySchemePanel getSpyScheme() {
    return spyScheme;
  }
  public SvtCratesFrame getCratesFrame() {
    return crateStatusFrame;
  }
  public HistogramDisplayFrame getHistogramDisplay() {
    return histoDisplay;
  }
  public TextPanel getHistoryLogger() {
    return tabPanel.getHistoryPanel();
  }
  public String [] getRCDest() {
    return rcSubDest;
  }
  public String getRCDest(int index) {
    return rcSubDest[index];
  }
  public String [] getSVTDest() {
    return svtSubDest;
  }
  public String getSVTDest(int index) {
    return svtSubDest[index];
  }
  public void setRCDest(String [] dest) {
    rcSubDest = dest;
  }
  public void setRCDest(int index, String dest) {
    rcSubDest[index] = dest;
  }
  public void setSVTDest(String [] dest) {
    svtSubDest = dest;
  }
  public void setSVTDest(int index, String dest) {
    svtSubDest[index] = dest;
  }
  public SvtBufferData getBufferData(final String crate, int slot, 
                                     final String board, final String spy) 
    throws NullPointerException 
  {
    return rThread.getErrorData().getCrateData(crate).getBoardData(board,slot).getBufferData(spy);
  }
  public ReceiverThread getReceiverThread() {
    return rThread;
  }
  public SvtErrorData getErrorData() {
    return rThread.getErrorData();
  }
  public void updateSpyScheme(int index) {
    spyScheme.changeButtonColor(index, Color.green);
    nAck++;
    if (nAck == AppConstants.nCrates) 
      spyScheme.changeIcon(new ImageIcon(AppConstants.iconDir+"svt_scheme_3.png"));
  }

  /**
   * <P>
   * A separate thread which spins off SpyMessenger, sets up R_C command and Spy Buffer 
   * related callbacks and waits indefinitely for the messages. When relevant messages
   * arrive proper actions are taken. This thread manipulates parent variables 
   * 'activePartition' 'clientName' and 'activeState' via protected methods.</P>
   *
   * <P>
   * Before taking any action which involves partition (R_C), this thread must check 
   * whether the application wants to be a part of the partition. This is needed in 
   * the light of the fact that it is not really simple for a unix process to become 
   * a SS client in the real data taking.</P>
   *
   * @author  Subir Sarkar
   * @version 0.1, July 2000
   */
  public class ReceiverThread extends Observable 
  			      implements Runnable, 
  					 UserCallout
  {
      /** Sleep for 10 seconds before looking for message again */
    private final int N_SECONDS = 10000;
      /** The receiver thread itself */
    private Thread thread;
      /** Reference to the caller class */
    protected SpyMessenger parent;
  
      /** Reference to SmartSockets RT Server */
    private TipcSrv srv = null;
  
      /** Reference to Spy Buffer message handler */
    private SpyDataMessage sbDataMessage;
      /** Reference to Svt error + spy buffer message handler */
    private SvtErrorDataMessage seDataMessage;
      /** Reference to histogram message handler */
    private HistogramColl histoData = null;
    private BeamPosition beamPos    = new BeamPosition();
    private SpyBufferDump spyBuff   = new SpyBufferDump();
  
    // Observers
    private Observer histoObserver  = new HistogramObserver();
    private Observer errorObserver  = new SvtErrorObserver();
    private Observer beamObserver   = new BeamObserver();
    private Observer bufferObserver = new SpyBufferObserver();
  
    private RCState rcState  = null;
  
    /** 
     * Initialise the Thread 
     * @param parent  The <CODE>SpyMessenger</CODE>
     * @param srv     The SmartSockets RT Server
     * @param mode    The title string of the thread
     */
    public ReceiverThread(TipcSrv srv, String mode) {
      this.srv = srv;
      if (this.srv != null) registerCallback(true);
      
      if (thread == null) {
        thread = new Thread(this, mode);
  	thread.start();
      }
    }
    /** Override and implement the run method of <CODE>Thread</CODE> class */
    public void run() {
      Thread currThread = Thread.currentThread();
      while (thread == currThread) {
  	do {
  	  try {
  	    srv.mainLoop(3.0); 
  	  }  catch (TipcException te) {
  	    Tut.warning(te);
  	    warn("run(): TipcException " + te.getMessage(), Color.red);
  	  }
  	  try {
  	    Thread.sleep((long)(Math.random() * N_SECONDS));
  	  } catch (InterruptedException e) {
  	    warn("run(): Sleep interrupted " + e, Color.red);
  	  }
  	} while (!stopThread);
      }
    }
    /** Inner class to process R_C command related messages */
    public class ProcessCommandMessage implements TipcProcessCb {
      /** 
       * Process R_C command related messages when the callback is triggered
       * @param  msg   Reference to the SmartSockets message
       * @param  obj   User specified object that might be passed to the callback
       */
      public void process (TipcMsg msg, Object arg) {
  	if (!isPartitionNeeded()) {
  	  if (rcState == null) rcState = new RCState(partitionToWatch);
  	  rcState.update(msg);
  	  if (isDebugOn()) System.out.println(rcState);
  	}
  	else { 
  	  try {
  	    msg.setCurrent(0);  // position the field ptr to the beginning of the message
  	    String cmd = msg.nextStr();
  	    int time   = msg.nextInt4();
  	    int part   = msg.nextInt4();
  	    warn("Processing Command " + cmd, Color.blue);
  	    
  	    String sender = msg.getSender();
  	    String dest   = msg.getDest();
//  	    setSender(sender);
  	    if (isDebugOn()) System.out.println("Sender: " + sender + " Dest: " + dest);
  	    
  	    if (cmd.equals("rc.TransitionMessage")) {
  	      cmd = msg.nextStr();
  	      warn("R_C Command:" + cmd, Color.green);
  	    
  	      if (isPartitionNeeded()) sendAck();
  	      if (cmd.equals("Shutdown")) {
  		callout(cmd, RCStateConstants.STATE_SHUTDOWN, Color.red);
  	      }
  	      else if (cmd.equals("Reset")) {
  		if (isPartitionNeeded()) {
  		  registerCallback(false);
  		  redoPartition();
  		}
  		callout(cmd, RCStateConstants.STATE_RESET, Color.green);
  	      }
  	      else if (cmd.equals("Activate")) {
  		callout(cmd, RCStateConstants.STATE_ACTIVE, Color.green);
  	      }
  	      else if (cmd.equals("Config") || cmd.equals("Setup")) {
  		callout(cmd, RCStateConstants.STATE_READY, Color.green);
  	      }
  	      else if (cmd.equals("End")) {
  		callout(cmd, RCStateConstants.STATE_IDLE, Color.yellow);
  	      }
  	      else if (cmd.equals("Abort")) {
  		msg.print();
  		callout(cmd, RCStateConstants.STATE_WAITING, Color.red);
  	      }
  	      else if (cmd.equals("Halt")) {
  		callout(cmd, RCStateConstants.STATE_READY, Color.red);
  	      }
  	      else if (cmd.equals("Recover")) {
  		callout(cmd, RCStateConstants.STATE_ACTIVE, Color.yellow);
  	      }
  	      else if (cmd.equals("Run")) {
  		callout(cmd, RCStateConstants.STATE_ACTIVE, Color.green);
  	      }
  	      else if (cmd.equals("Pause")) {
  		callout(cmd, RCStateConstants.STATE_READY, Color.green);
  	      }
  	      else if (cmd.equals("Resume")) {
  		callout(cmd, RCStateConstants.STATE_ACTIVE, Color.green);
  	      }
  	    
  	      // always update GUI via event handling thread
  	      Runnable setLabelRun = new Runnable() {
  		public void run() {
  		  try {
  		    getTabPanel().getCommandPanel().updateLabels();
  		  }
  		  catch (Exception ex) {
  		    ex.printStackTrace();
  		  }
  		}
  	      };
  	      SwingUtilities.invokeLater(setLabelRun);
  	    }
  	  } catch (TipcException e) {
  	    Tut.warning(e);
  	  }
  	}    
      } 
    }
    /** Inner class to handle SVT event related message */
    public class ProcessBufferMesssage implements TipcProcessCb {
      /** 
       * Process SVT Buffer data messages when the callback is triggered
       * @param  msg   Reference to the SmartSockets message
       * @param  obj   User specified object that might be passed to the callback
       */
      public void process (TipcMsg msg, Object arg) {
        String sender   = new String(" ");
  	StringBuilder sb = new StringBuilder(AppConstants.LARGE_BUFFER_SIZE);
  	String line;
  	try {
  	  msg.setCurrent(0);  // position the field ptr to the beginning of the message
  	  if (isDebugOn()) 
            System.out.println("ProcessBufferMesssage: Spy Buffer message published ...");
  		//  For the time being just print the last buffer published
  		//   if (sbDataMessage == null) 
  		//   sbDataMessage = new SpyDataMessage(parent, srv);
  		//   sbDataMessage.update(msg);
  	  while (true) {
  	    line = msg.nextStr();
            if (sender.equals(" ") && line.startsWith("b0svt")) sender = line.trim();
  	    if (line == null) break;
  	    sb.append(line).append("\n");
  	  }
  	} 
        catch (TipcException e) {
          // Ignore the exception as this is way to indicate EOF
  	  // Tut.warning(e);
  	}    

        spyBuff.setSender(sender);
        spyBuff.setText(sb.toString());

        setChanged();
        notifyObservers(spyBuff); 
      }
    }
    /** Inner class to handle SVT event related message */
    public class ProcessBeamMessage implements TipcProcessCb {
      /** 
       * Process Beam alignment messages when the callback is triggered
       * @param  msg   Reference to the SmartSockets message
       * @param  obj   User specified object that might be passed to the callback
       */
      public void process (TipcMsg msg, Object arg) {
  	beamPos.update(msg);
  	
  	setChanged();
  	notifyObservers(beamPos);
      } 
    }
    /** Inner class to handle SVT error related message */
    public class ProcessStatusMessage implements TipcProcessCb {
      /** 
       * Process SVT error messages when the callback is triggered
       * @param  msg   Reference to the SmartSockets message
       * @param  obj   User specified object that might be passed to the callback
       */
      public void process (TipcMsg msg, Object arg) {
  	try {
  	  msg.setCurrent(0);  // position the field ptr to the beginning of the message
          String sender = msg.getSender();
  	  if (isDebugOn()) System.out.println("Sender: "+ sender);
          if (sender.indexOf("b0svttest00") > -1) return;

  	  if (seDataMessage == null) 
  	     seDataMessage = new SvtErrorDataMessage();
  	  seDataMessage.update(msg);
  	} catch (TipcException e) {
  	  Tut.warning(e);
  	}    

  	setChanged();
  	notifyObservers(seDataMessage.getCrateData());
      } 
    }
    /** Inner class to handle SVT crate sckowledgement message */
    public class ProcessAckMessage implements TipcProcessCb {
      /** 
       * Process SVT acknowledgement messages when the callback is triggered
       * @param  msg   Reference to the SmartSockets message
       * @param  obj   User specified object that might be passed to the callback
       */
      int crateID;
      public void process (TipcMsg msg, Object arg) {
  	String [] mess = null;
  	try {
  	  msg.setCurrent(0);  // position the field ptr to the beginning of the message
  	  String line = msg.nextStr();
  	  mess = Tools.split(line, " ");
  	} catch (TipcException e) {
  	  Tut.warning(e);
  	}    
  	getTabPanel().getHistoryPanel().warn(mess[0]+" responds", Color.green);
  	crateID = Tools.getCrateIndex(mess[0]);
  
  	// always update GUI via event handling thread
  	Runnable setLabelRun = new Runnable() {
  	  public void run () {
  	    try {
  	      getSpyScheme().getButtonPanel().getButton(crateID).setBackground(Color.green);
  	      getSpyScheme().getLabelPanel().getLabel().setIcon(
  		  new ImageIcon(AppConstants.iconDir+"svt_scheme_3.png"));
  	    }
  	    catch (Exception ex) {
              StringWriter sw = new StringWriter();
              ex.printStackTrace(new PrintWriter(sw));
              String stacktrace = sw.toString();
              System.out.println("stacktrace = " + stacktrace);
  	    }
  	  }
  	};
  	SwingUtilities.invokeLater(setLabelRun);
      } 
    }
    /** Inner class to handle SVT Histogram related message */
    public class ProcessHistMessage implements TipcProcessCb {
      /** 
       * Process SVT Histogram related messages when the callback is triggered
       * @param  msg   Reference to the SmartSockets message
       * @param  obj   User specified object that might be passed to the callback
       */
      public void process (TipcMsg msg, Object arg) {
  	if (isDebugOn()) System.out.println("Histogram messages published");
  	try {
  	  msg.setCurrent(0);  // position the field ptr to the beginning of the message
  	  int run   = msg.nextInt4();
  	  int time  = msg.nextInt4();
  	  int nhist = msg.nextMsgArray().length;   // Find how many histograms are published
  	  if (isDebugOn()) System.out.println("# of histograms published: " + nhist);
  	  if (nhist > 0) {
  	    if (histoData == null) histoData = new SvtHistogramColl();
  	    histoData.fillHistogramData(msg);
  	  }
  	} catch (TipcException e) {
  	  Tut.warning(e);
  	}    

  	setChanged();
        notifyObservers(histoData);
      } 
    }
    /** Register R_C command and SVT event callbacks */
    protected void registerCallback(boolean setCallback) throws NullPointerException {
      String wtxt = "registerCallback(): " 
  		  + ((setCallback)?"Register":"Unregister") + " callbacks";
      warn(wtxt, ((setCallback) ? Color.green : Color.red));
      if (!isPartitionNeeded()) rcStateCallback(setCallback);
      statusCallback(setCallback);
      bufferCallback(setCallback);
      histoCallback(setCallback);
      beamCallback(setCallback);
      ackCallback(setCallback);
    }
    /** Register R_C command callback */
    protected void rcStateCallback (boolean setCallback) {
      TipcCb pComm = null;
      String dest = svtSubDest[0];
      
      try {
  	if (setCallback) {
  	  if (subscriptionsCB[0].isSelected()) { 
  	    if (!srv.getSubjectSubscribe(dest)) {
  	      ProcessCommandMessage comRef = new ProcessCommandMessage();
  	      pComm = srv.addProcessCb(comRef, dest, srv);
  	      if (pComm == null) {
  		Tut.exitFailure("WARNING. Couldn't register command subject callback!\n");
  	      }
  	      srv.setSubjectSubscribe(dest, true);
  	      if (isDebugOn()) System.out.println("INFO. Subscribed to  " + dest);
  	      warn("INFO. Subscribed to  " +  dest, Color.green);
  	    }
  	  }
  	} else {
  	  if (srv.getSubjectSubscribe(dest)) {
  	    srv.setSubjectSubscribe(dest, false);
  	    warn("INFO. Unsubscribed from  " +  dest, Color.red);
  	  }
  	}
      } catch (TipcException Tipe) {
  	Tut.warning(Tipe);
      } 
    }
    /** Register SVT status callback */
    protected void statusCallback (boolean setCallback) {
      TipcCb pErro = null;
      String dest  = AppConstants.STATUS_SUBJECT;
      try {
  	if (setCallback) {
  	  if (subscriptionsCB[1].isSelected()) {
  	    if (!srv.getSubjectSubscribe(dest)) {
  	      ProcessStatusMessage evRef = new ProcessStatusMessage();
  	      pErro = srv.addProcessCb(evRef, dest, srv); 
  	      if (pErro == null) {
  		Tut.exitFailure("WARNING. Couldn't register error subject callback!\n");
  	      }
  	      srv.setSubjectSubscribe(dest, true);
  	      if (isDebugOn()) System.out.println("INFO. Subscribed to  " + dest);
  	      warn("INFO. Subscribed to  "+  dest, Color.green);
  	    }
  
  	    addObserver(errorObserver);
  	  }
  	} else {
  	  if (srv.getSubjectSubscribe(dest)) {
  	    srv.setSubjectSubscribe(dest, false);
  	    warn("INFO. Unsubscribed from  " +  dest, Color.red);
  
  	    deleteObserver(errorObserver);
  	  }
  	}
      } catch (TipcException Tipe) {
  	Tut.warning(Tipe);
      } 
    }
    /** Register SVT buffer callback */
    protected void bufferCallback (boolean setCallback) {
      TipcCb pEven = null;
      String dest  = AppConstants.BUFFER_SUBJECT;;
      try {
  	if (setCallback) {
  	  if (subscriptionsCB[2].isSelected()) {
  	    if (!srv.getSubjectSubscribe(dest)) {
  	      ProcessBufferMesssage evRef = new ProcessBufferMesssage();
  	      pEven = srv.addProcessCb(evRef, dest, srv); 
  	      if (pEven == null) {
  		Tut.exitFailure("WARNING. Couldn't register event subject callback!\n");
  	      }
  	      srv.setSubjectSubscribe(dest, true);
  	      if (isDebugOn()) System.out.println("INFO. Subscribed to  " + dest);
  	      warn("INFO. Subscribed to  "+  dest, Color.green);
  	    }
  	 
  	    addObserver(bufferObserver);
  	  }
  	} else {
  	  if (srv.getSubjectSubscribe(dest)) {
  	    srv.setSubjectSubscribe(dest, false);
  	    warn("INFO. Unsubscribed from  " +  dest, Color.red);
  
  	    deleteObserver(bufferObserver);
  	  }
  	}
      } catch (TipcException Tipe) {
  	Tut.warning(Tipe);
      } 
    }
    /** Register SVT histogram callback */
    protected void histoCallback (boolean setCallback) {
      TipcCb pHist = null;
      String dest  = AppConstants.HISTO_SUBJECT;
      try {
  	if (setCallback) {
  	  if (subscriptionsCB[3].isSelected()) {
  	    if (!srv.getSubjectSubscribe(dest)) {
  	      ProcessHistMessage evRef = new ProcessHistMessage();
  	       pHist = srv.addProcessCb(evRef, dest, srv); 
  	      if (pHist == null) {
  		Tut.exitFailure("WARNING. Couldn't register Histogram subject callback!\n");
  	      }
  	      srv.setSubjectSubscribe(dest, true);
  	      if (isDebugOn()) System.out.println("INFO. Subscribed to  " + dest);
  	      warn("INFO. Subscribed to  "+  dest, Color.green);
  
  	      addObserver(histoObserver);
  	    }
  	  }
  	} else {
  	  if (srv.getSubjectSubscribe(dest)) {
  	    srv.setSubjectSubscribe(dest, false);
  	    warn("INFO. Unsubscribed from  " +  dest, Color.red);
  
  	    deleteObserver(histoObserver);
  
  	  }
  	}
      } catch (TipcException Tipe) {
  	Tut.warning(Tipe);
      } 
    }
    /** Register SVT beam alignment related callback */
    protected void beamCallback (boolean setCallback) {
      TipcCb pBeam = null;
      String dest  = AppConstants.BEAM_SUBJECT;
      try {
  	if (setCallback) {
  	  if (subscriptionsCB[4].isSelected()) {
  	    if (!srv.getSubjectSubscribe(dest)) {
  	      ProcessBeamMessage evRef = new ProcessBeamMessage();
  	      pBeam = srv.addProcessCb(evRef, dest, srv); 
  	      if (pBeam == null) {
  		Tut.exitFailure("WARNING. Couldn't register error subject callback!\n");
  	      }
  	      srv.setSubjectSubscribe(dest, true);
  	      if (isDebugOn()) System.out.println("INFO. Subscribed to  " + dest);
  	      warn("INFO. Subscribed to  "+  dest, Color.green);
  
  	      addObserver(beamObserver);
  	    }
  	  }
  	} 
        else {
  	  if (srv.getSubjectSubscribe(dest)) {
  	    srv.setSubjectSubscribe(dest, false);
  	    warn("INFO. Unsubscribed from  " +  dest, Color.red);
  
  	    deleteObserver(beamObserver);
  	  }
  	}
      } 
      catch (TipcException Tipe) {
  	Tut.warning(Tipe);
      } 
    }
    /** Register SVT acknowledgement callback */
    protected void ackCallback (boolean setCallback) {
      TipcCb pAck = null;
      String dest = AppConstants.ACK_SUBJECT;
      try {
  	if (setCallback) {
  	  if (subscriptionsCB[5].isSelected()) {
  	    if (!srv.getSubjectSubscribe(dest)) {
  	      ProcessAckMessage evRef = new ProcessAckMessage();
  	      pAck = srv.addProcessCb(evRef, dest, srv); 
  	      if (pAck == null) {
  		Tut.exitFailure("WARNING. Couldn't register Acknowledment subject callback!\n");
  	      }
  	      srv.setSubjectSubscribe(dest, true);
  	      if (isDebugOn()) System.out.println("INFO. Subscribed to  " + dest);
  	      warn("INFO. Subscribed to  "+  dest, Color.green);
  	    }
  	  }
  	} else {
  	  if (srv.getSubjectSubscribe(dest)) {
  	    srv.setSubjectSubscribe(dest, false);
  	    warn("INFO. Unsubscribed from  " +  dest, Color.red);
  	  }
  	}
      } catch (TipcException Tipe) {
  	Tut.warning(Tipe);
      } 
    }
    public void warn(String text, Color color) {
      getTabPanel().getHistoryPanel().warn(text, color);
    }
    public SvtErrorData getErrorData() {
      return seDataMessage.getErrorData();
    }
      /** 
       *  Respond to R_C state transition commands by setting new active state,
       *  printing the transition message etc.
       * 
       *  @param state  RC State
       *  @param value  Value which the active state will assume now 
       *  @param color  Text color which depends on the type of RC State (reset, end etc.)
       */
    public void callout(final String state, int value, Color color) {
      warn("Callout: R_C state " + state, color);
      setActiveState(value);
    }
      /** 
       *  Set new active state
       *  @param state  RC State
       */
    public void setActiveState(int state) {
      setActiveState(state);
    }
  }
  public class BeamObserver implements Observer {
    public BeamObserver() {}
    public void update(Observable obj, Object arg) {
      if (!(arg instanceof BeamPosition)) return;
      final BeamPosition bp = (BeamPosition) arg;
      // Always update GUI via event handling thread
      Runnable setLabelRun = new Runnable() {
        public void run () {
          try {
            getTabPanel().getBeamPanel().displayText(bp.toString());
          }
          catch (Exception ex) {
            ex.printStackTrace();
          }
        }
      };
      SwingUtilities.invokeLater(setLabelRun);
    }
  }  
  public class SpyBufferObserver implements Observer {
    public SpyBufferObserver() {}
    public void update(Observable obj, Object arg) {
      if (!(arg instanceof SpyBufferDump)) return;
      if (!dumpSpyCB.isSelected()) return;
     
      final SpyBufferDump spyDump = (SpyBufferDump) arg; 

      SvtDataManager dataManager = SvtDataManager.getInstance();
      dataManager.updateBuffer(spyDump.sb.toString());

      // Always update GUI via event handling thread
      Runnable setLabelRun = new Runnable() {
        public void run () {
          try {
            // Probably it is not required to club together all the non-GUI 
            // statements. They might be used outside the event handling
            // thread as well
            getTabPanel().getDumpPanel().setSender(spyDump.sender);
            getTabPanel().getDumpPanel().setData(spyDump.sb.toString());
            getTabPanel().getDumpPanel().update();
          }
          catch (Exception ex) {
            ex.printStackTrace();
          }
        }
      };
      SwingUtilities.invokeLater(setLabelRun);
    }   
  }
  public class HistogramObserver implements Observer {
    public HistogramObserver() {}
    public void update(Observable obj, Object arg) {
      if (!(arg instanceof HistogramColl)) return;
      final HistogramColl histoData = (HistogramColl) arg;
  
      if (histoDisplay == null) createHistogramViewer();

      // Always update GUI via event handling thread
      Runnable setLabelRun = new Runnable() {
        public void run () {
          try {
            histoDisplay.update(histoData);
          }
          catch (Exception ex) {
            ex.printStackTrace();
          }
        }
      };
      SwingUtilities.invokeLater(setLabelRun);
      if (isDebugOn()) System.out.println("Histogram dump:\n" + histoData);
    }
  }
  public class SvtErrorObserver implements Observer {
    public SvtErrorObserver() {}
    public void update(Observable obj, Object arg) {
      if (!(arg instanceof SvtCrateData)) return;
      final SvtCrateData crateData = (SvtCrateData) arg;
  
      // Do not update GUI directly but put it in the event queue
      // This is the safest way when using Swing in a threaded application.
      // After a widget has been made visible, only the event handling
      // thread which is started automatically by the JVM is allowed to
      // modify it, this is a feature which is there to optimize performance with
      // Swing components.
      Runnable setLabelRun = new Runnable() {
        public void run() {
          try {
            updateChain(crateData);
          }
          catch (NullPointerException ex) { 
            System.out.println("Exception " + ex.getMessage());
            ex.printStackTrace();
          }
        }
      };
      SwingUtilities.invokeLater(setLabelRun);
    }
    public void updateChain(final SvtCrateData crateData) {
   
      // Create a map with ("Crate Name", "Crate Data") 
      // Only one instance of the map should serve each and 
      // every operation with SVT Crate Status informations
      SvtCrateMap map = SvtCrateMap.getInstance();
      int index = Tools.getCrateIndex(crateData.getName());
      if (!map.isCrateReady(index)) {
        map.addEntry(crateData);
        getHistoryLogger().warn(crateData.getName()+
      		   ": message at " + new Date(), Color.blue);
  
        // Update the color of the buttons on the Spy scheme panel.
        // For the time being just make one green if spyGUI received
        // data from it. Later on we should set up timers such that
        // after receiveing data from individual crates individual
        // timers will go off after a certain duration and if 
        // more data is NOT published during that time a warning will 
        // be issued.
        updateSpyScheme(index);

        // Add new crate in SvtCrateConfig window if the window is open
        if (configFrame != null) configFrame.updateGUI(crateData);
      }
  
      // The crate status should not be opened automatically.
      // We should wait for the first crate status message(s) to arrive, dynamically
      // create the Crate status window and display status. At that point
      // It will contain only those crates which will have sent informations.
      showCrateStatus(crateData);
    }
  }
  public class SpyBufferDump {
    public String sender;
    public StringBuilder sb = new StringBuilder(AppConstants.LARGE_BUFFER_SIZE);
    public SpyBufferDump() {
      this("Unknown", "Not valid");
    }
    public SpyBufferDump(String sender, String text) {
      this.sender = sender;
      this.sb.insert(0, text);
    }
    public void setSender(final String sender) {this.sender = sender;}
    public void setText(final String text) {
      sb.setLength(0);
      sb.insert(0, text);
    }
    public String getSender() {return sender;}
    public String getText() {return sb.toString();}
  }
  /** Test the class standalone */
  public static void main(String [] argv) {
    JFrame app = new SpyMessenger("Spy Messenger");
    app.setSize(600, 650);
    app.setVisible(true);
  }
}   
