add_library(Table SHARED Table.h Table.cpp)
target_link_libraries(Table sqlite3)
target_include_directories(Table PUBLIC "${CMAKE_CURRENT_SOURCE_DIR}")