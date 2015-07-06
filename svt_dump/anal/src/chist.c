/*
 * simple 1d histogramming package
 * WJA 4/96    creation
 * SS  8/2000  a few other functionalities
 */

/*
 * Significant change 2000-09-26 (WJA, SS):  Histograms can have ID
 * numbers.  If ID is non-zero, then it must be unique (among all
 * histograms not yet freed), and histogram will be managed in a
 * static data structure, so that operations (e.g. "free") can be
 * performed on all managed histograms.
 */

/*
 *  New: hist1d_reset(p), hist1d_resetall() and hist1d_get_nhist()
 *       hist1d_get_attr(..), hist1d_list(), hist1d_dumpall()
 *       hist1d_get_id(int), hist1d_dump_to_file(hist1d_p, FILE *)
 *  09/29/00 -- Subir
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <assert.h>
#include "chist.h"

#define hist1d_typetag 0x5680
#define dbgout stdout
static int nhist = 0;

/*
 * List of all "managed" histograms; for now, we implement this as
 * a simple "vector," with dumb linear search for ID number.  So that
 * freeing is simple, unused spaces are given ID number zero.
 */
static struct {
  int id;
  hist1d_p p;
} *managed = 0;
static int nmanaged = 0; /* number of managed histograms */
static int mmanaged = 0; /* allocated space for managed histograms */

/*
 * Prototypes for internal functions
 */
static void *chk_malloc(int size);
static void *chk_realloc(void *p, int size);
float fmax(float, float);

/*
 * Malloc that aborts if it fails
 */
static void *
chk_malloc(int size)
{
  void *p;
  if (!(p = malloc(size))) {
    fprintf(dbgout, "chist: chk_malloc: malloc() returns NULL\n");
    abort();
  }
  return p;
}

/*
 * Realloc that aborts if it fails
 */
static void *
chk_realloc(void *p, int size)
{
  if (!(p = realloc(p, size))) {
    fprintf(dbgout, "chist: chk_realloc: realloc() returns NULL\n");
    abort();
  }
  return p;
}

/*
 * Dynamically allocate & initialize a new 1d histogram
 */
hist1d_p 
hist1d_new(int id, char *name, int nbins, float low, float high)
{
  int i;
  hist1d_p h = 0;

#if 0
  fprintf(dbgout, "Input (1) -> %d %s %d %6.1f %6.1f\n", 
     id, name, nbins, low, high);  
#endif
  if (hist1d_idtoptr(id) != 0) {
    fprintf(dbgout, "ID %d already in use!\n", id);
    return 0;    /* ID already in use */
  }
  h = (hist1d_p) chk_malloc(sizeof(hist1d_t));
  h->typetag  = hist1d_typetag;
  h->hid      = id;
  h->imanaged = -1;
  if (id != 0) { /* add to "managed" list */
    int i = 0;
    for (i = 0; i < nmanaged; i++)
      if (managed[i].p == 0) {
	h->imanaged = i; /* re-use old (deleted) index */
	break;
      }
    if (h->imanaged == -1) { /* there is no existing index to re-use */
      if (nmanaged >= mmanaged) {
	/* need more memory */
	int minmanaged = 16;
	mmanaged = (mmanaged < minmanaged) ? minmanaged : 2*mmanaged;
	managed = chk_realloc(managed, mmanaged*sizeof(managed[0]));
	assert(managed != 0);
      }
      assert(nmanaged < mmanaged);
      h->imanaged = nmanaged++;
    }
    assert(h->imanaged >= 0 && h->imanaged < nmanaged);
    managed[h->imanaged].id = h->hid;
    managed[h->imanaged].p = h;
  }
  h->nbins  = nbins;
  h->low    = low;
  h->high   = high;
  h->ncalls = 0;
  h->nunder = 0;
  h->nover  = 0;
  h->bin    = (float *) chk_malloc(nbins*sizeof(float));
  h->error  = (float *) chk_malloc(nbins*sizeof(float));
  for (i = 0; i < nbins; i++) {
    h->bin[i]   = 0.0;
    h->error[i] = 0.0;
  }
  h->name = (char *) chk_malloc(1+strlen(name));
  strcpy(h->name, name);
  fprintf(dbgout, "hist1d_new: created at p = %p id = %d \"%s\" with\n", 
	  h, h->hid, h->name);
  fprintf(dbgout, "            %d bins from %7.2f to %7.2f\n", 
	  h->nbins, h->low, h->high);
  nhist++;
  return h;
}

/*
 * Free a 1d histogram
 */
void 
hist1d_free(hist1d_p h)
{
  printf("freeing p=%p id=%d (nm=%d, im=%d)\n", 
	 h, h->hid, nmanaged, h->imanaged);
  assert(h->typetag == hist1d_typetag);
  if (h->hid != 0) {
    assert(h->imanaged < nmanaged);
    assert(h->imanaged >= 0);
    assert(managed[h->imanaged].p == h);
    managed[h->imanaged].p  = 0;
    managed[h->imanaged].id = 0;
  }
  /*
   * Overwrite bin contents with garbage, then free it
   */
  assert(h->bin);
  memset(h->bin, -1, h->nbins*sizeof(float));
  free(h->bin);
  /*
   * Overwrite error contents with garbage, then free it
   */
  assert(h->error);
  memset(h->error, -1, h->nbins*sizeof(float));
  free(h->error);
  /*
   * Overwrite histogram title with garbage, then free it
   */
  assert(h->name);
  memset(h->name, -1, 1+strlen(h->name));
  free(h->name);
  /*
   * Overwrite hist1d structure with garbage, then free it
   */
  memset(h, -1, sizeof(*h));
  free(h);
}

/*
 * Dump details of a histograms
 */
void
hist1d_dump(hist1d_p h)
{
  int ibin;
  float *cont, *error;
  assert(h->typetag == hist1d_typetag);
  fprintf(dbgout, "hist1d_dump: histogram object %p id %d named \"%s\"\n", 
	  h, h->hid, h->name);
  fprintf(dbgout, "            %d bins from %f to %f\n", 
	  h->nbins, h->low, h->high);
  cont  = hist1d_get_content(h);
  error = hist1d_get_error(h);
  for (ibin = 0; ibin < h->nbins; ibin++) {
     fprintf(dbgout, "%6.1f %6.1f\n", cont[ibin], error[ibin]);
  }
}

/*
 * Dump details of a histograms to a file
 */
void
hist1d_dump_to_file(hist1d_p h, FILE *fp)
{
  int ibin;
  assert(h->typetag == hist1d_typetag);
  fprintf(fp,"%d %d %9.2f %9.2f %d %d %d\n", 
       h->hid, h->nbins, h->low, h->high, h->ncalls, h->nunder, h->nover);  
  fprintf(fp, "%s\n", h->name);
  for (ibin = 0; ibin < h->nbins; ibin++) {
     fprintf(fp, "%6.1f %6.1f\n", h->bin[ibin], h->error[ibin]);
  }
}

/*
 * Add an entry to a 1d histogram
 */
void
hist1d_fill(hist1d_p h, float x, float weight)
{
  int bin;
  assert(h->typetag == hist1d_typetag);
  h->ncalls++;
  bin = hist1d_xtobin(h, x);
  if (bin == -1) h->nunder++;
  else if (bin == h->nbins) h->nover++;
  else {
    assert(bin >= 0 && bin < h->nbins);
    h->bin[bin] += weight;
  }
}

/*
 * Convert x to bin index (-1/N for under/overflow)
 */
int 
hist1d_xtobin(hist1d_p h, float x)
{
  int bin;
  assert(h->typetag == hist1d_typetag);
#if 0
  assert((h->high - h->low) > 0);
#endif
  if ((h->high - h->low) <= 0) return 0;
  if (x < h->low) return -1;

  bin = (x - h->low) * h->nbins / (h->high - h->low);
  /* A temporary and somewhat wrong fix */
  if (bin < 0) {
    fprintf(stderr, "hist1d_xtobin() -> Red Alert, bin = %d, returning 0\n", bin);
    return 0;
  }
#if 0
  assert(bin >= 0);
#endif
  if (bin >= h->nbins) return h->nbins;
  return bin;
}

/*
 * Get contents of a bin
 */
float
hist1d_get_biny(hist1d_p h, int bin)
{
  assert(h->typetag == hist1d_typetag);
  assert(bin >= 0 && bin < h->nbins);
  return h->bin[bin];
}

/*
 * Get Histogram ID
 */
int
hist1d_get_hid(hist1d_p h)
{
  assert(h->typetag == hist1d_typetag);
  return h->hid;
}

/*
 * Get name
 */
char *
hist1d_get_name(hist1d_p h)
{
  assert(h->typetag == hist1d_typetag);
  return h->name;
}

/*
 * Get number of bins
 */
int
hist1d_get_nbins(hist1d_p h)
{
  assert(h->typetag == hist1d_typetag);
  return h->nbins;
}

/*
 * Get lower bound of the histogram
 */
float
hist1d_get_xlow(hist1d_p h)
{
  assert(h->typetag == hist1d_typetag);
  return h->low;
}

/*
 * Get upper bound of the histogram
 */
float
hist1d_get_xhigh(hist1d_p h)
{
  assert(h->typetag == hist1d_typetag);
  return h->high;
}

/*
 * Get underflow
 */
int
hist1d_get_underflow(hist1d_p h)
{
  assert(h->typetag == hist1d_typetag);
  return h->nunder;
}

/*
 * Get overflow
 */
int
hist1d_get_overflow(hist1d_p h)
{
  assert(h->typetag == hist1d_typetag);
  return h->nover;
}

/*
 * Get bin content
 */
float *
hist1d_get_content(hist1d_p h)
{
  assert(h->typetag == hist1d_typetag);
  return h->bin;
}

/*
 * Get error content
 */
float *
hist1d_get_error(hist1d_p h)
{
  assert(h->typetag == hist1d_typetag);
  return h->error;
}

/*
 * Return total histogram content
 */
float
hist1d_hsum(hist1d_p h)
{
  int ibin;
  float *cont;
  float sum = 0;

  assert(h->typetag == hist1d_typetag);
  cont = hist1d_get_content(h);
  for (ibin = 0; ibin < h->nbins; ibin++) 
    sum += cont[ibin];
  return sum;
}

/*
 * Compute mean and standard deviation of the histogram
 */
void 
hist1d_hstati(hist1d_p h, float *mean, float *sd)
{
  int ix, nbin;
  float *cont, *error;
  float xlo, xhi, delx, xval, xave = 0.0, cnor = 0.0;
  float xdev = 0.0, xerr = 0.0;

  assert(h->typetag == hist1d_typetag);

  xlo  = h->low;
  xhi  = h->high; 
  nbin = h->nbins;
  assert(nbin > 0);
  delx = (xhi - xlo) / nbin;

  /* Unpack histograms */
  cont  = hist1d_get_content(h);
  error = hist1d_get_error(h);

  /* Compute average */
  for (ix = 0; ix < h->nbins; ix++) {
    xval = xlo + (ix + 0.5) * delx;
    cnor = cnor + cont[ix];
    xave = xave + xval * cont[ix];
    printf("%f %f %f\n", xval, cnor, xave);
  }
  xave = xave / fmax(1.0e-12, cnor);

  /* Now compute standard deviation */
  for (ix = 0; ix < h->nbins; ix++) {
    xval = xlo  + (ix - 0.5) * delx;
    xdev = xdev +  cont[ix] * pow((xval - xave),2);
  }
  xdev = sqrt(fmax(xdev/fmax(1.0e-12,cnor),0.0));
  xerr = xdev / fmax(1.0e-12, sqrt(fmax(cnor,1.0)));
  
  *mean = xave;
  *sd   = xerr; 
}

/*
 * Return maximum bin content and the corresponding bin index 
 */
float
hist1d_hmax(hist1d_p h, int *ibin)
{
  int i, itmp = -1;
  float *cont, xmax = 0.0;
  assert(h->typetag == hist1d_typetag);
  cont = hist1d_get_content(h);
  for (i = 0; i < h->nbins; i++) {
    if (cont[i] > xmax) {
      xmax = cont[i];
      itmp = i;
    } 
  }
  *ibin = itmp;
  return xmax;
}

/*
 * Return minimum bin content and the corresponding bin index 
 */
float
hist1d_hmin(hist1d_p h, int *ibin)
{
  int i, itmp = -1;
  float *cont, xmin = 999999.0;
  assert(h->typetag == hist1d_typetag);
  cont = hist1d_get_content(h);
  for (i = 0; i < h->nbins; i++) {
    if (cont[i] < xmin) {
      xmin = cont[i];
      itmp = i;
    } 
  }
  *ibin = itmp;
  return xmin;
}

/*
 * Return histogram entries
 */
int
hist1d_nentry(hist1d_p h)
{
  assert(h->typetag == hist1d_typetag);
  return h->ncalls;
}

/* 
 * Return greater of the two values 
 */
float
fmax(float first, float second) 
{
   return (first >= second) ? first : second;
}

/*
 * Look up histogram by id number
 */
hist1d_p
hist1d_idtoptr(int id)
{
  int i = 0;
  assert(id != 0); /* ID should never be 0 for a managed histogram */
  for (i = 0; i < nmanaged; i++)
    if (managed[i].id == id) { /* found a match */
      assert(managed[i].p->imanaged == i);
      return managed[i].p;
    }
  return 0; /* not found */
}

/*
 * Free all managed histograms
 */
void
hist1d_freeall(FILE *fp_printids)
{
  int i = 0;
  for (i = 0; i<nmanaged; i++)
    if (managed[i].p) {
      if (fp_printids) 
	fprintf(fp_printids, 
		"hist1d_freeall: freeing HID %d\n", managed[i].id);
      hist1d_free(managed[i].p);
    }
  nmanaged = 0;
}

/* 
 * Reset the contents of a histogram 
 */
void 
hist1d_reset(hist1d_p h)
{
  if (h) {
    int i;
    h->ncalls = 0;
    h->nunder = 0;
    h->nover  = 0;
    for (i=0; i < h->nbins; i++) {
      h->bin[i]   = 0.0;
      h->error[i] = 0.0;
    }
  }
}

/* 
 * Reset the contents of all the histogram 
 */
void 
hist1d_resetall(void)
{
  int i = 0;
  for (i = 0; i < nmanaged; i++) {
    if (managed[i].p) {
      hist1d_reset(managed[i].p);
    }
  }
}

/* 
 * Get number of histograms created 
 */
int 
hist1d_get_nhist(void)
{
  return nhist;
}

/* 
 * List booked histograms 
 */
void
hist1d_list(void) 
{
  int nbins;
  char *title = "";
  float low, high;

  int i = 0;
  printf("hist1d_list() :\n");
  for (i = 0; i < nmanaged; i++) {
    if (managed[i].id) {
      title = hist1d_get_attr(managed[i].id, &nbins, &low, &high);
      printf("  %d\t\'%s \'  %d %6.2f %6.2f\n", 
         managed[i].id, title, nbins, low, high);
    }
  }
   
}

/* 
 * Get histogram attributes 
 */
char *
hist1d_get_attr(int hid, int *nbins, float *low, float *high)
{
  hist1d_p h = hist1d_idtoptr(hid);
  *nbins = h->nbins;  
  *low   = h->low;  
  *high  = h->high;  

  return h->name;
}

/* 
 * Print details of all the histograms 
 */
void
hist1d_dumpall(void) 
{
  int i = 0;
  for (i = 0; i < nmanaged; i++) 
    if (managed[i].p) 
       hist1d_dump(managed[i].p);
}

/* 
 * Get histogram Id's sequentially 
 */
int 
hist1d_get_id(int index)
{
  return managed[index].id;
}
