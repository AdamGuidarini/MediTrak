#include "DbManager/DbManager.h"

#include <jni.h>
#include <android/log.h>

extern "C" JNICALL void
Java_projects_medicationtracker_Settings_DbManager(
        JNIEnv *env, jobject thiz, jstring database
        ) {
    std::string db = env->GetStringUTFChars(database, new jboolean(true));
    auto* manager = new DbManager(db);

    manager->getAllRowFromAllTables();

    delete manager;
}
