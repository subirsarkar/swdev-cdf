package config.beam;

import java.util.Date;
import java.text.SimpleDateFormat;

import config.util.AppConstants;

public class Position {
  private final static boolean DEBUG = false;
  public final static int CM2MU = 10000;
  private static SimpleDateFormat formatter = 
     new SimpleDateFormat("hh:mm:ss, MM/dd/yy");
  private final static int FIELD_LENGTH = 12;
  private final static float xmax = 5000;
  private final static float ymax = 10000;

  private    int ntrk;
  private    int mtrk;
  private    int fit_q;
  private    int fit_e;
  private  float xpos;
  private  float ypos;
  private  float sx;
  private  float sy;
  private  float sxy;
  private  float sd;
  private  float xori;
  private  float yori;
  private  String lupstr = new String("<FONT COLOR=\"teal\">No Fit!</FONT>");

  public Position() { 
    float [] vec = new float[FIELD_LENGTH];
    setData(vec, 0);
  }
  public void setData(float [] vec, long utime) {
    if (vec.length != FIELD_LENGTH)
      throw new RuntimeException("Wrong array size = " + vec.length);
    ntrk   = (int)vec[0];
    mtrk   = (int)vec[1];
    fit_q  = (int)vec[2];
    fit_e  = (int)vec[3];
    xpos   = vec[4];
    ypos   = vec[5];
    sx     = vec[6];
    sy     = vec[7];
    sxy    = vec[8];
    sd     = vec[9];
    xori   = vec[10];
    yori   = vec[11];
    if (fit_q != 0 && fit_e == 0) 
      lupstr = formatter.format(new Date(utime*1000));
   }
  public String getFormattedData(int index) {
    return getHtmlData(index);
  }
  public String getHtmlData(int index) {
    StringBuilder buf = new StringBuilder(AppConstants.MEDIUM_BUFFER_SIZE);

    // Construct the relevant quantities
    double x    = getX();
    double y    = getY();
    double dx   = getXError();
    double dy   = getYError(); 
    double dd   = Math.sqrt(sd) * CM2MU; 
    double corr = 2*sxy/(sx+sy+0.0001); 
 
    buf.append("<TR ALIGN=RIGHT>\n<TD >").append(index);  
    buf.append("</TD>\n<TD>").append(ntrk); 
    buf.append("</TD>\n<TD>").append(getPosString(x, xmax)); 
    buf.append("</TD>\n<TD>").append(getPosString(y, ymax));  
    buf.append("</TD>\n<TD>").append(AppConstants.f82Format.sprintf(dd));  
    buf.append("</TD>\n<TD>").append(AppConstants.f83Format.sprintf(dx));  
    buf.append("</TD>\n<TD>").append(AppConstants.f83Format.sprintf(dy));  
    buf.append("</TD>\n<TD>").append(AppConstants.f85Format.sprintf(corr));  
    buf.append("</TD>\n<TD>").append(lupstr);  
    buf.append("</TD>\n</TR>\n"); 

    return buf.toString();
  }
  public double getX() {
    return xpos * CM2MU + xori;  // cm to micron + origin shift 
  }  
  public double getY() {
    return ypos * CM2MU + yori;  // cm to micron + origin shift 
  }  
  public double getXError() {
    return Math.sqrt(sx) * CM2MU; 
  }  
  public double getYError() {
    return Math.sqrt(sy) * CM2MU; 
  }  
  public String getPosString(double val, double vmax) {
    StringBuilder buf = new StringBuilder(AppConstants.SMALL_BUFFER_SIZE);
    if (val > vmax) buf.append("<FONT COLOR=\"red\">");
    buf.append(AppConstants.f82Format.sprintf(val));
    if (val > vmax) buf.append("</FONT>");
    return buf.toString();
  }
  public static String getTableHeader(final String name) {
    StringBuilder buf = new StringBuilder(AppConstants.SMALL_BUFFER_SIZE);
    buf.append("\n<TD>").append("<B>").append(name).append("</B></TD>");
    buf.append("\n<TD><B>Ntrk</B></TD>");
    buf.append("\n<TD><B>XPos</B></TD>");
    buf.append("\n<TD><B>YPos</B></TD>");
    buf.append("\n<TD><B>Sig</B></TD>");
    buf.append("\n<TD><B>XErr</B></TD>");
    buf.append("\n<TD><B>YErr</B></TD>");
    buf.append("\n<TD><B>Corr</B></TD>");
    buf.append("\n<TD><B>Last update</B></TD>");

    return buf.toString();
  }
  /** Override <CODE>toString()</CODE> to return an htmlifiled beam position information
   *  @return htmlifiled beam position information
   */
  public String toString() {
    return getHtmlData(1);
  }
}
