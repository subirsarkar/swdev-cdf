#ifndef __HEADER_HBOOK_
#define __HEADER_HBOOK_

#include "spy_buffer.h"
#include "chist.h"

enum {
  CRATE_BASE = 100000000,
  BOARD_BASE = 10000000,
  SLOT_BASE  = 100000,
  SPY_BASE   = 1000
};

/* Histograms */
void hsbook(void);
void hsdel(void);
void book_error(int, char *, int);
void book_hit_word(int, char *, int); 
void book_road_word(int, char *, int); 
void book_hf_input_word(int, char *, int); 
void book_hf_output_word(int, char *, int); 
void book_mrg_word(int, char *, int); 
void book_hb_output_word(int, char *, int); 
void book_tf_input_word(int, char *, int); 
void book_tf_output_word(int, char *, int); 
void fill_error(uint4, int); 
void fill_error_bit(int, int, int); 
void fill_common_error(int index, int idbase);

void fill_hit(uint4, int); 
void fill_road(uint4, int); 

void fill_hf_input_word(int, int); 
void fill_hf_output_word(int, int); 
void fill_hb_output_word(int, int); 
void fill_hit_word(int, int); 
void fill_road_word(int, int); 
void fill_hf_word(int, int);
void fill_mrg_word(int, int);
void fill_ams_word(int, int);
void fill_hb_word(int, int);
void fill_tf_input_word(int, int);
void fill_tf_output_word(int, int);

int getSpyBase(int spyId);
char *objectName(int spyId);
void hsfill(void);
void write_hist(char *filename);
#endif
