package jsvtsim;

public class HfSimImpl {
  public static native hfsim_t svtsim_hf_new(final String metakey);
  public static native void svtsim_hf_del(final hfsim_t self);
  public static native void svtsim_hf_procEvent(final hfsim_t self);
  public static native void svtsim_hf_loadStream(final hfsim_t self, int stream, final int [] data, int ndata);
  public static native cable_t svtsim_hf_outputCable(final hfsim_t self);
  public static native void svtsim_hf_useMaps(final hfsim_t self, final wedgemaps_t maps);
}
