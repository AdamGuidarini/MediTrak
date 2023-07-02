#include "DataExporter/DataExporter.h"

#include <jni.h>
#include <android/log.h>

extern "C" JNICALL void
Java_projects_medicationtracker_Settings_DataExporter(
        JNIEnv *env, jobject thiz, jstring database
        ) {
    std::string db = env->GetStringUTFChars(database, new jboolean(true));

    auto* exporter = new DataExporter(db);

    exporter->getDataFromTables();

    delete exporter;
}
