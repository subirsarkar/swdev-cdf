package config.util;

import java.awt.Color;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.IOException;
import javax.swing.text.JTextComponent;
import javax.swing.SwingUtilities;

abstract public class ConsolePanel extends TextPanel {
  PipedInputStream pistream = null;
  PipedOutputStream postream = null;
  Color color; 
  public ConsolePanel() {
    this(Color.black);
  }
  public ConsolePanel(Color color) {
    this.color = color;
    try {
      pistream = new PipedInputStream();
      postream = new PipedOutputStream(pistream);

      setStream();
      new ReaderThread(pistream).start();
    }
    catch (IOException ex) {
      ex.printStackTrace();
    }
  }
  public void setTextColor(Color color) {
    this.color = color;
  }
  protected PipedOutputStream getPipedOutputStream() {
    return postream; 
  }
  protected abstract void setStream();

  class ReaderThread extends Thread {
    final int idealSize = 1000;
    final int maxExcess = 500;
    PipedInputStream pi;
    ReaderThread(PipedInputStream pi) {
      this.pi = pi;
    }
    public void run() {
      final byte[] buf = new byte[1024];
      try {
       	while (true) {
      	  final int len = pi.read(buf);
      	  if (len == -1) {
      	    break;
      	  }
      	  SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              JTextComponent comp = getTextComponent();
              addText(new String(buf, 0, len), ConsolePanel.this.color);

              // Make sure the last line is always visible
              comp.setCaretPosition(comp.getDocument().getLength());
  
              // Keep the text area down to a certain character size
              int excess = comp.getDocument().getLength() - idealSize;
              if (excess >= maxExcess) {
		  //comp.replaceRange("", 0, excess);
              }
            }
          });
      	}
      } 
      catch (IOException e) {}
    }
  }
}

