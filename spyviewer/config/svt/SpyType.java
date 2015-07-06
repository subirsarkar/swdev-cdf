package config.svt;

import config.util.AppConstants;

public class SpyType {
  public static final int         HIT =  0;
  public static final int        ROAD =  1;
  public static final int      PACKET =  2;
  public static final int      SVTTRK =  3;
  public static final int      XFTTRK =  4;
  public static final int UNSUPPORTED = -1;
  public static final int      NOTYPE = -2;
  
  public static final String [] svtTypeList = 
    {
      "Hit",
      "Road",
      "Packet",
      "SvtTrack",
      "XftTrack"
    };
  private static final String    
        HIT_TYPE = "HF_OSPY || AMS_HIT_SPY || AMSRW_HIT_SPY || HB_HIT_SPY || HBPP_HIT_SPY",
       ROAD_TYPE = "AMS_OUT_SPY || AMSRW_OUT_SPY || HB_ROAD_SPY || HBPP_ROAD_SPY",
     PACKET_TYPE = "HB_OUT_SPY || HBPP_OUT_SPY || TF_ISPY || TFPP_ISPY",
     SVTTRK_TYPE = "MRG_D_SPY || TF_OSPY || TFPP_OSPY",
     XFTTRK_TYPE = "XTFA_TRK_SPY";

  public static int findType(final String spy) {
    int type; 
    if (HIT_TYPE.indexOf(spy) != -1)
      type = HIT;
    else if (ROAD_TYPE.indexOf(spy) != -1)
      type = ROAD;
    else if (PACKET_TYPE.indexOf(spy) != -1)
      type = PACKET;
    else if (SVTTRK_TYPE.indexOf(spy) != -1)
      type = SVTTRK;
    else if (XFTTRK_TYPE.indexOf(spy) != -1)
      type = XFTTRK;
    else
      type = UNSUPPORTED;

    return type;
  }
  public static int findMRGType(final String dtype) {
    int type; 
    if (dtype.equals(svtTypeList[0]))
      type = HIT;
    else if (dtype.equals(svtTypeList[1])) // Unfortunately AMS and AMSRW OUT spy differ
      type = ROAD;
    else if (dtype.equals(svtTypeList[2]))
      type = PACKET;
    else if (dtype.equals(svtTypeList[3]))
      type = SVTTRK;
    else if (dtype.equals(svtTypeList[4]))
      type = XFTTRK;
    else
      type = -1;

    return type;
  }
}
