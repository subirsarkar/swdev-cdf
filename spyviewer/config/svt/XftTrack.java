package config.svt;

import config.util.AppConstants;

public class XftTrack implements SvtObject {
  public static final double SCALE_PT_TO_CURV = 0.002116;
  // Long tracks
  private static final double [] PtAtBin_l = 
  {
     1.52,  1.57,  1.63,  1.68,  1.75,
     1.81,  1.88,  1.96,  2.04,  2.13,
     2.23,  2.34,  2.46,  2.59,  2.74,
     2.91,  3.05,  3.15,  3.25,  3.37,
     3.49,  3.62,  3.76,  3.92,  4.09,
     4.27,  4.47,  4.68,  4.92,  5.19,
     5.49,  5.82,  6.19,  6.62,  7.11,
     7.68,  8.35,  9.14, 10.11, 11.29,
    12.80, 14.77, 17.45, 21.33, 27.43,
    38.40, 64.00, 99.99 
  };

  // Short tracks
  private static final double [] PtAtBin_s = 
  {
      1.57,  1.71,  1.89,  2.12,  2.40,
      2.77,  3.27,  4.00,  4.80,  5.54,
      6.55,  8.00, 10.29, 14.40, 24.00,
     99.99 
  };
  private int word;
  private boolean valid;
  org.omg.CORBA.DoubleHolder ptObj = new org.omg.CORBA.DoubleHolder();
  private int index; 

  public XftTrack(int word) {
    setWord(word);
  }
  public void setWord(int word) {
    this.word = word;
    unpack();
  }
  void unpack() { 
    boolean junk = false;
    boolean illegal = CalculatePt(isShort(), getPtbin(), ptObj);
    if ((getWedge() == 1) && (getPtbin() == 0)) junk = true;
    valid = !junk && !illegal;
  }
  public int getWord() { 
    return word; 
  }
  public int getPhi() { 
    return word & 0xfff; 
  }
  public int getMiniwedge() { 
    return getPhi() >> 3; 
  }
  public int getMiniphi() { 
    return getPhi() & 0x7; 
  }
  public int getWedge() { 
    return getMiniwedge()/24; 
  }
  public int getPtbin() { 
    return (word >> 12) & 0x7f; 
  }
  public boolean isIsolated() { 
    return ((word >> 19 & 0x1) == 1) ? true : false; 
  }
  public boolean isShort() { 
    return ((word >> 20 & 0x1) == 1) ? true : false;
  }
  public double getPhirad() { 
    return getPhi()*2*Math.PI/2304; 
  }
  public double getPt() { 
    return ptObj.value;
  }
  public double getCurvature() { 
    return GetCurvature(ptObj.value);  
  }
  public boolean isValid() { 
    return valid; 
  }
  public static boolean CalculatePt(boolean shrt, int ptbin, org.omg.CORBA.DoubleHolder ptObj) {
    // Calculate Track pt from Pt bins and a lookup table
    // of Pt at bin center from XFT linker specs for long and 
    // short tracks.
 
    boolean illegal = false;
    double lpt = 0.0;
    if (!shrt) {
      if (ptbin > 95)  { 
        lpt = 0.; 
        illegal = true;
      }
      else {
        lpt = (ptbin < 48) ? -PtAtBin_l[ptbin]   
                           :  PtAtBin_l[95 - ptbin]; 
      }
    }
    else {
      if (ptbin < 32 || ptbin > 63) { 
        lpt = 0.; 
        illegal = true;
      }
      else {
        lpt = (ptbin < 48) ? -PtAtBin_s[ptbin - 32]
                           :  PtAtBin_s[63 - ptbin]; 
      }
    }
    ptObj.value = lpt;
    return illegal;
  }
    /** Calculate track curvature from transverse momentum */
  public static double GetCurvature(double pt) {
    int sign = (pt > 0.0) ? 1 : -1;
    return ( (Math.abs(pt) > 1.0e-10) ? (SCALE_PT_TO_CURV / pt)
                                      : (1.0e10 * sign) );
  }
    /** Scale phi onto the usual range of [0, 2PI] */
  public static double NormPhi(double phi_i) {
    double phi = phi_i;
    if (phi < 0.0) phi += 2*Math.PI;
    return phi%2*Math.PI;
  }
  public void setIndex(int index) {
    this.index = index;
  }
  public int getIndex() {
    return index;
  }
  public static String getBanner() {
    StringBuilder sb = new StringBuilder(AppConstants.SMALL_BUFFER_SIZE);

    sb.append(AppConstants.s8Format.sprintf("Word"))
      .append(AppConstants.s8Format.sprintf("Phi"))
      .append(AppConstants.s10Format.sprintf("Curv"))
      .append(AppConstants.s9Format.sprintf("Pt"))
      .append(AppConstants.s8Format.sprintf("Phibin"))
      .append(AppConstants.s4Format.sprintf("Wed"))
      .append(AppConstants.s5Format.sprintf("Mwed"))
      .append(AppConstants.s5Format.sprintf("Mphi"))
      .append(AppConstants.s6Format.sprintf("ptbin"))
      .append(AppConstants.s4Format.sprintf("iso"))
      .append(AppConstants.s6Format.sprintf("short"))
      .append(AppConstants.s6Format.sprintf("valid"))
      .append("\n");
    
    return sb.toString();
  }
  public String getInfo() {
    StringBuilder sb = new StringBuilder(AppConstants.SMALL_BUFFER_SIZE);

    sb.append(AppConstants.h8Format.sprintf(word));
    sb.append(AppConstants.f83Format.sprintf(getPhirad()));
    double curv = getCurvature();
    double sign = (curv < 0.0) ? -1.0 : 1.0;
    double curvx = sign * Math.min(Math.abs(curv), 9.999999);
    sb.append(AppConstants.f106Format.sprintf(curvx));

    sign = (ptObj.value < 0.0) ? -1.0 : 1.0;
    double ptx = sign * Math.min(Math.abs(ptObj.value),999.);
    sb.append(AppConstants.f93Format.sprintf(ptx));
    sb.append(AppConstants.d8Format.sprintf(getPhi()));
    sb.append(AppConstants.d4Format.sprintf(getWedge()));
    sb.append(AppConstants.d5Format.sprintf(getMiniwedge()));
    sb.append(AppConstants.d5Format.sprintf(getMiniphi()));
    sb.append(AppConstants.d6Format.sprintf(getPtbin()));
    sb.append(AppConstants.d4Format.sprintf(isIsolated() ? 1 : 0));
    sb.append(AppConstants.d6Format.sprintf(isShort() ? 1 : 0));
    sb.append(AppConstants.d6Format.sprintf(valid ? 1 : 0));

    sb.append("\n");

    return sb.toString();
    
  }
  public String toString() {
    return getInfo();
  }
  public static void main(String [] argv) {
    int word = 0x15af55;
    System.out.println(new XftTrack(word));
  }
}
