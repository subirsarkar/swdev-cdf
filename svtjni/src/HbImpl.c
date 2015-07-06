#include <assert.h>
#include "svt_functions.h"
#include "hb_functions.h"
#include "jsvtvme_HbImpl.h"

jint hbState(jint self, jint boardReg, uint4 *state, int opt);
jint tModeHb(jint self, uint4 *state, int opt);

/*
 * Implementation of <svtvme_hb_getState>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_HbImpl_svtvme_1hb_1getState
  (JNIEnv *env, jobject obj, jint boardReg, jobject state, jint self)
{
   int error;
   uint4 stateValue;
   jclass cls;
   jfieldID fid;

   error = hbState(self, boardReg, &stateValue, -1);

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
 * Implementation of <svtvme_hb_resetState>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_HbImpl_svtvme_1hb_1resetState
  (JNIEnv *env, jobject obj, jint boardReg, jint self)
{
  return hbState(self, boardReg, 0, 1);
}

/*
 * Implementation of <svtvme_hb_setState>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_HbImpl_svtvme_1hb_1setState
  (JNIEnv *env, jobject obj, jint boardReg, jint state, jint self)
{
   uint4 stateValue = (uint4) state;
  return hbState(self, boardReg, &stateValue, 0);
}

/*
 * Implementation of <svtvme_hb_init>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_HbImpl_svtvme_1hb_1init
  (JNIEnv *env, jobject obj, jint self)
{
  int  error;

  svtvme_t *_self = (svtvme_t *) self;
  assert(_self != NULL);

  error = svtvme_hb_init ( _self);
  return ((jint) error);
}

/*
 * Implementation of <svtvme_hb_outputFile>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_HbImpl_svtvme_1hb_1outputFile
  (JNIEnv *env, jobject obj, jint service, jstring filename, jint self)
{
  int error;
  uint4 _service;
  svtvme_t * _self;
  const char * _filename;

  _service  = (uint4) service;

  _filename = (*env)->GetStringUTFChars(env, filename, NULL);
  assert(_filename != NULL);   

  _self = (svtvme_t *) self;
  assert(_self != NULL);

  error = svtvme_hb_outputFile(_service, (char *) _filename, _self);

  (*env)->ReleaseStringUTFChars(env, filename, _filename);

  return ((jint) error);
}

/*
 * Implementation of <svtvme_hb_tModeEnable>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_HbImpl_svtvme_1hb_1tModeEnable
  (JNIEnv *env, jobject obj, jint self)
{
  return tModeHb(self,0,0);
}

/*
 * Implementation of <svtvme_hb_tModeStatus>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_HbImpl_svtvme_1hb_1tModeStatus
  (JNIEnv *env, jobject obj, jobject state, jint self)
{
   int error;
   uint4 stateValue;
   jclass cls;
   jfieldID fid;

   error = tModeHb(self, &stateValue, 1);
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
 * Implementation of <svtvme_hb_writeToOutput>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_HbImpl_svtvme_1hb_1writeToOutput
  (JNIEnv *env, jobject obj, jint service, jint numWords, jintArray data, jint self)
{
  int error;
  uint4 _service;
  uint4 _numWords;
  jint *_carr;
  svtvme_t * _self;

  _service  = (uint4) service;
  _numWords = (uint4) numWords;

  _carr = (*env)->GetIntArrayElements(env, data, NULL);
  if (_carr == NULL) {
    return -1;
  }
  _self  = (svtvme_t *) self;
  assert(_self != NULL);

  error = svtvme_hb_writeToOutput (_service, _numWords, (uint4 *)_carr, _self);
  (*env)->ReleaseIntArrayElements(env, data, _carr, 0);
  
  return ((jint)error);
}

/* Convenience functions */
jint hbState(jint self, jint boardReg, uint4 *state, int opt) 
{
  int error;
  uint4 _boardReg  = (uint4) boardReg;

  svtvme_t * _self  = (svtvme_t *) self;
  assert(_self != NULL);
 
  switch (opt) {
    case -1:
      error = svtvme_hb_getState(_boardReg, state, _self);
      break;
    case 1:
      error = svtvme_hb_resetState(_boardReg, _self);
      break;
    default:
      error = svtvme_hb_setState(_boardReg, *state, _self);
      break;
  }    
  
  return ((jint) error);
}

jint tModeHb(jint self, uint4 *state, int opt)
{
  int error;

  svtvme_t * _self = (svtvme_t *) self;
  assert(_self != NULL);
  
  switch (opt) {
    case 1:
      error = svtvme_hb_tModeStatus(state, _self);
      break;
    default:
      error = svtvme_hb_tModeEnable(_self);
      break;
  }

  return ((jint)error);
}
