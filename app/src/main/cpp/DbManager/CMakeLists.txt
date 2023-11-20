add_library(DbManager DbManager.h DbManager.cpp)
target_link_libraries(DbManager SqliteTable sqlite3)
target_include_directories(DbManager PUBLIC "${CMAKE_CURRENT_SOURCE_DIR}")