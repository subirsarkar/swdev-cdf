package config;

import java.io.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;

import config.util.*;

/**
 *  <P>
 *  Schematic of SPYMON. Once a configuration is chosen this is the
 *  permanent display which updates status of various processes continuously.
 *  For <I>Run Control</I>, <I>SmartSockets RT Server</I> and <I>The Workstatsion</I>
 *  processes information is shallow, whereas for the crates one can navigate through
 *  all the available informations. </P>
 *  <P>Consists of the following</P>
 *  <UI>
 *    <LI> SVT Crate processes</LI>
 *    <LI> Workstatsion control processes</LI>
 *    <LI> Run Control process</LI>
 *    <LI> SmartSockets RT Server</LI>
 *  </UI>
 *   
 *  @version 0.1, January 2001
 *  @author  Subir Sarkar
 */
public class SpySchemePanel extends JPanel  {
    /** Parent frame */
  private JFrame parent;
    /** Panel which contains the buttons at the bottom */
  private ButtonPanel buttonPanel;
  private LabelPanel labelPanel;

    /** Initialise the object */
  public SpySchemePanel(JFrame parent) {
    this.parent = parent;
    buildGUI();
  }
    /** Create the user interface */
  private void buildGUI() {
    setLayout(new BorderLayout());
    setBorder(Tools.etchedTitledBorder(" Spy Monitoring Schematic "));

    add(buttonPanel = new ButtonPanel(), BorderLayout.WEST);
    add(labelPanel  = new LabelPanel(), BorderLayout.CENTER);
  }
  public void changeIcon(final Icon icon) {
    labelPanel.getLabel().setIcon(icon);
  }
  public void changeButtonColor(int index, final Color color) {
    buttonPanel.getButton(index).setBackground(color);
  }
  protected ButtonPanel getButtonPanel() {
    return buttonPanel;
  }
  protected LabelPanel getLabelPanel() {
    return labelPanel;
  }
    /** Define the button panel */
  public class ButtonPanel extends JPanel implements ActionListener {
      /** <B>SVT crate buttons</B> */
    private JButton [] crateB;
    public ButtonPanel () {
      setLayout(new GridLayout(AppConstants.nCrates,1, 0, 2));
      setBorder(BorderFactory.createEtchedBorder());
      setBackground(Color.white);
      crateB = new JButton[AppConstants.nCrates];
      for (int i = 0; i < AppConstants.nCrates; i++) {
        crateB[i] = new JButton(AppConstants.SVT_CRATE_PREFIX + i);
        crateB[i].setBackground(Color.yellow);
        crateB[i].setBorder(BorderFactory.createRaisedBevelBorder());
        crateB[i].setPreferredSize(new Dimension(100, 20));
        crateB[i].addActionListener(this);
        crateB[i].setEnabled(false);
        add(crateB[i]);
      }
    }
    public void actionPerformed(ActionEvent ev) {
      String cpuName = null;
      Component source = (Component) ev.getSource();
      if (source instanceof JButton) {
        cpuName = ((JButton) source).getText().trim();
        // if (cpuName != null) ((SpyMessenger)parent).showSpyError(cpuName);
      }
    }
    protected JButton getButton(int index) {
      return crateB[index];
    }
  }
  public class LabelPanel extends JPanel {
      /** <B>SVT Scheme label with icons </B> */
    private JLabel  label;
    private Icon    icon;
    public LabelPanel () {
      setBackground(Color.white);
      setBorder(BorderFactory.createRaisedBevelBorder());
      label = new JLabel();
      icon = new ImageIcon(AppConstants.iconDir+"svt_scheme_1.png");
      label.setIcon(icon);
      add(label);
    }
    protected JLabel getLabel() {
      return label;
    }
  }
  public static void main (String [] argv) {
    JFrame f = new JFrame("Spy Schematic");
    Container content = f.getContentPane();
    content.add(new SpySchemePanel(f));
    f.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });
    f.setSize(600, 600);
    f.setVisible(true);
  }
}
