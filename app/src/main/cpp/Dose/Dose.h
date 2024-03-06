//
// Created by adam on 3/5/24.
//

#ifndef MEDICATIONTRACKER_DOSE_H
#define MEDICATIONTRACKER_DOSE_H

#include <cstring>

struct Dose {
    long id;
    long medicationId;
    bool taken;
    char* doseTime;

    Dose();
    Dose(long id, long medicationId, bool taken, char* doseTime);
};


#endif //MEDICATIONTRACKER_DOSE_H
