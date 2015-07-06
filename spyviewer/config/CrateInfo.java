package config;

import java.io.*;
import java.util.*;

import config.svt.*;

/** Class <CODE>CrateInfo</CODE> which the tree uses to
 *  display information about the Crate
 */
public class CrateInfo {
  /** Reference to a crate object */
  private SvtCrateData crateData;
  /** @param crateData A CrateData object */
  public CrateInfo(SvtCrateData crateData) {
    this.crateData = crateData;
  }
  /** Override <CODE>toString()</CODE> to return the Crate Name 
   *  @return Crate name
   */
  public String toString() {
    return crateData.getName();
  }
  /** Get a reference to a histogram
   *  @return Reference to a histogram
   */
  protected SvtCrateData getCrateData() {
    return crateData;
  }
}
