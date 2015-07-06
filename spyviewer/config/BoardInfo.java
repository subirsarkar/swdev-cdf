package config;

import java.io.*;
import java.util.*;

import config.svt.*;

/** Class <CODE>BoardInfo</CODE> which the tree uses to
 *  display information about the Board
 */
public class BoardInfo {
  /** Reference to a board object */
  private String crateName;
  private SvtBoardData boardData;
  /** 
   * @param crateName Crate name
   * @param boardData A BoardData object
   */
  public BoardInfo(final String crateName, final SvtBoardData boardData) {
    this.crateName = crateName;
    this.boardData = boardData;
  }
  /** Override <CODE>toString()</CODE> to return qualified Board Name 
   *  @return Qualified Board name
   */
  public String toString() {
    return boardData.getType()+"-"+boardData.getSlot();
  }
  /** @return Reference to <CODE>SvtBoardData</CODE> */
  public SvtBoardData getBoardData() {
    return boardData;
  }
  /** @return Crate name */
  public String getCrateName() {
    return crateName;
  }
  /** @return Board name */
  public String getBoardName() {
    return boardData.getType();
  }
  /** @return Board Slot number */
  public int getBoardSlot() {
    return boardData.getSlot();
  }
  /** Set Board data reference 
   *  @param boardData Reference to <CODE>SvtBoardData</CODE>
   */
  public void setBoardData(final SvtBoardData boardData) {
    this.boardData = boardData;
  }
}
