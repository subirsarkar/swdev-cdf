package config.hist;

import com.smartsockets.TipcSvc;
import com.smartsockets.TipcException;

public class HistoMessageCreator implements SVTHistoMessage {
  public static void createMessageTypes() {
    // SVT Histograms
    try {
      TipcSvc.createMt(SVTHistoContatiner_MessageName, 
                       SVTHistoContatiner_MessageType,
                       SVTHistoContatiner_MessageGrammar);   // Histogram container msg 

      TipcSvc.createMt(SVTHisto1D_MessageName, 
                       SVTHisto1D_MessageType,
                       SVTHisto1D_MessageGrammar);           // 1D Histogram msg 

      TipcSvc.createMt(SVTHisto2D_MessageName, 
                       SVTHisto2D_MessageType,
                       SVTHisto2D_MessageGrammar);            // 2D Histogram msg 

      TipcSvc.createMt(SVTHisto2DCompressed_MessageName, 
                       SVTHisto2DCompressed_MessageType,
                       SVTHisto2DCompressed_MessageGrammar);  // 2D compressed Histogram msg 
    }
    catch (TipcException ex) {
      System.out.println(ex.getMessage());
    }
  }
}
