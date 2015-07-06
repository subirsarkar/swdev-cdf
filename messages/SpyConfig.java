import java.io.*;
import daqmsg.*;

/**
  *   Spy Configuration
  *
  *   @verison 0.1
  *   @author  S. Sarkar 6/19/00
  */
public class SpyConfig extends DaqMsg implements Serializable {
    public String crateName;
    public int phiSlice;
    public int nBoards;
    public String [] boardName;
    public int [] slot;
    public int []nSpyBuffer;

    public static final int MESSAGE_TYPE = 4003;

    String [] boards  = {"SPY", "HF", "HF", "HF", "Merger", "AMS", "AMB", "AMS", "HB"};
    int [] slots      = {3, 2, 3, 4, 5, 6, 7, 8, 9};
    int [] nSpy       = {0, 11, 11, 11, 2, 0, 0, 2, 3};
    
    // Constructor
    public SpyConfig() {
       this.crateName  = "b0svt04";
       this.phiSlice   = 1;
       this.nBoards    = 9;
       this.boardName  = boards;
       this.slot       = slots;
       this.nSpyBuffer = nSpy;
    }

    public int getTime() {
        return time;
    }
    /* Get and set Crate name */ 
    public String getCrate() {
        return crateName;
    }
    public void setCrate(String crateName) {
        this.crateName = crateName;
    }

    /* Get phi Slice */
    public int getSlice() {return phiSlice;}

    /* Get and set number of boards present in the Slice */
    public int getNumBoads() {
        return nBoards;
    }
    public void setNumBoads(int nBoards) {
        this.nBoards = nBoards;
    }

    /* Get and set board names */
    public String [] getBoardName() {
      return boardName;
    }
    public void setBoardName(String [] boardName) {
      this.boardName = boardName;
    }

    /* Get and set(!!) slot numbers */
    public int [] getSlot() {return slot;}
    public void setSlot(int [] slot) {
      this.slot = slot;      
    }

    /* Get and sets number of Spy buffers in a each board */
    public int [] getNumSpy() {return nSpyBuffer;}
    public void setNumSpy(int [] nSpy) {
      this.nSpyBuffer = nSpy;      
    }
}  

