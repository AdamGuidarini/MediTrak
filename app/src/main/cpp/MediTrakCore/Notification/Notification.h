//
// Created by adam on 8/12/24.
//

#ifndef MEDICATIONTRACKER_NOTIFICATION_H
#define MEDICATIONTRACKER_NOTIFICATION_H

#include <string>

struct Notification {
    long id;
    long medId;
    long notificationId;
    std::string doseTime;

    Notification(long id, long medId, long notificationId, std::string doseTime);
};


#endif //MEDICATIONTRACKER_NOTIFICATION_H
