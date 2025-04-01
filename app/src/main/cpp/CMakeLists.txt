
# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.22.1)

# Declares and names the project.

project("medicationtracker")
set(CMAKE_CXX_STANDARD 17)

set(SQLITE3_ROOT_PATH "${CMAKE_CURRENT_SOURCE_DIR}/sqlite3")

file(READ "../../../../sqliteconfig.json" CONFIG)

string(JSON ZIP_URL GET ${CONFIG} "sqlite3_zip")
string(JSON TARGET_CHECKSUM GET ${CONFIG} "sqlite3_sha256")
string(REGEX REPLACE ".*/(.*)\\.zip" "\\1" AMALGA "${ZIP_URL}")

set(SQLITE3_PATH "${SQLITE3_ROOT_PATH}/${AMALGA}")

if (NOT EXISTS "${SQLITE3_ROOT_PATH}/sqlite3.zip")
    if (NOT EXISTS ${SQLITE3_ROOT_PATH})
        file(MAKE_DIRECTORY ${SQLITE3_ROOT_PATH})
    endif ()

    file(DOWNLOAD ${ZIP_URL} "${SQLITE3_ROOT_PATH}/sqlite3.zip" TIMEOUT 5000)

    file(SHA256 "${SQLITE3_ROOT_PATH}/sqlite3.zip" REAL_CHECKSUM)

    if (NOT ${REAL_CHECKSUM} MATCHES ${TARGET_CHECKSUM})
        message(SEND_ERROR "Received SQLITE Lib does not match expected result")
    endif ()

    file(
            ARCHIVE_EXTRACT
            INPUT "${SQLITE3_ROOT_PATH}/sqlite3.zip"
            DESTINATION ${SQLITE3_ROOT_PATH}
    )

    if (NOT EXISTS "${SQLITE3_PATH}/sqlite3.h")
        message(SEND_ERROR "Could not find extracted file")
    endif ()
endif ()

include_directories(${SQLITE3_PATH} MediTrakCore Sqlite3Database)

add_subdirectory(Sqlite3Database)
add_subdirectory(MediTrakCore)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library(medicationtracker SHARED medicationtracker.cpp)

add_library(sqlite3 SHARED ${SQLITE3_PATH}/sqlite3.c)

add_definitions( -DSQLITE_ENABLE_JSON1 )

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.
find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log
)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        medicationtracker
        android
        sqlite3
        DatabaseController
        Medication
        Dose
        Notification
        DbManager
        # Links the target library to the log library
        # included in the NDK.
        ${log-lib}
)
