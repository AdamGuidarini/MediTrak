add_library(sqlite3 sqlite3.h sqlite3.c)
target_link_libraries(sqlite3)
target_include_directories(sqlite3 PUBLIC "${CMAKE_CURRENT_SOURCE_DIR}")