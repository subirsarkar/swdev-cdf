/*
 * dump2hist.c
 * 
 * Read the Spy dump files generated during commissioning run and 
 * convert them into histograms or use them in simulation. Uses Bill/Stefano
 * code in the core with some freedom. The program is not at all general and
 * does not even follow all the steps implemented by Stefano/Bill. 
 * 
 * There are some constraints as well which I shall note down below later on.
 *
 * Subir Sarkar 25/11/2000
 */

#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include <getopt.h>
#include <sys/param.h>
#include <time.h>

#include "spy_buffer.h"
#include "hbook.h"

void openBoards(void);

static const int EE_MASK = 0x600000;
static const int num_word = 10000;
/*
 * State of parsing the SVT dump file
 */
static int nsamp, yyyy, mm, dd, hh, nn, ss, nevsc, llock, nevhf;
static int partition, rcevents, slot, nread, nspy, wrap, ndata;
static char rcstate[20], cratename[20], boardname[10], bufname[10];
static float trigrate;
static uint4 *data;

/*
 * Application-specific data
 */
static int nev2dump;
static int debug;
static int verbose;
static char dir_name[100];
static char board[4] = "HF";
static char buff[3] = "I0";

int findEEs(uint4 *input, int inputl, int *eeptr);
void testRoadsForDump(int ev, int ndata, uint4  *data, int *eeptr);
void dumpLastEvents(int nev, int totEv,
		    uint4 *data, int *eeptr, char *fname);
void dumpEvent(int ev, uint4 *data, int *eeptr, FILE *fp);


static void
begin(void)
{
  debug   = 0;
  verbose = 0;
  openBoards();
  alloc_buffers();
  build_mask(mask, NBIT);
}

static void
ns0(void)
{
  /* this is called before each sample (file) */

  /*
   * Just for debug
   */
  if (debug) 
    printf("NS %d %4.4d/%2.2d/%2.2d %2.2d:%2.2d:%2.2d %d %d (%d)\n",
	   nsamp, yyyy, mm, dd, hh, nn, ss, nevsc, llock, nevhf);
  /*
   * Application-specific
   */
  nev2dump=1000000;
}

static void
ns1(void)
{
#if 0
  /* this is called at the end of each sample (file) */
  int i;
  int spyok;

  nev2dump--; /* first event may not be complete */

  /* check for consistency */

  spyok = 1;
  if (namshit) {
    if (nmrgout != namshit) {
      spyok = 0;
      printf("mrgout %d amshit %d ???\n", nmrgout, namshit);
    }
    for (i = 0; i < nmrgout; i++)
      if (mrgout[i] != amshit[i]) {
	spyok = 0;
	if (verbose)
	  printf("i=%d mrgout/amshit= %06x/%06x\n",i,mrgout[i],amshit[i]);
      }
  }

  if (nhbhit) {
    if (nmrgout != nhbhit) {
      spyok = 0;
      printf("mrgout %d hbhit %d ???\n",nmrgout,nhbhit);
    }
    for (i=0; i<nmrgout; i++)
      if (mrgout[i] != hbhit[i]) {
	spyok = 0;
	if (verbose)
	  printf("i=%d mrgout/hbhit= %06x/%06x\n",i,mrgout[i],hbhit[i]);
      }
  }

  if (nhbroad) {
    if (namsout != nhbroad) {
      spyok = 0;
      printf("amsout %d hbroad %d ???\n",namsout,nhbroad);
    }
    for (i = 0; i < namsout; i++)
      if (amsout[i] != hbroad[i]) {
	spyok = 0;
	if (verbose)
	  printf("i = %d amsout/hbroad= %06x/%06x\n",i,amsout[i],hbroad[i]);
    }
  }

#if 0
  if (nev2dump) {
    int hbid, amsid, mrgid, i;
    int first = 1;
    for (i = 0; i < nev2dump; i++) {
      hbid  =  hbout[ hboutee[nhboev-i]]  & 0xff;
      amsid = amsout[amsoutee[namsoev-i]] & 0xff;
      mrgid = mrgout[mrgoutee[nmrgoev-i]] & 0xff;
      /*
	printf("i:%d id mrg/ams/hb = %02x/%02x/%02x\n",
	       i, mrgid, amsid, hbid);
      */
      if ( hbid != amsid || hbid != mrgid || amsid != mrgid) {
	if (verbose)
	  printf("nev2dump %d nev mrg/ams/hb: %d/%d/%d\n",
		 nev2dump, nmrgoev, namsoev, nhboev);
	if (first || verbose){
	  printf("EE mismatch for %d-th EE mrg/ams/hb= %02x/%02x/%02x\n",
	       i, mrgid, amsid, hbid);
	  first=0;
	}
	spyok=0;
	}
    }
  }
#endif
#endif
}

static void
rc(void)
{
  /* this is called after an RC line has been read in */

  /*
   * Just for debug
   */
  if (debug)
    printf("RC %d %s %d %.2f\n", partition, rcstate, rcevents, trigrate);
}

static void
sb0(void)
{
  char crate_id_str[20];
  /* this is called before each spy buffer data */
  /*
   * Just for debug
   */
  strcpy(crate.name, cratename);
  sscanf(crate.name, "%5s%d", crate_id_str, &crate.id);

  if (debug)
    printf("SB %s %d %s %s %d %d%s\n", cratename, slot, boardname, bufname, 
	   nread, nspy, wrap ? "+" : "");
}

static void
sb1(void)
{
  /* this is called after each spy buffer data has been read in */

  /*
   * Application-specific
   */
  spy_attr[nBuffer].nvalid = ndata; 
  memcpy(spy_attr[nBuffer].data, data, ndata*sizeof(uint4));
  spy_attr[nBuffer].name       = bufname;
  spy_attr[nBuffer].board_name = boardname;

  printf("Board Name = %s, Buffer Name = %s, ndata = %d\n", boardname, bufname, ndata);
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
    if      (!strcmp(bufname,"HF_ISPY_0"))   spy_attr[nBuffer].type = HF_ISPY_0;
    else if (!strcmp(bufname,"HF_ISPY_1"))   spy_attr[nBuffer].type = HF_ISPY_1;
    else if (!strcmp(bufname,"HF_ISPY_2"))   spy_attr[nBuffer].type = HF_ISPY_2;
    else if (!strcmp(bufname,"HF_ISPY_3"))   spy_attr[nBuffer].type = HF_ISPY_3;
    else if (!strcmp(bufname,"HF_ISPY_4"))   spy_attr[nBuffer].type = HF_ISPY_4;
    else if (!strcmp(bufname,"HF_ISPY_5"))   spy_attr[nBuffer].type = HF_ISPY_5;
    else if (!strcmp(bufname,"HF_ISPY_6"))   spy_attr[nBuffer].type = HF_ISPY_6;
    else if (!strcmp(bufname,"HF_ISPY_7"))   spy_attr[nBuffer].type = HF_ISPY_7;
    else if (!strcmp(bufname,"HF_ISPY_8"))   spy_attr[nBuffer].type = HF_ISPY_8;
    else if (!strcmp(bufname,"HF_ISPY_9"))   spy_attr[nBuffer].type = HF_ISPY_9;
    else                                     spy_attr[nBuffer].type = HF_OUT_SPY;
    nBuffer++;
  }
  else if (!strcmp(boardname, "MRG")) {
    spy_attr[nBuffer].board  = (slot == 7) ? 6 : 7;
    if      (!strcmp(bufname,"MRG_A_SPY"))   spy_attr[nBuffer].type = MRG_A_SPY;
    else if (!strcmp(bufname,"MRG_B_SPY"))   spy_attr[nBuffer].type = MRG_B_SPY;
    else if (!strcmp(bufname,"MRG_C_SPY"))   spy_attr[nBuffer].type = MRG_C_SPY;
    else if (!strcmp(bufname,"MRG_D_SPY"))   spy_attr[nBuffer].type = MRG_D_SPY;
    else                                     spy_attr[nBuffer].type = MRG_OUT_SPY;
    nBuffer++;
  }
  else if (!strcmp(boardname, "AMS")) {
    spy_attr[nBuffer].board  = (slot == 8) ? 8 : 9;
    if  (!strcmp(bufname,"AMS_HIT_SPY"))   spy_attr[nBuffer].type = AMS_HIT_SPY;
    else                                   spy_attr[nBuffer].type = AMS_OUT_SPY;
    nBuffer++;
  }
  else if (!strcmp(boardname, "HB")) {
    spy_attr[nBuffer].board  = (slot == 11) ? 10 : 11;
    if      (!strcmp(bufname,"HB_HIT_SPY"))   spy_attr[nBuffer].type = HB_HIT_SPY;
    else if (!strcmp(bufname,"HB_ROAD_SPY"))  spy_attr[nBuffer].type = HB_ROAD_SPY;
    else                                      spy_attr[nBuffer].type = HB_OUT_SPY;
    nBuffer++;
  }
  else if (!strcmp(boardname, "TF")) {
    spy_attr[nBuffer].board  = (slot == 12) ? 12 : 13;
    if      (!strcmp(bufname,"TF_ISPY"))   spy_attr[nBuffer].type = TF_ISPY;
    else                                   spy_attr[nBuffer].type = TF_OSPY;
    nBuffer++;
  }
  else {   /* XTFA */
  }
}

int findEEs(uint4 *input, int inputl, int *eeptr)
{
  int count = 0;
  int i;

  eeptr[0] = 0;
  for (i = 0; i < inputl; i++) {
    if ((input[i] & EE_MASK) == EE_MASK) {
      eeptr[++count] = i;
    }
  }
  eeptr[0] = count;
  return count;
}

void dumpLastEvents(int nev, int totEv,
		    uint4 *data, int *eeptr, char *fname)
{
  int i;

  FILE *fp = 0;
  fp = fopen(fname, "w");
  assert(fp!=0);
  for(i = totEv-nev+1; i <= totEv; i++) {
    dumpEvent(i, data, eeptr, fp);
  }
  return;
}

void dumpEvent(int ev, uint4  *data, int *eeptr, FILE *fp)
{
  int begin,end;
  int i;
  if (ev == 1) begin = 0; 
  else begin = eeptr[ev-1] + 1;
  end = eeptr[ev];
  for(i = begin; i <= end; i++) {
    fprintf(fp,"%06x\n", data[i]);
  }
  return;
}

static void
procfile(char *fnam)
{
  FILE *fp = 0;
  char str[120], lasttag[3] = "  ";
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
}

static void
end(void)
{
  delete_buffers();
}

/*
 * Parse command-line arguments
 */
static int
readargs(int argc, char **argv)
{
  int c;
  if (argc == 1) {
    printf("Extract the data for one specified Spy Buffer from the\n");
    printf("SVT Spy Buffer snapshot file(s)\n");
    printf("Usage:\n  %s <data_file(s)> --board=<name> --buff=<name> --dir=<dir> --debug\n",argv[0]);
    printf("Where:\n <data_file(s)> may contain wild cards\n");
    printf(" args with -- are optional, but board and buff are needed to dump\n");
    printf(" boards name: HF, AMS, MRG, HB, TF\n");
    printf(" buff name: I0 I1 .. I9(HF) A,B,C,D,O(MRG) H,R,O(HB) H,O(AMS) I,O(TF)\n");
    printf("Example: %s b0svt02*.dat --dir=./ --board=HB --buff=O\n",argv[0]);
  }
  while (1) {
    const char *optnam = 0;
    int option_index = 0;
    static struct option long_options[] = {
      { "debug", no_argument, &debug, 1 },
      { "verbose", no_argument, &verbose, 1 },
      { "board", required_argument, 0, 0 },
      { "buff", required_argument, 0, 0 },
      { "dir", required_argument, 0, 0 },
      { 0, 0, 0, 0 }
    };
    c = getopt_long(argc, argv, "", long_options, &option_index);
    if (c == -1) break;
    switch (c) {
    case 0:
      optnam = long_options[option_index].name;
      if (0) {
      } else if (!strcmp(optnam, "debug")) {
      } else if (!strcmp(optnam, "verbose")) {
      } else if (!strcmp(optnam, "dir")) {
	assert(strlen(optarg)<sizeof(dir_name));
	strcpy(dir_name, optarg);
      } else if (!strcmp(optnam, "board")) {
	assert(strlen(optarg) < sizeof(board));
	strcpy(board, optarg);
      } else if (!strcmp(optnam, "buff")) {
	assert(strlen(optarg) < sizeof(buff));
	strcpy(buff, optarg);
      } else {
	fprintf(stderr, "option %s not implemented\n", optnam);
	assert(0);
      }
      break;
    case '?':
      assert(0);
      break;
    default:
      fprintf(stderr, "?? getopt returned character code 0%o ??\n", c);
    }
  }
  return optind;
}

void alloc_buffers(void) 
{
  int i;
  for (i = 0; i < MAX_BUFFER; i++) {
    spy_attr[i].data = (uint4 *) malloc(num_word * sizeof(uint4));
    assert(spy_attr[i].data != 0);
  }
}

/*
 *  Free Spy buffer arrays
 */
void delete_buffers(void) 
{
  int i;
  /* Delete buffers  */
  for (i = 0; i < MAX_BUFFER; i++) {
    if (spy_attr[i].data) {
      memset(spy_attr[i].data, -1, num_word * sizeof(uint4));
      free(spy_attr[i].data);
      spy_attr[i].data = NULL;
    }
  }
}

/* 
 * Fill Board attribute manually
 */
void openBoards() 
{
  int i, slot;
  int slot_hf[]  = {4, 5, 6, 13, 14, 15};
  int slot_mrg[] = {7, 16};
  int slot_ams[] = {8, 17};
  int slot_hb[]  = {11, 20};
  int slot_tf[]  = {12, 21};
  char tmp_key[20];

  nBoards = 0;
 
  /* Hit Finder */
  for (i = 0; i < 6; i++) {
    slot = slot_hf[i] ;
   
    board_attr[nBoards].type = HF; 
    sprintf(tmp_key, "hf-%d", slot);
    strcpy(board_attr[nBoards].mkey, tmp_key);
    board_attr[nBoards].slot = slot;
    nBoards++;
  }

  /* Merger */
  for (i = 0; i < 2; i++) {
    slot = slot_mrg[i] ;

    board_attr[nBoards].type = MRG; 
    sprintf(tmp_key, "mrg-%d", slot);
    strcpy(board_attr[nBoards].mkey, tmp_key);
    board_attr[nBoards].slot = slot;
    nBoards++;
  }

  /* AMS */
  for (i = 0; i < 2; i++) {
    slot = slot_ams[i] ;

    board_attr[nBoards].type = AMS; 
    sprintf(tmp_key, "ams-%d", slot);
    strcpy(board_attr[nBoards].mkey, tmp_key);
    board_attr[nBoards].slot = slot;
    nBoards++;
  }
  
  /* Hit Buffer */
  for (i = 0; i < 2; i++) {
    slot = slot_hb[i] ;

    board_attr[nBoards].type = HB; 
    sprintf(tmp_key, "hb-%d", slot);
    strcpy(board_attr[nBoards].mkey, tmp_key);
    board_attr[nBoards].slot = slot;
    nBoards++;
  }

  /* Track Fitter */
  for (i = 0; i < 2; i++) {
    slot = slot_tf[i] ;

    board_attr[nBoards].type = TF; 
    sprintf(tmp_key, "tf-%d", slot);
    strcpy(board_attr[nBoards].mkey, tmp_key);
    board_attr[nBoards].slot = slot;
    nBoards++;
  }
}
/* 
 * If needed build masks 
 */
void build_mask (uint4 *mask, int n)  
{      /* Build mask */
    int i; 
    mask[0] = 0;

    for (i = 1; i<n; i++) {
       mask[i] = (mask[i-1] << 1) | 1;
#if 0
       printf("mask[%d] = 0x%x\n", i, mask[i]); 
#endif
    }
}

int
main(int argc, char *argv[])
{
  char infile[120], outfile[120], tmpname[120];
  int dirlen;
  char format[20], ext[3];
  static int isBooked = 0;
  int i = 0;
  begin();

  /*  srand(getpid()*2 - 1); */
  srand(time(NULL));

  for (i = readargs(argc, argv); i < argc; i++) {
    nBuffer = 0;
    dirlen = strlen(dir_name);
    sprintf(format, "%s%d%s", "%", dirlen,"s%s");
    printf("%s\n", format);
    
    printf("input = %s\n", argv[i]);
    sscanf(argv[i], format, dir_name, tmpname);
    printf("%s %s\n", dir_name, tmpname);

    sprintf(format, "%s%d%s", "%", strlen(tmpname)-4,"s%4s");
    sscanf(tmpname, format, infile, ext);
    printf("%s %s\n", infile, ext);

    sprintf(outfile, "%s%s", "./", infile);
    printf("Histogram file: %s\n", outfile);
    procfile(argv[i]); 
    if (!isBooked) {
      hsbook();
      isBooked = 1;
    }
    hsfill();
    write_hist(outfile);
    hist1d_resetall();
  }  
  end();
  return 0;
}
