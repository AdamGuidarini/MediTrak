include_directories(DbManager Table)

add_subdirectory(DbManager)
add_subdirectory(Table)

target_link_libraries(DbManager Table)