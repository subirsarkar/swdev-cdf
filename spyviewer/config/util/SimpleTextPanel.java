package config.util;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JFrame;
import javax.swing.JTextPane;
import javax.swing.text.JTextComponent;

// Switch between JTextPane and JEditorPane
public class SimpleTextPanel extends AbstractTextPanel {
  private JTextPane textWidget;
  public SimpleTextPanel() {
    this(null, new Dimension(500, 400));
  }
  public SimpleTextPanel(final Dimension d) {
    this(null, d);
  }
  public SimpleTextPanel(final JFrame parent, final Dimension d) {
    super(parent, d);
    textWidget = (JTextPane) getTextComponent();
  }
  protected JTextComponent createTextComponent() {
    JTextComponent viewer = new JTextPane();
    viewer.setEditable(false);
    viewer.setFont(new Font("monospaced", Font.PLAIN, 12));

    return viewer;
  }
  public void setContentType(final String type) {
    textWidget.setContentType(type); 
  }
  public void paintComponent(Graphics g)
  {
      Graphics2D g2 = (Graphics2D)g;
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                          RenderingHints.VALUE_ANTIALIAS_ON);
      super.paintComponent(g);
  }
}
