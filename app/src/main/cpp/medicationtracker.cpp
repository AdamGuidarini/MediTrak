#include "DbManager.h"

#include <jni.h>
#include <android/log.h>

extern "C"
JNIEXPORT jboolean JNICALL
Java_projects_medicationtracker_Dialogs_BackupDestinationPicker_DbManager(JNIEnv *env, jobject thiz,
                                                                          jstring database_name,
                                                                          jstring export_directory) {
    std::string db = env->GetStringUTFChars(database_name, new jboolean(true));
    std::string exportDir = env->GetStringUTFChars(export_directory, new jboolean(true));

    auto* manager = new DbManager(db, true);

    manager->openDb();

    manager->exportData(exportDir);

    manager->closeDb();

    delete manager;

    return true;
}