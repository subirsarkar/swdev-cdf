package config.util;

import java.awt.Container;
import java.awt.BorderLayout;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 *  Test the simple timer with 'start' and 'stop' buttons
 *
 *  @version 0.1, Feb 2002
 *  @author  Subir Sarkar
 */
public class TimeEventFrame extends JFrame { 
  private TimeEvent timerPanel;
  public TimeEventFrame() { 
    super("Timing an Event");

    Container content = getContentPane();
    
    timerPanel = new TimeEvent(-1);
    timerPanel.setBorder(BorderFactory.createEtchedBorder());
    content.add(timerPanel, BorderLayout.CENTER);

    JPanel panel = new JPanel(new BorderLayout());
    content.add(panel, BorderLayout.SOUTH);

    JButton button = new JButton("Start");
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        timerPanel.startTiming();
      }
    });
    panel.add(button, BorderLayout.WEST);

    button = new JButton("Stop");
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        timerPanel.stopTiming();
      }
    });
    panel.add(button, BorderLayout.EAST);
  } 
  /** Test the class */
  public static void main(String [] argv) {
    JFrame f = new TimeEventFrame();
    f.setSize(f.getPreferredSize());
    f.setVisible(true);
  }
}
