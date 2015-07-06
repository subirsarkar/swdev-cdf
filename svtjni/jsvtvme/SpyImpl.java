package jsvtvme;

/**
  *   SpyImpl.java
  *
  *   <P>
  *   Implements all the functionalities found in <B>$SVTVME_DIR/include/spy_functions.h</B>
  *   using native interface. </P>
  * 
  *   <P>For further details look at <A HREF=../jsvtvme/SvtImpl.html>SvtImpl</A> </P>
  *
  *   @version 0.1
  *   @author  Subir Sarkar
  */
public class SpyImpl {
  public SpyImpl() {}
  public native int svtvme_spy_getState(int boardReg, org.omg.CORBA.IntHolder state, int bHandle);
  public native int svtvme_spy_resetState(int boardReg, int bHandle);
  public native int svtvme_spy_setState(int boardReg, int state, int bHandle);
  public native int svtvme_spy_getFreezeGlobal(org.omg.CORBA.IntHolder state, int bHandle);
  public native int svtvme_spy_getFreezeBackplane(org.omg.CORBA.IntHolder state, int bHandle);
  public native int svtvme_spy_setFreeze (int bHandle);
  public native int svtvme_spy_releaseFreeze (int bHandle);
  public native int svtvme_spy_forceReleaseFreeze (int bHandle);

  // Convenience functions
  /** 
    *  Return true if the system is frozen, otherwise return false
    *  @param bHandle   SVT Spy Board handle
    *  @return          boolean decision 
    */
  public boolean isFrozen(int bHandle) {
    int error;
    org.omg.CORBA.IntHolder state = new org.omg.CORBA.IntHolder();
    error = svtvme_spy_getFreezeBackplane(state, bHandle);
    return ((state.value > 0) ? true : false);    
  }
  /** 
    *  Freeze a system
    *  @param bHandle   SVT Spy Board handle
    *  @return          Error code
    */
  public int freeze(int bHandle) {
    return svtvme_spy_setFreeze(bHandle);
  }
  /** 
    *  Release spy freeze
    *  @param bHandle   SVT Spy Board handle
    *  @return          Error code
    */
  public int release(int bHandle) {
    return svtvme_spy_releaseFreeze(bHandle);
  }
  /** 
    *  Get the global freeze status
    *  @param bHandle   SVT Spy Board handle
    *  @return          Global freeze status value
    */
  public int globalFreeze(int bHandle) {
    int error;
    org.omg.CORBA.IntHolder state = new org.omg.CORBA.IntHolder();
    error = svtvme_spy_getFreezeGlobal(state, bHandle);
    return state.value;    
  }
  /** 
    *  Get the back plane freeze status
    *  @param bHandle   SVT Spy Board handle
    *  @return          Backplane freeze status value
    */
  public int backplaneFreeze(int bHandle) {
    int error;
    org.omg.CORBA.IntHolder state = new org.omg.CORBA.IntHolder();
    error = svtvme_spy_getFreezeBackplane(state, bHandle);
    return state.value;    
  }
  /** 
    *  Return the Spy freeze state of the board 
    *  @param boardReg  Board register for which the information is asked for
    *  @param bHandle   SVT Spy Board handle
    *  @return          Spy freeze state value
    */
  public int getState(int boardReg, int bHandle) {
    int error;
    org.omg.CORBA.IntHolder state = new org.omg.CORBA.IntHolder();
    error = svtvme_spy_getState(boardReg, state, bHandle);
    return state.value;    
  }
}
