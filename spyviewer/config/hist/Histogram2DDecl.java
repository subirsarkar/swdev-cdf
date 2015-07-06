package config.hist;

/** Interface <CODE>Histogram2DDecl</CODE> defines a set of methods
 *  applicable to 2-dimensional histograms. 
 *  @author S. Sarkar
 *  @version 0.1, 7/29/2002
 */

public interface Histogram2DDecl extends Histogram {
  public void setCellContent(int bins, int biny, double value);
  public void setCellError(int bins, int biny, double value);
  public double getCellContent(int bins, int biny);
  public double getCellError(int bins, int biny);
  public int getBin(int binx, int biny);
}
