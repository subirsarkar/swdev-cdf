package config.util;

import java.util.Observable;

import com.smartsockets.TipcSrv;
import com.smartsockets.TipcException;
import com.smartsockets.Tut;

abstract public class AbstractMessageThread extends Observable implements Runnable {
  protected boolean threadToStop = false;
  protected boolean connected    = false;
  protected TipcSrv srv   = Tools.getServer();
  protected Thread thread = null;
    /** 
     * Initialise the Thread 
     */
  public AbstractMessageThread() {
  }
  public abstract void halt();
  public boolean isConnected() {
    return connected;
  }
  public void startThread() {
    thread = new Thread(this, "SVT Error Messgage");
    thread.start();
    connected = true;
  }
  public void setConnected(boolean connected) {
    this.connected = connected;
  }
  public void setThreadToStop(boolean threadToStop) {
    this.threadToStop = threadToStop;
  }
  /** Override and implement the run method of <CODE>Thread</CODE> class */
  public void run() {
    Thread currThread = Thread.currentThread();
    while (thread == currThread) {
      do {
        try {
  	  srv.mainLoop(3.0); 
  	}  
        catch (TipcException te) {
  	  Tut.warning(te);
  	}
  	try {
  	  Thread.sleep((long)(Math.random() * AppConstants.N_SECONDS));
  	} 
        catch (InterruptedException e) {
  	  System.out.println("run(): Sleep interrupted :-<");
  	}
      } while (!threadToStop);
    }
  }
  /** 
   * Close connection to RT Server 
   */
  public void closeSrv() {
    if (srv != null) {
      try {
        System.out.println("INFO. Closing connection to RT Server ...");
        srv.destroy(); // disconnect from RTserver
        srv = null;       
      } 
      catch (TipcException Tipe) {
        Tut.warning(Tipe);
      }
    }
  }
}
