package config.svt;

import config.util.AppConstants;

public class Road implements SvtObject {
  private int word;
  private int index; 
  public Road(int word) {
    setWord(word);
  }
  public void setWord(int word) {
    this.word = word;
  }
  public int getRoadID() { 
    return word & 0x3ff; 
  }
  public int getWedge() { 
    return (word >> 17) & 0xf; 
  }
  public int getPattern() { 
    return word & 0x7f; 
  }
  public int getAddr() { 
    return word & 0x1ffff; 
  }
  public int getAmBoard() { 
    return (word >> 14) & 0x3; 
  }
  public int getAmPlug() { 
    return (word >> 10) & 0xf; 
  }
  public int getAmChip() { 
    return (word >> 7) & 0x7; 
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
      .append(AppConstants.s6Format.sprintf("Wedge"))
      .append(AppConstants.s6Format.sprintf("Addr"))
      .append(AppConstants.s8Format.sprintf("AmBoard"))
      .append(AppConstants.s8Format.sprintf("AmPlug"))
      .append(AppConstants.s8Format.sprintf("Road"))
      .append(AppConstants.s6Format.sprintf("Chip"))
      .append(AppConstants.s8Format.sprintf("Pattern"))
      .append("\n");
    
    return sb.toString();
  }
  public String getInfo() {
    StringBuilder sb = new StringBuilder(AppConstants.SMALL_BUFFER_SIZE);

    sb.append(AppConstants.h8Format.sprintf(word))
      .append(AppConstants.d6Format.sprintf(getWedge()))
      .append(AppConstants.h65Format.sprintf(getAddr()))
      .append(AppConstants.d8Format.sprintf(getAmBoard()))
      .append(AppConstants.d8Format.sprintf(getAmPlug()))
      .append(AppConstants.d8Format.sprintf(getRoadID()))
      .append(AppConstants.d6Format.sprintf(getAmChip()))
      .append(AppConstants.d8Format.sprintf(getPattern()))
      .append("\n");

    return sb.toString();
  }
  public String toString() {
    return getInfo();
  }
  public static void main(String [] argv) {
    int word = 0x15af55;
    System.out.println(new Road(word));
  }
}
