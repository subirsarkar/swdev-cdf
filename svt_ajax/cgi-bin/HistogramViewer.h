#ifndef __HISTOGRAM_VIEWER_H
#define __HISTOGRAM_VIEWER_H

#include <vector>
#include <string>
#include <sstream>
#include <map>

class TDirectory;
class TFile;
class TH1;
class TEnv;

class HistogramViewer {

public:

  static HistogramViewer* Instance();

  virtual ~HistogramViewer();
  void dispose();
  void setRun(const std::string& run);
  TFile* openFile(const std::string& run);
  void closeFile();
  bool fileOpen() const {return (fFile && fFile->IsOpen());}
  bool act(const std::string& qString);
  void sendRunList();
  void sendRunListAsJSON();
  bool readConsumerList();
  void sendCanvasList();
  void sendCanvasListAsJSON();
  void sendImage(const std::string& name);
  void sendImage(const std::vector<std::string>& vec);
  void sendComparison(const std::string name, const std::string& refrun);
  void printImage(TCanvas* c1);
  void printPDF(TCanvas* c1);
  void DrawObjects(TCanvas* can);
  void sendOutput(TCanvas* c1);
  TH1* getHistogram(TFile* file, const std::string& name, bool& logy, int& code);
  int getRun(TFile* file, const std::string& name, std::string& run);
  TH1* getHist1D(TObject* obj);
  TH1* createHist1D(TH1* obj);
  void setLogyOption(TVirtualPad* pad, bool dec = false);
  bool logyValid(TVirtualPad* pad);
  void sendError(const std::string& error_string);
  bool hasKey(const std::string& key);
  void executeMacro(const std::string& name);

  static void tokenize(const std::string& str, std::vector<std::string>& tokens,
                       const std::string& delimiters=" ");
  static void escape(std::string& name);
  static void splice(std::string& name, const std::string& astr=" ", const std::string& bstr="_");
  void getPair(const std::string& urlParam, const std::string& pat, std::string& key, std::string& value);
  void fillMap(const std::string& urlstr);
  std::string getValue(const std::string& key);
  std::vector<std::string> getSelectedCanvasList();
  bool isOnline() const {return (fRun == "online");}  

  void sendXMLView();
  void sendTreeView();
  void traverse(TDirectory *dir, int& ndepth, std::string& lastdir, std::ostringstream& out);
  void traverse2(TDirectory *dir, int& ndepth, std::string& lastdir, std::ostringstream& out);
  static void addHeader(std::ostringstream& out, const std::string& ctype="text/xml");
  static void setRootEnv();
  static void signal_handler(int sig);

protected:
  HistogramViewer();
  HistogramViewer(const HistogramViewer&);
  HistogramViewer& operator= (const HistogramViewer&);

private:

  static HistogramViewer* pinstance;
 
  TFile* fFile;
  std::string fRun;
  std::multimap<std::string, std::string> dict;
  std::vector<std::string> consumerList;

public:
     int fDebug;

private:

  TEnv* fAppEnv;
  std::string fCommand, 
              fView;
     int fWidth,
         fHeight;
     int fRows,
         fCols;
  double fXmin, 
         fXmax;
    bool fLogy, 
         fGetPDF,
         fSavePDF,
         fHasPDFPlugin,
         fConsumerOnly,
         fReqFit;
  std::string fFitFunction;
  std::string fFitRange;
  std::string fFitParams;

  const static std::string sep[];
};
#endif
