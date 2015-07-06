#include <assert.h>
#include "svt_functions.h"
#include "am_functions.h"
#include "jsvtvme_AmImpl.h"

jint amState(jint self, jint boardReg, uint4 *state, int opt);
jint tModeAm(jint self, uint4 *state, int opt);
jint am_handle_pattern(JNIEnv *env, jobject obj, jint operation, jint plugs, 
       jstring filename, jstring badname, jobject problems, jint self, int opt);
jint am_handle_file(JNIEnv *env, jobject obj, jstring filename, jint self, int opt);
jint am_handle_writeTo(JNIEnv *env, jobject obj, jint numWord, jintArray data, jint self, int opt);
jint am_handle_readWrite(JNIEnv *env, jobject obj, jint layer, jint initialAdd, 
     jint numWord, jintArray data, jintArray badPattern, jint numBadPattern, jint self, int opt);

/*
 * Implementation of <svtvme_am_getState>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_AmImpl_svtvme_1am_1getState
  (JNIEnv *env, jobject obj, jint boardReg, jobject state, jint self)
{
   int error;
   uint4 stateValue;
   jclass cls;
   jfieldID fid;

   error = amState(self, boardReg, &stateValue, -1);

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
 * Implementation of <svtvme_am_resetState>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_AmImpl_svtvme_1am_1resetState
  (JNIEnv *env, jobject obj, jint boardReg, jint self)
{
  return amState(self, boardReg, 0, 1);
}

/*
 * Implementation of <svtvme_am_setState>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_AmImpl_svtvme_1am_1setState
  (JNIEnv *env, jobject obj, jint boardReg, jint self)
{
  return amState(self, boardReg, 0, 0);
}

/*
 * Implementation of <svtvme_am_Init>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_AmImpl_svtvme_1am_1Init
  (JNIEnv *env, jobject obj, jint initOption, jint self)
{
  uint4 _initOption;
  int  error;
  svtvme_t * _self;

  _initOption = (uint4) initOption;

  _self = (svtvme_t *) self;
  assert(_self != NULL);

  error = svtvme_am_Init (initOption, _self);
  return ((jint) error);
}

/*
 * Implementation of <svtvme_am_outputFile>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_AmImpl_svtvme_1am_1outputFile
  (JNIEnv *env, jobject obj, jstring filename, jint self)
{
  return am_handle_file(env, obj, filename, self, 1);
}

/*
 * Implementation of <svtvme_am_TmodeEnable>
 */
/*
JNIEXPORT jint JNICALL 
 Java_jsvtvme_AmImpl_svtvme_1am_1TmodeEnable
  (JNIEnv *env, jobject obj, jint opCode, jint self)
{
  uint4 _opCode = (uint4) opCode;
  return tModeAm(self,&_opCode,0);
}
*/

/*
 * Implementation of <svtvme_am_TmodeStatus>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_AmImpl_svtvme_1am_1TmodeStatus
  (JNIEnv *env, jobject obj, jobject state, jint self)
{
   int error;
   uint4 stateValue;
   jclass cls;
   jfieldID fid;

   error = tModeAm(self, &stateValue, 1);
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
 * Implementation of <svtvme_am_writeToOutput>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_AmImpl_svtvme_1am_1writeToOutput
  (JNIEnv *env, jobject obj, jint numWord, jintArray data, jint self)
{
  return am_handle_writeTo(env, obj, numWord, data, self, 1);
}

/*
 * Implementation of <svtvme_am_comparePattern>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_AmImpl_svtvme_1am_1comparePattern
   (JNIEnv *env, jobject obj, jint operation, jint plugs, jstring filename, 
       jstring badname, jobject problems, jint self)
{
   return am_handle_pattern(env, obj, operation, plugs, filename, badname, problems, self, 0);
}

/*
 * Implementation of <svtvme_am_downloadComparePattern>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_AmImpl_svtvme_1am_1downloadComparePattern
  (JNIEnv *env, jobject obj, jint operation, jint plugs, jstring filename, 
    jstring badname, jobject problems, jint self)
{
   return am_handle_pattern(env, obj, operation, plugs, filename, badname, problems, self, 1);
}

/*
 * Implementation of <svtvme_am_downloadPattern>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_AmImpl_svtvme_1am_1downloadPattern
  (JNIEnv *env, jobject obj, jint operation, jint plugs, 
     jstring filename, jstring badname, jint self)
{
   jobject _dummy = NULL;
   return am_handle_pattern(env, obj, operation, plugs, filename, badname, _dummy, self, 2);
}

/*
 * Implementation of <svtvme_am_inputFile>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_AmImpl_svtvme_1am_1inputFile
  (JNIEnv *env, jobject obj, jstring filename, jint self)

{
  return am_handle_file(env, obj, filename, self, 0);
}

/*
 * Implementation of <svtvme_am_read>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_AmImpl_svtvme_1am_1read
  (JNIEnv *env, jobject obj, jint layer, jint initialAdd, jint numWord, 
      jintArray data, jint self)
{
  return am_handle_readWrite(env, obj, layer, initialAdd, numWord, data, 0, 0, self, 0);
}

/*
 * Implementation of <svtvme_am_read_good>
 */

JNIEXPORT jint JNICALL 
 Java_jsvtvme_AmImpl_svtvme_1am_1read_1good
  (JNIEnv *env, jobject obj, jint layer, jint initialAdd, jint numWord, 
    jintArray data, jintArray badPattern, jint numBadPattern, jint self)
{
  return am_handle_readWrite(env, obj, layer, initialAdd, numWord, data, 
          badPattern, numBadPattern, self, 1);
}

/*
 * Implementation of <svtvme_am_sendOpCode>
 */

JNIEXPORT jint JNICALL 
 Java_jsvtvme_AmImpl_svtvme_1am_1sendOpCode
  (JNIEnv *env, jobject obj, jint opCode, jint self)
{
   int error;
   uint4 _opCode;
   svtvme_t *_self;

   _opCode = (uint4) opCode;
   _self   = (svtvme_t *) self;
   assert(_self != NULL);

   error = svtvme_am_sendOpCode(_opCode, _self);
   return ((jint) error);
}

/*
 * Implementation of <svtvme_am_uploadPattern>
 */

JNIEXPORT jint JNICALL 
 Java_jsvtvme_AmImpl_svtvme_1am_1uploadPattern
  (JNIEnv *env, jobject obj, jint operation, jint plugs, jstring filename, 
       jstring badname, jint self)
{
   jobject _dummy = NULL;
   return am_handle_pattern(env, obj, operation, plugs, filename, badname, _dummy, self, 3);
}

/*
 * Implementation of <svtvme_am_write>
 */

JNIEXPORT jint JNICALL 
 Java_jsvtvme_AmImpl_svtvme_1am_1write
  (JNIEnv *env, jobject obj, jint layer, jint initialAdd, jint numWord, 
           jintArray data, jint self)
{
  return am_handle_readWrite(env, obj, layer, initialAdd, numWord, data, 0, 0, self, 2);
}

/*
 * Implementation of <svtvme_am_writeToInput>
 */

JNIEXPORT jint JNICALL 
 Java_jsvtvme_AmImpl_svtvme_1am_1writeToInput
  (JNIEnv *env, jobject obj, jint numWord, jintArray data, jint self)
{
  return am_handle_writeTo(env, obj, numWord, data, self, 0);
}

/*
 * Implementation of <svtvme_am_am_write_good>
 */

JNIEXPORT jint JNICALL 
 Java_jsvtvme_AmImpl_svtvme_1am_1write_1good
  (JNIEnv *env, jobject obj, jint layer, jint initialAdd, jint numWord, 
          jintArray data, jintArray badPattern, jint numBadPattern, jint self)
{
  return am_handle_readWrite(env, obj, layer, initialAdd, numWord, data, 
       badPattern, numBadPattern, self, 3);
}

/* Get am states */
jint amState(jint self, jint boardReg, uint4 *state, int opt) 
{
  int error;
  uint4 _boardReg  = (uint4) boardReg;

  svtvme_t * _self  = (svtvme_t *) self;
  assert(_self != NULL);
 
  switch (opt) {
    case -1:
      error = svtvme_am_getState(_boardReg, state, _self);
      break;
    case 1:
      error = svtvme_am_resetState(_boardReg, _self);
      break;
    default:
      error = svtvme_am_setState(_boardReg, _self);
      break;
  }    
  
  return ((jint) error);
}

/* Convenience functions */
jint tModeAm(jint self, uint4 *state, int opt)
{
  int error;

  svtvme_t * _self = (svtvme_t *) self;
  assert(_self != NULL);
  
  switch (opt) {
    case 1:
      error = svtvme_am_TmodeStatus(state, _self);
      break;
    default:
      /* error = svtvme_am_TmodeEnable(*state, _self); */
      break;
  }

  return ((jint)error);
}
jint am_handle_pattern(JNIEnv *env, jobject obj, jint operation, jint plugs, jstring filename, 
       jstring badname, jobject problems, jint self, int opt) 
{
   int error;
   const char * _filename;
   const char * _badname;
   uint4 _operation, _plugs;
   uint4 stateValue;
   svtvme_t *_self;
   jclass cls;
   jfieldID fid;

   _operation = (uint4) operation;
   _plugs     = (uint4) plugs;
   _filename  = (*env)->GetStringUTFChars(env, filename, NULL);
   _badname   = (*env)->GetStringUTFChars(env, badname,  NULL);

   _self  = (svtvme_t *) self;
   assert(_self != NULL);

   switch (opt) {
   case 0:
     error = svtvme_am_comparePattern(_operation, _plugs, (char *) _filename, 
                                    (char *)_badname, &stateValue, _self);
     break;  
   case 1:
     error = svtvme_am_downloadComparePattern(_operation, _plugs, (char *) _filename, 
                                    (char *)_badname, &stateValue, _self);
     break;  
   case 2:
     error = svtvme_am_downloadPattern(_operation, _plugs, (char *)_filename,(char *) _badname, _self);
     break;  
   case 3:
     error = svtvme_am_uploadPattern(_operation, _plugs, (char *)_filename, (char *)_badname,  _self);
     break;  
   }
   if (opt < 2) {
     cls = (*env)->GetObjectClass(env, problems); /* Get a reference to org.omg.CORBA.IntHolder */  

     /* Look for the instance field 'value' in org.omg.CORBA.IntHolder*/
     fid = (*env)->GetFieldID(env, cls, "value", "I");
     if (fid == NULL) {
       return -1; /* Failed to find the field */
     }

     (*env)->SetIntField(env, problems, fid, (jint) stateValue);
   }

   (*env)->ReleaseStringUTFChars(env, filename, _filename);
   (*env)->ReleaseStringUTFChars(env, badname, _badname);

   return ((jint) error);

}
jint am_handle_file(JNIEnv *env, jobject obj, jstring filename, jint self, int opt)
{
  int error;
  svtvme_t * _self;
  const char * _filename;

  _filename = (*env)->GetStringUTFChars(env, filename, NULL);
  assert(_filename != NULL);   

  _self = (svtvme_t *) self;
  assert(_self != NULL);

  switch (opt) {
  case 0:
    error = svtvme_am_inputFile((char *) _filename, _self);
    break;
  case 1:
    error = svtvme_am_outputFile((char *) _filename, _self);
    break;
  }
  (*env)->ReleaseStringUTFChars(env, filename, _filename);

  return ((jint) error);
}

jint am_handle_writeTo(JNIEnv *env, jobject obj, jint numWord, jintArray data, 
               jint self, int opt)
{
  int error;
  uint4 _numWord;
  jint *_carr;
  svtvme_t * _self;

  _numWord = (uint4) numWord;

  _carr = (*env)->GetIntArrayElements(env, data, NULL);
  if (_carr == NULL) {
    return -1;
  }
  _self  = (svtvme_t *) self;
  assert(_self != NULL);

  switch (opt) {
  case 0:
    error = svtvme_am_writeToInput (_numWord, (uint4 *)_carr, _self);
    break;
  case 1:
    error = svtvme_am_writeToOutput (_numWord, (uint4 *)_carr, _self);
    break;
  }
  (*env)->ReleaseIntArrayElements(env, data, _carr, 0);
  
  return ((jint)error);
}

jint am_handle_readWrite(JNIEnv *env, jobject obj, jint layer, jint initialAdd, 
     jint numWord, jintArray data, jintArray badPattern, jint numBadPattern, jint self, int opt)
{
  int error;
  uint4 _layer, _initialAdd, _numWord, _numBadPattern;
  jint *_cdata, *_cbad;
  svtvme_t * _self;

  _layer          = (uint4) layer;
  _initialAdd     = (uint4) initialAdd;
  _numWord        = (uint4) numWord;
  _numBadPattern  = (uint4) numBadPattern;

  _cdata = (*env)->GetIntArrayElements(env, data, NULL);
  if (_cdata == NULL) {
    return -1;
  }
  if (opt == 1 || opt == 3) {
    _cbad = (*env)->GetIntArrayElements(env, badPattern, NULL);
    if (_cbad == NULL) {
      return -1;
    }
  }
  _self  = (svtvme_t *) self;
  assert(_self != NULL);

  switch (opt) {
  case 0:
    error = svtvme_am_read (_layer, _initialAdd, _numWord, (uint4 *)_cdata, _self);
    break;
  case 1:
    error = svtvme_am_read_good (_layer, _initialAdd, _numWord, (uint4 *)_cdata, 
           (uint4 *) _cbad, _numBadPattern, _self);
    break;
  case 2:
    error = svtvme_am_write (_layer, _initialAdd, _numWord, (uint4 *)_cdata, _self);
    break;
  case 3:
    error = svtvme_am_write_good (_layer, _initialAdd, _numWord, (uint4 *)_cdata, 
           (uint4 *) _cbad, _numBadPattern, _self);
    break;
  }
  (*env)->ReleaseIntArrayElements(env, data, _cdata, 0);
  if (opt == 1 || opt == 3) {
    (*env)->ReleaseIntArrayElements(env, badPattern, _cbad, 0);
  }
  
  return ((jint)error);
}
