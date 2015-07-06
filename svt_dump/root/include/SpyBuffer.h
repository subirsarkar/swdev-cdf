#ifndef __HEADER_SPYBUF__
#define __HEADER_SPYBUF__

typedef unsigned int uint4;

const int NBIT            = 25;
const int SPY_LENGTH      = 131072;
const int END_EVENT_BIT   = 0x600000;
const int END_EVENT_TAG   = 0xff;

const int MAX_BOARD   = 12;
const int MAX_BUFFER  = 86;

const int AMS  = 32;
const int HB   = 33;  
const int MRG  = 34;
const int HF   = 35;  
const int TF   = 36;

uint4 mask[NBIT];

const int HF_ISPY_0  = 0;
const int HF_ISPY_1  = 1;
const int HF_ISPY_2  = 2;
const int HF_ISPY_3  = 3;
const int HF_ISPY_4  = 4;
const int HF_ISPY_5  = 5;
const int HF_ISPY_6  = 6;
const int HF_ISPY_7  = 7;
const int HF_ISPY_8  = 8;
const int HF_ISPY_9  = 9;
const int HF_OUT_SPY = 10;
const int MRG_A_SPY  = 11;
const int MRG_B_SPY  = 12;
const int MRG_C_SPY  = 13;
const int MRG_D_SPY  = 14;
const int MRG_OUT_SPY = 15;
const int AMS_HIT_SPY = 16;
const int AMS_OUT_SPY = 17;
const int HB_HIT_SPY  = 18;
const int HB_ROAD_SPY = 19;
const int HB_OUT_SPY  = 20;

/* Useful structures */

class CrateAttribute {
public:
  CrateAttribute(char *name = "b0svt05", int id = 5):
    fName(name), fId(id)   {}
  ~CrateAttribute() {}
  char *getName() {return fName;}
  int  getId() {return fId;}
private:
  char fName[20];
  int fId;
};

class BoardAttribute {
public:
  BoardAttribute(const char name, CrateAttribute *crate, 
    int slot, int type, char *mkey):
    fName(name), fCrate(crate), fSlot(slot), fType(type), fMkey(mkey)   {}
  ~BoardAttribute() {}
  char *getName() {return fName;}
  CrateAttribute getCrate() {return fCrate;}
  int getType() {return fType;}  
  int getSlot() {return fSlot;}  
  char *  getMkey() {return fMkey;}
private:
  CrateAttribute *fCrate;
  int fSlot;
  char *fName;
  int fType;
  char *fMkey;
};

class SpyAttribute {
public:
  SpyAttribute(char *name, BoardAttribute *board, int *data, 
	       int nvalid, int pointer, int wrap):
      fName(name), fBoard(board), fData(data), fNvalid(nvalid), 
      fPointer(pointer), fWrap(wrap){}
  virtual ~SpyAttribute() {}
  char *getName() {return fName;}
  BoardAttribute *getBoardAttribute(){return fBoard;}
  int getValid() {return fNvalid;}
  int getPointer() {return fPointer;}
  int getWrap() {return fWrap;}
  virtual void buildVector(int *data);
  virtual void writeBuffer(String filename);
  virtual int *getEndEventWords();
  virtual int *getEventTags();
  EventFrac *getEvent(int index){return vec->At(index);}
private:
  TObjArray *vec;
  int fNvalid;
  int fPointer;
  int fWrap;
  BoardAttribute *fBoard;
  char *fName;
};

class EventFrac {
public:
  EventFrac(int *word, int endEvent):
    fWords(words), fEndEvent(endEvent) {}
  ~EventFrac() {}
  int *getWords() {return fWords;}
  int getEndEvent() {return fEndEvent;}
private:
  int *fWords;
  int fEndEvent;
};

int nBoards;
int nBuffer;

void alloc_buffers(void);
void delete_buffers(void);
void build_mask(uint4 *mask, int NBIT);
#endif
