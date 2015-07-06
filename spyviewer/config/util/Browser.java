package config.util;

import java.io.*;
import java.util.*;
import java.net.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.border.*;
import javax.swing.text.html.*;

public class Browser extends JFrame  {
  protected JEditorPane   m_browser;
  protected MemComboBox   m_locator;
  protected AnimatedLabel m_runner;
  protected boolean standAlone;
  protected StatusBar statusBar;
  protected JFileChooser fileChooser;

  private Action prevAction   = new PrevAction();
  private Action nextAction   = new NextAction();
  private Action homeAction   = new HomeAction();
  private Action reloadAction = new ReloadAction();
  private Action findAction   = new FindAction();

  private Action newAction    = new NewAction();
  private Action openAction   = new OpenAction();
  private Action saveAction   = new SaveAction();
  private Action printAction  = new PrintAction();
  private Action closeAction  = new CloseAction();
  private Action exitAction   = new ExitAction();

  protected JCheckBoxMenuItem viewToolBar;
  protected JCheckBoxMenuItem viewStatusBar;

  private Action prefAction   = new PrefAction();

  private Action aboutAction  = new AboutAction();

  public Browser(String url, boolean standAlone) {
    super("API Level Doc Browser");
    this.standAlone = standAlone;

    Container content = getContentPane();

    content.setLayout(new BorderLayout());

    statusBar =  new StatusBar();
    content.add(statusBar, BorderLayout.SOUTH);

    setJMenuBar(createMenuBar());

    JPanel pnew = new JPanel(new BorderLayout());
    pnew.add(createToolBar(), BorderLayout.NORTH);

    JPanel p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    JLabel addressLabel = new JLabel("Location:");
    addressLabel.setForeground(Color.black);
    p.add(addressLabel);
    p.add(Box.createRigidArea(new Dimension(10, 1)));

    m_locator = new MemComboBox();
    m_locator.setPreferredSize(new Dimension(400,20));
    m_locator.load("addresses.dat");
    m_locator.addItem(url);
    m_locator.setSelectedItem(url);

    BrowserListener lst = new BrowserListener();
    m_locator.addActionListener(lst);

    MemComboAgent agent = new MemComboAgent(m_locator);

    p.add(m_locator);
    p.add(Box.createRigidArea(new Dimension(10, 1)));

    m_runner = new AnimatedLabel("clock", 8);
    p.add(m_runner);
    pnew.add(p, BorderLayout.SOUTH);

    content.add(pnew, BorderLayout.NORTH);

    m_browser = new JEditorPane();
    m_browser.setEditable(false);
    m_browser.addHyperlinkListener(lst);

    JScrollPane sp = new JScrollPane();
    sp.getViewport().add(m_browser);
    getContentPane().add(sp, BorderLayout.CENTER);

    fileChooser = new JFileChooser();
    fileChooser.setCurrentDirectory(new File("."));

    WindowListener wndCloser = new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        m_locator.save("addresses.dat");
        closeWindow();
      }
    };
    addWindowListener(wndCloser);
    try {
      m_browser.setPage(url);
    }
    catch (IOException ex) {
      JOptionPane.showMessageDialog (Browser.this, 
                                    "Invalid URL", 
                                    "Invalid Input",
                                    JOptionPane.ERROR_MESSAGE);
    }
    m_locator.grabFocus();
  }
  protected Action getPrevAction() {return prevAction;}
  protected Action getNextAction() {return nextAction;}
  protected Action getHomeAction() {return homeAction;}
  protected Action getReloadAction() {return reloadAction;}
  protected Action getFindAction() {return findAction;}

  protected Action getNewAction()   {return newAction;}
  protected Action getOpenAction()  {return openAction;}
  protected Action getSaveAction()  {return saveAction;}
  protected Action getPrintAction() {return printAction;}
  protected Action getCloseAction() {return closeAction;}
  protected Action getExitAction()  {return exitAction;}

  protected JCheckBoxMenuItem toolbarCB() {return viewToolBar;}
  protected JCheckBoxMenuItem statusCB() {return viewStatusBar;}
  protected Action getPrefAction() {return prefAction;}

  protected Action getAboutAction() {return aboutAction;}

  // Create a simple JToolBar with some buttons
  protected JToolBar createToolBar() {
    JToolBar toolbar = new JToolBar();

    toolbar.add(new SmallButton(getPrevAction(), statusBar, "Go to the previous Page"));
    toolbar.add(new SmallButton(getNextAction(), statusBar,  "Go to the next Page"));
    toolbar.add(new SmallButton(getHomeAction(), statusBar, "Go to the Default Page"));

    toolbar.addSeparator();

    toolbar.add(new SmallButton(getReloadAction(), statusBar, "Reload the Page"));
    toolbar.add(new SmallButton(getFindAction(), statusBar, "Find text in Page"));
    return toolbar;
  }

  // Create a JMenuBar with file & other menus
  protected JMenuBar createMenuBar() {
    final JMenuBar menubar = new JMenuBar();
    JMenu file = new JMenu("File");
    file.setMnemonic('f');

    JMenuItem item = file.add(getNewAction());
    new MenuHelpTextAdapter(item, "Open a new Browser", statusBar);
    item.setMnemonic('n');
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_MASK));
    file.add(item);

    item = file.add(getOpenAction());
    new MenuHelpTextAdapter(item, "Open a page within the browser(file:/ or http://)", statusBar);
    item.setMnemonic('o');
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK));
    file.add(item);

    item = file.add(getSaveAction());
    new MenuHelpTextAdapter(item, "Save page as ...", statusBar);
    item.setMnemonic('S');
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_MASK));
    file.add(item);

    file.addSeparator();

    item  = file.add(getPrintAction());
    new MenuHelpTextAdapter(item, "Send the page to the printer", statusBar);
    item.setMnemonic('p');
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_MASK));
    file.add(item);

    file.addSeparator();

    item  = file.add(getCloseAction());
    new MenuHelpTextAdapter(item, "Close the current browser window", statusBar);
    item.setMnemonic('c');
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_MASK));
    file.add(item);

    item   = file.add(getExitAction());
    new MenuHelpTextAdapter(item, "Exit the Browser", statusBar);
    item.setMnemonic('x');
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_MASK));
    file.add(item);

    JMenu view = new JMenu("View");
    view.setMnemonic('v');

    item = view.add(getReloadAction());
    new MenuHelpTextAdapter(item, "Reload the page", statusBar);
    item.setMnemonic('r');
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_MASK));
    view.add(item);

    item = view.add(getFindAction());
    new MenuHelpTextAdapter(item, "Find text in page", statusBar);
    item.setMnemonic('i');
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.CTRL_MASK));
    view.add(item);

    view.addSeparator();

    viewToolBar = new JCheckBoxMenuItem("Tool Bar");
    new MenuHelpTextAdapter(viewToolBar, "Show/Hide Tool Bar", statusBar);
    view.add(viewToolBar);

    viewStatusBar = new JCheckBoxMenuItem("Status Bar");
    new MenuHelpTextAdapter(viewStatusBar, "Show/Hide Status Bar", statusBar);
    view.add(viewStatusBar);

    item = view.add(getPrefAction());
    new MenuHelpTextAdapter(item, "View/Edit Preferences", statusBar);
    item.setMnemonic('p');
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_MASK));
    view.add(item);

    JMenu go = new JMenu("Go");
    go.setMnemonic('g');

    item = go.add(getPrevAction());
    new MenuHelpTextAdapter(item, "Go to the Previous Page", statusBar);
    item.setMnemonic('b');
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_MASK));
    go.add(item);

    item = go.add(getNextAction());
    new MenuHelpTextAdapter(item, "Go to the Next Page", statusBar);
    item.setMnemonic('f');
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_MASK));
    go.add(item);

    item = go.add(getHomeAction());
    new MenuHelpTextAdapter(item, "Go to the Default Page", statusBar);
    item.setMnemonic('h');
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.CTRL_MASK));
    go.add(item);

    JMenu help = new JMenu("Help");
    help.setMnemonic('h');

    item = help.add(getAboutAction());
    new MenuHelpTextAdapter(item, "About this widget", statusBar);
    item.setMnemonic('a');
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_MASK));
    help.add(item);

    menubar.add(file);
    menubar.add(view);
    menubar.add(go);
    menubar.add(help);

    return menubar;
  }
  public void closeWindow() {
    if (standAlone) System.exit(0);
    else {
       dispose();
       setVisible(false);
    }
  }
  // "About" action
  class AboutAction extends AbstractAction {
    public AboutAction() { 
       super("About"); 
    }
    public void actionPerformed(ActionEvent e) {
      showHelp();
    }
  }
  public void showHelp() {
    StringBuilder message = new StringBuilder(AppConstants.SMALL_BUFFER_SIZE);
    message.insert(0, "<P>A simple Html document browser suited for viewing simple \n");
    message.append("text, in particular source level or formatted help pages.\n\n");
    message.append("Adapted from \"Swing\" by Matthew Robinson and Pavel Vorobiev.\n\n");
    message.append("<P>Subir Sarkar<P>");

    AboutDialog dialog = new AboutDialog(Browser.this, message.toString(), 
      "A Simple HTML Help Browser");
    dialog.show();
  }
  // "Preference" action
  class PrefAction extends AbstractAction {
    public PrefAction() { 
      super("Preferences"); 
    }
    public void actionPerformed(ActionEvent e) {
    }
  }
  // "New" action
  class NewAction extends AbstractAction {
    public NewAction() { 
      super("New"); 
    }
    public void actionPerformed(ActionEvent e) {
    }
  }
  // "Close" action
  class CloseAction extends AbstractAction {
    public CloseAction() { 
      super("Close"); 
    }
    public void actionPerformed(ActionEvent e) {
    }
  }
  // "Print" action
  class PrintAction extends AbstractAction {
    public PrintAction() { 
      super("Print", new ImageIcon(AppConstants.iconDir+"printer.gif")); 
    }
    public void actionPerformed(ActionEvent e) {
    }
  }
  // A very simple "quit" action
  class ExitAction extends AbstractAction {
    public ExitAction() { super("Exit"); }
    public void actionPerformed(ActionEvent ev) { 
      closeWindow(); 
    }
  }

  // An action that opens an existing file
  class OpenAction extends AbstractAction {
    public OpenAction() { 
      super("Open", new ImageIcon(AppConstants.iconDir+"open.gif")); 
    }
    // Use JFilechooser to get filename and attempt to open and read the file into the
    // text component
    public void actionPerformed(ActionEvent e)   {
    }
  }
  // An action that saves the document to a file
  class SaveAction extends AbstractAction {
    public SaveAction() {
      super("Save as ...");
    }

    // Query user for a filename and attempt to open and write the text
    // component's content to the file
    public void actionPerformed(ActionEvent e)    {
       Browser.this.repaint();
       if (fileChooser.showSaveDialog(Browser.this) != 
           JFileChooser.APPROVE_OPTION)  return;
       Thread runner = new Thread() {
          public void run() {
            FileWriter writer = null;
            File fName = fileChooser.getSelectedFile();
            try   {
               writer = new FileWriter(fName);
               m_browser.write(writer);
            } 
            catch (IOException ex)  {
               JOptionPane.showMessageDialog(Browser.this,
               "File Not Saved", "ERROR", JOptionPane.ERROR_MESSAGE);
               ex.printStackTrace();
            }
            finally {
              if (writer != null) {
                try {
                   writer.close();
                } catch (IOException x) {}
              }
            }
         }
      };
      runner.start();
    }
  }
  // "Go Back" action
  class PrevAction extends AbstractAction {
    public PrevAction() { 
       super("Back",new ImageIcon(AppConstants.iconDir+"back.gif")); 
    }
    public void actionPerformed(ActionEvent e) {
    }
  }
  // "Go Forward" action
  class NextAction extends AbstractAction {
    public NextAction() { 
       super("Forward",new ImageIcon(AppConstants.iconDir+"forward.gif")); 
    }
    public void actionPerformed(ActionEvent e) {
    }
  }
  // "Go Home" action
  class HomeAction extends AbstractAction {
    public HomeAction() { 
       super("Home", new ImageIcon(AppConstants.iconDir+"home.gif")); 
    }
    public void actionPerformed(ActionEvent e) {
    }
  }
  // "Reload Current Page" action
  class ReloadAction extends AbstractAction {
    public ReloadAction() { 
       super("Reload", new ImageIcon(AppConstants.iconDir+"reload.gif")); 
    }
    public void actionPerformed(ActionEvent e) {
    }
  }
  // "Find" action
  class FindAction extends AbstractAction {
    public FindAction() { 
       super("Find",new ImageIcon(AppConstants.iconDir+"find.gif")); 
    }
    public void actionPerformed(ActionEvent e) {
    }
  }
  class BrowserListener implements ActionListener, HyperlinkListener {
    public void actionPerformed (ActionEvent evt) {
      String sUrl = (String) m_locator.getSelectedItem();
      if (sUrl == null || 
          sUrl.length() == 0 || 
  	  m_runner.getRunning()) return;
   
      BrowserLoader loader = new BrowserLoader(sUrl);
      loader.start();
    }
   
    public void hyperlinkUpdate(final HyperlinkEvent e)	{
      if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
        URL url = e.getURL();
        if (url == null || m_runner.getRunning()) return;
        BrowserLoader loader = new BrowserLoader(url.toString());
        loader.start();
      }
      else if (e.getEventType() == HyperlinkEvent.EventType.ENTERED) {
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      }
      else if (e.getEventType() == HyperlinkEvent.EventType.EXITED) {
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      }
    }
  }
  class BrowserLoader extends Thread {
    protected String m_sUrl;
    public BrowserLoader(String sUrl) {
      m_sUrl = sUrl;
      if (!m_sUrl.endsWith("html")) {
        File tmpFile = null;
        try {
          tmpFile = File.createTempFile("Browser-tmp", ".html");
        }
        catch (IOException e) {
        }

        StringTokenizer st = new StringTokenizer(m_sUrl, ":");
        String filename = null;
        while (st.hasMoreTokens()) {
          filename = st.nextToken();
        }
        File file = new File(filename);

        StringBuilder buffer = new StringBuilder(AppConstants.SMALL_BUFFER_SIZE);
        buffer.append("<HTML>\n");
        buffer.append("<HEAD>m_sUrl</HEAD>\n");
        buffer.append("<BODY>\n");

        if (file.isDirectory()) {
          File [] files = file.listFiles();
          for (int i = 0; i < files.length; i++) {
            if ((files[i].isFile() || files[i].isDirectory()) && !(files[i].isHidden())) { 
              buffer.append("<A HREF="+files[i].getAbsolutePath()+">"+files[i].getAbsolutePath()+"</A><BR>\n"); 
            }
          }
        }
        else if (file.isFile()) {
          buffer.append("<HR>\n");
          buffer.append("Displaying "+m_sUrl+"\n");
          buffer.append("<P><IMG SRC="+m_sUrl+"></P>\n");
          buffer.append("<HR>\n");
        }
        buffer.append("</BODY>\n");
        buffer.append("</HTML>\n");
        Tools.writeFile(tmpFile.getName(), buffer.toString());
        m_sUrl = "file:"+tmpFile.getName();
      }
    }
   
    public void run() {
      setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      m_runner.setRunning(true);
   
      try {
        URL source = new URL(m_sUrl);
  	m_browser.setPage(source);
  	m_locator.add(m_sUrl);
      }
      catch (Exception e) {
  	JOptionPane.showMessageDialog(Browser.this, 
  		  "Error: "+e.toString(),
  		  "Warning", JOptionPane.WARNING_MESSAGE);
      }
   
      m_runner.setRunning(false);
      setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
  }
   
  public static void main(String argv[]) {
    JFrame f = 
      new Browser("file:"+Tools.getEnv("SVTMON_DIR")+"/help/a_InfoFrame.html", true);
    f.setSize(600,650);
    f.setVisible(true);
  }
}

class MemComboAgent extends KeyAdapter {
  protected JComboBox   m_comboBox;
  protected JTextField  m_editor;

  public MemComboAgent(JComboBox comboBox) {
    m_comboBox = comboBox;
    m_editor = (JTextField)comboBox.getEditor().getEditorComponent();
    m_editor.addKeyListener(this);
  }

  public void keyReleased(KeyEvent e)	{
    char ch = e.getKeyChar();
    if (ch == KeyEvent.CHAR_UNDEFINED || Character.isISOControl(ch))
	return;
    int pos = m_editor.getCaretPosition();
    String str = m_editor.getText();
    if (str.length() == 0) return;

    for (int k = 0; k < m_comboBox.getItemCount(); k++) {
      String item = m_comboBox.getItemAt(k).toString();
      if (item.startsWith(str))	{
	 m_editor.setText(item);
	 m_editor.setCaretPosition(item.length());
	 m_editor.moveCaretPosition(pos);
	 break;
      }
    }
  }
}

class MemComboBox extends JComboBox {
  public static final int MAX_MEM_LEN = 30;

  public MemComboBox()	{
    super();
    setEditable(true);
  }

  public void add(String item)	{
    removeItem(item);
    insertItemAt(item, 0);
    setSelectedItem(item);
    if (getItemCount() > MAX_MEM_LEN)
       removeItemAt(getItemCount()-1);
  }

  public void load(String fName) {
    try	{
      if (getItemCount() > 0)  removeAllItems();
      File f = new File(fName);
      if (!f.exists())	return;
      FileInputStream fStream =  new FileInputStream(f);
      ObjectInput stream  =  new ObjectInputStream(fStream);

      Object obj = stream.readObject();
      if (obj instanceof ComboBoxModel)	setModel((ComboBoxModel)obj);

      stream.close();
      fStream.close();
    }
    catch (Exception e) {
      System.err.println("Serialization error: "+e.toString());
    }
 }

 public void save(String fName) {
   try	{
     FileOutputStream fStream = new FileOutputStream(fName);
     ObjectOutput  stream     =  new  ObjectOutputStream(fStream);

     stream.writeObject(getModel());

     stream.flush();
     fStream.close();
     stream.close();
   }
   catch (Exception e) {
     System.err.println("Serialization error: "+e.toString());
   }
 }
}

class AnimatedLabel extends JLabel implements Runnable {
  protected Icon[] m_icons;
  protected int m_index = 0;
  protected boolean m_isRunning;

  public AnimatedLabel(String gifName, int numGifs) {
    m_icons = new Icon[numGifs];
    for (int k = 0; k < numGifs; k++)
      m_icons[k] = 
      new ImageIcon(Tools.getEnv("SVTMON_DIR")+"/src/config/icons/"+gifName+k+".gif");
    setIcon(m_icons[0]);

    Thread tr = new Thread(this);
    tr.setPriority(Thread.MAX_PRIORITY);
    tr.start();
  }
  public void setRunning(boolean isRunning) {
    m_isRunning = isRunning;
  }

  public boolean getRunning()	{
    return m_isRunning;
  }

  public void run() {
    while (true) {
       if (m_isRunning)	{
         m_index++;
         if (m_index >= m_icons.length)	m_index = 0;
	 setIcon(m_icons[m_index]);
  	 Graphics g = getGraphics();
 	 m_icons[m_index].paintIcon(this, g, 0, 0);
       }
       else {
         if (m_index > 0) {
           m_index = 0;
           setIcon(m_icons[0]);
         }
       }
       try {
         Thread.sleep(1000); 
       } catch(Exception ex) {}
    }
  }
}
