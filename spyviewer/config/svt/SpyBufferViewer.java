package config.svt;

import java.util.Observer;
import java.util.Observable;
import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import com.smartsockets.TipcMsg;
import com.smartsockets.TipcCb;
import com.smartsockets.TipcProcessCb;
import com.smartsockets.TipcException;
import com.smartsockets.Tut;

import config.SvtCrateMap;
import config.SvtDataManager;
import config.SvtErrorDataMessage;

import config.util.AbstractMessageThread;
import config.util.AppConstants;
import config.util.Tools;

public class SpyBufferViewer extends InfoFrame {
  private MessageThread mThread = null;
  final private static ImageIcon icon = new ImageIcon(AppConstants.iconDir+"fifteenpieces.png");

  boolean connectOnStartup;
  boolean connect2RT;
  public SpyBufferViewer(boolean standAlone, boolean connect2RT, boolean connectOnStartup) {
    super(standAlone, "Individual Spy Buffer Information", connect2RT);
    this.connectOnStartup = connectOnStartup;
    this.connect2RT       = connect2RT;

    if (connectOnStartup) startMessageThread();
    setIconImage(icon.getImage());
    pack();
  }
  protected void newWindow() {
    show(false, connect2RT, connectOnStartup);
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
      setThreadToStop(true);
      setConnected(false);
      closeSrv();
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
              setStatus("Adding " + crateData + ", please wait ...");
              addNewCrate(crateData);
              setStatus("Ready ...");
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
    public class BufferObserver implements Observer {
      public BufferObserver() {}
      public void update(Observable obj, Object arg) {
        if (!(arg instanceof String)) return;
        final String crateName = (String)arg;

        if (!Tools.isCrateReady(crateName)) return;

        // Always update GUI via event handling thread
        Runnable setLabelRun = new Runnable() {
          public void run () {
            try {
              // Data has already been updated, now update the spybuffer window
              setStatus("Updating data for " + crateName + ", please wait ...");
              SpyBufferViewer.this.updateGUI(crateName);
              setStatus("Ready ...");
            }
            catch (Exception ex) {
              ex.printStackTrace();
            }
          }
        };
        SwingUtilities.invokeLater(setLabelRun);
      }   
    }
    protected void addNewCrate(final SvtCrateData crateData) {
      SvtCrateMap map = SvtCrateMap.getInstance();
      if (map.isSystemReady()) {
        warn("Crates are in place, unsubscribe from status message ...", Color.blue);
        setStatusCallback(false);
        return;
      }

      final String name = crateData.getName();
      if (map.isCrateReady(name)) return;  
      map.addEntry(crateData);

      addCrate(crateData);
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
    /** Inner class to handle SVT error related message */
    public class ProcessStatusMessage implements TipcProcessCb {
      /** 
       * Process SVT error messages when the callback is triggered
       * @param  msg   Reference to the SmartSockets message
       * @param  obj   User specified object that might be passed to the callback
       */
      SvtCrateData crateData;
      public void process(TipcMsg msg, Object arg) {
        SpyBufferViewer.this.setVisible(true);
  	try {
  	  msg.setCurrent(0);  // position the field ptr to the beginning of the message
          String sender = msg.getSender();
  	  if (isDebugOn()) System.out.println("Sender: "+ sender);
          if (sender.indexOf("b0svttest00") != -1) return;   // do not consider

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
    /** Register SVT Status callback */
    protected void setStatusCallback (boolean setCallback) {
      TipcCb pStatus = null;
      final String dest = AppConstants.STATUS_SUBJECT;
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
      final String dest = AppConstants.BUFFER_SUBJECT;
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
  protected static void show(boolean sa, boolean connect2RT, boolean connectOnStartup) {
    JFrame gui = new SpyBufferViewer(sa, connect2RT, connectOnStartup);
    gui.setSize(1020, 800);
    gui.setVisible(true);
  }
  public static void main(String [] argv) {
    boolean connect2RT = false;
    if (argv.length > 0) 
      connect2RT = (Integer.parseInt(argv[0]) > 0) ? true : false;

    boolean connectOnStartup = false;
    if (connect2RT && argv.length > 1) 
      connectOnStartup = (Integer.parseInt(argv[1]) > 0) ? true : false;

    show(true, connect2RT, connectOnStartup);
  }
}
