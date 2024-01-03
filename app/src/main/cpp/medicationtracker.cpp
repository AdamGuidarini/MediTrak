#include "DbManager.h"
#include "DatabaseController.h"

#include <jni.h>
#include <android/log.h>
#include <string>
#include <map>

std::map<std::string, std::string> getValues(jobjectArray arr, JNIEnv *env) {
    const jclass pair = env->FindClass("android/util/Pair");

    _jfieldID *const firstFieldId = env->GetFieldID(pair, "first", "Ljava/lang/Object;");
    _jfieldID *const secondFieldId = env->GetFieldID(pair, "second", "Ljava/lang/Object;");

    map<string, string> vals;

    for (int i = 0; i < env->GetArrayLength(arr); i++) {
        jstring firstField = (jstring) env->GetObjectField(env->GetObjectArrayElement(arr, i), firstFieldId);
        jstring secondField = (jstring) env->GetObjectField(env->GetObjectArrayElement(arr, i), secondFieldId);

        std::string first = env->GetStringUTFChars(firstField, new jboolean(true));
        std::string second = env->GetStringUTFChars(secondField, new jboolean(true));

        vals.insert({ first, second });
    }

    return vals;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_projects_medicationtracker_Helpers_NativeDbHelper_dbExporter(
        JNIEnv *env,
        jobject thiz,
        jstring database_name,
        jstring export_directory,
        jobjectArray ignoredTables
) {
    std::string db = env->GetStringUTFChars(database_name, new jboolean(true));
    std::string exportDir = env->GetStringUTFChars(export_directory, new jboolean(true));
    std::vector<std::string> ignoredTbls;
    int len = env->GetArrayLength(ignoredTables);

    for (int i = 0; i < len; i++) {
        auto str = (jstring) (env->GetObjectArrayElement(ignoredTables, i));
        string rawString = env->GetStringUTFChars(str, JNI_FALSE);

        ignoredTbls.push_back(rawString);
    }

    DatabaseController controller(db);

    try {
        controller.exportJSON(exportDir, ignoredTbls);
    } catch (exception& e) {
        __android_log_write(ANDROID_LOG_ERROR, nullptr, e.what());

        return false;
    }

    return true;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_projects_medicationtracker_Helpers_NativeDbHelper_dbImporter(
        JNIEnv *env,
        jobject thiz,
        jstring db_path,
        jstring import_path,
        jobjectArray ignored_tables
) {
    std::string db = env->GetStringUTFChars(db_path, new jboolean(true));
    std::string importPath = env->GetStringUTFChars(import_path, new jboolean(true));
    std::vector<std::string> ignoredTbls;
    int len = env->GetArrayLength(ignored_tables);

    for (int i = 0; i < len; i++) {
        auto str = (jstring) (env->GetObjectArrayElement(ignored_tables, i));
        string rawString = env->GetStringUTFChars(str, JNI_FALSE);

        ignoredTbls.push_back(rawString);
    }

    DatabaseController controller(db);

    try {
        controller.importJSON(importPath, ignoredTbls);
    } catch (exception &e) {
        __android_log_write(ANDROID_LOG_ERROR, nullptr, e.what());

        return false;
    }

    return true;
}

extern "C"
JNIEXPORT void JNICALL
Java_projects_medicationtracker_Helpers_NativeDbHelper_dbCreate(JNIEnv *env, jobject thiz, jstring db_path) {
    std::string db = env->GetStringUTFChars(db_path, new jboolean(true));

    try {
        DatabaseController dbController(db);
        dbController.create();
    } catch (exception &e) {
        __android_log_write(ANDROID_LOG_ERROR, nullptr, e.what());
    }
}
extern "C"
JNIEXPORT void JNICALL
Java_projects_medicationtracker_Helpers_NativeDbHelper_dbUpgrade(JNIEnv *env, jobject thiz, jstring db_path, jint version) {
    std::string db = env->GetStringUTFChars(db_path, new jboolean(true));

    try {
        DatabaseController dbController(db);
        dbController.upgrade(static_cast<int>(version));
    } catch (exception &e) {
        __android_log_write(ANDROID_LOG_ERROR, nullptr, e.what());
    }
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_projects_medicationtracker_Helpers_NativeDbHelper_update(
        JNIEnv *env,
        jobject thiz,
        jstring db_path,
        jstring table,
        jobjectArray values,
        jobjectArray where
) {
    std::string path = env->GetStringUTFChars(db_path, new jboolean(true));
    std::string tbl = env->GetStringUTFChars(table, new jboolean(true));

    std::map<std::string, std::string> vals = getValues(values, env);
    std::map<std::string, std::string> whereVals = getValues(where, env);

    DatabaseController dbController(path);

    try {
        dbController.update(tbl, vals, whereVals);
    } catch (exception &e) {
        __android_log_write(ANDROID_LOG_ERROR, nullptr, e.what());

        return false;
    }

    return true;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_projects_medicationtracker_Helpers_NativeDbHelper_insert(
        JNIEnv *env,
        jobject thiz,
        jstring db_path,
        jstring table,
        jobjectArray values
) {
    std::string path = env->GetStringUTFChars(db_path, new jboolean(true));
    std::string tbl = env->GetStringUTFChars(table, new jboolean(true));

    DatabaseController dbController(path);

    try {
        return dbController.insert(tbl, getValues(values, env));
    } catch (exception &e) {
        __android_log_write(ANDROID_LOG_ERROR, nullptr, e.what());

        return -1;
    }
}