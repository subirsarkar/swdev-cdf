package config;

import java.util.*;
import java.io.*;

import config.svt.*;
import config.util.*;

public class SvtDataManager implements SvtDataMining {
  private static SvtDataManager _instance = null;
  private SvtDataManager() {}
   /** 
    * Singleton constructor. If the class is already instantiated
    * returns the instance, else creates it. Only one object of SvtCrateMap
    * class manages all the interactions using static methods.
    *
    * @return  An instance of <CODE>SvtCrateMap</CODE> class
    */
  public static synchronized SvtDataManager getInstance() {
    if (_instance == null) {
      _instance = new SvtDataManager();
    }
    return _instance;
  }
  public SvtCrateData getCrateData(final String crate) {
    SvtCrateMap map = SvtCrateMap.getInstance();
    return map.getCrateData(crate);
  }
  public SvtBoardData getBoardData(final String crate, 
                                   int slot, 
                                   final String board) 
       throws NullPointerException
  {
    SvtCrateData crateData = getCrateData(crate);
    if (crateData == null)
       throw new NullPointerException("Data for crate "+crate+" not found!");

    return crateData.getBoardData(board, slot);
  }
  public SvtBufferData getBufferData(final String crate, int slot,
                                     final String board, final String buffer)
       throws NullPointerException
  {
     SvtBoardData boardData = getBoardData(crate, slot, board);
     if (boardData == null)
       throw new NullPointerException("Data for crate/slot/board "+crate 
                                     +"/" + slot + "/" + board+" not found!");

     return boardData.getBufferData(buffer);
  }
  public synchronized void updateBuffer(final String text) {
    String [] lines = text.split("\\n");
    updateBuffer(lines);    
  }
  public synchronized void updateBuffer(final String [] lines) {
    // Now update Spy Buffer data arrays
    // At present do it here
    int [] indices = new int[AppConstants.MAXBUF];
    for (int i = 0; i < indices.length; i++) 
      indices[i] = -1;
    
    // Find starting position of individual buffers
    int nbuf = 0;
    for (int i = 0; i < lines.length; i++) {
      if (lines[i].startsWith("SB")) {
        indices[nbuf++] = i;
        if (nbuf == AppConstants.MAXBUF) break;
      }
    }
    updateBuffer(lines, indices);    
  }
  public synchronized void updateBuffer(final Vector<String> list, final Vector<Integer> indexList) {
    String [] lines = new String[list.size()];
    list.toArray(lines);

    int [] indices = new int[indexList.size()+1];
    int nx = 0;
    for (Iterator<Integer> it = indexList.iterator(); it.hasNext(); )
      indices[nx++] = it.next().intValue();
    indices[indices.length-1] = -1;

    updateBuffer(lines, indices);
  }
  public synchronized void updateBuffer(final String [] lines, final int [] indices) {
    // Now fill buffer data
    for (int i = 0; i < indices.length; i++)  {
      int idx = indices[i];
      if (idx == -1) break;

      int jdx = indices[i+1];  // number of lines belonging to a buffer
      if (jdx == -1) jdx = lines.length;

      String [] fields = Tools.split(lines[idx]); // This is the SB line
      String crate = fields[1];
      if (!SvtCrateMap.getInstance().isCrateReady(crate)) return;

      int slot = Integer.parseInt(fields[2]);
      String board = fields[3];
      String spy   = fields[4];

      // The following 3 values might also be taken from elsewhere
      int nvalid = Integer.parseInt(fields[5]);
      String p = fields[6];
      String plus  = p.substring(p.length()-1);
      int pointer, wrap = 0;
      if (plus.equals("+")) {
        pointer  = Integer.parseInt(p.substring(0,p.length()-1));
        wrap     = 1;
      }
      else 
        pointer  = Integer.parseInt(p);

      Vector<Integer> vec = new Vector<Integer>(AppConstants.MAXBUF);
      for (int j = idx+1; j < jdx; j++) {   // DA lines
        String [] data = Tools.split(lines[j]);
        for (int k = 1; k < data.length; k++) {  // "DA" is ignored
          try {
	    vec.addElement(Integer.parseInt(data[k], 16));
          }
          catch (NumberFormatException e) {
            System.out.println("Unrecognized format " + e.getMessage());
            e.printStackTrace();
          }
	}
      }
    
      try {
        SvtBufferData bufferData = getBufferData(crate, slot, board, spy);
        bufferData.fillData(vec);
        if (false) System.out.println(bufferData.toString());
      }
      catch (NullPointerException ex) {
        System.out.println("Buffer data cannot be accesses, " + ex.getMessage());
        ex.printStackTrace();
      }
    }
  }
}
