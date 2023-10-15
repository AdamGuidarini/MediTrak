#include "DbManager.h"

#include <jni.h>
#include <android/log.h>

extern "C"
JNIEXPORT jboolean JNICALL
Java_projects_medicationtracker_Dialogs_BackupDestinationPicker_dbExporter(JNIEnv *env, jobject thiz,
                                                                          jstring database_name,
                                                                          jstring export_directory,
                                                                          jobjectArray ignoredTables) {
    std::string db = env->GetStringUTFChars(database_name, new jboolean(true));
    std::string exportDir = env->GetStringUTFChars(export_directory, new jboolean(true));
    std::vector<std::string> ignoredTbls;
    int len = env->GetArrayLength(ignoredTables);

    for (int i = 0; i < len; i++) {
        auto str = (jstring) (env->GetObjectArrayElement(ignoredTables, i));
        string rawString = env->GetStringUTFChars(str, JNI_FALSE);

        ignoredTbls.push_back(rawString);
    }

    auto* manager = new DbManager(db, true);

    try {
        manager->openDb();

        manager->exportData(exportDir);

        manager->closeDb();
    } catch (exception &e) {
        cerr << e.what() << endl;

        delete manager;

        return false;
    }

    delete manager;

    return true;
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_projects_medicationtracker_Settings_dbImporter(JNIEnv *env, jobject thiz, jstring db_path, jstring import_path,
                                                    jobjectArray ignoredTables) {
    std::string db = env->GetStringUTFChars(db_path, new jboolean(true));
    std::string importPath = env->GetStringUTFChars(import_path, new jboolean(true));
    std::vector<std::string> ignoredTbls;
    int len = env->GetArrayLength(ignoredTables);

    for (int i = 0; i < len; i++) {
        auto str = (jstring) (env->GetObjectArrayElement(ignoredTables, i));
        string rawString = env->GetStringUTFChars(str, JNI_FALSE);

        ignoredTbls.push_back(rawString);
    }

    DbManager dbManager(db, true);

    try {
        dbManager.openDb();

        dbManager.importData(importPath, ignoredTbls);

        dbManager.closeDb();
    } catch (exception &e) {
        cerr << e.what() <<endl;

        return false;
    }

    return true;
}