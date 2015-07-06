package jsvtvme;

/**
  *   HbImpl.java
  *
  *   <P>
  *   Implements all the functionalities found in <B>$SVTVME_DIR/include/Hb_functions.h</B>
  *   using native interface.</P>
  *   <P>For further details look at <A HREF=../jsvtvme/SvtImpl.html>SvtImpl</A> </P>
  *
  *   @version 0.1
  *   @author  Subir Sarkar
  */
public class HbImpl {
  public HbImpl() {}
  public native int svtvme_hb_init(int bHandle);
  public native int svtvme_hb_getState(int boardReg, org.omg.CORBA.IntHolder state, int bHandle);
  public native int svtvme_hb_resetState(int boardReg, int bHandle);
  public native int svtvme_hb_setState(int boardReg, int state, int bHandle);
  public native int svtvme_hb_tModeEnable(int bHandle);
  public native int svtvme_hb_tModeStatus(org.omg.CORBA.IntHolder state, int bHandle);
  public native int svtvme_hb_writeToOutput(int service, int numWord, int [] data, int bHandle);
  public native int svtvme_hb_outputFile (int service, String filename, int bHandle);
  // Convenience methods
  /**
    * Get the state of the board
    * @param    boardReg   Hb Board register for which state is looked for
    * @param    hHandle    Pointer to the corresponding Hb board C structure
    * @return   Status value 
    */
  public int getState(int boardReg, int bHandle) {
    int error;
    org.omg.CORBA.IntHolder state = new org.omg.CORBA.IntHolder();
    error = svtvme_hb_getState(boardReg, state, bHandle);
    return state.value;    
  }
  /**
    * Get the Test mode value 
    * @param    hHandle    Pointer to the corresponding Hb board C structure
    * @return   Status value
    */
  public int tModeStatus(int bHandle) {
    int error;
    org.omg.CORBA.IntHolder state = new org.omg.CORBA.IntHolder();
    error = svtvme_hb_tModeStatus(state, bHandle);
    return state.value;    
  }
}
