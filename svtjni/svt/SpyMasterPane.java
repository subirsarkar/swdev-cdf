package svt;

import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
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


public class SpyMasterPane extends JPanel implements SvtvmeConstants {

  Spy spyFrame;

  public JButton gInitGenSetB, gInitGenReadB;
  public JButton gFreezeDelaySetB, gFreezeDelayReadB;
  public JButton gFreezeGenSetB, gFreezeGenReadB;
  public JButton gInitPulseWriteB;
  public HexPanel gFreezeDelayHF;

  private JRadioButton[] gInitGenRB;
  private ButtonGroup gInitGenGroup;

  private JCheckBox[] gFreezeGenB;
  private JCheckBox gInitGen2B;

  public SpyMasterPane(Spy parent)    {
    super();
    this.spyFrame = parent;

    setBorder(BorderFactory.createEtchedBorder());
    GridBagLayout GBL = new GridBagLayout();
    GridBagConstraints GBC = new GridBagConstraints();
    setLayout(GBL);
    GBC.insets = new Insets(0,3,0,3);
    
    int vPos = 0;
    
    /* G_INIT Generation */
    JLabel gigL = new JLabel("G_INIT Generation:", JLabel.LEFT);
    gigL.setForeground(Color.black);
    gInitGenSetB = new JButton("Set");
    gInitGenSetB.addActionListener(new ActionListener() {
      public void actionPerformed( ActionEvent event ) { 
        spyFrame.assertBoard();
        setGInitGen();
      }
    });

    gInitGenReadB = new JButton("Read");
    gInitGenReadB.addActionListener(new ActionListener() {
      public void actionPerformed( ActionEvent event ) { 
        spyFrame.assertBoard();
        getGInitGen();
      }
    });
    
    gInitGen2B = new JCheckBox("G_INIT drive to true", (spyFrame.gInitGen&4)==4);
    
    gInitGenRB = new JRadioButton[3];
    gInitGenGroup = new ButtonGroup();
    
    Tools.buildConstraints(GBC,0,vPos,1,1,0.,1.,GBC.WEST,GBC.HORIZONTAL);
    add(gigL,GBC);
    Tools.buildConstraints(GBC,1,vPos,1,1,0.,0.,GBC.WEST,GBC.NONE);
    add(gInitGen2B,GBC);
    Tools.buildConstraints(GBC,2,vPos,1,1,0.,0.,GBC.WEST,GBC.HORIZONTAL);
    add(gInitGenSetB,GBC);
    Tools.buildConstraints(GBC,3,vPos++,1,1,0.,0.,GBC.WEST,GBC.HORIZONTAL);
    add(gInitGenReadB,GBC);
    
    gInitGenRB[0] = new JRadioButton("G_INIT cleared - response to CDF_RECOVER disabled");
    gInitGenRB[1] = new JRadioButton("G_INIT forced true - response to CDF_RECOVER disabled");
    gInitGenRB[2] = new JRadioButton("G_INIT cleared - response to CDF_RECOVER enabled");
    for (int i = 0; i < 3; i++) {
      Tools.buildConstraints(GBC,1,vPos++,3,1,0.,0.,GBC.WEST,GBC.NONE);
      add(gInitGenRB[i],GBC);
      gInitGenGroup.add(gInitGenRB[i]);
    }
    gInitGenRB[(spyFrame.gInitGen&3)].setSelected(true);

    /* G_INIT Pulse */
    JLabel pL = new JLabel("G_INIT Pulse:", JLabel.LEFT);
    pL.setForeground(Color.black);
    gInitPulseWriteB = new JButton("Send");
    gInitPulseWriteB.addActionListener(new ActionListener() {
      public void actionPerformed( ActionEvent event ) { 
        spyFrame.assertBoard();
        setGInitPulse();
      }
    });

    Tools.buildConstraints(GBC,0,vPos,1,1,0.,1.,GBC.WEST,GBC.HORIZONTAL);
    add(pL,GBC);
    Tools.buildConstraints(GBC,2,vPos++,1,1,0.,0.,GBC.WEST,GBC.HORIZONTAL);
    add(gInitPulseWriteB,GBC);

    /* G_FREEZE Generation */
    JLabel gfgL = new JLabel("G_FREEZE Generation:", JLabel.LEFT);
    gfgL.setForeground(Color.black);
    gFreezeGenSetB = new JButton("Set");
    gFreezeGenSetB.addActionListener(new ActionListener() {
      public void actionPerformed( ActionEvent event ) { 
        spyFrame.assertBoard();
        setGFreezeGen();
      }
    });
    gFreezeGenReadB = new JButton("Read");
    gFreezeGenReadB.addActionListener(new ActionListener() {
      public void actionPerformed( ActionEvent event ) { 
        spyFrame.assertBoard();
        setGFreezeGen();
      }
    });
    
    gFreezeGenB = new JCheckBox[3];
    gFreezeGenB[0] = new JCheckBox("G_FREEZE Flip Flop", (spyFrame.gFreezeGen&1)==1);
    gFreezeGenB[1] = new JCheckBox("enable G_ERROR", ((spyFrame.gFreezeGen>>1)&1)==1);
    gFreezeGenB[2] = new JCheckBox("enable G_LLOCK", ((spyFrame.gFreezeGen>>2)&1)==1);
    
    Tools.buildConstraints(GBC,0,vPos,1,1,0.,1.,GBC.WEST,GBC.HORIZONTAL);
    add(gfgL,GBC);
    Tools.buildConstraints(GBC,1,vPos,1,1,0.,0.,GBC.WEST,GBC.NONE);
    add(gFreezeGenB[0],GBC);
    Tools.buildConstraints(GBC,2,vPos,1,1,0.,0.,GBC.WEST,GBC.HORIZONTAL);
    add(gFreezeGenSetB,GBC);
    Tools.buildConstraints(GBC,3,vPos++,1,1,0.,0.,GBC.WEST,GBC.HORIZONTAL);
    add(gFreezeGenReadB,GBC);
    Tools.buildConstraints(GBC,1,vPos++,1,1,0.,0.,GBC.WEST,GBC.HORIZONTAL);
    add(gFreezeGenB[1],GBC);
    Tools.buildConstraints(GBC,1,vPos++,1,1,0.,0.,GBC.WEST,GBC.HORIZONTAL);
    add(gFreezeGenB[2],GBC);

    /* G_FREEZE Delay */
    JLabel gfdL = new JLabel("G_FREEZE Delay:", JLabel.LEFT);
    gfdL.setForeground(Color.black);
    gFreezeDelaySetB = new JButton("Set");
    gFreezeDelaySetB.addActionListener(new ActionListener() {
      public void actionPerformed( ActionEvent event ) { 
        spyFrame.assertBoard();
        setGFreezeDelay();
      }
    });

    gFreezeDelayReadB = new JButton("Read");
    gFreezeDelayReadB.addActionListener(new ActionListener() {
      public void actionPerformed( ActionEvent event ) { 
        spyFrame.assertBoard();
        setGFreezeDelay();
      }
    });
    
    gFreezeDelayHF = new HexPanel(spyFrame.gFreezeDelay, 8);
    
    Tools.buildConstraints(GBC,0,vPos,1,1,0.,1.,GBC.WEST,GBC.HORIZONTAL);
    add(gfdL,GBC);
    Tools.buildConstraints(GBC,1,vPos,1,1,0.,0.,GBC.EAST,GBC.NONE);
    add(gFreezeDelayHF,GBC);
    Tools.buildConstraints(GBC,2,vPos,1,1,0.,0.,GBC.WEST,GBC.HORIZONTAL);
    add(gFreezeDelaySetB,GBC);
    Tools.buildConstraints(GBC,3,vPos++,1,1,0.,0.,GBC.WEST,GBC.HORIZONTAL);
    add(gFreezeDelayReadB,GBC);
  }

  public void update() {
    getGInitGen();
    getGFreezeGen();
    getGFreezeDelay();
  }

    /* Read G_INIT */
  public void getGInitGen() {
    IntHolder state = new IntHolder();
    int error = spyFrame.getBoard().getState(SC_G_INIT_GEN_REG, state);
    History.addText(getTextPane(), "SPY G_INIT Generation read with status " + error);
    spyFrame.gInitGen = state.value;
    gInitGenRB[(spyFrame.gInitGen & 3)].setSelected(true);
    gInitGen2B.setSelected((spyFrame.gFreezeGen & 4) == 4);
  }
  public void setGInitGen() {
    spyFrame.gInitGen = ((gInitGen2B.isSelected() == true) ? 4 : 0);
    for (int i = 0; i < 3; i++) {
       spyFrame.gInitGen |= ((gInitGenRB[i].isSelected() == true) ? i :0);
    }
    int error = spyFrame.getBoard().setState(SC_G_INIT_GEN_REG, spyFrame.gInitGen);
    History.addText(getTextPane(),"SPY G_INIT Generation set with status " + error);
  }

  public void setGInitPulse() {
    int error = spyFrame.getBoard().setState(SC_G_INIT_PULSE_REG, 1);
    History.addText(getTextPane(), "SPY G_INIT Pulse sent with status " + error);
  }

  public void getGFreezeGen() {
    IntHolder state = new IntHolder();  
    int error;

    error = spyFrame.getBoard().getState(SC_G_FREEZE_GEN_REG, state);
    History.addText(getTextPane(),"SPY G_FREEZE Generation read with status " + error);
    spyFrame.gFreezeGen = state.value;
    for (int i = 0; i < 3; i++) {
      gFreezeGenB[i].setSelected(((spyFrame.gFreezeGen>>i)&1)==1);
    }
  }

  public void setGFreezeGen() {
    spyFrame.gFreezeGen = 0;
    for (int i = 0; i < 3; i++) {
      spyFrame.gFreezeGen |= ((gFreezeGenB[i].isSelected()==true)?1<<i:0);
    }
    int error = spyFrame.getBoard().setState(SC_G_FREEZE_GEN_REG, spyFrame.gFreezeGen);
    History.addText(getTextPane(),"G_FREEZE Generation set done with status " + error);
  }

  public void getGFreezeDelay() {
    IntHolder state = new IntHolder();
    int error = spyFrame.getBoard().getState(SC_G_FREEZE_DELAY_REG, state);
    History.addText(getTextPane(),"SPY G_FREEZE delay read with status " + error);
    spyFrame.gFreezeDelay = state.value;
    gFreezeDelayHF.setInt(spyFrame.gFreezeDelay);
  }

  public void setGFreezeDelay() {
    spyFrame.gFreezeDelay = gFreezeDelayHF.getInt();
    int error = spyFrame.getBoard().setState(SC_G_FREEZE_DELAY_REG, spyFrame.gFreezeDelay);
    History.addText(getTextPane(),"SPY G_FREEZE delay set with status" + error);
  }

  public JTextPane getTextPane() {
    return spyFrame.getTextPane();
  }
}
