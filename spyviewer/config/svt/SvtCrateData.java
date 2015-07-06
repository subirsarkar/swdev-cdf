package config.svt;

import java.util.Date;
import com.smartsockets.TipcMsg;
import com.smartsockets.TipcException;
import config.util.AppConstants;
import config.util.Timex;

/** 
 * <P>Data structure for individual crates each of which contains <CODE>n</CODE> Board 
 * data structures. This class should be subclassed by daughter classes to implement 
 * <I>Tracker</I>, <I>Fitter</I>, and <I>Fanout</I> specific informations, if necessary.
 * At present we do not see such requirement, though. Once subclassing is done, one can 
 * use <CODE>TrackerCrateData</CODE> where <CODE>CrateData</CODE> is intended. 
 * This works for other Crate specific subclasses as well.</P>
 *
 * <P> The class contains the following fields,
 * <UL>
 *   <LI>Name</LI>
 *   <LI>Iteration number</LI>
 *   <LI>Date and Time as a <CODE>Date</CODE> object</LI>
 *   <LI>Run Number</LI>
 *   <LI>Level 1 Accept</LI>
 *   <LI>Partition number within which CDF DAQ runs</LI>
 *   <LI>Run Control state</LI>
 *   <LI>Board data array</LI>
 * </UL>
 * 
 * @author S. Sarkar
 * @version 0.1, March 2001 
 */
public class SvtCrateData {
    /** Debug flag */
  public static final boolean DEBUG = false;
    /** Name of the Crate */
  private String name;
    /** Name of the associated dump file, if applicable */
  private String filename;
    /** Iteration number of the spyMon buffer read */
  private int monIter;
    /** time when the data was read in this iteration as a <CODE>Date</CODE> object*/
  private Date timeStamp;
    /** Current Run number during data taking */
  private int runNumber;
    /** Level 1  accept since last iteration */
  private int L1Accept;
    /** Partition number within which CDF DAQ runs */
  private int partition;
    /** Run Control State */
  private String RCState;
    /** Reference to <CODE>SvtBoardData</CODE> structure array which is contained 
     *  in each crate. Each board data structure in turn contains a number of Spy 
     *  Buffer data structures in addition to other informations.
     *  @see SvtBoardData
     */
  private SvtBoardData [] boardData;
    /** Initialise the crate data object to be created. Instantiation is done
     *  using the name of the crate only and later a call to <CODE>fillData(...)</CODE>
     *  fill all the other fields. 
     *  @param  name  Name of the Crate
     */
  public SvtCrateData(final String name) {
    this(name, name + "-" + (new Timex()).toString());
  }
  public SvtCrateData(final String name, final String filename) {
    this.name     = name;
    this.filename = filename;
    boardData = new SvtBoardData[0];
  }
    /** Fill the member variables with appropriate data
     * @param  monIter  Iteration number of the SPYMON buffer read
     * @param  timeStamp    <CODE>Date</CODE> object when the data was read in this iteration
     * @param  runNumber    Current Run number during data taking
     * @param  L1Accept     Level 1  accept since last iteration
     * @param  partition    Run_Control partition number
     * @param  RCState      State of the RC/DAQ
     * @param  boardData    The board data structure array which contain relevant informations
     */
  public void fillData(int monIter, 
                       final Date timeStamp, 
                       int runNumber, 
                       int L1Accept, 
                       int partition,
                       final String RCState,
                       final SvtBoardData [] boardData)   
  {
    fillData(monIter, timeStamp, runNumber, L1Accept, partition, RCState);
    fillData(boardData);
  }
    /** Fill the member variables with appropriate data
     * @param  monIter Iteration number of the SPYMON buffer read
     * @param  timeStamp   User formatted time when the data was read in this iteration
     * @param  runNumber   Current Run number during data taking
     * @param  L1Accept    Level 1  accept since last iteration
     * @param  partition   Run_Control partition number
     * @param  RCState     State of the RC/DAQ
     */
  public void fillData(int monIter, 
                       final Date timeStamp, 
                       int L1Accept, 
                       int runNumber,
                       int partition,
                       final String RCState)
  {
    this.monIter   = monIter;
    this.timeStamp = timeStamp;
    this.runNumber = runNumber;
    this.L1Accept  = L1Accept;
    this.partition = partition;
    this.RCState   = RCState;
  }
    /** Fill member variables with appripriate data
     *  @param boardData  The <CODE>SvtBoardData</CODE> array
     */
  public void fillData(final SvtBoardData [] boardData) {
    setBoardData(boardData);
  }
    /** String representeation of the object is returned. The string packs 
     *  all the data present so that the state of the object can be 
     *  displayed in a convenient way.
     *  @return The String representeation of the object
     */
  public String toString() {
    StringBuilder sb = new StringBuilder(AppConstants.LARGE_BUFFER_SIZE);

    sb.insert(0, "\n");
    sb.append(name).append(" Crate Status:");
    sb.append("\nIteration = ").append(monIter);
    sb.append("\nTimestamp = ").append(timeStamp);
    sb.append("    \nRun # = ").append(runNumber);
    sb.append("\nL1 Accept = ").append(L1Accept);
    sb.append("\npartition = ").append(partition);
    sb.append("  \nRCState = ").append(RCState);
    sb.append("   \nBoards = ").append(boardData.length);
    for (int i = 0; i < boardData.length; i++)
      if (boardData[i] != null) sb.append("\n").append(boardData[i]);

    return sb.toString();
  }
    /** Get the name of the SVT crate
     *  @return The name of the SVT crate
     */
  public String getName() {
    return name;
  }
  public String getFilename() {
    return filename;
  }
    /** Set the spyMon iteration number of the current run
     *  @param monIter spyMon iteration number of the current run
     */
  public void setIteration(int monIter) {
    this.monIter = monIter;
  }
    /** Get the spyMon iteration number of the current run
     *  @return spyMon iteration number of the current run
     */
  public int getIteration() {
    return monIter;
  }
    /** Set the Level 1 Accept rate since last iteration
     *  @param L1Accept  Level 1 Accept rate since last iteration
     */
  public void setL1Accept(int L1Accept) {
    this.L1Accept = L1Accept;
  }
    /** Get the Level 1 accept rate since last iteration
     *  @return The Level 1 accept rate since last iteration
     */
  public int getL1Accept() {
    return L1Accept;
  }
    /** Set the Partition number of the RC during the current iteration
     *  @param partition Partition number of the RC during the current iteration
     */
  public void setPartition(int partition) {
    this.partition = partition;
  }
    /** Get the Partition number of the RC during the current iteration
     *  @return The Partition number of the RC during the current iteration
     */
  public int getPartition() {
    return partition;
  }
    /** Set the Run number of DAQ during the current iteration
     *  @param runNumber Run number of DAQ during the current iteration
     */
  public void setRunNumber(int runNumber) {
    this.runNumber = runNumber;
  }
    /** Get the Run number of DAQ during the current iteration
     *  @return Run number of DAQ during the current iteration
     */
  public int getRunNumber() {
    return runNumber;
  }
    /** Set the time stamp in the current iteration
     *  @param timeStamp Time stamp in user format of the current iteration
     */
  public void setTimeStamp(Date timeStamp) {
    this.timeStamp = timeStamp;
  }
    /** Get the time stamp in the current iteration
     *  @return The time stamp if the current iteration
     */
  public Date getTimeStamp() {
    return timeStamp;
  }
    /** Set the RC State in the current iteration
     *  @param RCState The RC State in the current iteration
     */
  public void setRCState(String RCState) {
    this.RCState = RCState;
  }
    /** Get the RC State in the current iteration
     *  @return The RC State in the current iteration
     */
  public String getRCState() {
    return RCState;
  }
    /** Get the Number of SVT boards present in the crate
     *  @return The Number of SVT boards present in the crate
     */
  public int getNBoards() {
    return boardData.length;
  }
    /** Set <CODE>SvtBoardData</CODE> array
     *  @param  boardData  The <CODE>SvtBoardData</CODE> array
     */
  public void setBoardData(final SvtBoardData [] boardData) {
    if (this.boardData.length != boardData.length) 
       this.boardData = new SvtBoardData[boardData.length];
    System.arraycopy(boardData, 0, this.boardData, 0, boardData.length);
  }
    /** Get reference to <CODE>SvtBoardData</CODE> array 
     *  @return Reference to <CODE>SvtBoardData</CODE> array
     */
  public SvtBoardData [] getBoardData() {
    return boardData;
  }
    /** Set <CODE>SvtBoardData</CODE> element denoted by array index <I>index</I>
     *  @param index   Array index
     *  @param val     The new <CODE>SvtBoardData</CODE> element
     */
  public void setBoardData(int index, final SvtBoardData obj) {
    boardData[index] = obj;
  }
    /** Get <CODE>SvtBoardData</CODE> array element given by index
     *  @param index  Array index
     *  @return <CODE>SvtBoardData</CODE> array element given by <I>index</I>
     */
  public SvtBoardData getBoardData(int index) {
    return boardData[index];
  }
    /** Get <CODE>SvtBoardData</CODE> array element given by the name of the board
     *  @param index  Array index
     *  @return <CODE>SvtBoardData</CODE> array element given by <I>index</I>
     */
  public SvtBoardData getBoardData(final String board, int slot) {
    int index = -1;
    SvtBoardData data = null;

    for (int i = 0; i < boardData.length; i++) {
      if (boardData[i].getType().equals(board) && boardData[i].getSlot() == slot) {
        index = i;
      }
    }
    if (index >= 0) {
      try {      
        data = boardData[index];
      }
      catch (ArrayIndexOutOfBoundsException ex) {
        System.out.println("Array index overflow: " + ex.getMessage());
        ex.printStackTrace();
      }
    }
    if (DEBUG) 
      System.out.println("getBoardData() -> " + board + " " + slot + " " + index);
    return data;
  }
    /**
     * Update data for all the boards for the crate for which message have 
     * been published.
     * @param msg  Reference to the message object
     */
  public void updateBoardData(TipcMsg msg) throws TipcException {
    TipcMsg [] boardMsg = msg.nextMsgArray();
    if (boardData.length == 0) boardData = new SvtBoardData[boardMsg.length];
    for (int i = 0; i < boardData.length; i++) {
      boardMsg[i].setCurrent(0);
      String clazz = boardMsg[i].nextStr();    // Class name
      long time    = boardMsg[i].nextInt4();   // time

      String type = boardMsg[i].nextStr();
      int slot    = boardMsg[i].nextInt4();

      if (DEBUG) System.out.println("Type/slot " + type + "/" + slot);
      if (boardData[i] == null)  
        boardData[i] = new SvtBoardData (type, slot);
      int status    = boardMsg[i].nextInt4();
      int [] eRegisters = boardMsg[i].nextInt4Array();
      int [] eCounters  = boardMsg[i].nextInt4Array();
      boardData[i].fillData (status, eRegisters, eCounters);
      boardData[i].updateBufferData(boardMsg[i]);
    }
    fillData(boardData);
  }
    /** A subclass of <CODE>SvtCrateData</CODE> which extends the parent
     *  class with <B>Tracker Crate</B> specific data. 
     */
  public class TrackerCrateData extends SvtCrateData {
    public TrackerCrateData (final String name) {
      super(name);
    }
  }
    /** A subclass of <CODE>SvtCrateData</CODE> which extends the parent
     *  class with <B>Fitter Crate</B> specific data. 
     */
  public class FitterCrateData extends SvtCrateData {
    public FitterCrateData (final String name) {
      super(name);
    }
  }
    /** A subclass of <CODE>SvtCrateData</CODE> which extends the parent
     *  class with <B>Fanout Crate</B> specific data. 
     */
  public class FanoutCrateData extends SvtCrateData {
    public FanoutCrateData (final String name) {
      super(name);
    }
  }
}
