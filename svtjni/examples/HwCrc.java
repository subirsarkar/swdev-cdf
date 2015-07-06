import jsvtsim.*;
 
public class HwCrc {
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
  }
  public static void main(String [] argv) {
    Dict d   = new Dict(-1);
    int ret  = d.addFile(argv[0]);
    long crc = d.crc("hwsetCrc");
    d.free();
    System.out.println(argv[0] + ": hwsetCrc " + crc); 
  }
}
