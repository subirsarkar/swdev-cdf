package jsvtvme;
import org.omg.CORBA.*;

/**
  *  <P>
  *  Implements all the functionalities found in <B>$SVTVME_DIR/include/svtvme_public.h</B>
  *  using Java Native Interface. The API has been wrapped using the exact calling
  *  sequence as found in the C library. All the methods are static in the native
  *  interface. As we want to create a real OO interface to svtvme we consider 
  *  this program as a more basic one and not to used by user programs. The real
  *  OO interface <B>Board.java</B> should provide all the methods found here plus a 
  *  few <I>convenience methods</I>.</P>
  * 
  *  <P>
  *  The original svtvme documentation should be consulted to understand
  *  the purpose of various methods and the meaning of the parameters.</P>
  *
  *  <P>
  *  Java does not allow passing an address of a variable as an output 
  *  variable, we, therefore, use a special Integer Holder object
  *  <PRE>
  *     IntHolder state = new IntHolder();
  *     int error = SvtvmeImpl.svtvme_getState(board, boardReg, state);
  *     System.out.println("State = " + state.value);
  *  </PRE>
  *
  *  <P>
  *  <B>Note:</B> For both input and output arrays declare them <I>with proper
  *  size before calling any methods which use them</I>. Otherwise segmentation fault
  *  may occur. No explicit protection has been implemented yet. </P>
  *
  *
  *  <P> N.B Functions related to checksum have not been wrapped</P>
  *
  *  @author  Subir Sarkar
  *  @version 0.1, August 2000 
  *  @version 1.0, February 2001
  *  @version 1.1, April 11, 2001
  *  @version 2.1, April 12, 2001
  *  @version 2.3, August 28, 2001
  */
public class SvtvmeImpl {
  // Global 
  public static native int svtvme_initialise();
  public static native int svtvme_setGlobalFlag(int flag, int value);
  public static native int svtvme_getGlobalFlag(int flag, IntHolder state);

  // Board def/del/setup (constructor/destructor/modifiers)
  public static native svtvme_t svtvme_openBoard(final String crate, int slot, int boardType);
  public static native int svtvme_closeBoard(final svtvme_t board);
  public static native int svtvme_disableFisionErrorMessages(final svtvme_t board);
  public static native int svtvme_enableFisionErrorMessages(final svtvme_t board);
  public static native int svtvme_setBoardFlag(final svtvme_t board, int flag, int value);
  public static native int svtvme_getBoardFlag(final svtvme_t board, int flag, IntHolder state);
  public static native int svtvme_setBoardDebugFile(final svtvme_t board, String filename);

  // Board enquiry (accessors) 
  public static native int svtvme_printObjects(int boardType);
  public static native int svtvme_probeSlot(final String crate, int slot, 
       IntHolder type, IntHolder serialNum);
  public static native int svtvme_getBoardIdentifier(final svtvme_t board, 
                             IntHolder type, 
                             IntHolder serialNum);
  public static native int svtvme_boardId(final svtvme_t board);
  public static native int svtvme_boardSn(final svtvme_t board);
  public static native int svtvme_boardType(final svtvme_t board);
  public static native int svtvme_boardSlot(final svtvme_t board);
  public static native int svtvme_boardCrate(final svtvme_t board, StringHolder crate);
  public static native int svtvme_boardName(int boardId, StringHolder boardName);
  public static native int svtvme_nSpy(int boardId);
  public static native int svtvme_getRegList(int boardType, int max, IntHolder nReg, 
                             int [] regIds);
  public static native int svtvme_getFifoList(int boardType, int max, IntHolder nFifo, 
                             int  [] fifoIds);
  public static native int svtvme_getMemList(int boardType, int max, IntHolder nMem, 
                             int [] memIds);
  public static native int svtvme_getSpyList(int boardType, int max, IntHolder nSpy, 
                             int [] spyIds);

  // Objects enquiry (accessors)
  public static native int svtvme_stringToObject(final String objectName);
  public static native int svtvme_objectName(int objId, StringHolder objName);
  public static native int svtvme_shortName(int objId, StringHolder objectShortName);
  public static native int svtvme_longName(int objId, StringHolder objectLongName);
  public static native int svtvme_address(int objectId);
  public static native int svtvme_nWords(int objectId);
  public static native int svtvme_mask(int objectId);
  public static native int svtvme_shift(int objectId);

  // Basic VME actions
  public static native int svtvme_reset(final svtvme_t board);
  public static native int svtvme_init(final svtvme_t board);
  public static native int svtvme_setTmode(final svtvme_t board);
  public static native int svtvme_isTmode(final svtvme_t board);
  public static native int svtvme_isHeld(final svtvme_t board);
  public static native int svtvme_isEmpty(final svtvme_t board, int fifoId);
  public static native int svtvme_isHold(final svtvme_t board, int fifoId);
  public static native int svtvme_isFull(final svtvme_t board, int fifoId);
  public static native int svtvme_getState(final svtvme_t board, int regId, IntHolder state);
  public static native int svtvme_setState(final svtvme_t board, int regId, int state);
  public static native int svtvme_readFifoMode(final svtvme_t board, int regId, 
                             int nw, int [] data);
  public static native int svtvme_writeFifoMode(final svtvme_t board, int regId, 
                             int nw, final int [] data);
  public static native int svtvme_readMemory(final svtvme_t board, int memId, 
                             int ndata, int [] data);
  public static native int svtvme_writeMemory(final svtvme_t board, int memId, int ndata, 
                             final int [] data);
  public static native int svtvme_writeVerifyMemory(final svtvme_t board, int memId, int ndata, 
                             final int [] data, int stopFlag, IntHolder nerr);
  public static native int svtvme_checkMemory(final svtvme_t board, int memId, int ndata, 
                             final int [] data, int stopFlag, IntHolder state);
  public static native int svtvme_checkMemoryFragment(final svtvme_t board, int memId, 
                             int offset, int ndata, final int [] data, int stopFlag, IntHolder nerr);

  // Memory pieces
  public static native int svtvme_readMemoryFragment(final svtvme_t board, int memId,
			     int offset, int nw, int [] data);
  public static native int svtvme_writeMemoryFragment(final svtvme_t board, int memId,
	 		     int offset, int nw, final int [] data);

  // Spy Buffer accessors 
  public static native int svtvme_spyCounter(final svtvme_t board, int spyId);
  public static native int svtvme_isFrozen(final svtvme_t board, int spyId);
  public static native int svtvme_isWrapped(final svtvme_t board, int spyId);
  public static native int svtvme_deltaSpy(int spyId, int end, int start);
  public static native int svtvme_resetSpy(final svtvme_t board, int spyId);
  public static native int svtvme_readSpyTail(final svtvme_t board, int spyId, 
                             int nw, int [] data);
  public static native int svtvme_readAllSpy(final svtvme_t board, int spyId, 
                             int ndata, int [] data);
  public static native int svtvme_sortWordsInBuffer(int [] data, int ndata);
  public static native int svtvme_sortPacketsInBuffer(int [] data, int ndata);

  // Data send/read
  public static native int svtvme_sendWord(final svtvme_t board, int word);
  public static native int svtvme_sendDataOnce(final svtvme_t board, int ndata, 
                             final int [] data, int speed);
  public static native int svtvme_sendDataLoop(final svtvme_t board, int ndata, final int [] data);
  public static native int svtvme_resendDataOnce(final svtvme_t board);
  public static native int svtvme_resendDataLoop(final svtvme_t board);
  public static native int svtvme_resendData(final svtvme_t board);
  public static native int svtvme_readFifo(final svtvme_t board, int fifoId, int nw, 
                             int [] data);
  public static native int svtvme_readAllFifo(final svtvme_t board, int fifoId, int maxWords,
		             int [] data, IntHolder state);
  public static native int svtvme_isLast(int fifoId, int word);

  // Hardware tests 
  public static native int svtvme_checkState(final svtvme_t board, int regId, int state);
  public static native int svtvme_testRegister(final svtvme_t board, int regId);
  public static native int svtvme_testMemory(final svtvme_t board, int memId, int ntimes,
		             int stopFlag, IntHolder state);
  public static native int svtvme_testIDPROM(final svtvme_t board);
  
  // Low level VME, hopefully never needed by user 
  public static native int svtvme_readWord(final svtvme_t board, int addr, IntHolder state);
  public static native int svtvme_writeWord(final svtvme_t board, int addr, int data);
  public static native int svtvme_readWords(final svtvme_t board, int addr, int [] data, int ndata);
  public static native int svtvme_writeWords(final svtvme_t board, int addr, int ndata, final int [] data);

  // Next 2 will soon be renamed 
  public static native int svtvme_readWordsAl(final svtvme_t board, int  addr, int [] data, int ndata);
  public static native int svtvme_writeWordsAl(final svtvme_t board, int addr, int ndata, final int [] data);
  
  // Checksum
  public static native int svtvme_cksumMemory(final svtvme_t board, int memId, IntHolder crc);
  public static native int svtvme_cksumBlock(final svtvme_t board, int addr, int mask, 
                             int ndata, IntHolder crc);
  // AMS specifics, waiting for a better place 
  public static native int svtvme_AmsUcodeSetOption(final String optionName, int optionValue);
  public static native int svtvme_AmsUcodeGen(int [] ucode, int ucodeLen);

  // Misc
  public static native int svtvme_rand(int mask);
  public static native int svtvme_wedge_randomtest(int wedge, String mapset, int nloops, int do_cs);
  public static native int svtvme_teststand_randomtest(int wedge, int nloops, int flags);
  public static native int svtvme_hf_fill_fram(int slot, String crate, String dirname, String mode);

  // Obsolescent 
  public static native int svtvme_getBoardType(final svtvme_t board);
  public static native int svtvme_length(int objectId);
  public static native int svtvme_SpyRamId(int spyId);
  public static native int svtvme_probeId(final String crate, int slot);
}
