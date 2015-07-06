import java.io.*;
import rc.*;
import daqmsg.*;

/**
  *  SVT data structure
  *      
  *  @verison 0.1
  *  @author  S. Sarkar 4/12/00
  */
public class EventFrac extends daqmsg.DaqMsg implements Defs, Serializable {
  public int partition;
  public String sender;
  public String state;
  public String description;
  public int [] words;
  public int endEvent;

  public static int MESSAGE_TYPE = 4001;

  /** No-arg constructor */
  public EventFrac() {
    initialise();
    words    = new int[0];
    endEvent = -1;     
  }
  /** 
   * Simple initialisation of the object
   *
   * @param  words      SVTword array
   * @param  endEvent   End event word
   * @return Object of type EventFrac which hold SVT words and endWord
   */
  public EventFrac(int [] words, int endEvent) {
    initialise();
    this.words    = words;
    this.endEvent = endEvent;      
  }
  void initialise() {
    messageType = MESSAGE_TYPE ;
    partition   = NOT_A_PARTITION;
    sender      = "nobody";
    state       = NOT_A_STATE;
    description = "SVT event fraction";
  }
  public String messageKey() {return super.messageKey() + sender;}
}
