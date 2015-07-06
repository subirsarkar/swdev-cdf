package config.svt;

import com.smartsockets.TipcMsg;
import com.smartsockets.TipcException;
import config.util.AppConstants;

/** 
 * <P>
 * Individual SVT Board specific data. The informations are mostly 
 * single bit and the same for all the boards except for name, slot
 * etc. The class comprises of the following:</P> 
 * <UL>
 *   <LI>Board type a la <B>svtvme</B></LI>
 *   <LI>Slot number where the board is placed in the crate to identify the
 *       board uniquely</LI>
 *   <LI>Status word
 *   <UL>
 *     <LI><B>Run/Test</B> flag string</LI>
 *     <LI><B>Output hold </B> flag</LI>
 *     <LI>CDF Error flag</LI>
 *     <LI>SVT Error flag</LI>
 *   </UL></LI>
 *   <LI>Board Error Registers, 1 ... n</LI>
 *   <LI>Board Error Counters,  1 ... n</LI>
 *   <LI>Spy Buffer data array</LI>
 * </UL>
 *
 * @author Subir Sarkar
 * @version 0.1, March 2001 
 */
public class SvtBoardData {
    /** Debug flag */
  public static final boolean DEBUG = false;
    /** Board name in short, "AMS", "HB" */
  private String type;
    /** Slot number which contains the board */
  private int slot;
    /** Status word which packs a number of informations */
  private int status;             
    /** Single word which packs all the error bits */
  private int [] errorRegisters;
    /** Individual board error register values */
  private int [] errorCounters;
    /** Reference to individual Spy buffer data structure which lies at the end of the
     *  chain. This contains End Event informations for each global erorr bit etc.
     *  @see SvtBufferData
      */
  private SvtBufferData [] buffers;
    /** Construct <CODE>SvtBoardData</CODE> by passing all name, type and slot number. 
     *  A call to <CODE>fillData</CODE> is made to fill all the other fields.
     *  @param  name  Board name in short, "AMS", "HB"
     *  @param  type  Board type, svtvme constants like AMS, HB etc.
     *  @param  slot  Slot number which contains the board
     */
  public SvtBoardData(final String type, int slot)  {
    this.type = type;
    this.slot = slot;
    errorRegisters = new int[0];
    errorCounters  = new int[0];
    buffers        = new SvtBufferData[0];
  }
    /** Fill all the fields with appropriate data
     *  @param  status        Board status
     *  @param  errorRegister Or of all the board error registers
     *  @param  errorCounters Individual board error register values
     *  @param  buffers       Spy buffer data elements
     */
  public void fillData(final int status,
                       final int [] errorRegisters,
                       final int [] errorCounters, 
                       final SvtBufferData [] buffers) 
  {
    fillData(status, errorRegisters, errorCounters);
    fillData(buffers);
  }
    /** Fill the fields with appropriate data
     *  @param  status        Board status
     *  @param  errorRegister Or of all the board error registers
     *  @param  errorCounters Individual board error register values
     */
  public void fillData(int status,
                       final int [] errorRegisters,
                       final int [] errorCounters)
  {
    setStatus(status);
    setErrorRegisters(errorRegisters);
    setErrorCounters(errorCounters);
  }
    /** Fill the fields with appropriate data
     *  @param  buffers       Spy buffer data elements
     */
  public void fillData(final SvtBufferData [] buffers) 
  {
    setBufferData(buffers);
  }
    /** String representeation of the object is returned. The string packs 
     *  all the data present so that the state of the object can be 
     *  displayed in a convenient way.
     *  @return The String representeation of the object
     */
  public String toString() {
    StringBuilder sb = new StringBuilder(AppConstants.MEDIUM_BUFFER_SIZE);

    sb.insert(0, "\n");
    sb.append("         \nBoard = ").append(type).append(" in Slot = ").append(slot);
    sb.append("        \nStatus = ").append(status);
    sb.append(" \nTest/Run mode = ").append(getTMode());
    sb.append("   \nOutput Hold = ").append(getHold());
    sb.append("     \nCDF Error = ").append(getCDFError());
    sb.append("     \nSVT Error = ").append(getSVTError());
    sb.append("\nError Register = ");
    for (int i = 0; i < errorRegisters.length; i++) 
      sb.append(AppConstants.d8Format.sprintf(errorRegisters[i]));
    sb.append(" \nError Counters: ");
    for (int i = 0; i < errorCounters.length; i++) 
      sb.append(AppConstants.d8Format.sprintf(errorCounters[i]));
    sb.append(" \n# of buffers: ").append(buffers.length).append("\n");
    for (int i = 0; i < buffers.length; i++) 
      sb.append(buffers[i]);

    return sb.toString();
  }
    /** Get the type of the SVT board
     *  @return The type of the SVT board
     */
  public String getType() {
    return type;
  }
    /** Get the slot number of the SVT board
     *  @return The slot number of the SVT board
     */
  public int getSlot() {
    return slot;
  }
    /** Set the status flag of the SVT board
     *  @param status Status flag of the SVT Board
     */
  public void setStatus(int status) {
    this.status = status;
  }
    /** Get the status of the board
     *  @return Status of the board
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
    /** Get the Spy status 
     *  @return Spy Status
     */
  public int getSpyStatus() {
    return (status >> 2) & 0x1;
  }
    /** Get the CDF Error flag of the SVT board
     *  @return The CDF Error flag of the SVT board
     */
  public int getCDFError() {
    return (status >> 3) & 0x1;
  }
    /** Get the SVT Error flag of the SVT board
     *  @return The SVT Error flag of the SVT board
     */
  public int getSVTError() {
    return (status >> 4) & 0x1;
  }
    /** Set the board error register status flag of the SVT board
     *  @param errorRegister The board error register status flag of the SVT Board
     */
  public void setErrorRegisters(final int [] errorRegisters) {
    this.errorRegisters  = new int[errorRegisters.length];
    System.arraycopy(errorRegisters, 0, this.errorRegisters, 0, errorRegisters.length);
  }
    /** Set <CODE>errorRegisters</CODE> array element denoted by array index <I>i</I>
     *  @param index   Array index
     *  @param val     The new value of the counter for index <I>i</I>
     */
  public void setErrorRegister(int i, int val)  {
    this.errorRegisters[i] = val;
  }
    /** Get the board error register status flag of the SVT board
     *  @return The board error register status flag of the SVT board
     */
  public int [] getErrorRegisters() {
    return errorRegisters;
  }
    /** Get reference to individual <CODE>errorCounters</CODE> array element 
     *  @param i  The array index
     *  @return Reference to <CODE>errorCounters</CODE> element indexed by i 
     */
  public int getErrorRegister(int i)  {
    return errorRegisters[i];
  }
    /** Set the <CODE>errorCounters</CODE> array
     *  @param errorCounters  The  error counter array
     */
  public void setErrorCounters(final int [] errorCounters) {
    this.errorCounters  = new int[errorCounters.length];
    System.arraycopy(errorCounters, 0, this.errorCounters, 0, errorCounters.length);
  }
    /** Get reference to <CODE>errorCounters</CODE> array 
     *  @return Reference to <CODE>errorCounters</CODE> array
     */
  public int [] getErrorCounters() {
    return errorCounters;
  }
    /** Set <CODE>errorCounters</CODE> array element denoted by array index <I>i</I>
     *  @param index   Array index
     *  @param val     The new value of the counter for index <I>i</I>
     */
  public void setErrorCounter(int i, int val)  {
    this.errorCounters[i] = val;
  }
    /** Get reference to individual <CODE>errorCounters</CODE> array element 
     *  @param i  The array index
     *  @return Reference to <CODE>errorCounters</CODE> element indexed by i 
     */
  public int getErrorCounter(int i)  {
    return errorCounters[i];
  }
    /** Get Number of buffers present in the board for which data are availble
     *  @return Number of buffer for which data are available
     */
  public int getNBuffers() {
    return buffers.length;
  }
    /** Set the  <CODE>SvtBufferData</CODE> array
     *  @param buffers  The  <CODE>SvtBufferData</CODE> array
     */
  public void setBufferData(final SvtBufferData [] buffers) {
    this.buffers = new SvtBufferData[buffers.length];
    System.arraycopy(buffers, 0, this.buffers, 0, buffers.length);
  }
    /** Get reference to <CODE>SvtBufferData</CODE> array 
     *  @return Reference to <CODE>SvtBufferData</CODE> array
     */
  public SvtBufferData [] getBufferData() {
    return buffers;
  }
    /** Set <CODE>SvtBufferData</CODE> array element denoted by array index <I>index</I>
     *  @param index   Array index
     *  @param val     The new value of total error counter for index <I>index</I>
     */
  public void setBufferData(int i, final SvtBufferData val) {
    this.buffers[i] = val;
  }
    /** Get reference to individual <CODE>SvtBufferData</CODE> array element 
     *  @param i  The array index
     *  @return Reference to <CODE>SvtBufferData</CODE> element indexed by i 
     */
  public SvtBufferData getBufferData(int i) {
    return buffers[i];
  }
    /** Get <CODE>SvtBufferData</CODE> array element given by the name of the buffer
     *  @param spy  name of the Spy buffer
     *  @return <CODE>SvtBoardData</CODE> array element given by <I>name/index</I>
     */
  public SvtBufferData getBufferData(final String spy) {
    int index = -1;
    SvtBufferData data = null;

    for (int i = 0; i < buffers.length; i++) {
      if (buffers[i].getType().equals(spy)) {
        index = i;
      }
    }
    if (index >= 0) {
      try {      
        data = buffers[index];
      }
      catch (ArrayIndexOutOfBoundsException ex) {
        ex.printStackTrace();
      }
    }
    if (DEBUG) System.out.println(spy + " " + index);
    return data;
  }
    /**
     * Get the EE error bit
     * @return The EE error bit
     */
  public int getEndEventError() {
    int status = 0;
    for (int i = 0; i < buffers.length; i++) {
      status |= buffers[i].getEndEventError() << i;
    }
    return status;
  }
    /**
     * Get the simulation error bit
     * @return The simulation error bit
     */
  public int getSimulationError() {
    int status = 0;
    for (int i = 0; i < buffers.length; i++) {
      status |= buffers[i].getSimulationError() << i;
    }
    return status;
  }
    /**
     * Update data for all the Spy Buffers for the ith board in the crate 
     * for which message have been published.
     * @param msg  Reference to the message object
     */
  public void updateBufferData(TipcMsg msg) throws TipcException {
    TipcMsg [] bufferMsg = msg.nextMsgArray();
    if (buffers.length == 0) 
      buffers = new SvtBufferData[bufferMsg.length];
    for (int j = 0; j < buffers.length; j++) {
      bufferMsg[j].setCurrent(0);
      String clazz = bufferMsg[j].nextStr();     // Clazz
      long time    = bufferMsg[j].nextInt4();    // time
      String type  = bufferMsg[j].nextStr();
      if (buffers[j] == null) 
         buffers[j] = new SvtBufferData (type);  // Buffer type
      if (DEBUG) System.out.println("Type " + type);

      int nEvents         = bufferMsg[j].nextInt4();
      int nTotEvents      = bufferMsg[j].nextInt4();
      int [] eCounters    = bufferMsg[j].nextInt4Array();
      int [] eTotCounters = bufferMsg[j].nextInt4Array();

      // Spy Buffer data part. We should always fill properly the first three
      // Fields, pointer, freeze, wrap bit, but fill the data array only when asked.
      // The best option may be to fill the first element of the data array with -1.
      // At present I try to extract data from spydump
      int pointer = bufferMsg[j].nextInt4();
      int status  = bufferMsg[j].nextInt4();
      buffers[j].fillData (pointer, status);

      // ----------------------------------------------------------------
      // Presently # of events are computed from spy buffer data which is 
      // filled elsewhere. This is probably going to be the final configuration
      // as well
      // ----------------------------------------------------------------
      // buffers[j].fillData (nEvents, nTotEvents, eCounters, eTotCounters);
      // buffers[j].fillData (bufferMsg[j].nextInt4Array()); 
    }
    fillData(buffers);
  }
}
