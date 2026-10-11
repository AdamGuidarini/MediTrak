add_library(Notification STATIC Notification.h Notification.cpp)
target_include_directories(Notification PUBLIC "${CMAKE_CURRENT_SOURCE_DIR}")
