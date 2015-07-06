package config.svt;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.Container;
import java.awt.Color;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JWindow;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.ImageIcon;
import javax.swing.BorderFactory;

import config.util.AppConstants;
import config.util.Tools;
import config.util.AbstractTextPanel;
import config.util.HtmlPanel;

class SplashWindow extends JWindow {
  JProgressBar progressBar = new JProgressBar();
  AbstractTextPanel textPanel = new HtmlPanel();
  String filename; 
  public SplashWindow(final String filename, JFrame f, int waitTime) {
    super(f);
    this.filename = filename; 
    Container content = getContentPane();
    content.setLayout(new BorderLayout());

    textPanel.setBorder(BorderFactory.createLoweredBevelBorder());
    textPanel.setWindowSize(new Dimension(400, 300));
    readFile();

    content.add(textPanel, BorderLayout.CENTER);

    JPanel p = new JPanel(new FlowLayout());
    p.setBackground(new Color(105,105,255));
    content.add(p, BorderLayout.SOUTH);

    progressBar.setMaximum(100);
    p.add(progressBar, null);

    Dimension screenSize =  Toolkit.getDefaultToolkit().getScreenSize();
    Dimension labelSize = textPanel.getPreferredSize();
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
  /** Read the content of the file and place them inside the text area */
  public void readFile() {
     FileReader reader = null;
     try  {
       reader = new FileReader(new File(filename));
       textPanel.read(reader);
       reader.close();
     }   
     catch (IOException ex)  {
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
  public static void main(String [] argv) {
    String filename = Tools.getEnv("SVTMON_DIR")+"/help/a_Splash.html";
    JFrame f = new JFrame("Splash");
    new SplashWindow(filename, f, 2000);

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

