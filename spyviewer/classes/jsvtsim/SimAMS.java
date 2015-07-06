package jsvtsim;

public class SimAMS implements SvtsimConstants {
  private amssim_t ams;
  private int wedge;
  /** the "wedge" argument may disappear at some point */
  public SimAMS(int wedge) { 
    this.wedge = wedge;
    ams = AmsSimImpl.svtsim_ams_new(wedge);
    //    System.out.println(SVTSIM_AMS_UCODE_4_OUT_OF_4);
    AmsSimImpl.svtsim_ams_setUcode(ams, SVTSIM_AMS_UCODE_4_OUT_OF_4);
  }
  protected void finalize() {
    del();
  }
  public void del() {
    AmsSimImpl.svtsim_ams_del(ams);
  }
  protected amssim_t getHandle() {
    return ams;
  }
  public int getWedge() {
    return wedge;
  }
  /** tell AMS about its input source */
  public void plugInput(Cable cable) {
    AmsSimImpl.svtsim_ams_plugInput(ams, cable.getHandle());
  }
  public void plugInput(cable_t cable) {
    AmsSimImpl.svtsim_ams_plugInput(ams, cable);
  }
  /** tell AMS about its map data */
  public void useMaps(WedgeMaps maps) {
    AmsSimImpl.svtsim_ams_useMaps(ams, maps.getHandle());
  }
  public void useMaps(wedgemaps_t maps) {
    AmsSimImpl.svtsim_ams_useMaps(ams, maps);
  }
  /** ask AMS where to find its output data */
  public cable_t outputCable() {
    return AmsSimImpl.svtsim_ams_outputCable(ams);
  }
  /** Process data from input cable */
  public void procEvent() {
    AmsSimImpl.svtsim_ams_procEvent1(ams);
  }
}