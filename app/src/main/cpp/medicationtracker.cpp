#include "DbManager.h"
#include "DatabaseController.h"

#include <jni.h>
#include <android/log.h>
#include <android/file_descriptor_jni.h>
#include <string>
#include <map>
#include <sys/stat.h>
#include <unistd.h>

/**
 * RAII wrapper for GetStringUTFChars - releases the chars on destruction.
 */
class JniString {
public:
    JniString(JNIEnv *env, jstring jstr) : env_(env), jstr_(jstr) {
        chars_ = env_->GetStringUTFChars(jstr_, nullptr);
    }

    ~JniString() {
        if (chars_ != nullptr) {
            env_->ReleaseStringUTFChars(jstr_, chars_);
        }
    }

    JniString(const JniString &) = delete;
    JniString &operator=(const JniString &) = delete;

    const char *c_str() const { return chars_; }
    std::string toStdString() const { return chars_ != nullptr ? std::string(chars_) : std::string(); }

private:
    JNIEnv *env_;
    jstring jstr_;
    const char *chars_;
};

/**
 * RAII wrapper for JNI local references - deletes the ref on destruction unless released.
 */
class LocalRef {
public:
    LocalRef(JNIEnv *env, jobject ref) : env_(env), ref_(ref) {}

    ~LocalRef() {
        if (ref_ != nullptr) {
            env_->DeleteLocalRef(ref_);
        }
    }

    LocalRef(const LocalRef &) = delete;
    LocalRef &operator=(const LocalRef &) = delete;

    template<typename T = jobject>
    T get() const { return reinterpret_cast<T>(ref_); }

    jobject release() {
        jobject r = ref_;
        ref_ = nullptr;
        return r;
    }

private:
    JNIEnv *env_;
    jobject ref_;
};

std::map<std::string, std::string> getValues(jobjectArray arr, JNIEnv *env) {
    LocalRef pairClass(env, env->FindClass("android/util/Pair"));

    _jfieldID *const firstFieldId = env->GetFieldID(pairClass.get<jclass>(), "first", "Ljava/lang/Object;");
    _jfieldID *const secondFieldId = env->GetFieldID(pairClass.get<jclass>(), "second", "Ljava/lang/Object;");

    std::map<std::string, std::string> vals;

    for (int i = 0; i < env->GetArrayLength(arr); i++) {
        LocalRef element(env, env->GetObjectArrayElement(arr, i));
        LocalRef firstField(env, env->GetObjectField(element.get(), firstFieldId));
        LocalRef secondField(env, env->GetObjectField(element.get(), secondFieldId));

        JniString first(env, (jstring) firstField.get());
        JniString second(env, (jstring) secondField.get());

        vals.insert({first.toStdString(), second.toStdString()});
    }

    return vals;
}

jobject doseToJavaConverter(const Dose &dose, JNIEnv *env, jobject &jMedication, jclass jDose) {
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

    if (javaDose == nullptr) {
        return nullptr;
    }

    if (dose.id == -1) {
        return javaDose;
    }

    env->CallVoidMethod(javaDose, setDoseId, dose.id);
    env->CallVoidMethod(javaDose, setTaken, dose.taken);
    env->CallVoidMethod(javaDose, setMedId, dose.medicationId);

    {
        LocalRef doseTimeStr(env, env->NewStringUTF(dose.doseTime.c_str()));
        env->CallVoidMethod(javaDose, setDoseTime, doseTimeStr.get());
    }

    if (dose.timeTaken.length() > 0) {
        LocalRef timeTakenStr(env, env->NewStringUTF(dose.timeTaken.c_str()));
        env->CallVoidMethod(javaDose, setTimeTaken, timeTakenStr.get());
    }

    if (dose.overrideDoseAmount != -1) {
        env->CallVoidMethod(javaDose, setOverrideDoseAmount, dose.overrideDoseAmount);
    }

    if (!dose.overrideDoseUnit.empty()) {
        LocalRef unitStr(env, env->NewStringUTF(dose.overrideDoseUnit.c_str()));
        env->CallVoidMethod(javaDose, setOverrideDoseUnit, unitStr.get());
    }
    return javaDose;
}

Dose javaDoseToNativeDoseConverter(jobject jDose, JNIEnv *env) {
    LocalRef jDoseClass(env, env->GetObjectClass(jDose));
    jmethodID getDoseId = env->GetMethodID(jDoseClass.get<jclass>(), "getDoseId", "()J");
    jmethodID getMedId = env->GetMethodID(jDoseClass.get<jclass>(), "getMedId", "()J");
    jmethodID isTaken = env->GetMethodID(jDoseClass.get<jclass>(), "isTaken", "()Z");
    jmethodID getTimeTakenText = env->GetMethodID(jDoseClass.get<jclass>(), "getTimeTakenText",
                                                  "()Ljava/lang/String;");
    jmethodID getDoseTimeText = env->GetMethodID(jDoseClass.get<jclass>(), "getDoseTimeText",
                                                 "()Ljava/lang/String;");
    jmethodID getOverrideDoseAmount = env->GetMethodID(jDoseClass.get<jclass>(), "getOverrideDoseAmount", "()F");
    jmethodID getOverrideDoseUnit = env->GetMethodID(jDoseClass.get<jclass>(), "getOverrideDoseUnit",
                                                     "()Ljava/lang/String;");

    std::string doseTimeStr;
    std::string timeTakenStr;
    std::string overrideUnitStr;

    {
        LocalRef doseTimeRef(env, env->CallObjectMethod(jDose, getDoseTimeText));
        JniString doseTime(env, (jstring) doseTimeRef.get());
        doseTimeStr = doseTime.toStdString();
    }

    {
        LocalRef timeTakenRef(env, env->CallObjectMethod(jDose, getTimeTakenText));
        JniString timeTaken(env, (jstring) timeTakenRef.get());
        timeTakenStr = timeTaken.toStdString();
    }

    {
        LocalRef unitRef(env, env->CallObjectMethod(jDose, getOverrideDoseUnit));
        JniString unit(env, (jstring) unitRef.get());
        overrideUnitStr = unit.toStdString();
    }

    return Dose(
            env->CallLongMethod(jDose, getDoseId),
            env->CallLongMethod(jDose, getMedId),
            env->CallBooleanMethod(jDose, isTaken),
            doseTimeStr,
            timeTakenStr,
            env->CallFloatMethod(jDose, getOverrideDoseAmount),
            overrideUnitStr
    );
}

jobject medicationToJavaConverter(Medication med, JNIEnv *env, jclass jMedication, jclass jDoseClass) {
    LocalRef stringClass(env, env->FindClass("java/lang/String"));
    LocalRef medTimes(env, env->NewObjectArray(med.times.size(), stringClass.get<jclass>(), NULL));
    jmethodID medConstructor = env->GetMethodID(
            jMedication,
            "<init>",
            "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;JIFLjava/lang/String;)V"
    );

    for (int i = 0; i < med.times.size(); i++) {
        std::string dateString =
                med.startDate.substr(0, med.startDate.find(" ")) + " " + med.times.at(i);

        LocalRef timeStr(env, env->NewStringUTF(dateString.c_str()));
        env->SetObjectArrayElement(medTimes.get<jobjectArray>(), i, timeStr.get());
    }

    jobject jMedicationInstance;
    {
        LocalRef nameStr(env, env->NewStringUTF(med.medicationName.c_str()));
        LocalRef patientStr(env, env->NewStringUTF(med.patientName.c_str()));
        LocalRef unitStr(env, env->NewStringUTF(med.dosageUnit.c_str()));
        LocalRef startDateStr(env, env->NewStringUTF(med.startDate.c_str()));
        LocalRef aliasStr(env, env->NewStringUTF(med.alias.c_str()));

        jMedicationInstance = env->NewObject(
                jMedication,
                medConstructor,
                nameStr.get(),
                patientStr.get(),
                unitStr.get(),
                medTimes.get(),
                startDateStr.get(),
                med.id,
                jint(med.frequency),
                med.dosage,
                aliasStr.get()
        );
    }

    if (jMedicationInstance == nullptr) {
        return nullptr;
    }

    jmethodID setActiveStatus = env->GetMethodID(jMedication, "setActiveStatus", "(Z)V");
    jmethodID setParent = env->GetMethodID(jMedication, "setParent",
                                           "(Lprojects/medicationtracker/Models/Medication;)V");
    jmethodID setChild = env->GetMethodID(jMedication, "setChild",
                                          "(Lprojects/medicationtracker/Models/Medication;)V");
    jmethodID setDoses = env->GetMethodID(jMedication, "setDoses",
                                          "([Lprojects/medicationtracker/Models/Dose;)V");
    jmethodID setDoseAmount = env->GetMethodID(jMedication, "setDoseAmount", "(I)V");
    jmethodID setEndDate = env->GetMethodID(jMedication, "setEndDate", "(Ljava/lang/String;)V");
    jmethodID setInstructions = env->GetMethodID(jMedication, "setInstructions", "(Ljava/lang/String;)V");
    jmethodID setNotifyWhenRemaining = env->GetMethodID(
            jMedication,
            "setNotifyWhenRemaining",
            "(I)V"
    );

    env->CallVoidMethod(jMedicationInstance, setActiveStatus, med.active);
    env->CallVoidMethod(jMedicationInstance, setDoseAmount, med.quantity);

    {
        LocalRef endDateStr(env, env->NewStringUTF(med.endDate.c_str()));
        env->CallVoidMethod(jMedicationInstance, setEndDate, endDateStr.get());
    }

    env->CallVoidMethod(jMedicationInstance, setNotifyWhenRemaining, med.notifyWhenRemainingAmount);

    {
        LocalRef instructionsStr(env, env->NewStringUTF(med.instructions.c_str()));
        env->CallVoidMethod(jMedicationInstance, setInstructions, instructionsStr.get());
    }

    if (med.child != nullptr) {
        Medication child = *med.child;
        LocalRef childTimes(env, env->NewObjectArray(child.times.size(), stringClass.get<jclass>(), NULL));

        for (int i = 0; i < child.times.size(); i++) {
            std::string dateString =
                    child.startDate.substr(0, child.startDate.find(" ")) + " " + child.times.at(i);

            LocalRef timeStr(env, env->NewStringUTF(dateString.c_str()));
            env->SetObjectArrayElement(childTimes.get<jobjectArray>(), i, timeStr.get());
        }

        jobject childObj = nullptr;
        {
            LocalRef nameStr(env, env->NewStringUTF(child.medicationName.c_str()));
            LocalRef patientStr(env, env->NewStringUTF(child.patientName.c_str()));
            LocalRef unitStr(env, env->NewStringUTF(child.dosageUnit.c_str()));
            LocalRef startDateStr(env, env->NewStringUTF(child.startDate.c_str()));
            LocalRef aliasStr(env, env->NewStringUTF(child.alias.c_str()));

            childObj = env->NewObject(
                    jMedication,
                    medConstructor,
                    nameStr.get(),
                    patientStr.get(),
                    unitStr.get(),
                    childTimes.get(),
                    startDateStr.get(),
                    child.id,
                    jint(child.frequency),
                    child.dosage,
                    aliasStr.get()
            );
        }

        LocalRef jChild(env, childObj);

        if (jChild.get() != nullptr) {
            env->CallVoidMethod(jChild.get(), setActiveStatus, child.active);
            env->CallVoidMethod(jChild.get(), setDoseAmount, child.quantity);

            {
                LocalRef endDateStr(env, env->NewStringUTF(child.endDate.c_str()));
                env->CallVoidMethod(jChild.get(), setEndDate, endDateStr.get());
            }

            env->CallVoidMethod(jChild.get(), setNotifyWhenRemaining, child.notifyWhenRemainingAmount);
            env->CallVoidMethod(jChild.get(), setParent, jMedicationInstance);
            env->CallVoidMethod(jMedicationInstance, setChild, jChild.get());
        }
    }

    if (auto parentPtr = med.parent.lock()) {
        LocalRef medParent(env, medicationToJavaConverter(*parentPtr, env, jMedication, jDoseClass));

        if (medParent.get() != nullptr) {
            env->CallVoidMethod(medParent.get(), setChild, jMedicationInstance);
            env->CallVoidMethod(jMedicationInstance, setParent, medParent.get());
        }
    }

    if (!med.doses.empty()) {
        LocalRef jDoses(env, env->NewObjectArray(med.doses.size(), jDoseClass, NULL));

        for (int i = 0; i < med.doses.size(); i++) {
            string msg = "In dose loop. Index: ";\

            msg += to_string(i).c_str();

            __android_log_write(ANDROID_LOG_INFO, nullptr, msg.c_str());

            LocalRef jDose(env, doseToJavaConverter(med.doses.at(i), env, jMedicationInstance, jDoseClass));

            env->SetObjectArrayElement(jDoses.get<jobjectArray>(), i, jDose.get());
        }

        env->CallVoidMethod(jMedicationInstance, setDoses, jDoses.get());
    }

    return jMedicationInstance;
}

Notification javaNotificationToNativeNotificationMapper(jobject notification, JNIEnv *env) {
    LocalRef jNotificationClass(env, env->GetObjectClass(notification));
    jmethodID getId = env->GetMethodID(jNotificationClass.get<jclass>(), "getId", "()J");
    jmethodID getMedId = env->GetMethodID(jNotificationClass.get<jclass>(), "getMedId", "()J");
    jmethodID getNotificationId = env->GetMethodID(jNotificationClass.get<jclass>(), "getNotificationId", "()J");
    jmethodID getDoseTime = env->GetMethodID(jNotificationClass.get<jclass>(), "getDoseTimeString",
                                             "()Ljava/lang/String;");

    std::string timeString;
    {
        LocalRef doseTimeRef(env, env->CallObjectMethod(notification, getDoseTime));
        JniString doseTime(env, (jstring) doseTimeRef.get());
        timeString = doseTime.toStdString();
    }

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

    LocalRef doseTimeStr(env, env->NewStringUTF(notification.doseTime.c_str()));

    return env->NewObject(
            notificationClass,
            constructor,
            notification.id,
            notification.medId,
            notification.notificationId,
            doseTimeStr.get()
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
    JniString dbStr(env, database_name);
    std::string db = dbStr.toStdString();

    JniString exportDirStr(env, export_directory);
    std::string exportDir = exportDirStr.toStdString();

    std::vector<std::string> ignoredTbls;
    int len = env->GetArrayLength(ignoredTables);

    for (int i = 0; i < len; i++) {
        LocalRef element(env, env->GetObjectArrayElement(ignoredTables, i));
        JniString str(env, (jstring) element.get());

        ignoredTbls.push_back(str.toStdString());
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
    JniString dbPathStr(env, db_path);
    std::string dbPath = dbPathStr.toStdString();

    JniString fileContentsStr(env, file_contents);
    std::string fileContents = fileContentsStr.toStdString();

    std::vector<std::string> ignoredTbls;
    int len = env->GetArrayLength(ignored_tables);
    auto success = JNI_TRUE;

    for (int i = 0; i < len; i++) {
        LocalRef element(env, env->GetObjectArrayElement(ignored_tables, i));
        JniString str(env, (jstring) element.get());

        ignoredTbls.push_back(str.toStdString());
    }

    DatabaseController controller(dbPath);

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
    JniString dbPathStr(env, db_path);
    std::string dbPath = dbPathStr.toStdString();

    try {
        DatabaseController dbController(dbPath);
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
    JniString pathStr(env, db_path);
    std::string db = pathStr.toStdString();
    (void) version;

    try {
        DatabaseController dbController(db);
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
    JniString pathStr(env, db_path);
    std::string path = pathStr.toStdString();

    JniString tblStr(env, table);
    std::string tbl = tblStr.toStdString();

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
    JniString pathStr(env, db_path);
    std::string path = pathStr.toStdString();

    JniString tblStr(env, table);
    std::string tbl = tblStr.toStdString();

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

    JniString pathStr(env, db_path);
    std::string path = pathStr.toStdString();

    JniString tblStr(env, table);
    std::string tbl = tblStr.toStdString();

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
    JniString pathStr(env, db_path);
    std::string path = pathStr.toStdString();

    DatabaseController dbController(path);

    try {
        return medicationToJavaConverter(
                dbController.getMedicationHistory(med_id),
                env,
                medClass,
                doseClass
        );
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
    JniString pathStr(env, db_path);
    std::string path = pathStr.toStdString();

    DatabaseController dbController(path);

    try {
        return medicationToJavaConverter(
                dbController.getMedication(med_id),
                env,
                medClass,
                doseClass
        );
    } catch (exception e) {
        __android_log_write(ANDROID_LOG_ERROR, nullptr, "Error retrieving history");

        return nullptr;
    }
}

extern "C"
JNIEXPORT jobjectArray JNICALL
    Java_projects_medicationtracker_Helpers_NativeDbHelper_getAllMedications(
            JNIEnv *env,
            jobject thiz,
            jstring db_path,
            jclass medClass,
            jclass doseClass
) {
    JniString pathStr(env, db_path);
    std::string path = pathStr.toStdString();

    DatabaseController dbController(path);
    std::vector<Medication> meds;

    try {
        meds = dbController.fetchMedications();
    } catch (exception e) {
        __android_log_write(
                ANDROID_LOG_ERROR,
                "getAllMedications",
                e.what()
        );

        meds = {};
    }

    if (meds.size() == 0) {
        return nullptr;
    }

    jobjectArray jMeds = env->NewObjectArray(
            meds.size(),
            medClass,
            nullptr
    );

    for (int i = 0; i < meds.size(); i++) {
        LocalRef jmed(env, medicationToJavaConverter(meds.at(i), env, medClass, doseClass));

        env->SetObjectArrayElement(jMeds, i, jmed.get());
    }

    return jMeds;
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
    LocalRef pairClass(env, env->FindClass("android/util/Pair"));
    JniString dbPathStr(env, db_path);
    std::string dbPath = dbPathStr.toStdString();

    JniString exportPathStr(env, export_path);
    std::string path = exportPathStr.toStdString();

    DatabaseController controller(dbPath);

    std::map<std::string, std::vector<std::string>> exportData;

    _jfieldID *const firstFieldId = env->GetFieldID(pairClass.get<jclass>(), "first", "Ljava/lang/Object;");
    _jfieldID *const secondFieldId = env->GetFieldID(pairClass.get<jclass>(), "second", "Ljava/lang/Object;");

    for (int i = 0; i < env->GetArrayLength(data); i++) {
        LocalRef element(env, env->GetObjectArrayElement(data, i));
        LocalRef keyRef(env, env->GetObjectField(element.get(), firstFieldId));
        LocalRef valuesRef(env, env->GetObjectField(element.get(), secondFieldId));

        JniString key(env, (jstring) keyRef.get());
        std::vector<std::string> vals;

        for (int j = 0; j < env->GetArrayLength((jobjectArray) valuesRef.get()); j++) {
            LocalRef valueRef(env, env->GetObjectArrayElement((jobjectArray) valuesRef.get(), j));
            JniString val(env, (jstring) valueRef.get());

            vals.push_back(val.toStdString());
        }

        exportData.insert({key.toStdString(), vals});
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
    JniString dbPathStr(env, db_path);
    std::string dbPath = dbPathStr.toStdString();

    DatabaseController controller(dbPath);
    JniString doseTimeStr(env, dose_time);
    std::string doseTime = doseTimeStr.toStdString();

    Dose *dose = controller.findDose(medication_id, doseTime);

    if (dose == nullptr) {
        dose = new Dose();
    }

    LocalRef jDose(env, doseToJavaConverter(*dose, env, medication, jDoseClass));

    delete dose;

    return jDose.release();
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
    JniString dbPathStr(env, db_path);
    std::string dbPath = dbPathStr.toStdString();

    DatabaseController controller(dbPath);

    Dose *d = nullptr;

    try {
        d = controller.getDoseById(dose_id);
    } catch (exception &e) {
        delete d;

        return nullptr;
    }

    if (d == nullptr) {
        return nullptr;
    }

    LocalRef jDose(env, doseToJavaConverter(*d, env, medication, jDoseClass));

    delete d;

    return jDose.release();
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_projects_medicationtracker_Helpers_NativeDbHelper_updateDose(
        JNIEnv *env,
        jobject thiz,
        jstring db_path,
        jobject dose
) {
    JniString dbPathStr(env, db_path);
    std::string dbPath = dbPathStr.toStdString();

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
    JniString dbPathStr(env, db_path);
    std::string dbPath = dbPathStr.toStdString();

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
    JniString dbPathStr(env, db_path);
    std::string dbPath = dbPathStr.toStdString();

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
    JniString dbPathStr(env, db_path);
    std::string dbPath = dbPathStr.toStdString();

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
        LocalRef noti(env, nativeNotificationToJavaNotificationConverter(
                env,
                notifications.at(i),
                jNotificationClass
        ));

        env->SetObjectArrayElement(
                jNotifications,
                i,
                noti.get()
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
    JniString dbPathStr(env, db_path);
    std::string dbPath = dbPathStr.toStdString();

    JniString scheduledTimeStr(env, scheduled_time);
    JniString takenTimeStr(env, taken_time);
    std::string scheduledTime = scheduledTimeStr.toStdString();
    std::string takenTime = takenTimeStr.toStdString();
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
    JniString dbPathStr(env, db_path);
    std::string dbPath = dbPathStr.toStdString();

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
    JniString dbPathStr(env, db_path);
    std::string dbPath = dbPathStr.toStdString();

    DatabaseController controller(dbPath);

    try {
        controller.deleteNotificationsByMedicationId(medicationid);
    } catch (exception& e) {
        auto err = "Unable to delete notifications for medication: " + to_string(medicationid);

        __android_log_write(ANDROID_LOG_ERROR, "SETTINGS UPDATE", err.c_str());
    }
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_projects_medicationtracker_Helpers_NativeDbHelper_isMedicationActive(
        JNIEnv *env,
        jobject thiz,
        jstring db_path,
        jlong medication_id
) {
    JniString dbPathStr(env, db_path);
    std::string dbPath = dbPathStr.toStdString();

    DatabaseController controller(dbPath);
    return controller.isMedicationActive(medication_id) ? JNI_TRUE : JNI_FALSE;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_projects_medicationtracker_Helpers_NativeDbHelper_isParentMedication(
        JNIEnv *env,
        jobject thiz,
        jstring db_path,
        jlong medication_id
) {
    JniString dbPathStr(env, db_path);
    std::string dbPath = dbPathStr.toStdString();

    DatabaseController controller(dbPath);
    return controller.isParentMedication(medication_id) ? JNI_TRUE : JNI_FALSE;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_projects_medicationtracker_Helpers_NativeDbHelper_getSettings(
        JNIEnv *env,
        jobject thiz,
        jstring db_path
) {
    JniString dbPathStr(env, db_path);
    std::string dbPath = dbPathStr.toStdString();

    DatabaseController controller(dbPath);
    std::unique_ptr<Table> settings = controller.getSettings();
    jobject jSettings;
    LocalRef jBundle(env, env->FindClass("android/os/Bundle"));

    jmethodID bundleConstructor = env->GetMethodID(
        jBundle.get<jclass>(), "<init>", "()V"
    );
    jmethodID putString = env->GetMethodID(
            jBundle.get<jclass>(), "putString", "(Ljava/lang/String;Ljava/lang/String;)V"
    );
    jmethodID putInt = env->GetMethodID(
            jBundle.get<jclass>(), "putInt", "(Ljava/lang/String;I)V"
    );
    jmethodID putBool = env->GetMethodID(
            jBundle.get<jclass>(), "putBoolean", "(Ljava/lang/String;Z)V"
    );

    jSettings = env->NewObject(jBundle.get<jclass>(), bundleConstructor);

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
        LocalRef keyStr(env, env->NewStringUTF(sKey.c_str()));
        LocalRef valueStr(env, env->NewStringUTF(settings->getItem(sKey).c_str()));

        env->CallVoidMethod(
                jSettings,
                putString,
                keyStr.get(),
                valueStr.get()
        );
    }

    for (const auto& iKey : intKeys) {
        LocalRef keyStr(env, env->NewStringUTF(iKey.c_str()));

        env->CallVoidMethod(
                jSettings,
                putInt,
                keyStr.get(),
                stoi(settings->getItem(iKey))
        );
    }

    for (const auto& bKey : boolKeys) {
        LocalRef keyStr(env, env->NewStringUTF(bKey.c_str()));

        env->CallVoidMethod(
                jSettings,
                putBool,
                keyStr.get(),
                settings->getItem(bKey) == "1"
        );
    }

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
    JniString pathStr(env, test_path);

    if (pathStr.c_str() == nullptr) {
        return JNI_FALSE;
    }

    std::string path = pathStr.toStdString();

    if (stat(path.c_str(), &fileStat) != 0) {
        return errno == ENOENT ? JNI_TRUE : JNI_FALSE;
    }

    return access(path.c_str(), W_OK) == 0 ? JNI_TRUE : JNI_FALSE;
}