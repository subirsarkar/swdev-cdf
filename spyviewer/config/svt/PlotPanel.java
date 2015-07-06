package config.svt;

import java.util.Vector;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Component;

import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JPanel;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.ImageIcon;
import javax.swing.JSplitPane;
import javax.swing.JList;
import javax.swing.DefaultListModel;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;

import javax.swing.event.*;

import hep.analysis.Histogram;
import hep.analysis.partition.Abstract1DPartition;

import jas.hist.DataSource;
import jas.hist.JASHist;
import jas.hist.JASHistData;
import jas.hist.JASHist1DHistogramStyle;
import jas.hist.JASHist2DHistogramStyle;
import jas.hep.PartitionAdapter;

import org.python.util.PythonInterpreter; 
import org.python.core.PyObject;
import org.python.core.Py;

import config.util.Tools;
import config.util.AppConstants;
import config.util.WordPanel;
import config.util.MutableList;
import config.util.PopupListener;

import config.svt.SpyType;

public class PlotPanel extends JPanel {
  public static final boolean DEBUG = false;
  public static final String EMPTY = "";
  static final Color color = new Color(70, 130, 180);
  static final ImageIcon hIcon = new ImageIcon(AppConstants.iconDir+"histogram_small.png");

  private HistPanel histPanel;
  private OptionPanel optPanel;
  private ListPanel listPanel;
  private Histogram dHist;
  private Vector<Histogram> hList = new Vector<Histogram>(20);
  private StringBuilder buf = new StringBuilder(AppConstants.SMALL_BUFFER_SIZE);
  SvtHistogrammer histogrammer;
  String crate = new String(EMPTY), 
         board = new String(EMPTY), 
         spy   = new String(EMPTY);
  int slot = 0;
  public PlotPanel() {
    super(true);
    buildGUI();	
  }
  protected void buildGUI() {
    setLayout(new BorderLayout());
    setBorder(BorderFactory.createEmptyBorder(1,2,1,2));

    JPanel panel1 = new JPanel(new BorderLayout());
    panel1.setBorder(BorderFactory.createEmptyBorder(0,0,2,0));
    add(panel1, BorderLayout.NORTH); 

    histPanel = new HistPanel();
    histPanel.setPreferredSize(new Dimension(700, 360));

    optPanel  = new OptionPanel();
    JSplitPane splitPane = Tools.createSplitPane(JSplitPane.HORIZONTAL_SPLIT, histPanel, optPanel);
    panel1.add(splitPane);

    JPanel panel2 = new JPanel(new BorderLayout());
    panel2.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
    add(panel2, BorderLayout.CENTER);

    listPanel = new ListPanel();
    panel2.add(listPanel, BorderLayout.CENTER);    
  }
  public void clear() {
    histPanel.removeAll();
    listPanel.getContents().clear();
    hList.removeAllElements();
  }
  public void setStatus(final SvtHistogrammer histogrammer, 
                        final String crate, 
                        final String board,
                        int slot, 
                        final String spy) 
  {
    this.histogrammer = histogrammer;
    this.crate = crate;
    this.board = board;
    this.slot  = slot;
    this.spy   = spy;
    
    clear();

    if (spy.indexOf("MRG") != -1) {
      optPanel.enableCombo(true);
      optPanel.getContents().clear();
    }
    else {
      optPanel.enableCombo(false);
      optPanel.setItems("TF");  // Temporary
    }
    DefaultListModel model = listPanel.getContents();
    String [] htits = histogrammer.getHistogramTitles();
    for (int i = 0; i < htits.length; i++) {
      buf.append(htits[i]).append("for Crate ").append(crate).append(" Board ")
         .append(board).append("-").append(slot).append(" Buffer ").append(spy);
      model.addElement(buf.toString());
      buf.setLength(0);
    }
    histogrammer.fillHistograms(true); // Always in this context since we use the same histograms
                                       // for buffers of the same kind from separate places
  }
  public void plotHistogram() {
    String xstr   = listPanel.getXText();
    String ystr   = listPanel.getYText();
    String cutstr = listPanel.getCutText();
    String title  = listPanel.getTitle();

    // MUST use unqualified names, the SvtObject is generically denoted by 'obj'
    // The variables names should never contain '.' which is evident
    // The 'cut' expression should contain only the variable names
    int index = xstr.indexOf(".");
    if (index != -1) xstr = xstr.substring(index+1);

    index = ystr.indexOf(".");
    if (index != -1) ystr = ystr.substring(index+1);

    // This is not robust at all, more later
    if (cutstr.indexOf("obj.") != -1) return;

    // Construct histogram title
    if (title.length() == 0) {
      if (ystr.length() == 0) 
        title = xstr;
      else 
        title = ystr + " vs " + xstr;
    }
    dHist = new Histogram(title);
    hList.addElement(dHist);
    listPanel.getContents().addElement(title);

    String matstr = "\"("+histogrammer.getMatchStr()+")\"";

    PythonInterpreter interp = Interpreter.getInstance().getInterpreter();
    if (DEBUG) {
      System.out.println(matstr);
      System.out.println("regexp = re.compile(" + matstr + ")");
      System.out.println("xpre   = regexp.subn(r'obj.\\1',\"" + xstr +"\")");
      System.out.println("ypre   = regexp.subn(r'obj.\\1',\"" + ystr +"\")");
      System.out.println("cutpre = regexp.subn(r'obj.\\1',\"" + cutstr +"\")");
    }

    interp.exec("regexp = re.compile(" + matstr + ")");
    interp.exec("xpre   = regexp.subn(r'obj.\\1',\"" + xstr +"\")");
    interp.exec("ypre   = regexp.subn(r'obj.\\1',\"" + ystr +"\")");
    interp.exec("cutpre = regexp.subn(r'obj.\\1',\"" + cutstr +"\")");
    if (DEBUG) interp.exec("print xpre[0], ypre[0], cutpre[0]");  
     
    interp.exec("xvar   = xpre[0]");
    interp.exec("yvar   = ypre[0]");
    interp.exec("cutvar = cutpre[0]");
    
    String xvar   = (String)interp.get("xvar",   String.class);
    String yvar   = (String)interp.get("yvar",   String.class);
    String cutvar = (String)interp.get("cutvar", String.class);

    histogrammer.fillHistogram(dHist, xvar, yvar, cutvar);

    try {
      histPanel.drawHist(dHist);
    }
    catch (NullPointerException nexp) {
      System.out.println(nexp.getMessage());
    }
    catch (Exception exp) {
      System.out.println(exp.getMessage());
    }
  }
  public void setDataType (final String type) {
    optPanel.setDataType(type);    
  }
  class ListPanel extends JPanel {
    private MutableList list;
    private CommandPanel comPanel;
    private JTabbedPane tabs;
    ListPanel() {
      super(true);
      setLayout(new BorderLayout());
      setBorder(BorderFactory.createEmptyBorder(0,0,0,0));

      // List View
      list = new MutableList();
      list.setFont(AppConstants.gFont);
      list.setCellRenderer(new config.util.MyCellRenderer(hIcon));
      list.addMouseListener(new MyMouseListener());
      list.setVisibleRowCount(6);

      JScrollPane scp = new JScrollPane(list);
      scp.setPreferredSize(new Dimension(500, 85));
  
      comPanel = new CommandPanel();

      tabs = new JTabbedPane();
      tabs.setFont(AppConstants.gFont);
      tabs.setTabPlacement(SwingConstants.RIGHT);
      
      // Add the panes and specify which pane is displayed first 
      tabs.addTab("List",    null, scp);
      tabs.addTab("Command", null, comPanel);

      add(tabs, BorderLayout.CENTER);
    }
    class MyMouseListener extends MouseAdapter {
      public void mouseClicked(MouseEvent e) {
        // if the histogram is statically defined get it through the histogrammer
        // otherwise get it from hList
        if (e.getClickCount() == 2) {
          Histogram hist;
          int index = list.locationToIndex(e.getPoint());
          int staticSize = histogrammer.getSize();  
          if (index < staticSize)
            hist = histogrammer.getHistogram(index);
          else 
            hist = hList.elementAt(index - staticSize);
          histPanel.drawHist(hist);
        }
      }
    }
    public final DefaultListModel getContents() {
      return list.getContents();
    }
    public final String getXText() {
      return comPanel.getXText();      
    }
    public final void setXText(String text) {
      comPanel.setXText(text);      
    }
    public final String getYText() {
      return comPanel.getYText();      
    }
    public final void setYText(String text) {
      comPanel.setYText(text);      
    }
    public final String getCutText() {
      return comPanel.getCutText();      
    }
    public final String getTitle() {
      return comPanel.getTitle();      
    }
    public final int getSelectedIndex() {
      return list.getSelectedIndex();
    }
  }
  class HistPanel extends JPanel {
    HistPanel() {
      super(true);
      setLayout(new BorderLayout());
      setBackground(Color.white);
      setBorder(BorderFactory.createLoweredBevelBorder());
    }
    private JASHist createPlot() {
      JASHist plot = new JASHist();
      plot.setDataAreaColor(Color.white);
      plot.setDataAreaBorderType(plot.NONE);
      plot.setAllowUserInteraction(true);
      return plot;
    }
    public void drawHist(final Histogram hist) throws NullPointerException {
      removeAll();
      JASHist plot = createPlot();
      add(plot, BorderLayout.CENTER);

      DataSource source = PartitionAdapter.create(hist);
      JASHistData jasData = plot.addData(source);

      if (hist.getPartition() instanceof Abstract1DPartition) {
        plot.getYAxis().setLabel("Number of Events");
        JASHist1DHistogramStyle style1D = (JASHist1DHistogramStyle) jasData.getStyle();
        style1D.setShowErrorBars(false);
        style1D.setHistogramBarColor(color);
        style1D.setDataPointColor(color);
      }
      else {
        //plot.getYAxis().setLabel(hist.getYTitle());
        JASHist2DHistogramStyle style2D = (JASHist2DHistogramStyle) jasData.getStyle();
        style2D.setShapeColor(color);
      }
      plot.setTitle(plot.getTitle());
      plot.setShowStatistics(true);
      //plot.getXAxis().setLabel(hist.getXTitle());
                
      jasData.show(true);
      repaint();
    }
  }

  class CommandPanel extends JPanel {
    private WordPanel xPanel;
    private WordPanel yPanel;
    private WordPanel cutPanel;
    private WordPanel titlePanel;
    CommandPanel() {
      super(true);
      setLayout(new BorderLayout());

      JPanel p = new JPanel(new BorderLayout());
      add(p, BorderLayout.NORTH);

      titlePanel = new WordPanel("Titl", "", 360, 6, JTextField.LEFT);
      p.add(titlePanel, BorderLayout.WEST);

      JPanel panel = new JPanel(new BorderLayout());
      add(panel, BorderLayout.CENTER);

      JPanel p1 = new JPanel(new BorderLayout());
      xPanel = new WordPanel("x%y", "phi0", 180, 4, JTextField.LEFT);
      yPanel = new WordPanel("",    "d0",   180, 0, JTextField.LEFT);
      p1.add(xPanel, BorderLayout.WEST);
      p1.add(yPanel, BorderLayout.CENTER);

      JPanel p2 = new JPanel(new BorderLayout());

      cutPanel = new WordPanel("Cut", "chi2 < 40", 180, 7, JTextField.LEFT);

      JPanel p3 = new JPanel();
      JButton button = new JButton("Plot");
      button.setPreferredSize(new Dimension(80, 22));
      button.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          plotHistogram();
        }
      });
      p3.add(button);

      button = new JButton("Clear");
      button.setPreferredSize(new Dimension(80, 22));
      button.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          titlePanel.setText("");
          xPanel.setText("");
          yPanel.setText("");
          cutPanel.setText("");
        }
      });
      p3.add(button);

      p2.add(cutPanel, BorderLayout.WEST);
      p2.add(p3, BorderLayout.CENTER);

      panel.add(p1, BorderLayout.NORTH);
      panel.add(p2, BorderLayout.CENTER);

      titlePanel.setText(getYText() + " vs " + getXText() + " for " + getCutText());
    }
    public String getXText() {
      return xPanel.getText().trim();
    }
    public void setXText(final String text) {
      xPanel.setText(text);
    }
    public String getYText() {
      return yPanel.getText().trim();
    }
    public void setYText(final String text) {
      yPanel.setText(text);
    }
    public String getCutText() {
      return cutPanel.getText().trim();
    }
    public String getTitle() {
      return titlePanel.getText().trim();
    }
  }
  class OptionPanel extends JPanel {
    private MutableList xlist;
    private JComboBox combo;
    protected JPopupMenu popup;
    OptionPanel() {
      super(true);
      setLayout(new BorderLayout());
      setBorder(BorderFactory.createEtchedBorder());
      JPanel panel = new JPanel(new GridLayout(2,1));

      JLabel label = new JLabel("Format MRG as:");
      label.setForeground(Color.black);
      label.setFont(AppConstants.gFont);

      combo = new JComboBox();
      combo.setFont(AppConstants.gFont);

      combo.addItem("None");
      for (int i = 0; i < SpyType.svtTypeList.length; i++)
        combo.addItem(SpyType.svtTypeList[i]);
      combo.setSelectedIndex(0); 
      combo.setEditable(true); 
      combo.setPreferredSize(new Dimension(80, 20)); 
      combo.setEnabled(false);

      panel.add(label); 
      panel.add(combo); 
    
      add(panel, BorderLayout.NORTH);

      JPanel panel2 = new JPanel(new BorderLayout());
      panel2.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));
      label = new JLabel("Named Vars:");
      label.setForeground(Color.black);
      label.setFont(AppConstants.gFont);

      // List View
      xlist = new MutableList();
      xlist.setCellRenderer(new config.util.MyCellRenderer(null));
      xlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      xlist.setFont(AppConstants.gFont);
      xlist.setVisibleRowCount(10);
      JScrollPane scp = new JScrollPane(xlist);
      scp.setPreferredSize(new Dimension(100, 200));
  
      popup = addPopup();

      // Add listener to components that can bring up popup menus.
      MouseListener popupListener = new PopupListener(popup);
      xlist.addMouseListener(popupListener);
      scp.addMouseListener(popupListener);

      panel2.add(label, BorderLayout.NORTH);
      panel2.add(scp, BorderLayout.CENTER);

      add(panel2, BorderLayout.CENTER);

      combo.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          setItems(combo.getSelectedItem().toString());
        }
      });
    }
    protected JPopupMenu addPopup() {
      // Add popup menu
      JPopupMenu popup = new JPopupMenu();

      JMenuItem item = new JMenuItem("Set as X");
      item.addActionListener(new ActionListener() {
    	public void actionPerformed(ActionEvent e) {
          listPanel.setXText(xlist.getSelectedValue().toString());
    	}
      });
      popup.add(item);

      item = new JMenuItem("Set as Y");
      item.addActionListener(new ActionListener() {
    	public void actionPerformed(ActionEvent e) {
          listPanel.setYText(xlist.getSelectedValue().toString());
    	}
      });
      popup.add(item);

      return popup;
    }
    public void setDataType(final String type) {
      combo.setSelectedItem(type);
    }
    public void enableCombo(boolean enable) {
      combo.setSelectedIndex(0); 
      combo.setEnabled(enable);
    } 
    public DefaultListModel getContents() {
      return xlist.getContents();
    }
    public void setItems(final String type) {
      String str = histogrammer.getMatchStr();
      if (str == null || str.length() == 0) return;
      String [] items = str.split("\\|");
      DefaultListModel model = xlist.getContents();
      model.clear();
      for (int i = 0; i < items.length; i++)
        model.addElement(items[i]);
    }
  }
    /** Test the unit */
  public static void main(String [] argv) {
    JFrame f = new JFrame("Plot Panel test");
    f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE); 
    f.getContentPane().add(new PlotPanel());
    f.setSize(600, 600);
    f.setVisible(true);
  }
}
