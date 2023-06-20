#include <jni.h>
#include <android/log.h>
#include "dbManager/DbManager.h"

extern "C" JNICALL void
Java_projects_medicationtracker_Settings_DbManager(
        JNIEnv *env, jobject thiz, jstring database
        ) {
    std::string db = env->GetStringUTFChars(database, new jboolean(true));

    __android_log_print(ANDROID_LOG_INFO, "DB NAME", "%s", db.c_str());

    auto* manager = new DbManager(db);

    manager->open();
    manager->read();
    manager->close();

    delete manager;
}
