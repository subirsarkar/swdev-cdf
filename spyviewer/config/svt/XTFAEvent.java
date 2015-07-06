package config.svt;

import config.util.AppConstants;

public class XTFAEvent extends SvtEvent {
    /** Constructor 
     *  @param data  The event words
     *  @param ee    The End Event word
     */
  public XTFAEvent(final int [] data, int ee) {
    super(data, ee);
  }
  public void addObjects(final int [] data) {
    for (int i = 0; i < data.length; i++)
      addObject(new XftTrack(data[i]));
  }
  public int getNtrk() {
    return getNumberOfObjects();
  }
    /** Get an Xft Track object
     *  @param i track index
     *  @return i-th Xft Track
     */
  public XftTrack getTrack(int i) {
    return (XftTrack) getObject(i);
  }
  public static void main(String [] argv) {
    int [] words = {
        0x15af55, 0x019a11,  0x0c1351,   0x09620e,   0x084e18,  0x000800,   0x20028f,
        0x15af55, 0x019a11,  0x0c1351,   0x09620e,   0x084e18,  0x000800,   0x20028f,
        0x15af55, 0x019a11,  0x0c1351,   0x09620e,   0x084e18,  0x000800,   0x20028f,
        0x15af55, 0x019a11,  0x0c1351,   0x09620e,   0x084e18,  0x000800,   0x20028f,
        0x15af55, 0x019a11,  0x0c1351,   0x09620e,   0x084e18,  0x000800,   0x20028f,
        0x15af55, 0x019a11,  0x0c1351,   0x09620e,   0x084e18,  0x000800,   0x20028f,       
        0x15af55, 0x019a11,  0x0c1351,   0x09620e,   0x084e18,  0x000800,   0x20028f
    }; 
    System.out.println(new XTFAEvent(words, 0x6003ff));
  }
}
