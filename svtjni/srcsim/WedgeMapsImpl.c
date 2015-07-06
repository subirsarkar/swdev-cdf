#include <stdlib.h>
#include <assert.h>
#include "jnitools.h"
#include "jsvtsim_WedgeMapsImpl.h"
#include "svtsim/svtsim.h"

/* Local function protoypes */
/* Convert the wedgemaps_t handle passed from Java into real pointer */
static svtsim_wedgeMaps_t *getCHandle(JNIEnv *env, jobject self);

static svtsim_wedgeMaps_t *wedgeMaps_new(void);
static void wedgeMaps_free(svtsim_wedgeMaps_t *p);
static int maps_pattSS(svtsim_wedgeMaps_t *p, int road, int layer);
static int maps_dSSdz(svtsim_wedgeMaps_t *p, int layer);
static int maps_ssStrips(svtsim_wedgeMaps_t *p, int layer);
static int maps_wedge(svtsim_wedgeMaps_t *p);
/*
 * Implementation of <wedgeMaps_new>
 */
JNIEXPORT jobject JNICALL Java_jsvtsim_WedgeMapsImpl_wedgeMaps_1new
  (JNIEnv *env, jclass cls)
{
  svtsim_wedgeMaps_t *_self = wedgeMaps_new();
  assert(_self != NULL);

  return JObjectFromPointer(env, "jsvtsim/wedgemaps_t", _self);
}
/*
 * Implementation of <wedgeMaps_free>
 */
JNIEXPORT void JNICALL Java_jsvtsim_WedgeMapsImpl_wedgeMaps_1free
  (JNIEnv *env, jclass cls, jobject self)
{
  wedgeMaps_free(getCHandle(env, self));
}
/* 
 * Implementation of <svtsim_hitToSS>
 */ 
JNIEXPORT jint JNICALL Java_jsvtsim_WedgeMapsImpl_svtsim_1hitToSS 
  (JNIEnv *env, jclass cls, jobject self, jint hit)
{
  return ((jint) svtsim_hitToSS(getCHandle(env, self), (int)hit));
} 

/* 
 * Implementation of <svtsim_ssEdge>
 */ 
JNIEXPORT jint JNICALL Java_jsvtsim_WedgeMapsImpl_svtsim_1ssEdge 
  (JNIEnv *env, jclass cls, jobject self, jint layer, jint ss)
{
  return ((jint) svtsim_ssEdge(getCHandle(env, self), (int)layer, (int)ss));
}

/* 
 * Implementation of <svtsim_tfSS>
 */ 
JNIEXPORT jint JNICALL Java_jsvtsim_WedgeMapsImpl_svtsim_1tfSS 
  (JNIEnv *env, jclass cls, jobject self, jint road)
{
  return ((jint) svtsim_hitToSS(getCHandle(env, self), (int)road));
}

/*
 * Implementation of <maps_dSSdz>
 */
JNIEXPORT jint JNICALL Java_jsvtsim_WedgeMapsImpl_maps_1dSSdz
  (JNIEnv *env, jclass cls, jobject self, jint layer)
{
  return ((jint)maps_dSSdz(getCHandle(env, self), (int)layer));
}
/*
 * Implementation of <svtsim_initMaps>
 */
JNIEXPORT jint JNICALL Java_jsvtsim_WedgeMapsImpl_svtsim_1initMaps
  (JNIEnv *env, jclass cls, jobject self, jint wedge, 
     jstring ifitFile, jstring pattFile, jstring ssFile)
{
  int ret;
  const char *_ifitFile = (*env)->GetStringUTFChars(env, ifitFile, NULL);  
  const char *_pattFile = (*env)->GetStringUTFChars(env, pattFile, NULL);  
  const char *_ssFile   = (*env)->GetStringUTFChars(env, ssFile, NULL);  

  assert(_ifitFile != NULL);     
  assert(_pattFile != NULL);     
  assert(_ssFile   != NULL);     

   ret = svtsim_initMaps(getCHandle(env, self), (int)wedge, 
     _ifitFile, _pattFile, _ssFile);

  (*env)->ReleaseStringUTFChars(env, ifitFile, _ifitFile); 
  (*env)->ReleaseStringUTFChars(env, pattFile, _pattFile); 
  (*env)->ReleaseStringUTFChars(env, ssFile,   _ssFile); 

   return ((jint)ret);
}


/*
 * Implementation of <maps_pattSS>
 */
JNIEXPORT jint JNICALL Java_jsvtsim_WedgeMapsImpl_maps_1pattSS
  (JNIEnv *env, jclass cls, jobject self, jint road, jint layer)
{
  return ((jint)maps_pattSS(getCHandle(env, self), (int)road, (int)layer));
}

/*
 * Implementation of <maps_ssStrips>
 */
JNIEXPORT jint JNICALL Java_jsvtsim_WedgeMapsImpl_maps_1ssStrips
  (JNIEnv *env, jclass cls, jobject self, jint layer)
{
  return ((jint)maps_ssStrips(getCHandle(env, self), (int)layer));
}

/*
 * Implementation of <maps_wedge>
 */
JNIEXPORT jint JNICALL Java_jsvtsim_WedgeMapsImpl_maps_1wedge
  (JNIEnv *env, jclass cls, jobject self)
{
  return ((jint)maps_wedge(getCHandle(env, self)));
}

/*
 * Implementation of <svtsim_crcRam>
 */
JNIEXPORT jint JNICALL Java_jsvtsim_WedgeMapsImpl_svtsim_1crcRam
  (JNIEnv *env, jclass cls, jobject self, jint which)
{
  return ((jint)svtsim_crcRam(getCHandle(env, self), (int)which));
}

/*
 * Implementation of <svtsim_getRam>
 */
JNIEXPORT jint JNICALL Java_jsvtsim_WedgeMapsImpl_svtsim_1getRam
  (JNIEnv *env, jclass cls, jobject self, jint which, jintArray d, jint nd, jint d0)
{
  int ret;
  jint *_carr;
  jsize len;

  len = (*env)->GetArrayLength(env, d);
  if (len <= 0) return -1;

  _carr = (*env)->GetIntArrayElements(env, d, NULL);
  if (_carr == NULL) { /* User must preallocate array in Java program */
    return -1;
  }

  ret = svtsim_getRam(getCHandle(env, self), (int)which, (int *)_carr, 
                          (int)nd, (int)d0);
  (*env)->ReleaseIntArrayElements(env, d, _carr, 0);

  return ((jint)ret);
}

/*
 * Implementation of <svtsim_initFromMapSet>
 */
JNIEXPORT jint JNICALL Java_jsvtsim_WedgeMapsImpl_svtsim_1initFromMapSet
  (JNIEnv *env, jclass cls, jobject self, jint wedge, 
      jstring msName, jlong msCrc, jstring dlPath, jboolean useDB)
{
  int ret;
  const char *_msName = (*env)->GetStringUTFChars(env, msName, NULL);  
  const char *_dlPath = (*env)->GetStringUTFChars(env, dlPath, NULL);  

  assert(_msName != NULL);     
  assert(_dlPath != NULL);     

  ret = svtsim_initFromMapSet(getCHandle(env, self), (int)wedge,
      _msName, (long)msCrc, _dlPath, ((useDB) ? 1 : 0));

  (*env)->ReleaseStringUTFChars(env, msName, _msName); 
  (*env)->ReleaseStringUTFChars(env, dlPath, _dlPath); 

  return ((jint) ret);
}

/*
 * Implementation of <svtsim_useHwSet>
 */
JNIEXPORT jint JNICALL Java_jsvtsim_WedgeMapsImpl_svtsim_1useHwSet
  (JNIEnv *env, jclass cls, jobject self, jint wedge, 
       jstring hsName, jlong hsCrc, jstring dlPath, jboolean useDB)
{
  int ret;
  const char *_hsName = (*env)->GetStringUTFChars(env, hsName, NULL);  
  const char *_dlPath = (*env)->GetStringUTFChars(env, dlPath, NULL);  

  assert(_hsName != NULL);     
  assert(_dlPath != NULL);     

  ret = svtsim_useHwSet(getCHandle(env, self), (int)wedge,
      _hsName, (long)hsCrc, _dlPath, ((useDB) ? 1 : 0));

  (*env)->ReleaseStringUTFChars(env, hsName, _hsName); 
  (*env)->ReleaseStringUTFChars(env, dlPath, _dlPath); 

  return ((jint) ret);
}

/* Obtain the C pointer to the object from the Integer which stores the value */
static svtsim_wedgeMaps_t *getCHandle(JNIEnv *env, jobject self) 
{
  svtsim_wedgeMaps_t *_self = (svtsim_wedgeMaps_t *) PointerFromJObject(env, self);
  assert(_self != NULL);
  return _self;
}

static svtsim_wedgeMaps_t *
wedgeMaps_new(void) {
  svtsim_wedgeMaps_t *p = 0;
  p = svtsim_malloc(sizeof(*p));
  return p;
}

static void
wedgeMaps_free(svtsim_wedgeMaps_t *p)
{
  free(p);
}

static int 
maps_pattSS(svtsim_wedgeMaps_t *p, int road, int layer)
{
  assert(layer >= 0 && layer < SVTSIM_NLAY);
  if (p == 0 || road >= p->nPatt) return -1;
  return p->patt[road][layer];
}

static int 
maps_dSSdz(svtsim_wedgeMaps_t *p, int layer) 
{
  assert(layer >= 0 && layer < SVTSIM_NLAY);
  return p->dSSdz[layer];
}

static int
maps_ssStrips(svtsim_wedgeMaps_t *p, int layer)
{
  assert(layer >= 0 && layer < SVTSIM_NLAY);
  return p->ssStrips[layer];
}

static int maps_wedge(svtsim_wedgeMaps_t *p) {return p->wedge;}


