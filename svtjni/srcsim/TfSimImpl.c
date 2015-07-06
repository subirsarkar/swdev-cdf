#include <stdlib.h>
#include <assert.h>
#include "jnitools.h"
#include "jsvtsim_TfSimImpl.h"
#include "svtsim/svtsim.h"

/* Local function protoypes */
/* Convert the tfsim handle passed from Java into real pointer */
static svtsim_tf_t *getCHandle(JNIEnv *env, jobject self);

/*
 * Implementation of <svtsim_tf_new>
 */
JNIEXPORT jobject JNICALL Java_jsvtsim_TfSimImpl_svtsim_1tf_1new
  (JNIEnv *env, jclass cls, jint wedge)
{
  svtsim_tf_t *_self = svtsim_tf_new((int)wedge);
  assert(_self != NULL);

  return JObjectFromPointer(env, "jsvtsim/tfsim_t", _self);
}

/*
 * Implementation of <svtsim_tf_del>
 */
JNIEXPORT void JNICALL Java_jsvtsim_TfSimImpl_svtsim_1tf_1del
  (JNIEnv *env, jclass cls, jobject self)
{
  svtsim_tf_del(getCHandle(env, self)); 
}

/*
 * Implementation of <svtsim_tf_procEvent>
 */
JNIEXPORT void JNICALL Java_jsvtsim_TfSimImpl_svtsim_1tf_1procEvent
  (JNIEnv *env, jclass cls, jobject self)
{
  svtsim_tf_procEvent(getCHandle(env, self)); 
}

/*
 * Implementation of <svtsim_tf_plugInput>
 */
JNIEXPORT void JNICALL Java_jsvtsim_TfSimImpl_svtsim_1tf_1plugInput
  (JNIEnv *env, jclass cls, jobject self, jobject cable)
{
  svtsim_cable_t *_cable = (svtsim_cable_t *) PointerFromJObject(env, cable);
  if (_cable == NULL) {
    printf("ERROR. svtsim_tf_plugInput: Could not get a valid cable!\n");
    return;
  }
  svtsim_tf_plugInput(getCHandle(env, self), _cable); 
}

/*
 * Implementation of <svtsim_tf_outputCable>
 */
JNIEXPORT jobject JNICALL Java_jsvtsim_TfSimImpl_svtsim_1tf_1outputCable
  (JNIEnv *env, jclass cls, jobject self)
{
  svtsim_cable_t *_cable = svtsim_tf_outputCable(getCHandle(env,self));
  if (_cable == NULL) return NULL;

  return JObjectFromPointer(env, "jsvtsim/cable_t", _cable);
}

/*
 * Implementation of <svtsim_tf_useMaps>
 */
JNIEXPORT void JNICALL Java_jsvtsim_TfSimImpl_svtsim_1tf_1useMaps
  (JNIEnv *env, jclass cls, jobject self, jobject maps)
{
  svtsim_wedgeMaps_t * _maps = (svtsim_wedgeMaps_t *) PointerFromJObject(env, maps);
  if (_maps == NULL) {
    printf("ERROR. svtsim_tf_useMaps: Could not get valid wedgeMaps!\n");
    return;
  }
  svtsim_tf_useMaps(getCHandle(env, self), _maps); 
}

/* Obtain the C pointer to the object from the Integer which stores the value */
static svtsim_tf_t *getCHandle(JNIEnv *env, jobject self) 
{
  svtsim_tf_t *_self = (svtsim_tf_t *) PointerFromJObject(env, self);
  assert(_self != NULL);
  return _self;
}
