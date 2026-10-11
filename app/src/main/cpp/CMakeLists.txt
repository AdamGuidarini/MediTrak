cmake_minimum_required(VERSION 3.22.1)

project("medicationtracker" CXX C)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

set(SQLITE3_ROOT_PATH "${CMAKE_CURRENT_SOURCE_DIR}/sqlite3")

file(READ "../../../../sqliteconfig.json" CONFIG)

string(JSON ZIP_URL GET ${CONFIG} "sqlite3_zip")
string(JSON TARGET_CHECKSUM GET ${CONFIG} "sqlite3_sha3_256")
string(REGEX REPLACE ".*/(.*)\\.zip" "\\1" AMALGA "${ZIP_URL}")

set(SQLITE3_PATH "${SQLITE3_ROOT_PATH}/${AMALGA}")

if (NOT EXISTS ${SQLITE3_PATH})
    if (NOT EXISTS ${SQLITE3_ROOT_PATH})
        file(MAKE_DIRECTORY ${SQLITE3_ROOT_PATH})
    endif ()

    file(DOWNLOAD ${ZIP_URL} "${SQLITE3_ROOT_PATH}/sqlite3.zip" TIMEOUT 5000)

    file(SHA3_256 "${SQLITE3_ROOT_PATH}/sqlite3.zip" REAL_CHECKSUM)

    if (NOT "${REAL_CHECKSUM}" STREQUAL "${TARGET_CHECKSUM}")
        message(SEND_ERROR "Received SQLITE Lib does not match expected result")
    endif ()

    file(
            ARCHIVE_EXTRACT
            INPUT "${SQLITE3_ROOT_PATH}/sqlite3.zip"
            DESTINATION ${SQLITE3_ROOT_PATH}
    )
endif ()

set(JSON_ROOT_PATH "${CMAKE_CURRENT_SOURCE_DIR}/json")
set(JSON_HPP_URL "https://raw.githubusercontent.com/nlohmann/json/v3.11.3/single_include/nlohmann/json.hpp")
set(JSON_HPP_PATH "${JSON_ROOT_PATH}/nlohmann/json.hpp")

if (NOT EXISTS ${JSON_HPP_PATH})
    file(MAKE_DIRECTORY "${JSON_ROOT_PATH}/nlohmann")
    file(DOWNLOAD ${JSON_HPP_URL} ${JSON_HPP_PATH} TIMEOUT 120)
endif()

# Header-only target for nlohmann/json
add_library(json INTERFACE)
target_include_directories(json INTERFACE ${JSON_ROOT_PATH})

# Build SQLite3 as a static library
add_library(sqlite3 STATIC ${SQLITE3_PATH}/sqlite3.c)
target_include_directories(sqlite3 PUBLIC ${SQLITE3_PATH})
target_compile_definitions(sqlite3 PRIVATE SQLITE_ENABLE_JSON1)

# Add subdirectories
add_subdirectory(Sqlite3Database)
add_subdirectory(MediTrakCore)

# Main JNI shared library
add_library(medicationtracker SHARED medicationtracker.cpp)

target_include_directories(medicationtracker PRIVATE
        ${CMAKE_CURRENT_SOURCE_DIR}/MediTrakCore
        ${CMAKE_CURRENT_SOURCE_DIR}/Sqlite3Database
)

find_library(log-lib log)

target_link_libraries(
        medicationtracker
        PRIVATE
        android
        DatabaseController
        ${log-lib}
)
