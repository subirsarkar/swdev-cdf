package jsvtsim;

public class SimTF implements SvtsimConstants {
  private tfsim_t tf;
  private int wedge;
  /** the "wedge" argument may disappear at some point */
  public SimTF(int wedge) { 
    this.wedge = wedge;
    tf = TfSimImpl.svtsim_tf_new(wedge);
  }
  /** Undo the underlying memory allocation */
  protected void finalize() {
    del();
  }
  public void del() {
    TfSimImpl.svtsim_tf_del(tf);
  }
  protected tfsim_t getHandle() {
    return tf;
  }
  public int getWedge() {
    return wedge;
  }
  /** tell TF about its input source */
  public void plugInput(Cable cable) {
    TfSimImpl.svtsim_tf_plugInput(tf, cable.getHandle());
  }
  public void plugInput(cable_t cable) {
    TfSimImpl.svtsim_tf_plugInput(tf, cable);
  }
  /** tell TF about its map data */
  public void useMaps(WedgeMaps maps) {
    TfSimImpl.svtsim_tf_useMaps(tf, maps.getHandle());
  }
  public void useMaps(wedgemaps_t maps) {
    TfSimImpl.svtsim_tf_useMaps(tf, maps);
  }
  /** ask TF where to find its output data */
  public cable_t outputCable() {
    return TfSimImpl.svtsim_tf_outputCable(tf);
  }
  /** Process data from input cable */
  public void procEvent() {
    TfSimImpl.svtsim_tf_procEvent(tf);
  }
}