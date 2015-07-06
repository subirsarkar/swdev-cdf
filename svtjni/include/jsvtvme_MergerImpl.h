/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class jsvtvme_MergerImpl */

#ifndef _Included_jsvtvme_MergerImpl
#define _Included_jsvtvme_MergerImpl
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     jsvtvme_MergerImpl
 * Method:    svtvme_merger_init
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_jsvtvme_MergerImpl_svtvme_1merger_1init
  (JNIEnv *, jobject, jint, jint);

/*
 * Class:     jsvtvme_MergerImpl
 * Method:    svtvme_merger_getState
 * Signature: (ILorg/omg/CORBA/IntHolder;I)I
 */
JNIEXPORT jint JNICALL Java_jsvtvme_MergerImpl_svtvme_1merger_1getState
  (JNIEnv *, jobject, jint, jobject, jint);

/*
 * Class:     jsvtvme_MergerImpl
 * Method:    svtvme_merger_resetState
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_jsvtvme_MergerImpl_svtvme_1merger_1resetState
  (JNIEnv *, jobject, jint, jint);

/*
 * Class:     jsvtvme_MergerImpl
 * Method:    svtvme_merger_setState
 * Signature: (III)I
 */
JNIEXPORT jint JNICALL Java_jsvtvme_MergerImpl_svtvme_1merger_1setState
  (JNIEnv *, jobject, jint, jint, jint);

/*
 * Class:     jsvtvme_MergerImpl
 * Method:    svtvme_merger_tModeEnable
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_jsvtvme_MergerImpl_svtvme_1merger_1tModeEnable
  (JNIEnv *, jobject, jint);

/*
 * Class:     jsvtvme_MergerImpl
 * Method:    svtvme_merger_tModeStatus
 * Signature: (Lorg/omg/CORBA/IntHolder;I)I
 */
JNIEXPORT jint JNICALL Java_jsvtvme_MergerImpl_svtvme_1merger_1tModeStatus
  (JNIEnv *, jobject, jobject, jint);

/*
 * Class:     jsvtvme_MergerImpl
 * Method:    svtvme_merger_writeToOutput
 * Signature: (II[II)I
 */
JNIEXPORT jint JNICALL Java_jsvtvme_MergerImpl_svtvme_1merger_1writeToOutput
  (JNIEnv *, jobject, jint, jint, jintArray, jint);

/*
 * Class:     jsvtvme_MergerImpl
 * Method:    svtvme_merger_outputFile
 * Signature: (ILjava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_jsvtvme_MergerImpl_svtvme_1merger_1outputFile
  (JNIEnv *, jobject, jint, jstring, jint);

/*
 * Class:     jsvtvme_MergerImpl
 * Method:    svtvme_merger_spyBufferWrite
 * Signature: (III[II)I
 */
JNIEXPORT jint JNICALL Java_jsvtvme_MergerImpl_svtvme_1merger_1spyBufferWrite
  (JNIEnv *, jobject, jint, jint, jint, jintArray, jint);

/*
 * Class:     jsvtvme_MergerImpl
 * Method:    svtvme_merger_downloadSpy
 * Signature: (ILjava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_jsvtvme_MergerImpl_svtvme_1merger_1downloadSpy
  (JNIEnv *, jobject, jint, jstring, jint);

/*
 * Class:     jsvtvme_MergerImpl
 * Method:    svtvme_merger_downloadCompareSpy
 * Signature: (ILjava/lang/String;Lorg/omg/CORBA/IntHolder;I)I
 */
JNIEXPORT jint JNICALL Java_jsvtvme_MergerImpl_svtvme_1merger_1downloadCompareSpy
  (JNIEnv *, jobject, jint, jstring, jobject, jint);

#ifdef __cplusplus
}
#endif
#endif