#include "DbManager/DbManager.h"

#include <jni.h>
#include <android/log.h>

extern "C" JNICALL void
Java_projects_medicationtracker_Settings_DbManager(
        JNIEnv *env, jobject thiz, jstring database, jstring exportPath
        ) {
    std::string db = env->GetStringUTFChars(database, new jboolean(true));
    std::string exportDir = env->GetStringUTFChars(exportPath, new jboolean(true));

    auto* manager = new DbManager(db);

    manager->openDb();

//    manager->getAllRowFromAllTables();

    manager->exportData(exportDir);

    manager->closeDb();

    delete manager;
}
