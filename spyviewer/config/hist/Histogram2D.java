package config.hist;

import config.util.AppConstants;

/**
 *  The bare bone 2D histogram class. Implements the
 *  <CODE>Histogram</CODE> interface.
 *
 *  @author S. Sarkar
 *  @version 0.1, 9/24/2001
 */
public class Histogram2D implements Histogram2DDecl {
  /** Histogram ID */
  private int hid;
  private int [] nbin    = new int[2];
  private double [] low  = new double[2];
  private double [] high = new double[2];
  private int entries;
  /** Histogram cell content array */
  private double [] cellContent;
  /** Histogram cell error content array */
  private double [] cellError;
  /** Histogram title */
  private String gTitle;
  private String xTitle = null;
  private String yTitle = null;

  private String folder   = AppConstants.DefaultFolder;
  private int prop        = 0;
  private boolean logFlag = false;

  /** Create a 2D histogram object
   *  @param  hid    Histogram ID
   *  @param  gTitle  Histogram title
   *  @param  nxbin  Number of bins along x
   *  @param  xlow   Lower edge in x
   *  @param  xhigh  upper edge in x
   *  @param  nybin  Number of bins along y
   *  @param  ylow   lower edge in y
   *  @param  yhigh  upper edge in y
   */
  public Histogram2D(int hid, String gTitle, 
                     int nxbin, double xlow, double xhigh,
                     int nybin, double ylow, double yhigh) 
  {
    this.hid     = hid;
    this.gTitle  = gTitle;

    this.nbin[0] = nxbin;
    this.low[0]  = xlow;
    this.high[0] = xhigh;

    this.nbin[1] = nybin;
    this.low[1]  = ylow;
    this.high[1] = yhigh;

    int ncell    = (nbin[0]+2)*(nbin[1]+2);
    cellContent  = new double[ncell];
    cellError    = new double[0];
  }
  public int getBin(int binx) {
     return getBin(binx, 1);
  }
  public int getBin(int binx, int biny) {
    int nx  = nbin[0] + 2;
    if (binx < 0)   binx = 0;
    if (binx >= nx) binx = nx - 1;

    int ny  = nbin[1] + 2;
    if (biny < 0)   biny = 0;
    if (biny >= ny) biny = ny - 1;

    return  binx + nx * biny;
  }
  /** Set cell content
   *  @param binx    x bin number
   *  @param biny    y bin number
   *  @param value   content of that cell
   */
  public void setCellContent(int binx, int biny, double value) {
    setCellContent(getBin(binx, biny), value);
  }
  /** Set cell content
   *  @param cell    cell number
   *  @param value   content of that cell
   */
  public void setCellContent(int cell, double value) {
    try {
      cellContent[cell] = value; 
    } catch (ArrayIndexOutOfBoundsException ex) {
      ex.printStackTrace();
    }
  }
  /** Set cell error content
   *  @param binx    x bin number
   *  @param biny    y bin number
   *  @param value   Error content of that cell
   */
  public void setCellError(int binx, int biny, double value) {
    if (binx < 0 || binx > nbin[0]+1) return;
    if (biny < 0 || biny > nbin[1]+1) return;
    setCellError(biny*(nbin[0]+2) + binx, value);
  }
  /** Set cell error content
   *  @param cell    cell number
   *  @param value   Error content of that cell
   */
  public void setCellError(int cell, double value) {
    try {
      cellError[cell] = value;
    } catch (ArrayIndexOutOfBoundsException ex) {
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
  /** Get # of cells in the histogram
   *  @return # of cells in the histogram ID
   */
  public int getNCell() {
    return (nbin[0]+2)*(nbin[1]+2);
  }    
  public int getXBins() {
    return nbin[0];
  }    
  public int getYBins() {
    return nbin[1];
  }    
  /** Get Histogram lower edge along x
   *  @return Histogram lower edge along x
   */
  public double getXLow() {
    return low[0];
  }    
  /** Get Histogram upper edge along x
   *  @return Histogram upper edge along x
   */
  public double getXHigh() {
     return high[0];
  }    
  /** Get Histogram lower edge along y
   *  @return Histogram lower edge along y
   */
  public double getYLow() {
    return low[1];
  }    
  /** Get Histogram upper edge along y
   *  @return Histogram upper edge along y
   */
  public double getYHigh() {
     return high[1];
  }    
  /** Get # of entries in the Histogram 
   *  @return # of entries in the Histogram 
   */
  public int getEntries() {
    return entries;
  }    
  /** Get content of the cell defined by <CODE>binx</CODE> and <CODE>biny</CODE>
   *  @param binx  Bin number along x
   *  @param biny  Bin number along y
   *  @return Content of the cell defined by <CODE>binx</CODE> and <CODE>biny</CODE>
   */
  public double getCellContent(int binx, int biny) {
    if (binx < 0 || binx > nbin[0]+1) return -1.0;
    if (biny < 0 || biny > nbin[1]+1) return -1.0;
    return getCellContent(biny*(nbin[0]+2) + binx);
  }
  /** Get content of the cell
   *  @param cell cell number under consideration
   *  @return content of the cell
   */
  public double getCellContent(int cell) {
    return cellContent[cell];
  }    
  /** Get contents of the histogram in an array
   *  @return Contents of the histogram in an array
   */
  public double [] getCellContents() {
    return cellContent;
  }    
  /** Get error content of the cell defined by <CODE>binx</CODE> and <CODE>biny</CODE>
   *  @param binx  Bin number along x
   *  @param biny  Bin number along y
   *  @return Error content of the cell defined by <CODE>binx</CODE> and <CODE>biny</CODE>
   */
  public double getCellError(int binx, int biny) {
    if (binx < 0 || binx > nbin[0]+1) return -1.0;
    if (biny < 0 || biny > nbin[1]+1) return -1.0;
    return getCellError(biny*(nbin[0]+2) + binx);
  }
  /** Get error content of the cell
   *  @param cell cell number under consideration
   *  @return Error content of the cell
   */
  public double getCellError(int cell) {
    return cellError[cell];
  }    
  /** Get error contents of the histogram in an array
   *  @return Error contents of the histogram in an array
   */
  public double [] getCellErrors() {
    return cellError;
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
  public void setXTitle(final String xTitle) {
    this.xTitle = xTitle;
  }
  public String getXTitle() {
    return xTitle;
  }
  public void setYTitle(final String yTitle) {
    this.yTitle = yTitle;
  }
  public String getYTitle() {
    return yTitle;
  }
  /** Override <CODE>toString()</CODE> and return a dump of the histogram 
   *  @return A dump of the histogram
   */
  public String toString() {
    StringBuilder buf = new StringBuilder(AppConstants.SMALL_BUFFER_SIZE);

    buf.insert(0, "Atributes:");
    buf.append("\n     Folder = ").append(folder);
    buf.append("\n         ID = ").append(hid);
    buf.append("\n      Title = ").append(gTitle);
    buf.append("\n  # of bins = ").append(nbin[0]).append(", ").append(nbin[1]);
    buf.append("\nLower edges = ").append(AppConstants.f72Format.sprintf(low[0]));
    buf.append(", ").append(AppConstants.f72Format.sprintf(low[1]));
    buf.append("\nUpper edges = ").append(AppConstants.f72Format.sprintf(high[0]));
    buf.append(", ").append(AppConstants.f72Format.sprintf(high[1]));
    buf.append("\n    Entries = ").append(entries);
    buf.append("\nUnderflow and overflow =  \n");
    buf.append("1. ").append(AppConstants.f90Format.sprintf(getCellContent(0, nbin[1]+1))).append("\n");
    buf.append("2. ").append(AppConstants.f90Format.sprintf(getCellContent(nbin[0], nbin[1]+1))).append("\n");
    buf.append("3. ").append(AppConstants.f90Format.sprintf(getCellContent(nbin[0]+1, nbin[1]+1))).append("\n");
    buf.append("4. ").append(AppConstants.f90Format.sprintf(getCellContent(nbin[0]+1, nbin[1]))).append("\n");
    buf.append("5. ").append(AppConstants.f90Format.sprintf(getCellContent(nbin[0]+1, 0))).append("\n");
    buf.append("6. ").append(AppConstants.f90Format.sprintf(getCellContent(nbin[0], 0))).append("\n");
    buf.append("7. ").append(AppConstants.f90Format.sprintf(getCellContent(0, 0))).append("\n");
    buf.append("8. ").append(AppConstants.f90Format.sprintf(getCellContent(0, nbin[1]))).append("\n");
    buf.append("\nCell Contents: \n");
    int ncol = 0;
    for (int i = 0; i < getNCell(); i++) {
      buf.append(AppConstants.f90Format.sprintf(cellContent[i])).append(" ");
      if (ncol++ == 10) {
        ncol = 0;
        buf.append("\n");   
      }
    }

    // Error content is no important at this moment
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
    Histogram h = new Histogram2D(100, "Test Histogram", 10, 0., 10., 10, 0., 10.);
    System.out.println(h);
  }
}
