package config.svt;

import java.util.Iterator;

import hep.analysis.Histogram;

import org.python.util.PythonInterpreter; 
import org.python.core.PyObject;
import org.python.core.Py;

import config.util.AppConstants;

public class TFEvents extends SvtEventsBase {
  private static final double MAXPT = 20.;
  private static final boolean DEBUG = true;
  private static final String nameStr 
    = new String("phi0|d0|chi2|curv|pt|zin|zout|road|wedge");
  private static final String [] titles = 
    {
       "EoE Error bits ",
       "No. of Tracks ", 
       "Phi0 ", 
       "Impact Parameter ", 
       "Curvature ", 
       "Transverse Momentum ", 
       "Chi2 ", 
       "Wedge ",
       "Z in ",
       "Z out ",
       "Road Number ",
       "XFT Number ",
       "Hit(0) ",
       "Hit(1) ",
       "Hit(2) ",
       "Hit(3) ",
       "Hit(4) ",
       "QFit ",
       "TF Status ",
       "TF Error ",
       "d0 vs phi0 "
    };
  private Histogram [] hists = new Histogram[0];

    /* Constructor */
  public TFEvents(final int [] data) {
    super(data);
  }
    /** Add an event to the event list
     *  @param  words Svt words for that event
     *  @param  EE End of Event word 
     */
  public void addEvent(final int [] words, int EE) { 
    addEvent(new TFEvent(words, EE));
  }
    /** Return a TF event from the collection
     *  @param i index in the event collection
     *  @return a TF Event index by i
     */
  public TFEvent getTFEvent(int i) {
    return (TFEvent) getEvent(i);
  }
  public String toString() {
    StringBuilder buf = new StringBuilder(config.util.AppConstants.LARGE_BUFFER_SIZE);
    if (!isEventListEmpty()) {
      buf.append(AppConstants.s8Format.sprintf("Trk")).append(SvtTrack.getBanner());
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
      TFEvent event = (TFEvent)it.next();
      if (event == null) throw new RuntimeException("Null TFEvent handle!");

      SvtEventsBase.fillEE(hists[0], event.getEE());
      hists[1].fill(event.getNtrk());
	
      for (Iterator<SvtObject> jt = event.iterator(); jt.hasNext();) {
        SvtTrack track = (SvtTrack) jt.next();
        double phi0 = track.getPhi0();
        double d0   = track.getD0();
        hists[2].fill(phi0);
        hists[3].fill(d0);
        hists[4].fill(track.getCurv());
        double pt = track.getPt();
        int sign  = (pt < 0.0) ? -1 : 1;
        if (Math.abs(pt) > MAXPT) pt = sign*MAXPT;
        hists[5].fill(pt);
        hists[6].fill(track.getChi2());
        hists[7].fill(track.getWedge());
        hists[8].fill(track.getZin());
        hists[9].fill(track.getZout());
        hists[10].fill(track.getRoad());
        hists[11].fill(track.getXftNumber());
        for (int i = 0; i < 5; i++) 
          hists[12+i].fill(track.getHit(i));
        hists[17].fill(track.getTFStatus());
        hists[18].fill(track.getFitQuality());
        hists[19].fill(track.getTFError());
        hists[20].fill(phi0, d0);
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
        SvtTrack obj = (SvtTrack) jt.next();
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
    System.out.println(new TFEvents(words));
  }
}
