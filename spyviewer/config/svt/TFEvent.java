package config.svt;

import config.util.AppConstants;

public class TFEvent extends SvtEvent {
  private final static int TRACK_SIZE = 7;
    /** Constructor 
     *  @param data  The event words
     *  @param ee    The End Event word
     */
  public TFEvent(final int [] data, int ee) {
    super(data, ee);
  }
  public void addObjects(final int [] data) {
    int nw = 0;
    int [] words = new int[TRACK_SIZE];
    for (int i = 0; i < data.length; i++) {
      words[nw++] = data[i];
      if (nw == TRACK_SIZE) {
        if ( (data[i] >> 21 & 0x1) == 1) { // End packet bit
          addObject(new SvtTrack(words));
        }
        nw = 0;
      }
    }
  }
    /** Get Number of tracks present in an event
     *  @return Number of tracks in an event
     */
  public int getNtrk() {
    return getNumberOfObjects();
  }
    /** Get Number of tracks present in an event which pass a chi2 cut
     *  @param chi2 quality cut of the tracks
     *  @return Number of tracks in an event
     */
  public int getNtrk(double chi2) {
    int ntrk = 0;
    for (int i = 0; i < getNumberOfObjects(); i++) {
      SvtTrack track = getTrack(i);
      if (track.getChi2() <= chi2) ntrk++;
    }
    return ntrk;
  }
    /** Get an SVT Track object
     *  @param i track index
     *  @return i-th SVT Track
     */
  public SvtTrack getTrack(int i) {
    return (SvtTrack) getObject(i);
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
    System.out.println(new TFEvent(words, 0x6003ff));
  }
}
