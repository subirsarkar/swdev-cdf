package config.util;

import java.awt.Font;
import javax.swing.ImageIcon;

/**
  *  Define Global constants   
  * 
  *  @author  S. Sarkar
  *  @version 0.1       12/7/99
  */
public class AppConstants {
  public final static PrintfFormat  d2Format = new PrintfFormat("%2d"),
                                    d3Format = new PrintfFormat("%3d"),
                                    d4Format = new PrintfFormat("%4d"),
                                    d5Format = new PrintfFormat("%5d"),
                                    d6Format = new PrintfFormat("%6d"),
                                    d8Format = new PrintfFormat("%8d"),
                                   d10Format = new PrintfFormat("%10d"),
                                    h3Format = new PrintfFormat("%3.3x"),
                                    h40Format = new PrintfFormat("%4x"),
                                    h4Format = new PrintfFormat("%4.4x"),
                                    h5Format = new PrintfFormat("%5x"),
                                    h60Format = new PrintfFormat("%6x"),
                                    h64Format = new PrintfFormat("%6.4x"),
                                    h65Format = new PrintfFormat("%6.5x"),
                                    h6Format = new PrintfFormat("%6.6x"),
                                    h8Format = new PrintfFormat("%8.6x"),
                                    h124Format = new PrintfFormat("%12.4x"),
                                   f50Format = new PrintfFormat("%5.0f"),
                                   f52Format = new PrintfFormat("%5.2f"),
                                   f63Format = new PrintfFormat("%6.3f"),
                                   f70Format = new PrintfFormat("%7.0f"),
                                   f72Format = new PrintfFormat("%7.2f"),
                                   f73Format = new PrintfFormat("%7.3f"),
                                   f82Format = new PrintfFormat("%8.2f"),
                                   f83Format = new PrintfFormat("%8.3f"),
                                   f85Format = new PrintfFormat("%8.5f"),
                                   f90Format = new PrintfFormat("%9.0f"),
                                   f93Format = new PrintfFormat("%9.3f"),
                                  f106Format = new PrintfFormat("%10.6f"),
                                    s3Format = new PrintfFormat("%3s"),
                                    s4Format = new PrintfFormat("%4s"),
                                    s5Format = new PrintfFormat("%5s"),
                                    s6Format = new PrintfFormat("%6s"),
                                    s7Format = new PrintfFormat("%7s"),
                                    s8Format = new PrintfFormat("%8s"),
                                    s9Format = new PrintfFormat("%9s"),
                                   s10Format = new PrintfFormat("%10s"),
                                   s12Format = new PrintfFormat("%12s"),
                                   s32Format = new PrintfFormat("%32s");

  public static final String SVT_CRATE_PREFIX = "b0svt0";
  /** Sleep for 10 seconds before looking for message again */
  public static final int N_SECONDS = 10000;
  public static final int MAXBUF = 90;

  public static final int 
    MASK01 = 0x1,   
    MASK02 = 0x3,   
    MASK03 = 0x7,   
    MASK04 = 0xf,   
    MASK05 = 0x1f,   
    MASK06 = 0x3f,   
    MASK07 = 0x7f,   
    MASK08 = 0xff,   
    MASK09 = 0x1ff,   
    MASK10 = 0x3ff,   
    MASK11 = 0x7ff,   
    MASK12 = 0xfff,
    MASK13 = 0x1fff,
    MASK14 = 0x3fff,
    MASK15 = 0x7fff,
    MASK16 = 0xffff,
    MASK17 = 0x1ffff,
    MASK18 = 0x3ffff,
    MASK19 = 0x7ffff,
    MASK20 = 0xfffff,
    MASK21 = 0x1fffff,
    MASK22 = 0x3fffff,
    MASK23 = 0x7fffff,
    MASK24 = 0xffffff;

  public static final int EEWORD = 0;
  public static final int DAWORD = 1;

  public static final Font gFont = new Font("times", Font.PLAIN, 11);
  public static final String 
      iconDir = Tools.getEnv("SVTMON_DIR")+"/icons/";
  public static final int nCrates = 10;

    /** Green LED indicates no error condition */
  public static final ImageIcon 
                                greenBall = new ImageIcon(iconDir+"stock_green.png"),
                                smallGreenBall = new ImageIcon(iconDir+"green-ball-small.gif"),
    /** Red LED indicates some error condition */
                                redBall = new ImageIcon(iconDir+"stock_draw-sphere.png"),
                                smallRedBall = new ImageIcon(iconDir+"red-ball-small.gif"),
    /** Yellow LED indicates lack of information */
                                yellowBall = new ImageIcon(iconDir+"new-yellow.gif"),
                                smallYellowBall = new ImageIcon(iconDir+"yellow-ball-small.gif"),
    /** Gray LED indicates lack of information */
                                  grayBall = new ImageIcon(iconDir+"stock_gray.png"),
                                  dredBall = new ImageIcon(iconDir+"new-dim-red.gif"),
                                  stopIcon = new ImageIcon(iconDir+"stop_small.png"),
                                  saveIcon = new ImageIcon(iconDir+"save_small.png"),
                                  openIcon = new ImageIcon(iconDir+"open.gif"),
                                windowIcon = new ImageIcon(iconDir+"windows.gif");

  public static final String [][] SVTBoards = 
   {
      {
        "Hit Finder", "Merger", "AM Sequencer", "Hit Buffer"
      }, 
      {
        "Track Fitter", "Merger"
      },
      {
        "Hit Finder", "XTF"
      }
   }; 

  public static final int LARGE_BUFFER_SIZE  = 10000,
                          MEDIUM_BUFFER_SIZE = 1000,
                          SMALL_BUFFER_SIZE  = 100;
  public static final String [] rcSubLabels = 
    {
       "Partition Message",
       "Command Messages",
       "Acknowledgement Message",
       "Error Messages"
    };

  public static final String [] svtSubLabels = 
    {
  	"RC Command", 
    	"Crate Error Status",
    	"Spy Buffers", 
    	"Histograms", 
    	"Beam Position", 
    	"Acknowledgement", 
    };
  public static final String [] defSubs = 
    {
         "/runControl/...",
         "/spymon/status",
         "/spymon/buffer",
         "/spymon/histo",
         "/spymon/beam",
         "/spymon/ack"
    };
  public static final String STATUS_SUBJECT = "/spymon/status",
                             BUFFER_SUBJECT = "/spymon/buffer",
                              HISTO_SUBJECT = "/spymon/histo",
                               BEAM_SUBJECT = "/spymon/beam",
                         BEAM_WEDGE_SUBJECT = "/spymon/beam/wedge_fit",
                                ACK_SUBJECT = "/spymon/ack";

    /** Name of the SVT Crate CPUs */
  public static final String [] cpuNameString = 
    {
      "b0svt00", "b0svt01", "b0svt02", "b0svt03", "b0svt04", 
      "b0svt05", "b0svt06", "b0svt07", "b0svt08", "b0svt09"
    };
    /** Identification strings of the SVT Crates */
  public static final String [] crateIdString = 
    {
      "22RR35I-T", "22RR35I-B", "22RR35H-B", "22RR35G-B", 
      "22RR35F-B", "22RR35F-T", "22RR35G-T", "22RR35H-T"  
    };
    /** SVT crate Names */
  public static final String [] crateNameString = 
    {
      "Phi Slice(0-1)", "Phi Slice(2-3)", "Phi Slice(4-5)", 
      "Phi Slice(6-7)", "Phi Slice(8-9)", "Phi Slice(10-11)", 
      "Fitter", "Fanout"
    };
    /** 'HF', 'MRG', 'AMS', 'HB' ... in that order */
  public static final String [][]  boardErrorRegNames = {
    {
      "Lost Sync",
      "FIFO Overflow",
      "Invalid Data", 
      "Truncated Data",
      "Lost Lock"
    },
    {
      "FIFO A Overflow",
      "FIFO B Overflow",
      "FIFO C Overflow",
      "FIFO D Overflow",
      "Stream A Parity Error",
      "Stream B Parity Error",
      "Stream C Parity Error",
      "Stream D Parity Error",
      "Lost Sync",
      "Truncated Output",
      "Comparison Error(Test mode - TMODCMP)"
    },
    {
      "FIFO Overflow", 
      "Parity Error", 
      "Invalid Data", 
      "Truncated Output"
    }, 
    {
      "Hit FIFO Overflow", 
      "Road FIFO Overflow", 
      "Hit Parity Error", 
      "Road Parity Error", 
      "Lost Sync", 
      "Invalid Output",
      "Invalid Data Hit", 
      "Invalid Data Road"
    },
    {
      "Parity Error",
      "Hit Overflow",
      "Less Hit",
      "Out of Order",
      "Combination Overflow",
      "Layer Overflow",
      "Input FIFO Overflow",
      "TF FIFO Overflow",
      "XFT Phi Overflow",
      "XFT Curvature Overflow",
      "Fit Overflow"
    },
    {
      "Track FIFO Overflow", 
      "Level1 FIFO Overflow", 
      "Parity Error", 
      "Invalid Data", 
      "Lost Sync"
    }
  };
  /** Names of the different global errors */
  public static final String [] gerrors = 
    {
      "Parity Error",
      "Lost Sync",
      "FIFO Overflow",
      "Invalid Data",
      "Internal Overflow",
      "Truncated Output",
      "G-Link Lost Lock",
      "Parity Error in Cable to Level 2"
    };
  public static final int SPY_LENGTH = 131072;
  public static final int nBarrel = 6;
  public static final int nWedge  = 12;
  public static final String DefaultFolder = "/General"; 
}
