package jsvtvme;

/**  
  *     AmConstants.java
  *
  *     Mirrors the enumeration constants defined in
  *     <B>$SVTVME_DIR/include/am_functions.h</B>. 
  */
public interface AmConstants {
   public static final int
        /*  Constants for identifying the system on which 
            to perform certain operations. */
        AM_BRD    = 0x100,
        AM_MOD0   = 0x101,
        AM_MOD1   = 0x102,

        /*  Some constants */
        AMBOARD_AMBANK_LENGTH = 0x4000,
        AM_MAX_LAYER_NUMBER = 6,
        AM_NUMBER_OF_PLUGS = 16,

        /*  All bit masks for bits in registers */
        AM_ID_PROM_MASK       = 0xff,
        AM_REG0_MASK          = 0x3c0000,
        AM_BRD_MASK           = 0x200000,
        AM_TMOD_MASK          = 0x100000,
        AM_MOD0_MASK          = 0x80000,
        AM_MOD1_MASK          = 0x40000,
	
        AM_ID_PROM            = 0x100000,
        AM_ID_PROM_END        = 0x17fffC,
        AM_REG0               = 0x000000,
        AM_P3SWVME            = 0x200000,
        AM_VMESWP3            = 0x300000,
        AM_HALTSWPIPE         = 0x400000,
	
        /* Opcode Constants */
	
        OPC_NOP0              = 0x0,
        OPC_READ              = 0x1,
        OPC_OUTPUT            = 0x2,
        OPC_WRITE             = 0x3,
        OPC_INPUT             = 0x4,
        OPC_CLEARHITREGISTER  = 0x5,
        OPC_NOP6              = 0x6,
        OPC_SHIFT             = 0x7,
        OPC_COUNT             = 0x8,
        OPC_CLEARCOUNT        = 0x9,
        OPC_INIT              = 0xA,
        OPC_NOP11             = 0xB,
        OPC_RESETHANDSHAKE    = 0xC,
        OPC_NOP13             = 0xD,
        OPC_NOP14             = 0xE,
        OPC_NOP15             = 0xF;
}
