add_library(Table STATIC Table.h Table.cpp)
target_link_libraries(Table PUBLIC sqlite3)
target_include_directories(Table PUBLIC "${CMAKE_CURRENT_SOURCE_DIR}")
