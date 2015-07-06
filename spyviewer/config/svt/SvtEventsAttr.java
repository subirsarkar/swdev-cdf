package config.svt;

import java.util.Iterator;
import java.util.Vector;

import hep.analysis.Histogram;

import config.util.AppConstants;

abstract public class SvtEventsAttr implements SvtEvents {
  private static final int INIT_SIZE = 200;
  private Vector<SvtEvent> events = new Vector<SvtEvent>(INIT_SIZE);
    /* Constructor */
  public SvtEventsAttr(final int [] data) {
    buildEvents(data);
  }
    /** Get the event list ifself 
     *  @return The event list
     */
  public Vector<SvtEvent> getEvents() { 
    return events;
  }
    /** Given a spy buffer, build an event list 
     *  @param  data spy buffer array
     */
  public void buildEvents(final int [] data) {
    int [] words;
    int nfirst = 0, nlast = 0;
    events.removeAllElements();
    for (int i = 0; i < data.length; i++) {
      if ((data[i] & 0x600000) == 0x600000) {   // EoE word
        nlast = i;
        if (nlast-nfirst > 0) {
          words = new int[nlast-nfirst];
          for (int j = nfirst; j < nlast; j++) {
            words[j-nfirst] = data[j] & AppConstants.MASK23;
          }
        } 
        else {
          words = new int[0];
        }
        nfirst = nlast + 1;
        addEvent(words, data[i] & AppConstants.MASK23);
      }
    }
  }
    /** Return the event tag array
     *  @return the event tags in an array
     */
  public int [] getEventTags() {
    return getArray(1);
  }
    /** Return the end event word array
     *  @return the end event words in an array
     */
  public int [] getEEWords() {
    return getArray(0);
  }
  private int [] getArray(int opt) {
    if (events.isEmpty()) return new int[0];
  
    int nev = 0;
    int [] words = new int[events.size()];
    for (Iterator<SvtEvent> it = events.iterator(); it.hasNext(); ) {
      SvtEvent event = it.next();
      words[nev++] = (opt == 0) ? event.getEE() : (event.getEE() & AppConstants.MASK08);
    }
    return words;
  }
    /** Return a generic Svt event from the collection
     *  @param i index in the event collection
     *  @return a generic Svt Event index by i
     */
  public SvtEvent getEvent(int i) {
    return events.elementAt(i);
  }
    /** Number of events in the collection
     *  @return Number of events in the collection
     */
  public int getNumberOfEvents() {
    return events.size();
  }
  public boolean isEventListEmpty() {
    for (Iterator<SvtEvent> it = events.iterator(); it.hasNext(); ) {
      SvtEvent event = it.next();
      if (!event.isEmpty()) return false;
    }
    return true;
  }
  public int size() {
    return events.size();
  }
  public String getInfo() {
    StringBuilder buf = new StringBuilder(config.util.AppConstants.LARGE_BUFFER_SIZE);
    int index = 0;
    for (Iterator<SvtEvent> it = events.iterator(); it.hasNext(); ) {
      SvtEvent event = it.next();
      if (event.isEmpty()) continue;
      event.setIndex(index);
      buf.append(event);
    }
    return buf.toString();
  }
  public String toString() {
    return getInfo();
  }
  public void addEvent(final SvtEvent event) {
     events.addElement(event);
  }
  public boolean isEmpty() {
    return events.isEmpty();
  }
  public Iterator<SvtEvent> iterator() {
    return events.iterator();
  }
  public static void fillEE(Histogram hist, int ee) {
    int errBits = ee >> 9  & 0xff;  // pick up the EE Error bits 
    for (int j = 0; j < 8; j++) 
      if ((errBits >> j & 0x1) > 0) hist.fill(j);
  }
  abstract public void addEvent(final int [] words, int EE);
}
