package config.svt;

import java.util.Vector;
import java.util.Iterator;

import config.util.AppConstants;

public class Packet implements SvtObject {
  private int [] words;
  private int index;
  public Packet(final Vector<Integer> vec) {
    int len = vec.size(), n = 0;
    int [] words = new int[len];
    for (Iterator<Integer> it = vec.iterator(); it.hasNext();) {
      Integer obj = it.next();
      words[n++] = obj.intValue();       
    }
    setWords(words);
  }
  public Packet(final int [] words) {
    setWords(words);
  }
  public void setWords(final int [] words) {
    this.words = new int[words.length];
    System.arraycopy(words, 0, this.words, 0, words.length);
  }
  public void setIndex(int index) {
    this.index = index;
  }
  public int getIndex() {
    return index;
  }
  public String getInfo() {
    StringBuilder buf = new StringBuilder(AppConstants.MEDIUM_BUFFER_SIZE);
    boolean lastWasXft = false,         
            lastWasSvx = false;
    int nroad = 0, ntrk = 0, nxft = 0, nsvx = 0;
    buf.append("-- Packet ").append(getIndex()).append(" --\n");
    for (int i = 0; i < words.length; i++) {
      int w = words[i];
      int layer = w >> 18 & 0x7;

      if ((w >> 21 & 0x1) == 1) { // Road word EP = 1 
        buf.append("    Road").append(Road.getBanner());
        buf.append(AppConstants.d8Format.sprintf(++nroad)).append(new Road(w));
        lastWasXft = false;
        lastWasSvx = false;
      }
      else if (lastWasXft) {     // Second XFT Word
        buf.append(" XFT Trk").append(XftTrack.getBanner());
        buf.append(AppConstants.d8Format.sprintf(++ntrk)).append(new XftTrack(w));
        lastWasXft = false;
        lastWasSvx = false;
      } 
      else if (layer == 5) {     // First XFT Word
        buf.append(" XFT Hit").append(Hit.getXFTBanner());
        buf.append(AppConstants.d8Format.sprintf(++nxft)).append(new Hit(w));
        lastWasXft = true;
        lastWasSvx = false;
      }
      else {                     // Silicon Hit Word
        if (!lastWasSvx) buf.append(" SVX Hit").append(Hit.getSVXBanner());
        buf.append(AppConstants.d8Format.sprintf(++nsvx)).append(new Hit(w));
        lastWasXft = false;
        lastWasSvx = true;
      }
    }
    return buf.toString();
  }
  public String toString() {
    return getInfo();
  }
  public static void main(String [] argv) {
    int [] words = {0x1459b0, 0x08659f, 0x2e0399};
    System.out.println(new Packet(words));
  }
}
