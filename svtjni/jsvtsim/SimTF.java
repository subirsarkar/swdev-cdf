package jsvtsim;

public class SimTF {
  private tfsim_t _self;
  private int wedge;
  /** the "wedge" argument may disappear at some point */
  public SimTF(int wedge) { 
    this.wedge = wedge;
    _self = TfSimImpl.svtsim_tf_new(wedge);
  }
  /** Undo the underlying memory allocation */
  protected void finalize() {
    del();
  }
  public void del() {
    TfSimImpl.svtsim_tf_del(_self);
  }
  protected tfsim_t getHandle() {
    return _self;
  }
  public int getWedge() {
    return wedge;
  }
  /** tell TF about its input source */
  public void plugInput(final Cable cable) {
    plugInput(cable.getHandle());
  }
  public void plugInput(final cable_t cable) {
    TfSimImpl.svtsim_tf_plugInput(_self, cable);
  }
  /** tell TF about its map data */
  public void useMaps(final WedgeMaps maps) {
    useMaps(maps.getHandle());
  }
  public void useMaps(final wedgemaps_t maps) {
    TfSimImpl.svtsim_tf_useMaps(_self, maps);
  }
  /** ask TF where to find its output data */
  public cable_t outputCable() {
    return TfSimImpl.svtsim_tf_outputCable(_self);
  }
  /** Process data from input cable */
  public void procEvent() {
    TfSimImpl.svtsim_tf_procEvent(_self);
  }
}
