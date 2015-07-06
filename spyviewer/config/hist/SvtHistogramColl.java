package config.hist;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

//import java.util.*;
import com.smartsockets.TipcMsg;
import com.smartsockets.TipcException;

import config.util.Tools;
import config.util.AppConstants;

/**
 * Converts/updates histograms using the smartsockets based histogram messages.
 * Works at present only for 1- and 2-D histograms. For 2D histograms only the binned
 * histogram can be created from the SmartSockets messages.
 * @author S. Sarkar
 * @version 0.5
 */
public class SvtHistogramColl extends HistogramColl {
  public static final boolean DEBUG = false;
  /** Create the object by passing the reference of an external agent which
   *  updated the GUI after the data structure has been updated.
   */
  public SvtHistogramColl() {
    super();
  }
  /** Create histograms using data contained in a large SmartSockets message
   *  which in turn contains individual histogram informations as separate
   *  messages.
   *  @param Object  The message object
   *  @return        Number fo histograms read from the message successfully
   */
  public int fillHistogramData(Object data) throws TipcException {
    TipcMsg msg = (TipcMsg) data;

    msg.setCurrent(0);
    int run  = msg.nextInt4();
    int time = msg.nextInt4();

    TipcMsg [] submsg = msg.nextMsgArray();
    if (DEBUG) System.out.println("Total # of histograms " + submsg.length);
    for (int i = 0; i < submsg.length; i++) {
      String mname = submsg[i].getType().getName().trim();
      if (DEBUG) System.out.println("Hist #" + i + ":: " + mname);
      if (mname.equals("SVTHISTO_1D"))
        unpack1DHistogram(submsg[i]);
      else if (mname.equals("SVTHISTO_2D"))
        unpack2DHistogram(submsg[i]);
    }
    return getNHist(); // Total # of histograms added so far
  }
  /** Unpack 1D histogram message and create and fill the histogram  
   *  @param msg The SmartSockets message which contain information of the 1D histogram 
   *  @exception TipcException
   */
  public boolean unpack1DHistogram(TipcMsg msg) throws TipcException {
    msg.setCurrent(0);
    if (DEBUG) msg.print();

    // Header informations
    int hid      = msg.nextInt4();
    String title = msg.nextStr();
    int nbins    = msg.nextInt4();
    float xlow   = msg.nextReal4();
    float xhig   = msg.nextReal4();

    String xtitle = new String(title);
    String gtitle = new String(title);
    String logOption = new String("0");

    String [] fields = title.split("#");
    int len = fields.length;

    if (len > 0) xtitle = fields[0];
    if (len > 1) gtitle = xtitle + " For " + fields[1];
    if (len > 2) logOption = fields[2];

    if (DEBUG) {
      System.out.println("Title: " + title);
      System.out.println("xTitle: " + xtitle);
      System.out.println("gTitle: " + gtitle);
    }

    // Create the histogram and add to the list of histogram objects
    if (isEmpty() || indexOf(gtitle) == -1) {
      Histogram histo = new Histogram1D(hid, gtitle, nbins, xlow, xhig);
      String folder = (msg.getDest().length() > 0) ? msg.getDest() 
                                                   : AppConstants.DefaultFolder;
      histo.setFolder(folder);
      histo.setProp(msg.getUserProp());
      histo.setLogarithmic(logOption.equals("Logy") ? true : false);

      if (DEBUG) System.out.println("... 1D: dest = " + msg.getDest() + 
                                       ", folder = " + folder +
                                    ", getFolder = " + histo.getFolder());
      addObject(histo);
    }

    // Retrieve the histogram for update
    Histogram histo = getHistogram(gtitle);
    if (histo == null) 
      throw new NullPointerException("Histogram <" + gtitle + "> could not be retrieved!");

    // Number of entries, underflow overflow etc.
    int nent  = msg.nextInt4();
    int uflow = msg.nextInt4();
    int oflow = msg.nextInt4();

    histo.setEntries(nent);                  // No. of Entries
    histo.setXTitle(xtitle);

    histo.setCellContent(0, uflow);          // Underflow
    float [] cont = msg.nextReal4Array(); 
    for (int i = 0; i < Math.min(histo.getNCell(), cont.length); i++) 
       histo.setCellContent(i+1, cont[i]);   // Bin Content array
    histo.setCellContent(nbins+1, oflow);    // Overflow

    cont = msg.nextReal4Array(); 
    for (int i = 0; i < Math.min(histo.getNCell(), cont.length); i++) 
      histo.setCellError(i+1, cont[i]);      // Error Content array

    return true;
  }
  /** Unpack 2D histogram message and create and fill the histogram  
   *  @param msg The SmartSockets message which contain information of the 2D histogram 
   *  @exception TipcException
   */
  public boolean unpack2DHistogram(TipcMsg msg) throws TipcException {
    msg.setCurrent(0);
    if (DEBUG) msg.print();

    int hid        = msg.nextInt4();
    String title   = msg.nextStr();
    int [] nbins   = msg.nextInt4Array();
    float [] range = msg.nextReal4Array();

    String xtitle = new String(title);
    String ytitle = new String(title);
    String gtitle = new String(title);

    String [] fields = title.split("#");
    int len = fields.length;

    if (len > 0) ytitle = fields[0];
    if (len > 1) xtitle = fields[1];
    if (len > 2) gtitle = ytitle + " vs " + xtitle + " For " + fields[2];

    if (DEBUG) {
      System.out.println("Title: " + title);
      System.out.println("xTitle: " + xtitle);
      System.out.println("yTitle: " + ytitle);
      System.out.println("gTitle: " + gtitle);
    }

    // Create the histogram and add to the list of histogram objects
    if (isEmpty() || indexOf(gtitle) == -1) {
      Histogram2D histo = new Histogram2D(hid, gtitle, nbins[0], range[0], range[1],
                                                       nbins[1], range[2], range[3]);
      // Set other histogram properties
      String folder = (msg.getDest().length() > 0) ? msg.getDest() 
                                                   : AppConstants.DefaultFolder;
      histo.setFolder(folder);
      histo.setProp(msg.getUserProp());

      if (DEBUG) System.out.println("... 2D: dest = " + msg.getDest() + 
                                       ", folder = " + folder +
                                    ", getFolder = " + histo.getFolder());
      addObject(histo);
    }
    // Retrieve the histogram for update
    Histogram2D histo = (Histogram2D) getHistogram(gtitle);
    if (histo == null) 
      throw new NullPointerException("Histogram <" + gtitle + "> could not be retrieved!");

    int nent = msg.nextInt4();
    histo.setEntries(nent);                  // No. of Entries
    histo.setXTitle(xtitle);
    histo.setYTitle(ytitle);

    // Underflow and overflow contents
    float [] uoflow = msg.nextReal4Array();
    if (uoflow.length != 9) 
      throw new IndexOutOfBoundsException("underflow/overflow array has a wrong size!");
    histo.setCellContent(0, nbins[1]+1, uoflow[1]); 
    histo.setCellContent(nbins[0], nbins[1]+1, uoflow[2]); 
    histo.setCellContent(nbins[0]+1, nbins[1]+1, uoflow[3]); 
    histo.setCellContent(nbins[0]+1, nbins[1], uoflow[4]);
    histo.setCellContent(nbins[0]+1, 0, uoflow[5]);
    histo.setCellContent(nbins[0], 0, uoflow[6]);
    histo.setCellContent(0, 0, uoflow[7]);
    histo.setCellContent(0, nbins[1], uoflow[8]);

    // Content
    float [] cont = msg.nextReal4Array(); 
    for (int j = 0; j < nbins[1]; j++) 
      for (int i = 0; i < nbins[0]; i++) 
         histo.setCellContent(i+1, j+1, cont[i + nbins[0] * j]);   // Bin Content array

     // No error is published

    return true;
  }
  /** Create histograms using specially formatted data from an ASCII file
   *  @param file  The file object
   *  @return      Number fo histograms read from the file successfully
   */
  public int fillHistogramData(final File file)  {
    String [] attr;
    String [] data;
    int hid = 0, nbins = 0, entries = 0, underflow = 0, overflow = 0;
    float low = 0.0F, high = 0.0F; 
    
    BufferedReader input = null;
    String line;
    try {
      input = new BufferedReader(new InputStreamReader(new
                                     FileInputStream(file)));
    }
    catch (FileNotFoundException e) {
      System.out.println("The file " + file.getName() + " wasn't found!");
    }
    catch (IOException e) {
      System.out.println("Error reading from " + file.getName());
    }  
    if (input != null) {
      try {
     	while (true) {
     	  line = Tools.getNextLine(input);
          int nHist = Integer.parseInt(line);
          if (nHist <= 0) return nHist; 
          for (int i = 0; i < nHist; i++) {
       	    line = Tools.getNextLine(input);

            attr  = Tools.split(line);

            hid   = Integer.parseInt(attr[0]);
            nbins = Integer.parseInt(attr[1]);

            low  = (new Float(attr[2])).floatValue();
            high = (new Float(attr[3])).floatValue();

            entries   = Integer.parseInt(attr[4]); 
            underflow = Integer.parseInt(attr[5]); 
            overflow  = Integer.parseInt(attr[6]); 

       	    String gtitle = Tools.getNextLine(input);
            
            if (isEmpty() || indexOf(gtitle) == -1) {
              Histogram histo = new Histogram1D(hid, line, nbins, low, high);
              addObject(histo);
            }
            int index = indexOf(gtitle);
            if (index < 0) return -1;
            Histogram histo = getHistogram(index);
            if (histo == null) continue;
            
            histo.setEntries(entries);

            histo.setCellContent(0, underflow);
            for (int j = 0; j < nbins; j++) {
              line = Tools.getNextLine(input);
              data = Tools.split(line);
              histo.setCellContent(j+1,  (new Float(data[0])).floatValue());
              histo.setCellError(j+1, (new Float(data[1])).floatValue());
            }
            histo.setCellContent(nbins+1, overflow);
          }
     	}
      }
      catch (IOException e) {
     	try {   
     	  input.close();
     	} 
        catch (IOException ex) {
          System.out.println("Error closing file " + ex);
     	}
      }
      catch (NullPointerException e) {
     	try {   
     	  input.close();
     	} 
        catch (IOException ex) {
     	  System.out.println("Error closing file " + ex);
     	}
      }
      finally {
     	try {   
     	  input.close();
     	} 
        catch (IOException ex) {
     	  System.out.println("Error closing file " + ex);
     	}
      }
    }
    return getNHist();
  }
}
