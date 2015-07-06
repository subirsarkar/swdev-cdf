package config;

import java.io.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;

import config.util.*;

/**
 *  <P>
 *  At startup the user of SPYMON should choose a session
 *  from the following</P>
 *  <UI>
 *    <LI> Standard Session</LI>
 *    <LI> Saved configuration from a previous session</LI>
 *    <LI> New session</LI>
 *    <LI> Debug Session</LI>
 *  </UI>
 *   
 *  @version 0.1, December 2000
 *  @author  Subir Sarkar
 */
public class SessionManager extends JPanel {
    /** Parent frame */
  private JFrame parent;
    /** Panel which contains the Radiobuttons */
  private RadioPanel radioPanel;
    /** Panel which contains the info area */
  private TextPanel infoPanel;
    /** Panel which contains the config file, browse button etc. */
  private FilePanel filePanel;
    /** Panel which contains the buttons at the bottom */
  private ButtonPanel buttonPanel;
    /** Session Label String */
  private static final String [] sessionString = {
    "Start the Standard Session",
    "Create a New Session",
    "Start Debugging Session",
    "Start a Previously Saved Session"
  };
  static final Dimension minSize = new Dimension(100, 60);
    /** Session Tooltip */
  private static final String [] sessionToolTip = {
    "Start the <I>default</I> session. This <B>should</B> almost always "+
    "be used during data taking. The other options are primarily <B>expert</B> "+
    "options and may not be tried unless <I>instructed</I>.",
    "Choose your own configuration <I>interactively</I> in a new configuration "+
    "window. The new configuration might be <B>saved for a future session</B>.",
    "This session turns on <B>verbose</B> mode and might be more <B>configurable</B> "+
    "than others. Should be used extensibly during <B>development phase</B>.",
    "Choose a session from a previous one <B>saved in a file</B>."
  };

    /** Initialise the object */
  public SessionManager(JFrame parent) {
    this.parent = parent;
    buildGUI();
  }
    /** Create the user interface */
  private void buildGUI() {
    setLayout(new BorderLayout());
    setBorder(Tools.etchedTitledBorder(" Session Manager "));

    JPanel panel1 = new JPanel(new BorderLayout());
    panel1.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
    add(panel1, BorderLayout.NORTH);

    panel1.add(infoPanel  = new TextPanel(minSize), BorderLayout.CENTER);
    infoPanel.setContentType("text/html");
    panel1.add(radioPanel = new RadioPanel(), BorderLayout.WEST);
    infoPanel.setPreferredSize(radioPanel.getPreferredSize());

    add(filePanel = new FilePanel(), BorderLayout.CENTER);

    JPanel panel2 = new JPanel(new BorderLayout());
    panel2.add(buttonPanel = new ButtonPanel(), BorderLayout.EAST);
    add(panel2, BorderLayout.SOUTH);
    
    infoPanel.setText(sessionToolTip[0]);
  }
    /** Define the button panel */
  public class ButtonPanel extends JPanel {
      /** <B>Previous button</B> */
    private JButton prevB;
      /** <B>Next button</B> */
    private JButton nextB;
      /** <B>Default</B> button */
    private JButton defB;
      /** <B>OK</B> button */
    private JButton okB;
    public ButtonPanel () {
      setLayout(new FlowLayout());
      setBorder(BorderFactory.createEmptyBorder(10,0,5,0));

      prevB = new JButton("<< Previous");
      prevB.setEnabled(false);
      prevB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
        }
      });

      nextB = new JButton("Next >>");
      nextB.setPreferredSize(prevB.getPreferredSize());
      nextB.setEnabled(false);
      nextB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          prevB.setEnabled(true);
        }
      });

      defB = new JButton("Default");
      defB.setPreferredSize(prevB.getPreferredSize());
      defB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          radioPanel.getRadioButton(0).setSelected(true); 
          buttonPanel.getNextButton().setEnabled(false);
          buttonPanel.getPrevButton().setEnabled(false);
          filePanel.initialise(false);
        }
      });

      okB = new JButton("Ok");
      okB.setPreferredSize(prevB.getPreferredSize());
      okB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          SessionManager.this.setVisible(false);
          insertScheme();
        }
      });

      add(prevB);
      add(nextB);
      add(defB);
      add(okB);
    }
    public JButton getPrevButton() {
      return prevB;
    }
    public JButton getNextButton() {
      return nextB;
    }
    public JButton getDefButton() {
      return defB;
    }
    public JButton getOkButton() {
      return okB;
    }
    public void insertScheme() {
      //((SpyMessenger) parent).insertScheme();
    }
  }
    /** Define the Radiobutton panel */
  public class RadioPanel extends JPanel {
      /** Radiobutton array */
    private JRadioButton [] sessionRB;
      /** Radiobutton group */
    private ButtonGroup bGroup;
    public RadioPanel () {
      setLayout(new GridLayout(sessionString.length,1));
      setBorder(BorderFactory.createEmptyBorder(5,10,5,0));
      sessionRB = new JRadioButton[sessionString.length];
      bGroup = new ButtonGroup();
      for (int i = 0; i < sessionString.length; i++) {
        sessionRB[i] = new JRadioButton(sessionString[i]);
        bGroup.add(sessionRB[i]);
        add(sessionRB[i]);
      }
      sessionRB[0].setSelected(true);
      infoPanel.setText(sessionToolTip[0]);      

      sessionRB[0].addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          infoPanel.setText(sessionToolTip[0]);      
          filePanel.initialise(false);
          buttonPanel.getNextButton().setEnabled(false);
          buttonPanel.getPrevButton().setEnabled(false);
        }
      });

      sessionRB[1].addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          infoPanel.setText(sessionToolTip[1]);      
          buttonPanel.getNextButton().setEnabled(true);
          filePanel.initialise(false);
        }
      });

      sessionRB[2].addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          infoPanel.setText(sessionToolTip[2]);      
          filePanel.initialise(false);
          buttonPanel.getNextButton().setEnabled(false);
          buttonPanel.getPrevButton().setEnabled(false);
        }
      });

      sessionRB[3].addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          infoPanel.setText(sessionToolTip[3]);      
          filePanel.initialise(true);
          buttonPanel.getNextButton().setEnabled(false);
          buttonPanel.getPrevButton().setEnabled(false);
        }
      });
    }
    public JRadioButton getRadioButton(int index) {
      return sessionRB[index];
    }
  }
    /** Define the file panel */
  public class FilePanel extends JPanel {
    private JPanel lPanel;
      /** <I>File</I> label */
    private JLabel label;
      /** Browse button */
    private JButton browseB;
      /** Input Text field */
    private JTextField fileField;
    protected JFileChooser fileChooser; 
    public FilePanel() {
      setLayout(new BorderLayout());
      setBorder(BorderFactory.createEmptyBorder(1,10,1,0));

      lPanel = new JPanel(new BorderLayout());

      JPanel panel     = new JPanel(new BorderLayout());
      lPanel.add(label = new JLabel("File:   "), BorderLayout.WEST);
      label.setForeground(Color.black);
      lPanel.add(fileField = new JTextField(38), BorderLayout.CENTER);

      panel.add(lPanel, BorderLayout.WEST);
      panel.add(Box.createHorizontalGlue());
      panel.add(browseB = new JButton("Browse"), BorderLayout.EAST);
      browseB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          openFile();
        }
      });
      add(panel, BorderLayout.NORTH);

      fileChooser = new JFileChooser(); 
      fileChooser.setCurrentDirectory(new File("."));

      initialise(false);
    }
    protected void initialise(boolean enabled) {
      label.setEnabled(enabled);
      browseB.setEnabled(enabled);
      fileField.setEnabled(enabled);
    }
    public void openFile() {
      SessionManager.this.repaint();
      if (fileChooser.showOpenDialog(SessionManager.this) != 
            JFileChooser.APPROVE_OPTION) return;
      Thread runner = new Thread() {
        public void run() {
          File fChoosen = fileChooser.getSelectedFile();
          fileField.setText(fChoosen.getAbsolutePath());
        }
      };
      runner.start();
    }
  }
    /** Define the text information panel */
  public class InfoTextPanel extends JPanel {
      /** Text component */
    private JTextComponent textComp;
      /** Scrollpane */
    private JScrollPane scrollPane;
    public InfoTextPanel() {
      setLayout(new BorderLayout());
      textComp   = createTextComponent();
      scrollPane = new JScrollPane(textComp);
      add(scrollPane, BorderLayout.CENTER);
    }
      /** 
       * Create the Text component 
       * @return Reference to the JTextComponent
       */
    protected JTextComponent createTextComponent() {
      JTextPane viewer = new JTextPane();
      viewer.setContentType("text/html");
      viewer.setEditable(false);
      return viewer;
    }
      /** 
       * Get reference to the Text component 
       * @return Reference to the JTextComponent
       */
    public JTextComponent getTextComponent() {
      return textComp;
    }
      /** 
       * Get reference to the Scrollpane which contain the text component
       * @return Reference to the JScrollPane
       */
    public JScrollPane  getScrollPane() {
      return scrollPane;
    }
  }
    /** 
     * Test the class 
     * @param argv Input argument list
     */
  public static void main (String [] argv) {
    JFrame f = new JFrame("Session Manager");
    Container content = f.getContentPane();
    content.add(new SessionManager(f));
    f.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });
    f.setSize(f.getPreferredSize());
    f.setVisible(true);
  }
}
