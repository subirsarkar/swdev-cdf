#include <iostream.h>
#include <TFile.h>
#include <TSystem.h>
#include <TEnv.h>

#include "AnalyseDump.h"
static FILE *gfopen(char *filename, char *mode);

//---------------------------------------------------------------------
//  Constructor
//  Create an Object array which holds the histograms
//  initialise the histogram reference to null
//---------------------------------------------------------------------
AnalyseDump::AnalyseDump() {
  fHistArray = new TObjArray(kMaxHist);
  histo = 0;
}
//---------------------------------------------------------------------
//  Destructor
//  Remove the content of the vector, then delete the vector
//  Delete the histogram as well
//---------------------------------------------------------------------
AnalyseDump::~AnalyseDump() {
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
Bool_t AnalyseDump::createHistograms(char *filename) 
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
Bool_t AnalyseDump::saveHistograms(char *filename) 
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
Bool_t AnalyseDump::bookHistograms() 
{
  return kTRUE;
}
Bool_t AnalyseDump::fillHistograms() 
{
  return kTRUE;
}
Bool_t AnalyseDump::processFile(char *filename) 
{
  FILE *fp = 0;
  char str[80], lasttag[3] = "  ";
  int firstsample = 1;
  printf("processing %s\n", fnam);
  fp = fopen(fnam, "r");
  assert(fp != 0);
  while ((0 != fgets(str, sizeof(str), fp)) && (0 != str[0])) {
    assert(strlen(str) > 2);
    assert(str[2] == ' ');
    str[2] = 0;
    if (!strcmp(lasttag, "DA") && strcmp(str, "DA")) {
      sb1();  /* Over, transfer temporary content into permanent one */
    }
    if (!strcmp(str, "NS")) {
      if (!firstsample) ns1();
      firstsample = 0;
      assert(10 == sscanf(str+3, "%d %d/%d/%d %d:%d:%d %d %d (%d)",
			&nsamp, &yyyy, &mm, &dd, &hh, &nn, &ss, 
			&nevsc, &llock, &nevhf));
      ns0();
    }
    else if (!strcmp(str, "RC")) {
      assert(4 == sscanf(str+3, "%d %s %d %f",
		       &partition, rcstate, &rcevents, &trigrate));
      rc();   /* Debug printout only */
    }
    else if (!strcmp(str, "SB")) {
      char plus[2];
      int nscan;
      nscan = sscanf(str+3, "%s %d %s %s %d %d%s",
		     cratename, &slot, boardname, bufname, &nread, &nspy, plus);
      assert(nscan == 6 || (nscan == 7 && !strcmp(plus, "+")));
      wrap = (nscan == 7);
      ndata = 0;
      if (data) free(data);
      data = (void *) malloc(nread*sizeof(data[0]));
      sb0();   /* Debug printout only */
    }
    else if (!strcmp(str, "DA")) {
      int nscan, i;
      uint4 d[10];
      nscan = sscanf(str+3, "%x %x %x %x %x %x %x %x %x %x",
		     d+0, d+1, d+2, d+3, d+4, d+5, d+6, d+7, d+8, d+9);
      assert(data != 0 && ndata+nscan <= nread);
      for (i = 0; i < nscan; i++)
	data[ndata++] = d[i];
    }
    else {
      assert(0); /* unrecognized tag */
    }
    strcpy(lasttag, str);
  }
  if (fp) fclose(fp);
  if (!strcmp(lasttag, "DA"))
    sb1();
  if (!firstsample) ns1();
  return kTRUE;
}
/* this is called after each spy buffer data has been read in */
AnalyseDump::sb1(void)
{
  /*
   * Application-specific
   */
  spy_attr[nBuffer].nvalid     = ndata; 
  memcpy(spy_attr[nBuffer].data, data, ndata*sizeof(uint4));
  spy_attr[nBuffer].name       = bufname;
  spy_attr[nBuffer].board_name = boardname;

  cout << "Board Name  = " << boardname << 
          "Buffer Name = " << bufName   << 
          "ndata =       " << ndata << endl;
  if (!strcmp(boardname, "HF")) {
    switch (slot) {
    case 4: 
      spy_attr[nBuffer].board  = 0;
      break;
    case 5: 
      spy_attr[nBuffer].board  = 1;
      break;
    case 6: 
      spy_attr[nBuffer].board  = 2;
      break;
    case 13: 
      spy_attr[nBuffer].board  = 3;
      break;
    case 14: 
      spy_attr[nBuffer].board  = 4;
      break;
    case 15: 
      spy_attr[nBuffer].board  = 5;
      break;
    }
    if      (!strcmp(bufname,"I0"))   spy_attr[nBuffer].type = HF_ISPY_0;
    else if (!strcmp(bufname,"I1"))   spy_attr[nBuffer].type = HF_ISPY_1;
    else if (!strcmp(bufname,"I2"))   spy_attr[nBuffer].type = HF_ISPY_2;
    else if (!strcmp(bufname,"I3"))   spy_attr[nBuffer].type = HF_ISPY_3;
    else if (!strcmp(bufname,"I4"))   spy_attr[nBuffer].type = HF_ISPY_4;
    else if (!strcmp(bufname,"I5"))   spy_attr[nBuffer].type = HF_ISPY_5;
    else if (!strcmp(bufname,"I6"))   spy_attr[nBuffer].type = HF_ISPY_6;
    else if (!strcmp(bufname,"I7"))   spy_attr[nBuffer].type = HF_ISPY_7;
    else if (!strcmp(bufname,"I8"))   spy_attr[nBuffer].type = HF_ISPY_8;
    else if (!strcmp(bufname,"I9"))   spy_attr[nBuffer].type = HF_ISPY_9;
    else                              spy_attr[nBuffer].type = HF_OUT_SPY;
    nBuffer++;
  }
  else if (!strcmp(boardname, "MRG")) {
    spy_attr[nBuffer].board  = (slot == 7) ? 6 : 7;
    if      (!strcmp(bufname,"A"))   spy_attr[nBuffer].type = MRG_A_SPY;
    else if (!strcmp(bufname,"B"))   spy_attr[nBuffer].type = MRG_B_SPY;
    else if (!strcmp(bufname,"C"))   spy_attr[nBuffer].type = MRG_C_SPY;
    else if (!strcmp(bufname,"D"))   spy_attr[nBuffer].type = MRG_D_SPY;
    else                             spy_attr[nBuffer].type = MRG_OUT_SPY;
    nBuffer++;
  }
  else if (!strcmp(boardname, "AMS")) {
    spy_attr[nBuffer].board  = (slot == 8) ? 8 : 9;
    if  (!strcmp(bufname,"H"))   spy_attr[nBuffer].type = AMS_HIT_SPY;
    else                         spy_attr[nBuffer].type = AMS_OUT_SPY;
    nBuffer++;
  }
  else if (!strcmp(boardname, "HB")) {
    spy_attr[nBuffer].board  = (slot == 12) ? 10 : 11;
    if      (!strcmp(bufname,"H"))   spy_attr[nBuffer].type = HB_HIT_SPY;
    else if (!strcmp(bufname,"R"))   spy_attr[nBuffer].type = HB_ROAD_SPY;
    else                             spy_attr[nBuffer].type = HB_OUT_SPY;
    nBuffer++;
  }
  else {   /* Track Fitter */
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
