package jsvtsim;

public class SimHB implements SvtsimConstants {
  private hbsim_t hb;
  private int nlay;
  /** the "wedge" argument may disappear at some point */
  public SimHB() {
    this(6);  // default nlay = 6
  }
  public SimHB(int nlay) { 
    this.nlay = nlay;
    hb = HbSimImpl.svtsim_hb_new(nlay);
  }
  protected void finalize() {
    del();
  }
  public void del() {
    HbSimImpl.svtsim_hb_del(hb);
  }
  protected hbsim_t getHandle() {
    return hb;
  }
  public void setHandle(hbsim_t hb) {
    this.hb = hb;
  }
  public int getLayer() {
    return nlay;
  }
  /** tell HB about its input source */
  public void plugHitInput(Cable cable) {
    HbSimImpl.svtsim_hb_plugHitInput(hb, cable.getHandle());
  }
  public void plugHitInput(cable_t cable) {
    HbSimImpl.svtsim_hb_plugHitInput(hb, cable);
  }
  /** tell HB about its road source */
  public void plugRoadInput(Cable cable) {
    HbSimImpl.svtsim_hb_plugRoadInput(hb, cable.getHandle());
  }
  public void plugRoadInput(cable_t cable) {
    HbSimImpl.svtsim_hb_plugRoadInput(hb, cable);
  }
  /** tell HB about its map data */
  public void useMaps(WedgeMaps maps) {
    HbSimImpl.svtsim_hb_useMaps(hb, maps.getHandle());
  }
  public void useMaps(wedgemaps_t maps) {
    HbSimImpl.svtsim_hb_useMaps(hb, maps);
  }
  /** ask HB where to find its output data */
  public cable_t outputCable() {
    return HbSimImpl.svtsim_hb_outputCable(hb);
  }
  /** Process data from input cable */
  public void procEvent() {
    HbSimImpl.svtsim_hb_procEvent1(hb);
  }
}