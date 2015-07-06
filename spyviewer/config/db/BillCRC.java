package config.db;

import java.io.IOException;
import jsvtsim.Dict;

import java.sql.SQLException;

public class BillCRC {
  static {
    try {
      System.loadLibrary("SvtsimImpl");
    } 
    catch (UnsatisfiedLinkError e) {
      e.printStackTrace();
      System.err.println("Cannot load the native code. " + 
        " \nMake sure your LD_LIBRARY_PATH contains the shared lib path, " + 
        "*** CRC does not work otherwise! ***");
      // System.exit(1);
    }
  }
  public static long compute(final String filename) throws SQLException, IOException {
    SvtDbBrowser.saveFile(filename);
    String key = new String();

    if      (filename.indexOf("hwset")  != -1)  key = "hwsetCrc";
    else if (filename.indexOf("mapset") != -1) key = "mapsetCrc";

    Dict d   = new Dict(-1);
    int ret  = d.addFile(filename);
    long crc = d.crc(key);
    d.free();

    return crc;
  }
}
