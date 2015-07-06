package config.util;

import java.io.File;
import java.io.IOException;
import java.io.FileReader;
import java.io.FileWriter;

import java.util.HashMap;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.Container;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.JFileChooser;
import javax.swing.JComboBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import javax.swing.JColorChooser;
import javax.swing.KeyStroke;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JMenuBar;
import javax.swing.JToolBar;

import javax.swing.event.*;
import javax.swing.text.JTextComponent;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Keymap;

import javax.swing.border.*;

public class SimpleEditor extends JFrame {
  protected Dimension shortSize = new Dimension(60, 20);
  protected Dimension   medSize = new Dimension(80, 20);
  protected Dimension  longSize = new Dimension(100, 20);

  private Action     newAction = new NewAction();
  private Action    openAction = new OpenAction();
  private Action    saveAction = new SaveAction();
  private Action  saveAsAction = new SaveAsAction();
  private Action   printAction = new PrintAction();
  private Action   closeAction = new CloseAction();
  private Action    quitAction = new QuitAction();
  private Action    gotoAction = new GotoAction();
  private Action    lineAction = new LineAction();
  private Action    findAction = new FindAction();
  private Action replaceAction = new ReplaceAction();
  private Action fgColorAction = new FgColorAction();
  private Action bgColorAction = new BgColorAction();
  private Action    fontAction = new FontAction();
  private Action   aboutAction = new AboutAction();

  private JTextComponent textComp;
  private JScrollPane scrollPane;
  private HashMap<String, Action> actionHash = new HashMap<String,Action>();

  protected JFileChooser fileChooser;
  protected StatusBar statusBar;
  protected File fChoosen;
  protected JComboBox fontFamilyCB, fontNameCB, fontSizeCB;
  protected JCheckBoxMenuItem commandLine;

  protected boolean standAlone;

  /* Font related parameters */
  private String cFontFamily;
  private int cFontName, cFontSize;
  private String [] fontFamily 
      = {"Monospaced", "ItxBeng",
         "lucida bright demibold","lucida sans demibold","lucida sans demibold italic",
         "Sans", 
         "Serif", "Helvetica", "Times", "Courier", "Fixed", "Palatino"};
  private int [] fontNames   = {Font.PLAIN, Font.BOLD, Font.ITALIC};
  private String [] fontName = {"Plain","Bold","Italic"};
  private String [] fontSize = {"8", "9","10","11","12","13","14",
                                "15","16","17","18","19","20","21",
                                "22", "23", "24"};
  private int searchIndex = 0;

  // Create an editor
  public SimpleEditor(boolean standAlone) {
    super("Simple Editor");
    this.standAlone = standAlone;

    cFontFamily = fontFamily[0];
    cFontName   = fontNames[0];
    cFontSize   = Integer.parseInt(fontSize[5]);

    textComp    = createTextComponent();
    textComp.setFont(new Font(cFontFamily, cFontName, cFontSize));
    scrollPane  = new JScrollPane(textComp);
    setupCombo();
    hashDefaultActions();
    makeActionsPretty();
    updateKeymap();

    Container content = getContentPane();
    content.add(scrollPane, BorderLayout.CENTER);

    statusBar = new StatusBar();
    content.add(statusBar, BorderLayout.SOUTH);

    content.add(createToolBar(), BorderLayout.NORTH);
    setJMenuBar(createMenuBar());

    fileChooser = new JFileChooser(); 
    fileChooser.setCurrentDirectory(new File("."));

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent ev) { 
        closeWindow();
      }
    });
  }
  public void closeWindow() {
    if (standAlone) System.exit(0);
    else {
      dispose();
      setVisible(false);
    }
  }

  public void setupCombo() {
    /* Font Family  */
    int i;
    fontFamilyCB = new JComboBox();
    fontFamilyCB.setMaximumSize(longSize);
    for (i = 0; i < fontFamily.length; i++) {
      fontFamilyCB.addItem(fontFamily[i]);   
    }
    fontFamilyCB.setSelectedIndex(3);
    fontFamilyCB.setMaximumRowCount(4);
    ComboListener fontFamilyListener = new ComboListener();
    fontFamilyCB.addActionListener(fontFamilyListener);

    /* Font types  */
    fontNameCB = new JComboBox();
    fontNameCB.setMaximumSize(medSize);
    for (i = 0; i < fontName.length; i++) {
      fontNameCB.addItem(fontName[i]);   
    }
    fontNameCB.setSelectedIndex(0);
    fontNameCB.setMaximumRowCount(3);
    ComboListener fontNameListener = new ComboListener();
    fontNameCB.addActionListener(fontNameListener);

    /* Font sizes */
    fontSizeCB = new JComboBox();
    fontSizeCB.setMaximumSize(shortSize);
    for (i = 0; i < fontSize.length; i++) {
       fontSizeCB.addItem(fontSize[i]);
    }
    fontSizeCB.setSelectedIndex(2);
    fontSizeCB.setEditable(true);
    fontSizeCB.setMaximumRowCount(4);
    ComboListener fontSizeListener = new ComboListener();
    fontSizeCB.addActionListener(fontSizeListener);
  }
  // Create the JTextComponent subclass.
  protected JTextComponent createTextComponent() {
    JTextArea ta = new JTextArea();
    ta.setLineWrap(true);
    //JTextPane ta = new JTextPane();
    return ta;
  }

  // Get all of the actions defined for our text component. Hash each one by name
  // so we can look for it later.
  protected void hashDefaultActions() {
    Action[] actions = textComp.getActions();
    for (int i = 0; i < actions.length; i++) {
      String name = (String)actions[i].getValue(Action.NAME);
      actionHash.put(name, actions[i]);
    }
  }

  // Get an action by name
  protected Action getHashedAction(String name) {
    return actionHash.get(name);
  }

  // Add icons and friendly names to actions we care about
  protected void makeActionsPretty() {
    Action a;
    a = getHashedAction(DefaultEditorKit.cutAction);
    a.putValue(Action.SMALL_ICON, new ImageIcon(AppConstants.iconDir+"cut.gif"));
    a.putValue(Action.NAME, "Cut");

    a = getHashedAction(DefaultEditorKit.copyAction);
    a.putValue(Action.SMALL_ICON, new ImageIcon(AppConstants.iconDir+"copy.gif"));
    a.putValue(Action.NAME, "Copy");

    a = getHashedAction(DefaultEditorKit.pasteAction);
    a.putValue(Action.SMALL_ICON, new ImageIcon(AppConstants.iconDir+"paste.gif"));
    a.putValue(Action.NAME, "Paste");

    a = getHashedAction(DefaultEditorKit.selectAllAction);
    a.putValue(Action.NAME, "Select All");
  }

  // Add some key->Action mappings
  protected void updateKeymap() {

    // Create a new child Keymap
    Keymap map = JTextComponent.addKeymap("NextPrevMap", textComp.getKeymap());

    // Define the keystrokes to be added
    KeyStroke next = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,
      InputEvent.CTRL_MASK, false);
    KeyStroke prev = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,
      InputEvent.CTRL_MASK, false);
    KeyStroke selNext = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,
      InputEvent.CTRL_MASK|InputEvent.SHIFT_MASK, false);
    KeyStroke selPrev = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,
      InputEvent.CTRL_MASK|InputEvent.SHIFT_MASK, false);

    // Add the new mappings used DefaultEditorKit actions
    map.addActionForKeyStroke(next, getHashedAction(DefaultEditorKit.nextWordAction));
    map.addActionForKeyStroke(prev, getHashedAction(DefaultEditorKit.previousWordAction));
    map.addActionForKeyStroke(selNext, getHashedAction(DefaultEditorKit.selectionNextWordAction));
    map.addActionForKeyStroke(selPrev, getHashedAction(DefaultEditorKit.selectionPreviousWordAction));

    // Set the Keymap for the text component
    textComp.setKeymap(map);
  }

  // Create a simple JToolBar with some buttons
  protected JToolBar createToolBar() {
    JToolBar toolbar = new JToolBar();

    // Add simple actions for opening & saving
    toolbar.add(new SmallButton(getOpenAction(), statusBar, "Open a file using JFileChooser"));
    toolbar.add(new SmallButton(getSaveAction(), statusBar, "Save the file..."));

    toolbar.addSeparator();

    // Add cut/copy/paste buttons
    toolbar.add(new SmallButton(getHashedAction(DefaultEditorKit.cutAction), 
             statusBar, "Cut selected text"));
    toolbar.add(new SmallButton(getHashedAction(DefaultEditorKit.copyAction), 
             statusBar, "Copy Selected Region"));
    toolbar.add(new SmallButton(getHashedAction(DefaultEditorKit.pasteAction), 
             statusBar, "Paste from clipboard at the cursor position"));

    toolbar.addSeparator();

    toolbar.add(fontFamilyCB);
    toolbar.add(fontNameCB);
    toolbar.add(fontSizeCB);
    return toolbar;
  }

  // Create a JMenuBar with file & edit menus
  protected JMenuBar createMenuBar() {
    final JMenuBar menubar = new JMenuBar();
    JMenu file = new JMenu("File");
    file.setMnemonic('f');

    JMenuItem item = file.add(getNewAction());
    item.setMnemonic('n');
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_MASK));
    file.add(item);

    item = file.add(getOpenAction());
    item.setMnemonic('o');
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK));
    file.add(item);

    item = file.add(getSaveAction());
    item.setMnemonic('S');
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK));
    file.add(item);

    item = file.add(getSaveAsAction());
    item.setMnemonic('a');
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_MASK));
    file.add(item);

    file.addSeparator();

    item  = file.add(getPrintAction());
    item.setMnemonic('p');
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_MASK));
    file.add(item);

    file.addSeparator();

    item  = file.add(getCloseAction());
    item.setMnemonic('c');
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_MASK));
    file.add(item);

    item   = file.add(getQuitAction());
    item.setMnemonic('x');
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_MASK));
    file.add(item);

    JMenu edit = new JMenu("Edit");
    edit.setMnemonic('e');

    item = edit.add(getHashedAction(DefaultEditorKit.copyAction));
    item.setMnemonic('c');
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK));
    edit.add(item);

    item = edit.add(getHashedAction(DefaultEditorKit.cutAction));
    item.setMnemonic('t');
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_MASK));
    edit.add(item);

    item = edit.add(getHashedAction(DefaultEditorKit.pasteAction));
    item.setMnemonic('p');
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_MASK));
    edit.add(item);

    item = edit.add(getHashedAction(DefaultEditorKit.selectAllAction));
    item.setMnemonic('s');
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK));
    edit.add(item);

    edit.addSeparator();

    item = edit.add(getGotoAction());
    item.setMnemonic('g');
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_MASK));
    edit.add(item);

    item = edit.add(getLineAction());
    item.setMnemonic('l');
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_MASK));
    edit.add(item);

    item = edit.add(getFindAction());
    item.setMnemonic('f');
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_MASK));
    edit.add(item);

    item = edit.add(getReplaceAction());
    item.setMnemonic('r');
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_MASK));
    edit.add(item);

    JMenu option = new JMenu("Option");
    option.setMnemonic('o');

    item = option.add(getFgColorAction());
    new MenuHelpTextAdapter(item, "Choose text color", statusBar);
    item.setMnemonic('f');
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_MASK));
    option.add(item);

    item = option.add(getBgColorAction());
    new MenuHelpTextAdapter(item, "Choose editor background color", statusBar);
    item.setMnemonic('b');
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_MASK));
    option.add(item);

    item = option.add(getFontAction());
    new MenuHelpTextAdapter(item, "Choose Font", statusBar);
    item.setMnemonic('c');
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK));
    option.add(item);

    option.addSeparator();
    commandLine = new JCheckBoxMenuItem("Command Line");
    option.add(commandLine);

    JMenu help = new JMenu("Help");
    help.setMnemonic('h');

    item = help.add(getAboutAction());
    item.setMnemonic('a');
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_MASK));
    help.add(item);

    menubar.add(file);
    menubar.add(edit);
    menubar.add(option);
    menubar.add(help);

    return menubar;
  }

  // Subclass can override to use a different action
  protected Action getNewAction()  {return newAction;}
  protected Action getOpenAction() {return openAction;}
  protected Action getSaveAction() {return saveAction;}
  protected Action getSaveAsAction() {return saveAsAction;}
  protected Action getPrintAction()  {return printAction;}
  protected Action getCloseAction()  {return closeAction;}
  protected Action getQuitAction()   {return quitAction;}

  protected Action getGotoAction() {return gotoAction;}
  protected Action getLineAction() {return lineAction;}
  protected Action getFindAction() {return findAction;}
  protected Action getReplaceAction() {return replaceAction;}

  protected Action getFgColorAction() {return fgColorAction;}
  protected Action getBgColorAction() {return bgColorAction;}
  protected Action getFontAction() {return fontAction;}

  protected Action getAboutAction() {return aboutAction;}

  protected JTextComponent getTextComponent() {return textComp;}

  public void changeColor(String label) {
    Color color = JColorChooser.showDialog(textComp, "Color Chooser", Color.blue);
    if (color != null)  {
      if (label.equals("bg")) 
        textComp.setBackground(color);
      else if (label.equals("fg")) 
        textComp.setForeground(color);
      else
        JOptionPane.showMessageDialog(SimpleEditor.this,
        "String not useful!", "ERROR", JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
    *   Go to a specified line number in the current text area
    * 
    *   @param    lineNo   the line number specified in the dialog box
    */
  public void gotoLine(int lineNo) {
    String toSearch   = textComp.getText().trim();
    String [] toSearchArray = toSearch.split("\\n");
    if (lineNo > toSearchArray.length) lineNo = toSearchArray.length;
    String pattern  = toSearchArray[lineNo-1];
    searchIndex     = toSearch.indexOf(pattern, searchIndex);
    if (searchIndex == -1) {   // string not found
      searchIndex = 0;
      // wrap and try again
      searchIndex = toSearch.indexOf(pattern, searchIndex);  
    }
    if (searchIndex != -1) {   // if something's found
      textComp.select(searchIndex, searchIndex+pattern.length());      
      searchIndex += pattern.length(); // For next search
    } 
    else {             // reset search index
      searchIndex = 0;
    }        
  }
  public void updateDisplay() {
    textComp.setFont(new Font(cFontFamily, cFontName, cFontSize));
    textComp.repaint();
  }
  /**
    *  Listens to the combo box for font related options
    */
  class ComboListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      JComboBox cb = (JComboBox) e.getSource();
      int ch = cb.getSelectedIndex();
      if (cb.equals(fontFamilyCB)) {
        cFontFamily = fontFamily[ch];
      }
      else if (cb.equals(fontNameCB)) {
        cFontName = fontNames[ch];
      }
      else if (cb.equals(fontSizeCB)) {             
        cFontSize = Integer.parseInt(fontSize[ch]);
      }
      else {
        System.err.println("Unknown font related event!");
      }
      updateDisplay();        
    }
  }   /* class ComboListener */
  // "Goto Line" action
  class GotoAction extends AbstractAction {
    public GotoAction() { 
      super("Goto Line"); 
    }
    public void actionPerformed(ActionEvent e) {
      String line = JOptionPane.showInputDialog(null, "Goto Line:");
      if (line.equals("")) return;
      gotoLine(Integer.parseInt(line));
    }
  }
  // "What Line" action
  class LineAction extends AbstractAction {
    public LineAction() { 
      super("What line"); 
    }
    public void actionPerformed(ActionEvent e) {
    }
  }
  // "Find" action
  class FindAction extends AbstractAction {
    public FindAction() { 
      super("Find", new ImageIcon(AppConstants.iconDir+"edit_find.gif")); 
    }
    public void actionPerformed(ActionEvent e) {
    }
  }
  // "Replace" action
  class ReplaceAction extends AbstractAction {
    public ReplaceAction() { 
      super("Replace"); 
    }
    public void actionPerformed(ActionEvent e) {
    }
  }
  // "Foreground Color" action
  class FgColorAction extends AbstractAction {
    public FgColorAction() { 
      super("Foreground Color"); 
    }
    public void actionPerformed(ActionEvent e) {
      changeColor("fg");
    }
  }
  // "Background Color" action
  class BgColorAction extends AbstractAction {
    public BgColorAction() { 
      super("Background Color"); 
    }
    public void actionPerformed(ActionEvent e) {
     changeColor("bg");
    }
  }
  // "Background Color" action
  class FontAction extends AbstractAction {
    public FontAction() { 
      super("Choose Font"); 
    }
    public void actionPerformed(ActionEvent e) {
      // create a font chooser
      //FontChooser fontChooser = new FontChooser(null);
      //int retValue = fontChooser.showDialog(null);

      // get the selected font
      //if (retValue == JFontChooser.OK_OPTION) {
	//	    Font selected = fontChooser.getSelectedFont();
	//	    // do something...
	//      }
    }
  }
  // "About" action
  class AboutAction extends AbstractAction {
    public AboutAction() { 
      super("About", new ImageIcon(AppConstants.iconDir+"about-mini.gif")); 
    }
    public void actionPerformed(ActionEvent e) {
      showHelp();
    }
  }
  public void showHelp() {
    String message = 
      "A simple editor included with Spy Monitoring software\n" + 
      "which can be used to edit short plain text files.\n" + 
      "The editor is based on JTextArea and therefore\n" +
      "has several limitations, in particular font/color/size\n"+
      "can be chosen for the whole buffer only.\n\n"+
      "Subir Sarkar";
    JPanel panel = new JPanel();
    panel.setBorder(BorderFactory.createLoweredBevelBorder());
    panel.setBackground(Color.white);
    JTextArea area = new JTextArea(message, 8, 5);
    area.setEditable(false);
    panel.add(area);
    Icon icon = new ImageIcon(AppConstants.iconDir+"edit_about.gif");      
    JOptionPane.showMessageDialog(SimpleEditor.this, 
          panel, 
          "About SimpleEditor",
          JOptionPane.INFORMATION_MESSAGE, icon);
  }
  // "New" action
  class NewAction extends AbstractAction {
    public NewAction() { 
       super("New"); 
    }
    public void actionPerformed(ActionEvent e) {
       textComp.setText("");
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
  class QuitAction extends AbstractAction {
    public QuitAction() {
      super("Exit");
    }
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
       SimpleEditor.this.repaint();
       if (fileChooser.showOpenDialog(SimpleEditor.this) 
              != JFileChooser.APPROVE_OPTION)  return;
       Thread runner = new Thread() {
          public void run() {
             FileReader reader = null;
             fChoosen = fileChooser.getSelectedFile();
             try  {
                reader = new FileReader(fChoosen);
                textComp.read(reader, null);
                reader.close();
                statusBar.setText(fChoosen.getAbsolutePath() + " opened ..");
             }   
             catch (IOException ex)  {
                JOptionPane.showMessageDialog(SimpleEditor.this,
                     "File Not Found", "ERROR", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
             }
             finally {
               if (reader != null) {
                 try {
                   reader.close();
                 } catch (IOException x) {}
	       }
             } 
          }
       };
       runner.start();
    }
  }
  // An action that saves the document to a file
  class SaveAction extends AbstractAction {
    public SaveAction() {
      super("Save", new ImageIcon(AppConstants.iconDir+"Save.gif"));
    }

    public void actionPerformed(ActionEvent e)    {
       Thread runner = new Thread() {
          public void run() {
            FileWriter writer = null;
            try   {
               writer = new FileWriter(fChoosen);
               textComp.write(writer);
               statusBar.setText(fChoosen.getAbsolutePath() + " saved ..");
            } 
            catch (IOException ex)  {
               JOptionPane.showMessageDialog(SimpleEditor.this,
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

  // An action that saves the document to a file
  class SaveAsAction extends AbstractAction {
    public SaveAsAction() {
      super("Save as ...");
    }

    // Query user for a filename and attempt to open and write the text
    // component's content to the file
    public void actionPerformed(ActionEvent e)    {
       SimpleEditor.this.repaint();
       if (fileChooser.showSaveDialog(SimpleEditor.this) != 
           JFileChooser.APPROVE_OPTION)  return;
       Thread runner = new Thread() {
          public void run() {
            FileWriter writer = null;
            File fName = fileChooser.getSelectedFile();
            try   {
               writer = new FileWriter(fName);
               textComp.write(writer);
               statusBar.setText("Buffer saved to " + fChoosen.getAbsolutePath());
            } 
            catch (IOException ex)  {
               JOptionPane.showMessageDialog(SimpleEditor.this,
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
  public static void main(String[] args) {
    SimpleEditor editor = new SimpleEditor(true);
    editor.setSize(500, 550);
    editor.setVisible(true);
  }
}
