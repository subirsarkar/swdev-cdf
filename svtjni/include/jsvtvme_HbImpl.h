/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class jsvtvme_HbImpl */

#ifndef _Included_jsvtvme_HbImpl
#define _Included_jsvtvme_HbImpl
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     jsvtvme_HbImpl
 * Method:    svtvme_hb_init
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_jsvtvme_HbImpl_svtvme_1hb_1init
  (JNIEnv *, jobject, jint);

/*
 * Class:     jsvtvme_HbImpl
 * Method:    svtvme_hb_getState
 * Signature: (ILorg/omg/CORBA/IntHolder;I)I
 */
JNIEXPORT jint JNICALL Java_jsvtvme_HbImpl_svtvme_1hb_1getState
  (JNIEnv *, jobject, jint, jobject, jint);

/*
 * Class:     jsvtvme_HbImpl
 * Method:    svtvme_hb_resetState
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_jsvtvme_HbImpl_svtvme_1hb_1resetState
  (JNIEnv *, jobject, jint, jint);

/*
 * Class:     jsvtvme_HbImpl
 * Method:    svtvme_hb_setState
 * Signature: (III)I
 */
JNIEXPORT jint JNICALL Java_jsvtvme_HbImpl_svtvme_1hb_1setState
  (JNIEnv *, jobject, jint, jint, jint);

/*
 * Class:     jsvtvme_HbImpl
 * Method:    svtvme_hb_tModeEnable
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_jsvtvme_HbImpl_svtvme_1hb_1tModeEnable
  (JNIEnv *, jobject, jint);

/*
 * Class:     jsvtvme_HbImpl
 * Method:    svtvme_hb_tModeStatus
 * Signature: (Lorg/omg/CORBA/IntHolder;I)I
 */
JNIEXPORT jint JNICALL Java_jsvtvme_HbImpl_svtvme_1hb_1tModeStatus
  (JNIEnv *, jobject, jobject, jint);

/*
 * Class:     jsvtvme_HbImpl
 * Method:    svtvme_hb_writeToOutput
 * Signature: (II[II)I
 */
JNIEXPORT jint JNICALL Java_jsvtvme_HbImpl_svtvme_1hb_1writeToOutput
  (JNIEnv *, jobject, jint, jint, jintArray, jint);

/*
 * Class:     jsvtvme_HbImpl
 * Method:    svtvme_hb_outputFile
 * Signature: (ILjava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_jsvtvme_HbImpl_svtvme_1hb_1outputFile
  (JNIEnv *, jobject, jint, jstring, jint);

#ifdef __cplusplus
}
#endif
#endif
