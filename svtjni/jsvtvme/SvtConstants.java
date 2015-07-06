package jsvtvme;

/**  
  *     SvtConstants.java
  *
  *     Mirrors the enumeration constants defined in
  *     <B>$SVTVME_DIR/include/svt_functions.h</B>. 
  */
public interface SvtConstants {
  public static final int   
        FULL_INIT = 0, /* The AMS has the option to do a full init or partial init.*/
        PART_INIT = 1, /*  Irrelevant for the other boards.*/

        SVT_READ  = 0, /* Used to specify if a Read or a write is requested */
        SVT_WRITE = 1,

       /*  
        * When writing to the output, used to specify the kind of check to do 
        * on the output hold
        */
        NO_HOLD    = 0,         /* Do not check to Output HOLD. Just send the data */
        HOLD_SENS  = 1,         /* If the HOLD is on, the function returns */
        WAIT_NO_HOLD = 2;       /* Waits in the function until HOLD is false */
                                /* then send the data. */


  /* Constants for identifying the Fifo type. */
  public static final int   
        AMS_HIT_FIFO  =  0x20,
        HB_HIT_FIFO   =  0x21,
        HB_ROAD_FIFO  =  0x22,
        MERGER_FIFO_A =  0x23,
        MERGER_FIFO_B =  0x24,
        MERGER_FIFO_C =  0x25,
        MERGER_FIFO_D =  0x26;

  /* Constants for identifying the RAM type. */
  public static final int   
        AMS_SS_MAP  = 0x0,
        AMS_MS_RAM  = 0x1,
        HB_SS_MAP   = 0x2,
        HB_AM_MAP   = 0x3,
        ID_PROM     = 0xf;

  /* Constants for identifying the SPY type. */
  public static final int   
        AMS_HIT_SPY  = 0x40,
        AMS_OUT_SPY  = 0x41,
        HB_HIT_SPY   = 0x42,
        HB_ROAD_SPY  = 0x43,
        HB_OUT_SPY   = 0x44,
        TF_ISPY      = 0x45,
        TF_OSPY      = 0x46,
        MERGER_A_SPY = 0x47,
        MERGER_B_SPY = 0x48,
        MERGER_C_SPY = 0x49,
        MERGER_D_SPY = 0x4a,
        MERGER_OUT_SPY = 0x4b;

  /* Constants for identifying the OUTPUT type. */
  public static final int   
        AMS_OUTPUT    = 0xf0,
        HB_OUTPUT     = 0xf1,
        MERGER_OUTPUT = 0xf2,
        AM_OUTPUT     = 0xf3,
        AM_INPUT      = 0xf4;
     
  /* Constants for the FIFO Status, i.e. the value of the FIFO status register */
  public static final int   
        FIFO_EMPTY      = 1,
        FIFO_HALF       = 2,
        FIFO_FULL       = 4,
        FIFO_NOT_EMPTY  = 0,
        FIFO_STAT_MASK  = 7;

  /* Spy Status register masks: */
  public static final int   
        SPY_POINTER = 0x1ffff,
        SPY_WRAP    = 0x20000,
        SPY_FREEZE  = 0x40000,
        FIFO_MASK   = 0x807fffff,
        SPY_MASK    = 0x7fffff;
     
  /* Some information about the ID PROM */
  public static final int   ID_PROM_MASK = 0xff;

  /*  The error messages returned with their meening */
  public static final int   
        MORE_DATA_BOARD = -100,   /*  Comparison: More Data board then in File */
        MORE_DATA_FILE  = -101,   /*  Comparison: More Data board then in File */
        OFFSET_ERR      = -102,   /*  Offset too large */
        END_OF_STRUCT   = -103,   /*  Reading/writing past end of structure */
        WORDS_TRANSFER  = -104,   /*  Not the right number of words transfered */
        STRANGE         = -105,   /*  WIHIH??? Unknow error!!! */
        NO_FILE         = -106,
        NO_MEMORY       = -107,
        GENERIC_VMESVT_ERROR = -108,
        NO_FREEZE       = -109,
        NO_TMODE        = -110,
        INVALID_SYSTEM    = -111,
        ILLEGAL_OPERATION = -112,
        INIT_FAILED       = -113,
        FILE_WRITE_PROBLEM = -114;
}
