package config.hist;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JPanel;

import jas.hist.JASHist;
import jas.hist.JASHistData;
import jas.hist.JASHist1DHistogramStyle;
import jas.hist.JASHistAxis;

public class XYPlotPanel extends JPanel {
  private static final Color color = new Color(70, 130, 180);
  GraphDataSource dataSource = new GraphDataSource();
  JASHist plot = HistogramPlotPanel.createPlot();
  private boolean isRangeAuto = true;
  private boolean plotVisible = false;
  private double xlow = -2.0, xhig = 8.0, ylow = -2200, yhig = -1500;
  private String xLabel = new String();
  private String yLabel = new String();

  public XYPlotPanel() {
    this(new Dimension(400, 400), true);
  }
  public XYPlotPanel(Dimension d) {
    this(d, true);
  }
  public XYPlotPanel(Dimension d, boolean isRangeAuto) {
    super(true);
    setLayout(new BorderLayout());
    setBackground(Color.white);
    setMinimumSize(d);
    this.isRangeAuto = isRangeAuto;
  }
  public void drawPlot() {
    if (!plotVisible) {
      add(plot, BorderLayout.CENTER);
      plotVisible = true;
    }
    plot.removeAllData();

    JASHistData jasData = plot.addData(dataSource);

    String title = dataSource.getTitle();
    if (!title.equals("")) plot.setTitle(title);

    JASHistAxis Xaxis = plot.getXAxis();
    JASHistAxis Yaxis = plot.getYAxis();
    Xaxis.setLabel(xLabel);
    Yaxis.setLabel(yLabel);
    Xaxis.setRangeAutomatic(isRangeAuto);
//    Yaxis.setRangeAutomatic(isRangeAuto);
    if (false) System.out.println(xlow + ", " + xhig + ", " + ylow + ", " + yhig);
    if (!isRangeAuto) {
      Xaxis.setRange(xlow, xhig);
//      Yaxis.setRange(ylow, yhig);
    }

    JASHist1DHistogramStyle style1D = (JASHist1DHistogramStyle) jasData.getStyle();
    style1D.setShowErrorBars(false); // seems not to be working
    style1D.setShowDataPoints(true);
    style1D.setShowHistogramBars(false);
    style1D.setDataPointColor(color);
    style1D.setLineColor(Color.magenta);
    style1D.setShowLinesBetweenPoints(true);                
    style1D.setDataPointStyle(JASHist1DHistogramStyle.SYMBOL_TRIANGLE);
    style1D.setDataPointSize(8);

    jasData.show(true);

    repaint();
  }
  public void setData(double [] xdata, 
                      double [] ydata, 
                      double [] error, String name) 
  {
    dataSource.setData(xdata, ydata, error, name);
  }
  public void setXLabel(final String xLabel) {
    this.xLabel = xLabel;
  }
  public void setYLabel(final String yLabel) {
    this.yLabel = yLabel;
  }
  public void setLabels(final String xLabel, final String yLabel) {
    setXLabel(xLabel);
    setYLabel(yLabel);
  }
  public void setXRange(double xlow, double xhig) {
    this.xlow = xlow;
    this.xhig = xhig;
  }
  public void setYRange(double ylow, double yhig) {
    this.ylow = ylow;
    this.yhig = yhig;
  }
  public void setRange(double xlow, double xhig, double ylow, double yhig) {
    setXRange(xlow, xhig);
    setYRange(ylow, yhig);
  }
}
