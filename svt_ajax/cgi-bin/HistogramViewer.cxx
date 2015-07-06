#include <iostream>
#include <fstream>
#include <iomanip>
#include <iterator>
#include <algorithm>
#include <cstdio>
#include <csignal>
#include <ctime>

#include "cgicc/CgiDefs.h"
#include "cgicc/Cgicc.h"
#include "cgicc/CgiUtils.h"
#include "cgicc/HTTPResponseHeader.h"
#include "cgicc/HTMLClasses.h"

#include <TEnv.h>
#include <TROOT.h>
#include <TStyle.h>
#include <TSystem.h>
#include <TList.h>
#include <TKey.h>
#include <TRandom.h>
#include <TCanvas.h>
#include <TPad.h>
#include <TImage.h>
#include <TImageDump.h>
#include <TF1.h>
#include <TH1.h>
#include <TH2.h>
#include <TH1K.h>
#include <TPaveStats.h>
#include <TPaveLabel.h>
#include <TPavesText.h>
#include <TFile.h>
#include <TLegend.h>

#include "HistogramViewer.h"

using namespace std;
using namespace cgicc;

const string HistogramViewer::sep[] = 
{
    "",
    "  ",
    "    ",
    "      ",
    "        ",
    "          ",
    "            ",
    "              ",
    "                "
};
HistogramViewer* HistogramViewer::pinstance = 0; // initialize pointer

HistogramViewer* HistogramViewer::Instance () 
{
  if (!pinstance) pinstance = new HistogramViewer;
  return pinstance;
}

HistogramViewer::HistogramViewer()
  : fAppEnv(new TEnv(".roothviewrc")),
    fFile(0), 
    fRun("-1"),
    fCommand("runlist"),
    fWidth(520),
    fHeight(440),
    fRows(1),
    fCols(1),
    fXmin(-1.0), 
    fXmax(-1.0),
    fLogy(false), 
    fGetPDF(false),
    fSavePDF(false),
    fHasPDFPlugin(false),
    fConsumerOnly(false),
    fReqFit(false),
    fFitFunction("gaus"),
    fFitRange(""),
    fFitParams(""),
    fView("xml")
{
  fDebug = fAppEnv->GetValue("HistogramViewer.fDebug", 0);
} 
HistogramViewer::~HistogramViewer() 
{
  dispose();
}
void HistogramViewer::dispose() 
{
  if (fAppEnv) {
    delete fAppEnv;
    fAppEnv = 0; 
  }
  if (fileOpen()) closeFile();
}
void HistogramViewer::setRun(const string& run) 
{
  fRun = run;  
  fFile = openFile(fRun);
}

TFile* HistogramViewer::openFile(const string& run) 
{
  const string ROOTFILE_DIR = fAppEnv->GetValue("HistogramViewer.fRootFileDir", 
    "/cdf/onln/data/cdf_svt/svttest/spymon/root_files/");

  ostringstream name;
  name << ROOTFILE_DIR << "SVTSPYMON_" << run << ".root";
  const char* cname = name.str().c_str();
  TFile* file = TFile::Open(cname, "READ");
 
  return file;
}
void HistogramViewer::closeFile() 
{
  fFile->Close();
  delete fFile;
  fFile = 0;
}
void HistogramViewer::escape(string& name) 
{
  if (name.find("<") != string::npos) splice(name, "<", "&lt;");
  if (name.find(">") != string::npos) splice(name, ">", "&gt;");
}
void HistogramViewer::splice(string& name, const string& istr, const string& ostr) 
{
  vector<string> tokens;
  HistogramViewer::tokenize(name, tokens, istr);
  int size = tokens.size();
  ostringstream strbuf;
  for (int i = 0; i < size; i++) {
    strbuf << tokens[i];
    if (i < size-1) strbuf << ostr;
  }
  name = strbuf.str();
}
void HistogramViewer::sendRunList() 
{
  const string ROOTFILE_DIR = fAppEnv->GetValue("HistogramViewer.fRootFileDir", 
    "/cdf/onln/data/cdf_svt/svttest/spymon/root_files/");

  ostringstream out;
  HistogramViewer::addHeader(out);
  out << endl; 

  out << "<runList>" << endl;

  string command = "ls -r1 " + ROOTFILE_DIR + 
                   " | grep SVTSPYMON | awk -F_ '{print $NF}' | awk -F. '{print $1}'";
  FILE* fp = gSystem->OpenPipe(command.c_str(), "r");
  if (fp) {
    char name[100], *result;
    while ( (result = fgets(name, 100, fp)) != NULL ) {
      string tag(result);
      string rstr(tag.substr(0,tag.length()-1)); // remove \n character
      if (rstr == "-1") continue; 
      if (rstr.find("current") != string::npos)
        rstr = "online";
      out << "  <run>" << rstr << "</run>" << endl;
    }
    gSystem->ClosePipe(fp);
  }
  else 
    cerr << "Could open command pipe: " << command << endl; 

  out << "</runList>" << endl;
  cout << out.str();
  if (fDebug > 1) cerr << out.str();
}
void HistogramViewer::sendRunListAsJSON() 
{
  const string ROOTFILE_DIR = fAppEnv->GetValue("HistogramViewer.fRootFileDir", 
    "/cdf/onln/data/cdf_svt/svttest/spymon/root_files/");

  int nw = 0;
  ostringstream out;
  HistogramViewer::addHeader(out, "text/plain");
  out << endl; // separator

  out << "{\n 'runs':[\n";

  string command = "ls -r1 " + ROOTFILE_DIR + 
                   " | grep SVTSPYMON | awk -F_ '{print $NF}' | awk -F. '{print $1}'";
  FILE* fp = gSystem->OpenPipe(command.c_str(), "r");
  ostringstream buf;
  if (fp) {
    char name[100], *result;
    while ( (result = fgets(name, 100, fp)) != NULL ) {
      string tag(result);
      string rstr(tag.substr(0,tag.length()-1)); // remove \n character
      if (rstr == "-1") continue; 
      if (rstr.find("current") != string::npos)
        rstr = "online";
      buf << "'" << rstr << "',";
      if (nw++ == 9) {
         buf << endl;
         nw = 0; 
      }
    }
    gSystem->ClosePipe(fp);
  }
  else 
    cerr << "Could open command pipe: " << command << endl; 

  string bufstr(buf.str());
  int len = bufstr.length();
  if (len) {
    bufstr = bufstr.substr(0, len-1); // pop off the last comma
    out << bufstr;
  }

  out << "\n]}";
  cout << out.str();
  if (fDebug > 1) cerr << out.str();
}
bool HistogramViewer::readConsumerList() 
{
  static const int SZ = 256;
  const string CONFIG_DIR = fAppEnv->GetValue("HistogramViewer.fConfigDir", 
    "/cdf/onln/data/cdf_svt/doc/svtspymon/ps_files/");

  // Open the file which contains the list of canvases
  string infile = CONFIG_DIR + "validation_list.cfg";
  ifstream fin(infile.c_str(), ios::in);
  if (!fin) {
    cerr << "ERROR. Could not open file: " << infile << " for reading " << endl;
    return false;
  }

  // Loop over the list of canvases and do what is needed
  char buf[SZ];
  while (fin.getline(buf, SZ))    // Removes \n
    consumerList.push_back(buf);

  // Now wrap up
  fin.close();

  return true;
}
void HistogramViewer::sendCanvasList() 
{
  // FLAT Format 
  // Check if the full list is requested
  // Presently we read the original list from a config file,
  // filter out all other canvases and send only the ones
  // from the list after validation
  bool ret = false;
  if (fConsumerOnly) ret = readConsumerList();
 
  ostringstream out;
  HistogramViewer::addHeader(out);
  out << endl; // separator

  out << "<canvasList>" << endl;
  if (fileOpen()) {
    fFile->cd();
    TKey *key;
    TIter nextkey(gDirectory->GetListOfKeys());
    while ( (key = dynamic_cast<TKey*>(nextkey())) ) {
      string name(key->GetName());
      if (ret && consumerList.size()>0) {
        vector<string>::const_iterator iter 
          = find(consumerList.begin(), consumerList.end(), name); 
        if (iter == consumerList.end()) continue;
      }
      escape(name);
      out << "  <canvas>" << name << "</canvas>" << endl;
    }
  }
  out << "</canvasList>" << endl;
  cout << out.str();
  if (fDebug > 1) cerr << out.str();
}
void HistogramViewer::sendCanvasListAsJSON() 
{
  // FLAT Format 
  // Check if the full list is requested
  // Presently we read the original list from a config file,
  // filter out all other canvases and send only the ones
  // from the list after validation
  bool ret = false;
  if (fConsumerOnly) ret = readConsumerList();
 
  ostringstream out;
  HistogramViewer::addHeader(out, "text/plain");
  out << endl; // separator

  out << "{\n 'canvases':[\n";

  ostringstream buf;
  if (fileOpen()) {
    fFile->cd();
    TKey *key;
    TIter nextkey(gDirectory->GetListOfKeys());
    while ( (key = dynamic_cast<TKey*>(nextkey())) ) {
      string name(key->GetName());
      if (ret && consumerList.size()>0) {
        vector<string>::const_iterator iter 
          = find(consumerList.begin(), consumerList.end(), name); 
        if (iter == consumerList.end()) continue;
      }
      buf << "'" << name << "'," << endl;
    }
  }
  string bufstr(buf.str());
  int len = bufstr.length();
  if (len) {
    bufstr = bufstr.substr(0, len-1); // pop off the last comma
    out << bufstr;
  }

  out << "\n]}";
  cout << out.str();
  if (fDebug > 1) cerr << out.str();
}
void HistogramViewer::addHeader(ostringstream& out, const string& ctype) 
{
  out << "Content-Type: " << ctype << endl
      << "Cache-Control: no-store, no-cache, must-revalidate, max-age=0" << endl
      << "Expires: Mon, 26 Jul 1997 05:00:00 GMT" << endl
      << "Connection: close" << endl;
}
void HistogramViewer::sendXMLView() 
{
  bool ret = false;
  if (fConsumerOnly) ret = readConsumerList();

  ostringstream out;
  HistogramViewer::addHeader(out);
  out << endl; // Separator

  if (fileOpen()) {
    fFile->cd();
    int ndepth = 0;
    string lastdir;
    traverse(fFile, ndepth, lastdir, out);
  }
  cout << out.str();
  if (fDebug > 1) cerr << out.str();
}
void HistogramViewer::traverse (TDirectory *dir, int& ndepth, string& lastdir, ostringstream& out)
{
  // loop on all keys of dir including possible subdirs
  TIter next (dir->GetListOfKeys());
  TKey *key;
  while ( (key = dynamic_cast<TKey*>(next())) ) {
    string clName(key->GetClassName());
    string name(key->GetName());
    if (clName == "TCanvas") {
      // try to handle ConsumerOnly histograms in an integrated way
      if (fConsumerOnly && consumerList.size()>0) {
        vector<string>::const_iterator iter 
          = find(consumerList.begin(), consumerList.end(), name); 
        if (iter == consumerList.end()) continue;
      }
      escape(name);
      out << sep[ndepth] << "<canvas>" << name << "</canvas>" << endl;
    }
    if (clName == "TDirectory") {
      string cname(name);
      HistogramViewer::splice(cname, " ", "_");
      out << sep[ndepth] << "<" << cname << ">" << endl;
      if (name != lastdir) {
        lastdir = name;
        ndepth++;
      }
      dir->cd(name.c_str());
      TDirectory *subdir = gDirectory;
      traverse(subdir, ndepth, lastdir, out);
      dir->cd();
      ndepth--;
      out << sep[ndepth] << "</" << cname << ">" << endl;
    }
  }
}
void HistogramViewer::sendTreeView() 
{
  ostringstream out;
  HistogramViewer::addHeader(out, "text/plain");
  out << endl; // Separator

  out << "<ul class=\"dhtmlgoodies_tree\" id=\"dhtmlgoodies_tree\">" << endl;
  if (fileOpen()) {
    fFile->cd();
    int ndepth = 0;
    string lastdir;
    traverse2(fFile, ndepth, lastdir, out);
  }
  out << "</ul>";
  cout << out.str();
}
void HistogramViewer::traverse2 (TDirectory *dir, int& ndepth, string& lastdir, ostringstream& out)
{
  // loop on all keys of dir including possible subdirs
  TIter next (dir->GetListOfKeys());
  TKey *key;
  while ( (key = dynamic_cast<TKey*>(next())) ) {
    string clName(key->GetClassName());
    string name(key->GetName());
    if (clName == "TCanvas") {
      escape(name);
      out << sep[ndepth] << "<li class=\"dhtmlgoodies_sheet.gif\"><a href=\"javascript:alert('')\">" << name << "</a></li>" << endl;
    }
    if (clName == "TDirectory") {
      string cname(name);
      out << sep[ndepth] << "<li><a href=\"#\">" << cname << "</a>" << endl;
      out << sep[ndepth] << "<ul>" << endl;
      if (name != lastdir) {
        lastdir = name;
        ndepth++;
      }
      dir->cd(name.c_str());
      TDirectory *subdir = gDirectory;
      traverse2(subdir, ndepth, lastdir, out);
      dir->cd();
      ndepth--;
      out << sep[ndepth] << "</ul>" << endl;
      out << sep[ndepth] << "</li>" << endl;
    }
  }
}
void HistogramViewer::sendImage(const string& name) 
{
  if (!fileOpen()) {
    ostringstream s;
    s << "Could not open " << fFile->GetName() << "!" << endl;
    sendError(s.str());
    return;
  }
  fFile->cd();

  cerr << "name=" << name << endl;
  TCanvas* can = dynamic_cast<TCanvas*>(gDirectory->Get(name.c_str()));
  if (!can) {
    ostringstream s;
    s << "Canvas:#" << name << "#could not be accessed!" << endl;
    sendError(s.str());
    return;
  }

  TCanvas* c1 = new TCanvas("c1");
  DrawObjects(can);
  sendOutput(c1);  

  // delete the canvas if it were created dynamically 
  delete c1;
}
void HistogramViewer::sendImage(const vector<string>& list) 
{
  if (!fileOpen()) {
    ostringstream s;
    s << "Could not open " << fFile->GetName() << "!" << endl;
    sendError(s.str());
    return;
  }
  fFile->cd();

  int nhist = list.size();
  fRows = nhist/fCols;
  fRows = (TMath::CeilNint((nhist*1.0)/fCols) > fRows) ? fRows+1 : fRows;

  TCanvas* c1 = new TCanvas("c1");
  c1->Divide(fCols, fRows);

  int zone = 1;
  for (vector<string>::const_iterator it = list.begin(); it != list.end(); it++) {
    const string& name = *it;
    TCanvas *can = dynamic_cast<TCanvas*>(gDirectory->Get(name.c_str()));
    if (!can) continue;

    c1->cd(zone++);
    DrawObjects(can);
  }

  sendOutput(c1);  

  // delete the canvas if it were created dynamically 
  delete c1;
}
void HistogramViewer::DrawObjects(TCanvas* can) 
{
  short fcolor = fAppEnv->GetValue("HistogramViewer.fFillColor", 31);
  TObject *obj;
  TIter next(can->GetListOfPrimitives());
  bool skipObj;
  while ((obj = next())) {
    skipObj = false;
    if (obj->InheritsFrom(TPad::Class())) {
      TPad* pad = dynamic_cast<TPad*>(obj);
      // Toggle logy
      if (fLogy) setLogyOption(pad);

      // Now look for the histogram inside the pad
      TObject *obj1;
      TIter next1(pad->GetListOfPrimitives());

      // Consider only 1-D histograms
      while ((obj1 = next1())) {
        if ( obj1->InheritsFrom(TH1::Class()) && 
            !obj1->InheritsFrom(TH2::Class()) ) 
        {
          TH1* hist = getHist1D(obj1);
          hist->SetFillColor(fcolor);
          if (fXmin != -1.0 || fXmax != -1.0) {
            TAxis* xa = hist->GetXaxis();
            xa->SetRangeUser((fXmin!=-1)?fXmin:xa->GetXmin(), 
                             (fXmax!=-1)?fXmax:xa->GetXmax());
          }
          if (fReqFit) {
            double xmin = hist->GetXaxis()->GetXmin(); 
            double xmax = hist->GetXaxis()->GetXmax(); 
            if (fFitRange != "") {
              vector<string> tokens;
              HistogramViewer::tokenize(fFitRange, tokens, ",");
              int n = tokens.size();
              if (n>0) xmin = atof(tokens[0].c_str());
              if (n>1) xmax = atof(tokens[1].c_str());
            }
            TF1* f1 = new TF1("f1", fFitFunction.c_str(), xmin, xmax);
            if (fFitParams != "") {
              vector<string> tokens;
              HistogramViewer::tokenize(fFitParams, tokens, ",");
              for (int i = 0; i < tokens.size(); i++) {
                double par = atof(tokens[i].c_str());
                f1->SetParameter(i, par);
              }
            }
            hist->Fit("f1", "QRO", "SAME");
            //hist->Draw();
            TPaveStats* ps = 
              dynamic_cast<TPaveStats*>(hist->GetListOfFunctions()->FindObject("stats")); 
            ps->SetOptFit(1111);
            ps->SetTextSize(0.035);
            ps->SetX1NDC(0.65);
            ps->SetX2NDC(1.0);
            ps->SetY1NDC(0.6);
            ps->SetY2NDC(1.0);
            ps->Draw();

            //skipObj = true;
          }
          break;
        }
      }
    }
    if (!skipObj) obj->Draw();
  }
}
void HistogramViewer::sendOutput(TCanvas* c1) 
{
  if (fGetPDF) printPDF(c1);
  else         printImage(c1); 
}
void HistogramViewer::printImage(TCanvas* c1) 
{
  // Draw the canvas
  c1->SetFixedAspectRatio(kTRUE);
  c1->SetCanvasSize(fWidth, fHeight);
  c1->Update();

  // Now extract the image
  // 114 - stands for "no write on Close"
  TImageDump imgdump("tmp.png", 114);
  c1->Paint();

  // get an internal image which will be automatically deleted
  // in the imgdump destructor
  TImage *image = imgdump.GetImage(); 

  char *buf;
  int sz;
  image->GetImageBuffer(&buf, &sz);         /* raw buffer */

  ostringstream out;
  HistogramViewer::addHeader(out, "image/png");
  out << "Content-Length: " << sizeof(char)*sz << endl        
         << endl;  

  for (int i = 0; i < sz; i++) 
    out << buf[i];
  cout << out.str();

  delete [] buf;
}
void HistogramViewer::printPDF(TCanvas* c1) 
{
  const string EXT = fAppEnv->GetValue("HistogramViewer.fPdfExt", "pdf");
  // Draw the canvas
  //  c1->UseCurrentStyle();
  c1->Draw();
  c1->SetFixedAspectRatio(kTRUE);
  c1->SetCanvasSize(fWidth, fHeight);

  // Create PDF
  int pid = gSystem->GetPid();
  ostringstream filename;
  filename << "/tmp/viewer_" << pid << "." << EXT;
  c1->Print(filename.str().c_str(), EXT.c_str());
  
  // Open the file again and read the content into xmlstr
  ifstream ifs(filename.str().c_str());
  if (!ifs) {
    cerr << "Could not open input file, " << filename.str() << endl;
    return;
  }
  ostringstream tmpstr;
  tmpstr << ifs.rdbuf();
  ifs.close();

  string content = tmpstr.str();

  ostringstream out;
  HistogramViewer::addHeader(out, "application/"+EXT);
  out << "Content-Length: " << content.length() << endl;
  if (fHasPDFPlugin || fSavePDF) 
    out << "Content-Disposition: attachment; filename=\"slide." 
        << EXT << "\"" << endl;
  out << endl; 

  out << content;

  cout << out.str();
  if (fDebug > 1) cerr << out.str();

  int code = gSystem->Unlink(filename.str().c_str());
}
void HistogramViewer::sendError(const string& error_string) {
  vector<string> tokens;
  HistogramViewer::tokenize(error_string, tokens, "#");
  int size = tokens.size();

  TCanvas* c1 = new TCanvas("c1", "c1", 200,10,700,500);
  c1->Range(0,0,19,12);
  TPavesText pt(1,3,18,10,3,"tr");
  pt.SetFillColor(42);
  for (int i = 0; i < size; i++) 
    pt.AddText(tokens[i].c_str());
  pt.Draw();

  sendOutput(c1);
  delete c1;
}
void HistogramViewer::sendComparison(const std::string name, 
                                     const std::string& refrun) 
{
  if (!fileOpen()) {
    ostringstream s;
    s << "Could not open " << fFile->GetName() << "!" << endl;
    sendError(s.str());
    return;
  }
  short fcolor = fAppEnv->GetValue("HistogramViewer.fFillColor", 31);
  short lcolor = fAppEnv->GetValue("HistogramViewer.fLineColor", 4);

  // Get the histograms
  bool logy = false;
  int icode = 0;
  TH1* hist = getHistogram(fFile, name, logy, icode);
  if (!hist) {
    ostringstream s;
    if (icode == 1)
       s << "File: " << fFile->GetName() << "#" << name << "#could not be accessed!" << endl;
    else
       s << "Comparison: Only 1D histograms are supported!" << endl;
    sendError(s.str());
    return;
  }
  string runstr("JUNK"); //(fRun);
  getRun(fFile, name, runstr);
  cerr << runstr << endl;

  // Now open the reference file and get the reference histogram
  TFile* refFile = openFile(refrun);
  if (!refFile->IsOpen()) {
    ostringstream s;
    s << "Could not open " << refFile->GetName() << "!" << endl;
    sendError(s.str());
    return;
  }
  bool blogy = false;
  icode = 0;
  TH1* refhist = getHistogram(refFile, name, blogy, icode);
  if (!refhist) {
    ostringstream s;
    if (icode == 1)
       s << "File: " << refFile->GetName() << "#" << name << "#could not be accessed!" << endl;
    else
       s << "Comparison: Only 1D histograms are supported!" << endl;
    sendError(s.str());
    return;
  }
 
  TCanvas* c1 = new TCanvas("c1", "c1");
  c1->Divide(1,2);
  
  c1->cd(1);
  if (logy) gPad->SetLogy(1);
  if (fLogy) setLogyOption(gPad, true); 

  //hist->Sumw2();    // Complicates life!
  Double_t    ent = hist->GetEntries();
  Double_t refent = refhist->GetEntries();
  double scale = ((ent>0) ? refent/ent : 1.0); 

  hist->Scale(scale);
  TAxis* xa = hist->GetXaxis();
  xa->SetRangeUser((fXmin!=-1)?fXmin:xa->GetXmin(), 
                   (fXmax!=-1)?fXmax:xa->GetXmax());

  // Adjust maximum 
  double cmax = hist->GetMaximum();
  double refcmax = refhist->GetMaximum();
  double fac = (gPad->GetLogy()) ? 1.2 : 1.05;
  cmax = ((cmax > refcmax) ? cmax : refcmax)*fac;
  hist->SetMaximum(cmax);
  hist->SetFillColor(fcolor);
  hist->Draw();
  TPaveStats* ps = 
    dynamic_cast<TPaveStats*>(hist->GetListOfFunctions()->FindObject("stats")); 
  ps->SetOptStat(1110);
  ps->SetTextSize(0.05);

  refhist->SetLineColor(lcolor);
  refhist->SetFillColor(0);
  xa = refhist->GetXaxis();
  xa->SetRangeUser((fXmin!=-1)?fXmin:xa->GetXmin(), 
                   (fXmax!=-1)?fXmax:xa->GetXmax());
  refhist->SetStats(kFALSE);
  refhist->Draw("SAME");   

  TLegend *legend = new TLegend(0.5,0.8,0.7,0.98,"Runs compared");
  legend->AddEntry(hist, runstr.c_str(),"f");
  legend->AddEntry(refhist,refrun.c_str(),"l");
  legend->Draw();

  // Bottom Plot
  c1->cd(2);
  TH1* dhist = createHist1D(hist);
  dhist->SetName("diff");
  dhist->Reset();
  dhist->Add(hist, refhist, 1.0, -1.0);
  xa = dhist->GetXaxis();
  xa->SetRangeUser((fXmin!=-1)?fXmin:xa->GetXmin(), 
                   (fXmax!=-1)?fXmax:xa->GetXmax());
  dhist->SetFillColor(fcolor);
  dhist->SetStats(kFALSE);
  dhist->Draw();

  sendOutput(c1);

  // time to dispose
  // delete the canvas
  delete dhist;

  // delete the canvas
  delete c1;

  // close the reference file, the main one will be automatically closed on exit
  refFile->Close();
  delete refFile;
}
TH1* HistogramViewer::getHistogram(TFile* file, const string& name, bool& logy, int& code) 
{
  file->cd();
  TCanvas* can = dynamic_cast<TCanvas*>(gDirectory->Get(name.c_str()));
  if (!can) {
    code = 1;
    return 0;
  }

  TObject *obj;
  TIter next(can->GetListOfPrimitives());
  while ((obj = next())) {
    if (obj->InheritsFrom(TPad::Class())) {
      TPad* pad = dynamic_cast<TPad*>(obj);
      if (pad->GetLogy()) logy = true; 

      // Now look for the histogram inside the pad
      TIter next1(pad->GetListOfPrimitives());
      while ((obj = next1())) {
        if (obj->InheritsFrom(TH1::Class()) && 
           !obj->InheritsFrom(TH2::Class()))
          return getHist1D(obj);
      }
    }
  }
  code = 2;
  return 0;
}
int HistogramViewer::getRun(TFile* file, const string& name, string& run) 
{
  file->cd();
  TCanvas* can = dynamic_cast<TCanvas*>(gDirectory->Get(name.c_str()));
  if (!can) return -1;

  TObject *obj;
  TIter next(can->GetListOfPrimitives());
  while ((obj = next())) {
    if (obj->InheritsFrom(TPaveLabel::Class())) {
      TPaveLabel* pl = dynamic_cast<TPaveLabel*>(obj);
      string l(pl->GetLabel());
      vector<string> tokens;
      HistogramViewer::tokenize(l, tokens);
      int size = tokens.size();
      if (size > 3) {
        run = tokens[3];
        break;
      }
    }
  }
  return 0;
}
TH1* HistogramViewer::createHist1D(TH1* obj) 
{
  TH1 *h = 0;
  if (obj->InheritsFrom(TH1D::Class()))
    h = new TH1D(*dynamic_cast<TH1D*>(obj));
  else if (obj->InheritsFrom(TH1C::Class()))
    h = new TH1C(*dynamic_cast<TH1C*>(obj));
  else if (obj->InheritsFrom(TH1K::Class()))
    h = new TH1K(*dynamic_cast<TH1K*>(obj));
  else if (obj->InheritsFrom(TH1S::Class()))
    h = new TH1S(*dynamic_cast<TH1S*>(obj));
  else if (obj->InheritsFrom(TH1I::Class()))
    h = new TH1I(*dynamic_cast<TH1I*>(obj));
  else
    h = new TH1F(*dynamic_cast<TH1F*>(obj));

  return h;
}
TH1* HistogramViewer::getHist1D(TObject* obj) 
{
  TH1 *h = 0;
  if (obj->InheritsFrom(TH1D::Class()))
    h = dynamic_cast<TH1D*>(obj);
  else if (obj->InheritsFrom(TH1C::Class()))
    h = dynamic_cast<TH1C*>(obj);
  else if (obj->InheritsFrom(TH1K::Class()))
    h = dynamic_cast<TH1K*>(obj);
  else if (obj->InheritsFrom(TH1S::Class()))
    h = dynamic_cast<TH1S*>(obj);
  else if (obj->InheritsFrom(TH1I::Class()))
    h = dynamic_cast<TH1I*>(obj);
  else
    h = dynamic_cast<TH1F*>(obj);

  return h;
}
bool HistogramViewer::logyValid(TVirtualPad* pad) 
{
  TObject *obj;
  bool logy = true;
  TIter next(pad->GetListOfPrimitives());
  while ((obj = next())) {
    if (obj->InheritsFrom(TH2::Class())) {
      logy = false;
      break;
    }
  }

  return logy;
}
void HistogramViewer::setLogyOption(TVirtualPad* pad, bool dec) 
{
  if (!logyValid(pad) && !dec) return;

  if (pad->GetLogy()) pad->SetLogy(0);
  else                pad->SetLogy(1);
}
void HistogramViewer::fillMap(const string& urlstr) 
{
  vector<string> tokens;
  HistogramViewer::tokenize(urlstr, tokens, "&");
  for (vector<string>::const_iterator it  = tokens.begin(); 
                                      it != tokens.end(); 
                                      it++) 
  {
    string item = *it;
    string key, value;
    getPair(item, "=", key, value);    
    dict.insert(make_pair(key, value));
  }

  // print content
  if (fDebug > 0) {
    multimap<string,string>::iterator pos;
    for (pos = dict.begin(); pos != dict.end(); ++pos) {
      cerr << "'[" << pos->first << "]' => '[" << pos->second << "]'"<< endl;
    }
    cerr << endl;
  }
}
void HistogramViewer::getPair(const string& urlParam, const string& pat, 
                                    string& key, string& value) 
{
  int index = urlParam.find(pat);
  if (index != string::npos) {
    key   = urlParam.substr(0, index);
    value = urlParam.substr(index+1);
  }
}
string HistogramViewer::getValue(const string& key) 
{
  multimap<string,string>::iterator pos = dict.find(key);
  string value;
  if (pos != dict.end())
    value = pos->second;

  return value;
}
vector<string> HistogramViewer::getSelectedCanvasList() 
{
  vector<string> list;
  string word("canvas");
  for (multimap<string, string>::iterator pos  = dict.lower_bound(word);
                                          pos != dict.upper_bound(word); 
                                        ++pos) 
  {
    list.push_back(pos->second);
  }

  return list;
}
bool HistogramViewer::hasKey(const string& key) 
{
  multimap<string, string>::iterator pos = dict.find(key);
  if (pos != dict.end()) return true;

  return false;
}
void HistogramViewer::executeMacro(const string& name) 
{
  // Load the already compiled shared library
  const string MACRO_DIR = 
    fAppEnv->GetValue("HistogramViewer.fMacroDir", "./macros/");
  string libName = MACRO_DIR + name + "_C";
  if (gSystem->Load(libName.c_str()) == -1) {
    string mess = libName + " does not exist!";
    sendError(mess);
    return;
  }
  // Note that in the realistic case we need also to pass the TFile
  // pointer to the macro.

  // Now form the macro name, pass the canvas name 
  // which the macro will access internally
  TCanvas* c1 = new TCanvas("c1");
  string line = name + "(\"" + c1->GetName() + "\")";
  if (fDebug > 1) cerr << line << endl;

  // Well, now process the macro
  gROOT->ProcessLine(line.c_str());
  sendOutput(c1);  

  // delete the canvas if it were created dynamically 
  delete c1;
} 
void HistogramViewer::tokenize(const string& str, vector<string>& tokens,
                               const string& delimiters)
{
  // ----------------------------
  // Skip delimiters at beginning.
  // ----------------------------
  string::size_type lastPos = str.find_first_not_of(delimiters, 0);

  // ----------------------------
  // Find first "non-delimiter".
  // ----------------------------
  string::size_type pos = str.find_first_of(delimiters, lastPos);

  while (string::npos != pos || string::npos != lastPos) {
    // ------------------------------------
    // Found a token, add it to the vector.
    // ------------------------------------
    tokens.push_back(str.substr(lastPos, pos - lastPos));

    // ------------------------------------
    // Skip delimiters.  Note the "not_of"
    // ------------------------------------
    lastPos = str.find_first_not_of(delimiters, pos);

    // --------------------------
    // Find next "non-delimiter"
    // --------------------------
    pos = str.find_first_of(delimiters, lastPos);
  }
}
bool HistogramViewer::act(const string& qString) 
{
  fillMap(qString);
   
  if (hasKey("command")) fCommand = getValue("command");
  if (fCommand == "runlist") {
    sendRunListAsJSON();
  }
  else {
    if (!hasKey("run")) {
      cerr << "ERROR. Incorrect query string, run number is mandatory" << endl;
      return false;
    }
    setRun(getValue("run"));

    // Now time to send back answer in XML format
    if (fCommand == "canvaslist") {         // send canvas list
      if (hasKey("consumerlist")) fConsumerOnly = true;
      if (hasKey("view")) fView = getValue("view");

      // sendCanvasList (as XML) and sendCanvasListAsJSON();
      // work only for FLAT Root file. Root files with
      // directories need the following
      if (fView == "xml") 
        sendXMLView();
      else if (fView == "tree") 
        sendTreeView();
    }
    else {
      // get the canvas size
      if (hasKey("width")) fWidth = atoi(getValue("width").c_str());
      if (!fWidth) fWidth = 520;

      if (hasKey("height")) fHeight = atoi(getValue("height").c_str());
      if (!fHeight) fHeight = 440;

      // PDF creation option
      if (hasKey("getpdf")) {
        fGetPDF = true;
        if (hasKey("savepdf")) fSavePDF = true;
        if (hasKey("plugin"))  fHasPDFPlugin = true;
      }

      if (fCommand == "macro") {         // execute a macro
        executeMacro(getValue("name"));
      }
      else {
        // Option common for actions like plot, compare
        if (hasKey("logy")) fLogy = true;
        if (hasKey("xmin")) fXmin = atof(getValue("xmin").c_str());
        if (hasKey("xmax")) fXmax = atof(getValue("xmax").c_str());

        if (fCommand == "plot") {    // create and send image
          // Collect histogram Fitting related information
          if (hasKey("reqfit")) {
            fReqFit = true;
            if (hasKey("fitfunction")) fFitFunction = getValue("fitfunction");
            // if fitfucntion is user defined we need another variable 
            // to hold the expression or function definition
            if (hasKey("fitrange")) fFitRange  = getValue("fitrange");
            if (hasKey("fitpar"))   fFitParams = getValue("fitpar");
          }

          const vector<string>& cList = getSelectedCanvasList();
          if (cList.size() == 1) {      // Can we generalise
            sendImage(cList[0]);
          }
          else {
            if (hasKey("rows")) fRows = atoi(getValue("rows").c_str());
            if (hasKey("cols")) fCols = atoi(getValue("cols").c_str());
            sendImage(cList);
          }
        }
        else if (fCommand == "compare") {
          const vector<string>& cList = getSelectedCanvasList();
          if (hasKey("refrun"))
            sendComparison(cList[0], getValue("refrun"));
        }
      }
    } 
  }
  return true;
}
void HistogramViewer::setRootEnv() 
{
  // ROOT starts automatically
  gROOT->SetBatch(kTRUE); 
  gStyle->SetLabelSize(0.03,"x");
  gStyle->SetLabelSize(0.03,"y");
  gStyle->SetOptStat(1110);
  gStyle->SetOptFit(11110);
}
void HistogramViewer::signal_handler(int sig) 
{
   const char* cause = (sig == SIGALRM)
      ? "timer triggers"
      : "interrupt received";
   time_t now = time(NULL);
   fprintf (stderr, "%s viewer: %s, clean-up and exit\n", ctime(&now), cause);

   // We do not really care about cleaning up, do we? 
   // pretend as if nothing happened!
   exit (EXIT_SUCCESS);
} 
int main(int argc, const char *argv[]) 
{
  // Install signal handlers. The process should not take > 2 mins.
  // A Terminate signal is handled as well, but we are not sure 
  // if it is ever sent by the server (or which one is sent)
  signal(SIGALRM, HistogramViewer::signal_handler);
  alarm(15);  // seconds
  signal(SIGTERM, HistogramViewer::signal_handler); 

  try {
    Cgicc cgi;

    HistogramViewer::setRootEnv();

    // Extract information from the query String

    // Check the CGI parameters and take action
    // Get a pointer to the environment
    const CgiEnvironment& env = cgi.getEnvironment();
    string qString = form_urldecode(env.getQueryString());
    cerr << qString << endl;
    // Get the singleton instance of the viewer
    HistogramViewer *viewer = HistogramViewer::Instance();
    if (viewer->fDebug) cerr << qString << endl; 
    bool res = viewer->act(qString);
    // if viewer cannot act successfully make sure that the browser gets something
    // This should not happen as we protect it on the client side, but ... :-)     
    delete viewer;
  }
  catch (exception& e) {
    // handle any errors, for simplicity we just show it
    e.what();
  }

  return EXIT_SUCCESS;
}
