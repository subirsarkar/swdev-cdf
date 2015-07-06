#include <stdlib.h>
#include <assert.h>
#include "jnitools.h"
#include "jsvtsim_AmsSimImpl.h"
#include "svtsim/svtsim.h"

/* Local function protoypes */
/* Convert the amssim handle passed from Java into real pointer */
static svtsim_ams_t *getCHandle(JNIEnv *env, jobject self);

/*
 * Implementation of <svtsim_ams_new>
 */
JNIEXPORT jobject JNICALL Java_jsvtsim_AmsSimImpl_svtsim_1ams_1new
  (JNIEnv *env, jclass cls, jint wedge)
{
  svtsim_ams_t *_self = svtsim_ams_new((int)wedge);
  assert(_self != NULL);

  return JObjectFromPointer(env, "jsvtsim/amssim_t", _self);
}

/*
 * Implementation of <svtsim_ams_del>
 */
JNIEXPORT void JNICALL Java_jsvtsim_AmsSimImpl_svtsim_1ams_1del
  (JNIEnv *env, jclass cls, jobject self)
{
  svtsim_ams_del(getCHandle(env, self)); 
}

/*
 * Implementation of <svtsim_ams_procEvent1>
 */
JNIEXPORT void JNICALL Java_jsvtsim_AmsSimImpl_svtsim_1ams_1procEvent1
  (JNIEnv *env, jclass cls, jobject self)
{
  svtsim_ams_procEvent1(getCHandle(env, self)); 
}

/*
 * Implementation of <svtsim_ams_plugInput>
 */
JNIEXPORT void JNICALL Java_jsvtsim_AmsSimImpl_svtsim_1ams_1plugInput
  (JNIEnv *env, jclass cls, jobject self, jobject cable)
{
  svtsim_cable_t *_cable = (svtsim_cable_t *) PointerFromJObject(env, cable);
  if (_cable == NULL) {
    printf("ERROR. svtsim_ams_plugInput: Could not get a valid cable!\n");
    return;
  }
  svtsim_ams_plugInput(getCHandle(env, self), _cable); 
}

/*
 * Implementation of <svtsim_ams_outputCable>
 */
JNIEXPORT jobject JNICALL Java_jsvtsim_AmsSimImpl_svtsim_1ams_1outputCable
  (JNIEnv *env, jclass cls, jobject self)
{
  svtsim_cable_t *_cable = svtsim_ams_outputCable(getCHandle(env,self));
  if (_cable == NULL) return NULL;

  return JObjectFromPointer(env, "jsvtsim/cable_t", _cable);
}

/*
 * Implementation of <svtsim_ams_useMaps>
 */
JNIEXPORT void JNICALL Java_jsvtsim_AmsSimImpl_svtsim_1ams_1useMaps
  (JNIEnv *env, jclass cls, jobject self, jobject maps)
{
  svtsim_wedgeMaps_t * _maps = (svtsim_wedgeMaps_t *) PointerFromJObject(env, maps);
  if (_maps == NULL) {
    printf("ERROR. svtsim_ams_useMaps: Could not get valid wedgeMaps!\n");
    return;
  }
  svtsim_ams_useMaps(getCHandle(env, self), _maps); 
}

/*
 * Implementation of <svtsim_ams_setUcode>
 */
JNIEXPORT void JNICALL Java_jsvtsim_AmsSimImpl_svtsim_1ams_1setUcode
  (JNIEnv *env, jclass cls, jobject self, jint ucode)
{
  svtsim_ams_setUcode(getCHandle(env, self), (int)ucode); 
}

/* Obtain the C pointer to the object from the Integer which stores the value */
static svtsim_ams_t *getCHandle(JNIEnv *env, jobject self) 
{
  svtsim_ams_t *_self = (svtsim_ams_t *) PointerFromJObject(env, self);
  assert(_self != NULL);
  return _self;
}
