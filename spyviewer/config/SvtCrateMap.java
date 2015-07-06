package config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import config.svt.SvtCrateData;
import config.util.AppConstants;
import config.util.Tools;

/**
 *  Create a Singleton class which holds the crate data message
 *  in a map as ("Crate Name", Crate Data). This map can subsequently
 *  be used to create 
 *  - Svt Crate Status Panel 
 *  - Svt Configurator Panel
 *  etc. dynamically.
 *
 *  @author S. Sarkar
 *  @version 0.1   10/2001
 */
public class SvtCrateMap {
  private HashMap<String, SvtCrateData> map;
  private boolean [] crateReady;
  private boolean [] crateShowing;
  public static int nCrateReady = 0;
   /** Static reference to <CODE>this</CODE> */
  private static SvtCrateMap _instance = null;
   /**
    * The constructor could be made private
    * to prevent others from instantiating this class.
    * But this would also make it impossible to
    * create instances of Singleton subclasses.
    */
  private SvtCrateMap() {
    map          = new HashMap<String, SvtCrateData>();
    crateReady   = new boolean[AppConstants.nCrates];
    crateShowing = new boolean[AppConstants.nCrates];
  }
   /** 
    * Singleton constructor. If the class is already instantiated
    * returns the instance, else creates it. Only one object of SvtCrateMap
    * class manages all the interactions using static methods.
    *
    * @return  An instance of <CODE>SvtCrateMap</CODE> class
    */
  public static synchronized SvtCrateMap getInstance() {
    if (_instance == null) {
      _instance = new SvtCrateMap();
    }
    return _instance;
  }
  public boolean isCrateReady(int index) {
    return crateReady[index];
  }
  public void setCrateReady(int index, boolean isReady) {
    crateReady[index] = isReady;
  }
  public boolean isCrateReady(final String crate) {
    return isCrateReady(Tools.getCrateIndex(crate));
  }
  public boolean isCrateShowing(int index) {
    return crateShowing[index];
  }
  public synchronized void setCrateShowing(int index, final boolean isShowing) {
    crateShowing[index] = isShowing;
  }
  public boolean isCrateShowing(final String crate) {
    return isCrateShowing(Tools.getCrateIndex(crate));
  }
  public synchronized void addEntry(final SvtCrateData crateData) {
    map.put(crateData.getName(), crateData);
    nCrateReady++;
    crateReady[Tools.getCrateIndex(crateData.getName())] = true;
  }
  public SvtCrateData getCrateData(final String crate) {
    return map.get(crate);
  }
  public synchronized int size() {
    return map.size();
  }
  public Set<Map.Entry<String, SvtCrateData>> entrySet() {
    return map.entrySet();
  }
  public boolean isSystemReady(int nCrates) {
    if (nCrateReady == nCrates) return true;
    return false;
  }
  public boolean isSystemReady() {
    return isSystemReady(AppConstants.nCrates);
  }
  public String toString() {
    StringBuilder str = new StringBuilder(AppConstants.LARGE_BUFFER_SIZE);
    str.append(map);

    return str.toString();
  }
}
