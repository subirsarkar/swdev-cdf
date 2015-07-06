import java.io.*;
import rc.*;
import daqmsg.*;

import HistoMessage.*;

/**
  *   Definition of a collection of histogram messages which is to be 
  *   passed between the crates and the Spy manager
  *      
  *   @verison 0.1
  *   @author  S. Sarkar 9/28/00
  */
public class HistoCollectionMessage extends daqmsg.DaqMsg implements Defs, Serializable {

  public static final int MAX_HIST = 200;
  public int nhist;
  public HistoMessage [] histos;
  
  public static int MESSAGE_TYPE = 4005;

  /* No-arg constructor required for reflection to work */
  public HistoCollectionMessage()  {
    super();
    messageType = MESSAGE_TYPE ;

    nhist  = MAX_HIST;
    histos = new HistoMessage[nhist];
  }

  public void print() {
    System.out.println("HistoCollectionMessage: Histogram atributes");
    for (int i = 0; i < histos.length; i++) {
      System.out.println("Histogram = " + i);
      System.out.println("     ID = " + histos[i].hid);
      System.out.println("     Title = " + histos[i].title);
      System.out.println("     # of bins = " + histos[i].nbins);
      System.out.println("     Lower edge = " + histos[i].low);
      System.out.println("     Upper edge = " + histos[i].high);
      System.out.println("     Entries = " + histos[i].nEntries);
      System.out.println("     Underflow = " + histos[i].nUnderflow);
      System.out.println("     Overflow = " + histos[i].nOverflow);
      System.out.println("     Content and errors ");
      for (int j = 0; j < histos[i].nbins; j++) {
        System.out.println(histos[i].content[j] + " " + histos[i].error[j]);
      }
    }
  }
}
