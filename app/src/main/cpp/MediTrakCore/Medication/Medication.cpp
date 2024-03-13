//
// Created by adam on 3/5/24.
//

#include "Medication.h"

Medication::Medication() {
    medicationName = "";
    dosageUnit = "";
    patientName = "";
    startDate = "";
    alias = "";
    times = {};
    id = -1;
    dosage = 0;
    frequency = 0;
    active = false;
}

Medication::~Medication() {
    deleteParents();
}

Medication::Medication(
        string medicationName,
        string patientName,
        string dosageUnit,
        vector<string> times,
        string startDate,
        long id,
        int dosage,
        int frequency,
        bool active,
        string alias
) {
    this->medicationName = medicationName;
    this->patientName = patientName;
    this->dosageUnit = dosageUnit;
    this->times = times;
    this->startDate = startDate;
    this->id = id;
    this->dosage = dosage;
    this->frequency = frequency;
    this->active = active;
    this->alias = alias;
}

void Medication::deleteParents() {
    if (parent == nullptr) {
        return;
    }

    if (parent->parent == nullptr) {
        delete parent;
    } else {
        Medication* temp = parent->parent;
        delete parent;

        parent = temp;

        deleteParents();
    }
}
