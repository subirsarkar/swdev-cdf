/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class jsvtvme_SvtImpl */

#ifndef _Included_jsvtvme_SvtImpl
#define _Included_jsvtvme_SvtImpl
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     jsvtvme_SvtImpl
 * Method:    svtvme_svt_open
 * Signature: (Ljava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_jsvtvme_SvtImpl_svtvme_1svt_1open
  (JNIEnv *, jobject, jstring, jint);

/*
 * Class:     jsvtvme_SvtImpl
 * Method:    svtvme_svt_close
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_jsvtvme_SvtImpl_svtvme_1svt_1close
  (JNIEnv *, jobject, jint);

/*
 * Class:     jsvtvme_SvtImpl
 * Method:    svtvme_spy_status
 * Signature: (ILorg/omg/CORBA/IntHolder;Lorg/omg/CORBA/IntHolder;Lorg/omg/CORBA/IntHolder;I)I
 */
JNIEXPORT jint JNICALL Java_jsvtvme_SvtImpl_svtvme_1spy_1status
  (JNIEnv *, jobject, jint, jobject, jobject, jobject, jint);

/*
 * Class:     jsvtvme_SvtImpl
 * Method:    svtvme_spy_pointer
 * Signature: (ILorg/omg/CORBA/IntHolder;I)I
 */
JNIEXPORT jint JNICALL Java_jsvtvme_SvtImpl_svtvme_1spy_1pointer
  (JNIEnv *, jobject, jint, jobject, jint);

/*
 * Class:     jsvtvme_SvtImpl
 * Method:    svtvme_spy_overflow
 * Signature: (ILorg/omg/CORBA/IntHolder;I)I
 */
JNIEXPORT jint JNICALL Java_jsvtvme_SvtImpl_svtvme_1spy_1overflow
  (JNIEnv *, jobject, jint, jobject, jint);

/*
 * Class:     jsvtvme_SvtImpl
 * Method:    svtvme_spy_freeze
 * Signature: (ILorg/omg/CORBA/IntHolder;I)I
 */
JNIEXPORT jint JNICALL Java_jsvtvme_SvtImpl_svtvme_1spy_1freeze
  (JNIEnv *, jobject, jint, jobject, jint);

/*
 * Class:     jsvtvme_SvtImpl
 * Method:    svtvme_svt_readFifo
 * Signature: (IILorg/omg/CORBA/IntHolder;[II)I
 */
JNIEXPORT jint JNICALL Java_jsvtvme_SvtImpl_svtvme_1svt_1readFifo
  (JNIEnv *, jobject, jint, jint, jobject, jintArray, jint);

/*
 * Class:     jsvtvme_SvtImpl
 * Method:    svtvme_svt_uploadFifo
 * Signature: (ILjava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_jsvtvme_SvtImpl_svtvme_1svt_1uploadFifo
  (JNIEnv *, jobject, jint, jstring, jint);

/*
 * Class:     jsvtvme_SvtImpl
 * Method:    svtvme_svt_compareFifo
 * Signature: (ILjava/lang/String;Lorg/omg/CORBA/IntHolder;I)I
 */
JNIEXPORT jint JNICALL Java_jsvtvme_SvtImpl_svtvme_1svt_1compareFifo
  (JNIEnv *, jobject, jint, jstring, jobject, jint);

/*
 * Class:     jsvtvme_SvtImpl
 * Method:    svtvme_svt_getIdPromBlock
 * Signature: (I[II)I
 */
JNIEXPORT jint JNICALL Java_jsvtvme_SvtImpl_svtvme_1svt_1getIdPromBlock
  (JNIEnv *, jobject, jint, jintArray, jint);

/*
 * Class:     jsvtvme_SvtImpl
 * Method:    svtvme_svt_singleRam
 * Signature: (III[II)I
 */
JNIEXPORT jint JNICALL Java_jsvtvme_SvtImpl_svtvme_1svt_1singleRam
  (JNIEnv *, jobject, jint, jint, jint, jintArray, jint);

/*
 * Class:     jsvtvme_SvtImpl
 * Method:    svtvme_svt_memoryOperation
 * Signature: (IIII[II)I
 */
JNIEXPORT jint JNICALL Java_jsvtvme_SvtImpl_svtvme_1svt_1memoryOperation
  (JNIEnv *, jobject, jint, jint, jint, jint, jintArray, jint);

/*
 * Class:     jsvtvme_SvtImpl
 * Method:    svtvme_svt_uploadRam
 * Signature: (ILjava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_jsvtvme_SvtImpl_svtvme_1svt_1uploadRam
  (JNIEnv *, jobject, jint, jstring, jint);

/*
 * Class:     jsvtvme_SvtImpl
 * Method:    svtvme_svt_downloadRam
 * Signature: (ILjava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_jsvtvme_SvtImpl_svtvme_1svt_1downloadRam
  (JNIEnv *, jobject, jint, jstring, jint);

/*
 * Class:     jsvtvme_SvtImpl
 * Method:    svtvme_svt_compareRam
 * Signature: (ILjava/lang/String;Lorg/omg/CORBA/IntHolder;I)I
 */
JNIEXPORT jint JNICALL Java_jsvtvme_SvtImpl_svtvme_1svt_1compareRam
  (JNIEnv *, jobject, jint, jstring, jobject, jint);

/*
 * Class:     jsvtvme_SvtImpl
 * Method:    svtvme_svt_downloadCompareRam
 * Signature: (ILjava/lang/String;Lorg/omg/CORBA/IntHolder;I)I
 */
JNIEXPORT jint JNICALL Java_jsvtvme_SvtImpl_svtvme_1svt_1downloadCompareRam
  (JNIEnv *, jobject, jint, jstring, jobject, jint);

/*
 * Class:     jsvtvme_SvtImpl
 * Method:    svtvme_svt_spyBufferRead
 * Signature: (III[II)I
 */
JNIEXPORT jint JNICALL Java_jsvtvme_SvtImpl_svtvme_1svt_1spyBufferRead
  (JNIEnv *, jobject, jint, jint, jint, jintArray, jint);

/*
 * Class:     jsvtvme_SvtImpl
 * Method:    svtvme_svt_uploadSpy
 * Signature: (ILjava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_jsvtvme_SvtImpl_svtvme_1svt_1uploadSpy
  (JNIEnv *, jobject, jint, jstring, jint);

/*
 * Class:     jsvtvme_SvtImpl
 * Method:    svtvme_svt_compareSpy
 * Signature: (ILjava/lang/String;Lorg/omg/CORBA/IntHolder;I)I
 */
JNIEXPORT jint JNICALL Java_jsvtvme_SvtImpl_svtvme_1svt_1compareSpy
  (JNIEnv *, jobject, jint, jstring, jobject, jint);

/*
 * Class:     jsvtvme_SvtImpl
 * Method:    svtvme_svt_randomAccess
 * Signature: (III[II)I
 */
JNIEXPORT jint JNICALL Java_jsvtvme_SvtImpl_svtvme_1svt_1randomAccess
  (JNIEnv *, jobject, jint, jint, jint, jintArray, jint);

/*
 * Class:     jsvtvme_SvtImpl
 * Method:    svtvme_svt_setDebug
 * Signature: (Ljava/lang/String;I)V
 */
JNIEXPORT void JNICALL Java_jsvtvme_SvtImpl_svtvme_1svt_1setDebug
  (JNIEnv *, jobject, jstring, jint);

#ifdef __cplusplus
}
#endif
#endif