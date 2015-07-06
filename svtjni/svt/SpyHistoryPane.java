package svt;
import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import config.util.*;

public class SpyHistoryPane extends JPanel  {
   private Spy spyFrame;
   protected JTextPane textPane;   
   protected History logger;
   protected JScrollPane scrollPane;
   public SpyHistoryPane(Spy parent) { 
     this.spyFrame = parent;
     createDisplay();
   }
   protected void createDisplay() {
     setLayout(new BorderLayout());
     setBorder(BorderFactory.createEtchedBorder());
     textPane = new JTextPane();
     textPane.setEditable(false);
     textPane.setFont(new Font("SansSerif",Font.PLAIN,16));
     scrollPane = new JScrollPane(textPane);
     scrollPane.setPreferredSize(new Dimension(500,370));
     add(scrollPane, BorderLayout.NORTH);

     logger = History.Instance();
   }
   public JTextPane getTextPane() {
     return textPane;
   }
   public static void main(String [] argv) {
      JFrame f = new JFrame();
      f.getContentPane().add(new SpyHistoryPane(null)); 
      f.setSize(700,600);
      f.setVisible(true);
   }
}
