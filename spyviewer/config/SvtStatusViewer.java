package config;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import com.smartsockets.*;
import config.svt.*;
import config.util.*;

public class SvtStatusViewer extends SvtCratesFrame {
  private MessageThread mThread = null;
  public SvtStatusViewer(boolean standAlone, boolean connectOnStartup) {
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
    private Observer statusObserver = new StatusObserver();
    private Observer bufferObserver = new BufferObserver();
    private SvtErrorDataMessage seDataMessage = null;
    /** 
     * Initialise the Thread 
     */
    public MessageThread() {
      setStatusCallback(true);
      setBufferCallback(true);
      
      startThread();
    }
    public void halt() {
      setStatusCallback(false);
      setBufferCallback(false);
      closeSrv();
      setThreadToStop(true);
      setConnected(false);
    }
    public class StatusObserver implements Observer {
      public StatusObserver() {}
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
              updateChain(crateData);
            }
            catch (NullPointerException ex) { 
              System.out.println("Exception " + ex.getMessage());
              ex.printStackTrace();
            }
          }
        };
        SwingUtilities.invokeLater(setLabelRun);
      }
      public void updateChain(final SvtCrateData crateData) {
   
        // Create a map with ("Crate Name", "Crate Data") 
        // Only one instance of the map should serve each and 
        // every operation with SVT Crate Status informations
        SvtCrateMap map = SvtCrateMap.getInstance();
        String crateName = crateData.getName();
        if (!map.isCrateReady(crateName)) {
          map.addEntry(crateData);
          if (isDebugOn()) warn(crateName +  ": message at " + new Date(), Color.blue);
        }
  
        // The crate status should not be opened automatically.
        // We should wait for the first crate status message(s) to arrive, dynamically
        // create the Crate status window and display status. At that point
        // It will contain only those crates which will have sent informations.
        if (!isVisible()) {
          pack();
          setVisible(true);
        }
        updateGUI(crateData);      
      }
    }
    public class BufferObserver implements Observer {
      public BufferObserver() {}
      public void update(Observable obj, Object arg) {
        if (!(arg instanceof String)) return;
        final String crateName = (String) arg; 

        if (!Tools.isCrateReady(crateName)) return;

        Runnable setLabelRun = new Runnable() {
          public void run() {
            try {
              updateBufferStatus(crateName);
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
    /** Inner class to handle SVT error related message */
    public class ProcessStatusMessage implements TipcProcessCb {
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
    /** Inner class to handle SVT event related message */
    public class ProcessBufferMessage implements TipcProcessCb {
      /** 
       * Process SVT Buffer data messages when the callback is triggered
       * @param  msg   Reference to the SmartSockets message
       * @param  obj   User specified object that might be passed to the callback
       */
      public void process (TipcMsg msg, Object arg) {
        String sender   = new String("");
  	StringBuilder sb = new StringBuilder(AppConstants.LARGE_BUFFER_SIZE);
  	String line;
  	try {
  	  msg.setCurrent(0);  // position the field ptr to the beginning of the message
  	  while (true) {
  	    line = msg.nextStr();
            if (sender.equals("") && line.startsWith("b0svt")) {
              sender = line.trim();
              if (isDebugOn()) 
                System.out.println("ProcessBufferMesssage: " + sender + " Spy Buffer message published ...");
            }
  	    if (line == null) break;
  	    sb.append(line).append("\n");
  	  }
  	} 
        catch (TipcException e) {
          // Ignore the exception as this is way to indicate EOF
  	  // Tut.warning(e);
  	}    

        SvtDataManager dataManager = SvtDataManager.getInstance();
        dataManager.updateBuffer(sb.toString());

        setChanged();
        notifyObservers(sender); 
      }
    }
    /** Register SVT Status callback */
    protected void setStatusCallback (boolean setCallback) {
      TipcCb pStatus = null;
      String dest = AppConstants.STATUS_SUBJECT;
      try {
  	if (setCallback) {
          if (!srv.getSubjectSubscribe(dest)) {
  	    ProcessStatusMessage evRef = new ProcessStatusMessage();
  	    pStatus = srv.addProcessCb(evRef, dest, srv); 
  	    if (pStatus == null) {
  	      Tut.exitFailure("WARNING. Couldn't register Svt Status subject callback!\n");
  	    }
  	    srv.setSubjectSubscribe(dest, true);
  	    warn("INFO. Subscribed to  " + dest, Color.green);
  
  	    addObserver(statusObserver);
  	  }
  	} 
        else {
  	  if (srv.getSubjectSubscribe(dest)) {
  	    srv.setSubjectSubscribe(dest, false);
  	    warn("INFO. Unsubscribed from  " +  dest, Color.red);
  
  	    deleteObserver(statusObserver);
  	  }
  	}
      } 
      catch (TipcException Tipe) {
  	Tut.warning(Tipe);
      } 
    }
    /** Register SVT buffer callback */
    protected void setBufferCallback (boolean setCallback) {
      TipcCb pBuffer = null;
      String dest = AppConstants.BUFFER_SUBJECT;
      try {
  	if (setCallback) {
          if (!srv.getSubjectSubscribe(dest)) {
  	    ProcessBufferMessage evRef = new ProcessBufferMessage();
  	    pBuffer = srv.addProcessCb(evRef, dest, srv); 
  	    if (pBuffer == null) {
  	      Tut.exitFailure("WARNING. Couldn't register Svt Buffer subject callback!\n");
  	    }
  	    srv.setSubjectSubscribe(dest, true);
  	    warn("INFO. Subscribed to  " + dest, Color.green);
  
  	    addObserver(bufferObserver);
  	  }
  	} 
        else {
  	  if (srv.getSubjectSubscribe(dest)) {
  	    srv.setSubjectSubscribe(dest, false);
  	    warn("INFO. Unsubscribed from  " +  dest, Color.red);
  
  	    deleteObserver(bufferObserver);
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

    SvtStatusViewer gui = new SvtStatusViewer(true, connOnStartup);
    gui.setSize(gui.getPreferredSize());
    // gui.setVisible(true);
  }
}