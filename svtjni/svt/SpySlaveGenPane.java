package svt;

import javax.swing.*;
import javax.swing.event.*;
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


public class SpySlaveGenPane extends JPanel implements SvtvmeConstants {

  Spy spyFrame;

  public static  JButton svtInitGenSetB, svtInitGenReadB;
  public static  JButton gLlockGenSetB, gLlockGenReadB;
  public static  JButton svtFreezeDelaySetB, svtFreezeDelayReadB;
  public static  JButton svtFreezeGenSetB, svtFreezeGenReadB;
  public static  JButton lvl1CounterSetB, lvl1CounterReadB;
  public static  JButton svtInitPulseWriteB;
  public static  HexPanel svtFreezeDelayHF, lvl1CounterHF;

  private JRadioButton[] svtInitGenRB, gLlockGenRB;
  private ButtonGroup svtInitGenGroup, gLlockGenGroup;

  private static JCheckBox[] svtFreezeGenB;
  private static JCheckBox gLlockGen2B;

  public SpySlaveGenPane(Spy parent)    {
    super();
    this.spyFrame = parent;

    setBorder(BorderFactory.createEtchedBorder());

    GridBagLayout GBL = new GridBagLayout();
    GridBagConstraints GBC = new GridBagConstraints();
    setLayout(GBL);
    GBC.insets = new Insets(0,3,0,3);
    
    int vPos = 0;

    /* SVT_INIT Generation */
    JLabel sigL = new JLabel("SVT_INIT Generation:", JLabel.LEFT);
    sigL.setForeground(Color.black);

    svtInitGenSetB = new JButton("Set");
    svtInitGenSetB.addActionListener(new ActionListener() {
      public void actionPerformed( ActionEvent event ) { 
        spyFrame.assertBoard();
        setSVTInitGen();
      }
    });

    svtInitGenReadB = new JButton("Read");
    svtInitGenReadB.addActionListener(new ActionListener() {
      public void actionPerformed( ActionEvent event ) { 
        spyFrame.assertBoard();
        getSVTInitGen();
      }
    });
    
    Tools.buildConstraints(GBC,0,vPos,1,1,0.,1.,GBC.WEST,GBC.HORIZONTAL);
    add(sigL,GBC);
    Tools.buildConstraints(GBC,2,vPos,1,1,0.,0.,GBC.WEST,GBC.HORIZONTAL);
    add(svtInitGenSetB,GBC);
    Tools.buildConstraints(GBC,3,vPos,1,1,0.,0.,GBC.WEST,GBC.HORIZONTAL);
    add(svtInitGenReadB,GBC);
    
    svtInitGenRB = new JRadioButton[3];
    svtInitGenGroup = new ButtonGroup();
    svtInitGenRB[0] = new JRadioButton("SVT_INIT forced false");
    svtInitGenRB[1] = new JRadioButton("SVT_INIT forced true");
    svtInitGenRB[2] = new JRadioButton("SVT_INIT generated when G_INIT");
    for (int i = 0; i < 3; i++) {
      Tools.buildConstraints(GBC,1,vPos++,3,1,0.,0.,GBC.WEST,GBC.NONE);
      add(svtInitGenRB[i],GBC);
      svtInitGenGroup.add(svtInitGenRB[i]);
    }
    svtInitGenRB[(spyFrame.svtInitGen&3)].setSelected(true);

    /* SVT_INIT Pulse */
    JLabel sipL = new JLabel("SVT_INIT Pulse:", JLabel.LEFT);
    sipL.setForeground(Color.black);
    svtInitPulseWriteB = new JButton("Send");
    svtInitPulseWriteB.addActionListener(new ActionListener() {
      public void actionPerformed( ActionEvent event ) { 
        spyFrame.assertBoard();
        setSVTInitPulse();
      }
    });
    
    Tools.buildConstraints(GBC,0,vPos,1,1,0.,1.,GBC.WEST,GBC.HORIZONTAL);
    add(sipL,GBC);
    Tools.buildConstraints(GBC,2,vPos++,1,1,0.,0.,GBC.WEST,GBC.HORIZONTAL);
    add(svtInitPulseWriteB,GBC);

    /* G_LLOCK Generation */
    JLabel glgL = new JLabel("G_LLOCK Generation:", JLabel.LEFT);
    glgL.setForeground(Color.black);

    gLlockGenSetB = new JButton("Set");
    gLlockGenSetB.addActionListener(new ActionListener() {
      public void actionPerformed( ActionEvent event ) { 
        spyFrame.assertBoard();
        setGLLOCKGen();
      }
    });

    gLlockGenReadB = new JButton("Read");
    gLlockGenReadB.addActionListener(new ActionListener() {
      public void actionPerformed( ActionEvent event ) { 
        spyFrame.assertBoard();
        getGLLOCKGen();
      }
    });
    
    gLlockGen2B = new JCheckBox("G_LLOCK drive true", (spyFrame.gLlockGen&4)==4);
    
    Tools.buildConstraints(GBC,0,vPos,1,1,0.,1.,GBC.WEST,GBC.HORIZONTAL);
    add(glgL,GBC);
    Tools.buildConstraints(GBC,1,vPos,1,1,0.,0.,GBC.WEST,GBC.NONE);
    add(gLlockGen2B,GBC);
    Tools.buildConstraints(GBC,2,vPos,1,1,0.,0.,GBC.WEST,GBC.HORIZONTAL);
    add(gLlockGenSetB,GBC);
    Tools.buildConstraints(GBC,3,vPos++,1,1,0.,0.,GBC.WEST,GBC.HORIZONTAL);
    add(gLlockGenReadB,GBC);
    
    gLlockGenRB = new JRadioButton[3];
    gLlockGenGroup = new ButtonGroup();
    gLlockGenRB[0] = new JRadioButton("G_LLOCK forced false");
    gLlockGenRB[1] = new JRadioButton("G_LLOCK forced true");
    gLlockGenRB[2] = new JRadioButton("G_LLOCK generated from SVT_LLOCK in backplane");
    for (int i = 0; i < 3; i++) {
      Tools.buildConstraints(GBC,1,vPos++,3,1,0.,0.,GBC.WEST,GBC.NONE);
      add(gLlockGenRB[i],GBC);
      gLlockGenGroup.add(gLlockGenRB[i]);
    }
    gLlockGenRB[(spyFrame.gLlockGen&3)].setSelected(true);

    /* SVT_FREEZE Generation */
    JLabel gfgL = new JLabel("SVT_FREEZE Generation:", JLabel.LEFT);
    gfgL.setForeground(Color.black);

    svtFreezeGenSetB = new JButton("Set");
    svtFreezeGenSetB.addActionListener(new ActionListener() {
      public void actionPerformed( ActionEvent event ) { 
        spyFrame.assertBoard();
        setSVTFreezeGen();
      }
    });

    svtFreezeGenReadB = new JButton("Read");
    svtFreezeGenReadB.addActionListener(new ActionListener() {
      public void actionPerformed( ActionEvent event ) { 
        spyFrame.assertBoard();
        getSVTFreezeGen();
      }
    });
    
    svtFreezeGenB = new JCheckBox[4];
    svtFreezeGenB[0] = new JCheckBox("SVT_FREEZE Flip Flop", (spyFrame.svtFreezeGen&1)==1);
    svtFreezeGenB[1] = new JCheckBox("enable SVT_ERROR", ((spyFrame.svtFreezeGen>>1)&1)==1);
    svtFreezeGenB[2] = new JCheckBox("enable SVT_LLOCK", ((spyFrame.svtFreezeGen>>2)&1)==1);
    svtFreezeGenB[3] = new JCheckBox("enable G_FREEZE", ((spyFrame.svtFreezeGen>>3)&1)==1);
    
    Tools.buildConstraints(GBC,0,vPos,1,1,0.,1.,GBC.WEST,GBC.HORIZONTAL);
    add(gfgL,GBC);
    Tools.buildConstraints(GBC,1,vPos,1,1,0.,0.,GBC.WEST,GBC.NONE);
    add(svtFreezeGenB[0],GBC);
    Tools.buildConstraints(GBC,2,vPos,1,1,0.,0.,GBC.WEST,GBC.HORIZONTAL);
    add(svtFreezeGenSetB,GBC);
    Tools.buildConstraints(GBC,3,vPos++,1,1,0.,0.,GBC.WEST,GBC.HORIZONTAL);
    add(svtFreezeGenReadB,GBC);
    Tools.buildConstraints(GBC,1,vPos++,1,1,0.,0.,GBC.WEST,GBC.HORIZONTAL);
    add(svtFreezeGenB[1],GBC);
    Tools.buildConstraints(GBC,1,vPos++,1,1,0.,0.,GBC.WEST,GBC.HORIZONTAL);
    add(svtFreezeGenB[2],GBC);
    Tools.buildConstraints(GBC,1,vPos++,1,1,0.,0.,GBC.WEST,GBC.HORIZONTAL);
    add(svtFreezeGenB[3],GBC);

    /* SVT_FREEZE Delay */
    JLabel sfdL = new JLabel("SVT_FREEZE Delay:", JLabel.LEFT);
    sfdL.setForeground(Color.black);

    svtFreezeDelaySetB = new JButton("Set");
    svtFreezeDelaySetB.addActionListener(new ActionListener() {
      public void actionPerformed( ActionEvent event ) { 
        spyFrame.assertBoard();
        setSVTFreezeDelay();
      }
    });

    svtFreezeDelayReadB = new JButton("Read");
    svtFreezeDelayReadB.addActionListener(new ActionListener() {
      public void actionPerformed( ActionEvent event ) { 
        spyFrame.assertBoard();
        getSVTFreezeDelay();
      }
    });
    
    svtFreezeDelayHF = new HexPanel(spyFrame.svtFreezeDelay, 8);
    
    Tools.buildConstraints(GBC,0,vPos,1,1,0.,1.,GBC.WEST,GBC.HORIZONTAL);
    add(sfdL,GBC);
    Tools.buildConstraints(GBC,1,vPos,1,1,0.,0.,GBC.EAST,GBC.NONE);
    add(svtFreezeDelayHF,GBC);
    Tools.buildConstraints(GBC,2,vPos,1,1,0.,0.,GBC.WEST,GBC.HORIZONTAL);
    add(svtFreezeDelaySetB,GBC);
    Tools.buildConstraints(GBC,3,vPos++,1,1,0.,0.,GBC.WEST,GBC.HORIZONTAL);
    add(svtFreezeDelayReadB,GBC);

    /* LEVEL 1 Counter */
    JLabel lcL = new JLabel("LEVEL 1 Counter:", JLabel.LEFT);
    lcL.setForeground(Color.black);

    lvl1CounterSetB = new JButton("Set");
    lvl1CounterSetB.addActionListener(new ActionListener() {
      public void actionPerformed( ActionEvent event ) { 
        spyFrame.assertBoard();
        setL1Counter();
      }
    });

    lvl1CounterReadB = new JButton("Read");
    lvl1CounterReadB.addActionListener(new ActionListener() {
      public void actionPerformed( ActionEvent event ) { 
        spyFrame.assertBoard();
        getL1Counter();
      }
    });
    
    lvl1CounterHF = new HexPanel(spyFrame.lvl1Counter, 8);
    
    Tools.buildConstraints(GBC,0,vPos,1,1,0.,1.,GBC.WEST,GBC.HORIZONTAL);
    add(lcL,GBC);
    Tools.buildConstraints(GBC,1,vPos,1,1,0.,0.,GBC.EAST,GBC.NONE);
    add(lvl1CounterHF,GBC);
    Tools.buildConstraints(GBC,2,vPos,1,1,0.,0.,GBC.WEST,GBC.HORIZONTAL);
    add(lvl1CounterSetB,GBC);
    Tools.buildConstraints(GBC,3,vPos++,1,1,0.,0.,GBC.WEST,GBC.HORIZONTAL);
    add(lvl1CounterReadB,GBC);
  }

  public void getSVTInitGen() {
    IntHolder state = new IntHolder();
    int error = spyFrame.getBoard().getState(SC_SVT_INIT_GEN_REG, state);
    History.addText(getTextPane(),"SPY SVT_INIT Generation read with status " + error);
    spyFrame.svtInitGen = state.value;
    svtInitGenRB[(spyFrame.svtInitGen&3)].setSelected(true);
  }
  public void setSVTInitGen() {
    spyFrame.svtInitGen = 0;
    for (int i=0;i<3;i++) {
      spyFrame.svtInitGen |= ((svtInitGenRB[i].isSelected()==true)?i:0);
    }
    int error = spyFrame.getBoard().setState(SC_SVT_INIT_GEN_REG, spyFrame.svtInitGen);
    History.addText(getTextPane(), "SPY SVT_INIT Generation set with status " + error);
  }
  public void setSVTInitPulse() {
    int error = spyFrame.getBoard().setState(SC_SVT_INIT_PULSE_REG, 1);
    History.addText(getTextPane(), "SPY SVT_INIT Pulse with status " + error);
  }
  public void getGLLOCKGen() {
    IntHolder state = new IntHolder();
    int error = spyFrame.getBoard().getState(SC_G_LLOCK_GEN_REG, state);
    History.addText(getTextPane(), "SPY G_LLOCK Generation read with status " + error);
    spyFrame.gLlockGen = state.value;
    gLlockGenRB[(spyFrame.gLlockGen&3)].setSelected(true);
    gLlockGen2B.setSelected((spyFrame.gLlockGen&4)==4);
  }
  public void setGLLOCKGen() {
    spyFrame.gLlockGen = ((gLlockGen2B.isSelected()==true)?4:0);
    for (int i=0;i<3;i++) {
      spyFrame.gLlockGen |= ((gLlockGenRB[i].isSelected()==true)?i:0);
    }
    int error = spyFrame.getBoard().setState(SC_G_LLOCK_GEN_REG, spyFrame.gLlockGen);
    History.addText(getTextPane(), "SPY G_LLOCK Generation set with status " + error);
  }
  public void getSVTFreezeGen() {
    IntHolder state = new IntHolder();
    int error = spyFrame.getBoard().getState(SC_SVT_FREEZE_GEN_REG, state);
    History.addText(getTextPane(), "SPY SVT_FREEZE Generation read with status " + error);
    spyFrame.svtFreezeGen = state.value;
    for (int i=0;i<4;i++) {
      svtFreezeGenB[i].setSelected(((spyFrame.svtFreezeGen>>i)&1)==1);
    }
  }
  public void setSVTFreezeGen() {
    spyFrame.svtFreezeGen = 0;
    for (int i=0;i<4;i++) {
      spyFrame.svtFreezeGen |= ((svtFreezeGenB[i].isSelected()==true)?1<<i:0);
    }
    int error = spyFrame.getBoard().setState(SC_SVT_FREEZE_GEN_REG, spyFrame.svtFreezeGen);
    History.addText(getTextPane(),"SVT_FREEZE Generation set with status " + error);
  }
  public void getSVTFreezeDelay() {
    IntHolder state = new IntHolder();
    int error = spyFrame.getBoard().getState(SC_SVT_FREEZE_DELAY_REG, state);
    History.addText(getTextPane(), "SPY SVT_FREEZE delay read with status " + error);
    spyFrame.svtFreezeDelay = state.value;
    svtFreezeDelayHF.setInt(spyFrame.svtFreezeDelay);
  }
  public void setSVTFreezeDelay() {
    spyFrame.svtFreezeDelay = svtFreezeDelayHF.getInt();
    int error = spyFrame.getBoard().setState(SC_SVT_FREEZE_DELAY_REG, spyFrame.svtFreezeDelay);
    History.addText(getTextPane(), "SPY SVT_FREEZE delay set with status " + error);
  }
  public void getL1Counter() {
    IntHolder state = new IntHolder();
    int error = spyFrame.getBoard().getState(SC_LVL1_COUNT_REG, state);
    History.addText(getTextPane(), "SPY LEVEL 1 Counter read with status " + error);
    spyFrame.lvl1Counter = state.value;
    lvl1CounterHF.setInt(spyFrame.lvl1Counter);
  }
  public void setL1Counter() {
    spyFrame.lvl1Counter = lvl1CounterHF.getInt();
    int error = spyFrame.getBoard().setState(SC_LVL1_COUNT_REG, spyFrame.lvl1Counter);
    History.addText(getTextPane(), "SPY LEVEL 1 Counter set with status " + error);
  }
  /**
   * This method updates the GUI display with the new variables.
   */
  public void update() {
    getSVTInitGen();
    getGLLOCKGen();
    getSVTFreezeGen();
    getSVTFreezeDelay();
    getL1Counter();
  } 
  public JTextPane getTextPane() {
    return spyFrame.getTextPane();
  }
}
