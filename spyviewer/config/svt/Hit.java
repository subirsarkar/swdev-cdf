package config.svt;

import config.util.AppConstants;

public class Hit implements SvtObject {
  private int word;
  private int layer;
  private int index; 

  public Hit(int word) {
    setWord(word);
  }
  public void setWord(int word) {
    this.word = word;
    layer = (word >> 18) & 0x7; 
  }
  public int getCoordinate() { 
    return word & 0x3fff; 
  }
  public int getLongClusterBit() { 
    return (word >> 14) & 0x1; 
  }
  public int getBarrel() { 
    return (word >> 15) & 0x7; 
  }
  public int getLayer() { 
    return layer; 
  }
  public int getSuperStripAddress() { 
    return (word >> 4) & 0x1fff; 
  }
  public boolean isXftWord() {
    return (layer == 5 ? true : false);
  }
  public String getWordType() {
    return (layer == 5 ? "XFT" : "SVX");
  }
  public void setIndex(int index) {
    this.index = index;
  }
  public int getIndex() {
    return index;
  }
  public static String getSVXBanner() {
    StringBuilder sb = new StringBuilder(AppConstants.SMALL_BUFFER_SIZE);
    sb.append(AppConstants.s8Format.sprintf("Word"))
      .append(AppConstants.s6Format.sprintf("Layer"))
      .append(AppConstants.s8Format.sprintf("Barrel"))
      .append(AppConstants.s6Format.sprintf("LCBit"))
      .append(AppConstants.s6Format.sprintf("Coord"))
      .append("\n");
    
    return sb.toString();
  }
  public static String getXFTBanner() {
    StringBuilder sb = new StringBuilder(AppConstants.SMALL_BUFFER_SIZE);

    sb.append(AppConstants.s8Format.sprintf("Word"))
      .append(AppConstants.s6Format.sprintf("Layer"))
      .append(AppConstants.s12Format.sprintf("SuperStrip"))
      .append("\n");
    
    return sb.toString();
  }
  public String getInfo() {
    StringBuilder sb = new StringBuilder(AppConstants.SMALL_BUFFER_SIZE);
    sb.append(AppConstants.h8Format.sprintf(word));
    sb.append(AppConstants.d6Format.sprintf(layer));
    if (layer == 5) {
      int addr = getSuperStripAddress();
      sb.append(AppConstants.h124Format.sprintf(addr))
        .append(" (").append(AppConstants.d6Format.sprintf(addr)).append(")");
    }
    else {
      sb.append(AppConstants.d8Format.sprintf(getBarrel()));
      sb.append(AppConstants.d6Format.sprintf(getLongClusterBit()));
      int coord = getCoordinate();
      sb.append(AppConstants.h64Format.sprintf(coord))
        .append(" (").append(AppConstants.d6Format.sprintf(coord)).append(")");
    }
    sb.append("\n");

    return sb.toString();
  }
  public String toString() {
    return getInfo();
  }
  public static void main(String [] argv) {
    int word = 0x15af55;
    System.out.println(new Hit(word));
  }
}
