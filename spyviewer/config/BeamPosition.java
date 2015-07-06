package config;

import java.util.*;
import java.text.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import com.smartsockets.*;

import config.util.*;

public class BeamPosition {
  private final static boolean DEBUG = false;
  private final static int nBarrel = 6;
  private final static int FIELD_LENGTH = 12;
  private static SimpleDateFormat formatter = 
     new SimpleDateFormat("hh:mm:ss, MM/dd/yy");
  private    int [] ntrk   = new int[nBarrel];
  private    int [] mtrk   = new int[nBarrel];
  private    int [] fit_q  = new int[nBarrel];
  private    int [] fit_e  = new int[nBarrel];
  private  float [] xpos   = new float[nBarrel];
  private  float [] ypos   = new float[nBarrel];
  private  float [] sx     = new float[nBarrel];
  private  float [] sy     = new float[nBarrel];
  private  float [] sxy    = new float[nBarrel];
  private  float [] sd     = new float[nBarrel];
  private  float [] xori   = new float[nBarrel];
  private  float [] yori   = new float[nBarrel];
  private String [] lupstr = new String[nBarrel];
  private long utime;

  /* Constructor */
  public BeamPosition() {
    for (int i = 0; i < lupstr.length; i++) 
      lupstr[i] = "No Fit!";
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
      for (int i = 0; i < nBarrel; i++) {
        float [] fields = msg.nextReal4Array();  
        if (fields.length != FIELD_LENGTH)
           throw new RuntimeException("Wrong array size = " + fields.length);
        ntrk[i]  = (int) fields[0];
        mtrk[i]  = (int) fields[1];
        fit_q[i] = (int) fields[2];
        fit_e[i] = (int) fields[3];
        if (DEBUG) System.out.println(fit_q[i] + "/" + fit_e[i]);
        if (fit_q[i] != 0 && fit_e[i] == 0) {
          lupstr[i] = formatter.format(new Date(utime*1000));
        }
        xpos[i]  = fields[4];
        ypos[i]  = fields[5];
        sx[i]    = fields[6];
        sy[i]    = fields[7];
        sxy[i]   = fields[8];
        sd[i]    = fields[9];
        xori[i]  = fields[10];
        yori[i]  = fields[11];
      }
    }
    catch (TipcException te) {
      Tut.warning(te);
    }
  }
  /** Override <CODE>toString()</CODE> to return an htmlifiled beam position information
   *  @return htmlifiled beam position information
   */
  public String toString() {
    StringBuilder buf = new StringBuilder(AppConstants.MEDIUM_BUFFER_SIZE);
    buf.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">\n"); 
    buf.append("<HTML>\n");
    buf.append("<HEAD><TITLE>Beam Position Update</TITLE></HEAD>\n");
 
    buf.append("<BODY BGCOLOR=\"white\" LINK=\"blue\" VLINK=\"red\">\n");

    buf.append("<FONT SIZE=+1 COLOR=blue>");
    buf.append("<B>Online Beam Position Monitor</B>").append("\n");
    buf.append("</FONT>");

    buf.append("<TABLE BGCOLOR=\"white\" CELLSPACING=1 CELLPADDING=2 WIDTH=\"100%\">");
    buf.append("<TR ALIGN=CENTER>");
    buf.append("\n<TD>Bar</TD>");
    buf.append("\n<TD>Ntrk</TD>");
    buf.append("\n<TD>XPos</TD>");
    buf.append("\n<TD>YPos</TD>");
    buf.append("\n<TD>Sig</TD>");
    buf.append("\n<TD>XErr</TD>");
    buf.append("\n<TD>YErr</TD>");
    buf.append("\n<TD>Corr</TD>");
    buf.append("\n<TD>Last update</TD>");
    buf.append("</TR>\n");

    for (int i = 0; i < nBarrel; i++) {
      double x    = xpos[i] * 10000 + xori[i];  // cm to micron + origin shift
      double y    = ypos[i] * 10000 + yori[i]; 
      double dx   = Math.sqrt(sx[i]) * 10000;
      double dy   = Math.sqrt(sy[i]) * 10000;
      double dd   = Math.sqrt(sd[i]) * 10000;
      double corr = 2*sxy[i]/(sx[i]+sy[i]+0.0001);

      buf.append("<TR ALIGN=RIGHT>\n<TD >").append(i); 
      buf.append("</TD>\n<TD>").append(ntrk[i]);
      buf.append("</TD>\n<TD>").append(AppConstants.f82Format.sprintf(x));
      buf.append("</TD>\n<TD>").append(AppConstants.f82Format.sprintf(y)); 
      buf.append("</TD>\n<TD>").append(AppConstants.f82Format.sprintf(dd)); 
      buf.append("</TD>\n<TD>").append(AppConstants.f83Format.sprintf(dx)); 
      buf.append("</TD>\n<TD>").append(AppConstants.f83Format.sprintf(dy)); 
      buf.append("</TD>\n<TD>").append(AppConstants.f85Format.sprintf(corr)); 
      buf.append("</TD>\n<TD>").append(lupstr[i]); 
      buf.append("</TD>\n</TR>\n");
    }
    buf.append("</TABLE>\n");

    Date date = new Date(utime * 1000);  // Convert to ms

    buf.append("<FONT COLOR=green>");
    buf.append("<P> - Last updated at ").append(formatter.format(date)).append("</P>\n");
    buf.append("<P> - Coordinates are in microns").append("</P>\n");
    buf.append("</FONT>");

    buf.append("</BODY>\n"); 
    buf.append("</HTML>\n");

    return buf.toString();
  }
}
