package config;

import java.util.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import config.util.*;
import config.svt.*;

/**
 *
 *  Decodes SVT Hex word according to data type. At present <I>Hit, Road
 *  Packet, Svt and XFT Track and EE </I>can be decoded. 
 *  
 *  @version 1.0 May 2003
 *  @author  Subir Sarkar
 *
 */
public class DecodeWord extends DataFrame  {
  final static int WIDTH   = 800, 
                   HEIGHTA = 300,
                   HEIGHTB = 300;
  private RadioPanel radioPanel = new RadioPanel();
  private ButtonPanel buttonPanel = new ButtonPanel();
  private TextPanel inputPanel  = new TextPanel(); 
  private JSplitPane splitPane;
  private StringBuilder buf = new StringBuilder(AppConstants.MEDIUM_BUFFER_SIZE);
  private Vector<String> vec = new Vector<String>(1000);

  /** @param label   The window title */
  public DecodeWord(String label, boolean standalone) {
    super(standalone, label, true, true, -1, false);
    buildGUI();

    String filename = Tools.getEnv("SVTMON_DIR")+"/help/a_DecodeWord.html";
    setHelpFile(filename, "SVT word decoder", new Dimension(600, 300));
  }
  /** Prepares the Graphical User Interface */
  public void buildGUI() {
    addToolBar();

    JPanel panel = new JPanel(new BorderLayout());

    // The main application area in the center
    getContentPane().add(panel, BorderLayout.CENTER);

    // Create the input area
    inputPanel.setEditable(true);
    inputPanel.setBorder(Tools.etchedTitledBorder(" Input Hex Word(s) "));
    inputPanel.setMinimumSize(new Dimension(WIDTH, HEIGHTA));
    inputPanel.setPreferredSize(new Dimension(WIDTH, HEIGHTA));

    // Panel that contains the Input and Radio Panels
    JPanel p1 = new JPanel(new BorderLayout());
    p1.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
    p1.add(inputPanel, BorderLayout.CENTER);  
    p1.add(radioPanel, BorderLayout.SOUTH);

    // Now the Output area
    JPanel outputPanel = getTextPanel();
    outputPanel.setBorder(Tools.etchedTitledBorder(" Decoded Words "));
    outputPanel.setPreferredSize(new Dimension(WIDTH, HEIGHTB));

    // Now the Split Pane that contains p1 and the output area
    splitPane = Tools.createSplitPane(JSplitPane.VERTICAL_SPLIT, p1, outputPanel);
    panel.add(splitPane, BorderLayout.CENTER);

    splitPane.setDividerLocation(HEIGHTA);
    splitPane.setPreferredSize(new Dimension(WIDTH, HEIGHTA));

    // Panel that contains the Button Panel
    JPanel p2 = new JPanel(new BorderLayout());
    p2.add(buttonPanel, BorderLayout.EAST);

    panel.add(p2, BorderLayout.SOUTH);

    addStatusBar();
  }
  class ButtonPanel extends JPanel {
    ButtonPanel() {
      setLayout(new FlowLayout());

      JButton button  = Tools.createButton("Decode");
      add(button);
      button.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          String [] lines = inputPanel.getText().trim().split("\\n");

          vec.removeAllElements();
          for (int i = 0; i < lines.length; i++) {
            String [] words = Tools.split(lines[i]);
            for (int j = 0; j < words.length; j++) {
              String word;
              // So called robustness!
              if (words[j].startsWith("0x")) 
                word = words[j].substring(2);
              else 
                word = words[j];
              if (word.length() > 6) continue;

              vec.addElement(word);
            }
          }
          if (isDebugOn()) {
            System.out.println("vec.size() = " + vec.size());
            for (Iterator<String> it = vec.iterator(); it.hasNext(); )
              System.out.println(it.next());
          }
          // Append one (600000) if the last word is not an EE
          int lword = Integer.parseInt(vec.lastElement().toString(), 16);
          if ((lword & 0x600000) != 0x600000) vec.addElement(new String("600000"));

          int [] data = new int[vec.size()];
          int n = 0;
          for (Iterator<String> it = vec.iterator(); it.hasNext(); ) {
            String word = it.next();
            try {
              data[n++] = Integer.parseInt(word, 16);
            } 
            catch (NumberFormatException err) {
              err.printStackTrace();
            }
          }
          decode(data);
        }  
      });

      button = Tools.createButton("Cancel");
      add(button);
      button.addActionListener( new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          DecodeWord.this.setVisible(false);
          DecodeWord.this.dispose();
        }         
      });
    }
  }
  class RadioPanel extends JPanel implements ActionListener {
    final String [] wordType = {
      "Hit", 
      "Road", 
      "Packet", 
      "Track", 
      "XFT", 
      "EE"
    };
    ButtonGroup bGroup;
    JRadioButton [] buttons = new JRadioButton[wordType.length];
    RadioPanel() {
      bGroup  = new ButtonGroup();
      boolean [] sFlag = {false, true, false, false, false, false};
      for (int i = 0; i < buttons.length; i++)  {
        buttons[i] = Tools.radioButton(wordType[i], bGroup, sFlag[i]);
        buttons[i].addActionListener(this);
        add(buttons[i]);
      }
    }
    public void actionPerformed(ActionEvent ev) {
    }
    protected boolean isSelected(int i) {
      return buttons[i].isSelected();
    }
  }
  /**
    *  Decode according to SVT data type
    *  @param   data    Array containing hex data
    */
  public void decode(int [] data) {
    SvtEvents events = null;
    buf.setLength(0);
    if (radioPanel.isSelected(5)) {  	       // EE
      buf.append(EEWord.getTextLabels());
      for (int i = 0; i < data.length; i++) {
        if ((data[i] & 0x600000) == 0x600000) 
          buf.append(EEWord.getTextData(data[i]));
      }
    }
    else {
      if (radioPanel.isSelected(0)) {  	       // Hit
        events = new HitEvents(data); 
      } 
      else if (radioPanel.isSelected(1)) {     // Road
        events = new RoadEvents(data); 
      }
      else if (radioPanel.isSelected(2)) {     // Packet
        events = new PacketEvents(data); 
      }
      else if (radioPanel.isSelected(3)) {     // Track
        events = new TFEvents(data); 
      }
      else if (radioPanel.isSelected(4)) {     // XFT Track
        events = new XTFAEvents(data); 
      }
      buf.append(events);
    }
    if (isDebugOn()) System.out.println(buf.toString());
    getTextPanel().setText(buf.toString());
  }
  /** Test the widget */
  public static void main(String [] argv) {
    JFrame f = new DecodeWord("Decode SVTword", true);
    f.setSize(800, 650);
    f.setVisible(true);
  }
}
