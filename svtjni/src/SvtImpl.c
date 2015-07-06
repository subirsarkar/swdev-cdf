#include <assert.h>
#include <svt_functions.h>
#include <jsvtvme_SvtImpl.h>

jint upload(JNIEnv *env, jobject obj, int self, jint boardReg, jstring filename, int opt);
jint download(JNIEnv *env, jobject obj,int self, jint boardReg, jstring filename, int opt);
jint spyInfo(JNIEnv *env, jobject obj,jint self, jint boardReg, jobject state, int opt);
jint compare(JNIEnv *env, jobject obj, jint self, jint boardReg, jstring filename, jobject state, int opt);

/*
 * Implementation of <svtvme_svt_open>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtImpl_svtvme_1svt_1open
  (JNIEnv *env, jobject obj, jstring crate, jint slot)
{
  int _slot;
  svtvme_t *bHandle;

  const char * _crate = (*env)->GetStringUTFChars(env, crate, NULL);
  assert(_crate != NULL);   

  _slot = (int) slot;
  bHandle = svtvme_svt_open(_crate, _slot);
  assert(bHandle != NULL);
  
  (*env)->ReleaseStringUTFChars(env, crate, _crate);

  return ((jint) bHandle);
}

/*
 * Implementation of <svtvme_svt_close>
 */
JNIEXPORT void JNICALL 
 Java_jsvtvme_SvtImpl_svtvme_1svt_1close
  (JNIEnv *env, jobject obj, jint self)
{
  svtvme_t * _bHandle = (svtvme_t *) self;
  assert(_bHandle != NULL);
  svtvme_svt_close(_bHandle);
}

/*
 * Implementation of <svtvme_svt_setDebug>
 */
JNIEXPORT void JNICALL 
 Java_jsvtvme_SvtImpl_svtvme_1svt_1setDebug
  (JNIEnv *env, jobject obj, jstring filename, jint self)
{
  FILE *file;
  svtvme_t * _bHandle;
  const char * _filename;

  _filename = (*env)->GetStringUTFChars(env, filename, NULL);
  assert(_filename != NULL);   

  _bHandle = (svtvme_t *) self;
  assert(_bHandle != NULL);

  file = fopen((char *)_filename, "w");

  svtvme_svt_setDebug(file, _bHandle);

  (*env)->ReleaseStringUTFChars(env, filename, _filename);
}
/*
 * Implementation of <svtvme_svt_pointer>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtImpl_svtvme_1spy_1pointer 
  (JNIEnv *env, jobject obj, jint boardReg, jobject pointer, jint self)
{
   return spyInfo(env, obj, self, boardReg, pointer, 0);
}

/*
 * Implementation of <svtvme_svt_overflow>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtImpl_svtvme_1spy_1overflow 
  (JNIEnv *env, jobject obj, jint boardReg, jobject overflow, jint self)
{
   return spyInfo(env, obj, self, boardReg, overflow, 1);
}

/*
 * Implementation of <svtvme_svt_freeze>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtImpl_svtvme_1spy_1freeze
  (JNIEnv *env, jobject obj, jint boardReg, jobject freeze, jint self)
{
   return spyInfo(env, obj, self, boardReg, freeze, 2);
}

/*
 * Implementation of <svtvme_svt_status>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtImpl_svtvme_1spy_1status 
  (JNIEnv *env, jobject obj, jint boardReg, 
     jobject pointer, jobject overflow, jobject freeze, jint self)
{
   jint error;
   error =  spyInfo(env, obj, self, boardReg, pointer, 0);
   error += spyInfo(env, obj, self, boardReg, overflow, 1);
   error += spyInfo(env, obj, self, boardReg, freeze, 2);
   return error;
}

/*
 * Implementation of <svtvme_svt_compareFifo>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtImpl_svtvme_1svt_1compareFifo 
  (JNIEnv *env, jobject obj, jint boardReg, jstring filename, jobject problems, jint self)
{
  return compare(env, obj, self, boardReg, filename, problems, 0);
}

JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtImpl_svtvme_1svt_1compareRam 
  (JNIEnv *env, jobject obj, jint boardReg, jstring filename, jobject problems, jint self)
{
  return compare(env, obj, self, boardReg, filename, problems, 1);
}

/*
 * Implementation of <svtvme_svt_compareSpy>
 */
JNIEXPORT jint JNICALL 
Java_jsvtvme_SvtImpl_svtvme_1svt_1compareSpy 
  (JNIEnv *env, jobject obj, jint boardReg, jstring filename, jobject problems, jint self)
{
  return compare(env, obj, self, boardReg, filename, problems, 2);
}

/*
 * Implementation of <svtvme_svt_downloadCompareRam>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtImpl_svtvme_1svt_1downloadCompareRam  
  (JNIEnv *env, jobject obj, jint boardReg, jstring filename, jobject problems, jint self)
{
  return compare(env, obj, self, boardReg, filename, problems, 3);
}

/*
 * Implementation of <svtvme_svt_downloadRam>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtImpl_svtvme_1svt_1downloadRam  
  (JNIEnv *env, jobject obj, jint boardReg, jstring filename, jint self)
{
  return download(env, obj, self, boardReg, filename,  0);
}

/*
 * Implementation of <svtvme_svt_getIdPromBlock>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtImpl_svtvme_1svt_1getIdPromBlock 
  (JNIEnv *env, jobject obj, jint offset, jintArray data, jint self)
{
  int error;
  uint4 _offset;
  jint *_carr;
  svtvme_t * _self;
  jsize len;

  _offset   = (uint4) offset;

  len   = (*env)->GetArrayLength(env, data);
  if (len <= 0) return -1;

  _carr = (*env)->GetIntArrayElements(env, data, NULL);
  if (_carr == NULL) { /* User must preallocate array in Java program */
    return -1;
  }
  _self  = (svtvme_t *) self;
  assert(_self != NULL);

  error = svtvme_svt_getIdPromBlock (_offset,(uint4 *)_carr, _self);

  (*env)->SetIntArrayRegion(env, data, 0, len, _carr);
  (*env)->ReleaseIntArrayElements(env, data, _carr, 0);
  
  return ((jint)error);
}

/*
 * Implementation of <svtvme_svt_memoryOperation>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtImpl_svtvme_1svt_1memoryOperation 
  (JNIEnv *env, jobject obj, jint boardReg, jint operation, jint offset, jint numWord, 
    jintArray data, jint self)
{
  int error;
  uint4 _boardReg;
  uint4 _operation;
  uint4 _offset;
  uint4 _numWord;
  jint *_carr;
  svtvme_t * _self;
  jsize len;

  _boardReg   = (uint4) boardReg;
  _operation  = (uint4) operation;
  _offset     = (uint4) offset;
  _numWord    = (uint4) numWord;

  len   = (*env)->GetArrayLength(env, data);
  if (len <= 0) return -1;

  _carr = (*env)->GetIntArrayElements(env, data, NULL);
  if (_carr == NULL) { /* User must preallocate array in Java program */
    return -1;
  }
  _self  = (svtvme_t *) self;
  assert(_self != NULL);

  error = svtvme_svt_memoryOperation (_boardReg, _operation, _offset, 
                 _numWord, (uint4 *)_carr, _self);

  if (operation == SVT_READ) {
    (*env)->SetIntArrayRegion(env, data, 0, len, _carr);
  }

  (*env)->ReleaseIntArrayElements(env, data, _carr, 0);
  
  return ((jint)error);
}

/*
 * Implementation of <svtvme_svt_randomAccess>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtImpl_svtvme_1svt_1randomAccess 
  (JNIEnv *env, jobject obj, jint operation, jint address, jint numWord, 
    jintArray data, jint self)
{
  int error;
  uint4 _operation;
  uint4 _address;
  uint4 _numWord;
  jint *_carr;
  svtvme_t * _self;
  jsize len;

  _operation  = (uint4) operation;
  _address    = (uint4) address;
  _numWord    = (uint4) numWord;

  len   = (*env)->GetArrayLength(env, data);
  _carr = (*env)->GetIntArrayElements(env, data, NULL);
  if (_carr == NULL) { /* User must preallocate array in Java program */
    return -1;
  }
  _self  = (svtvme_t *) self;
  assert(_self != NULL);

  error = svtvme_svt_randomAccess (_operation, _address, _numWord, (uint4 *)_carr, _self);

  if (operation == SVT_READ) {
    (*env)->SetIntArrayRegion(env, data, 0, len, _carr);
  }

  (*env)->ReleaseIntArrayElements(env, data, _carr, 0);
  
  return ((jint)error);
}

/*
 * Implementation of <svtvme_svt_readFifo>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtImpl_svtvme_1svt_1readFifo
  (JNIEnv *env, jobject obj, jint boardReg, jint numWord, jobject moreData, 
    jintArray data, jint self)
{
  int error;
  uint4 _boardReg;
  uint4 _numWord;
  jint *_carr;
  svtvme_t * _self;
  uint4 stateValue;
  jclass cls;
  jfieldID fid;
  int len;

  _boardReg  = (uint4) boardReg;
  _numWord   = (uint4) numWord;

  len   = (*env)->GetArrayLength(env, data);
  _carr = (*env)->GetIntArrayElements(env, data, NULL);
  if (_carr == NULL) { /* User must preallocate array in Java program */
    return -1;
  }
  _self  = (svtvme_t *) self;
  assert(_self != NULL);

  error = svtvme_svt_readFifo (_boardReg, _numWord, &stateValue, (uint4 *)_carr, _self);

  cls = (*env)->GetObjectClass(env, moreData); /* Get a reference to org.omg.CORBA.IntHolder */  

  /* Look for the instance field 'value' in org.omg.CORBA.IntHolder*/
  fid = (*env)->GetFieldID(env, cls, "value", "I");
  if (fid == NULL) {
    return -1; /* Failed to find the field */
  }

  (*env)->SetIntField(env, moreData, fid, (jint) stateValue);
  (*env)->SetIntArrayRegion(env, data, 0, len, _carr);

  (*env)->ReleaseIntArrayElements(env, data, _carr, 0);
  
  return ((jint)error);
}

/*
 * Implementation of <svtvme_svt_singleRam>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtImpl_svtvme_1svt_1singleRam
  (JNIEnv *env, jobject obj, jint boardReg, jint operation, jint offset, 
    jintArray data, jint self)
{
  int error;
  uint4 _operation;
  uint4 _boardReg;
  uint4 _offset;
  jint *_carr;
  svtvme_t * _self;
  jsize len;

  _operation  = (uint4) operation;
  _boardReg    = (uint4) boardReg;
  _offset    = (uint4) offset;

  len   = (*env)->GetArrayLength(env, data);
  if (len <= 0) return -1;

  _carr = (*env)->GetIntArrayElements(env, data, NULL);
  if (_carr == NULL) { /* User must preallocate array in Java program */
    return -1;
  }
  _self  = (svtvme_t *) self;
  assert(_self != NULL);

  error = svtvme_svt_singleRam (_boardReg, _operation, _offset, (uint4 *)_carr, _self);

  if (operation == SVT_READ) {
    (*env)->SetIntArrayRegion(env, data, 0, len, _carr);
  }

  (*env)->ReleaseIntArrayElements(env, data, _carr, 0);
  
  return ((jint)error);
}

/*
 * Implementation of <svtvme_svt_spyBufferRead>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtImpl_svtvme_1svt_1spyBufferRead
  (JNIEnv *env, jobject obj, jint boardReg, jint offset, jint numWord, 
      jintArray data, jint self)
{
  int error;
  uint4 _boardReg;
  uint4 _offset;
  uint4 _numWord;
  jint *_carr;
  svtvme_t * _self;
  jsize len;

  _boardReg = (uint4) boardReg;
  _offset   = (uint4) offset;
  _numWord  = (uint4) numWord;

  len   = (*env)->GetArrayLength(env, data);
  if (len <= 0) return -1;

  _carr = (*env)->GetIntArrayElements(env, data, NULL);
  if (_carr == NULL) { /* User must preallocate array in Java program */
    return -1;
  }
  _self  = (svtvme_t *) self;
  assert(_self != NULL);

  error = svtvme_svt_spyBufferRead (_boardReg, _offset, _numWord, (uint4 *)_carr, _self);

  (*env)->SetIntArrayRegion(env, data, 0, len, _carr);
  (*env)->ReleaseIntArrayElements(env, data, _carr, 0);
  
  return ((jint)error);
}

/*
 * Implementation of <svtvme_svt_uploadFifo>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtImpl_svtvme_1svt_1uploadFifo
  (JNIEnv *env, jobject obj, jint boardReg, jstring filename, jint self)
{
  return upload(env, obj, self, boardReg, filename, 0);
}

/*
 * Implementation of <svtvme_svt_uploadRam>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtImpl_svtvme_1svt_1uploadRam
  (JNIEnv *env, jobject obj, jint boardReg, jstring filename, jint self)
{
  return upload(env, obj, self, boardReg, filename, 1);
}

/*
 * Implementation of <svtvme_svt_uploadSpy>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtImpl_svtvme_1svt_1uploadSpy
  (JNIEnv *env, jobject obj, jint boardReg, jstring filename, jint self)
{
  return upload(env, obj, self, boardReg, filename, 2);
}

/* Convenience functions */
jint
upload(JNIEnv *env, jobject obj, int self, jint boardReg, jstring filename, int opt) 
{
   int error;
   uint4 _boardReg;
   const char * _filename;
   svtvme_t *_self;

   _boardReg  = (uint4) boardReg;

   _filename = (*env)->GetStringUTFChars(env, filename, NULL);
   assert(_filename != NULL);  

   _self = (svtvme_t *) self;
   assert(_self != NULL);
   
   switch (opt) {
   case 0: 
       error = svtvme_svt_uploadFifo(_boardReg, (char *)_filename, _self);
       break;
   case 1: 
       error = svtvme_svt_uploadRam(_boardReg, (char *)_filename, _self);
       break;
   case 2: 
       error = svtvme_svt_uploadSpy(_boardReg, (char *)_filename, _self);
       break;
   }

   (*env)->ReleaseStringUTFChars(env, filename, _filename);
   return ((jint) error); 
}

jint
download(JNIEnv *env, jobject obj, int self, jint boardReg, jstring filename, int opt) 
{
   int error;
   uint4 _boardReg;
   const char * _filename;
   svtvme_t *_self;

   _boardReg  = (uint4) boardReg;

   _filename = (*env)->GetStringUTFChars(env, filename, NULL);
   assert(_filename != NULL);  

   _self = (svtvme_t *) self;
   assert(_self != NULL);
   
   switch (opt) {
   default: 
       error = svtvme_svt_downloadRam(_boardReg, (char *)_filename, _self);
       break;
   }

   (*env)->ReleaseStringUTFChars(env, filename, _filename);
   return ((jint) error); 
}

jint
spyInfo(JNIEnv *env, jobject obj, jint self, jint boardReg, jobject state, int opt) 
{
   int error;
   uint4 stateValue;
   jclass cls;
   jfieldID fid;
   svtvme_t * _self;
   uint4 _boardReg;

   _boardReg = (uint4) boardReg;

   _self  = (svtvme_t *) self;
   assert(_self != NULL);

   switch (opt) {
   case 0:
     error = svtvme_spy_pointer(_boardReg, &stateValue, _self);
     break;
   case 1:
     error = svtvme_spy_overflow(_boardReg, &stateValue, _self);
     break;
   case 2:
     error = svtvme_spy_freeze(_boardReg, &stateValue, _self);
     break;
   }

   cls = (*env)->GetObjectClass(env, state); /* Get a reference to org.omg.CORBA.IntHolder */  

   /* Look for the instance field 'value' in org.omg.CORBA.IntHolder*/
   fid = (*env)->GetFieldID(env, cls, "value", "I");
   if (fid == NULL) {
     return -1; /* Failed to find the field */
   }

   (*env)->SetIntField(env, state, fid, (jint) stateValue);

   return ((jint)error);
}

jint 
compare(JNIEnv *env, jobject obj, jint self, jint boardReg, 
  jstring filename, jobject state, int opt) 
{
   int error;
   uint4 stateValue;
   jclass cls;
   jfieldID fid;
   svtvme_t * _self;
   uint4 _boardReg;
   const char *_filename;

   _boardReg = (uint4) boardReg;

   _filename = (*env)->GetStringUTFChars(env, filename, NULL);
   assert(_filename != NULL);  

   _self  = (svtvme_t *) self;
   assert(_self != NULL);

   switch (opt) {
   case 0:
     error = svtvme_svt_compareFifo(_boardReg, (char *)_filename, &stateValue, _self);
     break;
   case 1:
     error = svtvme_svt_compareRam(_boardReg, (char *)_filename, &stateValue, _self);
     break;
   case 2:
     error = svtvme_svt_compareSpy(_boardReg, (char *)_filename, &stateValue, _self);
     break;
   case 3:
     error = svtvme_svt_downloadCompareRam(_boardReg, (char *)_filename, &stateValue, _self);
     break;
   }

   cls = (*env)->GetObjectClass(env, state); /* Get a reference to org.omg.CORBA.IntHolder */  

   /* Look for the instance field 'value' in org.omg.CORBA.IntHolder*/
   fid = (*env)->GetFieldID(env, cls, "value", "I");
   if (fid == NULL) {
     return -1; /* Failed to find the field */
   }

   (*env)->SetIntField(env, state, fid, (jint) stateValue);
   (*env)->ReleaseStringUTFChars(env, filename, _filename);

   return ((jint)error);
}
