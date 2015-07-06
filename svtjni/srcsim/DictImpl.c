#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include "jsvtsim_DictImpl.h"
#include "svtsim/svtsim.h"
#include "jnitools.h"

/* Local function protoypes */
/* Convert the dict handle passed from Java into real pointer */
static svtsim_dict_t *getCHandle(JNIEnv *env, jobject self);

/*
 * Implementation of <svtsim_dict_new>
 */
JNIEXPORT jobject JNICALL Java_jsvtsim_DictImpl_svtsim_1dict_1new
  (JNIEnv *env, jclass cls, jint hashsize) 
{ 
  svtsim_dict_t *_self = svtsim_dict_new((int)hashsize);
  assert(_self != NULL);

  return JObjectFromPointer(env, "jsvtsim/dict_t", _self);
}

/*
 * Implementation of <svtsim_dict_clone>
 */
JNIEXPORT jobject JNICALL Java_jsvtsim_DictImpl_svtsim_1dict_1clone
  (JNIEnv *env, jclass cls, jobject self) 
{
  svtsim_dict_t *_dict = svtsim_dict_clone(getCHandle(env, self)); 
  assert(_dict != NULL);

  return JObjectFromPointer(env, "jsvtsim/dict_t", _dict);
}

/*
 * Implementation of <svtsim_dict_free>
 */
JNIEXPORT void JNICALL Java_jsvtsim_DictImpl_svtsim_1dict_1free
  (JNIEnv *env, jclass cls, jobject self) 
{ 
  svtsim_dict_free(getCHandle(env, self)); 
}

/*
 * Implementation of <svtsim_dict_hash>
 */
JNIEXPORT jint JNICALL Java_jsvtsim_DictImpl_svtsim_1dict_1hash
  (JNIEnv *env, jclass cls, jobject self, jstring key) 
{ 
  int ret;

  const char *_key = (*env)->GetStringUTFChars(env, key, NULL);
  assert(_key != NULL);   

  ret = svtsim_dict_hash(getCHandle(env, self), _key);

  (*env)->ReleaseStringUTFChars(env, key, _key);

  return ((jint) ret);

}

/*
 * Implementation of <svtsim_dict_add>
 */
JNIEXPORT jint JNICALL Java_jsvtsim_DictImpl_svtsim_1dict_1add
  (JNIEnv *env, jclass cls, jobject self, jstring key, jstring value) 
{ 
  int ret;
  
  const char *_key   = (*env)->GetStringUTFChars(env, key, NULL);
  const char *_value = (*env)->GetStringUTFChars(env, value, NULL);

  assert(_key != NULL);   
  assert(_value != NULL);   

  ret = svtsim_dict_add(getCHandle(env, self), _key, _value);  

  (*env)->ReleaseStringUTFChars(env, key, _key);
  (*env)->ReleaseStringUTFChars(env, value, _value);

  return ((jint) ret);
}

/*
 * Implementation of <svtsim_dict_query>
 */
JNIEXPORT jstring JNICALL Java_jsvtsim_DictImpl_svtsim_1dict_1query
  (JNIEnv *env, jclass cls, jobject self, jstring key) 
{ 
  jstring _object;
  char *res;

  const char *_key = (*env)->GetStringUTFChars(env, key, NULL); 
  assert(_key != NULL);    

  res = (char *) svtsim_dict_query(getCHandle(env, self), _key);

  (*env)->ReleaseStringUTFChars(env, key, _key); 

  _object = (*env)->NewStringUTF(env, res);

   return _object;
}

/*
 * Implementation of <svtsim_dict_addOptionString>
 */
JNIEXPORT jint JNICALL Java_jsvtsim_DictImpl_svtsim_1dict_1addOptionString
  (JNIEnv *env, jclass cls, jobject self, jstring str) 
{ 
  int ret;
  const char *_str = (*env)->GetStringUTFChars(env, str, NULL);  
  assert(_str != NULL);     

  ret = svtsim_dict_addOptionString(getCHandle(env, self), _str);

  (*env)->ReleaseStringUTFChars(env, str, _str); 

  return ((jint) ret);
}

/*
 * Implementation of <svtsim_dict_addFile>
 */
JNIEXPORT jint JNICALL Java_jsvtsim_DictImpl_svtsim_1dict_1addFile
  (JNIEnv *env, jclass cls, jobject self, jstring fnam) 
{ 
  int ret;
  const char *_fnam = (*env)->GetStringUTFChars(env, fnam, NULL);  
  assert(_fnam != NULL);     

  ret = svtsim_dict_addFile(getCHandle(env, self), _fnam);

  (*env)->ReleaseStringUTFChars(env, fnam, _fnam); 

  return ((jint) ret);
}

/*
 * Implementation of <svtsim_dict_addBlob>
 */
JNIEXPORT jint JNICALL Java_jsvtsim_DictImpl_svtsim_1dict_1addBlob
  (JNIEnv *env, jclass cls, jobject self, jstring data, jint len) 
{
  int ret;
  const char *_data = (*env)->GetStringUTFChars(env, data, NULL);  
  assert(_data != NULL);     

  ret = svtsim_dict_addBlob(getCHandle(env, self), _data, (int)len);
  (*env)->ReleaseStringUTFChars(env, data, _data); 

  return ((jint) ret);
}

/*
 * Implementation of <svtsim_dict_dump>
 */
JNIEXPORT void JNICALL Java_jsvtsim_DictImpl_svtsim_1dict_1dump
  (JNIEnv *env, jclass cls, jobject self) 
{
   svtsim_dict_dump(getCHandle(env, self));
}

/*
 * Implementation of <svtsim_dict_crc>
 */
JNIEXPORT jlong JNICALL Java_jsvtsim_DictImpl_svtsim_1dict_1crc
  (JNIEnv *env, jclass cls, jobject self, jstring key) 
{
  unsigned long ret;
  const char *_key = (*env)->GetStringUTFChars(env, key, NULL);  
  assert(_key != NULL);     

  ret = svtsim_dict_crc(getCHandle(env, self), _key);

  (*env)->ReleaseStringUTFChars(env, key, _key); 

  return ((jlong) ret);
}

/* Obtain the C pointer to the object from the Integer which stores the value */
static svtsim_dict_t *getCHandle(JNIEnv *env, jobject self) 
{
  svtsim_dict_t *_self = (svtsim_dict_t *) PointerFromJObject(env, self);
  assert(_self != NULL);
  return _self;
}
