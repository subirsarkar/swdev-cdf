package jsvtsim;

public class Cable {
  private cable_t cable;
  private boolean cableIsMine = true;
  /** with no arg, create a new cable; with arg, access an existing cable */
  public Cable() {
    this.cable = CableImpl.svtsim_cable_new();
    this.cableIsMine = true;
  }
  public Cable(cable_t cable) {
    this.cable = cable;
    this.cableIsMine = false;
  }
  protected cable_t getHandle() {
    return cable;
  }
  public boolean isCableMine() {
    return cableIsMine;
  }
  /** Free underlying allocated space */
  protected void finalize() {
    del();
  }
  protected void del() {
    if (cableIsMine)
      CableImpl.svtsim_cable_del(cable);
  }
  /** Return number of words on cable */
  public int ndata() {
    return CableImpl.cable_ndata(cable);
  }
  /** Access one word of cable data */
  public int datum(final int i) {
    return CableImpl.cable_datum(cable, i);
  }
  /** Return array of cable data  */
  public int [] data() {
    int [] arr = new int[ndata()];
    for (int i = 0; i < arr.length; i++) 
      arr[i] = datum(i);
    return arr;
  }
  /** Replace cable data with given array */
  public void copyWords(final int [] words) {
    CableImpl.svtsim_cable_copywords(cable, words, words.length);
  }
  /** Append given data to cable data */
  public void addWords(final int [] words) {
    CableImpl.svtsim_cable_addwords(cable, words, words.length);
  }
}