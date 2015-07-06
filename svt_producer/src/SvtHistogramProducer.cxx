// ------------------------------------------------------------------------------
// This application collects histograms and other monitoring data from the
// SPYMON program and makes them available to clients like the standard 
// histogram browser, HistoDisplayMain. The Display Server classes developed in
// the Consumer Framework form the basis of this application. The source of data,
// however, is not the usual data stream out of the CSL but those recorded by the 
// SVT boards for the purpose of monitoring the functioning of the SVT system 
// online.  In this respect, this is different from the other consumers. 
// The communication between the SPYMON processes and this application takes 
// place through SmartSockets. The program sets histogram related callback(s) and 
// sits in an infinite loop waiting for histogram  mesasges. When the histogram 
// callback is triggered, the histogram message is converted to root histograms, 
// or already existing histograms are updated. After every n iterations, where n 
// can be configured the histograms are sent to the Display Server which runs 
// under the Consumer framework. Presently, histograms are sent at each 
// iteration. 
// 
// Subject areas:
// 
// /spymon/histo          - Histogram related message
// /spymon/beam           - Beam profile message
// /spymon/beam/wedge_fit - Beam profile message
// /spymon/consumer       - Command message 
// (/spymon/debug/consumer)
//                     o Save    - Save histograms in a file with run number
//                                 sent as a part of the message
//                     o Savenow - Save histograms in a file with run number
//                     o Write   - Write a log file with time stamp, at present
//                                 we save the log file at the end of a 
//                                 session only. So this option may go away 
//                                 someday.
//                     o Stop    - Stop the consumer
//                     o Config  - not implemented yet
//                     o Debug   - Change debug flag value dynamically
//                    Example:
//                       setup svtmon -d
//                       svtcom /spymon/consumer Stop
//                       [svtcom /spymon/consumer Debug 3]  
// /runControl/...  - Messages from the Run Control about the state of DAQ 
//
// Debug Flag:     Bits   (various options can be combined)
//                  1 - Minimal  
//                  2 - Command messages
//                  3 - Beam position
//                  4 - Histograms
//                  5 - 1D histogram 
//                  6 - 2D histogram
//                  7 - RC State message
// -------------------------------------------------------------------------------------

// Standard C++ library  
#include <iostream>
#include <iomanip>
#include <fstream>
#include <sstream>
#include <string>
#include <algorithm>

// C++ style inclusion of Standard C library  
#include <cstdio>
#include <cstdlib>
#include <csignal>
#include <ctime>

// Root library 
#include "TError.h"
#include "TROOT.h"
#include "TApplication.h"
#include "TSystem.h"
#include "TStyle.h"
#include "TH1.h"
#include "TH2.h"
#include "TF1.h"
#include "TPad.h"
#include "TCanvas.h"
#include "TFile.h"
#include "TString.h"
#include "TPaveStats.h"
#include "TPaveText.h"
#include "TPaveLabel.h"
#include "TObjectTable.h"
#include "TPostScript.h"

// Histogram message types 
#include "messages/SVTHistoMessage.h"

// Class definition 
#undef Check
#include "consumer/SvtHistogramProducer.h"

using namespace std;

#define NEL(x) (sizeof((x))/sizeof((x)[0]))

Int_t SvtHistogramProducer::nIter = 0;

// Prototype for static functions 
static void cntrl_c_handler(int sig);
static void SetRootOptions();
static char* timestamp(const int itime);
static void unfreeze(ostringstream& str);
static void tokenize(const string& str, vector<string>& tokens, const string& delimiters=" ");
static void MyErrorHandler(int level, Bool_t abort, const char* location, const char *msg);

#ifdef ROOT_3_01
static void SetBinContent(TH1 *h, Int_t binx, Int_t biny, Stat_t content);
#endif

static const int SZ = 256;
static const int xIteration = 1000;
static const string hext(" (Hist)");
static const string pext(" (Pave)");
static const string defold("/General");
static const string blank(" ");
static const string str_vs(" vs ");
static const string stat_str("stats");
static const string pave_str("TPave");
static const string EMPTY(blank);
static const string dhname[] = {
                                  "SVT proc. time (1 ms range) (us) FROM GB board", 
                                  "SVT processing time(us) FROM GB board",
                                  "EoE Errors Total (frequency) MRG_A_SPY b0svt07 slot 10",
                                  "EoE Errors Total (frequency) MRG_B_SPY b0svt07 slot 10",
                                  "EoE Errors Total (frequency) MRG_OUT_SPY b0svt07 slot 10",
                                  "EoE Errors Total (frequency) XTFA_TRK_SPY b0svt07 slot 17",
                                  "EoE Errors Total (frequency) XTFA_L1_SPY b0svt07 slot 17",
                                  "EoE Errors Total (frequency) XTFA_OUT_SPY b0svt07 slot 17"
                               };
static ErrorHandlerFunc_t neh = MyErrorHandler;  // A dummy event handler
//
// Class Constructor 
// partition    - Receive histograms only from this partition
// name         - Name of the consumer
// dFlag        - Debug Flag
// saveAsOnline - Save histogram preserving the folder structure as online
// port         - Port number where the server could be connected, default 9050 
//
SvtHistogramProducer::SvtHistogramProducer(Int_t partition, 
                                           TString& name, 
                                           Int_t dFlag, 
                                           Bool_t asOnline, 
                                           UInt_t port)
  : fPartitionToWatch(partition), 
    fClientName(name), 
    fDebugFlag(dFlag), 
    fSaveAsOnline(asOnline), 
    fPort(port),
    fRunNo(-1), 
    fDaqPartition(-1), 
    fRCState("UNKNOWN"),
    fStopFlag(kFALSE), 
    fConsExp(new ConsumerExport(fPort, kTRUE)),
    fConsInfo(new TConsumerInfo(fClientName))
{
  // Open Log file 
  string bdir(getenv("SVTMON_DATA_DIR"));

  ostringstream logfile;
  logfile << bdir << "/log_files/" << fClientName.Data() << "_current.log"; 
  fLog.open(logfile.str().c_str(), ios::out);

  if (!fLog) {
    cerr << "ERROR. Could not open log file " 
         << buf << " for writing " << endl;
    exit(2);
  }
  fLog << "Starting Session at: " << timestamp(time(NULL)) << endl;

  CreateHistoMessage();

  ResetMess();
}

//
// Destructor 
//
SvtHistogramProducer::~SvtHistogramProducer() {
  delete fConsExp;
  delete fConsInfo;
}

//
// Initialize the array containing # of histogram messages from each crate
//
void SvtHistogramProducer::ResetMess() {
  for (int i = 0; i < nCrates; i++) nmess[i] = 0;
}
//
// Create Histogram message types which are published by the SVT Crates.
// At present, 1D and 2D are in use
//
void SvtHistogramProducer::CreateHistoMessage() {
  TipcMt mt;

  // SVT Histogram 
  mt.Create(SVTHistoContatiner_MessageName, 
            SVTHistoContatiner_MessageType,
            SVTHistoContatiner_MessageGrammar);   // Histogram container msg 

  mt.Create(SVTHisto1D_MessageName, 
            SVTHisto1D_MessageType,
            SVTHisto1D_MessageGrammar);           // 1D Histogram msg 

  mt.Create(SVTHisto2D_MessageName, 
            SVTHisto2D_MessageType,
            SVTHisto2D_MessageGrammar);           // 2D Histogram msg 

  mt.Create(SVTHisto2DCompressed_MessageName, 
            SVTHisto2DCompressed_MessageType,
            SVTHisto2DCompressed_MessageGrammar); // 2D compressed Histogram msg 
}

//
// Callback which handles the command messages sent to the producer. 
// This message may have upto 3 fields for now, 
//        T_STR  destination,  i.e /spymon/consumer
//        T_STR  command,      i.e "Save", "Stop", "Write", "Debug" etc.
//        T_INT4 value         i.e debug flag
// 
// conn - Connection object
// data - Message data 
// arg  - auxiliary argument, we add the 'self' object because the callbacks
//        are static methods
//
void SvtHistogramProducer::ProcessCommandMessage (
           T_IPC_CONN conn, T_IPC_CONN_PROCESS_CB_DATA data, 
           T_CB_ARG arg)
{
  TipcMsg msg(data->msg);

  // Get the object reference back in this static method
  SvtHistogramProducer *self = static_cast<SvtHistogramProducer *>(arg);
  if (!self) {
    cout << "WARNING. CommandMessage: self pointer is null! Returning ..." << endl;
    return;
  }
  if (self->fDebugFlag >> 1 & 0x1) {
    TipcMt mt = msg.Type();
    cout << "INFO. CommandMessage:  Message Type: <" <<  mt.Name() << ">" << endl;
  }

  msg.Current(0);
  if (!msg.Status()) {
    self->fLog << "WARNING. CommandMesage: Could not set current "
               << "field of command message!" << endl;
    return;
  }

  T_STR command;
  msg >> command >> Check;
  if (!msg.Status()) {
    self->fLog << "WARNING. CommandMesage: Could not get the command !" << endl;
    return;
  }

  if (self->fDebugFlag >> 1 & 0x1) 
    cout << "CommandMesage: " 
         << "Message Received, Command: <" << command << ">"<< endl;

  if (!strcmp(command, "stop") || !strcmp(command, "Stop")) {
    if (self->fDebugFlag >> 1 & 0x1) 
      cout << "-> Stopping <" << self->fClientName << ">" << endl;
    self->fStopFlag = kTRUE;
  }
  else if (!strcmp(command, "save") || !strcmp(command, "Save")) {
    T_INT4 runNo;
    msg >> runNo >> Check;
    if (!msg.Status()) {
      self->fLog << "WARNING. CommandMesage: Could not get run number!" << endl;
      return;
    }
    self->Save(runNo);
  }
  else if (!strcmp(command, "savenow") || !strcmp(command, "Savenow")) {
    self->Save(self->fSvtmonRun);
  }
  else if (!strcmp(command, "write") || !strcmp(command, "Write")) {
    self->WriteLog(self->fSvtmonRun);
  }
  else if (!strcmp(command, "config") || !strcmp(command, "Config")) {
    cout << "INFO. CommandMesage: Config not yet implemented ..." << endl;
  }
  else if (!strcmp(command, "debug") ||  !strcmp(command, "Debug")) {
    T_INT4 dFlag = 0;
    msg >> dFlag >> Check;
    cout << "--> Changing Debug flag, old value = " << self->fDebugFlag 
	 << " new value = " << dFlag << endl;
    self->fDebugFlag = dFlag;
  }
  else {
    cout << "INFO. CommandMessage: Unknown command:  " << command << endl;
  }
} 

//
// Default callback. If it is created and subscribed all the messages
// should be trapped. Not used yet.
//
void SvtHistogramProducer::ProcessDefaultMessage (
           T_IPC_CONN conn, T_IPC_CONN_PROCESS_CB_DATA data, 
           T_CB_ARG arg)
{
  TipcMsg msg(data->msg);

  // Print out the name of the type of the message 
  TipcMt mt  = msg.Type();
  T_STR name = mt.Name();
  TutOut("INFO. DefaultMessage(): Unexpected message type name is <%s>\n", name);
} 

//
// Beam Finding related message (/spymon/beam)
//
void SvtHistogramProducer::ProcessBeamFinderMessage (
           T_IPC_CONN conn, T_IPC_CONN_PROCESS_CB_DATA data, 
           T_CB_ARG arg)
{
  static const string canvas_names [] = {
    "d vs phi BARREL 0 (chi2<25) Final MRG sb MRG_OUT_SPY slot 20",
    "d vs phi BARREL 1 (chi2<25) Final MRG sb MRG_OUT_SPY slot 20",
    "d vs phi BARREL 2 (chi2<25) Final MRG sb MRG_OUT_SPY slot 20",
    "d vs phi BARREL 3 (chi2<25) Final MRG sb MRG_OUT_SPY slot 20",
    "d vs phi BARREL 4 (chi2<25) Final MRG sb MRG_OUT_SPY slot 20",
    "d vs phi BARREL 5 (chi2<25) Final MRG sb MRG_OUT_SPY slot 20"
  };
  static const int nBarrel = 6;
  static string bdir(getenv("BEAM_WWWDIR"));
  static string cbarrel("/Barrel_");
  static string epsext(".eps");

  TipcMsg msg(data->msg);

  // Get the object reference back in this static method
  SvtHistogramProducer *self = static_cast<SvtHistogramProducer *>(arg);
  if (!self) {
    cout << "WARNING. BeamFinder: self pointer is null! Returning .." << endl;
    return;
  }

  // Print out the name of the type of message 
  TipcMt mt  = msg.Type();
  T_STR name = mt.Name();
  if (self->fDebugFlag >> 2 & 0x1) {
    TutOut("INFO. BeamFinder: Message type name is <%s>\n", name);
    msg.Print(TutOut);
  }

  // Start reading from the beginning
  msg.Current(0);
  if (!msg.Status()) {
    self->fLog << "WARNING. BeamFinder: Could not set current "
               << "field of beam finding message!" << endl;
    return;
  }

  // Read time 
  T_INT4 utime;
  msg >> utime >> Check;
  if (!msg.Status()) {
    self->fLog << "WARNING. BeamFinder: Could not retrive the time "
               << "field from beam finding message" << endl;
    return;
  }

  // Open web page file and write the header 
  string cwebpage = bdir + "/indexl.html";
  ofstream webpage(cwebpage.c_str(), ios::out);

  if (!webpage) {
    ostringstream elog;
    elog << "WARNING. BeamFinder: Could not open " 
	 << cwebpage << " for writing ";
    cerr << elog << endl;
    self->fLog << elog << endl;
    return;
  }

  // Open txt file to allow GB startup from scratch 
  string beamtxt = bdir +  "/lastbeam.txt";
  ofstream fbeamtxt(beamtxt.c_str(), ios::out);

  if (!fbeamtxt) {
    ostringstream elog;
    elog << "WARNING. BeamFinder: Could not open " 
	 << beamtxt << " for writing ";
    cerr << elog << endl;
    self->fLog << log << endl;
    return;
  }
  fbeamtxt << "//   Xfit     Yfit    units: micron" << endl;

  webpage << "<meta http-equiv=\"Refresh\" content=\"5\">" << endl
          << "<body>" << endl
          << "<b><tt><u><font color=\"#3333FF\"><font size=+4>" << endl
          << "SVT: Online Beam Position Monitor</font></font></u></tt></b>" << endl
          << "<PRE>" << endl
          << "<font size=+2><b>" << endl
          << "This page updates automatically every 5 seconds" << endl << endl
          << "Last update : " << timestamp(utime) << endl << endl
          << "Coordinates are in microns" << endl << endl
          << "   z  ntrks    x        y      sigma   err x   err y   corr      width x       width y" << endl
          << "---------------------------------------------------------------------------------------" << endl;

  //  Extract the parameters
  //  1. Number of Tracks 2. Number of tracks
  //  3. Fit quality   4. Fit Error 
  //  5. x position    6. y position
  //  7. sigma^2 x     8. sigma^2 y
  //  9. sigma^2 xy   10. sigma^2 d
  // 11. Xorigin (um) 12. Yorigin (um)
  
  T_REAL4 *beam_array;
  T_INT4  array_size;

  ErrorHandlerFunc_t oeh = GetErrorHandler();  // uninstall error handler
  SetErrorHandler(neh);

  for (int i = 0; i < nBarrel; i++) {
    // Unpack bin content message onto arrays
    msg.Next(&beam_array, &array_size);
    if (!msg.Status()) {
      self->fLog << "WARNING. Could not get beam finding message for barrel " 
                 <<  i << endl;
      fbeamtxt << setw(9) << 0 << setw(9) << 0 << endl;
      continue;
    }
    //    if (array_size != 12 && array_size != 17) {  // beam size measurement is now default!
    if (array_size != 17) {
      self->fLog << "WARNING. Wrong array size " << array_size 
                 << " from message for barrel " << i << endl;
      fbeamtxt << setw(9) << 0 << setw(9) << 0 << endl;
      continue;
    }

    Int_t ntrk    = static_cast<Int_t>(beam_array[0]);
    Int_t mtrk    = static_cast<Int_t>(beam_array[1]);
    Int_t fit_q   = static_cast<Int_t>(beam_array[2]);
    Int_t fit_e   = static_cast<Int_t>(beam_array[3]);
    Double_t xval = beam_array[4];
    Double_t yval = beam_array[5];
    Float_t sx    = beam_array[6];
    Float_t sy    = beam_array[7];
    Float_t sxy   = beam_array[8];
    Float_t sd    = beam_array[9];
    Double_t xori = beam_array[10];
    Double_t yori = beam_array[11];
    //    Int_t n_pairs = (Int_t) beam_array[12];
    Float_t x_width     = beam_array[13];
    Float_t x_width_err = beam_array[14]; 
    Float_t y_width     = beam_array[15];
    Float_t y_width_err = beam_array[16]; 

    if (fit_q == 0 || fit_e != 0) {
      webpage << "  " << i << ": No fit!" << endl;
      fbeamtxt << setw(9) << 0 << setw(9) << 0 << endl;
    }
    else {
      Float_t x    = xval * 10000 + xori;  // cm to micron + origin shift in fcon
      Float_t y    = yval * 10000 + yori; 
      Float_t dx   = sqrt(sx) * 10000;
      Float_t dy   = sqrt(sy) * 10000;
      Float_t dd   = sqrt(sd) * 10000;
      Float_t corr = 2*sxy/(sx+sy+0.0001);
      Float_t bwx  = x_width * 10000.;
      Float_t dbwx = x_width_err * 10000.;
      Float_t bwy  = y_width * 10000.;
      Float_t dbwy = y_width_err * 10000.;

      webpage << setiosflags(ios::fixed)
              << " " 
              << setw(2) << dec << i << ":"
              << setw(6) << dec << ntrk 
              << setw(9) << setprecision(2) << x
              << setw(9) << y
              << setw(8) << dd
              << setw(8) << setprecision(3) << dx
              << setw(8) << dy
              << setw(8) << corr 
              << setw(8) << setprecision(2) << bwx << " +" 
              << setw(5) << dbwx  
              << setw(8) << bwy   << " +" 
              << setw(5) << dbwy << endl
              << resetiosflags(ios::fixed);
      fbeamtxt << setw(9) << static_cast<Int_t>(xval * 10000) 
	       << setw(9) << static_cast<Int_t>(yval * 10000) << endl;
    }
    if (self->fDebugFlag >> 2 & 0x1) cout << "mtrk = " << mtrk << endl;

    if (xval == 0.0 && yval == 0.0) {
      if (self->fDebugFlag >> 2 & 0x1) 
        self->fLog << "INFO. BeamFinder: Both the beam fit parameters "
                   << "are zero for barrel " << i << ", continuing ..." << endl;
      continue;
    }

    // Retrieve the canvas which contains the corresponding histogram
    TCanvas *canvas = dynamic_cast<TCanvas *>(
        self->GetCanvasList()->FindObject(canvas_names[i].c_str()));
    if (!canvas) {
      cout << "ERROR. BeamFinder: "
           << "Cannot retrive canvas object for <" 
           << canvas_names[i] << ">, continuing ..." << endl;
      continue;
    }
    canvas->cd(); 

    TPad *pad = dynamic_cast<TPad *>(canvas->GetPrimitive(canvas->GetName()));
    if (!pad) {
      cout << "ERROR. BeamFinder: Pad cannot be retrieved!,  index = " 
           << index << " name = " << canvas->GetName() << endl;
      continue;
    }
    pad->cd();

    // Function name
    ostringstream tmpstr;
    tmpstr << "Beam Profile in Barrel [" << i << "]";
    string fname(tmpstr.str());

    string pave_name = fname + pext;

    // Create the function only for the first time and add to the function list
    if (self->GetFunctionList()->IsEmpty() || 
       !self->GetFunctionList()->Contains(fname.c_str())) 
    {
      TF1 *func = new TF1(fname.c_str(), "[1]*cos(x)-[0]*sin(x)", 0.0, 2*TMath::Pi());
      func->SetLineColor(kRed);         // Red Line
      func->SetParName(0, "x_value");
      func->SetParName(1, "y_value");

      func->Draw("CSAME");      // Add reference of the function object in the canvas
#if 0
      func->DrawCopy("SAME");  // Add reference of the function object in the canvas
                               // Use DrawCopy() if the function needs to be drawn
                               // multiple times with updated parameters
#endif

      // Add TPaveText to display fitted quantities
      // just below the statistic box (even if the statistics box is not there!)
      Double_t x1 = 0., 
               x2 = 0., 
               y1 = 0., 
               y2 = 0.;
      TPaveStats *pst = 
          dynamic_cast<TPaveStats *>(pad->GetPrimitive(stat_str.c_str()));
      if (pst) {
        x1 = pst->GetX1();
        x2 = pst->GetX2();
        y1 = pst->GetY1();
        y2 = pst->GetY2();
	if (self->fDebugFlag >> 2 & 0x1) 
           cout << "--> 2 corners: (" << x1 << "," << y1 << ") and ("
                                      << x2 << "," << y2 << ")" << endl;
      }

      TPaveText *pave = new TPaveText(0.2, 0.78, 0.8, 0.9, "NDC");
      pave->SetName(pave_name.c_str());
      pave->Draw();
    }

    // Retrieve the function from the list and update parameters
    TF1 *func = 
       dynamic_cast<TF1 *>(self->GetFunctionList()->FindObject(fname.c_str()));
    if (!func) {
      cout << "WARNING. BeamFinder: "
           << "Cannot retrive the function for <" 
           << fname << ">, continuing ..." << endl;
      continue;
    }

    // Update function parameters
    func->SetParameter(0, xval*10000.);   // cm -> micron 
    func->SetParameter(1, yval*10000.);
    if (self->fDebugFlag >> 2 & 0x1) func->Print();

    // Retrieve the object from the list
    TPaveText *pave = 
       dynamic_cast<TPaveText *>(pad->GetPrimitive(pave_name.c_str()));
    if (pave) {
      pave->Clear();

      ostringstream xline;
      xline << setiosflags(ios::fixed);
      xline << setprecision(1) 
            << "x_{SVT} = " << setw(6) << xval * 10000 
                      << " #pm " 
                      << setw(3) << sqrt(sx) * 10000 << " #mum ; "
            << "x_{beam} = " << setw(6) << xval * 10000 + xori 
                      << " #pm " 
                      << setw(3) << sqrt(sx) * 10000 << " #mum";
      xline << resetiosflags(ios::fixed);
      TText *txt1 = pave->AddText(xline.str());
      txt1->SetTextSize(0.025);

      ostringstream yline;
      yline << setiosflags(ios::fixed);
      yline << setprecision(1) 
            << "y_{SVT} = " << setw(6) << yval * 10000 
                      << " #pm " 
                      << setw(3) << sqrt(sy) * 10000 << " #mum ; "
            << "y_{beam} = " << setw(6) << yval * 10000 + yori 
                      << " #pm " 
                      << setw(3) << sqrt(sy) * 10000 << " #mum";
      yline << resetiosflags(ios::fixed);
      TText *txt2 = pave->AddText(yline.str());
      txt2->SetTextSize(0.025);
    }
    else {
      cout << "WARNING. BeamFinder:"
           << "Cannot retrive the TPaveText for Barrel <" 
           << i << ">, ignoring ..." << endl;
    }

    // Remove statistics box for the beam profile plots
    // Redundant as statistics box are not displayed for scatter plots
    string hname = canvas_names[i] + hext;
    TH1 *hist = dynamic_cast<TH1 *>(pad->GetPrimitive(hname.c_str()));
    if (hist) {
      hist->SetStats(kFALSE);
    }

    // Update the primitives in the canvas
    canvas->Modified();
    canvas->Update();
    if (self->fDebugFlag >> 2 & 0x1) canvas->ls();  // List the primitives

    // Now save the canvas for the 6 barrels as eps files for the same web page
    ostringstream filename;
    filename << bdir.c_str() << cbarrel.c_str() 
             << i << epsext.c_str();
    canvas->SaveAs(filename.str());
  }
  SetErrorHandler(oeh);                         // reinstall error handler
  webpage << "---------------------------------------------------------------------------------------" << endl
	  << "</PRE>" << endl
          << "</body>" << endl;

  webpage.close();
  fbeamtxt.close();

  // rename temporary file in to the browsable one!

  string cwebpage_f = bdir + "/index.html";
  rename(cwebpage.c_str(), cwebpage_f.c_str());
} 
//
// Beam Finding related message (/spymon/beam/wedge_fit)
//
void SvtHistogramProducer::ProcessBeamFinderWedgeMessage (
           T_IPC_CONN conn, T_IPC_CONN_PROCESS_CB_DATA data, 
           T_CB_ARG arg)
{
  static const int nBarrel = 6;
  static const int nWedge = 12;
  static string bdir(getenv("BEAM_WWWDIR"));
  static string cbarrel("/Barrel_");
  static string epsext(".eps");

  TipcMsg msg(data->msg);

  // Get the object reference back in this static method
  SvtHistogramProducer *self = static_cast<SvtHistogramProducer *>(arg);
  if (!self) {
    cout << "WARNING. BeamFinderWedge: self pointer is null! Returning ..." << endl;
    return;
  }

  // Print out the name of the type of message 
  TipcMt mt  = msg.Type();
  T_STR name = mt.Name();
  if (self->fDebugFlag >> 2 & 0x1) {
    TutOut("INFO. BeamFinderWedge: Message type name is <%s>\n", name);
    msg.Print(TutOut);
  }

  // Start reading from the beginning
  msg.Current(0);
  if (!msg.Status()) {
    self->fLog << "WARNING. BeamFinderWedge: Could not set current "
               << "field of beam finding message!" << endl;
    return;
  }

  // Read time 
  T_INT4 utime;
  msg >> utime >> Check;
  if (!msg.Status()) {
    self->fLog << "WARNING. BeamFinderWedge: Could not retrive the time "
               << "field from beam finding message" << endl;
    return;
  }

  // Open web page file and write the header 
  string cwebpage = bdir + "/wedgefits.tmp";
  ofstream webpage(cwebpage.c_str(), ios::out);

  if (!webpage) {
    ostringstream elog;
    elog << "WARNING. BeamFinderWedge: Could not open " 
	 << cwebpage << " for writing ";
    cerr << elog << endl;
    self->fLog << elog << endl;
    return;
  }

  webpage << "<meta http-equiv=\"Refresh\" content=\"5\">" << endl
          << "<body>" << endl
          << "<b><tt><u><font color=\"#3333FF\"><font size=+4>" << endl
          << "SVT: Online Beam Position Monitor</font></font></u></tt></b>" << endl
          << "<PRE>" << endl
          << "<font size=+2><b>" << endl
          << "This page updates automatically every 5 seconds" << endl << endl
          << "Last update : " << timestamp(utime) << endl << endl
          << "Coordinates are in microns" << endl; 

  //  Extract the parameters
  //  1. Number of Tracks 2. Number of tracks
  //  3. Fit quality   4. Fit Error 
  //  5. x position    6. y position
  //  7. sigma^2 x     8. sigma^2 y
  //  9. sigma^2 xy   10. sigma^2 d
  // 11. Xorigin (um) 12. Yorigin (um)
  
  T_REAL4 *beam_array;
  T_INT4  array_size;
  for (int i = 0; i < nBarrel; i++) {
    for (int j = 0; j < nWedge; j++) {
      // Unpack bin content message onto arrays
      msg.Next(&beam_array, &array_size);
      if (!msg.Status()) {
	self->fLog << "WARNING. Could not get beam finding message for barrel " 
		   <<  i << endl;
	continue;
      }
      if (array_size != 12) {
	self->fLog << "WARNING. Wrong array size " << array_size 
		   << " from message for barrel " << i << " wedge " << j << endl;
	continue;
      }
      
      Int_t ntrk    = static_cast<Int_t>(beam_array[0]);
      Int_t mtrk    = static_cast<Int_t>(beam_array[1]);
      Int_t fit_q   = static_cast<Int_t>(beam_array[2]);
      Int_t fit_e   = static_cast<Int_t>(beam_array[3]);
      Double_t xval = beam_array[4];
      Double_t yval = beam_array[5];
      Float_t sx    = beam_array[6];
      Float_t sy    = beam_array[7];
      Float_t sxy   = beam_array[8];
      Float_t sd    = beam_array[9];
      Double_t xori = beam_array[10];
      Double_t yori = beam_array[11];
      
      if (j==0) {
	webpage << endl
	        << " Barrel: " << i << " SVT origin shift X:" << xori << " Y:" << yori << endl
	        << endl
	        << "   w  ntrks    x        y      sigma   err x   err y   corr"
	        << endl
	        << "-----------------------------------------------------------"
	        << endl;
      }
      if (fit_q == 0 || fit_e != 0) {
	webpage << "  " << j << ": No fit!" << endl;
      }
      else {
	Float_t x    = xval * 10000 + xori;  // cm to micron + origin shift in fcon
	Float_t y    = yval * 10000 + yori; 
	Float_t dx   = sqrt(sx) * 10000;
	Float_t dy   = sqrt(sy) * 10000;
	Float_t dd   = sqrt(sd) * 10000;
	Float_t corr = 2*sxy/(sx+sy+0.0001);
	
	webpage << setiosflags(ios::fixed)
	        << " " 
		<< setw(2) << dec << j << ":"
		<< setw(6) << dec << ntrk 
		<< setw(9) << setprecision(2) << x
		<< setw(9) << y
		<< setw(8) << dd
		<< setw(8) << setprecision(3) << dx
		<< setw(8) << dy
		<< setw(8) << corr << " test " << endl
	        << resetiosflags(ios::fixed);
      }
      if (self->fDebugFlag >> 2 & 0x1) cout << "mtrk = " << mtrk << endl;
      
      if (xval == 0.0 && yval == 0.0) {
	if (self->fDebugFlag >> 2 & 0x1) 
	  self->fLog << "INFO. BeamFinderWedge: Both the beam fit parameters "
		     << "are zero for barrel " << i << ", continuing ..." << endl;
	continue;
      }
    }
  }
  webpage << "----------------------------------------------------------------"
          << endl
          << "</PRE>" << endl
          << "</body>" << endl;

  webpage.close();

  // rename temporary file in to the browsable one!

  string cwebpage_f = bdir + "/wedgefits.html";
  rename(cwebpage.c_str(), cwebpage_f.c_str());
} 

//
// Listen to the state of Data Acquisition, namely, partition #, RC state,
// Run # etc.
//
void SvtHistogramProducer::ProcessRCStateMessage (
           T_IPC_CONN conn, T_IPC_CONN_PROCESS_CB_DATA data, 
           T_CB_ARG arg)
{
  TipcMsg msg(data->msg);

  // Get the object reference back in this static method
  SvtHistogramProducer *self = static_cast<SvtHistogramProducer *>(arg);
  if (!self) {
    cout << "WARNING. RCState: self pointer is null! Returning ..." << endl;
    return;
  }

  // Print out the name of the type of the message 
  TipcMt mt  = msg.Type();
  T_STR name = mt.Name();

  // ------------------------------------------------------
  // Figures out what this message says.
  // Expect something with 4 words:
  // 1. Verb (Enter, Leave, In)
  // 2. State we are dealing with  (this may be two words)
  // 3. Partition number
  // 4. the present state manager
  // 5. Time stamp
  // 6. Sender ID
  // 7. Run Number
  // ------------------------------------------------------
  T_STR  msg_str;
  msg >> msg_str >> Check;
  if (!msg.Status()) {
    self->fLog << "ERROR. Could not get the RC State message string" << endl; 
    return;
  }

  string str(msg_str);
  vector<string> tokens;
  tokenize(str, tokens);

  int size = tokens.size();
  if (size > 0) self->fDaqPartition = atoi(tokens[0].c_str());
  if (size > 1) self->fRunNo        = atoi(tokens[1].c_str());
  if (size > 2) self->fRCState      = tokens[2];

  // Let's be consistent and use run number published by the crate(s)
  // self->fConsInfo->setRunNumber(self->fRunNo);

  if (self->fDebugFlag >> 6 & 0x1) {
    TutOut("INFO. RCMessage: Message type name is <%s>\n", name);
    msg.Print(TutOut);
    cout << "INFO. " << self->fDaqPartition << "/" << self->fRCState 
         << "/" << self->fRunNo << endl;
  }
} 

//
// Processs the histogram callback. At each iteration check whether new 
// histograms are added comparing to the previous list. If new histograms 
// are found, place them in the proper place, i.e at the end of the object 
// list. For the existing histograms simply reset the contents first and 
// fill with the new ones. 
// 
// Support for both 1 and 2 dimensional histograms exists. From the 
// smartsockets message type the Histogram type is determined and the 
// corresponding unpacking routine is called.
//
// conn - Connection object
// data - Message data 
// arg  - auxiliary argument, we add the 'self' object because the callbacks
//        are static methods
//
void SvtHistogramProducer::ProcessHistogramMessage (
           T_IPC_CONN conn, T_IPC_CONN_PROCESS_CB_DATA data, 
           T_CB_ARG arg) 
{
  static const string htype_1d("SVTHISTO_1D");
  static const string htype_2d("SVTHISTO_2D");
  
  // Get the object reference back in this static method 
  SvtHistogramProducer *self = static_cast<SvtHistogramProducer *>(arg);
  if (!self) {
    cout << "WARNING. HistogramMessage: self pointer is null! Returning ..." << endl;
    return;
  }

  // # of times histogram messages have been published so far
  SvtHistogramProducer::nIter++;

  // Get SmartSockets message handle
  TipcMsg msg(data->msg);

  if (self->fDebugFlag >> 3 & 0x1) 
    cout << "Packet Size: " << msg.PacketSize() << endl;

  msg.Current(0);
  if (!msg.Status()) {
    self->fLog << "ERROR. Process: Could not set current field of " 
               << "histogram message!" << endl;
    return;
  }

  string sender = msg.Sender();
  unsigned int index = sender.find("b0svt");
  if (index != string::npos) {
    int iCrate = atoi(sender.substr(index+6, 1).c_str());
    if (iCrate >= 0 && iCrate < nCrates) self->nmess[iCrate]++;
  }
  if (self->fDebugFlag >> 3 & 0x1) {
    cout << "INFO. Sender: " << sender << endl;
    cout << "Histogram message published, decode .." << endl;
  }

  // Run number and timestamp
  T_INT4 run_number, mon_time;
  msg >> run_number >> mon_time >> Check;
  self->fSvtmonRun = static_cast<Int_t>(run_number);
  self->fSvtmonTime = static_cast<Int_t>(mon_time);
  //  msg >> static_cast<T_INT4>(self->fSvtmonRun) 
  //    >> static_cast<T_INT4>(self->fSvtmonTime) >> Check;
  if (!msg.Status()) {
    self->fLog << "ERROR. Could not get run # and time stamp from " 
               << "svtmon, sender " << msg.Sender() << "!" << endl; 
    return;
  }
  self->fConsInfo->setRunNumber(self->fSvtmonRun);

  // 
  // Get the message array and the array length
  //
  TipcMsg *submsg_array;
  T_INT4  submsg_array_size = 0;
  msg.Next(&submsg_array, &submsg_array_size);
  if (!msg.Status()) {
    self->fLog << "ERROR. Could not get the histogram message array, " 
               << "sender " << msg.Sender() << "!" << endl;
    return;
  }

  Int_t nHistogram = submsg_array_size;   // Convenience
  if (self->fDebugFlag >> 1 & 0x1) cout << "INFO. Sender " << msg.Sender() 
                                        << ", Nhist: " << nHistogram << endl;

  // Check if new histograms have been added/removed at runtime. This is non-trivial
  // because histograms are published by all the crates, hence 
  // nEntries != nHistogram will always be true. As checking the condition is not trivial,
  // it is virtually commented out for now.
  if (self->fDebugFlag >> 10 & 0x1) {
    if (!self->GetHistList()->IsEmpty()) {
      Int_t nEntries = self->GetNHistograms();
      if (nEntries != nHistogram) {
        cout << "WARNING. Histogram list has changed!" << endl;
        cout << "# of histograms present in the list " << nEntries  
             << ", # histograms published in this iteration " 
             << nHistogram << endl;
      }
    }
  }

  // Now loop over individual histogram messages and create Root histograms
  for (int i = 0; i < nHistogram; i++) {
    // Check the type of the message here
    TipcMt mt  = submsg_array[i].Type(); 
    T_STR name = mt.Name(); 

    if (!strcmp(name, htype_1d.c_str())) 
      self->Unpack1DHist(submsg_array[i], i);           // 1D histograms
    else if (!strcmp(name, htype_2d.c_str())) 
      self->Unpack2DHist(submsg_array[i], i);           // 2D histograms
  }
  // This is needed as 'Next' or an extraction op actually extracts a C message array,
  // determine the size of the array and dynamically allocate an array of TipcMsg
  delete [] submsg_array;

  self->fConsInfo->setNevents(self->GetNEvt());

  if (self->fDebugFlag >> 3 & 0x1) {
    cout << SvtHistogramProducer::nIter << endl;
    self->fConsInfo->print();
    cout << "INFO. Run # " << self->fConsInfo->runnumber() << " nevent  " 
         << self->fConsInfo->nevents() << endl;
    cout << "INFO. Modified " << self->fConsInfo->isModified() << endl;;
  }

  // Send out the consumer info and all objects in its list
  // provided the list is not empty
  if (nHistogram > 0) self->fConsExp->send(self->fConsInfo);
  if (SvtHistogramProducer::nIter%xIteration == 0) 
    cout << "INFO. Sent out objects to the Server. " 
         << "Iteration # " << SvtHistogramProducer::nIter << endl;
}

//
// Unpack individual 1D histogram message 
//
Bool_t SvtHistogramProducer::Unpack1DHist(TipcMsg& msg, const T_INT4 index) {
  static const string hcl_1d("TH1F");
  static const string ytit_str("Event Count");
  static const string logy_str("Logy");

  // Validate the message
  msg.Current(0);
  if (!msg.Status()) {
    fLog << "ERROR. Could not set current field for " 
         << index <<  "th message!" << endl;
    return kFALSE;
  }

  if (fDebugFlag >> 4 & 0x1) 
    cout << "INFO. Dump 1D Histo: " << endl;

  // Get Histogram header first
  T_INT4 hid, nbins, nentries;
  T_REAL4 xlow, xhig;
  T_INT4 uflow, oflow;
  T_STR title;
  msg >> hid >> title >> nbins >> xlow >> xhig 
             >> nentries >> uflow >> oflow >> Check;
  if (!msg.Status()) {
    fLog << "ERROR. Could not get the 1D histogram attribute "     
         << "fields for " << index << "th message!" << endl; 
    return kFALSE;
  }
  
  // Decode histogram title, LOGY option etc from SS message
  string xTitle(title), 
         gTitle(title), 
         yopt("Liny");  

  string str(title);
  vector<string> tokens;
  tokenize(str, tokens, "#");

  int size = tokens.size();
  if (size == 0 && (fDebugFlag >> 4 & 0x1)) 
    cout << "WARNING. A complete String! cannot be split: " << title << endl;

  if (size > 0) xTitle = tokens[0];
  if (size > 1) gTitle = tokens[1];
  if (size > 2) yopt   = tokens[2];

  // Name of the canvas (shown in the list view of HistoDisplayMain)
  string cname = xTitle + blank + gTitle;
  // Create a different name (appending hid) for the histogram!
  string hname = cname + hext;

  if (fDebugFlag >> 4 & 0x1) 
    cout << "INFO. Histogram Header: " << index << " " << hid << " " << title 
         << " " << nbins << " " << xlow << " " << xhig << endl;

  // Create a stamp with run number and the svtmon time
  string stamp;

  // Create histogram and the container canvas and add to respective lists
  if (GetHistList()->IsEmpty() || !GetHistList()->Contains(hname.c_str())) {
    // Construct Histogram browser foldername from message destination
    string folder(fClientName);
    T_STR fold = msg.Dest();
    string pathstr = (!fold) ? defold.c_str() : fold;
    folder += pathstr;

    // Build the vector with unique folder names
    int where = 0;
    //count(uniqPath.begin(), uniqPath.end(), pathstr, where);
    if (!where) uniqPath.push_back(pathstr);
    
    // Create the histogram   
    if (fDebugFlag >> 4 & 0x1) 
      cout << "Creating: " << index << ": " << hname << endl;
    TH1F *histo = new TH1F(hname.c_str(), gTitle.c_str(), nbins, xlow, xhig);
    if (!histo) {
      fLog << "ERROR. Could not create Histogram for id = " << hid << endl; 
      return kFALSE;
    }
  
    // Set and modify histogram attributes
    histo->SetStats(kTRUE);

#ifdef ROOT_3_01
    histo->SetXTitle(xTitle.c_str());
#else
    histo->SetXTitle(const_cast<char *>(xTitle.c_str()));
#endif
    histo->SetYTitle(ytit_str.c_str());
    histo->SetFillColor(48);
    histo->GetXaxis()->CenterTitle(kTRUE);
    histo->GetYaxis()->CenterTitle(kTRUE);

    // Now create the canvas, which will contain a few labels and a Pad. The Pad
    // in turn contains the histogram. In order to modify the look and feel
    // of the histograms, one modifies attributes of the components of the Pad
    TCanvas *canvas = new TCanvas(cname.c_str(), cname.c_str());
    canvas->SetBatch();      // May be redundant as gROOT->SetBatch() is in effect

    // Now create a pad which holds the histogram
    TPad *pad = 
      new TPad(canvas->GetName(), canvas->GetName(), 0.005, 0.005, 0.995, 0.945);
    pad->Draw();             // Part of the canvas

    // Show run number and time stamp at the top of the canvas in a label
    EventStamp(stamp);
    TPaveLabel *rlabel = 
      new TPaveLabel(0.005, 0.95, 0.995, 0.995, stamp.c_str());
    rlabel->Draw();          // Part of the canvas

    // Draw the histogram on the pad
    pad->cd();
    histo->Draw();
    
    // Add explanation for EoE plots
    if (cname.find("EoE") != string::npos) {
      TPaveText *_label2 = new TPaveText(0.5, 0.4, 0.9, 0.8, "NDC");
      _label2->AddText("0) Parity Error");
      _label2->AddText("1) Lost Sync");
      _label2->AddText("2) FIFO Overflow");
      _label2->AddText("3) Invalid Data");
      _label2->AddText("4) Internal Overflow");
      _label2->AddText("5) Truncated Output");
      _label2->AddText("6) G-link Lost-Lock");
      _label2->AddText("7) Parity Error on cable to L2");
      _label2->SetTextAlign(13);
      _label2->SetFillColor(17);
      _label2->SetTextColor(1);
      _label2->SetTextSize(0.03);
      _label2->SetTextFont(62);
      _label2->Draw();
    }

    // Add the canvas to the TConsumerInfo object
    if (!fConsInfo->getAddress(cname.c_str())) {
      fConsInfo->addObject(canvas->GetName(), folder.c_str(), TConsumerInfo::Okay, canvas);
      // Add to Slide show
      if (cname.find("Slide") != string::npos) {
        ostringstream slidePath; 
        slidePath << fClientName <<  "/Slides/";
        fConsInfo->addObject(canvas->GetName(), slidePath.str(), TConsumerInfo::Okay, canvas);
      }
    }

    if (fDebugFlag >> 4 & 0x1)
      cout << "INFO. Dest: " << msg.Dest() 
           << " UserProp: "  << msg.UserProp() << endl;
  }

  // Get back the canvas from the List 
  TCanvas *canvas 
     = dynamic_cast<TCanvas *>(GetCanvasList()->FindObject(cname.c_str())); 
  if (!canvas) {
    fLog << "ERROR. Canvas cannot be retrieved!, index = " << index 
         << " name = " << cname << endl;
    return kFALSE;
  }
  canvas->cd();

  TPad *pad = 
    dynamic_cast<TPad *>(canvas->GetPrimitive(canvas->GetName()));
  if (!pad) {
    fLog << "ERROR. Pad cannot be retrieved!, index = " << index 
         << " name = " << canvas->GetName() << endl;
    return kFALSE;
  }

  // Get back the histogram from the List consistently for any iteration
  TH1 *histo = 
    dynamic_cast<TH1 *>(GetHistList()->FindObject(hname.c_str())); 
  if (!histo) {
    fLog << "ERROR. Cannot retrieve the " << index 
         << "th histogram from the List,"  << endl;
    return kFALSE;
  }
  if (strcmp(histo->ClassName(), hcl_1d.c_str())) return kFALSE;

  histo->Reset();    // Assume that the most recently published histogram
                     // is the one to be displayed
  
  // Unpack bin content message onto arrays
  T_REAL4 *bins_array;
  T_INT4 array_size;
  msg.Next(&bins_array, &array_size);
  if (!msg.Status()) {
    fLog << "ERROR. Could not get histogram bin content!" << endl;
    return kFALSE;
  }

  // Fill the histogram with the array content
  Int_t xbins = histo->GetNbinsX();

  histo->SetBinContent(0, 1.0*uflow);
  for (int j = 0; j < xbins; j++)
    histo->SetBinContent(j+1, bins_array[j]);    
  histo->SetBinContent(xbins+1, 1.0*oflow);
  histo->SetEntries(nentries);

  // Read error contents and ignore (check whether this is published at all)
  T_REAL4 *errors_array;
  msg.Next(&errors_array, &array_size);
  if (!msg.Status()) {
    fLog << "ERROR. Could not get histogram error content!" << endl;
    return kFALSE;
  }

  // Tackle Logy option
  if (yopt == logy_str) {
    if (histo->GetEntries() > 0) 
      pad->SetLogy(1);   // set LOGY option
    else 
      pad->SetLogy(0);   // restore linear option
  }
  else {
    pad->SetLogy(0);     // restore linear option
  }  

  // Change the statistics option
  // Play the following trick to remove 'histogram name' from the statistics box
  TPaveStats *pst = 
    dynamic_cast<TPaveStats *>(pad->GetPrimitive(stat_str.c_str()));
  if (pst) {
    if (fDebugFlag >> 4 & 0x1) 
       cout << "Statistics option: " << pst->GetOptStat();
    pst->SetOptStat(111110);
  }
  else {
    if (fDebugFlag >> 4 & 0x1) 
      cout << "ERROR. Cannot retrieve the statistics box for " << cname << endl;
  }

  // Get back the label
  TPaveLabel *label 
   = dynamic_cast<TPaveLabel *>(canvas->GetPrimitive(pave_str.c_str()));

  // Update run number and time stamp
  EventStamp(stamp);
  if (label) label->SetLabel(stamp.c_str());

  canvas->Modified();
  canvas->Update();

  // Print all the primitives contained in the canvas
  if (fDebugFlag >> 4 & 0x1) canvas->ls();  

  return kTRUE;
}

//
// Process individual 2D histogram message
//
Bool_t SvtHistogramProducer::Unpack2DHist(TipcMsg& msg, const T_INT4 index) {
  static const string hcl_2d("TH2F");

  msg.Current(0);
  if (!msg.Status()) {
    fLog << "ERROR. Could not set current field for "
         << index << "th message!" << endl;
    return kFALSE;
  }

  // Retrieve histogram ID and the title
  T_INT4 hid;
  T_STR title;
  msg >> hid >> title >> Check;
  if (!msg.Status()) {
    fLog << "ERROR. Could not get the 2d histogram attribute "
         << "fields for " << index << "th message!" << endl; 
    return kFALSE;
  }
  
  // Retrieve histogram x and y # of bins
  T_INT4 *nbins_array;
  T_INT4 nbins_array_size;
  msg.Next(&nbins_array, &nbins_array_size);
  if (!msg.Status()) {
    fLog << "ERROR. Could not get 2D histogram bin numbers!" << endl;
    return kFALSE;
  }

  // Retrieve histogram x and y low and high axis values
  T_REAL4 *range_array;
  T_INT4 range_array_size;
  msg.Next(&range_array, &range_array_size);
  if (!msg.Status()) {
    fLog << "ERROR. Could not get 2D histogram x and y ranges!" << endl;
    return kFALSE;
  }

  if (nbins_array_size != 2 || range_array_size != 4) { 
    fLog << "ERROR. Incorrect number of elements in histogram "
         << "nbins and histograms ranges, " << nbins_array_size 
         << "," << range_array_size << endl;
    return kFALSE;
  }

  // Get histogram title etc from the SS message
  string yTitle(title), 
         gTitle(title), 
         xTitle(title);  

  string str(title);
  vector<string> tokens;
  tokenize(str, tokens, "#");

  int size = tokens.size();
  if (size == 0 && (fDebugFlag >> 5 & 0x1)) 
    cout << "WARNING. A complete String! cannot be split " << endl;

  if (size > 0) yTitle = tokens[0];
  if (size > 1) xTitle = tokens[1];
  if (size > 2) gTitle = tokens[2];

  // Canvas name
  ostringstream tmpstr;
  tmpstr << yTitle << str_vs << xTitle << blank << gTitle;
  string cname(tmpstr.str());

  // Construct histogram name appending hid to the canvas name
  string hname = cname + hext;

  if (fDebugFlag >> 5 & 0x1) {
    cout << "INFO. Histogram Header: " << index << " " << hid << " " << title << endl;
    cout <<  cname << " " << nbins_array[0] << " " 
                  << range_array[0] << " " << range_array[1] 
                  << " " << nbins_array[1] << " " 
                  << range_array[2] << " " << range_array[3] 
                  << endl;
  }

  // Create a stamp with run number and the svtmon time
  string stamp;

  // Create and add the histogram and the containing canvas to the respective Lists
  if (GetHistList()->IsEmpty() || !GetHistList()->FindObject(hname.c_str())) {
    if (fDebugFlag >> 3 & 0x1) 
      cout << "Packet Size: " << msg.PacketSize() << " bytes" << endl;

    // Construct folder name
    string folder(fClientName);
    T_STR fold = msg.Dest();
    string pathstr = (!fold) ? defold.c_str() : fold;
    folder += pathstr;

    // Build the vector with unique folder names
    int where = 0;
    //count(uniqPath.begin(), uniqPath.end(), pathstr, where);
    if (!where) uniqPath.push_back(pathstr);

    // Book the histogram 
    if (fDebugFlag >> 3 & 0x1) 
      cout << "Creating: " << index << ": " << hname << endl;
    TH2F *histo = new TH2F(hname.c_str(), gTitle.c_str(), 
                           nbins_array[0], range_array[0], range_array[1], 
                           nbins_array[1], range_array[2], range_array[3]);
    if (!histo) {
      fLog << "ERROR. Could not create 2D Histogram for id = " << hid << endl; 
      return kFALSE;
    }
  
    // Set and Modify histogram properties
    histo->SetStats(kFALSE);
#ifdef ROOT_3_01
    histo->SetXTitle(xTitle.c_str());
    histo->SetYTitle(yTitle.c_str());
#else
    histo->SetXTitle(const_cast<char *>(xTitle.c_str()));
    histo->SetYTitle(const_cast<char *>(yTitle.c_str()));
#endif
    histo->SetFillColor(48);
    histo->GetXaxis()->CenterTitle(kTRUE);
    histo->GetYaxis()->CenterTitle(kTRUE);

    // Now create the canvas, which will contain the histogram, for flexibility
    TCanvas *canvas = new TCanvas(cname.c_str(), cname.c_str());
    canvas->SetBatch();

    // Now create a pad which holds the histogram
    TPad *pad = 
      new TPad(canvas->GetName(), canvas->GetName(), 0.005, 0.005, 0.995, 0.945);
    pad->Draw();   // Part of the canvas

    // Show run number and time stamp at the top of the canvas
    EventStamp(stamp);
    TPaveLabel *rlabel = 
      new TPaveLabel(0.005, 0.95, 0.995, 0.995, stamp.c_str());
    rlabel->Draw();          // Part of the canvas

    pad->cd();
    histo->Draw();           // Draw the histogram on the canvas

    // Add object to consumer info
    if (!fConsInfo->getAddress(cname.c_str())) {
      fConsInfo->addObject(canvas->GetName(), folder.c_str(), TConsumerInfo::Okay, canvas);
      // Add to Slide show
      if (cname.find("Slide") != string::npos) {
        ostringstream slidePath; 
        slidePath << fClientName <<  "/Slides/";
        fConsInfo->addObject(canvas->GetName(), slidePath.str(), TConsumerInfo::Okay, canvas);
      }
    }

    if (fDebugFlag >> 5 & 0x1)
      cout << "INFO. Dest = " << msg.Dest() 
           << " UserProp  = " << msg.UserProp() << endl;
  }
  // Get back the canvas from the List 
  TCanvas *canvas = 
    dynamic_cast<TCanvas *>(GetCanvasList()->FindObject(cname.c_str())); 
  if (!canvas) {
    fLog << "ERROR. Canvas cannot be retrieved!,  index = " << index 
         << " name = " << cname << endl;
    return kFALSE;
  }
  canvas->cd();

  TPad *pad = dynamic_cast<TPad *>(canvas->GetPrimitive(canvas->GetName()));
  if (!pad) {
    fLog << "ERROR. Pad cannot be retrieved!,  index = " << index 
         << " name = " << canvas->GetName() << endl;
    return kFALSE;
  }
  // Now retrieve the histogram from the List
  TH1 *histo = dynamic_cast<TH1 *>(GetHistList()->FindObject(hname.c_str())); 
  if (!histo) {
    fLog << "ERROR. Cannot retrieve the " << index 
         << "th histogram from the List" << endl;
    return kFALSE;
  }
  if (strcmp(histo->ClassName(), hcl_2d.c_str())) return kFALSE;

  histo->Reset();  // Assume that the most recently published histogram
                   // is the one to be displayed
  
  // Get histogram entries
  T_INT4 nentries;
  msg >> nentries >> Check;
  if (!msg.Status()) {
    fLog << "ERROR. Could not get histogram entries!" << endl;
    return kFALSE;
  }

  histo->SetEntries(static_cast<Int_t>(nentries));

  // Now get the 9 numbers of overflow/underflow
  T_REAL4 *flow_array;
  T_INT4 flow_array_size = 0;
  msg.Next(&flow_array, &flow_array_size);
  if (!msg.Status()) {
    fLog << "ERROR. Could not get histogram under/overflow contents!" << endl;
    for (int i = 0; i < flow_array_size; i++) 
      fLog << flow_array[i] << "\t";
    fLog << endl;
    return kFALSE;
  }
  if (flow_array_size != 9) { 
    fLog << "ERROR. Incorrect number of elements " 
         << flow_array_size << " in histogram " 
         << "underflow/overflow contents" << endl;
    return kFALSE;
  }

  // Retrieve histogram contents as flat array
  T_REAL4 *bins_array;
  T_INT4 bins_array_size = 0;
  msg.Next(&bins_array, &bins_array_size);
  if (!msg.Status()) {
    fLog << "ERROR. Could not get histogram channel contents!" << endl;
    return kFALSE;
  }

  // Check for consistency
  int xbins = histo->GetNbinsX();
  int ybins = histo->GetNbinsY();
  if (bins_array_size != xbins*ybins) {
    fLog << "WARNING. Total number of chanels " << bins_array_size 
         << " retrieved inconsistent with " <<  xbins*ybins << endl;
    return kFALSE;
  }

  // Fill the histogram with the array content
  for (int j = 0; j < ybins; j++) {      // y bins   
    for (int i = 0; i < xbins; i++) {    // x bins
#ifdef ROOT_3_01
      SetBinContent(histo, i+1, j+1, bins_array[i + xbins * j]);    
#else
      histo->SetBinContent(i+1, j+1, bins_array[i + xbins * j]);    
#endif
    }
  }

  // Add the underflow and overflow content
#ifdef ROOT_3_01
  SetBinContent(histo, 0,       ybins+1, flow_array[1]);
  SetBinContent(histo, xbins,   ybins+1, flow_array[2]);
  SetBinContent(histo, xbins+1, ybins+1, flow_array[3]);
  SetBinContent(histo, xbins+1, ybins,   flow_array[4]);
  SetBinContent(histo, xbins+1, 0,       flow_array[5]);
  SetBinContent(histo, xbins,   0,       flow_array[6]);
  SetBinContent(histo, 0,       0,       flow_array[7]);
  SetBinContent(histo, 0,       ybins,   flow_array[8]);
#else    
  histo->SetBinContent(0,       ybins+1, flow_array[1]);
  histo->SetBinContent(xbins,   ybins+1, flow_array[2]);
  histo->SetBinContent(xbins+1, ybins+1, flow_array[3]);
  histo->SetBinContent(xbins+1, ybins,   flow_array[4]);
  histo->SetBinContent(xbins+1, 0,       flow_array[5]);
  histo->SetBinContent(xbins,   0,       flow_array[6]);
  histo->SetBinContent(0,       0,       flow_array[7]);
  histo->SetBinContent(0,       ybins,   flow_array[8]);
#endif

  // Error is not published yet

  // Play the following trick to remove 'histogram name' from the statistics box
  // At present statistic box is not drawn, anyway
  TPaveStats *pst = 
    dynamic_cast<TPaveStats *>(pad->GetPrimitive(stat_str.c_str()));
  if (pst) {
    if (fDebugFlag >> 5 & 0x1) 
      cout << "INFO. Statistics option: " << pst->GetOptStat();
    pst->SetOptStat(111110);
  }
     
  // Get back the label
  TPaveLabel *label = 
    dynamic_cast<TPaveLabel *>(canvas->GetPrimitive(pave_str.c_str()));

  // Update run number and time stamp
  EventStamp(stamp);
  if (label) label->SetLabel(stamp.c_str());

  canvas->Modified();
  canvas->Update();

  // Print all the primitives contained in the canvas
  if (fDebugFlag >> 5 & 0x1) canvas->ls();  

  return kTRUE;
}

//
// Setup RC State message callback. Pass the 'this' reference as an argument
// in the callback routine such that within the callback instance variables
// can be managed.
// srv          - Reference to the RTServer 
// setCallback  - true/false if subscribing/unsubscribing
//
void SvtHistogramProducer::SetRCStateCallback(TipcSrv& srv, 
                           const Bool_t setCallback) 
{
  static T_STR dest = "/runControl/...";
  if (setCallback) {
    // Subscribe to the appropriate subject 
    if (!srv.SubjectSubscribe(dest)) {
      // Send the object reference 'this' to the static method
      srv.SubjectCbCreate(dest, NULL, 
          SvtHistogramProducer::ProcessRCStateMessage, this);
      srv.SubjectSubscribe(dest, kTRUE);
    }
  }
  else {
    // Unsubscribe
    if (srv.SubjectSubscribe(dest)) {
      srv.SubjectSubscribe(dest, kFALSE);
    }
  }
}

//
// Setup command callback. Pass the 'this' reference as an argument
// in the callback routine such that within the callback instance variables
// can be managed.
// srv          - Reference to the RTServer 
// setCallback  - true/false if subscribing/unsubscribing
//
void SvtHistogramProducer::SetCommandCallback(TipcSrv& srv, 
                           const Bool_t setCallback) 
{
  static T_STR dest;

  if (!fClientName.CompareTo("SVTSPYMON")) 
    dest = "/spymon/consumer";
  else
    dest = "/spymon/debug/consumer";

  if (setCallback) {
    // Subscribe to the appropriate subject 
    if (!srv.SubjectSubscribe(dest)) {
      // Send the object reference 'this' to the static method
      srv.SubjectCbCreate(dest, NULL, 
          SvtHistogramProducer::ProcessCommandMessage, this);
      srv.SubjectSubscribe(dest, kTRUE);
    }
  }
  else {
    // Unsubscribe
    if (srv.SubjectSubscribe(dest)) {
      srv.SubjectSubscribe(dest, kFALSE);
    }
  }
}

//
// Setup Histogram message callback. Pass the 'this' reference as an argument
// in the callback routine such that within the callback instance variables
// can be managed.
// srv          - Reference to the RTServer 
// setCallback  - true/false if subscribing/unsubscribing
//
void SvtHistogramProducer::SetHistoCallback(TipcSrv& srv, 
                           const Bool_t setCallback) 
{
  static T_STR dest = "/spymon/histo";
  if (setCallback) {
    // Subscribe to the appropriate subject 
    if (!srv.SubjectSubscribe(dest)) {
      // Send the object reference 'this' to the static method
      srv.SubjectCbCreate(dest, NULL, 
          SvtHistogramProducer::ProcessHistogramMessage, this);
      srv.SubjectSubscribe(dest, kTRUE);
    }
  }
  else {
    // Unsubscribe
    if (srv.SubjectSubscribe(dest)) {
      srv.SubjectSubscribe(dest, kFALSE);
    }
  }
}

//
// Setup beam finder message callback. Pass the 'this' reference as an argument
// in the callback routine such that within the callback instance variables
// can be managed.
// srv          - Reference to the RTServer 
// setCallback  - true/false if subscribing/unsubscribing
//
void SvtHistogramProducer::SetBeamFinderCallback(TipcSrv& srv, 
                           const Bool_t setCallback) 
{
  static T_STR dest = "/spymon/beam";

  if (setCallback) {
    // Subscribe to the appropriate subject 
    if (!srv.SubjectSubscribe(dest)) {
      // Send the object reference 'this' to the static method
      srv.SubjectCbCreate(dest, NULL, 
          SvtHistogramProducer::ProcessBeamFinderMessage, this);
      srv.SubjectSubscribe(dest, kTRUE);
    }
  }
  else {
    // Unsubscribe
    if (srv.SubjectSubscribe(dest)) {
      srv.SubjectSubscribe(dest, kFALSE);
    }
  }
}

//
// Setup beam finder message callback. Pass the 'this' reference as an argument
// in the callback routine such that within the callback instance variables
// can be managed.
// srv          - Reference to the RTServer 
// setCallback  - true/false if subscribing/unsubscribing
//
void SvtHistogramProducer::SetBeamFinderWedgeCallback(TipcSrv& srv, 
                           const Bool_t setCallback) 
{
  static T_STR dest = "/spymon/beam/wedge_fit";

  if (setCallback) {
    // Subscribe to the appropriate subject 
    if (!srv.SubjectSubscribe(dest)) {
      // Send the object reference 'this' to the static method
      srv.SubjectCbCreate(dest, NULL, 
          SvtHistogramProducer::ProcessBeamFinderWedgeMessage, this);
      srv.SubjectSubscribe(dest, kTRUE);
    }
  }
  else {
    // Unsubscribe
    if (srv.SubjectSubscribe(dest)) {
      srv.SubjectSubscribe(dest, kFALSE);
    }
  }
}

// 
// Initialise and establish connection to the RTServer 
// Try to see if the default init method sometime
//
void SvtHistogramProducer::InitSrv(TipcSrv& srv) {
  string configFileName = getenv("SMARTSOCKETS_CONFIG_DIR");
  configFileName += "/unix.cm";
  cout << "Config File: " << configFileName << endl; 

  // Read SmartSockets configuration from a standard file, works now
  TutCommandParseFile(const_cast<char *>(configFileName.c_str()));

  // Connect to RTserver 
  if (!srv.Create(T_IPC_SRV_CONN_FULL)) {
    TutOut("ERROR. InitSrv: Could not connect to RTserver!\n");
    exit(T_EXIT_FAILURE);
  }
  else {
    TutOut("INFO. Connected to RT Server...\n");
  }
}

//
// Close connection to RT Server 
//
void SvtHistogramProducer::CloseSrv(TipcSrv& srv) {
  SvtHistogramProducer::DisconnectRT(srv);
}

//
// Close connection to RT Server 
//
void SvtHistogramProducer::DisconnectRT(TipcSrv& srv) {
  if (srv) srv.Destroy(T_IPC_SRV_CONN_NONE); // disconnect from RTserver
}


//
// Save histograms in a root file (no run number given - filename )
//
Bool_t SvtHistogramProducer::Save() {  
  // Save the most recent histograms in a file w/o run number
  static string dirname(getenv("SVTMON_DATA_DIR"));
  static string wwwdir(getenv("SVTMON_WWWDIR"));

  ostringstream rootfile;
  rootfile << dirname << "/" << fClientName.Data() << "_current.root";
  (fSaveAsOnline) ? SaveTree(rootfile.str(), "RECREATE")
                  : SaveHistogram(rootfile.str(), "RECREATE");

  ostringstream psfile;
  psfile << wwwdir << "/ps_files/" << fClientName.Data() << "_current.ps";
  CreatePS(psfile.str());

  return kTRUE;
}


//
// Save histograms in a root file 
//
Bool_t SvtHistogramProducer::Save(const Int_t runNo) {  
  // Save the most recent histograms in a file with run number
  static string dirname(getenv("SVTMON_DATA_DIR"));
  static string wwwdir(getenv("SVTMON_WWWDIR"));

  ostringstream rootfile;
  rootfile   << dirname << "/root_files/" 
             << fClientName.Data() << "_" << runNo << ".root";

  (fSaveAsOnline) ? SaveTree(rootfile.str(), "RECREATE")
                  : SaveHistogram(rootfile.str(), "RECREATE");

  // If the run is good save a postscript file as well
  if (IsGoodRun()) {
    fLog << "INFO. Save: Good Run, saving ps file ..." << endl; 
    ostringstream psfile;
    psfile << wwwdir << "/ps_files/" << fClientName.Data() << "_lastrun.ps";
    CreatePS(psfile.str());
  }

  return kTRUE;
}

//
// Test if the run is worth looking at later on
//
Bool_t SvtHistogramProducer::IsGoodRun() {
  return (GetNEvt() > 100 && nmess[7] > 1);    // XTRP crate
}
//
// Extract the number of events processed so far. Looks at a list
// of histograms and extracts the first number > 0
//
Int_t SvtHistogramProducer::GetNEvt() {
  Int_t nevt = 0;

  // Try to extract the # of events for the collection
  Int_t narr = NEL(dhname);
  for (int i = 0; i < narr; i++) {
    string obj = dhname[i];
    TCanvas *canvas = dynamic_cast<TCanvas *>(GetCanvasList()->FindObject(obj.c_str()));
    if (!canvas) continue;
    canvas->cd();

    TPad *pad = dynamic_cast<TPad *>(canvas->GetPrimitive(canvas->GetName()));
    if (!pad) continue;
    pad->cd();

    string hname = obj + hext;

    // Get back the histogram from the List
    TH1 *histo = dynamic_cast<TH1 *>(GetHistList()->FindObject(hname.c_str())); 
    if (!histo) continue;

    nevt = static_cast<Int_t>(histo->GetEntries());
    if (nevt > 0) break;
  }
  return nevt;  
}
// 
// Save histograms in a root file 
//
Bool_t SvtHistogramProducer::CreatePS(const string& filename) {
  static string imgtype(".jpeg");
  static string bdir(getenv("SVTMON_WWWDIR"));
  if (GetDebugFlag() >> 2 & 0x1) cout << "PS File: " << filename << endl;
  TPostScript *ps = new TPostScript(filename.c_str(), 112);
  if (!ps) {
    cerr << "ERROR. Cannot open the ps file " << filename << endl;
    return kFALSE;
  }

#if 0
  string htmlfile(filename);
  htmlfile.replace(htmlfile.length()-2,2,"html");
  ofstream fHtml(htmlfile.c_str(), ios::out);
  if (!fHtml) {
    cerr << "ERROR. Could not open html file " 
         << htmlfile << " for writing " << endl;
    return kFALSE;
  }
  WriteHeader(fHtml);
#endif

  // Now open the file which contains the list of canvases
  string infile = bdir + "/ps_files/validation_list.cfg";
  ifstream fin(infile.c_str(), ios::in);
  if (!fin) {
    cerr << "ERROR. Could not open the configuration file " 
         << infile << " for reading " << endl;
    ps->Close();
    return kFALSE;
  }

  // Loop over the list of canvases and do what is needed
  char buf[SZ];
  while (fin.getline(buf, SZ)) { // Removes \n
    char* cp = buf;
    TCanvas *can = dynamic_cast<TCanvas *>(gROOT->GetListOfCanvases()->FindObject(cp));
    if (!can) continue;

    ps->NewPage();
    can->Draw();
    can->Update();

#if 0
    // Replace space with "_" and construct a new canvas name
    // which is subsequently used to create the eps/image files
    string str(can->GetName());
    vector<string> tokens;
    tokenize(str, tokens);

    int len = tokens.size();
    if (len <= 0) continue;

    // Use string buffers instead of immuatble string for efficiency
    // This makes life complicated with possibility of memory leak
    // when the internal character buffer is extracted
    // 'stringstream' should be used when available
    ostringstream newstr;
    for (int i = 0; i < len; i++) {
      newstr << tokens[i]; 
      if (i < len-1) newstr << "_";
    }
    char *newstr_char = newstr.str().c_str();

    if (0) cout << ">>> newstr = " << newstr_char << endl;

    ostringstream epsfile;
    epsfile << bdir << "/ps_files/image/" << newstr_char << ".eps";
    char *epsfile_char = epsfile.str().c_str();
    can->Print(epsfile_char, "eps");

    string imgfile(epsfile_char);
    imgfile.replace(imgfile.length()-4,4,imgtype);

    // Must escape the ugly parentheses for the command to work
    ostringstream command;
    command << "convert " << "\'" << epsfile_char << "\'" << " \'" << imgfile << "\'";
    char *comstr = command.str().c_str();
    if (0) cout << ">>> Command: " << comstr << endl;
    gSystem->Exec(comstr);

    fHtml << "<LI> <A HREF=\"javascript:popUp(\'" 
          << "image/" << newstr_char << imgtype << "\');\">" 
          << str << "</A></LI>" << endl;

#endif
  }
#if 0
  WriteFooter(fHtml);
#endif
  // Now wrap up
  fin.close();
  ps->Close();
  delete ps;

#if 0
  fHtml.close();
#endif
  return kTRUE;
}
// 
// Save histograms in a root file 
//
Bool_t SvtHistogramProducer::SaveHistogram(const string& filename, 
                                           const string& opt) 
{
  fLog << "Saving histograms in " << filename << " at: "<< timestamp(time(NULL)) << endl;

  Int_t nHist = GetNHistograms();

  if (!nHist) {
    fLog << "WARNING. SaveHistogram: No histogram in the list, size = " 
         << nHist << endl;
    return kFALSE;
  }
     
  if (GetDebugFlag() >> 2 & 0x1) cout << "Root File: " << filename << endl;
  TFile *file = new TFile(filename.c_str(), opt.c_str());
  if (!file) {
    cout << "WARNING. SaveHistogram: File " << filename 
         << " cannot be opened for writing!!" << endl;
    return kFALSE;
  }

  gROOT->GetListOfCanvases()->Write();

  file->Write();
  file->Close();
  delete file;

  return kTRUE;
}
// 
// Save histograms in a root file 
//
void SvtHistogramProducer::GetCanvasAtPath(const string& path, TSeqCollection& list) 
{
  string fullPath = fClientName.Data() + path;

  list.Clear("nodelete");

  TSeqCollection *canList = GetCanvasList();
  for (int i = 0; i < canList->GetSize(); i++) {
    TCanvas *canvas = dynamic_cast<TCanvas *>(canList->At(i));
    if (!canvas) continue;
    string folder(fConsInfo->getPath(canvas->GetName()));
    if (folder == fullPath) list.Add(canvas);
  }
}
//
// Add canvases to the corresponding directory
//
void SvtHistogramProducer::AddCanvasToPath(const string& path, TSeqCollection& list) 
{
  static string pat("/");
  unsigned int index = path.find(pat, 1);
  string name = path.substr(1, index-1);

  if (path == EMPTY) {
    list.Write();
  }
  else {
    TDirectory *dir = dynamic_cast<TDirectory *>(gDirectory->FindObject(name.c_str()));
    if (!dir) dir = gDirectory->mkdir(name.c_str());    
    dir->cd();   // Now becomes the current directory

    string subpath = (index != string::npos) ? path.substr(index) : EMPTY;
    AddCanvasToPath(subpath, list);
  }
}
// 
// Save histograms in a root file following the same tree structure as  displayed online
//
Bool_t SvtHistogramProducer::SaveTree(const string& filename, const string& opt) 
{
  fLog << "Saving canvases in " << filename << " at: "<< timestamp(time(NULL)) << endl;

  // Open output file
  TFile *file = new TFile(filename.c_str(), opt.c_str());
  if (!file) {
    cout << "WARNING. SaveHistogram: File " << filename 
         << " cannot be opened for writing!!" << endl;
    return kFALSE;
  }
  file->cd();  // probably implicit

  // Create the top directory (SVTSPYMON) in this file and go there
  TDirectory *svt = file->mkdir(fClientName);
  svt->cd();

  for (unsigned int i = 0; i < uniqPath.size(); i++) {
    TList list;
    GetCanvasAtPath(uniqPath[i], list);
    AddCanvasToPath(uniqPath[i], list);
    svt->cd();
  }

  file->cd();
  file->Write();
  file->Close();
  delete file;

  // Come back to the global dir level, probably implicit
  gROOT->cd();

  return kTRUE;
}

//
// Write a log file
//
Bool_t SvtHistogramProducer::WriteLog(const Int_t runNo) {
  static string bdir = getenv("SVTMON_DATA_DIR"); 
  // Open Log file 
  ostringstring source;
  source << bdir << "/log_files/" << fClientName.Data() << "_current.log";

  ostringstream dest;
  dest << bdir << "/log_files/" 
       << fClientName.Data() << "_" << runNo << ".log";

  ostringstring command;
  command <<  "cp " << source.str() << " " << dest.str();

  const char* command_cstr = command.str().c_str();
  fLog << command_cstr << endl;
  fLog << "Finising Session at: " << timestamp(time(NULL)) << endl;
  fLog.close();  

  gSystem->Exec(command_cstr); 
  
  return kTRUE;
}

void SvtHistogramProducer::EventStamp(string& str) {
  static string rtag(" Run Number = ");
  static string ttag("   Last Updated at = ");
  ostringstream ostmp;
  ostmp << rtag << fSvtmonRun << ttag <<  timestamp(fSvtmonTime);
  str = ostmp.str();
}

//
// Print unique folder names
//
// void SvtHistogramProducer::PrintUPath() {
//   cout << "INFO. Unique folder names are: " << endl;
//   ostream_iterator<string> out(cout, "\n");
//   copy(uniqPath.begin(), uniqPath.end(), out);
//   cout << endl;
// }
void SvtHistogramProducer::PrintUPath(const vector<string>& uniqPath) {
  cout << "INFO. Unique folder names are: " << endl;
  for (unsigned int i = 0; i < uniqPath.size(); i++)
    cout << uniqPath[i] << endl;
  cout << endl;
}
//
// Initial comments
//
void SvtHistogramProducer::WriteHeader(ofstream& fHtml) {
  // fHtml << "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">" << endl;
  fHtml << "<HTML>" << endl
        << "<HEAD>" << endl 
        << "   <META HTTP-EQUIV=\"refresh\" CONTENT=\"30\">" << endl
        << "   <TITLE>Validation plots for Current Run " << fSvtmonRun << "</TITLE>" << endl
        << "   <META NAME=\"Author\" CONTENT=\"SVTSPYMON\">" << endl
        << "</HEAD>" << endl
        << "<SCRIPT LANGUAGE=\"JavaScript\">" << endl
        << "<!-- Begin " << endl
        << "function popUp(image) { " << endl
        << "  windowprops = \"height=400,width=600,location=no,\" " << endl
        << "    +   \"scrollbars=no,menubars=no,toolbars=no,resizable=yes\"; " << endl
        << "  window.open(image, \"Popup\", windowprops);" << endl
        << "}" << endl
        << "//  End --> " << endl
        << "</script>" << endl
        << "<BODY BGCOLOR=\"#ffffff\" LINK=\"#CC0000\" VLINK=\"#999966\">" << endl
        << "<H2><FONT COLOR=blue>Run Number </FONT> = " << fSvtmonRun << "</H2>" << endl
        << "<BR>Last update at: " << timestamp(fSvtmonTime) << endl
        << "<UL>" << endl;
}
//
// Footer
//
void SvtHistogramProducer::WriteFooter(ofstream& fHtml) {
  fHtml << "</UL>" << endl
        << "</BODY></HTML>" << endl;
}
// 
// Interrupt (Control C) handler 
//
static void cntrl_c_handler(int sig) {
  char answer[10];
  cerr << "Interrupt received! Signal = " << sig << endl << 
          "INPUT. Do you wish to continue(c) or quit(q)? ";
  cin >> answer;
  cerr << endl;
  if (*answer == 'c') {
    cout << "INFO. Reinstalling the Interrupt handler " << endl;
    signal(SIGINT, cntrl_c_handler);
  }
  else {
    cout << "INFO. Shutting down connection to RT Server " << endl;
    TipcSrv& srv = TipcSrv::Instance();      // Obtain singleton handle to SS Server

    const T_STR subject = "/spymon/consumer";

    // Create a message 
    TipcMsg msg(T_MT_STRING_DATA);
    if (srv.SubjectSubscribe(subject))
      msg.Dest(subject);
    else 
      msg.Dest("/spymon/debug/consumer");

    // Build the message 
    msg << "Stop" << Check;
    if (!msg) {
      TutOut("ERROR. handler: Could not append fields of TipcMsg object\n");
    }
    else {
      // Publish the message 
      srv.Send(msg);
      srv.Flush();

      // Most probably you need to sleep for a second here
      // sleep (1);
    }
  }
}

//
// Set global Root options
//
static void SetRootOptions() {
  gStyle->SetMarkerStyle(1);
  gStyle->SetTitleOffset(1.0);
  gStyle->SetTitleOffset(1.2, "Y");
  gStyle->SetLabelSize(.03, "X");
  gStyle->SetLabelSize(.03, "Y");
  gStyle->SetNdivisions(510, "X");
  gStyle->SetNdivisions(510, "Y");
}

#ifdef ROOT_3_01
// Temporary solution for adding bin contents
static void SetBinContent(TH1 *h, Int_t binx, Int_t biny, Stat_t content) {
  if (binx < 0 || binx > h->GetXaxis()->GetNbins()+1) return;
  if (biny < 0 || biny > h->GetYaxis()->GetNbins()+1) return;
  h->SetBinContent(biny*(h->GetXaxis()->GetNbins()+2) + binx, content);
}
#endif

//
// Convert time() to time stamp string
//
static char* timestamp(const int itime) {
  static char str[64];   // static is very much needed, otherwise 'str' is on the stack and
                         // disappears as soon as the method returns
  time_t time_now = static_cast<time_t>(itime);
  struct tm *time_struct = localtime(&time_now);  // translates time into struct 
  sprintf(str, "20%02d/%02d/%02d %02d:%02d:%02d",
          (*time_struct).tm_year-100, (*time_struct).tm_mon+1,
          (*time_struct).tm_mday, (*time_struct).tm_hour,
          (*time_struct).tm_min,  (*time_struct).tm_sec );
  return str;
}

static void tokenize(const string& str,
                      vector<string>& tokens,
                      const string& delimiters)
{
  // Skip delimiters at beginning.
  string::size_type lastPos = str.find_first_not_of(delimiters, 0);

  // Find first "non-delimiter".
  string::size_type pos = str.find_first_of(delimiters, lastPos);

  while (string::npos != pos || string::npos != lastPos)  {
    // Found a token, add it to the vector.
    tokens.push_back(str.substr(lastPos, pos - lastPos));

    // Skip delimiters.  Note the "not_of"
    lastPos = str.find_first_not_of(delimiters, pos);

    // Find next "non-delimiter"
    pos = str.find_first_of(delimiters, lastPos);
  }
}

void MyErrorHandler(int level, Bool_t abort, const char* location, const char *msg) {
}
//
// Main entry point 
//
//extern void InitGui(); 
//VoidFuncPtr_t initfuncs[] = {InitGui, 0};
//static TROOT rootP("SVT Consumer", "Root HistogramProducer", initfuncs);

// ---------------------
// Implement GetOpt
// ---------------------

int main(int argc, char **argv) {
  // Interpret the command line arguments first
  Int_t partition = 0;
  Char_t *name    = 0;
  Int_t  dflag    = 1;
  Int_t rcopt     = 0;
  Bool_t asOnline = kFALSE;
  UInt_t port     = 9050;

  // Get a time  
  time_t now, then;

  if (argc > 1) partition  = atoi(argv[1]);  // Partition to watch
  if (argc > 2) name       = argv[2];        // Consumer name i.e SVTSPYMON
  if (argc > 3) dflag      = atoi(argv[3]);  // Debug flag
  if (argc > 4) rcopt      = atoi(argv[4]);  // RC option, if on subscribes to RC messages
  if (argc > 5) asOnline   = (atoi(argv[5]) > 0) ? kTRUE : kFALSE;  // save the online folder structure
  if (argc > 6) port       = atoi(argv[6]);  // Server port #
  if (!name)    name       = "SVTSPYMON";
  
  TApplication app("app", &argc, argv);    // Root application

  gROOT->SetBatch(kTRUE);                  // Set batch mode of running
  SetRootOptions();                        // Set Default style
 
  // Instantiate the Root histogram producer
  TString cname(name);
  SvtHistogramProducer prod(partition, cname, dflag, asOnline, port);

  TipcSrv& srv = TipcSrv::Instance();      // Obtain singleton handle to SS Server

  prod.InitSrv(srv);                       // Connect to RTServer

  prod.SetCommandCallback(srv, kTRUE);     // Subscribe to command message
  prod.SetBeamFinderCallback(srv, kTRUE);  //              beam finder messages
                                           //              beam finder wedge result messages
  prod.SetBeamFinderWedgeCallback(srv, kTRUE);  
  prod.SetHistoCallback(srv, kTRUE);       //              histogramming messages
  if (rcopt > 0) 
    prod.SetRCStateCallback(srv, kTRUE);   //              RC State message

  // Setup interrupt handler
  signal(SIGINT, cntrl_c_handler);

  // Initialise then time 
  then = time(&then);

  // Read and process all incoming messages
  while (!prod.GetStopFlag()) {            // Check the 'producer' stop flag  
    srv.MainLoop(10.0);                    // every 10 secs
    now = time(&now);                      // Save latest histogram in a root file every
    if (difftime(now, then) > 60.) {       // 60 seconds
      prod.Save(); 
      then = mktime(localtime (&now));
                                           // Track memory leak using in-built tool. However, this
                                           // tool only knows about ROOT classes (naturally)
      if (prod.GetDebugFlag() >> 2 & 0x1) gObjectTable->Print();     
    }
  }
  prod.SetCommandCallback(srv, kFALSE);    // Unsubscribe from command messages
  prod.SetBeamFinderCallback(srv, kFALSE); //                  histogramming messages
                                           //                  wedge fit messages 
  prod.SetBeamFinderWedgeCallback(srv, kFALSE); 
  prod.SetHistoCallback(srv, kFALSE);      //                  histogramming messages
  if (rcopt > 0) 
    prod.SetRCStateCallback(srv, kFALSE);  //                  RC State message

  prod.CloseSrv(srv);                      // Now close connection with the RTServer and quit

  // Save the most recent histograms and write out the log messages
  Int_t run = prod.GetMonitorRun();
  prod.Save(run);
  prod.WriteLog(run);

  return T_EXIT_SUCCESS;
}
