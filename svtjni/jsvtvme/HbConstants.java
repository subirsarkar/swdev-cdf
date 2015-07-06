package jsvtvme;

/**  
  *     HbConstants.java
  *
  *     Mirrors the enumeration constants defined in
  *     <B>$SVTVME_DIR/include/hb_functions.h</B>. 
  */
public interface HbConstants {
  /*  Constants for identifying particular actions */
  public static final int
        HB_ERROR  = 0x100,
        HB_HOLD   = 0x101,
        HB_FSM    = 0x102,
        HB_FSMH   = 0x103,
        HB_FSMR   = 0x104,
        HB_CDF_ERR_EN  = 0x105,
        HB_CDF_ERR_ST  = 0x106,
        HB_DIP_STAT    = 0x107,
        HB_FREEZE = 0x108,
        HB_ERREN  = 0x109,

        HB_AM_MAP_LENGTH = 0x100000,  /* 1048576 */
        HB_SS_MAP_LENGTH = 131072,
        HB_FIFODEPTH = 4096;

  /*  All bit masks for bits in registers */
  public static final int
        HB_ERROR_MASK      = 0xff,
        HB_INIT_MASK       = 0x1,
        HB_TMOD_MASK       = 0x1,
        HB_HIT_F_S_MASK    = 0x7,
        HB_ROAD_F_S_MASK   = 0x7,
        HB_HOLD_MASK       = 0x1,
        HB_FSM_MASK        = 0xff,
        HB_FSMH_MASK       = 0xff,
        HB_FSMR_MASK       = 0xff,
        HB_OUTPUT_MASK     = 0x7fffff,
        HB_FREEZE_MASK     = 0x1,
        HB_HSPY_MASK       = 0x7ffff,
        HB_RSPY_MASK       = 0x7ffff,
        HB_OSPY_MASK       = 0x7ffff,
        HB_ERREN_MASK      = 0xff,
        HB_CDF_ERR_MASK    = 0x1,
        HB_DIP_MASK        = 0xf,

        HB_ID_PROM_MASK        = 0xff,

        HB_HIT_FIFO_MASK       = 0x807fffff,
        HB_ROAD_FIFO_MASK      = 0x807fffff,
        HB_HIT_SPY_MASK        = 0x7fffff,

        HB_ROAD_SPY_MASK       = 0x7fffff,
        HB_OUT_SPY_MASK        = 0x7fffff,
        HB_AM_MAP_MASK         = 0xffff,
        HB_SS_MAP_MASK         = 0x7fff;
} 
