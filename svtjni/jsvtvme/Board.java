package jsvtvme; 

import java.*;
import java.util.*;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.StringHolder;

/**
 * <P>
 * Wrap the Native interface which is implemented in SvtvmeImpl.java in an OO fashion. 
 * Inspired by code developed by Federico Cozzi and Bill Ashmanskas for JPython. One 
 * can use this class from JPython as well and extend it to construct board specific
 * features. Board object instantiation goes as follows:</P>
 *
 * <UL>
 *   <LI>Java:   
 *   <PRE>Board ams = new Board("b0svt05", 8, AMS)</PRE></LI>
 *   <LI>Jython: 
 *   <PRE>ams = Board("b0svt05", 8, AMS)</PRE></LI>
 * </UL>
 * <P>
 * We use the same calling sequence as it is done in the original C
 * library. In addition, we've defined a few <I>convenience methods</I>.
 * This class uses simplified naming convention and removes <B>svtvme_</B>
 * prefix. As Java package and class names already protect namespace, for the purpose 
 * of JPython scripting one can use shorter method names unambigously i.e 
 * some of the methods might be  re-implemented using simplified 
 * naming conventions as <I>convenience methods</I>.</P> 
 *
 * <UL>
 * <LI>Java Example
 * <PRE>
 * import org.omg.CORBA.IntHolder;
 * import org.omg.CORBA.StringHolder;
 * import jsvtvme.*;
 * 
 * public class AmsTest {
 *   private Board spyB, amsB;
 *   static {
 *     try {
 *       System.loadLibrary("SvtvmeImpl");
 *     } catch (UnsatisfiedLinkError e) {
 *       System.err.println("Cannot load the example native code. " + 
 *        " \nMake sure your LD_LIBRARY_PATH contains the path " + 
 *        "where the shared library is, try" +
 *        "setenv LD_LIBRARY_SPATH ${LD_LIBRARY_PATH}:${SVTVME_LD_PATH}");
 *       System.exit(1);
 *     }
 *   }
 *   public AmsTest() {
 *     System.out.println("Initialise AmsTest");
 *   }  
 *   public AmsTest(final String crate, int spySlot, int amsSlot) {
 *     int error = 0;
 *     IntHolder state = new IntHolder();
 *
 *     spyB = new Board(crate, spySlot, SvtvmeConstants.SC);
 *     amsB = new Board(crate, amsSlot, SvtvmeConstants.AMS);
 *
 *     spyB.freeze();
 *     System.out.println(" Error code: " + error);
 * 
 *     error = spyB.getState(SvtvmeConstants.SC_BACKPLANE_FREEZE, state);
 *     System.out.println(" Freeze Status: " + state.value);
 *     error = amsB.getState(SvtvmeConstants.AMS_HSPY_PTR, state);
 *     System.out.println(" Error code: " + error);
 *     System.out.println("Ams Hit Spy buffer Pointer " + state.value);
 * 
 *     error = amsB.getState(SvtvmeConstants.AMS_HSPY_FRZ, state);
 *     System.out.println(" Error code: " + error);
 *     System.out.println("Ams Hit Spy buffer Freeze   " + state.value);
 * 
 *     error = amsB.getState(SvtvmeConstants.AMS_HSPY_WRP, state);
 *     System.out.println(" Error code: " + error);
 *     System.out.println("Ams Hit Spy buffer Overflow " + state.value);
 * 
 *     int [] data = new int[1000];
 *     error = amsB.readSpyTail(SvtvmeConstants.AMS_HIT_SPY, 1000, data);
 *     for (int i = 0; i < 1000; i++) {
 *       System.out.println("Add = 0x" + Integer.toHexString(i) + 
 *          " (" + i + "),  Data = 0x" + Integer.toHexString(data[i]&0x7fffff) + 
 *          " = " + Integer.toBinaryString(data[i]));
 *     }
 * 
 *     spyB.release();
 *     System.out.println(" Error code: " + error);
 * 
 *      error = spyB.getState(SvtvmeConstants.SC_BACKPLANE_FREEZE, state);
 *      System.out.println(" Freeze Status now: " + state.value);
 * 
 *      spyB.closeBoard();
 *      amsB.closeBoard();
 *   }
 *   public void printHistory() {
 *     System.out.println("Print history");
 *   }
 *   public static void main(String [] argv) {
 *     new AmsTest("b0svt05.fnal.gov", 3, 8);
 *   }
 * }
 * </PRE>
 * </LI>
 * <LI>JPython Example
 * <PRE>
 *  import sys
 *  from org.omg.CORBA import IntHolder
 *  import jsvtvme
 *  from jsvtvme import Board,SvtvmeConstants
 *  from jsvtvme.Board import *
 *  from jsvtvme.SvtvmeConstants import *
 *
 *  crate = sys.argv[1]
 *  state = IntHolder()
 * 
 *  spy = Board(crate, 3, SC)
 *   
 *  # Jumper status
 *  spy.getState(SC_JUMPER_MASTER, state)
 *  print 'Master enabled: ', state.value
 *   
 *  spy.getState(SC_JUMPER_LAST, state)
 *  print 'This board sits at the end of the daisy chain: ', state.value
 *   
 *  # G_Bus Input Status
 *  spy.getState(SC_GINIT_IN, state)
 *  print 'G_INIT from upstream: ', state.value
 *   
 *  spy.getState(SC_GFREEZE_IN, state)
 *  print 'G_FREEZE from upstream: ', state.value
 *   
 *  spy.getState(SC_GERROR_OUT, state)
 *  print 'G_ERROR from downstream: ', state.value
 *   
 *  spy.getState(SC_GLLOCK_OUT, state)
 *  print 'G_LLOCK from downStream: ', state.value
 *   
 *  spy.getState(SC_GBUS, state)
 *  print 'G_BUS Input status: ', state.value
 *   
 *  # SVT_INIT Generation
 *  spy.getState(SC_INIT_FORCE, state)
 *  print 'SVT_INIT is forced to false(0) or true (1): ', state.value
 *   
 *  spy.getState(SC_INIT_ON_GINIT, state)
 *  print 'SVT_INIT is generated in response to the " + 
 *     "G_INIT signal coming from the master: ', state.value
 *   
 *  # Backplane Status
 *  spy.getState(SC_BACKPLANE_INIT, state)
 *  print 'Backplane status, SVT_INIT: ', state.value
 *   
 *  spy.getState(SC_BACKPLANE_FREEZE, state)
 *  print 'Backplane status, SVT_FREEZE: ', state.value
 *   
 *  spy.getState(SC_BACKPLANE_ERROR, state)
 *  print 'Backplane status, SVT_ERROR: ', state.value
 *   
 *  spy.getState(SC_BACKPLANE_LLOCK, state)
 *  print 'Backplane status, SVT_LLOCK: ', state.value
 *   
 *  spy.getState(SC_BACKPLANE, state)
 *  print 'Backplane status: ', state.value
 *   
 *  # G_ERROR Generation
 *  spy.getState(SC_GERROR_FORCE, state)
 *  print 'G_ERROR is forced to false(0) or true (1)', state.value
 *   
 *  spy.getState(SC_GERROR_ON_ERROR, state)
 *  print 'G_ERROR is generated in response to the " + 
 *    "SVT_ERROR in the local backplane: ', state.value
 *   
 *  spy.getState(SC_GERROR_DRIVEN, state)
 *  print 'G_ERROR is driven true by this board: ', state.value
 *   
 *  # G_LLOCK Generation
 *  spy.getState(SC_GLLOCK_FORCE, state)
 *  print 'G_LLOCK is forced to true: ', state.value
 *   
 *  spy.getState(SC_GLLOCK_ON_LLOCK, state)
 *  print 'G_LLOCK is generated in response to the " + 
 *    "SVT_LLOCK in the local backplane: ', state.value
 *   
 *  spy.getState(SC_GLLOCK_DRIVEN, state)
 *  print 'G_LLOCK is being driven true by this board: ', state.value
 *   
 *  # SVT_FREEZE Generation
 *  spy.getState(SC_FREEZE_FORCE, state)
 *  print 'FREEZE Flip-Flop: ', state.value
 *   
 *  spy.getState(SC_FREEZE_ON_ERROR, state)
 *  print 'Enable SVT_ERROR: ', state.value
 *   
 *  spy.getState(SC_FREEZE_ON_LLOCK, state)
 *  print 'Enable SVT_LLOCK: ', state.value
 *   
 *  spy.getState(SC_FREEZE_ON_GFREEZE, state)
 *  print 'Enable G_FREEZE: ', state.value
 *   
 *  # SVT_FREEZE Delay
 *  spy.getState(SC_FREEZE_DELAY, state)
 *  print 'SVT_FREEZE Delay in steps of 1 microsec: ', state.value
 *   
 *  # LEVEL1 Counter
 *  spy.getState(SC_LEVEL1COUNTER, state)
 *  print 'LEVEL1 Counter: ', state.value
 *   
 *  # CDF_ERROR Generation
 *  spy.getState(SC_CDFERR_FORCE, state)
 *  print 'CDF_ERROR Flip-Flop: ', state.value
 *   
 *  spy.getState(SC_CDFERR_ON_ERROR, state)
 *  print 'Enable SVT_ERROR: ', state.value
 *   
 *  spy.getState(SC_CDFERR_ON_LLOCK, state)
 *  print 'Enable SVT_LLOCK: ', state.value
 *   
 *  spy.getState(SC_CDFERR_ON_GERROR, state)
 *  print 'Eneble G_ERROR(Master only): ', state.value
 *   
 *  spy.getState(SC_CDFERR_ON_GLLOCK, state)
 *  print 'Enable G_LLOCK(Master only): ', state.value
 *   
 *  # CDF_RECOVER and CDF_RUN Status
 *  spy.getState(SC_CDFRECOV, state)
 *  print 'CDF_RECOVER: ', state.value
 *   
 *  spy.getState(SC_CDFRUN, state)
 *  print 'CDF_RUN: ', state.value
 *   
 *  # Master registers
 *  # G_INIT Generation
 *  spy.getState(SC_GINIT_FORCE, state)
 *  print 'G_INIT is forced true .....:', state.value
 *   
 *  spy.getState(SC_GINIT_ON_CDFSIGS, state)
 *  print 'GINIT from CDFsignals: ', state.value
 *   
 *  spy.getState(SC_GINIT_DRIVEN, state)
 *  print 'G_INIT is being driven true: ', state.value
 *   
 *  # G_FREEZE Generation
 *  spy.getState(SC_GFREEZE_FORCE, state)
 *  print 'G_FREEZE Flip-Flop: ', state.value
 *   
 *  spy.getState(SC_GFREEZE_ON_GERROR, state)
 *  print 'Enable G_ERROR: ', state.value
 *   
 *  spy.getState(SC_GFREEZE_ON_GLLOCK, state)
 *  print 'Enable G_LLOCK: ', state.value
 *   
 *  # G_FREEZE Delay
 *  spy.getState(SC_GFREEZE_DELAY, state)
 *  print 'G_FREEZE delay in steps of 1 microsec: ', state.value
 *   
 *  spy.closeBoard()   // Might not be needed if finalise method is properly implemented
 *   
 *  </PRE>
 *  </LI></UL>
 *
 * @author Subir Sarkar
 * @version 1.0,   2/2001 
 * @version 1.1,   4/2001 
 */
public class Board {
    /** Board type to board name map */
  private static Hashtable map = new Hashtable();
    /* Load the shared Native library at initialisation, exit on failure */
  static {
    try {
      System.loadLibrary("SvtvmeImpl");
    } catch (UnsatisfiedLinkError e) {
      System.err.println("Cannot load the example native code. " + 
        " \nMake sure your LD_LIBRARY_PATH contains the path where " + 
        "the shared library is, try \n" +
        "setenv LD_LIBRARY_SPATH ${LD_LIBRARY_PATH}:${SVTVME_LD_PATH}");
      System.exit(1);
    }
    fillTable();
  }
    /** SVT Crate name e.g 'b0svt01' */
  private String crate;    
    /** Crate slot number which contains a particular board */
  private int slot;
    /** Special board type constants defined in the svtvme library */
  private int boardType;
    /** The real board handle, the pointer returned by svtvme_openBoard() */
  private svtvme_t board;
    /** 
     * @param crate      The SVT crate name string 
     * @param slot       The board slot number 
     * @param boardType  Special board type constants (SvtvmeConstants.AMS = 32)
     */
  public Board(final String crate, int slot, int boardType) {
    this.crate     = crate;
    this.slot      = slot;
    this.boardType = boardType;
    this.board     = SvtvmeImpl.svtvme_openBoard(crate, slot, boardType);
  }
    /** Close the board handle 
     *  @return The svtvme error code of execution
     */
  public int closeBoard() {
    return SvtvmeImpl.svtvme_closeBoard(board);
  }
    /** Called when garbage collection takes over */
  protected void finalise() {
    System.out.println("Closing board " + board);
    int error = closeBoard();
    if (error != 0) 
	System.out.println("Error closing board " + board);
  }
    /** Override toString() to return meaningful information about the board
     *  @return meaningful information about the object, position of the board
     *          in the crate 
     */
  public String toString() {
    return what();
  }
    /** Get meaningful information about the board in a string
     *  @return meaningful information about the object, position of the board
     *          in the crate 
     */
  public String what() {
    IntHolder type      = new IntHolder();
    IntHolder serialNum = new IntHolder();
    int error           = getBoardIdentifier(type, serialNum);
    return "[" + (String)map.get(new Integer(boardType)) 
               + " Board (Type: "+ boardType + ", Serial: " + serialNum.value 
               + ") opened in Crate " + crate + " Slot " + slot + "]";
  }
    /** Create a map of Board type constants to board names */
  protected static void fillTable() {
    map.put(new Integer(SvtvmeConstants.TRC),  new String("TRC"));    
    map.put(new Integer(SvtvmeConstants.AMB),  new String("AMB"));    
    map.put(new Integer(SvtvmeConstants.AMS),  new String("AMS"));    
    map.put(new Integer(SvtvmeConstants.HB),   new String("HB"));    
    map.put(new Integer(SvtvmeConstants.MRG),  new String("MRG"));    
    map.put(new Integer(SvtvmeConstants.HF),   new String("HF"));    
    map.put(new Integer(SvtvmeConstants.TF),   new String("TF"));    
    map.put(new Integer(SvtvmeConstants.XTFA), new String("XTFA"));    
    map.put(new Integer(SvtvmeConstants.SC),   new String("SC"));    
    map.put(new Integer(SvtvmeConstants.XTFC), new String("XTFC"));    
    map.put(new Integer(SvtvmeConstants.TCLK), new String("TCLK"));    
    map.put(new Integer(SvtvmeConstants.GB),   new String("GB"));    
    map.put(new Integer(SvtvmeConstants.SVTB), new String("Generic SVT Board"));    
  }
    /** Get crate name where the board sits 
     *  @return the SVT crate name the board sits in 
     */ 
  public String getCrate() {
    return crate;
  }
    /** Get the slot number of the board
     *  @return the crate slot which contain the board 
     */
  public int getSlot() {
    return slot;
  }
    /** Get name of the crate
     *  @return Crate name string
     */
  public String boardCrate() {
    StringHolder crateName = new StringHolder();
    int error = SvtvmeImpl.svtvme_boardCrate(board, crateName);
    return crateName.value;
  }
    /** Get name of the crate where the board sits
     *  @param crateName  Reference to StringHolder which will 
     *         contain the return value
     *  @return Crate name string
     */
  public int boardCrate(StringHolder crateName) {
    return SvtvmeImpl.svtvme_boardCrate(board, crateName);
  }
    /** Get the slot number for the board
     *  @return Slot number for the board
     */
  public int boardSlot() {
    return SvtvmeImpl.svtvme_boardSlot(board);
  }
    /** Disable throwing Fision Error related Message
     *  @return Error return code 
     */
  public int disableFisionErrorMessages() {
    return SvtvmeImpl.svtvme_disableFisionErrorMessages(board);
  }
    /** Enable throwing Fision Error related Message
     *  @return Error return code 
     */
  public int enableFisionErrorMessages() {
    return SvtvmeImpl.svtvme_enableFisionErrorMessages(board);
  }
   /** Set a board flag to the given value 
    *  @param flag   The board flag which is to be set
    *  @param value  The board flag value
    *  @return The original svtvme error code
    */ 
  public int setBoardFlag(int flag, int value) {
    return SvtvmeImpl.svtvme_setBoardFlag(board, flag, value);
  }
   /** Get a board flag value 
    *  @param flag   The board flag which is to be set
    *  @param state  The Integer holder object which contains the value
    *  @return The original svtvme error code
    */
  public int getBoardFlag(int flag, IntHolder state) {
    return SvtvmeImpl.svtvme_getBoardFlag(board, flag, state);
  }
   /** Set the name of the debug output file for a board 
    *  @param filename   The name of the file
    *  @return The original svtvme error code
    */
  public int setBoardDebugFile(final String filename) {
    return SvtvmeImpl.svtvme_setBoardDebugFile(board, filename);
  }
    /** Get the board type as an integer 
     *  @return Board type as an integer
     */
  public int boardType() {
    return SvtvmeImpl.svtvme_boardType(board);
  }
    /** Get the board type as an integer (<CODE>boardType()</CODE> preferred)
     *  @return Board type as an integer
     */
  public int getBoardType() {
    return SvtvmeImpl.svtvme_getBoardType(board);
  }
    /** Get the board identifier 
     *  @return Board identifier
     */
  public int boardId() {
    return SvtvmeImpl.svtvme_boardId(board);
  }
    /** Get the board serial number
     *  @return Board serial number
     */
  public int boardSn() {
    return SvtvmeImpl.svtvme_boardSn(board);
  }
    /** Get the board identifier 
     *  @param type  IntHolder which returns id
     *  @param serialNum IntHolder which returns board serial number
     *  @return Error return code
     */
  public int getBoardIdentifier(IntHolder type, IntHolder serialNum) {
    return SvtvmeImpl.svtvme_getBoardIdentifier(board, type, serialNum);
  }
    /** Initialise board 
     *  @return Original svtvme error return code
     */
  public int init() {
    return SvtvmeImpl.svtvme_init(board);
  }
    /** Set board to test mode if in run mode 
     *  @return svtvme error return code
     */ 
  public int setTmode() {
    return SvtvmeImpl.svtvme_setTmode(board);
  }
    /** Test if the board is in test mode
     *  @return true if the board is in test mode
     */ 
  public int isTmode() {
    return SvtvmeImpl.svtvme_isTmode(board);
  }
    /** Get the value of a board register
     *  @param regId   Board register constant
     *  @param state   Integer holder object which contains the result
     *  @return svtvme error return code
     */
  public int getState(int regId, IntHolder state) {
    return SvtvmeImpl.svtvme_getState(board, regId, state);
  }
    /** Set the value of a board register
     *  @param regId Board register constant
     *  @return svtvme error return code
     */
  public int setState(int regId, int d) {
    return SvtvmeImpl.svtvme_setState(board, regId, d);
  }
    /** Check the value of a board register
     *  @param regId   Board register constant
     *  @param d       the value the register should have
     *  @return svtvme error return code
     */
  public int checkState(int regId, int d) {
    return SvtvmeImpl.svtvme_checkState(board, regId, d);
  }
    /** Test a board register
     *  @param regId   Board register constant
     *  @return svtvme error return code
     */
  public int testRegister(int regId) {
    return SvtvmeImpl.svtvme_testRegister(board, regId);
  }
    /** Read data from board FIFO
     *  @param regId     Board register
     *  @param nw        Number of words to be read
     *  @param data      Output data array
     *  @return   svtvme error return code
     */
  public int readFifoMode(int regId, int nw, int [] data) {
    return SvtvmeImpl.svtvme_readFifoMode(board, regId, nw, data);
  }  
    /** Write data onto board FIFO
     *  @param regId     Board register
     *  @param nw        Number of words to be written
     *  @param data      Input data array
     *  @return   svtvme error return code
     */
  public int writeFifoMode(int regId, int nw, final int [] data) {
    return SvtvmeImpl.svtvme_writeFifoMode(board, regId, nw, data);
  }
    /** Read Memory content
     *  @param memId     Starting memory address
     *  @param nw        Number of words to be read
     *  @param data      Output data array
     *  @return   svtvme error return code
     */
  public int readMemory(int memId, int nw, int [] data ) {
    return SvtvmeImpl.svtvme_readMemory(board, memId, nw, data);
  }
    /** Check Memory content
     *  @param memId     Starting memory address
     *  @param nw        Number of words to be checked
     *  @param data      Input data array
     *  @param stopFlag  Stop flag
     *  @param state     Result contained in an integer holder object
     *  @return   svtvme error return code
     */
  public int checkMemory(int memId, int nw, final int [] data, 
           int stopFlag, IntHolder state) {
    return SvtvmeImpl.svtvme_checkMemory(board, memId, nw, data, stopFlag, state);
  }
    /** Check Sum Memory content
     *  @param memId     Starting memory address
     *  @param state     Result contained in an integer holder object
     *  @return   svtvme error return code
     */
  public int cksumMemory(int memId, IntHolder state) {
    return SvtvmeImpl.svtvme_cksumMemory(board, memId, state);
  }
    /** Test memory content
     *  @param memId     Starting memory address
     *  @param ntimes    Number of times to test memory
     *  @param stopFlag  Stop flag
     *  @param state     Result contained in an integer holder object
     *  @return   svtvme error return code
     */
  public int testMemory(int memId, int ntimes, int stopFlag, IntHolder state) {
    return SvtvmeImpl.svtvme_testMemory(board, memId, ntimes, stopFlag, state);
  }
    /** Write at the speified memory address
     *  @param memId     Starting memory address
     *  @param nw        Number of data words 
     *  @param data      Input data array
     *  @return svtvme error return code
     */
  public int writeMemory(int memId, int ndata, final int [] data) {
    return SvtvmeImpl.svtvme_writeMemory(board, memId, ndata, data);
  }
    /** Write at the speified memory address and verify
     *  @param memId     Starting memory address
     *  @param ndata     Number of data words 
     *  @param data      Input data array
     *  @param stopFlag  Stop flag
     *  @param nerr      number of error contained in an integer holder object
     *  @return svtvme error return code
     */
  public int writeVerifyMemory(int memId, int ndata, final int [] data,
        int stopFlag, IntHolder nerr) {
    return SvtvmeImpl.svtvme_writeVerifyMemory(board, memId, ndata, data, stopFlag, nerr);
  }
    /** Read a fragment of memory content with an offset 
     *  @param memId Starting memory address
     *  @param offset The position beyond the beginning where to start reading
     *  @param nw     Number of data words to be read
     *  @param data   The output data array
     *  @return svtvme error return code
     */
  public int readMemoryFragment(int memId, int offset, int nw, int [] data) {
    return SvtvmeImpl.svtvme_readMemoryFragment(board, memId, offset, nw, data);
  }
    /** Check memory fragment at the speified memory address
     *  @param memId     Starting memory address
     *  @param offset    Start reading at offset
     *  @param nw        Number of data words 
     *  @param data      Input data array
     *  @param stopFlag  Stop flag
     *  @param nerr      number of error contained in an integer holder object
     *  @return svtvme error return code
     */
  public int checkMemoryFragment(int memId, int offset, int nw, final int [] data, 
                                 int stopFlag, IntHolder nerr) {
    return SvtvmeImpl.svtvme_checkMemoryFragment(board, memId, offset, 
             nw, data, stopFlag, nerr);
  }
    /** Checksum a block of data 
     *  @param addr    Beginning memory address
     *  @param mask    Error mask
     *  @param ndata   Number of words
     *  @param state  Stores the checksum result in an IntHolder object
     *  @return svtvme error return code
     */
  public int cksumBlock(int addr, int mask, int ndata, IntHolder state) {
    return SvtvmeImpl.svtvme_cksumBlock(board, addr, mask, ndata, state);
  }
    /** Write a fragment of memory content with an offset 
     *  @param memId Starting memory address
     *  @param offset The position beyond the beginning where to start reading
     *  @param nw     Number of data words to be written
     *  @param data   The input data array
     *  @return svtvme error return code
     */
  public int writeMemoryFragment(int memId, int offset, int nw, final int [] data) {
    return SvtvmeImpl.svtvme_writeMemoryFragment(board, memId, offset, nw, data);
  }
    /** Write a fragment of memory content with an offset 
     *  @param memId Starting memory address
     *  @param offset The position beyond the beginning where to start reading
     *  @param data   The input data array
     *  @return svtvme error return code
     */
  public int writeMemoryFragment(int memId, int offset, final int [] data) {
    return SvtvmeImpl.svtvme_writeMemoryFragment(board, memId, offset, data.length, data);
  }
    /** Read the Spy pointer value for the spy buffer 
     *  @param spyId  Spy buffer identifier constant
     *  @return The Spy buufer pointer value where the next word will be written
     */
  public int spyCounter(int spyId) {
    return SvtvmeImpl.svtvme_spyCounter(board, spyId);
  }
    /** Check if the Spy buffer is in 'freeze' mode
     *  @param spyId  Spy Buffer identifier constant
     *  @return True if the Spy buffer is frozen
     */
  public int isFrozen(int spyId) {
    return SvtvmeImpl.svtvme_isFrozen(board, spyId);
  }
    /** Test if the Spy buffer is 'wrapped'
     *  @param spyId  Spy Buffer identifier constant
     *  @return True if the Spy buffer is wrapped
     */
  public int isWrapped(int spyId) {
    return SvtvmeImpl.svtvme_isWrapped(board, spyId);
  }
    /** Reset the spy buffer pointer
     * @param spyId Spy buffer identifier constant
     * @return svtvme error return code
     */
  public int resetSpy(int spyId) {
    return SvtvmeImpl.svtvme_resetSpy(board, spyId);
  }
    /** Read spy buffer from the end 
     *  @param spyId The spy buffer identifier constant
     *  @param nw    Number of words to be read
     *  @param data  Output Data array 
     */
  public int readSpyTail(int spyId, int nw, int [] data) {
    return SvtvmeImpl.svtvme_readSpyTail(board, spyId, nw, data);
  }
    /** Send a data word to a board 
     *  @param word   The input data word
     *  @return svtvme error return code     
     */
  public int sendWord(int word) {
    return SvtvmeImpl.svtvme_sendWord(board, word);
  }
    /** Send data from a board donwstream once 
     *  @param nw     Number of words to be sent
     *  @param data   The input data array
     *  @param speed  Speed at which data should be written
     *  @return svtvme error return code     
     */
  public int sendDataOnce(int nw, final int [] data, int speed) {
    return SvtvmeImpl.svtvme_sendDataOnce(board, nw, data, speed);
  }
    /** Send data from a board downstream in a loop
     *  @param nw     Number of words to be sent
     *  @param data   The input data array
     *  @return svtvme error return code     
     */
  public int sendDataLoop(int nw, final int [] data) {
    return SvtvmeImpl.svtvme_sendDataLoop(board, nw, data);
  }
    /** Send data again from a board downstream once
     *  @return svtvme error return code     
     */
  public int resendDataOnce() {
    return SvtvmeImpl.svtvme_resendDataOnce(board);
  }
    /** Send data again from a  board downstream in a loop
     *  @return svtvme error return code     
     */
  public int resendDataLoop() {
    return SvtvmeImpl.svtvme_resendDataLoop(board);
  }
    /** Send data again
     *  @return svtvme error return code     
     */
  public int resendData() {
    return SvtvmeImpl.svtvme_resendData(board);
  }
    /** Send data from a board downstream once 
     *  @param data   The input data array
     *  @return svtvme error return code     
     */
  public int sendDataOnce(final int [] data) {
    return sendDataOnce(data.length, data, SvtvmeConstants.FASTER);
  }
    /** Send data from a board downstream once 
     *  @param nw     Number of words to be sent
     *  @param data   The input data array
     *  @return svtvme error return code     
     */
  public int sendDataOnce(final int [] data, int speed) {
    return sendDataOnce(data.length, data, speed);
  }
    /** Read a single word at the specified address
     *  @param addr  Address where data to be written
     *  @param state The integer holder which contains the data word
     *  @return   svtvme error return code
     */
  public int readWord(int addr, IntHolder state) {
    return SvtvmeImpl.svtvme_readWord(board, addr, state);
  }
    /** Read a single word at the specified address
     *  @param addr  Address where data to be written
     *  @return   The word read if no error occursm other wise returns error code
     */
  public int readWord(int addr) {
    IntHolder state = new IntHolder();
    int error = SvtvmeImpl.svtvme_readWord(board, addr, state);
    if (error == 0) {
      return state.value;
    }
    else {
      System.out.println("readWord(): Error in operation, error = " + error);
      return error;
    }
  }
    /** Read words at the specified address
     *  @param addr  Address where data to be written
     *  @param nw    Number of words to be read
     *  @param data  The output data array    
     *  @return   svtvme error return code
     */
  public int readWords(int addr, int [] data, int nw) {
    return SvtvmeImpl.svtvme_readWords(board, addr, data, nw);
  }
    /** Read words at the specified address
     *  @param addr  Address where data to be written
     *  @param nw    Number of words to be read
     *  @return   The data array
     */
  public int [] readWords(int addr, int nw) {
    int [] data = new int[nw];
    int error = SvtvmeImpl.svtvme_readWords(board, addr, data, nw);
    if (error == 0) {
      return data;
    }
    else {
      System.out.println("readWords(): Error in operation, error = " + error);
      return new int[0];
    }
  }
    /** Write a single word at the specified address
     *  @param addr  Address where data to be written
     *  @param word  The data word
     *  @return   svtvme error return code
     */
  public int writeWord(int addr, int word) {
    return SvtvmeImpl.svtvme_writeWord(board, addr, word);
  }
    /** Write words at the specified address
     *  @param addr  Address where data to be written
     *  @param nw    Number of words to be written
     *  @param data  The input data array    
     *  @return   svtvme error return code
     */
  public int writeWords(int addr, int nw, final int [] data) {
    return SvtvmeImpl.svtvme_writeWords(board, addr, nw, data);
  }
    /** Write words at the specified address
     *  @param addr  Address where data to be written
     *  @param data  The input data array    
     *  @return   svtvme error return code
     */
  public int writeWords(int addr, final int [] data) {
    return SvtvmeImpl.svtvme_writeWords(board, addr, data.length, data);
  }
    /** Read a block of words from VME taking care of alignment
     *  @param add  The address where data to read from
     *  @param data The output data array
     *  @param ndata number of words to be read
     *  @return Error return code
     */
  public int readWordsAl(int addr, int [] data, int ndata) {
    return SvtvmeImpl.svtvme_readWordsAl(board, addr, data, ndata);
  }
    /** Write a block of words to VME taking care of alignment
     *  @param add  The address where data to write to 
     *  @param ndata number of words to be written
     *  @param data The input data array
     *  @return Error return code
     */
  public int writeWordsAl(int addr, int ndata, final int [] data) {
    return SvtvmeImpl.svtvme_writeWordsAl(board, addr, ndata, data);
  }
    /** Check whether the board output hold is on
     *  @return true if the board output hold is on
     */
  public int isHeld() {
    return SvtvmeImpl.svtvme_isHeld(board);
  }
    /** Check whether the board FIFO is empty
     *  @param fifoId  Board FIFO identifier
     *  @return true if the board FIFO is empty
     */
  public int isEmpty(int fifoId) {
    return SvtvmeImpl.svtvme_isEmpty(board, fifoId);
  }
    /** Check whether the word is the last word on the board FIFO
     *  @param fifoId  Board FIFO identifier
     *  @param word    A word read from FIFO
     *  @return true if the word is the last one on the board FIFO
     */
  public static int isLast(int fifoId, int word) {
    return SvtvmeImpl.svtvme_isLast(fifoId, word);
  }
    /** Get the Board Spy RAM identifier
     *  @param spyId   Board spy buffer identifier constant
     *  @return The Spy RAM id 
     */ 
  public static int SpyRamId(int spyId) {
    return SvtvmeImpl.svtvme_SpyRamId(spyId);  
  }
    /** Read all the spy buffer words from a buffer
     *  @param spyId  The spy buffer identifier constant
     *  @param nw     Number of words to be read (if (nw < length) return -6666 )
     *  @return Number of words read or the error code in case of failure 
     */
  public int readAllSpy(int spyId, int nw, int [] data) {
    return SvtvmeImpl.svtvme_readAllSpy(board, spyId, nw, data);
  }
    /** Computes the number of words between two values of the spy buffer
     *  counter, keeping into account counter wrapping at spy max length
     *  @param spyId The spy buffer identifier
     *  @param end   The end value of spy pointer
     *  @param start The beginning value of spy pointer
     *  @return The number of words within the bound
     */
  public static int deltaSpy(int spyId, int end, int start) {
    return SvtvmeImpl.svtvme_deltaSpy(spyId, end, start);
  }
  
    /** Read words from FIFO
     *  @param fifoId  Board FIFO identifier  
     *  @param nw  number of words to be read
     *  @param data  The output data array
     *  @return Number of words read or the error return code
     */
  public int readFifo(int fifoId, int nw, int [] data) {
    return SvtvmeImpl.svtvme_readFifo(board, fifoId, nw, data);
  }
    /** Read all the words from a FIFO
     *  @param fifoId  Board FIFO identifier  
     *  @param maxWords upto this number of words will be read
     *  @param data  The output data array
     *  @param state The object contains whether more data can be read
     *  @return Number of words read or the error return code
     */
  public int readAllFifo(int fifoId, int maxWords, int [] data, IntHolder state) {
    return SvtvmeImpl.svtvme_readAllFifo(board, fifoId, maxWords, data, state);
  }
    /** Test the board IDPROM 
     *  @return The IDProm value or 1 if board ID does not match
     */
  public int testIDPROM() {
    return SvtvmeImpl.svtvme_testIDPROM(board);
  }
    // Convenience methods 
    /** Returns the value of the register if the error return code
     *  of the svtvme call is 0, but does not return the error code itself.
     *  @param regId  The Board Register (name)
     *  @return The value of the register specified by <CODE>regId</CODE> 
     */
  public int read(int regId) {
    IntHolder state = new IntHolder();
    int error = getState(regId, state);
    if (error != 0) {
      System.out.println("Operation fails, error return code " + error);
      return error;
    }
    return state.value;
  }
    /** Just an alias to <CODE>setState</CODE> 
     *  @param regId  The Board Register (name)
     *  @param value  The value to be set for the Board Register
     *  @return The original svtvme error return code
     */
  public int write(int regId, int value) {
    return setState(regId, value);
  }
    /** Reads the spy buffer spcified by <CODE>spyId</CODE> and 
     *  if no error occurs return the data array. On error returns
     *  a null array of length 1.
     *  @param spyId  The Board Spy ID
     *  @param nw     Number of words to be read
     *  @return       The data array on Spy buffer read
     */
  public int [] readSpy(int spyId, int nw) {
    int error = 0;
    int [] data = new int[nw];
    int pointer = spyCounter(spyId);
    if (isWrapped(spyId) == 1)
      error = readSpyTail(spyId, nw, data);
    else
      error = readSpyTail(spyId, Math.min(nw, pointer), data);

    if (error > 0) return data;
    else return new int[0];
  }
    /** Send a freeze from the Spy Control. If any other board 
     *  is used, an error message is printed and no action takes place
     */
  public void freeze() {
    if (boardType() == SvtvmeConstants.SC) 
      setState(SvtvmeConstants.SC_FREEZE_FORCE, 1);
    else
      System.out.println("Operation allowed only for Spy Control Board");
  }
    /** Releases freeze from the Spy Control. If any other board 
     *  is used, an error message is printed and no action takes place
     */
  public void release() {
     if (boardType() == SvtvmeConstants.SC) 
       setState(SvtvmeConstants.SC_FREEZE_FORCE, 0);
     else
       System.out.println("Operation allowed only for Spy Control Board");
  }
    // Global methods, do not really fit here properly
    /** Initialise svt
     *  @return Error return code
     */ 
 public static int initialise() {
    return SvtvmeImpl.svtvme_initialise();
  }
    /** 
     *  Set the value of a global flag
     *  @param flag   SVT global flag
     *  @param value  Value to be set for the global flag 
     *  @return The original svtvme error return code
     */
  public static int setGlobalFlag(int flag, int value) {
    return SvtvmeImpl.svtvme_setGlobalFlag(flag, value);
  }
    /** 
     *  Get the value of a global flag
     *  @param flag   SVT global flag
     *  @param state  Integer holder object 
     *  @return The original svtvme error return code
     */
  public static int getGlobalFlag(int flag, IntHolder state) {
    return SvtvmeImpl.svtvme_getGlobalFlag(flag, state);
  }
    /** Returns the corresponding value of the SVT object specified 
     *  @param objectName  SVT object name as defined in the library
     *  @return The value of the object 
     */
  public static int stringToObject(final String objectName) {
    return SvtvmeImpl.svtvme_stringToObject(objectName);
  }
    /** Get the object name which matches the object specified as an argument.
     *  @param objectId    SVT object  as defined in the library
     *  @return Object name as a string
     */
  public static String objectName(int objId) {
    StringHolder objectName = new StringHolder();
    int error = objectName(objId, objectName);
    return objectName.value;
  }
    /** Get the object name which matches the object specified as an argument.
     *  The value is put as in the second argument
     *  @param objId       SVT object  as defined in the library
     *  @param objectName  The name of the object under discussion
     *  @return The original svtvme error return code
     */
  public static int objectName(int objId, StringHolder objectName) {
    return SvtvmeImpl.svtvme_objectName(objId, objectName);
  }
    /** Get the long object name which matches the object specified as an argument.
     *  @param objectId    SVT object  as defined in the library
     *  @return Object name as a string
     */
  public static String longName(int objId) {
    StringHolder objectName = new StringHolder();
    int error = longName(objId, objectName);
    return objectName.value;
  }
    /** Get the long object name which matches the object specified as an argument.
     *  The value is put as in the second argument
     *  @param objId       SVT object  as defined in the library
     *  @param objectName  The name of the object under discussion
     *  @return The original svtvme error return code
     */
  public static int longName(int objId, StringHolder objectName) {
    return SvtvmeImpl.svtvme_longName(objId, objectName);
  }
    /** Get the short object name which matches the object specified as an argument.
     *  @param objectId    SVT object  as defined in the library
     *  @return Object name as a string
     */
  public static String shortName(int objId) {
    StringHolder objectName = new StringHolder();
    int error = shortName(objId, objectName);
    return objectName.value;
  }
    /** Get the short object name which matches the object specified as an argument.
     *  The value is put as in the second argument
     *  @param objId       SVT object  as defined in the library
     *  @param objectName  The name of the object under discussion
     *  @return The original svtvme error return code
     */
  public static int shortName(int objId, StringHolder objectName) {
    return SvtvmeImpl.svtvme_shortName(objId, objectName);
  }
    /** Get depth of the object referred to by <CODE>objectId</CODE> (use nWords instead)
     *  @param objectId  Identifier for a Reg/Mem/FIFO/Spy register
     *  @return The size of the object
     */
  public static int length(int objectId) {
    return SvtvmeImpl.svtvme_length(objectId);
  }
    /** Get depth of the object referred to by <CODE>objectId</CODE>
     *  @param objectId  Identifier for a Reg/Mem/FIFO/Spy register
     *  @return The size of the object
     */
  public static int nWords(int objectId) {
    return SvtvmeImpl.svtvme_nWords(objectId);
  }
    /** Get mask of the object referred to by <CODE>objectId</CODE>
     *  @param objectId  Identifier for a Reg/Mem/FIFO/Spy register
     *  @return The size of the object
     */
  public static int mask(int objectId) {
    return SvtvmeImpl.svtvme_mask(objectId);
  }
    /** Get shift of the object referred to by <CODE>objectId</CODE>
     *  @param objectId  Identifier for a Reg/Mem/FIFO/Spy register
     *  @return The size of the object
     */
  public static int shift(int objectId) {
    return SvtvmeImpl.svtvme_shift(objectId);
  }
    /** Get address for the pnemonic constant
     *  @param  objectId constant defined in svtvme
     *  @return Address corresponding to the constant
     */
  public static int address(int objectId) {
    return address(objectId);
  }
    /** Probe board id and report error
     *  @param  crate Crate name
     *  @param  slot  Board slot number
     *  @return error return code
     */
  public static int probeId(String crate, int slot) {
    return SvtvmeImpl.svtvme_probeId(crate, slot);
  }
    /** Get the name of the board
     *  @return Board name string
     */
  public String boardName() {
    StringHolder boardName = new StringHolder();
    int error = boardName(boardName);
    return boardName.value;
  }
    /** Get the name of the board
     *  @param  boardName  Stores board name string as output 
     *  @return Error return code
     */
  public int boardName(StringHolder boardName) {
    return boardName(this.boardType, boardName);
  }
    /** Get the name of the board
     *  @param  boardType  board Type
     *  @param  boardName  Stores board name string as output 
     *  @return Error return code
     */
  public static int boardName(int boardType, StringHolder boardName) {
    return SvtvmeImpl.svtvme_boardName(boardType, boardName);
  }
    /** Print all the objects that belong to the board specified by type
     *  @return error return code
     */
  public int printObjects() {
    return printObjects(this.boardType);
  }
    /** Print all the objects that belong to the board specified by type
     *  @param boardType   Board type
     *  @return error return code
     */
  public static int printObjects(int boardType) {
    return SvtvmeImpl.svtvme_printObjects(boardType);
  }
    /** Get number of spy buffers for the board specified by type
     *  @return Error return code
     */
  public int nSpy() {
    return nSpy(this.boardType);
  }
    /** Get number of spy buffers for the board specified by type
     *  @param boardType  Board type
     *  @return Error return code
     */
  public static int nSpy(int boardType) {
    return SvtvmeImpl.svtvme_nSpy(boardType);
  }
    /** Get the list of registers for the board specified by type
     *  @param max   Maximum number allowed
     *  @param nReg  The actual number of registers returned
     *  @param regIds The array containing the list on output
     *  @return Error return code
     */
  public int getRegList(int max, IntHolder nReg, int [] regIds) {
    return getRegList(this.boardType, max, nReg, regIds);
  }
    /** Get the list of registers for the board specified by type
     *  @param boardType Board type
     *  @param max   Maximum number allowed
     *  @param nReg  The actual number of registers returned
     *  @param regIds The array containing the list on output
     *  @return Error return code
     */
  public static int getRegList(int boardType, int max, IntHolder nReg, int [] regIds) {
    return SvtvmeImpl.svtvme_getRegList(boardType, max, nReg, regIds);
  }
    /** Get the list of FIFOs for the board specified by type
     *  @param max     Maximum number allowed
     *  @param nFifo   The actual number of FIFOs returned
     *  @param fifoIds The array containing the list on output
     *  @return Error return code
     */
  public int getFifoList(int max, IntHolder nFifo, int [] fifoIds) {
    return getFifoList(this.boardType, max, nFifo, fifoIds);
  }
    /** Get the list of FIFOs for the board specified by type
     *  @param boardType Board type
     *  @param max     Maximum number allowed
     *  @param nFifo   The actual number of FIFOs returned
     *  @param fifoIds The array containing the list on output
     *  @return Error return code
     */
  public static int getFifoList(int boardType, int max, IntHolder nFifo, int [] fifoIds) {
    return SvtvmeImpl.svtvme_getFifoList(boardType, max, nFifo, fifoIds);
  }
    /** Get the list of memories for the board specified by type
     *  @param max    Maximum number allowed
     *  @param nMem   The actual number of memories returned
     *  @param memIds The array containing the list on output
     *  @return Error return code
     */
  public int getMemList(int max, IntHolder nMem, int [] memIds) {
    return getMemList(this.boardType, max, nMem, memIds);
  }
    /** Get the list of registers for the board specified by type
     *  @param boardType Board type
     *  @param max   Maximum number allowed
     *  @param nMem   The actual number of memories returned
     *  @param memIds The array containing the list on output
     *  @return Error return code
     */
  public static int getMemList(int boardType, int max, IntHolder nMem, int [] memIds) {
    return SvtvmeImpl.svtvme_getMemList(boardType, max, nMem, memIds);
  }
    /** Get the list of SPYs for the board specified by type
     *  @param max    Maximum number allowed
     *  @param nSpy   The actual number of SPYs returned
     *  @param spyIds The array containing the list on output
     *  @return Error return code
     */
  public int getSpyList(int max, IntHolder nSpy, int [] spyIds) {
    return getSpyList(this.boardType, max, nSpy, spyIds);
  }
    /** Get the list of SPYs for the board specified by type
     *  @param boardType Board type
     *  @param max    Maximum number allowed
     *  @param nSpy   The actual number of SPYs returned
     *  @param spyIds The array containing the list on output
     *  @return Error return code
     */
  public static int getSpyList(int boardType, int max, IntHolder nSpy, int [] spyIds) {
    return SvtvmeImpl.svtvme_getSpyList(boardType, max, nSpy, spyIds);
  }
    /** Returns a random number 
     *  @param  mask   Initial seed
     *  @return The correspondin random number
     */
  public static int rand(int mask) {
    return SvtvmeImpl.svtvme_rand(mask);
  }
    /** Test the class */
  public static void main(String [] argv) { 
    Board ams = new Board("b0svt04", 8, SvtvmeConstants.AMS); 
    for (int i = 0; i < 10; i++) {
      System.out.println(ams.spyCounter(SvtvmeConstants.AMS_HIT_SPY));
      try {
        Thread.sleep(4000);
      } 
      catch (Exception e) {
        System.out.println("Sleep interrupted!");
      }
    }
  }
}
