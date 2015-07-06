#include <stdio.h>
#include <stdlib.h>
#include "jnitools.h"

jobject JObjectFromPointer(JNIEnv *jenv, char *className, void *p) {
  jclass jc;
  jmethodID jmid;
  jobject jresult;
  jlong jl;

  if (p == NULL) return NULL;

  jc = (*jenv)->FindClass(jenv, className);
  if (jc == NULL) {
    printf("ERROR. Class %s not found!\n", className);
    return NULL;
  }

  jmid = (*jenv)->GetStaticMethodID(jenv, jc, "initializeFromPointer", "(J)Ljava/lang/Object;");
  if (jmid == NULL) {
    printf("ERROR. Method initializeFromPointer() not found!\n");
    return NULL;
  }
  
  *(void **) &jl = p;  /* Need to understand better */
  jresult = (*jenv)->CallStaticObjectMethod(jenv, jc, jmid, jl);

  return jresult;
}
void *PointerFromJObject(JNIEnv *jenv, jobject jo) {
  jclass jc;
  jmethodID jmid;
  jlong jl;

  if (jo == NULL) return NULL;

  jc = (*jenv)->GetObjectClass(jenv, jo);
  if (jc == NULL) return NULL;

  jmid = (*jenv)->GetMethodID(jenv, jc, "self", "()J");
  if (jmid == NULL) return NULL;

  jl = (*jenv)->CallLongMethod(jenv, jo, jmid);

  return *(void **) &jl;
}
