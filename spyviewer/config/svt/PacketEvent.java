package config.svt;

import java.util.Vector;
import java.util.Iterator;
import config.util.AppConstants;

public class PacketEvent extends SvtEvent {
    /** Constructor 
     *  @param data  The event words
     *  @param ee    The End Event word
     */
  public PacketEvent(final int [] data, int ee) {
    super(data, ee);
  }
  public void addObjects(final int [] data) {
    Vector<Integer> vec = new Vector<Integer>(100);
    for (int i = 0; i < data.length; i++) {
      vec.addElement(new Integer(data[i]));
      if ( (data[i] >> 21 & 0x1) == 1) { // End packet bit
        addObject(new Packet(vec));
        vec.removeAllElements();
      }
    }
  }
    /** Get a Packet object
     *  @param i Packet index
     *  @return i-th Packet
     */
  public Packet getPacket(int i) {
    return (Packet) getObject(i);
  }
  public String toString() {
    StringBuilder buf = new StringBuilder(config.util.AppConstants.MEDIUM_BUFFER_SIZE);
    buf.append("== Ev ").append(getIndex()).append(" ==\n");
    int index = 0;
    for (Iterator<SvtObject> it = iterator(); it.hasNext(); ) {
      SvtObject obj = it.next();
      obj.setIndex(index);
      buf.append(obj);
    }
    return buf.toString();
  }
  public static void main(String [] argv) {
    int [] words = {
        0x15af55, 0x019a11,  0x0c1351,   0x09620e,   0x084e18,  0x000800,   0x20028f,
    }; 
    System.out.println(new PacketEvent(words, 0x6003ff));
  }
}
