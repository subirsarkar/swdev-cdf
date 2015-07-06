/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class jsvtsim_HbSimImpl */

#ifndef _Included_jsvtsim_HbSimImpl
#define _Included_jsvtsim_HbSimImpl
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     jsvtsim_HbSimImpl
 * Method:    svtsim_hb_new
 * Signature: (I)Ljsvtsim/hbsim_t;
 */
JNIEXPORT jobject JNICALL Java_jsvtsim_HbSimImpl_svtsim_1hb_1new
  (JNIEnv *, jclass, jint);

/*
 * Class:     jsvtsim_HbSimImpl
 * Method:    svtsim_hb_del
 * Signature: (Ljsvtsim/hbsim_t;)V
 */
JNIEXPORT void JNICALL Java_jsvtsim_HbSimImpl_svtsim_1hb_1del
  (JNIEnv *, jclass, jobject);

/*
 * Class:     jsvtsim_HbSimImpl
 * Method:    svtsim_hb_procEvent1
 * Signature: (Ljsvtsim/hbsim_t;)V
 */
JNIEXPORT void JNICALL Java_jsvtsim_HbSimImpl_svtsim_1hb_1procEvent1
  (JNIEnv *, jclass, jobject);

/*
 * Class:     jsvtsim_HbSimImpl
 * Method:    svtsim_hb_plugHitInput
 * Signature: (Ljsvtsim/hbsim_t;Ljsvtsim/cable_t;)V
 */
JNIEXPORT void JNICALL Java_jsvtsim_HbSimImpl_svtsim_1hb_1plugHitInput
  (JNIEnv *, jclass, jobject, jobject);

/*
 * Class:     jsvtsim_HbSimImpl
 * Method:    svtsim_hb_plugRoadInput
 * Signature: (Ljsvtsim/hbsim_t;Ljsvtsim/cable_t;)V
 */
JNIEXPORT void JNICALL Java_jsvtsim_HbSimImpl_svtsim_1hb_1plugRoadInput
  (JNIEnv *, jclass, jobject, jobject);

/*
 * Class:     jsvtsim_HbSimImpl
 * Method:    svtsim_hb_outputCable
 * Signature: (Ljsvtsim/hbsim_t;)Ljsvtsim/cable_t;
 */
JNIEXPORT jobject JNICALL Java_jsvtsim_HbSimImpl_svtsim_1hb_1outputCable
  (JNIEnv *, jclass, jobject);

/*
 * Class:     jsvtsim_HbSimImpl
 * Method:    svtsim_hb_useMaps
 * Signature: (Ljsvtsim/hbsim_t;Ljsvtsim/wedgemaps_t;)V
 */
JNIEXPORT void JNICALL Java_jsvtsim_HbSimImpl_svtsim_1hb_1useMaps
  (JNIEnv *, jclass, jobject, jobject);

#ifdef __cplusplus
}
#endif
#endif
