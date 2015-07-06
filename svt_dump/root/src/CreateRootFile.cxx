#include <iostream.h>
#include <TFile.h>
#include <TSystem.h>
#include <TEnv.h>

#include "CreateRootFile.h"
static FILE *gfopen(char *filename, char *mode);

//---------------------------------------------------------------------
//  Constructor
//  Create an Object array which holds the histograms
//  initialise the histogram reference to null
//---------------------------------------------------------------------
CreateRootFile::CreateRootFile() {
  fHistArray = new TObjArray(kMaxHist);
  histo = 0;
}
//---------------------------------------------------------------------
//  Destructor
//  Remove the content of the vector, then delete the vector
//  Delete the histogram as well
//---------------------------------------------------------------------
CreateRootFile::~CreateRootFile() {
  fHistArray->Delete();
  delete fHistArray;
  delete histo;
}
//----------------------------------------------------------------------
//
//  Read ASCII data in a known format from a file and convert them to 
//  Root histograms. Only 1d histograms are supported. The histograms
//  created are displayed in the histogram list view area
//
//----------------------------------------------------------------------
Bool_t CreateRootFile::createHistograms(char *filename) 
{
  cout << "open File -> " << filename << endl;
  FILE *fp;
  if (!(fp = gfopen(filename, "r"))) return kFALSE;

    // How many histogram data do we have in the file?
  Int_t nhis;
  fscanf(fp,"%d\n", &nhis);
  cout << "nhist = " << nhis << endl;
  if ( nhis > kMaxHist)  nhis = kMaxHist;

  if (histo) histo = 0; // Assume 1d histogram only at present

    // Loop over nhis times and build histograms
  Int_t hid, nbins, ncalls, nunder, nover;
  Float_t xlow, xhig;
  char name[255], tag[255];
  char key[20];

    // Now read file content 
  float x, ex;

  fHistArray->Delete();

  for (int j = 0; j < nhis; j++) {
    fscanf(fp,"%d %d %f %f %d %d %d\n",
          &hid, &nbins, &xlow, &xhig, &ncalls, &nunder, &nover);
    fgets(name, 255, fp);
    sprintf(tag, 
     "hid %d nbins %d xlow %f xhig %f ncalls %d nunder %d nover %d",
      hid, nbins, xlow , xhig, ncalls, nunder, nover);

    cout << tag << endl;
    cout << name << endl;

    sprintf(key, "h%d", j);
    if (histo) histo = 0;
    histo = new TH1F(key, name, nbins, xlow, xhig);
    if (!histo) break;

    fHistArray->AddAt(histo, j);
    for (int i = 0; i < nbins; i++) {
      fscanf(fp,"%f %f\n", &x, &ex);
      sprintf(tag, "%f %f\n", x, ex);
      histo->AddBinContent(i, x);
    }
    histo->SetEntries(ncalls);
  }    
  fclose(fp);

  return kTRUE;
}
//-------------------------------------------------------------------------
// 
//  Save histograms in a Root file, retrieve the histograms from the 
//  vector container and assign them to histo, one at a time
//-------------------------------------------------------------------------
Bool_t CreateRootFile::saveHistograms(char *filename) 
{
   // if filename does not have extension .root should warn and exit 
   TFile *file = new TFile(filename, "RECREATE");
   if (!file) return kFALSE;
   TH1F *histo;

   for (int i = 0; i < kMaxHist; i++) {
     histo = (TH1F *) fHistArray->At(i);
     if (!histo) break;
     histo->Write();
   }
   file->Write();
   file->Close();
   delete file;
   return kTRUE;
}
//-------------------------------------------------------------------------
// 
//  Open a file gracefully, otherwise return null
//
//-------------------------------------------------------------------------
FILE *gfopen(char *filename, char *mode)
{
  FILE *fp;
  if ((fp = fopen(filename, mode)) == NULL) {
    fprintf(stderr, "Cannot open %s - returning!\n", filename);
    return NULL;
  }
  return fp;
}
