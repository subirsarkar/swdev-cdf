package config.svt;

import java.util.Date;

import config.util.Tools;
import config.util.AppConstants;

import com.smartsockets.TipcMsg;
import com.smartsockets.TipcException;

/**
 *  <P> Holds special data structure which is sufficient to update the SvtCratesPanel 
 *  window in each iteration. This structure can be made light enough by not publishing
 *  spy buffer data at each iteration. This way one can use only one data structure and 
 *  update all the windows following update configuration. In many cases, we would only 
 *  be interested in updating the crate status window. Then we should make sure that 
 *  we know when the spy buffer data was last updated.
 *  </P>
 * 
 *  <P> The data structure maintains <B> Crate -> Board -> Data</B> hiererchy, assuming 
 *  that the information will be published as crate messages at a time. This way 
 *  handling messages will be easier even if they come in arbitrary order in time.</P> 
 *
 *  <P>Here is the layout of the data structure:
 *  <UL>
 *    <LI> Crate 1:
 *      <UL>
 *        <LI>Name</LI>
 *        <LI>Date and Time as a formatted string</LI>
 *        <LI>Run Number</LI>
 *        <LI>Iteration number</LI>
 *        <LI>Level 1 Accept</LI>
 *        <LI>Partition number within which CDF DAQ runs</LI>
 *        <LI>Run Control state</LI>
 *        <LI>Board 1</LI>
 *          <UL>
 *            <LI>Type, Slot</LI>
 *            <LI>Board Status
 *            <UL>
 *              <LI>Run/Test Mode</LI>
 *              <LI>Output Hold</LI>
 *              <LI>CDF Error</LI>
 *              <LI>SVT Error</LI>
 *            </UL></LI>
 *            <LI>Error Registers</LI>
 *            <LI>Board Error Registers 1 ... Board Error Registers n</LI>
 *            <LI>Spy Buffer 1</LI>
 *              <UL>
 *                <LI>Type</LI>
 *                <LI>Events</LI>
 *                <LI>TotEvents</LI>
 *                <LI>EE Error 1 ... EE Error n</LI>
 *                <LI>Total EE Error 1 ... Total EE Error n</LI>
 *                <LI>Spy pointer value </LI>
 *                <LI>Status
 *                <UL>
 *                  <LI>VME Error</LI>
 *                  <LI>Freeze bit</LI>
 *                  <LI>Wrap bit</LI>
 *                  <LI>EE Error</LI>
 *                  <LI>Simulation Error</LI>
 *                </UL></LI>
 *                <LI>Spy buffer data array</LI>
 *              </UL>
 *            <LI> Repeat for all the Spy Buffers</LI>
 *          </UL>
 *        <LI>Repeat for all the boards present</LI>
 *      </UL>
 *    </LI>
 *    <LI> Repeat for other crates </LI>
 *  </UL>
 *  </P>
 *  @author S. Sarkar
 *  @version 0.1, March 2001
 */
public class SvtErrorData {
    /** Debug flag */
  public static final boolean DEBUG = false;
    /** Array of Crate Data Structure. The array is initialised to have
     *  <CODE>AppConstants.nCrates</CODE> elements in the constructor.
     *  Each time a related message published, the crate name is searched for
     *  and the correspoding CrateData is updated.
     *  @see SvtCrateData
     */
  private SvtCrateData [] crateData;
    /** Initialise the class on object creation, creates the <CODE>SvtCrateData</CODE> 
     *  array for all the <CODE>AppConstants.nCrates</CODE> crates and
     *  initialises to null.
     */
  public SvtErrorData() {
    crateData = new SvtCrateData[AppConstants.nCrates];
  }
    /** Set reference to the <CODE>SvtCrateData</CODE> array 
     *  @param crateData Reference to <CODE>SvtCrateData</CODE> array
     */
  public void setCrateData(final SvtCrateData [] crateData) {
    if (crateData.length > this.crateData.length)
       throw new ArrayIndexOutOfBoundsException("Crate data array must be <= " + this.crateData.length);
    System.arraycopy(crateData, 0, this.crateData, 0, crateData.length);
  }
    /** Set individual <CODE>SvtCrateData</CODE> element given the index 
     *  @param  index  Array index
     *  @param  obj    Reference to individual <CODE>Cratedata</CODE> element
     */
  public void setCrateData(int index, final SvtCrateData obj) {
    crateData[index] = obj;
  }
    /** Get reference to the <CODE>SvtCrateData</CODE> array 
     *  @return Reference to <CODE>SvtCrateData</CODE> array
     */
  public SvtCrateData [] getCrateData() {
    return crateData;
  }
    /** Get individual <CODE>SvtCrateData</CODE> element given the index 
     *  @param  index  Array index
     *  @return Reference to individual <CODE>Cratedata</CODE> element
     */
  public SvtCrateData getCrateData(int index)  {
    return crateData[index];
  }
    /** Get individual <CODE>SvtCrateData</CODE> element given the index 
     *  @param  name  Name of the crate
     *  @return Reference to individual <CODE>Cratedata</CODE> element
     */
  public SvtCrateData getCrateData(final String name) {
    int index = Tools.getCrateIndex(name);
    if (DEBUG) System.out.println("getCrateData() -> " + name + " " + index);
    try {
      return crateData[index];
    }
    catch (ArrayIndexOutOfBoundsException ex) {
      System.out.println("Looking beyond range " + ex.getMessage());
      ex.printStackTrace();
      return null;
    }
  }
    /** String representeation of the object is returned. The string packs 
     *  all the data present so that the state of the object can be 
     *  displayed in a convenient way.
     *  @return The String representeation of the object
     */
  public String toString() {
    StringBuilder sb = new StringBuilder(AppConstants.LARGE_BUFFER_SIZE);
    for (int i = 0; i < crateData.length; i++) {
      if (crateData[i] == null)
	sb.append("\n").append("Element crateData[").append(i).append("] not filled yet!"); 
      else
	sb.append("\n").append(crateData[i]);
    }
    return sb.toString();
  }
    /**
     * Update data for the crate for which message have been published.
     * @param name Name of the crate
     * @param msg  Reference to the message object
     */
  public int updateCrateData(TipcMsg msg) throws TipcException {
    if (DEBUG) msg.print();
    msg.setCurrent(0);

    String clazz = msg.nextStr();
    long time    = msg.nextInt4();
    Date date    = new Date(time*1000L);
    String name  = msg.nextStr();

    if (DEBUG) System.out.println("Updating SvtErrorData for crate " + name);
    int index = Tools.getCrateIndex(name);    
    if (index >= AppConstants.nCrates)
       throw new ArrayIndexOutOfBoundsException("Crate data array size must be <= " + this.crateData.length);

    if (crateData[index] == null) crateData[index] = new SvtCrateData(name);

    int iter  = msg.nextInt4();
    int run   = msg.nextInt4();
    int L1Acc = msg.nextInt4();
    int partition = msg.nextInt4();
    String rcState = msg.nextStr();
    crateData[index].fillData (iter, date, run, L1Acc, partition, rcState);
    crateData[index].updateBoardData(msg);
    return index;
  }
}
