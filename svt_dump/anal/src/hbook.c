#include <stdio.h>
#include <stdio.h>
#include <assert.h>
#include <math.h>

#include "spy_buffer.h"
#include "hbook.h"
#include "util.h"

/* define constants  */

#define PHI_SCALE 8192.
#define D0_SCALE 0.0010 
#define CUR_SCALE 1.17e-5  
#define CHI_SCALE 1.

/* to convert from strip/16 to cm: using SVXII layers 0,1,2,3,4 */
#define HIT0_SCALE (10000.*16./60.)
#define HIT1_SCALE (10000.*16./62.)
#define HIT2_SCALE (10000.*16./60.)
#define HIT3_SCALE (10000.*16./60.)
#define HIT4_SCALE (10000.*16./65.)

/* 
 * Book histograms. 
 */
void hsbook(void) 
{
  int board, idbase;
  int i, index, jndex;

  printf("hsbook(): nBuffer = %d\n", nBuffer);
  for (i = 0; i < nBuffer; i++) {
    jndex = spy_attr[i].type;
    index = spy_attr[i].board;
    board = board_attr[index].type;
    printf("hsbook(): jndex = %d, index = %d, board = %d\n", jndex, index, board);
    idbase = CRATE_BASE * (crate.id+1) +  BOARD_BASE * (board-30) + 
             SLOT_BASE  * board_attr[index].slot; 
    book_error(i, board_attr[index].mkey, idbase);
    switch (jndex) {  
      case HF_ISPY_0:
      case HF_ISPY_1:
      case HF_ISPY_2:
      case HF_ISPY_3:
      case HF_ISPY_4:
      case HF_ISPY_5: 
      case HF_ISPY_6:
      case HF_ISPY_7:
      case HF_ISPY_8:
      case HF_ISPY_9:
        book_hf_input_word(i, board_attr[index].mkey, idbase);
	break;
      case HF_OUT_SPY:
        book_hf_output_word(i, board_attr[index].mkey, idbase);
	break;
      case MRG_A_SPY:
      case MRG_B_SPY:
      case MRG_C_SPY:
      case MRG_D_SPY:
      case MRG_OUT_SPY:
        book_mrg_word(i, board_attr[index].mkey, idbase);
	break;
      case AMS_HIT_SPY:
        book_hit_word(i, board_attr[index].mkey, idbase);
	break;
      case AMS_OUT_SPY:
        book_road_word(i, board_attr[index].mkey, idbase);
	break;
      case HB_HIT_SPY:
        book_hit_word(i, board_attr[index].mkey, idbase);
	break;
      case HB_ROAD_SPY:
        book_road_word(i, board_attr[index].mkey, idbase);
	break;
      case HB_OUT_SPY:
        book_hb_output_word(i, board_attr[index].mkey, idbase);
	break;
      case TF_ISPY:
        book_tf_input_word(i, board_attr[index].mkey, idbase);
      case TF_OSPY:
        book_tf_output_word(i, board_attr[index].mkey, idbase);
	break;
      default:
	break;
    }
  }
}
/* 
 * Book histograms for the error bits collected in the end event
 * words. We use one histogram to plot the 'ones' of the n error bits
 * and another to plot the 'zeros'. We should think about how to 
 * combine these two sets into a meaningful one such that one can
 * display the percentage of times errors occur in individual error
 * bits.
 */
void book_error(int index, char *key, int idbase) 
{
  int idx, hid, jndex;
  hist1d_p hsid[6];
  char title[120];
  char *spy_name;
  jndex = spy_attr[index].type;

  idx = idbase + SPY_BASE * getSpyBase(jndex);
  spy_name = objectName(jndex);

  hid = idx + 1; 
  sprintf(title, " %s Event Tag for %s", key, spy_name);
  if (!hist1d_idtoptr(hid)) hsid[0] = hist1d_new(hid, title, 32, 0., 255.);
  assert(hsid[0] == hist1d_idtoptr(hid));

  hid = idx + 2; 
  sprintf(title, " %s Parity Bit for %s", key, spy_name);
  if (!hist1d_idtoptr(hid)) hsid[1] = hist1d_new(hid, title, 2, 0., 2.);
  assert(hsid[1] == hist1d_idtoptr(hid));
      
  hid = idx + 3; 
  sprintf(title, "%s Error bits (1) for %s", key, spy_name);
  if (!hist1d_idtoptr(hid)) hsid[2] = hist1d_new(hid, title, 8, 1., 9.);
  assert(hsid[2] == hist1d_idtoptr(hid));

  hid = idx + 4; 
  sprintf(title, "%s Error bits (all) for %s", key, spy_name);
  if (!hist1d_idtoptr(hid))  hsid[3] = hist1d_new(hid, title, 8, 1., 9.);
  assert(hsid[3] == hist1d_idtoptr(hid));

  hid = idx + 5; 
  sprintf(title, "%s L1 Trigger Info for %s", key, spy_name);
  if (!hist1d_idtoptr(hid)) hsid[4] = hist1d_new(hid, title, 4, 0., 3.);
  assert(hsid[4] == hist1d_idtoptr(hid));
      
  hid = idx + 6; 
  sprintf(title, "%s L2 Buffer ID for %s", key, spy_name);
  if (!hist1d_idtoptr(hid)) hsid[5] = hist1d_new(hid, title, 4, 0., 3.);
  assert(hsid[5] == hist1d_idtoptr(hid));
}
void book_tf_input_word(int index, char *mkey, int idbase) 
{
}
void book_tf_output_word(int index, char *key, int idbase) 
{
  int idx, i, jndex;
  hist1d_p hsid[12];
  char title[120];
  char *spy_name;

  jndex = spy_attr[index].type;
  idx = idbase + SPY_BASE * getSpyBase(jndex);
  spy_name = objectName(jndex);

  sprintf(title,"%s Number of tracks for %s", key, spy_name);
  hsid[0] = hist1d_new(idx+11, title, 50, 0., 50.);

  sprintf(title,"%s Z_out of the track for %s", key, spy_name);
  hsid[1] = hist1d_new(idx+12, title, 6, 0., 6.);

  sprintf(title,"%s Z_in of the track for %s", key, spy_name);
  hsid[2] = hist1d_new(idx+13, title, 6, 0., 6.);

  sprintf(title,"%s Phi of the track for %s", key, spy_name);
  hsid[3] = hist1d_new(idx+14, title, 50, 0., 2.*M_PI);

  sprintf(title,"%s Impact Parameter of the track for %s", key, spy_name);
  hsid[4] = hist1d_new(idx+15, title, 100, -0.05, 0.05);

  sprintf(title,"%s Pt of the track for %s", key, spy_name);
  hsid[5] = hist1d_new(idx+16, title, 100, 0., 10.);

  for (i = 0; i < 5; i++) {
    sprintf(title,"%s Track Hit Coordinate in layer %d for %s", key, i, spy_name);
    hsid[6+i] = hist1d_new(idx+17+i, title, 100, 0., 0.1);
  }
  sprintf(title,"%s Fit Chi2 of the track for %s", key, spy_name);
  hsid[11] = hist1d_new(idx+22, title, 100, 0., 100.);
}
/* 
 * Book histograms related to Hit words 
 */
void book_hit_word(int index, char *key, int idbase) 
{
  int idx, hid, jndex;
  hist1d_p hsid[5];
  char title[120];
  char *spy_name;

  jndex = spy_attr[index].type;
  idx = idbase + SPY_BASE * getSpyBase(jndex);
  spy_name = objectName(jndex);

  hid = idx + 11; 
  sprintf(title,"%s Number of Hits for %s", key, spy_name);
  if (!hist1d_idtoptr(hid)) hsid[0] = hist1d_new(hid, title, 100, 0., 100.);
  assert(hsid[0] == hist1d_idtoptr(hid));

  hid = idx + 12; 
  sprintf(title,"%s Hit Coordinates for %s", key, spy_name);
  if (!hist1d_idtoptr(hid)) hsid[1] = hist1d_new(hid, title, 100, 0., 100.);
  assert(hsid[1] == hist1d_idtoptr(hid));

  sprintf(title,"%s Long Cluster for %s",key, spy_name);
  hsid[2] = hist1d_new(idx+13, title, 2, 0., 2.);

  sprintf(title,"%s Barrel ID for %s",key, spy_name);
  hsid[3] = hist1d_new(idx+14, title, 7, 0., 7.);

  sprintf(title,"%s SVX Layer ID for %s",key, spy_name);
  hsid[4] = hist1d_new(idx+15, title, 5, 0., 5.);
}
/* 
 * Book histograms related to Road words 
 */
void book_road_word(int index, char *key, int idbase) 
{
  int idx, hid, jndex;
  hist1d_p hsid[6];
  char title[120];
  char *spy_name;

  jndex = spy_attr[index].type;
  idx = idbase + SPY_BASE * getSpyBase(jndex);
  spy_name = objectName(jndex);

  hid = idx + 11;
  sprintf(title,"%s number of Roads for %s ", key, spy_name);
  if (!hist1d_idtoptr(hid)) hsid[0] =  hist1d_new(hid, title, 10, 0., 10.);
  assert(hsid[0] == hist1d_idtoptr(hid));

  sprintf(title,"%s number of Patterns for %s",key, spy_name);
  hsid[1] =  hist1d_new(idx+12, title, 64, 0., 128.);

  sprintf(title," %s number of AMChips for %s", key, spy_name);
  hsid[2] = hist1d_new(idx+13, title, 8, 0., 8.);

  sprintf(title," %s number of AMplugs for %s", key, spy_name);
  hsid[3] = hist1d_new(idx+14, title, 16, 0., 16.);

  sprintf(title," %s number of AMboard for %s", key, spy_name);
  hsid[4] = hist1d_new(idx+15, title, 4, 0., 4.);

  sprintf(title,"Phi Sector for %s", spy_name);
  hsid[5] = hist1d_new(idx+16, title, 13, 0., 13.);
}
void book_hf_input_word(int index, char *mkey, int idx)
{
}
void book_hf_output_word(int index, char *mkey, int idx)
{
}
void book_mrg_word(int index, char *mkey, int idx)
{
}
void book_hb_output_word(int index, char *mkey, int idx)
{
}
void book_tf_word(int index, char *mkey, int idx)
{
}
/* 
 * Delete all the managed histograms
 */
void hsdel(void) {
  hist1d_freeall(NULL);
}
void fill_hf_input_word(int index, int idbase) 
{
}
void fill_hf_output_word(int index, int idbase) 
{
}
void fill_mrg_word(int index, int idbase)
{
}
/* AMS and HB Spys: Reformat the information and fill histograms */
void fill_hit_word(int index, int idbase) 
{
  int nhit = 0;
  int i, idx, jndex;

  jndex = spy_attr[index].type;
  idx = idbase + SPY_BASE * getSpyBase(jndex);
  printf("index = %d, jndex = %d, idx = %d\n", index, jndex, idx);

  /* Hit Spy */
  for (i = 0; i < spy_attr[index].nvalid; i++) {
    if (spy_attr[index].data[i] >> 22) {
      hist1d_fill(hist1d_idtoptr(idx+11), (float) nhit, 1.);
      nhit = 0;
    } else {
      nhit++; 
      fill_hit(spy_attr[index].data[i], idx);
    }
  }
}
/* HB Hit and Road and Out Spys: Reformat the information and fill histograms */
void fill_road_word(int index, int idbase) 
{
  int nroad = 0;
  int i, idx, jndex;

  jndex = spy_attr[index].type;
  idx = idbase + SPY_BASE * getSpyBase(jndex);
  printf("fill_road_word(): index = %d, jndex = %d, idx = %d\n", index, jndex, idx);

  for (i = 0; i < spy_attr[index].nvalid; i++) {
    if (spy_attr[index].data[i] >> 22) {
      hist1d_fill(hist1d_idtoptr(idx+11), (float) nroad, 1.);
      nroad = 0;
    } else {
      nroad++; 
      fill_road(spy_attr[index].data[i], idx);
    }
  }
}
/* 
 * Fill Event tag, Parity bit and the error bits 
 */
void fill_error(uint4 val, int idx) 
{
  hist1d_fill(hist1d_idtoptr(idx+1), (float) (val & mask[8]), 1.);       /* Event Tag */
  hist1d_fill(hist1d_idtoptr(idx+2), (float) ((val >> 8) & mask[1]), 1.); /* Parity Bit */

  fill_error_bit(((val >>  9) & 0x1), idx, 1);  /* Parity Error */
  fill_error_bit(((val >> 10) & 0x1), idx, 2);  /* Lost Sync */
  fill_error_bit(((val >> 11) & 0x1), idx, 3);  /* Fifo Overflow */
  fill_error_bit(((val >> 12) & 0x1), idx, 4);  /* Invalid Data */
  fill_error_bit(((val >> 13) & 0x1), idx, 5);  /* Internal Overflow */
  fill_error_bit(((val >> 14) & 0x1), idx, 6);  /* Truncated Output */
  fill_error_bit(((val >> 15) & 0x1), idx, 7);  /* G-link Lost Lock */
  fill_error_bit(((val >> 16) & 0x1), idx, 8);  /* Parity Error in cable to level 2 */

  hist1d_fill(hist1d_idtoptr(idx+5),(float) ((val >>17) & 0x3), 1.); /* L1 Triiger info */
  hist1d_fill(hist1d_idtoptr(idx+6),(float) ((val >>19) & 0x3), 1.); /* L2 buffer ID */
}
/* 
 * Fill all the error bits in 2 histograms
 */
void fill_error_bit(int abit, int idx, int place) 
{
  if (abit)
    hist1d_fill(hist1d_idtoptr(idx+3), (float) place, 1.0);  /* Error on Bit */
  else
    hist1d_fill(hist1d_idtoptr(idx+4), (float) place, 1.0);  /* Error off bit */
}
/* 
 * Fill the Hit words 
 */
void fill_hit(uint4 val, int idx) 
{
  hist1d_fill(hist1d_idtoptr(idx+12), (float) ((val & mask[14])>>4), 1.);   /* Hit Coordinates */
  hist1d_fill(hist1d_idtoptr(idx+13), (float) ((val & mask[15])>>14), 1.);  /* Long Cluster */
  hist1d_fill(hist1d_idtoptr(idx+14), (float) ((val & mask[18])>>15), 1.);  /* Barrel ID */
  hist1d_fill(hist1d_idtoptr(idx+15), (float) ((val & mask[21])>>18), 1.);  /* SVX Layer ID */
}
/* 
 * Fill the road words 
 */
void fill_road(uint4 val, int idx) 
{
#if 0
  printf("-> %d/%d/%d/%d/%d\n", val & mask[7], ((val >> 7)  & mask[3]), 
	 ((val >> 10) & mask[4]), ((val >> 14) & mask[2]), 
         ((val >> 17) & mask[4]));
#endif
  hist1d_fill(hist1d_idtoptr(idx+12), (float) ( val & mask[7]), 1.0);         /* Pattern */
  hist1d_fill(hist1d_idtoptr(idx+13), (float) ((val >> 7)  & mask[3]), 1.0);  /* AMChip */
  hist1d_fill(hist1d_idtoptr(idx+14), (float) ((val >> 10) & mask[4]), 1.0);  /* AmPlug */
  hist1d_fill(hist1d_idtoptr(idx+15), (float) ((val >> 14) & mask[2]), 1.0);  /* AmBoard */
  hist1d_fill(hist1d_idtoptr(idx+16), (float) ((val >> 17) & mask[4]), 1.0);  /* Phi Sector */
}
void fill_hb_output_word(int index, int idx)
{
}
/* Track fitter input and output words */
void fill_tf_input_word(int index, int idbase) 
{
}
void fill_tf_output_word(int index, int idbase) 
{
  int ntrk = 0, iword = 0;
  int i, idx, jndex;
  int val;
  float d0, curv, pt, phi, zin, zout;

  jndex = spy_attr[index].type;
  idx = idbase + SPY_BASE * getSpyBase(jndex);
  printf("index = %d, jndex = %d, idx = %d\n", index, jndex, idx);

  /* Track output spy buffer */
  for (i = 0; i < spy_attr[index].nvalid; i++) {
    if (spy_attr[index].data[i] >> 22) {
      hist1d_fill(hist1d_idtoptr(idx+11), ntrk*1.0, 1.);
      ntrk = 0;
    } 
    else {
      iword++;
      val = spy_attr[index].data[i];
      if (iword == 1) {
        zout = ((val >> 16) & mask[3]);
        hist1d_fill(hist1d_idtoptr(idx+12), zout, 1.0);  /* Z outer */

        zin = ((val >> 13) & mask[3]);
        hist1d_fill(hist1d_idtoptr(idx+13), zin, 1.0);  /* Z inner */

        phi = (val & mask[13])*((M_PI*2)/PHI_SCALE);
        hist1d_fill(hist1d_idtoptr(idx+14), phi, 1.0); /* phi */
#if 0
	printf("-> %d/%x/%f/%f/%f\n", iword, val, zin, zout, phi);
#endif
      }            
      else if (iword == 2) {
        d0 = ((val >> 9 & 0x1) ? -1.0 : 1.0)*(val & mask[9])*D0_SCALE;
        hist1d_fill(hist1d_idtoptr(idx+15), d0, 1.0);   /* Impact parameter */

        curv = ((val >> 18 & 0x1) ? -1.0 : 1.0)*(val >> 10 & mask[8])*CUR_SCALE;
        if (fabs(curv) > 1e-10) 
           pt = fabs(0.002112 / curv);
        else
           pt = 1e10;
        hist1d_fill(hist1d_idtoptr(idx+16), pt, 1.0);              /* Curvature */
#if 1
	printf("-> %d/%x/%f/%f/%f\n", iword, val, d0, curv, pt);
#endif
      }            
      else if (iword == 4) { 
        hist1d_fill(hist1d_idtoptr(idx+17), (float) (val & mask[10]), 1.0);         /* x[0] */
        hist1d_fill(hist1d_idtoptr(idx+18), (float) ((val >> 10) & mask[10]), 1.0); /* x[1] */
#if 0
	printf("-> %d/%x/%d/%d\n", iword, val, val & mask[10], ((val >> 10) & mask[10]));
#endif
      }
      else if (iword == 5) { 
#if 0
	printf("-> %d/%x/%d/%d\n", iword, val, val & mask[10], ((val >> 10) & mask[10]));
#endif
        hist1d_fill(hist1d_idtoptr(idx+19), (float) (val & mask[10]), 1.0);         /* x[2] */
        hist1d_fill(hist1d_idtoptr(idx+20), (float) ((val >> 10) & mask[10]), 1.0); /* x[3] */
      }
      else if (iword == 6) { 
#if 0
	printf("-> %d/%x/%d/%d\n", iword, val, val & mask[10], ((val >> 10) & mask[11]));
#endif
        hist1d_fill(hist1d_idtoptr(idx+21), (float) (val & mask[10]), 1.0);         /* x[4] */
        hist1d_fill(hist1d_idtoptr(idx+22), (float) ((val >> 10) & mask[11]), 1.0); /* chi2 */
      }
      if (iword == 7) {
        ntrk++;
        iword = 0;
      }
    }
  }
}
int getSpyBase(int spyId) 
{
  switch (spyId) {
    case HF_ISPY_0: 	return 1;
    case HF_ISPY_1: 	return 2;
    case HF_ISPY_2: 	return 3;
    case HF_ISPY_3: 	return 4;
    case HF_ISPY_4: 	return 5;
    case HF_ISPY_5: 	return 6;
    case HF_ISPY_6: 	return 7;
    case HF_ISPY_7: 	return 8;
    case HF_ISPY_8: 	return 9;
    case HF_ISPY_9: 	return 10;
    case HF_OUT_SPY: 	return 11;
    case MRG_A_SPY:     return 1;
    case MRG_B_SPY:     return 2;
    case MRG_C_SPY:     return 3;
    case MRG_D_SPY:     return 4;
    case MRG_OUT_SPY:   return 5;
    case AMS_HIT_SPY:   return 1;
    case AMS_OUT_SPY:   return 2;
    case HB_HIT_SPY:    return 1;
    case HB_ROAD_SPY:   return 2;
    case HB_OUT_SPY:    return 3;
    case TF_ISPY:       return 1;
    case TF_OSPY:       return 2;
    default:            return 0;
  }
}
char *objectName(int spyId)
{
  switch (spyId) {
    case HF_ISPY_0: 	return "HF_ISPY_0";
    case HF_ISPY_1: 	return "HF_ISPY_1";
    case HF_ISPY_2: 	return "HF_ISPY_2";
    case HF_ISPY_3: 	return "HF_ISPY_3";
    case HF_ISPY_4: 	return "HF_ISPY_4";
    case HF_ISPY_5: 	return "HF_ISPY_5";
    case HF_ISPY_6: 	return "HF_ISPY_6";
    case HF_ISPY_7: 	return "HF_ISPY_7";
    case HF_ISPY_8: 	return "HF_ISPY_8";
    case HF_ISPY_9: 	return "HF_ISPY_9";
    case HF_OUT_SPY: 	return "HF_OUT_SPY";
    case MRG_A_SPY:     return "MRG_A_SPY";
    case MRG_B_SPY:     return "MRG_B_SPY";
    case MRG_C_SPY:     return "MRG_C_SPY";
    case MRG_D_SPY:     return "MRG_D_SPY";
    case MRG_OUT_SPY:   return "MRG_OUT_SPY";
    case AMS_HIT_SPY:   return "AMS_HIT_SPY";
    case AMS_OUT_SPY:   return "AMS_OUT_SPY";
    case HB_HIT_SPY:    return "HB_HIT_SPY";
    case HB_ROAD_SPY:   return "HB_ROAD_SPY";
    case HB_OUT_SPY:    return "HB_OUT_SPY";
    case TF_ISPY:       return "TF_ISPY";
    case TF_OSPY:       return "TF_OSPY";
    default: 	        return  " ";
  }
}
void hsfill(void) {
  int i, index, jndex, board, idbase;
  for (i = 0; i < nBuffer; i++) { 
    jndex = spy_attr[i].type;
    index = spy_attr[i].board;
    board = board_attr[index].type;
    printf("hsfill(): jndex = %d, index = %d, board = %d\n", jndex, index, board);

    idbase = CRATE_BASE * (crate.id+1) +  BOARD_BASE * (board-30) + 
	     SLOT_BASE  * board_attr[index].slot; 
#if 1
    fill_common_error(i, idbase);
#endif
    switch (jndex) {  
    case HF_ISPY_0:
    case HF_ISPY_1:
    case HF_ISPY_2:
    case HF_ISPY_3:
    case HF_ISPY_4:
    case HF_ISPY_5:
    case HF_ISPY_6:
    case HF_ISPY_7:
    case HF_ISPY_8:
    case HF_ISPY_9:
      printf("Fill HF Input Words\n");
      fill_hf_input_word(i, idbase);
      break;
    case HF_OUT_SPY:
      printf("Fill HF Output Words\n");
      fill_hf_output_word(i, idbase);
      break;
    case MRG_A_SPY:
    case MRG_B_SPY:
    case MRG_C_SPY:
    case MRG_D_SPY:
    case MRG_OUT_SPY:
      printf("Fill Merger Words\n");
      fill_mrg_word(i, idbase);
      break;
    case AMS_HIT_SPY:
      printf("Fill AMS Hit Words\n");
      fill_hit_word(i, idbase);
      break;
    case AMS_OUT_SPY:
      printf("Fill AMS ROAD Words\n");
      fill_road_word(i, idbase);
      break;
    case HB_HIT_SPY:
      printf("Fill HB Hit Words\n");
      fill_hit_word(i, idbase);
      break;
    case HB_ROAD_SPY:
      printf("Fill HB ROAD Words\n");
      fill_road_word(i, idbase);
      break;
    case HB_OUT_SPY:
      printf("Fill HB Output Words\n");
      fill_hb_output_word(i,idbase);
      break;
    case TF_ISPY:
      fill_tf_input_word(i, idbase);
      break;
    case TF_OSPY:
      fill_tf_output_word(i, idbase);
      break;
    default:
      break;
    }
  }
}
void write_hist(char *filename) 
{
  int i;
  hist1d_p h;
  char hist_file[80];
  FILE *fp;
  sprintf(hist_file, "%s.hist", filename);
  fp = gfopen(hist_file, "w");
  fprintf(fp, "%d\n", hist1d_get_nhist());
  for (i = 0; i < hist1d_get_nhist(); i++) {
    h = (hist1d_p) hist1d_idtoptr(hist1d_get_id(i));    
    hist1d_dump_to_file(h, fp);
  }
  fclose(fp);
}
void fill_common_error(int index, int idbase) 
{
  int i, idx, jndex;

  jndex = spy_attr[index].type;
  idx = idbase +  SPY_BASE * getSpyBase(jndex);

  printf("index = %d, idbase = %d, nvalid = %d\n", index, idbase, spy_attr[index].nvalid);
  for (i = 0; i < spy_attr[index].nvalid; i++) {
    if (spy_attr[index].data[i] >> 22) {
      fill_error(spy_attr[index].data[i], idx);
    }
  }
}
