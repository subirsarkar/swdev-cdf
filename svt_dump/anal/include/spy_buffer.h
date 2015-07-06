#ifndef __HEADER_SPYBUF__
#define __HEADER_SPYBUF__

#define NEL(x) (sizeof((x))/sizeof((x)[0]))

typedef unsigned int uint4;
enum {
  NBIT            = 25,
  SPY_LENGTH      = 131072,
  END_EVENT_BIT   = 0x600000,
  END_EVENT_TAG   = 0xff,
};

enum {
  MAX_BOARD   = 13,
  MAX_BUFFER  = 100
};

enum { 
  AMS  = 32, 
  HB   = 33,  
  MRG  = 34,
  HF   = 35,  
  TF   = 36 
};

uint4 mask[NBIT];

enum {
   HF_ISPY_0,
   HF_ISPY_1,
   HF_ISPY_2,
   HF_ISPY_3,
   HF_ISPY_4,
   HF_ISPY_5,
   HF_ISPY_6,
   HF_ISPY_7,
   HF_ISPY_8,
   HF_ISPY_9,
   HF_OUT_SPY,
   MRG_A_SPY,
   MRG_B_SPY,
   MRG_C_SPY,
   MRG_D_SPY,
   MRG_OUT_SPY,
   AMS_HIT_SPY,
   AMS_OUT_SPY,
   HB_HIT_SPY,
   HB_ROAD_SPY,
   HB_OUT_SPY,
   TF_ISPY,
   TF_OSPY
};

/* Useful structures */
typedef struct 
{
  char name[20];
  int id;
} svt_crate;
svt_crate crate;

typedef struct 
{
  int type;            /* Board type a la svtvme */
  char mkey[20];       /* Board metakey */
  int slot;            /* Board slot number */
} SvtBoardAttr;
SvtBoardAttr board_attr[MAX_BOARD];

typedef struct 
{
  uint4 *data;         /* Array containing Spy buffer */
  int pointer;         /* Spy Pointer value */
  int freeze;          /* Spy Freeze bit */
  int wrap;            /* Spy Wrap bit */
  int nvalid;          /* Number of valid words read */
  int type;            /* Spy buffer type */
  int board;           /* Spy buffer belongs to this board */
  char *board_name;    /* Name of the board which holds this spy buffer */
  char *name;          /* Name of the spy buffer, needed for special format of output */
} SpyBufferAttr;
SpyBufferAttr spy_attr[MAX_BUFFER];

int nBoards;
int nBuffer;

void alloc_buffers(void);
void delete_buffers(void);
void build_mask(uint4 *mask, int NBIT);
#endif
