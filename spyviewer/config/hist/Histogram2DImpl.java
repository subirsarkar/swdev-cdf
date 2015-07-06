package config.hist;

import jas.hist.Rebinnable2DHistogramData; 
import jas.hist.HasStyle; 
import jas.hist.HasStatistics;  
import jas.hist.JASHist2DHistogramStyle;
import jas.hist.Statistics;
import jas.hist.JASHistStyle;

public class Histogram2DImpl implements 
                       Rebinnable2DHistogramData, 
                       HasStyle, 
                       HasStatistics  
{
  private Histogram2D hist;
  private JASHist2DHistogramStyle style;
  private Statistics stats = new HistStatistics();
  private double mean = 0.0;    
  private double rms  = 0.0;
  private static final String[] statNames = {"Entries"};
  
  public Histogram2DImpl(final Histogram hist) {
    this.hist = (Histogram2D) hist;
  }
  public String toString() {
    return "DataSource info:\n" + hist;
  }
  public Histogram getHistogram() {
    return hist;
  }
  public void setStyle(JASHist2DHistogramStyle style) {
    this.style = style;
  }
  public JASHistStyle getStyle() {
    return this.style;
  }
  public String getTitle() {
    return hist.getTitle();
  }
  public double getXMin() {
    return hist.getXLow();
  }
  public double getXMax() {
    return hist.getXHigh();
  }
  public double getYMin() {
    return hist.getYLow();
  }
  public double getYMax() {
    return hist.getYHigh();
  }
  public boolean isRebinnable()  {
    return false;
  }
  public String[] getXAxisLabels() {
    return null;
  }
  public String[] getYAxisLabels() {
    return null;
  }
  public int getXBins() {
    return hist.getXBins();
  }
  public int getYBins() {
    return hist.getYBins();
  }
  public int getXAxisType()  {
    return Rebinnable2DHistogramData.DOUBLE;
  }
  public int getYAxisType()  {
    return Rebinnable2DHistogramData.DOUBLE;
  }
  public double[][][] rebin(int Xbins, double Xmin, double Xmax,  
                            int Ybins, double Ymin, double Ymax, 
                            boolean wantErrors, boolean hurry, boolean overflow) 
  {
    int xbins = getXBins();
    int ybins = getYBins();
    double [][] cont = new double[xbins][ybins];
    for (int j = 0; j < ybins; j++) 
      for (int i = 0; i < xbins; i++) 
        cont[i][j] = hist.getCellContent(i+1, j+1);

    double [][][] result = { cont };
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
  private class HistStatistics implements Statistics  {    
    public String [] getStatisticNames()  {
      return statNames;
    }
    public double getStatistic(String name)   {
      if (name == statNames[0]) return hist.getEntries();
      if (name == statNames[1]) return mean;
      if (name == statNames[2]) return rms;
      //if (name == statNames[3]) return hist.getUnderflow();
      //if (name == statNames[4]) return hist.getOverflow();
      return 0.;
    }
  }
}
