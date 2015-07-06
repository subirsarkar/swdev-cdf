package config.util;

import java.awt.Color;

import java.io.PrintStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.OutputStream;
import java.io.IOException;

public class ErrorLogPanel extends TextPanel {
  PrintStream printStream  =
    new PrintStream(new FilteredStream(new ByteArrayOutputStream()));

  public ErrorLogPanel() {
    System.setErr(printStream); // catches error messages
  }
  // Redirection class
  class FilteredStream extends FilterOutputStream {
    public FilteredStream(OutputStream stream) {
      super(stream);
    }
    public void write(byte b[]) throws IOException {
      String s = new String(b);
      System.out.println(s);
      addText(s, Color.blue);
    }
    public void write(byte b[], int off, int len) throws IOException {
      String s = new String(b, off, len);
      System.out.println(s);
      addText(s, Color.blue);
    }
  }
}
