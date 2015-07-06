package config.svt;

import java.util.Iterator;

import hep.analysis.Histogram;

import org.python.util.PythonInterpreter; 
import org.python.core.PyObject;
import org.python.core.Py;

import config.util.AppConstants;

public class RoadEvents extends SvtEventsBase {
  private static final String nameStr 
   = new String("wedge|addr|amBoard|amPlug|roadID|amChip|pattern"); 
  private static final String [] titles = 
    {
       "EoE Error bits ",
       "No. of Roads ", 
       "Wedge ", 
       "Address ", 
       "AmBoard ", 
       "AmPlug ", 
       "Road ID ", 
       "Chip ",
       "Pattern ",
    };
  private Histogram [] hists = new Histogram[0];

    /* Constructor */
  public RoadEvents(final int [] data) {
    super(data);
  }
    /** Add an event to the event list
     *  @param  words Svt words for that event
     *  @param  EE End of Event word 
     */
  public void addEvent(final int [] words, int EE) { 
    addEvent(new RoadEvent(words, EE));
  }
    /** Return a Road event from the collection
     *  @param i index in the event collection
     *  @return a Road Event index by i
     */
  public RoadEvent getRoadEvent(int i) {
    return (RoadEvent) getEvent(i);
  }
  public String toString() {
    StringBuilder buf = new StringBuilder(config.util.AppConstants.LARGE_BUFFER_SIZE);
    if (!isEventListEmpty()) {
      buf.append(AppConstants.s8Format.sprintf("Road")).append(Road.getBanner());
      buf.append(super.getInfo());
    }
    return buf.toString();
  }
  public String [] getHistogramTitles() {
    return titles;
  }
  public int getSize() {
    return hists.length;
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
      RoadEvent event = (RoadEvent)it.next();
      if (event == null) throw new RuntimeException("Null event handle!");

      SvtEventsBase.fillEE(hists[0], event.getEE());
      hists[1].fill(event.getNumberOfObjects());
	
      for (Iterator<SvtObject> jt = event.iterator(); jt.hasNext();) {
        Road road = (Road) jt.next();
        hists[2].fill(road.getWedge());
        hists[3].fill(road.getAddr());
        hists[4].fill(road.getAmBoard());
        hists[5].fill(road.getAmPlug());
        hists[6].fill(road.getRoadID());
        hists[7].fill(road.getAmChip());
        hists[8].fill(road.getPattern());
      }
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
        Road obj = (Road) jt.next();
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
    int [] words = {0x2e04cc, 0x2e0f31, 0x600085};
    System.out.println(new RoadEvents(words));
  }
}
