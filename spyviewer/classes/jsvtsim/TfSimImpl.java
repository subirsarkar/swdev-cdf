package jsvtsim;

/**
  *   TfSimImpl.java
  *
  *   <P>
  *   Implements the Track Fitter
  *
  *   @version 0.1
  *   @author  Subir Sarkar
  */
public class TfSimImpl {
  public static native tfsim_t svtsim_tf_new(int wedge);
  public static native void svtsim_tf_del(final tfsim_t self);
  public static native void svtsim_tf_procEvent(final tfsim_t self);
  public static native void svtsim_tf_plugInput(final tfsim_t self, cable_t cable);
  public static native cable_t svtsim_tf_outputCable(final tfsim_t self);
  public static native void svtsim_tf_useMaps(final tfsim_t self, wedgemaps_t maps);
}
