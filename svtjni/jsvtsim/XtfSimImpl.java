package jsvtsim;

/**
  *   XtfSimImpl.java
  *
  *   <P>
  *   Implements XTF Simulation
  *
  *   @version 0.1
  *   @author  Subir Sarkar
  */
public class XtfSimImpl {
  public static native xtfsim_t svtsim_xtf_new();
  public static native void svtsim_xtf_del(final xtfsim_t self);
  public static native void svtsim_xtf_procEvent(final xtfsim_t self);
  public static native void svtsim_xtf_plugInput(final xtfsim_t self, final cable_t cable);
  public static native cable_t svtsim_xtf_outputCable(final xtfsim_t self);
  public static native void svtsim_xtf_useMaps(final xtfsim_t self, final wedgemaps_t maps);
}
