package config.svt;

import hep.analysis.Histogram;

public interface SvtHistogrammer {
  public void createHistograms();
  public void fillHistograms(boolean singleShot);
  public Histogram getHistogram(int index);
  public String [] getHistogramTitles();
  public void fillHistogram(Histogram dHist, final String xvar, 
                                             final String yvar, 
                                             final String cutvar); 
  public String getMatchStr();
  public int getSize();
}
