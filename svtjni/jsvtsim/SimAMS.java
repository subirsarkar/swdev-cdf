package jsvtsim;

public class SimAMS {
  private amssim_t _self;
  private int wedge;
  /** the "wedge" argument may disappear at some point */
  public SimAMS(int wedge) { 
    this.wedge = wedge;
    _self = AmsSimImpl.svtsim_ams_new(wedge);
    // System.out.println(SVTSIM_AMS_UCODE_4_OUT_OF_4);
    AmsSimImpl.svtsim_ams_setUcode(_self,  SvtsimConstants.SVTSIM_AMS_UCODE_4_OUT_OF_4);
  }
  protected void finalize() {
    del();
  }
  public void del() {
    AmsSimImpl.svtsim_ams_del(_self);
  }
  protected amssim_t getHandle() {
    return _self;
  }
  public int getWedge() {
    return wedge;
  }
  /** tell AMS about its input source */
  public void plugInput(final Cable cable) {
    plugInput(cable.getHandle());
  }
  public void plugInput(final cable_t cable) {
    AmsSimImpl.svtsim_ams_plugInput(_self, cable);
  }
  /** tell AMS about its map data */
  public void useMaps(final WedgeMaps maps) {
    useMaps(maps.getHandle());
  }
  public void useMaps(final wedgemaps_t maps) {
    AmsSimImpl.svtsim_ams_useMaps(_self, maps);
  }
  /** ask AMS where to find its output data */
  public cable_t outputCable() {
    return AmsSimImpl.svtsim_ams_outputCable(_self);
  }
  /** Process data from input cable */
  public void procEvent() {
    AmsSimImpl.svtsim_ams_procEvent1(_self);
  }
}
