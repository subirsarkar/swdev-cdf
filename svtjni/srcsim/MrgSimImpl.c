#include <stdlib.h>
#include <assert.h>
#include "jnitools.h"
#include "jsvtsim_MrgSimImpl.h"
#include "svtsim/svtsim.h"

/* Local function protoypes */
/* Convert the mrgsim handle passed from Java into real pointer */
static svtsim_mrg_t *getCHandle(JNIEnv *env, jobject self);

/*
 * Implementation of <svtsim_mrg_new>
 */
JNIEXPORT jobject JNICALL Java_jsvtsim_MrgSimImpl_svtsim_1mrg_1new
  (JNIEnv *env, jclass cls)
{
  svtsim_mrg_t *_self = svtsim_mrg_new();
  assert(_self != NULL);

  return JObjectFromPointer(env, "jsvtsim/mrgsim_t", _self);
}

/*
 * Implementation of <svtsim_mrg_del>
 */
JNIEXPORT void JNICALL Java_jsvtsim_MrgSimImpl_svtsim_1mrg_1del
  (JNIEnv *env, jclass cls, jobject self)
{
  svtsim_mrg_del(getCHandle(env, self)); 
}

/*
 * Implementation of <svtsim_mrg_procEvent1>
 */
JNIEXPORT jint JNICALL Java_jsvtsim_MrgSimImpl_svtsim_1mrg_1procEvent
  (JNIEnv *env, jclass cls, jobject self)
{
  return (jint)svtsim_mrg_procEvent(getCHandle(env, self)); 
}

/*
 * Implementation of <svtsim_mrg_plugInput>
 */
JNIEXPORT void JNICALL Java_jsvtsim_MrgSimImpl_svtsim_1mrg_1plugInput
  (JNIEnv *env, jclass cls, jobject self, jint num, jobject cable)
{
  svtsim_cable_t *_cable = (svtsim_cable_t *) PointerFromJObject(env, cable);
  if (_cable == NULL) {
    printf("ERROR. svtsim_mrg_plugInput: Could not get a valid cable!\n");
    return;
  }
  svtsim_mrg_plugInput(getCHandle(env, self), (int)num, _cable); 
}

/*
 * Implementation of <svtsim_mrg_outputCable>
 */
JNIEXPORT jobject JNICALL Java_jsvtsim_MrgSimImpl_svtsim_1mrg_1outputCable
  (JNIEnv *env, jclass cls, jobject self)
{
  svtsim_cable_t *_cable = svtsim_mrg_outputCable(getCHandle(env,self));
  if (_cable == NULL) return NULL;

  return JObjectFromPointer(env, "jsvtsim/cable_t", _cable);
}

/* Obtain the C pointer to the object from the Integer which stores the value */
static svtsim_mrg_t *getCHandle(JNIEnv *env, jobject self) 
{
  svtsim_mrg_t *_self = (svtsim_mrg_t *) PointerFromJObject(env, self);
  assert(_self != NULL);
  return _self;
}
