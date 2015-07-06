import jsvtvme.*;

public class AmsTest implements SvtvmeConstants {
  private Board spyB, amsB;
  static {
    try {
      System.loadLibrary("SvtvmeImpl");
    } catch (UnsatisfiedLinkError e) {
      System.err.println("Cannot load the example native code. " + 
        " \nMake sure your LD_LIBRARY_PATH contains the path where the shared library is, try" +
        "setenv LD_LIBRARY_SPATH ${LD_LIBRARY_PATH}:${SVTVME_LD_PATH}");
      System.exit(1);
    }
  }
  public AmsTest() {
     System.out.println("Initialise AmsTest");
  }  
  public AmsTest(final String crate, final int spySlot, final int amsSlot) {
    int error = 0;
    org.omg.CORBA.IntHolder state = new org.omg.CORBA.IntHolder();

    spyB = new Board(crate, spySlot, SC);
    amsB = new Board(crate, amsSlot, AMS);

    spyB.freeze();
    System.out.println(" Error code: " + error);

    error = spyB.getState(SC_BACKPLANE_FREEZE, state);
    System.out.println(" Freeze Status: " + state.value);

    error = amsB.getState(AMS_HSPY_PTR, state);
    System.out.println(" Error code: " + error);
    System.out.println("Ams Hit Spy buffer Pointer " + state.value);

    error = amsB.getState(AMS_HSPY_FRZ, state);
    System.out.println(" Error code: " + error);
    System.out.println("Ams Hit Spy buffer Freeze   " + state.value);

    error = amsB.getState(AMS_HSPY_WRP, state);
    System.out.println(" Error code: " + error);
    System.out.println("Ams Hit Spy buffer Overflow " + state.value);

    int [] data = new int[1000];
    error = amsB.readSpyTail(AMS_HIT_SPY, 1000, data);
    for (int i = 0; i < 1000; i++) {
      System.out.println("Add = 0x" + Integer.toHexString(i) + " (" + i + "),  Data = 0x" +
       Integer.toHexString(data[i]&0x7fffff) + " = " + Integer.toBinaryString(data[i]));
    }

    spyB.release();
    System.out.println(" Error code: " + error);

    error = spyB.getState(SC_BACKPLANE_FREEZE, state);
    System.out.println(" Freeze Status now: " + state.value);

    // svt.closeBoard();
    // svt.closeBoard();
  }
  public void printHistory() {
    System.out.println("Print history");
  }
  public static void main(String [] argv) {
    new AmsTest("b0svt05.fnal.gov", 3, 8);
  }
}
