package config.hist;

import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Font;
import java.awt.Color;
import java.awt.Point;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.JComboBox;
import javax.swing.JSplitPane;
import javax.swing.JDesktopPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.BorderFactory;
import javax.swing.JRadioButton;
import javax.swing.JRadioButton;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.ButtonGroup;
import javax.swing.JInternalFrame;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import config.util.DataFrame;
import config.util.AppConstants;
import config.util.TextPanel;
import config.util.StatusBar;
import config.util.Tools;

/** A simple histogram viewer interface. 
 *  @author S. Sarkar
 *  @version 0.1, March 2001
 */
public class HistogramDisplayFrame extends DataFrame 
                                   implements HistogramDrawingManager
 {
  private HistogramListPanel listPanel;
  private OptionPanel optPanel;
  private StatusPanel statusPanel;
  private JComboBox zoneCB;
  private JSplitPane splitPane;
  private JDesktopPane plotCanvas;
  private static int openFrameCount = 0; 
  private int xZone = 1, 
              yZone = 1;
  private static final Dimension minSize  = new Dimension(300, 400);
  private static final int XWIDTH  = 900;
  private static final int XHEIGHT = 520;
  private JSplitPane splitPane2;

  /* Constructor */
  public HistogramDisplayFrame(boolean standalone) {
    super(standalone, "Histogram Display Application", false, true, -1);
    buildGUI();

    // Set Help file
    String filename = Tools.getEnv("SVTMON_DIR")+"/help/a_HistogramDisplayFrame.html";
    setHelpFile(filename, "About Histogram Display", new Dimension(600, 400));
  }
  /** Build user interface */
  protected void buildGUI() { 
    // Add Toolbar
    addToolBar();

    // Setup combo box
    setupComboBox();

    // Update contents of the Menu/Tool Bar 
    addViewMenu(getJMenuBar());
    addHelpMenu();

    addToolButtons();
    addHelpInToolBar();
    
    // Create the main application area now
    JPanel panel = new JPanel(new BorderLayout());

    // Add the scroll panes to a split pane 
    plotCanvas = new JDesktopPane();
    plotCanvas.setBackground(Color.white);

    listPanel = new HistogramListPanel(this);
    listPanel.setBorder(BorderFactory.createEmptyBorder(5,0,0,0));

    optPanel  = new OptionPanel();

    splitPane = Tools.createSplitPane(JSplitPane.HORIZONTAL_SPLIT, listPanel, optPanel);

    listPanel.setMinimumSize(minSize);
    optPanel.setMinimumSize(minSize);

    splitPane.setDividerLocation(250);
    splitPane.setPreferredSize(new Dimension(XWIDTH, XHEIGHT));

    TextPanel textPanel = getTextPanel();
    textPanel.setBorder(Tools.etchedTitledBorder(" Message Logger "));
//    textPanel.setPreferredSize(new Dimension(500, 80));

    splitPane2 = Tools.createSplitPane(JSplitPane.VERTICAL_SPLIT, splitPane, textPanel);
    splitPane2.setDividerLocation(480);
    panel.add(splitPane2, BorderLayout.CENTER);

    // Add at the center
    getContentPane().add(panel, BorderLayout.CENTER);

    // Finally a custom status bar at the bottom
    statusPanel = new StatusPanel();
    getContentPane().add(statusPanel, BorderLayout.SOUTH);
  }
  /** An inner class which defines a status oanel at the bottom of the window */
  public class StatusPanel extends JPanel {
    private JLabel loadLabel;
    public StatusPanel() {
      buildGUI();
    }
    private void buildGUI() {
      setLayout(new BorderLayout());
      setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

      JPanel panel = new JPanel(new BorderLayout());
      panel.setBorder(BorderFactory.createLoweredBevelBorder());

      loadLabel = new JLabel("");
      loadLabel.setFont(new Font("SanSerif", Font.PLAIN, 11));
      loadLabel.setForeground(Color.black);
      loadLabel.setBorder(BorderFactory.createEmptyBorder(0,3,0,0));
      loadLabel.setMinimumSize(new Dimension(50, 20));
      loadLabel.setPreferredSize(new Dimension(100, 20));
      panel.add(loadLabel, BorderLayout.CENTER);

      add(panel, BorderLayout.WEST);

      StatusBar statusBar = getStatusBar();
      statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
      add(statusBar, BorderLayout.CENTER);
    }
    public String getLoadText() {
      return loadLabel.getText();
    }
    public void setLoadText(final String text) {
      loadLabel.setText(text);
    }
  }
  /** An inner class to specify histogram source */
  public class OptionPanel extends JPanel {
    private JRadioButton [] optionRB;
    private ButtonGroup optionBG;
    private JCheckBox autoUpdateCB;
    private JPanel radioP, checkP, buttonP;
    private JButton okB;
    private String [] options = {
      "Read Histogram Information from Text File",
      "Read Spy Buffer Dump Files and Create Histograms",
      "Listen to SmartSockets Histogram Messages from SVT Crates"
    };
    public OptionPanel() {
      buildGUI();
    }
    public boolean isAutoUpdateEnabled() {
      return autoUpdateCB.isSelected();
    }
    public void setAutoUpdate(boolean decision) {
      autoUpdateCB.setSelected(decision);
    }
    public JRadioButton getRadioButton(int index) {
      if (index >= optionRB.length) throw new IndexOutOfBoundsException();
      return optionRB[index];
    }
    public JButton getButton() {
      return okB;
    }
    private void buildGUI() {
      setLayout(new BorderLayout());
      setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      
      JPanel panel = new JPanel(new BorderLayout());

      radioP = new JPanel(new GridLayout(options.length, 1));
      radioP.setBorder(Tools.etchedTitledBorder(" Select Data Source "));
      optionRB = new JRadioButton[options.length];
      optionBG = new ButtonGroup();
      for (int i = 0; i < options.length; i++) {
        optionRB[i] = new JRadioButton(options[i]);
        optionRB[i].setBorder(BorderFactory.createEmptyBorder(5, 5, 2, 0));
        optionBG.add(optionRB[i]);
        radioP.add(optionRB[i]);
      }
      optionRB[options.length-1].setSelected(true);

      checkP = new JPanel(new GridLayout(1, 1));
      checkP.setBorder(Tools.etchedTitledBorder(" Select Other Options "));
      
      autoUpdateCB = new JCheckBox("Update Histograms automaticaly", true); 
      checkP.add(autoUpdateCB);

      panel.add(radioP, BorderLayout.NORTH);
      panel.add(checkP, BorderLayout.CENTER);

      buttonP = new JPanel(new BorderLayout());

      okB = new JButton("OK");
      okB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          splitPane.remove(splitPane.getRightComponent());
          splitPane.setRightComponent(plotCanvas);
        }
      });
      buttonP.add(okB, BorderLayout.EAST);

      add(panel, BorderLayout.NORTH);
      add(buttonP, BorderLayout.SOUTH);
    }
  }
  /** Show plot canvas */
  public void showCanvas() {
    optPanel.getButton().doClick();
  }
  /** Get a reference to the histogram List panel */
  public HistogramListPanel getListPanel() {
    return listPanel;
  }
  /** The histogram frame */
  public class HistogramInFrame extends JInternalFrame {
    protected HistogramPlotPanel panel;
    HistogramInFrame(HistogramPlotPanel panel, int count) {
      super("Frame #" + (++count), true, true, true, true);
                    // (name, resizable, closable, maximizable, iconifiable)
      this.panel = panel;      
      setVisible(true);      // necessary as of kestrel
      try {
        setSelected(true);
      } 
      catch (java.beans.PropertyVetoException e) {
        e.printStackTrace();
      }
      getContentPane().add(panel);
    }
    public void drawHist() {
      panel.drawHist();
    }
  }
  private void setupComboBox() {
    zoneCB = new JComboBox();
    zoneCB.setMaximumSize(new Dimension(80, 20));
    String [] zoneOptions = 
    {
      "1 x 1", 
      "1 x 2", 
      "2 x 1", 
      "2 x 2"
    };
    for (int i = 0; i < zoneOptions.length; i++) {
      zoneCB.addItem(zoneOptions[i]);   
    }
    zoneCB.setSelectedIndex(0);
    zoneCB.setMaximumRowCount(zoneOptions.length);
    zoneCB.setEditable(false);  
    ComboListener zoneListener = new ComboListener();
    zoneCB.addActionListener(zoneListener);
  }
  /**
   *  Listens to the combo box for font related options
   */
  class ComboListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      JComboBox cb = (JComboBox) e.getSource();
      String item = (String) cb.getSelectedItem();
      if (isDebugOn()) System.out.println("Zones: " + item);
      String [] zones = item.split("x");
      if (zones.length == 2) {
        try {
          xZone = Integer.parseInt(zones[0].trim());
          yZone = Integer.parseInt(zones[1].trim());
        }
        catch (NumberFormatException ex) {
          xZone = 1;
          yZone = 1;
        }
      }
      else {
        xZone = 1;
        yZone = 1;
      }
      clearPanel();
    }
  }
  // Add new buttons to the existing toolbar
  protected void addToolButtons() {
    Icon icon = new ImageIcon(AppConstants.iconDir+"plot.gif");
    Action action = new AbstractAction("Plot", icon) {
      public void actionPerformed(ActionEvent e) {
        drawHist(getSelectedHistogram());
      }
    };
    addToolElement(action,"Plot the Selected Histogram ...", -1, 2);

    icon = new ImageIcon(AppConstants.iconDir+"back.gif");
    action = new AbstractAction("Prev", icon) {
      public void actionPerformed(ActionEvent e) {
        drawHist(getPreviousHistogram());
      }
    };
    addToolElement(action, "Plot the Previous Histogram ...", -1, 3);

    icon = new ImageIcon(AppConstants.iconDir+"forward.gif");
    action = new AbstractAction("Next", icon) {
      public void actionPerformed(ActionEvent e) {
        drawHist(getNextHistogram());
      }
    };
    addToolElement(action, "Plot the Next Histogram ...", -1, 4);

    icon = new ImageIcon(AppConstants.iconDir+"Refresh.gif");
    action = new AbstractAction("Clear", icon) {
      public void actionPerformed(ActionEvent e) {
        clearPanel();
      }
    };
    addToolElement(action, "Clear the Drawing Area ...", -1, 5);

    icon = new ImageIcon(AppConstants.iconDir+"mini-edit.gif");
    action = new AbstractAction("Dump", icon) {
      public void actionPerformed(ActionEvent e) {
        dumpHist(getSelectedHistogram());
      }
    };
    addToolElement(action, "Dump histogram as ASCII text ...", -1, 6);

    addToolSeparator();
    addToolElement(zoneCB, 7);
  }

  // Add View menu to Menubar
  protected void addViewMenu(JMenuBar menuBar) {
    JMenu menu = new JMenu("View");
    menu.setMnemonic('v');

    Icon icon = new ImageIcon(AppConstants.iconDir+"Wizard.gif");
    Action action =  new AbstractAction("Preference", icon) { 
      public void actionPerformed(ActionEvent e) {
      }
    };
    JMenuItem item = menu.add(action);
    item.setMnemonic('p');
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_MASK));
    menu.add(item);

    addToolElement(action, "Preferences ", -1, 1);
    menuBar.add(menu);
  }
  public void update(final HistogramColl histoData) {
    listPanel.updateList(histoData);
    if (!isVisible()) setVisible(true);
  }
  public void dumpHist(final Histogram histo) {
    displayText(histo.toString());
  }
  public void drawHist(final Histogram histo) {
    HistogramPlotPanel panel = new HistogramPlotPanel(histo);
    panel.setBackground(Color.white);
    panel.setMinimumSize(minSize);
    panel.drawHist();

    int ndiv = xZone * yZone;
    JInternalFrame[] frames = plotCanvas.getAllFrames();
    if (frames.length >= ndiv) clearPanel();

    JInternalFrame frame = new HistogramInFrame(panel, openFrameCount);
    Dimension totalSize  = plotCanvas.getSize();

    int width  = totalSize.width/xZone;
    int height = totalSize.height/yZone;
    if (isDebugOn()) System.out.println("width/height: " + width + "/" + height);
    frame.setSize(new Dimension(width, height));

    Point p;
    if (frames.length == 0 || ndiv == 1 || openFrameCount == 0) {
      p = new Point(0, 0);
    }
    else {
      JInternalFrame lastframe = frames[frames.length-1];
      p = lastframe.getLocation();
      if (openFrameCount%xZone > 0) 
        p.translate(lastframe.getWidth(), 0);
      else
        p.translate(-p.x, lastframe.getHeight());
    }
    if (isDebugOn()) System.out.println("Top left corner: " + p.x + ", " + p.y);
    frame.setLocation(p);

    plotCanvas.add(frame);
    plotCanvas.repaint();

    openFrameCount++;
  }
  public void redrawHistograms() {
    JInternalFrame [] frames = plotCanvas.getAllFrames();
    for (int i = 0; i < frames.length; i++) {
      if (isDebugOn()) System.out.println("Redraw " + i);
      ((HistogramInFrame)frames[i]).drawHist();
    }
  }
  public void clearPanel() {
    openFrameCount = 0;
    plotCanvas.removeAll();
    plotCanvas.repaint();
  }
  public Histogram getSelectedHistogram() {
    return listPanel.getSelectedHistogram();
  }
  public Histogram getPreviousHistogram() {
    return listPanel.getPreviousHistogram();
  }
  public Histogram getNextHistogram() {
    return listPanel.getNextHistogram();
  }
  public void setLabel(final String text) {
    statusPanel.setLoadText(text);
  }
  public static void main(String argv[]) {
    JFrame f = new HistogramDisplayFrame(true);
    f.setSize(900, 800);
    f.setVisible(true);
  }
}
