import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import jsvtvme.*;

/**
  *  Watch the backplane freeze status continuously 
  *  svtvme functions are used through vision calls from workstation 
  * 
  *  @version 0.1     07/01/2000
  *  @author  Subir Sarkar
  */
public class FreezeMonitor extends JFrame 
                implements Runnable, ActionListener, SvtvmeConstants {
  private Thread freezeThread = null;
  private JTextField numberField;
  private JPanel messagePanel, updatePanel, buttonPanel;
  private JButton yieldB, quitB, readTimeB;
  private JLabel message, numberLabel;
  protected final Icon redBall    = new ImageIcon("red-ball.gif"),
                       greenBall  = new ImageIcon("green-ball.gif"),
                       yellowBall = new ImageIcon("yellow-ball.gif");
  protected boolean isFrozen;
  protected long timeInterval = 10000;
  private Board spyB; 

  static {
    try {
        System.loadLibrary("SvtvmeImpl");
    } catch (UnsatisfiedLinkError e) {
      System.err.println("Cannot load the example native code. " + 
        " \nMake sure your LD_LIBRARY_PATH contains \'.\'\n" + e);
      System.exit(1);
    }
  }
  /* Constructor */
  /** @param mode            The window title 
    * @param freezeHandle    Reference to the Spy handle  
    */
  public FreezeMonitor(String crate, int slot) {
     super("Freeze Monitor");
     createDisplay();
     spyB = new Board(crate, slot, SC);

     if (freezeThread == null) {
       freezeThread = new Thread(this, "Freeze Monitor");
       freezeThread.start();
     }
  }
  /** Prepare user interface */
  public void createDisplay() {
    this.addWindowListener(new WindowAdapter() {  
        public void windowClosing(WindowEvent e) {
           System.exit(0);
        }
    });
    Container content = getContentPane();

    /* Setup the global layout manager */
    GridBagLayout GBL = new GridBagLayout();
    content.setLayout(GBL);

    GridBagConstraints GBC = new GridBagConstraints();
    setFont(new Font("SansSerif", Font.PLAIN, 14));

    /* Message display */
    messagePanel = new JPanel(); 
    messagePanel.setBackground(Color.white);
    messagePanel.setForeground(Color.black);
    messagePanel.setBorder(new TitledBorder(new LineBorder(Color.black, 1),
           "", TitledBorder.CENTER, TitledBorder.TOP));
    GridBagLayout GBL1 = new GridBagLayout();  // local layout manager
    messagePanel.setLayout(GBL1);

    message = new JLabel("Starting Freeze Line Monitoring, Please wait ...", 
               yellowBall, JLabel.LEFT);
    /* Add */
    buildConstraints(GBC,0,0,1,1,1.,0.,GBC.WEST,GBC.HORIZONTAL);
    GBL1.setConstraints(message,GBC);
    messagePanel.add(message);

    /* Update frequency */
    updatePanel = new JPanel(new FlowLayout());   // local layout manager
    numberLabel = new JLabel("Monitoring Frequency in ms:");
    numberLabel.setForeground(Color.black);

    numberField = new JTextField(Long.toString(timeInterval), 8);
    numberField.setHorizontalAlignment(JTextField.RIGHT);
    numberField.setFont(new Font("SansSerif",Font.PLAIN,10));
    numberField.setBackground(Color.white);
    numberField.setEditable(true);
    numberField.setActionCommand("Interval");       
    numberField.addActionListener(this);       

    readTimeB = createButton("Change");
    readTimeB.addActionListener(this);

    /* Add the components to the panel */
    updatePanel.add(numberLabel);
    updatePanel.add(numberField);
    updatePanel.add(readTimeB);
    
    /* Butons */
    buttonPanel = new JPanel();
    buttonPanel.setLayout(new FlowLayout());  // local layout manager

    yieldB  = createButton("Yield");
    yieldB.addActionListener(this);

    quitB    = createButton("Quit");
    quitB.addActionListener(this);

    buttonPanel.add(yieldB);
    buttonPanel.add(quitB);

    /* Add the panels to the window */
    buildConstraints(GBC,0,0,1,1,1.,0.,GBC.NORTH,GBC.HORIZONTAL);
    GBL.setConstraints(messagePanel,GBC);

    buildConstraints(GBC,0,1,1,1,1.,0.,GBC.NORTH,GBC.HORIZONTAL);
    GBL.setConstraints(updatePanel,GBC);

    buildConstraints(GBC,0,2,1,1,1.,0.,GBC.NORTH,GBC.HORIZONTAL);
    GBL.setConstraints(buttonPanel,GBC);

    content.add(messagePanel);
    content.add(updatePanel);
    content.add(buttonPanel);
  }
  /** Implement run() from the Runnable interface */
  public void run () {
    Thread currThread = Thread.currentThread();
    while (freezeThread == currThread) {
      updateWindow(); // the first time
      do {
         try {
	    // System.out.println("Time Interval: " + timeInterval);
            currThread.sleep((long)(Math.random() * timeInterval));
         } catch (InterruptedException e) {
            System.out.println("FreezeMonitor: Sleep interrupted! ...\n");
         }
         updateWindow();
      } while (true);
    }
  }
  /** Update freeze status */
  public void updateWindow() {
     org.omg.CORBA.IntHolder state = new org.omg.CORBA.IntHolder();
     int error = spyB.getState(SC_BACKPLANE_FREEZE, state);
     if (state.value == 1) 
       isFrozen = true;
     else
       isFrozen = false;                
     timeInterval = Long.parseLong(numberField.getText());
     setLabel(isFrozen);
  }
  /** Change the freeze label according to state 
    * @param isFrozen  Check whether freeze is on
    */
  public void setLabel(boolean isFrozen) {
    if (isFrozen) {
      message.setIcon(redBall);
      message.setText("Freeze is on! Stop at crossroad ..");
    }
    else {
      message.setIcon(greenBall);
      message.setText("Time to send freeze, hurry up!");
    }
  }
  /** Implement action listener from ActionListener interface */
  public void actionPerformed(ActionEvent ev) {
    Component source = (Component) ev.getSource();
    if (source instanceof JButton) {
      String label = ((JButton) source).getText();
      if (label.equals("yield")) {
        freezeThread.yield();
      }  
      else if (label.equals("Quit")) {
        if (freezeThread.isAlive()) freezeThread.interrupt();
        freezeThread = null;
        System.exit(0);
      }
      else if (label.equals("Change")) {
        timeInterval = Long.parseLong(numberField.getText());
      }
    }
  }
  /** 
    *   Create a JButton and return a reference to it
    * 
    *   @param label  String label attached to the button and action command
    *   @return the reference to the button
    */
  public static JButton createButton (String label) {
     return createButton (label, label);
  }
  /** 
    *   Create a JButton and return a reference to it
    * 
    *   @param label       String label attached to the button
    *   @param command     Command to be executed
    *   @return the reference to the button
    */
  public static JButton createButton (String label, String command) {
     JButton button = new JButton(label);
     button.setActionCommand(command);
     return button;
  }
  public static void buildConstraints(GridBagConstraints gbc, int gx, int gy,
                                  int gw, int gh, double wx, 
                                  double wy, int anchor, int fill) {
    gbc.gridx      = gx;
    gbc.gridy      = gy;
    gbc.gridwidth  = gw;
    gbc.gridheight = gh;
    gbc.weightx    = wx;
    gbc.weighty    = wy;
    gbc.anchor     = anchor;
    gbc.fill       = fill;
  }

  /* Test the class */
  public static void main(String [] argv) {
     JFrame f = new FreezeMonitor("b0svt05.fnal.gov", 3);
     f.setVisible(true);
     f.pack();
  }
}
