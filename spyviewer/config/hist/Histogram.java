package config.hist;

/** Interface <CODE>Histogram</CODE> defines a common set of methods
 *  applicable to both 1- and 2-dimensional histograms. <I>May not be the
 *  most appropriate one</I>
 *  @author S. Sarkar
 *  @version 0.1, 9/24/2001
 */

public interface Histogram {
  public int getBin(int binx);
  public void setCellContent(int cell, double value);
  public void setCellError(int cell, double value);
  public void setEntries(int entries);

  public int getHid();
  public String getTitle();
  public int getNCell();
  public int getEntries();
  public double getCellContent(int cell);
  public double [] getCellContents();
  public double getCellError(int cell);
  public double [] getCellErrors();

  public void setLogarithmic(boolean logFlag);
  public boolean isLogarithmic();

  public void setFolder(final String folder);
  public String getFolder();

  public void setXTitle(final String title);
  public String getXTitle();
  public void setYTitle(final String title);
  public String getYTitle();
    
  public void setProp(int prop);
  public int getProp();
}
