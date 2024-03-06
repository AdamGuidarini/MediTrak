//
// Created by adam on 3/5/24.
//

#include "Dose.h"

Dose::Dose() {
    id = -1;
    medicationId = -1;
    taken = false;
    doseTime = {};
}

Dose::Dose(long id, long medicationId, bool taken, char *doseTime) {

}
