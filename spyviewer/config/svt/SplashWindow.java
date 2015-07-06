package config.svt;

import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.Container;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.RenderingHints;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JWindow;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import config.util.AppConstants;

class SplashWindow extends JWindow {
  JProgressBar progressBar = new JProgressBar();
  JLabel l;
  public SplashWindow(String filename, JFrame f, int waitTime) {
    super(f);
    Container content = getContentPane();
    content.setLayout(new BorderLayout());
    content.setBackground(Color.white);
  
    //l = new JLabel(new ImageIcon(filename));
    StringBuilder buf = new StringBuilder();
    buf.append("<html>");
    buf.append("<h1 Align=center valign=top>SpyBufferViewer v0.9</h1>");
    buf.append("<br><br><br><br><br><br>");
    buf.append("Initializing the viewer ....");
    buf.append("</html>");

    l = new JLabel(buf.toString(), new ImageIcon(filename), JLabel.LEFT);
    l.setPreferredSize(new Dimension(550,280));
    //Set the position of its text, relative to its icon:
    l.setVerticalTextPosition(JLabel.TOP);
    l.setHorizontalTextPosition(JLabel.RIGHT);

    content.add(l, BorderLayout.NORTH);

    JPanel p = new JPanel(new FlowLayout());
    p.setBackground(new Color(0,0,0));
    content.add(p, BorderLayout.SOUTH);

    progressBar.setMaximum(100);
    progressBar.setPreferredSize(new Dimension(300,25));
    p.add(progressBar, null);

    Dimension screenSize =  Toolkit.getDefaultToolkit().getScreenSize();
    Dimension labelSize = l.getPreferredSize();
    setLocation(screenSize.width/2 - (labelSize.width/2),
                screenSize.height/2 - (labelSize.height/2));
    addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        setVisible(false);
        dispose();
      }
    });
    final int pause = waitTime;
    final Runnable closerRunner = new Runnable()  {
      public void run() {
        setVisible(false);
        dispose();
      }
    };
    Runnable waitRunner = new Runnable()  {
      public void run()  {
        try  {
	  for (int i = 0; i < 100; i++) {
            progressBar.setValue(i);
	    Thread.sleep((new Float(pause/100)).intValue());
	  }
          SwingUtilities.invokeAndWait(closerRunner);
        }
        catch (Exception e) {
          e.printStackTrace();
          // can catch InvocationTargetException
          // can catch InterruptedException
        }
      }
    };
    Thread splashThread = new Thread(waitRunner, "SplashThread");
    splashThread.start();

    //setLocationRelativeTo(null);
    pack();
    setVisible(true);
  }
  public static void main(String [] argv) {
    JFrame f = new JFrame("Splash");
    new SplashWindow(AppConstants.iconDir+"splash.png", f, 2000);

    try {
      Class.forName("config.svt.SpyBufferViewer").getMethod("main", new Class[]
	 {String[].class}).invoke(null, new Object[] {argv});
    }
    catch (Throwable e) {
      e.printStackTrace();
      System.err.flush();
      System.exit(10);
    }
  }
}

