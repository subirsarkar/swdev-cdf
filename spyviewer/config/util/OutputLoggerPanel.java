package config.util;

import java.io.PrintStream;

public class OutputLoggerPanel extends ConsolePanel {
  public void setStream() {
    System.setOut(new PrintStream(getPipedOutputStream(), true));
  }
}
