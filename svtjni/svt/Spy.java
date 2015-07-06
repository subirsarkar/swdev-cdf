package svt;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import org.omg.CORBA.IntHolder;

import config.util.*;
import jsvtvme.*;

/**
 * Class which represent the Spy control boards with the accessible
 * R/W VME registers. This application uses the new svtvme library
 *
 * @version 0.6
 * @author Subir Sarkar, GUI Adapted from Spy.java in svtdaq by Thomas Speer
 */

public class Spy extends JFrame 
         implements SwingConstants, SvtvmeConstants  {

  private static final int MAX_CRATE = 8;
     /** Path where to find the icons used in this application */
  protected static final String 
     iconDir = Tools.getEnv("SVTMON_DIR")+"/icons/";
  
  protected JPanel statusPanel;
  protected JLabel statusLabel;
  protected JPanel statusP;
  protected JPanel masterP;
  protected JComboBox crateNameCB, slotCB;
  protected JMenuBar menuBar;
    /** File Open, Save Dialog */
  protected JFileChooser fileChooser;             

  protected Action connectAction    = new ConnectAction();
  protected Action disconnectAction = new DisconnectAction();
  protected Action updateAction     = new UpdateAction();
  protected Action saveAction       = new SaveAction();
  protected Action exitAction       = new ExitAction();
  protected Action aboutAction      = new AboutAction(); 

  protected JButton masterB;
  protected StatusButton masterSB, daisySB; 
 
  public int gInitGen, gFreezeGen, gFreezeDelay;
  public int svtInitGen, svtFreezeGen, svtFreezeDelay, lvl1Counter;
  public int gErrorGen, gLlockGen, gBusInput, backplane;
  public int cdfErrorGen, cdfRecover;
  public boolean master, daisy;

  protected JTabbedPane tabbedPane;
  protected SpySlaveStatPane spySlaveStatPane;
  protected SpySlaveGenPane spySlaveGenPane;
  protected SpySlaveErrPane spySlaveErrPane;
  protected SpyMasterPane spyMasterPane;
  protected SpyHistoryPane spyHistoryPane;

  protected Vector paneV;

  private int slot;
  protected Board svtB;
  private String crate;

    /* Define preferred sizes for my entry fields */
  private static Dimension shortSize  = new Dimension( 30, 20);
  private static Dimension mediumSize = new Dimension(100, 20);
  private static Dimension longSize   = new Dimension(240, 20);
  private static Dimension hugeSize   = new Dimension(240, 80);
  
    /** Load the shared native library */
  static {
    try {
      System.loadLibrary("SvtvmeImpl");
    } catch (UnsatisfiedLinkError e) {
      System.err.println("Cannot load the example native code. " + 
        " \nMake sure LD_LIBRARY_PATH contains the shared library path, try " +
        "setup svtvme -d\n" + e);
      System.exit(1);
    }
  }

    /* Constructor */
  public Spy (String title, final int slot)  {
    super(title);
    this.slot = slot;
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE); 
    WindowListener wndCloser = new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        confirmQuit();
      }
    };
    addWindowListener(wndCloser);
    buildGUI();
  }
    /** Get crate name and spy slot number from GUI */
  public void getInput() {
    String slotString  = (String) slotCB.getSelectedItem();
    slot = Integer.parseInt(slotString);
    crate = (String) crateNameCB.getSelectedItem(); 
  }

  /**
   * Create the status panel which will display the board's state
   */
  private void buildGUI() {

    Container content = getContentPane();

    crateNameCB = new JComboBox();
    crateNameCB.setPreferredSize(mediumSize);
    crateNameCB.setEditable(false);
    crateNameCB.addItem("b0svttest00");
    for (int i = 0; i < MAX_CRATE; i++) 
      crateNameCB.addItem("b0svt0"+i);
    crateNameCB.setSelectedIndex(0);

    crateNameCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) { 
        History.addText(Spy.this.getTextPane(), 
          "Crate " + crateNameCB.getSelectedItem() + " selected");
      }
    });

    slotCB = new JComboBox();
    slotCB.setPreferredSize(shortSize);
    slotCB.setEditable(true);
    slotCB.addItem("3");
    // slotCB.setAlignmentX(SwingConstants.LEFT_ALIGNMENT);
    slotCB.setSelectedIndex(0);
    slotCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) { 
        History.addText(Spy.this.getTextPane(), 
          "Slot " + slotCB.getSelectedItem() + " selected");
      }
    });

    statusP = new JPanel(true);
    statusP.setLayout(new BorderLayout());
    statusP.setBorder(BorderFactory.createEtchedBorder());

    spySlaveStatPane = new SpySlaveStatPane(this);
    spySlaveGenPane  = new SpySlaveGenPane(this);
    spySlaveErrPane  = new SpySlaveErrPane(this);
    spyMasterPane    = new SpyMasterPane(this);
    spyHistoryPane   = new SpyHistoryPane(this);

    paneV = new Vector(5);
    paneV.addElement(spySlaveStatPane);
    paneV.addElement(spySlaveGenPane);
    paneV.addElement(spySlaveErrPane);
    paneV.addElement(spyMasterPane);
    paneV.addElement(spyHistoryPane);

    tabbedPane = new JTabbedPane();
    tabbedPane.addTab("Slave - Status", null, spySlaveStatPane);
    tabbedPane.addTab("Slave - Generation", null, spySlaveGenPane);
    tabbedPane.addTab("Slave - Error", null, spySlaveErrPane);
    tabbedPane.addTab("Master", null, spyMasterPane);
    tabbedPane.addTab("History", null, spyHistoryPane);
    tabbedPane.setSelectedIndex(0);
    statusP.add(tabbedPane, BorderLayout.CENTER);

    /* Master Status */

    masterP = new JPanel();
    // masterP.setBorder(BorderFactory.createEtchedBorder());

    JLabel label = new JLabel("Jumpers: ");
    label.setForeground(Color.black);
    masterP.add(label);
    masterP.add(masterSB = new StatusButton("Master Enabled", "Master Disabled", master));
    masterP.add(daisySB  = new StatusButton("Daisy chain end", "Not End", daisy));
    masterP.add(masterB  = new JButton("Read"));
    masterB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) { 
	readJumperStatus();
      }
    });

    statusP.add(masterP, BorderLayout.SOUTH);
    content.add(statusP, BorderLayout.CENTER);

    statusPanel = new JPanel(new BorderLayout());
    statusPanel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1)); 
    statusLabel = new JLabel("Ready ....", SwingConstants.LEFT);
    statusLabel.setForeground(Color.black);
    statusLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    statusPanel.add(statusLabel, BorderLayout.WEST);
    content.add(statusPanel, BorderLayout.SOUTH);

    content.add(createToolBar(), BorderLayout.NORTH);

    menuBar = createMenuBar();
    setJMenuBar(menuBar);

    fileChooser = new JFileChooser(); 
    fileChooser.setCurrentDirectory(new File("."));
  }
  protected JToolBar createToolBar() {
    JToolBar toolbar = new JToolBar();

    toolbar.add(new SmallButton(getConnectAction(), statusLabel, "Open Connection to board ..."));
    toolbar.add(new SmallButton(getDisconnectAction(), statusLabel, "Close Connection to board ..."));
    toolbar.add(new SmallButton(getUpdateAction(), statusLabel, "Update info ..."));
    toolbar.add(new SmallButton(getSaveAction(), statusLabel, "Save History to a file..."));

    toolbar.addSeparator();
    JLabel label = new JLabel("Crate: ");
    label.setForeground(Color.black);
    toolbar.add(label);
    toolbar.add(crateNameCB);

    toolbar.addSeparator();
    label = new JLabel("Slot: ");
    label.setForeground(Color.black);
    toolbar.add(label);
    toolbar.add(slotCB);

    toolbar.addSeparator(new Dimension(200,1));
    toolbar.add(new SmallButton(getAboutAction(), statusLabel, "About the application ..."));
    return toolbar;
  }
   /** 
    * Create menubar and add the menus and meun items within 
    */
   private JMenuBar createMenuBar() {
     JMenuBar menuBar = new JMenuBar();
     JMenuItem item;
     ImageIcon imIcon;

     JMenu mFile = new JMenu("File");
     mFile.setMnemonic('f');

     item =  mFile.add(getConnectAction()); 
     item.setMnemonic('c');
     item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK));
     mFile.add(item);

     item =  mFile.add(getDisconnectAction()); 
     item.setMnemonic('d');
     item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_MASK));
     mFile.add(item);

     item =  mFile.add(getUpdateAction()); 
     item.setMnemonic('u');
     item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, KeyEvent.CTRL_MASK));
     mFile.add(item);

     item =  mFile.add(getSaveAction());
     item.setMnemonic('s');
     item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK));
     mFile.add(item);

     item =  mFile.add(getExitAction());
     item.setMnemonic('x');
     item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_MASK));
     menuBar.add(mFile);

     /* Option menu */
     JMenu mOption = new JMenu("Options");
     mOption.setMnemonic('p');
     menuBar.add(mOption);

     JMenu mHelp = new JMenu("Help");
     mHelp.setMnemonic('h');

     item =  mHelp.add(getAboutAction());
     item.setMnemonic('b');
     item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.CTRL_MASK));
     mHelp.add(item);

     menuBar.add(mHelp);
     return menuBar;
  }
  
  // Subclass can override to use a different action
  protected Action getConnectAction()  {return connectAction;}
  protected Action getDisconnectAction() {return disconnectAction;}
  protected Action getUpdateAction() {return updateAction;}
  protected Action getSaveAction() {return saveAction;}
  protected Action getExitAction() {return exitAction;}
  protected Action getAboutAction()  {return aboutAction;}

  public void showHelp() {
     StringBuffer mess = new StringBuffer();
     mess.insert(0,"<P>Spy Control Board a la svtdaq uses the new svtvme functions ");
     mess.append("and Fision calls directly from the workstation using JNI over ");
     mess.append("svtvme. From the combobox a crate can be selected and connection ");
     mess.append("established, one at a time. Disconnect the connected crate ");
     mess.append("before connecting another one. If no board is connected and any ");
     mess.append("read or write button is pressed, connection is made to the default ");
     mess.append("crate.</P>\n\n");
     mess.append("<P>The GUI is taken almost as it is from Thomas' svtdaq, but for ");
     mess.append("simplicity converted into a standalone application. A history ");
     mess.append("area similar to that in svtdaq is added which stores all the ");
     mess.append("important actions and output messages. From the menu the content ");
     mess.append("of the history area can be saved onto a file.</P>\n\n");
     mess.append("<P>Subir Sarkar</P>\n");
     mess.append("<P>University of Rome</P>\n");
  
     AboutDialog dialog = new AboutDialog(this, mess.toString(), "About Spy Control Board");
     dialog.show();
  }
  /** Save content of message area in a file */
  protected void saveHistory() {
    Spy.this.repaint();
    if (fileChooser.showSaveDialog(Spy.this) != 
           JFileChooser.APPROVE_OPTION) return;
    Thread runner = new Thread() {
      public void run() {
         File fChoosen = fileChooser.getSelectedFile();
         try  {
           FileWriter out = new FileWriter(fChoosen);
           getTextPane().write(out);
           out.close();
         } catch (IOException ex) {
           ex.printStackTrace();
         }
      }
    };
    runner.start();
  }

  /** Quit the application on confirmation */
  private void confirmQuit() {
    int error = 0;    
    int confirm = JOptionPane.showOptionDialog(this,
                  "Really Exit?", 
                  "Exit Confirmation",
                  JOptionPane.YES_NO_OPTION, 
                  JOptionPane.QUESTION_MESSAGE,
                  null, null, null);
    if (confirm == 0) {
      closeBoard();
      dispose();          
      System.exit(0);
    }
  }
  public void assertBoard() {
    if (svtB == null) {
      History.addText(getTextPane(),"Board not open!! ", Color.red);
      connect(true);
    }
  }
  /**
   *  ActionListener method for the read button
   */
  public void readJumperStatus() {
    assertBoard();
    IntHolder state = new IntHolder();
    int error = svtB.getState(SC_JUMPER_MASTER, state);
    master    = (state.value == 1);
    error     = svtB.getState(SC_JUMPER_LAST, state);
    daisy     = (state.value == 1);
   
    masterSB.setSelected(master);
    daisySB.setSelected(daisy);
  }

  /**
   * Returns the status Panel
   */
  public JPanel getStatusPanel () {
    return statusP;
  }

  /**
   * Update display, connection is made automatically if the Spy Control
   * sitting on the crate displayed is not opened.
   * Each individual Pane handles its own update calls
   */
  public void update() {
    if (svtB == null) connect(true);
    readJumperStatus();
    spySlaveStatPane.update();
    spySlaveGenPane.update();
    spySlaveErrPane.update();
    spyMasterPane.update();
    statusLabel.setText(Tools.getTimeString());
  }

  /** 
   * Open board if it is not already opened
   */
  public void connect(boolean connect) {
    if (connect) {
      closeBoard();
      getInput();
      svtB = new Board(crate, slot, SC);
      if (svtB == null) {
        History.addText(getTextPane(),
                       "connect(): Fails to open board in crate " + crate + 
                       " Slot " + slot, Color.red);
        tabbedPane.setSelectedIndex(4);
      }
      else {
        History.addText(getTextPane(),
                       "connect(): Spy Control board opened in crate " + crate + 
                       " Slot " + slot, Color.green);
      }
    }
    else {
      closeBoard();
    }
  }
  private void closeBoard() {
    if (svtB == null) return;
    int error = svtB.closeBoard();
    if (error > 0) {
      History.addText(getTextPane(), " Error closing Board handle ...", Color.red);
    }
    else {
      History.addText(getTextPane(), " Board handle closed ...", Color.green);
      svtB = null;
    }
  }
    /* Getters */
  public SpyHistoryPane getHistoryPane() {
    return spyHistoryPane;
  }
  public SpyMasterPane getMasterPane() {
    return spyMasterPane;
  }
  public SpySlaveStatPane getSlaveStatPane() {
    return spySlaveStatPane;
  }
  public SpySlaveErrPane getSlaveErrPane() {
    return spySlaveErrPane;
  }
  public SpySlaveGenPane getSlaveGenPane() {
    return spySlaveGenPane;
  }
  public JTextPane getTextPane() {
    return spyHistoryPane.getTextPane();
  }
  public Board getBoard() {
    return svtB;
  }
  public int getSlot() {
    return slot;
  }
  public String getCrate() {
    return crate;
  }

  class ConnectAction extends AbstractAction {
    public ConnectAction() {
      super("Connect Board", new ImageIcon(iconDir+"mini-connect.gif"));
    }
    public void actionPerformed(ActionEvent e) {
      connect(true);
    }
  }
  class DisconnectAction extends AbstractAction {
    public DisconnectAction() {
      super("Disconnect Board", new ImageIcon(iconDir+"mini-disconnect.gif"));
    }
    public void actionPerformed(ActionEvent e) {
      connect(false);
    }
  }
  class UpdateAction extends AbstractAction {
    public UpdateAction() {
      super("Update", new ImageIcon(iconDir+"mini-turn.gif"));
    }
    public void actionPerformed(ActionEvent e) {
      update();
    }
  }
  class SaveAction extends AbstractAction {
    public SaveAction() {
      super("Save History", new ImageIcon(iconDir+"file_save.gif"));
    }
    public void actionPerformed(ActionEvent e) {
      saveHistory();
    }
  }
  class ExitAction extends AbstractAction {
    public ExitAction() {
      super("Exit", new ImageIcon(iconDir+"mini-cross.gif"));
    }
    public void actionPerformed(ActionEvent e) {
      confirmQuit();
    }
  }
  class AboutAction extends AbstractAction {
    public AboutAction() {
      super("About", new ImageIcon(iconDir+"about-mini.gif"));
    }
    public void actionPerformed(ActionEvent e) {
      showHelp();
    }
  }

  public static void main(String [] argv) {
    if (argv.length < 1 || argv[0].equals("-h") || argv[0].equals("-help")) {
      System.out.println("Usage: java svt.Spy <slot_no> ");
      System.exit(0);
    }
    JFrame frame = new Spy("Spy Control Board", Integer.parseInt(argv[0]));
    frame.pack();
    frame.setVisible(true);
  } 
}
