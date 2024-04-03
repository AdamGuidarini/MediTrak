#include "DbManager.h"
#include "DatabaseController.h"

#include <jni.h>
#include <android/log.h>
#include <string>
#include <map>
#include <cstdio>

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

jobject parseLocalDateTime(std::string dateString, JNIEnv* env) {
    jclass LocalDateTime = env->FindClass("java/time/LocalDateTime");
    jmethodID of = env->GetStaticMethodID(LocalDateTime, "of", "(IIIII)Ljava/time/LocalDateTime;");
    int dateParts[5];
    int i = 0;

    char* t = strtok(dateString.data(), ": -");

    while(t) {
        dateParts[i] = atoi(t);
        t = strtok(NULL, ": -");

        i++;
    }

    return env->CallStaticObjectMethod(
            LocalDateTime,
            of,
            dateParts[0],
            dateParts[1],
            dateParts[2],
            dateParts[3],
            dateParts[4]
    );
}

jobjectArray doseToJavaConverter(vector<Dose> doses, JNIEnv* env, jobject &jMedication) {
    jfieldID medDoses = env->GetFieldID(env->GetObjectClass(jMedication), "doses", "[Lprojects/medicationtracker/SimpleClasses/Dose;");
    jobjectArray jDoses = static_cast<jobjectArray>(env->GetObjectField(jMedication, medDoses));
    jclass jDose = env->GetObjectClass(env->GetObjectArrayElement(jDoses, 0));

    jDoses = env->NewObjectArray(doses.size(), jDose, NULL);

    jmethodID constructor = env->GetMethodID(
        jDose,
        "<init>",
        "(JJZLjava/time/LocalDateTime;Ljava/time/LocalDateTime;)V"
    );

    for (int i = 0; i < doses.size(); i++) {
        jobject dose = env->NewObject(
            jDose,
            constructor,
            doses.at(i).id,
            doses.at(i).medicationId,
            doses.at(i).taken,
            parseLocalDateTime(doses.at(i).timeTaken, env),
            parseLocalDateTime(doses.at(i).doseTime, env)
        );

        env->SetObjectArrayElement(jDoses, i, dose);
    }

    return jDoses;
}

void medicationToJavaConverter(Medication med, JNIEnv* env, jobject &jMedicationInstance) {
    jclass jMedication = env->GetObjectClass(jMedicationInstance);
    jclass LocalDateTime = env->FindClass("java/time/LocalDateTime");
    jobjectArray medTimes = env->NewObjectArray(med.times.size(), LocalDateTime, NULL);

    jmethodID setId = env->GetMethodID(jMedication, "setId", "(J)V");
    jmethodID setName = env->GetMethodID(jMedication, "setName", "(Ljava/lang/String;)V");
    jmethodID setFrequency = env->GetMethodID(jMedication, "setFrequency", "(I)V");
    jmethodID setDosage = env->GetMethodID(jMedication, "setDosage", "(I)V");
    jmethodID setDosageUnits = env->GetMethodID(jMedication, "setDosageUnits", "(Ljava/lang/String;)V");
    jmethodID setPatientName = env->GetMethodID(jMedication, "setPatientName", "(Ljava/lang/String;)V");
    jmethodID setStartDate = env->GetMethodID(jMedication, "setStartDate", "(Ljava/time/LocalDateTime;)V");
    jmethodID setAlias = env->GetMethodID(jMedication, "setAlias", "(Ljava/lang/String;)V");
    jmethodID setActiveStatus = env->GetMethodID(jMedication, "setActiveStatus", "(Z)V");
    jmethodID setParent = env->GetMethodID(jMedication, "setParent", "(Lprojects/medicationtracker/SimpleClasses/Medication;)V");
    jmethodID setDoses = env->GetMethodID(jMedication, "setDoses", "([Lprojects/medicationtracker/SimpleClasses/Dose;)V");
    jmethodID setTimes = env->GetMethodID(jMedication, "setTimes", "([Ljava/time/LocalDateTime;)V");

    env->CallVoidMethod(jMedicationInstance, setId, med.id);
    env->CallVoidMethod(jMedicationInstance, setName, env->NewStringUTF(med.medicationName.c_str()));
    env->CallVoidMethod(jMedicationInstance, setFrequency, jint(med.frequency));
    env->CallVoidMethod(jMedicationInstance, setDosage, jint(med.dosage));
    env->CallVoidMethod(jMedicationInstance, setDosageUnits, env->NewStringUTF(med.dosageUnit.c_str()));
    env->CallVoidMethod(jMedicationInstance, setPatientName, env->NewStringUTF(med.patientName.c_str()));
    env->CallVoidMethod(jMedicationInstance, setStartDate, parseLocalDateTime(med.startDate, env));
    env->CallVoidMethod(jMedicationInstance, setAlias, env->NewStringUTF(med.alias.c_str()));
    env->CallVoidMethod(jMedicationInstance, setActiveStatus, med.active);

    for (int i = 0; i < med.times.size(); i++) {
        std::string dateString = med.startDate.substr(0, med.startDate.find(" ")) + " " + med.times.at(i);

        env->SetObjectArrayElement(medTimes, i, parseLocalDateTime(dateString, env));
    }

    env->CallVoidMethod(jMedicationInstance, setTimes, medTimes);

    if (med.parent != nullptr) {
        jmethodID clone = env->GetMethodID(jMedication, "clone", "()Ljava/lang/Object;");
        jmethodID setChild = env->GetMethodID(jMedication, "setChild", "(Lprojects/medicationtracker/SimpleClasses/Medication;)V");
        jobject cloneMed = env->CallObjectMethod(jMedicationInstance, clone);

        env->CallVoidMethod(cloneMed, setId, med.id);
        env->CallVoidMethod(cloneMed, setChild, jMedicationInstance);

        medicationToJavaConverter(*med.parent, env, cloneMed);

        env->CallVoidMethod(jMedicationInstance, setParent, cloneMed);
    }

    auto jDoses = doseToJavaConverter(med.doses, env, jMedicationInstance);

    env->CallVoidMethod(jMedicationInstance, setDoses, jDoses);
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
        jstring file_contents,
        jobjectArray ignored_tables
) {
    std::string db = env->GetStringUTFChars(db_path, new jboolean(true));
    std::string fileContents = env->GetStringUTFChars(file_contents, new jboolean(true));
    std::vector<std::string> ignoredTbls;
    int len = env->GetArrayLength(ignored_tables);
    bool success = true;

    for (int i = 0; i < len; i++) {
        auto str = (jstring) (env->GetObjectArrayElement(ignored_tables, i));
        string rawString = env->GetStringUTFChars(str, JNI_FALSE);

        ignoredTbls.push_back(rawString);
    }

    DatabaseController controller(db);

    try {
        controller.importJSONString(fileContents, ignoredTbls);
    } catch (exception &e) {
        __android_log_write(ANDROID_LOG_ERROR, nullptr, e.what());

        success = false;
    }

    return success;
}

extern "C"
JNIEXPORT void JNICALL
Java_projects_medicationtracker_Helpers_NativeDbHelper_dbCreate(JNIEnv *env, jobject thiz, jstring db_path) {
    std::string db = env->GetStringUTFChars(db_path, new jboolean(true));

    try {
        DatabaseController dbController(db);
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
        if (tbl == dbController.SETTINGS_TABLE) {
            dbController.updateSettings(vals);
        }  else {
            dbController.update(tbl, vals, whereVals);
        }
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

extern "C"
JNIEXPORT jlong JNICALL
Java_projects_medicationtracker_Helpers_NativeDbHelper_delete(
        JNIEnv *env,
        jobject thiz,
        jstring db_path,
        jstring table,
        jobjectArray values) {

    std::string path = env->GetStringUTFChars(db_path, new jboolean(true));
    std::string tbl = env->GetStringUTFChars(table, new jboolean(true));

    DatabaseController dbController(path);

    try {
        dbController.deleteRecord(tbl, getValues(values, env));

        return true;
    } catch (exception &e) {
        return false;
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_projects_medicationtracker_Helpers_NativeDbHelper_getMedHistory(
        JNIEnv *env,
        jobject thiz,
        jstring db_path,
        jlong med_id,
        jobject medInstance
) {
    std::string path = env->GetStringUTFChars(db_path, new jboolean(true));

    DatabaseController dbController(path);

    medicationToJavaConverter(dbController.getMedication(med_id), env, medInstance);
}
