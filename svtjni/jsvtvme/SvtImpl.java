package jsvtvme;

/**
  *   SvtImpl.java
  *
  *   <P>
  *   Implements all the functionalities found in <B>$SVTVME_DIR/include/svt_functions.h</B>
  *   using native interface. The API has been wrapped using the exact calling
  *   sequence as found in the C library. </P>
  *   <P>
  *   As Java package and class names already protect namespace, for the purpose 
  *   of JPython scripting one can use shorter method names unambigously i.e 
  *   some of the methods might be  re-implemented using simplified 
  *   naming conventions as <I>convenience methods</I>.<P>
  *   <P>
  *   The original svtvme documentation should be consulted to understand
  *   the purpose of various methods and the meaning of the parameters. We
  *   have provided API level documentation here only for the convenience
  *   methods.
  *   <P>
  *   Java does not allow passing an address of a variable as an output 
  *   variable, we, therefore, use a special Integer Holder object
  *   <PRE>
  *      org.omg.CORBA.IntHolder state = new org.omg.CORBA.IntHolder();
  *      int error = svtvme_am_getState(boardReg, state, bHandle);
  *      System.out.println("State = " + state.velue);
  *   </PRE>
  *
  *   <P>
  *   <B>Note:</B> For both input and output arrays declare them <I>with proper
  *   size before calling any methods using them</I>. Otherwise segmentation fault
  *   may occur. No explicit protection has been implemented yet.
  *   <P>
  *
  *   <P>Java Example</P>
  *   <PRE>
  *   import jsvtvme.SvtImpl;
  *   import jsvtvme.SpyImpl;
  *   import jsvtvme.AmsImpl;
  *   
  *   public class AmsTest  implements jsvtvme.SvtConstants, jsvtvme.AmsConstants {
  *   	private SvtImpl svtObject;
  *   	private SpyImpl spyObject;
  *   	private AmsImpl amsObject;
  *   	private int spyHandle, amsHandle;
  *   	static {
  *   	  try {
  *   	      System.loadLibrary("SvtvmeImpl");
  *   	  } catch (UnsatisfiedLinkError e) {
  *   	    System.err.println("Cannot load the example native code. " + 
  *   	      " \nMake sure your LD_LIBRARY_PATH contains " +
  *   	      "try setenv CLASSPATH ${CLASSPATH}:/cdf/people2/sarkar/svtvme_test/Native/jsvtvme/lib\n" + e);
  *   	    System.exit(1);
  *   	  }
  *   	}
  *   	public AmsTest() {
  *   	   System.out.println("Initialise AmsTest");
  *   	}  
  *   	public AmsTest(final String crate, final int spySlot, final int amsSlot) {
  *   	  int error;
  *   	  org.omg.CORBA.IntHolder state = new org.omg.CORBA.IntHolder();
  *   
  *   	  svtObject = new SvtImpl();
  *   	  spyObject = new SpyImpl();
  *   	  amsObject = new AmsImpl();
  *   	  spyHandle = svtObject.svtvme_svt_open(crate, spySlot);
  *   	  amsHandle = svtObject.svtvme_svt_open(crate, amsSlot);
  *   
  *   	  error = spyObject.svtvme_spy_setFreeze(spyHandle);
  *   	  System.out.println(" Error code: " + error);
  *   
  *   	  error = spyObject.svtvme_spy_getFreezeBackplane(state, spyHandle);
  *   	  System.out.println(" Freeze Status: " + state.value);
  *   
  *   	  error = amsObject.svtvme_ams_getState(AMS_HIT_SPY, state, amsHandle);
  *   	  System.out.println(" Error code: " + error);
  *   
  *   	  System.out.println("Ams Out Spy buffer Freeze   " + Integer.toBinaryString(state.value>>18));
  *   	  System.out.println("Ams Out Spy buffer Overflow " + Integer.toBinaryString(state.value>>17));
  *   	  System.out.println("Ams Out Spy buffer Pointer  " + Integer.toBinaryString(state.value &0x1ffff));
  *   
  *   	  error = svtObject.svtvme_spy_freeze(AMS_HIT_SPY, state, amsHandle);
  *   	  System.out.println(" Error code: " + error);
  *   	  System.out.println("Ams Out Spy buffer Freeze using svtvme_spy_freeze   " + state.value);
  *   
  *   	  int [] data = new int[1000];
  *   	  error = svtObject.svtvme_svt_spyBufferRead (AMS_HIT_SPY, 0, 1000, data, amsHandle);
  *   	  for (int i = 0; i < 1000; i++) {
  *   	    System.out.println("Add = 0x" + Integer.toHexString(i) + " (" + i + "),  Data = 0x" +
  *   	     Integer.toHexString(data[i]&0x7fffff) + " = " + Integer.toBinaryString(data[i]));
  *   	  }
  *   
  *   	  error = spyObject.svtvme_spy_releaseFreeze(spyHandle);
  *   	  System.out.println(" Error code: " + error);
  *   
  *   	  error = spyObject.svtvme_spy_getFreezeBackplane(state, spyHandle);
  *   	  System.out.println(" Freeze Status now: " + state.value);
  *   
  *   	  svtObject.svtvme_svt_close(spyHandle);
  *   	  svtObject.svtvme_svt_close(amsHandle);
  *   	}
  *   	public void printHistory() {
  *   	  System.out.println("Print history");
  *   	}
  *   	public static void main(String [] argv) {
  *   	    new AmsTest("b0svt04.fnal.gov", 3, 8);
  *   	}
  *   }
  *   </PRE>
  *   <P>JPython Example</P>
  *   <PRE>
  *   import java
  *   java.lang.System.loadLibrary("SvtvmeImpl_IRIX64")
  *   from jsvtvme import SvtImpl,AmsImpl,SpyImpl
  *   from org.omg.CORBA import IntHolder
  * 
  *   svt = SvtImpl()
  *   spy = SpyImpl()
  *   ams = AmsImpl()
  *   spyH = svt.svtvme_svt_open("b0svt04.fnal.gov",3)
  *   amsH = svt.svtvme_svt_open("b0svt04.fnal.gov",8)
  * 
  *   spy.freeze(spyH)       
  *   spy.isFrozen(spyH)   
  *   
  *   from jarray import array,zeros  #  Special to JPython to access Java arrays
  *   data = zeros(100,'i')
  *  
  *   error = svt.svtvme_svt_spyBufferRead(0x40, 0, 100, data, amsH)
  *   print error
  *   for i in data:
  *   print "%x" % (i & 0x7fffff)
  * 
  *   print 'Second method'
  *   data_n = svt.readSpy(0x40, 0, 100, amsH)
  *   for i in data_n:
  *     print "%x" % (i & 0x7fffff)
  * 
  *   spy.release(spyH)       
  *   spy.isFrozen(spyH)  
  * 
  *   error = spy.svtvme_spy_setState(0x40, 1, amsH);
  *
  *   state = IntHolder()
  *   error = spy.svtvme_spy_getState(0x40, state, amsH);
  *   print 'state.value = ', state.value
  * 
  *   print 'state = ', spy.getState(0x40, amsH)
  *   print 'Global Freeze = ', spy.getGlobalFreeze(spyH)
  *   print 'Backplane Freeze = ', spy.getBackplaneFreeze(spyH)
  *
  *   </PRE>
  *
  *   @version 0.1
  *   @author  Subir Sarkar
  */
public class SvtImpl {
  public SvtImpl() {}
  public native int svtvme_svt_open(final String crate, final int slot);
  public native void svtvme_svt_close(int bHandle);
  public native int svtvme_spy_status(int boardReg, 
           org.omg.CORBA.IntHolder pointer,
           org.omg.CORBA.IntHolder overflow,
           org.omg.CORBA.IntHolder freeze,
      int bHandle);
  public native int svtvme_spy_pointer(int boardReg, 
           org.omg.CORBA.IntHolder pointer, int bHandle);
  public native int svtvme_spy_overflow(int boardReg, 
           org.omg.CORBA.IntHolder overflow, int bHandle);
  public native int svtvme_spy_freeze(int boardReg, 
           org.omg.CORBA.IntHolder freeze, int bHandle);

  public native int svtvme_svt_readFifo(int boardReg, int numWord, 
           org.omg.CORBA.IntHolder moreData, int [] data, int bHandle);
  public native int svtvme_svt_uploadFifo(int boardReg, String filename, int bHandle);
  public native int svtvme_svt_compareFifo(int boardReg, String filename, 
           org.omg.CORBA.IntHolder problems, int bHandle);
  public native int svtvme_svt_getIdPromBlock(int offset, int [] data, int bHandle);
  public native int svtvme_svt_singleRam(int boardReg, int operation, int offset, 
           int [] data, int bHandle);
  public native int svtvme_svt_memoryOperation(int boardReg, int operation, 
           int offset, int numWord, int [] data, int bHandle);
  public native int svtvme_svt_uploadRam(int boardReg, String filename, int bHandle);
  public native int svtvme_svt_downloadRam(int boardReg, String filename, int bHandle);
  public native int svtvme_svt_compareRam(int boardReg, String filename, 
           org.omg.CORBA.IntHolder problems, int bHandle);
  public native int svtvme_svt_downloadCompareRam(int boardReg, String filename, 
           org.omg.CORBA.IntHolder problems, int bHandle);
  public native int svtvme_svt_spyBufferRead(int boardReg, int offset, int numWord,
           int [] data, int bHandle);
  public native int svtvme_svt_uploadSpy(int boardReg, String filename, int bHandle);
  public native int svtvme_svt_compareSpy(int boardReg, String filename, 
           org.omg.CORBA.IntHolder problems, int bHandle);
  public native int svtvme_svt_randomAccess (int operation, int address, int numWords, 
           int []data, int bHandle);
  public native void svtvme_svt_setDebug(final String filename, int bHandle);

  // Convenience methods 
  /**
    * Return a board handle which is in the crate/slot passed as parameters
    * @param  crate   SVT crate name i.e "b0svt04.fnal.gov"
    * @param  slot    Slot number where a board is in  
    * @return         The pointer to the board structure in an integer
    */
  public int openBoard(String crate, int slot) {
    return svtvme_svt_open(crate, slot);
  }
  /**
    * Close a board which is already open
    * @param bHandle   The pointer to the SVT board structure
    */
  public void closeBoard(int bHandle) {
    svtvme_svt_close(bHandle);
  }
  /**
    * Get the pointer value of the board Spy buffer
    * @param boardReg  Register of an SVT board for which info is asked for
    * @param bHandle   The pointer to the SVT board structure
    * @return          Value of the spy pointer
    */
  public int getPointer(int boardReg, int bHandle) {
    int error;
    org.omg.CORBA.IntHolder state = new org.omg.CORBA.IntHolder();
    error = svtvme_spy_pointer(boardReg, state, bHandle);
    return state.value;
  }
  /**
    * Get the overflow bit of the board Spy buffer
    * @param boardReg  Register of an SVT board for which info is asked for
    * @param bHandle   The pointer to the SVT board structure
    * @return          Value of the overflow bit
    */
  public int getOverflow(int boardReg, int bHandle) {
    int error;
    org.omg.CORBA.IntHolder state = new org.omg.CORBA.IntHolder();
    error = svtvme_spy_overflow(boardReg, state, bHandle);
    return state.value;
  }
  /**
    * Get the freeze status of a board
    * @param boardReg  Register of an SVT board for which info is asked for
    * @param bHandle   The pointer to the SVT board structure
    * @return          Value of the Freeze bit
    */
  public int getFreeze(int boardReg, int bHandle) {
    int error;
    org.omg.CORBA.IntHolder state = new org.omg.CORBA.IntHolder();
    error = svtvme_spy_freeze(boardReg, state, bHandle);
    return state.value;
  }
  /**
    * Get the Pointer/Overflow/Freeze info of a board Spy buffer in an array
    * @param boardReg  Register of an SVT board for which info is asked for
    * @param bHandle   The pointer to the SVT board structure
    * @return          The status array
    */
  public int [] status (int boardReg, int bHandle) {
     int [] state = new int[3];
     state[0] = getPointer(boardReg, bHandle);
     state[1] = getOverflow(boardReg, bHandle);
     state[2] = getFreeze(boardReg, bHandle);
     return state;
  }
  /**
    * Read Spy buffer indicated by boardReg
    * @param boardReg  Register of an SVT board for which info is asked for
    * @param offset    Start from an offset, not just from the beginning
    * @param numWord   Read this many number of words
    * @param bHandle   The pointer to the SVT board structure
    * @return The data read in an array or an empty one
    */
  public int [] readSpy(int boardReg, int offset, int numWord, int bHandle) {
    int [] data = new int[numWord];
    int error;
    error = svtvme_svt_spyBufferRead (boardReg, offset, numWord, data, bHandle);
    return ((error > 0)  ? data : new int[0]);
  }
  /**
    * Read Spy buffer indicated by boardReg, simple wrapper to svtvme_svt_spyBufferRead
    * @param boardReg  Register of an SVT board for which info is asked for
    * @param offset    Start from an offset, not just from the beginning
    * @param numWord   Read this many number of words
    * @param data      Output data Array
    * @param bHandle   The pointer to the SVT board structure
    * @return Number of words read or an error code
    */
  public int readSpy(int boardReg, int offset, int numWord, int [] data, int bHandle) {
    return svtvme_svt_spyBufferRead (boardReg, offset, numWord, data, bHandle);
  }
}
