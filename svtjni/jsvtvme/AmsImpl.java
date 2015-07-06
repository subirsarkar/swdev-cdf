package jsvtvme;

/**
  *   AmsImpl.java
  *
  *   <P>
  *   Implements all the functionalities found in <B>$SVTVME_DIR/include/ams_functions.h</B>
  *   using native interface. </P>
  *   <P>For further details look at <A HREF=../jsvtvme/SvtImpl.html>SvtImpl</A> </P>
  *
  *   @version 0.1
  *   @author  Subir Sarkar
  */
public class AmsImpl {
  public AmsImpl() {} // no-arg constructor
  public native int svtvme_ams_init(int initType, int bHandle);
  public native int svtvme_ams_getState(int boardReg, org.omg.CORBA.IntHolder state, int bHandle);
  public native int svtvme_ams_resetState(int boardReg, int bHandle);
  public native int svtvme_ams_setState(int boardReg, int state, int bHandle);
  public native int svtvme_ams_tModeEnable(int bHandle);
  public native int svtvme_ams_tModeStatus(org.omg.CORBA.IntHolder state, int bHandle);
  public native int svtvme_ams_writeToOutput(int service, int numWord, int [] data, int bHandle);
  public native int svtvme_ams_outputFile (int service, String filename, int bHandle);

  // Convenience methods
  /**
    * Get the state of the board
    * @param    boardReg   Ams Board register for which state is looked for
    * @param    hHandle    Pointer to the corresponding Ams board C structure
    * @return   Status value 
    */
  public int getState(int boardReg, int bHandle) {
    int error;
    org.omg.CORBA.IntHolder state = new org.omg.CORBA.IntHolder();
    error = svtvme_ams_getState(boardReg, state, bHandle);
    return state.value;    
  }
  /**
    * Get the Test mode value 
    * @param    hHandle    Pointer to the corresponding Ams board C structure
    * @return   Status value
    */
  public int tModeStatus(int bHandle) {
    int error;
    org.omg.CORBA.IntHolder state = new org.omg.CORBA.IntHolder();
    error = svtvme_ams_tModeStatus(state, bHandle);
    return state.value;    
  }
}
