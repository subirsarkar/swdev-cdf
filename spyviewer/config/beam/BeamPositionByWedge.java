package config.beam;

import java.text.SimpleDateFormat;

import com.smartsockets.TipcMsg;
import com.smartsockets.TipcMt;
import com.smartsockets.TipcException;
import com.smartsockets.Tut;

import config.util.Tools;
import config.util.AppConstants;

public class BeamPositionByWedge implements IBeamPosition {
  private final static boolean DEBUG = false;
  private final static int FIELD_LENGTH = 12;
  private static SimpleDateFormat formatter = 
     new SimpleDateFormat("hh:mm:ss, MM/dd/yy");
  private long utime;
  private int barrel;
  private Position [][] pList 
      = new Position[AppConstants.nBarrel][AppConstants.nWedge];
  double [] xDataArray = new double[AppConstants.nWedge];
  double [] yDataArray = new double[AppConstants.nWedge];
  double [] xErrArray  = new double[AppConstants.nWedge];
  double [] yErrArray  = new double[AppConstants.nWedge];

  /* Constructor */
  public BeamPositionByWedge() {
    barrel = 0;
  }
  /** Update Beam position informations 
   *  @param msg  SmartSockets message
   */
  public void update(TipcMsg msg)  {
    try {
      msg.setCurrent(0);

      // Print out the name of the type of message 
      TipcMt mt   = msg.getType();
      String name = mt.getName();
      if (DEBUG) {
        System.out.println("INFO. BeamFinder: Message type name is " + name);
        msg.print();
      }
     
      utime = (long) msg.nextInt4(); 

      for (int i = 0; i < AppConstants.nBarrel; i++) {
        for (int j = 0; j < AppConstants.nWedge; j++) {
          float [] fields = msg.nextReal4Array();  
          if (pList[i][j] == null) pList[i][j] = new Position();
          pList[i][j].setData(fields, utime);
        }
      }
    }
    catch (TipcException te) {
      Tut.warning(te);
    }
  }
  public void setBarrel(int barrel) {
    this.barrel = barrel;
    for (int i = 0; i < AppConstants.nWedge; i++) {
      if (pList[barrel][i] == null) continue;
      xDataArray[i] = pList[barrel][i].getX();
      yDataArray[i] = pList[barrel][i].getY();
      xErrArray[i]  = pList[barrel][i].getXError();
      yErrArray[i]  = pList[barrel][i].getYError();
    }    
  }
  public int getBarrel() {
    return barrel;
  }
  public String getData() {
    if (barrel < 0 || barrel > AppConstants.nBarrel-1)
      throw new RuntimeException("Invalid Barrel number = " + barrel);

    StringBuilder buf = new StringBuilder(AppConstants.MEDIUM_BUFFER_SIZE);
    buf.append(Tools.getHeader("Beam position for barrel " + barrel));

    buf.append("<FONT SIZE=+1 COLOR=blue>");
    buf.append("<B>Beam Positions for Barrel ").append(barrel).append("</B>").append("\n");
    buf.append("</FONT>");
    buf.append("<HR>");

    buf.append("<TABLE BGCOLOR=\"white\" CELLSPACING=1 CELLPADDING=2 WIDTH=\"100%\">");
    buf.append("<TR ALIGN=CENTER>");
    buf.append(Position.getTableHeader("Wedge"));
    buf.append("</TR>\n");

    for (int i = 0; i < AppConstants.nWedge; i++) {
      if (pList[barrel][i] == null) continue;
      buf.append(pList[barrel][i].getHtmlData(i)); 
    }
    buf.append("</TABLE>\n");
    buf.append(Tools.getFooter());

    return buf.toString();
  }
  /** Override <CODE>toString()</CODE> to return an htmlifiled beam position information
   *  @return htmlifiled beam position information
   */
  public String toString() {
    StringBuilder buf = new StringBuilder(AppConstants.LARGE_BUFFER_SIZE);
    for (int i = 0; i < AppConstants.nBarrel; i++) {
      setBarrel(i);
      buf.append(getData());
    }
    return buf.toString();
  }
  public double [] getXDataArray() {
    return xDataArray;    
  }
  public double [] getYDataArray() {
    return yDataArray;    
  }
  public double [] getXErrorArray() {
    return xErrArray;    
  }
  public double [] getYErrorArray() {
    return yErrArray;    
  }
}
