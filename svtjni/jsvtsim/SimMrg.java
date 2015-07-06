package jsvtsim;

public class SimMrg {
  private mrgsim_t _self;
  public SimMrg() { 
    _self = MrgSimImpl.svtsim_mrg_new();
  }
  protected void finalize() {
    del();
  }
  public void del() {
    MrgSimImpl.svtsim_mrg_del(_self);
  }
  protected mrgsim_t getHandle() {
    return _self;
  }
  /** tell Mrg about its input source */
  public void plugInput(int num, final Cable cable) {
    plugInput(num, cable.getHandle());
  }
  public void plugInput(int num, final cable_t cable) {
    MrgSimImpl.svtsim_mrg_plugInput(_self, num, cable);
  }
  /** ask Mrg where to find its output data */
  public cable_t outputCable() {
    return MrgSimImpl.svtsim_mrg_outputCable(_self);
  }
  /** Process data from input cable */
  public void procEvent() {
    MrgSimImpl.svtsim_mrg_procEvent(_self);
  }
}
