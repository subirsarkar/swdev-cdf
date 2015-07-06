package config.hist;

import daqmsg.DaqMsgBase;

/** 
 * Histogram related message types definitions go here
 * @author S. Sarkar
 * @version 0.1, 05/20/2001
 */
public interface SVTHistoMessage {
  public static final String SVTHistoContatiner_MessageName = "SVTHISTO_CONTAINER";
  public static final int SVTHistoContatiner_MessageType = DaqMsgBase.SVTMON_BASE + 200;
  public static final String SVTHistoContatiner_MessageGrammar = "msg_array";

  public static final String SVTHisto1D_MessageName = "SVTHISTO_1D";
  public static final int SVTHisto1D_MessageType = DaqMsgBase.SVTMON_BASE + 201;
  public static final String SVTHisto1D_MessageGrammar  = 
              "int4 str int4 real4 real4 int4 int4 int4 real4_array real4_array";

  public static final String SVTHisto2D_MessageName = "SVTHISTO_2D";
  public static final int SVTHisto2D_MessageType = DaqMsgBase.SVTMON_BASE + 202;
  public static final String SVTHisto2D_MessageGrammar = 
              "int4 str int4_array real4_array int4 real4_array real4_array";

  public static final String SVTHisto2DCompressed_MessageName = "SVTHISTO_2D_COMPRESSED";
  public static final int SVTHisto2DCompressed_MessageType = DaqMsgBase.SVTMON_BASE + 203;
  public static final String SVTHisto2DCompressed_MessageGrammar = 
              "int4 str int4_array real4_array int4 real4_array binary";
}
