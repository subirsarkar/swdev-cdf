package config.svt;

import config.util.AppConstants;

public class SvtTrack implements SvtObject {
  // To convert from strip/16 to cm:  
  // using SVXII layers 0, 1, 2, 3, 4 
  public static final int NWORDS = 7;
  public static final double [] HIT_SCALE = 
  {
     10000.*16./60., 
     10000.*16./62., 
     10000.*16./60., 
     10000.*16./60., 
     10000.*16./65.
  }; 
 
  // Other scale constants 
  public static final double PHI_SCALE = 8192.0;
  public static final double D0_SCALE  = 0.0010;
  public static final double CUR_SCALE = 1.17e-5;
  public static final double CHI_SCALE = 1.;
  public static final double SCALE_PT_TO_CURV = 0.002116;

  private int index; 
  private int [] words = new int[NWORDS];
  public SvtTrack(final int [] words) {
    System.arraycopy(words, 0, this.words, 0, Math.min(this.words.length, words.length));
  }
  public double getPhi0() { 
    return (words[0] & 0x1fff)*(2*Math.PI/PHI_SCALE); 
  }
  public int getZin() {
    return (words[0] >> 13) & 0x7; 
  }
  public int getZout() {
    return (words[0] >> 16) & 0x7; 
  }
  public double getCurv() {
    int sign = ((words[1] >> 18 & 0x1) > 0) ? 1 : -1;
    return sign * (words[1] >> 10 & 0xff) * CUR_SCALE;
  }
  public double getD0() {
    int sign = ((words[1] >> 9 & 0x1) > 0) ? 1 : -1;
    return sign * (words[1] & 0x1ff) * D0_SCALE;  
  }
  public int getWedge() {
    return (words[2] >> 17) & 0xf;
  }
  public int getRoad() {
    return words[2] & 0x7fff;
  } 
  public double getChi2() {
    return (words[5] >> 10 & 0x7ff)*CHI_SCALE; 
  } 
  public int getXftNumber() {
    return words[6] & 0x1ff;
  } 
  public int getTFStatus() {
    return (words[6] >> 9) & 0xfff;  
  } 
  public int getFitQuality() {
    return getTFStatus() & 0xf;
  } 
  public int getTFError()  {
    return (getTFStatus() >> 5) & 0x7f;
  } 
  public int getHitOverflow() {
    return getTFError() & 0x1; 
  } 
  public int getLayerOverflow()  {
    return (getTFError() >> 1) & 0x1;
  } 
  public int getInvalidData()  {
    return (getTFError() >> 3) & 0x1;
  } 
  public int getFitOverflow()  {
    return (getTFError() >> 4) & 0x1;
  } 
  public int getFifoOverflow()  {
    return (getTFError() >> 5) & 0x1;
  } 
  public int getErrorOR()  {
    return (getTFError() >> 6) & 0x1; 
  }
  public double getHit(int layer)  {
    if (layer < 0 || layer > 4) return -1.0;
    return getHit16(layer)/HIT_SCALE[layer];
  }
  public int getHit16(int layer)  {
    int value;
    switch (layer) {
      case 0:
        value = words[3] & 0xff;
        break;
      case 1:
        value = (words[3] >> 10) & 0xff;
        break;
      case 2:
        value = words[4] & 0xff;
        break;
      case 3:
        value = (words[4] >> 10) & 0xff;
        break;
      default:
        value = words[5] & 0xff;
        break;
    }
    return value;
  }
  public int getHitLCF(int layer)  {
    int value;
    switch (layer) {
      case 0:
        value = (words[3] >>  8) & 0x1;
        break;
      case 1:
        value = (words[3] >> 18) & 0x1;
        break;
      case 2:
        value = (words[4] >>  8) & 0x1;
        break;
      case 3:
        value = (words[4] >> 18) & 0x1;
        break;
      default:
        value = (words[5] >>  8) & 0x1;
        break;
    }
    return value;
  }
  public int getHitEF(int layer) {
    int value;
    switch (layer) {
      case 0:
        value = (words[3] >>  9) & 0x1;
        break;
      case 1:
        value = (words[3] >> 19) & 0x1;
        break;
      case 2:
        value = (words[4] >>  9) & 0x1;
        break;
      case 3:
        value = (words[4] >> 19) & 0x1;
        break;
      default:
        value = (words[5] >>  9) & 0x1;
        break;
    }
    return value;
  }
  public int getGBit() {
    return (words[4] >> 20) & 0x1;
  }
  public double getPt() {
    double curv = getCurv();
    return ( (Math.abs(curv) > 1.0e-10) ? SCALE_PT_TO_CURV / Math.abs(curv)
                             : 1.0e10 ); 
  }  
  public void setIndex(int index) {
    this.index = index;
  }
  public int getIndex() {
    return index;
  }
  public static String getBanner() {
    StringBuilder sb = new StringBuilder(AppConstants.SMALL_BUFFER_SIZE);

    sb.append(AppConstants.s5Format.sprintf("Phi"))
      .append(AppConstants.s8Format.sprintf("D0"))
      .append(AppConstants.s10Format.sprintf("Curv"))
      .append(AppConstants.s8Format.sprintf("Pt"))
      .append(AppConstants.s5Format.sprintf("Chi2"))
      .append(AppConstants.s4Format.sprintf("Wed"))
      .append(AppConstants.s3Format.sprintf("ZI"))
      .append(AppConstants.s3Format.sprintf("ZO"))
      .append(AppConstants.s6Format.sprintf("Road"))
      .append(AppConstants.s4Format.sprintf("XFT"))
      .append(AppConstants.s6Format.sprintf("Hit0"))
      .append(AppConstants.s6Format.sprintf("Hit1"))
      .append(AppConstants.s6Format.sprintf("Hit2"))
      .append(AppConstants.s6Format.sprintf("Hit3"))
      .append(AppConstants.s6Format.sprintf("Hit4"))
      .append(AppConstants.s4Format.sprintf("QFt"))
      .append(AppConstants.s5Format.sprintf("TFSt"))
      .append(AppConstants.s5Format.sprintf("TFEr"))
      .append("\n");
    
    return sb.toString();
  }
  public String getInfo() {
    StringBuilder sb = new StringBuilder(AppConstants.SMALL_BUFFER_SIZE);

    sb.append(AppConstants.f52Format.sprintf(getPhi0()));
    sb.append(AppConstants.f83Format.sprintf(getD0()));
    sb.append(AppConstants.f106Format.sprintf(getCurv()));
    double pt = ( (Math.abs(getPt()) > 999.99) ? ( (getPt() < 0.0 ? -1 : 1) * 999.99 )
                                               : getPt() );
    sb.append(AppConstants.f82Format.sprintf(pt));
    sb.append(AppConstants.f50Format.sprintf(getChi2()));
    sb.append(AppConstants.d4Format.sprintf(getWedge()));
    sb.append(AppConstants.d3Format.sprintf(getZin()));
    sb.append(AppConstants.d3Format.sprintf(getZout()));
    sb.append(AppConstants.d6Format.sprintf(getRoad()));
    sb.append(AppConstants.d4Format.sprintf(getXftNumber()));
    
    sb.append(AppConstants.f63Format.sprintf(getHit(0)));
    sb.append(AppConstants.f63Format.sprintf(getHit(1)));
    sb.append(AppConstants.f63Format.sprintf(getHit(2)));
    sb.append(AppConstants.f63Format.sprintf(getHit(3)));
    sb.append(AppConstants.f63Format.sprintf(getHit(4)));

    sb.append(AppConstants.h40Format.sprintf(getFitQuality()));
    sb.append(AppConstants.h5Format.sprintf(getTFStatus()));
    sb.append(AppConstants.h5Format.sprintf(getTFError()));

    sb.append("\n");

    return sb.toString();
    
  }
  public String toString() {
    return getInfo();
  }
  public static void main(String [] argv) {
    int [] words = {
        0x15af55,
        0x019a11,
        0x0c1351,
        0x09620e,
        0x084e18,
        0x000800,  
        0x20028f
    }; 
    System.out.println(new SvtTrack(words));
  }
}
