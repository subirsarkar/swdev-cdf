package jsvtvme;

/**
  *   MergerImpl.java
  *
  *   <P>
  *   Implements all the functionalities found in <B>$SVTVME_DIR/include/merger_functions.h</B>
  *   using native interface. </P>
  *
  *   <P>For further details look at <A HREF=../jsvtvme/SvtImpl.html>SvtImpl</A> </P>
  *
  *   @version 0.1
  *   @author  Subir Sarkar
  */
public class MergerImpl {
  public MergerImpl() {}
  public native int svtvme_merger_init(int initType, int bHandle);
  public native int svtvme_merger_getState(int boardReg, org.omg.CORBA.IntHolder state, int bHandle);
  public native int svtvme_merger_resetState(int boardReg, int bHandle);
  public native int svtvme_merger_setState(int boardReg, int state, int bHandle);
  public native int svtvme_merger_tModeEnable(int bHandle);
  public native int svtvme_merger_tModeStatus(org.omg.CORBA.IntHolder state, int bHandle);
  public native int svtvme_merger_writeToOutput(int service, int numWord, int [] data, int bHandle);
  public native int svtvme_merger_outputFile (int service, String filename, int bHandle);
  public native int svtvme_merger_spyBufferWrite (int boardReg, int offset, int numWord, 
                           int [] data, int bHandle);
  public native int svtvme_merger_downloadSpy (int boardReg, String filename, int bHandle);
  public native int svtvme_merger_downloadCompareSpy (int boardReg, String filename, 
                             org.omg.CORBA.IntHolder problems, int bHandle);
}
