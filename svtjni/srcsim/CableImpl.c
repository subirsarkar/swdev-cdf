#include <stdlib.h>
#include <assert.h>
#include "jnitools.h"
#include "jsvtsim_CableImpl.h"
#include "svtsim/svtsim.h"

/* Local function protoypes */
/* Convert the cable_t handle passed from Java into real pointer */
static svtsim_cable_t *getCHandle(JNIEnv *env, jobject self);
static int cable_ndata(svtsim_cable_t *p) { return p->ndata; }
static uint4 cable_datum(svtsim_cable_t *p, int i) { return p->data[i]; }

/*
 * Implementation of <svtsim_cable_new>
 */
JNIEXPORT jobject JNICALL Java_jsvtsim_CableImpl_svtsim_1cable_1new
  (JNIEnv *env, jclass cls) 
{
  svtsim_cable_t *_self = svtsim_cable_new();
  assert(_self != NULL);

  return JObjectFromPointer(env, "jsvtsim/cable_t", _self);
}
/*
 * Implementation of <svtsim_cable_del>
 */
JNIEXPORT void JNICALL Java_jsvtsim_CableImpl_svtsim_1cable_1del
  (JNIEnv *env, jclass cls, jobject self)
{
  svtsim_cable_del(getCHandle(env, self));
}
/*
 * Implementation of <cable_datum>
 */
JNIEXPORT jint JNICALL Java_jsvtsim_CableImpl_cable_1datum
  (JNIEnv *env, jclass cls, jobject self, jint i)
{
  return ((jint)cable_datum(getCHandle(env, self), (int)i));
}

/*
 * Implementation of <cable_ndata>
 */
JNIEXPORT jint JNICALL Java_jsvtsim_CableImpl_cable_1ndata
  (JNIEnv *env, jclass cls, jobject self)
{
  return ((jint) cable_ndata(getCHandle(env, self))); 
}

/*
 * Implementation of <svtsim_cable_addword>
 */
JNIEXPORT void JNICALL Java_jsvtsim_CableImpl_svtsim_1cable_1addword
  (JNIEnv *env, jclass cls, jobject self, jint word)
{
  svtsim_cable_addword(getCHandle(env, self), (int)word);
}

/*
 * Implementation of <svtsim_cable_addwords>
 */
JNIEXPORT void JNICALL Java_jsvtsim_CableImpl_svtsim_1cable_1addwords
  (JNIEnv *env, jclass cls, jobject self, jintArray words, jint nw)
{
  jint *_carr;
  jsize len;

  len = (*env)->GetArrayLength(env, words);
  if (len <= 0) {
    printf("ERROR. svtsim_cable_addwords: Null array!\n");
    return;
  }

  _carr = (*env)->GetIntArrayElements(env, words, NULL);

  if (_carr == NULL) { /* User must preallocate array in Java program */
    printf("ERROR. svtsim_cable_addwords: Failed to preallocate array!\n");
    return;
  }
  svtsim_cable_addwords(getCHandle(env, self), (uint4 *)_carr, (int)nw);

  (*env)->ReleaseIntArrayElements(env, words, _carr, 0);
}

/*
 * Implementation of  <svtsim_cable_copywords>
 */
JNIEXPORT void JNICALL Java_jsvtsim_CableImpl_svtsim_1cable_1copywords
  (JNIEnv *env, jclass cls, jobject self, jintArray words, jint nw)
{
  jint *_carr;
  jsize len;

  len = (*env)->GetArrayLength(env, words);
  if (len <= 0) {
    printf("ERROR. svtsim_cable_copywords: Null array!\n");
    return;
  }

  _carr = (*env)->GetIntArrayElements(env, words, NULL);

  if (_carr == NULL) { /* User must preallocate array in Java program */
    printf("ERROR. svtsim_cable_addwords: Failed to preallocate array!\n");
    return;
  }
  svtsim_cable_copywords(getCHandle(env, self), (uint4 *)_carr, (int)nw);

  (*env)->ReleaseIntArrayElements(env, words, _carr, 0);
}

/* Obtain the C pointer to the object from the Integer which stores the value */
static svtsim_cable_t *getCHandle(JNIEnv *env, jobject self) 
{
  svtsim_cable_t *_self = (svtsim_cable_t *) PointerFromJObject(env, self);
  assert(_self != NULL);
  return _self;
}
