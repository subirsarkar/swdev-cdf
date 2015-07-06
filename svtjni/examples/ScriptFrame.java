import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.python.util.PythonInterpreter;
import jsvtvme.*;

public class ScriptFrame extends JFrame implements ActionListener {
  // TextArea
  private JTextPane rPane = new JTextPane();
  private JTextPane tPane = new JTextPane();
  // Button
  private JButton b = new JButton("OK"); { b.addActionListener(this); }

  // Jython interpreter!
  private PythonInterpreter interp = new PythonInterpreter();
  private JScrollPane scrollPane = new JScrollPane(tPane);
  private JScrollPane rscrollPane = new JScrollPane(rPane);
  private static final String str = "from getmod import *\n"+
                                    "print board.what() \n" +
                                    "state = IntHolder() \n"+
                                    "board.getState(MRG_TMODE, state)\n"+
                                    "print 'mode = ', state.value";
  private Board board;
    
  public ScriptFrame() {
    Container content = getContentPane();

    content.add(rscrollPane, BorderLayout.NORTH);
    rscrollPane.setPreferredSize(new Dimension(400, 150));

    JPanel p = new JPanel();
    p.setBorder(BorderFactory.createEmptyBorder(1,0,10,0));
    p.add(scrollPane);
    scrollPane.setPreferredSize(new Dimension(400, 150));
    content.add(p, BorderLayout.CENTER);
   
    p = new JPanel();
    p.add(b);
    content.add(p, BorderLayout.SOUTH);   
  
    tPane.setText(str);

    // Add a variable named 'board' to the toplevel scope of the Jython interpreter,
    // bound to the Java object board
    board = new Board("b0svttest00", 7, SvtvmeConstants.MRG);
    interp.set("board", board);
  }
    
  public void actionPerformed(ActionEvent e) {
    // When the OK button is clicked, we grab the text from the JTextPane,
    String script = tPane.getText();
    // execute it in the Jython interpreter,
    interp.exec(script);
    // prepare for the next user command
    tPane.selectAll();
    tPane.requestFocus();
  }
  public static void main(String[] args) {
    JFrame f = new ScriptFrame();
    f.pack();
    f.setVisible(true);
  }
}
   
    
  

