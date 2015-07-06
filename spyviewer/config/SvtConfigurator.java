package config;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import com.smartsockets.*;
import config.svt.*;
import config.util.*;

public class SvtConfigurator extends SvtCrateConfig {
  private MessageThread mThread = null;

  public SvtConfigurator(boolean standAlone, boolean connectOnStartup) {
    super(standAlone);
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
    private Observer errorObserver = new ErrorObserver();
    private SvtErrorDataMessage seDataMessage = null;
    /** 
     * Initialise the Thread 
     */
    public MessageThread() {
      setErrorCallback(true);
      
      startThread();
    }
    public void halt() {
      mThread.setErrorCallback(false);
      mThread.closeSrv();
      setThreadToStop(true);
      setConnected(false);
    }
    public class ErrorObserver implements Observer {
      public ErrorObserver() {}
      public void update(Observable obj, Object arg) {
        if (!(arg instanceof SvtCrateData)) return;
        final SvtCrateData crateData = (SvtCrateData) arg;

        // Do not update GUI directly but put it in the event queue
        // This is the safest way when using Swing in a threaded application.
        // After a widget has been made visible, only the event handling
        // thread which is started automatically by the JVM is allowed to
        // modify it, this is a feature which is there to optimize performance with
        // Swing components.
        Runnable setLabelRun = new Runnable() {
          public void run() {
            try {
              updateGUI(crateData);
            }
            catch (NullPointerException ex) { 
              System.out.println("Exception " + ex.getMessage());
              ex.printStackTrace();
            }
          }
        };
        SwingUtilities.invokeLater(setLabelRun);
      }
    }
    protected void updateGUI(final SvtCrateData crateData) {
      SvtCrateMap map = SvtCrateMap.getInstance();
      if (map.isSystemReady()) {
        warn("Crates are in place, no need to remain connected to RT Server..", Color.blue);
        stopMessageThread();          
        return;
      }

      final String name = crateData.getName();
      if (map.isCrateReady(name)) return;  
      map.addEntry(crateData);

      SvtConfigurator.this.updateGUI(crateData);
    }
    /** Inner class to handle SVT error related message */
    public class ProcessErrorMessage implements TipcProcessCb {
      /** 
       * Process SVT error messages when the callback is triggered
       * @param  msg   Reference to the SmartSockets message
       * @param  obj   User specified object that might be passed to the callback
       */
      SvtCrateData crateData;
      public void process (TipcMsg msg, Object arg) {
  	try {
  	  msg.setCurrent(0);  // position the field ptr to the beginning of the message
          String sender = msg.getSender();
  	  if (isDebugOn()) System.out.println("Sender: "+ sender);
          if (sender.indexOf("b0svttest00") > -1) return;

  	  if (seDataMessage == null) 
  	     seDataMessage = new SvtErrorDataMessage();
  	  seDataMessage.update(msg);
          crateData = seDataMessage.getCrateData();
  	} 
        catch (TipcException e) {
  	  Tut.warning(e);
  	}    

  	setChanged();
  	notifyObservers(crateData);
      } 
    }
    /** Register SVT histogram callback */
    protected void setErrorCallback (boolean setCallback) {
      TipcCb pError = null;
      String dest = AppConstants.STATUS_SUBJECT;
      try {
  	if (setCallback) {
          if (!srv.getSubjectSubscribe(dest)) {
  	    ProcessErrorMessage evRef = new ProcessErrorMessage();
  	    pError = srv.addProcessCb(evRef, dest, srv); 
  	    if (pError == null) {
  	      Tut.exitFailure("WARNING. Couldn't register Svt Error subject callback!\n");
  	    }
  	    srv.setSubjectSubscribe(dest, true);
  	    warn("INFO. Subscribed to  " + dest, Color.green);
  
  	    addObserver(errorObserver);
  	  }
  	} 
        else {
  	  if (srv.getSubjectSubscribe(dest)) {
  	    srv.setSubjectSubscribe(dest, false);
  	    warn("INFO. Unsubscribed from  " +  dest, Color.red);
  
  	    deleteObserver(errorObserver);
  	  }
  	}
      } 
      catch (TipcException Tipe) {
  	Tut.warning(Tipe);
      } 
    }
  }
  public static void main(String [] argv) {
    boolean connOnStartup = true;
    if (argv.length > 0) connOnStartup = (Integer.parseInt(argv[0]) > 0) ? true : false;

    SvtConfigurator gui = new SvtConfigurator(true, connOnStartup);
    gui.setSize(gui.getPreferredSize());
    gui.setVisible(true);
  }
}