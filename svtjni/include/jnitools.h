#ifndef TOOLS_H
#define TOOLS_h
#include <jni.h>
jobject JObjectFromPointer(JNIEnv *jenv, char *className, void *p);
void *PointerFromJObject(JNIEnv *jenv, jobject jo);
#endif
