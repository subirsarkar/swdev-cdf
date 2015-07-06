package svt;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import org.omg.CORBA.IntHolder;

import config.util.*;

import jsvtvme.*;

/**
 * The status register panel of the SPY. 
 * Each opened SPY has one such static panel. This instance is 
 * then the same for each opened board of this type.
 * As these variables are unique and used only on the SPY, the SPY class 
 * holds directly the relevant variables.
 * When switching between the boards, this class updates the panel with the 
 * new values.
 * @see svt.Spy
 * @version 1.0
 * @author Th. Speer
 */


public class SpySlaveErrPane extends JPanel implements SvtvmeConstants   {

  Spy spyFrame;

  private static  JButton gErrorGenSetB, gErrorGenReadB;
  private static  JButton cdfErrorGenSetB, cdfErrorGenReadB;

  private JRadioButton[] gErrorGenRB;
  private ButtonGroup gErrorGenGroup;

  private static JCheckBox[] cdfErrorGenB;
  private static JCheckBox gErrorGen2B;

  public SpySlaveErrPane(Spy parent)    {
    super();
    this.spyFrame = parent;

    setBorder(BorderFactory.createEtchedBorder());

    GridBagLayout GBL = new GridBagLayout();
    GridBagConstraints GBC = new GridBagConstraints();
    setLayout(GBL);
    GBC.insets = new Insets(0,3,0,3);

    int vPos = 0;

    /* G_ERROR Generation */
    JLabel gegL = new JLabel("G_ERROR Generation:", JLabel.LEFT);
    gegL.setForeground(Color.black);
    gErrorGenSetB = new JButton("Set");
    gErrorGenSetB.addActionListener(new ActionListener() {
      public void actionPerformed( ActionEvent event ) { 
        spyFrame.assertBoard();
        setGErrorGen();
      }
    });

    gErrorGenReadB = new JButton("Read");
    gErrorGenReadB.addActionListener(new ActionListener() {
      public void actionPerformed( ActionEvent event ) { 
        spyFrame.assertBoard();
        getGErrorGen();
      }
    });

    gErrorGen2B = new JCheckBox("G_ERROR drive to true by board", (spyFrame.gErrorGen&4)==4);

    Tools.buildConstraints(GBC,0,vPos,1,1,0.,0.,GBC.WEST,GBC.HORIZONTAL);
    add(gegL,GBC);
    Tools.buildConstraints(GBC,1,vPos,1,1,0.,0.,GBC.WEST,GBC.NONE);
    add(gErrorGen2B,GBC);
    Tools.buildConstraints(GBC,2,vPos,1,1,0.,0.,GBC.WEST,GBC.HORIZONTAL);
    add(gErrorGenSetB,GBC);
    Tools.buildConstraints(GBC,3,vPos++,1,1,0.,0.,GBC.WEST,GBC.HORIZONTAL);
    add(gErrorGenReadB,GBC);
    
    gErrorGenRB = new JRadioButton[3];
    gErrorGenGroup = new ButtonGroup();
    gErrorGenRB[0] = new JRadioButton("G_ERROR forced false");
    gErrorGenRB[1] = new JRadioButton("G_ERROR forced true");
    gErrorGenRB[2] = new JRadioButton("G_ERROR generated from SVT_ERROR in backplane");
    for (int i = 0; i < 3; i++) {
      Tools.buildConstraints(GBC,1,vPos++,3,1,0.,0.,GBC.WEST,GBC.NONE);
      add(gErrorGenRB[i],GBC);
      gErrorGenGroup.add(gErrorGenRB[i]);
    }
    gErrorGenRB[(spyFrame.gErrorGen&3)].setSelected(true);

    /* CDF_ERROR Generation */
    JLabel cegL = new JLabel("CDF_ERROR Generation:", JLabel.LEFT);
    cegL.setForeground(Color.black);
    cdfErrorGenSetB = new JButton("Set");
    cdfErrorGenSetB.addActionListener(new ActionListener() {
      public void actionPerformed( ActionEvent event ) { 
        spyFrame.assertBoard();
        setCDFErrorGen();
      }
    });

    cdfErrorGenReadB = new JButton("Read");
    cdfErrorGenReadB.addActionListener(new ActionListener() {
      public void actionPerformed( ActionEvent event ) { 
        spyFrame.assertBoard();
        getCDFErrorGen();
      }
    });
    
    cdfErrorGenB = new JCheckBox[5];
    cdfErrorGenB[0] = new JCheckBox("CDF_ERROR Flip Flop", (spyFrame.cdfErrorGen&1)==1);
    cdfErrorGenB[1] = new JCheckBox("enable SVT_ERROR", ((spyFrame.cdfErrorGen>>1)&1)==1);
    cdfErrorGenB[2] = new JCheckBox("enable SVT_LLOCK", ((spyFrame.cdfErrorGen>>2)&1)==1);
    cdfErrorGenB[3] = new JCheckBox("enable G_ERROR", ((spyFrame.cdfErrorGen>>3)&1)==1);
    cdfErrorGenB[4] = new JCheckBox("enable G_LLOCK", ((spyFrame.cdfErrorGen>>3)&1)==1);
    
    Tools.buildConstraints(GBC,0,vPos,1,1,0.,0.,GBC.WEST,GBC.HORIZONTAL);
    add(cegL,GBC);
    Tools.buildConstraints(GBC,1,vPos,1,1,0.,0.,GBC.WEST,GBC.NONE);
    add(cdfErrorGenB[0],GBC);
    Tools.buildConstraints(GBC,2,vPos,1,1,0.,0.,GBC.WEST,GBC.HORIZONTAL);
    add(cdfErrorGenSetB,GBC);
    Tools.buildConstraints(GBC,3,vPos++,1,1,0.,0.,GBC.WEST,GBC.HORIZONTAL);
    add(cdfErrorGenReadB,GBC);
    Tools.buildConstraints(GBC,1,vPos++,1,1,0.,0.,GBC.WEST,GBC.HORIZONTAL);
    add(cdfErrorGenB[1],GBC);
    Tools.buildConstraints(GBC,1,vPos++,1,1,0.,0.,GBC.WEST,GBC.HORIZONTAL);
    add(cdfErrorGenB[2],GBC);
    Tools.buildConstraints(GBC,1,vPos++,1,1,0.,0.,GBC.WEST,GBC.HORIZONTAL);
    add(cdfErrorGenB[3],GBC);
    Tools.buildConstraints(GBC,1,vPos++,1,1,0.,0.,GBC.WEST,GBC.HORIZONTAL);
    add(cdfErrorGenB[4],GBC);
  }


  /**
   * This method updates the GUI display with the new variables.
   */

  public void update() {
    getGErrorGen();
    getCDFErrorGen();
  }
  public void getGErrorGen() {
    IntHolder state = new IntHolder();
    int error = spyFrame.getBoard().getState(SC_G_ERROR_GEN_REG, state);
    History.addText(getTextPane(),"SPY G_ERROR Generation read with status " + error);
    spyFrame.gErrorGen = state.value;
    gErrorGenRB[(spyFrame.gErrorGen&3)].setSelected(true);
    gErrorGen2B.setSelected((spyFrame.gErrorGen&4)==4);
  }
  public void setGErrorGen() {
    spyFrame.gErrorGen = ((gErrorGen2B.isSelected()==true)?4:0);
    for (int i=0;i<3;i++) {
      spyFrame.gErrorGen |= ((gErrorGenRB[i].isSelected()==true)?i:0);
    }
    int error = spyFrame.getBoard().setState(SC_G_ERROR_GEN_REG, spyFrame.gErrorGen);
    History.addText(getTextPane(), "SPY G_ERROR Generation set with status " + error);
  }
  public void  getCDFErrorGen() {
    IntHolder state = new IntHolder();
    int error = spyFrame.getBoard().getState(SC_CDF_ERROR_GEN_REG, state);
    History.addText(getTextPane(), "SPY CDF_ERROR Generation read with status " + error);
    spyFrame.cdfErrorGen = state.value;
    for (int i=0;i<5;i++) {
      cdfErrorGenB[i].setSelected(((spyFrame.cdfErrorGen>>i)&1)==1);
    }
  }
  public void  setCDFErrorGen() {
    spyFrame.cdfErrorGen = 0;
    for (int i=0;i<5;i++) {
      spyFrame.cdfErrorGen |= ((cdfErrorGenB[i].isSelected()==true)?1<<i:0);
    }
    int error = spyFrame.getBoard().setState(SC_CDF_ERROR_GEN_REG, spyFrame.cdfErrorGen);
    History.addText(getTextPane(),"CDF_ERROR Generation set with status " + error);
  }
  public JTextPane getTextPane() {
    return spyFrame.getTextPane();
  }
}
