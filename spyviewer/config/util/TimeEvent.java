package config.util;

import java.awt.Font;
import java.awt.Color;
import java.awt.BorderLayout;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.JLabel;
import javax.swing.BorderFactory;

/**
 *  A simple timer which may be used to time relevant events
 *
 *  @version 0.1, August 2000
 *  @author  Subir Sarkar
 */
public class TimeEvent extends JPanel { 

  private int limit;
    /** Tick the clock every second */
  private static final int ONE_SECOND = 1000;
    /** String indicating the timer is idle */
  private static String IDLE = "Timer Idle ...";
    /** Timer which fire events every second */
  protected Timer timer;
    /** Digital clock display */
  protected JLabel display;
    /** Time elapsed between start and stop */
  private int timeElapsed;
    /** Time in millisecond when the timer has started */
  private long timeStarted; 
    /** Time in millisecond between a start and a stop */
  private long timePeriod; 

  /** Initialise the Digital clock and set initial time */
  public TimeEvent(int limit) { 
    super(true);
    this.limit = limit;   // seconds
    timeElapsed = 0;
    setLayout(new BorderLayout());

    JPanel displayP = new JPanel(new BorderLayout());
    add(displayP, BorderLayout.NORTH);

    display = new JLabel(IDLE);
    display.setForeground(Color.black);
    display.setFont(new Font("SanSerif", Font.PLAIN, 11));
    display.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 0));
    displayP.add(display, BorderLayout.NORTH);

    timer = new Timer(0, new ActionListener() { 
      public void actionPerformed(ActionEvent event) { 
        updateDisplay();
      } 
    });
    timer.setRepeats(true);
    timer.setCoalesce(false);
  } 
  public void setLimit(int limit) {
    this.limit = limit;
  }
  public int getLimit() {
    return limit;
  }
  /** Get the timer elapsed */
  public int getTimeElapsed() {
    return timeElapsed;
  }
  /** Get the timer display */
  protected JLabel getDisplay() {
    return display;
  }
  /** Get hold of the timer instance */
  protected Timer getTimer() {
    return timer;
  }
  /** Start timer */
  public synchronized void startTiming() {
    if (timer.isRunning()) timer.stop();

    timer.setDelay(ONE_SECOND);
    timeStarted = System.currentTimeMillis();
    timer.start();
  }
  /** Stop timer */
  public synchronized void stopTiming() {
    timer.stop();
    timeElapsed = 0;

    long timeNow = System.currentTimeMillis();
    timePeriod   = timeNow - timeStarted;

    display.setText(Long.toString(timePeriod) + " ms");

    Timer oneShot = new Timer(2*ONE_SECOND, new ActionListener() { 
      public void actionPerformed(ActionEvent event) { 
        showIdle();
      } 
    });
    oneShot.setRepeats(false);
    oneShot.start();
  }
  public synchronized void restart() {
    startTiming();
  }
  /** Display 'Timer Idle ...'*/
  protected void showIdle() {
    if (!timer.isRunning()) display.setText(IDLE);
  }
  public void addActionListener(ActionListener listener) {
    timer.addActionListener(listener);
  }
  /** Helper method called to update display */
  protected synchronized void updateDisplay() { 
    timeElapsed++;
    display.setText(Integer.toString(timeElapsed) + " sec");
    if (limit > 0 && timeElapsed >= limit) {
      System.out.println("Specified Time limit " + limit + " has expired!");
    }
  } 
}
