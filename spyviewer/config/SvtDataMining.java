package config;

import config.svt.SvtCrateData;
import config.svt.SvtBoardData;
import config.svt.SvtBufferData;

public interface SvtDataMining {
  public SvtCrateData getCrateData(final String crate);
  public SvtBoardData getBoardData(final String crate, int slot, String board);
  public SvtBufferData getBufferData(final String crate, int slot,
                                     final String board, String buffer);
  public void updateBuffer(String text);
}
