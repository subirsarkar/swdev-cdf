package config;

/**
 *  Interface which provides useful contants for the RC state
 *  @author  Subir Sarkar
 *  @version 0.1,  July 2000
 */
public interface RCStateConstants {
  /** DAQ is closing */
  public static final int  STATE_CLOSE = 0;
    /** State before the partition is ready */
  public static final int  STATE_WAITING = 1;
    /** State once partition is ready */  
  public static final int  STATE_PARTITION = 2;
    /** State after R_C resets */
  public static final int  STATE_RESET = 3;
    /** Undefined yet */
  public static final int  STATE_IDLE = 4;
    /** State once the system is configured */
  public static final int  STATE_READY = 5;
    /** RC is Active, datataking going on */
  public static final int  STATE_ACTIVE = 6;
    /** RC sending initialisation command */
  public static final int  STATE_COMMAND = 7;
    /** R_C sends a shutdown message */
  public static final int  STATE_SHUTDOWN = 8;
    /** RC is starting */
  public static final int  STATE_START = 9;
    /** Minimum number of partitions present */
  public static final int  MIN_PARTITION = 0;
    /** Maximum number of partitions allowed */
  public static final int  MAX_PARTITION = 100;

  public static final int  OFF = 0;
  public static final int  ON =  1;
}
