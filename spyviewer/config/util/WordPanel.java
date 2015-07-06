package config.util;

import java.awt.Font;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.BorderFactory;

public class WordPanel extends JPanel {
  private JLabel wordLBL;
  private JTextField wordTF;
  private static final Font cfont = new Font("SansSerif", Font.PLAIN, 12);
  public WordPanel (String titl, String text, int width, int gap, int align) {
    super(true);
    setLayout(new BorderLayout());
      
    JPanel panel = new JPanel();
    add(panel, BorderLayout.WEST);
      
    panel.add(wordLBL = new JLabel(titl));
    wordLBL.setForeground(Color.black);
    wordLBL.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, gap));

    panel.add(wordTF = new JTextField(text));
    wordTF.setBackground(Color.white);
    wordTF.setBorder(BorderFactory.createLoweredBevelBorder());
    wordTF.setPreferredSize(new Dimension(width, 22));
    wordTF.setHorizontalAlignment(align);
    wordTF.setFont(cfont);
  }
  public void setEditable(boolean dec) {
    wordTF.setEditable(dec);
  }
  protected JLabel getLabel() {
    return wordLBL;
  }
  protected JTextField getTextField() {
    return wordTF;
  }
  public String getText() {
    return wordTF.getText();
  }
  public void setText(String text) {
    wordTF.setText(text);
  }
  public void setAlignment(int align) {
    wordTF.setHorizontalAlignment(align);
  }
  public void addHelp(final String helpText) {
    wordTF.setToolTipText(helpText);
  }
}
