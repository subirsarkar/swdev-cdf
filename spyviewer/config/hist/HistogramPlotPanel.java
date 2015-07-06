package config.hist;

import java.awt.Color;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.BorderFactory;

import jas.hist.JASHist;
import jas.hist.DataSource;
import jas.hist.JASHist1DHistogramStyle;
import jas.hist.JASHist2DHistogramStyle;
import jas.hist.JASHistData;
import jas.hist.test.Gauss;

public class HistogramPlotPanel extends JPanel {
  Histogram hist;
  private static final Color color = new Color(70, 130, 180);
  public HistogramPlotPanel(Histogram hist) {
    this.hist = hist;
    setLayout(new BorderLayout());
    setBackground(Color.white);
    setBorder(BorderFactory.createLoweredBevelBorder());
  }
  public static JASHist createPlot() {
    JASHist plot = new JASHist();
    plot.setDataAreaColor(Color.white);
    plot.setDataAreaBorderType(plot.NONE);
    plot.setAllowUserInteraction(true);
    plot.setShowLegend(0);

    return plot;
  }
  public void drawHist()  {
    removeAll();
    JASHist plot = createPlot();
    add(plot, BorderLayout.CENTER);

    DataSource dataSource = null;
    if (hist instanceof Histogram1D) {
      dataSource = new Histogram1DImpl(hist);
      plot.getYAxis().setLabel("Number of Events");
    }
    else {
      dataSource = new Histogram2DImpl(hist);
      plot.getYAxis().setLabel(hist.getYTitle());
    }
    JASHistData jasData = plot.addData(dataSource);
    if (hist instanceof Histogram1D) {
      JASHist1DHistogramStyle style1D = (JASHist1DHistogramStyle) jasData.getStyle();
      style1D.setShowErrorBars(false);
      style1D.setHistogramBarColor(color);
      style1D.setDataPointColor(color);
    }
    else {
      JASHist2DHistogramStyle style2D = (JASHist2DHistogramStyle) jasData.getStyle();
      style2D.setShapeColor(color);
    }
    plot.getYAxis().setLogarithmic(hist.isLogarithmic());
    plot.setTitle(hist.getTitle());
    plot.setShowStatistics(true);
    plot.getXAxis().setLabel(hist.getXTitle());
                
    jasData.show(true);
    repaint();
  }
  public void drawGauss(String title, String xLabel, String yLabel)  {
    JASHist plot = createPlot();
    add(plot, BorderLayout.CENTER);
    
    JASHistData jasData = plot.addData(new Gauss());

    plot.setTitle(title);
    plot.getYAxis().setLabel(xLabel);
    plot.getXAxis().setLabel(yLabel);
                
    jasData.show(true);
  }
  void clearPanel() {
    removeAll();
    repaint();
  }
}
