package config;

import java.io.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import config.svt.*;
import config.util.*;

/**
 * 
 * <P>
 *
 * @author   S. Sarkar
 * @version  0.1, May 2001
 *   
 */
public class SpyErrorFrame extends DataFrame {
  private JSplitPane splitPane;
  private SpyErrorPanel spyPanel;
  private InfoFrame spyInfoFrame = null;
  private static final int ONE_SECOND = 1000;
  private static final int TIMER_PERIOD = 20 * ONE_SECOND;  
  private static Dimension medSize = new Dimension(180, 100);
  private static final Dimension buttonSize = new Dimension(40, 22);
  private WindowAdapter winListener = new MyWindowListener();
  private JCheckBoxMenuItem cycleCB = new JCheckBoxMenuItem("Cycle thru crates", true);

  public SpyErrorFrame(boolean standAlone) {
    super(standAlone, "Spy Error Status", true, true, -1);
    buildGUI();

    // Set Help File
    String filename = Tools.getEnv("SVTMON_DIR")+"/help/a_SpyErrorFrame.html";
    setHelpFile(filename, "About Spy Buffer Errors", new Dimension(500, 200));
  }
  protected void buildGUI() {
    addToolBar();

    JPanel panel = new JPanel(new BorderLayout());

    spyPanel = new SpyErrorPanel(getStatusBar());
    spyPanel.setPreferredSize(new Dimension(850, 370));

    JPanel textPanel = getTextPanel();
    textPanel.setBorder(Tools.etchedTitledBorder(" Message Logger "));
    textPanel.setPreferredSize(medSize);

    splitPane = Tools.createSplitPane(JSplitPane.VERTICAL_SPLIT, spyPanel, textPanel);
    panel.add(splitPane, BorderLayout.CENTER);
    splitPane.setDividerLocation(370);

    updateOptionsMenu(getJMenuBar());

    getContentPane().add(panel, BorderLayout.CENTER);
    addStatusBar();
  }
  private void updateOptionsMenu(JMenuBar menuBar) {
    JMenu menu = menuBar.getMenu(1);

    cycleCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
        if (cycleCB.isSelected()) {
          spyPanel.startTimer();
          warn("INFO. Automatic traversal of crates enabled", Color.green);
        }
        else {
          spyPanel.stopTimer();
          warn("INFO. Automatic traversal of crates disabled", Color.red);
        }
      }
    });
    menu.add(cycleCB);
  }
  protected void updateGUI(final String crateName) {
    spyPanel.updateGUI(crateName);
  }
  protected SpyErrorPanel getErrorPanel() {
    return spyPanel;    
  }
  protected void addCrate(final SvtCrateData crateData) {
    spyPanel.addCrate(crateData);
  }
  public void showSpyInfo(String cpuName, int slot, String board, String spy) {
    if (spyInfoFrame == null) {
      spyInfoFrame = new InfoFrame(false, "Individual Spy Buffer Information");
      spyInfoFrame.addWindowListener(winListener);
      spyInfoFrame.setSize(900, 700);
    }
    spyInfoFrame.showData(cpuName, slot, board, spy);
    spyInfoFrame.setVisible(true);
  }
  public class MyWindowListener extends WindowAdapter implements WindowListener {
    public MyWindowListener() {}
    public void windowClosed(WindowEvent e) {
      if (isDebugOn()) System.out.println(e.paramString());
      Window obj = e.getWindow();
      if (isDebugOn()) System.out.println(((JFrame)obj).getTitle());
      if (obj instanceof InfoFrame) {
        spyInfoFrame.removeWindowListener(winListener);
        spyInfoFrame = null;
      }
    }
  }
  public class SpyErrorPanel extends JPanel  {
    private JPanel tabPanel;
    private JTabbedPane tabs;
    private SvtSingleCratePanel [] cratePanel = new SvtSingleCratePanel[AppConstants.nCrates];
  
    private Vector<SvtSingleCratePanel> paneV = new Vector<SvtSingleCratePanel>(AppConstants.nCrates);
  
    private StatusBar statusBar;
    private javax.swing.Timer timer;

    /* Constructor */
    public SpyErrorPanel(StatusBar statusBar) {
      this.statusBar = statusBar;
      setTimer();
      buildGUI();
    }
    protected void buildGUI() {
      setLayout(new BorderLayout());
  
      tabs = new JTabbedPane();
      tabs.setTabPlacement(SwingConstants.BOTTOM);
  
      // Create the tabs Add them in a vector 
      // Add the panes and specify which pane is displayed first 

      addCrates();
  
      tabPanel = new JPanel();
      tabPanel.setLayout(new BorderLayout());
      tabPanel.setBorder(BorderFactory.createLoweredBevelBorder()); 
      tabPanel.add(tabs, BorderLayout.CENTER);
  
      add(tabPanel, BorderLayout.CENTER);

      tabs.addChangeListener(new ChangeListener() {
  	public void stateChanged(ChangeEvent ev) {
          setStatusText();
  	}
      });
    }
    public void setStatusText() {
      SvtSingleCratePanel comp = (SvtSingleCratePanel) tabs.getSelectedComponent();
      statusBar.setText("Crate " + comp.getCrate() + " selected");
    }
    public void setTimer() {
      timer = new javax.swing.Timer(TIMER_PERIOD, new ActionListener() { 
        public void actionPerformed(ActionEvent event) { 
          showTab();
        } 
      });
      timer.setRepeats(true);
      timer.setCoalesce(false);
      timer.start();
    }
    public void startTimer() {
      timer.restart();
    }
    public void stopTimer() {
      if (timer.isRunning()) timer.stop();
    }
    public void showTab() {
      int indx = tabs.getSelectedIndex();
      if (indx == AppConstants.nCrates-1) indx = 0;
      else indx++;

      if (indx < tabs.getTabCount()) {
        tabs.setSelectedIndex(indx);
        setStatusText();
      }
    }
    protected void addCrates() {
      SvtCrateMap map = SvtCrateMap.getInstance();
      if (map == null) return;

      for (int i = 0; i < AppConstants.nCrates; i++) {
  	if (!map.isCrateReady(i)) continue;
  	String name = new String(AppConstants.SVT_CRATE_PREFIX + i);
  	SvtCrateData crateData = map.getCrateData(name);
  	if (crateData == null) continue;
        
        addCrate(crateData);
      }
    }
    protected void addCrate(final SvtCrateData crateData) {
      if (tabs.indexOfTab(crateData.getName()) != -1) return;

      int indx = Tools.getCrateIndex(crateData.getName());
      if (indx < 0 || indx >= AppConstants.nCrates) return;
 
      cratePanel[indx] = new SvtSingleCratePanel(this, statusBar, crateData);
      paneV.addElement(cratePanel[indx]);
      tabs.addTab(crateData.getName(), null, cratePanel[indx]);
    }
    protected void updateGUI(final String crateName) {
      if (!Tools.isCrateNameValid(crateName)) return;

      // Update selected panel
      int index = Tools.getCrateIndex(crateName);
      if (cratePanel[index] == null) {
        warn("CratePanel handle null for index = " + index + ", returning ...", Color.red);
        return;
      }

      SvtCrateData crateData = getCrateData(crateName);
      cratePanel[index].updatePanel(crateData);

      repaint();
      
      // Update the SpyInfo window which is being displayed
      if (spyInfoFrame != null) spyInfoFrame.updateGUI(crateData);
    }
    protected SvtCrateData getCrateData(final String crateName) {
      SvtCrateMap map = SvtCrateMap.getInstance();
      return map.getCrateData(crateName);
    }
    protected JTabbedPane getTab() {
      return tabs;
    }
    public void selectTab(final String name) {
      tabs.setSelectedIndex(tabs.indexOfTab(name));
    }
    public class SvtSingleCratePanel extends JPanel  {
      private JPanel parent;
      private String crateName;
      private StatusBar statusBar;
      private BoardSpyPanel [] boardPanels; 
      
      /* Constructor */
      public SvtSingleCratePanel(JPanel parent, 
  				 final StatusBar statusBar, 
                                 final SvtCrateData crateData) 
      {
  	this.parent    = parent;
  	this.crateName = crateData.getName();
  	this.statusBar = statusBar;
      
  	buildGUI(crateData);
      }
      protected void buildGUI(final SvtCrateData crateData) {
  	setLayout(new BorderLayout());
  	setBorder(Tools.etchedTitledBorder(" " + crateName + " Board Configuration "));
       
  	JPanel lPanel = new JPanel(new FlowLayout());
  	JPanel rPanel = new JPanel(new FlowLayout());
  
  	SvtBoardData [] boardData = crateData.getBoardData();
  	int len = boardData.length;
  
  	// Count number of panels to be created 
  	int index = 0;
  	for (int i = 0; i < len; i++) {
  	  if (isValidBoard(boardData[i].getType())) index++;
  	}
  	boardPanels = new BoardSpyPanel[index];
  
  	 // Now create the panels 
  	index = 0;
  	for (int i = 0; i < len; i++) {
  	  if (!isValidBoard(boardData[i].getType())) continue;

  	  SvtBufferData [] bufferData = boardData[i].getBufferData(); 
  	  String [] spys = new String[bufferData.length];
  	  for (int j = 0; j < spys.length; j++) 
  	    spys[j] = bufferData[j].getType();
  	    
  	  boardPanels[index] = new BoardSpyPanel(crateData.getName(), 
  						 boardData[i].getSlot(), 
  						 boardData[i].getType(),
  						 spys);
  	  if (boardData[i].getSlot() > 12) rPanel.add(boardPanels[index]); 
  	  else                             lPanel.add(boardPanels[index]); 
  
  	  index++;
  	}      
  	JSplitPane splitPane = Tools.createSplitPane(JSplitPane.HORIZONTAL_SPLIT,lPanel,rPanel);
  	
  	Dimension minSize = new Dimension(250, 200);
  	lPanel.setMinimumSize(minSize);
  	rPanel.setMinimumSize(minSize);
  	
  	splitPane.setPreferredSize(new Dimension(800, 300));
        splitPane.setDividerLocation(390);
  	add(splitPane, BorderLayout.CENTER);
      }
      public boolean isValidBoard(final String bname) {
  	if (bname.equals("SC") ||
  	    bname.equals("AMB") ||
  	    bname.equals("GB")) return false;
        return true;
      }
      protected BoardSpyPanel [] getBoardPanels() {
  	return boardPanels;
      }
      protected BoardSpyPanel getBoardPanel(int slot, final String board) 
  	  throws NullPointerException {
  	for (int i = 0; i < boardPanels.length; i++) {
  	  if (slot == boardPanels[i].getSlot() &&  board.equals(boardPanels[i].getName())) 
            return boardPanels[i];
  	}
        return null;
      }
      protected void updatePanel(SvtCrateData crateData) {
  	int i, j, nBuffers;
  
  	if (crateData == null) return;
  	BoardSpyPanel [] panels = getBoardPanels();
        int index = 0;
  	try {
  	  for (i = 0; i < crateData.getNBoards(); i++) {
  	    SvtBoardData boardData = crateData.getBoardData(i); 
   	    if (!isValidBoard(boardData.getType())) continue;

  	    nBuffers = boardData.getNBuffers();
  	    if (nBuffers == 0) continue;
  	    boolean [] errorOn  = new boolean[nBuffers];
            boolean [] validBuf = new boolean[nBuffers];

  	    for (j = 0; j < nBuffers; j++) {
  	      SvtBufferData bufferData = boardData.getBufferData(j);
              validBuf[j] = bufferData.isValid();
  	      errorOn[j]  = (bufferData.getEndEventError() == 1) ? true : false; 
  	    }
  	    panels[index].updatePanel(errorOn, validBuf);
            index++;
  	  }
  	}
  	catch (NullPointerException ex) {
  	  System.out.println("updatePanel() -> Cannot retrieve # of board");
  	  ex.printStackTrace();
  	} 
      }
      public String getCrate() {
  	return crateName;
      }
      public StatusBar getStatusBar() {
  	return statusBar;
      }
      public JPanel getParentPanel() {
  	return parent;
      }
    }
    class BoardSpyPanel extends JPanel implements ActionListener {
      protected String crate;
      protected int slot;
      protected String board;
      protected String [] spys;
  
      protected JButton [] inputB;
      protected JButton outB;
      protected String  tipText;
  
      BoardSpyPanel(final String crate, int slot, 
  		    final String board, final String [] spys) {
  	this.crate = crate;
  	this.slot  = slot;
  	this.board = board;
  	this.spys  = new String[spys.length];
  	System.arraycopy(spys, 0, this.spys, 0, spys.length);
  	buildGUI();
      }
      protected void buildGUI() {
  	if (isDebugOn()) {
  	  System.out.println("crate/board/slot = " + crate + "/" + board + "/" + slot);
  	  System.out.println("nspy = " + spys.length);
  	  for (int i = 0; i < spys.length; i++) 
  	    System.out.println(spys[i]);
  	}        
  	setLayout(new BorderLayout());
  	setBorder(Tools.emptyTitledBorder(board+"-"+slot));
  	int last = spys.length-1;
  
  	JPanel po = new JPanel(new BorderLayout());
  	tipText = "Output Spy Buffer " + spys[last]
  				       + " for crate " + crate + " slot " + slot;
  	outB = new SpyButton("O", AppConstants.grayBall, statusBar, tipText);
  	outB.addActionListener(this);
  	outB.setActionCommand(spys[last]);
  	po.add(outB, BorderLayout.CENTER);
  
  	inputB = new JButton[spys.length-1];
  	JPanel pi = new JPanel(new GridLayout(inputB.length, 1));
  	String tag = "";
  	for (int i = 0; i < inputB.length; i++) {
  	  tipText = "Input Spy Buffer " + spys[i] 
  					+ " for crate " + crate + " slot " + slot;
  	  int pos = spys[i].indexOf("_");
  	  tag = (board.equals("HF")) ? spys[i].substring(spys[i].length()-1)
  				     : spys[i].substring(pos+1,pos+2);
  	  inputB[i] = new SpyButton(tag, AppConstants.grayBall, statusBar, tipText);
  	  inputB[i].addActionListener(this);
  	  inputB[i].setActionCommand(spys[i]);
  	  pi.add(inputB[i]);
  	}
  
  	add(po, BorderLayout.NORTH);
  	add(pi, BorderLayout.CENTER);
      }
      public void actionPerformed(ActionEvent ev) { 
  	String command;
  	Component source = (Component) ev.getSource();
  	if (source instanceof JButton) {
  	  command = ((JButton) source).getActionCommand();
  	  showSpyInfo(crate, slot, board, command);
  	}
      }
      public int getSlot() {
  	return slot;
      }
      public String getName() {
  	return board;
      }
      public String [] getSpys() {
  	return spys;
      }
      public String getSpy(int index) {
  	return spys[index];
      }
      protected JButton [] getInputButtons() {
  	return inputB;
      }
      protected JButton getInputButton(int index)  {
  	return inputB[index];
      }
      protected JButton getOutputButton() {
  	return outB;
      }
      public void updatePanel(final boolean [] errorOn, final boolean[] validBuf) {
        JButton button;
        int len = Math.min(errorOn.length, spys.length);
  	for (int i = 0; i < len; i++) {
          button = (i < len-1) ? inputB[i] : outB;
  	  if (validBuf[i]) button.setIcon((errorOn[i]) ? AppConstants.redBall : AppConstants.greenBall);
        }
      }
    }
    class SpyButton extends JButton implements MouseListener {
      private StatusBar statusBar;
      public SpyButton(String name, Icon icon) {
  	this(name, icon, null, "");
      }
      public SpyButton(String name, Icon icon, StatusBar statusBar, String tip) {
  	super(name, icon);
  	this.statusBar = statusBar;
  	setMargin(new Insets(0, 0, 0, 0));
  	setPreferredSize(buttonSize);
  	setToolTipText(tip);
  	addMouseListener(this);
      }
      public void mousePressed(MouseEvent e) { }
      public void mouseReleased(MouseEvent e) {}
      public void mouseClicked(MouseEvent e) {}
      public void mouseEntered(MouseEvent e) {
  	if (statusBar != null) statusBar.setText(getToolTipText());
      }
      public void mouseExited(MouseEvent e) {
  	if (statusBar != null) statusBar.setText(" ");
      }
    }
  }
  public static void main(String [] argv) {
    JFrame f = new SpyErrorFrame(true);
    f.setSize(f.getPreferredSize());
    f.setVisible(true);
  }
}
