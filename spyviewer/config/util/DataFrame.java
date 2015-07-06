package config.util;

import java.io.File;
import java.util.Vector;
import java.util.Iterator;

import java.awt.Dimension;
import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.RenderingHints;

import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.ButtonGroup;
import javax.swing.JToolBar;
import javax.swing.JPanel;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.BorderFactory;
import javax.swing.WindowConstants;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import javax.swing.JToolBar.Separator;
import javax.swing.border.Border;

/*
 * @author   S. Sarkar
 * @version  0.1, July 2000
 */

public class DataFrame extends JFrame {
  private static boolean DEBUG = false;
  private static int nWindows  = 0;
  private static final int MAX_WINDOWS = 20;
  private static final Icon lconIcon   = new ImageIcon(AppConstants.iconDir+"connect_large.png");
  private static final Icon sconIcon   = new ImageIcon(AppConstants.iconDir+"connect_small.png");
  private static final Icon ldconIcon  = new ImageIcon(AppConstants.iconDir+"disconnect_large.png");
  private static final Icon sdconIcon  = new ImageIcon(AppConstants.iconDir+"disconnect_small.png");
  private static final Icon laboutIcon = new ImageIcon(AppConstants.iconDir+"help_large.png"); 
  private static final Icon saboutIcon = new ImageIcon(AppConstants.iconDir+"help_small.png"); 
  private static final Icon lsaveasIcon = new ImageIcon(AppConstants.iconDir+"saveas_large.png"); 
  private static final Icon ssaveasIcon = new ImageIcon(AppConstants.iconDir+"saveas_small.png"); 
  private JCheckBoxMenuItem debugCB = 
          new JCheckBoxMenuItem("Debug on/off", false);
  private JRadioButtonMenuItem rbMenuItem;
  private MenuListener menuListener;
  private Vector<Component> toolList = new Vector<Component>(10); 
  private boolean standAlone;
  private boolean showHelp;
  private boolean hasTextPanel;
  private int borderType;

  private TextPanel textPanel = null;
  private StatusBar statusBar;
  private JToolBar toolBar;
  private String helpString;
  private String helpFile;
  private String aboutApp;
  private Dimension dialogSize;
  private boolean isFile;
  private boolean needsConnection;

  private SmallButton helpButton = null;
  private static final String connStr = new String("Connect to RT Server ..."),
                              dconStr = new String("Disconnect from RT Server ...");

  private JMenuItem connMenuItem, dconMenuItem;
  private SmallButton connButton, dconButton;

  public DataFrame(boolean standAlone, final String label, boolean build) {
    this(standAlone, label, true, true, -1, false);
    if (build) buildDefaultGUI();
  }
  public DataFrame(boolean standAlone, final String label, 
                   boolean showHelp, boolean hasTextPanel, 
                   int borderType) 
  {
    this(standAlone, label, showHelp, hasTextPanel, borderType, true);
  }
  public DataFrame(boolean standAlone, final String label, 
                   boolean showHelp, boolean hasTextPanel, 
                   int borderType, boolean needsConnection) 
  {
    super(label);
    this.standAlone      = standAlone;
    this.showHelp        = showHelp;
    this.hasTextPanel    = hasTextPanel;
    this.borderType      = borderType;
    this.needsConnection = needsConnection;
    createComponents();
  }
  private void createComponents()  {
    if (nWindows >= MAX_WINDOWS) {
      String [] message = {
	  "Too many windows open at a time! > " + MAX_WINDOWS,
          "Please close the ones not needed",
          "Anyway, click Ok to continue"
      };
      JOptionPane.showMessageDialog(DataFrame.this,
          message, "Alarm", JOptionPane.WARNING_MESSAGE);
    }
    if (hasTextPanel) textPanel = new TextPanel(new Dimension(600, 50));

    // Always create before menu and tool bars because 
    // statusbar is used by them
    statusBar = new StatusBar(); 
    statusBar.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));

    toolBar = createToolBar();
    getContentPane().add(toolBar, BorderLayout.PAGE_START);

    setJMenuBar(createMenuBar());
    if (standAlone && needsConnection) setEnabledConnButtons(true);
   
    addOptionMenu(getJMenuBar());
    if (showHelp) addHelpMenu();
    getJMenuBar().setFont(AppConstants.gFont);

    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE); 
    WindowListener wndListener = new WindowAdapter() {
      public void windowOpened(WindowEvent e) {
        nWindows++;
        if (debugCB.isSelected()) 
           System.out.println("DataFrame -> Window # " + nWindows);
      }
      public void windowClosing(WindowEvent e) {
        closeFrame();
      }
      public void windowClosed(WindowEvent e) {
        nWindows--;    
        if (debugCB.isSelected()) 
          System.out.println("DataFrame -> # of open Windows " + nWindows);
      }
    };
    addWindowListener(wndListener);

    // Fallback help file
    setHelpFile(Tools.getEnv("SVTMON_DIR")+"/help/a_DefaultHelp.html",
                                  "About Data Viewer", new Dimension(400, 250)); 
  }
  /** Prepare UI */
  private void buildDefaultGUI() {
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE); 
    Container content = getContentPane();
    content.add(toolBar,   BorderLayout.PAGE_START);
    content.add(textPanel, BorderLayout.CENTER);
    content.add(statusBar, BorderLayout.SOUTH);
  }
  /** Create a JMenuBar with file, option and help menus */
  private JMenuBar createMenuBar() {
    final JMenuBar menubar = new JMenuBar();
    menubar.setBackground(new Color(240, 240, 240));
    menubar.setBorder(BorderFactory.createLineBorder(Color.black));

    JMenu file = new JMenu("File");
    file.setMnemonic(KeyEvent.VK_F);

    JMenuItem item = file.add(new NewAction());
    item.setMnemonic(KeyEvent.VK_N);
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_MASK));
    //    item.setEnabled(false);
    file.add(item);

    item = file.add(new OpenAction(AppConstants.openIcon));
    item.setMnemonic(KeyEvent.VK_O);
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK));
    item.setEnabled(false);
    file.add(item);

    // It will certainly be a much better design to have two different classes
    // For online and offline 
    if (standAlone && needsConnection) {
      file.addSeparator();
  
      connMenuItem = file.add(new ConnectAction(sconIcon)); 
      connMenuItem.setMnemonic(KeyEvent.VK_C);
      connMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK));
      file.add(connMenuItem);
  
      dconMenuItem = file.add(new DisconnectAction(sdconIcon)); 
      dconMenuItem.setMnemonic(KeyEvent.VK_D);
      dconMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_MASK));
      file.add(dconMenuItem);
  
      file.addSeparator();
    }

    item = file.add(new SaveAsAction(ssaveasIcon));
    item.setMnemonic(KeyEvent.VK_S);
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK));
    if (!hasTextPanel) item.setEnabled(false);
    file.add(item);

    file.addSeparator();

    item  = file.add(new CloseAction());
    item.setMnemonic(KeyEvent.VK_W);
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_MASK));
    item.setDisplayedMnemonicIndex(0);
    file.add(item);

    item  = file.add(new ExitAction());
    item.setMnemonic(KeyEvent.VK_Q);
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_MASK));
    item.setDisplayedMnemonicIndex(1);
    if (!standAlone) item.setEnabled(false);
    file.add(item);

    menubar.add(file);

    return menubar;
  }
   /** 
    * Create menubar and add the menus and meun items within 
    */
  private void addOptionMenu(JMenuBar menuBar) {
    JMenuItem item;
  
    /* Option menu */
    JMenu menu = new JMenu("Options");
    menu.setMnemonic('o');
    menuBar.add(menu);
  
    JMenu lafMenu = new JMenu("Choose LaF");
    menu.add(lafMenu);
  
    // A group of radio button menu items
    ButtonGroup group = new ButtonGroup();
    int mnemonics [] = 
      {
	KeyEvent.VK_J, 
        KeyEvent.VK_C,
        KeyEvent.VK_S,
        KeyEvent.VK_G,
        KeyEvent.VK_M
      };
    menuListener = new MenuListener(DataFrame.this);
    for (int i = 0; i < Tools.lafNames.length; i++) {
      rbMenuItem = new JRadioButtonMenuItem(Tools.lafNames[i]);
      rbMenuItem.setMnemonic(mnemonics[i]);
      if (i == 0) rbMenuItem.setSelected(true);
      rbMenuItem.addActionListener(menuListener);
      group.add(rbMenuItem);
      lafMenu.add(rbMenuItem);
    }

    menu.add(debugCB);
  }
  /** An ActionListener that listens to the radio buttons. */
  class MenuListener implements ActionListener {
    JFrame frame;
    MenuListener(JFrame frame) {
      this.frame = frame;
    }
    public void actionPerformed(ActionEvent e) {
      JMenuItem comp = (JMenuItem) e.getSource();
      String name = comp.getText();
      try {
        Tools.setLookAndFeel(frame, name);
      }
      catch (Exception ex) {
      }
    }
  }
  public int getBorderType() {
    return borderType;
  }
  public void addStatusBar() {
    getContentPane().add(statusBar, BorderLayout.PAGE_END);
  }
  public void addStatusBar(JPanel panel, int layout) {
    panel.add(statusBar, layout);
  }
  public void setStatusBorder(Border border) {
    statusBar.setBorder(border);
  }
  public boolean isDebugOn() {
    return debugCB.isSelected();
  }
  public void setDebugOn(boolean debugFlag) {
    debugCB.setSelected(debugFlag);
  }
  /** Create a JMenuBar with file, option and help menus */
  protected void addHelpMenu() {
    JMenuBar menuBar = getJMenuBar();
    menuBar.add(Box.createHorizontalGlue());

    JMenu help = new JMenu("Help");
    help.setMnemonic('h');

    JMenuItem item = help.add(new AboutAction(saboutIcon));
    item.setMnemonic('a');
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, 
           KeyEvent.CTRL_MASK));
    help.add(item);

    menuBar.add(help);
  }
  protected void setEnabledConnButtons(boolean enabled) {
    connMenuItem.setEnabled(enabled);
    connButton.setEnabled(enabled);
    
    dconMenuItem.setEnabled(!enabled);
    dconButton.setEnabled(!enabled);
  }
  private JToolBar createToolBar() {
    JToolBar toolbar = new JToolBar();
    //toolbar.setBorderPainted(false);
    toolbar.setBackground(new Color(240, 240, 240));
    // Put in the config widget
    toolbar.setFloatable(true);
    toolbar.setRollover(true);

    // Add simple actions for opening & saving
    JButton button = new SmallButton(new SaveAsAction(lsaveasIcon), statusBar,
				     "Save content of text area in a file ...", borderType);
    if (!hasTextPanel) button.setEnabled(false); 
    toolList.add(button);
    toolList.add(new JToolBar.Separator());    

    if (standAlone && needsConnection) {
      connButton = new SmallButton(new ConnectAction(lconIcon), statusBar, connStr, borderType);
      toolList.add(connButton);

      dconButton = new SmallButton(new DisconnectAction(ldconIcon), statusBar, dconStr, borderType);
      toolList.add(dconButton);
      toolList.add(new JToolBar.Separator());    
    }
    return toolbar;
  }
  public int getToolLength() {
    return toolList.size();
  }
  public void addToolBar() {
    toolBar.removeAll();
    for (Iterator<Component> it = toolList.iterator(); it.hasNext(); ) {
	toolBar.add(it.next());
    }
    addHelpInToolBar();
  }
  public void addToolSeparator() {
    toolList.add(new JToolBar.Separator());
  }
  public void addToolElement(Action action, String text, int borderType) {
    toolList.add(new SmallButton(action, statusBar, text, borderType));
  }
  public void addToolElement(Action action, String text, int borderType, int index) {
    toolList.add(index, new SmallButton(action, statusBar, text, borderType));
  }
  public void addToolElement(Component component) {
    toolList.add(component);
  }
  public void addToolElement(Component component, int index) {
    toolList.add(index, component);
  }
  protected void addHelpInToolBar() {
    toolBar.add(Box.createHorizontalGlue());  // Help should be the last element

    helpButton = new SmallButton(new AboutAction(laboutIcon),  statusBar, 
                          "About the Application ...", borderType);
    toolBar.add(helpButton);
  }
  protected JToolBar getToolBar() {
    return toolBar;
  }
  public StatusBar getStatusBar() {
    return statusBar;
  }
  protected TextPanel getTextPanel() {
    return textPanel;
  }
  public void setText(final String text) {
    textPanel.setText(text);
  }
  public void setStatusText(final String status) {
    statusBar.setText(status);
  }
  protected JFileChooser getFileChooser() {
    return textPanel.getFileChooser();
  }
  public void addText(final String text, Color color) {
    textPanel.addText(text, color);
  }
  public void displayText(final String text) {
    textPanel.displayText(text);
  }
  public void warn(final String text) {
    warn(text, Color.red);
  }
  public void warn(final String text, Color color) {
    textPanel.warn(text, color);
  }
  private void showAbout() {
    AboutDialog dialog;
    if (isFile && !helpFile.equals(" "))
      dialog = new AboutDialog(this, new File(helpFile), aboutApp);
    else 
      dialog = new AboutDialog(this, helpString, aboutApp);
    dialog.setSize(dialogSize);
    dialog.show();
  }
  protected void newWindow() {
     String [] message = {
	  "DataFrame.newWindow() ",
	  "May not clone this window by default ",
          "The daughter class should override this method",
          "to implement a clone properly"
     };
     JOptionPane.showMessageDialog(DataFrame.this,
          message, "Clone not allowed", JOptionPane.INFORMATION_MESSAGE);
  }
    /** Quit the application. Daughter class may override this to confirm 
     *  quit or perform any other suitable action */
  protected void exitApp() {
    dispose();
    System.exit(0);    
  }
  protected void closeFrame() {
    if (standAlone) {
      exitApp();
    }
    else {
      setVisible(false);
      dispose();
      // Memory is at premium, run garbage collector 
      Runtime.getRuntime().gc();
    }
  }
  public boolean isStandAlone() {
    return standAlone;
  }
  public boolean isOnline() {
    return isConnectionNeeded();
  }
  public boolean isConnectionNeeded() {
    return needsConnection;
  }
  public void setHelpString(final String help, final String about) {
    setHelpString(help, about, new Dimension(500, 400));
  }
  public void setHelpString(final String help, final String about, Dimension size) {
    helpString = help;
    aboutApp   = about;
    dialogSize = size;
    isFile     = false;
  }
  public void setHelpFile(final String filename, final String about) {
    setHelpFile(filename, about, new Dimension(500, 400));
  }
  public void setHelpFile(final String filename, final String about, Dimension size) {
    helpFile   = filename;
    aboutApp   = about;
    dialogSize = size;
    isFile = true;
  }

  /* 'Connect' action */
  class ConnectAction extends AbstractAction {
    public ConnectAction(Icon icon) { 
      super(DataFrame.connStr, icon); 
    }
    public void actionPerformed(ActionEvent e) {
      startMessageThread();
    }
  }
  /* 'Disconnect' action */
  class DisconnectAction extends AbstractAction {
    public DisconnectAction(Icon icon) { 
      super(DataFrame.dconStr, icon); 
    }
    public void actionPerformed(ActionEvent e) {
      stopMessageThread();
    }
  }

  /* 'New' action */
  class NewAction extends AbstractAction {
    public NewAction() { 
      super("New Window"); 
    }
    public void actionPerformed(ActionEvent e) {
      newWindow();
    }
  }
  /* 'Open' action */
  class OpenAction extends AbstractAction {
    public OpenAction(Icon icon) { 
      super("Open", icon); 
    }
    public void actionPerformed(ActionEvent e) {
    }
  }
  /* 'Close' action */
  class CloseAction extends AbstractAction {
    public CloseAction() { 
      super("Close", AppConstants.stopIcon); 
    }
    public void actionPerformed(ActionEvent e) {
      closeFrame();
    }
  }
  /* 'Exit' action */
  class ExitAction extends AbstractAction {
    public ExitAction() { 
      super("Exit"); 
    }
    public void actionPerformed(ActionEvent e) {
      exitApp();
    }
  }
  /* 'About' action */
  class AboutAction extends AbstractAction {
    public AboutAction(Icon icon) { 
      super("About", icon); 
    }
    public void actionPerformed(ActionEvent e) {
      showAbout();
    }
  }
  /* An action that saves the document to a file */
  class SaveAsAction extends AbstractAction {
    public SaveAsAction(Icon icon) {
      super("Save as ...", icon);
    }
    // Query user for a filename and attempt to open and write the text
    // component's content to the file
    public void actionPerformed(ActionEvent e)    {
      if (textPanel != null) textPanel.saveText();
    }
  }
  // The following 2 methods are mostly overridden/extended
  protected void startMessageThread() {
    if (isDebugOn()) warn("Start SmartSockets Message Thread ... ...", Color.green);
    if (isVisible()) {
        Runnable setLabelRun = new Runnable() {
          public void run() {
            try {
              setEnabledConnButtons(false);
            }
            catch (NullPointerException ex) { 
              System.out.println("Exception " + ex.getMessage());
              ex.printStackTrace();
            }
          }
        };
        SwingUtilities.invokeLater(setLabelRun);
    }
    else {
      setEnabledConnButtons(false);
    }
  }
  protected void stopMessageThread() {
    if (isDebugOn()) warn("Stop SmartSockets Message Thread ... ...", Color.green);
    Runnable setLabelRun = new Runnable() {
      public void run() {
        try {
          setEnabledConnButtons(true);
        }
        catch (NullPointerException ex) { 
          System.out.println("Exception " + ex.getMessage());
          ex.printStackTrace();
        }
      }
    };
    SwingUtilities.invokeLater(setLabelRun);
  }
  public static void main(String [] argv) {
    JFrame f = new DataFrame(true, "Data Frame", true);
    f.setSize(600,500);
    f.setVisible(true);
  }
}
