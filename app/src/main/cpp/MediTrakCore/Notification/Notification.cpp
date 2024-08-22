//
// Created by adam on 8/12/24.
//

#include "Notification.h"
#include <utility>

Notification::Notification(long id, long medId, long notificationId, std::string doseTime) {
    this->id = id;
    this->medId = medId;
    this->notificationId = notificationId;
    this->doseTime = std::move(doseTime);
}
