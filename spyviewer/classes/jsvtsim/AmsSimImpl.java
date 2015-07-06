package jsvtsim;

public class AmsSimImpl {
  public static native amssim_t svtsim_ams_new(int wedge);
  public static native void svtsim_ams_del(final amssim_t self);
  public static native void svtsim_ams_procEvent1(final amssim_t self);
  public static native void svtsim_ams_plugInput(final amssim_t self, cable_t cable);
  public static native cable_t svtsim_ams_outputCable(final amssim_t self);
  public static native void svtsim_ams_useMaps(final amssim_t self, wedgemaps_t maps);
  public static native void svtsim_ams_setUcode(final amssim_t self, int ucode);
}