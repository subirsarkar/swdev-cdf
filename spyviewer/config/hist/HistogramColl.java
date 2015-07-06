package config.hist;

import java.util.Vector;
import java.util.Iterator;
import com.smartsockets.TipcException;

import config.util.AppConstants;

/** Book-keeping of histograms, provides a container and add,remove
 *  methods.
 *  @author S. Sarkar
 *  @version  1.0
 */
public abstract class HistogramColl {
  private static final int INIT_SIZE = 1000;
  protected Vector<Histogram> histoList = new Vector<Histogram>(INIT_SIZE);
  public HistogramColl() {}
  public abstract int fillHistogramData(Object data) throws TipcException;
  /** Add a histogram object to the container 
   *  @param hist  Reference to the histogram object
   */
  public void addObject(Histogram hist) {
    histoList.addElement(hist);
  }
  /** Remove a histogram object from the container 
   *  @param hist  Reference to the histogram object
   */
  public void removeObject(Histogram hist) {
    histoList.removeElement(hist);
  }
  /** Check if the list contains the histogram
   *  @param hist  Reference to the histogram object
   *  @return true if the list contains the histogram object, false otherwise
   */
  public boolean contains(Histogram hist) {
    return histoList.contains(hist);
  }
  /** Get the position of the histogram in the list
   *  @param id histogram ID
   *  @return histogram index in the list
   */
  public int indexOf(int id) {
    for (Iterator<Histogram> it = histoList.iterator(); it.hasNext(); ) {
      Histogram hist = it.next();
      if (hist != null && hist.getHid() == id) return histoList.indexOf(hist);
    }
    return -1;
  }
  /** Get the position of the histogram in the list
   *  @param title histogram title
   *  @return histogram index in the list
   */
  public int indexOf(final String title) {
    for (Iterator<Histogram> it = histoList.iterator(); it.hasNext(); ) {
      Histogram hist = it.next();
      if (hist != null && hist.getTitle().equals(title)) 
        return histoList.indexOf(hist);
    }
    return -1;
  }
  /** Get number of histograms present in the list
   *  @return Number of histograms
   */
  public int getNHist() {
    return histoList.size();
  }
  /** Get reference to the list itself
   *  @return the list itself
   */
  public Vector<Histogram> getHistogramList() {
    return histoList;
  }
  /** Get the i-th histogram object 
   *  @param index  i-th index
   *  @return  the i-th histogram object or null
   */
  public Histogram getHistogram(int index) {
    return histoList.elementAt(index);
  }
  /** Get the i-th histogram object 
   *  @param title  histogram title
   *  @return  the i-th histogram object or null
   */
  public Histogram getHistogram(final String title) {
    int index = indexOf(title);
    if (index < 0) return null;

    return getHistogram(index);
  }
  /** Check if the histogram list is empty
   *  @retrun true if the list is empty, true otherwise
   */
  public boolean isEmpty() {
    return histoList.isEmpty();
  }
  /** A custom string representation of the <CODE>HistogramColl</CODE> object
   *  @retrun A custom string representation of the <CODE>HistogramColl</CODE> object
   */
  public String toString() {
    StringBuilder buf = new StringBuilder(AppConstants.LARGE_BUFFER_SIZE);

    buf.insert(0, histoList.size());
    buf.append("\n" + histoList.toString());

    return buf.toString();
  }
}
