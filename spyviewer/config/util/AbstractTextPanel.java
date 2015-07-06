package config.util;

import java.util.HashMap;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.io.IOException;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.BorderLayout;
import java.awt.event.KeyEvent;

import java.awt.event.MouseListener;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JFileChooser;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import javax.swing.text.JTextComponent;
import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultEditorKit;

abstract public class AbstractTextPanel extends JPanel  {
   public static final Icon  copyIcon = new ImageIcon(AppConstants.iconDir+"editcopy.png");
   public static final Icon pasteIcon = new ImageIcon(AppConstants.iconDir+"editpaste.png");
   public static final Icon   cutIcon = new ImageIcon(AppConstants.iconDir+"editcut.png");
   public static final Icon  openIcon = new ImageIcon(AppConstants.iconDir+"fileopen_small.png");
   public static final Icon  findIcon = new ImageIcon(AppConstants.iconDir+"find.png");
   public static final Icon  saveIcon = new ImageIcon(AppConstants.iconDir+"saveas_small.png");
   public static final Icon clearIcon = new ImageIcon(AppConstants.iconDir+"editclear.png");
   public static final Icon  fontIcon = new ImageIcon(AppConstants.iconDir+"fonts_small.png");

   private static final String EMPTY = "";
   private JTextComponent textComp;
   private JPopupMenu popup;
   private JScrollPane scrollPane;
   private JFileChooser fileChooser;
   private JFrame parent;
   private Dimension d;
   private JCheckBoxMenuItem editableCB;

   private int searchIndex = 0;

   public AbstractTextPanel() {
     this(null, new Dimension(500, 400));
   }
   public AbstractTextPanel(final Dimension d) {
     this(null, d);
   }
   public AbstractTextPanel(final JFrame parent, final Dimension d) {
     this.parent = parent;
     this.d      = d;
     buildGUI();
   }
   protected void buildGUI() {
     setLayout(new BorderLayout());

     textComp = createTextComponent();
     scrollPane = new JScrollPane(textComp);
     scrollPane.setPreferredSize(d);
     add(scrollPane, BorderLayout.CENTER);
     
     fileChooser = new JFileChooser();
     fileChooser.setCurrentDirectory(new File("."));

     popup = addPopup();

     updatePopup(popup); 
     changeItemState(textComp.isEditable());

     // Add listener to components that can bring up popup menus.
     MouseListener popupListener = new PopupListener(popup);
     textComp.addMouseListener(popupListener);
     scrollPane.addMouseListener(popupListener);
   }
   protected JPopupMenu addPopup() {
     // Add popup menu
     JPopupMenu popup = new JPopupMenu();
       
     JMenuItem item = popup.add(new ReadAction());
     item.setMnemonic('p');
     popup.add(item);

     item = popup.add(new SaveAction());
     item.setMnemonic('e');
     popup.add(item);

     item = popup.add(new SaveMarkedAction());
     item.setMnemonic('m');
     popup.add(item);

     item = popup.add(new ClearAction());
     item.setMnemonic('l');
     popup.add(item);

     popup.addSeparator();

     editableCB = new JCheckBoxMenuItem("Editable ", false);
     editableCB.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent e) {
     	 textComp.setEditable(editableCB.isSelected());
         changeItemState(textComp.isEditable());
       }
     });
     popup.add(editableCB);

     item = popup.add(new FontAction());
     popup.add(item);

     return popup;
   }
   public void updatePopup(JPopupMenu popup) {
     popup.addSeparator();

     JMenuItem item = popup.add(new CopyAction());
     item.setMnemonic('c');
     item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK));
     popup.add(item);
     
     item = popup.add(new CutAction());
     item.setMnemonic('x');
     item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_MASK));
     popup.add(item);
     
     item = popup.add(new PasteAction());
     item.setMnemonic('v');
     item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_MASK));
     popup.add(item);
     
     item = popup.add(new SelectAllAction());
     item.setMnemonic('a');
     item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_MASK));
     popup.add(item);

     popup.addSeparator();

     item = popup.add(new FindAction());
     item.setMnemonic('f');
     popup.add(item);

     item = popup.add(new GotoAction());
     item.setMnemonic('g');
     popup.add(item);
   }
   // An action that opens an existing file
   public void readFile() {
     // Use JFilechooser to get filename and attempt to open and read the file into the
     // text component
     repaint();
     if (fileChooser.showOpenDialog(AbstractTextPanel.this) 
             != JFileChooser.APPROVE_OPTION)  return;
     final File fName = fileChooser.getSelectedFile();
     if (fName == null) return;
     
     Thread runner = new Thread() {
       public void run() {
         FileReader reader = null;
         try  {
           reader = new FileReader(fName);
           textComp.read(reader, null);
         }   
         catch (IOException ex)  {
           JOptionPane.showMessageDialog(AbstractTextPanel.this,
                "File Not Found", "ERROR", JOptionPane.ERROR_MESSAGE);
           ex.printStackTrace();
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
     };
     runner.start();
   }
   protected void saveText() {
     repaint();
     if (fileChooser.showSaveDialog(parent) != 
           JFileChooser.APPROVE_OPTION)  return;
     final File fName = fileChooser.getSelectedFile();
     if (fName == null) return;

     (new FileRunner(fName)).start();
   }
   class FileRunner extends Thread {
     File file;
     FileRunner(final File file) {
       this.file = file; 
     }      
     public void run() {
       FileWriter writer = null;
       try {
         writer = new FileWriter(file);
         textComp.write(writer);
       } 
       catch (IOException ex)  {
         JOptionPane.showMessageDialog(parent,
             "File Not Saved", "ERROR", JOptionPane.ERROR_MESSAGE);
         ex.printStackTrace();
       }
       finally {
         if (writer != null) {
           try {
             writer.close();
           } 
           catch (IOException x) {}
         }
       }
     }
   }
   protected void saveMarkedText() {
     if (fileChooser.showSaveDialog(parent) != 
           JFileChooser.APPROVE_OPTION)  return;
     final File fName = fileChooser.getSelectedFile();
     if (fName == null) return;

     Thread runner = new Thread() {
       public void run() {
         String text = textComp.getSelectedText();
         if (text != null) Tools.writeFile(fName.getName(), text);
       }
     };
     runner.start();
   }
   public void setTextThreaded(final String text) {
     Thread runner = new Thread() {
       public void run() {
         textComp.setText(text);
       }
     };
     runner.start();

     repaint();
   }
   public void setText(final String text) {
     textComp.setText(text);
   }
   public String getText() {
     return textComp.getText();
   }
   public boolean isEditable() {
     return textComp.isEditable();
   }
   public void setEditable(boolean editable) {
     textComp.setEditable(editable);
     editableCB.setSelected(editable);
     changeItemState(editable);
   }
   protected void changeItemState(boolean state) {
     popup.getComponent(9).setEnabled(state); 
     popup.getComponent(10).setEnabled(state); 
   }
   protected abstract JTextComponent createTextComponent();
   public abstract void setContentType(final String type);

   protected JTextComponent getTextComponent() {
     return textComp;
   }
   protected JScrollPane getScrollPane() {
     return scrollPane;
   }
   protected JPopupMenu getPopupMenu() {
     return popup;
   }
   protected JFileChooser getFileChooser() {
     return fileChooser;
   }
   class SaveAction extends AbstractAction {
     public SaveAction() { 
       super("Save", saveIcon); 
     }
     public void actionPerformed(ActionEvent e) {
       saveText();
     }
   } 
   class SaveMarkedAction extends AbstractAction {
     public SaveMarkedAction() { 
       super("Save Marked", saveIcon); 
     }
     public void actionPerformed(ActionEvent e) {
       saveMarkedText();
     }
   } 
   class ReadAction extends AbstractAction {
     public ReadAction() { 
       super("Open File", openIcon); 
     }
     public void actionPerformed(ActionEvent e) {
       readFile();
     }
   } 
   class ClearAction extends AbstractAction {
     public ClearAction() { 
       super("Clear", clearIcon); 
     }
     public void actionPerformed(ActionEvent e) {
       textComp.setText("");
     }
   } 
  // "Copy" action
  class CopyAction extends AbstractAction {
    public CopyAction() { 
      super("Copy", copyIcon); 
    }
    public void actionPerformed(ActionEvent e) {
      textComp.copy();
    }
  }
  // "Cut" action
  class CutAction extends AbstractAction {
    public CutAction() { 
      super("Cut", cutIcon); 
    }
    public void actionPerformed(ActionEvent e) {
      textComp.cut();
    }
  }
  // "Paste" action
  class PasteAction extends AbstractAction {
    public PasteAction() { 
      super("Paste", pasteIcon); 
    }
    public void actionPerformed(ActionEvent e) {
      textComp.paste();
    }
  }
  // "Select All" action
  class SelectAllAction extends AbstractAction {
    public SelectAllAction() { 
      super("Select All"); 
    }
    public void actionPerformed(ActionEvent e) {
      textComp.selectAll();
    }
  }
  // "Goto Line" action
  class GotoAction extends AbstractAction {
    public GotoAction() { 
      super("Goto Line"); 
    }
    public void actionPerformed(ActionEvent e) {
      String line = JOptionPane.showInputDialog(parent, "Goto Line:");
      if (line.equals("")) return;
      try {
        gotoLine(Integer.parseInt(line));
      }
      catch (NumberFormatException ex) { 
        System.out.println(ex.getMessage());
      }
    }
  }
  // "Select Font" action
  class FontAction extends AbstractAction {
    public FontAction() { 
	super("Choose Font", fontIcon); 
    }
    public void actionPerformed(ActionEvent e) {
      FontChooser dlg = new FontChooser(parent);
      if (dlg.getOption() == JOptionPane.OK_OPTION) {
        AttributeSet attrs = dlg.getAttributes();
        Tools.setTextAttributes((JTextPane)textComp, attrs);
      }
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
      super("Find", findIcon); 
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
  public void write(Writer out) throws IOException {
    textComp.write(out);
  }
  public void read(Reader in) throws IOException {
    textComp.read(in, null);
  }
  public void displayText(final String text) {
    Tools.displayText(textComp, text);
  }
  public void setWindowSize(final Dimension size) {
    scrollPane.setPreferredSize(size);
  }
  public void clear() {
    textComp.setText(EMPTY);
  }
  public void setTextFont(final Font f) {
    textComp.setFont(f);
  }
  public void setEnabledHScroll(boolean hScroll) {
    if (hScroll) scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    else         scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
  }
}
