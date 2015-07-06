package config.beam;

import java.text.SimpleDateFormat;

import com.smartsockets.TipcMsg;
import com.smartsockets.TipcMt;
import com.smartsockets.TipcException;
import com.smartsockets.Tut;

import config.util.Tools;
import config.util.AppConstants;

public class BeamPositionByBarrel implements IBeamPosition {
  private final static boolean DEBUG = false;
  private static SimpleDateFormat formatter = 
     new SimpleDateFormat("hh:mm:ss, MM/dd/yy");
  private long utime = 0;
  Position [] pList    = new Position[AppConstants.nBarrel];
  double [] xDataArray = new double[AppConstants.nBarrel];
  double [] yDataArray = new double[AppConstants.nBarrel];
  double [] xErrArray  = new double[AppConstants.nBarrel];
  double [] yErrArray  = new double[AppConstants.nBarrel];
  
  /* Constructor */
  public BeamPositionByBarrel() {
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
        float [] fields = msg.nextReal4Array();  
        if (pList[i] == null) pList[i] = new Position();
        pList[i].setData(fields, utime);
        xDataArray[i] = pList[i].getX();
        yDataArray[i] = pList[i].getY();
        xErrArray[i]  = pList[i].getXError();
        yErrArray[i]  = pList[i].getYError();
      }
    }
    catch (TipcException te) {
      Tut.warning(te);
    }
  }
  /* In principle a few lines could be moved to Position.java */
  public String getData() {
    StringBuilder buf = new StringBuilder(AppConstants.MEDIUM_BUFFER_SIZE);
    buf.append(Tools.getHeader("Beam position"));

    buf.append("<FONT SIZE=+1 COLOR=blue>");
    buf.append("<B>Beam Positions </B>").append("\n");
    buf.append("</FONT>");
    buf.append("<HR>");

    buf.append("<TABLE BGCOLOR=\"white\" CELLSPACING=1 CELLPADDING=2 WIDTH=\"100%\">");
    buf.append("<TR ALIGN=CENTER>");
    buf.append(Position.getTableHeader("Bar"));
    buf.append("</TR>\n");

    for (int i = 0; i < AppConstants.nBarrel; i++) {
      if (pList[i] == null) continue;
      buf.append(pList[i].getHtmlData(i)); 
    }
    buf.append("</TABLE>\n");
    buf.append(Tools.getFooter());

    return buf.toString();
  }
  /** Override <CODE>toString()</CODE> to return an htmlifiled beam position information
   *  @return htmlifiled beam position information
   */
  public String toString() {
    return getData();
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
