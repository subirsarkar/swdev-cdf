package config.util;

import java.awt.Dimension;
import java.awt.Container;
import java.awt.Color;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.BorderLayout;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.BorderFactory;

import javax.swing.border.*;

/**
 *
 *  Displays JVM memory usage online, runs on a thread different 
 *  than the main
 *   
 *  @version 1.0
 *  @author  Subir Sarkar
 */
public class MemoryTest extends DataFrame implements Runnable {
  private Thread memoryThread = null;
  private long timeInterval   = 100000;
  private TextPanel textPanel;  
  private JNumberField nfield;
  private static final long MEMORY_MIN = 500000;

  /** Construct MemoryTest
   *  @param mode        Window title 
   *  @param standalone  true if standalone
   */
  public MemoryTest(String mode, boolean standalone) {
    super(standalone, mode, false, true, -1);
    buildGUI();

    String filename = Tools.getEnv("SVTMON_DIR")+"/help/a_MemoryTest.html";
    setHelpFile(filename, "About Memory Test Window", new Dimension(500, 400));

    if (memoryThread == null) {
      memoryThread = new Thread(this, "Memory Test");
      memoryThread.start();
    }
  }
  /**
   * Create display for the application
   */
  public void buildGUI() {
    Container content = getContentPane();

    addHelpMenu();
    addHelpInToolBar();

    addToolBar();

    JPanel panel = new JPanel(new BorderLayout());
    content.add(panel, BorderLayout.CENTER);

    textPanel = getTextPanel();
    textPanel.setBorder(Tools.etchedTitledBorder(" Message Logger "));
    textPanel.setPreferredSize(new Dimension(180, 180));
    panel.add(textPanel, BorderLayout.CENTER);

    ButtonPanel bp = new ButtonPanel();
    panel.add(bp, BorderLayout.SOUTH);

    addStatusBar();
  }
  class ButtonPanel extends JPanel {
    ButtonPanel() {
      super(true);
      buildGUI();
    }
    void buildGUI() {   
      setLayout(new BorderLayout());
      setBorder(BorderFactory.createEmptyBorder(1,1,1,1));

      JPanel panel = new JPanel(new BorderLayout()); 

      JPanel p1 = new JPanel(new BorderLayout());
      p1.setBorder(BorderFactory.createEmptyBorder(5, 1, 4, 1));

      JLabel label = new JLabel("Update freq (ms): ");
      label.setFont(new Font("SansSerif", Font.PLAIN, 10));
      label.setForeground(Color.black);

      nfield = new JNumberField(Long.toString(timeInterval), 8);
      nfield.setPreferredSize(new Dimension(130, 20));
      nfield.setBorder(BorderFactory.createLoweredBevelBorder());
      nfield.setHorizontalAlignment(JNumberField.RIGHT);
      nfield.setFont(new Font("SansSerif", Font.PLAIN, 10));
      nfield.setBackground(Color.white);
      nfield.setEditable(true);
      nfield.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          timeInterval = Long.parseLong(nfield.getText());
        }
      });       

      p1.add(label, BorderLayout.WEST);
      p1.add(nfield, BorderLayout.CENTER);

      JPanel p2 = new JPanel(new FlowLayout());
      JButton button = new JButton("Change");
      button.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          timeInterval = Long.parseLong(nfield.getText());
        }
      });
      p2.add(button);

      button = new JButton("Yield");
      button.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          memoryThread.yield();
        }
      });
      p2.add(button);

      /* Add the components to the panel */
      panel.add(p1, BorderLayout.CENTER);
      panel.add(p2, BorderLayout.EAST);

      add(panel, BorderLayout.EAST);
    }
  }
  /**
   * Implements run() method of runnable
   */
  public void run () {
    Thread currThread = Thread.currentThread();
    while (memoryThread == currThread) {
      long  freeNow = -1;
      Runtime rt = Runtime.getRuntime();
      long total = rt.totalMemory();
      textPanel.warn("Total JVM Memory at start: <" + total + "> bytes");
      long freeAtStart = rt.freeMemory();
      textPanel.warn("Memory free at start: <" + freeAtStart + "> bytes");
      do {
        long totalNow  = rt.totalMemory();
        freeNow   = rt.freeMemory();
        if (freeNow < MEMORY_MIN) {
          String message = "Free Memory: <" + freeNow + "> bytes. Garbage collection takes over! (" +
                           Tools.getTimeString() + ")";
          textPanel.warn(message, Color.red);
          rt.gc();
        }
        try {
          currThread.sleep((long)(Math.random() * timeInterval));
        } catch (InterruptedException e) {
          textPanel.addText("Sleep interrupted! ...", Color.red);
        }
        updateWindow(totalNow, freeNow);
        if (textPanel.getText().length() > 10000) textPanel.setText("");
      } while (freeNow > 1000);
    }
  }
  /**
   * Update display with stretched total memory and available memory
   * 
   * @param totalMemory        Total memory available to JVM
   * @param freeMemory         Memory available at that moment
   */
  public void updateWindow(long totalMemory, long freeMemory) {
     getStatusBar().setText("Total/Free Memory: " 
         + Long.toString(totalMemory) + "/" + Long.toString(freeMemory) + " bytes (" + 
         Tools.getTimeString() + ")");
     timeInterval = Long.parseLong(nfield.getText());
  }
  /* Test the class */
  public static void main(String [] argv) {
    JFrame f = new MemoryTest("Memory Test", true);
    f.setSize(600, 400);
    f.setVisible(true);
  }
}
