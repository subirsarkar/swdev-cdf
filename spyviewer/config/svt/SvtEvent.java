package config.svt;

import java.util.Vector;
import java.util.Iterator;
import config.util.AppConstants;

/** Define  a structure to hold SVT words */
abstract public class SvtEvent {
  private final static int INIT_SIZE  = 100;
  private Vector<SvtObject> objects = new Vector<SvtObject>(INIT_SIZE);
  /** SVT word array */
  private int [] words;
  /** End Event word */
  private int endEvent;
  private int index;
   
  /** 
   * Simple initialisation of the object
   *
   * @param  words      SVTword array
   * @param  endEvent   End event word
   */
  public SvtEvent(final int [] words, int endEvent) {
    setWords(words);
    setEE(endEvent);      

    objects.removeAllElements();
    addObjects(words);
  }
  public void setWords(final int [] words) {
    this.words = new int[words.length];
    System.arraycopy(words, 0, this.words, 0, this.words.length);
  }
  protected int [] getWords() {
    return words;
  }
  public void setEE(int endEvent) {
    this.endEvent = endEvent;
  }
  public int getEE() {
    return endEvent;
  }
  public boolean isEmpty() {
    return objects.isEmpty();
  }
  public void addObject(SvtObject obj) {
    objects.addElement(obj);
  }
  public void removeObject(SvtObject obj) {
    objects.removeElement(obj);
  }
  public int getNumberOfObjects() {
    return objects.size();
  }
  public Object getObject(int i) {
    return objects.elementAt(i);
  }
  public Iterator<SvtObject> iterator() {
    return objects.iterator();
  }
  public String toString() {
    StringBuilder buf = new StringBuilder(config.util.AppConstants.MEDIUM_BUFFER_SIZE);
    buf.append("Ev ").append(index).append(" ==\n");
    int nObj = 0;
    for (Iterator<SvtObject> it = objects.iterator(); it.hasNext();) {
      SvtObject obj = it.next();
      buf.append(AppConstants.d8Format.sprintf(++nObj)).append(obj);
    }
    return buf.toString();
  }
  public void setIndex(int index) {
    this.index = index;
  }
  public int getIndex() {
    return index;
  }
  public abstract void addObjects(final int [] data);
}
