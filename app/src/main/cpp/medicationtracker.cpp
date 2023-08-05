#include "DbManager.h"

#include <jni.h>
#include <android/log.h>

extern "C" JNICALL void
Java_projects_medicationtracker_Settings_DbManager(
        JNIEnv *env, jobject thiz, jstring database, jstring exportPath
        ) {
    std::string db = env->GetStringUTFChars(database, new jboolean(true));
    std::string exportDir = env->GetStringUTFChars(exportPath, new jboolean(true));

    time_t date = time(nullptr);
    tm* now = localtime(&date);
//    std::string fileName = "/meditrak_"
//                      + to_string(now->tm_year) + "_"
//                      + to_string(now->tm_mon) + "_"
//                      + to_string(now->tm_mday) + ".json";

    auto* manager = new DbManager(db, true);

    manager->openDb();

    manager->exportData(exportDir);

    manager->closeDb();

    delete manager;
}
