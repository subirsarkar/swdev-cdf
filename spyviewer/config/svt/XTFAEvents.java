package config.svt;

import java.util.Iterator;

import hep.analysis.Histogram;

import org.python.util.PythonInterpreter; 
import org.python.core.PyObject;
import org.python.core.Py;

import config.util.AppConstants;

public class XTFAEvents extends SvtEventsBase {
  private static final double MAXPT = 20.;
  private static final String nameStr 
     = new String("phirad|curvature|pt|phi|wedge|miniwedge|miniphi");
  private static final String [] titles = 
    {
       "EoE Error bits ",
       "No. of Tracks ", 
       "Phi ", 
       "Curvature ", 
       "Transverse Momentum ", 
       "Phibin ", 
       "Wedge ",
       "MiniWedge ",
       "MiniPhi ",
       "Isolation bit ",
       "Short Bit "
    };
  private Histogram [] hists = new Histogram[0];
    /* Constructor */
  public XTFAEvents(final int [] data) {
    super(data);
  }
    /** Add an event to the event list
     *  @param  words Svt words for that event
     *  @param  EE End of Event word 
     */
  public void addEvent(final int [] words, int EE) {
    addEvent(new XTFAEvent(words, EE));
  }
    /** Return a XTFA event from the collection
     *  @param i index in the event collection
     *  @return a XTFA Event index by i
     */
  public XTFAEvent getXTFAEvent(int i) {
    return (XTFAEvent) getEvent(i);
  }
  public String toString() {
    return getInfo();
  }
  public String getInfo() {
    StringBuilder buf = new StringBuilder(config.util.AppConstants.LARGE_BUFFER_SIZE);
    if (!isEventListEmpty()) {
      buf.append(AppConstants.s8Format.sprintf("Trk")).append(XftTrack.getBanner());
      buf.append(super.getInfo());
    }
    return buf.toString();
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
      XTFAEvent event = (XTFAEvent)it.next();
      if (event == null) throw new RuntimeException("Null event handle!");

      SvtEventsBase.fillEE(hists[0], event.getEE());
      hists[1].fill(event.getNtrk());
	
      for (Iterator<SvtObject> jt = event.iterator(); jt.hasNext();) {
        XftTrack track = (XftTrack)jt.next();
        double phi  = track.getPhirad();
        double curv = track.getCurvature();
        double pt   = track.getPt();
        int sign = (pt < 0.0) ? -1 : 1;
        if (Math.abs(pt) > MAXPT) pt = sign*MAXPT;
        int phibin  = track.getPhi();
        int wedge   = track.getWedge();
        int mwed    = track.getMiniwedge();
        int mphi    = track.getMiniphi();
        int isoBit  = track.isIsolated() ? 1 : 0;
        int shrtBit = track.isShort() ? 1 : 0;

        hists[2].fill(phi);
        hists[3].fill(curv);
        hists[4].fill(pt);
        hists[5].fill(phibin);
        hists[6].fill(wedge);
        hists[7].fill(mwed);
        hists[8].fill(mphi);
        hists[9].fill(isoBit);
        hists[10].fill(shrtBit);
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
        XftTrack obj = (XftTrack) jt.next();
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
        0x15af55, 0x019a11,  0x0c1351,   0x09620e,   0x084e18,  0x000800,   0x20028f,
        0x15af55, 0x019a11,  0x0c1351,   0x09620e,   0x084e18,  0x000800,   0x20028f,
        0x15af55, 0x019a11,  0x0c1351,   0x09620e,   0x084e18,  0x000800,   0x20028f,
        0x15af55, 0x019a11,  0x0c1351,   0x09620e,   0x084e18,  0x000800,   0x20028f,
        0x15af55, 0x019a11,  0x0c1351,   0x09620e,   0x084e18,  0x000800,   0x20028f,
        0x15af55, 0x019a11,  0x0c1351,   0x09620e,   0x084e18,  0x000800,   0x20028f,       
        0x15af55, 0x019a11,  0x0c1351,   0x09620e,   0x084e18,  0x000800,   0x20028f,
        0x6003ff
    }; 
    System.out.println(new XTFAEvents(words));
  }
}
