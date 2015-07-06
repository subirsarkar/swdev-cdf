package jsvtsim;

/**
  *   CableImpl.java
  *
  *   <P>
  *   Implements the cable
  *
  *   @version 0.1
  *   @author  Subir Sarkar
  */
public class CableImpl {
  public static native cable_t svtsim_cable_new();
  public static native void svtsim_cable_del(final cable_t self);
  public static native void svtsim_cable_copywords(final cable_t self, int [] words, int nword);
  public static native void svtsim_cable_addwords(final cable_t self, int [] words, int nword);
  public static native void svtsim_cable_addword(final cable_t self, int word);
  public static native int cable_ndata(final cable_t self);
  public static native int cable_datum(final cable_t self, int i);
}
