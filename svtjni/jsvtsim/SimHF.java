package jsvtsim;

public class SimHF {
  private hfsim_t _self;
  private String metakey;

  public SimHF() {
    this("");  // default metakey 
  }
  public SimHF(final String metakey) { 
    this.metakey = metakey;
    _self = HfSimImpl.svtsim_hf_new(metakey);
  }
  protected void finalize() {
    del();
  }
  public void del() {
    HfSimImpl.svtsim_hf_del(_self);
  }
  protected hfsim_t getHandle() {
    return _self;
  }
  public void setHandle(final hfsim_t _self) {
    this._self = _self;
  }
  public String getMetakey() {
    return metakey;
  }
  public int loadStream(int stream, final int [] data) {
    return HfSimImpl.svtsim_hf_loadStream(_self, stream, data, data.length);
  }
  /** tell HF about its map data */
  public void useMaps(final WedgeMaps maps) {
    useMaps(maps.getHandle());
  }
  public void useMaps(final wedgemaps_t maps) {
    HfSimImpl.svtsim_hf_useMaps(_self, maps);
  }
  /** ask HF where to find its output data */
  public cable_t outputCable() {
    return HfSimImpl.svtsim_hf_outputCable(_self);
  }
  /** Process data from input cable */
  public void procEvent() {
    HfSimImpl.svtsim_hf_procEvent(_self);
  }
}
