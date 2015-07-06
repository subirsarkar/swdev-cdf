package config.svt;

import java.io.File;
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;

import java.util.Vector;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Enumeration;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Container;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.Point;
import java.awt.Component;
import java.awt.Toolkit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JLabel;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JFileChooser;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JComboBox;
import javax.swing.JSeparator;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.ButtonGroup;
import javax.swing.BorderFactory;
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.KeyStroke;

import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;

import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import config.SvtDataManager;
import config.SvtCrateMap;

import config.util.DataFrame;
import config.util.AppConstants;
import config.util.Tools;
import config.util.DataFrame;
import config.util.SmallButton;
import config.util.TextPanel;
import config.util.FetchURL;
import config.util.ArchiveExtractor;
import config.util.TarExtractor;
import config.util.JarExtractor;
import config.util.ZipExtractor;
import config.util.Gunzip;
import config.util.FetchURL;
import config.util.MyTreeModelListener;
import config.util.SimpleTextPanel;
import config.util.AbstractTextPanel;
import config.util.PrintfFormat;
import config.util.StatusBar;
import config.util.FileTypeFilter;
import config.util.RemoteFileDialog;
import config.util.ErrorLoggerPanel;

/** 
 * <P>
 * Display the spy buffer informations for the selected spy buffer.
 * This window embed <CODE>InfoPanel</CODE> which contains most of the
 * informations.
 * 
 * @author S. Sarkar
 * @version 1.0,  8/2000
 * @version 1.2,  5/2006
 */
public class InfoFrame extends DataFrame {
  public final static int 
    TREEWIDTH  = 150,
    BUFWIDTH   = 850,
    TOTALWIDTH = TREEWIDTH+BUFWIDTH,
    XHEIGHT    = 520;

  private static final Dimension shortSize = new Dimension(100, 50);
  private boolean showEE = false;
  private static final String NOINFO = "No Info ";

  private static final Icon initIcon = AppConstants.grayBall;
  private static final Icon statIcon = new ImageIcon(AppConstants.iconDir+"viewstat_large.png");
  private static final Icon dataIcon = new ImageIcon(AppConstants.iconDir+"viewtext_large.png");
  private static final Icon sdataIcon = new ImageIcon(AppConstants.iconDir+"viewtext_small.png");
  private static final Icon histIcon = new ImageIcon(AppConstants.iconDir+"histogram_large.png");
  private static final Icon docuIcon = new ImageIcon(AppConstants.iconDir+"document2.png");

  private static final Icon saddIcon = new ImageIcon(AppConstants.iconDir+"add_small.png");
  private static final Icon laddIcon = new ImageIcon(AppConstants.iconDir+"add_large.png");
  private static final Icon ssubIcon = new ImageIcon(AppConstants.iconDir+"subtract_small.png");
  private static final Icon lsubIcon = new ImageIcon(AppConstants.iconDir+"subtract_large.png");
  private static final Icon sremIcon = new ImageIcon(AppConstants.iconDir+"remove_small.png");
  private static final Icon lremIcon = new ImageIcon(AppConstants.iconDir+"remove_large.png");
  private static final Icon swizIcon = new ImageIcon(AppConstants.iconDir+"wizard_small.png");
  private static final Icon lwizIcon = new ImageIcon(AppConstants.iconDir+"wizard_large.png");

  private HashMap<String, String> map = new HashMap<String, String>();
  private ComboPanel comboPanel;
  private ErrorLoggerPanel errlPanel; 
  private StatusPanel sbPanel; 

  /** The panel contained inside the frame */
  private InfoPanel infoPanel;  
  private JCheckBoxMenuItem autoUpdateCB;
  private JCheckBoxMenuItem onlineUseCB;

  JButton remButton;
  JMenuItem remMenuItem, remAllMenuItem;

  /** Construct the Frame which displays informations about a spy buffer
   *  @parent A label which specifies the name of the window
   */
  public InfoFrame(boolean standAlone, final String label) {
    this(standAlone, label, true);
  }
  public InfoFrame(boolean standAlone, final String label, boolean connect2RT) {
    // standalone/title/showHelp/hasTextPanel/border/needConnection
    super(standAlone, label, false, true, -1, connect2RT); 
    buildGUI();

    String filename = Tools.getEnv("SVTMON_DIR")+"/help/a_InfoFrame.html";
    setHelpFile(filename, "About Individual Spy Buffer", new Dimension(600, 550));
  }
  /** Construct the UI */
  protected void buildGUI() {
    Container content = getContentPane();

    // Menu
    updateOptionsMenu(getJMenuBar());
    addActionMenu(getJMenuBar());
    addHelpMenu();

    // Tool
    addToolSeparator();  // Does it work at all?
    JButton b = new SmallButton(new AddAction(laddIcon), getStatusBar(), "Add dump(s) ...", -1);
    addToolElement(b,getToolLength()-1);

    // Disabled to start with, should be enabled when the first buffer is added
    // When the last buffer is removed should be disabled again
    remButton = new SmallButton(new RemoveAction(lsubIcon), getStatusBar(), "Remove selected dump(s) ...", -1);
    remButton.setEnabled(false);
    addToolElement(remButton,getToolLength()-1);
    addToolSeparator();

    // Setup combo box
    comboPanel = new ComboPanel();
    addToolElement(comboPanel);
    addToolBar();

    sbPanel = new StatusPanel();
    content.add(sbPanel, BorderLayout.SOUTH);
  
    // Main content
    TextPanel textPanel = getTextPanel();
    textPanel.setPreferredSize(new Dimension(600, 80));

    errlPanel = new ErrorLoggerPanel();
    errlPanel.setPreferredSize(new Dimension(600, 80));

    // Create a JTabbedPane and the panes and specify which pane is displayed first 
    JTabbedPane tabs = new JTabbedPane();
    tabs.setFont(AppConstants.gFont);
    tabs.addTab("Message Logger", sdataIcon, textPanel);
    tabs.addTab("System I/O",     sdataIcon, errlPanel);
    tabs.setTabPlacement(JTabbedPane.TOP);

    infoPanel = new InfoPanel();  
    JSplitPane splitPane = Tools.createSplitPane(JSplitPane.VERTICAL_SPLIT, infoPanel, tabs);

    JPanel panel = new JPanel(new BorderLayout());
    panel.add(splitPane, BorderLayout.CENTER);

    content.add(panel, BorderLayout.CENTER);
  }
  public void addActionMenu(JMenuBar menuBar) {
    JMenu menu = new JMenu("Action");
    menu.setMnemonic(KeyEvent.VK_A);
    menuBar.add(menu);
  
    JMenuItem item = menu.add(new AddAction(saddIcon));
    item.setMnemonic(KeyEvent.VK_0);
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0, KeyEvent.CTRL_MASK));
    menu.add(item);

    item = menu.add(new AddRemoteAction(null));
    item.setMnemonic(KeyEvent.VK_1);
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, KeyEvent.CTRL_MASK));
    menu.add(item);

    // disabled to start with, enable when the first buffer is added
    remMenuItem = menu.add(new RemoveAction(ssubIcon));
    remMenuItem.setEnabled(false);
    menu.add(remMenuItem);
 
    remAllMenuItem = menu.add(new RemoveAllAction(sremIcon));
    remAllMenuItem.setEnabled(false);
    menu.add(remAllMenuItem);

    item = menu.add(new FracAction(swizIcon));
    menu.add(item);
  }
  public void enableItems(boolean decision) {
    remButton.setEnabled(decision);
    remMenuItem.setEnabled(decision);
    remAllMenuItem.setEnabled(decision);
  }
  /* 'Add' action */
  class AddAction extends AbstractAction {
    public AddAction(Icon icon) { 
      super("Add dump(s)", icon); 
    }
    public void actionPerformed(ActionEvent e) {
      boolean ans = addDump();
      if (ans) enableItems(true);
    }
  }
  /* 'Add' action */
  class AddRemoteAction extends AbstractAction {
    public AddRemoteAction(Icon icon) { 
      super("Add remote dump(s)", icon); 
    }
    public void actionPerformed(ActionEvent e) {
      RemoteFileDialog dlg = new RemoteFileDialog();
      if (dlg.items.length == 0) return; 
      String [] urls = dlg.items;
      boolean ans = addRemoteFiles(urls);
      if (ans) enableItems(true);
    }
  }
  /* 'Remove' action */
  class RemoveAction extends AbstractAction {
    public RemoveAction(Icon icon) { 
      super("Remove selected dump(s)", icon); 
    }
    public void actionPerformed(ActionEvent e) {
      removeDump();
    }
  }
  /* 'Remove' action */
  class RemoveAllAction extends AbstractAction {
    public RemoveAllAction(Icon icon) { 
      super("Remove all", icon); 
    }
    public void actionPerformed(ActionEvent e) {
      removeAll();
    }
  }
  /* 'Remove' action */
  class FracAction extends AbstractAction {
    public FracAction(Icon icon) { 
      super("Read Dump Fraction", icon); 
    }
    public void actionPerformed(ActionEvent e) {
      infoPanel.bufferPanel.selectReadFraction();
    }
  }
  public boolean addDump() {
    // Use JFilechooser to get filename and attempt to open and read the file into the
    // text component
    repaint();
    JFileChooser chooser = getFileChooser();
    chooser.setMultiSelectionEnabled(true);

    // Add a custom file filter and disable the default
    // (Accept All) file filter.
    chooser.addChoosableFileFilter(new FileTypeFilter());
    chooser.setAcceptAllFileFilterUsed(false);

    if (chooser.showOpenDialog(InfoFrame.this) 
            != JFileChooser.APPROVE_OPTION)  return false;
    final File [] files = chooser.getSelectedFiles();
    if (files.length == 0) return false;
    
    return addLocalFiles(files);
  }
  public boolean addLocalFiles(final File [] files) {
    new LocalFileRunner(files).start();
    return true;
  }
  public boolean addRemoteFiles(final String [] urls) {
    new RemoteFileRunner(urls).start();
    return true;
  }
  class LocalFileRunner extends Thread {
    private File [] files; 
    LocalFileRunner (final File [] files) {
      int size = files.length;
      this.files = new File[size];
      System.arraycopy(files, 0, this.files, 0, size);
    }
    public void run() {
      for (int i = 0; i < files.length; i++) {
        File file = files[i];

        BufferedReader reader = null;
        String infile = file.getAbsolutePath();
        String ext = FileTypeFilter.getExtension(infile);            
        if (isDebugOn()) System.out.println("Input File: " + infile);
        try {
          if ( FileTypeFilter.isArchive(infile) ) {
            ArchiveExtractor ae = null; 
            if (ext.equals(FileTypeFilter.JAR)) 
              ae = new JarExtractor(infile);
            else if (ext.equals(FileTypeFilter.ZIP))
              ae = new ZipExtractor(infile);
            else if (FileTypeFilter.isTar(infile) || FileTypeFilter.isTgz(infile))
              ae = new TarExtractor(infile);

            if (ae == null) return;
            for (Map.Entry<String, String> entry : ae.getEntries()) {  
              String filename = entry.getKey(); 
              String   buffer = entry.getValue(); 
              reader = new BufferedReader (new StringReader(buffer));
              if (isDebugOn()) System.out.println("Filename: " + filename);
              readDump(filename, reader);
              reader = null;
            } 
	  }
	  else if ( FileTypeFilter.isGzipped(infile) ) {
            Gunzip obj = new Gunzip(infile);
            reader = new BufferedReader (new StringReader(obj.getContent()));
            String uName = infile.substring(0,infile.lastIndexOf(".gz"));
            readDump(uName, reader);
	  }
	  // Assume plain ascii file
	  else if ( FileTypeFilter.isPlain(infile) ) {
            reader = new BufferedReader(new FileReader(file));
	    readDump(infile, reader);
          }
          else
            JOptionPane.showMessageDialog(null, 
               "Unsupported file type, ext = " + ext, "Alert", JOptionPane.ERROR_MESSAGE);
        }
        catch (FileNotFoundException ex) { 
          System.out.println("The file " + infile + " wasn't found!"); 
        } 
        catch (IOException ex) {
          System.out.println("Error reading from " + infile);
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
    }
  }
  class RemoteFileRunner extends Thread {
    private String [] urls; 
    RemoteFileRunner (final String [] urls) {
      int size = urls.length;
      this.urls = new String[size];
      System.arraycopy(urls, 0, this.urls, 0, size);
    }
      // Hide local/remote inside the Gunzip. Similarly create a PlainDump
      // Class to hide local/remote 
      // sbPanel.setProgress(filename, 1);

    public void run() {
      for (int i = 0; i < urls.length; i++ ) {
        String location = urls[i];
        if ( !FileTypeFilter.isWebFile(location) ) continue;
        BufferedReader reader = null;
        if (isDebugOn()) System.out.println("Input File: " + location);
        try  {
          if ( FileTypeFilter.isTar(location) || FileTypeFilter.isTgz(location) ) {
            ArchiveExtractor ae = new TarExtractor(location);
            for (Map.Entry<String, String> entry : ae.getEntries()) {  
              String filename = entry.getKey(); 
              String   buffer = entry.getValue(); 
              reader = new BufferedReader (new StringReader(buffer));
              if (isDebugOn()) System.out.println("Filename: " + filename);
              readDump(filename, reader);
              reader = null;
            } 
	  }
          else if ( FileTypeFilter.isGzipped(location) ) {
	    Gunzip gzobj = new Gunzip((new FetchURL(location)).getInputStream());
            reader = new BufferedReader (new StringReader(gzobj.getContent()));
            String infile = location.substring(location.lastIndexOf('/'));
            infile = infile.substring(0, infile.lastIndexOf(FileTypeFilter.GZ));
            readDump(infile, reader);
	  }
          else if ( FileTypeFilter.isPlain(location) ) {
            reader = new BufferedReader (new StringReader((new FetchURL(location)).toString()));
            String infile = location.substring(location.lastIndexOf('/'));
            readDump(infile, reader);
          }
          else
            JOptionPane.showMessageDialog(null, 
             "Unsupported file type, URL = " + location, "Alert", JOptionPane.ERROR_MESSAGE);
        }
        catch (IOException ex) {
          System.out.println("Error reading from " + location);
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
    }
  }
  public synchronized void readFraction(final String content) {
    String [] lines = content.split("\\n");

    // In case there were many comments lines at the top
    // find the first useful line
    int index = -1;
    for (int i = 0; i < lines.length; i++) {
      if (Tools.isComment(lines[i])) continue;
      index = i; break; 
    }
    if (index < 0) return;

    // Now get the header
    String [] fields = Tools.split(lines[index]);
    String crate = fields[0];
        int slot = Integer.parseInt(fields[1]);
    String board = fields[2];
    String   spy = fields[3];

    // And then the content
    Vector<Integer> vec = new Vector<Integer>(AppConstants.MAXBUF);
    for (int i = index+1; i < lines.length; i++) {
      if (Tools.isComment(lines[i])) continue;
      String [] words = Tools.split(lines[i]);
      for (int j = 0; j < words.length; j++) {
        vec.add(new Integer(Integer.parseInt(words[j], 16))); 
      }
    }
    
    SvtBufferData [] bufferData = new SvtBufferData[1];
    bufferData[0] = new SvtBufferData(spy);
    bufferData[0].setData(vec);

    SvtBoardData [] boardData = new SvtBoardData[1];
    boardData[0] = new SvtBoardData(board, slot);
    boardData[0].setBufferData(bufferData);

    // Now add boards to the crate
    SvtCrateData crateData = new SvtCrateData(crate);
    crateData.setBoardData(boardData);

    // Finally add the crate to the global map and update the tree
    // Dump filename is ready, in case of offline use the cratename
    // should be the fully specified filename 
    SvtCrateMap cratemap = SvtCrateMap.getInstance();
    cratemap.addEntry(crateData);
    addCrate(crateData);
  }
  public synchronized void readDump(final String filename, BufferedReader reader) throws IOException {
    Vector<Integer> indexList = new Vector<Integer>(AppConstants.MAXBUF);
    Vector<String>     sbList = new Vector<String>(AppConstants.MAXBUF);
    Vector<String>       list = new Vector<String>(AppConstants.MEDIUM_BUFFER_SIZE);
    String line;

    int nlines = 0;
    while ((line = reader.readLine()) != null) {
      list.addElement(line);
      if (line.startsWith("SB")) {
        indexList.addElement(new Integer(nlines));
        sbList.addElement(line);
      }
      nlines++;   // must be here
    }          
    reader.close();
          
    // The map should be empty every time
    map.clear();

    // Try to add the crate to the tree
    String crateName = new String("");
    String lastBoardTag = new String("");
    StringBuilder buf = new StringBuilder(AppConstants.SMALL_BUFFER_SIZE);
    boolean first = true;
    for ( Iterator<String> it = sbList.iterator(); it.hasNext(); ) {
      String obj = it.next();
      String [] words = Tools.split(obj);
      if (crateName.indexOf("b0svt") == -1) crateName = words[1];

      String boardTag = words[3]+"-"+words[2]; // like MRG-3
      if (first) {
        buf.append(words[4]).append(":");
        first = false;
      }
      else if (boardTag.equals(lastBoardTag)) {
        buf.append(words[4]).append(":");
      }
      else {
        map.put(lastBoardTag, buf.toString());
        buf.setLength(0);

        buf.append(words[4]).append(":");
      }
      lastBoardTag = boardTag;
    }
    map.put(lastBoardTag, buf.toString());
    buf.setLength(0);

    // Ok, crate, board, buffer list is ready
    SvtBoardData [] boardData = new SvtBoardData[map.size()];
    int nb = 0;
    for (Map.Entry<String, String> entry : map.entrySet()) {  
      String   boardTag = entry.getKey(); 
      String bufferList = entry.getValue(); 
      String [] words = boardTag.split("-"); // e.g MRG-3

      // Board info
      boardData[nb] = new SvtBoardData(words[0], Integer.parseInt(words[1]));

      if (bufferList.endsWith(":")) 
        bufferList = bufferList.substring(0, bufferList.length()-1);
      String [] spys = bufferList.split(":");

      // Buffer info, add buffers to a board 
      SvtBufferData [] bufferData = new SvtBufferData[spys.length];
      for (int i = 0; i < bufferData.length; i++)
        bufferData[i] = new SvtBufferData(spys[i]);

      boardData[nb].setBufferData(bufferData);

      nb++;
    }

    // Now add boards to the crate
    SvtCrateData crateData = new SvtCrateData(crateName);
    crateData.setBoardData(boardData);

    // Finally add the crate to the global map and update the tree
    // Dump filename is ready, in case of offline use the cratename
    // should be the fully specified filename 
    SvtCrateMap cratemap = SvtCrateMap.getInstance();
    cratemap.addEntry(crateData);
    addCrate(crateData);

    SvtDataManager manager = SvtDataManager.getInstance();
    manager.updateBuffer(list, indexList);
  }
    // Clear the selected node from the tree and also from the data structure
  public void removeDump() {
    boolean ok = infoPanel.listPanel.removeCurrentNode();
    if (ok) resetAll(infoPanel.listPanel.noNode());
  }
    // Clear the tree and also the data structure
  public void removeAll() {
    infoPanel.listPanel.clear();
    resetAll(true);   
  }
  public void resetAll(boolean noNode) {
    infoPanel.bufferPanel.reset();
    sbPanel.reset();
    if (noNode) enableItems(false);
  }
  public void setStatus(final String status) {
    getStatusBar().setText(status);
  }
  private void updateOptionsMenu(JMenuBar menuBar) {
    JMenu menu = menuBar.getMenu(1);

    menu.addSeparator();
    onlineUseCB = new JCheckBoxMenuItem("Online/Offline", false);
    onlineUseCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
        warn("Use the tool: " + ((onlineUseCB.isSelected()) ? "Online" : "Offline"));
      }
    });
    menu.add(onlineUseCB);

    autoUpdateCB = new JCheckBoxMenuItem("Auto Update", true);
    autoUpdateCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
        warn("AutoUpdate " + ((autoUpdateCB.isSelected()) ? "enabled" : "disabled"));
      }
    });
    menu.add(autoUpdateCB);

    menu.addSeparator();
    ButtonGroup group = new ButtonGroup();
    JRadioButtonMenuItem item = Tools.radioButtonItem("Show all SVT words", group, 
                                     true, Color.black, KeyEvent.VK_D, 0);
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
        showEE = false;
        infoPanel.bufferPanel.showData(0);
      }
    });
    menu.add(item);

    item = Tools.radioButtonItem("Show EE words only", group, 
                         false, Color.black, KeyEvent.VK_Y, 0);
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
        showEE = true;
        infoPanel.bufferPanel.showData(1);
      }
    });
    menu.add(item);

  }
  /* Accessors */

  /** Get a reference to the <CODE>InfoPanel</CODE>
    * @return A reference to the <CODE>InfoPanel</CODE>
    */
  protected InfoPanel getInfoPanel() {
    return infoPanel;
  }
  public void addCrate(final SvtCrateData crateData) {
    infoPanel.listPanel.addCrate(crateData);
  }
  public void showData(final String cpuName, 
                       int slot, 
                       final String board,
                       final String spy)
  {
    infoPanel.listPanel.showData(cpuName, slot, board, spy);
    infoPanel.bufferPanel.updateDisplay(cpuName, slot, board, spy);
  }
  public void updateGUI(final String cpuName, 
                        int slot, 
                        final String board,
                        final String spy)
  {
    if (autoUpdateCB.isSelected())
      infoPanel.bufferPanel.updateDisplay(cpuName, slot, board, spy);
  }
  public void updateGUI(final SvtCrateData crateData) {
    if (autoUpdateCB.isSelected())
      infoPanel.bufferPanel.updateDisplay(crateData.getName());
  }
  public void updateGUI(final String crateName) {
    if (autoUpdateCB.isSelected())
      infoPanel.bufferPanel.updateDisplay(crateName);
  }
  class ProgressPanel extends JPanel {
    private JProgressBar progressBar;
    ProgressPanel() {
      super(true);
      setLayout(new BorderLayout());
       
      progressBar = new JProgressBar();
      progressBar.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 4));
      progressBar.setIndeterminate(false);
      progressBar.setStringPainted(true);
      progressBar.setString("waiting ...");

      add(progressBar, BorderLayout.CENTER);
      add(new JSeparator(SwingConstants.VERTICAL), BorderLayout.EAST);
    } 
    private void setMessage(String message) {
      progressBar.setString(message);
    }
    public void setProgressMax(int maxProgress) {
      progressBar.setMaximum(maxProgress);
    }
    public void setProgress(int progress) {
      final int theProgress = progress;
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          progressBar.setValue(theProgress);
        }
      });
    }
    public void setProgress(final String message, int progress) {
      final int theProgress = progress;
      final String theMessage = message;
      setProgress(progress);
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          progressBar.setValue(theProgress);
          setMessage(theMessage);
        }
      });
    }
  }
  class StatusPanel extends JPanel {
    private StatusBar idBar, 
                      typeBar, 
                      formBar, 
                      nwBar;
    private ProgressPanel progressPanel;
    StatusPanel() {
      super(true);
      setLayout(new BorderLayout());
      setBorder(BorderFactory.createEmptyBorder(3,1,3,1));

      // Then the ProgressBar/StatusBar/Buffer Identity/Word Type/Format labels
      StatusBar statusBar = getStatusBar();

      JPanel pWest = new JPanel(new BorderLayout());
      add(pWest, BorderLayout.WEST);

      progressPanel = new ProgressPanel();
      progressPanel.setPreferredSize(new Dimension(200, statusBar.getHeight()));
      pWest.add(progressPanel, BorderLayout.WEST);

      statusBar.setPreferredSize(new Dimension(200, statusBar.getHeight()));
      pWest.add(statusBar, BorderLayout.CENTER);

      idBar = new StatusBar();  
      add(idBar, BorderLayout.CENTER);
  
      JPanel pEast = new JPanel(new BorderLayout());
      add(pEast, BorderLayout.EAST);

      nwBar = new StatusBar("0 word");
      pEast.add(nwBar, BorderLayout.WEST);

      typeBar = new StatusBar("All Words");
      pEast.add(typeBar, BorderLayout.CENTER);

      formBar = new StatusBar("Physics/Hex");
      formBar.setPreferredSize(new Dimension(75, statusBar.getHeight()));
      pEast.add(formBar, BorderLayout.EAST);
    }
    void setId(final String text) {
      idBar.setText(text);
    }
    void setNw(final String text) {
      nwBar.setText(text);
    }
    void setType(final String text) {
      typeBar.setText(text);
    }
    void setForm(final String text) {
      formBar.setText(text);
    }
    void setProgress(final String message, int p) {
      progressPanel.setProgress(message, p);
    }
    public void reset() {
      setId("Ready ..."); 
      setNw("0 word"); 
    }
  }
  class ComboPanel extends JPanel {
    JComboBox combo = new JComboBox();
    ComboPanel() {
      setLayout(new BorderLayout());
      setMaximumSize(new Dimension(140, 30));
      setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 2));

      // Add a Label
      JLabel label = Tools.createLabel("MRG data as: ", null, JLabel.LEFT, Color.black, null);
      add(label, BorderLayout.WEST);

      combo.setPreferredSize(new Dimension(100, 25));
      int len = SpyType.svtTypeList.length;
      for (int i = 0; i < len; i++)
        combo.addItem(SpyType.svtTypeList[i]);   
      combo.setSelectedIndex(0);
      combo.setMaximumRowCount(len);
      combo.setEditable(false);  
      combo.setEnabled(false);

      // Listens to the combo box for SVT Data type selection for MRG Data
      combo.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          JComboBox cb = (JComboBox) e.getSource();
          String item = (String) cb.getSelectedItem();

          // Trigger display of the correct data in the Buffer Panel
          infoPanel.bufferPanel.showData((showEE)?1:0);

          // Synchronize the the Plot Panel 
          // option ComboBox 
          infoPanel.bufferPanel.plotPanel.setDataType(item);

          // Histogram list
           
        }
      });
      add(combo,BorderLayout.CENTER);      
    }
    void enableCombo(boolean decision) {
      combo.setEnabled(decision);
    }
    JComboBox getCombo() {
      return combo;
    }
  }
  class InfoPanel extends JPanel {
    protected ListPanel listPanel;
    protected BufferPanel bufferPanel;      
    protected JSplitPane splitPane;

    InfoPanel() {
      super(true);
      buildGUI();
    }
    protected void buildGUI() {
      setLayout(new BorderLayout());

      // create the Crate List Panel 
      listPanel = new ListPanel();
      listPanel.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
      Dimension minSize = listPanel.getScrollPane().getPreferredSize();

      bufferPanel = new BufferPanel();      
      bufferPanel.setMinimumSize(minSize);
      bufferPanel.setBorder(BorderFactory.createLineBorder(new Color(200,200,200)));

      // Add the scroll panes to a split pane
      splitPane = Tools.createSplitPane(JSplitPane.HORIZONTAL_SPLIT, listPanel, bufferPanel);
      splitPane.setDividerLocation(TREEWIDTH);
      splitPane.setPreferredSize(new Dimension(TOTALWIDTH, XHEIGHT));

      add(splitPane, BorderLayout.CENTER);
    }
    class MyRenderer extends DefaultTreeCellRenderer {
      final private Icon       openIcon = new ImageIcon(AppConstants.iconDir+"opened.gif");
      final private Icon      closeIcon = new ImageIcon(AppConstants.iconDir+"closed.gif");
      final private Icon       leafIcon = new ImageIcon(AppConstants.iconDir+"mini-ball.gif");
      final private Icon activeLeafIcon = new ImageIcon(AppConstants.iconDir+"stock_calc-accept.png");
      public MyRenderer() {}
      public Component getTreeCellRendererComponent(
                        JTree tree,
                        Object value,
                        boolean sel,
                        boolean expanded,
                        boolean leaf,
                        int row,
                        boolean hasFocus) 
      {

        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
      
        setOpenIcon(openIcon);
        setClosedIcon(closeIcon);
        setLeafIcon(leafIcon);

        if (leaf && isOnline()) {
          if (isActive(value)) {
            setIcon(activeLeafIcon);
            setToolTipText("This buffer has received data");
          } 
          else {
            setToolTipText("This buffer has not yet received any data");
          } 
        }
        return this;
      }
      protected boolean isActive(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        if (!(node.getUserObject() instanceof BufferInfo)) return false;
        BufferInfo nodeInfo = (BufferInfo) node.getUserObject();
        if (nodeInfo.hasData()) return true;

        return false;
      }
    }
    class ListPanel extends JPanel {
      private JTree tree;
      private JScrollPane treeView;
      private DefaultMutableTreeNode rootNode;
      private DefaultTreeModel treeModel;
      private MyTreeModelListener mtmListener;
      private DefaultTreeCellRenderer renderer;
      ListPanel() {
	super(true);
    	buildGUI();
      } 
      protected void buildGUI() {
        setLayout(new BorderLayout());
    	// Create the nodes 
    	rootNode    = new DefaultMutableTreeNode("SVT");
        treeModel   = new DefaultTreeModel(rootNode);
        mtmListener = new MyTreeModelListener(treeModel);
        treeModel.addTreeModelListener(mtmListener);

    	createNodes();
      
    	// Create a tree that allows one selection at a time 
    	tree = new JTree(treeModel);
        ToolTipManager.sharedInstance().registerComponent(tree);
        tree.setFont(AppConstants.gFont);
    	tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
      
    	tree.putClientProperty("JTree.lineStyle", "Angled");
    	tree.setShowsRootHandles(true);

        if (!isOnline()) 
	  tree.addMouseListener(new NodeMonitor(tree, rootNode));

        renderer = new MyRenderer();
        tree.setCellRenderer(renderer);

    	// Listen to the selection changes
    	tree.addTreeSelectionListener(new TreeSelectionListener() {
    	  public void valueChanged(TreeSelectionEvent e) {
    	    DefaultMutableTreeNode node = 
    	      (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
    	    if (node == null) return;
      
    	    Object nodeInfo = node.getUserObject();
    	    String leafName = nodeInfo.toString();

            // Enable/Disable MRG Data type Combo panel
            // PlotPanel should be synchronized
            if (leafName.startsWith("MRG")) 
              comboPanel.enableCombo(true);
            else 
              comboPanel.enableCombo(false);

    	    if (node.isLeaf()) {  
              DefaultMutableTreeNode board = (DefaultMutableTreeNode) node.getParent();
              DefaultMutableTreeNode crate = null;
              if (board != null) crate = (DefaultMutableTreeNode) board.getParent();

              if (crate != null) {
                String [] fields = board.toString().split("-");
                String boardName = fields[0];
                int slot         = Integer.parseInt(fields[1]);
               
                bufferPanel.setSpyName(crate.toString(), slot, boardName, leafName);;
                bufferPanel.updateDisplay(crate.toString());

                Component comp = infoPanel.splitPane.getRightComponent();
                if ( !(comp instanceof BufferPanel) ) {
                  infoPanel.splitPane.remove(infoPanel.splitPane.getRightComponent());
                  infoPanel.splitPane.setRightComponent(bufferPanel);
                } 
              }
    	    }
            repaint();
    	  }
    	});
    	// Create a scroll pane and add the tree to it 
    	treeView = new JScrollPane(tree);
    	add(treeView, BorderLayout.CENTER);
        treeView.setPreferredSize(new Dimension(TREEWIDTH, XHEIGHT));
      }
      public boolean noNode() {
        return (rootNode.getChildCount() == 0);
      }
      /** Remove all nodes except the root node. */
      public void clear() {
	rootNode.removeAllChildren();
	treeModel.reload();
      }
       /** Remove the currently selected node. */
      public boolean removeCurrentNode() {
        TreePath sPath = tree.getSelectionPath();
        if (sPath == null) return false; 
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) (sPath.getLastPathComponent());
        MutableTreeNode pnode = (MutableTreeNode)(node.getParent());
        if (pnode == rootNode) {
          treeModel.removeNodeFromParent(node);
          return true;
        }
        // Either there was no selection, or the root was selected.
        Toolkit.getDefaultToolkit().beep();
        return false;
      }
      public boolean showData(String crate, int slot, String board, String spy) {
        boolean found = false;
        for (Enumeration e = rootNode.children() ; e.hasMoreElements() ;) {
          DefaultMutableTreeNode crateNode = (DefaultMutableTreeNode) e.nextElement();
          if (crateNode.toString().equals(crate)) {
            TreePath cratePath = new TreePath(new Object[] {rootNode, crateNode}); 
            // Does not work with 1.1.8
            //tree.setExpandsSelectedPaths(true);
            tree.expandPath(cratePath); 
            for (Enumeration x = crateNode.children() ; x.hasMoreElements() ;) {
              DefaultMutableTreeNode boardNode = 
                (DefaultMutableTreeNode) x.nextElement();
              String name = board+"-"+slot;
              if (boardNode.toString().equals(name)) {
                TreePath boardPath = new TreePath(new Object[] {rootNode, crateNode, boardNode}); 
                tree.expandPath(boardPath); 

                for (Enumeration y = boardNode.children(); y.hasMoreElements(); ) {
                  DefaultMutableTreeNode bufferNode = 
                      (DefaultMutableTreeNode) y.nextElement();
                  if (bufferNode.toString().equals(spy)) {
                    if (bufferNode.isRoot() || !bufferNode.isLeaf()) break;
                    TreePath bufferPath 
                      = new TreePath(new Object[] {rootNode, crateNode, boardNode, bufferNode}); 
                    if (isDebugOn()) System.out.println(bufferPath);

                    tree.setSelectionPath(bufferPath);
                    tree.setSelectionRow(tree.getRowForPath(bufferPath));
                    tree.makeVisible(bufferPath);
                    repaint();
                    found = true;
                    return found;
                  }
                }
              }
            }
          }
        }        
        return found;
      }
      protected void createNodes() {
        SvtCrateMap map = SvtCrateMap.getInstance();

        for (Map.Entry<String, SvtCrateData> entry : map.entrySet()) {  
          String           crate = entry.getKey();
          SvtCrateData crateData = entry.getValue();
          if (crateData == null) continue;

          addCrate(crateData);
        }    	
      }
      protected void addCrate(final SvtCrateData crateData) throws NullPointerException {
        String crateName = crateData.getName();
    	DefaultMutableTreeNode crate = new DefaultMutableTreeNode(crateName);
    	rootNode.add(crate);
	
        SvtBoardData [] boardData = crateData.getBoardData();
        for (int j = 0; j < boardData.length; j++) { 
          String boardName = boardData[j].getType();
                  int slot = boardData[j].getSlot();
          if (boardData[j].getNBuffers() == 0) continue;

      	  DefaultMutableTreeNode board = new DefaultMutableTreeNode(boardName+"-"+slot);
    	  crate.add(board);
	
          SvtBufferData [] bufferData = boardData[j].getBufferData();
          for (int k = 0; k < bufferData.length; k++) {
            String bufferName = bufferData[k].getType();
            BufferInfo nodeInfo = new BufferInfo(crateName, boardName, slot, bufferData[k]);
            DefaultMutableTreeNode buffer = new DefaultMutableTreeNode(nodeInfo);
    	    board.add(buffer);
          }
        }
        treeModel.reload(); 
      }
      protected JTree getTree() {
        return tree;
      }
      protected JScrollPane getScrollPane() {
        return treeView;
      }
      protected DefaultMutableTreeNode getRootNode() {
        return rootNode;
      }
    }
    class NodeMonitor extends MouseAdapter {
      JTree tree;
      DefaultMutableTreeNode rootNode;
      JPopupMenu popup;

      public NodeMonitor(JTree tree, DefaultMutableTreeNode rootNode) {
        this.tree = tree;
        this.rootNode = rootNode;
        popup = new JPopupMenu("Node Options");
        JMenuItem item = new JMenuItem("remove node");
        item.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ev) {
            removeDump();
          }
        });
        popup.add(item);
        popup.pack();

        tree.add(popup);
      }

      private void checkPopupTrigger(MouseEvent e) {
        if (e.isPopupTrigger()) {
          Point p = e.getPoint();
          TreePath path = tree.getClosestPathForLocation(p.x, p.y);
          if (path == null) return; 
          if (path != tree.getSelectionPath())
            tree.setSelectionPath(path);

          DefaultMutableTreeNode node = 
             (DefaultMutableTreeNode) path.getLastPathComponent();
          MutableTreeNode pnode = (MutableTreeNode)node.getParent();
          if (pnode != rootNode) return;

          Rectangle r = tree.getPathBounds(path);
          popup.show(tree, r.x + r.width, p.y);
        }
      }

      public void mousePressed(MouseEvent e)  { checkPopupTrigger(e); }
      public void mouseClicked(MouseEvent e)  { checkPopupTrigger(e); }
      public void mouseReleased(MouseEvent e) { checkPopupTrigger(e); }
    }
    /**
     * Contains all the informations about a board spy buffer.
     * The panel consists of 2 pages. The upper part of the first page
     * indicates occurrance of error on the EE word, giving detail
     * (accumulated number of times error of a kind has occurred) at
     * the bottom. The second page displays the Spy buffer data itself.
     * @author S. Sarkar
     * @version 1.0, 8/2000
     */
    class BufferPanel extends JPanel { 
      private final String [] options = {
    	"Show End Event Words", 
    	"Show All Data"
      }; 
        /** Name of the crate under consideration */
      private String crate;
        /** Slot number the board under consideration sits in */
      private int slot;
        /** Name of the board under consideration */
      private String board;
        /** Spy Buffer under consideration */
      private String spy;
        /** The string which describes the spy buffer uniquely */
      private String title;
      
        /** The JTabbedPane variable */
      private JTabbedPane tabs;
        /** The Global status panel */
      private GlobalStatusPanel statusPanel;
        /** Spy Buffer data panel */
      private DataPanel dataPanel;
        /** The plot  panel */
      private PlotPanel plotPanel;
      private JPanel fracPanel;
      
      SvtEventsBase events = null;
      String lastSpyBuffer = "";

      /** 
       * Construct <CODE>InfoPanel</CODE>
       * @param crate Name of the crate
       * @param slot Slot number 
       * @param board Name of the board
       * @param spy   The spy buffer under consideration
       */
      public BufferPanel() {
        this("b0svt05", 12, "TF", "TF_OSPY");
      }
      public BufferPanel(String crate, int slot, String board, String spy) {
    	setSpyName(crate, slot, board, spy);
    	buildGUI();
      }
      /**
       * Build the title from crate/board/slot/spy  names
       * @param crate Name of the crate
       * @param slot Slot number 
       * @param board Name of the board
       * @param spy   The spy buffer under consideration
       */
      public void setSpyName(String crate, int slot, String board, String spy) {
    	this.crate = crate;
    	this.slot  = slot;
    	this.board = board;
    	this.spy   = spy;
    	title      = " " + spy + " in crate " 
    			 + crate + " slot " + slot + " "; 
      }
      /** Build the user interface */
      private void buildGUI() {
	setLayout(new BorderLayout());
    	//sbPanel.setId(title);
      
    	tabs = new JTabbedPane();
        tabs.setFont(AppConstants.gFont);
    	tabs.setTabPlacement(JTabbedPane.BOTTOM);
      
    	// Add the panes and specify which pane is displayed first 
    	tabs.addTab("Global Status", statIcon, statusPanel = new GlobalStatusPanel(this));
    	tabs.addTab("Data",          dataIcon,   dataPanel = new DataPanel(this));
        tabs.addTab("Plots",         histIcon,   plotPanel = new PlotPanel());
        tabs.addTab("Read Fraction", docuIcon,   fracPanel = new FractionPanel(this));

    	add(tabs, BorderLayout.CENTER);
      }
      public void selectReadFraction() {
	tabs.setSelectedComponent(fracPanel);
      }
      // Accessors
      /** Get the Global status panel 
       *  @return The reference to the global status panel
       */
      public GlobalStatusPanel getStatusPanel() {
    	return statusPanel;
      }
      /** Get the Spy buffer data display panel 
       *  @return The reference to the data display panel
       */
      public DataPanel getDataPanel() {
    	return dataPanel;
      }
      /** Get the Spy Buffer display panel 
       *  @return The reference to the Spy buffer display panel
       */
      public SpyStatusPanel getSpyPanel() {
    	return statusPanel.getSpyPanel();
      }
      /** Get the Global Error panel 
       *  @return The reference to the Global Error panel
       */
      public GlobalErrorPanel getErrorPanel() {
    	return statusPanel.getErrorPanel();
      }
      /** Get the basic text panel 
       *  @return The reference to the underlying text panel
       */
      public AbstractTextPanel getRawDataPanel() {
    	return dataPanel.getRawDataPanel();
      }
      public AbstractTextPanel getPhysicsDataPanel() {
    	return dataPanel.getPhysicsDataPanel();
      }
      public void showData(int option) throws NullPointerException {
    	sbPanel.setType(((option == 0) ? "All " : "EE ") + "Words");

    	// Get Spy buffer data first
    	SvtBufferData bufferData = getBufferData(crate, slot, board, spy);
    	int [] data = bufferData.getData();
      
        showRawData(option, data);  // EEOnly: 1, All: 0
        showPhysicsData(option, data);

        repaint(); 
      }
      public void showRawData(int option, final int [] data) {
    	AbstractTextPanel textP = getRawDataPanel();
 
        PrintfFormat format = AppConstants.h6Format;
        int maxcol = 10;

        if (spy.indexOf("HF_ISPY") != -1) {
          format = AppConstants.h4Format;
          maxcol = 14; 
        }

    	// Now display data
    	int ncol = 0;
        int nwords = 0;
    	StringBuilder sb = new StringBuilder(AppConstants.MEDIUM_BUFFER_SIZE);
    	for (int i = 0; i < data.length; i++) {
    	  if (option == 0 || (data[i] & 0x600000) == 0x600000) { 
    	    sb.append(format.sprintf(data[i])).append(" ");
    	    ncol++;
            nwords++;
    	  }
    	  if (ncol == maxcol) {
    	    sb.append("\n");
    	    ncol = 0;
          }
    	}
    	textP.setTextThreaded(sb.toString());
        sbPanel.setNw( ((data.length < 1) ? 0 : nwords) + " word(s)" );
      }
      public void showPhysicsData(int option, final int [] data) {
        StringBuilder buf = new StringBuilder(AppConstants.LARGE_BUFFER_SIZE);        
    	AbstractTextPanel textP = getPhysicsDataPanel();

        // Check if this still applies  
        if (option > 0 && spy.indexOf("HF_ISPY") == -1) {  // HF_ISPYs do not have EoE words
          buf.append(EEWord.getTextLabels(board));
          for (int i = 0; i < data.length; i++) {
            if ((data[i] & 0x600000) == 0x600000) 
              buf.append(EEWord.getTextData(data[i]));
          }
        }
        else {
          int type;
          if (spy.indexOf("MRG") != -1) {
            String dType = (String) comboPanel.getCombo().getSelectedItem();
            type = SpyType.findMRGType(dType);
          }
          else {
            type = SpyType.findType(spy);
          }
          StringBuilder b = new StringBuilder();
          b.append(crate).append(":").append(slot).append(":").append(board).append(":").append(spy);
          String thisSpyBuffer = b.toString();
          if (thisSpyBuffer == lastSpyBuffer) {  
            // Same buffer is updated, reuse the existing event. Can we?
            // We'll have to ensure that it is not coming from a different
            // create/slot/board combination
            //events.buildEvents(data);
          }
          else {                                 // Different buffer, clear the event and load
                                                 // with new data
            events = null;                       // invite Garbage collector
            if (isDebugOn()) System.out.println("spy = " + spy + " lastSpyBuffer = " + lastSpyBuffer);

            if      (type == SpyType.HIT)    events = new HitEvents(data);
            else if (type == SpyType.ROAD)   events = new RoadEvents(data);
            else if (type == SpyType.PACKET) events = new PacketEvents(data);
            else if (type == SpyType.SVTTRK) events = new TFEvents(data);
            else if (type == SpyType.XFTTRK) events = new XTFAEvents(data);
            else
              JOptionPane.showMessageDialog(null, 
                "Unsupported SVT Data type " + spy, "Alert", JOptionPane.WARNING_MESSAGE);

	    lastSpyBuffer = thisSpyBuffer;
          }
          if (events == null) {
            plotPanel.clear();
            buf.setLength(0);
          }
          else {
            buf.append(events);
            // -- UPDATE --
            // If Plot panel is available, prepare data for plotting as well
            // for HF_ISPY skip (true also for XTFA L1 and OUT)
            if (spy.indexOf("HF_ISPY") != -1 ||
                spy.indexOf("XTFA_L1_SPY") != -1 ||
                spy.indexOf("XTFA_OUT_ISPY") != -1) plotPanel.clear();
            else
              plotPanel.setStatus(events, crate, board, slot, spy);
          } 
        }
        textP.clear();
     	textP.setTextThreaded(buf.toString());
      }
      public void reset() {
	statusPanel.reset();
        dataPanel.clear();
	plotPanel.clear();
      }
      /** Inner class <CODE>GlobalStatusPanel</CODE>. This class consists of
       *  <UL>
       *    <LI>Spy status panel which shows spy pointer, wrap and freeze bits</LI>
       *    <LI>The global error display panel which shows 8 LEDs corresponding
       *        to 8 EE error bits</LI>
       *  </UL>
       */
      class GlobalStatusPanel extends JPanel {
    	/** Reference to the Spy Bufer status panel */
    	SpyStatusPanel spyPanel;    
    	/** Reference to the Global Error panel */
    	GlobalErrorPanel gErrorPanel;
      
    	/** Construct <CODE>GlobalStatusPanel</CODE> which displays 
    	 *  LEDs to indicate occurance of EE error 
    	 *  @param parent The parent window
    	 */
    	GlobalStatusPanel(JPanel parent) {
    	  setLayout(new BorderLayout());
    	  setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
      
    	  JPanel panel = new JPanel(new BorderLayout());
    	  add(panel, BorderLayout.NORTH);
    	  
    	  spyPanel  = new SpyStatusPanel();    
    	  spyPanel.setPreferredSize(new Dimension(250, 100));
    	  JPanel p1 = new JPanel(new BorderLayout());
    	  p1.add(spyPanel, BorderLayout.WEST);
      
    	  gErrorPanel = new GlobalErrorPanel();
    	  panel.add(p1, BorderLayout.NORTH);      
    	  panel.add(gErrorPanel, BorderLayout.CENTER);      
    	}
    	// Accessors
    	/** Get the Spy Buffer status panel 
    	 *  @return The reference to the Spy buffer status panel
    	 */
    	public SpyStatusPanel getSpyPanel() {
    	  return spyPanel;
    	}
    	/** Get the Global Error panel 
    	 *  @return The reference to the Global Error panel
    	 */
    	public GlobalErrorPanel getErrorPanel() {
    	  return gErrorPanel;
    	}
        public void reset() {
          spyPanel.reset();
          gErrorPanel.reset();
        }
      }
      /** Inner class <CODE>FractionPanel</CODE>. The purpose is to synchronously dump
       *  a number of buffers in a crate. This class consists of
       *  <UL>
       *    <LI>a panel containing checkboxes corresponding to buffers in a crate 
       *    <LI>a button panel with 'save', 'cancel' 'clear' buttons</LI>
       *  </UL>
       */
      class FractionPanel extends JPanel {
    	private AbstractTextPanel textArea = new SimpleTextPanel(new Dimension(700, 500));
        FractionPanel(JPanel parent) {
          setLayout(new BorderLayout());
    	  setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
          add(textArea, BorderLayout.CENTER);

          JPanel p = new JPanel(new BorderLayout());
          p.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
          JButton b = new JButton("Read");
          b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
              SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                  readFraction(textArea.getText());
                  enableItems(true);
                }
              });
            }
          });
          p.add(b, BorderLayout.EAST);
    	  add(p, BorderLayout.SOUTH);
        }
      } 
      /** Inner class <CODE>DataPanel</CODE>. This class consists of
       *  <UL>
       *    <LI>The Spy buffer display option panel, (1) All Data, (2) EE words only</LI>
       *    <LI>The single text panel which displays formatted data words</LI>
       *  </UL>
       */
      class DataPanel extends JPanel {
    	/** Reference to the text panels which display SVT words */
    	private AbstractTextPanel pvPanel = new SimpleTextPanel(new Dimension(700, 500));
    	private AbstractTextPanel rvPanel = new SimpleTextPanel(new Dimension(700, 500));
        private JSplitPane splitPane;

    	/** Construct <CODE>DataPanel</CODE> which displays the SVT data words,
    	 *  either all the words, or the EE words only
    	 *  @param parent The parent window
    	 */
    	DataPanel(JPanel parent) {
    	  setLayout(new BorderLayout());
    	  setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

          splitPane = Tools.createSplitPane(JSplitPane.VERTICAL_SPLIT, pvPanel, rvPanel);
          splitPane.setDividerLocation(400);
    	  add(splitPane, BorderLayout.CENTER);
    	}
    	// Accessors
    	/** Get the Spy Buffer display text  panel 
    	 *  @return The reference to the Spy buffer display text panel
    	 */
    	public AbstractTextPanel getRawDataPanel() {
    	  return rvPanel;
    	}
    	public AbstractTextPanel getPhysicsDataPanel() {
    	  return pvPanel;
    	}
        public void clear() {
          rvPanel.clear();
          pvPanel.clear();
        }
      }
      /** Inner class <CODE>SpyStatusPanel</CODE> which displays the Spy pointer,
       *  Spy Wrap and freeze bit in the upper part of the first page of 
       *  <CODE>InfoPanel</CODE>
       */
      class SpyStatusPanel extends JPanel {
    	/** Reference to the label which displays the value of the pointer */
    	private JLabel pointerLbl;
    	/** Reference to the label which displays the spy wrap bit */
    	private JLabel wrapLbl;
    	/** Reference to the label which displays the spy buffer freeze status */
    	private JLabel freezeLbl;
      
    	// Constructor
    	SpyStatusPanel() {
    	  buildGUI();
    	}
    	/** Build the user interface */
    	protected void buildGUI() {
    	  setLayout(new BorderLayout());    
    	  setBorder(Tools.etchedTitledBorder(" Status "));
      
    	  JPanel panel = new JPanel(new GridLayout(3, 2));
    	  panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
    	  add(panel, BorderLayout.CENTER);
      
    	  JLabel label = Tools.createLabel("Pointer ", null, JLabel.LEFT, Color.black, null);
    	  panel.add(label);
    	    
    	  String ptr = Integer.toHexString(0);
    	  pointerLbl = Tools.createLabel(ptr, null, JLabel.RIGHT, Color.black, null);
    	  pointerLbl.setPreferredSize(new Dimension(60, 20));
    	  pointerLbl.setBorder(BorderFactory.createLoweredBevelBorder());
    	  pointerLbl.setBackground(Color.white);
    	  pointerLbl.setOpaque(true);
    	  panel.add(pointerLbl);
    	    
    	    //  Insets(int top, int left, int bottom, int right) 
    	  label = Tools.createLabel("Wrap ", null, JLabel.LEFT, Color.black, 
    		  BorderFactory.createEmptyBorder(2, 2, 2, 2));
    	  panel.add(label);
    	    
    	  wrapLbl = Tools.createLabel(NOINFO, initIcon, JLabel.LEFT, Color.black,
    		  BorderFactory.createEmptyBorder(2, 2, 2, 2));
    	  panel.add(wrapLbl);
    	    
    	  label = Tools.createLabel("Freeze ", null, JLabel.LEFT, Color.black,
    		  BorderFactory.createEmptyBorder(2, 2, 2, 2));
    	  panel.add(label);
    	    
    	  freezeLbl = Tools.createLabel(NOINFO, initIcon, JLabel.LEFT, Color.black,
    		  BorderFactory.createEmptyBorder(2, 2, 2, 2));
    	  panel.add(freezeLbl);
    	}
    	// Accessors
    	/** Get the label displaying the Spy pointer value
    	 *  @return The reference to the label which displays the Spy pointer value
    	 */
    	public JLabel getPointerLabel() {
    	  return pointerLbl;
    	}
    	/** Get the label displaying the Spy Wrap bit
    	 *  @return The reference to the label which displays Spy wrap bit
    	 */
    	public JLabel getWrapLabel() {
    	  return wrapLbl;
    	}
    	/** Get the label displaying the Spy freeze status bit
    	 *  @return The reference to the label which displays Spy freeze status bit
    	 */
    	public JLabel getFreezeLabel() {
    	  return freezeLbl;
    	}
        public void reset() {
          pointerLbl.setText(Integer.toHexString(0));
          wrapLbl.setText(NOINFO);
          wrapLbl.setIcon(initIcon);
          freezeLbl.setText(NOINFO);
          freezeLbl.setIcon(initIcon);
        } 
      }
      protected SvtBufferData getBufferData(final String crate,
      					    int slot, 
      					    final String board,
      					    final String spy) throws NullPointerException
      {
      	 SvtBufferData data = null;
      	 try {
           SvtDataManager manager = SvtDataManager.getInstance();
      	   data = manager.getBufferData(crate, slot, board, spy);
      	 }
      	 catch (NullPointerException e) {
      	   warn("Cannot retrive SpyBuffer Data for " + crate + ":" + slot + ":" + board + ":" + spy); 
      	 }
      
      	 return data;
      }
      /** Inner class <CODE>GlobalErrorPanel</CODE>. This panel
       *  holds a summary information of the occurrance of EE errors
       *  and a detailed account
       */
      class GlobalErrorPanel extends JPanel {
      	  /** Reference to the label array representing EE errors */
      	private JLabel [] errorLbl;
        private String [] gerrors;     
	  /** User defined data model to be used by the table */
      	private MyTableModel dataModel;
      	  /** The JTable UI component */
      	private JTable table;
	private boolean showGB = false;            

      	// Constructor
      	public GlobalErrorPanel() {
      	  super(true);
      	  setLayout(new BorderLayout());
      	  setBorder(Tools.etchedTitledBorder(" Global Error Flags "));
      	  
      	  JPanel panel = new JPanel(new BorderLayout());
      
          int len = AppConstants.gerrors.length;
          this.gerrors = new String[len];
          System.arraycopy(AppConstants.gerrors, 0, this.gerrors, 0, AppConstants.gerrors.length);

      	  JPanel labelP = new JPanel(new GridLayout(len,1));
      	  errorLbl = new JLabel[len];
      	  for (int i = 0; i < len; i++) {
      	    errorLbl[i] = 
      	      Tools.createLabel(this.gerrors[i], initIcon, JLabel.LEFT, Color.black, 
      		  BorderFactory.createEmptyBorder(2, 1, 3, 1));
      	    labelP.add(errorLbl[i]);
      	  }
      
      	  JPanel tableP = new JPanel(new BorderLayout());
      	  tableP.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
      	  dataModel = new MyTableModel();
      	  table     = new JTable(dataModel);
      
      	  // The first column should be wider
      	  table.getColumnModel().getColumn(0).setPreferredWidth(220);
      
      	  table.setPreferredScrollableViewportSize(new Dimension(500, 136));
      	  JScrollPane scrollPane = new JScrollPane(table);
      	  tableP.add(scrollPane);
      
      	  if (isDebugOn()) {
      	    table.addMouseListener(new MouseAdapter() {
      	      public void mouseClicked(MouseEvent e) {
      		dataModel.printDebugData();
      	      }
      	    });
      	  }
      
      	  panel.add(labelP, BorderLayout.NORTH);
      	  panel.add(tableP, BorderLayout.CENTER);
      
      	  add(panel, BorderLayout.NORTH);
      	}
        public void setGB(boolean gbFlag) {
          showGB = gbFlag;
          if (gbFlag) {
            this.gerrors[4] = "Number of good tracks > 0";           
            this.gerrors[5] = "Number of good tracks > 1";
          }
          else {
            this.gerrors[4] = AppConstants.gerrors[4];
            this.gerrors[5] = AppConstants.gerrors[5];
          }
          for (int i = 4; i < 6; i++) {
            // Change JLabels
            errorLbl[i].setText(this.gerrors[i]);

            // Change Table columns
      	    dataModel.setValueAt(this.gerrors[i], i, 0);
          }
          // Now update
          repaint();
	}
        public boolean showsGB() {
	  return showGB;
	}
        public void reset() {
          for (int i = 0; i < errorLbl.length; i++) {
            errorLbl[i].setIcon(initIcon);

      	    dataModel.setValueAt(new Integer(0), i, 1);
      	    dataModel.setValueAt(new Double(AppConstants.f52Format.sprintf(0.0)), i, 2);

      	    dataModel.setValueAt(new Integer(0), i, 3);
      	    dataModel.setValueAt(new Double(AppConstants.f52Format.sprintf(0.0)), i, 4);
          }
        } 
      	// Accessors 
      	/** Get the reference to the array of EE error tags
      	 *  @return The reference to the array of EE error tags
      	 */
      	public String [] getErrorNames() {
      	  return gerrors;
      	}
      	/** Get the reference to an element of the array of EE error tags by index
      	 *  @param index  Index correxpoding to an array element
      	 *  @return The reference to an element of the array of EE error tags by index
      	 */
      	public String getErrorName(int index)  {
      	  return gerrors[index];
      	}
      	/** Get the reference to the array of EE error labels
      	 *  @return The reference to the array of EE error labels
      	 */
      	public JLabel [] getErrorLabels() {
      	  return errorLbl;
      	}
      	/** Get the reference to an element of the array of EE error labels by index
      	 *  @param index  Index correxpoding to an array element
      	 *  @return The reference to an element of the array of EE error label by index
      	 */
      	public JLabel getErrorLabel(int index) {
      	  return errorLbl[index];
      	}
      	/** Get the table data model
      	 *  @return The reference to the table data model
      	 */
      	public AbstractTableModel getDataModel() {
      	  return dataModel;
      	}
      	  /** Inner class TableModel specific to the application */
      	class MyTableModel extends AbstractTableModel {
      	    /** Application specific column names */
      	  protected final String [] columnNames = 
      	       {"Error Flags", "This Buffer", "% Frac", "Total ", "% Frac"};
      	    /** Application specific data matrix */
      	  protected Object [][] data = {
      	    {gerrors[0], new Integer(0), new Double(0.0), new Integer(0), new Double(0.0)},
      	    {gerrors[1], new Integer(0), new Double(0.0), new Integer(0), new Double(0.0)},
      	    {gerrors[2], new Integer(0), new Double(0.0), new Integer(0), new Double(0.0)}, 
      	    {gerrors[3], new Integer(0), new Double(0.0), new Integer(0), new Double(0.0)}, 
      	    {gerrors[4], new Integer(0), new Double(0.0), new Integer(0), new Double(0.0)}, 
      	    {gerrors[5], new Integer(0), new Double(0.0), new Integer(0), new Double(0.0)}, 
      	    {gerrors[6], new Integer(0), new Double(0.0), new Integer(0), new Double(0.0)}, 
      	    {gerrors[7], new Integer(0), new Double(0.0), new Integer(0), new Double(0.0)} 
      	  };
      	    /** Get number of columns */
      	  public int getColumnCount() {
      	    return columnNames.length;
      	  }
      	    /** Get number of rows */
      	  public int getRowCount() {
      	    return data.length;
      	  }
      	    /** 
      	     * Get name of the ith column 
      	     * @param  col      column number 
      	     */
      	  public String getColumnName(int col) {
      	    return columnNames[col];
      	  }
      	    /** 
      	     * Get value at cell (row,col)
      	     * @param  row      row number 
      	     * @param  col      column number 
      	     */
      	  public Object getValueAt(int row, int col) {
      	    return data[row][col];
      	  }
      	    /** 
      	     * Check if the Table cell referred to by (row,col) is editable 
      	     * @param  row      row number 
      	     * @param  col      column number 
      	     */
      	  public boolean isCellEditable(int row, int col) {
            return ((col < 1) ? false : true);
      	  }
      	    /**
      	     * JTable uses this method to determine the default renderer/
      	     * editor for each cell.  If we didn't implement this method,
      	     * then the last column would contain text ("true"/"false"),
      	     * rather than a check box.
      	     * @param col    Column number
      	     */
      	  public Class getColumnClass(int col) {
      	    return getValueAt(0, col).getClass();
      	  }
      	   /**
      	    * Need to implement this method if your table's data can change.
      	    * @param value   New value at cell (row,col) 
      	    * @param row     Row    Number
      	    * @param col     Column number
      	    */
      	  public void setValueAt(Object value, int row, int col) {
      	    if (isDebugOn()) {
      		System.out.println("Setting value at " + row + "," + col
      			       + " to " + value
      			       + " (an instance of " 
      			       + value.getClass() + ")");
            }
      	    if (data[0][col] instanceof Integer && !(value instanceof Integer)) {
      	       try {
      		 data[row][col] = new Integer(value.toString());
      		 fireTableCellUpdated(row, col);
      	       } 
               catch (NumberFormatException e) {
      		 JOptionPane.showMessageDialog(null,
      		   "The \"" + getColumnName(col)
      		   + "\" column accepts only integer values.", "Alert", JOptionPane.ERROR_MESSAGE);
      	       }
      	    } 
            else {
      	      data[row][col] = value;
      	      fireTableCellUpdated(row, col);
      	    }
      
      	    if (isDebugOn()) {
      	      System.out.println("New value of data:");
      	      printDebugData();
      	    }
      	  }
      	   /** Debug method which prints the contents of the table */
      	  private void printDebugData() {
      	    int numRows = getRowCount();
      	    int numCols = getColumnCount();
      
      	    for (int i = 0; i < numRows; i++) {
      	      System.out.print("    row " + i + ":");
      	      for (int j = 0; j < numCols; j++) {
      		System.out.print("  " + data[i][j]);
      	      }
      	      System.out.println();
      	    }
      	    System.out.println("--------------------------");
      	  }
      	}  
      }
      /** Update display with the latest values of the buffer
       *  @param crate  Name of the crate (e.g b0svt05)
       *  @param slot   Slot number where the board sits in (eg. 8)
       *  @param board  The name of the board under consideration (e.g AMS)
       *  @param spy    The name of the spy buffer under consideration (i.e AMS_HIT_SPY)
       */
      public void updateDisplay(final String crateName) {
        if (!crateName.equals(crate)) return;
      	updateDisplay(crate, slot, board, spy);
      }
      public void updateDisplay(final String crate, int slot, 
                                final String board, final String spy) throws NullPointerException {
      	setSpyName(crate, slot, board, spy);
      	sbPanel.setId(title);
      
      	if (isDebugOn()) System.out.println(crate + " " + board + " " + slot + " " + spy);
      	SvtBufferData bufferData = getBufferData(crate, slot, board, spy);
      	SpyStatusPanel spyP = getSpyPanel();

      	// Update Spy Status Panel
        int ptext = 0;
        Icon wrapIcon    = initIcon;
        String wrapLabel = NOINFO;    
        Icon frIcon      = initIcon;
        String frLabel   = NOINFO;    

        if (bufferData.isValid()) {
          ptext = bufferData.getPointer();
          wrapIcon  = (bufferData.getWrap() != 0) ? AppConstants.redBall : AppConstants.greenBall;
          wrapLabel = (bufferData.getWrap() != 0) ? "Wrapped" : "No Wrap";

          frIcon    = (bufferData.getFreeze() != 0) ? AppConstants.redBall : AppConstants.greenBall;
          frLabel   = (bufferData.getFreeze() != 0) ? "On" : "Off";
        }
        spyP.getPointerLabel().setText(Integer.toHexString(ptext));    
        spyP.getWrapLabel().setIcon(wrapIcon);
        spyP.getWrapLabel().setText(wrapLabel);

        spyP.getFreezeLabel().setIcon(frIcon);
        spyP.getFreezeLabel().setText(frLabel);

      	GlobalErrorPanel gErrorP = getErrorPanel();

        // Now handle Labels for GB
        if (board.equals("GB") && !gErrorP.showsGB())
          gErrorP.setGB(true);
        else if (gErrorP.showsGB()) 
	  gErrorP.setGB(false);

      	// Update End Event Error Window 
      	int [] eCounters = bufferData.getErrorCounters();
      	int [] eTotal    = bufferData.getTotalErrorCounters();
      	int nEvents      = bufferData.getEvent();
      	int nTotEvents   = bufferData.getTotEvent(); 
        if (false) {
          System.out.println("=============== INFO: " + crate + ":" + board + ":" + slot + ":" + spy);
          System.out.println("nEvents:" + nEvents + ", nTotEvents:" + nTotEvents);
          System.out.println("Buffer size = " + bufferData.getSize());     
        }

      	JLabel [] labels = gErrorP.getErrorLabels();
        Icon icon;
        int nError, nErrorTotal;
        double frac, fracTotal;
        for (int i = 0; i < labels.length; i++) {
          if (!bufferData.isValid()) {
            icon   = initIcon;
            nError = nErrorTotal = 0;
            frac   = fracTotal   = 0.0;
          }
          else {
            icon = ((eCounters[i] > 0) ? AppConstants.redBall : AppConstants.greenBall);
            nError = eCounters[i];
      	    frac = (nEvents > 0) ? eCounters[i]*100.0/nEvents : 0.0;

            nErrorTotal = eTotal[i];
            fracTotal = (nTotEvents > 0) ? eTotal[i]*100.0/nTotEvents : 0.0;
          }
          if (isDebugOn()) 
            System.out.println("eCounters:" + nError + ", TotCounters:" + nErrorTotal);

          labels[i].setIcon(icon);

      	  gErrorP.getDataModel().setValueAt(new Integer(nError), i, 1);
      	  gErrorP.getDataModel().setValueAt(new Double(AppConstants.f52Format.sprintf(frac)), i, 2);

      	  gErrorP.getDataModel().setValueAt(new Integer(nErrorTotal), i, 3);
      	  gErrorP.getDataModel().setValueAt(new Double(AppConstants.f52Format.sprintf(fracTotal)), i, 4);
        }
        repaint();

    	// Buffer data
        showData((showEE) ? 1 : 0);
      }
    }
  }
  /** Test the app standalone */
  public static void main (String [] argv) {
    JFrame f = new InfoFrame(true, "Individual Spy Buffer Information");
    f.setSize(f.getPreferredSize());
    f.setVisible(true);
  }
}
