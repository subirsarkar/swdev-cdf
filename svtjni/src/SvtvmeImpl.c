#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include "jnitools.h"
#include "svtvme_public.h"
#include "jsvtvme_SvtvmeImpl.h"

/*
 * The native (C) part of the Java Native Interface. All the svtvme
 * functions are wrapped here by functions with the naming convention
 * Java_jsvtvme_SvtvmeImpl_svtvme_name(..). We strictly adhere to
 * JNI specification and use the header file created by javah from
 * the Java Native interface spcification.
 */

/* Local function protoypes */
/* Convert the board handle passed from Java into real pointer */
static svtvme_h getCHandle(JNIEnv *env, jobject self);

/* Open gile gracefully */
static FILE *gfopen(char *filename, char *mode);

/* Convenience functions */
JNIEXPORT jint JNICALL 
   jsvtvme_getSvtList(JNIEnv *env, jclass jcl, 
      jint boardType, jint max, jobject state, jintArray data, int opt);
JNIEXPORT jint JNICALL 
   jsvtvme_setIntegerObjectValue(JNIEnv *env, jobject state, int value); 
JNIEXPORT jint JNICALL 
   jsvtvme_setStringObjectValue(JNIEnv *env, jobject state, char *value);

/* Implementation detail */

/* Implementation of <svtvme_initialise> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1initialise(JNIEnv *env, jclass jcl)
{
  return ((jint) svtvme_initialise());
}

/* Implementation  of <svtvme_setGlobalFlag> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1setGlobalFlag
  (JNIEnv *env, jclass jcl, jint flag, jint value) 
{
  return ((jint) svtvme_setGlobalFlag((int)flag, (int)value));
}

/* Implementation of <svtvme_getGlobalFlag> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1getGlobalFlag
  (JNIEnv *env, jclass jcl, jint flag, jobject state) 
{
  int _value, error, icode;

  error = svtvme_getGlobalFlag((int)flag, &_value);
  icode = jsvtvme_setIntegerObjectValue(env, state, _value); 
  if (icode != 0) {
    printf("Could not set value %d in the integer object\n", _value);
  }
  return ((jint)error);
}

/* Implementation of <svtvme_openBoard> */
JNIEXPORT jobject JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1openBoard
  (JNIEnv *env, jclass jcl, jstring crate, jint slot, jint boardType)
{
  svtvme_h _board;

  const char * _crate = (*env)->GetStringUTFChars(env, crate, NULL);
  assert(_crate != NULL);   

  _board = svtvme_openBoard((char *)_crate, (int)slot, (int)boardType);
  assert(_board != NULL);
  
  (*env)->ReleaseStringUTFChars(env, crate, _crate);

  return JObjectFromPointer(env, "jsvtvme/svtvme_t", _board);
}

/* Implementation of <svtvme_closeBoard> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1closeBoard
  (JNIEnv *env, jclass jcl, jobject board)
{
  return ((jint) svtvme_closeBoard(getCHandle(env, board)));
}

/* Implementation of <svtvme_disableFisionErrorMessages> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1disableFisionErrorMessages
  (JNIEnv *env, jclass jcl, jobject board)
{
  return ((jint) svtvme_disableFisionErrorMessages(getCHandle(env, board)));
}

/* Implementation of <svtvme_enableFisionErrorMessages> */
JNIEXPORT jint JNICALL 
Java_jsvtvme_SvtvmeImpl_svtvme_1enableFisionErrorMessages
  (JNIEnv *env, jclass jcl, jobject board)
{
  return ((jint) svtvme_enableFisionErrorMessages(getCHandle(env, board)));
}

/* Implementation of <svtvme_setBoardFlag> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1setBoardFlag
  (JNIEnv *env, jclass jcl, jobject board, jint flag, jint value)
{
  return ((jint)svtvme_setBoardFlag(getCHandle(env, board), (int)flag, (int)value));
}

/* Implementation of <svtvme_getBoardFlag> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1getBoardFlag
  (JNIEnv *env, jclass jcl, jobject board, jint flag, jobject state)
{
  int _value, error, icode;

  error = svtvme_getBoardFlag(getCHandle(env, board), (int)flag, &_value);
  icode = jsvtvme_setIntegerObjectValue(env, state, _value); 
  if (icode != 0) {
    printf("Could not set value %d in the integer object\n", _value);
  }

  return ((jint)error);
}

/* Implementation of <svtvme_setBoardDebugFile> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1setBoardDebugFile
  (JNIEnv *env, jclass jcl, jobject board, jstring filename) 
{
  int error;
  FILE *file;
  const char * _filename = (*env)->GetStringUTFChars(env, filename, NULL);
  assert(_filename != NULL);   

  if (!(file = gfopen((char *) filename, "r"))) 
    error = -111;
  else
    error = svtvme_setBoardDebugFile(getCHandle(env, board), file);

  (*env)->ReleaseStringUTFChars(env, filename, _filename);

  return ((jint) error);
}

/* Implementation of <svtvme_stringToObject> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1stringToObject
  (JNIEnv *env, jclass jcl, jstring objName)
{
  int objId;
  const char * _objName;

  _objName = (*env)->GetStringUTFChars(env, objName, NULL);
  assert(_objName != NULL);   

  objId = svtvme_stringToObject((char *) _objName);
  (*env)->ReleaseStringUTFChars(env, objName, _objName);

  return ((jint)objId);
}

/* Implementation <svtvme_objectName> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1objectName
  (JNIEnv *env, jclass jcl, jint objId, jobject objName) 
{
  int error, icode;
  char _value[40];

  error = svtvme_objectName((int)objId, _value);
  icode = jsvtvme_setStringObjectValue(env, objName, _value); 
  if (icode != 0) {
    printf("Could not set value %s in the integer object\n", _value);
  }

  return ((jint)error);
}

/* Implementation of <svtvme_boardName> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1boardName
  (JNIEnv *env, jclass jcl, jint boardType, jobject boardName)
{
  int error, icode;
  char _value[40];

  error = svtvme_boardName((int)boardType, _value);
  icode = jsvtvme_setStringObjectValue(env, boardName, _value); 
  if (icode != 0) {
    printf("Could not set value %s in the integer object\n", _value);
  }

  return ((jint)error);
}

/* Implementation of <svtvme_nSpy> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1nSpy  (JNIEnv *env, jclass jcl, jint boardId) 
{
  return ((jint) svtvme_nSpy((int)boardId));
}

/* Implementation of <svtvme_getBoardType> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1getBoardType
  (JNIEnv *env, jclass jcl, jobject board)
{
  return ((jint) svtvme_getBoardType(getCHandle(env, board)));
}

/* Implementation of <svtvme_getBoardIdentifier> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1getBoardIdentifier
   (JNIEnv *env, jclass jcl, jobject board, jobject typeState, jobject serialNumState) 
{
  int error, icode;
  int _type   = 0;
  int _serial = 0;

  error = svtvme_getBoardIdentifier(getCHandle(env, board), &_type, &_serial);

  icode = jsvtvme_setIntegerObjectValue(env, typeState, _type); 
  if (icode != 0) {
    printf("Could not set value %d in the integer object\n", _type);
  }

  icode = jsvtvme_setIntegerObjectValue(env, serialNumState, _serial); 
  if (icode != 0) {
    printf("Could not set value %d in the integer object\n", _serial);
  }

  return ((jint)error);
}

/* Implementation of <svtvme_boardId> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1boardId(JNIEnv *env, jclass jcl, jobject board)
{
  return ((jint) svtvme_boardId(getCHandle(env, board)));
}

/* Implementation of <svtvme_boardSn> */
JNIEXPORT jint JNICALL 
Java_jsvtvme_SvtvmeImpl_svtvme_1boardSn(JNIEnv *env, jclass jcl, jobject board)
{
  return ((jint) svtvme_boardSn(getCHandle(env, board)));
}
/* Implementation of <svtvme_probeId> */
JNIEXPORT jint JNICALL 
Java_jsvtvme_SvtvmeImpl_svtvme_1probeId
    (JNIEnv *env, jclass jcl, jstring crate, jint slot)
{
  int _id;
  const char * _crate = (*env)->GetStringUTFChars(env, crate, NULL);
  assert(_crate != NULL);
  
  _id = svtvme_probeId((char *)_crate, (int)slot);

  (*env)->ReleaseStringUTFChars(env, crate, _crate);
  return ((jint) _id);
}

/* Implementation of <svtvme_boardType> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1boardType(JNIEnv *env, jclass jcl, jobject board)
{
  return ((jint) svtvme_boardType(getCHandle(env, board)));
}

/* Implementation of <svtvme_address> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1address (JNIEnv *env, jclass jcl, jint objectId)
{
  return ((jint) svtvme_address((int)objectId));
} 

/* Implementation of <svtvme_length> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1length(JNIEnv *env, jclass jcl, jint objectId) 
{
  return ((jint) svtvme_length((int)objectId));
}

/* Implementation of <svtvme_nWords> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1nWords(JNIEnv *env, jclass jcl, jint objectId)
{
  return ((jint) svtvme_nWords((int)objectId));
}

/* Implementation of <svtvme_mask> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1mask(JNIEnv *env, jclass jcl, jint objectId)
{
  return ((jint) svtvme_mask((int)objectId));
}

/* Implementation of <svtvme_shift> */
JNIEXPORT jint JNICALL Java_jsvtvme_SvtvmeImpl_svtvme_1shift
  (JNIEnv *env, jclass jcl, jint objectId)
{
  return ((jint) svtvme_shift((int)objectId));
}

/* Implementation of <svtvme_init> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1init
  (JNIEnv *env, jclass jcl, jobject board)
{
  return ((jint) svtvme_init(getCHandle(env, board)));
}

/* Implementation of  <svtvme_setTmode> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1setTmode
  (JNIEnv *env, jclass jcl, jobject board)
{
  return ((jint) svtvme_setTmode(getCHandle(env, board)));
}

/* Implementation of <svtvme_isTmode> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1isTmode
  (JNIEnv *env, jclass jcl, jobject board) 
{
  return ((jint) svtvme_isTmode(getCHandle(env, board)));
}

/* Implementation of <svtvme_getState> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1getState
  (JNIEnv *env, jclass jcl, jobject board, jint regId, jobject state)
{
  int error, icode;
  uint4 _value;

  error = svtvme_getState(getCHandle(env, board), (int)regId, &_value);
  icode = jsvtvme_setIntegerObjectValue(env, state, _value); 
  if (icode != 0) {
    printf("Could not set value %d in the integer object\n", _value);
  }

  return ((jint)error);
}

/* Implementation of <svtvme_setState> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1setState
  (JNIEnv *env, jclass jcl, jobject board, jint regId, jint stateValue)
{
  return ((jint) svtvme_setState(getCHandle(env, board), (int)regId, (uint4)stateValue));
}

/* Implementation of <svtvme_checkState> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1checkState
  (JNIEnv *env, jclass jcl, jobject board, jint regId, jint stateValue)
{
  return ((jint) svtvme_checkState(getCHandle(env, board), (int)regId, (uint4)stateValue));
}
/* Implementation of <svtvme_testRegister> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1testRegister
  (JNIEnv *env, jclass jcl, jobject board, jint regId)
{
  return ((jint) svtvme_testRegister(getCHandle(env, board), (int)regId));
}

/* Implementation of <svtvme_readFifoMode> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1readFifoMode
  (JNIEnv *env, jclass jcl, jobject board, jint regId, jint nWord, jintArray data)
{
  int error;
  jint *_carr;
  jsize len;

  len = (*env)->GetArrayLength(env, data);
  if (len <= 0) return -1;

  _carr = (*env)->GetIntArrayElements(env, data, NULL);
  if (_carr == NULL) { /* User must preallocate array in Java program */
    return -1;
  }
  error = svtvme_readFifoMode(getCHandle(env, board), 
        (int)regId, (int)nWord, (uint4 *)_carr);

  (*env)->SetIntArrayRegion(env, data, 0, len, _carr);
  (*env)->ReleaseIntArrayElements(env, data, _carr, 0);
  
  return ((jint)error);
}

/* Implementation of <svtvme_writeFifoMode> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1writeFifoMode
  (JNIEnv *env, jclass jcl, jobject board, jint regId, jint nWord, jintArray data)
{
  int error;
  jint *_carr;
  jsize len;

  len = (*env)->GetArrayLength(env, data);
  if (len <= 0) return -1;

  _carr = (*env)->GetIntArrayElements(env, data, NULL);
  if (_carr == NULL) { /* User must preallocate array in Java program */
    return -1;
  }
  error = svtvme_writeFifoMode(getCHandle(env, board), 
          (int)regId, (int)nWord, (uint4 *)_carr);
  (*env)->ReleaseIntArrayElements(env, data, _carr, 0);
  
  return ((jint)error);
}

/* Implementation of <svtvme_readMemory> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1readMemory
  (JNIEnv *env, jclass jcl, jobject board, jint memId, jint nData, jintArray data)
{
  int error;
  jint *_carr;
  jsize len;

  len = (*env)->GetArrayLength(env, data);
  if (len <= 0) return -1;

  _carr = (*env)->GetIntArrayElements(env, data, NULL);
  if (_carr == NULL) { /* User must preallocate array in Java program */
    return -1;
  }
  error = svtvme_readMemory(getCHandle(env, board), 
       (int)memId, (int)nData, (uint4 *)_carr);

  (*env)->SetIntArrayRegion(env, data, 0, len, _carr);
  (*env)->ReleaseIntArrayElements(env, data, _carr, 0);
  
  return ((jint)error);
}

/* Implementation of <svtvme_checkMemory> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1checkMemory
  (JNIEnv *env, jclass jcl, jobject board, jint memId, jint nData, jintArray data, 
   jint stopFlag, jobject state) 
{
  int error, icode;
  int _nError;
  jint *_carr;
  jsize len;

  len   = (*env)->GetArrayLength(env, data);
  if (len <= 0) return -1;

  _carr = (*env)->GetIntArrayElements(env, data, NULL);
  if (_carr == NULL) { /* User must preallocate array in Java program */
    return -1;
  }
  error = svtvme_checkMemory(getCHandle(env, board), (int)memId, (int)nData, 
            (uint4 *)_carr, (int)stopFlag, &_nError); 

  (*env)->SetIntArrayRegion(env, data, 0, len, _carr); /* Check C code */
  (*env)->ReleaseIntArrayElements(env, data, _carr, 0);

  icode = jsvtvme_setIntegerObjectValue(env, state, _nError); 
  if (icode != 0) {
    printf("Could not set value %d in the integer object\n", _nError);
  }
  
  return ((jint)error);
}

/* Implementation of <svtvme_cksumMemory> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1cksumMemory
  (JNIEnv *env, jclass jcl, jobject board, jint memId, jobject state)
{
   int error, icode;
   uint4 _value = 0;

   error = svtvme_cksumMemory(getCHandle(env, board), (int)memId, &_value);

  icode = jsvtvme_setIntegerObjectValue(env, state, _value); 
  if (icode != 0) {
    printf("Could not set value %d in the integer object\n", _value);
  }

  return ((jint)error);
}

/* Implementation of <svtvme_testMemory> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1testMemory(JNIEnv *env, jclass jcl, 
       jobject board, jint memId, jint nTimes, jint stopFlag, jobject state)
{
  int error, icode;
  int _nError;

  error = svtvme_testMemory(getCHandle(env, board), (int)memId, (int)nTimes, 
                                (int)stopFlag, &_nError); 
  
  icode = jsvtvme_setIntegerObjectValue(env, state, _nError); 
  if (icode != 0) {
    printf("Could not set value %d in the integer object\n", _nError);
  }
  return ((jint)error);
}

/* Implementation of <svtvme_testIDPROM> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1testIDPROM
  (JNIEnv *env, jclass jcl, jobject board)
{
   return ((jint) svtvme_testIDPROM(getCHandle(env, board)));
}

/* Implementation of <svtvme_writeMemory> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1writeMemory
  (JNIEnv *env, jclass jcl, jobject board, jint memId, jint nData, jintArray data)
{
  int error;
  jint *_carr;
  jsize len;

  len = (*env)->GetArrayLength(env, data);
  if (len <= 0) return -1;

  _carr = (*env)->GetIntArrayElements(env, data, NULL);
  if (_carr == NULL) { /* User must preallocate array in Java program */
    return -1;
  }
  error = svtvme_writeMemory(getCHandle(env, board), 
        (int)memId, (int)nData, (uint4 *)_carr);

  (*env)->ReleaseIntArrayElements(env, data, _carr, 0);
  
  return ((jint)error);
}

/* Implementation of <svtvme_writeVerifyMemory> */
JNIEXPORT jint JNICALL Java_jsvtvme_SvtvmeImpl_svtvme_1writeVerifyMemory
  (JNIEnv *env, jclass jcl, jobject board, jint memId, 
      jint ndata, jintArray data, jint stopFlag, jobject state)
{
  int error, icode;
  jint *_carr;
  jsize len;
  int _value = 0;

  len   = (*env)->GetArrayLength(env, data);
  if (len <= 0) return -1;

  _carr = (*env)->GetIntArrayElements(env, data, NULL);
  if (_carr == NULL) { /* User must preallocate array in Java program */
    return -1;
  }
  error = svtvme_writeVerifyMemory(getCHandle(env, board), (int)memId, 
     (int) ndata, (uint4 *)_carr, (int)stopFlag, &_value);
  (*env)->ReleaseIntArrayElements(env, data, _carr, 0);
  
  icode = jsvtvme_setIntegerObjectValue(env, state, _value); 
  if (icode != 0) {
    printf("Could not set value %d in the integer object\n", _value);
  }

  return ((jint)error);
}

/* Implementation of <svtvme_readMemoryFragment> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1readMemoryFragment(JNIEnv *env, jclass jcl, 
     jobject board, jint memId, jint offset, jint nWord, jintArray data)
{
  int error;
  jint *_carr;
  jsize len;

  len = (*env)->GetArrayLength(env, data);
  if (len <= 0) return -1;

  _carr = (*env)->GetIntArrayElements(env, data, NULL);
  if (_carr == NULL) { /* User must preallocate array in Java program */
    return -1;
  }
  error = svtvme_readMemoryFragment(getCHandle(env, board), (int)memId, 
                                     (uint4)offset, (int)nWord, (uint4 *)_carr);

  (*env)->SetIntArrayRegion(env, data, 0, len, _carr);
  (*env)->ReleaseIntArrayElements(env, data, _carr, 0);
  
  return ((jint)error);
}

/* Implementation of <svtvme_writeMemoryFragment> */
JNIEXPORT jint JNICALL
 Java_jsvtvme_SvtvmeImpl_svtvme_1writeMemoryFragment(JNIEnv *env, jclass jcl, 
       jobject board, jint memId, jint offset, jint nWord, jintArray data)
{
  int error;
  jint *_carr;
  jsize len;

  len = (*env)->GetArrayLength(env, data);
  if (len <= 0) return -1;

  _carr = (*env)->GetIntArrayElements(env, data, NULL);
  if (_carr == NULL) { /* User must preallocate array in Java program */
    return -1;
  }
  error = svtvme_writeMemoryFragment(getCHandle(env, board), (int)memId, 
                                     (uint4)offset, (int)nWord, (uint4 *)_carr);

  (*env)->ReleaseIntArrayElements(env, data, _carr, 0);
  
  return ((jint)error);
}

/* Implementation of <svtvme_checkMemoryFragment> */
JNIEXPORT jint JNICALL 
Java_jsvtvme_SvtvmeImpl_svtvme_1checkMemoryFragment
  (JNIEnv *env, jclass jcl, jobject board, jint memId, jint offset, 
        jint ndata, jintArray data, jint stopFlag, jobject state)
{
  int error, icode;
  jint *_carr;
  jsize len;
  int _value = 0;

  len = (*env)->GetArrayLength(env, data);
  if (len <= 0) return -1;

  _carr = (*env)->GetIntArrayElements(env, data, NULL);
  if (_carr == NULL) { /* User must preallocate array in Java program */
    return -1;
  }
  error = svtvme_checkMemoryFragment(getCHandle(env, board), (int)memId, 
     (uint4)offset, (int) ndata, (uint4 *)_carr, (int)stopFlag, &_value);
  (*env)->ReleaseIntArrayElements(env, data, _carr, 0);
  
  icode = jsvtvme_setIntegerObjectValue(env, state, _value); 
  if (icode != 0) {
    printf("Could not set value %d in the integer object\n", _value);
  }

  return ((jint)error);
}

/* Implementation of <svtvme_readWord> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1readWord
  (JNIEnv *env, jclass jcl, jobject board, jint addr, jobject state) 
{
  uint4 _value;
  int error, icode;

  error = svtvme_readWord(getCHandle(env, board), (uint4)addr, &_value);

  icode = jsvtvme_setIntegerObjectValue(env, state, _value); 
  if (icode != 0) {
    printf("Could not set value %d in the integer object\n", _value);
  }

  return ((jint)error);
}

/* Implementation of <svtvme_readWords> */
JNIEXPORT jint JNICALL 
  Java_jsvtvme_SvtvmeImpl_svtvme_1readWords
    (JNIEnv *env, jclass jcl, jobject board, jint addr, jintArray data, jint nData)
{
  int error;
  jint *_carr;
  jsize len;

  len = (*env)->GetArrayLength(env, data);
  if (len <= 0) return -1;

  _carr = (*env)->GetIntArrayElements(env, data, NULL);
  if (_carr == NULL) { /* User must preallocate array in Java program */
    return -1;
  }
  error = svtvme_readWords(getCHandle(env, board), 
         (uint4)addr, (uint4 *)_carr, (int)nData);

  (*env)->SetIntArrayRegion(env, data, 0, len, _carr);
  (*env)->ReleaseIntArrayElements(env, data, _carr, 0);
  
  return ((jint)error);
}

/* Implementation of <svtvme_cksumBlock> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1cksumBlock
  (JNIEnv *env, jclass jcl, jobject board, jint addr, 
   jint mask, jint nData, jobject state) 
{
  int error, icode;
  uint4 _value;

  error = svtvme_cksumBlock(getCHandle(env, board), 
     (uint4)addr, (uint4)mask, (int)nData, &_value);

  icode = jsvtvme_setIntegerObjectValue(env, state, _value); 
  if (icode != 0) {
    printf("Could not set value %d in the integer object\n", _value);
  }

  return ((jint) error);
}

/* Implementation of <svtvme_writeWord> */
JNIEXPORT jint JNICALL 
  Java_jsvtvme_SvtvmeImpl_svtvme_1writeWord
    (JNIEnv *env, jclass jcl, jobject board, jint addr, jint data)
{
   int error;
   error = svtvme_writeWord(getCHandle(env, board), (uint4)addr, (uint4)data);
   return ((jint)error);
}

/* Implementation of <svtvme_writeWords> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1writeWords
  (JNIEnv *env, jclass jcl, jobject board, jint addr, jint nData, jintArray data)
{
  int error;
  jint *_carr;
  jsize len;

  len = (*env)->GetArrayLength(env, data);
  if (len <= 0) return -1;

  _carr = (*env)->GetIntArrayElements(env, data, NULL);
  if (_carr == NULL) { /* User must preallocate array in Java program */
    return -1;
  }
  error = svtvme_writeWords(getCHandle(env, board), 
     (uint4)addr, (int)nData, (uint4 *)_carr);

  (*env)->ReleaseIntArrayElements(env, data, _carr, 0);
  
  return ((jint)error);
}

/* Implementation of <svtvme_isHeld> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1isHeld
  (JNIEnv *env, jclass jcl, jobject board)
{
  return ((jint) svtvme_isHeld(getCHandle(env, board)));
}

/* Implementation of <svtvme_isEmpty> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1isEmpty
  (JNIEnv *env, jclass jcl, jobject board, jint fifoId)
{
  return ((jint) svtvme_isEmpty(getCHandle(env, board), (int) fifoId));
}

/* Implementation of <svtvme_isLast> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1isLast
  (JNIEnv *env, jclass jcl, jint fifoId, jint word)
{
  return ((jint) svtvme_isLast((int) fifoId, (uint4) word));
}

/* Implementation of <svtvme_readFifo> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1readFifo
  (JNIEnv *env, jclass jcl, jobject board, jint fifoId, jint nWord, jintArray data)
{
  int error;
  jint *_carr;
  jsize len;

  len = (*env)->GetArrayLength(env, data);
  if (len <= 0) return -1;

  _carr = (*env)->GetIntArrayElements(env, data, NULL);
  if (_carr == NULL) { /* User must preallocate array in Java program */
    return -1;
  }
  error = svtvme_readFifo(getCHandle(env, board), 
         (int)fifoId, (int)nWord, (uint4 *)_carr);

  (*env)->SetIntArrayRegion(env, data, 0, len, _carr);
  (*env)->ReleaseIntArrayElements(env, data, _carr, 0);
  
  return ((jint)error);
}

/* Implementation of <svtvme_readAllFifo> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1readAllFifo(JNIEnv *env, jclass jcl, 
      jobject board, jint fifoId, jint maxWords, jintArray data, jobject state)
{
  int error, icode, _moreData;
  jint *_carr;
  jsize len;

  len   = (*env)->GetArrayLength(env, data);
  if (len <= 0) return -1;

  _carr = (*env)->GetIntArrayElements(env, data, NULL);
  if (_carr == NULL) { /* User must preallocate array in Java program */
    return -1;
  }
  error = svtvme_readAllFifo(getCHandle(env, board), 
        (int)fifoId, (int)maxWords, (uint4 *)_carr, &_moreData);

  (*env)->SetIntArrayRegion(env, data, 0, len, _carr);
  (*env)->ReleaseIntArrayElements(env, data, _carr, 0);
  
  icode = jsvtvme_setIntegerObjectValue(env, state, _moreData); 
  if (icode != 0) {
    printf("Could not set value %d in the integer object\n", _moreData);
  }
  return ((jint)error);
}

/* Implementation of <svtvme_spyCounter> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1spyCounter
  (JNIEnv *env, jclass jcl, jobject board, jint spyId) 
{
  return ((jint) svtvme_spyCounter(getCHandle(env, board), (int) spyId));
}

/* Implementation of <svtvme_isFrozen> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1isFrozen
  (JNIEnv *env, jclass jcl, jobject board, jint spyId) 
{
  return ((jint) svtvme_isFrozen(getCHandle(env, board), (int) spyId));
}

/* Implementation of <svtvme_isWrapped> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1isWrapped
  (JNIEnv *env, jclass jcl, jobject board, jint spyId)
{
  return ((jint) svtvme_isWrapped(getCHandle(env, board), (int) spyId));
}

/* Implementation of <svtvme_deltaSpy> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1deltaSpy
  (JNIEnv *env, jclass jcl, jint spyId, jint end, jint start)
{
  return ((jint) svtvme_deltaSpy((int) spyId, (uint4) end, (uint4) start));
}

/* Implementation of <svtvme_SpyRamId> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1SpyRamId
  (JNIEnv *env, jclass jcl, jint spyId)
{
  return ((jint) svtvme_SpyRamId((int) spyId));
}

/* Implementation of <svtvme_resetSpy> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1resetSpy
  (JNIEnv *env, jclass jcl, jobject board, jint spyId)
{
  return ((jint) svtvme_resetSpy(getCHandle(env, board), (int) spyId));
}

/* Implementation of <svtvme_readSpyTail> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1readSpyTail
  (JNIEnv *env, jclass jcl, jobject board, jint spyId, jint nWord, jintArray data)
{
  int error;
  jint *_carr;
  jsize len;

  len = (*env)->GetArrayLength(env, data);
  if (len <= 0) return -1;

  _carr = (*env)->GetIntArrayElements(env, data, NULL);
  if (_carr == NULL) { /* User must preallocate array in Java program */
    return -1;
  }
  error = svtvme_readSpyTail(getCHandle(env, board), 
         (int)spyId, (int)nWord, (uint4 *)_carr);

  (*env)->SetIntArrayRegion(env, data, 0, len, _carr);
  (*env)->ReleaseIntArrayElements(env, data, _carr, 0);
  
  return ((jint)error);
}

/* Implementation of <svtvme_readAllSpy> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1readAllSpy
  (JNIEnv *env, jclass jcl, jobject board, jint spyId, jint nData, jintArray data)
{
  int error;
  jint *_carr;
  jsize len;

  len = (*env)->GetArrayLength(env, data);
  if (len <= 0) return -1;

  _carr = (*env)->GetIntArrayElements(env, data, NULL);
  if (_carr == NULL) { /* User must preallocate array in Java program */
    return -1;
  }
  error = svtvme_readAllSpy(getCHandle(env, board), 
        (int)spyId, (int)nData, (uint4 *)_carr);

  (*env)->SetIntArrayRegion(env, data, 0, len, _carr);
  (*env)->ReleaseIntArrayElements(env, data, _carr, 0);
  
  return ((jint)error);
}

/* Implementation of <svtvme_sendDataOnce> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1sendDataOnce
  (JNIEnv *env, jclass jcl, jobject board, jint nData, jintArray data, jint speed)
{
  int error;
  jint *_carr;
  jsize len;

  len = (*env)->GetArrayLength(env, data);
  if (len <= 0) return -1;

  _carr = (*env)->GetIntArrayElements(env, data, NULL);
  if (_carr == NULL) { /* User must preallocate array in Java program */
    return -1;
  }
  error = svtvme_sendDataOnce(getCHandle(env, board), 
                 (int)nData, (uint4 *)_carr, (int)speed);

  (*env)->ReleaseIntArrayElements(env, data, _carr, 0);
  
  return ((jint)error);
}

/* Implementation of <svtvme_sendDataLoop> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1sendDataLoop 
   (JNIEnv *env, jclass jcl, jobject board, jint ndata, jintArray data)
{
  int error;
  jint *_carr;
  jsize len;

  len = (*env)->GetArrayLength(env, data);
  if (len <= 0) return -1;

  _carr = (*env)->GetIntArrayElements(env, data, NULL);
  if (_carr == NULL) { /* User must preallocate array in Java program */
    return -1;
  }
  error = svtvme_sendDataLoop(getCHandle(env, board), (int) ndata, (uint4 *)_carr);
  (*env)->ReleaseIntArrayElements(env, data, _carr, 0);
  
  return ((jint)error);
}

/* Implementation of <svtvme_resendDataOnce> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1resendDataOnce(JNIEnv *env, jclass jcl, jobject board)
{
  return ((jint) svtvme_resendDataOnce(getCHandle(env, board)));
}

/* Implementation of <svtvme_resendDataLoop> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1resendDataLoop
    (JNIEnv *env, jclass jcl, jobject board)
{
  return ((jint) svtvme_resendDataLoop(getCHandle(env, board)));
}

/* Implementation of <svtvme_resendData> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1resendData (JNIEnv *env, jclass jcl, jobject board) 
{
  return ((jint) svtvme_resendData(getCHandle(env, board)));
}

/* Implementation of <svtvme_rand> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1rand
  (JNIEnv *env, jclass jcl, jint mask) 
{
  return ((jint) svtvme_rand((uint4) mask));
}

/* Implementation of <svtvme_writeWordsAl> */
JNIEXPORT jint JNICALL 
Java_jsvtvme_SvtvmeImpl_svtvme_1writeWordsAl
  (JNIEnv *env, jclass jcl, jobject board, jint addr, jint ndata, jintArray data)
{
  int error;
  jint *_carr;
  jsize len;

  len = (*env)->GetArrayLength(env, data);
  if (len <= 0) return -1;

  _carr = (*env)->GetIntArrayElements(env, data, NULL);
  if (_carr == NULL) { /* User must preallocate array in Java program */
    return -1;
  }
  error = svtvme_writeWords(getCHandle(env, board), 
         (uint4)addr, (int)ndata, (uint4 *)_carr);

  (*env)->ReleaseIntArrayElements(env, data, _carr, 0);
  
  return ((jint)error);
}
/* Implementation of <svtvme_readWordsAl> */
JNIEXPORT jint JNICALL 
Java_jsvtvme_SvtvmeImpl_svtvme_1readWordsAl
  (JNIEnv *env, jclass jcl, jobject board, jint addr, jintArray data, jint ndata)
{
  int error;
  jint *_carr;
  jsize len;

  len = (*env)->GetArrayLength(env, data);
  if (len <= 0) return -1;

  _carr = (*env)->GetIntArrayElements(env, data, NULL);
  if (_carr == NULL) { /* User must preallocate array in Java program */
    return -1;
  }
  error = svtvme_readWords(getCHandle(env, board), 
       (uint4)addr, (uint4 *)_carr, (int)ndata);

  (*env)->SetIntArrayRegion(env, data, 0, len, _carr);
  (*env)->ReleaseIntArrayElements(env, data, _carr, 0);
  
  return ((jint)error);
}

/* Implementation of <svtvme_printObjects> */
JNIEXPORT jint JNICALL 
Java_jsvtvme_SvtvmeImpl_svtvme_1printObjects
  (JNIEnv *env, jclass jcl, jint boardType)
{
  return ((jint) svtvme_printObjects((int) boardType));
}

/* Implementation of <svtvme_getRegList> */
JNIEXPORT jint JNICALL 
Java_jsvtvme_SvtvmeImpl_svtvme_1getRegList(JNIEnv *env, jclass jcl, 
       jint boardType, jint max, jobject nRegs, jintArray regIds)
{
  return jsvtvme_getSvtList(env, jcl, boardType, max, nRegs, regIds, 0);
}

/* Implementation of <svtvme_getFifoList> */
JNIEXPORT jint JNICALL 
Java_jsvtvme_SvtvmeImpl_svtvme_1getFifoList(JNIEnv *env, jclass jcl, 
       jint boardType, jint max, jobject nFifo, jintArray fifoIds)
{
  return jsvtvme_getSvtList(env, jcl, boardType, max, nFifo, fifoIds, 1);
}

/* Implementation of <svtvme_getMemList> */
JNIEXPORT jint JNICALL 
Java_jsvtvme_SvtvmeImpl_svtvme_1getMemList(JNIEnv *env, jclass jcl, 
       jint boardType, jint max, jobject nMem, jintArray memIds)
{
  return jsvtvme_getSvtList(env, jcl, boardType, max, nMem, memIds, 2);
}

/* Implementation of <svtvme_getSpyList> */
JNIEXPORT jint JNICALL 
Java_jsvtvme_SvtvmeImpl_svtvme_1getSpyList(JNIEnv *env, jclass jcl, 
       jint boardType, jint max, jobject nSpy, jintArray spyIds)
{
  return jsvtvme_getSvtList(env, jcl, boardType, max, nSpy, spyIds, 3);
}

/* Implementation of <svtvme_boardSlot> */
JNIEXPORT jint JNICALL 
Java_jsvtvme_SvtvmeImpl_svtvme_1boardSlot
  (JNIEnv *env, jclass jcl, jobject board)
{
  return ((jint) svtvme_boardSlot(getCHandle(env, board)));
}

/* Implementation of <svtvme_boardCrate> */
JNIEXPORT jint JNICALL 
Java_jsvtvme_SvtvmeImpl_svtvme_1boardCrate
  (JNIEnv *env, jclass jcl, jobject board, jobject crateName)
{
  int error, icode;
  char _value[20];

  error = svtvme_boardCrate(getCHandle(env, board), _value);

  icode = jsvtvme_setStringObjectValue(env, crateName, _value); 
  if (icode != 0) {
    printf("Could not set value %s in the integer object\n", _value);
  }
  return ((jint)error);
}

/* Implementation of <svtvme_hf_fill_fram> */
JNIEXPORT jint JNICALL 
  Java_jsvtvme_SvtvmeImpl_svtvme_1hf_1fill_1fram
   (JNIEnv *env, jclass cls, jint slot, jstring crate, jstring dirname, jstring mode)
{
  int error;
  const char * _crate   = (*env)->GetStringUTFChars(env, crate, NULL);
  const char * _dirname = (*env)->GetStringUTFChars(env, dirname, NULL);
  const char * _mode    = (*env)->GetStringUTFChars(env, mode, NULL);
  assert(_crate != NULL && _dirname != NULL && _mode != NULL);
  
  error = svtvme_hf_fill_fram((int) slot, (char *)_crate, (char *)_dirname, (char *)_mode);

  (*env)->ReleaseStringUTFChars(env, crate, _crate);
  (*env)->ReleaseStringUTFChars(env, dirname, _dirname);
  (*env)->ReleaseStringUTFChars(env, mode, _mode);

  return ((jint)error);
}

/*
 * Implementation of <svtvme_longName>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1longName
  (JNIEnv *env, jclass cls, jint objId, jobject objName) 
{
  int error, icode;
  char _value[40];

  error = svtvme_longName((int)objId, _value);
  icode = jsvtvme_setStringObjectValue(env, objName, _value); 
  if (icode != 0) {
    printf("Could not set value %s in the integer object\n", _value);
  }

  return ((jint)error);
}

/* Implemenation of <svtvme_sendWord> */
JNIEXPORT jint JNICALL 
  Java_jsvtvme_SvtvmeImpl_svtvme_1sendWord
    (JNIEnv *env, jclass cls, jobject board, jint word) 
{
   return ((jint)svtvme_sendWord(getCHandle(env, board), (int) word));
}

/* Implementation <svtvme_shortName> */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1shortName
  (JNIEnv *env, jclass cls, jint objId, jobject objName)
{
  int error, icode;
  char _value[40];

  error = svtvme_shortName((int)objId, _value);
  icode = jsvtvme_setStringObjectValue(env, objName, _value); 
  if (icode != 0) {
    printf("Could not set value %s in the integer object\n", _value);
  }

  return ((jint)error);
}

/*
 * Implementation of <svtvme_wedge_randomtest>
 */
JNIEXPORT jint JNICALL 
 Java_jsvtvme_SvtvmeImpl_svtvme_1wedge_1randomtest
  (JNIEnv *env, jclass cls, jint wedge, jstring mapset, jint nloops, jint do_cs) 
{
  int error;
  const char * _mapset = (*env)->GetStringUTFChars(env, mapset, NULL); 

  error = svtvme_wedge_randomtest((int)wedge, (char *)_mapset, (int)nloops, (int)do_cs);
  (*env)->ReleaseStringUTFChars(env, mapset, _mapset);
  return ((jint)error);
}

/* -------- Convenience Fucntions -------------------- */

/* Open a file gracefully */
static FILE *gfopen(char *filename, char *mode)
{
  FILE *fp;
  if ((fp = fopen(filename, mode)) == NULL) {
    fprintf(stderr, "Cannot open %s - returing!\n", filename);
    return NULL;
  }
  return fp;
}

/* Get SVT Board Reg/Mem/Fifo/Spy ids */
JNIEXPORT jint JNICALL 
   jsvtvme_getSvtList(JNIEnv *env, jclass jcl, 
      jint boardType, jint max, jobject state, jintArray data, int opt)
{
  int error, icode;
  jint *_carr;
  jsize _len;
  int _value;

  _len = (*env)->GetArrayLength(env, data);
  if (_len <= 0) return -1;

  _carr = (*env)->GetIntArrayElements(env, data, NULL);
  if (_carr == NULL) { /* User must preallocate array in Java program */
    return -1;
  }
  switch (opt) {
  case 0:
    error = svtvme_getRegList((int)boardType, (int)max, &_value, (int *)_carr);
    break;
  case 1:
    error = svtvme_getFifoList((int)boardType, (int)max, &_value, (int *)_carr);
    break;
  case 2:
    error = svtvme_getMemList((int)boardType, (int)max, &_value, (int *)_carr);
    break;
  default:
    error = svtvme_getSpyList((int)boardType, (int)max, &_value, (int *)_carr);
    break;
  }
  (*env)->SetIntArrayRegion(env, data, 0, _len, _carr);
  (*env)->ReleaseIntArrayElements(env, data, _carr, 0);

  icode = jsvtvme_setIntegerObjectValue(env, state, _value); 
  if (icode != 0) {
    printf("Could not set value %d in the integer object\n", _value);
  }
  return ((jint)error);
}

/* Mimic return by pointer agruments for integer in Java */
JNIEXPORT jint JNICALL 
  jsvtvme_setIntegerObjectValue(JNIEnv *env, jobject state, int value) 
{
  jclass cls;
  jfieldID fid;
  
#if 0
  printf("setInteger() -> value to be set is %d\n", value);
#endif
  /* Get a reference to org.omg.CORBA.IntHolder */  
  cls = (*env)->GetObjectClass(env, state);
  
  /* Look for the instance field 'value' in org.omg.CORBA.IntHolder*/
  fid = (*env)->GetFieldID(env, cls, "value", "I");
  if (fid == NULL) {
    return -1; /* Failed to find the field */
  }
  (*env)->SetIntField(env, state, fid, (jint)value);
  
  return 0;
}

/* Mimic return by pointer agruments for char[]  in Java */
JNIEXPORT jint JNICALL 
  jsvtvme_setStringObjectValue(JNIEnv *env, jobject state, char *value) 
{
  jclass cls;
  jfieldID fid;
  jstring _objectValue;
  
#if 0
  printf("setString() -> value to be set is %s\n", value);
#endif
  /* Get a reference to org.omg.CORBA.StringHolder */  
  cls = (*env)->GetObjectClass(env, state); 

  /* Look for the instance field 'value' in org.omg.CORBA.StringHolder */
  fid = (*env)->GetFieldID(env, cls, "value", "Ljava/lang/String;");
  if (fid == NULL) {
    return -1; /* Failed to find the field */
  }

  _objectValue = (*env)->NewStringUTF(env, value);
  (*env)->SetObjectField(env, state, fid, _objectValue);
  
  return 0;
}
/* Obtain the C pointer to the object from the Integer which stores the value */
static svtvme_h getCHandle(JNIEnv *env, jobject self) 
{
  svtvme_h _self = (svtvme_h) PointerFromJObject(env, self);
  assert(_self != NULL);
  return _self;
}
