/*
 * data & function definitions for simple 1d histogramming 
 * Original code: WJA 4/96
 * A few more functionalities: SS 8/2000
 */

/*
 * Prevent double inclusion of file
 */
#ifndef MYHIST_H
#define MYHIST_H

/*
 * Data structure to describe simple 1d histogram
 */
typedef struct {
  int typetag;		/* type tag for pointer validation */
  int hid;              /* histogram id to uniquely specify a histogram */
  int imanaged;		/* index into list of "managed" histograms (or -1) */
  int nbins;		/* number of bins */
  float low;		/* lower edge */
  float high;		/* upper edge */
  int ncalls;		/* number of fill calls */
  int nunder;		/* number of underflows */
  int nover;		/* number of overflows */
  float *bin;		/* bin contents */
  float *error;         /* error content */
  char *name;		/* title */
} hist1d_t;
typedef hist1d_t *hist1d_p;

/*
 * Function prototypes for externally linked functions
 */

/* Initialise the histogram */
hist1d_p hist1d_new(int id, char *name, int nbins, float low, float high);

/* Delete histogram object */
void hist1d_free(hist1d_p h);

/* Fill the histogram*/
void hist1d_fill(hist1d_p h, float x, float weight);

/* Given x value, returns the corresponding bin number */
int hist1d_xtobin(hist1d_p h, float x);

/* Content of the ith bin */
float hist1d_get_biny(hist1d_p h, int bin);

/* Getters */
int hist1d_get_hid(hist1d_p h);                         /* Histogram id */
int hist1d_get_nbins(hist1d_p h);                       /* no. of bins */
float hist1d_get_xlow(hist1d_p h);                      /* Lower x value */
float hist1d_get_xhigh(hist1d_p h);                     /* Upper x value */
int hist1d_get_underflow(hist1d_p h);                   /* Underflow content */
int hist1d_get_overflow(hist1d_p h);                    /* Overflow content */
float *hist1d_get_content(hist1d_p h);                  /* Bin contents */
float *hist1d_get_error(hist1d_p h);                    /* Error contents */
char *hist1d_get_name(hist1d_p h);                      /* Title */

void hist1d_dump(hist1d_p h);                           /* Prints details */
void hist1d_dumpall(void);                                  /* Prints details */
float hist1d_hsum(hist1d_p h);                          /* Sum bin contents */
void hist1d_hstati(hist1d_p h, float *mean, float *sd); /* Mean and sd */
float hist1d_hmax(hist1d_p h, int *ibin);               /* Max bin content */
float hist1d_hmin(hist1d_p h, int *ibin);               /* Min bin content */
int hist1d_nentry(hist1d_p h);                          /* # hist entries */

/* 
 * Use a hash table with the histogram id as the 'key' and the
 * histogram 'pointer' as the value in order not to mess with global
 * variables when booking and filling histograms in different
 * places. This will also give us the possibility to lookup the
 * hashtable content and delete all the histograms when we are
 * through. The implementation of the hashtable must take care of the
 * fact that when histograms are created new key-value pairs are
 * added, and when a particular histogram is deleted the corresponding
 * entry is removed too from the table.  
 */

/* Retrieve pointer to the histogram from the histogram ID */
hist1d_p hist1d_idtoptr(int);

/* Free all histograms with non-zero ID numbers */
void hist1d_freeall(FILE *fp_printids);

/* Reset the contents of a histogram */
void hist1d_reset(hist1d_p h);
void hist1d_resetall(void);

/* Get number of histograms created */
int hist1d_get_nhist(void);

/* Get histogram attributes */
char *hist1d_get_attr(int, int *, float *, float *);

/* List booked histograms */
void hist1d_list(void);

/* Get histogram Id's sequentially */
int hist1d_get_id(int index);
/*
 * Dump details of a histograms to a file
 */
void hist1d_dump_to_file(hist1d_p h, FILE *fp);

#endif /* MYHIST_H */
