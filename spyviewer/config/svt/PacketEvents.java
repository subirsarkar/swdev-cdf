package config.svt;

import java.util.Iterator;

import hep.analysis.Histogram;

import org.python.util.PythonInterpreter; 
import org.python.core.PyObject;
import org.python.core.Py;

public class PacketEvents extends SvtEventsBase {
  private static final String nameStr = new String("|"); 
  private static final String [] titles = 
    {
       "EoE Error bits ",
       "No. of Packets ", 
    };
  private Histogram [] hists = new Histogram[0];
    /* Constructor */
  public PacketEvents(final int [] data) {
    super(data);
  }
    /** Add an event to the event list
     *  @param  words Svt words for that event
     *  @param  EE End of Event word 
     */
  public void addEvent(final int [] words, int EE) { 
    addEvent(new PacketEvent(words, EE));
  }
    /** Return a Packet event from the collection
     *  @param i index in the event collection
     *  @return a Packet Event index by i
     */
  public PacketEvent getPacketEvent(int i) {
    return (PacketEvent) getEvent(i);
  }
  public int getSize() {
    return hists.length;
  }
  public String [] getHistogramTitles() {
    return titles;
  }
  public void createHistograms() {
    hists = new Histogram[titles.length];
    for (int i = 0; i < titles.length; i++)
      hists[i] = new Histogram(titles[i]);
  }
  public void clearHistograms() {
    for (int i = 0; i < hists.length; i++)
      hists[i].clear();
  }
  public Histogram getHistogram(int index) {
    return hists[index];
  }
  public void fillHistograms(boolean singleShot) {
    if (hists.length == 0) 
      createHistograms();
    else if (singleShot)
      clearHistograms();
    for (Iterator<SvtEvent> it = iterator(); it.hasNext(); ) {
      PacketEvent event = (PacketEvent)it.next();
      if (event == null) throw new RuntimeException("Null event handle!");

      SvtEventsBase.fillEE(hists[0], event.getEE());
      hists[1].fill(event.getNumberOfObjects());
    }
  }
  public void fillHistogram(Histogram dHist, final String xvar, 
                                             final String yvar, 
                                             final String cutvar) 
  {
    PythonInterpreter interp = Interpreter.getInstance().getInterpreter();
    for (Iterator<SvtEvent> it = iterator(); it.hasNext(); ) {
      SvtEvent event = it.next();
      for (Iterator<SvtObject> jt = event.iterator(); jt.hasNext();) {
        Packet obj = (Packet) jt.next();
        interp.set("obj", obj);
        PyObject ab = null;
        if (!cutvar.equals("") && cutvar.length() > 0) {
          interp.exec("b = " + cutvar);
          ab = interp.get("b");
        }
        if (ab == null || Py.py2boolean(ab)) {
          interp.exec("x = " + xvar);
          PyObject xval = interp.get("x");
          if (yvar.equals("")) {
            dHist.fill(Py.py2double(xval));
          }
          else {
            interp.exec("y = " + yvar);
            PyObject yval = interp.get("y");
            dHist.fill(Py.py2double(xval), Py.py2double(yval));
          }
        }
      }
    }
  }
  public String getMatchStr() {
   return nameStr;
  }
  public static void main (String [] argv) {
    int [] words = {
       0x15af55, 0x019a11, 0x0c1351, 0x09620e, 0x084e18, 0x000800, 0x20028f, 0x6003ff
    }; 
    System.out.println(new PacketEvents(words));
  }
}
