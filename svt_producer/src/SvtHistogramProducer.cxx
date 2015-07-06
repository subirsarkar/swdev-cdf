// ----------------------------------------------------------------------------------
// This application collects histograms and other monitoring data from the
// SPYMON program and makes them available to clients like the standard 
// histogram browser, HistoDisplayMain. The Display Server classes developed in
// the Consumer Framework form the basis of this application. The source of data,
// however, is not the usual data stream out of the CSL but those recorded by the SVT
// boards for the purpose of monitoring the functioning of the SVT system online. 
// In this respect, this is different from the other consumers. 
// The communication between the SPYMON processes and this application takes place 
// through SmartSockets. The program sets histogram related callback(s) and sits in 
// an infinite loop waiting for histogram  mesasges. When the histogram callback is 
// triggered, the histogram message is converted into root histograms, or already 
// existing histograms are updated. After every n iterations, where n can be configured 
// the histograms are sent to the Display Server which runs under the Consumer framework. 
// Presently, histograms are sent at each iteration. 
// 
// Subject areas:
// 
// /spymon/histo    - Histogram related message
// /spymon/beam     - Beam finder histogram message
// /spymon/consumer - Any command message 
//                     o Save  - Save histograms in a file with time stamp
//                     o Write - Write a log file with time stamp
//                     o Stop  - Stop the consumer
// /runControl/...  - Messages from the Run Control about the state of DAQ 
//
// S. Sarkar  April 14, 2001
// May 18, 2001  Added support for 2 dimensional histograms
// -------------------------------------------------------------------------------------

// Standard C++ library  
#include <iostream>
#include <strstream>

// C++ style inclusion of Standard c library  
#include <cstdio>
#include <cstdlib>
#include <csignal>

// Root library 
#include "TROOT.h"
#include "TApplication.h"
#include "TStyle.h"
#include "TH1.h"
#include "TH2.h"
#include "TCanvas.h"
#include "TFile.h"
#include "TString.h"
#include "TPaveStats.h"

// Histogram message types 
#include "SVTHistoMessage.h"

// Class definition 
#include "SvtHistogramProducer.h"

Int_t SvtHistogramProducer::nIter = 0;
static char *clientName = "producer_test";

// Prototype for static functions 
static void cntrl_c_handler(int sig);
static Int_t SplitString(const T_STR title, Int_t hopt, TString& newTitle, 
                                            TString& gTitle, TString& yopt);
static void SplitRCState (const T_STR iStr, Int_t *partition, Int_t *run, TString& state);
static void SetRootOptions();

//
// Class Constructor 
// dFlag  - Debug Flag
// nHist  - Number of histograms to be sent known at startup, the TList
//          will expand itself as and when necessary
// port   - Port number where the server could be connected, default 9050 
//
SvtHistogramProducer::SvtHistogramProducer(Int_t dFlag, UInt_t nMaxHist, UInt_t port)
  : fDebugFlag(dFlag), fNMaxHist(nMaxHist), fPort(port),
    fRunNo(117971), fPartition(-1), fRCState("UNKNOWN")
{
  fStopFlag  = kFALSE;
  fHistList  = new TList();
  fHistList->SetOwner(kTRUE);

  fCanvasList  = new TList();
  fCanvasList->SetOwner(kTRUE);

  if (fDebugFlag > 3) 
    cout << "Constructing SvtHistogramProducer " << fRunNo << "  " << fPort << endl;
  fConsExp  = new ConsumerExport(fPort, kTRUE);
  fConsInfo = new TConsumerInfo(clientName, fRunNo);
  createHistoMessage();
}

//
// Destructor 
//
SvtHistogramProducer::~SvtHistogramProducer() {
  if (!fHistList->IsEmpty()) fHistList->Delete();
  delete fHistList;

  if (!fCanvasList->IsEmpty()) fCanvasList->Delete();
  delete fCanvasList;

  delete fConsExp;
  delete fConsInfo;
}

//
// Create Histogram message types which are published by the SVT Crates.
// At present, 1D and 2D are in use
//
void SvtHistogramProducer::createHistoMessage (void) {
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
// This message should have two fields for now, 
//        T_STR  "Save", "Stop", "Write" etc.
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
  T_STR command;
  TipcMsg msg(data->msg);

  // Get the object reference back in this static method
  SvtHistogramProducer *self = (SvtHistogramProducer *) arg;

  if (self->fDebugFlag > 3) {
    TipcMt mt = msg.Type();
    cout << "Type " <<  mt.Name() << endl;
  }

  msg >> command >> Check;
  if (self->fDebugFlag) 
    cout << "Message Received, Command: " << command << endl;
  if (!strcmp(command, "stop") ||
      !strcmp(command, "Stop")) {
    if (self->fDebugFlag) 
      cout << "-> Stopping " << clientName << endl;
    self->fStopFlag = kTRUE;
  }
  else if (!strcmp(command, "save") || 
           !strcmp(command, "Save")) {
    self->Save();
  }
  else if (!strcmp(command, "write") || 
           !strcmp(command, "Write")) {
    self->WriteLog();
  }
  else {
    cout << "Unknown command:  " << command << endl;
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
  TipcMt mt;
  T_STR  name;

  // print out the name of the type of the message 
  mt   = msg.Type();
  name = mt.Name();
  TutOut("ProcessDefault: Unexpected message type name is <%s>\n", name);
} 

//
// Beam Finding related message (/spymon/beam)
//
void SvtHistogramProducer::ProcessBeamFinderMessage (
           T_IPC_CONN conn, T_IPC_CONN_PROCESS_CB_DATA data, 
           T_CB_ARG arg)
{
  TipcMsg msg(data->msg);
  TipcMt mt;
  T_STR  name;

  // Print out the name of the type of the message 
  mt   = msg.Type();
  name = mt.Name();
  TutOut("ProcessBeamFinderMessage: Message type name is <%s>\n", name);
  msg.Print(TutOut);
} 

//
// Listen to state of Data Acquisition, namely, partition #, RC state,
// Run # etc.
//
void SvtHistogramProducer::ProcessRCStateMessage (
           T_IPC_CONN conn, T_IPC_CONN_PROCESS_CB_DATA data, 
           T_CB_ARG arg)
{
  TipcMsg msg(data->msg);
  TipcMt mt;
  T_STR  name, c_str;

  // Get the object reference back in this static method
  SvtHistogramProducer *self = (SvtHistogramProducer *) arg;

  // Print out the name of the type of the message 
  mt   = msg.Type();
  name = mt.Name();

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
  msg >> c_str >> Check;
  if (!msg.Status()) {
    TutOut("--> Could not get the RC State message string\n"); 
    return;
  }
  SplitRCState(c_str, &self->fPartition, &self->fRunNo, self->fRCState);
  self->fConsInfo->setRunNumber(self->fRunNo);
  if (self->fDebugFlag > 3) {
    TutOut("ProcessRCStateMessage: Message type name is <%s>\n", name);
    msg.Print(TutOut);
    cout << self->fPartition << "/" << self->fRCState 
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
  TipcMsg *submsg_array;
  T_INT4  submsg_array_size = 0;
  T_INT4  nHistogram = 0;
  
  // Get the object reference back in this static method 
  SvtHistogramProducer *self = (SvtHistogramProducer *) arg;
  SvtHistogramProducer::nIter++;

  // Get SmartSockets message handle
  TipcMsg msg(data->msg);
  msg.Current(0);
  if (!msg.Status()) {
    TutOut("-> Could not set current field of histogram message!\n");
    return;
  }

  if (self->fDebugFlag > 1) {
    cout << "-> Sender: " << msg.Sender() << endl;
    cout << "Histogram message published, handle that .." << endl;
  }

  // Get the message array and the array length
  msg.Next(&submsg_array, &submsg_array_size);
  if (!msg.Status()) {
    TutOut("Could not get the histogram message array, sender %s!\n", msg.Sender());
    return;
  }

  nHistogram = submsg_array_size;   // Convenience
  if (self->fDebugFlag > 1) 
    cout << "Nhist: " << nHistogram << endl;

  // Check if new histograms have been added/removed at runtime. This is non-trivial
  // because histograms are published by all the crates, hence 
  // nEntries != nHistogram will always be true. Checking the condition is not trivial.
  // It is virtually commented out
  if (self->fDebugFlag > 10) {
    if (!self->fHistList->IsEmpty()) {
      Int_t nEntries = self->fHistList->GetSize();
      if (nEntries != nHistogram) {
        cout << "-> Histogram list has changed!" << endl;
        cout << "-> # of histograms present in the list " << nEntries  
             << ", # histograms published in this iteration " 
             << nHistogram << endl;
      }
    }
  }

  // Now loop individual messages and create Root histograms
  for (int i = 0; i < nHistogram; i++) {
    // Check the type of the message here
    TipcMt mt  = submsg_array[i].Type(); 
    T_STR name = mt.Name(); 

    (!strcmp(name, "SVTHISTO_1D")) 
      ? self->Unpack1DHist(submsg_array[i], i)            // 1D histograms
      : self->Unpack2DHist(submsg_array[i], i);           // 2D histograms
  }
  delete [] submsg_array;

  // Update the consumer info event number
  Int_t nent = 0;
  for (int i = 0; i < 12; i++) {
    ostrstream tmpstr;
    tmpstr << "Ntrk Ntrk TF_OSPY wedge " << i << ends;
    TString hname(tmpstr.str());
    TH1 *histo = (TH1 *) self->fHistList->FindObject(hname);
    if (histo) {
      nent = static_cast<Int_t>(histo->GetEntries());
      self->fConsInfo->setNevents(nent);
    }
  }

  if (self->fDebugFlag > 2) {
    cout << SvtHistogramProducer::nIter << endl;
    self->fConsInfo->print();
    cout << "Run # " << self->fConsInfo->runnumber() << " nevent  " 
         << self->fConsInfo->nevents() << endl;
    cout << "Modified " << self->fConsInfo->isModified() << endl;;
  }

  // Send out the consumer info and all objects in its list.
  self->fConsExp->send(self->fConsInfo);
  if (SvtHistogramProducer::nIter%100 == 0) 
    cout << "Producer: Sent out objects to the Server. " 
         << "Iteration # " << SvtHistogramProducer::nIter << endl;
}

//
// Unpack individual 1D histogram message 
//
Bool_t SvtHistogramProducer::Unpack1DHist(TipcMsg& msg, const T_INT4 index) {
  T_INT4 hid, nbins, nentries;
  T_REAL4 xlow, xhig;
  T_INT4 uflow, oflow;
  T_STR title;
  T_REAL4 *bins_array, *errors_array;
  T_INT4 array_size;
  Int_t nparts = 0;

  msg.Current(0);
  if (!msg.Status()) {
    TutOut("--> Could not set current field for %dth message!\n", index);
    return kFALSE;
  }

  if (fDebugFlag > 3) 
    cout << "Dump 1D Histo: " << endl;

  // Get Histogram header first
  msg >> hid >> title >> nbins >> xlow >> xhig 
             >> nentries >> uflow >> oflow >> Check;
  if (!msg.Status()) {
    TutOut("--> Could not get the 1D histogram attribute fields! for %dth message\n", 
                index);
    return kFALSE;
  }
  
  // Decode histogram title, LOGY option etc from SS message
  TString xTitle, gTitle, yopt;  
  nparts = SplitString(title, 0, xTitle, gTitle, yopt);
  if (!nparts && fDebugFlag > 10) 
    cout << "A complete String! cannot be split: " << title << endl;

  // Name of the canvas (shown in the list view of HistoDisplayMain)
  ostrstream canvas_str;
  canvas_str << xTitle << " " << gTitle << ends;
  TString cname(canvas_str.str());

  // Create a different name (appending hid) for the histogram!
  ostrstream histo_str;
  histo_str << canvas_str << " (" << hid << ")" << ends;
  TString hname(histo_str.str());

  if (fDebugFlag > 1) 
    cout << "Histogram Header: " << index << " " << hid << " " << title 
         << " " << nbins << " " << xlow << " " << xhig << endl;

  // Create histogram and the container canvas and add to respective lists
  if (fHistList->IsEmpty() || !fHistList->Contains(hname))  {
    // Create the histogram   
    TH1F *histo = new TH1F(hname.Data(), gTitle.Data(), nbins, xlow, xhig);
    if (!histo) {
      cout << "-> Could not create Histogram for id = " << hid << endl; 
      return kFALSE;
    }
  
    // Add to the List
    fHistList->Add(histo);

    // Set and modify histogram attributes
    histo->SetStats(kTRUE);
    histo->SetXTitle(xTitle);
    histo->SetYTitle("Event Count");
    histo->SetFillColor(48);
    histo->GetXaxis()->CenterTitle(kTRUE);
    histo->GetYaxis()->CenterTitle(kTRUE);

    // Now create the canvas, which will contain the histogram, for flexibility
    TCanvas *canvas = new TCanvas(cname.Data(), cname.Data());
    fCanvasList->Add(canvas);   // Add to the list of canvases

    canvas->cd();
    histo->Draw();              // Draw the histogram on the canvas

    // Construct Histogram browser foldername from message destination
    TString folder(clientName);
    T_STR fold = msg.Dest();
    folder.Append((!fold) ? "/General" : fold);

    // Add the canvas to the TConsumerInfo object
    if (!fConsInfo->getAddress(cname))
        fConsInfo->addObject(cname, folder, 0, canvas);

    if (fDebugFlag > 2)
      cout << "Dest: " << msg.Dest() << " UserProp: " << msg.UserProp() << endl;
  }

  // Get back the histogram from the List consistently for any iteration
  TH1F *histo = (TH1F *) fHistList->FindObject(hname); 
  if (!histo) {
    cout << "-> Cannot retrieve the " << index 
         << "th histogram from the List,"  << endl;
    return kFALSE;
  }
  if (strcmp(histo->ClassName(), "TH1F")) return kFALSE;

  histo->Reset();    // Assume that the most recently published histogram
                     // is the one to be displayed
  
  // Unpack bin content message onto arrays
  msg.Next(&bins_array, &array_size);
  if (!msg.Status()) {
    TutOut("--> Could not get histogram bin content\n");
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
  msg.Next(&errors_array, &array_size);
  if (!msg.Status()) {
    TutOut("--> Could not get histogram error content\n");
    return kFALSE;
  }

  // Get back the canvas from the List 
  TCanvas *canvas = (TCanvas *) fCanvasList->FindObject(cname); 
  if (canvas) {
    // Play the following trick to remove 'histogram name' from the statistics box
    canvas->cd();
    TPaveStats *pst = (TPaveStats *)canvas->GetPrimitive("stats");
    if (pst) {
      if (fDebugFlag > 1) cout << "Statistics option: " << pst->GetOptStat();
      pst->SetOptStat(1110);
    }
     
    // Another trick to tackle Logy option
    if (!yopt.CompareTo("Logy")) {
      if (histo->GetEntries() > 0) 
        canvas->SetLogy(1);   // set LOGY option
      else 
        canvas->SetLogy(0);   // restore liner option
    }  
    canvas->Modified();
    canvas->Update();

    // Print all the primitives contained in the canvas
    if (fDebugFlag > 2) canvas->ls();  
  }
  else {
    cout << "Canvas cannot be retrieved!,  index = " << index 
         << " name = " << cname << endl;
    return kFALSE;
  }

  return kTRUE;
}

//
// Process individual 2D histogram message
//
Bool_t SvtHistogramProducer::Unpack2DHist(TipcMsg& msg, const T_INT4 index) {
  T_INT4 hid, nentries, nbins_array_size, range_array_size;
  T_STR title;
  T_INT4 *nbins_array;
  T_REAL4 *range_array;
  T_REAL4 *flow_array;
  T_REAL4 *bins_array;
  T_INT4 flow_array_size, bins_array_size;
  Int_t nparts = 0;

  msg.Current(0);
  if (!msg.Status()) {
    TutOut("--> Could not set current field for %dth message!\n", index);
    return kFALSE;
  }

  // Retrieve histogram ID and the title
  msg >> hid >> title >> Check;
  if (!msg.Status()) {
    TutOut("--> Could not get the 2d histogram attribute fields! for %dth message\n", 
                index);
    return kFALSE;
  }
  
  // Retrieve histogram x and y # of bins
  msg.Next(&nbins_array, &nbins_array_size);
  if (!msg.Status()) {
    TutOut("--> Could not get 2D histogram bin numbers\n");
    return kFALSE;
  }

  // Retrieve histogram x and y low and high axis values
  msg.Next(&range_array, &range_array_size);
  if (!msg.Status()) {
    TutOut("--> Could not get 2D histogram x and y ranges\n");
    return kFALSE;
  }

  if (nbins_array_size != 2 || range_array_size != 4) { 
    TutOut("--> Incorrect number of elements in histogram nbins and histograms ranges\n");
    return kFALSE;
  }

  // Get histogram title etc from the SS message
  TString yTitle, gTitle, xTitle;  
  nparts = SplitString(title, 1, yTitle, gTitle, xTitle);
  if (!nparts && fDebugFlag > 10) 
    cout << "A complete String! cannot be split " << endl;

  // Canvas name
  ostrstream canvas_str;
  canvas_str << yTitle << " vs " << xTitle << " " << gTitle << ends;
  TString cname(canvas_str.str());

  // Histogram name
  ostrstream histo_str;
  histo_str << canvas_str << " (" << hid << ")" << ends;
  TString hname(histo_str.str());

  if (fDebugFlag > 1) {
    cout << "Histogram Header: " << index << " " << hid << " " << title << endl;
    cout <<  cname << " " << nbins_array[0] << " " 
                  << range_array[0] << " " << range_array[1] 
                  << " " << nbins_array[1] << " " 
                  << range_array[2] << " " << range_array[3] 
                  << endl;
  }

  // Create and add the histogram and the containing canvas to the respective Lists
  if (fHistList->IsEmpty() || !fHistList->FindObject(hname)) {
    // Book the histogram 
    TH2F *histo = new TH2F(hname.Data(), gTitle.Data(), 
         nbins_array[0], range_array[0], range_array[1], 
         nbins_array[1], range_array[2], range_array[3]);
    if (!histo) {
      cout << "-> Could not create 2D Histogram for id = " << hid << endl; 
      return kFALSE;
    }
  
    // Add to the List
    fHistList->Add(histo);

    // Set and Modify histogram properties
    histo->SetStats(kTRUE);
    histo->SetXTitle(xTitle);
    histo->SetYTitle(yTitle);
    histo->SetFillColor(48);
    histo->GetXaxis()->CenterTitle(kTRUE);
    histo->GetYaxis()->CenterTitle(kTRUE);

    // Now create the canvas, which will contain the histogram, for flexibility
    TCanvas *canvas = new TCanvas(cname.Data(), cname.Data());
    fCanvasList->Add(canvas);   // Add to the list of canvases

    canvas->cd();
    histo->Draw();              // Draw the histogram on the canvas

    // Construct folder name
    TString folder(clientName);
    T_STR fold = msg.Dest();
    if (fDebugFlag > 2) 
      cout << "2D Dest: " << fold << endl;;
    folder.Append((!fold) ? "/General" : fold);

    // Add object to consumer info
    if (!fConsInfo->getAddress(cname)) 
      fConsInfo->addObject(cname, folder, 0, canvas);

    if (fDebugFlag > 2)
      cout << "Dest: " << msg.Dest() << " UserProp: " << msg.UserProp() << endl;
  }

  // Now retrieve the histogram from the List
  TH2F *histo = (TH2F *) fHistList->FindObject(hname); 
  if (strcmp(histo->ClassName(), "TH2F")) return kFALSE;

  if (!histo) {
    cout << "-> Cannot retrieve the " << index << "th histogram from the List" << endl;
    return kFALSE;
  }
  histo->Reset();  // Assume that the most recently published histogram
                   // is the one to be displayed
  
  // Get histogram entries
  msg >> nentries >> Check;
  if (!msg.Status()) {
    TutOut("--> Could not get histogram entries\n");
    return kFALSE;
  }
  histo->SetEntries(nentries);

  // Now get the 9 numbers of overflow/underflow
  msg.Next(&flow_array, &flow_array_size);
  if (!msg.Status()) {
    TutOut("--> Could not get histogram underflow/overflow contents\n");
    for (int i = 0; i < flow_array_size; i++) 
      cout << flow_array[i] << "\t";
    cout << endl;
    return kFALSE;
  }
  if (flow_array_size != 9) { 
    cout << "--> Incorrect number of elements " 
         << flow_array_size << " in histogram " 
         << "underflow/overflow contents" << endl;
    return kFALSE;
  }

  // Retrieve histogram contents as flat array
  msg.Next(&bins_array, &bins_array_size);
  if (!msg.Status()) {
    TutOut("--> Could not get histogram channel contents\n");
    return kFALSE;
  }

  // Check for consistency
  int xbins = histo->GetNbinsX();
  int ybins = histo->GetNbinsY();
  if (bins_array_size != xbins*ybins) {
    cout << "--> Total number of chanels " << bins_array_size 
         << " retrieved inconsistent with " <<  xbins*ybins << endl;
    return kFALSE;
  }

  // Fill the histogram with the array content
  for (int j = 0; j < ybins; j++) {      // y bins   
    for (int i = 0; i < xbins; i++) {    // x bins
      histo->SetBinContent(i+1, j+1, bins_array[i + xbins * j]);    
    }
  }

  // Add the underflow and overflow content
  histo->SetBinContent(0,       ybins+1, flow_array[1]);
  histo->SetBinContent(xbins,   ybins+1, flow_array[2]);
  histo->SetBinContent(xbins+1, ybins+1, flow_array[3]);
  histo->SetBinContent(xbins+1, ybins,   flow_array[4]);
  histo->SetBinContent(xbins+1, 0,       flow_array[5]);
  histo->SetBinContent(xbins,   0,       flow_array[6]);
  histo->SetBinContent(0,       0,       flow_array[7]);
  histo->SetBinContent(0,       ybins,   flow_array[8]);
    
  // Error is not published yet

  // Get back the canvas from the List 
  TCanvas *canvas = (TCanvas *) fCanvasList->FindObject(cname); 
  if (canvas) {
    // Play the following trick to remove 'histogram name' from the statistics box
    canvas->cd();
    TPaveStats *pst = (TPaveStats *)canvas->GetPrimitive("stats");
    if (pst) {
      if (fDebugFlag > 1) cout << "Statistics option: " << pst->GetOptStat();
      pst->SetOptStat(1110);
    }
     
    canvas->Modified();
    canvas->Update();

    // Print all the primitives contained in the canvas
    if (fDebugFlag > 2) canvas->ls();  
  }
  else {
    cout << "Canvas cannot be retrieved!,  index = " << index 
         << " name = " << cname << endl;
    return kFALSE;
  }
  return kTRUE;
}

//
// Setup RC State message callback. Pass the 'this' reference as an argument
// in the callback routine such that within the callback instance variables
// can be managed.
// srv          - Reference to the RTServer 
// setCallback  - true/false if subscribing/unsubscribing
//
void SvtHistogramProducer::SetRCStateCallback(TipcSrv& srv, const Bool_t setCallback) {
  static T_STR dest = "/runControl/...";
  if (setCallback) {
    // Subscribe to the appropriate subject 
    if (!srv.SubjectSubscribe(dest)) {
      // Send the object reference 'this' to the static method
      srv.SubjectCbCreate(dest, NULL, SvtHistogramProducer::ProcessRCStateMessage, this);
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
// Setup comand callback. Pass the 'this' reference as an argument
// in the callback routine such that within the callback instance variables
// can be managed.
// srv          - Reference to the RTServer 
// setCallback  - true/false if subscribing/unsubscribing
//
void SvtHistogramProducer::SetCommandCallback(TipcSrv& srv, const Bool_t setCallback) {
  static T_STR dest = "/spymon/producer";
  if (setCallback) {
    // Subscribe to the appropriate subject 
    if (!srv.SubjectSubscribe(dest)) {
      // Send the object reference 'this' to the static method
      srv.SubjectCbCreate(dest, NULL, SvtHistogramProducer::ProcessCommandMessage, this);
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
void SvtHistogramProducer::SetHistoCallback(TipcSrv& srv, const Bool_t setCallback) {
  static T_STR dest = "/spymon/histo";
  if (setCallback) {
    // Subscribe to the appropriate subject 
    if (!srv.SubjectSubscribe(dest)) {
      // Send the object reference 'this' to the static method
      srv.SubjectCbCreate(dest, NULL, SvtHistogramProducer::ProcessHistogramMessage, this);
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
void SvtHistogramProducer::SetBeamFinderCallback(TipcSrv& srv, const Bool_t setCallback) {
  static T_STR dest = "/spymon/beam";
  if (setCallback) {
    // Subscribe to the appropriate subject 
    if (!srv.SubjectSubscribe(dest)) {
      // Send the object reference 'this' to the static method
      srv.SubjectCbCreate(dest, NULL, SvtHistogramProducer::ProcessBeamFinderMessage, this);
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
//
void SvtHistogramProducer::InitSrv(TipcSrv& srv) {
  ostrstream configFileName;
  configFileName << getenv("SMARTSOCKETS_CONFIG_DIR") << "/unix.cm" << ends;
  cout << "Config File: " << configFileName.str() << endl; 

  // Read SmartSockets configuration from a standard file, Does not work !!!!
#if 0
  TutCommandParseFile(configFileName.str());
#endif
  TutCommandParseStr("setopt server_names start_never:tcp:b0dau30");
  TutCommandParseStr("setopt lm_names tcp:b0dau30");
  TutCommandParseStr("setopt lm_start_max_tries 5.0");
  TutCommandParseStr("setopt project cdf_daq");
  TutCommandParseStr("setopt server_keep_alive_timeout 30.0");
  TutCommandParseStr("setopt socket_connect_timeout 10.0");
  TutCommandParseStr("setopt server_disconnect_mode warm");
  TutCommandParseStr("setopt server_start_delay 10.0");
  TutCommandParseStr("setopt lm_start_delay 10.");

  // Connect to RTserver 
  if (!srv.Create(T_IPC_SRV_CONN_FULL)) {
    TutOut("Could not connect to RTserver!\n");
    exit(T_EXIT_FAILURE);
  }
  else {
    TutOut("Connected to RT Server...\n");
  }
}

//
// Close connection to RT Server 
//
void SvtHistogramProducer::CloseSrv(TipcSrv& srv) {
  if (srv) srv.Destroy(T_IPC_SRV_CONN_NONE); // disconnect from RTserver
}

//
// Save histograms in a root file 
//
Bool_t SvtHistogramProducer::Save(void) {  
  // Save the most recent histograms 
  TDatime *datime = new TDatime();
  Int_t date = datime->GetDate();
  Int_t time = datime->GetTime();
  ostrstream filename;
  filename << clientName << "_" << date << time << ".root" << ends;
  SaveHistogram(filename.str(), "RECREATE");
  delete datime;
  return kTRUE;
}

// 
// Save histograms in a root file 
//
Bool_t SvtHistogramProducer::SaveHistogram(const char *filename, const char *opt) {
  Int_t nHist = fHistList->GetSize();

  cout << "nHist = " << nHist << endl;
  if (!nHist) {
    cout << "No histogram in the list, size = " << nHist << endl;
    return kFALSE;
  }
     
  TH1 *histo = 0;
  TFile *file = new TFile(filename, opt);
  if (!file) {
    cout << "Warning. File " << filename 
         << " cannot be opened for writing!!" << endl;
    return kFALSE;
  }

  for (int i = 0; i < nHist; i++) {
    histo = (TH1 *) fHistList->At(i);
    if (!histo) break;
    histo->Write();
  }
  file->Write();
  file->Close();
  delete file;
  file = 0;
  return kTRUE;
}

//
// Check whether the producer stop flag is set 
//
inline Bool_t SvtHistogramProducer::GetStopFlag(void) {
  return fStopFlag;
}

//
// Set the producer stop flag externally, probably bia a message 
// Bool_t option   Stop option to be set
//
inline void SvtHistogramProducer::SetStopFlag(Bool_t option) {
  fStopFlag = option;
}

// 
// Get number of histograms used in the app 
//
inline UInt_t SvtHistogramProducer::GetNHistograms(void) {
  return fHistList->GetSize();
}

// 
// Get the List which contains the histograms 
//
inline TList *SvtHistogramProducer::GetHistArray(void) {
  return fHistList;
}

// 
// Get the port number stored 
//
inline UInt_t SvtHistogramProducer::GetPort(void) {
  return fPort;
}

// 
// Get and Set run number 
//
inline Int_t SvtHistogramProducer::GetRunNumber(void) {
  return fRunNo;
}
inline void SvtHistogramProducer::SetRunNumber(const Int_t runNo) {
  fRunNo = runNo;
}

//
// Get and set RC state
// 
inline TString& SvtHistogramProducer::GetRCState(void) {
  return fRCState;
}
inline void  SvtHistogramProducer::SetRCState(const TString& state) {
  fRCState = state;
}

//
// Get and set partition number
// 
inline Int_t SvtHistogramProducer::GetPartition(void) {
  return fPartition;
}
inline void  SvtHistogramProducer::SetPartition(const Int_t partition) {
  fPartition = partition;
}

//
// Write a log file
//
Bool_t SvtHistogramProducer::WriteLog(void) {
  return kTRUE;
}

//
// Split a string across a tag, '#' here and return 3 substrings
//
static Int_t SplitString(T_STR title, Int_t hopt, TString& newTitle, 
                                 TString& gTitle, TString& yopt)
{
  // Unique histogram name
  TString full_name(title);
  TString first_str, second_str, third_str;
  Int_t index = full_name.Index("#", 1);
  if (index >= 0) {
    first_str = full_name(0, index);
    TString tmpStr = full_name(index+1, full_name.Length());
    index = tmpStr.Index("#", 1);
    if (index >= 0) {
      second_str = tmpStr(0, index);
      third_str  = tmpStr(index+1, tmpStr.Length());
    }
    else {
      second_str = tmpStr;
      third_str  = " ";
    }
  }

  newTitle.Append(first_str);
  if (hopt) {
    gTitle.Append(third_str);
    yopt.Append(second_str);
  }  
  else {
    gTitle.Append(second_str);
    yopt.Append(third_str);
  }

  return 0;
}

//
// Split a string across a tag, ' ' here and return substrings
//
void SplitRCState (T_STR iStr, Int_t *partition, Int_t *run, TString& state) {
  TString sentence(iStr);
  Int_t index = -1, jndex = -1;
  TString aStr, bStr;

  index = sentence.Index(" ", 1);                      // Verb
  if (index >= 0 && index < sentence.Length()) {
    aStr = sentence(index+1, sentence.Length());
    jndex = aStr.Index(" ", 1);                        // State
    if (jndex >= 0 && jndex < aStr.Length()) {
      state = aStr(0, jndex);
      bStr = aStr(jndex+1, aStr.Length());
      index = bStr.Index(" ", 1);                      // Partition
      if (index >= 0 && index < bStr.Length()) {
        *partition = atoi(bStr(0, index).Data());
        aStr = bStr(index+1, bStr.Length());
        jndex = aStr.Index(" ", 1);                    // State Manager
        if (jndex >= 0 && jndex < aStr.Length()) {
          bStr = aStr(jndex+1, aStr.Length());
          index = bStr.Index(" ", 1);                  // Time
          if (index >= 0 && index < bStr.Length()) {
            aStr = bStr(index+1, bStr.Length());
            jndex = aStr.Index(" ", 1);                // Sender ID
            if (jndex >= 0 && jndex < aStr.Length()) {
              *run = atoi(aStr(jndex+1, aStr.Length()).Data());
            }
	  }
        }
      }
    }
  }
}

// 
// Interrupt (Control C) handler 
//
static void cntrl_c_handler(int sig) 
{
  char answer[10];
  fprintf(stderr,"\n\n%s%d\n\n%s", "Interrupt received! Signal = ", sig,
	  "Do you wish to continue(c) or quit(q)? ");
  scanf("%s", answer);
  if (*answer == 'c') {
    signal(SIGINT, cntrl_c_handler);
  }
  else {
    exit(1);
  }
}

//
// Set global Root options
//
static void SetRootOptions() 
{
  gStyle->SetMarkerStyle(1);
  gStyle->SetTitleOffset(1.0);
  gStyle->SetTitleOffset(1.2, "Y");
  gStyle->SetLabelSize(.03, "X");
  gStyle->SetLabelSize(.03, "Y");
  gStyle->SetNdivisions(510, "X");
  gStyle->SetNdivisions(510, "Y");
}

//
// Main entry point 
//
extern void InitGui(); 
VoidFuncPtr_t initfuncs[] = {InitGui, 0};
static TROOT rootP(clientName, "Root HistogramProducer", initfuncs);

int main(int argc, char **argv) 
{
  // Interpret the command line arguments first
  Int_t  dflag = 1;
  UInt_t nhist = 1000;
  UInt_t port  = 9050;
  if (argc > 1) dflag = atoi(argv[1]);
  if (argc > 2) nhist = atoi(argv[3]);
  if (argc > 3) port  = atoi(argv[4]);
  
  rootP.SetBatch(kTRUE);                   // Set batch mode of running
  TApplication app("app", &argc, argv);    // Root application
  SetRootOptions();                        // Set Default style
 
  // Instantiate the Root histogram producer
  SvtHistogramProducer prod(dflag, nhist, port);

  TipcSrv& srv = TipcSrv::Instance();      // Connect to SS Server

  prod.InitSrv(srv);                       // Connect to RTServer
  prod.SetCommandCallback(srv, kTRUE);     // Subscribe to command message
  prod.SetBeamFinderCallback(srv, kTRUE);  // Subscribe to histogramming messages
  prod.SetHistoCallback(srv, kTRUE);       // Subscribe to histogramming messages
  prod.SetRCStateCallback(srv, kTRUE);     // Subscribe to RC State message

  // Setup interrupt handler
  signal(SIGINT, cntrl_c_handler);

  // Read and process all incoming messages
  while (!prod.GetStopFlag()) {            // Check the 'producer' stop flag  
    srv.MainLoop(10.0);                    // every 10 secs
  }
  prod.SetCommandCallback(srv, kFALSE);    // Unsubscribe from command messages
  prod.SetBeamFinderCallback(srv, kFALSE); // Subscribe to histogramming messages
  prod.SetHistoCallback(srv, kFALSE);      // Unsubscribe from histogramming messages
  prod.SetRCStateCallback(srv, kFALSE);    // Unsubscribe from RC State message
  prod.CloseSrv(srv);                      // Now close connection with the RTServer and quit

  // Save the most recent histograms 
  prod.Save();

  return T_EXIT_SUCCESS;
}
