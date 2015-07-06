package config.svt;

import java.util.Vector;
import java.util.Iterator;
import config.util.Tools;
import config.util.AppConstants;

/** 
 * <P>
 * Defines the individual Spy Buffer data structure. Objects
 * of this data type are contained within <CODE>BoardData</CODE> as member.
 * This class comprises of the following informations:</P>
 * <UL>
 *   <LI>Type (name) of buffer a la <B>svtvme</B></LI>
 *   <LI>Number of Events present in the Spy Buffer read in the present iteration</LI>
 *   <LI>Total number of events analysed so far</LI>
 *   <LI>End Event Error 1 ... EE Error n array</LI>
 *   <LI>Total End Event Error 1 ... Total EE Error n array</LI>
 *   <LI>Spy pointer value</LI>
 *   <LI>
 *    Status which pack a number of informations
 *    <UL>
 *      <LI>Bit 0: VME access error</LI>
 *      <LI>Bit 1: Freeze Status</LI>
 *      <LI>Bit 2: Wrap Status</LI>
 *      <LI>Bit 3: End Event Error</LI>
 *      <LI>Bit 4: Simulation Error</LI>
 *    </UL>
 *   <LI>Spy buffer data</LI>
 * </UL>
 *
 * @author Subir Sarkar
 * @version 0.1, March 2001 
 */
public class SvtBufferData implements SvtEvents {
  private static final int INIT_SIZE  = 100;
    /** Debug flag */
  public static final boolean DEBUG = false;
    /** Type of the Spy buffer, i.e <CODE>AMS_HIT_SPY</CODE> etc.*/
  private String type;
    /** Number of words present in the spy buffer read */
  private int nEvents = 0;
    /** Total number of events analysed so far */
  private int nTotEvents = 0;
    /** Individual End Event error counters */
  private int [] errorCounters;
    /** Total End Event error counters */
  private int [] totalErrorCounters;
    /** Spy pointer value */
  private int pointer = 0;
    /** Status integer which packs a number of informations */
  private int status = 0;
    /** Spy buffer Data */
  private int [] data;
    /** Names of End Event error bits */
  private Vector<SvtEvent> events = new Vector<SvtEvent>(INIT_SIZE);
    /** Initialise the object by with name and type. All the other fields are
     *  filled later with a call to fillData or alternatively to call to 
     *  individual set methods
     *  @param type  Name of the Spy buffer, i.e <CODE>AMS_HIT_SPY</CODE> etc.         
     */
  public SvtBufferData(final String type) {
    this.type  = type;
    errorCounters      = new int[AppConstants.gerrors.length];
    totalErrorCounters = new int[AppConstants.gerrors.length];
    for (int i = 0; i < AppConstants.gerrors.length; i++)
       totalErrorCounters[i] = 0;
    data = new int[0];
  }
    /** Fill data fields with appropriate values
     * @param nEvents            Number of words present in the spy buffer read
     * @param errorCounters      Individual End Event error counters
     * @param totalErrorCounters Total End Event error counters
     * @param status             Status of the buffer as a whole
     */
  public void fillData(int nEvents, int nTotEvents,
                       final int [] errorCounters, 
                       final int [] totalErrorCounters)
  {
    this.nEvents    = nEvents;
    this.nTotEvents = nTotEvents;
    setErrorCounters(errorCounters);
    setTotalErrorCounters(errorCounters);
  }
    /** Fill data fields with appropriate values
     * @param pointer Spy pointer value
     * @param status  Status of the buffer as a whole
     */
  public void fillData(int pointer, int status) {
    this.pointer = pointer;
    this.status  = status;
  }
    /** Fill data fields with appropriate values
     * @param data  Spy buffer words
     */
  public void fillData(final Vector<Integer> list) {
    setData(list);
  }
  public void fillData(final int [] data) {
    setData(data);
  }
    /** String representeation of the object is returned. The string packs 
     *  all the data present so that the state of the object can be 
     *  displayed in a convenient way.
     *  @return The String representeation of the object
     */
  public String toString() {
    StringBuilder sb = new StringBuilder(AppConstants.MEDIUM_BUFFER_SIZE);

    sb.insert(0,"-------------------------------------------\n");
    sb.append("          Buffer = ").append(type).append("\n");
    sb.append("     # of Events = ").append(nEvents).append("\n");
    sb.append("   Error Counters: ").append("\n");
    for (int i = 0; i < errorCounters.length; i++) {
      sb.append(AppConstants.s32Format.sprintf(AppConstants.gerrors[i]));
      sb.append(AppConstants.d6Format.sprintf(errorCounters[i]));
      sb.append(AppConstants.d6Format.sprintf(totalErrorCounters[i]));
      sb.append("\n");
    }
    sb.append("     Spy pointer = ").append(AppConstants.h6Format.sprintf(pointer)).append("\n");
    sb.append("       VME Error = ").append(getVmeError()).append("\n");
    sb.append("   Freeze Status = ").append(getFreeze()).append("\n");
    sb.append("        Wrap bit = ").append(getWrap()).append("\n");
    sb.append("        EE Error = ").append(getEndEventError()).append("\n");
    sb.append("Simulation Error = ").append(getSimulationError()).append("\n");
    sb.append(Tools.getFD(data));

    return sb.toString();
  }
    /** Get the type of the Spy buffer 
     *  @return The type of the Spy buffer
     */
  public String getType() {
    return type;
  }
    /** Set number of events read by the monitoring program 
     *  @param nEvent  Number of events read my the monitoring program
     */
  public void setEvent(int nEvents) {
    this.nEvents = nEvents;
  }
    /** Get number of event read by the monitoring program 
     *  @return number of event read by the monitoring program
     */
  public int getEvent() {
    return nEvents;
  }
    /** Set total number of events analysed so far
     *  @param nTotEvent  Total number of events analysed so far
     */
  public void setTotEvent(int nTotEvents) {
    this.nTotEvents = nTotEvents;
  }
    /** Get total number of events analysed so far
     *  @return Total number of events analysed so far
     */
  public int getTotEvent() {
    return nTotEvents;
  }
    /** Set the error counter array
     *  @param  errorCounters  The error counter array
     */
  public void setErrorCounters(final int [] errorCounters) {
    System.arraycopy(errorCounters, 0, this.errorCounters, 0, 
         Math.min(errorCounters.length, this.errorCounters.length));
  }
    /** Get reference to error counter array 
     *  @return Reference to error counter array
     */
  public int [] getErrorCounters() {
    return errorCounters;
  }
    /** Set EE error for error denoted by array index <I>index</I>
     *  @param index   Array index
     *  @param val     The new value of total error counter for index <I>index</I>
     */
  public void setErrorCounter(int index, int val) {
    errorCounters[index] = val;
  }
    /** Get EE error by error array index
     *  @param index  Array index
     *  @return The total number of times error denoted by <I>index</I> has occured 
     */
  public int getErrorCounter(int index) {
    return errorCounters[index];
  }
    /** Get error value by <B>error name</B>
     *  @param errorName  Name of the error (<I>Parity Error</I> etc.)
     *  @return The value of error denoted by <I>errorName</I>
     */
  public int getErrorCounter(final String errorName) {
    int index = -1;
    for (int i = 0; i < AppConstants.gerrors.length; i++) {
      if (errorName.equals(AppConstants.gerrors[i])) index = i;
    }
    return ((index < 0) ? -1 : errorCounters[index]);
  }
    /** Set total error counter array
     *  @param  totalErrorCounters  The error counter array
     */
  public void setTotalErrorCounters(final int [] totalErrorCounters) {
    System.arraycopy(totalErrorCounters, 0, this.totalErrorCounters, 0, 
         Math.min(totalErrorCounters.length, this.totalErrorCounters.length));
  }
    /** Get reference to total error counter array 
     *  @return Reference to total error counter array
     */
  public int [] getTotalErrorCounters() {
    return totalErrorCounters;
  }
    /** Set total error count for error denoted by array index <I>index</I>
     *  @param index   Array index
     *  @param val     The new value of total error counter for index <I>index</I>
     */
  public void setTotalErrorCounter(int index, int val) {
    this.totalErrorCounters[index] = val;
  }
    /** Get total EE error count by error array index
     *  @param index  Array index
     *  @return The total number of times error denoted by <I>index</I> has occured 
     */
  public int getTotalErrorCounter(int index) {
    return totalErrorCounters[index];
  }
    /** Get total EE error count by <B>error name</B>
     *  @param errorName  Name of the error (<I>Parity Error</I> etc.)
     *  @return The total number of times error denoted by <I>errorName</I> has occured 
     */
  public int getTotalErrorCounter(final String errorName) {
    int index = -1;
    for (int i = 0; i < AppConstants.gerrors.length; i++) 
      if (errorName.equals(AppConstants.gerrors[i])) index = i;
    return ((index < 0) ? -1 : totalErrorCounters[index]);
  }
    /** Get the status of the present buffer
     *  @return Status of the buffer
     */
  public void setStatus(int status) {
    this.status = status;
  }
    /** Get the status of the present buffer
     *  @return Status of the buffer
     */
  public int getStatus() {
    return status;
  }
    /** Set the spy pointer value of the present buffer
     *  @param pointer spy pointer value of the present buffer
     */
  public void setPointer(int pointer) {
    this.pointer = pointer;
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
    /** Get the End Event error status bit
     *  @return The End Event error status bit
     */
  public int getEndEventError() {
    return (status >> 3) & 0x1;
  }
    /** Get the Simulation Error  error status bit
     *  @return The Simulation error status bit
     */
  public int getSimulationError() {
    return (status >> 4) & 0x1;
  }
    /** Set the Spy buffer data for the present buffer
     *  @param data Spy buffer data array
     */
  public void setData(final Vector<Integer> list) {
    int [] data = new int[list.size()];
    int ncol = 0, j = 0;
    for ( Iterator<Integer> it = list.iterator(); it.hasNext(); ) {
      data[j] = it.next().intValue();
      if (false) {
        System.out.println(AppConstants.h6Format.sprintf(data[j]) + " ");
        if (ncol++ == 10) {
          System.out.println("\n");
          ncol = 0;
        }
      }
      j++; // in order to satisfy the debug printout
    }
    setData(data);
  }
  public void setData(final int [] data) {
    if (this.data.length != data.length) 
       this.data = new int[data.length];
    System.arraycopy(data, 0, this.data, 0, data.length);
    
    int [] eewords = getEEWords();
       nEvents  = eewords.length;
    nTotEvents += nEvents;

    for (int i = 0; i < AppConstants.gerrors.length; i++) {
      errorCounters[i] = 0;
      for (int j = 0; j < nEvents; j++) {
        int err = (eewords[j] >> 9) & 0xff;  // pick up the EE Error bits
        if ((err >> i & 0x1) > 0) errorCounters[i]++;
      }
      totalErrorCounters[i] += errorCounters[i];
    }
    for (int i = 0; i < AppConstants.gerrors.length; i++) {
      if (errorCounters[i] > 0) {
        status |= (1 << 3);
        break;
      }
    }
  }
    /** Get the Spy buffer data for the present buffer
     *  @return Spy buffer data array
     */
  public int [] getData() {
    return data;
  }
    /**
     *  @return the event tags in an array
     */
  public int [] getEventTags() {
    return getWords(data, AppConstants.DAWORD);
  }
    /**
     *  @return the end event words in an array
     */
  public int [] getEEWords() {
    return getWords(data, AppConstants.EEWORD);
  }
  public static int [] getWords(int [] data, int option) {
    if (data.length <= 0) return new int[0];

    int mask = AppConstants.MASK08;
    if (option == AppConstants.EEWORD) mask = AppConstants.MASK24;
 
    int nElem = 0;
    for (int i = 0; i < data.length; i++)
      if ((data[i] & 0x600000) == 0x600000) nElem++;

    int [] words = new int[nElem];
    nElem = 0;
    for (int i = 0; i < words.length; i++) {
      if ((data[i] & 0x600000) == 0x600000) 
	  words[nElem++] = data[i] & mask;
    }
    return words;
  }
  public Vector<SvtEvent> getEvents() {
    return events;
  }
  public SvtEvent getEvent(int i) {
    return events.elementAt(i);
  }
  public void buildEvents(final int [] data) {
    int [] words;
    int nfirst = 0, nlast = 0;
    for (int i = 0; i < data.length; i++) {
      if ((data[i] & 0x600000) == 0x600000) {
        nlast = i;
        if (nlast-nfirst > 0) {
          words = new int[nlast-nfirst];
          for (int j = nfirst; j < nlast; j++) {
            words[j-nfirst] = data[j] & AppConstants.MASK23;
          }
        } 
        else {
          words = new int[0];
        }
        nfirst = nlast + 1;
        events.addElement(new EventFrac(words, data[i] & AppConstants.MASK23, type));
      }
    }
  }
  public int getSize() {
    return data.length;
  }
  public boolean isValid() {
    return (data.length > 0) ? true : false;
  }
  public int getNumberOfEvents() {
    return nEvents;
  }
}
