package config;

import java.io.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import config.util.*;
import config.svt.*;

public class SvtCratesFrame extends DataFrame {
  private static final int ONE_SECOND = 1000; //ms
  private static final int limit = 60 * ONE_SECOND;  
  private static final String [] buttonNames = {
    "EndEvent",       "Simulation",    "Output Hold", 
    "Test/Run Mode",  "CDF/SVT Error", "Board Error Register"
  };
  public static final HashMap<String, String[]> berrMap = new HashMap<String, String[]>();
  static {
    berrMap.put("HF",   AppConstants.boardErrorRegNames[0]);
    berrMap.put("MRG",  AppConstants.boardErrorRegNames[1]);
    berrMap.put("AMS",  AppConstants.boardErrorRegNames[2]);
    berrMap.put("HB",   AppConstants.boardErrorRegNames[3]);
    berrMap.put("TF",   AppConstants.boardErrorRegNames[4]);
    berrMap.put("XTFA", AppConstants.boardErrorRegNames[5]);
  }
    /** Insert blank spaces */
  private static final String spaces = "    ";
    /** Position of the various crates, 6 Tracker, Fitter, Fanout 
     *  respectively in the rack 
     */
  private static final int nSlots = 18;
  private static final int [] pos = {0, 7, 6, 5, 1, 2, 3, 4};
    /** Width of SVT board graphics */
  private static final int width  = 10;
    /** Height of SVT board graphics */
  private static final int height = 135;
    /** Preferred size of the Board Panel */
  private static final Dimension preferredSize = new Dimension(width+2, height+2);
  private static final Dimension totalSize     = new Dimension(18*(width+2), height+2);
    /** Width of the image boxes which contain the LEDs */
  private static final int imageBoxWidth  = (int) (0.8*width);
    /** Height of the image boxes which contain the LEDs */
  private static final int imageBoxHeight = (int) (0.3*height);
    /** CPU, Tracer and Spy Control board colors */
  private static final Color [] 
         buttonColors = {
            new Color(238, 201, 0),     // CPU
            new Color(255, 250, 205),   // Tracer
            new Color(144, 238, 144)    // SC
         };
  
    // Now the individual board color 
    /** Hit Finder Board Color */
  private static final Color red2    = new Color(205, 78, 38);
  private static final Color green2  = new Color(144, 238, 144);
  private static final Color yellow2 = new Color(255, 250, 205);

  private static final Color hfColor  = new Color(155, 205, 155);
    /** Merger Board Color */
  private static final Color mrgColor = new Color(176, 226, 255);
    /** AM Sequencer Board Color */
  private static final Color amsColor = new Color(205, 180, 205);
    /** AM Board Color */
  private static final Color ambColor = new Color(255,192,203);
    /** Hit Buffer Board Color */
  private static final Color hbColor  = Color.pink;
    /** Track Fitter Board Color */
  private static final Color tfColor  = new Color(70, 130, 180);
    /** XTF(A/B/C) Board Color */
  private static final Color xtfColor = new Color(205, 85, 85);
    /** We need another map for board colors */
    private static final HashMap<String, Color> bColorMap = new HashMap<String, Color>();
  static {
    bColorMap.put("HF",   hfColor);
    bColorMap.put("MRG",  mrgColor);
    bColorMap.put("AMB",  ambColor);
    bColorMap.put("AMS",  amsColor);
    bColorMap.put("HB",   hbColor);
    bColorMap.put("TF",   tfColor);
    bColorMap.put("XTFA", xtfColor);
    bColorMap.put("XTFC", xtfColor);
  }
  
    /** Offset due to the first 3 common boards, namely the CPU, Tracer, SC 
     *  The spy control board someday may go to the other group
     */
  private static final int OFFSET = 4;
    /** Number of rows of crates in the rack */
  private static final int ROWS = 2;
    /** Number of columns of crates in a row */
  private static final int COLUMNS = 4;

  private SvtCratesPanel cratesPanel;  
  private Action resetAction  = new ResetAction();
  private Action legendAction = new LegendAction();
  private Action colorAction  = new BoardColorAction();
  private JCheckBoxMenuItem showTModeCB;
  private JCheckBoxMenuItem showHoldCB;
  private JCheckBoxMenuItem showIndErrorCB;
  private JSplitPane splitPane; 
  private static Dimension medSize   = new Dimension(180, 50);
  private static Dimension largeSize = new Dimension(1000, 600);

  private SvtBoardErrorFrame boardErrorFrame = null;
  private SpyErrorFrame spyErrorFrame        = null;
  private WindowAdapter winListener = new MyWindowListener();

  public SvtCratesFrame(boolean standAlone) {
    super(standAlone, "Svt Crate Status Display", false, true, -1);
    buildGUI();

    String filename = Tools.getEnv("SVTMON_DIR")+"/help/a_SvtCratesFrame.html";
    setHelpFile(filename, "About SVT Crates Configuration", new Dimension(500, 500));
  }
  protected void buildGUI() {
    updateOptionMenu(getJMenuBar());
    addHelpMenu();
    updateHelpMenu(getJMenuBar());

    addToolElement(Box.createHorizontalGlue());
    addToolElement(getLegendAction(), 
        "Show meaning of the button of the panel and the error color codes...", -1);
    addToolElement(getBoardColorAction(), "Explain board panel colors  ...", -1);
    addHelpInToolBar();

    addToolBar();

    JPanel panel = new JPanel(new BorderLayout());

    cratesPanel = new SvtCratesPanel(this);

    TextPanel textPanel = getTextPanel();
    textPanel.setBorder(Tools.etchedTitledBorder(" Message Logger "));

    splitPane = Tools.createSplitPane(JSplitPane.VERTICAL_SPLIT, cratesPanel, textPanel);
    panel.add(splitPane, BorderLayout.CENTER);

    getContentPane().add(panel, BorderLayout.CENTER);
    addStatusBar();

    setResizable(false);
  }
  class MyWindowListener extends WindowAdapter implements WindowListener {
    public MyWindowListener() {}
    public void windowClosed(WindowEvent e) {
      if (isDebugOn()) System.out.println(e.paramString());
      Window obj = e.getWindow();
      if (isDebugOn()) System.out.println(((JFrame)obj).getTitle());
      if (obj instanceof SvtBoardErrorFrame) {
        boardErrorFrame.removeWindowListener(winListener);
        boardErrorFrame = null;
      }
      else if (obj instanceof SpyErrorFrame) {
        spyErrorFrame.removeWindowListener(winListener);
        spyErrorFrame = null;
      }
    }
  }
  private void updateHelpMenu(JMenuBar menuBar) {
    JMenu menu = menuBar.getMenu(menuBar.getMenuCount()-1);

    JMenuItem item = menu.add(getLegendAction());
    item.setMnemonic('l');
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_MASK));
    menu.add(item);

    item = menu.add(getBoardColorAction());
    item.setMnemonic('r');
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_MASK));
    menu.add(item);
  }
  protected void updateGUI(final SvtCrateData crateData) {
    // Update crate panel
    cratesPanel.updateGUI(crateData);

    // Add/Update informations for this crate in BoardErrorFrame
    if (boardErrorFrame != null) boardErrorFrame.updateGUI(crateData);
  
    // Finally Update Spy Error frame and all its daughter
    if (spyErrorFrame != null) spyErrorFrame.updateGUI(crateData.getName());
  }
  protected void updateBufferStatus(final String crateName) {
    if (spyErrorFrame != null) spyErrorFrame.updateGUI(crateName);
  }
   /** 
    * Create menubar and add the menus and meun items within 
    */
  private void updateOptionMenu(JMenuBar menuBar) {
    JMenuItem item;
    JMenu menu = menuBar.getMenu(1);
  
    item = menu.add(getResetAction());
    item.setMnemonic('t');
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_MASK));
    menu.add(item);
  
    menu.addSeparator();

    showIndErrorCB = new JCheckBoxMenuItem("Show Errors on Separate Windows", false);
    menu.add(showIndErrorCB);

    showTModeCB = new JCheckBoxMenuItem("Show TMode Warning", false);
    menu.add(showTModeCB);
  
    showHoldCB = new JCheckBoxMenuItem("Show Output Hold Warning", false);
    menu.add(showHoldCB);
  }
  protected Action getResetAction()   {return resetAction;}
  protected Action getLegendAction()   {return legendAction;}
  protected Action getBoardColorAction() {return colorAction;}

  /* Reset error action */
  class ResetAction extends AbstractAction {
    public ResetAction() { 
      super("Reset Errors", null); 
    }
    public void actionPerformed(ActionEvent e) {
      cratesPanel.resetErrors();
    }
  }

  /* Legend action */
  class LegendAction extends AbstractAction {
    public LegendAction() { 
      super("Button Legends", new ImageIcon(AppConstants.iconDir+"mini-board-button.png")); 
    }
    public void actionPerformed(ActionEvent e) {
      JLabel label = new JLabel(new ImageIcon(AppConstants.iconDir+"board-panel.png"));
      label.setBorder(BorderFactory.createLoweredBevelBorder());
      JOptionPane.showMessageDialog(SvtCratesFrame.this, 
         label, 
         "Explanation of Board Panel Buttons", 
         JOptionPane.PLAIN_MESSAGE
      );
    }
  }

  /* BoardColor action */
  class BoardColorAction extends AbstractAction {
    public BoardColorAction() { 
      super("Board Panel color", new ImageIcon(AppConstants.iconDir+"mini-board-panel.png")); 
    }
    public void actionPerformed(ActionEvent e) {
      JLabel label = new JLabel(new ImageIcon(AppConstants.iconDir+"board-color.png"));
      label.setBorder(BorderFactory.createLoweredBevelBorder());
      JOptionPane.showMessageDialog(SvtCratesFrame.this, 
         label,
         "Explanation of Colors of different Board Panels", 
         JOptionPane.PLAIN_MESSAGE
      );
    }
  }
  public boolean showIndError() {
    return showIndErrorCB.isSelected();
  }
  public boolean showTMode() {
    return showTModeCB.isSelected();
  }
  public boolean showHold() {
    return showHoldCB.isSelected();
  }
  protected SvtCratesPanel getCratesPanel() {
    return cratesPanel;
  }
  /**
   * <P>
   * Reproduces the SVT crate configuration. Standard colors for
   * the board as used in the official place have been used. These
   * way, without labelling the boards it will be possible to identify
   * them easily. The crate CPU and the Spy Control buttons are active
   * while the tracer button is not. Clicking on the CPU button opens
   * individual crate Spy Buffer status window. The boards with Spy buffers
   * have several LEDs. The LEDs act as active buttons as well and clicking
   * on them either opens new windows or display messages in the 
   * <I>message logger</I> area. </P>
   *
   * <P>
   * This window should summarise all the relevant informations about the
   * functioning of SVT and hence may be open all through. However, we have
   * provided a toggle button in the main applicaition which will hide/show 
   * this window as desired.</P>
   *
   * <P>
   * The LEDs on each board represent the following
   * <UL>
   *   <LI>TopPanel</LI>
   *   <UL>
   *     <LI>EE Event related error indicator</LI>
   *     <LI>Spy Bufer related error indicator</LI>
   *     <LI>Hold Error indicator </LI>
   *   </UL>
   *   <LI>Bottom Panel</LI>
   *   <UL>
   *     <LI>Run/Test mode indicator</LI>
   *     <LI>CDF/SVT error indicator</LI>
   *     <LI>Board error register indicator</LI>
   *   </UL>
   * </UL>
   * </P>
   *
   * @author S. Sarkar with ideas from L. Zanello and M. Rescigno
   * @version 0.2, 04/08/2001, Track fitters moved to correct positions
   * @version 0.3, 05/02/2001, All the buttons respond sensibly now
   * @version 0.5, 05/20/2001, Runs off real data from the crates now
   * @version 0.6  11/15/2001, Trimmed a lot following automatic creation of panels
   */
      /** The SVT Tracker crate panel which consists of the 6 crates (b0svt00 ... b0svt05)
       *  for the 12 SVT Phi Slices, the so-called track fitter crate (b0svt06) and the Fanout 
       *  (XTF) crate (b0svt07). The positions of these crates in the mesh
       *  are given in the global array <CODE>pos</CODE>. 
       */
  public class SvtCratesPanel extends JPanel {
      /** Reference to the parent window passed as an argument to the constructor */
    private SvtCratesFrame frame;
    private TestModeFrame tModeFrame = null;
      /** An array of <CODE>AppConstants.nCrates</CODE> Crate Panels */
    private CratePanel [] cratePanels;

    private DataFrame textInfo;
    private SvtDataManager manager = SvtDataManager.getInstance();
      /** Instantiate SvtCratesPanel object here
       *  @param frame The container Frame 
       */
    public SvtCratesPanel(SvtCratesFrame frame) {
      this.frame = frame;
      buildGUI();
    } 
      /** Setup user interface */
    protected void buildGUI() {
      setLayout(new GridLayout(ROWS, COLUMNS));
      setBorder(Tools.etchedTitledBorder(" SVT Crate Status "));
      setName("SVT Crates Configuration Panel");
  
      cratePanels     = new CratePanel[AppConstants.nCrates];    // Create statically
      SvtCrateMap map = SvtCrateMap.getInstance();  // Access the dynamically updated crate map
  
      // Create all the crate panels at once. The ones with no board data
      // associated should have black panels
      for (int i = 0; i < AppConstants.nCrates; i++) {
  	SvtCrateData crateData = map.getCrateData(AppConstants.cpuNameString[i]);
  	cratePanels[i] = new CratePanel(AppConstants.cpuNameString[i], AppConstants.crateIdString[i], 
  			                AppConstants.crateNameString[i], crateData);
      }
  
      // Now place them correctly
      for (int i = 0; i < AppConstants.nCrates; i++) {
  	int j = pos[i];
  	add(cratePanels[j], i);
  	if (map.isCrateReady(j)) {
  	  map.setCrateShowing(j, true);
  	  setDetails(j);
  	}
      }
    }
    // ** The argument is useless at present. Is there a solution?
    // Explore
    protected void updateGUI(final SvtCrateData crateDataX) {
      SvtCrateMap map = SvtCrateMap.getInstance();
  
      String crate = crateDataX.getName();
      if (!map.isCrateReady(crate)) return;
  
      SvtCrateData crateData = map.getCrateData(crate);
      int index = Tools.getCrateIndex(crate);
      if (!map.isCrateShowing(index)) {
  	cratePanels[index].addBoards(crateData);
  	map.setCrateShowing(index, true);
  	setDetails(index);
      }
      cratePanels[index].update(crateData);
      cratePanels[index].startTimer();
      repaint();
    }
    protected void setDetails(final String crate) {
      setDetails(Tools.getCrateIndex(crate));
    }
    protected void setDetails(int index) {
      frame.warn(AppConstants.cpuNameString[index] + ": added at " + new Date(), Color.green);
      cratePanels[index].getButtonPanel().getCpuButton().setEnabled(true);
      cratePanels[index].getLabelPanel().getButton().setEnabled(true);
    } 
      /** String representeation of the object is returned. The string packs 
       *  all the data present so that the state of the object can be 
       *  displayed in a convenient way.
       *  @return The String representeation of the object
       */
    public String toString() {
      StringBuilder buf = new StringBuilder(AppConstants.SMALL_BUFFER_SIZE);
      buf.insert(0,"The SVT crate configuration\n");
      buf.append("---------------------------\n");
      for (int i = 0; i < AppConstants.cpuNameString.length; i++) {
  	buf.append(AppConstants.cpuNameString[i]).append(spaces);
  	buf.append(AppConstants.crateIdString[i]).append(spaces);
  	buf.append(AppConstants.crateNameString[i]).append("\n");
      }
      return buf.toString();
    }
      /** Get the Crate panel array
       *  @return Reference to the CratePanel array 
       */
    protected CratePanel [] getCratePanels () {
      return cratePanels;
    }
      /** Get reference to a crate panel element
       *  @param i   Array index
       *  @return Reference to the individual crate (CratePanel array element) 
       */
    protected CratePanel getCratePanel(int i) throws ArrayIndexOutOfBoundsException {
      return cratePanels[i];
    }
      /** Get reference to a crate panel element by crate name
       *  @param crate Crate name
       *  @return Reference to the individual CratePanel array element 
       */
    protected CratePanel getCratePanel(final String crate) {
      return getCratePanel(Tools.getCrateIndex(crate));
    }
      /** Get the reference to any button spcified by crate/board/slot/name
       *  @param crate   Crate name, ie b0svt0[0..7]
       *  @param board   Name of the SVT board
       *  @param slot    Slot number where the board is placed in the crate
       *  @param name    Name of the Button 
       *  @return Reference to the button spcified by crate/board/slot/name
       */
    protected JButton getButton(final String crate, final String board, 
  				int slot, final String name) 
  		throws NullPointerException
    {
      return getCratePanel(crate).getBoardPanel(board, slot).getButton(name);
    }
      /** Update a single crate panel
       *  @param crateData Reference to the crate data needed to update the panel
       */
    protected void update(final SvtCrateData crateData) 
  	      throws ArrayIndexOutOfBoundsException, NullPointerException 
    {
      getCratePanel(Tools.getCrateIndex(crateData.getName())).update(crateData);
    }
   
      /** Reset Errors manually */
    protected void resetErrors() {
      for (int i = 0; i < AppConstants.nCrates; i++) {
  	  
      }
    }
    class TestModeFrame extends DataFrame {
      private JPanel panel;
      TestModeFrame() {
        super(false, "Boards in Test Mode", true, false, -1);
        addToolBar();
        panel = new JPanel(new BorderLayout());
        getContentPane().add(panel, BorderLayout.CENTER);
        addStatusBar();
      }
      protected void updateDisplay(final SvtCrateData crateData) {
        panel.removeAll();        
        int nBoards = crateData.getNBoards();
        JPanel p = new JPanel(new GridLayout(nBoards, 1));
        p.setBorder(Tools.etchedTitledBorder(" Crate " + crateData.getName() + " "));
        panel.add(p, BorderLayout.NORTH);
        JLabel [] labels = new JLabel[nBoards];
    
        int nOcc = 0;
        SvtBoardData [] bData = crateData.getBoardData();
        for (int i = 0; i < nBoards; i++) {
          int tmode = bData[i].getTMode();
          if (tmode > 0) nOcc++;
          labels[i] = Tools.createLabel("Board " + bData[i].getType() + 
      				  " Slot " + bData[i].getSlot(), 
      	     (tmode == 0) ? AppConstants.greenBall : AppConstants.redBall, JLabel.LEFT, Color.black, 
      	     BorderFactory.createEmptyBorder(3, 3, 3, 3));
          labels[i].setPreferredSize(new Dimension(250, 24));
          p.add(labels[i]);
        }
        panel.add(p);
        panel.repaint();
      }
    }
      /** Inner class which constructs one crate panel completely */
    class CratePanel extends JPanel {
  	/** Crate identification string */
      protected String crateId;    
  	/** Crate name */
      protected String crateName;    
  	/** Crate CPU name */
      protected String cpuName;
  	/** Panel which hold the name and identification label of the crate */
      protected LabelPanel labelPanel;
  	/** Panel which contains CPU, Tracer, SC boards as buttons */
      protected ButtonPanel buttonPanel;
      protected JPanel  [] boardPanels = new JPanel[nSlots];
      protected boolean [] validSlots  = new boolean[nSlots];
      protected JPanel bPanel;
      private javax.swing.Timer timer;
  	/** 
  	 *  @param cpuName    Name of the crate (CPU) i.e b0svt05
  	 *  @param crateId    Identification string of the crates 
  	 *  @param crateName  Name of the crates on the basis of its operation
  	 */
      CratePanel(final String cpuName, final String crateId, 
  		 final String crateName, final SvtCrateData crateData) {
  	this.cpuName   = cpuName;
  	this.crateId   = crateId;
  	this.crateName = crateName;      

        setTimer();

  	buildGUI(crateData);
      }
      public void setTimer() {
        timer = new javax.swing.Timer(limit, new ActionListener() { 
          public void actionPerformed(ActionEvent event) { 
            setUpdated(false);
          } 
        });
        timer.setRepeats(false);
        timer.setCoalesce(false);
      }
      public void startTimer() {
        setUpdated(true);
        timer.restart();
      }
      public void setUpdated(boolean updated) {
        if (isDebugOn() )System.out.println("updated = " + updated);
        labelPanel.label.setText((updated) ? "Online" : "Offline");
        labelPanel.label.setBackground((updated) ? green2 : red2);
      }
  	/** Create the user interface */
      protected void buildGUI(final SvtCrateData crateData) {
  	setLayout(new BorderLayout());
  	setBorder(BorderFactory.createEmptyBorder(1, 10, 20, 10));
  	setName(crateName+" Crate Panel");
  
  	// The crate name label
  	labelPanel  = new LabelPanel(crateId, cpuName);
  
  	// Crate cpu, Tracer and SC Buttons
  	buttonPanel = new ButtonPanel(cpuName);
  
  	JPanel panel1 = new JPanel(new FlowLayout());
  	panel1.add(labelPanel);
  	
  	JPanel panel2 = new JPanel(new BorderLayout());
  	panel2.add(buttonPanel, BorderLayout.WEST);
  
  	add(panel1, BorderLayout.NORTH);      
  	add(panel2, BorderLayout.CENTER);      
  
  	bPanel = new JPanel();
  	panel2.add(bPanel, BorderLayout.CENTER);      
  
  	addBoards(crateData);
      }
      protected void addBoards(final SvtCrateData crateData) {
  	if (crateData == null) {
  	  bPanel.setBackground(Color.black);
  	  bPanel.setPreferredSize(totalSize);
  	  return;
  	}
  
  	// Now the boards
  	SvtBoardData [] boardData = crateData.getBoardData();
  	bPanel.setLayout(new GridLayout(1, boardPanels.length));
  
  	// Now create the board panels
  	for (int j = 0; j < boardPanels.length; j++) {
  	  int index = -1;
  	  for (int i = 0; i < boardData.length; i++) {
  	    if (boardData[i].getType().equals("SC") ||
  		boardData[i].getType().equals("GB") 
  	      ) continue;
  	    if (boardData[i].getSlot() == j+OFFSET) {
  	      index = i;
  	      break;
  	    }
  	  }
  	  if (index < 0) {
  	    boardPanels[j] = new JPanel();
  	    boardPanels[j].setBackground(Color.black);
  	  }
  	  else if (boardData[index].getType().equals("AMB")) {
  	    boardPanels[j] = new JPanel();
  	    boardPanels[j].setBackground(ambColor);
  	  }
  	  else {
  	    boardPanels[j] = new BoardPanel(bColorMap.get(boardData[index].getType()), 
  					    cpuName, 
  					    boardData[index].getType(), 
  					    boardData[index].getSlot());
  	    validSlots[j] = true;
  	  }
  	  boardPanels[j].setPreferredSize(preferredSize);
  	  bPanel.add(boardPanels[j]); 
  	}
      }
  	/** Return useful information about the object
  	 *  @return A string representeation of the object of that class 
  	 */
      public String toString() {
  	return "Crate: " + cpuName + spaces + "ID: " + crateId 
  	       + spaces +"SVT Specific name: " + crateName;
      }
  	/** Get Reference to the LabelPanel object
  	 *  @return Reference to the LabelPanel object
  	 */
      protected LabelPanel getLabelPanel() {
  	return labelPanel;
      }
  	/** Get reference to the ButtonPanel object
  	 *  @return Reference to the ButtonPanel object  
  	 */
      protected ButtonPanel getButtonPanel() {
  	return buttonPanel;
      }
  	/** Get the name of the crate CPU
  	 *  @return The CPU name 
  	 */
      public String getCPUName () {
  	return cpuName;
      }
  	/** Get the crate CPU identifier string 
  	 *  @return The Crate ID string 
  	 */
      public String getCrateId () {
  	return crateId;
      }
  	/** Get the name of the crate CPU
  	 *  @return The Crate name string 
  	 */
      public String getCrateName () {
  	return crateName;
      }
  	/** Update crate panel
  	 *  @param crateData Reference to SVT crate data 
  	 */
      protected void update(final SvtCrateData crateData) 
  			    throws ArrayIndexOutOfBoundsException
      {
  	if (isDebugOn()) System.out.println("Updating " + crateData.getName());
  	for (int i = 0; i < crateData.getNBoards(); i++) {
  	  String boardName = crateData.getBoardData(i).getType();
  	  int slot         = crateData.getBoardData(i).getSlot();
  	  if (isDebugOn()) System.out.println("board/slot: " + boardName + "/" + slot);
  
  	  // **** Temporary Solution *****
  	  if (boardName.equals("SC") ||  
              boardName.equals("AMB") ||
              boardName.equals("GB")) continue;
  	  try {
  	    BoardPanel board = getBoardPanel(boardName, slot);
  	    board.update(crateData.getBoardData(i));
  	  }
  	  catch (NullPointerException ex) {
  	    System.out.println("board/slot: " + boardName + "/" 
  					      + slot + " Pointer = " + ex.getMessage());
  	    if (isDebugOn()) ex.printStackTrace();
  	    continue;
  	  }
  	}
  	checkTmode(crateData, frame.showTMode());
  	checkOutputHold(crateData, frame.showHold());
      }
      public void checkTmode(final SvtCrateData crateData, boolean scream) {
  	if (scream) {
  	  if (tModeFrame == null) tModeFrame = new TestModeFrame();
  	  tModeFrame.updateDisplay(crateData);
  	  tModeFrame.setSize(tModeFrame.getPreferredSize());
  	  if (!tModeFrame.isVisible()) tModeFrame.setVisible(true);
  	}
      }
      public void checkOutputHold(final SvtCrateData crateData, boolean scream) {
  	int nBoards = crateData.getNBoards();
  	StringBuilder buf = new StringBuilder(100);
  	buf.insert(0, "--------------------------\n");
  	buf.append("Output Hold ON for boards in "+crateData.getName());
  	int nOcc = 0;
  	SvtBoardData [] bData = crateData.getBoardData();
  	for (int i = 0; i < nBoards; i++) {
  	  if (bData[i].getHold() > 0) {
  	    buf.append("\nBoard ");
  	    buf.append(bData[i].getType());
  	    buf.append(" Slot ");
  	    buf.append(bData[i].getSlot());       
  	    nOcc++;
  	  }
  	}
  	buf.append("\n");
  	if (scream && nOcc > 0) {
  	  showInfo(buf.toString());
  	}
      }
  	/** Get reference to the BoardPanel keyed by board name and slot number
  	 *  @param  board   The board name String 
  	 *  @param  slot    Slot number which contains the board specified by <B>board</B>
  	 *  @return Reference to the correct board when board name and slot number
  	 *          is provided
  	 */
      protected BoardPanel getBoardPanel(final String board, int slot) 
  		    throws NullPointerException 
      {
  	BoardPanel panel = null;      
  	for (int i = 0; i < boardPanels.length; i++) {
  	  if (!validSlots[i]) continue;
  	  if (((BoardPanel)boardPanels[i]).getBoardName().equals(board) &&
  	      ((BoardPanel)boardPanels[i]).getBoardSlot() == slot)
  	    panel = (BoardPanel) boardPanels[i];
  	}
  	return panel;
      }
    }
      /** Implements each board in a crate. The panel consists of two 
       *  subpanels, each of which contains a number of buttons with LED
       *  to indicate certain error conditions. The buttons are active and can
       *  be clicked to obtain further informations. The color of each of the
       *  board panels corresponds to the color code of the real SVT board.
       *  The board panel should know its color, name, the crate name etc.
       *  in order to behave properly.
       */
    class BoardPanel extends JPanel {
  	/** Run/Test mode, CDF/SVT Error, SVT Board Error Registers */
      private ButtonBoxPanel boardLEDPanel;
  	/** EE, SpyBuffer Errors, Hold */
      private ButtonBoxPanel spyLEDPanel;
  	/** Background color or board color */
      private Color bgColor;
  	/** Name of the SVT crate CPU */
      private String cpuName;
  	/*** The name of the board */
      private String boardName;
  	/** The slot where the board sits in the crate */
      private int slot;
  	/** Construct the board panel with proper input
  	 *  @param bgColor    Color of board background (as in svt area)
  	 *  @param cpuName    Name of the crate CPU
  	 *  @param boardName  Name of the SVT board
  	 *  @param slot       The slot which contains the board
  	 */
      BoardPanel(final Color bgColor, final String cpuName, 
  		 final String boardName, int slot) {
  	this.bgColor   = bgColor;
  	this.cpuName   = cpuName;
  	this.boardName = boardName;
  	this.slot      = slot;
  	buildGUI();
      }
  	/** Create user interface */
      protected void buildGUI() {
  	setLayout(new BorderLayout());
  	setBorder(BorderFactory.createEtchedBorder());
  	setBackground(bgColor);
    
  	spyLEDPanel = new ButtonBoxPanel(cpuName, boardName, slot, 
  	      new String[]{buttonNames[0], buttonNames[1], buttonNames[2]});
  	boardLEDPanel = new ButtonBoxPanel(cpuName, boardName, slot, 
  	      new String[]{buttonNames[3], buttonNames[4], buttonNames[5]});
  
  	add(spyLEDPanel, BorderLayout.NORTH);
  	add(boardLEDPanel, BorderLayout.SOUTH);
      }
  	/** Returns the size of the panel as Dimension(x,y)
  	 *  @return Preferred size of the widget
  	 */
      public Dimension getPreferredSize() {
  	return preferredSize;
      } 
  	/** Get reference to the Board LED Panel
  	 *  @return Reference to the SVT board LED Panel
  	 */
      protected ButtonBoxPanel getBoardLEDPanel() {
  	return boardLEDPanel;
      }   
  	/** Get reference to the Spy Buffer relared LED Panel
  	 *  @return Reference to the Spy buffer related LED Panel
  	 */
      protected ButtonBoxPanel getSpyLEDPanel() {
  	return spyLEDPanel;
      } 
  	/** Get the board color
  	 *  @return Reference to the Board color
  	 */
      protected Color getBoardColor() {
  	return bgColor;
      }  
  	/** Get board slot number
  	 *  @return The slot number which contains the board inside the crate
  	 */
      public int getBoardSlot() {
  	return slot;
      }  
  	/** Get board slot number
  	 *  @return The slot number which contains the board inside the crate
  	 */
      public String getBoardName() {
  	return boardName;
      }  
  	/** Get reference to a Button by button tag
  	 *  @param tag  The button tag
  	 *  @return reference to the correct button given by button tag
  	 */
      protected JButton getButton(final String tag) {
  	if (tag.equals(buttonNames[0]) || 
  	    tag.equals(buttonNames[1]) || 
  	    tag.equals(buttonNames[2]))
  	  return spyLEDPanel.getButton(tag);
  	else
  	  return boardLEDPanel.getButton(tag);
      }
       /** Update LEDs using data read at each cycle
  	*  @param topValues  Array of decisions for the top button Panel
  	*  @param bottomValues  Array of decisions for the bottom button Panel
  	*/
      protected void update(final int [] topValues, 
  			    final int [] bottomValues) 
      {
  	spyLEDPanel.update(topValues, 0);
  	boardLEDPanel.update(bottomValues, 1);
      }
       /** Update LEDs using data read at each cycle
  	*  @param topValues  Array of decisions for the top button Panel
  	*  @param bottomValues  Array of decisions for the bottom button Panel
  	*/
      protected void update(final SvtBoardData boardData) 
  			    throws NullPointerException
      {
  	int cdfErr = 0, boardErrReg = 0; 
  	if ((boardData.getCDFError() > 0) || (boardData.getSVTError() > 0)) {
  	  cdfErr = 1;
  	}
  
  	if (boardData.getType().equals("HF")) {
  	  int [] errRegs = boardData.getErrorRegisters();
  	  for (int i = 0; i < errRegs.length; i++) {
  	    if (errRegs[i] > 0) boardErrReg++;
  	  }
  	}
  	else {
  	  boardErrReg = boardData.getErrorRegister(0);
  	}
  	update(
  	  new int [] {boardData.getEndEventError(), 
  		      boardData.getSimulationError(), 
  		      boardData.getHold()}, 
  	  new int [] {boardData.getTMode(), cdfErr, boardErrReg}
  	);
      }
    }
      /** Implements Crate CPU, Tracer and the Spy Control buttons
       *  which are common to all the SVT Crates
       */
    class ButtonPanel extends JPanel {
  	/** Crate CPU button */
      private JButton cpuButton;
  	/** Tracer board button */
      private JButton tracerButton;
  	/** Spy Control board button */
      private JButton scButton;
  	/** Name of the crate CPU */
      private String cpuName;
  	/** Build the button panel with crate CPU, tracer and SC buttons
  	 *  @param cpuName  Name of the crate CPU
  	 */
      String tipText;
  
      ButtonPanel(final String cpuName) {
  	this.cpuName = cpuName;
  	setLayout(new GridLayout(1, 3));
       
  	tipText = cpuName + " CPU";
  	cpuButton = new SvtButton(getStatusBar(), tipText+" ...");
  	cpuButton.setName(tipText);
  	cpuButton.setPreferredSize(preferredSize);
  	cpuButton.setBackground(buttonColors[0]);
  	cpuButton.setEnabled(false);
  	cpuButton.addActionListener(new ActionListener() {
  	  public void actionPerformed(ActionEvent evt) {
  	    showSpyError(ButtonPanel.this.cpuName);
  	  }
  	});
  	add(cpuButton);
  
  	tipText = cpuName + " Tracer";
  	tracerButton = new SvtButton(getStatusBar(), tipText+" ...");
  	tracerButton.setName(tipText);
  	tracerButton.setPreferredSize(preferredSize);
  	tracerButton.setBackground(buttonColors[1]);
  	tracerButton.setEnabled(false);
  	add(tracerButton);
  
  	tipText = cpuName + " Spy Control Board";
  	scButton = new SvtButton(getStatusBar(), tipText+" ...");
  	scButton.setName(tipText);
  	scButton.setPreferredSize(preferredSize);
  	scButton.setBackground(buttonColors[2]);
  	scButton.addActionListener(new ActionListener() {
  	  public void actionPerformed(ActionEvent evt) {
  	    showInfo(tipText);
  	  }
  	});
  	add(scButton);
      }
  	/** Get reference to the crate CPU Button 
  	 *  @return Reference to the CPU Button
  	 */
      protected JButton getCpuButton() {
  	return cpuButton;
      }
  	/** Get reference to the Tracer Board button
  	 *  @return Reference to the Tracer button
  	 */
      protected JButton getTracerButton() {
  	return tracerButton;
      }
  	/** Get reference to the Spy Control board button
  	 *  @return Reference to the Spy Control button
  	 */
      protected JButton getScButton() {
  	return scButton;
      }
    }
      /** This class implements the label and the attached border on 
       *  top of the individual Crate Panel
       */
    class LabelPanel extends JPanel {
  	/** The label which contains the Crate identifier */
      private JButton button;
      private JLabel  label;
      //private TimeEvent timerPanel;
      private JPanel timerPanel;
  	/** 
  	 *  @param name   Identifier name of the SVT Crate
  	 */
      LabelPanel(final String crateId, final String cpuName) {
        setLayout(new BorderLayout());

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

        label = Tools.createLabel("Offline", null, JLabel.CENTER, Color.black,
                BorderFactory.createEtchedBorder());
        label.setBackground(yellow2);
        label.setOpaque(true);
        label.setPreferredSize(new Dimension(50, 15));
        label.setFont(new Font("SansSerif", Font.PLAIN, 8));
        panel.add(label, BorderLayout.NORTH);  
     
        timerPanel = new JPanel(); // TimeEvent();
        panel.add(timerPanel, BorderLayout.CENTER);

        add(panel, BorderLayout.WEST);

  	button = new JButton(crateId + " ("+cpuName+")");
  	button.setEnabled(false);
  	button.addActionListener(new ActionListener() {
  	  public void actionPerformed(ActionEvent evt) {
  	    showSpyError(cpuName);
  	  }
  	});
  	add(button, BorderLayout.CENTER);
      }
  	/** Get reference to the label
  	 *  @return Reference to the Label 
  	 */
      protected JButton getButton() {
  	return button;
      }
      public void setText(final String text) {
        label.setText(text);
      }
    }
      /** Implements a Button box panel with black background. The panel
       *  consists of a number of active buttons with LEDs to indicate 
       *  error conditions.
       */
    class ButtonBoxPanel extends JPanel implements ActionListener {
  	/** The buttons inside the panel */
      private JButton [] buttons;
      String  tipText;
  	/** 
  	 * @param crateName  Name of the crate (CPU) i.e b0svt05
  	 * @param boardName  Name of the SVT board
  	 * @param slot       The slot number to identify the board uniquely
  	 * @param buttonTags Array containing the button names
  	 */
      ButtonBoxPanel(final String cpuName, final String boardName,
  		     int slot, final String [] buttonTags) {
  	buttons = new JButton[buttonTags.length];
  	setLayout(new GridLayout(buttons.length, 1));
  	setBackground(Color.black);
  	setPreferredSize(new Dimension(imageBoxWidth, imageBoxHeight));
  
  	for (int i = 0; i < buttons.length; i++) {
  	  tipText = cpuName + ":" + boardName + ":" + slot + ":" + buttonTags[i];
  	  buttons[i] = new SvtButton(AppConstants.smallYellowBall, getStatusBar(), tipText+" ...");
  	  buttons[i].setName(tipText);
  	  buttons[i].setBackground(Color.black);
  	  buttons[i].setBorder(BorderFactory.createRaisedBevelBorder());
  	  buttons[i].addActionListener(this);
  	  add(buttons[i]);
  	}
      }
  	/** Get the reference to a button by its tag 
  	 *  @param Tag The last part of the fully specified name 
  	 *             of the button (crate:board:slot:tag)
  	 *  @return Reference to the corresponding button which matches tag
  	 */
      protected JButton getButton(final String name) {
  	int index = -1;
  	String [] tags;
  	for (int i = 0; i < buttons.length; i++) {
  	  tags = buttons[i].getName().split(":");
  	  if (name.equals(tags[tags.length-1])) index = i;  
  	}
  	if (index >= 0) 
  	  return buttons[index];
  	else
  	  return null;
      }
  	/** Get reference to the button array
  	 *  @return Reference to the button array
  	 */
      protected JButton [] getButtons() {
  	return buttons;
      }
  	/** Get reference to a button array element
  	 *  @param i   Array index
  	 *  @return Reference to a button array element
  	 */
      protected JButton getButton(int i) {
  	return buttons[i];
      }
  	/** Implement the action performed by the buttons
  	 *  @param ev The ActionEvent which is triggered on button click
  	 */ 
      public void actionPerformed(ActionEvent ev) {
  	String label = null;
  	String crate, board, bname;
  	int slot;
  	SvtBoardData data = null;
  	Component source = (Component) ev.getSource();
  	if (source instanceof JButton) {
  	  label = ((JButton) source).getName().trim();
  	  String [] tags = label.split(":");
  
  	  crate = tags[0];
  	  board = tags[1];
  	  slot  = Integer.parseInt(tags[2]);
  	  bname = tags[tags.length-1];
  
  	  try {
  	    data = manager.getBoardData(crate, slot, board);
  	  }
  	  catch (NullPointerException ex) {
  	    JOptionPane.showMessageDialog(frame, 
  		      "Data cannot be retrieved for Crate/Slot/Board " + 
  		       crate + "/" + slot + "/" + board,
  		       "Warning", JOptionPane.WARNING_MESSAGE);
  	    if (isDebugOn()) {
  	       System.out.println("Data cannot be retrieved for crate/slot/board " + 
  				   crate + "/" + slot + "/" + board);
  	       System.out.println("SvtBoardData = " + ex.getMessage()); 
  	    }
  	    return;
  	  }
  	  try {
            if (!frame.showIndError()) {
  	      showBoardError(crate, data);
            }
            else {
    	      if (bname.equals(buttonNames[0])) {        // EndEvent
  	        showEndEventError(crate, board,  slot);
  	      }
  	      else if (bname.equals(buttonNames[1])) {   // Simulation
  	        showSimulationError(crate, board,  slot);
  	      }
  	      else if (bname.equals(buttonNames[4])) {   // CDF/SVT Error
  	        showCDFError(crate, board,  slot);
  	      }
  	      else if (bname.equals(buttonNames[5])) {   // Board Error Register
  	        showErrorReg(crate, board,  slot);
  	      }
  	      else if (bname.equals(buttonNames[2])) {   // Hold
  	        showInfo(crate+"/"+slot+"/"+board+" Output Hold: " + 
  		        ((data.getHold() > 0) ? "On" : "Off"));
  	      }
  	      else {                                     // Test Mode 
  	        showInfo(crate+"/"+slot+"/"+board+" in " + 
  		        ((data.getTMode() > 0) ? "Test" : "Run") + " mode");
  	      }
            }
  	  }
  	  catch (NullPointerException ex) {
  	    JOptionPane.showMessageDialog(frame, 
  		  "Cannot retrieve data for Crate/Slot/Board " + 
  		   crate + "/" + slot + "/" + board,
  		   "Warning", JOptionPane.WARNING_MESSAGE);
  	    if (isDebugOn()) {
  	      System.out.println("Cannot retrive data for Crate/Board/Slot" +
  				  crate + " " + board + " " + slot);
  	      System.out.println("Error Message: " + ex.getMessage());
  	    }
  	    ex.printStackTrace();
  	  }
  	}
  	if (isDebugOn()) frame.warn(label+" clicked");
      }
       /** Update LEDs using data read at each cycle
  	*  @param decisions  Array of decisions
  	*/
      protected synchronized void update(final int [] decisions, int opt) {
        Icon icon;
  	for (int i = 0; i < decisions.length; i++) {
  	  if (decisions[i] == 0) {
            icon = AppConstants.smallGreenBall;
          }
          else {
            if (opt > 1 && i == 2) 
              icon = AppConstants.smallYellowBall;  // Special for CDF/SVT Error
            else 
              icon = AppConstants.smallRedBall;
          }
  	  buttons[i].setIcon(icon);
  	}
      }
    }
    protected StatusBar getStatusBar() {
      return frame.getStatusBar();
    }
    public void showInfo(final String infoText) {
      if (textInfo == null) {
  	textInfo = new TextInfoFrame("Status Info");
  	textInfo.setSize(new Dimension(600, 400));
  	textInfo.setVisible(true);
      }
      textInfo.warn(infoText, Color.blue);
    }
    public void showEndEventError(final String crate, final String board, int slot) {
      JPanel panel = new GlobalErrorSummaryPanel(crate, board, slot, "EndEvent");
      panel.setBorder(Tools.etchedTitledBorder(" " +crate+"/"+board+"/"+slot+ " End Event Error "));
      JOptionPane.showMessageDialog(frame, 
  	 panel, "Global Error Summary for End Event", JOptionPane.INFORMATION_MESSAGE, null);
    }
    public void showSimulationError(final String crate, String board, int slot) {
      JPanel panel = new GlobalErrorSummaryPanel(crate, board, slot, "Simulation");
      panel.setBorder(Tools.etchedTitledBorder(" " +crate+"/"+board+"/"+slot+ " Simulation Error "));
      JOptionPane.showMessageDialog(frame, 
  	 panel, "Global Error Summary for Simulation", JOptionPane.INFORMATION_MESSAGE, null);
    }
    public void showErrorReg(final String crate, String board, int slot) {
      JPanel panel;
      if (board.equals("HF")) 
  	panel = new HFBoardErrorRegPanel(crate, board, slot);
      else 
  	panel = new BoardErrorRegPanel(crate, board, slot);
      panel.setBorder(Tools.etchedTitledBorder(" " +crate+"/"+board+"/"+slot+" Error Regs "));
      JOptionPane.showMessageDialog(frame, 
  	 panel, "Board Error registers", JOptionPane.INFORMATION_MESSAGE, null);
    }
    public void showCDFError(final String crate, String board, int slot) {
      JPanel panel = new CDFErrorPanel(crate, board, slot);
      panel.setBorder(Tools.etchedTitledBorder(" " +crate+"/"+board+"/"+slot+" CDF/SVT Errors"));
      JOptionPane.showMessageDialog(frame, 
  	 panel, "Board CDF/SVT Errors", JOptionPane.INFORMATION_MESSAGE, null);
    }
    public class TextInfoFrame extends DataFrame  {
      TextInfoFrame(String label) {
  	super(false, label, true);
  
  	// Set Help file
  	String filename = Tools.getEnv("SVTMON_DIR")+"/help/a_TextInfoFrame.html";
  	setHelpFile(filename, "About Info Window", new Dimension(400, 200)); 
      }
      protected void closeFrame() {
  	super.closeFrame();
  	SvtCratesPanel.this.textInfo = null;
      }
    }
  }
  public void showSpyError(String cpuName) {
    if (spyErrorFrame == null) {
      spyErrorFrame = new SpyErrorFrame(false);
      spyErrorFrame.addWindowListener(winListener);
      spyErrorFrame.setSize(spyErrorFrame.getPreferredSize());
    }
    spyErrorFrame.getErrorPanel().selectTab(cpuName);
    spyErrorFrame.setVisible(true);
  }
  public void showBoardError(String crate, SvtBoardData data) {
    if (boardErrorFrame == null) {
      boardErrorFrame = new SvtBoardErrorFrame(false);
      boardErrorFrame.addWindowListener(winListener);
      boardErrorFrame.setSize(800, 600);
    }
    boardErrorFrame.showData(new BoardInfo(crate, data)); 
    boardErrorFrame.setVisible(true);
  }
  public SpyErrorFrame getSpyError() {
    return spyErrorFrame;
  }
  public static void main (String [] argv) {
    JFrame f = new SvtCratesFrame(true);
    f.setSize(800, 600);
    f.setVisible(true);
  }
}
