add_library(DataExporter dataExporter.h dataExporter.cpp)
target_link_libraries(DataExporter DbManager)
target_include_directories(DataExplorer PUBLIC "${CMAKE_CURRENT_SOURCE_DIR}")