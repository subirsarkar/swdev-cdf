package messages;

import rc.*;
import daqmsg.*;
import java.io.*;

/** 
 * <P>
 * Individual SVT Board specific data. The informations are mostly 
 * single bit and the same for all the boards except for type, slot
 * etc. The class contains the following informations</P> 
 * <UL>
 *   <LI>Board type a la <B>svtvme</B></LI>
 *   <LI>Slot number where the board is placed in the crate to identify the
 *       board uniquely</LI>
 *   <LI>Status word
 *   <UL>
 *     <LI>Bit 0: <B>Run/Test</B> flag string</LI>
 *     <LI>Bit 1: <B>Output hold </B> flag</LI>
 *     <LI>Bit 2: CDF Error flag</LI>
 *     <LI>Bit 3: SVT Error flag</LI>
 *   </UL></LI>
 *   <LI>Error Registers, the value read thru' VME</LI>
 *   <LI>Board Error Registers 1 ... Board Error Registers n flags</LI>
 *   <LI>Spy Buffer information array</LI>
 * </UL>
 *
 */
public class SVTBoardStatusElement
       extends daqmsg.DaqMsg implements Serializable 
{
    /** DaqMsg message type, has to be unique */
  public static int MESSAGE_TYPE = DaqMsgBase.SVTMON_BASE + 101;

  // Board data
    /** Name of the board */
  public String Type;
    /** Slot position in the crate */
  public int slot;  
    /** Status word which contains status for
     *  <UL>
     *    <LI>Test/Run Mode</LI>
     *    <LI>Output Hold</LI>
     *    <LI>CDF Error</LI>
     *    <LI>SVT Error</LI>
     *  </UL>
     */
  public int status;
    /** The error register value which is read thru' VME */
  public int[] errorRegister;
    /** The above info split into individual bits */
  public int[] errorCounter;
    /** Spy Buffer data array */
  public SVTSpyBufferStatusElement[] sbElement;

   // no-arg constructor, required for DaqMsg
  public SVTBoardStatusElement() 
  {
    super();
    messageType = MESSAGE_TYPE;
    Type        = "";
    slot        = -1;
    status      = 0;
    errorRegister = new int[1];
    errorCounter  = new int[1];
    for (int i = 0; i < errorCounter.length; i++) 
      errorCounter[i] = 0;
    sbElement = new SVTSpyBufferStatusElement[1];   
  }

   /** A print method which is similar to <CODE>toString()</CODE> but 
    *  directly prints the informations 
    */
  public void print() {
    System.out.println("BoardStatus:");
    System.out.println("      Type =" + Type);
    System.out.println("      slot = " + slot);
    System.out.println("    status = " + status);
    for (int i = 0; i < errorRegister.length; i++) {
      System.out.println("   Error Reg n." + i + errorRegister[i]);
    }
    for (int i = 0; i < errorCounter.length; i++) {
      System.out.println("   Error bit " + i + errorCounter[i]);
    }
    for (int i = 0; i < sbElement.length; i++) {
      sbElement[i].print();
    }
  }
    /** String representeation of the object is returned. The string packs 
     *  all the data present so that the state of the object can be 
     *  displayed in a convenient way.
     *  @return The String representeation of the object
     */
  public String toString() {
    StringBuffer buf = new StringBuffer(1000);
    buf.insert(0, "\nBoard: " + Type + "in Slot: " + slot + ":");
    buf.append(" \nStatus: " + status);
    buf.append(" \nError Registers: ");
    for (int i = 0; i < errorRegister.length; i++) 
      buf.append(" " + errorRegister[i]);
    buf.append(" \nError Counters: ");
    for (int i = 0; i < errorCounter.length; i++) 
      buf.append(" " + errorCounter[i]);
    buf.append(" \n# of buffers: " + sbElement.length + "\n");
    for (int i = 0; i < sbElement.length; i++) 
      buf.append(sbElement[i]);
    return buf.toString();
  }
    /** Get the type of the SVT board
     *  @return The type of the SVT board
     */
  public String getType() {
    return Type;
  }
    /** Get the slot number of the SVT board
     *  @return The slot number of the SVT board
     */
  public int getSlot() {
    return slot;
  }
    /** Get the status word which contains packed informations
     *  @return The status word which contains packed informations 
     */
  public int getStatus() {
    return status;
  }
    /** Get Test/Run mode flag of the SVT board
     *  @return The Test/Run mode flag of the SVT board
     */
  public int getTMode() {
    return status & 0x1;
  }
    /** Get the output hold flag of the SVT board
     *  @return The OR of the output hold flags of the SVT board
     */
  public int getHold() {
    return (status >> 1) & 0x1;
  }
    /** Get the CDF Error flag of the SVT board
     *  @return The CDF Error flag of the SVT board
     */
  public int getCDFError() {
    return (status >> 2) & 0x1;
  }
    /** Get the SVT Error flag of the SVT board
     *  @return The SVT Error flag of the SVT board
     */
  public int getSVTError() {
    return (status >> 3) & 0x1;
  }
    /** Get the board error registers array of the SVT board
     *  @return The board error register status flag of the SVT board
     */
  public int [] getErrorRegister() {
    return errorRegister;
  }
    /** Get reference to individual <CODE>errorRegister</CODE> array element 
     *  @param i  The array index
     *  @return Reference to <CODE>errorRegister</CODE> element indexed by i 
     */
  public int getErrorRegister(int i)  {
    return errorRegister[i];
  }
    /** Get reference to <CODE>errorCounter</CODE> array 
     *  @return Reference to <CODE>errorCounter</CODE> array
     */
  public int [] getErrorCounters() {
    return errorCounter;
  }  
    /** Get reference to individual <CODE>errorCounter</CODE> array element 
     *  @param i  The array index
     *  @return Reference to <CODE>errorCounter</CODE> element indexed by i 
     */
  public int getErrorCounter(int i)  {
    return errorCounter[i];
  }
    /** Get Number of buffers present in the board for which data are availble
     *  @return Number of buffer for which data are availble
     */
  public int getNBuffers() {
    return sbElement.length;
  }
    /** Get reference to <CODE>SVTSpyBufferStatusElement</CODE> array 
     *  @return Reference to <CODE>SVTSpyBufferStatusElement</CODE> array
     */
  public SVTSpyBufferStatusElement [] getBufferData() {
    return sbElement;
  }
    /** Get reference to individual <CODE>SVTSpyBufferStatusElement</CODE> array element 
     *  @param i  The array index
     *  @return Reference to <CODE>SVTSpyBufferStatusElement</CODE> element indexed by i 
     */
  public SVTSpyBufferStatusElement getBufferData(int i) {
    SVTSpyBufferStatusElement data = null;
    try {      
      data = sbElement[i];
    }
    catch (ArrayIndexOutOfBoundsException ex) {
      ex.printStackTrace();
    }
    return data;
  }
    /** Get <CODE>SVTSpyBufferStatusElement</CODE> array element given by the name of the buffer
     *  @param spy  name of the Spy buffer
     *  @return <CODE>SVTSpyBufferStatusElement</CODE> array element given by <I>name/index</I>
     */
  public SVTSpyBufferStatusElement getBufferData(final String spy) {
    int index = -1;

    for (int i = 0; i < sbElement.length; i++) {
      if (sbElement[i].getType().equals(spy)) {
        index = i;
      }
    }
    return getBufferData(index);
  }
}




