package config.util;

import java.awt.Font;

public class HtmlPanel extends TextPanel {
  public HtmlPanel() {
    setTextFont(new Font("SanSerif", Font.PLAIN, 12));
    setContentType("text/html");
  }
}
