package jsvtsim;

public class SimHB {
  private hbsim_t _self;
  private int nlay;
  /** the "wedge" argument may disappear at some point */
  public SimHB() {
    this(6);  // default nlay = 6
  }
  public SimHB(int nlay) { 
    this.nlay = nlay;
    _self = HbSimImpl.svtsim_hb_new(nlay);
  }
  protected void finalize() {
    del();
  }
  public void del() {
    HbSimImpl.svtsim_hb_del(_self);
  }
  protected hbsim_t getHandle() {
    return _self;
  }
  public void setHandle(final hbsim_t _self) {
    this._self = _self;
  }
  public int getLayer() {
    return nlay;
  }
  /** tell HB about its input source */
  public void plugHitInput(final Cable cable) {
    plugHitInput(cable.getHandle());
  }
  public void plugHitInput(final cable_t cable) {
    HbSimImpl.svtsim_hb_plugHitInput(_self, cable);
  }
  /** tell HB about its road source */
  public void plugRoadInput(final Cable cable) {
    plugRoadInput(cable.getHandle());
  }
  public void plugRoadInput(final cable_t cable) {
    HbSimImpl.svtsim_hb_plugRoadInput(_self, cable);
  }
  /** tell HB about its map data */
  public void useMaps(final WedgeMaps maps) {
    useMaps(maps.getHandle());
  }
  public void useMaps(final wedgemaps_t maps) {
    HbSimImpl.svtsim_hb_useMaps(_self, maps);
  }
  /** ask HB where to find its output data */
  public cable_t outputCable() {
    return HbSimImpl.svtsim_hb_outputCable(_self);
  }
  /** Process data from input cable */
  public void procEvent() {
    HbSimImpl.svtsim_hb_procEvent1(_self);
  }
}
