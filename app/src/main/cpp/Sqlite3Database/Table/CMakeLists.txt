add_library(Table SHARED Table.h Table.cpp)
target_link_libraries(Table)
target_include_directories(Table PUBLIC "${CMAKE_CURRENT_SOURCE_DIR}")