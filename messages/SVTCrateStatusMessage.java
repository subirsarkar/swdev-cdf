package messages;

import java.io.*;
import rc.*;
import daqmsg.*;

/**
  * Definition of an SVT status message to be exchanged 
  * between svtmon crate process and other clients. 
  *      
  * @author  M. Rescigno 2/22/01
  * @version 0.1
  * @version 0.2  new informations plus accessor to each (public) field added
  */
public class SVTCrateStatusMessage 
      extends daqmsg.DaqMsg implements Serializable 
{
    /** DaqMsg message type, has to be unique */
  public static int MESSAGE_TYPE = DaqMsgBase.SVTMON_BASE + 100;

  // First crate general data
    /** Crate name */
  public String crateName;   // NEVER override getName() accessor in this class!!!!
    /** Iteration number of SPYMON */
  public int monitorIter;
    /** Run number of data taking during the VME read */
  public int runNumber;
    /** L1 Accept since last read */
  public int L1AsinceLastIter;
    /** Partition number */
  public int partition;
    /** Run Control state */
  public String rcState;
    /** Board status info array */
  public SVTBoardStatusElement [] boardElement;

    /** no-arg constructor, required for DaqMsg to work */
  public SVTCrateStatusMessage() { 
    super(); 
    messageType  = MESSAGE_TYPE;
    crateName    = "";
    runNumber    = -1;
    monitorIter  = -1;
    L1AsinceLastIter = -1;
    partition    = -1;
    rcState      = "Unknown";
    boardElement = new SVTBoardStatusElement[1];
  }

    /** print method similar to <CODE>toString()</CODE> but prints 
     *  the information directly 
     */
  public void print() {
    System.out.println("CrateStatus:");
    System.out.println("      Name =" + crateName);
    System.out.println("      run # = " + runNumber);
    System.out.println("    monitor cycle  = " + monitorIter);
    System.out.println(" L1A = " + L1AsinceLastIter);
    System.out.println("     # card = " + boardElement.length);
    for (int i = 0; i < boardElement.length; i++) {
      boardElement[i].print();
    }
  }
    /** String representeation of the object is returned. The string packs 
     *  all the data present so that the state of the object can be 
     *  displayed in a convenient way.
     *  @return The String representeation of the object
     */
  public String toString() {
    StringBuffer buf = new StringBuffer(1000);
    buf.insert(0, " CrateStatus:");
    buf.append("\nName:      " + crateName);
    buf.append("\nTimestamp: " + time);
    buf.append("\nIteration: " + monitorIter);
    buf.append("\nRun #:     " + runNumber);
    buf.append("\nL1 Accept: " + L1AsinceLastIter);
    buf.append("\npartition: " + partition);
    buf.append("\nRCState: "   + rcState);
    buf.append("\nBoards:    " + boardElement.length);
    for (int i = 0; i < boardElement.length; i++)
      if (boardElement[i] != null) buf.append("\n" + boardElement[i]);
    return buf.toString();
  }
    /** Get the name of the SVT crate
     *  @return The name of the SVT crate
     */
  public String getCrateName() {
    return crateName;
  }
    /** Get the time stamp in the current iteration
     *  @return The time stamp if the current iteration
     */
  public long getTimeStamp() {
    return time;
  }
    /** Get the spyMon iteration number of the current run
     *  @return spyMon iteration number of the current run
     */
  public int getIteration() {
    return monitorIter;
  }
    /** Get the Run number of DAQ during the current iteration
     *  @return Run number of DAQ during the current iteration
     */
  public int getRunNumber() {
    return runNumber;
  }
    /** Get the Level 1 accept rate since last iteration
     *  @return The Level 1 accept rate since last iteration
     */
  public int getL1Accept() {
    return L1AsinceLastIter;
  }
    /** Get the Partition number of the RC during the current iteration
     *  @return The Partition number of the RC during the current iteration
     */
  public int getPartition() {
    return partition;
  }
    /** Get the RC State in the current iteration
     *  @return The RC State in the current iteration
     */
  public String getRCState() {
    return rcState;
  }
    /** Get reference to <CODE>SVTBoardStatusElement</CODE> array 
     *  @return Reference to <CODE>SVTBoardStatusElement</CODE> array
     */
  public SVTBoardStatusElement [] getBoardData() {
    return boardElement;
  }
    /** Get <CODE>SVTBoardStatusElement</CODE> array element given by index
     *  @param index  Array index
     *  @return <CODE>SVTBoardStatusElement</CODE> array element given by <I>index</I>
     */
  public SVTBoardStatusElement getBoardData(int index) {
    SVTBoardStatusElement data = null;
    try {
      data = boardElement[index];
    }
    catch (ArrayIndexOutOfBoundsException ex) {
      System.out.println("Exceptio: " + ex.getMessage());
      ex.printStackTrace();
    }
    catch (Exception ex) {
      System.out.println("Exceptio: " + ex.getMessage());
      ex.printStackTrace();
    }
    return data;
  }
    /** Get <CODE>SvtBoardData</CODE> array element given by the name of the board
     *  @param index  Array index
     *  @return <CODE>SvtBoardData</CODE> array element given by <I>index</I>
     */
  public SVTBoardStatusElement getBoardData(final String board, final int slot) {
    int index = -1;

    for (int i = 0; i < boardElement.length; i++) {
      if (boardElement[i].getType().equals(board) && boardElement[i].getSlot() == slot) {
        index = i;
      }
    }
    return getBoardData(index);
  }
}

