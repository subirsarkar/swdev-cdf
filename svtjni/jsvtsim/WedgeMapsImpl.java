package jsvtsim;

import org.omg.CORBA.IntHolder;
/**
  *   WedgeMapsImpl.java
  *
  *   <P>
  *   Implements the cable
  *
  *   @version 0.1
  *   @author  Subir Sarkar
  */
public class WedgeMapsImpl {
  public static native wedgemaps_t wedgeMaps_new();
  public static native void wedgeMaps_free(final wedgemaps_t self);
  public static native int svtsim_getRam(final wedgemaps_t self, int ramid, 
        int [] d, int opt, int offset);
  public static native int svtsim_crcRam(final wedgemaps_t self, int ramid);
  public static native int svtsim_hitToSS(final wedgemaps_t self, int hit);
  public static native int svtsim_ssEdge(final wedgemaps_t self, int layer, int ss);
  public static native int svtsim_tfSS(final wedgemaps_t self, int road);

  public static native int svtsim_initMaps(final wedgemaps_t self, 
                                           int wedge,
                                           String ifitFnal, 
                                           String pattFnam, 
                                           String ssFnam);
  public static native int svtsim_initFromMapSet(final wedgemaps_t self,  
                                                int wedge, 
                                                final String mapSetName, 
                                                long mapSetCrc,
                                                final String dlPath, 
                                                boolean usedb);
  public static native int svtsim_useHwSet(final wedgemaps_t self,  
                                           int wedge, 
                                           final String hwSetName, 
                                           long hwSetCrc,
                                           final String dlPath, 
                                           boolean usedb);
  public static native int maps_ssStrips(final wedgemaps_t self, int layer);
  public static native int maps_dSSdz(final wedgemaps_t self, int layer);
  public static native int maps_pattSS(final wedgemaps_t self, int road, int layer);
  public static native int maps_wedge(final wedgemaps_t self);
}
