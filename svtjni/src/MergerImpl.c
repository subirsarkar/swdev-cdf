#include <assert.h>
#include "svt_functions.h"
#include "merger_functions.h"
#include "jsvtvme_MergerImpl.h"

jint mergerState(jint self, jint boardReg, uint4 *state, int opt);
jint tModeMerger(jint self, uint4 *state, int opt);

/*
 * Implementation of <svtvme_merger_getState>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_MergerImpl_svtvme_1merger_1getState
  (JNIEnv *env, jobject obj, jint boardReg, jobject state, jint self)
{
   int error;
   uint4 stateValue;
   jclass cls;
   jfieldID fid;

   error = mergerState(self, boardReg, &stateValue, -1);

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
 * Implementation of <svtvme_merger_resetState>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_MergerImpl_svtvme_1merger_1resetState
  (JNIEnv *env, jobject obj, jint boardReg, jint self)
{
  return mergerState(self, boardReg, 0, 1);
}

/*
 * Implementation of <svtvme_merger_setState>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_MergerImpl_svtvme_1merger_1setState
  (JNIEnv *env, jobject obj, jint boardReg, jint state, jint self)
{
   uint4 stateValue = (uint4) state;
  return mergerState(self, boardReg, &stateValue, 0);
}

/*
 * Implementation of <svtvme_merger_init>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_MergerImpl_svtvme_1merger_1init
  (JNIEnv *env, jobject obj, jint initOption, jint self)
{
  uint4 _initOption;
  int  error;
  svtvme_t * _self;

  _initOption = (uint4) initOption;

  _self = (svtvme_t *) self;
  assert(_self != NULL);

  error = svtvme_merger_init (initOption, _self);
  return ((jint) error);
}

/*
 * Implementation of <svtvme_merger_outputFile>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_MergerImpl_svtvme_1merger_1outputFile
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

  error = svtvme_merger_outputFile(_service, (char *) _filename, _self);

  (*env)->ReleaseStringUTFChars(env, filename, _filename);

  return ((jint) error);
}

/*
 * Implementation of <svtvme_merger_tModeEnable>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_MergerImpl_svtvme_1merger_1tModeEnable
  (JNIEnv *env, jobject obj, jint self)
{
  return tModeMerger(self,0,0);
}

/*
 * Implementation of <svtvme_merger_tModeStatus>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_MergerImpl_svtvme_1merger_1tModeStatus
  (JNIEnv *env, jobject obj, jobject state, jint self)
{
   int error;
   uint4 stateValue;
   jclass cls;
   jfieldID fid;

   error = tModeMerger(self, &stateValue, 1);
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
 * Implementation of <svtvme_merger_writeToOutput>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_MergerImpl_svtvme_1merger_1writeToOutput
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

  error = svtvme_merger_writeToOutput (_service, _numWords, (uint4 *)_carr, _self);
  (*env)->ReleaseIntArrayElements(env, data, _carr, 0);
  
  return ((jint)error);
}

/*
 * Implementation of <svtvme_merger_downloadCompareSpy>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_MergerImpl_svtvme_1merger_1downloadCompareSpy
  (JNIEnv *env, jobject obj, jint boardReg, jstring filename, jobject problems, jint self) 
{
  int error;
  uint4 _boardReg;
  svtvme_t *_self;
  const char * _filename;
  uint4 _prb;
  jclass cls;
  jfieldID fid;

  _boardReg  = (uint4) boardReg;

  _filename = (*env)->GetStringUTFChars(env, filename, NULL);
  assert(_filename != NULL);   

  _self = (svtvme_t *) self;
  assert(_self != NULL);

  error = svtvme_merger_downloadCompareSpy (_boardReg, (char *) _filename, &_prb, _self);
  
  cls = (*env)->GetObjectClass(env, problems); /* Get a reference to org.omg.CORBA.IntHolder */  

  /* Look for the instance field 'value' in org.omg.CORBA.IntHolder*/
  fid = (*env)->GetFieldID(env, cls, "value", "I");
  if (fid == NULL) {
     return -1; /* Failed to find the field */
  }

  (*env)->SetIntField(env, problems, fid, (jint) _prb);

   return ((jint)error);
}

/*
 * Implementation of <svtvme_merger_downloadSpy>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_MergerImpl_svtvme_1merger_1downloadSpy
  (JNIEnv *env, jobject obj, jint boardReg, jstring filename, jint self)
{
  int error;
  uint4 _boardReg;
  svtvme_t * _self;
  const char * _filename;

  _boardReg  = (uint4) boardReg;

  _filename = (*env)->GetStringUTFChars(env, filename, NULL);
  assert(_filename != NULL);   

  _self = (svtvme_t *) self;
  assert(_self != NULL);

  error = svtvme_merger_downloadSpy (_boardReg, (char *) _filename, _self);

  (*env)->ReleaseStringUTFChars(env, filename, _filename);

  return ((jint) error);  
}

/*
 * Implementation of <svtvme_merger_spyBufferWrite>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_MergerImpl_svtvme_1merger_1spyBufferWrite
  (JNIEnv *env, jobject obj, jint boardReg, jint offset, jint numWord, jintArray data, jint self)
{
  int error;
  uint4 _boardReg, _offset, _numWord;
  jint *_carr;
  svtvme_t *_self;

  _boardReg  = (uint4) boardReg;
  _offset    = (uint4) offset;
  _numWord   = (uint4) numWord;

  _carr = (*env)->GetIntArrayElements(env, data, NULL);
  if (_carr == NULL) {
    return -1;
  }

  _self  = (svtvme_t *) self;
  assert(_self != NULL);

  error = svtvme_merger_spyBufferWrite (_boardReg, _offset, _numWord, (uint4 *) _carr, _self);
  (*env)->ReleaseIntArrayElements(env, data, _carr, 0);
  
  return ((jint)error);
}

/* Convenience functions */
jint mergerState(jint self, jint boardReg, uint4 *state, int opt) 
{
  int error;
  uint4 _boardReg  = (uint4) boardReg;

  svtvme_t * _self  = (svtvme_t *) self;
  assert(_self != NULL);
 
  switch (opt) {
    case -1:
      error = svtvme_merger_getState(_boardReg, state, _self);
      break;
    case 1:
      error = svtvme_merger_resetState(_boardReg, _self);
      break;
    default:
      error = svtvme_merger_setState(_boardReg, *state, _self);
      break;
  }    
  
  return ((jint) error);
}

jint tModeMerger(jint self, uint4 *state, int opt)
{
  int error;

  svtvme_t * _self = (svtvme_t *) self;
  assert(_self != NULL);
  
  switch (opt) {
    case 1:
      error = svtvme_merger_tModeStatus(state, _self);
      break;
    default:
      error = svtvme_merger_tModeEnable(_self);
      break;
  }

  return ((jint)error);
}
