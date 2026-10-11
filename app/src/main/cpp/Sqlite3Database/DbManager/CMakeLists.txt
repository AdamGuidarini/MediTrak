add_library(DbManager STATIC DbManager.h DbManager.cpp)
target_link_libraries(DbManager PUBLIC Table sqlite3 json)
target_include_directories(DbManager PUBLIC "${CMAKE_CURRENT_SOURCE_DIR}")
