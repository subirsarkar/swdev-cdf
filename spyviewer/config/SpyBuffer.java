package config;

import java.util.*;

import config.svt.*;
import config.util.*;

/** 
 *  <P>
 *  Holds Spy Buffer data structure a la Spy dump. The 
 *  class builds mainly on <CODE>SpyAttribute</CODE> which is
 *  as inner class. The composition order is the following:
 * <PRE>
 *  SpyAttribute 
 *     o- BoardAttribute 
 *       o- CrateAttribute
 * </PRE> 
 * </P>
 *
 *  <P>
 *  Only one instance of the buffer should provide a source
 *  of data to all the peripheral clients like data display,
 *  histogram viewer etc. </P>
 *
 *  @author Subir Sarkar 
 *  @version 0.2, May 2003
 */
public class SpyBuffer {
    /** Iteration number */
  private static int nCycles;  
    /** Date as a formatted string */
  private static String date;
    /** Time as a formatted string */
  private static String time;
    /** Level 1 Counter */
  private static int L1Counter;
    /** Global freeze status */
  private static int freeze;
    /** HF Counter */
  private static int hfCounter;
    /** Partition number within which data taking goes on */
  private static int partition;
    /** present Run Control state */
  private static String state;
    /** Event number */
  private static int event;
    /** Current event rate */
  private static float rate;
    /** Run number */
  private static int run;

    /** Spy Attribute */
  private SpyAttribute spyAttr;

    /**
     * @param crateName  Crate name (b0svtnn, nn = 00, 01 ...)
     * @param slot       Slot number which contains the board
     * @param boardName  Name of the board (AMS, HB ...)
     * @param mkey       Uniquely specifies a buffer
     * @param spyName    Name of the Spy Buffer
     * @param data       Spy Buffer array
     * @param nvalid     Number of valid words 
     * @param pointer    Pointer value where the next word would be written
     * @param wrap       Spy Buffer wrap bit 
     */
  public SpyBuffer(String crateName, int slot, String boardName, String mkey,
    String spyName, int nvalid, int pointer, int wrap, int [] data) 
  {
    CrateAttribute crate = new CrateAttribute (
      crateName, Tools.getCrateIndex(crateName) // Crate id
    );
    BoardAttribute board = new BoardAttribute (
      boardName,
      crate,         // Crate Attribute
      slot,             
      mkey           // Board Metakey
    );
    spyAttr = new SpyAttribute (
      spyName, 
      board,         // Board Attribute
      data,          // Spy Buffer array
      nvalid,        // Number of words read
      pointer,       // Pointer value
      wrap           // Wrap bit
    );
  }
    /** 
     * Get reference to Spy Attribute 
     * @return Reference to Spy Attribute
     */
  public SpyAttribute getSpyAttribute() {
    return spyAttr;
  }
    /** 
     * Fill data structure for static variables
     * @param nCycles     Cycle or iteration number
     * @param date        Current Date
     * @param time        Current time
     * @param L1Counter   Level 1 Counter value
     * @param freeze      Spy Freeze status
     * @param hfCounter   HF Counter 
     * @param partition   Partition number 
     * @param state       RC state
     * @param event       Event number
     * @param rate        Current event rate
     * @param run         Run number
     */
  public static void fillStatic (
       int nCycles, String date, String time, int L1Counter, int freeze, 
       int hfCounter, int partition, String state, int event, float rate,
       int run) 
  {
    SpyBuffer.nCycles   = nCycles;  
    SpyBuffer.date      = date;
    SpyBuffer.time      = time;
    SpyBuffer.L1Counter = L1Counter;
    SpyBuffer.freeze    = freeze;
    SpyBuffer.hfCounter = hfCounter;

    SpyBuffer.partition = partition;
    SpyBuffer.state     = state;
    SpyBuffer.event     = event;
    SpyBuffer.rate      = rate;
    SpyBuffer.run       = run;
  }
    /** 
     * Get iteration number 
     * @return Iteration number
     */
  public static int getCycles() {
    return nCycles;
  }
    /** 
     * Get date attached to this iteration  
     * @return date which corresponds to this buffer
     */
  public static String getDate() {
    return date;
  }
    /** 
     * Get time attched to this iteration
     * @return time which corresponds to this buffer
     */
  public static String getTime() {
    return time;
  }
    /** 
     * Get Level 1 Counter value
     * @return Level 1 Counter
     */
  public static int getL1Counter() {
    return L1Counter;
  }
    /** 
     * Get freeze status
     * @return freeze status (Frozen or spy)
     */
  public static int getFreeze() {
    return freeze;
  }
    /** 
     * Get HF Counter value
     * @return HF Counter
     */
  public static int getHfCounter() {
    return hfCounter;
  }
  /** Define SVT Crate */
  class CrateAttribute {
      /** Name of the SVT crate (b0svtnn, nn = 00, 01 ...) */
    private String name;
      /** SVT Crate ID (nn = 00, 01, ...) */
    private int id;
      /**
       * @param name   Crate name
       * @param id     Crate id (last 2 characters of <B>name</B>)
       */
    public CrateAttribute(String name, int id) {
      this.name = name;
      this.id   = id;
    }
      /** 
       * Get name of the crate
       * @return Name of the crate
       */
    public String getName() {
      return name;
    }
      /** 
       * Get crate id
       * @return crate id
       */
    public int getId() {
      return id;
    }
  }
    /** Define an SVT Board */
  class BoardAttribute {
      /** Crate Attribute */
    private CrateAttribute crate;
      /** Slot number where the board sits */
    private int slot;
      /** Name of the board */
    private String name;
      /** unique specification of the boards */
    private String mkey;
      /** 
       * @param  name  Board name
       * @param  crate Reference to the crate which contains the board
       * @param  slot  Slot number
       * @param  mkey  uniquely specifies the board in a crate
       */
    public BoardAttribute (
       String name, CrateAttribute crate, int slot, String mkey) 
    {
      this.name  = name;
      this.crate = crate;
      this.slot  = slot;
      this.mkey  = mkey;
    }
      /** 
       * Get reference to the crate
       * @return Reference to the crate which contains the board
       */
    public CrateAttribute getCrate() {
      return crate;
    }
      /** 
       * Get Board slot number 
       * @return Board slot
       */
    public int getSlot() {
      return slot;
    }
      /** 
       * Get board name 
       * @return Name of the board
       */
    public String getName() {
      return name;
    }
      /** 
       * Get Board metakey
       * @return Board metakey
       */
    public String getMkey() {
      return mkey;
    }
  }
    /** Define Spy Buffer */
  class SpyAttribute extends SvtEventsAttr {
      /** Number of valid words in the buffer */
    private int nvalid;
      /** Pointer value where the next word will be written */
    private int pointer;
      /** Spy Wrap bit */
    private int wrap;
      /** Reference to Board Attribute */
    private BoardAttribute board;
      /** Name of the buffer */
    private String name;
    /**
     * @param   name         Name of the spy buffer
     * @param   board        Reference to the board structure
     * @param   data         Spy Buffer array
     * @param   nvalid       Number of valid words 
     * @param   pointer      Pointer value where the next word would be written
     * @param   wrap         Spy Buffer wrap bit 
     */
    public SpyAttribute (String name, BoardAttribute board, int [] data, 
      int nvalid, int pointer, int wrap) 
    {
      super(data);
      this.name    = name;
      this.board   = board;
      this.nvalid  = nvalid;
      this.pointer = pointer;
      this.wrap    = wrap;
    }
    public void addEvent(final int [] words, int EE) {
      addEvent(new EventFrac(words, EE, name));
    }
      /** @return Number of valid words */
    public int getValid() {
      return nvalid;
    }
      /** @return Pointer value */
    public int getPointer() {
      return pointer;
    }
      /** @return Wrap bit */
    public int getWrap() {
      return wrap;
    }
      /** @return Reference to Board Attribute */
    public BoardAttribute getBoardAttribute() {
      return board;
    }
      /** @return Name of the spy buffer */
    public String getName() {
      return name;
    }
      /**
       * Writes the spy buffer content to a file
       * @param filename  Save buffer to this file
       */
    public void writeBuffer(String filename) {
    }
  } 
}
