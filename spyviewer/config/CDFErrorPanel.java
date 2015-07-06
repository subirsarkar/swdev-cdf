package config;

import java.util.*;
import java.awt.*;
import javax.swing.*;

import config.svt.*;
import config.util.*;

public class CDFErrorPanel extends JPanel {
  protected JLabel [] errorLbl;
  protected String [] berrors = {
                       "CDF Error", 
                       "SVT Error"
                      };
  public CDFErrorPanel(final String crate, final String board, int slot) {
    setLayout(new BorderLayout());
    setBorder(Tools.etchedTitledBorder(" CDF/SVT Errors "));
    
    JPanel panel = new JPanel(new GridLayout(berrors.length, 1));
    errorLbl     = new JLabel[berrors.length];

    SvtCrateMap map        = SvtCrateMap.getInstance();
    SvtCrateData crateData = map.getCrateData(crate);
    if (crateData != null) {
      SvtBoardData data = crateData.getBoardData(board, slot);
      int [] errs = {data.getCDFError(), data.getSVTError()};
      for (int i = 0; i < Math.min(errs.length, berrors.length); i++) {
        errorLbl[i] = Tools.createLabel(berrors[i], 
               (errs[i] == 0) ? AppConstants.greenBall 
                              : AppConstants.redBall, 
               JLabel.LEFT, Color.black, BorderFactory.createEmptyBorder(3, 3, 3, 3));
        panel.add(errorLbl[i]);
      }
      add(panel, BorderLayout.NORTH);
    }
  }
}
