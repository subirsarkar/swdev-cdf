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


public class SpySlaveStatPane extends JPanel
                           implements SvtvmeConstants   {

  Spy spyFrame;

  private static JButton gBusInputReadB, backplaneReadB, cdfRecoverReadB;
  private static JCheckBox[] gBusInputB, backplaneB, cdfRecoverB;

  public SpySlaveStatPane(Spy parent)    {
    super();
    this.spyFrame = parent;

    setBorder(BorderFactory.createEtchedBorder());

    GridBagLayout GBL = new GridBagLayout();
    GridBagConstraints GBC = new GridBagConstraints();
    setLayout(GBL);
    GBC.insets = new Insets(0,3,0,3);

    int vPos = 0;

    /* G_BUS Input Status */

    JLabel gbiL = new JLabel("G_BUS Input Status:", JLabel.LEFT);
    gbiL.setForeground(Color.black);
    gBusInputReadB = new JButton("Read");
    gBusInputReadB.addActionListener(new ActionListener() {
      public void actionPerformed( ActionEvent event ) { 
        spyFrame.assertBoard();
        getGBusStatus();
      }
    });

    gBusInputB    = new JCheckBox[4];
    gBusInputB[0] = new JCheckBox("SVT_FREEZE Flip Flop", (spyFrame.gBusInput&1)==1);
    gBusInputB[1] = new JCheckBox("enable SVT_ERROR", ((spyFrame.gBusInput>>1)&1)==1);
    gBusInputB[2] = new JCheckBox("enable SVT_LLOCK", ((spyFrame.gBusInput>>2)&1)==1);
    gBusInputB[3] = new JCheckBox("enable G_FREEZE", ((spyFrame.gBusInput>>3)&1)==1);

    Tools.buildConstraints(GBC,0,vPos,1,1,0.,1.,GBC.WEST,GBC.HORIZONTAL);
    add(gbiL,GBC);
    Tools.buildConstraints(GBC,1,vPos,1,1,0.,0.,GBC.WEST,GBC.NONE);
    add(gBusInputB[0],GBC);
    Tools.buildConstraints(GBC,3,vPos++,1,1,0.,0.,GBC.WEST,GBC.HORIZONTAL);
    add(gBusInputReadB,GBC);

    Tools.buildConstraints(GBC,1,vPos++,1,1,0.,0.,GBC.WEST,GBC.HORIZONTAL);
    add(gBusInputB[1],GBC);
    Tools.buildConstraints(GBC,1,vPos++,1,1,0.,0.,GBC.WEST,GBC.HORIZONTAL);
    add(gBusInputB[2],GBC);
    Tools.buildConstraints(GBC,1,vPos++,1,1,0.,0.,GBC.WEST,GBC.HORIZONTAL);
    add(gBusInputB[3],GBC);

    /* Backplane Status */
    JLabel bsL = new JLabel("Backplane Status:", JLabel.LEFT);
    bsL.setForeground(Color.black);
    backplaneReadB = new JButton("Read");
    backplaneReadB.addActionListener(new ActionListener() {
      public void actionPerformed( ActionEvent event ) { 
        spyFrame.assertBoard();
        getBackplaneStatus();  
      }
    });

    backplaneB = new JCheckBox[4];
    backplaneB[0] = new JCheckBox("SVT_FREEZE Flip Flop", (spyFrame.backplane&1)==1);
    backplaneB[1] = new JCheckBox("enable SVT_ERROR", ((spyFrame.backplane>>1)&1)==1);
    backplaneB[2] = new JCheckBox("enable SVT_LLOCK", ((spyFrame.backplane>>2)&1)==1);
    backplaneB[3] = new JCheckBox("enable G_FREEZE", ((spyFrame.backplane>>3)&1)==1);

    Tools.buildConstraints(GBC,0,vPos,1,1,0.,1.,GBC.WEST,GBC.HORIZONTAL);
    add(bsL,GBC);
    Tools.buildConstraints(GBC,1,vPos,1,1,0.,0.,GBC.WEST,GBC.NONE);
    add(backplaneB[0],GBC);
    Tools.buildConstraints(GBC,3,vPos++,1,1,0.,0.,GBC.WEST,GBC.HORIZONTAL);
    add(backplaneReadB,GBC);
    Tools.buildConstraints(GBC,1,vPos++,1,1,0.,0.,GBC.WEST,GBC.HORIZONTAL);
    add(backplaneB[1],GBC);
    Tools.buildConstraints(GBC,1,vPos++,1,1,0.,0.,GBC.WEST,GBC.HORIZONTAL);
    add(backplaneB[2],GBC);
    Tools.buildConstraints(GBC,1,vPos++,1,1,0.,0.,GBC.WEST,GBC.HORIZONTAL);
    add(backplaneB[3],GBC);


    /* CDF_RECOVER Status */
    JLabel crsL = new JLabel("CDF_RECOVER/CDF_RUN Status:", JLabel.LEFT);
    crsL.setForeground(Color.black);
    cdfRecoverReadB = new JButton("Read");
    cdfRecoverReadB.addActionListener(new ActionListener() {
      public void actionPerformed( ActionEvent event ) { 
        spyFrame.assertBoard();
        getCDFRecoveryStatus(); 
      }
    });

    cdfRecoverB = new JCheckBox[2];
    cdfRecoverB[0] = new JCheckBox("CDF_RECOVER", (spyFrame.cdfRecover&1)==1);
    cdfRecoverB[1] = new JCheckBox("CDF_RUN", ((spyFrame.cdfRecover>>1)&1)==1);

    Tools.buildConstraints(GBC,0,vPos,1,1,0.,1.,GBC.WEST,GBC.HORIZONTAL);
    add(crsL,GBC);
    Tools.buildConstraints(GBC,1,vPos,1,1,0.,0.,GBC.WEST,GBC.NONE);
    add(cdfRecoverB[0],GBC);
    Tools.buildConstraints(GBC,3,vPos++,1,1,0.,0.,GBC.WEST,GBC.HORIZONTAL);
    add(cdfRecoverReadB,GBC);
    Tools.buildConstraints(GBC,1,vPos++,1,1,0.,0.,GBC.WEST,GBC.HORIZONTAL);
    add(cdfRecoverB[1],GBC);
  }

  /**
   * Method to read all the status registers from the board and update the
   * local variables and the display
   */
  public void update() {
    getGBusStatus();
    getBackplaneStatus();  
    getCDFRecoveryStatus(); 
  }

    /* G_BUS Input Status */
  public void getGBusStatus() {
    IntHolder state = new IntHolder();

    int error = spyFrame.getBoard().getState(SC_G_BUS_INP_REG, state);
    spyFrame.gBusInput = state.value;

    History.addText(getTextPane(),"SPY G_BUS Input Status read with status " + error);
    for (int i = 0; i < 4; i++) {
      gBusInputB[i].setSelected(((spyFrame.gBusInput>>i)&1)==1);
    }
  }

    /* Backplane Status */
  public void getBackplaneStatus() {
    IntHolder state = new IntHolder();

    int error = spyFrame.getBoard().getState(SC_BACKPLANE_REG, state);
    spyFrame.backplane = state.value;

    History.addText(getTextPane(),"SPY Backplane Status read with status " + error);
    for (int i = 0;i < 4; i++) {
      backplaneB[i].setSelected(((spyFrame.backplane>>i)&1)==1);
    }
  }
    /* CDF_RECOVER Status */
  public void getCDFRecoveryStatus() {
    IntHolder state = new IntHolder();

    int error = spyFrame.getBoard().getState(SC_CDF_RECOVER_REG, state);
    spyFrame.cdfRecover = state.value;

    History.addText(getTextPane(),"SPY CDF_RECOVERY Status read with status " + error);
    for (int i = 0; i < 2; i++) {
      cdfRecoverB[i].setSelected(((spyFrame.cdfRecover>>i)&1)==1);
    }
  }
  public JTextPane getTextPane() {
    return spyFrame.getTextPane();
  }
}
