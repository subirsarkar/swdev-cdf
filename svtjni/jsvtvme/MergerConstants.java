package jsvtvme;

/**  
  *     MergerConstants.java
  *
  *     Mirrors the enumeration constants defined in
  *     <B>$SVTVME_DIR/include/merger_functions.h</B>. 
  */
public interface MergerConstants {
    public static final int
        MERGER_HOLD             = 0x101,
        MERGER_TMOD             = 0x102,
        MERGER_ERROR            = 0x103,
        MERGER_TMOD_INPR        = 0x104,
        MERGER_TMOD_CMP         = 0x105,
        MERGER_TMOD_OUTD        = 0x106,
        MERGER_SHADOW_ERROR     = 0x107,
        MERGER_DET_MODE         = 0x108,
        MERGER_FSM              = 0x109,
        MERGER_END_EVT          = 0x10a,
        MERGER_OUT_SPY_TEMP     = 0x10b,
        MERGER_SVT_ERREN        = 0x10c,
        MERGER_CDF_ERREN        = 0x10d,
        MERGER_GLO_ERREN        = 0x10e,
        MERGER_CDF_ERR_ST       = 0x10f,
        MERGER_DIP              = 0x110,

        /*  All bit masks for bits in registers */

        MERGER_INIT_MASK        = 0x1,
        MERGER_TMOD_MASK        = 0x1,
        MERGER_HOLD_MASK        = 0x3,
        MERGER_ERROR_MASK       = 0x7f,
        MERGER_TMOD_INPR_MASK   = 0x1,
        MERGER_TMOD_CMP_MASK    = 0x3,
        MERGER_TMOD_OUTD_MASK   = 0x3,
        MERGER_SHADOW_ERROR_MASK= 0x7f,
        MERGER_DET_MODE_MASK    = 0x1,
        MERGER_FSM_MASK         = 0xf,
        MERGER_END_EVT_MASK     = 0xf,
        MERGER_OUT_SPY_TEMP_MASK= 0x7ffff,
        MERGER_SVT_ERREN_MASK   = 0x7ff,
        MERGER_CDF_ERREN_MASK   = 0x7ff,
        MERGER_GLO_ERREN_MASK   = 0xff,
        MERGER_CDF_ERR_ST_MASK  = 0x1,
        MERGER_DIP_MASK         = 0x3f,

        MERGER_HIT_FIFO_MASK    = 0x807fffff,

        MERGER_A_EMPTY          = 0,
        MERGER_A_HOLD           = 1,
        MERGER_A_END            = 2,
        MERGER_A_ENABLE         = 3,
        MERGER_B_EMPTY          = 4,
        MERGER_B_HOLD           = 5,
        MERGER_B_END            = 6,
        MERGER_B_ENABLE         = 7,
        MERGER_C_EMPTY          = 8,
        MERGER_C_HOLD           = 9,
        MERGER_C_END            = 10,
        MERGER_C_ENABLE         = 11,
        MERGER_D_EMPTY          = 12,
        MERGER_D_HOLD           = 13,
        MERGER_D_END            = 14,
        MERGER_D_ENABLE         = 15,
        MERGER_OA_HOLD          = 16,
        MERGER_OA_HDIS          = 17,
        MERGER_OB_HOLD          = 18,
        MERGER_OB_HDIS          = 19,
        MERGER_TMOD_B           = 20,
        MERGER_OUT_DIS_B        = 21;
}
