#include <assert.h>
#include "svt_functions.h"
#include "ams_functions.h"
#include "jsvtvme_AmsImpl.h"

jint amsState(jint self, jint boardReg, uint4 *state, int opt);
jint tModeAms(jint self, uint4 *state, int opt);

/*
 * Implementation of <svtvme_ams_getState>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_AmsImpl_svtvme_1ams_1getState
  (JNIEnv *env, jobject obj, jint boardReg, jobject state, jint self)
{
   int error;
   uint4 stateValue;
   jclass cls;
   jfieldID fid;

   error = amsState(self, boardReg, &stateValue, -1);

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
 * Implementation of <svtvme_ams_resetState>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_AmsImpl_svtvme_1ams_1resetState
  (JNIEnv *env, jobject obj, jint boardReg, jint self)
{
  return amsState(self, boardReg, 0, 1);
}

/*
 * Implementation of <svtvme_ams_setState>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_AmsImpl_svtvme_1ams_1setState
  (JNIEnv *env, jobject obj, jint boardReg, jint state, jint self)
{
   uint4 stateValue = (uint4) state;
  return amsState(self, boardReg, &stateValue, 0);
}

/*
 * Implementation of <svtvme_ams_init>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_AmsImpl_svtvme_1ams_1init
  (JNIEnv *env, jobject obj, jint initOption, jint self)
{
  uint4 _initOption;
  int  error;
  svtvme_t * _self;

  _initOption = (uint4) initOption;

  _self = (svtvme_t *) self;
  assert(_self != NULL);

  error = svtvme_ams_init (initOption, _self);
  return ((jint) error);
}

/*
 * Implementation of <svtvme_ams_outputFile>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_AmsImpl_svtvme_1ams_1outputFile
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

  error = svtvme_ams_outputFile(_service, (char *) _filename, _self);

  (*env)->ReleaseStringUTFChars(env, filename, _filename);

  return ((jint) error);
}

/*
 * Implementation of <svtvme_ams_tModeEnable>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_AmsImpl_svtvme_1ams_1tModeEnable
  (JNIEnv *env, jobject obj, jint self)
{
  return tModeAms(self,0,0);
}

/*
 * Implementation of <svtvme_ams_tModeStatus>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_AmsImpl_svtvme_1ams_1tModeStatus
  (JNIEnv *env, jobject obj, jobject state, jint self)
{
   int error;
   uint4 stateValue;
   jclass cls;
   jfieldID fid;

   error = tModeAms(self, &stateValue, 1);
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
 * Implementation of <svtvme_ams_writeToOutput>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_AmsImpl_svtvme_1ams_1writeToOutput
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

  error = svtvme_ams_writeToOutput (_service, _numWords, (uint4 *)_carr, _self);
  (*env)->ReleaseIntArrayElements(env, data, _carr, 0);
  
  return ((jint)error);
}

/* Convenience functions */
jint amsState(jint self, jint boardReg, uint4 *state, int opt) 
{
  int error;
  uint4 _boardReg  = (uint4) boardReg;

  svtvme_t * _self  = (svtvme_t *) self;
  assert(_self != NULL);
 
  switch (opt) {
    case -1:
      error = svtvme_ams_getState(_boardReg, state, _self);
      break;
    case 1:
      error = svtvme_ams_resetState(_boardReg, _self);
      break;
    default:
      error = svtvme_ams_setState(_boardReg, *state, _self);
      break;
  }    
  
  return ((jint) error);
}

jint tModeAms(jint self, uint4 *state, int opt)
{
  int error;

  svtvme_t * _self = (svtvme_t *) self;
  assert(_self != NULL);
  
  switch (opt) {
    case 1:
      error = svtvme_ams_tModeStatus(state, _self);
      break;
    default:
      error = svtvme_ams_tModeEnable(_self);
      break;
  }

  return ((jint)error);
}
