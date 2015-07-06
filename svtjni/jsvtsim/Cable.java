package jsvtsim;

public class Cable {
  private cable_t _self;
  private boolean cableIsMine = true;
  /** with no arg, create a new cable; with arg, access an existing cable */
  public Cable() {
    this._self = CableImpl.svtsim_cable_new();
    this.cableIsMine = true;
  }
  public Cable(cable_t _self) {
    this._self = _self;
    this.cableIsMine = false;
  }
  protected cable_t getHandle() {
    return _self;
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
      CableImpl.svtsim_cable_del(_self);
  }
  /** Return number of words on cable */
  public int ndata() {
    return CableImpl.cable_ndata(_self);
  }
  /** Access one word of cable data */
  public int datum(int i) {
    return CableImpl.cable_datum(_self, i);
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
    CableImpl.svtsim_cable_copywords(_self, words, words.length);
  }
  /** Append given data to cable data */
  public void addWords(final int [] words) {
    CableImpl.svtsim_cable_addwords(_self, words, words.length);
  }
}
