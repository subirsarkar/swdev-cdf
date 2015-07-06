package config.hist;

import jas.hist.XYDataSource;

public class GraphDataSource implements XYDataSource {
   public GraphDataSource() {
     xdata = new double[0];
   }
   public GraphDataSource(double [] xdata, double [] ydata, double [] error) {
      this(xdata, ydata, error, "Array Data Source");
   }
   public GraphDataSource(double[] xdata, double [] ydata, double [] error, String name)  {
     setData(xdata, ydata, error, name);
   }
   public void setData(double[] xdata, double [] ydata, double [] error, String name) {
      this.xdata = xdata;
      this.ydata = ydata;
      this.error = error;
      this.name  = name;
   }
   public int getNPoints() {
     return xdata.length;
   }
   public double getX(int index) {
     return xdata[index];
   }
   public double getY(int index) {
     return ydata[index];
   }
   public double getPlusError(int index) {
     return 0.5*error[index];
   }
   public double getMinusError(int index) {
     return 0.5*error[index];
   }
   public String getTitle() {return name;}
   /**
    * Returns one of DOUBLE or DATE
    */
   public int getAxisType() {return DOUBLE;}
              
   private double [] xdata;
   private double [] ydata;
   private double [] error;
   private String name = new String();
}
