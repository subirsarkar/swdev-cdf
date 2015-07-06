package config;

import java.util.*;

import com.smartsockets.*;
import config.util.*;

public class RCState {
  private int runNumber;
  private String sender = "Unknown";
  private long utime;
  private String stateManager = "Unknown";
  private int partition;
  private String daqState = "Unknown";

  private int partitionToWatch = 0;

  public RCState() {
    this(0);
  }
  public RCState(int partitionToWatch) {
    this.partitionToWatch = partitionToWatch;    
  }
  public void update(TipcMsg msg) {
    try {
      msg.setCurrent(0);  // position the field ptr to the beginning of the message
      String str = msg.nextStr();
      String [] fields = Tools.split(str, " ");
      int len = fields.length;
      partition = Integer.parseInt(fields[len-5]);
      if (partition == partitionToWatch && len >= 7) {
        runNumber     = Integer.parseInt(fields[len-1]);
        sender        = fields[len-2];
        utime         = Long.parseLong(fields[len-3]);
        stateManager  = fields[len-4];
        daqState      = fields[0];
        for (int i = 1; i < len-5; i++) {
          daqState = daqState.concat(" " + fields[i]);
        }
      }
    }
    catch (TipcException ex) {
      Tut.warning(ex);
    }
  }
  public String toString() {
    StringBuilder buf = new StringBuilder(AppConstants.SMALL_BUFFER_SIZE);

    buf.append("   Run Number = ").append(runNumber).append("\n");
    buf.append("     DAQ Time = ").append(new Date(utime)).append("\n");
    buf.append("State Machine = ").append(stateManager).append("\n");
    buf.append("    Partition = ").append(partition).append("\n");
    buf.append("   DAQ Status = ").append(daqState).append("\n");

    return buf.toString();
  }
  public void setRunNumber(int runNumber) {
    this.runNumber = runNumber;
  }
  public int getRunNumber() {
    return runNumber;
  }
  public void setUtime(long utime) {
    this.utime = utime;
  }
  public long getUtime() {
    return utime;
  }
  public void setPartition(int partition) {
    this.partition = partition;
  }
  public int getPartition() {
    return partition;
  }
  public void setDaqState(final String daqState) {
    this.daqState = daqState;
  }
  public String getDaqState() {
    return daqState;
  }
  public void setRCStateInfo(int partition, 
                             int runNumber, 
                             long utime, 
                             final String daqState) 
  {
    this.partition = partition;
    this.runNumber = runNumber;
    this.utime     = utime;
    this.daqState  = daqState;
  }
}
