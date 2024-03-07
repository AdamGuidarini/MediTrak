add_library(DatabaseController DatabaseController.h DatabaseController.cpp)
target_link_libraries(DatabaseController DbManager Dose Medication)
target_include_directories(DatabaseController PUBLIC "${CMAKE_CURRENT_SOURCE_DIR}")
