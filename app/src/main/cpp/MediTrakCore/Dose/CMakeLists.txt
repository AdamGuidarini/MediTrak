add_library(Dose Dose.h Dose.cpp)
target_link_libraries(Dose)
target_include_directories(Dose PUBLIC "${CMAKE_CURRENT_SOURCE_DIR}")