# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.22.1)

project("medicationtracker")
set(CMAKE_CXX_STANDARD 17)

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

    if (NOT ${REAL_CHECKSUM} MATCHES ${TARGET_CHECKSUM})
        message(SEND_ERROR "Received SQLITE Lib does not match expected result")
    endif ()

    file(
            ARCHIVE_EXTRACT
            INPUT "${SQLITE3_ROOT_PATH}/sqlite3.zip"
            DESTINATION ${SQLITE3_ROOT_PATH}
    )
endif ()

# Check if sqlite3.h exists after extraction
while (NOT EXISTS "${SQLITE3_PATH}/sqlite3.h")
    file(GLOB SQLITE3_H "${SQLITE3_ROOT_PATH}/*/*.h")
    if (SQLITE3_H)
        break()
    endif ()
    execute_process(
            COMMAND ${CMAKE_COMMAND} -E sleep 1
    )
endwhile ()

include_directories(${SQLITE3_PATH} MediTrakCore Sqlite3Database)

add_subdirectory(Sqlite3Database)
add_subdirectory(MediTrakCore)

add_library(medicationtracker SHARED medicationtracker.cpp)

add_library(sqlite3 SHARED ${SQLITE3_PATH}/sqlite3.c)

add_definitions(-DSQLITE_ENABLE_JSON1)

find_library(log-lib log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries(
        medicationtracker
        android
        sqlite3
        DatabaseController
        Medication
        Dose
        Notification
        DbManager
        ${log-lib}
)
