package config.hist;

import java.util.Observer;
import java.util.Observable;
import java.awt.Color;
import javax.swing.SwingUtilities;

import com.smartsockets.TipcCb;
import com.smartsockets.TipcMsg;
import com.smartsockets.TipcProcessCb;
import com.smartsockets.TipcException;
import com.smartsockets.Tut;

import config.util.DataFrame;
import config.util.Tools;
import config.util.AbstractMessageThread;
import config.util.AppConstants;

public class HistogramViewer extends HistogramDisplayFrame {
  private MessageThread mThread = null;

  public HistogramViewer(boolean standAlone, boolean connectOnStartup) {
    super(standAlone);
    HistoMessageCreator.createMessageTypes();
    if (connectOnStartup) startMessageThread();
  }
  protected void exitApp() {
    if (mThread != null && mThread.isConnected()) stopMessageThread();      
    dispose();          
    System.exit(0);
  }
  protected void startMessageThread() {
    super.startMessageThread();
    mThread = new MessageThread();
  }
  protected void stopMessageThread() {
    super.stopMessageThread();
    Tools.ensureEventThread();
    if (mThread == null) throw new NullPointerException("Message Thread not available!");
    mThread.halt();
  }
  public class MessageThread extends AbstractMessageThread {
    private Observer histoObserver = new HistogramObserver();
    private HistogramColl histoData = null;
    /** 
     * Initialise the Thread 
     */
    public MessageThread() {
      setHistoCallback(true);
      
      startThread();
    }
    public void halt() {
      setHistoCallback(false);
      setThreadToStop(true);
      setConnected(false);
      closeSrv();
    }
    public class HistogramObserver implements Observer {
      public HistogramObserver() {}
      public void update(Observable obj, Object arg) {
        if (!(arg instanceof HistogramColl)) return;
        final HistogramColl histoData = (HistogramColl) arg;
  
        // Always update GUI via event handling thread
        Runnable setLabelRun = new Runnable() {
          public void run () {
            try {
              HistogramViewer.this.update(histoData);
            }
            catch (Exception ex) {
              ex.printStackTrace();
            }
          }
        };
        SwingUtilities.invokeLater(setLabelRun);
        if (isDebugOn()) warn("Histogram dump:\n" + histoData);
      }
    }
    /** Inner class to handle SVT Histogram related message */
    public class ProcessHistMessage implements TipcProcessCb {
      /** 
       * Process SVT Histogram related messages when the callback is triggered
       * @param  msg   Reference to the SmartSockets message
       * @param  obj   User specified object that might be passed to the callback
       */
      public void process (TipcMsg msg, Object arg) {
  	if (isDebugOn()) warn("Histogram messages published", Color.green);
  	try {
  	  msg.setCurrent(0);  // position the field ptr to the beginning of the message
  	  int run   = msg.nextInt4();
  	  int time  = msg.nextInt4();
  	  int nhist = msg.nextMsgArray().length;   // Find how many histograms are published
  	  if (isDebugOn()) warn("# of histograms published: " + nhist, Color.blue);
  	  if (nhist > 0) {
  	    if (histoData == null) histoData = new SvtHistogramColl();
  	    histoData.fillHistogramData(msg);
  	  }
  	} catch (TipcException e) {
  	  Tut.warning(e);
  	}    
  
  	setChanged();
  	notifyObservers(histoData);
      } 
    }
    /** Register SVT histogram callback */
    protected void setHistoCallback (boolean setCallback) {
      TipcCb pHist = null;
      String dest = AppConstants.HISTO_SUBJECT;
      try {
  	if (setCallback) {
          if (!srv.getSubjectSubscribe(dest)) {
  	    ProcessHistMessage evRef = new ProcessHistMessage();
  	    pHist = srv.addProcessCb(evRef, dest, srv); 
  	    if (pHist == null) {
  	      Tut.exitFailure("WARNING. Couldn't register Histogram subject callback!\n");
  	    }
  	    srv.setSubjectSubscribe(dest, true);
  	    warn("INFO. Subscribed to  " + dest, Color.green);
  
  	    addObserver(histoObserver);
  	  }
  	} else {
  	  if (srv.getSubjectSubscribe(dest)) {
  	    srv.setSubjectSubscribe(dest, false);
  	    warn("INFO. Unsubscribed from  " +  dest, Color.red);
  
  	    deleteObserver(histoObserver);
  	  }
  	}
      } catch (TipcException Tipe) {
  	Tut.warning(Tipe);
      } 
    }
  }
  public static void main(String [] argv) {
    boolean connOnStartup = true;
    if (argv.length > 0) connOnStartup = (Integer.parseInt(argv[0]) > 0) ? true : false;

    HistogramViewer gui = new HistogramViewer(true, connOnStartup);
    gui.setSize(gui.getPreferredSize());
    gui.showCanvas();
    gui.setVisible(true);
  }
}
