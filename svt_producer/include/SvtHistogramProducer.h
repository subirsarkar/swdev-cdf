#ifndef __SvtHistogramProducer_H
#define __SvtHistogramProducer_H

#include <memory>
#include <vector>

#include "TCollection.h"
#include "TList.h"

#include "rtworks/cxxipc.hxx"

// CDF Consumer Framework 
#include "ConsumerFramework/TConsumerInfo.hh"
#include "ConsumerFramework/ConsumerExport.hh"

using namespace std;

class SvtHistogramProducer 
{

public:

   SvtHistogramProducer(Int_t partition, 
                        TString& name, 
                        Int_t dFlag=0, 
                        Bool_t asOnline=kFALSE, 
                        UInt_t port=9050);
   virtual ~SvtHistogramProducer();

   // Public Interface
   // Handle connection to the RT Server
   void InitSrv(TipcSrv& srv);
   void CloseSrv(TipcSrv& srv);

   // Setup callbacks
   void SetCommandCallback(TipcSrv& srv, const Bool_t option);  
   void SetBeamFinderCallback(TipcSrv& srv, const Bool_t option);  
   void SetBeamFinderWedgeCallback(TipcSrv& srv, const Bool_t option);  
   void SetHistoCallback(TipcSrv& srv, const Bool_t option);  
   void SetRCStateCallback(TipcSrv& srv, const Bool_t option);  

   void  ResetMess();                        // Reset # of histogram messages coming from ind. crates
   Int_t GetNEvt();                          // Return # of events analysed so far
   Bool_t IsGoodRun();                       // Is this run worth saving 
   Bool_t Save();                            // Save in file SVTSPYMON_current.root
   Bool_t CreatePS(const string& filename);  // Create a postscript files with important histograms
   Bool_t Save(const Int_t runNo);           // Save in file SVTSPYMON_runnumber.root
   Bool_t SaveHistogram(const string& filename, const string& opt);  // Save histograms in a file
   Bool_t SaveTree(const string& filename, const string& opt);       //  --- do ---

   // Get a list of canvases within folder 'path'
   void GetCanvasAtPath(const string& path, TSeqCollection& list);

   // Add a canvas to a list corresponding to the folder 'path' 
   void AddCanvasToPath(const string& path, TSeqCollection& list);
   //   void PrintUPath();                        // Print the unique histogram folder names
   static void PrintUPath(const vector<string>& uniqPath);          // Print the unique histogram folder names

   // Utility
   void WriteHeader(ofstream& fHtml);        // Write header lines of an html file
   void WriteFooter(ofstream& fHtml);        // Write footer lines of an html file

   // Prepare string with run number a time stamp
   void EventStamp(string& str);

   // Write log file
   Bool_t WriteLog(const Int_t runNo);

   const TString& GetClientName() const { return fClientName;}
   Bool_t GetStopFlag() const { return fStopFlag;}
   void SetStopFlag(const Bool_t option) {fStopFlag = option;}
   Int_t GetDebugFlag() const { return fDebugFlag;}
   //
   // Set the producer debug flag externally, probably via a message 
   // Int_t dflag   Stop option to be set
   //
   void SetDebugFlag(const Int_t dflag) {fDebugFlag = dflag;}
   UInt_t GetNHistograms() const {return GetHistList()->GetSize();}
   Int_t GetPaveList(TSeqCollection& list) const {
     TSeqCollection *fCol = GetFunctionList();
     for (Int_t i = 0; i < fCol->GetSize(); i++) {
       TF1 *f = dynamic_cast<TF1 *>(fCol->At(i));
       if (!f) continue;
       const char *fname = f->GetName();
       string pave_name = fname + pext;;
       TObject *obj = gROOT->FindObject(pave_name.c_str());
       if (!obj) continue;
       list.Add(obj);
     }
     return list.GetSize();
   }
   UInt_t GetPort() const {return fPort;}
   Int_t GetRunNumber() const {return fRunNo;}
   void SetRunNumber(Int_t runNo) {fRunNo = runNo;}
   Int_t GetMonitorRun() const {return fSvtmonRun;}
   Int_t GetMonitorTime() const {return fSvtmonTime;}
   const string& GetRCState() const {return fRCState;}
   void SetRCState(const string& state) {fRCState = state;}
   Int_t GetDaqPartition() const {return fDaqPartition;}
   void SetDaqPartition(Int_t partition) {fDaqPartition = partition;}
   Int_t GetPartitionToWatch() const {return fPartitionToWatch;}
   void  SetPartitionToWatch(Int_t partition) {fPartitionToWatch = partition;}

protected:

   TSeqCollection *GetCanvasList() const {return gROOT->GetListOfCanvases();}
   TList *GetHistList() const {return gDirectory->GetList();}
   TSeqCollection *GetFunctionList() const {return gROOT->GetListOfFunctions();}

   void CreateHistoMessage();
   void DisconnectRT(TipcSrv& srv);

   // Callbacks 
   static void ProcessCommandMessage(T_IPC_CONN conn,
                                T_IPC_CONN_PROCESS_CB_DATA data,
                                T_CB_ARG arg);
   static void ProcessDefaultMessage(T_IPC_CONN conn,
                                T_IPC_CONN_PROCESS_CB_DATA data,
                                T_CB_ARG arg);
   static void ProcessHistogramMessage(T_IPC_CONN conn, 
                                T_IPC_CONN_PROCESS_CB_DATA data,
                                T_CB_ARG arg);
   static void ProcessBeamFinderMessage(T_IPC_CONN conn, 
                                T_IPC_CONN_PROCESS_CB_DATA data, 
                                T_CB_ARG arg);
   static void ProcessBeamFinderWedgeMessage(T_IPC_CONN conn, 
                                T_IPC_CONN_PROCESS_CB_DATA data, 
                                T_CB_ARG arg);
   static void ProcessRCStateMessage (T_IPC_CONN conn, 
                                T_IPC_CONN_PROCESS_CB_DATA data, 
                                T_CB_ARG arg);
   // Unpack histogram messages  
   Bool_t Unpack1DHist(TipcMsg& msg, const T_INT4 index);
   Bool_t Unpack2DHist(TipcMsg& msg, const T_INT4 index);

public:
   enum {nCrates = 8};

private:

   static Int_t nIter;

   Int_t    fPartitionToWatch; 
   TString  fClientName;     // Name of the client which is displayed
   Int_t    fDebugFlag;      // Turns on/off printouts
   Bool_t   fSaveAsOnline;   // Save the histogram preserving the online folder structure
   UInt_t   fPort;           // port where the server should be connected
	    
   Int_t    fRunNo;          // Starting run number
   Int_t    fDaqPartition;   // Daq partition to keep an eye on
   string   fRCState;        // State of the Run_Control
   Bool_t   fStopFlag;       // Determines whether the Histograms producer should stop
	    
   Int_t    fSvtmonRun;      // Run # from the crates process
   Int_t    fSvtmonTime;     // Time stamp from the crate process

   ConsumerExport *fConsExp; // Interaction with the ConsumerFramework
   TConsumerInfo  *fConsInfo;

   ofstream fLog;            // Open a file for output and write log messages

   UInt_t nmess[nCrates];    // # of messages coming to the 8 crates
   vector<string> uniqPath;  // Save a list of unique folder path names
};

#endif
