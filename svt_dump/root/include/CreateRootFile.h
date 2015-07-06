#include <TH1.h>
#include <TObjArray.h>

enum EHistogramTabConst {
  kMaxHist = 1000
};

class CreateRootFile {
public:
  CreateRootFile();
  ~CreateRootFile();
  Bool_t createHistograms(char *filename);
  Bool_t saveHistograms(char *filename);
private:
  TObjArray *fHistArray;
  TH1F *histo;
};
