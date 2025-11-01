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
    quantity = -1;
    endDate = "";
    notifyWhenRemainingAmount = -1;
}

Medication::Medication(
        string medicationName,
        string patientName,
        string dosageUnit,
        vector<string> times,
        string startDate,
        long id,
        float dosage,
        int frequency,
        bool active,
        string alias,
        int quantity,
        string endDate,
        int notifyWhenRemainingAmount
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
    this->quantity = quantity;
    this->endDate = endDate;
    this->notifyWhenRemainingAmount = notifyWhenRemainingAmount;
}
