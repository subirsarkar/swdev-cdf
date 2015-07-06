package jsvtvme;

/**  
  *     AmsConstants.java
  *
  *     Mirrors the enumeration constants defined in
  *     <B>$SVTVME_DIR/include/ams_functions.h</B>. 
  */
public interface AmsConstants {
  /* Constants for identifying the system on which to perform 
     certain operations. */
  public static final int  AMS_HOLD   = 0x100, 
                           AMS_FSMH   = 0x101,
                           AMS_MSEQ   = 0x102,
                           AMS_ERROR  = 0x103,
                           AMS_EMPTY  = 0x104,
                           AMS_PHIS   = 0x105,
                           AMS_ERREN  = 0x106,
                           AMS_ROAD   = 0x107;
  public static final int  AMS_MS_RAM_LENGTH = 131072,
                           AMS_SS_MAP_LENGTH = 131072,
                           AMS_FIFODEPTH = 16384;
  /* All bit masks for bits in registers */
  public static final int  AMS_ERROR_MASK      = 0x1f,
                           AMS_INIT_MASK       = 0x1,
                           AMS_TMOD_MASK       = 0x1,
                           AMS_HIT_F_S_MASK    = 0x7,
                           AMS_HOLD_MASK       = 0x1,
                           AMS_FSMH_MASK       = 0xff,
                           AMS_MSEQ_MASK       = 0xff,
                           AMS_OUTPUT_MASK     = 0x7fffff,
                           AMS_HSPY_MASK       = 0x7ffff,
                           AMS_OSPY_MASK       = 0x7ffff,
                           AMS_ERREN_MASK      = 0x1f,
                           AMS_RLIM_MASK       = 0x1fbf,
                           AMS_PHIS_MASK       = 0xf,
                           AMS_ROAD_MASK       = 0x3f,
                           AMS_EMPTY_MASK      = 0x1f80,
                           AMS_MS_RAM_MASK     = 0xffffff,
                           AMS_SS_MAP_MASK     = 0x7fff;
}
