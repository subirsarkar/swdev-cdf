package jsvtvme;

/**
  *   AmImpl.java
  *
  *   <P>
  *   Implements all the functionalities found in <B>$SVTVME_DIR/include/am_functions.h</B>
  *   using native interface. </P>
  *   <P>For further details look at <A HREF=../jsvtvme/SvtImpl.html>SvtImpl</A> </P>
  *
  *   @version 0.1
  *   @author  Subir Sarkar
  */
public class AmImpl {
  public AmImpl() {}   // No-arg constructor
  public native int svtvme_am_Init(int initType, int bHandle);
  public native int svtvme_am_TmodeStatus(org.omg.CORBA.IntHolder state, int bHandle);
  public native int svtvme_am_getState(int boardReg, org.omg.CORBA.IntHolder state, int bHandle);
  public native int svtvme_am_resetState(int boardReg, int bHandle);
  public native int svtvme_am_setState(int boardReg, int bHandle);
  // public native int svtvme_am_TmodeEnable(int opCode, int bHandle);
  public native int svtvme_am_sendOpCode(int opCode, int bHandle);
  public native int svtvme_am_writeToOutput(int numWord, int [] data, int bHandle);
  public native int svtvme_am_writeToInput(int numWord, int [] data, int bHandle);
  public native int svtvme_am_outputFile (String filename, int bHandle);
  public native int svtvme_am_inputFile (String filename, int bHandle);
  public native int svtvme_am_read(int layer, int initialAdd, int numWord, 
				   int []data, int bHandle);
  public native int svtvme_am_write(int layer, int initialAdd, int numWord, 
				   int []data, int bHandle); 
  public native int svtvme_am_write_good(int layer, int initialAdd, int numWord, 
				   int[] data, int [] badPattern, int numBadPattern,
                                   int bHandle);  
  public native int svtvme_am_read_good(int layer, int initialAdd, int numWord, 
				   int[] data, int [] badPattern, int numBadPattern,
                                   int bHandle);  
  public native int svtvme_am_downloadPattern (int operation, int plugs, String filename, 
                                   String badname, int bHandle);
  public native int svtvme_am_uploadPattern (int operation, int plugs, String filename, 
                                   String badname, int bHandle);
  public native int svtvme_am_comparePattern (int operation, int plugs, String filename, 
                                   String badname, org.omg.CORBA.IntHolder problems, int bHandle);
  public native int svtvme_am_downloadComparePattern (int operation, int plugs, String filename, 
                                   String badname, org.omg.CORBA.IntHolder problems, int bHandle);
}
