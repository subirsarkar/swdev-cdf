package config;

import java.util.*;
import config.util.*;

import com.smartsockets.*;

/**
 *  <P>
 *  Decode the SmartSocket message and fill the Spy Buffer
 *  data structure for the present iteration. This object is 
 *  created only once and stays there for the whole lifetime
 *  of the application. The second thread of execution which 
 *  manages all the communications sets up a callback
 *  which will be triggered whenever Spy Buffer data is 
 *  published. 
 *  </P>
 *
 *  @author S. Sarkar
 *  @version 0.1, January 2001
 */
public class SpyDataMessage {
    /** Reference to the RT Server */
  private HashMap<String, SpyBuffer> map = new HashMap<String, SpyBuffer>();
  private String key;
  public SpyDataMessage() {}
  protected void update(TipcMsg msg) {
    TipcMsg submsg;
    try {
      int ncycles    = msg.nextInt4();
      String date    = msg.nextStr();
      String time    = msg.nextStr();
      int L1Acc      = msg.nextInt4();
      int freeze     = msg.nextInt4();
      int HFCounter  = msg.nextInt4();
      int partition  = msg.nextInt4();
      String rcState = msg.nextStr();
      int event      = msg.nextInt4();
      float rate     = msg.nextReal4();
      int run        = msg.nextInt4();
      SpyBuffer.fillStatic(ncycles, date, time, L1Acc, freeze, HFCounter,
                           partition, rcState, event, rate, run);
      while ((submsg = msg.nextMsg()) != null) {
        submsg.setCurrent(0);
    
        String crateName = submsg.nextStr();
        int slot         = submsg.nextInt4();
        String boardName = submsg.nextStr();
        String spyName   = submsg.nextStr();

        int nvalid       = submsg.nextInt4();
        int pointer      = submsg.nextInt4();    
        int wrap         = submsg.nextInt4();
        int [] data      = submsg.nextInt4Array();

        map.put(key, new SpyBuffer(crateName,  slot, boardName, boardName+"-"+slot,
                                               spyName, nvalid, pointer, wrap, data));
      }
    }
    catch (TipcException e) {
      Tut.warning(e);
    }
  }
  public HashMap<String, SpyBuffer> getMap() {
    return map;
  }
}
