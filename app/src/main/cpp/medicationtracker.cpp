#include <jni.h>
#include <android/log.h>
#include "dbManager/DbManager.h"

extern "C" JNICALL void
Java_projects_medicationtracker_Settings_DbManager(
        JNIEnv *env, jobject thiz, jstring database
        ) {

    DbManager* manager = new DbManager(database);

    manager->open();
    manager->close();

    delete manager;
}
