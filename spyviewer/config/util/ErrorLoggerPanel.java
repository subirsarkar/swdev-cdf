package config.util;

import java.awt.Color;
import java.io.PrintStream;

public class ErrorLoggerPanel extends ConsolePanel {
  public void setStream() {
    setTextColor(Color.blue);
    //System.setErr(new PrintStream(getPipedOutputStream(), true)); // autoflush
  }
}
