#include "DbManager.h"
#include "DatabaseController.h"

#include <jni.h>
#include <android/log.h>
#include <android/file_descriptor_jni.h>
#include <string>
#include <map>
#include <sys/stat.h>
#include <unistd.h>

std::map<std::string, std::string> getValues(jobjectArray arr, JNIEnv *env) {
    const jclass pair = env->FindClass("android/util/Pair");

    _jfieldID *const firstFieldId = env->GetFieldID(pair, "first", "Ljava/lang/Object;");
    _jfieldID *const secondFieldId = env->GetFieldID(pair, "second", "Ljava/lang/Object;");

    std::map<std::string, std::string> vals;

    for (int i = 0; i < env->GetArrayLength(arr); i++) {
        jstring firstField = (jstring) env->GetObjectField(env->GetObjectArrayElement(arr, i),
                                                           firstFieldId);
        jstring secondField = (jstring) env->GetObjectField(env->GetObjectArrayElement(arr, i),
                                                            secondFieldId);

        std::string first = env->GetStringUTFChars(firstField, new jboolean(true));
        std::string second = env->GetStringUTFChars(secondField, new jboolean(true));

        vals.insert({first, second});
    }

    return vals;
}

jobject doseToJavaConverter(const Dose &dose, JNIEnv *env, jobject &jMedication, jclass jDose) {
    jfieldID medDoses = env->GetFieldID(env->GetObjectClass(jMedication), "doses",
                                        "[Lprojects/medicationtracker/Models/Dose;");
    auto jDoses = static_cast<jobjectArray>(env->GetObjectField(jMedication, medDoses));

    jmethodID setOverrideDoseAmount = env->GetMethodID(jDose, "setOverrideDoseAmount", "(F)V");
    jmethodID setOverrideDoseUnit = env->GetMethodID(jDose, "setOverrideDoseUnit",
                                                     "(Ljava/lang/String;)V");

    jmethodID setDoseId = env->GetMethodID(jDose, "setDoseId", "(J)V");
    jmethodID setTaken = env->GetMethodID(jDose, "setTaken", "(Z)V");
    jmethodID setMedId = env->GetMethodID(jDose, "setMedId", "(J)V");
    jmethodID setTimeTaken = env->GetMethodID(jDose, "setTimeTaken", "(Ljava/lang/String;)V");
    jmethodID setDoseTime = env->GetMethodID(jDose, "setDoseTime", "(Ljava/lang/String;)V");

    jmethodID constructorDefault = env->GetMethodID(
            jDose,
            "<init>",
            "()V"
    );

    jobject javaDose = env->NewObject(jDose, constructorDefault);

    if (dose.id == -1) {
        return javaDose;
    }

    env->CallVoidMethod(javaDose, setDoseId, dose.id);
    env->CallVoidMethod(javaDose, setTaken, dose.taken);
    env->CallVoidMethod(javaDose, setMedId, dose.medicationId);
    env->CallVoidMethod(javaDose, setDoseTime, env->NewStringUTF(dose.doseTime.c_str()));

    if (dose.timeTaken.length() > 0) {
        env->CallVoidMethod(javaDose, setTimeTaken, env->NewStringUTF(dose.timeTaken.c_str()));
    }

    if (dose.overrideDoseAmount != -1) {
        env->CallVoidMethod(javaDose, setOverrideDoseAmount, dose.overrideDoseAmount);
    }

    if (!dose.overrideDoseUnit.empty()) {
        env->CallVoidMethod(
                javaDose,
                setOverrideDoseUnit,
                env->NewStringUTF(dose.overrideDoseUnit.c_str())
        );
    }
    return javaDose;
}

Dose javaDoseToNativeDoseConverter(jobject jDose, JNIEnv *env) {
    jclass jDoseClass = env->GetObjectClass(jDose);
    jmethodID getDoseId = env->GetMethodID(jDoseClass, "getDoseId", "()J");
    jmethodID getMedId = env->GetMethodID(jDoseClass, "getMedId", "()J");
    jmethodID isTaken = env->GetMethodID(jDoseClass, "isTaken", "()Z");
    jmethodID getTimeTakenText = env->GetMethodID(jDoseClass, "getTimeTakenText",
                                                  "()Ljava/lang/String;");
    jmethodID getDoseTimeText = env->GetMethodID(jDoseClass, "getDoseTimeText",
                                                 "()Ljava/lang/String;");
    jmethodID getOverrideDoseAmount = env->GetMethodID(jDoseClass, "getOverrideDoseAmount", "()F");
    jmethodID getOverrideDoseUnit = env->GetMethodID(jDoseClass, "getOverrideDoseUnit",
                                                     "()Ljava/lang/String;");

    return Dose(
            env->CallLongMethod(jDose, getDoseId),
            env->CallLongMethod(jDose, getMedId),
            env->CallBooleanMethod(jDose, isTaken),
            env->GetStringUTFChars((jstring) env->CallObjectMethod(jDose, getDoseTimeText),
                                   new jboolean(false)),
            env->GetStringUTFChars((jstring) env->CallObjectMethod(jDose, getTimeTakenText),
                                   new jboolean(false)),
            env->CallFloatMethod(jDose, getOverrideDoseAmount),
            env->GetStringUTFChars((jstring) env->CallObjectMethod(jDose, getOverrideDoseUnit),
                                   new jboolean(false))
    );
}

jobject medicationToJavaConverter(Medication med, JNIEnv *env, jclass jMedication, jclass jDoseClass) {
    jclass String = env->FindClass("java/lang/String");
    jobjectArray medTimes = env->NewObjectArray(med.times.size(), String, NULL);
    jmethodID medConstructor = env->GetMethodID(
            jMedication,
            "<init>",
            "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;JIFLjava/lang/String;)V"
    );

    for (int i = 0; i < med.times.size(); i++) {
        std::string dateString =
                med.startDate.substr(0, med.startDate.find(" ")) + " " + med.times.at(i);

        env->SetObjectArrayElement(medTimes, i, env->NewStringUTF(dateString.c_str()));
    }

    jobject jMedicationInstance = env->NewObject(
            jMedication,
            medConstructor,
            env->NewStringUTF(med.medicationName.c_str()),
            env->NewStringUTF(med.patientName.c_str()),
            env->NewStringUTF(med.dosageUnit.c_str()),
            medTimes,
            env->NewStringUTF(med.startDate.c_str()),
            med.id,
            jint(med.frequency),
            med.dosage,
            env->NewStringUTF(med.alias.c_str())
    );

    jmethodID setActiveStatus = env->GetMethodID(jMedication, "setActiveStatus", "(Z)V");
    jmethodID setParent = env->GetMethodID(jMedication, "setParent",
                                           "(Lprojects/medicationtracker/Models/Medication;)V");
    jmethodID setDoses = env->GetMethodID(jMedication, "setDoses",
                                          "([Lprojects/medicationtracker/Models/Dose;)V");
    jmethodID setDoseAmount = env->GetMethodID(jMedication, "setDoseAmount", "(I)V");
    jmethodID setEndDate = env->GetMethodID(jMedication, "setEndDate", "(Ljava/lang/String;)V");
    jmethodID setNotifyWhenRemaining = env->GetMethodID(
            jMedication,
            "setNotifyWhenRemaining",
            "(I)V"
    );

    env->CallVoidMethod(jMedicationInstance, setActiveStatus, med.active);
    env->CallVoidMethod(jMedicationInstance, setDoseAmount, med.quantity);
    env->CallVoidMethod(jMedicationInstance, setEndDate, env->NewStringUTF(med.endDate.c_str()));
    env->CallVoidMethod(jMedicationInstance, setNotifyWhenRemaining, med.notifyWhenRemainingAmount);

    if (med.parent != nullptr) {
        jmethodID setChild = env->GetMethodID(jMedication, "setChild",
                                              "(Lprojects/medicationtracker/Models/Medication;)V");
        jobject medParent = medicationToJavaConverter(*med.parent, env, jMedication, jDoseClass);

        env->CallVoidMethod(medParent, setChild, jMedicationInstance);
        env->CallVoidMethod(jMedicationInstance, setParent, medParent);
    }

    if (!med.doses.empty()) {//
        jfieldID medDoses = env->GetFieldID(env->GetObjectClass(jMedicationInstance), "doses",
                                            "[Lprojects/medicationtracker/Models/Dose;");
        jobjectArray jDoses = static_cast<jobjectArray>(env->GetObjectField(jMedicationInstance,
                                                                            medDoses));
        jDoses = env->NewObjectArray(med.doses.size(), jDoseClass, NULL);

        for (int i = 0; i < med.doses.size(); i++) {
            string msg = "In dose loop. Index: ";\

            msg += to_string(i).c_str();

            __android_log_write(ANDROID_LOG_INFO, nullptr, msg.c_str());

            env->SetObjectArrayElement(jDoses, i,
                                       doseToJavaConverter(med.doses.at(i), env, jMedicationInstance, jDoseClass));
        }

        env->CallVoidMethod(jMedicationInstance, setDoses, jDoses);
    }

    return jMedicationInstance;
}

Notification javaNotificationToNativeNotificationMapper(jobject notification, JNIEnv *env) {
    jclass jNotificationClass = env->GetObjectClass(notification);
    jmethodID getId = env->GetMethodID(jNotificationClass, "getId", "()J");
    jmethodID getMedId = env->GetMethodID(jNotificationClass, "getMedId", "()J");
    jmethodID getNotificationId = env->GetMethodID(jNotificationClass, "getNotificationId", "()J");
    jmethodID getDoseTime = env->GetMethodID(jNotificationClass, "getDoseTimeString",
                                             "()Ljava/lang/String;");

    std::string timeString = env->GetStringUTFChars(
            (jstring) env->CallObjectMethod(notification, getDoseTime),
            new jboolean(true)
    );

    return Notification(
            env->CallLongMethod(notification, getId),
            env->CallLongMethod(notification, getMedId),
            env->CallLongMethod(notification, getNotificationId),
            timeString
    );
}

jobject nativeNotificationToJavaNotificationConverter(JNIEnv *env, Notification notification,
                                                      jclass notificationClass) {
    jmethodID constructor = env->GetMethodID(notificationClass, "<init>",
                                             "(JJJLjava/lang/String;)V");

    return env->NewObject(
            notificationClass,
            constructor,
            notification.id,
            notification.medId,
            notification.notificationId,
            env->NewStringUTF(notification.doseTime.c_str())
    );
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
        auto rawString = env->GetStringUTFChars(str, JNI_FALSE);

        ignoredTbls.push_back(rawString);
    }

    DatabaseController controller(db);

    try {
        controller.exportJSON(exportDir, ignoredTbls);
    } catch (exception &e) {
        __android_log_write(ANDROID_LOG_ERROR, nullptr, e.what());

        return JNI_FALSE;
    }

    return JNI_TRUE;
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
    auto success = JNI_TRUE;

    for (int i = 0; i < len; i++) {
        auto str = (jstring) (env->GetObjectArrayElement(ignored_tables, i));
        auto rawString = env->GetStringUTFChars(str, JNI_FALSE);

        ignoredTbls.push_back(rawString);
    }

    DatabaseController controller(db);

    try {
        controller.importJSONString(fileContents, ignoredTbls);
        controller.repairImportErrors();
    } catch (exception &e) {
        __android_log_write(ANDROID_LOG_ERROR, nullptr, e.what());

        success = JNI_FALSE;
    }

    return success;
}

extern "C"
JNIEXPORT void JNICALL
Java_projects_medicationtracker_Helpers_NativeDbHelper_dbCreate(
        JNIEnv *env,
        jobject thiz,
        jstring db_path
) {
    std::string db = env->GetStringUTFChars(db_path, new jboolean(true));

    try {
        DatabaseController dbController(db);
    } catch (exception &e) {
        __android_log_write(ANDROID_LOG_ERROR, nullptr, e.what());
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_projects_medicationtracker_Helpers_NativeDbHelper_dbUpgrade(
        JNIEnv *env,
        jobject thiz,
        jstring db_path,
        jint version
) {
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
        } else {
            dbController.update(tbl, vals, whereVals);
        }
    } catch (exception &e) {
        __android_log_write(ANDROID_LOG_ERROR, nullptr, e.what());

        return JNI_FALSE;
    }

    return JNI_TRUE;
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
JNIEXPORT jobject JNICALL
Java_projects_medicationtracker_Helpers_NativeDbHelper_getMedHistory(
        JNIEnv *env,
        jobject thiz,
        jstring db_path,
        jlong med_id,
        jclass medClass,
        jclass doseClass
) {
    std::string path = env->GetStringUTFChars(db_path, new jboolean(true));

    DatabaseController dbController(path);

    try {
        return medicationToJavaConverter(dbController.getMedicationHistory(med_id), env, medClass, doseClass);
    } catch (exception e) {
        __android_log_write(ANDROID_LOG_ERROR, nullptr, "Error retrieving history");

        return nullptr;
    }
}

extern "C"
JNIEXPORT jobject JNICALL
        Java_projects_medicationtracker_Helpers_NativeDbHelper_getMedicationById(
        JNIEnv *env,
        jobject thiz,
        jstring db_path,
        jlong med_id,
        jclass medClass,
        jclass doseClass
) {
    std::string path = env->GetStringUTFChars(db_path, new jboolean(true));

    DatabaseController dbController(path);

    try {
    return medicationToJavaConverter(dbController.getMedication(med_id), env, medClass, doseClass);
    } catch (exception e) {
    __android_log_write(ANDROID_LOG_ERROR, nullptr, "Error retrieving history");

    return nullptr;
    }
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_projects_medicationtracker_Helpers_NativeDbHelper_exportMedHistory(
        JNIEnv *env,
        jobject thiz,
        jstring db_path,
        jstring export_path,
        jobjectArray data
) {
    const jclass pair = env->FindClass("android/util/Pair");
    std::string dbPath = env->GetStringUTFChars(db_path, new jboolean(true));
    std::string path = env->GetStringUTFChars(export_path, new jboolean(true));

    DatabaseController controller(dbPath);

    std::map<std::string, std::vector<std::string>> exportData;

    _jfieldID *const firstFieldId = env->GetFieldID(pair, "first", "Ljava/lang/Object;");
    _jfieldID *const secondFieldId = env->GetFieldID(pair, "second", "Ljava/lang/Object;");

    for (int i = 0; i < env->GetArrayLength(data); i++) {
        jstring key = (jstring) env->GetObjectField(env->GetObjectArrayElement(data, i),
                                                    firstFieldId);
        jobjectArray values = (jobjectArray) env->GetObjectField(
                env->GetObjectArrayElement(data, i), secondFieldId);
        std::vector<std::string> vals;

        for (int j = 0; j < env->GetArrayLength(values); j++) {
            std::string val = env->GetStringUTFChars(
                    (jstring) env->GetObjectArrayElement(values, j),
                    new jboolean(true)
            );

            vals.push_back(val);
        }

        exportData.insert({env->GetStringUTFChars(key, new jboolean(true)), vals});
    }

    try {
        controller.exportCsv(path, exportData);
    } catch (exception &e) {
        __android_log_write(ANDROID_LOG_ERROR, nullptr, e.what());

        return JNI_FALSE;
    }

    return JNI_TRUE;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_projects_medicationtracker_Helpers_NativeDbHelper_findDose(
        JNIEnv *env, jobject thiz, jstring db_path, jlong medication_id, jstring dose_time,
        jobject medication,
        jclass jDoseClass
) {
    std::string dbPath = env->GetStringUTFChars(db_path, new jboolean(true));
    DatabaseController controller(dbPath);
    std::string doseTime = env->GetStringUTFChars(dose_time, new jboolean(true));

    Dose *dose = controller.findDose(medication_id, doseTime);

    if (dose == nullptr) {
        dose = new Dose();
    }

    jobject jDose = doseToJavaConverter(*dose, env, medication, jDoseClass);

    delete dose;

    return jDose;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_projects_medicationtracker_Helpers_NativeDbHelper_getDoseById(
        JNIEnv *env,
        jobject thiz,
        jstring db_path,
        jlong dose_id,
        jobject medication,
        jclass jDoseClass
) {
    std::string dbPath = env->GetStringUTFChars(db_path, new jboolean(true));
    DatabaseController controller(dbPath);

    Dose *d;

    try {
        d = controller.getDoseById(dose_id);
    } catch (exception &e) {
        delete d;

        return nullptr;
    }

    auto jDose = doseToJavaConverter(*d, env, medication, jDoseClass);

    delete d;

    return jDose;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_projects_medicationtracker_Helpers_NativeDbHelper_updateDose(
        JNIEnv *env,
        jobject thiz,
        jstring db_path,
        jobject dose
) {
    std::string dbPath = env->GetStringUTFChars(db_path, new jboolean(true));
    DatabaseController controller(dbPath);

    Dose nDose = javaDoseToNativeDoseConverter(dose, env);

    return controller.updateDose(nDose) ? JNI_TRUE : JNI_FALSE;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_projects_medicationtracker_Helpers_NativeDbHelper_stashNotification(
        JNIEnv *env,
        jobject thiz,
        jstring db_path,
        jobject notification
) {
    std::string dbPath = env->GetStringUTFChars(db_path, new jboolean(true));
    DatabaseController controller(dbPath);

    Notification notificationToStash = javaNotificationToNativeNotificationMapper(notification,
                                                                                  env);

    try {
        return controller.stashNotification(notificationToStash) ? JNI_TRUE : JNI_FALSE;
    } catch (exception &e) {
        __android_log_write(ANDROID_LOG_ERROR, nullptr, e.what());

        return JNI_FALSE;
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_projects_medicationtracker_Helpers_NativeDbHelper_deleteNotification(
        JNIEnv *env,
        jobject thiz,
        jstring db_path,
        jlong notificationId
) {
    std::string dbPath = env->GetStringUTFChars(db_path, new jboolean(true));
    DatabaseController controller(dbPath);

    try {
        controller.deleteNotification(notificationId);
    } catch (exception &e) {
        std::string err =
                "Notification deletion failed. NotificationId: " + to_string(notificationId);
        __android_log_write(ANDROID_LOG_ERROR, nullptr, err.c_str());
    }
}

extern "C"
JNIEXPORT jobjectArray JNICALL
Java_projects_medicationtracker_Helpers_NativeDbHelper_getNotifications(
        JNIEnv *env,
        jobject thiz,
        jstring db_path,
        jclass jNotificationClass
) {
    std::string dbPath = env->GetStringUTFChars(db_path, new jboolean(true));
    DatabaseController controller(dbPath);

    std::vector<Notification> notifications;

    try {
        notifications = controller.getStashedNotifications();
    } catch (exception &e) {
        __android_log_write(
                ANDROID_LOG_ERROR,
                nullptr,
                "Failed to retrieve notifications"
        );

        notifications = {};
    }

    jobjectArray jNotifications = env->NewObjectArray(
            notifications.size(),
            jNotificationClass,
            nullptr
    );

    for (int i = 0; i < notifications.size(); i++) {
        jobject noti = nativeNotificationToJavaNotificationConverter(
                env,
                notifications.at(i),
                jNotificationClass
        );

        env->SetObjectArrayElement(
                jNotifications,
                i,
                noti
        );
    }

    return jNotifications;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_projects_medicationtracker_Helpers_NativeDbHelper_addDose(
        JNIEnv *env,
        jobject thiz,
        jstring db_path,
        jlong medId,
        jstring scheduled_time,
        jstring taken_time,
        jboolean taken
) {
    std::string dbPath = env->GetStringUTFChars(db_path, new jboolean(true));
    std::string scheduledTime = env->GetStringUTFChars(scheduled_time, new jboolean(true));
    std::string takenTime = env->GetStringUTFChars(taken_time, new jboolean(true));
    long rowId = -1;

    DatabaseController controller(dbPath);

    try {
        rowId = controller.addDose(medId, scheduledTime, takenTime, taken);
    } catch (exception &e) {
        std::string err =
                "Could not create dose for medication: "
                + to_string(medId)
                + "at time: "
                + scheduledTime;

        __android_log_write(ANDROID_LOG_ERROR, nullptr, err.c_str());
    }

    return rowId;
}

extern "C"
JNIEXPORT void JNICALL
Java_projects_medicationtracker_Helpers_NativeDbHelper_updateSettings(
        JNIEnv *env,
        jobject thiz,
        jstring db_path,
        jobjectArray settings
) {
    std::string dbPath = env->GetStringUTFChars(db_path, new jboolean(true));
    std::map<std::string, std::string> opts = getValues(settings, env);

    DatabaseController controller(dbPath);

    try {
        controller.updateSettings(opts);
    } catch (exception& e) {
        auto err = "Unable to update settings";

        __android_log_write(ANDROID_LOG_ERROR, "SETTINGS UPDATE", err);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_projects_medicationtracker_Helpers_NativeDbHelper_deleteNotificationsByMedId(
    JNIEnv *env,
    jobject thiz,
    jstring db_path,
    jlong medicationid
) {
    std::string dbPath = env->GetStringUTFChars(db_path, new jboolean(true));

    DatabaseController controller(dbPath);

    try {
        controller.deleteNotificationsByMedicationId(medicationid);
    } catch (exception& e) {
        auto err = "Unable to delete notifications for medication: " + to_string(medicationid);

        __android_log_write(ANDROID_LOG_ERROR, "SETTINGS UPDATE", err.c_str());
    }
}

extern "C"
JNIEXPORT jobject JNICALL
Java_projects_medicationtracker_Helpers_NativeDbHelper_getSettings(
        JNIEnv *env,
        jobject thiz,
        jstring db_path
) {
    std::string dbPath = env->GetStringUTFChars(db_path, new jboolean(true));

    DatabaseController controller(dbPath);
    Table* settings = controller.getSettings();
    jobject jSettings;
    jclass jBundle = env->FindClass("android/os/Bundle");

    jmethodID bundleConstructor = env->GetMethodID(
        jBundle, "<init>", "()V"
    );
    jmethodID putString = env->GetMethodID(
            jBundle, "putString", "(Ljava/lang/String;Ljava/lang/String;)V"
    );
    jmethodID putInt = env->GetMethodID(
            jBundle, "putInt", "(Ljava/lang/String;I)V"
    );
    jmethodID putBool = env->GetMethodID(
            jBundle, "putBoolean", "(Ljava/lang/String;Z)V"
    );

    jSettings = env->NewObject(jBundle, bundleConstructor);

    const std::string stringKeys[] = {
            controller.THEME,
            controller.DATE_FORMAT,
            controller.TIME_FORMAT,
            controller.EXPORT_START,
            controller.EXPORT_FILE_NAME
    };

    const std::string intKeys[] = {
            controller.TIME_BEFORE_DOSE,
            controller.EXPORT_FREQUENCY
    };

    const std::string boolKeys[] = {
            controller.ENABLE_NOTIFICATIONS,
            controller.SEEN_NOTIFICATION_REQUEST,
            controller.SEEN_SCHEDULE_EXACT_REQUEST,
            controller.AGREED_TO_TERMS
    };

    for (const auto& sKey : stringKeys) {
        env->CallVoidMethod(
                jSettings,
                putString,
                env->NewStringUTF(sKey.c_str()),
                env->NewStringUTF(settings->getItem(sKey).c_str())
        );
    }

    for (const auto& iKey : intKeys) {
        env->CallVoidMethod(
                jSettings,
                putInt,
                env->NewStringUTF(iKey.c_str()),
                stoi(settings->getItem(iKey))
        );
    }

    for (const auto& bKey : boolKeys) {
        env->CallVoidMethod(
                jSettings,
                putBool,
                env->NewStringUTF(bKey.c_str()),
                settings->getItem(bKey) == "1"
        );
    }

    delete settings;

    return jSettings;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_projects_medicationtracker_Helpers_NativeDbHelper_canWriteFile(
        JNIEnv *env,
        jobject thiz,
        jstring test_path
) {
    struct stat fileStat;
    const char* pathChars = env->GetStringUTFChars(test_path, nullptr);

    if (pathChars == nullptr) {
        return JNI_FALSE;
    }

    std::string path(pathChars);

    env->ReleaseStringUTFChars(test_path, pathChars);

    if (stat(path.c_str(), &fileStat) != 0) {
        return errno == ENOENT ? JNI_TRUE : JNI_FALSE;
    }

    return access(path.c_str(), W_OK) == 0 ? JNI_TRUE : JNI_FALSE;
}