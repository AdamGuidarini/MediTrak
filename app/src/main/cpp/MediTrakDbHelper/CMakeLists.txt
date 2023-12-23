add_library(MediTrakDbHelper MediTrakDbHelper.h MediTrakDbHelper.cpp)
target_link_libraries(MediTrakDbHelper DbManager)
target_include_directories(MediTrakDbHelper PUBLIC "${CMAKE_CURRENT_SOURCE_DIR}")
