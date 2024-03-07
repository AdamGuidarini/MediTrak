add_library(Medication Medication.h Medication.cpp)
target_link_libraries(Medication Dose)
target_include_directories(Medication PUBLIC "${CMAKE_CURRENT_SOURCE_DIR}")