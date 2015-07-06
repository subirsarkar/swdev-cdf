package jsvtsim;

public class MrgSimImpl {
  public static native mrgsim_t svtsim_mrg_new();
  public static native void svtsim_mrg_del(final mrgsim_t self);
  public static native void svtsim_mrg_plugInput(final mrgsim_t self, int num, final cable_t cable);
  public static native cable_t svtsim_mrg_outputCable(final mrgsim_t self);
  public static native int svtsim_mrg_procEvent(final mrgsim_t self);
}