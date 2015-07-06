package jsvtsim;

public class HbSimImpl {
  public static native hbsim_t svtsim_hb_new(int wedge);
  public static native void svtsim_hb_del(final hbsim_t self);
  public static native void svtsim_hb_procEvent1(final hbsim_t self);
  public static native void svtsim_hb_plugHitInput(final hbsim_t self, cable_t cable);
  public static native void svtsim_hb_plugRoadInput(final hbsim_t self, cable_t cable);
  public static native cable_t svtsim_hb_outputCable(final hbsim_t self);
  public static native void svtsim_hb_useMaps(final hbsim_t self, wedgemaps_t maps);
}