package config.hist;

public interface HistogramDrawingManager {
  public void drawHist(final Histogram histo);
  public void redrawHistograms();
  public void setLabel(final String text);
}