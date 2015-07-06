#include <TH1.h>
#include <TObjArray.h>

enum EHistogramTabConst {
  kMaxHist = 1000
};

class AnalyseDump {
public:
  AnalyseDump();
  ~AnalyseDump();
  Bool_t processFile(char *filename);
  Bool_t bookHistograms();
  Bool_t fillHistograms();
  Bool_t saveHistograms(char *filename);
private:
  TObjArray *fHistArray;
  TH1F *histo;
  TFile *file;
};
