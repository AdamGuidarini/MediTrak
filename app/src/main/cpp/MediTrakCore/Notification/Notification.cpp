//
// Created by adam on 8/12/24.
//

#include "Notification.h"
#include <utility>

Notification::Notification(long id, long medId, std::string doseTime) {
    this->id = id;
    this->medId = medId;
    this->doseTime = std::move(doseTime);
}
