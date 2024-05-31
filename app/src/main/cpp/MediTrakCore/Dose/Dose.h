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
    int overrideDoseAmount;
    std::string overrideDoseUnit;

    Dose();
    Dose(long id, long medicationId, bool taken, std::string doseTime, std::string timeTaken, int overrideDose = -1, std::string overrideDoseUnit = "");
};


#endif //MEDICATIONTRACKER_DOSE_H
