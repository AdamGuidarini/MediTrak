add_library(DataExporter DataExporter.h DataExporter.cpp)
target_link_libraries(DataExporter DbManager)
target_include_directories(DataExplorer PUBLIC "${CMAKE_CURRENT_SOURCE_DIR}")