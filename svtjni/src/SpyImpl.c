#include <stdio.h>
#include <assert.h>
#include "svt_functions.h"
#include "spy_functions.h"
#include "jsvtvme_SpyImpl.h"

/* Local function prototypes */
jint getFreezeStatus(jint self, int opt, uint4 *state); 
jint changeFreezeState(jint self, int opt); 
jint spyState(jint self, jint boardReg, uint4 *state, int opt);

/*
 * Implementation of  <svtvme_spy_getFreezeBackplane>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SpyImpl_svtvme_1spy_1getFreezeBackplane 
    (JNIEnv *env, jobject obj, jobject state, jint self) 
{
  uint4 stateValue;
  int error;
  jclass cls;
  jfieldID fid;

  error = getFreezeStatus(self, 0, &stateValue);

  cls = (*env)->GetObjectClass(env, state); /* Get a reference to org.omg.CORBA.IntHolder */  

  /* Look for the instance field 'value' in org.omg.CORBA.IntHolder*/
  fid = (*env)->GetFieldID(env, cls, "value", "I");
  if (fid == NULL) {
    return -1; /* Failed to find the field */
  }

  (*env)->SetIntField(env, state, fid, (jint) stateValue);

  return ((jint)error);
}

/*
 * Implementation of  <svtvme_spy_getFreezeGlobal>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SpyImpl_svtvme_1spy_1getFreezeGlobal
  (JNIEnv *env, jobject obj, jobject state, jint self)
{
  uint4 stateValue;
  int error;
  jclass cls;
  jfieldID fid;

  error = getFreezeStatus(self, 1, &stateValue);

  cls = (*env)->GetObjectClass(env, state); /* Get a reference to org.omg.CORBA.IntHolder */  

  /* Look for the instance field 'value' in org.omg.CORBA.IntHolder*/
  fid = (*env)->GetFieldID(env, cls, "value", "I");
  if (fid == NULL) {
    return -1; /* Failed to find the field */
  }

  (*env)->SetIntField(env, state, fid, (jint) stateValue);

  return ((jint)error);
}

/*
 * Implementation of <svtvme_spy_getState>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SpyImpl_svtvme_1spy_1getState
  (JNIEnv *env, jobject obj, jint boardReg, jobject state, jint self)
{
  int error;
  uint4 stateValue;
  jclass cls;
  jfieldID fid;

  error = spyState(self, boardReg, &stateValue, -1);

  cls = (*env)->GetObjectClass(env, state); /* Get a reference to org.omg.CORBA.IntHolder */  

  /* Look for the instance field 'value' in org.omg.CORBA.IntHolder*/
  fid = (*env)->GetFieldID(env, cls, "value", "I");
  if (fid == NULL) {
    return -1; /* Failed to find the field */
  }

  (*env)->SetIntField(env, state, fid, stateValue);

  return ((jint) error);
}

/*
 * Implementation of <svtvme_spy_resetState>
 */
JNIEXPORT jint JNICALL
 Java_jsvtvme_SpyImpl_svtvme_1spy_1resetState
  (JNIEnv *env, jobject obj, jint boardReg, jint self)
{
  return spyState(self, boardReg, 0, 1);
}

/*
 * Implementation of <svtvme_spy_setState>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SpyImpl_svtvme_1spy_1setState
  (JNIEnv *env, jobject obj, jint boardReg, jint state, jint self)
{
  uint4 stateValue = (uint4) state;
  return spyState(self, boardReg, &stateValue, 0);
}

/*
 * Implementation of <svtvme_spy_setFreeze>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SpyImpl_svtvme_1spy_1setFreeze
  (JNIEnv *env, jobject obj, jint self)
{
  return changeFreezeState(self, -1);
}

/*
 * Implementation of <svtvme_spy_releaseFreeze> 
 */
JNIEXPORT jint JNICALL
 Java_jsvtvme_SpyImpl_svtvme_1spy_1releaseFreeze
  (JNIEnv *env, jobject obj, jint self)
{
  return changeFreezeState(self, 0);
}

/*
 * Implementation of <svtvme_spy_forceReleaseFreeze> 
 */
JNIEXPORT  jint JNICALL 
 Java_jsvtvme_SpyImpl_svtvme_1spy_1forceReleaseFreeze 
  (JNIEnv *env, jobject obj, jint self)
{
  return changeFreezeState(self, 1);
}

/* Convenience functions */
jint getFreezeStatus(jint self, int opt, uint4 *state) 
{
  int error;

  svtvme_t * _self = (svtvme_t *) self;
  assert(_self != NULL);

  switch (opt) {
    case 1:
      error = svtvme_spy_getFreezeGlobal(state, _self);
      break;
    default:
      error = svtvme_spy_getFreezeBackplane(state, _self);
      break;
  }    
  return ((jint)error);
}

jint changeFreezeState(jint self, int opt) 
{
  int error;

  svtvme_t * _self = (svtvme_t *) self;
  assert(_self != NULL);

  switch (opt) {
    case -1:
      error = svtvme_spy_setFreeze(_self);
      break;
    case 1:
      error = svtvme_spy_forceReleaseFreeze(_self);
      break;
    default:
      error = svtvme_spy_releaseFreeze(_self);
      break;
  }    
  
  return ((jint) error);
}

jint spyState(jint self, jint boardReg, uint4 *state, int opt) 
{
  int error;
  uint4 _boardReg  = (uint4) boardReg;

  svtvme_t * _self = (svtvme_t *) self;
  assert(_self != NULL);

  switch (opt) {
    case -1:
      error = svtvme_spy_getState(_boardReg, state, _self);
      break;
    case 1:
      error = svtvme_spy_resetState(_boardReg, _self);
      break;
    default:
      error = svtvme_spy_setState(_boardReg, *state, _self);
      break;
  }    
  
  return ((jint) error);
}

