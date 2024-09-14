add_library(Notification Notification.h Notification.cpp)
target_link_libraries(Notification)
target_include_directories(Notification PUBLIC "${CMAKE_CURRENT_SOURCE_DIR}")