//
// Created by adam on 3/5/24.
//

#ifndef MEDICATIONTRACKER_DOSE_H
#define MEDICATIONTRACKER_DOSE_H

#include <string>

struct Dose {
    long id;
    long medicationId;
    bool taken;
    std::string doseTime;
    std::string timeTaken;

    Dose();
    Dose(long id, long medicationId, bool taken, std::string doseTime, std::string timeTaken);
};


#endif //MEDICATIONTRACKER_DOSE_H
