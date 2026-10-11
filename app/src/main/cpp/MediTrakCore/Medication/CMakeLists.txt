add_library(Medication STATIC Medication.h Medication.cpp)
target_link_libraries(Medication PUBLIC Dose)
target_include_directories(Medication PUBLIC "${CMAKE_CURRENT_SOURCE_DIR}")
