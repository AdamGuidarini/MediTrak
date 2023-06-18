#include <jni.h>

// Write C++ code here.
//
// Do not forget to dynamically load the C++ library into your application.
//
// For instance,
//
// In MainActivity.java:
//    static {
//       System.loadLibrary("medicationtracker");
//    }
//
// Or, in MainActivity.kt:
//    companion object {
//      init {
//         System.loadLibrary("medicationtracker")
//      }
//    }

//#include <jni.h>
//#include "DbManager.h"
//
//JNIEXPORT jint
//extern "C" JNICALL Java_projects_medicationtracker_Settings_DbManager(JNIEnv *env, jobject thiz) {
//    return 0;
//}

#include <android/log.h>

extern "C" JNICALL void
Java_projects_medicationtracker_Settings_DbManager(JNIEnv *env, jobject thiz) {
    __android_log_print(ANDROID_LOG_DEFAULT, "CPP TAG", "HELLO");
}
