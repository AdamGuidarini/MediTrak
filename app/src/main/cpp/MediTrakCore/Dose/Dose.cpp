//
// Created by adam on 3/5/24.
//

#include "Dose.h"
#include <string>

Dose::Dose() {
    id = -1;
    medicationId = -1;
    taken = false;
    doseTime = {};
}

Dose::Dose(long id, long medicationId, bool taken, std::string doseTime, std::string timeTaken) {
    this->id = id;
    this->medicationId = medicationId;
    this->taken = taken;
    this->doseTime = doseTime;
    this->timeTaken = timeTaken;
}
