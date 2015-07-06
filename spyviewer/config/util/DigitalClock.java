package config.util;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.BorderFactory;
import javax.swing.Timer;

import java.util.GregorianCalendar;

/**
 *  A simple digital clock which display Date.getTime on a JLabel
 *
 *  @version 0.1, August 2000
 *  @author  Subir Sarkar
 */
public class DigitalClock extends JPanel { 
    /** Timer which fire events every second */
  protected Timer timerDigital;
    /** Digital clock display */
  protected JLabel display;
    /** Tick the clock every second */
  private static final int ONE_SECOND = 1000;
    /** Calendar to obtain formatted date,time */
  private static GregorianCalendar calendar = new GregorianCalendar();
  /** Initialise the Digital clock and set initial time */
  public DigitalClock() { 
    setLayout(new BorderLayout());
    display = new JLabel(calendar.getTime().toString());
    display.setForeground(Color.black);
    display.setFont(new Font("SanSerif", Font.PLAIN, 11));
    display.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 0));
    add(display, BorderLayout.NORTH);
    timerDigital = new Timer(ONE_SECOND, new ActionListener() { 
      public void actionPerformed(ActionEvent event) { 
        updateDisplay();
      } 
    });
    timerDigital.setRepeats(true);
    timerDigital.start();
  } 
  /** Helper method called to update display */
  void updateDisplay() { 
    calendar.add(GregorianCalendar.MILLISECOND, ONE_SECOND);
    display.setText(calendar.getTime().toString());
  } 
}
