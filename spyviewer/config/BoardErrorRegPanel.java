package config;

import java.util.*;
import java.awt.*;

import javax.swing.*;

import config.svt.*;
import config.util.*;

public class BoardErrorRegPanel extends JPanel {
  protected JLabel [] errorLbl;
  protected JLabel [] errorTotLbl;
  String [] berrors;
  public BoardErrorRegPanel(final String crate, final String board, int slot) {
    setLayout(new BorderLayout());
    setBorder(Tools.etchedTitledBorder(" Error Regs "));
      
    berrors = (String[]) SvtCratesFrame.berrMap.get(board);

    JPanel panel = new JPanel(new GridLayout(berrors.length, 2));

    SvtCrateMap map        = SvtCrateMap.getInstance();
    SvtCrateData crateData = map.getCrateData(crate);
    if (crateData != null) {
      int errBit    = crateData.getBoardData(board, slot).getErrorRegister(0);
      int [] errTot = crateData.getBoardData(board, slot).getErrorCounters();

      errorLbl    = new JLabel[errTot.length];
      errorTotLbl = new JLabel[errTot.length];

      for (int i = 0; i < Math.min(errTot.length, berrors.length); i++) {
        // Most recently read Board Error Register values
        errorLbl[i] = Tools.createLabel(berrors[i], ((errBit >> i & 0x1 ) == 0) 
                        ? AppConstants.greenBall : AppConstants.redBall, 
                        JLabel.LEFT, Color.black, BorderFactory.createEmptyBorder(3, 3, 3, 3));
        panel.add(errorLbl[i]);

        // Total occurrance of errors since last reset of the container
        errorTotLbl[i] = Tools.createLabel(errTot[i] + " ", null, JLabel.RIGHT, 
                              ((errTot[i] > 0) ? Color.red : Color.black), 
                              BorderFactory.createEmptyBorder(3, 3, 3, 3));
        panel.add(errorTotLbl[i]);
      }
      add(panel, BorderLayout.NORTH);
    }
  }
}
