add_library(SqliteTable SqliteTable.h SqliteTable.cpp)
target_link_libraries(SqliteTable sqlite3)
target_include_directories(SqliteTable PUBLIC "${CMAKE_CURRENT_SOURCE_DIR}")