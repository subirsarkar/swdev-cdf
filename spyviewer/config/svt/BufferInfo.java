package config.svt;

/** Class <CODE>BufferInfo</CODE> which the tree uses to
 *  display information about the Buffer
 */
public class BufferInfo {
  /** Reference to a SvtBuffer object */
  private String crate;
  private String board;
  private int slot;
  private SvtBufferData bufferData;
  /** 
   * @param crate Crate name
   * @param board Board name
   * @param slot  Board slot
   * @param bufferData SvtBufferData object
   */
  public BufferInfo(final String crate, 
                    final String board, 
                    int slot, 
                    final SvtBufferData bufferData) {
    this.crate = crate;
    this.board = board;
    this.slot  = slot;
    this.bufferData = bufferData;
  }
  /** Override <CODE>toString()</CODE> to return Buffer Name 
   *  @return Buffer name
   */
  public String toString() {
    return bufferData.getType();
  }
  /** @return Reference to <CODE>SvtBoardData</CODE> */
  public SvtBufferData getBufferData() {
    return bufferData;
  }
  /** @return Crate name */
  public String getCrate() {
    return crate;
  }
  /** @return Board name */
  public String getBoard() {
    return board;
  }
  /** @return Board Slot number */
  public int getSlot() {
    return slot;
  }
  /** Set Buffer data reference 
   *  @param bufferData Reference to <CODE>SvtBoardData</CODE>
   */
  public void setBufferData(final SvtBufferData bufferData) {
    this.bufferData = bufferData;
  }
  public boolean hasData() {
    return bufferData.isValid();
  }
}
