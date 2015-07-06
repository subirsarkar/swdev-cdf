#ifndef __SvtHistogramProducer_H
#define __SvtHistogramProducer_H

#include "TCollection.h"
#include "TList.h"

#include "rtworks/cxxipc.hxx"

/* CDF Consumer Framework */
#include "ConsumerFramework/TConsumerInfo.hh"
#include "ConsumerFramework/ConsumerExport.hh"

class SvtHistogramProducer 
{

public:
   SvtHistogramProducer(Int_t dFlag=0, UInt_t nMaxHist=1000, UInt_t port=9050);
   virtual ~SvtHistogramProducer();

   // Handle connection to the RT Server
   void InitSrv(TipcSrv& srv);
   void CloseSrv(TipcSrv& srv);

   // Setup callbacks
   void SetCommandCallback(TipcSrv& srv, const Bool_t option);  
   void SetBeamFinderCallback(TipcSrv& srv, const Bool_t option);  
   void SetHistoCallback(TipcSrv& srv, const Bool_t option);  
   void SetRCStateCallback(TipcSrv& srv, const Bool_t option);  

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
   static void ProcessRCStateMessage (T_IPC_CONN conn, 
                                T_IPC_CONN_PROCESS_CB_DATA data, 
                                T_CB_ARG arg);

   void createHistoMessage (void);

   // Unpack histogram messages  
   Bool_t Unpack1DHist(TipcMsg& msg, const T_INT4 index);
   Bool_t Unpack2DHist(TipcMsg& msg, const T_INT4 index);

   // Save histograms in a Root file
   Bool_t Save(void); 
   Bool_t SaveHistogram(const char *filename, const char *option);

   // Write log file
   Bool_t WriteLog(void);

   // Get and set methods of the private members
   Bool_t GetStopFlag(void);
   void SetStopFlag(const Bool_t option);

   Int_t GetDebugFlag(void);
   void SetDebugFlag(const Int_t flag);

   UInt_t GetNHistograms(void);
   TList *GetHistArray(void);
   TList *GetCanvasArray(void);
   UInt_t GetPort(void);

   Int_t GetRunNumber(void);
   void  SetRunNumber(const Int_t runNo);

   TString& GetRCState(void);
   void  SetRCState(const TString &state);

   Int_t GetPartition(void);
   void  SetPartition(const Int_t partition);

private:
   static Int_t nIter;

   Int_t       fDebugFlag;      // Turns on/off printouts
   UInt_t      fNMaxHist;       // Maximum number of histograms to be used
   UInt_t      fPort;           // port where the server should be connected
   Int_t       fRunNo;          // Starting run number
   Int_t       fPartition;
   TString     fRCState;
   Bool_t      fStopFlag;       // Determines whether the Histograms producer should stop
   TList      *fHistList;       // Vector which contains the histograms
   TList      *fCanvasList;     // Vector which contains the canvases, in 1-to-1 correspondence

   ConsumerExport *fConsExp;
   TConsumerInfo *fConsInfo;
};

#endif
