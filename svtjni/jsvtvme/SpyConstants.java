package jsvtvme;

/**  
  *     SpyConstants.java
  *
  *     Mirrors the enumeration constants defined in
  *     <B>$SVTVME_DIR/include/spy_functions.h</B>. 
  */
public interface SpyConstants {

  public static final int
        SPY_JUMPER             = 0x100,
        SPY_G_BUS_INP          = 0x101,
        SPY_SVT_INIT_GEN       = 0x102,
        SPY_SVT_INIT_PULSE     = 0x103,
        SPY_BACKPLANE          = 0x104,
        SPY_G_ERROR_GEN        = 0x105,
        SPY_G_LLOCK_GEN        = 0x106,
        SPY_SVT_FREEZE_GEN     = 0x107,
        SPY_SVT_FREEZE_DELAY   = 0x108,
        SPY_LVL1_COUNT         = 0x109,
        SPY_CDF_ERROR_GEN      = 0x10A,
        SPY_CDF_RECOVER        = 0x10B,
        SPY_G_INIT_GEN         = 0x10C,
        SPY_G_INIT_PULSE       = 0x10D,
        SPY_G_FREEZE_GEN       = 0x10E,
        SPY_G_FREEZE_DELAY     = 0x10F,


/*  All bit masks for bits in registers */

        SPY_JUMPER_MASK                = 0x3,
        SPY_G_BUS_INP_MASK             = 0xf,
        SPY_SVT_INIT_GEN_MASK          = 0x3,
        SPY_SVT_INIT_PULSE_MASK        = 0x1,
        SPY_BACKPLANE_MASK             = 0xf,
        SPY_G_ERROR_GEN_MASK           = 0x7,
        SPY_G_LLOCK_GEN_MASK           = 0x7,
        SPY_SVT_FREEZE_GEN_MASK        = 0xf,
        SPY_SVT_FREEZE_DELAY_MASK      = 0xffff,
        SPY_LVL1_COUNT_MASK            = 0xffff,
        SPY_CDF_ERROR_GEN_MASK         = 0x1f,
        SPY_CDF_RECOVER_MASK           = 0x3,
        SPY_G_INIT_GEN_MASK            = 0x7,
        SPY_G_INIT_PULSE_MASK          = 0x1,
        SPY_G_FREEZE_GEN_MASK          = 0x7,
        SPY_G_FREEZE_DELAY_MASK        = 0xffff,
        SPY_ID_PROM_MASK               = 0xff000000;
}
