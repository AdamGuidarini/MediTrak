add_library(Dose STATIC Dose.h Dose.cpp)
target_include_directories(Dose PUBLIC "${CMAKE_CURRENT_SOURCE_DIR}")
