import java.io.*;
import rc.*;
import daqmsg.*;

/**
  *   Define a message of type SVTEvent
  *      
  *   @verison 0.1
  *   @author  S. Sarkar 4/12/00
  */
public class SVTEventMessage extends daqmsg.DaqMsg implements Defs, Serializable {
  public int partition;
  public String sender;
  public String state;
  public String description;
  public EventFrac event;

  public static int MESSAGE_TYPE = 4002;

  // temporary stuff
  int [] wordArray = {101,102,103,104,105,106}; 
  int endEv        = 107;

  /* No-arg constructor required for reflection to work */
  public SVTEventMessage()  {
    messageType = MESSAGE_TYPE ;
    partition   = NOT_A_PARTITION;
    sender      = "nobody";
    state       = NOT_A_STATE;
    description = "This message contains SVT event fraction";
    event       = new EventFrac(wordArray,endEv);
  }

  public String messageKey() {return super.messageKey() + sender;}
}
