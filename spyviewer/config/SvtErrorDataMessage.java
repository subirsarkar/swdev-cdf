package config;

import java.util.*;
import java.awt.*;
import javax.swing.*;

import config.util.*;
import config.svt.*;

import com.smartsockets.*;

/**
 * <P>
 * Decode the SmartSocket message and fill the SVT Error 
 * data structure for the present iteration. This object is 
 * created only once and stays there for the whole lifetime
 * of the application. The second thread of execution, i.e the 
 * (<CODE>ReceiverThread</CODE>) which  manages all the 
 * communications sets up a callback which will be triggered 
 * whenever SVT error data is  published by any of the SVT Crates. 
 * </P>
 *
 * <P> 
 * Error update message is published on a crate basis which contains a number of
 * boards, each of which in turn contains a certain number of Spy Buffers.
 * The update method simply follows that and once a crate data is updated it
 * is updated in the <CODE>SvtErrorData</CODE> object. We also update the UI
 * for a particular crate as soon as the crate data is updated.
 * </P>
 * 
 * @author S. Sarkar
 * @version 0.1, March 16, 2001
 * @version 0.2, April 27, 2001
 */
public class SvtErrorDataMessage {
    /** Debug flag */
  private static final boolean DEBUG = false;
    /** Current crate index for which data have presently been published */
  private int currentIndex;
    /** Reference to <CODE>SvtErrorData</CODE> which contains all the informations to
     *  update <CODE>SvtCratesPanel</CODE> and much more which might be useful for 
     *  a text based display. We must understand what other information beyond what
     *  is already implemented need be stored in order to generate a useful diagnostic
     *  on error at the non-expert level. This may not be trivial at all without 
     *  affecting network trafic. 
     */
  private SvtErrorData errorData;
  public SvtErrorDataMessage() {
    errorData = new SvtErrorData(); 
  }
  public SvtErrorData getErrorData() {
    return errorData;
  }
    /** Update <CODE>SvtErrorData</CODE> structure for this crate as and when message
     *  is published for a particular crate.
     *  @param msg  Reference to the variable which contains the published message
     */
  public void update(TipcMsg msg) throws TipcException {
    currentIndex = errorData.updateCrateData(msg);

    if (DEBUG) System.out.println("The crate status: \n" + 
       errorData.getCrateData(currentIndex));
  }
  public int getCrateIndex() {
    return currentIndex;
  }
  public SvtCrateData getCrateData() {
    return errorData.getCrateData(currentIndex);
  }
}
