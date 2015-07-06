package config.hist;

import jas.hist.Rebinnable1DHistogramData;
import jas.hist.HasStyle;
import jas.hist.HasStatistics;
import jas.hist.JASHist1DHistogramStyle;
import jas.hist.Statistics;
import jas.hist.JASHistStyle;

public class Histogram1DImpl implements 
             Rebinnable1DHistogramData, HasStyle, HasStatistics  {
  private Histogram1D hist;
  private JASHist1DHistogramStyle style;
  private Statistics stats = new HistStatistics();
  private double mean;    
  private double rms;
  private static final String[] statNames = {
       "Entries","Mean", "RMS", "Underflow", "Overflow"
    };

  public Histogram1DImpl (final Histogram hist) {
    this.hist = (Histogram1D) hist;
    calculateMean();
  }
  public String toString() {
    return "DataSource info:\n" + hist;
  }
  protected Histogram getHistogram() {
    return hist;
  }
  public void setStyle(JASHist1DHistogramStyle style) {
    this.style = style;
  }
  public JASHistStyle getStyle() {
    return this.style;
  }
  public String getTitle() {
    return hist.getTitle();
  }
  public double getMin() {
    return hist.getLow();
  }
  public double getMax() {
    return hist.getHigh();
  }
  public boolean isRebinnable()  {
    return false;
  }
  public String[] getAxisLabels() {
    return null;
  }
  public int getBins() {
    return hist.getNCell();
  }
  public int getAxisType()  {
    return Rebinnable1DHistogramData.DOUBLE;
  }
  public double[][] rebin(int rBins, double rMin, double rMax,
			     boolean wantErrors, boolean hurry)  {
    double [] cont = new double[hist.getNCell()];
    for (int i = 0; i < cont.length; i++) {
      cont[i] = hist.getCellContent(i + 1);
    }
    double[][] result =  { cont };
    return result;
  }        
  public int getSize() {
    return hist.getEntries();
  }
  public double getData(int i) {
    return hist.getCellContent(i);
  }
  public Statistics getStatistics() {
    return stats;
  }
  public double getMean() {
    return mean;
  }
  public double getRMS() {
    return rms;
  }
  public void calculateMean() {
    int ix, nbin;
    double [] cont;
    double [] error;
    double xlo, 
           xhi, 
           delx, 
           xval, 
           xave = 0.0, 
           cnor = 0.0,
           xdev = 0.0, 
           xerr = 0.0;

    // Unpack histogram 
    xlo  = hist.getLow();
    xhi  = hist.getHigh(); 
    nbin = hist.getNCell()+2;
    delx = (xhi - xlo) / nbin;
    cont  = hist.getCellContents();
    error = hist.getCellErrors();

    // Compute average 
    for (ix = 0; ix < nbin; ix++) {
      xval = xlo + (ix + 0.5) * delx;
      cnor = cnor + cont[ix];
      xave = xave + xval * cont[ix];
    }
    mean = xave / Math.max(1.0e-12, cnor);

    // Now compute standard deviation 
    for (ix = 0; ix < nbin; ix++) {
      xval = xlo  + (ix + 0.5) * delx;
      xdev = xdev +  cont[ix] * Math.pow((xval - mean), 2);
    }
    xdev = Math.sqrt(Math.max(xdev/Math.max(1.0e-12, cnor), 0.0));
    rms  = xdev;
    // rms = xdev / Math.max(1.0e-12, Math.sqrt(Math.max(cnor, 1.0)));
  }
  private class HistStatistics implements Statistics  {    
    public String [] getStatisticNames()  {
      return statNames;
    }
    public double getStatistic(String name)   {
      if (name == statNames[0]) return hist.getEntries();
      if (name == statNames[1]) return getMean();
      if (name == statNames[2]) return getRMS();
      if (name == statNames[3]) return hist.getUnderflow();
      if (name == statNames[4]) return hist.getOverflow();
      return 0.;
    }
  }
}
