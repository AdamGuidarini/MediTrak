add_library(DatabaseController STATIC DatabaseController.h DatabaseController.cpp)
target_link_libraries(DatabaseController PUBLIC DbManager Dose Medication Notification)
target_include_directories(DatabaseController PUBLIC "${CMAKE_CURRENT_SOURCE_DIR}")
