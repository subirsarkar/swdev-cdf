package jsvtsim;

public class SimXTF {
  private xtfsim_t _self;

  public SimXTF() { 
    _self = XtfSimImpl.svtsim_xtf_new();
  }
  /** Undo the underlying memory allocation */
  protected void finalize() {
    del();
  }
  public void del() {
    XtfSimImpl.svtsim_xtf_del(_self);
  }
  protected xtfsim_t getHandle() {
    return _self;
  }
  /** tell XTF about its input source */
  public void plugInput(final Cable cable) {
    plugInput(cable.getHandle());
  }
  public void plugInput(final cable_t cable) {
    XtfSimImpl.svtsim_xtf_plugInput(_self, cable);
  }
  /** tell XTF about its map data */
  public void useMaps(final WedgeMaps maps) {
    useMaps(maps.getHandle());
  }
  public void useMaps(final wedgemaps_t maps) {
    XtfSimImpl.svtsim_xtf_useMaps(_self, maps);
  }
  /** ask XTF where to find its output data */
  public cable_t outputCable() {
    return XtfSimImpl.svtsim_xtf_outputCable(_self);
  }
  /** Process data from input cable */
  public void procEvent() {
    XtfSimImpl.svtsim_xtf_procEvent(_self);
  }
}
