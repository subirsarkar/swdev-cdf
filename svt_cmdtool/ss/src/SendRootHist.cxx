#include <iostream>
#include <iomanip>
#include <fstream>
#include <strstream>

// C++ style inclusion of Standard C library  
#include <cstdlib>
#include <ctime>

#include "TROOT.h"
#include "TApplication.h"
#include "TH1.h"
#include "TFile.h"
#include "TKey.h"

#include "rtworks/cxxipc.hxx"
#include "messages/SVTHistoMessage.h"

// ----------------
// Main entry point 
// ----------------
extern void InitGui(); 
VoidFuncPtr_t initfuncs[] = {InitGui, 0};
static TROOT rootP("Sender", "Root Histogram Sender", initfuncs);

void InitSrv(TipcSrv& srv);
void CreateHistoMessage (void);
void Pack1DHist (TH1& histo, TipcMsg& submsg, Int_t index);
void Pack2DHist (TH1& histo, TipcMsg& submsg, Int_t index);

static const Bool_t DEBUG = kFALSE;

int main(int argc, char *argv[]) {
  // Interpret the command line arguments first
  Char_t *name = 0;
  Char_t *dest = "/spymon/debug/histo";
  Int_t run_number = 128350;

  if (argc < 2) {
    cout << "Usage: " << argv[0] << " <Root Filename> [<Destination> <Run Number>]" << endl;
    exit(1);
  }
  if (argc > 1) name       = argv[1];
  if (argc > 2) dest       = argv[2];
  if (argc > 3) run_number = atoi(argv[3]);

  cout << "Sending Histogram to " << dest << " From " << name << endl;

  gROOT->SetBatch(kTRUE);                  // Set batch mode of running

  TipcSrv& srv = TipcSrv::Instance();      // Obtain singleton handle to SS Server
  InitSrv(srv);

  CreateHistoMessage();

  ostrstream filename;
  filename << name << ends;

  TFile *file = new TFile(filename.str());

  if (!file) {
    cout << "File " << filename.str() << " could not be opened! " << endl;
    exit(1);
  }

  if (file->IsZombie()) {
    cout << filename.str() << " not a root file! " << endl;
    file->Close();
    exit(1);
  }

  TipcMt mt(SVTHistoContatiner_MessageType);
  TipcMsg msg(mt);

  if (!msg) {
    TutOut("Could not create message.\n");
    return T_EXIT_FAILURE;
  }
  msg.NumFields(0);

  msg << (T_INT4)run_number << (T_INT4)time(NULL) << Check;

  TH1 *fHist = 0;

  TKey *key;
  TIter it(file->GetListOfKeys());
  Int_t nhist = 0;
  while ((key = (TKey*) it())) {
    if (!strcmp(key->GetClassName(), "TH1F") || 
        !strcmp(key->GetClassName(), "TH2F")) {
      fHist = (TH1 *) key->ReadObj();
      cout << fHist->GetName() << endl;      
      nhist++;
    }
  }

  TipcMsg msg_array[nhist];

  TIter xit(file->GetListOfKeys());
  Int_t index = 0;
  while ((key = (TKey*) xit())) {
    if (!strcmp(key->GetClassName(), "TH1F") || 
        !strcmp(key->GetClassName(), "TH2F")) {
      fHist = (TH1 *) key->ReadObj();

      if ( strcmp(fHist->ClassName(), "TH1F") == 0 ) 
        Pack1DHist(*fHist, msg_array[index], index);
      else
        Pack2DHist(*fHist, msg_array[index], index);
      index++;
    }
  }
  file->Close();

  cout << "nhist = " << nhist << endl;
  msg.Append(msg_array, nhist);
  if (DEBUG) msg.Print(TutOut);

  // -------------------
  // Publish the message
  // ------------------- 
  msg.Dest(dest);

  srv.Send(msg);
  srv.Flush();
  srv.Destroy(T_IPC_SRV_CONN_NONE); // force blocking close

  return T_EXIT_SUCCESS;
}
// -------------------------------------------------------
// Create a 1D Histogram message from a Root 1D histogram
// -------------------------------------------------------
void Pack1DHist (TH1& histo, TipcMsg& submsg, Int_t index) {
  TipcMt mt(SVTHisto1D_MessageType);
  submsg.Create(mt);
  
  submsg.NumFields(0);
  
  T_INT4 hid = 10000 + index;
  submsg << hid << Check; // Dummy Histogram ID
  Int_t nbins = histo.GetNbinsX();
  submsg.Append(const_cast<char *>(histo.GetName()));
  submsg.Append(static_cast<T_INT4>(nbins));
  submsg.Append(static_cast<T_REAL4>(histo.GetXaxis()->GetXmin()));
  submsg.Append(static_cast<T_REAL4>(histo.GetXaxis()->GetXmax()));
  submsg.Append(static_cast<T_INT4>(histo.GetEntries()));
  submsg.Append(static_cast<T_INT4>(histo.GetBinContent(0)));
  submsg.Append(static_cast<T_INT4>(histo.GetBinContent(nbins+1)));

  Float_t cont[nbins];
  Float_t error[nbins];
  for (int i = 0; i < nbins; i++) {
    cont[i]  = histo.GetBinContent(i+1);
    error[i] = histo.GetBinError(i+1);
  }
  submsg.Append(cont,  nbins);
  submsg.Append(error, nbins);

  submsg.UserProp(1);
  submsg.Dest("/histogram/1D"); // Where do you get the folder name?

  if (DEBUG) submsg.Print(TutOut);
}
// -------------------------------------------------------
// Create a 2D Histogram message from a Root 2D histogram
// -------------------------------------------------------
void Pack2DHist (TH1& histo, TipcMsg& submsg, Int_t index) {
  TipcMt mt(SVTHisto2D_MessageType);
  submsg.Create(mt);

  submsg.NumFields(0);
  T_INT4 hid = 20000 + index;
  submsg << hid << Check; // Dummy Histogram ID
  submsg.Append(const_cast<char *>(histo.GetName()));

  Int_t nxbins = histo.GetNbinsX();
  Int_t nybins = histo.GetNbinsY();
  T_INT4 nbins[] = {nxbins, nybins};
  submsg.Append(nbins, 2);

  T_REAL4 range[] = {
    histo.GetXaxis()->GetXmin(),
    histo.GetXaxis()->GetXmax(),
    histo.GetYaxis()->GetXmin(),
    histo.GetYaxis()->GetXmax()
  };
  submsg.Append(range, 4);
  submsg.Append(static_cast<T_INT4>(histo.GetEntries()));

  T_REAL4 uvflow[] = {
      histo.GetBinContent(nbins[0],   nbins[1]),
      histo.GetBinContent(0,          nbins[1]+1),
      histo.GetBinContent(nbins[0],   nbins[1]+1),
      histo.GetBinContent(nbins[0]+1, nbins[1]+1),
      histo.GetBinContent(nbins[0]+1, nbins[1]),
      histo.GetBinContent(nbins[0]+1, 0),
      histo.GetBinContent(nbins[0],   0),
      histo.GetBinContent(0,          0),
      histo.GetBinContent(0,   nbins[1])
  };
  submsg.Append(uvflow, 9);

  // Histogram content
  Float_t cont[nbins[0]*nbins[1]];
  for (int j = 0; j < nbins[1]; j++) {      // y bins   
    for (int i = 0; i < nbins[0]; i++) {    // x bins
      cont[i+nbins[0]*j] = histo.GetBinContent(i+1, j+1);    
    }
  }
  submsg.Append(cont, nbins[0]*nbins[1]);

  // -----------------------------
  // Errors are not yet published
  // ----------------------------- 
  // Float_t error[nbins[0]*nbins[1]];
  // submsg.Append(error, nbins[0]*nbins[1]);

  submsg.UserProp(1);
  submsg.Dest("/histogram/2D"); // Where do you get the folder name?
}
// -----------------------------------------------------
// Initialise and establish connection to the RTServer 
// Try to see if the default init method sometime
// -----------------------------------------------------
void InitSrv(TipcSrv& srv) {
  ostrstream configFileName;
  configFileName << getenv("SMARTSOCKETS_CONFIG_DIR") << "/unix.cm" << ends;
  cout << "Config File: " << configFileName.str() << endl; 

  // Read SmartSockets configuration from a standard file, Does not work !!!!
  TutCommandParseFile(configFileName.str());
  // Connect to RTserver 
  if (!srv.Create(T_IPC_SRV_CONN_FULL)) {
    TutOut("ERROR. InitSrv: Could not connect to RTserver!\n");
    exit(T_EXIT_FAILURE);
  }
  else {
    TutOut("INFO. Connected to RT Server...\n");
  }
}
// ---------------------------------------------------------------------
// Create Histogram message types which are published by the SVT Crates.
// At present, 1D and 2D are in use
// ----------------------------------------------------------------------
void CreateHistoMessage (void) {
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
