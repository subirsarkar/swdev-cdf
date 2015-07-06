package config;

import java.util.*;
import java.awt.*;
import javax.swing.*;

import config.svt.*;
import config.util.*;

public class GlobalErrorSummaryPanel extends JPanel {
  private String [] spyName;
  private JLabel [] spyLbl;
  String title;
  public GlobalErrorSummaryPanel(final String crate, final String board, int slot) {
    this(crate, board, slot, "EndEvent");
  }
  public GlobalErrorSummaryPanel(final String crate, 
                                 final String board, 
                                 int slot,
                                 final String opt) 
  {
    setLayout(new BorderLayout());
    if (opt.equals("EndEvent")) 
      title = "End Event";
    else
      title = "Simulation";
    setBorder(Tools.etchedTitledBorder(" " +title + " Error "));

    JPanel panel = new JPanel();

    SvtCrateMap map        = SvtCrateMap.getInstance();
    SvtCrateData crateData = map.getCrateData(crate);

    if (crateData != null) {
      SvtBoardData boardData      = crateData.getBoardData(board, slot);
      SvtBufferData [] bufferData = boardData.getBufferData();

      int len = bufferData.length;
      panel.setLayout(new GridLayout(len, 1));
      spyLbl = new JLabel[len];

      for (int i = 0; i < len; i++) {
        // Because this is a transient window, check the status from the 
        // data structure and accordingly create the labels with correct
        // icons instead of waiting for an update event. TO be done
        spyLbl[i] = Tools.createLabel(bufferData[i].getType(), 
            (((bufferData[i].getEndEventError() >> i) & 0x1) == 0) 
                 ? AppConstants.greenBall : AppConstants.redBall, 
               JLabel.LEFT, Color.black, 
               BorderFactory.createEmptyBorder(3, 3, 3, 3));
        panel.add(spyLbl[i]);
      }
      add(panel, BorderLayout.NORTH);
    }
  }
}
