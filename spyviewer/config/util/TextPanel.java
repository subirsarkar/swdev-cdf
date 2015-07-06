package config.util;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Color;

import javax.swing.JTextPane;
import javax.swing.JFrame;
import javax.swing.text.JTextComponent;

public class TextPanel extends AbstractTextPanel {
  JTextPane textWidget;
  public TextPanel() {
    this(null, new Dimension(500, 400));
  }
  public TextPanel(final Dimension d) {
    this(null, d);
  }
  public TextPanel(final JFrame parent, final Dimension d) {
    super(parent, d);
    textWidget = (JTextPane) getTextComponent();
    textWidget.setFont(new Font("monospaced", Font.PLAIN, 13));
  }
  protected JTextComponent createTextComponent() {
    JTextComponent viewer = new JTextPane();
    viewer.setEditable(false);

    return viewer;
  }
  public void warn(final String text) {
    History.warn(textWidget, text);
  }
  public void warn(final String text, Color color) {
    History.warn(textWidget, text, color);
  }
  public void addText(String text, String fontFamily, 
                      String fontType, int fontSize, Color fgColor)
  {
    History.addText(textWidget, text, fontFamily, fontType, fontSize, fgColor);
  }
  public void addText(String text, Color fgColor) {
    History.addText(textWidget, text, fgColor);
  }
  public void setContentType(final String type) {
    textWidget.setContentType(type); 
  }
}
