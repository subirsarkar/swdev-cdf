#include <stdlib.h>
#include <assert.h>
#include "jnitools.h"
#include "jsvtsim_HbSimImpl.h"
#include "svtsim/svtsim.h"

/* Local function protoypes */
/* Convert the hbsim handle passed from Java into real pointer */
static svtsim_hb_t *getCHandle(JNIEnv *env, jobject self);

/*
 * Implementation of <svtsim_hb_new>
 */
JNIEXPORT jobject JNICALL Java_jsvtsim_HbSimImpl_svtsim_1hb_1new
  (JNIEnv *env, jclass cls, jint layer)
{
  svtsim_hb_t *_self = svtsim_hb_new((int)layer);
  assert(_self != NULL);

  return JObjectFromPointer(env, "jsvtsim/hbsim_t", _self);
}

/*
 * Implementation of <svtsim_hb_del>
 */
JNIEXPORT void JNICALL Java_jsvtsim_HbSimImpl_svtsim_1hb_1del
  (JNIEnv *env, jclass cls, jobject self)
{
  svtsim_hb_del(getCHandle(env, self)); 
}

/*
 * Implementation of <svtsim_hb_procEvent1>
 */
JNIEXPORT void JNICALL Java_jsvtsim_HbSimImpl_svtsim_1hb_1procEvent1
  (JNIEnv *env, jclass cls, jobject self)
{
  svtsim_hb_procEvent1(getCHandle(env, self)); 
}

/*
 * Implementation of <svtsim_hb_plugHitInput>
 */
JNIEXPORT void JNICALL Java_jsvtsim_HbSimImpl_svtsim_1hb_1plugHitInput
  (JNIEnv *env, jclass cls, jobject self, jobject cable)
{
  svtsim_cable_t *_cable = (svtsim_cable_t *) PointerFromJObject(env, cable);
  if (_cable == NULL) {
    printf("ERROR. svtsim_hb_plugHitInput: Could not get a valid cable!\n");
    return;
  }
  svtsim_hb_plugHitInput(getCHandle(env, self), _cable); 
}

/*
 * Implementation of <svtsim_hb_plugRoadInput>
 */
JNIEXPORT void JNICALL Java_jsvtsim_HbSimImpl_svtsim_1hb_1plugRoadInput
  (JNIEnv *env, jclass cls, jobject self, jobject cable)
{
  svtsim_cable_t *_cable = (svtsim_cable_t *) PointerFromJObject(env, cable);
  if (_cable == NULL) {
    printf("ERROR. svtsim_hb_plugRoadInput: Could not get a valid cable!\n");
    return;
  }
  svtsim_hb_plugRoadInput(getCHandle(env, self), _cable); 
}

/*
 * Implementation of <svtsim_hb_outputCable>
 */
JNIEXPORT jobject JNICALL Java_jsvtsim_HbSimImpl_svtsim_1hb_1outputCable
  (JNIEnv *env, jclass cls, jobject self)
{
  svtsim_cable_t *_cable = svtsim_hb_outputCable(getCHandle(env, self));
  if (_cable == NULL) return NULL;

  return JObjectFromPointer(env, "jsvtsim/cable_t", _cable);
}

/*
 * Implementation of <svtsim_hb_useMaps>
 */
JNIEXPORT void JNICALL Java_jsvtsim_HbSimImpl_svtsim_1hb_1useMaps
  (JNIEnv *env, jclass cls, jobject self, jobject maps)
{
  svtsim_wedgeMaps_t * _maps = (svtsim_wedgeMaps_t *) PointerFromJObject(env, maps);
  if (_maps == NULL) {
    printf("ERROR. svtsim_hb_useMaps: Could not get valid wedgeMaps!\n");
    return;
  }
  svtsim_hb_useMaps(getCHandle(env, self), _maps); 
}

/* Obtain the C pointer to the object from the Integer which stores the value */
static svtsim_hb_t *getCHandle(JNIEnv *env, jobject self) 
{
  svtsim_hb_t *_self = (svtsim_hb_t *) PointerFromJObject(env, self);
  assert(_self != NULL);
  return _self;
}
