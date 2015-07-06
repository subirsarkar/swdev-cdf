import java.io.*;
import rc.*;
import daqmsg.*;

/**
  *   Definition of a histogram message which is to be 
  *   passed between the crates and the Spy manager
  *      
  *   @verison 0.1
  *   @author  S. Sarkar 9/24/00
  */
public class HistoMessage extends daqmsg.DaqMsg implements Defs, Serializable {
  public static final int MAX_BIN = 100;
  public int hid;
  public String title;
  public int nbins;
  public float low;
  public float high;
  public int nEntries;
  public int nUnderflow;
  public int nOverflow;
  public float [] content = new float[MAX_BIN];
  public float [] error   = new float[MAX_BIN];
  
  public static int MESSAGE_TYPE = 4004;

  /* No-arg constructor required for reflection to work */
  public HistoMessage()  {
    super();
    messageType = MESSAGE_TYPE ;
    hid = -1;
    title = " ";
    nbins = MAX_BIN;
    low   = 0.0F;
    high  = 1.0F;
    nEntries = 0;
    nUnderflow = 0;
    nOverflow  = 0;
    for (int i = 0; i < MAX_BIN; i++) {
      content[i] = 0.0F;
      error[i]   = 0.0F;
    }
  }

  public void print() {
    System.out.println("HistoMessage: Histogram atributes");
    System.out.println("     ID = " + hid);
    System.out.println("     Title = " + title);
    System.out.println("     # of bins = " + nbins);
    System.out.println("     Lower edge = " + low);
    System.out.println("     Upper edge = " + high);
    System.out.println("     Entries = " + nEntries);
    System.out.println("     Underflow = " + nUnderflow);
    System.out.println("     Overflow = " + nOverflow);
    System.out.println("     Content and errors ");
    for (int i = 0; i < nbins; i++) {
      System.out.println(content[i] + " " + error[i]);
    }
  }
}
