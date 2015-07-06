package jsvtsim;

import org.omg.CORBA.IntHolder;

public class Ram {
  private wedgemaps_t maps;
  private int ramid;
  public Ram(wedgemaps_t maps, int ramid) {
    this.maps  = maps;
    this.ramid = ramid;
  }
  public int [] value(int offset) {
    int [] d = new int[0];
    WedgeMapsImpl.svtsim_getRam(maps, ramid, d, 1, offset);
    return d;
  }
  public long crc() {
    return WedgeMapsImpl.svtsim_crcRam(maps, ramid);
  }
}