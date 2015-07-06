package config.hist;

import config.util.AppConstants;

/**
 *  The bare bone 1D histogram class. Implements the
 *  <CODE>Histogram</CODE> interface.
 *
 *  @author S. Sarkar
 *  @version 0.1, 9/24/2001
 */
public class Histogram1D implements Histogram {
  /** Histogam ID, available in this case */
  private int hid;
  /** Number of bins */
  private int ncell;
  /** Lower edge of the histogram */
  private double low;
  /** Upper edge of the histogram */
  private double high;
  /** Number of entries of the histogram */
  private int entries;
  /** Histogram content aray */
  private double [] cellContent;
  /** Error content array */
  private double [] cellError;
  /** Histogram title */
  private String gTitle;
  private String xTitle = null;
  
  private String folder   = AppConstants.DefaultFolder;
  private int prop        = 0;
  private boolean logFlag = false;
  /** Create a 1D histogram object
   *  @param  hid    Histogram ID
   *  @param  gTitle  Histogram title 
   *  @param  ncell  Number of bins of the histogram
   *  @param  low    Lower edge
   *  @param  high   Upper edge
   */
  public Histogram1D(int hid, String gTitle, int ncell, double low, double high) {
    this.hid    = hid;
    this.gTitle = gTitle;
    this.ncell  = ncell;
    this.low    = low;
    this.high   = high;

    cellContent = new double[ncell+2];    // Number of bins plus under/over flow 
    cellError   = new double[ncell+2]; 
  }
  public int getBin(int binx) {
    int nx  = ncell + 2;
    if (binx < 0)   binx = 0;
    if (binx >= nx) binx = nx - 1;

    return  binx;
  }
  /** Set histogram bin content 
   *  @param cell  Bin number
   *  @param value Bin content
   */
  public void setCellContent(int cell, double value) {
    try {
      cellContent[cell] = value; 
    } 
    catch (ArrayIndexOutOfBoundsException ex) {
      ex.printStackTrace();
    }
  }
  /** Set histogram error  content 
   *  @param cell  Bin number
   *  @param value Error content
   */
  public void setCellError(int cell, double value) {
    try {
      cellError[cell] = value;
    } 
    catch (ArrayIndexOutOfBoundsException ex) {
      ex.printStackTrace();
    }
  }
  /** Set histogram entries
   *  @param entries  Number of entries in the histogram
   */
  public void setEntries(int entries) {
    this.entries = entries;
  }    
  /** Get Histogram ID
   *  @return Histogram ID
   */
  public int getHid() {
    return hid;
  }    
  /** Get Histogram title
   *  @return Histogram title
   */
  public String getTitle() {
    return gTitle;
  }    
  /** Get # of bins in the histogram
   *  @return # of bins in the histogram ID
   */
  public int getNCell() {
    return ncell;
  }    
  /** Get Histogram lower edge
   *  @return Histogram lower edge
   */
  public double getLow() {
    return low;
  }    
  /** Get Histogram upper edge
   *  @return Histogram upper edge
   */
  public double getHigh() {
     return high;
  }    
  /** Get # of entries in the Histogram 
   *  @return # of entries in the Histogram 
   */
  public int getEntries() {
    return entries;
  }    
  /** Get content of the cell
   *  @param cell cell number under consideration
   *  @return content of the cell
   */
  public double getCellContent(int cell) {
    double value = -1.0;
    try {
      value = cellContent[cell];
    } 
    catch (ArrayIndexOutOfBoundsException ex) {
      ex.printStackTrace();
    }
    return value;
  }    
  /** Get contents of the histogram in an array
   *  @return Contents of the histogram in an array
   */
  public double [] getCellContents() {
    return cellContent;
  }    
  /** Get error content of the cell
   *  @param cell cell number under consideration
   *  @return Error content of the cell
   */
  public double getCellError(int cell) {
    double value = -1.0;
    try {
      value = cellError[cell];
    } 
    catch (ArrayIndexOutOfBoundsException ex) {
      ex.printStackTrace();
    }
    return value;
  }    
  /** Get error contents of the histogram in an array
   *  @returnError  contents of the histogram in an array
   */
  public double [] getCellErrors() {
    return cellError;
  }    
  /** Get underflow content
   *  @return Underflow content
   */
  public double getUnderflow() {
    return cellContent[0];
  }    
  /** Get Overflow content
   *  @return Overflow content
   */
  public double getOverflow() {
    return cellContent[ncell+1];
  }    
  public void setXTitle(final String xTitle) {
    this.xTitle = xTitle;
  }
  public String getXTitle() {
    return xTitle;
  }
  public void setYTitle(final String title) {
    setXTitle(title);
  }
  public String getYTitle() {
    return getXTitle();
  }
  public void setFolder(final String folder) {
    this.folder = folder;
  }
  public String getFolder() {
    return folder;
  }
  public void setProp(int prop) {
    this.prop = prop;
  }
  public int getProp() {
    return prop;
  }
  /** Override <CODE>toString()</CODE> and return a dump of the histogram 
   *  @return A dump of the histogram
   */
  public String toString() {
    StringBuilder buf = new StringBuilder(AppConstants.SMALL_BUFFER_SIZE);
    buf.insert(0, "Atributes:");
    buf.append("\n    Folder = ").append(folder);
    buf.append("\n        ID = ").append(hid);
    buf.append("\n     Title = ").append(gTitle);
    buf.append("\n # of bins = ").append(ncell);
    buf.append("\nLower edge = ").append(low);
    buf.append("\nUpper edge = ").append(high);
    buf.append("\n   Entries = ").append(entries);
    buf.append("\n Underflow = ").append(cellContent[0]);
    buf.append("\n  Overflow = ").append(cellContent[ncell+1]);
    buf.append("\nBin Content:\n");

    // Bin Content
    int ncol = 0;
    for (int i = 1; i < ncell; i++) {
      buf.append(AppConstants.f90Format.sprintf(cellContent[i])).append(" ");
      if (ncol++ == 10) {
        buf.append("\n");
        ncol = 0;
      }
    }
    if (false) {
      // Error content
      ncol = 0;
      buf.append("\nError Content:\n");
      for (int i = 1; i < ncell; i++) {
        buf.append(AppConstants.f72Format.sprintf(cellError[i])).append(" ");
        if (ncol++ == 10) {
          buf.append("\n");
          ncol = 0;
        }
      }
    }
    return buf.toString();
  }
  public void setLogarithmic(boolean logFlag) {
    this.logFlag = logFlag;
  }
  public boolean isLogarithmic() {
    return logFlag;
  }
  /** Test the Histogram class */
  public static void main(String [] argv) {
    Histogram h = new Histogram1D(100, "Test Histogram", 100, 0., 100.);
    System.out.println(h);
  }
}
