package messages;

import rc.*;
import daqmsg.*;
import java.io.*;

/** 
 * <P>
 * Defines the individual Spy Buffer data structure. Objects
 * of this data type are contained within <CODE>SvtBoardStatusElement</CODE> as member.
 * This class comprises the following informations</P>
 * <UL>
 *   <LI>Type (name) of buffer a la <B>svtvme</B></LI>
 *   <LI>Number of Events present in the Spy Buffer read in the present iteration</LI>
 *   <LI>Total number of events analysed so far</LI>
 *   <LI>End Event Error 1 ... EE Error n array</LI>
 *   <LI>Total End Event Error 1 ... Total EE Error n array</LI>
 *   <LI>Spy pointer value</LI>
 *   <LI>
 *    Status word which packs a number of informations
 *    <UL>
 *      <LI>Bit 0: VME access error</LI>
 *      <LI>Bit 1: Freeze Status</LI>
 *      <LI>Bit 2: Wrap Status</LI>
 *      <LI>Bit 3: End Event Error</LI>
 *      <LI>Bit 4: Simulation Error</LI>
 *    </UL></LI>
 * </UL>
 *
 */
public class SVTSpyBufferStatusElement
       extends daqmsg.DaqMsg implements Serializable 
{
  public static int MESSAGE_TYPE = DaqMsgBase.SVTMON_BASE + 102;

  // Spy buffer data

    /** Type a la svtvme */
  public String Type;
    /** Number of events in <I>this</I> buffer */
  public int nEvents;
    /** Total number of events since last reset */
  public int nTotEvents;
    /** Error counters in <I>this</I> buffer */
  public int[] errorCounter;
    /** Total Error counters acculated since last reset */
  public int[] TotErrorCounter;
    /** Spy pointer value */
  public int pointer;
    /** Status which packs many informations 
     * <UL>
     *   <LI>Bit 0: VME access error</LI>
     *   <LI>Bit 1: Freeze Status</LI>
     *   <LI>Bit 2: Wrap Status</LI>
     *   <LI>Bit 3: End Event Error</LI>
     *   <LI>Bit 4: Simulation Error</LI>
     * </UL>
     */
  public int status;

  public SVTSpyBufferStatusElement() 
  {
    super();
    messageType = MESSAGE_TYPE;
    Type        = "";
    nEvents     = -1;
    nTotEvents  = -1; 
    status      = 0;
    errorCounter    = new int[8];
    TotErrorCounter = new int[8];
    for (int i = 0; i < errorCounter.length; i++) { 
      errorCounter[i] = 0;
      TotErrorCounter[i] = 0;
    }
  }

  public void print() {
    System.out.println("Spy Buffer Status:");
    System.out.println("            Type =" + Type);
    System.out.println("      nEvents = " + nEvents);
    System.out.println("    nTotEvents = " + nTotEvents);
    System.out.println("    status = " + status);
    for (int i = 0; i < errorCounter.length; i++) {
      System.out.println("   Error counter " + i + errorCounter[i]);
    }
    for (int i = 0; i < TotErrorCounter.length; i++) {
      System.out.println("   Tot Error counter " + i + TotErrorCounter[i]);
    }  
  }
    /** String representeation of the object is returned. The string packs 
     *  all the data present so that the state of the object can be 
     *  displayed in a convenient way.
     *  @return The String representeation of the object
     */
  public String toString() {
    StringBuffer buf = new StringBuffer(255);
    buf.insert(0, "\nBuffer: " + Type);
    buf.append("\n# of Events: " + nEvents + "Total # of events " + nTotEvents);
    buf.append("\nError Counters:");
    String [] errorNames = {
       "Parity Error",
       "Lost Sync",
       "FIFO Overflow",
       "Invalid Data",
       "Internal Overflow",
       "Truncated Output",
       "G-Link Lost Lock",
       "Parity Error in Cable to Level 2"
    };     
    for (int i = 0; i < errorCounter.length; i++)
      buf.append("\n" + errorNames[i] + 
                 " "  + errorCounter[i] +
                 " "  + TotErrorCounter[i]
      );
    buf.append("\nSpy pointer " + pointer);
    buf.append("\nStatus: " + status);
    buf.append("\nVME Error" + getVmeError());
    buf.append("\nFreeze Status" + getFreeze());
    buf.append("\nWrap bit " + getWrap() + "\n");
    buf.append("\nEE Error" + getEndEventError());
    buf.append("\nSimulation Error" + getSimulationError());
    return buf.toString();
  }
    /** Get the type of the Spy buffer 
     *  @return The type of the Spy buffer
     */
  public String getType() {
    return Type;
  }
    /** Get number of events read by the monitoring program 
     *  @return number of event read by the monitoring program
     */
  public int getEvents() {
    return nEvents;
  }
    /** Get total number of events read by the monitoring program  
     *  @return number of event read by the monitoring program 
     */ 
  public int getTotEvents() { 
    return nTotEvents; 
  }           
    /** Get reference to error counter array 
     *  @return Reference to error counter array
     */
  public int [] getErrorCounters() {
    return errorCounter;
  }
    /** Get EE error by error array index
     *  @param index  Array index
     *  @return The total number of times error denoted by <I>index</I> has occured 
     */
  public int getErrorCounter(int index) {
    return errorCounter[index];
  }
      /** Get reference to total error counter array 
     *  @return Reference to total error counter array
     */
  public int [] getTotalErrorCounters() {
    return TotErrorCounter;
  }
      /** Get total EE error count by error array index
     *  @param index  Array index
     *  @return The total number of times error denoted by <I>index</I> has occured 
     */
  public int getTotalErrorCounter(int index) {
    return TotErrorCounter[index];
  }
    /** Get the spy pointer value of the present buffer
     *  @return spy pointer value of the present buffer
     */
  public int getPointer() {
    return pointer;
  }
    /** Get the VME error status bit
     *  @return The VME error status bit 
     */
  public int getVmeError() {
    return status  & 0x1;
  }
    /** Get the freeze status bit of the present buffer
     *  @return freeze status bit of the present buffer
     */
  public int getFreeze() {
    return (status >> 1) & 0x1;
  }
    /** Get the Spy pointer overflow (wrap) bit of the present buffer
     *  @return Spy pointer overflow (wrap) bit of the present buffer
     */
  public int getWrap() {
    return (status >> 2) & 0x1;
  }
    /** Get the VME error status bit
     *  @return The VME error status bit 
     */
  public int getEndEventError() {
    return (status >> 3) & 0x1;
  }
    /** Get the VME error status bit
     *  @return The VME error status bit 
     */
  public int getSimulationError() {
    return (status  >> 4) & 0x1;
  }
}
